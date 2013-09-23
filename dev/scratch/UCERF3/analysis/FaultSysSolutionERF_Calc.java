package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentException;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.WeightedFuncList;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolTimeDepERF;
import scratch.UCERF3.griddedSeismicity.FaultPolyMgr;
import scratch.UCERF3.griddedSeismicity.SmallMagScaling;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
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
		try {
			invFss = FaultSystemIO.loadInvSol(faultSysSolZipFile);
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}

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
	 * This makes the iconic figure of U3.3, Fm3.1 participation rate maps, where section rates are properly 
	 * mapped onto polygon grid nodes, and topography is included. Aftershocks are included.
	 * Results are in OpenSHA/dev/scratch/UCERF3/data/scratch/GMT/COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL_Part_*
	 * (where the "*" part is the minMagArray value(s) set below)
	 */
	public static void makeIconicFigureForU3pt3_and_FM3pt1() {

		try {
			
			double[] minMagArray = {5,6.7};	// the mags to iterate over
			double maxMag=10d;

			// average solution for FM 3.1
			String f ="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
			File file = new File(f);

			System.out.println("Instantiating ERF...");
			UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(file);
			erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.12);
			erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(false);
			erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);	// don't include fault based sources here
			erf.getParameter("Treat Background Seismicity As").setValue(BackgroundRupType.CROSSHAIR);	// this creates some faint cross artifacts due to tighter smoothing
			erf.updateForecast();
			String fileName = "COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL";
			
			System.out.println(erf.getAdjustableParameterList().toString());
			
			
			String scaleLabel = "Participation Rate";
			String metadata =" ";
			
			// get the fault system solution and the polygon manager
			InversionFaultSystemSolution fss = (InversionFaultSystemSolution) erf.getSolution();
			FaultPolyMgr fltPolyMgr = fss.getRupSet().getInversionTargetMFDs().getGridSeisUtils().getPolyMgr();
			
			for(double minMag: minMagArray) {
				
				// compute participation rates for supra-seis rups mapped onto grid nodes inside polygons
				System.out.println("fss.calcParticRateForAllSects...");
				double[] sectPartRates = fss.calcParticRateForAllSects(minMag, maxMag);
				GriddedGeoDataSet supraSeisPartRates_xyzData = new GriddedGeoDataSet(GMT_CA_Maps.defaultGridRegion, true);	// true makes X latitude
				for(int s=0; s<sectPartRates.length;s++) {
					Map<Integer, Double> nodesForSectMap = fltPolyMgr.getNodeFractions(s);
					Set<Integer> nodeIndicesList = nodesForSectMap.keySet();
					for(int index:nodeIndicesList) {
						double oldRate = supraSeisPartRates_xyzData.get(index);
						supraSeisPartRates_xyzData.set(index, oldRate+nodesForSectMap.get(index)*sectPartRates[s]);
					}
				}
				
				// convert minMag to string for filename
				Double tempDouble = new Double(minMag);
				String magString = tempDouble.toString();
				String dirName = fileName+"_Part_"+magString.replace(".", "pt");
				
				System.out.println(dirName);
				
				System.out.println("ERF_Calculator.getParticipationRatesInRegion...");
				GriddedGeoDataSet geoDataSetForGridSeis = ERF_Calculator.getParticipationRatesInRegion(erf, GMT_CA_Maps.defaultGridRegion, minMag, maxMag);
				GMT_MapGenerator gmt_MapGenerator = GMT_CA_Maps.getDefaultGMT_MapGenerator();
				
				GeoDataSet sumGeoDataSet = GeoDataSetMath.add(supraSeisPartRates_xyzData, geoDataSetForGridSeis);
				
				System.out.println("Making GMT Map...");
				//override default scale
				gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, -6d);
				gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, -2d);
				gmt_MapGenerator.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_30_GLOBAL);
				gmt_MapGenerator.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, true);
				gmt_MapGenerator.setParameter(GMT_MapGenerator.DPI_PARAM_NAME, 300);
				gmt_MapGenerator.setParameter(GMT_MapGenerator.BLACK_BACKGROUND_PARAM_NAME, false);
				gmt_MapGenerator.setParameter(GMT_MapGenerator.KML_PARAM_NAME, true);

				// must set this parameter this way because the setValue(CPT) method takes a CPT object, and it must be the
				// exact same object as in the constraint (same instance); the setValue(String) method was added for convenience
				// but it won't succeed for the isAllowed(value) call.
				CPTParameter cptParam = (CPTParameter )gmt_MapGenerator.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
				cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());

				GMT_CA_Maps.makeMap(sumGeoDataSet, "M>="+minMag+" "+scaleLabel, metadata, dirName, gmt_MapGenerator);

			}
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

		GraphWindow graph = new GraphWindow(funcs, "Test of UCERF2 Figure 25");
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


		GraphWindow graph = new GraphWindow(funcs, "Cumulative MFDs in "+region.getName(),plotChars); 
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
	 * This comparison does not include aftershocks
	 */
	public static void plot_U3pt3_U2_TotalMeanMFDs() {
		
//		double aleatoryMagAreaVar = 0.12;
		double aleatoryMagAreaVar = 0.0;
		
		Region relmRegion = RELM_RegionUtils.getGriddedRegionInstance();

		// average solution for FM 3.1
		String f ="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		File file = new File(f);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(file);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(aleatoryMagAreaVar);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(false);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
		erf.getParameter("Treat Background Seismicity As").setValue(BackgroundRupType.POINT);
		erf.updateForecast();
		SummedMagFreqDist mfd_U3_total = ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true);
		
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		erf.updateForecast();
		SummedMagFreqDist mfd_U3_faults = ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true);
		
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
		erf.updateForecast();
		SummedMagFreqDist mfd_U3_gridded = ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true);


		// average solution for FM 3.1
		String f2 ="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_2_MEAN_BRANCH_AVG_SOL.zip";
		File file2 = new File(f2);
		erf = new UCERF3_FaultSysSol_ERF(file2);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(aleatoryMagAreaVar);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(false);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
		erf.getParameter("Treat Background Seismicity As").setValue(BackgroundRupType.POINT);
		erf.updateForecast();
		
		mfd_U3_total.addResampledMagFreqDist(ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true), true);
		mfd_U3_total.scale(0.5);
		EvenlyDiscretizedFunc mfd_U3_total_cum = mfd_U3_total.getCumRateDistWithOffset();
		mfd_U3_total_cum.setName("Cumulative MFD for Mean UCERF3 - Total");
		
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		erf.updateForecast();
		mfd_U3_faults.addResampledMagFreqDist(ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true), true);
		mfd_U3_faults.scale(0.5);
		EvenlyDiscretizedFunc mfd_U3_faults_cum = mfd_U3_faults.getCumRateDistWithOffset();
		mfd_U3_faults_cum.setName("Cumulative MFD for Mean UCERF3 - Faults");

		
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
		erf.updateForecast();
		mfd_U3_gridded.addResampledMagFreqDist(ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true), true);
		mfd_U3_gridded.scale(0.5);
		EvenlyDiscretizedFunc mfd_U3_gridded_cum = mfd_U3_gridded.getCumRateDistWithOffset();
		mfd_U3_gridded_cum.setName("Cumulative MFD for Mean UCERF3 - Gridded");


		// Now mean UCERF2
		UCERF2_MFD_ConstraintFetcher U2_fetcher = new UCERF2_MFD_ConstraintFetcher(relmRegion);
		EvenlyDiscretizedFunc mfd_U2_total_cum = U2_fetcher.getTotalMFD().getCumRateDistWithOffset();
		EvenlyDiscretizedFunc mfd_U2_faults_cum = U2_fetcher.getFaultMFD().getCumRateDistWithOffset();
		EvenlyDiscretizedFunc mfd_U2_gridded_cum = U2_fetcher.getBackgroundSeisMFD().getCumRateDistWithOffset();
		
		
//		MeanUCERF2 erf_U2= new MeanUCERF2();
//		ModMeanUCERF2 erf_U2= new ModMeanUCERF2();
//		erf_U2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
//		erf_U2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
//		erf_U2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		erf_U2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
//		erf_U2.getTimeSpan().setDuration(1d);
//		erf_U2.updateForecast();
//		SummedMagFreqDist mfd_U2 = ERF_Calculator.getMagFreqDistInRegion(erf_U2, relmRegion, 5.05, 40, 0.1, true);
//		EvenlyDiscretizedFunc mfd_U2_cum = mfd_U2.getCumRateDistWithOffset();
//		mfd_U2_cum.setName("Cumulative MFD for Mean UCERF2");
//		mfd_U2_cum.setInfo(mfd_U2.toString());

		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		funcs.add(mfd_U3_total_cum);
		funcs.add(mfd_U2_total_cum);
		funcs.add(mfd_U3_faults_cum);
		funcs.add(mfd_U2_faults_cum);
		funcs.add(mfd_U3_gridded_cum);
		funcs.add(mfd_U2_gridded_cum);

		GraphWindow graph = new GraphWindow(funcs, "U2 and U3 Cumulative MFDs");
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Cumulative Rate (per year)");
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(18);
		graph.setTickLabelFontSize(16);
		graph.setX_AxisRange(5, 9.0);
		graph.setY_AxisRange(1e-6, 10);
		graph.setYLog(true);
		
		String tableString = new String();
		tableString="U3_total\tU3_faults\tU3_gridded\tU2_total\tU2_faults\tU2_gridded\n";
		for(double mag=5;mag<8.8;mag+=0.1) {
			tableString+=(float)mag+"\t";
			tableString+=mfd_U3_total_cum.getClosestY(mag)+"\t";
			tableString+=mfd_U3_faults_cum.getClosestY(mag)+"\t";
			tableString+=mfd_U3_gridded_cum.getClosestY(mag)+"\t";
			tableString+=mfd_U2_total_cum.getClosestY(mag)+"\t";
			tableString+=mfd_U2_faults_cum.getClosestY(mag)+"\t";
			tableString+=mfd_U2_gridded_cum.getClosestY(mag)+"\n";
		}
		System.out.println(tableString);		
		
		File dataFile = new File("dev/scratch/UCERF3/data/scratch/aveMFDs_ForU3_andU2.txt");
		try {
			FileWriter fw = new FileWriter(dataFile);
			fw.write(tableString);
			fw.close ();
			graph.saveAsPDF("dev/scratch/UCERF3/data/scratch/aveMFDs_ForU3_andU2.pdf");
		}
		catch (IOException e) {
			System.out.println ("IO exception = " + e );
		}

	}

	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		plot_U3pt3_U2_TotalMeanMFDs();
		
//		makeIconicFigureForU3pt3_and_FM3pt1();
				
//		makeUCERF2_PartRateMaps();
		
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
