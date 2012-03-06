package scratch.UCERF3.inversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM2pt1_Ruptures;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM3_Ruptures;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_Ruptures;

/**
 * This represents all of the inversion configuration parameters specific to an individual model
 * on the UCERF3 logic tree. Parameters can be fetched for a given logic tree branch with the 
 * <code>forModel(...)</code> method.
 * 
 * @author Kevin
 *
 */
public class InversionConfiguration {
	
	private boolean weightSlipRates;
	private double relativePaleoRateWt; 
	private double relativeMagnitudeEqualityConstraintWt;
	private double relativeMagnitudeInequalityConstraintWt;
	private double relativeRupRateConstraintWt;
	private double relativeParticipationSmoothnessConstraintWt;
	private double participationConstraintMagBinSize;
	private double relativeMinimizationConstraintWt;
	private double[] aPrioriRupConstraint;
	private double[] initialRupModel;
	// thses are the rates that should be used for water level computation. this will
	// often be set equal to initial rup model or a priori rup constraint
	private double[] minimumRuptureRateBasis;
	private List<MFD_InversionConstraint> mfdEqualityConstraints;
	private List<MFD_InversionConstraint> mfdInequalityConstraints;
	private double minimumRuptureRateFraction;

	private double relativeSmoothnessWt;
	
	public InversionConfiguration(
			boolean weightSlipRates,
			double relativePaleoRateWt, 
			double relativeMagnitudeEqualityConstraintWt,
			double relativeMagnitudeInequalityConstraintWt,
			double relativeRupRateConstraintWt, 
			double relativeParticipationSmoothnessConstraintWt,
			double participationConstraintMagBinSize,
			double relativeMinimizationConstraintWt,
			double[] aPrioriRupConstraint,
			double[] initialRupModel,
			double[] minimumRuptureRateBasis, 
			double relativeSmoothnessWt,
			List<MFD_InversionConstraint> mfdEqualityConstraints,
			List<MFD_InversionConstraint> mfdInequalityConstraints,
			double minimumRuptureRateFraction) {
		this.weightSlipRates = weightSlipRates;
		this.relativePaleoRateWt = relativePaleoRateWt;
		this.relativeMagnitudeEqualityConstraintWt = relativeMagnitudeEqualityConstraintWt;
		this.relativeMagnitudeInequalityConstraintWt = relativeMagnitudeInequalityConstraintWt;
		this.relativeRupRateConstraintWt = relativeRupRateConstraintWt;
		this.relativeParticipationSmoothnessConstraintWt = relativeParticipationSmoothnessConstraintWt;
		this.participationConstraintMagBinSize = participationConstraintMagBinSize;
		this.relativeMinimizationConstraintWt = relativeMinimizationConstraintWt;
		this.aPrioriRupConstraint = aPrioriRupConstraint;
		this.initialRupModel = initialRupModel;
		this.minimumRuptureRateBasis = minimumRuptureRateBasis;
		this.relativeSmoothnessWt = relativeSmoothnessWt;
		this.mfdEqualityConstraints = mfdEqualityConstraints;
		this.mfdInequalityConstraints = mfdInequalityConstraints;
		this.minimumRuptureRateFraction = minimumRuptureRateFraction;
	}
	
	/**
	 * This generates an inversion configuration for the given inversion model and rupture set
	 * 
	 * @param model
	 * @param rupSet
	 * @return
	 */
	public static InversionConfiguration forModel(InversionModels model, FaultSystemRupSet rupSet) {
		/* *******************************************
		 * COMMON TO ALL MODELS
		 * ******************************************* */
		// If true, slip rate misfit is % difference for each section (recommended since
		// it helps fit slow-moving faults).  If false, misfit is absolute difference.
		boolean weightSlipRates = true;
		
		// weight of paleo-rate constraint relative to slip-rate constraint
		// (recommended: 1.0 if weightSlipRates=true, 0.01 otherwise)
		double relativePaleoRateWt = 1.0;
		
		// weight of magnitude-distribution EQUALITY constraint relative to slip-rate constraint
		// (recommended:  1000 if weightSlipRates=true, 10 otherwise)
		double relativeMagnitudeEqualityConstraintWt = 0;
		
		// weight of magnitude-distribution INEQUALITY constraint relative to slip-rate constraint
		// (recommended:  1000 if weighted per bin -- this is hard-coded in)
		double relativeMagnitudeInequalityConstraintWt = 1000;
		
		// magnitude-bin size for mfd participation smoothness constraint
		double participationConstraintMagBinSize = 0.1;
		
		// weight of rupture-rate minimization constraint weights relative to slip-rate constraint (recommended: 10,000)
		double relativeMinimizationConstraintWt = 0;
		
		// weight of entropy-maximization constraint (should smooth rupture rates) (recommended: 10000)
		double relativeSmoothnessWt = 0;
		
		boolean ucerf3MFDs = true;
		UCERF2_MFD_ConstraintFetcher ucerf2Constraints = null;
		if (ucerf3MFDs)
			ucerf2Constraints = new UCERF2_MFD_ConstraintFetcher();
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
		if (ucerf3MFDs) {
			mfdInequalityConstraints.add(UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.NO_CA_1850));
		} else {
			ucerf2Constraints.setRegion(noCal);
			mfdInequalityConstraints.add(ucerf2Constraints.getTargetMFDConstraint());
		}
		// add MFD constraint for Southern CA
		if (entire_region != noCal) {
			// don't add so cal if we're just doing a no cal inversion
			if (ucerf3MFDs) {
				mfdInequalityConstraints.add(UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.SO_CA_1850));
			} else {
				ucerf2Constraints.setRegion(soCal);
				mfdInequalityConstraints.add(ucerf2Constraints.getTargetMFDConstraint());
			}
		}
		
		/* *******************************************
		 * MODEL SPECIFIC
		 * ******************************************* */
		// define model specific value here (leave them as null or unassigned, then set values
		// in the below switch statement
		
		// weight of rupture rate constraint (recommended strong weight: 5.0, weak weight: 0.1;
		// 100X those weights if weightSlipRates=true) - can be UCERF2 rates or Smooth G-R rates
		double relativeRupRateConstraintWt;
		
		// weight of participation MFD smoothness weight - applied on subsection basis (recommended:  1000)
		double relativeParticipationSmoothnessConstraintWt;
		
		// fraction of the minimum rupture rate basis to be used as initial rates
		double minimumRuptureRateFraction;
		
		double[] aPrioriRupConstraint;
		double[] initialRupModel;
		double[] minimumRuptureRateBasis;
		
		switch (model) {
		case CHAR:
			relativeParticipationSmoothnessConstraintWt = 0;
			relativeRupRateConstraintWt = 1;
			aPrioriRupConstraint = getUCERF2Solution(rupSet);
			initialRupModel = Arrays.copyOf(aPrioriRupConstraint, aPrioriRupConstraint.length);
			double transitionMag = 7.6;
			mfdInequalityConstraints = makeMFDConstraintsBilinear(mfdInequalityConstraints, findBValueForMomentRateReduction(rupSet.getDeformationModel(), transitionMag, rupSet), transitionMag);
			mfdInequalityConstraints = accountForVaryingMinMag(mfdInequalityConstraints, rupSet);
			minimumRuptureRateFraction = 0.01;
			minimumRuptureRateBasis = adjustStartingModel(getSmoothStartingSolution(rupSet,getGR_Dist(rupSet, 1.0, 8.3)), mfdInequalityConstraints, rupSet);
			if (relativeMagnitudeInequalityConstraintWt>0.0) initialRupModel = adjustStartingModel(initialRupModel, mfdInequalityConstraints, rupSet);  
			break;
		case GR:
			relativeParticipationSmoothnessConstraintWt = 1000;
			relativeRupRateConstraintWt = 0;
			aPrioriRupConstraint = null;
			initialRupModel = getSmoothStartingSolution(rupSet,getGR_Dist(rupSet, 1.0, 8.3));
			mfdInequalityConstraints = reduceMFDConstraint(mfdInequalityConstraints, findMomentFractionOffFaults(rupSet.getDeformationModel()));
			mfdInequalityConstraints = accountForVaryingMinMag(mfdInequalityConstraints, rupSet);
			minimumRuptureRateFraction = 0.01;
			minimumRuptureRateBasis = adjustStartingModel(initialRupModel, mfdInequalityConstraints, rupSet);
			if (relativeMagnitudeInequalityConstraintWt>0.0) initialRupModel = adjustStartingModel(initialRupModel, mfdInequalityConstraints, rupSet); 
			break;
		case UNCONSTRAINED:
			relativeParticipationSmoothnessConstraintWt = 0;
			relativeRupRateConstraintWt = 0;
			aPrioriRupConstraint = null;
			initialRupModel = new double[rupSet.getNumRuptures()];
			minimumRuptureRateBasis = null;
			minimumRuptureRateFraction = 0;
			break;

		default:
			throw new IllegalStateException("Unknown inversion model: "+model);
		}
		
		return new InversionConfiguration(
				weightSlipRates,
				relativePaleoRateWt,
				relativeMagnitudeEqualityConstraintWt,
				relativeMagnitudeInequalityConstraintWt,
				relativeRupRateConstraintWt,
				relativeParticipationSmoothnessConstraintWt,
				participationConstraintMagBinSize,
				relativeMinimizationConstraintWt,
				aPrioriRupConstraint,
				initialRupModel,
				minimumRuptureRateBasis,
				relativeSmoothnessWt,
				mfdEqualityConstraints,
				mfdInequalityConstraints,
				minimumRuptureRateFraction);
	}
	
	
	
	
	/**
	 * This method lowers an MFD constraint to account for spatially-varying minimum magnitudes on faults.
	 */
	private static ArrayList<MFD_InversionConstraint> accountForVaryingMinMag(
			ArrayList<MFD_InversionConstraint> mfdInequalityConstraints, FaultSystemRupSet rupSet) {
		
		HistogramFunction adjustmentRatio = FaultSystemRupSetCalc.getMinMagHistogram(rupSet, 5.05,40,0.1, true).getCumulativeDistFunction();  // CURRENTLY THIS IS NOT REGION SPECIFIC
		
		for (int i=0; i<mfdInequalityConstraints.size(); i++) {
			for (double m = mfdInequalityConstraints.get(i).getMagFreqDist().getMinX(); m <= mfdInequalityConstraints.get(i).getMagFreqDist().getMaxX(); m += mfdInequalityConstraints.get(i).getMagFreqDist().getDelta()) {
				mfdInequalityConstraints.get(i).getMagFreqDist().set(m, mfdInequalityConstraints.get(i).getMagFreqDist().getClosestY(m) * adjustmentRatio.getClosestY(m));
			}
		}
		return mfdInequalityConstraints;
	}

	
	/**
	 * This method adjusts the starting model to ensure that for each MFD inequality constraint magnitude-bin, the starting model is below the MFD.
	 * It will uniformly reduce the rates of ruptures in any bins that are over the MFD.
	 */
	private static double[] adjustStartingModel(double[] initialRupModel,
			ArrayList<MFD_InversionConstraint> mfdInequalityConstraints, FaultSystemRupSet rupSet) {
		
		double[][] fractRupsInsideMFD_Regions = rupSet.computeFractRupsInsideMFD_Regions(mfdInequalityConstraints);
		double[] rupMeanMag = rupSet.getMagForAllRups();
		
		
		for (int i=0; i<mfdInequalityConstraints.size(); i++) {
			IncrementalMagFreqDist targetMagFreqDist = mfdInequalityConstraints.get(i).getMagFreqDist();
			IncrementalMagFreqDist startingModelMagFreqDist = new IncrementalMagFreqDist(targetMagFreqDist.getMinX(), targetMagFreqDist.getNum(), targetMagFreqDist.getDelta());
			startingModelMagFreqDist.setTolerance(0.1);
			
			// Find the starting model MFD
			for(int rup=0; rup<rupSet.getNumRuptures(); rup++) {
				double mag = rupMeanMag[rup];
				double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
				if (fractRupInside > 0) {
					startingModelMagFreqDist.add(mag, fractRupInside * initialRupModel[rup]);
				}
			}
			
			System.out.println("starting model MFD: "+startingModelMagFreqDist);
			System.out.println("target MFD: "+targetMagFreqDist);
			// Find the amount to adjust starting model MFD to be below or equal to Target MFD
			IncrementalMagFreqDist adjustmentRatio = new IncrementalMagFreqDist(targetMagFreqDist.getMinX(), targetMagFreqDist.getNum(), targetMagFreqDist.getDelta());
			for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m+= targetMagFreqDist.getDelta()) {
				if (startingModelMagFreqDist.getClosestY(m) > targetMagFreqDist.getClosestY(m))
					adjustmentRatio.set(m, targetMagFreqDist.getClosestY(m) / startingModelMagFreqDist.getClosestY(m));
				else
					adjustmentRatio.set(m, 1.0);
			}
			System.out.println("adjustment ratio: "+adjustmentRatio);
			for(int rup=0; rup<rupSet.getNumRuptures(); rup++) {
				double mag = rupMeanMag[rup];
				initialRupModel[rup] = initialRupModel[rup] * adjustmentRatio.getClosestY(mag);
			}
			
		}
		
		
		return initialRupModel;
	}

	
	/**
	 * This method multiplies the input MFD constraints by fractionRateReduction.
	 * For example, if the off-fault rates are 10% of the rates, setting fractionRateReduction to 0.1 will reduce the MFDs to 90% of their previous rates.
	 */
	private static ArrayList<MFD_InversionConstraint> reduceMFDConstraint(
			ArrayList<MFD_InversionConstraint> mfdInequalityConstraints,
			double fractionRateReduction) {
		for (int i=0; i<mfdInequalityConstraints.size(); i++) {
			for (double mag=mfdInequalityConstraints.get(i).getMagFreqDist().getMinX(); mag<mfdInequalityConstraints.get(i).getMagFreqDist().getMaxX(); mag+=mfdInequalityConstraints.get(i).getMagFreqDist().getDelta()) {
				double setVal=mfdInequalityConstraints.get(i).getMagFreqDist().getY(mag) * (1-fractionRateReduction);
				mfdInequalityConstraints.get(i).getMagFreqDist().set(mag, setVal);
			}
		}		
		return  mfdInequalityConstraints;
	}

	
	/**
	 * This method returns the bValue that will achieve a reduction in moment that matches the % off-fault deformation for a given deformation model.  
	 * This is for use with Bilinear MFD Constraint.  The momentRate is the moment, in Nm, desired off the faults.
	 * The returned b-value is the b-value below the transition magnitude (assuming the previous MFD was G-R with b=1) to achieve this momentRate reduction.
	 */
	private static double findBValueForMomentRateReduction(DeformationModels deformationModel, double transitionMag, FaultSystemRupSet rupSet) {
		boolean D = true; //debugging
		
		double momentFractionOffFaults = findMomentFractionOffFaults(deformationModel);
		double totalMoment = rupSet.getTotalMomentRateForAllSections();
		if (D) System.out.println("\nImplementing bilinear MFD constraint . . .\nTotal Moment = "+totalMoment);
		
		// Use the mag-dist for the whole region since the deformation model off-fault moment #s from Kaj are also for the whole region
		IncrementalMagFreqDist magDist = UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850).getMagFreqDist();
		
		// Find total moment below transition magnitude (on-fault + off-fault)
		double totalMomentBelowTransition = 0;
		for (double mag=magDist.getMinX(); mag<=transitionMag; mag+=magDist.getDelta()) {
			totalMomentBelowTransition += magDist.getMomentRate(mag);
		}
		if (D) System.out.println("Total Moment below the Transition Magnitude of "+transitionMag+" = "+totalMomentBelowTransition);
		
		double momentRateToRemove = momentFractionOffFaults*totalMoment;
		if (D) System.out.println("The amount of moment to remove from MFD = "+momentRateToRemove);
		if (momentRateToRemove>=totalMomentBelowTransition)
			throw new IllegalStateException("This is not going to work. The total moment below your transition magnitude is less than the off-fault moment");
		
		double momentReduction = momentRateToRemove/totalMomentBelowTransition;  // fraction of moment that should be off-fault below transition magnitude
		double bValue = (3.0*momentReduction-1.0)/(2.0*momentReduction);  // b-value below transition magnitude that will achieve desired moment reduction
		if (D) System.out.println("b-Value below transition magnitude = "+bValue+"\n");
		
		return bValue;
	}
	
//	/**
//	 * This method returns the bValue that will achieve a desired reduction in moment.  
//	 * This is for use with Bilinear MFD Constraint.  The momentRate is the moment, in Nm, desired off the faults.
//	 * The returned b-value is the b-value below the transition magnitude (assuming the previous MFD was G-R with b=1) to achieve this momentRate reduction.
//	 */
//	private static double findBValueForMomentRateReduction(double momentRate, double transitionMag) {
//		boolean D = true; //debugging
//		
//		// Use the mag-dist for the whole region since the deformation model off-fault moment #s from Kaj are also for the whole region
//		IncrementalMagFreqDist magDist = UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850).getMagFreqDist();
//		
//		// Find total moment below transition magnitude (on-fault + off-fault)
//		double totalMomentBelowTransition = 0;
//		for (double mag=magDist.getMinX(); mag<=transitionMag; mag+=magDist.getDelta()) {
//			totalMomentBelowTransition += magDist.getMomentRate(mag);
//		}
//		if (D) System.out.println("totalMomentBelowTransition = "+totalMomentBelowTransition);
//		if (D) System.out.println("momentRate to reduce = "+momentRate);
//		
//		if (momentRate>=totalMomentBelowTransition)
//			throw new IllegalStateException("This is not going to work. The total moment below your transition magnitude is less than the off-fault moment");
//		
//		double momentReduction = momentRate/totalMomentBelowTransition;  // fraction of moment that should be off-fault below transition magnitude
//		if (D) System.out.println("momentReduction = "+momentReduction);
//		double bValue = (3.0*momentReduction-1.0)/(2.0*momentReduction);  // b-value below transition magnitude that will achieve desired moment reduction
//		if (D) System.out.println("bValue = "+bValue);
//		
//		return bValue;
//	}
	
	
	private static double findMomentFractionOffFaults (DeformationModels deformationModel) {
		// These values are from an e-mail from Kaj dated 2/29/12, for Zeng model see 3/5/12 e-mail
		double momentFractionOffFaults;
		switch (deformationModel) {
		case GEOLOGIC:
			UCERF2_MFD_ConstraintFetcher ucerf2Constraints = new UCERF2_MFD_ConstraintFetcher();
			Region entire_region = new CaliforniaRegions.RELM_TESTING();
			ucerf2Constraints.setRegion(entire_region);
			double UCERF2_OnFaultMoment = ucerf2Constraints.getTargetMinusBackgroundMFD().getTotalMomentRate();
			double UCERF2_TargetMoment = ucerf2Constraints.getTargetMFDConstraint().getMagFreqDist().getTotalMomentRate();
			momentFractionOffFaults = 1 - UCERF2_OnFaultMoment / UCERF2_TargetMoment;
			System.out.println("UCERF2 moment fraction off faults = " + momentFractionOffFaults);  // Ned calculates 26%
		case ABM:
			momentFractionOffFaults = 33.6766 / 100.0;
			break;
		case GEOBOUND:
			momentFractionOffFaults = 34.2229 / 100.0;
			break;
		case NEOKINEMA:
			momentFractionOffFaults = 28.0202 / 100.0;
			break;
		case GEOLOGIC_PLUS_ABM:
			momentFractionOffFaults = (findMomentFractionOffFaults(DeformationModels.GEOLOGIC) + findMomentFractionOffFaults(DeformationModels.ABM)) / 2.0;
		case ZENG:
			momentFractionOffFaults = 37.0728 / 100.0;
			break;	
		default:
			throw new IllegalStateException("Moment fraction off faults is not defined for this deformation model :(");
		}
		
		return momentFractionOffFaults;
	}
	
	
	/**
	 * This method changes the input MFD constraints (WHICH IT ASSUMES ARE G-R WITH b=1) by changing the b-Value below a transition magnitude.
	 * The returned MFDs are G-R both below and above the transition magnitude, b=1 above it, and the specified b-value below it.
	 */
	private static ArrayList<MFD_InversionConstraint> makeMFDConstraintsBilinear(
			ArrayList<MFD_InversionConstraint> mfdConstraints, double bValueBelowTransition, double transitionMag) {
		for (int i=0; i<mfdConstraints.size(); i++) {
			for (double mag=mfdConstraints.get(i).getMagFreqDist().getMinX(); mag<transitionMag; mag+=mfdConstraints.get(i).getMagFreqDist().getDelta()) {
				double setVal=mfdConstraints.get(i).getMagFreqDist().getY(mag)*Math.pow(10, (transitionMag-mag)*(bValueBelowTransition-1));
				mfdConstraints.get(i).getMagFreqDist().set(mag, setVal);
			}
		}
		return mfdConstraints;
	}

	
	public static List<MFD_InversionConstraint> getGriddedConstraints(
			UCERF2_MFD_ConstraintFetcher UCERF2Constraints, Region region,
			double latBoxSize, double lonBoxSize) {
		ArrayList<MFD_InversionConstraint> mfdEqualityConstraints = new ArrayList<MFD_InversionConstraint>();
		// UCERF2 MFD constraints for subregions - 1-degree boxes
		double minLat = region.getMinLat(); double maxLat = region.getMaxLat();
		double minLon = region.getMinLon(); double maxLon = region.getMaxLon();
		for (double lat=minLat; lat<maxLat; lat+=latBoxSize){
			for (double lon=minLon; lon<maxLon; lon+=lonBoxSize){
				Region currentSubRegion = new Region(new Location(lat,lon),new Location(lat+latBoxSize,lon+lonBoxSize));
				LocationList border = currentSubRegion.getBorder();
				boolean currentSubRegionInRegion = true;
				// SubRegion is in the region if all 4 border points are in the region
				// TODO: (should work for now -- change later!)
				for (int i=0; i<border.size(); i++)
					if (region.contains(border.get(i)) == false) currentSubRegionInRegion = false; 
				if (currentSubRegionInRegion == true) {
					UCERF2Constraints.setRegion(currentSubRegion);
					mfdEqualityConstraints.add(UCERF2Constraints.getTargetMinusBackgrMFD_Constraint());
				}
			}
		}
		return mfdEqualityConstraints;
	}
	
	
	/**
	 * This method returns a G-R magnitude distribution with specified b-value. The a-value is set
	 * to match the target moment rate implied by the slip rates FOR THE WHOLE REGION.
	 * Mmax is a strict upper-magnitude cut-off (set to nearest 0.1 magnitude unit) 
	 */
	public static IncrementalMagFreqDist getGR_Dist(FaultSystemRupSet faultSystemRupSet, double bValue, double Mmax) {
		
		// Set up (unnormalized) G-R magnitude distribution
		IncrementalMagFreqDist magDist = new IncrementalMagFreqDist(5.05,35,0.1);
		double totalMoment = 0;  // total moment per year implied by magDist 
		for(double m=5.05; m<=8.45; m=m+0.1) {
			if (m<Mmax) {
			magDist.set(m, Math.pow(10, -bValue*m));
			// Note/TODO: the current moment calculation will be a bit off because we are adding
			// up at the bin centers rather than integrating over each bin.
			// It would be better to analytically integrate with a precise Mmax
			totalMoment += magDist.getClosestY(m) * Math.pow(10,1.5*(m + 10.7))*(Math.pow(10,-7)); // in N*m/yr
			} 
			else magDist.set(m, 0);
		}
			
		// Find total moment/year implied my slip rates
		// Treats NaN slip rates as zero
		double targetTotalMoment = 0;  // total moment per year implied by slip rates
		double[] sectSlipRateReduced = faultSystemRupSet.getSlipRateForAllSections();
		double[] sectArea = faultSystemRupSet.getAreaForAllSections();
		for (int sect=0; sect<faultSystemRupSet.getNumSections(); sect++) 
			if (!Double.isNaN(sectSlipRateReduced[sect]))
				// in N*m/yr
				targetTotalMoment += sectSlipRateReduced[sect]*sectArea[sect]*FaultMomentCalc.SHEAR_MODULUS;
		
		// Scale magnitude distribution (set a-value) to match the total moment implied by slip rates
		for (int i=0; i<magDist.getNum(); i++) 
			magDist.set(i, magDist.getY(i)*targetTotalMoment/totalMoment);
		
		return magDist;
		
	}
	
	/**
	 * Probably has to be revised for FM 3.1? currently not used
	 * 
	 * @param findUCERF2_Rups
	 * @param faultSystemRupSet
	 * @return
	 */
	public static double[] getUCERF2Solution(FaultSystemRupSet faultSystemRupSet) {
		Preconditions.checkNotNull(faultSystemRupSet, "No rupture set supplied!");
		FaultModels fm = faultSystemRupSet.getFaultModel();
		Preconditions.checkNotNull(fm, "A fault model must be specified by the rupture set in order" +
				" to get a UCERF2 solution. It's possible that you're using an old rupture set that doesn't have this data" +
				" embedded. In that case, regenerate your rupture set (or beg Kevin to modify it for you)");
		FindEquivUCERF2_Ruptures findUCERF2_Rups;
		if (fm == FaultModels.FM2_1) 
			findUCERF2_Rups = new FindEquivUCERF2_FM2pt1_Ruptures(faultSystemRupSet,
					UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
		else
			findUCERF2_Rups = new FindEquivUCERF2_FM3_Ruptures(faultSystemRupSet,
					UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, fm);
		
		int numRuptures=faultSystemRupSet.getNumRuptures();
		double[] initial_state = new double[numRuptures];
		ArrayList<double[]> ucerf2_magsAndRates = findUCERF2_Rups.getMagsAndRatesForRuptures();
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
	public static double[] getSmoothStartingSolution(
			FaultSystemRupSet faultSystemRupSet, IncrementalMagFreqDist targetMagFreqDist) {
		List<List<Integer>> rupList = faultSystemRupSet.getSectionIndicesForAllRups();
		
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		double[] sectSlipRateReduced = faultSystemRupSet.getSlipRateForAllSections(); 
		int numRup = rupMeanMag.length;
		double[] initial_state = new double[numRup];  // starting model
		double[] meanSlipRate = new double[numRup];  // mean slip rate per section for each rupture
		boolean[] flagRup = new boolean[numRup]; // flag rup if it has ny NaN or zero slip-rate sections
		
		// Calculate mean slip rates for ruptures
		// If there are NaN slip rates, treat them as 0
		for (int rup=0; rup<meanSlipRate.length; rup++) {
			List<Integer> sects = faultSystemRupSet.getSectionsIndicesForRup(rup);
			double totalOfSlipRates = 0;
			for (int i=0; i<sects.size(); i++) {
				int sect = sects.get(i);
				if (Double.isNaN(sectSlipRateReduced[sect])  || sectSlipRateReduced[sect] == 0)  { // if rupture has any NaN or zero slip-rate sections, flag it!
					flagRup[rup] = true;
				} else 	totalOfSlipRates+=sectSlipRateReduced[sect];
			}
			meanSlipRate[rup] = totalOfSlipRates/sects.size(); // average mean slip rate for sections in rupture
		}
			
		
		// Get list of ruptures for each section
		ArrayList<List<Integer>> rupsPerSect = new ArrayList<List<Integer>>();
		for (int sect=0; sect<sectSlipRateReduced.length; sect++) {
			List<Integer> rups = faultSystemRupSet.getRupturesForSection(sect);
			rupsPerSect.add(sect, rups);
		}
	
		// Find magnitude distribution of ruptures (as discretized)
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,40,0.1);
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
			// COMMENT THIS OUT FOR NOW - STARTING SOLUTION IS BETTER WITHOUT RUPTURE OVERLAP CORRECTION
//			List<Integer> sects = rupList.get(rup);
//			// total amount of overlap of rupture with rups of same mag (when rounded),
//			// in units of original rupture's length
//			double totalOverlap = 0;
//			for (int sect: sects) {
//				ArrayList<Integer> rups = rupsPerSect.get(sect);
//				for (int r: rups) {
//					if (Math.round(10*rupMeanMag[r])==Math.round(10*rupMeanMag[rup]))
//						totalOverlap+=1;
//				}
//			}
//			// add percentages of total overlap with each rupture + 1 for original rupture itself
//			totalOverlap = totalOverlap/sects.size() + 1; 
			double totalOverlap = 1d;
		
			// Divide rate by total number of similar ruptures (same magnitude, has section overlap)
			// - normalize overlapping ruptures by percentage overlap
			initial_state[rup] = targetMagFreqDist.getClosestY(rupMeanMag[rup])
					* meanSlipRate[rup] / (magHist.getClosestY(rupMeanMag[rup]) * totalOverlap);
//			if (D && rup % 100 == 0)
//				System.out.println("Done with rup: "+rup);
		}
		
		// Find normalization for all ruptures (so that MFD matches target MFD normalization)
		// Can't just add up all the mag bins to normalize because some bins don't have ruptures.
		// Instead let's choose one mag bin (that we know has rups) that has rups and normalize
		// all bins by the amount it's off:
		double totalEventRate=0;
		for (int rup=0; rup<numRup; rup++) {
			//if ((double) Math.round(10*rupMeanMag[rup])/10==7.0)
			if (rupMeanMag[rup]>7.0 && rupMeanMag[rup]<=7.1)
				totalEventRate += initial_state[rup];
		}
		double normalization = targetMagFreqDist.getClosestY(7.0)/totalEventRate;	
		// Adjust rates to match target MFD total event rates
		for (int rup=0; rup<numRup; rup++) {
			initial_state[rup]=initial_state[rup]*normalization;
			if (Double.isNaN(initial_state[rup]) || Double.isInfinite(initial_state[rup]))
				throw new IllegalStateException("initial_state["+rup+"] = "+initial_state[rup]);
		}
		
		
		// set initial_state to 0 for flagged rups (these have 0 or NaN slip-rate sections)
		for (int i=0; i<numRup; i++) {
			if (flagRup[i]) initial_state[i] = 0;
		}
		
		// NO PLOTTING CODE ALLOWED HERE!!!!! do it somewhere else please!
//		// plot magnitude histogram for the inversion starting model
//		IncrementalMagFreqDist magHist2 = new IncrementalMagFreqDist(5.05,40,0.1);
//		magHist2.setTolerance(0.2);	// this makes it a histogram
//		for(int r=0; r<numRup;r++)
//			magHist2.add(rupMeanMag[r], initial_state[r]);
//		ArrayList funcs = new ArrayList();
//		funcs.add(magHist2);
//		magHist2.setName("Magnitude Distribution of Starting Model (before Annealing)");
//		magHist2.setInfo("(number in each mag bin)");
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Magnitude Histogram"); 
//		graph.setX_AxisLabel("Magnitude");
//		graph.setY_AxisLabel("Frequency (per bin)");
		
		return initial_state;
		
	}

	public boolean isWeightSlipRates() {
		return weightSlipRates;
	}

	public void setWeightSlipRates(boolean weightSlipRates) {
		this.weightSlipRates = weightSlipRates;
	}

	public double getRelativePaleoRateWt() {
		return relativePaleoRateWt;
	}

	public void setRelativePaleoRateWt(double relativePaleoRateWt) {
		this.relativePaleoRateWt = relativePaleoRateWt;
	}

	public double getRelativeMagnitudeEqualityConstraintWt() {
		return relativeMagnitudeEqualityConstraintWt;
	}

	public void setRelativeMagnitudeEqualityConstraintWt(
			double relativeMagnitudeEqualityConstraintWt) {
		this.relativeMagnitudeEqualityConstraintWt = relativeMagnitudeEqualityConstraintWt;
	}

	public double getRelativeMagnitudeInequalityConstraintWt() {
		return relativeMagnitudeInequalityConstraintWt;
	}

	public void setRelativeMagnitudeInequalityConstraintWt(
			double relativeMagnitudeInequalityConstraintWt) {
		this.relativeMagnitudeInequalityConstraintWt = relativeMagnitudeInequalityConstraintWt;
	}

	public double getRelativeRupRateConstraintWt() {
		return relativeRupRateConstraintWt;
	}

	public void setRelativeRupRateConstraintWt(double relativeRupRateConstraintWt) {
		this.relativeRupRateConstraintWt = relativeRupRateConstraintWt;
	}

	public double getRelativeParticipationSmoothnessConstraintWt() {
		return relativeParticipationSmoothnessConstraintWt;
	}

	public void setRelativeParticipationSmoothnessConstraintWt(
			double relativeParticipationSmoothnessConstraintWt) {
		this.relativeParticipationSmoothnessConstraintWt = relativeParticipationSmoothnessConstraintWt;
	}

	public double getParticipationConstraintMagBinSize() {
		return participationConstraintMagBinSize;
	}

	public void setParticipationConstraintMagBinSize(
			double participationConstraintMagBinSize) {
		this.participationConstraintMagBinSize = participationConstraintMagBinSize;
	}

	public double getRelativeMinimizationConstraintWt() {
		return relativeMinimizationConstraintWt;
	}

	public void setRelativeMinimizationConstraintWt(
			double relativeMinimizationConstraintWt) {
		this.relativeMinimizationConstraintWt = relativeMinimizationConstraintWt;
	}

	public double[] getA_PrioriRupConstraint() {
		return aPrioriRupConstraint;
	}

	public void setA_PrioriRupConstraint(double[] aPrioriRupConstraint) {
		this.aPrioriRupConstraint = aPrioriRupConstraint;
	}

	public double[] getInitialRupModel() {
		return initialRupModel;
	}

	public void setInitialRupModel(double[] initialRupModel) {
		this.initialRupModel = initialRupModel;
	}

	public double[] getMinimumRuptureRateBasis() {
		return minimumRuptureRateBasis;
	}

	public void setMinimumRuptureRateBasis(double[] minimumRuptureRateBasis) {
		this.minimumRuptureRateBasis = minimumRuptureRateBasis;
	}

	public double getRelativeSmoothnessWt() {
		return relativeSmoothnessWt;
	}

	public void setRelativeSmoothnessWt(double relativeSmoothnessWt) {
		this.relativeSmoothnessWt = relativeSmoothnessWt;
	}

	public List<MFD_InversionConstraint> getMfdEqualityConstraints() {
		return mfdEqualityConstraints;
	}

	public void setMfdEqualityConstraints(
			List<MFD_InversionConstraint> mfdEqualityConstraints) {
		this.mfdEqualityConstraints = mfdEqualityConstraints;
	}

	public List<MFD_InversionConstraint> getMfdInequalityConstraints() {
		return mfdInequalityConstraints;
	}

	public void setMfdInequalityConstraints(
			List<MFD_InversionConstraint> mfdInequalityConstraints) {
		this.mfdInequalityConstraints = mfdInequalityConstraints;
	}

	public double getMinimumRuptureRateFraction() {
		return minimumRuptureRateFraction;
	}

	public void setMinimumRuptureRateFraction(double minimumRuptureRateFraction) {
		this.minimumRuptureRateFraction = minimumRuptureRateFraction;
	}

}
