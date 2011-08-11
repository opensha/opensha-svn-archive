package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.UCERF2_PaleoSegRateData;


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
	
	FaultSystemRupSet faultSystemRupSet;
	private InversionFaultSystemSolution inversion;

	public RunInversion() {
		this(new File("dev/scratch/UCERF3/preComputedData/"));
	}
	
	private static InversionFaultSystemRupSet buildRupSet(File precomputedDataDir) {
		// the InversionFaultSystemRupSet parameters
		double maxJumpDist = 5.0;
		double maxAzimuthChange = 45;
		double maxTotAzimuthChange = 90;
		double maxRakeDiff = 90;
		int minNumSectInRup = 2;
		double moRateReduction = 0.1;
		ArrayList<MagAreaRelationship> magAreaRelList = new ArrayList<MagAreaRelationship>();
		magAreaRelList.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelList.add(new HanksBakun2002_MagAreaRel());
		
		// Instantiate the FaultSystemRupSet
		long startTime = System.currentTimeMillis();
		InversionFaultSystemRupSet invFaultSystemRupSet = new InversionFaultSystemRupSet(DeformationModelFetcher.DefModName.UCERF2_NCAL,
				maxJumpDist,maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, minNumSectInRup, magAreaRelList, 
				moRateReduction,  InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL , precomputedDataDir);
		long runTime = System.currentTimeMillis()-startTime;
		System.out.println("\nFaultSystemRupSet instantiation took " + (runTime/1000) + " seconds");
		
		// These lines make files like those sent to Tom Parsons on April 19th 
		// for the DefModName.UCERF2_NCAL case (see email that day).  The results are 
		// no longer the same due to Morgan taking the creeping section out and a change 
		// in the header for the sections file (and maybe other changes)
//		faultSystemRupSet.writeSectionsToFile("sectionsForTom041911");
//		faultSystemRupSet.writeRupsToFiles("rupturesForTom041911");

		
		// plot the mag histogram
		invFaultSystemRupSet.plotMagHistogram();
		
		// save to XML
		if (D) System.out.print("Saving RupSet to XML...");
		File xmlOut = new File(precomputedDataDir.getAbsolutePath()+File.separator+"rupSet.xml");
		try {
			new SimpleFaultSystemRupSet(invFaultSystemRupSet).toFile(xmlOut);
			if (D) System.out.println("DONE");
		} catch (IOException e) {
			System.out.println("IOException saving Rup Set to XML!");
			e.printStackTrace();
		}
		
		return invFaultSystemRupSet;
	}
	
	public RunInversion(File precomputedDataDir) {
		this(buildRupSet(precomputedDataDir), precomputedDataDir);
	}
	
	public RunInversion(FaultSystemRupSet faultSystemRupSet, File precomputedDataDir) {
		// Parameters for InversionFaultSystemSolution
		boolean weightSlipRates = true; // If true, slip rate misfit is % difference for each section (recommended since it helps fit slow-moving faults).  If false, misfit is absolute difference.
		double relativeSegRateWt = 1.0;  // weight of paleo-rate constraint relative to slip-rate constraint (recommended: 1.0 if weightSlipRates=true, 0.01 otherwise)
		double relativeMagDistWt = 1000.0;  // weight of UCERF2 magnitude-distribution constraint relative to slip-rate constraint - WORKS ONLY FOR NORTHERN CALIFORNIA INVERSION (recommended:  1000.0 if weightSlipRates=true, 10.0 otherwise)
		double relativeRupRateConstraintWt = 10.0;  // weight of rupture rate constraint (recommended strong weight: 5.0, weak weight: 0.1; 100X those weights if weightSlipRates=true) - can be UCERF2 rates or Smooth G-R rates
		int numIterations = 100000;  // number of simulated annealing iterations (increase this to decrease misfit) - For Northern CA inversion, 100,000 iterations is ~5 min.
		
		ArrayList<SegRateConstraint> segRateConstraints = UCERF2_PaleoSegRateData.getConstraints(precomputedDataDir, faultSystemRupSet.getFaultSectionDataList());

		// create class the gives UCERF2-related constraints
		if(D) System.out.println("\nFinding equivalent UCERF2 ruptures . . .");
		FindEquivUCERF2_Ruptures findUCERF2_Rups = new FindEquivUCERF2_Ruptures(faultSystemRupSet.getFaultSectionDataList(), precomputedDataDir);
		double[] UCERF2Solution = getUCERF2Solution(findUCERF2_Rups, faultSystemRupSet);  // need to run this if we use getN_CalTargetMinusBackground_MFD() method in initial model or MFD constraints (below)
		
		
		
		if(D) System.out.println("\nDefining inversion constraints . . .");
		
		// a priori constraint
		double[] aPrioriRupConstraint = null;
		// Use UCERF2 Solution (Only works for Northern CA)
		aPrioriRupConstraint = UCERF2Solution;
		// Or use smooth starting solution with UCERF2 target MFD (only works for Northern CA):  
//		aPrioriRupConstraint = getSmoothStartingSolution(findUCERF2_Rups.getN_CalTargetMinusBackground_MFD());  
		
		
		// Initial model
		double[] initialRupModel = new double[faultSystemRupSet.getNumRuptures()];  // initial guess at solution x
		// Use a Priori rupture rate constraint (if used) as starting solution
		initialRupModel = aPrioriRupConstraint;
		// Or start with zeros:
//		for (int r=0; r<faultSystemRupSet.getNumRuptures(); r++) initialRupModel[r]=0;


		// Create the MFD constraints (ArrayList so we can apply this to multiple subregions)
		ArrayList<MFD_InversionConstraint> mfdConstraints = new ArrayList<MFD_InversionConstraint>();
		// Just add the N CAL one for now with a null region (apply it to full model)
		MFD_InversionConstraint mfdConstraintUCERF2 = new MFD_InversionConstraint(findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(), null);
		mfdConstraints.add(mfdConstraintUCERF2);
		
		
		
		if(D) System.out.println("\nStarting inversion . . .");
		long startTime = System.currentTimeMillis();
		inversion = new InversionFaultSystemSolution(faultSystemRupSet, weightSlipRates, relativeSegRateWt, 
				relativeMagDistWt, relativeRupRateConstraintWt, numIterations, segRateConstraints, 
				aPrioriRupConstraint, initialRupModel, mfdConstraints);
		long runTime = System.currentTimeMillis()-startTime;
		System.out.println("\nInversionFaultSystemSolution took " + (runTime/1000) + " seconds");
		if (D) System.out.print("Saving Solution to XML...");
		File xmlOut = new File(precomputedDataDir.getAbsolutePath()+File.separator+"solution.xml");
		try {
			new SimpleFaultSystemSolution(inversion).toFile(xmlOut);
			if (D) System.out.println("DONE");
		} catch (IOException e) {
			System.out.println("IOException saving Rup Set to XML!");
			e.printStackTrace();
		}
	}
	
	
	public void saveRupSet(File file) throws IOException {
		SimpleFaultSystemRupSet simple = SimpleFaultSystemRupSet.toSimple(faultSystemRupSet);
		simple.toFile(file);
	}
	
	
	private double[] getUCERF2Solution(FindEquivUCERF2_Ruptures findUCERF2_Rups, FaultSystemRupSet faultSystemRupSet) {
		int numRuptures=faultSystemRupSet.getNumRuptures();
		double[] initial_state = new double[numRuptures];
		ArrayList<double[]> ucerf2_magsAndRates = findUCERF2_Rups.getMagsAndRatesForRuptures(faultSystemRupSet.getSectionIndicesForAllRups());
		for (int r=0; r<numRuptures; r++) {
			double[] magAndRate = ucerf2_magsAndRates.get(r);
			if(magAndRate != null) 
				initial_state[r] = magAndRate[1];
			else
				initial_state[r] = 0;
		}
		return initial_state;
		
	}
	
	
	/**
	 * GET MORGAN TO EXPLAIN WHAT THIS DOES (I HAVEN"T LOOKED AT IT IN DETAIL)
	 * 
	 * @param targetMagFreqDist
	 * @return
	 */
	private double[] getSmoothStartingSolution(IncrementalMagFreqDist targetMagFreqDist) {
		
		List<List<Integer>> rupList = faultSystemRupSet.getSectionIndicesForAllRups();
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		double[] sectSlipRateReduced = faultSystemRupSet.getSlipRateForAllSections(); 
		
		int numRup = rupMeanMag.length;
		double[] meanSlipRate = new double[numRup]; // mean slip rate per section for each rupture
		double[] initial_state = new double[numRup]; // starting model
		
		
		// Get list of ruptures for each section
		ArrayList<ArrayList<Integer>> rupsPerSect = new ArrayList<ArrayList<Integer>>();
		for (int sect=0; sect<sectSlipRateReduced.length; sect++) rupsPerSect.add(new ArrayList<Integer>(0));
		for (int rup=0; rup<numRup; rup++) {	
			List<Integer> sects = rupList.get(rup);
			for (int sect: sects) rupsPerSect.get(sect).add(rup);
		}
		
		// Find mean slip rate per section for each rupture
		for (int rup=0; rup<numRup; rup++) {			
			List<Integer> sects = rupList.get(rup);
			meanSlipRate[rup]=0;
			for (int i=0; i<sects.size(); i++) {
				meanSlipRate[rup] += sectSlipRateReduced[sects.get(i)];
			}
			meanSlipRate[rup] = meanSlipRate[rup]/sects.size();
		}
	
		// Find magnitude distribution of ruptures (as discretized)
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist.setTolerance(0.1);	// this makes it a histogram
		for(int rup=0; rup<numRup;rup++) {
			// magHist.add(rupMeanMag[rup], 1.0);
			// Each bin in the magnitude histogram should be weighted by the mean slip rates of those ruptures 
			// (since later we weight the ruptures by the mean slip rate, which would otherwise result in 
			// starting solution that did not match target MFD if the mean slip rates per rupture 
			// differed between magnitude bins)
			magHist.add(rupMeanMag[rup], meanSlipRate[rup]);  // each bin
		}
		
		
		// Set up initial (non-normalized) target MFD rates for each rupture, normalized by meanSlipRate
		for (int rup=0; rup<numRup; rup++) {
			// Find number of ruptures that go through same sections as rupture and have the same magnitude
			List<Integer> sects = rupList.get(rup);
			double totalOverlap = 0; // total amount of overlap of rupture with rups of same mag (when rounded), in units of original rupture's length
			for (int sect: sects) {
				ArrayList<Integer> rups = rupsPerSect.get(sect);
				for (int r: rups) {
					if (Math.round(10*rupMeanMag[r])==Math.round(rupMeanMag[rup]))
						totalOverlap+=1;
					
				}
			}
			totalOverlap = totalOverlap/sects.size() + 1;  // add percentages of total overlap with each rupture + 1 for original rupture itself

			// Divide rate by total number of similar ruptures (same magnitude, has section overlap)  - normalize overlapping ruptures by percentage overlap
			initial_state[rup] = targetMagFreqDist.getClosestY(rupMeanMag[rup]) * meanSlipRate[rup] / (magHist.getClosestY(rupMeanMag[rup]) * totalOverlap);  
		}
		
		
		// Find normalization for all ruptures (so that MFD matches target MFD normalization)
		// Can't just add up all the mag bins to normalize because some bins don't have ruptures.
		// Instead let's choose one mag bin (that we know has rups) that has rups and normalize all bins by the amount it's off:
		double totalEventRate=0;
		for (int rup=0; rup<numRup; rup++) {
			//if ((double) Math.round(10*rupMeanMag[rup])/10==7.0)
			if (rupMeanMag[rup]>7.0 && rupMeanMag[rup]<=7.1)
				totalEventRate += initial_state[rup];
		}
		double normalization = targetMagFreqDist.getClosestY(7.0)/totalEventRate;	
		// Adjust rates to match target MFD total event rates
		for (int rup=0; rup<numRup; rup++) 
			initial_state[rup]=initial_state[rup]*normalization;
		
		
		// plot magnitude histogram for the inversion starting model
		IncrementalMagFreqDist magHist2 = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist2.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<numRup;r++)
			magHist2.add(rupMeanMag[r], initial_state[r]);
		ArrayList funcs = new ArrayList();
		funcs.add(magHist2);
		magHist2.setName("Magnitude Distribution of Starting Model (before Annealing)");
		magHist2.setInfo("(number in each mag bin)");
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Magnitude Histogram"); 
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Frequency (per bin)");
	
		
		return initial_state;
		
	}


	



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RunInversion test = new RunInversion();
		test.inversion.plotStuff();
	}


}
