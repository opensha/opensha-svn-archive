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

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
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
	private double relativeMinimizationConstraintWt;
	private double relativeMomentConstraintWt;
	private double[] aPrioriRupConstraint;
	private double[] initialRupModel;
	// thses are the rates that should be used for water level computation. this will
	// often be set equal to initial rup model or a priori rup constraint
	private double[] minimumRuptureRateBasis;
	private List<MFD_InversionConstraint> mfdEqualityConstraints;
	private List<MFD_InversionConstraint> mfdInequalityConstraints;
	private double minimumRuptureRateFraction;

	private double relativeSmoothnessWt;
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
			double relativeMinimizationConstraintWt,
			double relativeMomentConstraintWt,
			double[] aPrioriRupConstraint,
			double[] initialRupModel,
			double[] minimumRuptureRateBasis, 
			double relativeSmoothnessWt,
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
		this.relativeMinimizationConstraintWt = relativeMinimizationConstraintWt;
		metadata += "\nrelativeMinimizationConstraintWt: "+relativeMinimizationConstraintWt;
		this.relativeMomentConstraintWt = relativeMomentConstraintWt;
		metadata += "\nrelativeMomentConstraintWt: "+relativeMomentConstraintWt;
		this.aPrioriRupConstraint = aPrioriRupConstraint;
		this.initialRupModel = initialRupModel;
		this.minimumRuptureRateBasis = minimumRuptureRateBasis;
		this.relativeSmoothnessWt = relativeSmoothnessWt;
		this.mfdEqualityConstraints = mfdEqualityConstraints;
		this.mfdInequalityConstraints = mfdInequalityConstraints;
		this.minimumRuptureRateFraction = minimumRuptureRateFraction;
		metadata += "\nminimumRuptureRateFraction: "+minimumRuptureRateFraction;
		
		this.metadata = metadata;
	}
	
	public static final double DEFAULT_MFD_EQUALITY_WT = 10;
	public static final double DEFAULT_MFD_INEQUALITY_WT = 1000;
	private static final String DeformationModelOffFaultMoRateData = null;
	
	/**
	 * This generates an inversion configuration for the given inversion model and rupture set
	 * 
	 * @param model
	 * @param rupSet
	 * @return
	 */
	public static InversionConfiguration forModel(InversionModels model, FaultSystemRupSet rupSet) {
		double offFaultAseisFactor = 0;
		double mfdConstraintModifier = 1;
		double mfdEqualityConstraintWt = DEFAULT_MFD_EQUALITY_WT;
		double mfdInequalityConstraintWt = DEFAULT_MFD_INEQUALITY_WT;
		
		return forModel(model, rupSet, offFaultAseisFactor, mfdConstraintModifier, mfdEqualityConstraintWt, mfdInequalityConstraintWt);
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
	public static InversionConfiguration forModel(InversionModels model, FaultSystemRupSet rupSet,
			double offFaultAseisFactor, double mfdConstraintModifier,
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
		
		String metadata = "";
		metadata += "offFaultAseisFactor: "+offFaultAseisFactor;
		metadata += "\nmfdConstraintModifier: "+mfdConstraintModifier;
		
		boolean ucerf3MFDs = true;
		metadata += "\nucerf3MFDs: "+ucerf3MFDs;
		
		List<MFD_InversionConstraint> mfdConstraints = getOriginalConstraints(rupSet, ucerf3MFDs, mfdConstraintModifier);
		
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
		
		double bilinearTransitionMag = 7.6;
		if (model == InversionModels.CHAR)
			metadata += "\nbilinearTransitionMag: "+bilinearTransitionMag;
		mfdConstraints = getTargetMFDConstraints(mfdConstraints, model, rupSet, offFaultAseisFactor, bilinearTransitionMag);
		
		switch (model) {
		case CHAR:
			relativeParticipationSmoothnessConstraintWt = 0;
			relativeRupRateConstraintWt = 100;
			aPrioriRupConstraint = getUCERF2Solution(rupSet);
			initialRupModel = Arrays.copyOf(aPrioriRupConstraint, aPrioriRupConstraint.length);
			minimumRuptureRateFraction = 0.01;
			minimumRuptureRateBasis = adjustStartingModel(getSmoothStartingSolution(rupSet,getGR_Dist(rupSet, 1.0, 9.0)), mfdConstraints, rupSet, true);
			initialRupModel = adjustIsolatedSections(rupSet, initialRupModel);
			if (mfdInequalityConstraintWt>0.0 || mfdEqualityConstraintWt>0.0) initialRupModel = adjustStartingModel(initialRupModel, mfdConstraints, rupSet, true);
			break;
		case GR:
			relativeParticipationSmoothnessConstraintWt = 1000;
			relativeRupRateConstraintWt = 0;
			aPrioriRupConstraint = null;
			initialRupModel = getSmoothStartingSolution(rupSet,getGR_Dist(rupSet, 1.0, 9.0));
			minimumRuptureRateFraction = 0.01;
			minimumRuptureRateBasis = adjustStartingModel(initialRupModel, mfdConstraints, rupSet, true);
			if (mfdInequalityConstraintWt>0.0 || mfdEqualityConstraintWt>0.0) initialRupModel = adjustStartingModel(initialRupModel, mfdConstraints, rupSet, true); 
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
				relativeMinimizationConstraintWt,
				relativeMomentConstraintWt,
				aPrioriRupConstraint,
				initialRupModel,
				minimumRuptureRateBasis,
				relativeSmoothnessWt,
				mfdEqualityConstraints,
				mfdInequalityConstraints,
				minimumRuptureRateFraction,
				metadata);
	}
	
	/**
	 * This builds the original MFD constraints without making any modifications for minimum magnitudes of off fault moment.
	 * 
	 * @param rupSet
	 * @param ucerf3MFDs
	 * @param mfdConstraintModifier
	 * @return
	 */
	public static List<MFD_InversionConstraint> getOriginalConstraints(FaultSystemRupSet rupSet, boolean ucerf3MFDs, double mfdConstraintModifier) {
		UCERF2_MFD_ConstraintFetcher ucerf2Constraints = null;
		if (!ucerf3MFDs)
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
		
		
		List<MFD_InversionConstraint> mfdConstraints = new ArrayList<MFD_InversionConstraint>();
		// add MFD constraint for Northern CA
		if (ucerf3MFDs) {
			mfdConstraints.add(UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.NO_CA_1850));
		} else {
			ucerf2Constraints.setRegion(noCal);
			mfdConstraints.add(ucerf2Constraints.getTargetMFDConstraint());
		}
		// add MFD constraint for Southern CA
		if (entire_region != noCal) {
			// don't add so cal if we're just doing a no cal inversion
			if (ucerf3MFDs) {
				mfdConstraints.add(UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.SO_CA_1850));
			} else {
				ucerf2Constraints.setRegion(soCal);
				mfdConstraints.add(ucerf2Constraints.getTargetMFDConstraint());
			}
		}
		
		if (mfdConstraintModifier != 1 && mfdConstraintModifier > 0) {
			// this multiples each MFD bin by the mfdConstraintModifier
			for (int i=0; i<mfdConstraints.size(); i++) {
				IncrementalMagFreqDist magDist = mfdConstraints.get(i).getMagFreqDist();
				for (double m=magDist.getMinX(); m<=magDist.getMaxX(); m+=magDist.getDelta()) {
					double setVal = mfdConstraintModifier * magDist.getClosestY(m);
					mfdConstraints.get(i).getMagFreqDist().set(m, setVal);
				}
			}
		}
		
		return mfdConstraints;
	}
	
	/**
	 * This modifies the MFD constraints for minimum magnitudes and off fault aseismicity
	 * 
	 * @param mfdConstraints
	 * @param model
	 * @param rupSet
	 * @param offFaultAseisFactor
	 * @param bilinearTransitionMag only applicable to CHAR branch
	 * @return
	 */
	public static List<MFD_InversionConstraint> getTargetMFDConstraints(
			List<MFD_InversionConstraint> mfdConstraints,
			InversionModels model,
			FaultSystemRupSet rupSet,
			double offFaultAseisFactor,
			double bilinearTransitionMag) {
		
		switch (model) {
		case CHAR:
			mfdConstraints = makeMFDConstraintsBilinear(mfdConstraints, findBValueForMomentRateReduction(bilinearTransitionMag,
					rupSet, offFaultAseisFactor), bilinearTransitionMag);
			mfdConstraints = accountForVaryingMinMag(mfdConstraints, rupSet);
			break;
		case GR:
			mfdConstraints = reduceMFDConstraint(mfdConstraints, getSeisMomentFractionOffFault(rupSet, offFaultAseisFactor));
			mfdConstraints = accountForVaryingMinMag(mfdConstraints, rupSet);
			break;
		case UNCONSTRAINED:
			mfdConstraints = reduceMFDConstraint(mfdConstraints, getSeisMomentFractionOffFault(rupSet, offFaultAseisFactor));
			mfdConstraints = accountForVaryingMinMag(mfdConstraints, rupSet);
			break;

		default:
			break;
		}
		
		return mfdConstraints;
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
					for(int sect:sects) avgSlipRate+=rupSet.getSlipRateForSection(sect);
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
	 * This returns the moment rate reductions for each section in the given rupture set according to the
	 * given inversion model
	 * @param rupSet
	 * @param model
	 * @return moRateReductions
	 */
	public static double[] getMomentRateReductionsForSections(FaultSystemRupSet rupSet, InversionModels model) {
		double[] moRateReductions = new double[rupSet.getNumSections()];

		if (model == InversionModels.CHAR) {
			// simple hardcoded at 7%
			for (int i=0; i<moRateReductions.length; i++)
				moRateReductions[i] = 0.07;
		} else if (model == InversionModels.GR || model == InversionModels.UNCONSTRAINED) {
			// based on mMin and mMax
			double bValue = 1d;
			for (int sectIndex=0; sectIndex<moRateReductions.length; sectIndex++) {
				double magLower = rupSet.getMinMagForSection(sectIndex);
				double magUpper = rupSet.getMaxMagForSection(sectIndex);
				moRateReductions[sectIndex] = FaultSystemRupSetCalc.getFractMomentReductionForSmallMags(magLower, magUpper, bValue);
			}
		} else {
			throw new IllegalArgumentException("Can't create moment rate reductions for: "+model);
		}
		return moRateReductions;
	}
	
	/**
	 * This method lowers an MFD constraint to account for spatially-varying minimum magnitudes on faults.
	 */
	private static List<MFD_InversionConstraint> accountForVaryingMinMag(
			List<MFD_InversionConstraint> mfdInequalityConstraints, FaultSystemRupSet rupSet) {
		
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

	
	/**
	 * This method multiplies the input MFD constraints by fractionRateReduction.
	 * For example, if the off-fault rates are 10% of the rates, setting fractionRateReduction to 0.1 will reduce the MFDs to 90% of their previous rates.
	 */
	private static List<MFD_InversionConstraint> reduceMFDConstraint(
			List<MFD_InversionConstraint> mfdInequalityConstraints,
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
	 * This is for use with Bilinear MFD Constraint. 
	 * The returned b-value is the b-value below the transition magnitude (assuming the previous MFD was G-R with b=1) to achieve the desired moment rate reduction.
	 */
	private static double findBValueForMomentRateReduction(double transitionMag, FaultSystemRupSet rupSet, double offFaultAseisFactor) {
		
		DeformationModelOffFaultMoRateData moRateData = new DeformationModelOffFaultMoRateData();
		double totalMomentOffFaults =  moRateData.getTotalOffFaultMomentRate(rupSet.getFaultModel(), rupSet.getDeformationModel());
		double momentRateToRemove = totalMomentOffFaults*(1d-offFaultAseisFactor);
		
		// Use the mag-dist for the whole region since the deformation model off-fault moment #s from Kaj are also for the whole region
		IncrementalMagFreqDist magDist = UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850).getMagFreqDist();
		
		// Find total moment below transition magnitude (on-fault + off-fault)
		double totalMomentBelowTransition = 0;
		for (double mag=magDist.getMinX(); mag<=transitionMag; mag+=magDist.getDelta()) {
			totalMomentBelowTransition += magDist.getMomentRate(mag);
		}
		if (D) System.out.println("Total Moment below the Transition Magnitude of "+transitionMag+" = "+totalMomentBelowTransition);
		
		if (D) System.out.println("The amount of moment to remove from MFD = "+momentRateToRemove);
		if (momentRateToRemove>=totalMomentBelowTransition)
			throw new IllegalStateException("This is not going to work. The total moment below your transition magnitude is less than the off-fault moment");
		
		double momentRatio = 1.0 - momentRateToRemove/totalMomentBelowTransition;  // fraction of moment that should be on-fault below transition magnitude
		
		// b-value below transition magnitude that will achieve desired moment reduction
		// This integrates over all mags up to the transition mag and assumes an initial b-value of 1.0
		double bValue = (3.0*momentRatio-1.0)/(2.0*momentRatio);  // b-value below transition magnitude that will achieve desired moment reduction
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
	
	/**
	 * This Applies the offFaultAsiesFactor, and considers the aseismicity and 
	 * coupling coefficients (including the default on the former), but does not include
	 * the moment reduction for sub-seismo ruptures.  In other words, it's for the pure,
	 * off fault seismicity.
	 * 
	 * @param rupSet
	 * @param offFaultAseisFactor
	 * @return
	 */
	public static double getSeisMomentFractionOffFault(FaultSystemRupSet rupSet, double offFaultAseisFactor) {
		// These values are from an e-mail from Kaj dated 2/29/12, for Zeng model see 3/5/12 e-mail
		
		DeformationModelOffFaultMoRateData offFaultData = new DeformationModelOffFaultMoRateData();
		double offFaultMoment = offFaultData.getTotalOffFaultMomentRate(rupSet.getFaultModel(), rupSet.getDeformationModel());
		offFaultMoment *= (1-offFaultAseisFactor); // apply off fault asiesmicity factor
		double onFaultTotal = rupSet.getTotalOrigMomentRate();
		
		double overallTotal = offFaultMoment + onFaultTotal;
		
		return offFaultMoment / overallTotal;
	}
	
	/**
	 * This method changes the input MFD constraints (WHICH IT ASSUMES ARE G-R WITH b=1) by changing the b-Value below a transition magnitude.
	 * The returned MFDs are G-R both below and above the transition magnitude, b=1 above it, and the specified b-value below it.
	 */
	private static List<MFD_InversionConstraint> makeMFDConstraintsBilinear(
			List<MFD_InversionConstraint> mfdConstraints, double bValueBelowTransition, double transitionMag) {
		
		if (D) for (int i=0; i<mfdConstraints.size(); i++) {
			System.out.println("Initial total moment rate = "+mfdConstraints.get(0).getMagFreqDist().getTotalMomentRate()+" for MFD constraint #"+i);
			}
		
		for (int i=0; i<mfdConstraints.size(); i++) {
			double totalMomentRate = 0; double totalMomentRate2 = 0;
			for (double mag=mfdConstraints.get(i).getMagFreqDist().getMinX(); mag<=transitionMag; mag+=mfdConstraints.get(i).getMagFreqDist().getDelta()) {
				if (mag<=transitionMag)
					totalMomentRate += mfdConstraints.get(i).getMagFreqDist().getMomentRate(mag);
				double setVal=mfdConstraints.get(i).getMagFreqDist().getY(mag)*Math.pow(10.0, (transitionMag-mag)*(bValueBelowTransition-1.0));
				mfdConstraints.get(i).getMagFreqDist().set(mag, setVal);
				if (mag<=transitionMag)
					totalMomentRate2 += mfdConstraints.get(i).getMagFreqDist().getMomentRate(mag);
			}
		}
		
		if (D) for (int i=0; i<mfdConstraints.size(); i++) {
			System.out.println("New total moment rate = "+mfdConstraints.get(0).getMagFreqDist().getTotalMomentRate()+" for MFD constraint #"+i);
			}
		
		return mfdConstraints;
	}

	
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
		boolean[] flagRup = new boolean[numRup]; // flag rup if it has any NaN or zero slip-rate sections
		
		// Calculate mean slip rates for ruptures
		// If there are NaN slip rates, treat them as 0
//		for (int rup=0; rup<meanSlipRate.length; rup++) {
//			List<Integer> sects = faultSystemRupSet.getSectionsIndicesForRup(rup);
//			double totalOfSlipRates = 0;
//			for (int i=0; i<sects.size(); i++) {
//				int sect = sects.get(i);
//				if (Double.isNaN(sectSlipRateReduced[sect])  || sectSlipRateReduced[sect] == 0)  { // if rupture has any NaN or zero slip-rate sections, flag it!
//					flagRup[rup] = true;
//				} else 	totalOfSlipRates+=sectSlipRateReduced[sect];
//			}
//			meanSlipRate[rup] = totalOfSlipRates/sects.size(); // average mean slip rate for sections in rupture
//		}
		
		// Calculate minimum slip rates for ruptures
		// If there are NaN slip rates, treat them as 0
//		double[] numRupturesOnSlowestSection = new double[numRup];
//		double[] maxNumRupturesOnSectInRup = new double[numRup];
		for (int rup=0; rup<meanSlipRate.length; rup++) {
			List<Integer> sects = faultSystemRupSet.getSectionsIndicesForRup(rup);
			double minimumSlipRate = Double.POSITIVE_INFINITY;
			for (int i=0; i<sects.size(); i++) {
				int sect = sects.get(i);
//				if (faultSystemRupSet.getRupturesForSection(sect).size()>maxNumRupturesOnSectInRup[sect]) maxNumRupturesOnSectInRup[sect] = faultSystemRupSet.getRupturesForSection(sect).size();
				if (Double.isNaN(sectSlipRateReduced[sect])  || sectSlipRateReduced[sect] == 0)  { // if rupture has any NaN or zero slip-rate sections, flag it!
					minimumSlipRate = 0;
//					numRupturesOnSlowestSection[rup] = 1;
				} else 	if (sectSlipRateReduced[sect] < minimumSlipRate) {
					minimumSlipRate = sectSlipRateReduced[sect];
//					numRupturesOnSlowestSection[rup] = faultSystemRupSet.getRupturesForSection(sect).size();
				}
			}
			meanSlipRate[rup] = minimumSlipRate; // use minimum slip rate instead of mean slip rate for histogram below
		}
		
			
		if (D) for (int i=0; i<sectSlipRateReduced.length; i++) {
			if (Double.isNaN(sectSlipRateReduced[i])  || sectSlipRateReduced[i] == 0)  {
				System.out.println("Sect Slip Rate Reduced ["+i+"] = "+sectSlipRateReduced[i]);
			}
		}
		
	
		// Find magnitude distribution of ruptures (as discretized)
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,40,0.1);
		magHist.setTolerance(0.05);	// this makes it a histogram
		for(int rup=0; rup<numRup;rup++) {
			// magHist.add(rupMeanMag[rup], 1.0);
			// Each bin in the magnitude histogram should be weighted by the mean slip rates of those ruptures 
			// (since later we weight the ruptures by the mean slip rate, which would otherwise result in 
			// starting solution that did not match target MFD if the mean slip rates per rupture 
			// differed between magnitude bins)
			if (meanSlipRate[rup]!=0)
				magHist.add(rupMeanMag[rup], meanSlipRate[rup]);  // each bin
			else magHist.add(rupMeanMag[rup], 1E-4);
		}
		
		
		// Set up initial (non-normalized) target MFD rates for each rupture, normalized by meanSlipRate
		for (int rup=0; rup<numRup; rup++) {
			// Find number of ruptures that go through same sections as rupture and have the same magnitude
			// COMMENT THIS OUT FOR NOW - TAKES WAY TOO LONG
//			List<Integer> sects = rupList.get(rup);
//			// total amount of overlap of rupture with rups of same mag (when rounded),
//			// in units of original rupture's length
//			double totalOverlap = 0;
//			for (int sect: sects) {
//				List<Integer> rups = faultSystemRupSet.getRupturesForSection(sect);
//				for (int r: rups) {
//					if (Math.round(10*rupMeanMag[r])==Math.round(10*rupMeanMag[rup]))
//						totalOverlap+=1;
//				}
//			}
//			// add percentages of total overlap with each rupture + 1 for original rupture itself
//			totalOverlap = totalOverlap/sects.size() + 1; 
			double totalOverlap = 1d;	// Don't apply overlap
//			double totalOverlap = numRupturesOnSlowestSection[rup];  // Use number of ruptures on smallest slip-rate section of rup as a proxy for overlap
//			double totalOverlap = maxNumRupturesOnSectInRup[rup];  // Use max number of ruptures on section in rup as a proxy for overlap
			
			// Divide rate by total number of similar ruptures (same magnitude, has section overlap)
			// - normalize overlapping ruptures by percentage overlap
			initial_state[rup] = targetMagFreqDist.getClosestY(rupMeanMag[rup]) * meanSlipRate[rup] / (magHist.getClosestY(rupMeanMag[rup]) * totalOverlap);
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
		// plot magnitude histogram for the inversion starting model
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
	
	
	public double getRelativeMomentConstraintWt() {
		return relativeMomentConstraintWt;
	}

	public void setRelativeMomentConstraintWt(
			double relativeMomentConstraintWt) {
		this.relativeMomentConstraintWt = relativeMomentConstraintWt;
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

}
