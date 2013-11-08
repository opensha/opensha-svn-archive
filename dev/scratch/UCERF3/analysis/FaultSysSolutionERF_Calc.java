package scratch.UCERF3.analysis;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.dom4j.DocumentException;
import org.jfree.data.Range;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.WeightedFuncList;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.erf.FSSRupsInRegionCache;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolTimeDepERF;
import scratch.UCERF3.erf.mean.MeanUCERF3;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.FaultPolyMgr;
import scratch.UCERF3.griddedSeismicity.SmallMagScaling;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
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
		
		MeanUCERF3 erf = new MeanUCERF3();
		erf.setMeanParams(10d, true, 0d, MeanUCERF3.RAKE_BASIS_MEAN);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(aleatoryMagAreaVar);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(false);
		erf.getParameter("Treat Background Seismicity As").setValue(BackgroundRupType.POINT);
		
		System.out.println("Working on INCLUDE");
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
		erf.updateForecast();
		SummedMagFreqDist mfd_U3_total = ERF_Calculator.getMagFreqDistInRegionFaster(erf, relmRegion, 5.05, 40, 0.1, true);
		
		System.out.println("Working on EXCLUDE");
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		erf.updateForecast();
		SummedMagFreqDist mfd_U3_faults = ERF_Calculator.getMagFreqDistInRegionFaster(erf, relmRegion, 5.05, 40, 0.1, true);
		
		System.out.println("Working on ONLY");
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
		erf.updateForecast();
		SummedMagFreqDist mfd_U3_gridded = ERF_Calculator.getMagFreqDistInRegionFaster(erf, relmRegion, 5.05, 40, 0.1, true);
		
		EvenlyDiscretizedFunc mfd_U3_total_cum = mfd_U3_total.getCumRateDistWithOffset();
		mfd_U3_total_cum.setName("Cumulative MFD for Mean UCERF3 - Total");
		EvenlyDiscretizedFunc mfd_U3_faults_cum = mfd_U3_faults.getCumRateDistWithOffset();
		mfd_U3_faults_cum.setName("Cumulative MFD for Mean UCERF3 - Faults");
		EvenlyDiscretizedFunc mfd_U3_gridded_cum = mfd_U3_gridded.getCumRateDistWithOffset();
		mfd_U3_gridded_cum.setName("Cumulative MFD for Mean UCERF3 - Gridded");




//		// average solution for FM 3.1
//		String f ="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
//		File file = new File(f);
//		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(file);
//		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(aleatoryMagAreaVar);
//		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(false);
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
//		erf.getParameter("Treat Background Seismicity As").setValue(BackgroundRupType.POINT);
//		erf.updateForecast();
//		SummedMagFreqDist mfd_U3_total = ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true);
//		
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
//		erf.updateForecast();
//		SummedMagFreqDist mfd_U3_faults = ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true);
//		
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
//		erf.updateForecast();
//		SummedMagFreqDist mfd_U3_gridded = ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true);
//
//
//		// average solution for FM 3.2
//		String f2 ="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_2_MEAN_BRANCH_AVG_SOL.zip";
//		File file2 = new File(f2);
//		erf = new UCERF3_FaultSysSol_ERF(file2);
//		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(aleatoryMagAreaVar);
//		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(false);
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
//		erf.getParameter("Treat Background Seismicity As").setValue(BackgroundRupType.POINT);
//		erf.updateForecast();
//		
//		mfd_U3_total.addResampledMagFreqDist(ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true), true);
//		mfd_U3_total.scale(0.5);
//		EvenlyDiscretizedFunc mfd_U3_total_cum = mfd_U3_total.getCumRateDistWithOffset();
//		mfd_U3_total_cum.setName("Cumulative MFD for Mean UCERF3 - Total");
//		
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
//		erf.updateForecast();
//		mfd_U3_faults.addResampledMagFreqDist(ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true), true);
//		mfd_U3_faults.scale(0.5);
//		EvenlyDiscretizedFunc mfd_U3_faults_cum = mfd_U3_faults.getCumRateDistWithOffset();
//		mfd_U3_faults_cum.setName("Cumulative MFD for Mean UCERF3 - Faults");
//
//		
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
//		erf.updateForecast();
//		mfd_U3_gridded.addResampledMagFreqDist(ERF_Calculator.getMagFreqDistInRegion(erf, relmRegion, 5.05, 40, 0.1, true), true);
//		mfd_U3_gridded.scale(0.5);
//		EvenlyDiscretizedFunc mfd_U3_gridded_cum = mfd_U3_gridded.getCumRateDistWithOffset();
//		mfd_U3_gridded_cum.setName("Cumulative MFD for Mean UCERF3 - Gridded");


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
	
//	public static void makeRegionalProb

	/**
	 * This calculates a cumulative magnitude vs probability distribution for the given FSS ERF and region.
	 * Each point in the returned function represents the probability in the forecast (using the forecast duration)
	 * of a rupture at or above the given magnitude with any portion inside the region.
	 * 
	 * @param erf
	 * @param region
	 * @param minMag
	 * @param numMag
	 * @param deltaMag
	 * @param calcFromMFD if true probabilities will be calculated by first computing participation MFD for the region,
	 * otherwise probabilities will be summed for each source as totProb = 1 - (1 - prob1)*(1 - prob2)*...*(1 - probN)
	 * @param cache optional but recommended - this cache will greatly speed up calculations and can be reused for
	 * different calls to this method with different durations, probability models, or regions.
	 * @return
	 */
	public static EvenlyDiscretizedFunc calcMagProbDist(FaultSystemSolutionERF erf, Region region,
			double minMag, int numMag, double deltaMag, boolean calcFromMFD, FSSRupsInRegionCache cache) {
		Preconditions.checkState(numMag > 0);
		erf.updateForecast();
		double duration = erf.getTimeSpan().getDuration();
		
		if (calcFromMFD) {
			// just use the MFD itself
			// we want the cumulative distribution, so shift minMag up by half a mag bin
			// and then get cumulative dist with offset
			SummedMagFreqDist incrMFD = ERF_Calculator.getParticipationMagFreqDistInRegion(
					erf, region, minMag+0.5*deltaMag, numMag, deltaMag, true, cache);
			EvenlyDiscretizedFunc mfd = incrMFD.getCumRateDistWithOffset();
			Preconditions.checkState(minMag == mfd.getMinX());
			EvenlyDiscretizedFunc result = calcProbsFromSummedMFD(mfd, duration);
			Preconditions.checkState(minMag == result.getMinX());
			return result;
		} else {
			// calc from each source itself
			if (cache == null)
				cache = new FSSRupsInRegionCache(erf);
			
			EvenlyDiscretizedFunc result = new EvenlyDiscretizedFunc(minMag, numMag, deltaMag);
			
			// this tracks the rupture probabilities for each mag bin
			List<List<Double>> probsList = Lists.newArrayList();
			for (int m=0; m<numMag; m++)
				probsList.add(new ArrayList<Double>());
			
			for (int sourceID=0; sourceID<erf.getNumFaultSystemSources(); sourceID++) {
				ProbEqkSource source = erf.getSource(sourceID);
				if (!cache.isRupInRegion(source, source.getRupture(0), sourceID, 0, region))
					// source is just for a single rupture, if the first rup isn't in the region none are
					continue;
				for (ProbEqkRupture rup : source) {
					double prob = rup.getProbability();
					double mag = rup.getMag();
					populateProbList(mag, prob, probsList, result);
				}
			}
			
			// now sum the probabilities as:
			calcSummedProbs(probsList, result);
			return result;
		}
	}
	
	private static EvenlyDiscretizedFunc calcProbsFromSummedMFD(EvenlyDiscretizedFunc cmlMFD, double duration) {
		int numMag = cmlMFD.getNum();
		EvenlyDiscretizedFunc result = new EvenlyDiscretizedFunc(cmlMFD.getMinX(), numMag, cmlMFD.getDelta());
		
		// convert from rates to poisson probabilities
		for (int i=0; i<numMag; i++) {
			double rate = cmlMFD.getY(i);
			double prob = 1-Math.exp(-rate*duration);
			result.set(i, prob);
		}
		return result;
	}
	
	private static void calcSummedProbs(List<List<Double>> probsList, EvenlyDiscretizedFunc result) {
		// now sum the probabilities as:
		// totProb = 1 - (1 - prob1)*(1 - prob2)*...*(1 - probN)
		for (int i=0; i<result.getNum(); i++) {
			List<Double> probs = probsList.get(i);
			double totOneMinus = 1;
			for (double prob : probs) {
				totOneMinus *= (1-prob);
			}
			double totProb = 1 - totOneMinus;
			result.set(i, totProb);
//			System.out.println("\tM "+result.getX(i)+"+ Prob: "+(float)(totProb*100d)+" %");
		}
	}
	
	private static void populateProbList(double mag, double prob, List<List<Double>> probsList,
			EvenlyDiscretizedFunc xVals) {
		// we want to find the smallest mag in the function where rupMag >= mag
		if (mag < xVals.getMinX())
			return;
		int magIndex = xVals.getClosestXIndex(mag);
		// closest could be above, check for that and correct
		if (mag < xVals.getX(magIndex))
			magIndex--;
		Preconditions.checkState(magIndex >= 0);
		for (int m=0; m<=magIndex && m<xVals.getNum(); m++)
			probsList.get(m).add(prob);
	}
	
	/**
	 * This calculates rupture probability distributions for supra-seismogenic ruptures
	 * on each fault section from the given ERF. Functions are returned in an array
	 * corresponding to the section index in the FaultSystemRupSet, with magnitude in the
	 * x field and cumulative probability in the y field.
	 * 
	 * @param erf
	 * @param minMag
	 * @param numMag
	 * @param deltaMag
	 * @return
	 */
	public static EvenlyDiscretizedFunc[] calcSubSectSupraSeisMagProbDists(
			FaultSystemSolutionERF erf, double minMag, int numMag, double deltaMag) {
		return calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag, 10d);
	}
	
	private static EvenlyDiscretizedFunc[] calcSubSectSupraSeisMagProbDists(
			FaultSystemSolutionERF erf, double minMag, int numMag, double deltaMag, double overallMaxMag) {
		erf.updateForecast();
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		int numSects = rupSet.getNumSections();
		
		// create a list of all rupture probs for each section
		List<List<List<Double>>> sectProbLists = Lists.newArrayList();
		for (int i=0; i<numSects; i++) {
			List<List<Double>> probLists = Lists.newArrayList();
			for (int m=0; m<numMag; m++)
				probLists.add(new ArrayList<Double>());
			sectProbLists.add(probLists);
		}
		
		EvenlyDiscretizedFunc xVals = new EvenlyDiscretizedFunc(minMag, numMag, deltaMag);
		
		for (int sourceID=0; sourceID<erf.getNumFaultSystemSources(); sourceID++) {
			int invIndex = erf.getFltSysRupIndexForSource(sourceID);
			for (ProbEqkRupture rup : erf.getSource(sourceID)) {
				double mag = rup.getMag();
				if (mag > overallMaxMag)
					continue;
				double prob = rup.getProbability();
				for (int sectIndex : rupSet.getSectionsIndicesForRup(invIndex)) {
					populateProbList(mag, prob, sectProbLists.get(sectIndex), xVals);
				}
			}
		}
		
		EvenlyDiscretizedFunc[] results = new EvenlyDiscretizedFunc[numSects];
		for (int sectIndex=0; sectIndex<numSects; sectIndex++) {
			results[sectIndex] = new EvenlyDiscretizedFunc(minMag, numMag, deltaMag);
			calcSummedProbs(sectProbLists.get(sectIndex), results[sectIndex]);
		}
		return results;
	}
	
	/**
	 * This calculates rupture probability distributions for supra-seismogenic ruptures
	 * on each parent fault section from the given ERF. Functions are returned in a map
	 * keyed on the parent section ID, with magnitude in the x field and cumulative
	 * probability in the y field.
	 * 
	 * @param erf
	 * @param minMag
	 * @param numMag
	 * @param deltaMag
	 * @return
	 */
	public static Map<Integer, EvenlyDiscretizedFunc> calcParentSectSupraSeisMagProbDists(
			FaultSystemSolutionERF erf, double minMag, int numMag, double deltaMag) {
		return calcParentSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag, 10d);
	}
	
	private static Map<Integer, EvenlyDiscretizedFunc> calcParentSectSupraSeisMagProbDists(
			FaultSystemSolutionERF erf, double minMag, int numMag, double deltaMag, double overallMaxMag) {
		erf.updateForecast();
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		
		HashSet<Integer> parentIDs = new HashSet<Integer>();
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList())
			parentIDs.add(sect.getParentSectionId());
		
		// create a list of all rupture probs for each parent section
		Map<Integer, List<List<Double>>> sectProbLists = Maps.newHashMap();
		for (Integer parentID : parentIDs) {
			List<List<Double>> probLists = Lists.newArrayList();
			for (int m=0; m<numMag; m++)
				probLists.add(new ArrayList<Double>());
			sectProbLists.put(parentID, probLists);
		}
		
		EvenlyDiscretizedFunc xVals = new EvenlyDiscretizedFunc(minMag, numMag, deltaMag);
		
		for (int sourceID=0; sourceID<erf.getNumFaultSystemSources(); sourceID++) {
			int invIndex = erf.getFltSysRupIndexForSource(sourceID);
			for (ProbEqkRupture rup : erf.getSource(sourceID)) {
				double mag = rup.getMag();
				if (mag > overallMaxMag)
					continue;
				double prob = rup.getProbability();
				for (int parentID : rupSet.getParentSectionsForRup(invIndex)) {
					populateProbList(mag, prob, sectProbLists.get(parentID), xVals);
				}
			}
		}
		
		Map<Integer, EvenlyDiscretizedFunc> results = Maps.newHashMap();
		for (int parentID : parentIDs) {
			EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(minMag, numMag, deltaMag);
			calcSummedProbs(sectProbLists.get(parentID), func);
			results.put(parentID, func);
		}
		return results;
	}
	
	private static final double YEARS_PER_MILLI = 1d/((double)(1000l*60l*60l*24l)*365.242);
	
	/**
	 * This generates a set of statewide fault probability gain maps for the given fault system
	 * solution.
	 * @param erf
	 * @param saveDir directory where plots should be saved
	 * @param prefix file prefix
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public static void makeFaultProbGainMaps(FaultSystemSolutionERF erf, File saveDir, String prefix
			) throws GMT_MapException, RuntimeException, IOException {
		double minMag = 6.5;
		int numMag = 4;
		double deltaMag = 0.5;
		
		FaultSystemSolution sol = erf.getSolution();
		
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] poissonFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] poissonAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
		EvenlyDiscretizedFunc[] poissonSmallMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d);
		
		// TODO historical open interval?
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] bptFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] bptAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
		EvenlyDiscretizedFunc[] bptSmallMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d);
		
		// log space
		CPT probCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-4, 0);
//		CPT ratioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-0.5, 0.5);
		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT();
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		double duration = erf.getTimeSpan().getDuration();
		prefix += (float)duration+"yr";
		
		for (int i=0; i<numMag+2; i++) {
			
			double[] poissonVals;
			double[] bptVals;
			String myPrefix;
			String magStr;
			if (i == numMag) {
				poissonVals = extractYVals(poissonAllMags, 0);
				bptVals = extractYVals(bptAllMags, 0);
				myPrefix = prefix+"_supra_seis";
				magStr = "Supra Seis";
			} else if (i == numMag+1) {
				poissonVals = extractYVals(poissonSmallMags, 0);
				bptVals = extractYVals(bptSmallMags, 0);
				myPrefix = prefix+"_below_7";
				magStr = "All M<=7";
			} else {
				poissonVals = extractYVals(poissonFuncs, i);
				bptVals = extractYVals(bptFuncs, i);
				double mag = poissonFuncs[0].getX(i);
				myPrefix = prefix+"_"+(float)mag+"+";
				magStr = "M>="+(float)mag;
			}
			
			double[] ratioVals = new double[poissonVals.length];
			for (int j=0; j<ratioVals.length; j++)
				ratioVals[j] = bptVals[j]/poissonVals[j];
			
			// poisson probs
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(poissonVals), region,
					saveDir, myPrefix+"_poisson", false, true,
					"Log10("+(float)duration+" yr "+magStr+" Poisson Prob)");
			// bpt probs
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(bptVals), region,
					saveDir, myPrefix+"_bpt", false, true,
					"Log10("+(float)duration+" yr "+magStr+" BPT Prob)");
			// prob gain
			FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, ratioVals, region,
					saveDir, myPrefix+"_prob_gain", false, true,
					(float)duration+" yr "+magStr+" BPT/Poisson Prob Gain");
		}
		
		// now make normalized time since last event
		double[] normTimeSinceLast = new double[poissonAllMags.length];
		ProbabilityModelsCalc calc = new ProbabilityModelsCalc(
				((BPT_AperiodicityParam)erf.getParameter(BPT_AperiodicityParam.NAME)).getValue());
		double[] sectImpliedProbGain = new double[poissonAllMags.length];
		long curTime = System.currentTimeMillis();
		FaultSystemRupSet rupSet = sol.getRupSet();
		double[] partRates = sol.calcTotParticRateForAllSects();
		for (int i=0; i<normTimeSinceLast.length; i++) {
			FaultSectionPrefData sect = rupSet.getFaultSectionData(i);
			long dateLast = sect.getDateOfLastEvent();
			if (dateLast == Long.MIN_VALUE) {
				normTimeSinceLast[i] = Double.NaN;
				sectImpliedProbGain[i] = Double.NaN;
			} else {
				long deltaMillis = curTime - dateLast;
				double diffYears = YEARS_PER_MILLI*deltaMillis;
				double ri = 1d/partRates[i];
				normTimeSinceLast[i] = diffYears / ri;
				double bptProb = calc.computeBPT_ProbFast(ri, diffYears, duration);
				double poissonProb = ProbabilityModelsCalc.computePoissonProb(ri, duration);
				sectImpliedProbGain[i] = bptProb/poissonProb;
			}
		}
		// norm time since last
		FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, normTimeSinceLast, region,
				saveDir, prefix+"_norm_time_since_last", false, true,
				"Normalized Time Since Last Event");
		// sect implied
		FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, sectImpliedProbGain, region,
				saveDir, prefix+"_sect_implied_prob_gain", false, true,
				"Sect Implied Prob Gain");
	}
	
	private static boolean[] toArray(boolean... vals) {
		return vals;
	}
	
	private static String getBPTCalcTypeStr(boolean[] choices) {
		String str;
		if (choices[0])
			str = "AveRI";
		else
			str = "AveRate";
		str += "&";
		if (choices[1])
			str += "AveNormTS";
		else
			str += "AveTS";
		return str;
	}
	
	/**
	 * This generates a set of statewide fault probability gain maps for the given fault system
	 * solution, exploring different averaging options.
	 * @param erf no need to update forecast
	 * @param saveDir directory where plots should be saved
	 * @param prefix file prefix
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public static void makeAvgMethodProbGainMaps(FaultSystemSolutionERF erf, File saveDir, String prefix
			) throws GMT_MapException, RuntimeException, IOException {
		double minMag = 6.5;
		int numMag = 4;
		double deltaMag = 0.5;
		
		List<boolean[]> avgBools = Lists.newArrayList();
		int refIndex = 1;
		avgBools.add(toArray(false, false));
		avgBools.add(toArray(false, true)); // reference
		avgBools.add(toArray(true, false));
		avgBools.add(toArray(true, true));
		
		// TODO historical open interval?
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.updateForecast();
		
		List<EvenlyDiscretizedFunc[]> regFuncsList = Lists.newArrayList();
		List<EvenlyDiscretizedFunc[]> allMagsList = Lists.newArrayList();
		List<EvenlyDiscretizedFunc[]> smallMagsList = Lists.newArrayList();
		
		for (int i=0; i<avgBools.size(); i++) {
			boolean[] bools = avgBools.get(i);
			erf.testSetBPT_CalcType(bools[0], bools[1]);
			// do this to make sure it gets updated
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
			
			regFuncsList.add(calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag));
			allMagsList.add(calcSubSectSupraSeisMagProbDists(erf, 0d, numMag, deltaMag));
			smallMagsList.add(calcSubSectSupraSeisMagProbDists(erf, 0d, numMag, deltaMag, 7d));
		}
		
		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT().rescale(0.8d, 1.2d);
		
		FaultSystemSolution sol = erf.getSolution();
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		double duration = erf.getTimeSpan().getDuration();
		
		prefix += (float)duration+"yr";
		
		String refLabel = getBPTCalcTypeStr(avgBools.get(refIndex));
		
		for (int i=0; i<numMag+2; i++) {
			for (int j=0; j<avgBools.size(); j++) {
				if (j == refIndex)
					continue;
				String testLabel = getBPTCalcTypeStr(avgBools.get(j));
				double[] testVals;
				double[] refVals;
				String myPrefix;
				String magStr;
				if (i == numMag) {
					testVals = extractYVals(allMagsList.get(j), 0);
					refVals = extractYVals(allMagsList.get(refIndex), 0);
					myPrefix = prefix+"_supra_seis";
					magStr = "Supra Seis";
				} else if (i == numMag+1) {
					testVals = extractYVals(smallMagsList.get(j), 0);
					refVals = extractYVals(smallMagsList.get(refIndex), 0);
					myPrefix = prefix+"_below_7";
					magStr = "All M<=7";
				} else {
					testVals = extractYVals(regFuncsList.get(j), 0);
					refVals = extractYVals(regFuncsList.get(refIndex), 0);
					double mag = regFuncsList.get(j)[0].getX(i);
					myPrefix = prefix+"_"+(float)mag+"+";
					magStr = "M>="+(float)mag;
				}
				
				double[] ratioVals = new double[testVals.length];
				for (int k=0; k<ratioVals.length; k++)
					ratioVals[k] = testVals[k]/refVals[k];
				
				String fName = myPrefix+"_"+testLabel.replaceAll("\\W+", "_")+"_vs_"+refLabel.replaceAll("\\W+", "_");
				
				// prob gain
				FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, ratioVals, region,
						saveDir, fName, false, true,
						(float)duration+"yr "+magStr+testLabel+"/"+refLabel);
			}
		}
	}
	
	private static double[] extractYVals(EvenlyDiscretizedFunc[] funcs, int index) {
		double[] vals = new double[funcs.length];
		for (int i=0; i<funcs.length; i++)
			vals[i] = funcs[i].getY(index);
		return vals;
	}
	
	private static void populateMagDurationXYZ(FaultSystemSolutionERF erf, EvenlyDiscrXYZ_DataSet[] xyzs,
			Region[] regions, FSSRupsInRegionCache cache) {
		double minMag = xyzs[0].getMinY();
		int numMag = xyzs[0].getNumY();
		double deltaMag = xyzs[0].getGridSpacingY();
		for (int x=0; x<xyzs[0].getNumX(); x++) {
			// x is duration
			double duration = xyzs[0].getX(x);
			erf.getTimeSpan().setDuration(duration);
			erf.updateForecast();
			
			for (int i=0; i<xyzs.length; i++) {
				EvenlyDiscretizedFunc func = calcMagProbDist(erf, regions[i], minMag, numMag, deltaMag, true, cache);
				for (int y=0; y<numMag; y++)
					xyzs[i].set(x, y, func.getY(y));
			}
		}
	}
	
	/**
	 * This makes XYZ plots of probability as a function of mag/duration for various california regions
	 * with both Poisson and BPT probabilities (and prob gains).
	 * @param erf
	 * @param saveDir
	 * @param prefix
	 * @throws IOException
	 */
	public static void makeMagDurationProbPlots(FaultSystemSolutionERF erf, File saveDir, String prefix)
			throws IOException {
		double minMag = 6.5;
		int numMag = 21;
		double deltaMag = 0.1;
		
		double minDuration = 5d;
		double deltaDuration = 5d;
		int numDuration = 20;
		
		Region[] regions = { new CaliforniaRegions.RELM_COLLECTION(), new CaliforniaRegions.RELM_SOCAL(),
				new CaliforniaRegions.RELM_NOCAL(), new CaliforniaRegions.LA_BOX(), new CaliforniaRegions.SF_BOX() };
		String[] regNames = { "Statewide", "So Cal", "No Cal", "LA", "SF" };
		
		EvenlyDiscrXYZ_DataSet[] poissonXYZs = new EvenlyDiscrXYZ_DataSet[regions.length];
		EvenlyDiscrXYZ_DataSet[] bptXYZs = new EvenlyDiscrXYZ_DataSet[regions.length];
		
		for (int r=0; r<regions.length; r++) {
			poissonXYZs[r] = new EvenlyDiscrXYZ_DataSet(
					numDuration, numMag, minDuration, minMag, deltaDuration, deltaMag);
			bptXYZs[r] = new EvenlyDiscrXYZ_DataSet(
					numDuration, numMag, minDuration, minMag, deltaDuration, deltaMag);
		}
		
		FSSRupsInRegionCache cache = new FSSRupsInRegionCache(erf);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		
		populateMagDurationXYZ(erf, poissonXYZs, regions, cache);
		
		// TODO historical open interval?
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		
		populateMagDurationXYZ(erf, bptXYZs, regions, cache);
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
		CPT ratioCPT = (CPT)FaultBasedMapGen.getLinearRatioCPT().clone();
		ratioCPT.setNanColor(Color.WHITE);
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		for (int r=0; r<regions.length; r++) {
			EvenlyDiscrXYZ_DataSet ratioXYZ = new EvenlyDiscrXYZ_DataSet(
					numDuration, numMag, minDuration, minMag, deltaDuration, deltaMag);
			for (int i=0; i<ratioXYZ.size(); i++)
				ratioXYZ.set(i, bptXYZs[r].get(i)/poissonXYZs[r].get(i));
			
			String name = regNames[r];
			String fName = prefix+name.toLowerCase().replaceAll("\\W+", "_");
			
			writeMagProbXYZ(cpt, poissonXYZs[r], name+" Poisson Prob", "Probability", saveDir, fName+"_poisson");
			writeMagProbXYZ(cpt, bptXYZs[r], name+" BPT Prob", "Probability", saveDir, fName+"_bpt");
			writeMagProbXYZ(ratioCPT, ratioXYZ, name+" BPT/Poisson Prob Gain", "Prob Gain", saveDir, fName+"_prob_gain");
		}
	}
	
	private static void writeMagProbXYZ(CPT cpt, EvenlyDiscrXYZ_DataSet xyz, String title, String zLabel, File saveDir, String prefix)
			throws IOException {
		XYZPlotSpec spec = new XYZPlotSpec(xyz, cpt, title, "Duration (years)", "Min Mag", zLabel);
		
		Range xRange = new Range(xyz.getMinX()-0.5*xyz.getGridSpacingX(), xyz.getMaxX()+0.5*xyz.getGridSpacingX());
		Range yRange = new Range(xyz.getMinY()-0.5*xyz.getGridSpacingY(), xyz.getMaxY()+0.5*xyz.getGridSpacingY());
		
		XYZGraphPanel gp = new XYZGraphPanel();
		gp.drawPlot(spec, false, false, xRange, yRange);
		
		File file = new File(saveDir, prefix);
		
		gp.getChartPanel().setSize(1000, 1000);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		
		// now write XYZ
		EvenlyDiscrXYZ_DataSet.writeXYZFile(xyz, file.getAbsolutePath()+"_xyz.txt");
	}
	
	/**
	 * This writes out a CSV file of varios probabilities/rates for each sub section.
	 * @param erf
	 * @param outputFile
	 * @throws IOException
	 */
	public static void writeSubSectionTimeDependenceCSV(FaultSystemSolutionERF erf, File outputFile)
			throws IOException {
		writeTimeDependenceCSV(erf, outputFile, false);
	}
	
	private static Map<Integer, EvenlyDiscretizedFunc> remap(EvenlyDiscretizedFunc[] funcs) {
		Map<Integer, EvenlyDiscretizedFunc> map = Maps.newHashMap();
		for (int i=0; i<funcs.length; i++)
			map.put(i, funcs[i]);
		return map;
	}
	
	private static void writeTimeDependenceCSV(FaultSystemSolutionERF erf, File outputFile, boolean parent)
			throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		double minMag = 6.5;
		int numMag = 4;
		double deltaMag = 0.5;
		
		FaultSystemSolution sol = erf.getSolution();
		
		double allMagMin = 5d;
		int numAllMag = 3+numMag;
		double allMagMax = allMagMin + deltaMag*(numAllMag-1);
		String[] magRangeStrs = { "M>=6.5", "M>=7.0", "M>=7.5", "M>=8.0", "Supra Seis", "M<=7.0" };
		
		// header
		List<String> header = Lists.newArrayList("Section Name");
		if (parent)
			header.add("Parent Section ID");
		else
			header.add("Sub Section ID");
		for (String rangeStr : magRangeStrs) {
			header.add(rangeStr);
			// these are just averaged (unweighted) since area approx the same for subsections of a parent
			header.add("Recurr Int.");
			if (parent)
				header.add("Open Int. Where Known");
			else
				header.add("Open Int.");
			header.add("Ppois");
			header.add("Pbpt");
			header.add("Prob Gain");
			header.add("Sect Impl Gain");
		}
		csv.addLine(header);
		
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.updateForecast();
		
		Map<Integer, EvenlyDiscretizedFunc> poissonFuncs, poissonAllMags, poissonSmallMags;
		if (parent) {
			poissonFuncs = calcParentSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
			poissonAllMags = calcParentSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
			poissonSmallMags = calcParentSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d);
		} else {
			poissonFuncs = remap(calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag));
			poissonAllMags = remap(calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag));
			poissonSmallMags = remap(calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d));
		}
		
		// TODO historical open interval?
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.updateForecast();

		Map<Integer, EvenlyDiscretizedFunc> bptFuncs, bptAllMags, bptSmallMags;
		if (parent) {
			bptFuncs = calcParentSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
			bptAllMags = calcParentSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
			bptSmallMags = calcParentSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d);
		} else {
			bptFuncs = remap(calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag));
			bptAllMags = remap(calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag));
			bptSmallMags = remap(calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d));
		}
		
		// parent names and mapping to IDs
		HashSet<String> namesSet = new HashSet<String>();
		Map<String, Integer> nameIDMap = Maps.newHashMap();
		Map<Integer, List<Long>> parentLastEventMap = Maps.newHashMap();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList()) {
			if (parent) {
				int parentID = sect.getParentSectionId();
				String parentName = sect.getParentSectionName();
				if (!namesSet.contains(parentName)) {
					namesSet.add(parentName);
					nameIDMap.put(parentName, parentID);
					parentLastEventMap.put(parentID, new ArrayList<Long>());
				}
				if (sect.getDateOfLastEvent() > Long.MIN_VALUE)
					parentLastEventMap.get(parentID).add(sect.getDateOfLastEvent());
			} else {
				int sectID = sect.getSectionId();
				String name = sect.getSectionName();
				namesSet.add(name);
				nameIDMap.put(name, sectID);
			}
		}
		List<String> sectNames = Lists.newArrayList(namesSet);
		Collections.sort(sectNames);
		
		long curMillis = System.currentTimeMillis();
		
		ProbabilityModelsCalc calc = new ProbabilityModelsCalc(
				((BPT_AperiodicityParam)erf.getParameter(BPT_AperiodicityParam.NAME)).getValue());
		
		double duration = erf.getTimeSpan().getDuration();
		
		for (String sectName : sectNames) {
			List<String> line = Lists.newArrayList();
			
			Integer sectID = nameIDMap.get(sectName);
			
			line.add(sectName);
			line.add(sectID+"");
			
			double oi;
			List<Long> lastDates = parentLastEventMap.get(sectID);
			if (!parent) {
				lastDates = Lists.newArrayList();
				long last = sol.getRupSet().getFaultSectionData(sectID).getDateOfLastEvent();
				if (last > Long.MIN_VALUE)
					lastDates.add(last);
			}
			if (lastDates.isEmpty()) {
				oi = Double.NaN;
			} else {
				oi = 0d;
				for (long lastDate : lastDates) {
					long deltaMillis = curMillis - lastDate;
					double diffYears = YEARS_PER_MILLI*deltaMillis;
					oi += diffYears;
				}
				oi /= lastDates.size();
			}
			
			for (int i=0; i<numMag+2; i++) {
				line.add("");
				
				IncrementalMagFreqDist mfd;
				if (parent)
					mfd = sol.calcParticipationMFD_forParentSect(
						sectID, allMagMin+0.5*deltaMag, allMagMax+0.5*deltaMag, numAllMag);
				else
					mfd = sol.calcParticipationMFD_forSect(
							sectID, allMagMin+0.5*deltaMag, allMagMax+0.5*deltaMag, numAllMag);
				EvenlyDiscretizedFunc cmlMFD = mfd.getCumRateDistWithOffset();
				
				double poissonProb;
				double bptProb;
				double ri;
				if (i == numMag) {
					poissonProb = poissonAllMags.get(sectID).getY(0);
					bptProb = bptAllMags.get(sectID).getY(0);
					ri = 1d/cmlMFD.getY(0);
				} else if (i == numMag+1) {
					poissonProb = poissonSmallMags.get(sectID).getY(0);
					bptProb = bptSmallMags.get(sectID).getY(0);
					double smallRate = 0d;
					for (Point2D pt : mfd) {
						if (pt.getX()>7d)
							break;
						smallRate += pt.getY();
					}
					ri = 1d/smallRate;
				} else {
					poissonProb = poissonFuncs.get(sectID).getY(i);
					bptProb = bptFuncs.get(sectID).getY(i);
					ri = 1d/cmlMFD.getY(i+(numAllMag - numMag));
				}
				
				line.add(ri+"");
				line.add(oi+"");
				line.add(poissonProb+"");
				line.add(bptProb+"");
				line.add(bptProb/poissonProb+"");
				double implBPTProb = calc.computeBPT_ProbFast(ri, oi, duration);
				double implPoissonProb = ProbabilityModelsCalc.computePoissonProb(ri, duration);
				line.add(implBPTProb/implPoissonProb+"");
			}
			csv.addLine(line);
		}
		
		csv.writeToFile(outputFile);
	}
	
	/**
	 * This writes out a CSV file of varios probabilities/rates aggregated over each parent section. Parent section
	 * RI's are calculated as 1 over total parent section partiticpation rate. Open intervals are averaged among all
	 * fault sections where known, or NaN if no sections of a parent have a date of last event.
	 * @param erf
	 * @param outputFile
	 * @throws IOException
	 */
	public static void writeParentSectionTimeDependenceCSV(FaultSystemSolutionERF erf, File outputFile)
			throws IOException {
		writeTimeDependenceCSV(erf, outputFile, true);
	}
	
	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws RuntimeException 
	 * @throws GMT_MapException 
	 */
	public static void main(String[] args) throws IOException, DocumentException, GMT_MapException, RuntimeException {
		
		scratch.UCERF3.utils.RELM_RegionUtils.printNumberOfGridNodes();
		
//		plot_U3pt3_U2_TotalMeanMFDs();
		
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
		
		
		
		FaultSystemSolution meanSol = FaultSystemIO.loadSol(
				new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
						"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		erf.getTimeSpan().setDuration(30d);
		
		// this will generate the prob gain mains for BPT/Poisson
//		File saveDir = new File("/tmp/prob_maps");
//		if (!saveDir.exists())
//			saveDir.mkdir();
//		makeFaultProbGainMaps(meanSol, saveDir, "ucerf3_ca", false, 30d);
		
		// this will write the mag/duration/prob XYZ files for various CA regions
//		File saveDir = new File("/tmp/prob_xyzs");
//		if (!saveDir.exists())
//			saveDir.mkdir();
//		makeMagDurationProbPlots(meanSol, saveDir, "ucerf3", false);
		
		// this will write the parent section CSV file
		writeParentSectionTimeDependenceCSV(erf, new File("/tmp/erf_time_dependence_parent_probs_30yr.csv"));
		writeSubSectionTimeDependenceCSV(erf, new File("/tmp/erf_time_dependence_sub_probs_30yr.csv"));

		// this will generate prob gain maps for BPT parameters
		File saveDir = new File("/tmp/bpt_calc_prob_gains");
		if (!saveDir.exists())
			saveDir.mkdir();
		makeAvgMethodProbGainMaps(erf, saveDir, "bpt_param");
		
//		double minMag = 6.5;
//		double deltaMag = 0.1;
//		int numMag = 21;
//		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
//		erf.getTimeSpan().setDuration(30d);
//		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
//		erf.updateForecast();
//		FSSRupsInRegionCache cache = new FSSRupsInRegionCache(erf);
//		Region region = new CaliforniaRegions.RELM_SOCAL();
//		// preload the cache so that timing comparisons are fair
//		System.out.println("Preloading cache");
//		Stopwatch watch = new Stopwatch();
//		watch.start();
//		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
//			ProbEqkSource source = erf.getSource(sourceID);
//			cache.isRupInRegion(source, source.getRupture(0), sourceID, 0, region);
//		}
//		watch.stop();
//		System.out.println("Done. Took "+(watch.elapsed(TimeUnit.MILLISECONDS)/1000f)+"s");
//		watch.reset();
//		watch.start();
//		System.out.println("Calculating indep MFD");
//		EvenlyDiscretizedFunc indepProbsMFD = calcMagProbDist(erf, region, minMag, numMag, deltaMag, true, cache);
//		watch.stop();
//		System.out.println("Took "+(watch.elapsed(TimeUnit.MILLISECONDS)/1000f)+"s");
//		watch.reset();
//		watch.start();
//		System.out.println("Calculating indep Sum");
//		EvenlyDiscretizedFunc indepProbsSum = calcMagProbDist(erf, region, minMag, numMag, deltaMag, false, cache);
//		watch.stop();
//		System.out.println("Took "+(watch.elapsed(TimeUnit.MILLISECONDS)/1000f)+"s");
//		watch.reset();
//		watch.start();
//		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
//		erf.updateForecast();
//		System.out.println("Calculating dep MFD");
//		EvenlyDiscretizedFunc depProbsMFD = calcMagProbDist(erf, region, minMag, numMag, deltaMag, true, cache);
//		watch.stop();
//		System.out.println("Took "+(watch.elapsed(TimeUnit.MILLISECONDS)/1000f)+"s");
//		watch.reset();
//		watch.start();
//		System.out.println("Calculating dep Sum");
//		EvenlyDiscretizedFunc depProbsSum = calcMagProbDist(erf, region, minMag, numMag, deltaMag, false, cache);
//		watch.stop();
//		System.out.println("Took "+(watch.elapsed(TimeUnit.MILLISECONDS)/1000f)+"s");
//		watch.reset();
//		watch.start();
//		List<DiscretizedFunc> funcs = Lists.newArrayList();
//		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
//		funcs.add(indepProbsMFD);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
//		funcs.add(indepProbsSum);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.GRAY));
//		funcs.add(depProbsMFD);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
//		funcs.add(depProbsSum);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.CYAN));
//		
//		new GraphWindow(funcs, "30 Year So Cal Probabilities", chars);
	}

}
