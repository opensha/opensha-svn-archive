package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.opensha.commons.eq.MagUtils;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.simulatedAnnealing.SerialSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.ProgressTrackingCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.utils.PaleoProbabilityModel;
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

	public static void main(String[] args) {
		// flags!
		String fileName = "CHARUnconstrainedRef1Iter";
		boolean writeMatrixZipFiles = false;
		boolean writeSolutionZipFile = true;
		
		InversionModels inversionModel = InversionModels.CHAR_UNCONSTRAINED;
		
		// fetch the rupture set
		InversionFaultSystemRupSet rupSet = null;
		double defaultAseis = 0.1;
		try {
//			rupSet = InversionFaultSystemRupSetFactory.forBranch(DeformationModels.GEOLOGIC_PLUS_ABM);
			LaughTestFilter filter = LaughTestFilter.getDefault();
			rupSet = InversionFaultSystemRupSetFactory.forBranch(filter, defaultAseis, inversionModel, FaultModels.FM3_1, DeformationModels.NEOKINEMA,
					ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.UNIFORM, TotalMag5Rate.RATE_8p7, MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3);
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
		if (D) System.out.println("Total Final (creep & subseismogenic rup reduced) Moment Rate = "+rupSet.getTotalReducedMomentRate());
		
		// get the inversion configuration
		InversionConfiguration config;
		config = InversionConfiguration.forModel(inversionModel, rupSet);
		config.updateRupSetInfoString(rupSet);
		
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
//		criteria = TimeCompletionCriteria.getInMinutes(1); 
//		criteria = TimeCompletionCriteria.getInSeconds(30); 
		// or use this to run until a set amount of iterations have been completed
		criteria = new IterationCompletionCriteria(1); 

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
			
			ThreadedSimulatedAnnealing tsa = new ThreadedSimulatedAnnealing(A, d, initial, relativeSmoothnessWt,
					A_ineq, d_ineq, minimumRuptureRates, numThreads, subCompetionCriteria);
			
			tsa.setRanges(gen.getRangeEndRows(), gen.getRangeNames());
			
			sa = tsa;
		} else {
			// serial simulated annealing
			sa = new SerialSimulatedAnnealing(A, d, initial, relativeSmoothnessWt, A_ineq, d_ineq);
		}
//		sa.setVariablePerturbationBasis(InversionConfiguration.getSmoothStartingSolution(rupSet,InversionConfiguration.getGR_Dist(rupSet, 1.0, 9.0)));
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
		
		//	Load in solution from file
//		SimpleFaultSystemSolution solution = SimpleFaultSystemSolution.fromZipFile(new File("/Users/pagem/Desktop/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarMomRed_050_VarMFDMod_100_VarDefaultAseis_0.2_sol.zip"));
		
		// Calculate total moment of solution
		double totalSolutionMoment = 0;
		for (int rup=0; rup<solution.getNumRuptures(); rup++) 
			totalSolutionMoment += solution.getRateForRup(rup)*MagUtils.magToMoment(solution.getMagForRup(rup));
		if (D) System.out.println("Total moment of solution = "+totalSolutionMoment);
		
		// Make plots
		if (D) System.out.print("\nMaking plots . . . ");
		long startTime = System.currentTimeMillis();
		solution.plotRuptureRates();
		solution.plotSlipRates();
		solution.plotPaleoObsAndPredPaleoEventRates(paleoRateConstraints);
		InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(solution);
		invSol.plotMFDs();
		try {
			CommandLineInversionRunner.writeMFDPlots(invSol, new File("/tmp"), rupSet.getLogicTreeBranch().buildFileName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		solution.plotMFDs(config.getMfdEqualityConstraints());
//		solution.plotMFDs(config.getMfdInequalityConstraints());
		long runTime = System.currentTimeMillis()-startTime;
		if (D) System.out.println("Done after "+ (runTime/1000.) +" seconds.");	
	}


}

