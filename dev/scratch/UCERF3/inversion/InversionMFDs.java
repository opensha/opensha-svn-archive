package scratch.UCERF3.inversion;

import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.RELM_RegionUtils;

public class InversionMFDs {

	FaultSystemRupSet fltSysRupSet;
	double totalRegionRateMgt5;
	double mMaxOffFault;
	boolean applyImpliedCouplingCoeff;
	SpatialSeisPDF spatialSeisPDF;
	InversionModels inversionModel;

	double fractionSeisOnFault;
	double impliedOnFaultCouplingCoeff;
	double impliedTotalCouplingCoeff;
	double finalOffFaultCouplingCoeff;
	GutenbergRichterMagFreqDist totalTargetGR;
	SummedMagFreqDist targetOnFaultSupraSeisMFD;
	SummedMagFreqDist targetNoCalOnFaultSupraSeisMFD;
	SummedMagFreqDist targetSoCalOnFaultSupraSeisMFD;
	IncrementalMagFreqDist trulyOffFaultMFD;
	ArrayList<GutenbergRichterMagFreqDist> subSeismoOnFaultMFD_List;
	SummedMagFreqDist totalSubSeismoOnFaultMFD;		// this is a sum of the MFDs in subSeismoOnFaultMFD_List
	
	List<MFD_InversionConstraint> mfdConstraintsForNoAndSoCal;

	// discretization parameters for MFDs
	public final static double MIN_MAG = 0.05;
	public final static int NUM_MAG = 90;
	public final static double DELTA_MAG = 0.1;


	public InversionMFDs(InversionFaultSystemRupSet fltSysRupSet, double totalRegionRateMgt5, double mMaxOffFault, 
			boolean applyImpliedCouplingCoeff, SpatialSeisPDF spatialSeisPDF, InversionModels inversionModel) {
		
		this.fltSysRupSet=fltSysRupSet;
		this.totalRegionRateMgt5 = totalRegionRateMgt5;
		this.mMaxOffFault = mMaxOffFault;
		this.applyImpliedCouplingCoeff = applyImpliedCouplingCoeff;
		this.spatialSeisPDF=spatialSeisPDF;
		this.inversionModel=inversionModel;
		
		// test to make sure it's a statewide deformation model
		DeformationModels dm = fltSysRupSet.getDeformationModel();
		if(dm == DeformationModels.UCERF2_BAYAREA || dm == DeformationModels.UCERF2_NCAL)
			throw new RuntimeException("Error - "+dm+" not yet supported by InversionMFD");
		
		List<FaultSectionPrefData> faultSectionData =  fltSysRupSet.getFaultSectionDataList();
		
		GriddedRegion noCalGrid = RELM_RegionUtils.getNoCalGriddedRegionInstance();
		GriddedRegion soCalGrid = RELM_RegionUtils.getSoCalGriddedRegionInstance();
		double fractSeisInSoCal = spatialSeisPDF.getFractionInRegion(soCalGrid);
		mfdConstraintsForNoAndSoCal = new ArrayList<MFD_InversionConstraint>();
		IncrementalMagFreqDist noCalTargetMFD, soCalTargetMFD;

		fractionSeisOnFault = DeformationModelsCalc.getFractSpatialPDF_InsideSectionPolygons(faultSectionData, spatialSeisPDF);
		double onFaultRegionRateMgt5 = totalRegionRateMgt5*fractionSeisOnFault;
		double offFaultRegionRateMgt5 = totalRegionRateMgt5-onFaultRegionRateMgt5;
		double onFltDefModMoRate = DeformationModelsCalc.calculateTotalMomentRate(faultSectionData,true);
		double offFltDefModMoRate = DeformationModelsCalc.calcMoRateOffFaultsForDefModel(fltSysRupSet.getFaultModel(), fltSysRupSet.getDeformationModel());

		// make the total target GR for region
		totalTargetGR = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		double roundedMmax = totalTargetGR.getX(totalTargetGR.getClosestXIndex(fltSysRupSet.getMaxMag()));
		totalTargetGR.setAllButTotMoRate(MIN_MAG, roundedMmax, totalRegionRateMgt5*1e5, 1.0);

		
		if (inversionModel == InversionModels.CHAR_CONSTRAINED) {

			// check that the following can be calculated since "this" is not fully instantiated
			double aveMinSeismoMag = FaultSystemRupSetCalc.getMeanMinMag(fltSysRupSet, true);

			trulyOffFaultMFD = FaultSystemRupSetCalc.getTriLinearCharOffFaultTargetMFD(totalTargetGR, onFaultRegionRateMgt5, aveMinSeismoMag, mMaxOffFault);

			subSeismoOnFaultMFD_List = FaultSystemRupSetCalc.getCharSubSeismoOnFaultMFD_forEachSection(fltSysRupSet, spatialSeisPDF, totalTargetGR);

			totalSubSeismoOnFaultMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(GutenbergRichterMagFreqDist mfd:subSeismoOnFaultMFD_List) {
				totalSubSeismoOnFaultMFD.addIncrementalMagFreqDist(mfd);
			}

			targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			targetOnFaultSupraSeisMFD.addIncrementalMagFreqDist(totalTargetGR);
			targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(trulyOffFaultMFD);
			targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(totalSubSeismoOnFaultMFD);
			
			// split the above between N & S cal
			noCalTargetMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			soCalTargetMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(int i=0; i<NUM_MAG; i++) {
				noCalTargetMFD.set(i,targetOnFaultSupraSeisMFD.getY(i)*(1.0-fractSeisInSoCal));
				soCalTargetMFD.set(i,targetOnFaultSupraSeisMFD.getY(i)*fractSeisInSoCal);
			}

			// compute coupling coefficients
			impliedOnFaultCouplingCoeff = (targetOnFaultSupraSeisMFD.getTotalMomentRate()+totalSubSeismoOnFaultMFD.getTotalMomentRate())/onFltDefModMoRate;
			finalOffFaultCouplingCoeff = trulyOffFaultMFD.getTotalMomentRate()/offFltDefModMoRate;
			impliedTotalCouplingCoeff = totalTargetGR.getTotalMomentRate()/(onFltDefModMoRate+offFltDefModMoRate);

		} else if (inversionModel == InversionModels.GR_CONSTRAINED) {

			// get the total GR nucleation MFD for all fault section
			SummedMagFreqDist impliedOnFault_GR_NuclMFD = FaultSystemRupSetCalc.calcImpliedGR_NucleationMFD(fltSysRupSet, MIN_MAG, NUM_MAG, DELTA_MAG);

			// compute coupling coefficient
			impliedOnFaultCouplingCoeff = onFaultRegionRateMgt5/impliedOnFault_GR_NuclMFD.getCumRate(5.05);
			double tempCoupCoeff = 1;	// defaults to 1.0; this is used below
			if(applyImpliedCouplingCoeff && impliedOnFaultCouplingCoeff < 1.0) 	// only apply if it's < 1
				tempCoupCoeff = impliedOnFaultCouplingCoeff;	

			// split the on-fault MFDs into supra- vs sub-seismo MFDs, and apply tempCoupCoeff
			ArrayList<GutenbergRichterMagFreqDist> grNuclMFD_List = FaultSystemRupSetCalc.calcImpliedNuclMFD_ForEachSection(fltSysRupSet, MIN_MAG, NUM_MAG, DELTA_MAG);
			subSeismoOnFaultMFD_List = new ArrayList<GutenbergRichterMagFreqDist>();
			totalSubSeismoOnFaultMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			noCalTargetMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			soCalTargetMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(int s=0;s<grNuclMFD_List.size();s++) {
				GutenbergRichterMagFreqDist grNuclMFD = grNuclMFD_List.get(s);
				int minSupraMagIndex = grNuclMFD.getClosestXIndex(fltSysRupSet.getMinMagForSection(s));
				double maxMagSubSeismo = grNuclMFD.getX(minSupraMagIndex-1);
				GutenbergRichterMagFreqDist subSeisGR = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, MIN_MAG, maxMagSubSeismo, 1.0, 1.0);
				double rateAtZeroMagBin = grNuclMFD.getY(0)*tempCoupCoeff;
				subSeisGR.scaleToIncrRate(0, rateAtZeroMagBin);
				subSeismoOnFaultMFD_List.add(subSeisGR);
				totalSubSeismoOnFaultMFD.addIncrementalMagFreqDist(subSeisGR);
				FaultTrace sectTrace = faultSectionData.get(s).getStirlingGriddedSurface(1.0).getRowAsTrace(0);
				double fractSectInSoCal = RegionUtils.getFractionInside(soCalGrid, sectTrace);
				for(int i=minSupraMagIndex;i<grNuclMFD.getNum();i++) {
					targetOnFaultSupraSeisMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff);
					noCalTargetMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff*(1.0-fractSectInSoCal));
					soCalTargetMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff*fractSectInSoCal);
				}
			}

			trulyOffFaultMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, MIN_MAG, mMaxOffFault, 1.0, 1.0);
			trulyOffFaultMFD.scaleToCumRate(0, offFaultRegionRateMgt5*1e5);

			// compute coupling coefficients
			finalOffFaultCouplingCoeff = trulyOffFaultMFD.getTotalMomentRate()/offFltDefModMoRate;
			impliedTotalCouplingCoeff = (impliedOnFaultCouplingCoeff*onFltDefModMoRate+finalOffFaultCouplingCoeff*offFltDefModMoRate)/(onFltDefModMoRate+offFltDefModMoRate);

		} else if (inversionModel == InversionModels.GR_UNCONSTRAINED) {
			throw new RuntimeException("Not yet implemented");

		} else {
			throw new RuntimeException("Not yet implement");
		}
		
		mfdConstraintsForNoAndSoCal.add(new MFD_InversionConstraint(noCalTargetMFD, noCalGrid));
		mfdConstraintsForNoAndSoCal.add(new MFD_InversionConstraint(soCalTargetMFD, soCalGrid));

	}


	
	public double getTotalRegionRateMgt5() {return totalRegionRateMgt5;}
	
	public double getMmaxOffFault() {return mMaxOffFault;}
	
	public boolean getApplyImpliedCouplingCoeffBoolean() {return applyImpliedCouplingCoeff;}
	
	public SpatialSeisPDF getSpatialSeisPDF() {return spatialSeisPDF;}
	
	public double getFractionSeisOnFault() {return fractionSeisOnFault;}
	
	public double getImpliedOnFaultCouplingCoeff() {return impliedOnFaultCouplingCoeff;}
	
	public double getImpliedTotalCouplingCoeff() {return impliedTotalCouplingCoeff;}
	
	public double getFinalOffFaultCouplingCoeff() {return finalOffFaultCouplingCoeff;}
	
	public SummedMagFreqDist getTargetOnFaultSupraSeisMFD() {return targetOnFaultSupraSeisMFD;}
	
	public IncrementalMagFreqDist getTrulyOffFaultMFD() {return trulyOffFaultMFD;}
	
	public ArrayList<GutenbergRichterMagFreqDist> getSubSeismoOnFaultMFD_List() {return subSeismoOnFaultMFD_List;}
	
	public SummedMagFreqDist getTotalSubSeismoOnFaultMFD() {return totalSubSeismoOnFaultMFD;}
	
	public GutenbergRichterMagFreqDist getTotalTargetGR() {return totalTargetGR;}
	
	/**
	 * This returns the northern and southern RELM region MFD_InversionConstraint 
	 * (as the 0th and 1st List elements, respectively).  The associated MFDs have been reduced
	 * by both off-fault and subseismogenic ruptures.
	 * @return
	 */
	public List<MFD_InversionConstraint> getMFD_ConstraintsForNoAndSoCal() { return mfdConstraintsForNoAndSoCal; }

}
