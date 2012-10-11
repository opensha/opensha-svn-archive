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
import org.opensha.sha.magdist.TaperedGR_MagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.griddedSeismicity.GriddedSeisUtils;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class InversionMFDs {
	
	// debugging flag
	final static boolean D = false;
	final static boolean GR_OFF_FAULT_IS_TAPERED = true;
	String debugString;

	FaultSystemRupSet fltSysRupSet;
	InversionFaultSystemSolutionInterface invRupSet;
	double totalRegionRateMgt5, onFaultRegionRateMgt5, offFaultRegionRateMgt5;
	double mMaxOffFault;
	boolean applyImpliedCouplingCoeff;
	SpatialSeisPDF spatialSeisPDF;
	SpatialSeisPDF spatialSeisPDFforOnFaultRates;
	InversionModels inversionModel;
	GriddedSeisUtils gridSeisUtils;
	
	double origOnFltDefModMoRate, offFltDefModMoRate, aveMinSeismoMag, roundedMmaxOnFault;
	double fractSeisInSoCal;
	double fractionSeisOnFault;
	double impliedOnFaultCouplingCoeff;
	double impliedTotalCouplingCoeff;
	double finalOffFaultCouplingCoeff;
	GutenbergRichterMagFreqDist totalTargetGR, totalTargetGR_NoCal, totalTargetGR_SoCal;
	SummedMagFreqDist targetOnFaultSupraSeisMFD;
	SummedMagFreqDist targetNoCalOnFaultSupraSeisMFD;
	SummedMagFreqDist targetSoCalOnFaultSupraSeisMFD;
	IncrementalMagFreqDist trulyOffFaultMFD;
	ArrayList<GutenbergRichterMagFreqDist> subSeismoOnFaultMFD_List;
	SummedMagFreqDist totalSubSeismoOnFaultMFD;		// this is a sum of the MFDs in subSeismoOnFaultMFD_List
	IncrementalMagFreqDist noCalTargetSupraMFD, soCalTargetSupraMFD;

	
	List<MFD_InversionConstraint> mfdConstraintsForNoAndSoCal;

	// discretization parameters for MFDs
	public final static double MIN_MAG = 0.05;
	public final static int NUM_MAG = 90;
	public final static double DELTA_MAG = 0.1;


	/**
	 * TODO All the values after fltSysRupSet could be obtained from the LogicTreeBranch
	 * 		object within fltSysRupSet (if the latter is an InversionFaultSystemRupSet)
	 * @param fltSysRupSet
	 * @param totalRegionRateMgt5
	 * @param mMaxOffFault - this should be the upper edge of the mag bin
	 * @param applyImpliedCouplingCoeff
	 * @param spatialSeisPDF
	 * @param inversionModel
	 */
	public InversionMFDs(FaultSystemRupSet fltSysRupSet, InversionFaultSystemSolutionInterface invRupSet, double totalRegionRateMgt5, double mMaxOffFault, 
			boolean applyImpliedCouplingCoeff, SpatialSeisPDF spatialSeisPDF, InversionModels inversionModel) {
		
		// convert mMaxOffFault to bin center
		mMaxOffFault -= DELTA_MAG/2;
		
		this.fltSysRupSet=fltSysRupSet;
		this.invRupSet=invRupSet;
		this.totalRegionRateMgt5 = totalRegionRateMgt5;
		this.mMaxOffFault = mMaxOffFault;
		this.applyImpliedCouplingCoeff = applyImpliedCouplingCoeff;
		this.spatialSeisPDF=spatialSeisPDF;
		this.inversionModel=inversionModel;
		
		// this prevents using any non smoothed seismicity PDF for computing rates on fault (def mod PDF doesn't make sense)
		if(spatialSeisPDF == SpatialSeisPDF.UCERF2 || spatialSeisPDF == SpatialSeisPDF.UCERF3)
			spatialSeisPDFforOnFaultRates = spatialSeisPDF;
		else
			spatialSeisPDFforOnFaultRates = SpatialSeisPDF.UCERF3;

		
		// test to make sure it's a statewide deformation model
		DeformationModels dm = fltSysRupSet.getDeformationModel();
		if(dm == DeformationModels.UCERF2_BAYAREA || dm == DeformationModels.UCERF2_NCAL)
			throw new RuntimeException("Error - "+dm+" not yet supported by InversionMFD");
		
		List<FaultSectionPrefData> faultSectionData =  fltSysRupSet.getFaultSectionDataList();
		
		gridSeisUtils = new GriddedSeisUtils(faultSectionData, spatialSeisPDFforOnFaultRates, 12.0);
		
		GriddedRegion noCalGrid = RELM_RegionUtils.getNoCalGriddedRegionInstance();
		GriddedRegion soCalGrid = RELM_RegionUtils.getSoCalGriddedRegionInstance();
		
		fractSeisInSoCal = spatialSeisPDFforOnFaultRates.getFractionInRegion(soCalGrid);
//		fractionSeisOnFault = DeformationModelsCalc.getFractSpatialPDF_InsideSectionPolygons(faultSectionData, spatialSeisPDFforOnFaultRates);
		fractionSeisOnFault = gridSeisUtils.pdfInPolys();

		onFaultRegionRateMgt5 = totalRegionRateMgt5*fractionSeisOnFault;
		offFaultRegionRateMgt5 = totalRegionRateMgt5-onFaultRegionRateMgt5;
		origOnFltDefModMoRate = DeformationModelsCalc.calculateTotalMomentRate(faultSectionData,true);
		offFltDefModMoRate = DeformationModelsCalc.calcMoRateOffFaultsForDefModel(fltSysRupSet.getFaultModel(), fltSysRupSet.getDeformationModel());

		// make the total target GR for region
		totalTargetGR = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		roundedMmaxOnFault = totalTargetGR.getX(totalTargetGR.getClosestXIndex(fltSysRupSet.getMaxMag()));
		totalTargetGR.setAllButTotMoRate(MIN_MAG, roundedMmaxOnFault, totalRegionRateMgt5*1e5, 1.0);
		
		totalTargetGR_NoCal = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);	
		totalTargetGR_NoCal.setAllButTotMoRate(MIN_MAG, roundedMmaxOnFault, totalRegionRateMgt5*(1-fractSeisInSoCal)*1e5, 1.0);
		
		
		totalTargetGR_SoCal = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);	
		totalTargetGR_SoCal.setAllButTotMoRate(MIN_MAG, roundedMmaxOnFault, totalRegionRateMgt5*fractSeisInSoCal*1e5, 1.0);
		
		// get ave min seismo mag for region
		double tempMag = FaultSystemRupSetCalc.getMeanMinMag(fltSysRupSet, invRupSet, true);
		aveMinSeismoMag = totalTargetGR.getX(totalTargetGR.getClosestXIndex(tempMag));	// round to nearest MFD value


		if(D) {
			debugString = "\ttotalRegionRateMgt5 =\t"+totalRegionRateMgt5+"\n"+
					"\tmMaxOffFault =\t"+mMaxOffFault+"\n"+
					"\tapplyImpliedCouplingCoeff =\t"+applyImpliedCouplingCoeff+"\n"+
					"\tspatialSeisPDF =\t"+spatialSeisPDF+"\n"+
					"\tspatialSeisPDFforOnFaultRates =\t"+spatialSeisPDFforOnFaultRates+"\n"+
					"\tinversionModel =\t"+inversionModel+"\n"+
					"\tfractSeisInSoCal =\t"+(float)fractSeisInSoCal+"\n"+
					"\tfractionSeisOnFault =\t"+(float)fractionSeisOnFault+"\n"+
					"\tonFaultRegionRateMgt5 =\t"+(float)onFaultRegionRateMgt5+"\n"+
					"\toffFaultRegionRateMgt5 =\t"+(float)offFaultRegionRateMgt5+"\n"+
					"\torigOnFltDefModMoRate =\t"+(float)origOnFltDefModMoRate+"\n"+
					"\toffFltDefModMoRate =\t"+(float)offFltDefModMoRate+"\n"+
					"\troundedMmaxOnFault =\t"+(float)roundedMmaxOnFault+"\n"+
					"\ttotalTargetGR(5.05) =\t"+(float)totalTargetGR.getCumRate(5.05)+"\n"+
					"\taveMinSeismoMag =\t"+(float)aveMinSeismoMag+"\n";
		}

		
		
		if (inversionModel.isCharacteristic()) {

			trulyOffFaultMFD = FaultSystemRupSetCalc.getTriLinearCharOffFaultTargetMFD(totalTargetGR, onFaultRegionRateMgt5, aveMinSeismoMag, mMaxOffFault);

//			subSeismoOnFaultMFD_List = FaultSystemRupSetCalc.getCharSubSeismoOnFaultMFD_forEachSection(fltSysRupSet, spatialSeisPDF, totalTargetGR);
			subSeismoOnFaultMFD_List = FaultSystemRupSetCalc.getCharSubSeismoOnFaultMFD_forEachSection(fltSysRupSet, invRupSet, gridSeisUtils, totalTargetGR);

			totalSubSeismoOnFaultMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(int m=0; m<subSeismoOnFaultMFD_List.size(); m++) {
				GutenbergRichterMagFreqDist mfd = subSeismoOnFaultMFD_List.get(m);
				if(mfd.getMagUpper() <= 5.05 & D) {
					debugString += "\tWARNING: "+faultSectionData.get(m).getName()+" has a max subSeism mag of "+mfd.getMagUpper()+" so no contribution above M5!\n";
				}
//				if(Double.isNaN(mfd.getTotalIncrRate()))
//					throw new RuntimeException("Bad MFD for section:\t"+m+"\t"+faultSectionData.get(m).getName()+"\tslipRate="+faultSectionData.get(m).getReducedAveSlipRate());
				totalSubSeismoOnFaultMFD.addIncrementalMagFreqDist(mfd);
			}

			targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			targetOnFaultSupraSeisMFD.addIncrementalMagFreqDist(totalTargetGR);
			targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(trulyOffFaultMFD);
			targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(totalSubSeismoOnFaultMFD);
			
			// split the above between N & S cal
			noCalTargetSupraMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			soCalTargetSupraMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(int i=0; i<NUM_MAG; i++) {
				noCalTargetSupraMFD.set(i,targetOnFaultSupraSeisMFD.getY(i)*(1.0-fractSeisInSoCal));
				soCalTargetSupraMFD.set(i,targetOnFaultSupraSeisMFD.getY(i)*fractSeisInSoCal);
			}

			// compute coupling coefficients
			impliedOnFaultCouplingCoeff = (targetOnFaultSupraSeisMFD.getTotalMomentRate()+totalSubSeismoOnFaultMFD.getTotalMomentRate())/origOnFltDefModMoRate;
			finalOffFaultCouplingCoeff = trulyOffFaultMFD.getTotalMomentRate()/offFltDefModMoRate;
			impliedTotalCouplingCoeff = totalTargetGR.getTotalMomentRate()/(origOnFltDefModMoRate+offFltDefModMoRate);

		} else {
			// GR
			
			// get the total GR nucleation MFD for all fault section
			SummedMagFreqDist impliedOnFault_GR_NuclMFD = FaultSystemRupSetCalc.calcImpliedGR_NucleationMFD(fltSysRupSet, MIN_MAG, NUM_MAG, DELTA_MAG);

			// compute coupling coefficient
			impliedOnFaultCouplingCoeff = onFaultRegionRateMgt5/impliedOnFault_GR_NuclMFD.getCumRate(5.05);
			double tempCoupCoeff = 1;	// defaults to 1.0; this is used below
			if(applyImpliedCouplingCoeff && impliedOnFaultCouplingCoeff < 1.0) 	// only apply if it's < 1
				tempCoupCoeff = impliedOnFaultCouplingCoeff;	
			
			if(D) {
				debugString += "\timpliedOnFault_GR_NuclMFD(5.05) =\t"+impliedOnFault_GR_NuclMFD.getCumRate(5.05);
				debugString += "\tempCoupCoeff =\t"+tempCoupCoeff+"\n";
			}

			// split the on-fault MFDs into supra- vs sub-seismo MFDs, and apply tempCoupCoeff
			ArrayList<GutenbergRichterMagFreqDist> grNuclMFD_List = FaultSystemRupSetCalc.calcImpliedGR_NuclMFD_ForEachSection(fltSysRupSet, MIN_MAG, NUM_MAG, DELTA_MAG);
			subSeismoOnFaultMFD_List = new ArrayList<GutenbergRichterMagFreqDist>();
			totalSubSeismoOnFaultMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			noCalTargetSupraMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			soCalTargetSupraMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			// loop over sections
			for(int s=0;s<grNuclMFD_List.size();s++) {
				GutenbergRichterMagFreqDist grNuclMFD = grNuclMFD_List.get(s);
//				int minSupraMagIndex = grNuclMFD.getClosestXIndex(fltSysRupSet.getMinMagForSection(s));
//				double maxMagSubSeismo = grNuclMFD.getX(minSupraMagIndex-1);
				double maxMagSubSeismo = invRupSet.getUpperMagForSubseismoRuptures(s);
				int minSupraMagIndex = grNuclMFD.getXIndex(maxMagSubSeismo)+1;
				GutenbergRichterMagFreqDist subSeisGR = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, MIN_MAG, maxMagSubSeismo, 1.0, 1.0);
				double rateAtZeroMagBin = grNuclMFD.getY(0)*tempCoupCoeff;
				subSeisGR.scaleToIncrRate(0, rateAtZeroMagBin);
				subSeismoOnFaultMFD_List.add(subSeisGR);
				totalSubSeismoOnFaultMFD.addIncrementalMagFreqDist(subSeisGR);
				FaultTrace sectTrace = faultSectionData.get(s).getStirlingGriddedSurface(1.0).getRowAsTrace(0);
				double fractSectInSoCal = RegionUtils.getFractionInside(soCalGrid, sectTrace);
				for(int i=minSupraMagIndex;i<grNuclMFD.getNum();i++) {
					targetOnFaultSupraSeisMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff);
					noCalTargetSupraMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff*(1.0-fractSectInSoCal));
					soCalTargetSupraMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff*fractSectInSoCal);
				}
			}
			
			// If on the NoFix branch, we need to reduce totalSubSeismoOnFaultMFD and targetOnFaultSupraSeisMFD so
			// that they sum with trulyOffFaultMFD to match the regional target (where we let the inversion reduce the slip
			// rates); Note that we are not reducing subSeismoOnFaultMFD_List because these are used elsewhere in reducing final
			// target slip rates; Note that subSeismoOnFaultMFD_List needs to be recomputed on this GR NoFix branch
			// since final slip rates will vary (and we need the subseismo GR to be consistent with the supra-seismo GR)
			if(!applyImpliedCouplingCoeff && impliedOnFaultCouplingCoeff < 1.0) {
				totalSubSeismoOnFaultMFD.scale(impliedOnFaultCouplingCoeff);
				targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
				targetOnFaultSupraSeisMFD.addIncrementalMagFreqDist(totalTargetGR);
				targetOnFaultSupraSeisMFD.scaleToIncrRate(5.05, impliedOnFault_GR_NuclMFD.getY(5.05)*impliedOnFaultCouplingCoeff);
//				targetOnFaultSupraSeisMFD.scale(fractionSeisOnFault); this has numerical precisions problems?
				targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(totalSubSeismoOnFaultMFD);
				noCalTargetSupraMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
				soCalTargetSupraMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
				for(int i=0;i<targetOnFaultSupraSeisMFD.getNum();i++) {
					noCalTargetSupraMFD.add(i, targetOnFaultSupraSeisMFD.getY(i)*(1.0-fractSeisInSoCal));	// this is approximate ?????????
					soCalTargetSupraMFD.add(i, targetOnFaultSupraSeisMFD.getY(i)*fractSeisInSoCal);
				}
			}
			
			trulyOffFaultMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, MIN_MAG, mMaxOffFault, 1.0, 1.0);
			trulyOffFaultMFD.scaleToCumRate(0, offFaultRegionRateMgt5*1e5);
			if(GR_OFF_FAULT_IS_TAPERED) {
				double moRate = trulyOffFaultMFD.getTotalMomentRate();
				trulyOffFaultMFD = new TaperedGR_MagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
				((TaperedGR_MagFreqDist)trulyOffFaultMFD).setAllButCornerMag(MIN_MAG, moRate, offFaultRegionRateMgt5*1e5, 1.0);
			}

			// compute coupling coefficients
			finalOffFaultCouplingCoeff = trulyOffFaultMFD.getTotalMomentRate()/offFltDefModMoRate;
			impliedTotalCouplingCoeff = (impliedOnFaultCouplingCoeff*origOnFltDefModMoRate+finalOffFaultCouplingCoeff*offFltDefModMoRate)/(origOnFltDefModMoRate+offFltDefModMoRate);

		}
		
		if(D) {
			debugString += "\timpliedOnFaultCouplingCoeff =\t"+(float)impliedOnFaultCouplingCoeff+"\n"+
					"\tfinalOffFaultCouplingCoeff =\t"+(float)finalOffFaultCouplingCoeff+"\n"+
					"\timpliedTotalCouplingCoeff =\t"+(float)impliedTotalCouplingCoeff+"\n"+
					"\ttrulyOffFaultMFD(5.05) =\t"+(float)trulyOffFaultMFD.getCumRate(5.05)+"\n"+
					"\ttotalSubSeismoOnFaultMFD(5.05) =\t"+(float)totalSubSeismoOnFaultMFD.getCumRate(5.05)+"\n"+
					"\ttargetOnFaultSupraSeisMFD(5.05) =\t"+(float)targetOnFaultSupraSeisMFD.getCumRate(5.05)+"\n"+
					"\tsum of above three =\t"+(float)(trulyOffFaultMFD.getCumRate(5.05)+totalSubSeismoOnFaultMFD.getCumRate(5.05)+targetOnFaultSupraSeisMFD.getCumRate(5.05))+"\n"+
					"\tnoCalTargetMFD(5.05) =\t"+(float)noCalTargetSupraMFD.getCumRate(5.05)+"\n"+
					"\tsoCalTargetMFD(5.05) =\t"+(float)soCalTargetSupraMFD.getCumRate(5.05)+"\n"+
					"\tsum of above two =\t"+(float)(noCalTargetSupraMFD.getCumRate(5.05)+soCalTargetSupraMFD.getCumRate(5.05))+"\n";
			System.out.println(debugString);
		}
		
		mfdConstraintsForNoAndSoCal = new ArrayList<MFD_InversionConstraint>();
		mfdConstraintsForNoAndSoCal.add(new MFD_InversionConstraint(noCalTargetSupraMFD, noCalGrid));
		mfdConstraintsForNoAndSoCal.add(new MFD_InversionConstraint(soCalTargetSupraMFD, soCalGrid));

	}

	/**
	 * This returns the sum of the truly off-fault and total sub-seismo MFDs
	 * @return
	 */
	public SummedMagFreqDist getTotalSubSeismoOnPlusTrulyOffFaultMFD() {
		SummedMagFreqDist mfd =new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);		
		mfd.addIncrementalMagFreqDist(getTrulyOffFaultMFD());
		mfd.addIncrementalMagFreqDist(getTotalSubSeismoOnFaultMFD());
		return mfd;
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
	
	public GutenbergRichterMagFreqDist getTotalTargetGR_NoCal() {return totalTargetGR_NoCal;}
	
	public GutenbergRichterMagFreqDist getTotalTargetGR_SoCal() {return totalTargetGR_SoCal;}
	
	public double getOrigOnFltDefModMoRate() {return origOnFltDefModMoRate; }
	
	
	
	/**
	 * This returns the northern and southern RELM region MFD_InversionConstraint 
	 * (as the 0th and 1st List elements, respectively).  The associated MFDs have been reduced
	 * by both off-fault and subseismogenic ruptures.
	 * @return
	 */
	public List<MFD_InversionConstraint> getMFD_ConstraintsForNoAndSoCal() { return mfdConstraintsForNoAndSoCal; }
	
	public String getPreInversionAnalysisData() {
		String str = (float)fractionSeisOnFault+"\t" +
			(float)fractSeisInSoCal+"\t"+
			(float)roundedMmaxOnFault+"\t" +
			(float)aveMinSeismoMag+"\t" +
			(float)origOnFltDefModMoRate+"\t" +
			(float)offFltDefModMoRate+"\t" +
			(float)impliedOnFaultCouplingCoeff+"\t"+
			(float)finalOffFaultCouplingCoeff+"\t"+
			(float)impliedTotalCouplingCoeff+"\t"+
			(float)trulyOffFaultMFD.getCumRate(5.05)+"\t"+
			(float)totalSubSeismoOnFaultMFD.getCumRate(5.05)+"\t"+
			(float)targetOnFaultSupraSeisMFD.getCumRate(5.05)+"\t"+
			(float)noCalTargetSupraMFD.getCumRate(5.05)+"\t"+
			(float)soCalTargetSupraMFD.getCumRate(5.05)+"\t"+
			(float)trulyOffFaultMFD.getTotalMomentRate()+"\t"+
			(float)totalSubSeismoOnFaultMFD.getTotalMomentRate()+"\t"+
			(float)targetOnFaultSupraSeisMFD.getTotalMomentRate()+"\t"+
			(float)noCalTargetSupraMFD.getTotalMomentRate()+"\t"+
			(float)soCalTargetSupraMFD.getTotalMomentRate();

		return str;
	}
	
	public String getPreInversionAnalysisDataHeader() {
		String str = "frSeisOnFlt"+"\t" +
			"frSeisInSoCal"+"\t"+
			"MmaxOnFlt"+"\t" +
			"aveSupraSeisMmin"+"\t" +
			"onFltDefModMoRate"+"\t" +
			"offFltDefModMoRate"+"\t" +
			"implOnFaultCC"+"\t"+
			"finalOffFaultCC"+"\t"+
			"implTotalCC"+"\t"+
			"trulyOffFltMFD_RateM5"+"\t"+
			"subSeisOnFltMFD_RateM5"+"\t"+
			"targetOnFtSupraSeisMFD_RateM5"+"\t"+
			"noCalTargetSuprSeisMFD_RateM5"+"\t"+
			"soCalTargetSuprSeisMFD_RateM5"+"\t"+
			"trulyOffFltMFD_MoRate"+"\t"+
			"subSeisOnFltMFD_MoRate"+"\t"+
			"targetOnFtSupraSeisMFD_MoRate"+"\t"+
			"noCalTargetSuprSeisMFD_MoRate"+"\t"+
			"soCalTargetSuprSeisMFD_MoRate";
		return str;
	}

	/**
	 * This returns the maximum magnitude off fault if the total original off-fault 
	 * moment rate is satisfied.  If (inversionModel.isCharacteristic() == true), Double.NaN 
	 * is returned if it's impossible to satisfy the moment rate.
	 * @return
	 */
	public double getOffFaultMmaxIfOrigMoRateSatisfied() {
		double maxOffMagWithFullMoment;
		if(inversionModel.isCharacteristic()) {
			IncrementalMagFreqDist charOffMFD = FaultSystemRupSetCalc.getTriLinearCharOffFaultTargetMFD(offFltDefModMoRate, 
					totalTargetGR, onFaultRegionRateMgt5, aveMinSeismoMag);
			if(charOffMFD != null)
				maxOffMagWithFullMoment = charOffMFD.getMaxMagWithNonZeroRate();
			else
				maxOffMagWithFullMoment = Double.NaN;
		}
		else {
			GutenbergRichterMagFreqDist tempOffFaultGR = new GutenbergRichterMagFreqDist(0.005, 2000, 0.01);
			tempOffFaultGR.setAllButMagUpper(0.005, offFltDefModMoRate, offFaultRegionRateMgt5*1e5, 1.0, true);
			maxOffMagWithFullMoment = tempOffFaultGR.getMagUpper();
		}
			
		return maxOffMagWithFullMoment;

	}
	
	public double getOffFaultRegionRateMgt5() {return offFaultRegionRateMgt5; }
	
	/**
	 * Returns the utility GriddedSeisUtils instance for reuse elsewhere.
	 * @return
	 */
	public GriddedSeisUtils getGridSeisUtils() {
		return gridSeisUtils;
	}


}
