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
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM2pt1_Ruptures;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM3_Ruptures;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_Ruptures;

import com.google.common.base.Preconditions;

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
	private double relativeNucleationMFDConstraintWt;
	private double relativeMinimizationConstraintWt;
	private double relativeMomentConstraintWt;
	private double relativeParkfieldConstraintWt;
	private double[] aPrioriRupConstraint;
	private double[] initialRupModel;
	// these are the rates that should be used for water level computation. this will
	// often be set equal to initial rup model or a priori rup constraint
	private double[] minimumRuptureRateBasis;
	private List<MFD_InversionConstraint> mfdEqualityConstraints;
	private List<MFD_InversionConstraint> mfdInequalityConstraints;
	private double minimumRuptureRateFraction;

	private double relativeSmoothnessWt; // rupture rate smoothness (entropy)
	private double relativeEventRateSmoothnessWt; // parent section event-rate smoothing
	protected final static boolean D = true;  // for debugging
	
	private String metadata;
	
	public InversionConfiguration(
			boolean weightSlipRates,
			double relativePaleoRateWt, 
			double relativeMagnitudeEqualityConstraintWt,
			double relativeMagnitudeInequalityConstraintWt,
			double relativeRupRateConstraintWt, 
			double relativeParticipationSmoothnessConstraintWt,
			double participationConstraintMagBinSize,
			double relativeNucleationMFDConstraintWt,
			double relativeMinimizationConstraintWt,
			double relativeMomentConstraintWt,
			double relativeParkfieldConstraintWt,
			double[] aPrioriRupConstraint,
			double[] initialRupModel,
			double[] minimumRuptureRateBasis, 
			double relativeSmoothnessWt,
			double relativeEventRateSmoothnessWt,
			List<MFD_InversionConstraint> mfdEqualityConstraints,
			List<MFD_InversionConstraint> mfdInequalityConstraints,
			double minimumRuptureRateFraction,
			String metadata) {
		if (metadata == null || metadata.isEmpty())
			metadata = "";
		else
			metadata += "\n";
		this.weightSlipRates = weightSlipRates;
		metadata += "weightSlipRates: "+weightSlipRates;
		this.relativePaleoRateWt = relativePaleoRateWt;
		metadata += "\nrelativePaleoRateWt: "+relativePaleoRateWt;
		this.relativeMagnitudeEqualityConstraintWt = relativeMagnitudeEqualityConstraintWt;
		metadata += "\nrelativeMagnitudeEqualityConstraintWt: "+relativeMagnitudeEqualityConstraintWt;
		this.relativeMagnitudeInequalityConstraintWt = relativeMagnitudeInequalityConstraintWt;
		metadata += "\nrelativeMagnitudeInequalityConstraintWt: "+relativeMagnitudeInequalityConstraintWt;
		this.relativeRupRateConstraintWt = relativeRupRateConstraintWt;
		metadata += "\nrelativeRupRateConstraintWt: "+relativeRupRateConstraintWt;
		this.relativeParticipationSmoothnessConstraintWt = relativeParticipationSmoothnessConstraintWt;
		metadata += "\nrelativeParticipationSmoothnessConstraintWt: "+relativeParticipationSmoothnessConstraintWt;
		this.participationConstraintMagBinSize = participationConstraintMagBinSize;
		metadata += "\nparticipationConstraintMagBinSize: "+participationConstraintMagBinSize;
		this.relativeNucleationMFDConstraintWt = relativeNucleationMFDConstraintWt;
		metadata += "\nrelativeNucleationMFDConstraintWt: "+relativeNucleationMFDConstraintWt;
		this.relativeMinimizationConstraintWt = relativeMinimizationConstraintWt;
		metadata += "\nrelativeMinimizationConstraintWt: "+relativeMinimizationConstraintWt;
		this.relativeMomentConstraintWt = relativeMomentConstraintWt;
		metadata += "\nrelativeMomentConstraintWt: "+relativeMomentConstraintWt;
		this.relativeParkfieldConstraintWt = relativeParkfieldConstraintWt;
		metadata += "\nrelativeParkfieldConstraintWt: "+relativeParkfieldConstraintWt;
		this.aPrioriRupConstraint = aPrioriRupConstraint;
		this.initialRupModel = initialRupModel;
		this.minimumRuptureRateBasis = minimumRuptureRateBasis;
		this.relativeSmoothnessWt = relativeSmoothnessWt;
		metadata += "\nrelativeSmoothnessWt: "+relativeSmoothnessWt;
		this.relativeEventRateSmoothnessWt = relativeEventRateSmoothnessWt;
		metadata += "\nrelativeEventRateSmoothnessWt: "+relativeEventRateSmoothnessWt;
		this.mfdEqualityConstraints = mfdEqualityConstraints;
		this.mfdInequalityConstraints = mfdInequalityConstraints;
		this.minimumRuptureRateFraction = minimumRuptureRateFraction;
		metadata += "\nminimumRuptureRateFraction: "+minimumRuptureRateFraction;
		
		this.metadata = metadata;
	}
	
	public static final double DEFAULT_MFD_EQUALITY_WT = 10;
	public static final double DEFAULT_MFD_INEQUALITY_WT = 1000;
	
	/**
	 * This generates an inversion configuration for the given inversion model and rupture set
	 * 
	 * @param model
	 * @param rupSet
	 * @return
	 */
	public static InversionConfiguration forModel(InversionModels model, InversionFaultSystemRupSet rupSet) {
		double mfdEqualityConstraintWt = DEFAULT_MFD_EQUALITY_WT;
		double mfdInequalityConstraintWt = DEFAULT_MFD_INEQUALITY_WT;
		
		return forModel(model, rupSet, mfdEqualityConstraintWt, mfdInequalityConstraintWt);
	}
	
	/**
	 * This generates an inversion configuration for the given inversion model and rupture set
	 * 
	 * @param model
	 * @param rupSet
	 * @param offFaultAseisFactor aseismicity factor for off fault seismicity (affects off fault moment rate)
	 * @param mfdConstraintModifier multiplier for the a value of the MFD constraint.
	 * 1 for no modification, 1.3 for a 30 % increase, etc for the MFD constraint values.
	 * @param mfdEqualityConstraintWt weight of magnitude-distribution EQUALITY constraint relative to
	 * slip-rate constraint (recommended: 10)
	 * @param mfdInequalityConstraintWt weight of magnitude-distribution INEQUALITY constraint relative
	 * to slip-rate constraint (recommended:  1000)
	 * @return
	 */
	public static InversionConfiguration forModel(InversionModels model, InversionFaultSystemRupSet rupSet,
			double mfdEqualityConstraintWt, double mfdInequalityConstraintWt) {
		
		
		/* *******************************************
		 * COMMON TO ALL MODELS
		 * ******************************************* */
		// If true, slip rate misfit is % difference for each section (recommended since
		// it helps fit slow-moving faults).  If false, misfit is absolute difference.
		boolean weightSlipRates = true;
		
		// weight of paleo-rate constraint relative to slip-rate constraint (recommended: 1.0 if weightSlipRates=true, 0.01 otherwise)
		double relativePaleoRateWt = 1.0;
		
		// weight of magnitude-distribution EQUALITY constraint relative to slip-rate constraint (recommended: 10)
//		double mfdEqualityConstraintWt = 10;
		
		// weight of magnitude-distribution INEQUALITY constraint relative to slip-rate constraint (recommended:  1000)
//		double mfdInequalityConstraintWt = 1000;
		
		// magnitude-bin size for MFD participation smoothness constraint
		double participationConstraintMagBinSize = 0.1;
		
		// weight of rupture-rate minimization constraint weights relative to slip-rate constraint (recommended: 10,000)
		double relativeMinimizationConstraintWt = 0;
		
		// weight of entropy-maximization constraint (should smooth rupture rates) (recommended: 10000)
		double relativeSmoothnessWt = 0;
		
		// weight of Moment Constraint (set solution moment to equal deformation model moment) (recommended: 1e-17)
		double relativeMomentConstraintWt = 0;
		
		// weight of Parkfield rupture rate Constraint (recommended: 1000)
		double relativeParkfieldConstraintWt = 1000;
		
		// get MFC constraints
		List<MFD_InversionConstraint> mfdConstraints = rupSet.getInversionMFDs().getMFD_ConstraintsForNoAndSoCal();
		
		
		String metadata = "";
		
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
		
		// weight of nucleation MFD constraint weight - applied on subsection basis
		double relativeNucleationMFDConstraintWt;
		
		// weight of parent-section event-rate smoothness constraint
		double relativeEventRateSmoothnessWt;
		
		// fraction of the minimum rupture rate basis to be used as initial rates
		double minimumRuptureRateFraction;
		
		double[] aPrioriRupConstraint;
		double[] initialRupModel;
		double[] minimumRuptureRateBasis;
		
		SummedMagFreqDist targetOnFaultMFD =  rupSet.getInversionMFDs().getTargetOnFaultSupraSeisMFD();
//		System.out.println("SUPRA SEIS MFD = ");
//		System.out.println(rupSet.getInversionMFDs().getTargetOnFaultSupraSeisMFD());
		
		if (model.isConstrained()) {
			// CONSTRAINED BRANCHES
			if (model == InversionModels.CHAR_CONSTRAINED) {
				relativeParticipationSmoothnessConstraintWt = 0;
				relativeNucleationMFDConstraintWt = 1000;
				relativeEventRateSmoothnessWt = 0;
				relativeRupRateConstraintWt = 100;
				aPrioriRupConstraint = getUCERF2Solution(rupSet);
				initialRupModel = Arrays.copyOf(aPrioriRupConstraint, aPrioriRupConstraint.length); 
				minimumRuptureRateFraction = 0.01;
				minimumRuptureRateBasis = adjustStartingModel(getSmoothStartingSolution(rupSet,targetOnFaultMFD), mfdConstraints, rupSet, true);
				initialRupModel = adjustIsolatedSections(rupSet, initialRupModel);
				if (mfdInequalityConstraintWt>0.0 || mfdEqualityConstraintWt>0.0) initialRupModel = adjustStartingModel(initialRupModel, mfdConstraints, rupSet, true);
				initialRupModel = adjustParkfield(rupSet, initialRupModel);
			} else if (model == InversionModels.GR_CONSTRAINED) {
				relativeParticipationSmoothnessConstraintWt = 1000;
				relativeNucleationMFDConstraintWt = 0;
				relativeEventRateSmoothnessWt = 0;
				relativeRupRateConstraintWt = 0;
				aPrioriRupConstraint = null;
				initialRupModel = getSmoothStartingSolution(rupSet,targetOnFaultMFD);
				minimumRuptureRateFraction = 0.01;
				minimumRuptureRateBasis = adjustStartingModel(initialRupModel, mfdConstraints, rupSet, true);
				if (mfdInequalityConstraintWt>0.0 || mfdEqualityConstraintWt>0.0) initialRupModel = adjustStartingModel(initialRupModel, mfdConstraints, rupSet, true); 
				initialRupModel = adjustParkfield(rupSet, initialRupModel);
			} else
				throw new IllegalStateException("Unknown inversion model: "+model);
		} else {
			// UNCONSTRAINED BRANCHES
			relativeParticipationSmoothnessConstraintWt = 0;
			relativeNucleationMFDConstraintWt = 0;
			relativeEventRateSmoothnessWt = 0;
			relativeRupRateConstraintWt = 0;
			aPrioriRupConstraint = null;
			initialRupModel = new double[rupSet.getNumRuptures()];
			minimumRuptureRateBasis = null;
			minimumRuptureRateFraction = 0;
		}
		
		List<MFD_InversionConstraint> mfdInequalityConstraints = new ArrayList<MFD_InversionConstraint>();
		List<MFD_InversionConstraint> mfdEqualityConstraints = new ArrayList<MFD_InversionConstraint>();
		
		if (mfdEqualityConstraintWt>0.0 && mfdInequalityConstraintWt>0.0) {
			// we have both MFD constraints, apply a transition mag from equality to inequality
			double MFDTransitionMag = 7.85; // magnitude to switch from MFD equality to MFD inequality
			metadata += "\nMFDTransitionMag: "+MFDTransitionMag;
			mfdEqualityConstraints = restrictMFDConstraintMagRange(mfdConstraints, mfdConstraints.get(0).getMagFreqDist().getMinX(), MFDTransitionMag);
			mfdInequalityConstraints = restrictMFDConstraintMagRange(mfdConstraints, MFDTransitionMag, mfdConstraints.get(0).getMagFreqDist().getMaxX());
		} else if (mfdEqualityConstraintWt>0.0) {
			mfdEqualityConstraints = mfdConstraints;
		} else if (mfdInequalityConstraintWt>0.0) {
			mfdInequalityConstraints = mfdConstraints;
		} else {
			// no MFD constraints, do nothing
		}
		
		return new InversionConfiguration(
				weightSlipRates,
				relativePaleoRateWt,
				mfdEqualityConstraintWt,
				mfdInequalityConstraintWt,
				relativeRupRateConstraintWt,
				relativeParticipationSmoothnessConstraintWt,
				participationConstraintMagBinSize,
				relativeNucleationMFDConstraintWt,
				relativeMinimizationConstraintWt,
				relativeMomentConstraintWt,
				relativeParkfieldConstraintWt,
				aPrioriRupConstraint,
				initialRupModel,
				minimumRuptureRateBasis,
				relativeSmoothnessWt, relativeEventRateSmoothnessWt,
				mfdEqualityConstraints,
				mfdInequalityConstraints,
				minimumRuptureRateFraction,
				metadata);
	}
	
	// Adjust rates of 6 Parkfield M~6 ruptures (6- 7- and 8- subsection ruptures on the Parkfield section)
	// So that they sum to 1/25 per year for the initial model
	private static double[] adjustParkfield(FaultSystemRupSet rupSet,
			double[] initialRupModel) {
		
		double parkfieldMeanRate = 1.0/25.0; // Bakun et al. (2005)
		int parkfieldParentSectID = 32;
		
		// Find Parkfield M~6 ruptures
		List<Integer> potentialRups = rupSet.getRupturesForParentSection(parkfieldParentSectID);
		List<Integer> parkfieldRups = new ArrayList<Integer>();
		rupLoop:
		for (int i=0; i<potentialRups.size(); i++) {
			List<Integer> sects = rupSet.getSectionsIndicesForRup(potentialRups.get(i));
			// Make sure there are 6-8 subsections
			if (sects.size()<6 || sects.size()>8)
				continue rupLoop;
			// Make sure each section in rup is in Parkfield parent section
			for (int s=0; s<sects.size(); s++) {
				int parent = rupSet.getFaultSectionData(sects.get(s)).getParentSectionId();
				if (parent != parkfieldParentSectID)
					continue rupLoop;
			}
			parkfieldRups.add(potentialRups.get(i));
		}
		
		for (int i=0; i<parkfieldRups.size(); i++) 
			initialRupModel[parkfieldRups.get(i)] = parkfieldMeanRate/(double)parkfieldRups.size();
			
		return initialRupModel;
	}

	
	
	
	/**
	 * This method returns the input MFD constraint array with each constraint now restricted between minMag and maxMag.
	 * WARNING!  This doesn't interpolate.  For best results, set minMag & maxMag to points along original MFD constraint (i.e. 7.05, 7.15, etc)
	 * @param mfConstraints
	 * @param minMag
	 * @param maxMag
	 * @return newMFDConstraints
	 */
	private static List<MFD_InversionConstraint> restrictMFDConstraintMagRange(List<MFD_InversionConstraint> mfdConstraints, double minMag, double maxMag) {
		
		List<MFD_InversionConstraint> newMFDConstraints = new ArrayList<MFD_InversionConstraint>();
		
		for (int i=0; i<mfdConstraints.size(); i++) {
			IncrementalMagFreqDist originalMFD = mfdConstraints.get(i).getMagFreqDist();
			double delta = originalMFD.getDelta();
			IncrementalMagFreqDist newMFD = new IncrementalMagFreqDist(minMag, maxMag, (int) Math.round((maxMag-minMag)/delta + 1.0)); 
			newMFD.setTolerance(delta/2.0);
			for (double m=minMag; m<=maxMag; m+=delta) {
				// WARNING!  This doesn't interpolate.  For best results, set minMag & maxMag to points along original MFD constraint (i.e. 7.05, 7.15, etc)
				newMFD.set(m, originalMFD.getClosestY(m));
			}
			newMFDConstraints.add(i,new MFD_InversionConstraint(newMFD, mfdConstraints.get(i).getRegion()));	
		}
		
		return newMFDConstraints;
	}
	
	
	
	/**
	 * This method adjusts the starting solution for "wall-to-wall" (section-long) ruptures on any isolated sections (sections
	 * that only have ruptures on that section).  The starting solution is ONLY adjusted if that rupture currently has a 0 rate.
	 * The new rupture rate is the average slip rate for that section divided by the average slip of that rupture.
	 * @param rupSet
	 * @param initialRupModel
	 * @return initialRupModel
	 */
	public static double[] adjustIsolatedSections(FaultSystemRupSet rupSet, double[] initialRupModel) {
		
		List<Integer> isolatedParents = new ArrayList<Integer>();
		List<String> isolatedParentNames = new ArrayList<String>();
		List<Integer> nonIsolatedParents = new ArrayList<Integer>();
		
		// Find "isolated" parent sections that only have ruptures on that section
		for (int sect=0; sect<rupSet.getNumSections(); sect++) {
			int parentId = rupSet.getFaultSectionData(sect).getParentSectionId();
			List<Integer> rupsOnSect = rupSet.getRupturesForSection(sect);
			
			checkForRupsOnDifferentParents:
			for (int i=0; i<rupsOnSect.size(); i++) {
				int rup = rupsOnSect.get(i);
				List<Integer> sects = rupSet.getSectionsIndicesForRup(rup);
				for (int j=0; j<sects.size(); j++) {
					int newSect = sects.get(j);
					if (parentId != rupSet.getFaultSectionData(newSect).getParentSectionId()) {
						if (!nonIsolatedParents.contains(parentId))
							nonIsolatedParents.add(parentId);
						if (isolatedParents.contains(parentId)) {
							isolatedParents.remove(isolatedParents.indexOf(parentId));
							isolatedParentNames.remove(rupSet.getFaultSectionDataList().get(newSect).getParentSectionName());
						}
						break checkForRupsOnDifferentParents;
					}
				}
			}
			if (!isolatedParents.contains(parentId) && !nonIsolatedParents.contains(parentId)) {
				isolatedParents.add(parentId);
				isolatedParentNames.add(rupSet.getFaultSectionDataList().get(sect).getParentSectionName());
			}		
		}

		// Find wall-to-wall rup for each isolated parent section
		for (int p=0; p<isolatedParents.size(); p++)  {
			int parentId = isolatedParents.get(p);
			List<Integer> sectsForParent = new ArrayList<Integer>();			
			for (int sect=0; sect<rupSet.getNumSections(); sect++) 
				if (rupSet.getFaultSectionData(sect).getParentSectionId()==parentId)sectsForParent.add(sect);
					
			RuptureLoop:
			for (int rup=0; rup<rupSet.getNumRuptures(); rup++) {
				List<Integer> sects = rupSet.getSectionsIndicesForRup(rup);
				if (sects.size()!=sectsForParent.size()) continue;
				for (int sect:sects) {
					if (!sectsForParent.contains(sect))
						continue RuptureLoop;
				}
				// We have found the "wall-to-wall" rupture for this isolated parent section.
				// If initial rup rate is 0, we will adjust the rate.
				if (initialRupModel[rup]==0) {
					double avgSlipRate = 0;
					for(int sect:sects) {
						if (!Double.isNaN(rupSet.getSlipRateForSection(sect)))
							avgSlipRate+=rupSet.getSlipRateForSection(sect);
					}
					avgSlipRate/=sects.size();  // average slip rate of sections in rup
					double[] rupSlip = rupSet.getSlipOnSectionsForRup(rup);
					double avgSlip = 0;
					for(int i=0; i<rupSlip.length; i++) avgSlip+=rupSlip[i];
					avgSlip/=rupSlip.length; // average rupture slip
					double charRupRate = avgSlipRate/avgSlip; // rate of rup that will, on average, match slip rate
					System.out.println("Adjusting starting rupture rate for isolated fault "+isolatedParentNames.get(p));
					initialRupModel[rup] = charRupRate;
				}	
				break;	
			}
		}
		
		return initialRupModel;
	}
	
	

	
	/**
	 * This method adjusts the starting model to ensure that for each MFD inequality constraint magnitude-bin, the starting model is below the MFD.
	 * If adjustOnlyIfOverMFD = false, it will adjust the starting model so that it's MFD equals the MFD constraint.
	 * It will uniformly reduce the rates of ruptures in any magnitude bins that need adjusting.
	 */
	private static double[] adjustStartingModel(double[] initialRupModel,
			List<MFD_InversionConstraint> mfdInequalityConstraints, FaultSystemRupSet rupSet, boolean adjustOnlyIfOverMFD) {
		
		double[] rupMeanMag = rupSet.getMagForAllRups();
		
		
		for (int i=0; i<mfdInequalityConstraints.size(); i++) {
			double[] fractRupsInside = rupSet.getFractRupsInsideRegion(mfdInequalityConstraints.get(i).getRegion(), false);
			IncrementalMagFreqDist targetMagFreqDist = mfdInequalityConstraints.get(i).getMagFreqDist();
			IncrementalMagFreqDist startingModelMagFreqDist = new IncrementalMagFreqDist(targetMagFreqDist.getMinX(), targetMagFreqDist.getNum(), targetMagFreqDist.getDelta());
			startingModelMagFreqDist.setTolerance(0.1);
			
			// Find the starting model MFD
			for(int rup=0; rup<rupSet.getNumRuptures(); rup++) {
				double mag = rupMeanMag[rup];
				double fractRupInside = fractRupsInside[rup];
				if (fractRupInside > 0) 
					if (mag<8.5)  // b/c the mfdInequalityConstraints only go to M8.5!
						startingModelMagFreqDist.add(mag, fractRupInside * initialRupModel[rup]);
			}
			
			// Find the amount to adjust starting model MFD to be below or equal to Target MFD
			IncrementalMagFreqDist adjustmentRatio = new IncrementalMagFreqDist(targetMagFreqDist.getMinX(), targetMagFreqDist.getNum(), targetMagFreqDist.getDelta());
			for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m+= targetMagFreqDist.getDelta()) {
				if (adjustOnlyIfOverMFD == false)
					adjustmentRatio.set(m, targetMagFreqDist.getClosestY(m) / startingModelMagFreqDist.getClosestY(m));
				else {
					if (startingModelMagFreqDist.getClosestY(m) > targetMagFreqDist.getClosestY(m))
						adjustmentRatio.set(m, targetMagFreqDist.getClosestY(m) / startingModelMagFreqDist.getClosestY(m));
					else
						adjustmentRatio.set(m, 1.0);
				}
			}
			
			// Adjust initial model rates
			for(int rup=0; rup<rupSet.getNumRuptures(); rup++) {
				double mag = rupMeanMag[rup];
				if (!Double.isNaN(adjustmentRatio.getClosestY(mag)) && !Double.isInfinite(adjustmentRatio.getClosestY(mag)))
					initialRupModel[rup] = initialRupModel[rup] * adjustmentRatio.getClosestY(mag);
			}
			
		}
		
		
/*		// OPTIONAL: Adjust rates of largest rups to equal global target MFD
		IncrementalMagFreqDist targetMagFreqDist = UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850).getMagFreqDist();
		IncrementalMagFreqDist startingModelMagFreqDist = new IncrementalMagFreqDist(targetMagFreqDist.getMinX(), targetMagFreqDist.getNum(), targetMagFreqDist.getDelta());
		startingModelMagFreqDist.setTolerance(0.1);
		for(int rup=0; rup<rupSet.getNumRuptures(); rup++) {
			double mag = rupMeanMag[rup];
			if (mag<8.5)
				startingModelMagFreqDist.add(mag, initialRupModel[rup]);
		}	
		IncrementalMagFreqDist adjustmentRatio = new IncrementalMagFreqDist(targetMagFreqDist.getMinX(), targetMagFreqDist.getNum(), targetMagFreqDist.getDelta());
		for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m+= targetMagFreqDist.getDelta()) {
			if (m>8.0)	adjustmentRatio.set(m, targetMagFreqDist.getClosestY(m) / startingModelMagFreqDist.getClosestY(m));
			else adjustmentRatio.set(m, 1.0);
		}
		for(int rup=0; rup<rupSet.getNumRuptures(); rup++) {
			double mag = rupMeanMag[rup];
			if (!Double.isNaN(adjustmentRatio.getClosestY(mag)) && !Double.isInfinite(adjustmentRatio.getClosestY(mag)))
				initialRupModel[rup] = initialRupModel[rup] * adjustmentRatio.getClosestY(mag);
		}	*/
		
		
		return initialRupModel;
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
	
	

	/**
	 * @deprecated
	 */
	public static List<MFD_InversionConstraint> getGriddedConstraints(
			UCERF2_MFD_ConstraintFetcher UCERF2Constraints, Region region,
			double latBoxSize, double lonBoxSize) {
		List<MFD_InversionConstraint> mfdEqualityConstraints = new ArrayList<MFD_InversionConstraint>();
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
	 * Probably has to be revised for FM 3.1? currently not used
	 * 
	 * @param findUCERF2_Rups
	 * @param faultSystemRupSet
	 * @return
	 */
	public static ArrayList<double[]> getUCERF2MagsAndrates(FaultSystemRupSet faultSystemRupSet) {
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
		
		return findUCERF2_Rups.getMagsAndRatesForRuptures();
	}
	
	/**
	 * Probably has to be revised for FM 3.1? currently not used
	 * 
	 * @param findUCERF2_Rups
	 * @param faultSystemRupSet
	 * @return
	 */
	public static double[] getUCERF2Solution(FaultSystemRupSet faultSystemRupSet) {
		ArrayList<double[]> ucerf2_magsAndRates = getUCERF2MagsAndrates(faultSystemRupSet);
		int numRuptures=faultSystemRupSet.getNumRuptures();
		double[] initial_state = new double[numRuptures];
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
	 * This creates a smooth starting solution, which partitions the available rates from the target MagFreqDist
	 * to each rupture in the rupture set.  So the total rate of all ruptures in a given magnitude bin as defined by the MagFreqDist 
	 * is partitioned among all the ruptures with a magnitude in that bin, in proportion to the minimum slip rate section for each rupture.
	 * NaN slip rates are treated as zero (so any ruptures with a NaN or 0 slip rate section will have a zero rate in the returned starting solution).
	 * 
	 * Making rates proportional to the minimum slip rate section of a rupture was found to work better than making the rates proportional to the mean slip rate
	 * for each rupture.  Also, the current code does not account for overlap of ruptures.  This was tested and did not lead to better starting solutions, 
	 * and in addition had a great computational cost.
	 * 
	 * @param faultSystemRupSet, targetMagFreqDist
	 * @return initial_state
	 */
	public static double[] getSmoothStartingSolution(
			FaultSystemRupSet faultSystemRupSet, IncrementalMagFreqDist targetMagFreqDist) {
		List<List<Integer>> rupList = faultSystemRupSet.getSectionIndicesForAllRups();
		
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		double[] sectSlipRateReduced = faultSystemRupSet.getSlipRateForAllSections(); 
		int numRup = rupMeanMag.length;
		double[] initial_state = new double[numRup];  // starting model to be returned
		double[] minimumSlipRate = new double[numRup];  // mean slip rate per section for each rupture
		
		// Calculate minimum slip rates for ruptures
		// If there are NaN slip rates, treat them as 0
		for (int rup=0; rup<numRup; rup++) {
			List<Integer> sects = faultSystemRupSet.getSectionsIndicesForRup(rup);
			minimumSlipRate[rup] = Double.POSITIVE_INFINITY;
			for (int i=0; i<sects.size(); i++) {
				int sect = sects.get(i);
				if (Double.isNaN(sectSlipRateReduced[sect])  || sectSlipRateReduced[sect] == 0)  { 
					minimumSlipRate[rup] = 0;
				} else 	if (sectSlipRateReduced[sect] < minimumSlipRate[rup]) {
					minimumSlipRate[rup] = sectSlipRateReduced[sect];
				}
			}
		}
		

		// Find magnitude distribution of ruptures (as discretized)
		double minMag = Math.floor(faultSystemRupSet.getMinMag()*10.0)/10.0;
		double maxMag = Math.ceil(faultSystemRupSet.getMaxMag()*10.0)/10.0;
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(minMag,(int) Math.round((maxMag-minMag)*10+1),0.1);
		magHist.setTolerance(0.05);
		for(int rup=0; rup<numRup;rup++) {
			// Each bin in the magnitude histogram should be weighted by the mean slip rates of those ruptures 
			// (since later we weight the ruptures by the mean slip rate, which would otherwise result in 
			// starting solution that did not match target MFD if the mean slip rates per rupture 
			// differed between magnitude bins)
			if (minimumSlipRate[rup]!=0) 
				magHist.add(rupMeanMag[rup], minimumSlipRate[rup]);  // each bin
			else magHist.add(rupMeanMag[rup], 1E-4);
		}
		
		
		// Set up initial (non-normalized) target MFD rates for each rupture, normalized by meanSlipRate
		for (int rup=0; rup<numRup; rup++) {
			initial_state[rup] = targetMagFreqDist.getClosestY(rupMeanMag[rup]) * minimumSlipRate[rup] / magHist.getClosestY(rupMeanMag[rup]);
			if (Double.isNaN(initial_state[rup]) || Double.isInfinite(initial_state[rup]))
				throw new IllegalStateException("Pre-normalization initial_state["+rup+"] = "+initial_state[rup]);
		}
		
		
		// Find normalization for all ruptures (so that MFD matches target MFD normalization)
		// Can't just add up all the mag bins to normalize because some bins don't have ruptures.
		// Instead let's choose one mag bin (that we know has rups) that has rups and normalize
		// all bins by the amount it's off:
		double totalEventRate=0;
		for (int rup=0; rup<numRup; rup++) {
			if (rupMeanMag[rup]>7.0 && rupMeanMag[rup]<=7.1)
				totalEventRate += initial_state[rup];
		}
		double normalization = targetMagFreqDist.getClosestY(7.0)/totalEventRate;	
		if (targetMagFreqDist.getClosestY(7.0)==0)
			throw new IllegalStateException("targetMagFreqDist.getClosestY(7.0) = 0.  Check rupSet.getInversionMFDs().getTargetOnFaultSupraSeisMFD()");
		// Adjust rates by normalization to match target MFD total event rates
		for (int rup=0; rup<numRup; rup++) {
			initial_state[rup]=initial_state[rup]*normalization;
			if (Double.isNaN(initial_state[rup]) || Double.isInfinite(initial_state[rup]))
				throw new IllegalStateException("initial_state["+rup+"] = "+initial_state[rup]
						+" (norm="+normalization+", totalEventRate="+totalEventRate+")");
		}
		
		
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
	
	
	public double getRelativeMomentConstraintWt() {
		return relativeMomentConstraintWt;
	}

	public void setRelativeMomentConstraintWt(
			double relativeMomentConstraintWt) {
		this.relativeMomentConstraintWt = relativeMomentConstraintWt;
	}

	public double getRelativeParkfieldConstraintWt() {
		return relativeParkfieldConstraintWt;
	}

	public void setRelativeParkfieldConstraintWt(
			double relativeParkfieldConstraintWt) {
		this.relativeParkfieldConstraintWt = relativeParkfieldConstraintWt;
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

	public double getRelativeNucleationMFDConstraintWt() {
		return relativeNucleationMFDConstraintWt;
	}

	public void setRelativeNucleationMFDConstraintWt(double relativeNucleationMFDConstraintWt) {
		this.relativeNucleationMFDConstraintWt = relativeNucleationMFDConstraintWt;
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
	
	public String getMetadata() {
		return metadata;
	}
	
	public void updateRupSetInfoString(FaultSystemRupSet rupSet) {
		String info = rupSet.getInfoString();
		info += "\n\n****** Inversion Configuration Metadata ******";
		info += "\n"+getMetadata();
		info += "\n**********************************************";
		rupSet.setInfoString(info);
	}

	public double getEventRateSmoothnessWt() {
		return relativeEventRateSmoothnessWt;
	}

	public void setEventRateSmoothnessWt(double relativeEventRateSmoothnessWt) {
		this.relativeEventRateSmoothnessWt = relativeEventRateSmoothnessWt;
	}
	

}
