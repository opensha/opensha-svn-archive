package scratch.UCERF3.inversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.Region;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.simulatedAnnealing.SerialSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.ProgressTrackingCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF2_PaleoRateConstraintFetcher;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;
import cern.colt.matrix.tdouble.DoubleMatrix2D;


/**
 * This class runs the Grand Inversion.
 * 
 * TO DO:
 * 
 * 1) Wrap this in a GUI?
 * 
 * @author  Field, Page, Milner, & Powers
 *
 */

public class RunInversion {

	protected final static boolean D = true;  // for debugging
	
	public enum CoulombWeightType {
		MEAN_SIGMA,
		WEAKEST_LINK;
	}
	
	private double[] getCoulombWeights(int numRups, CoulombWeightType coulombWeight) {
		
		// TODO if we ever use this, we'll want to move it. don't leave it here!!!
		
		double[] weights = new double[numRups];
		String fileName;
		
		switch (coulombWeight) {
		case MEAN_SIGMA: // Use mean stress of all rupture connections
			fileName = "MeanSigma.txt";
			break;
		case WEAKEST_LINK: // Use stress of "weakest link" connection
			fileName = "WeakestLink.txt";
			break;
		default:
			throw new IllegalStateException("Unspecified Coulomb Weight Type");
		}
		
	
		FileReader inputFile;
		try {
			BufferedReader in = new BufferedReader(
					UCERF3_DataUtils.getReader("coulomb", fileName));
			String s = in.readLine();
			int i = 0;
			while(s!=null)
			{
			    weights[i] = 1.0 / Double.parseDouble(s);
			    s = in.readLine();
			    i++;
			}
			in.close();
			if (i != numRups) {
				throw new Exception("Number of ruptures does not match number of Coulomb weights");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return weights;
	
		
	}
	
	private static InversionConfiguration buildCustomConfiguration(FaultSystemRupSet rupSet) {
		boolean weightSlipRates = true; // If true, slip rate misfit is % difference for each section (recommended since it helps fit slow-moving faults).  If false, misfit is absolute difference.
		double relativePaleoRateWt = 1.0;  // weight of paleo-rate constraint relative to slip-rate constraint (recommended: 1.0 if weightSlipRates=true, 0.01 otherwise)
		double relativeMagnitudeEqualityConstraintWt = 0;  // weight of magnitude-distribution EQUALITY constraint relative to slip-rate constraint (recommended:  1000 if weightSlipRates=true, 10 otherwise)
		double relativeMagnitudeInequalityConstraintWt = 1000;  // weight of magnitude-distribution INEQUALITY constraint relative to slip-rate constraint (recommended:  1000 if weighted per bin -- this is hard-coded in)
		double relativeParticipationSmoothnessConstraintWt = 0; // weight of participation MFD smoothness weight - applied on subsection basis (recommended:  1000)
		double participationConstraintMagBinSize = 0.1; // magnitude-bin size for above constraint
		double relativeRupRateConstraintWt = 0;  // weight of rupture rate constraint (recommended strong weight: 5.0, weak weight: 0.1; 100X those weights if weightSlipRates=true) - can be UCERF2 rates or Smooth G-R rates
		double relativeMinimizationConstraintWt = 0; // weight of rupture-rate minimization constraint weights relative to slip-rate constraint (recommended: 10,000)
		double relativeSmoothnessWt = 0; // weight of entropy-maximization constraint (should smooth rupture rates) (recommended: 10000)
		double relativeMomentConstraintWt = 0;
		
		double[] aPrioriRupConstraint;
		// Use UCERF2 Solution 
//		aPrioriRupConstraint = UCERF2Solution;
		// Or use smooth starting solution with target MFD:
//		Region region = new CaliforniaRegions.RELM_NOCAL(); UCERF2_MFD_ConstraintFetcher UCERF2Constraints = new UCERF2_MFD_ConstraintFetcher(region); aPrioriRupConstraint = getSmoothStartingSolution(faultSystemRupSet, UCERF2Constraints.getTargetMinusBackgroundMFD());
		aPrioriRupConstraint = InversionConfiguration.getSmoothStartingSolution(
				rupSet, InversionConfiguration.getGR_Dist(rupSet, 1.0, 8.3));
		double[] initialRupModel = aPrioriRupConstraint;
		// or all zeros
//		for (int r=0; r<faultSystemRupSet.getNumRuptures(); r++) initialRupModel[r]=0;
		
		double minimumRuptureRateFraction = 0.01;
		double[] waterlevelRateBasis = initialRupModel;
		
		UCERF2_MFD_ConstraintFetcher ucerf2Constraints = new UCERF2_MFD_ConstraintFetcher();
		Region noCal = new CaliforniaRegions.RELM_NOCAL(); noCal.setName("Northern CA");
		Region soCal = new CaliforniaRegions.RELM_SOCAL(); soCal.setName("Southern CA");
		Region entire_region;
		if (rupSet.getDeformationModel() == DeformationModels.UCERF2_NCAL
				|| rupSet.getDeformationModel() == DeformationModels.UCERF2_BAYAREA) {
			entire_region = noCal;
		} else {
			// TODO should this be Testing or Collection? currently using TESTING because it's what
			// Ned uses lots of places
			entire_region = new CaliforniaRegions.RELM_TESTING();
		}
		
		// MFD constraints
		ArrayList<MFD_InversionConstraint> mfdEqualityConstraints = new ArrayList<MFD_InversionConstraint>();
		// add MFD constraint for whole region
//		ucerf2Constraints.setRegion(region);
//		mfdEqualityConstraints.add(UCERF2Constraints.getTargetMinusBackgrMFD_Constraint());
		// UCERF2 MFD constraints for subregions - 1-degree boxes
//		mfdEqualityConstraints.addAll(getGriddedConstraints(ucerf2Constraints, entire_region, 1d, 1d));
		
		ArrayList<MFD_InversionConstraint> mfdInequalityConstraints = new ArrayList<MFD_InversionConstraint>();
//		// add MFD constraint for the entire region
//		ucerf2Constraints.setRegion(entire_region);
		// add MFD constraint for Northern CA
		ucerf2Constraints.setRegion(noCal);
		mfdInequalityConstraints.add(ucerf2Constraints.getTargetMFDConstraint());
		// add MFD constraint for Southern CA
		if (entire_region != noCal) {
			// don't add so cal if we're just doing a no cal inversion
			ucerf2Constraints.setRegion(soCal);
			mfdInequalityConstraints.add(ucerf2Constraints.getTargetMFDConstraint());
		}
		
		return new InversionConfiguration(weightSlipRates, relativePaleoRateWt,
				relativeMagnitudeEqualityConstraintWt, relativeMagnitudeInequalityConstraintWt,
				relativeRupRateConstraintWt, relativeParticipationSmoothnessConstraintWt,
				participationConstraintMagBinSize, relativeMinimizationConstraintWt, relativeMomentConstraintWt,
				aPrioriRupConstraint, initialRupModel, waterlevelRateBasis, relativeSmoothnessWt,
				mfdEqualityConstraints, mfdInequalityConstraints, minimumRuptureRateFraction);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// flags!
		String fileName = "GEOLOGICPLUSABM_MFDReduced_FitMoment_30MIN";
		boolean writeMatrixZipFiles = false;
		boolean writeSolutionZipFile = true;
		
		InversionModels inversionModel = InversionModels.CHAR;
		
		// fetch the rupture set
		FaultSystemRupSet rupSet = null;
		try {
			rupSet = InversionFaultSystemRupSetFactory.forBranch(DeformationModels.GEOLOGIC_PLUS_ABM);
//			rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.AVE_UCERF2,
//																	AveSlipForRupModels.AVE_UCERF2, SlipAlongRuptureModels.UNIFORM, inversionModel);
//			rupSet = InversionFaultSystemRupSetFactory.cachedForBranch(DeformationModels.UCERF2_ALL);  // CAREFUL USING THIS - WILL ALWAYS RUN CHAR BRANCH momentRateReduction
			// or you can load one for yourself!
//			rupSet = SimpleFaultSystemRupSet.fromFile(new File(""));

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		if (D) System.out.println("Total Orig (creep reduced) Moment Rate = "+rupSet.getTotalOrigMomentRate());
		if (D) System.out.println("Total Final (creep & subseismogenic rup reduced) Moment Rate = "+rupSet.getTotalSubseismogenicReducedMomentRate());
		
		// get the inversion configuration
		InversionConfiguration config;
		// this will get it for the GR branch
		config = InversionConfiguration.forModel(inversionModel, rupSet);
		// this can be used for testing other inversions
//		config = buildCustomConfiguration(rupSet);
		
		File precomputedDataDir = UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR;
		
		// get the paleo rate constraints
		List<PaleoRateConstraint> paleoRateConstraints = null;
		try {	
			if (rupSet.getFaultModel() == FaultModels.FM2_1) 
				paleoRateConstraints = UCERF2_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
			else
				paleoRateConstraints = UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
		} catch (IOException e1) {
			e1.printStackTrace();
			// this is a critical error, need to exit
			System.exit(1);
		}
		
		// get the improbability constraints
		double[] improbabilityConstraint = null; // null for now
//		improbabilityConstraint = getCoulombWeights(faultSystemRupSet.getNumRuptures(), CoulombWeightType.MEAN_SIGMA, precomputedDataDir);
		
		// paleo probability model
		PaleoProbabilityModel paleoProbabilityModel = null;
		try {
			paleoProbabilityModel = InversionInputGenerator.loadDefaultPaleoProbabilityModel();
		} catch (IOException e) {
			e.printStackTrace();
			// exit
			System.exit(1);
		}
		
		// create the input generator
		InversionInputGenerator gen = new InversionInputGenerator(rupSet, config, paleoRateConstraints,
				improbabilityConstraint, paleoProbabilityModel);
		
		// generate the inputs
		gen.generateInputs();
		// optionally we can specify the class we want to use for the A matrix:
//		gen.generateInputs(SparseDoubleMatrix2D.class);
		
		// write solution to disk (optional)
		if (writeMatrixZipFiles) {
			try {
				gen.writeZipFile(new File(precomputedDataDir, fileName+"_inputs.zip"), precomputedDataDir, false);
			} catch (IOException e) {
				// a failure here is actually not the end of the world. just print the trace and move on
				e.printStackTrace();
			}
		}
		
		// column compress it for fast annealing!
		gen.columnCompress();
		
		// fetch matrices
		DoubleMatrix2D A = gen.getA();
		double[] d = gen.getD();
		DoubleMatrix2D A_ineq = gen.getA_ineq();
		double[] d_ineq = gen.getD_ineq();
		double[] initial = gen.getInitial();
		double[] minimumRuptureRates = gen.getMinimumRuptureRates();
		
		// now lets the run the inversion!
		CompletionCriteria criteria;
		// use one of these to run it for a set amount of time:
//		criteria = TimeCompletionCriteria.getInHours(1); 
		criteria = TimeCompletionCriteria.getInMinutes(30); 
//		criteria = TimeCompletionCriteria.getInSeconds(30); 
		// or use this to run until a set amount of iterations have been completed
//		criteria = new IterationCompletionCriteria(1); 

		SimulatedAnnealing sa;
		double relativeSmoothnessWt = config.getRelativeSmoothnessWt();
		boolean threading = true;
			
		if (threading) {
			// Bring up window to track progress
			criteria = new ProgressTrackingCompletionCriteria(criteria, 0.25);
			
			// this will use all available processors
			int numThreads = Runtime.getRuntime().availableProcessors();
			
			// this is the "sub completion criteria" - the amount of time (or iterations) between synchronization
			CompletionCriteria subCompetionCriteria = TimeCompletionCriteria.getInSeconds(1); // 1 second;
			
			sa = new ThreadedSimulatedAnnealing(A, d, initial, relativeSmoothnessWt,
					A_ineq, d_ineq, minimumRuptureRates, numThreads, subCompetionCriteria);
		} else {
			// serial simulated annealing
			sa = new SerialSimulatedAnnealing(A, d, initial, relativeSmoothnessWt, A_ineq, d_ineq);
		}
		// actually do the annealing
		sa.iterate(criteria);
		
		// now assemble the solution
		double[] solution_raw = sa.getBestSolution();
		
		// adjust for minimum rates if applicable
		double[] solution_adjusted = gen.adjustSolutionForMinimumRates(solution_raw);
		SimpleFaultSystemSolution solution = new SimpleFaultSystemSolution(rupSet, solution_adjusted);
		
		// lets save this solution...we just worked so hard for it, after all! (optional)
		if (writeSolutionZipFile) {
			try {
				solution.toZipFile(new File(precomputedDataDir, fileName+"_solution.zip"));
			} catch (IOException e) {
				// a failure here is OK. who needs a solution anyway?
				e.printStackTrace();
			}
		}
		
		// Calculate total moment of solution
		double totalSolutionMoment = 0;
		for (int rup=0; rup<solution.getNumRuptures(); rup++) 
			totalSolutionMoment += solution.getRateForRup(rup)*MagUtils.magToMoment(solution.getMagForRup(rup));
		if (D) System.out.println("Total moment of solution = "+totalSolutionMoment);
		
		// finally assemble a solution and make plots
		if (D) System.out.print("\nMaking plots . . . ");
		long startTime = System.currentTimeMillis();
		solution.plotRuptureRates();
		solution.plotSlipRates();
		solution.plotPaleoObsAndPredPaleoEventRates(paleoRateConstraints);
		solution.plotMFDs(config.getMfdEqualityConstraints());
		solution.plotMFDs(config.getMfdInequalityConstraints());
		long runTime = System.currentTimeMillis()-startTime;
		if (D) System.out.println("Done after "+ (runTime/1000.) +" seconds.");
	}


}

