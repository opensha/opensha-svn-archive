package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.WeightedFuncList;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolTimeDepERF;
import scratch.UCERF3.griddedSeismicity.SmallMagScaling;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;

public class FaultSysSolutionERF_Calc {
	
	
	/**
	 * This returns and instance of a UCERF3_FaultSysSol_ERF where the duration 
	 * has been set as 1 year and the forecast has been updated
	 * @param faultSysSolZipFile
	 * @return
	 */
	public static UCERF3_FaultSysSol_ERF getUCERF3_ERF_Instance(File faultSysSolZipFile) {
		InversionFaultSystemSolution invFss;
		SimpleFaultSystemSolution tmp = null;
		try {
			tmp =  SimpleFaultSystemSolution.fromFile(faultSysSolZipFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		invFss = new InversionFaultSystemSolution(tmp);

		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss);

		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
		erf.getTimeSpan().setDuration(1d);
		erf.updateForecast();
		
		return erf;

	}
	
	
	

	
	
	
	public static void makePrelimReportPartPlots() {

		try {
			// UCERF2_FaultSysSol_ERF
//			UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
//			erf.updateForecast();
//			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, "testUCERF3_ERF", "test", "testUCERF3_ERF");
			
//			ModMeanUCERF2 erf= new ModMeanUCERF2();
//			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
//			erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
//			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//			erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
//			erf.getTimeSpan().setDuration(1d);
//			erf.updateForecast();
//			String fileName = "UCERF2";

//			File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/Fig16_ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.MO_REDUCTION);
//			String fileName = "UCERF3_CHAR_DefMod_MoBal";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_CHAR_DefMod_Seis";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.UCERF3,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_CHAR_U3smSeis_Seis";
			
			File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/Fig16_ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_GR_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file);
			String fileName = "UCERF3_GR_DefMod_MoBal";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_GR_DefMod_Seis";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.UCERF3,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_GR_U3smSeis_Seis";


			
			GMT_CA_Maps.plotParticipationRateMap(erf, 5.0, 10d, fileName+"_Part5pt0", "test", fileName+"_Part5pt0");
			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, fileName+"_Part6pt7", "test", fileName+"_Part6pt7");
			GMT_CA_Maps.plotParticipationRateMap(erf, 7.7, 10d, fileName+"_Part7pt7", "test", fileName+"_Part7pt7");
			GMT_CA_Maps.plotM6_5_BulgeMap(erf, 6.5, 1.0, fileName+"_Bulge", "test", fileName+"_Bulge");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static void makeDraftFinalModelReportPartPlots() {

		try {
			// UCERF2_FaultSysSol_ERF
//			UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
//			erf.updateForecast();
//			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, "testUCERF3_ERF", "test", "testUCERF3_ERF");
			
//			ModMeanUCERF2 erf= new ModMeanUCERF2();
//			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
//			erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
//			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//			erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
//			erf.getTimeSpan().setDuration(1d);
//			erf.updateForecast();
//			String fileName = "UCERF2";

			File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/draftFinalModelReport/FaultSystemSolutions/FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_sol.zip");
			UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(file);
			erf.updateForecast();
			String fileName = "UCERF3_Char_Ref_Zeng_Model";
			
//			File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/draftFinalModelReport/FaultSystemSolutions/FM3_1_ZENG_EllB_DsrUni_GRConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_sol.zip");
//			UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(file);
//			erf.updateForecast();
//			String fileName = "UCERF3_GR_Ref_Zeng_Model";
			
			GMT_CA_Maps.plotParticipationRateMap(erf, 5.0, 10d, fileName+"_Part5pt0", "test", fileName+"_Part5pt0");
			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, fileName+"_Part6pt7", "test", fileName+"_Part6pt7");
			GMT_CA_Maps.plotParticipationRateMap(erf, 7.7, 10d, fileName+"_Part7pt7", "test", fileName+"_Part7pt7");
			GMT_CA_Maps.plotM6_5_BulgeMap(erf, 6.5, 1.0, fileName+"_Bulge", "test", fileName+"_Bulge");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	/**
	 *
	 */
	public static void makeUCERF2_PartRateMaps() {

		try {
			
			MeanUCERF2 erf= new MeanUCERF2();
			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
			erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
			erf.getTimeSpan().setDuration(1d);
			erf.updateForecast();
			String fileName = "UCERF2";

			
			
			GMT_CA_Maps.plotParticipationRateMap(erf, 5.0, 10d, fileName+"_Part5pt0", "test", fileName+"_Part5pt0");
			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, fileName+"_Part6pt7", "test", fileName+"_Part6pt7");
			GMT_CA_Maps.plotParticipationRateMap(erf, 7.7, 10d, fileName+"_Part7pt7", "test", fileName+"_Part7pt7");
			GMT_CA_Maps.plotParticipationRateMap(erf, 8.0, 10d, fileName+"_Part8pt0", "test", fileName+"_Part8pt0");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	/**
	 * The result here is pretty close, but not exact.  Looking back at the UCERF2 code 
	 * it looks like binning might have been off by a half delta (e.g., the excel probability file
	 * has off-fault values for M 7 and above), but I'm not sure the explains the diff.  I think the
	 * new way of computing things here is cleaner, and therefore likely better.  The differences are
	 * not noticable unless you do a close overlay, so I'm not sure this is worth pursuing further.
	 */
	public static void testUCERF2_Figure25() {
		
		UCERF2_TimeDependentEpistemicList erf_list = new UCERF2_TimeDependentEpistemicList();

		erf_list.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		erf_list.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		erf_list.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		erf_list.getTimeSpan().setDuration(1d);
		erf_list.updateForecast();
		String fileName = "UCERF2";
		XY_DataSetList wtedFuncList = new XY_DataSetList();
		ArrayList<Double> relativeWts = new ArrayList<Double>();
		
		for(int e=0;e<erf_list.getNumERFs();e++) {
//		for(int e=0;e<10;e++) {
			System.out.println(e+" of "+erf_list.getNumERFs());
			SummedMagFreqDist mfdPart = ERF_Calculator.getParticipationMagFreqDistInRegion(erf_list.getERF(e), new CaliforniaRegions.SF_BOX(), 5.05, 40, 0.1, true);
			EvenlyDiscretizedFunc testMFD_Part = mfdPart.getCumRateDistWithOffset();
			for(int i=0;i<testMFD_Part.getNum();i++) {
				double prob_part = 1.0 - Math.exp(-testMFD_Part.getY(i)*30);
				testMFD_Part.set(i,prob_part);
			}
			wtedFuncList.add(testMFD_Part);
			relativeWts.add(erf_list.getERF_RelativeWeight(e));
		}
		FractileCurveCalculator fractileCalc = new FractileCurveCalculator(wtedFuncList,relativeWts);
		
		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		funcs.add(fractileCalc.getMeanCurve());
		funcs.add(fractileCalc.getFractile(0.025));
		funcs.add(fractileCalc.getFractile(0.5));
		funcs.add(fractileCalc.getFractile(0.975));

		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Test of UCERF2 Figure 25");
		graph.setX_AxisRange(5, 8.5);
		graph.setY_AxisRange(0, 1);
	}
	
	
	public static void plotMFD_InRegion(Region region, String fileName) {
		
		// First all UCERF2 branches:
		UCERF2_TimeIndependentEpistemicList erf_list = new UCERF2_TimeIndependentEpistemicList();
		erf_list.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		erf_list.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		erf_list.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		erf_list.getTimeSpan().setDuration(1d);
		erf_list.updateForecast();
				
		XY_DataSetList wtedFuncList = new XY_DataSetList();
		ArrayList<Double> relativeWts = new ArrayList<Double>();
		
		for(int e=0;e<erf_list.getNumERFs();e++) {
			System.out.println(e+" of "+erf_list.getNumERFs());
			SummedMagFreqDist mfdPart = ERF_Calculator.getParticipationMagFreqDistInRegion(erf_list.getERF(e), region, 5.05, 40, 0.1, true);
			wtedFuncList.add(mfdPart.getCumRateDistWithOffset());
			relativeWts.add(erf_list.getERF_RelativeWeight(e));
		}
		FractileCurveCalculator fractileCalc = new FractileCurveCalculator(wtedFuncList,relativeWts);
		
		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		funcs.add(fractileCalc.getMeanCurve());
//		funcs.add(fractileCalc.getFractile(0.025));
//		funcs.add(fractileCalc.getFractile(0.975));	
		funcs.add(fractileCalc.getMinimumCurve());
		funcs.add(fractileCalc.getMaximumCurve());
		funcs.add(fractileCalc.getFractile(0.5));

		// Now mean UCERF2
		MeanUCERF2 erf= new MeanUCERF2();
		erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		erf.getTimeSpan().setDuration(1d);
		erf.updateForecast();
		SummedMagFreqDist meanPart = ERF_Calculator.getParticipationMagFreqDistInRegion(erf, region, 5.05, 40, 0.1, true);
		meanPart.setName("MFD for MeanUCERF2");
		meanPart.setInfo(" ");
		funcs.add(meanPart.getCumRateDistWithOffset());	
		
		
		File file = new File("/Users/field/Downloads/FaultSystemSolutions/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
		UCERF3_FaultSysSol_ERF ucerf3_erf = new UCERF3_FaultSysSol_ERF(file);
		ucerf3_erf.updateForecast();
		SummedMagFreqDist ucerf3_Part = ERF_Calculator.getParticipationMagFreqDistInRegion(ucerf3_erf, region, 5.05, 40, 0.1, true);
		ucerf3_Part.setName("MFD for UCERF3 Char Reference Branch");
		ucerf3_Part.setInfo(" ");
		funcs.add(ucerf3_Part.getCumRateDistWithOffset());	
		
//		File file = new File("/Users/field/Downloads/FaultSystemSolutions/FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_sol.zip");
//		UCERF3_FaultSysSol_ERF ucerf3_erf_2 = new UCERF3_FaultSysSol_ERF(file);
//		ucerf3_erf_2.updateForecast();
//		SummedMagFreqDist ucerf3_Part_2 = ERF_Calculator.getParticipationMagFreqDistInRegion(ucerf3_erf_2, region, 5.05, 40, 0.1, true);
//		ucerf3_Part_2.setName("MFD for UCERF3 Char Reference Branch w/ Zeng");
//		ucerf3_Part_2.setInfo(" ");
//		funcs.add(ucerf3_Part_2.getCumRateDistWithOffset());	

//		file = new File("/Users/field/Downloads/FaultSystemSolutions/FM3_1_ZENG_EllB_DsrUni_GRConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_sol.zip");
//		UCERF3_FaultSysSol_ERF ucerf3_erf_3 = new UCERF3_FaultSysSol_ERF(file);
//		ucerf3_erf_3.updateForecast();
//		SummedMagFreqDist ucerf3_Part_3 = ERF_Calculator.getParticipationMagFreqDistInRegion(ucerf3_erf_3, region, 5.05, 40, 0.1, true);
//		ucerf3_Part_3.setName("MFD for UCERF3 GR Reference Branch w/ Zeng");
//		ucerf3_Part_3.setInfo(" ");
//		funcs.add(ucerf3_Part_3.getCumRateDistWithOffset());	
		
    	ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, null, 1f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, null, 1f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.GREEN));


		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Cumulative MFDs in "+region.getName(),plotChars); 
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Rate (per year)");
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(18);
		graph.setTickLabelFontSize(16);
		graph.setX_AxisRange(5, 8.5);
		graph.setY_AxisRange(1e-4, 1);
		graph.setYLog(true);
		
		try {
			graph.saveAsPDF(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		makeUCERF2_PartRateMaps();
		
//		testUCERF2_Figure25();
		
//		makeDraftFinalModelReportPartPlots();
		
//		plotMFD_InRegion(new CaliforniaRegions.NORTHRIDGE_BOX(), "Northridge_BoxMFDs.pdf");
//		plotMFD_InRegion(new CaliforniaRegions.SF_BOX(), "SF_BoxMFDs.pdf");
//		plotMFD_InRegion(new CaliforniaRegions.LA_BOX(), "LA_BoxMFDs.pdf");
		
//		makePrelimReportPartPlots();
		
		
//		File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/Fig16_ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//		SimpleFaultSystemSolution tmp = null;
//		try {
//			tmp =  SimpleFaultSystemSolution.fromFile(file);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("numRups="+tmp.getNumRuptures());
//		System.out.println("numSect="+tmp.getNumSections());


	}

}
