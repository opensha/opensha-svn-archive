package scratch.UCERF3;

import java.io.File;
import java.util.ArrayList;

import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.UCERF2_PaleoSegRateData;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;

public class RunInversion {

	protected final static boolean D = true;  // for debugging

	public RunInversion() {
		this(new File("dev/scratch/UCERF3/preComputedData/"));
	}
	
	
	public RunInversion(File precomputedDataDir) {
		
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
		InversionFaultSystemRupSet faultSystemRupSet = new InversionFaultSystemRupSet(DeformationModelFetcher.DefModName.UCERF2_NCAL,maxJumpDist, 
				maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, 
				minNumSectInRup, magAreaRelList, 
				moRateReduction,  InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL , precomputedDataDir);
		long runTime = System.currentTimeMillis()-startTime;
		System.out.println("\nFaultSystemRupSet instantiation took " + (runTime/1000) + " seconds");
	
		// Below will be for running an inversion
		double relativeSegRateWt = 0.01;  // weight of paleo-rate constraint relative to slip-rate constraint (recommended: 0.01)
		double relativeMagDistWt = 10.0;  // weight of UCERF2 magnitude-distribution constraint relative to slip-rate constraint - WORKS ONLY FOR NORTHERN CALIFORNIA INVERSION (recommended: 10.0)
		double relativeRupRateConstraintWt = 0.1;  // weight of rupture rate constraint (recommended strong weight: 5.0, weak weight: 0.1) - can be UCERF2 rates or Smooth G-R rates
		int numIterations = 10000;
		
		ArrayList<SegRateConstraint> segRateConstraints = UCERF2_PaleoSegRateData.getConstraints(precomputedDataDir, faultSystemRupSet.getFaultSectionList());

		FindEquivUCERF2_Ruptures findUCERF2_Rups = new FindEquivUCERF2_Ruptures(faultSystemRupSet.getFaultSectionList(), precomputedDataDir);
		
		// a priori constraint
		double[] aPriorRupConstraint = getUCERF2Solution(findUCERF2_Rups, faultSystemRupSet);
		// OTHER A-PRIORI MODELS TO IMPLEMENT: ******************************
		//solutionConstraint = getSmoothGRStartingSolution(rupList,rupMeanMag);
		
		// Initial model
		double[] initialRupModel = aPriorRupConstraint;
		// OTHER STARTING MODELS TO IMPLEMENT: ******************************
//		initial_state = getSmoothGRStartingSolution(rupList,rupMeanMag);  	// G-R initial solution
//		initial_state = getSmoothGRStartingSolution(rupList,rupMeanMag);  // "Smooth" G-R starting solution that takes into account number of rups through a section
//		IncrementalMagFreqDist targetMagFreqDist = findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(); initial_state = getSmoothStartingSolution(rupList,rupMeanMag,targetMagFreqDist);  // "Smooth" starting solution that takes into account number of rups through a section
//		for (int r=0; r<numRuptures; r++) initial_state[r]=0;			// initial solution of zero rupture rates


		ArrayList<MFD_InversionConstraint> mfdConstraints = new ArrayList<MFD_InversionConstraint>();
		MFD_InversionConstraint mfdConstraintUCERF2 = new MFD_InversionConstraint(findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(), null);
		mfdConstraints.add(mfdConstraintUCERF2);
		
		if(D) System.out.println("Starting inversion");
		InversionFaultSystemSolution inversion = new InversionFaultSystemSolution(faultSystemRupSet, relativeSegRateWt, 
				relativeMagDistWt, relativeRupRateConstraintWt, numIterations, segRateConstraints, 
				aPriorRupConstraint, initialRupModel, mfdConstraints);
		if(D) System.out.println("Done with inversion");

	}
	
	
	private double[] getUCERF2Solution(FindEquivUCERF2_Ruptures findUCERF2_Rups, InversionFaultSystemRupSet faultSystemRupSet) {
		int numRuptures=faultSystemRupSet.getNumRupRuptures();
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
	 * @param args
	 */
	public static void main(String[] args) {
		
		RunInversion test = new RunInversion();
		
		// the following was run on April 19th for N Cal case for Tom Parsons (see email that day)
//		test.writeSectionsToFile("sectionsForTom041911");
//		test.writeRupsToFiles("rupturesForTom041911");
		
		/* Tests for the Loc at the N. end of the Parkfield Trace
		Region nCalRegion = new CaliforniaRegions.RELM_NOCAL();
		System.out.println(nCalRegion.contains(new Location(36.002647,-120.56089000000001)));
		Region mod_relm_nocal = new Region(nCalRegion.getBorder(), BorderType.GREAT_CIRCLE);
		System.out.println(mod_relm_nocal.contains(new Location(36.002647,-120.56089000000001)));
		*/

	}


}

