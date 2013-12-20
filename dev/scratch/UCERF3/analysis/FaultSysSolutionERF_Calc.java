package scratch.UCERF3.analysis;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dom4j.DocumentException;
import org.jfree.data.Range;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.NamedComparator;
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
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
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
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.LastEventData;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;
import scratch.UCERF3.utils.UCERF2_Section_MFDs.UCERF2_Section_MFDsCalc;
import scratch.UCERF3.utils.UCERF2_Section_MFDs.UCERF2_Section_TimeDepMFDsCalc;

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
	
	private static void testProbSumMethods() throws IOException, DocumentException {
		FaultSystemSolution meanSol = FaultSystemIO.loadSol(
				new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
						"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		
		double duration = 30d;
		String durStr = (int)duration+"yr";
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		erf.getTimeSpan().setDuration(duration);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		Region region = new CaliforniaRegions.LA_BOX();
		double minMag = 5d;
		int numMag = 40;
		double deltaMag = 0.1;
		FSSRupsInRegionCache cache = new FSSRupsInRegionCache(erf);
		EvenlyDiscretizedFunc mfdVals = calcMagProbDist(erf, region, minMag, numMag, deltaMag, true, cache);
		EvenlyDiscretizedFunc sumVals = calcMagProbDist(erf, region, minMag, numMag, deltaMag, true, cache);
		
		for (int i=0; i<numMag; i++) {
			double mfdY = mfdVals.getY(i);
			double sumY = sumVals.getY(i);
			double pDiff = DataUtils.getPercentDiff(sumY, mfdY);
			Preconditions.checkState(pDiff < 0.01, "Fails within 0.01%: mfdY="+mfdY+", sumY="+sumY+", pDiff="+pDiff+"%");
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
			double totProb = calcSummedProbs(probs);
			result.set(i, totProb);
//			System.out.println("\tM "+result.getX(i)+"+ Prob: "+(float)(totProb*100d)+" %");
		}
	}
	
	static double calcSummedProbs(List<Double> probs) {
		double totOneMinus = 1;
		for (double prob : probs) {
			totOneMinus *= (1-prob);
		}
		double totProb = 1 - totOneMinus;
		
		return totProb;
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
		double minMag = 6.7;
		int numMag = 4;
		double deltaMag = 0.5;
		
		double duration = erf.getTimeSpan().getDuration();
		
		FaultSystemSolution sol = erf.getSolution();
		
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.getTimeSpan().setDuration(duration);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] poissonFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] poissonAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
		EvenlyDiscretizedFunc[] poissonSmallMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d);
		
		// TODO historical open interval?
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(duration);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] bptFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] bptAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
		EvenlyDiscretizedFunc[] bptSmallMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag, 7d);
		
		// log space
		CPT probCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-4, 0);
//		CPT ratioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-0.5, 0.5);
//		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT();
		CPT ratioCPT = getScaledLinearRatioCPT(0.02);
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		prefix += (int)duration+"yr";
		
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
//		ProbabilityModelsCalc calc = new ProbabilityModelsCalc(
//				((BPT_AperiodicityParam)erf.getParameter(BPT_AperiodicityParam.NAME)).getValue());
		ProbabilityModelsCalc calc = new ProbabilityModelsCalc(erf);
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
				double bptProb = calc.computeBPT_ProbFast(ri, diffYears, duration, 0d);
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
	
	
	
	
	
	/**
	 * This generates various state-wide fault probability maps for the WG02 approach.
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public static void makeWG02_FaultProbMaps() throws GMT_MapException, RuntimeException, IOException {
		double minMag = 6.7;
		int numMag = 4;
		double deltaMag = 0.5;
		
		String prefix = "aper0pt3";
		String dirName = "WG02_tests_Aper0pt3";
		File saveDir = new File(dirName);
		if (!saveDir.exists())
			saveDir.mkdir();
		
		
		FaultSystemSolution meanSol=null;
		try {
			meanSol = FaultSystemIO.loadSol(
					new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
							"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double duration = 30d;
		String durStr = (int)duration+"yr";
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		erf.getTimeSpan().setDuration(duration);

		
		FaultSystemSolution sol = erf.getSolution();
		
		// Poisson values
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] poissonFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] poissonAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
		
		// U3 Values for const aper=0.3
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.setParameter(MagDependentAperiodicityParam.NAME, MagDependentAperiodicityOptions.ALL_PT3_VALUES);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] bptFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] bptAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
	
		// WG02 Values for const aper=0.3
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.WG02_BPT);
		erf.setParameter(MagDependentAperiodicityParam.NAME, MagDependentAperiodicityOptions.ALL_PT3_VALUES);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] wg02_Funcs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] wg02_AllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);

		// log space
		CPT probCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-4, 0);
//		CPT ratioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-0.5, 0.5);
//		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT();
		CPT ratioCPT = getScaledLinearRatioCPT(0.02);
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		prefix += (int)duration+"yr";
		
		for (int i=0; i<numMag+1; i++) {
			
			double[] poissonVals;
			double[] bptVals;
			double[] wg02_Vals;
			String myPrefix;
			String magStr;
			if (i == numMag) {
				poissonVals = extractYVals(poissonAllMags, 0);
				bptVals = extractYVals(bptAllMags, 0);
				wg02_Vals =  extractYVals(wg02_AllMags, 0);
				myPrefix = prefix+"_supra_seis";
				magStr = "Supra Seis";
			} else {
				poissonVals = extractYVals(poissonFuncs, i);
				bptVals = extractYVals(bptFuncs, i);
				wg02_Vals =  extractYVals(wg02_Funcs, 0);
				double mag = poissonFuncs[0].getX(i);
				myPrefix = prefix+"_"+(float)mag+"+";
				magStr = "M>="+(float)mag;
			}
			
			double[] wg02overPoisVavs = new double[poissonVals.length];
			double[] U3overWG02_Vavs = new double[poissonVals.length];
			for (int j=0; j<wg02overPoisVavs.length; j++) {
				wg02overPoisVavs[j] = wg02_Vals[j]/poissonVals[j];
				U3overWG02_Vavs[j] = bptVals[j]/wg02_Vals[j];
			}
			
			// poisson probs
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(poissonVals), region,
					saveDir, myPrefix+"_poisson", false, true,
					"Log10("+(float)duration+" yr "+magStr+" Poisson Prob)");
			// bpt probs
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(bptVals), region,
					saveDir, myPrefix+"_U3", false, true,
					"Log10("+(float)duration+" yr "+magStr+" U3 Prob)");
			// bpt probs
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(wg02_Vals), region,
					saveDir, myPrefix+"_WG02", false, true,
					"Log10("+(float)duration+" yr "+magStr+" WG02 Prob)");
			// prob gain
			FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, wg02overPoisVavs, region,
					saveDir, myPrefix+"_WG02_prob_gain", false, true,
					(float)duration+" yr "+magStr+" WG02 Prob Gain");
			// prob gain
			FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, U3overWG02_Vavs, region,
					saveDir, myPrefix+"_U2overWG02_ratio", false, true,
					(float)duration+" yr "+magStr+" U3 over WG02 Prob Ratio");
		}
		
	}
	
	private static boolean[] toArray(boolean... vals) {
		return vals;
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
		
		// TRUE: RI, NTS
		// FALSE: Rate, TS
		List<BPTAveragingTypeOptions> avgTypes = Lists.newArrayList();
		int refIndex = 1;
//		avgBools.add(toArray(false, false));
		avgTypes.add(BPTAveragingTypeOptions.AVE_RATE_AVE_NORM_TIME_SINCE);
		avgTypes.add(BPTAveragingTypeOptions.AVE_RI_AVE_TIME_SINCE); // reference
		avgTypes.add(BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE);
		
		
		makeAvgMethodProbGainMaps(erf, saveDir, prefix, avgTypes, refIndex);
		
		avgTypes = Lists.newArrayList();
		refIndex = 0;
		avgTypes.add(BPTAveragingTypeOptions.AVE_RATE_AVE_NORM_TIME_SINCE); // reference
		avgTypes.add(BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE);
		
		makeAvgMethodProbGainMaps(erf, saveDir, prefix, avgTypes, refIndex);
	}
	
	private static void makeAvgMethodProbGainMaps(FaultSystemSolutionERF erf, File saveDir, String prefix,
			List<BPTAveragingTypeOptions> avgTypes, int refIndex) throws GMT_MapException, RuntimeException, IOException {
		double minMag = 6.7;
		int numMag = 4;
		double deltaMag = 0.5;
		double duration = erf.getTimeSpan().getDuration();
		System.out.println("Duration: "+duration);
		
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(duration);
		erf.updateForecast();
		
		List<EvenlyDiscretizedFunc[]> regFuncsList = Lists.newArrayList();
		List<EvenlyDiscretizedFunc[]> allMagsList = Lists.newArrayList();
		List<EvenlyDiscretizedFunc[]> smallMagsList = Lists.newArrayList();
		
		for (int i=0; i<avgTypes.size(); i++) {
			BPTAveragingTypeOptions avgType = avgTypes.get(i);
			erf.setParameter(BPTAveragingTypeParam.NAME, avgType);
			// do this to make sure it gets updated
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
			erf.getTimeSpan().setDuration(duration);
			
			regFuncsList.add(calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag));
			allMagsList.add(calcSubSectSupraSeisMagProbDists(erf, 0d, numMag, deltaMag));
			smallMagsList.add(calcSubSectSupraSeisMagProbDists(erf, 0d, numMag, deltaMag, 7d));
		}
		
//		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT().rescale(0.8d, 1.2d);
//		CPT ratioCPT = getScaledLinearRatioCPT(0.02, 0.8d, 1.2d);
		CPT ratioCPT = getScaledLinearRatioCPT(0.02);
		
		FaultSystemSolution sol = erf.getSolution();
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		prefix += (float)duration+"yr";
		
		String refLabel = avgTypes.get(refIndex).getCompactLabel();
		
		for (int i=0; i<numMag+2; i++) {
			for (int j=0; j<avgTypes.size(); j++) {
				if (j == refIndex)
					continue;
				String testLabel = avgTypes.get(j).getCompactLabel();
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
		double minMag = 6.7;
		int numMag = 4;
		double deltaMag = 0.5;
		
		FaultSystemSolution sol = erf.getSolution();
		double duration = erf.getTimeSpan().getDuration();
		
		double allMagMin = 5.2d;
		int numAllMag = 3+numMag;
		double allMagMax = allMagMin + deltaMag*(numAllMag-1);
		String[] magRangeStrs = { "M>=6.7", "M>=7.2", "M>=7.7", "M>=8.2", "Supra Seis", "M<=7.0" };
		
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
		erf.getTimeSpan().setDuration(duration);
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
		erf.getTimeSpan().setDuration(duration);
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
		
//		ProbabilityModelsCalc calc = new ProbabilityModelsCalc(
//				((BPT_AperiodicityParam)erf.getParameter(BPT_AperiodicityParam.NAME)).getValue());
		ProbabilityModelsCalc calc = new ProbabilityModelsCalc(erf);
		
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
				double implCompareMag;
				if (i == numMag) {
					poissonProb = poissonAllMags.get(sectID).getY(0);
					bptProb = bptAllMags.get(sectID).getY(0);
					ri = 1d/cmlMFD.getY(0);
					implCompareMag = 0;
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
					implCompareMag = 0;
				} else {
					poissonProb = poissonFuncs.get(sectID).getY(i);
					bptProb = bptFuncs.get(sectID).getY(i);
					implCompareMag = poissonFuncs.get(sectID).getX(i)+0.05; // make sure to get above
					ri = 1d/cmlMFD.getY(i+(numAllMag - numMag));
				}
				
				line.add(ri+"");
				line.add(oi+"");
				line.add(poissonProb+"");
				line.add(bptProb+"");
				line.add(bptProb/poissonProb+"");
				double implBPTProb = calc.computeBPT_ProbFast(ri, oi, duration, implCompareMag);
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
	 * This writes out the rupture gains for the different averaging methods, and for the hard-coded erf.
	 */
	public static void writeDiffAveragingMethodsRupProbGains() {

		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(fileName);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(30d);

		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.LOW_VALUES);
		erf.updateForecast();
		ProbabilityModelsCalc testCalc = new ProbabilityModelsCalc(erf);
		testCalc.writeRupProbGainsForDiffAveragingMethods(erf.getTimeSpan().getStartTimeInMillis(), 
				erf.getTimeSpan().getDuration(), "RupProbGainsForDiffAveMethods30yrs_LowApers.txt");
		
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
		erf.updateForecast();
		testCalc = new ProbabilityModelsCalc(erf);
		testCalc.writeRupProbGainsForDiffAveragingMethods(erf.getTimeSpan().getStartTimeInMillis(), 
				erf.getTimeSpan().getDuration(), "RupProbGainsForDiffAveMethods30yrs_MidApers.txt");
		
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.HIGH_VALUES);
		erf.updateForecast();
		testCalc = new ProbabilityModelsCalc(erf);
		testCalc.writeRupProbGainsForDiffAveragingMethods(erf.getTimeSpan().getStartTimeInMillis(), 
				erf.getTimeSpan().getDuration(), "RupProbGainsForDiffAveMethods30yrs_HighApers.txt");
	}
	
	
	
	
	/**
	 * This writes out the rupture gains for the different averaging methods, 
	 * for rups that utilize the given section, and for the hard-coded erf.
	 */
	public static void writeDiffAveragingMethodsRupProbGains(int subSectIndex) {
		
		 
		String erfFileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(erfFileName);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(30d);
		
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.LOW_VALUES);
		erf.updateForecast();
		String fileName = "sect"+subSectIndex+"_RupProbGainsForDiffAveMethods30yrs_LowApers.txt";
		ProbabilityModelsCalc testCalc = new ProbabilityModelsCalc(erf);
		testCalc.writeRupProbGainsForDiffAveragingMethods(erf.getTimeSpan().getStartTimeInMillis(), 
				erf.getTimeSpan().getDuration(), fileName,subSectIndex);
		
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
		erf.updateForecast();
		fileName = "sect"+subSectIndex+"_RupProbGainsForDiffAveMethods30yrs_MidApers.txt";
		testCalc = new ProbabilityModelsCalc(erf);
		testCalc.writeRupProbGainsForDiffAveragingMethods(erf.getTimeSpan().getStartTimeInMillis(), 
				erf.getTimeSpan().getDuration(), fileName,subSectIndex);
		
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.HIGH_VALUES);
		erf.updateForecast();
		fileName = "sect"+subSectIndex+"_RupProbGainsForDiffAveMethods30yrs_HighApers.txt";
		testCalc = new ProbabilityModelsCalc(erf);
		testCalc.writeRupProbGainsForDiffAveragingMethods(erf.getTimeSpan().getStartTimeInMillis(), 
				erf.getTimeSpan().getDuration(), fileName,subSectIndex);
	}

	
	private static CPT getScaledLinearRatioCPT(double fractToWashOut) throws IOException {
		return getScaledLinearRatioCPT(fractToWashOut, 0d, 2d);
	}
	
	private static CPT getScaledLinearRatioCPT(double fractToWashOut, double min, double max) throws IOException {
		Preconditions.checkArgument(fractToWashOut >= 0 && fractToWashOut < 1);
		CPT ratioCPT = GMT_CPT_Files.UCERF3_HAZ_RATIO_P3.instance().rescale(min, max);
		CPT belowCPT = new CPT();
		CPT afterCPT = new CPT();
		for (CPTVal val : ratioCPT) {
			if (val.end < 1d)
				belowCPT.add(val);
			else if (val.start > 1d)
				afterCPT.add(val);
		}
		belowCPT = belowCPT.rescale(min, 1d-fractToWashOut);
		afterCPT = afterCPT.rescale(1d+fractToWashOut, max);
		CPT combCPT = (CPT) ratioCPT.clone();
		combCPT.clear();
		combCPT.addAll(belowCPT);
		if (fractToWashOut > 0) {
			Color washColor = combCPT.get(combCPT.size()-1).maxColor;
			combCPT.add(new CPTVal(belowCPT.getMaxValue(), washColor, afterCPT.getMinValue(), washColor));
		}
		combCPT.addAll(afterCPT);
		return combCPT;
	}
	
	
	public static void writeDiffAveragingMethodsSubSectionTimeDependenceCSV(File outputDir) throws IOException {
		FaultSystemSolution meanSol;
		try {
			meanSol = FaultSystemIO.loadSol(new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
						"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}				
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		double duration = 30d;
		erf.getTimeSpan().setDuration(duration);
		String durStr = (int)duration+"yr";
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
		
		MagDependentAperiodicityOptions[] covFuncs = { MagDependentAperiodicityOptions.LOW_VALUES,
				MagDependentAperiodicityOptions.MID_VALUES, MagDependentAperiodicityOptions.HIGH_VALUES };

		for (MagDependentAperiodicityOptions cov : covFuncs) {
			erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(cov);
			
			List<BPTAveragingTypeOptions> avgTypesList = Lists.newArrayList();
			List<CSVFile<String>> csvFiles = Lists.newArrayList();
			
			for (BPTAveragingTypeOptions aveType : BPTAveragingTypeOptions.values()) {
				erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
				String calcType = aveType.getFileSafeLabel();
				System.out.println("working on "+calcType);
				File csvFile = new File(outputDir, "SubSectProbData_"+durStr+"_"+cov.name()+"_COVs_"+calcType+".csv");
				writeSubSectionTimeDependenceCSV(erf, csvFile);
				
				// keep track of settings and parse the CSV file
				avgTypesList.add(aveType);
				csvFiles.add(CSVFile.readFile(csvFile, true));	
			}
			
			// now stitch into a master file for this COV func
			// mag ranges to do
			double[] minMags = { 0, 6.7 };
			// start col ("Recurr Int.") for that mag range
			int[] startCols = { 31, 3 };
			for (int i=0; i<minMags.length; i++) {
				double minMag = minMags[i];
				int startCol = startCols[i];
				
				// use this for common to all columns
				CSVFile<String> refCSV = csvFiles.get(0);
				
				CSVFile<String> csv = new CSVFile<String>(true);
				// add first two columns (name and ID
				csv.addColumn(refCSV.getColumn(0));
				csv.addColumn(refCSV.getColumn(1));
				
				// now add common to all values
				csv.addColumn(refCSV.getColumn(startCol)); // recurr int
				csv.addColumn(refCSV.getColumn(startCol+1)); // open int
				csv.addColumn(refCSV.getColumn(startCol+2)); // Ppois
				csv.addColumn(refCSV.getColumn(startCol+5)); // Sect Impl Gain
				
				// now for each calc setting
				for (int j=0; j<avgTypesList.size(); j++) {
					BPTAveragingTypeOptions avgTypes = avgTypesList.get(j);
					CSVFile<String> calcCSV = csvFiles.get(j);
					
					// now add blank column except for header which states settings
					List<String> headerCol = Lists.newArrayList();
					String calcType = avgTypes.getCompactLabel();
					headerCol.add(calcType);
					while (headerCol.size() < refCSV.getNumRows())
						headerCol.add("");
					csv.addColumn(headerCol);
					
					// now add unique data columns
					csv.addColumn(calcCSV.getColumn(startCol+3)); // Pbpt
					csv.addColumn(calcCSV.getColumn(startCol+4)); // Prob Gain
				}
				
				// write out combined CSV
				String magStr;
				if (minMag < 5)
					magStr = "supra_seis";
				else
					magStr = (float)minMag+"+";
				File csvFile = new File(outputDir, "SubSectProbData_"+durStr+"_"+cov.name()+"_COVs_"+magStr+"_combined.csv");
				csv.writeToFile(csvFile);
			}
		}
	}
	
	/**
	 * 
	 * @param zipFile
	 * @param outputDir
	 * @param parents
	 * @param magRangeIndex
	 * @param minMag
	 * @param duration
	 * @param cov
	 * @return map from parent section IDs to mean probs list (1 entry for parents, multiple for subs) 
	 * @throws ZipException
	 * @throws IOException
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 */
	public static Map<Integer, List<Double>> writeBranchAggregatedTimeDepFigs(File zipFile, File outputDir,
			boolean parents, int magRangeIndex, double minMag, double duration)
					throws ZipException, IOException, GMT_MapException, RuntimeException {
//		HashSet<MagDependentAperiodicityOptions> covsToInclude;
//		if (cov == null) {
//			// all
//			covsToInclude = null;
//		} else {
//			// just one
//			covsToInclude = new HashSet<MagDependentAperiodicityOptions>();
//			covsToInclude.add(cov);
//		}
//		
//		int magRangeIndex = 0; // 6.7+
		
		Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
				Map<LogicTreeBranch, SectProbGainResults[]>> table = loadBranchCSVVals(
				zipFile, new int[] {magRangeIndex}, parents).get(0);
		
		return writeBranchAggregatedTimeDepFigs(table, outputDir, parents, minMag, duration);
	}
	
	public static Map<Integer, List<Double>> writeBranchAggregatedTimeDepFigs(
			Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
			Map<LogicTreeBranch, SectProbGainResults[]>> table, File outputDir,
			boolean parents, double minMag, double duration)
					throws ZipException, IOException, GMT_MapException, RuntimeException {
		
		Map<LogicTreeBranch, SectProbGainResults[]> branchVals;
		branchVals = Maps.newHashMap();
		// TODO non-equal weighting?
		double cellWeight = 1d/(double)table.size();
		for (Cell<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
				Map<LogicTreeBranch, SectProbGainResults[]>> cell : table.cellSet()) {
			Map<LogicTreeBranch, SectProbGainResults[]> subBranchMap = cell.getValue();
			for (LogicTreeBranch branch : subBranchMap.keySet()) {
				SectProbGainResults[] vals = subBranchMap.get(branch);
				
				SectProbGainResults[] curVals = branchVals.get(branch);
				if (curVals == null) {
					curVals = new SectProbGainResults[vals.length];
					for (int i=0; i<curVals.length; i++)
						curVals[i] = new SectProbGainResults(0d, 0d, 0d, 0d, 0d, 0d);
					branchVals.put(branch, curVals);
				}
				for (int j=0; j<vals.length; j++) {
					curVals[j].recurrInt += vals[j].recurrInt*cellWeight;
					curVals[j].openInt += vals[j].openInt*cellWeight;
					curVals[j].pPois += vals[j].pPois*cellWeight;
					curVals[j].pBPT += vals[j].pBPT*cellWeight;
					curVals[j].pGain += vals[j].pGain*cellWeight;
					curVals[j].implGain += vals[j].implGain*cellWeight;
				}
				if (branch.equals(LogicTreeBranch.DEFAULT) && parents)
					System.out.println("REF BRANCH. Mojave S="+vals[230].pBPT+", running avg="+curVals[230].pBPT);
			}
		}
		
		HashSet<FaultModels> fms = new HashSet<FaultModels>();
		for (LogicTreeBranch branch : branchVals.keySet()) {
			FaultModels fm = branch.getValue(FaultModels.class);
			if (!fms.contains(fm))
				fms.add(fm);
		}
		
		HashSet<FaultTraceComparable> tracesSet = new HashSet<FaultSysSolutionERF_Calc.FaultTraceComparable>();
		Map<FaultModels, Map<FaultTraceComparable, Integer>> fmIndexMaps = Maps.newHashMap();
		Map<FaultTraceComparable, Double> meanYearsSinceMap = Maps.newHashMap();
		Map<FaultTraceComparable, Double> fractWithYearsSinceMap;
		if (parents)
			fractWithYearsSinceMap = Maps.newHashMap();
		else
			fractWithYearsSinceMap = null;
		long curMillis = new GregorianCalendar(FaultSystemSolutionERF.START_TIME_DEFAULT, 0, 0).getTimeInMillis();
		for (FaultModels fm : fms) {
			Map<FaultTraceComparable, Integer> fmIndexMap = Maps.newHashMap();
			fmIndexMaps.put(fm, fmIndexMap);
			
			ArrayList<FaultSectionPrefData> subSects = new DeformationModelFetcher(
					fm, DeformationModels.GEOLOGIC,
					UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1d).getSubSectionList();
			LastEventData.populateSubSects(subSects, LastEventData.load());
			
			if (parents) {
				// average open intervals 
				Map<Integer, List<FaultSectionPrefData>> subSectsMap = Maps.newHashMap();
				for (FaultSectionPrefData sect : subSects) {
					List<FaultSectionPrefData> subSectsForParent = subSectsMap.get(sect.getParentSectionId());
					if (subSectsForParent == null) {
						subSectsForParent = Lists.newArrayList();
						subSectsMap.put(sect.getParentSectionId(), subSectsForParent);
					}
					subSectsForParent.add(sect);
				}
				
				List<FaultSectionPrefData> parentSects = fm.fetchFaultSections();
				Collections.sort(parentSects, new NamedComparator());
				for (int i = 0; i < parentSects.size(); i++) {
					FaultSectionPrefData sect = parentSects.get(i);
					FaultTraceComparable comp = new FaultTraceComparable(
							sect.getName(), sect.getSectionId(), sect.getFaultTrace());
					tracesSet.add(comp);
					fmIndexMap.put(comp, i);
					
					Integer parentID = sect.getSectionId();
					List<FaultSectionPrefData> sects = subSectsMap.get(parentID);
					List<Long> lastDates = Lists.newArrayList();
					for (FaultSectionPrefData subSect : sects)
						if (subSect.getDateOfLastEvent() > Long.MIN_VALUE)
							lastDates.add(subSect.getDateOfLastEvent());
					double oi;
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
					double fractWith = (double)lastDates.size()/(double)sects.size();
					meanYearsSinceMap.put(comp, oi);
					fractWithYearsSinceMap.put(comp, fractWith);
				}
			} else {
				for (int i = 0; i < subSects.size(); i++) {
					FaultSectionPrefData sect = subSects.get(i);
					FaultTraceComparable comp = new FaultTraceComparable(
							sect.getName(), sect.getParentSectionId(), sect.getFaultTrace());
					tracesSet.add(comp);
					fmIndexMap.put(comp, i);
					double oi;
					if (sect.getDateOfLastEvent() > Long.MIN_VALUE)
						oi = YEARS_PER_MILLI*(curMillis - sect.getDateOfLastEvent());
					else
						oi = Double.NaN;
					meanYearsSinceMap.put(comp, oi);
				}
			}
		}
		
		List<FaultTraceComparable> traceComps = Lists.newArrayList(tracesSet);
		Collections.sort(traceComps);
		List<LocationList> traces = Lists.newArrayList();
		double[] meanBPTVals = new double[traceComps.size()];
		double[] minBPTVals = new double[traceComps.size()];
		double[] maxBPTVals = new double[traceComps.size()];
		double[] gainU3Vals = new double[traceComps.size()];
		double[] gainU3U2Vals = new double[traceComps.size()];
		Map<MagDependentAperiodicityOptions, double[]> meanBPT_COVVals = null;
		Map<BPTAveragingTypeOptions, double[]> meanBPT_CalcVals = null;
		if (table.rowKeySet().size() > 1) {
			meanBPT_COVVals = Maps.newHashMap();
			for (MagDependentAperiodicityOptions theCOV : table.rowKeySet())
				meanBPT_COVVals.put(theCOV, new double[traceComps.size()]);
		}
		if (table.columnKeySet().size() > 1) {
			meanBPT_CalcVals = Maps.newHashMap();
			for (BPTAveragingTypeOptions aveType : table.columnKeySet())
				meanBPT_CalcVals.put(aveType, new double[traceComps.size()]);
		}
		
		Map<FaultTraceComparable, Integer> traceToCombIndexMap = Maps.newHashMap();
		for (int i=0; i<traceComps.size(); i++) {
			FaultTraceComparable traceComp = traceComps.get(i);
			traces.add(traceComp.trace);
			traceToCombIndexMap.put(traceComp, i);
		}
		
		System.out.println(tracesSet.size()+" unique sects");
		
		BranchWeightProvider weightProv = new APrioriBranchWeightProvider();
		
		// aggregated CSV file
		CSVFile<String> csv = new CSVFile<String>(true);
		// TODO
		List<String> header = Lists.newArrayList("Name", "Fract With Years Since", "Average Years Since", "U3 Mean pBPT",
				"U3 Min", "U3 Max", "U3 pPois", "U3 pBPT/pPois");
		if (parents)
			header.addAll(Lists.newArrayList("U2 Mean", "U2 min", "U2 max", "U2 pPois", "MeanU3/MeanU2"));
		csv.addLine(header);
		
		// this is what gets returned
		Map<Integer, List<Double>> meanMap = Maps.newHashMap();
		
		for (int i = 0; i < traceComps.size(); i++) {
			FaultTraceComparable trace = traceComps.get(i);
			List<Double> bptVals = Lists.newArrayList();
			List<Double> poisVals = Lists.newArrayList();
			List<Double> gainVals = Lists.newArrayList();
			List<Double> weights = Lists.newArrayList();
			Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions, List<Double>>
				bptOpsValsTable = null;
			if (meanBPT_COVVals != null || meanBPT_CalcVals != null) {
				bptOpsValsTable = HashBasedTable.create();
				for (Cell<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
						Map<LogicTreeBranch, SectProbGainResults[]>> cell : table.cellSet())
					bptOpsValsTable.put(cell.getRowKey(), cell.getColumnKey(), new ArrayList<Double>());
			}
			for (LogicTreeBranch branch : branchVals.keySet()) {
				FaultModels fm = branch.getValue(FaultModels.class);
				Integer index = fmIndexMaps.get(fm).get(trace);
				if (index == null)
					continue;
				SectProbGainResults val = branchVals.get(branch)[index];
				bptVals.add(val.pBPT);
				poisVals.add(val.pPois);
				gainVals.add(val.pGain);
				weights.add(weightProv.getWeight(branch));
				if (bptOpsValsTable != null) {
					for (Cell<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
							Map<LogicTreeBranch, SectProbGainResults[]>> cell : table.cellSet())
						bptOpsValsTable.get(cell.getRowKey(), cell.getColumnKey()).add(
								table.get(cell.getRowKey(), cell.getColumnKey()).get(branch)[index].pBPT);
				}
			}
			double[] bptValsArray = Doubles.toArray(bptVals);
			double[] poisValsArray = Doubles.toArray(poisVals);
			double[] weightsArray = Doubles.toArray(weights);
			
//			double meanBPT = weightedAvgNonZero(bptVals, weights);
//			double minBPT = minNonZero(bptVals);
//			double maxBPT = maxNonZero(bptVals);
//			double meanPois = weightedAvgNonZero(poisVals, weights);
			double meanBPT = FaultSystemSolutionFetcher.calcScaledAverage(
					bptValsArray, weightsArray);
			double minBPT = StatUtils.min(bptValsArray);
			double maxBPT = StatUtils.max(bptValsArray);
			double meanPois = FaultSystemSolutionFetcher.calcScaledAverage(
					poisValsArray, weightsArray);
			double gainU3 = weightedAvgNonZero(gainVals, weights);
			
			double oi = meanYearsSinceMap.get(trace);
			double fractWith;
			if (fractWithYearsSinceMap == null) {
				if (Double.isNaN(oi))
					fractWith = 0d;
				else
					fractWith = 1d;
			} else {
				fractWith = fractWithYearsSinceMap.get(trace);
			}
			
			List<String> line = Lists.newArrayList(trace.name, fractWith+"", oi+"",
					meanBPT+"", minBPT+"", maxBPT+"", meanPois+"", gainU3+"");
			
			if (parents) {
				// 
				
				double meanU2 = Double.NaN;
				double minU2 = Double.NaN;
				double maxU2 = Double.NaN;
				double meanU2pois = Double.NaN;
				ArrayList<IncrementalMagFreqDist> mfds =
						UCERF2_Section_TimeDepMFDsCalc.getMeanMinAndMaxMFD(trace.parentID, true, true);
				if (mfds != null) {
					// cumulative mfd so get the first above
					meanU2 = calcProbAboveMagFromMFD(mfds.get(0), minMag, duration);
					minU2 = calcProbAboveMagFromMFD(mfds.get(1), minMag, duration);
					maxU2 = calcProbAboveMagFromMFD(mfds.get(2), minMag, duration);
					meanU2pois = calcProbAboveMagFromMFD(UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(
							trace.parentID, true, true).get(0), minMag, duration);
				}
				
				double gainU3U2 = meanBPT / meanU2;
				gainU3U2Vals[i] = gainU3U2;
				
				line.addAll(Lists.newArrayList(meanU2+"", minU2+"", maxU2+"", meanU2pois+"", gainU3U2+""));
			}
			csv.addLine(line);
			
			meanBPTVals[i] = meanBPT;
			minBPTVals[i] = minBPT;
			maxBPTVals[i] = maxBPT;
			gainU3Vals[i] = gainU3;
			
			if (meanBPT_COVVals != null) {
				for (MagDependentAperiodicityOptions theCOV : meanBPT_COVVals.keySet()) {
					List<Double> avgVals = Lists.newArrayList();
					for (Cell<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,List<Double>> cell
							: bptOpsValsTable.cellSet()) {
						if (cell.getRowKey() != theCOV)
							continue;
						double[] bptCOV_ValsArray = Doubles.toArray(cell.getValue());
						avgVals.add(FaultSystemSolutionFetcher.calcScaledAverage(
							bptCOV_ValsArray, weightsArray));
					}
					Preconditions.checkState(!avgVals.isEmpty());
					meanBPT_COVVals.get(theCOV)[i] = StatUtils.mean(Doubles.toArray(avgVals));
				}
			}
			
			if (meanBPT_CalcVals != null) {
				for (BPTAveragingTypeOptions theAve : meanBPT_CalcVals.keySet()) {
					List<Double> avgVals = Lists.newArrayList();
					for (Cell<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,List<Double>> cell
							: bptOpsValsTable.cellSet()) {
						if (cell.getColumnKey() != theAve)
							continue;
						double[] bptCOV_ValsArray = Doubles.toArray(cell.getValue());
						avgVals.add(FaultSystemSolutionFetcher.calcScaledAverage(
							bptCOV_ValsArray, weightsArray));
					}
					Preconditions.checkState(!avgVals.isEmpty());
					meanBPT_CalcVals.get(theAve)[i] = StatUtils.mean(Doubles.toArray(avgVals));
				}
			}
			
			List<Double> parentVals = meanMap.get(trace.parentID);
			if (parentVals == null) {
				parentVals = Lists.newArrayList();
				meanMap.put(trace.parentID, parentVals);
			}
			parentVals.add(meanBPT);
		}
		
		File csvFile;
		if (parents)
			csvFile = new File(outputDir, "branch_aggregated_parents.csv");
		else
			csvFile = new File(outputDir, "branch_aggregated_subs.csv");
		csv.writeToFile(csvFile);
		
		CPT ratioCPT = getScaledLinearRatioCPT(0.02d);
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (parents) {
			FaultBasedMapGen.makeFaultPlot(ratioCPT, traces, gainU3U2Vals, region,
					outputDir, "gain_u3_u2", false, true, "UCERF3/UCERF2 BPT Ratio");
			return meanMap;
		}
		
		// now do branch choice ratio maps
		File comparePlotsDir = new File(outputDir, "branch_ratios");
		if (!comparePlotsDir.exists())
			comparePlotsDir.mkdir();
		
		for (Class<? extends LogicTreeBranchNode<?>> clazz : LogicTreeBranch.getLogicTreeNodeClasses()) {
			if (clazz.equals(InversionModels.class) || clazz.equals(MomentRateFixes.class))
				continue;
			String className = ClassUtils.getClassNameWithoutPackage(clazz);
			LogicTreeBranchNode<?>[] choices = clazz.getEnumConstants();
			for (LogicTreeBranchNode<?> choice : choices) {
				if (choice.getRelativeWeight(InversionModels.CHAR_CONSTRAINED) <= 0)
					continue;
				double[] choiceVals = new double[meanBPTVals.length];
				double[] weightTots = new double[meanBPTVals.length];
				for (LogicTreeBranch branch : branchVals.keySet()) {
					if (branch.getValueUnchecked(clazz) != choice)
						continue;
					FaultModels fm = branch.getValue(FaultModels.class);
					double weight = weightProv.getWeight(branch);
					SectProbGainResults[] vals = branchVals.get(branch);
					for (int i = 0; i < traceComps.size(); i++) {
						FaultTraceComparable trace = traceComps.get(i);
						Integer index = fmIndexMaps.get(fm).get(trace);
						if (index == null)
							continue;
						double val = vals[index].pBPT;
						if (Double.isNaN(val))
							continue;
						choiceVals[i] += val*weight;
						weightTots[i] += weight;
					}
				}
				// scale for total weight
				for (int i=0; i<choiceVals.length; i++)
					choiceVals[i] /= weightTots[i];
				
				double[] ratios = new double[choiceVals.length];
				for (int i=0; i<ratios.length; i++)
					ratios[i] = choiceVals[i] / meanBPTVals[i];
				String prefix = className+"_"+choice.encodeChoiceString();
				String plotLabel = choice.encodeChoiceString()+"/Mean";
				
				FaultBasedMapGen.makeFaultPlot(ratioCPT, traces, ratios, region,
						comparePlotsDir, prefix, false, true, plotLabel);
			}
		}
		// now do it for COV values
		if (meanBPT_COVVals != null) {
			for (MagDependentAperiodicityOptions theCOV : meanBPT_COVVals.keySet()) {
				double[] choiceVals = meanBPT_COVVals.get(theCOV);
				double[] ratios = new double[choiceVals.length];
				for (int i=0; i<ratios.length; i++)
					ratios[i] = choiceVals[i] / meanBPTVals[i];
				String prefix = "MagDepAperiodicity_"+theCOV.name();
				String plotLabel = theCOV.name()+"/Mean";
				
				FaultBasedMapGen.makeFaultPlot(ratioCPT, traces, ratios, region,
						comparePlotsDir, prefix, false, true, plotLabel);
			}
		}
		// now do it for Ave Type values
		if (meanBPT_CalcVals != null) {
			for (BPTAveragingTypeOptions theAve : meanBPT_CalcVals.keySet()) {
				double[] choiceVals = meanBPT_CalcVals.get(theAve);
				double[] ratios = new double[choiceVals.length];
				for (int i=0; i<ratios.length; i++)
					ratios[i] = choiceVals[i] / meanBPTVals[i];
				String prefix = "BPTAveType_"+theAve.getFileSafeLabel();
				String plotLabel = theAve.getCompactLabel()+"/Mean";
				
				FaultBasedMapGen.makeFaultPlot(ratioCPT, traces, ratios, region,
						comparePlotsDir, prefix, false, true, plotLabel);
			}
		}
		
		// now do min/mean/max prob maps
		CPT logProbCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-4d, 0d);
		
		FaultBasedMapGen.makeFaultPlot(logProbCPT, traces, FaultBasedMapGen.log10(meanBPTVals), region,
				outputDir, "mean_bpt_prob", false, true, "UCERF3 Mean BPT Prob");
		FaultBasedMapGen.makeFaultPlot(logProbCPT, traces, FaultBasedMapGen.log10(minBPTVals), region,
				outputDir, "min_bpt_prob", false, true, "UCERF3 Min BPT Prob");
		FaultBasedMapGen.makeFaultPlot(logProbCPT, traces, FaultBasedMapGen.log10(maxBPTVals), region,
				outputDir, "max_bpt_prob", false, true, "UCERF3 Max BPT Prob");
		FaultBasedMapGen.makeFaultPlot(ratioCPT, traces, gainU3Vals, region,
				outputDir, "gain_u3", false, true, "UCERF3 Mean BPT/Poisson Prob Gain");
		
		return meanMap;
	}
	
	private static void writeBranchAggregatedFaultResults(
			Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
			Map<LogicTreeBranch, SectProbGainResults[]>> table, File outputDir,
			double minMag, double duration) throws IOException {
		
		Map<LogicTreeBranch, SectProbGainResults[]> branchVals;
		branchVals = Maps.newHashMap();
		// TODO non-equal weighting?
		double cellWeight = 1d/(double)table.size();
		for (Cell<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
				Map<LogicTreeBranch, SectProbGainResults[]>> cell : table.cellSet()) {
			Map<LogicTreeBranch, SectProbGainResults[]> subBranchMap = cell.getValue();
			for (LogicTreeBranch branch : subBranchMap.keySet()) {
				SectProbGainResults[] vals = subBranchMap.get(branch);
				
				SectProbGainResults[] curVals = branchVals.get(branch);
				if (curVals == null) {
					curVals = new SectProbGainResults[vals.length];
					for (int i=0; i<curVals.length; i++)
						curVals[i] = new SectProbGainResults(0d, 0d, 0d, 0d, 0d, 0d);
					branchVals.put(branch, curVals);
				}
				for (int j=0; j<vals.length; j++) {
					curVals[j].recurrInt += vals[j].recurrInt*cellWeight;
					curVals[j].openInt += vals[j].openInt*cellWeight;
					curVals[j].pPois += vals[j].pPois*cellWeight;
					curVals[j].pBPT += vals[j].pBPT*cellWeight;
					curVals[j].pGain += vals[j].pGain*cellWeight;
					curVals[j].implGain += vals[j].implGain*cellWeight;
				}
			}
		}
		
		// aggregated CSV file
		CSVFile<String> csv = new CSVFile<String>(true);
		csv.addLine("Name", "U3 Mean pBPT", "U3 Min", "U3 Max", "U3 pPois", "U3 pBPT/pPois");
		
		List<String> faultNames = Lists.newArrayList(FaultModels.parseNamedFaultsAltFile(
				UCERF3_DataUtils.getReader("FaultModels", "MainFaultsForTimeDepComparison.txt")).keySet());
		Collections.sort(faultNames);
		
		BranchWeightProvider weightProv = new APrioriBranchWeightProvider();
		
		for (int i = 0; i < faultNames.size(); i++) {
			String name = faultNames.get(i);
			List<Double> bptVals = Lists.newArrayList();
			List<Double> poisVals = Lists.newArrayList();
			List<Double> gainVals = Lists.newArrayList();
			List<Double> weights = Lists.newArrayList();
			for (LogicTreeBranch branch : branchVals.keySet()) {
				FaultModels fm = branch.getValue(FaultModels.class);
				if ((name.contains("FM3.1") && fm == FaultModels.FM3_2)
						|| (name.contains("FM3.2") && fm == FaultModels.FM3_1))
					continue;
				SectProbGainResults val = branchVals.get(branch)[i];
				if (Double.isNaN(val.pBPT))
					continue;
				bptVals.add(val.pBPT);
				poisVals.add(val.pPois);
				gainVals.add(val.pGain);
				weights.add(weightProv.getWeight(branch));
			}
			double[] bptValsArray = Doubles.toArray(bptVals);
			double[] poisValsArray = Doubles.toArray(poisVals);
			double[] weightsArray = Doubles.toArray(weights);
			
//			double meanBPT = weightedAvgNonZero(bptVals, weights);
//			double minBPT = minNonZero(bptVals);
//			double maxBPT = maxNonZero(bptVals);
//			double meanPois = weightedAvgNonZero(poisVals, weights);
			double meanBPT = FaultSystemSolutionFetcher.calcScaledAverage(
					bptValsArray, weightsArray);
			double minBPT = StatUtils.min(bptValsArray);
			double maxBPT = StatUtils.max(bptValsArray);
			double meanPois = FaultSystemSolutionFetcher.calcScaledAverage(
					poisValsArray, weightsArray);
			double gainU3 = weightedAvgNonZero(gainVals, weights);
			
			csv.addLine(name, meanBPT+"", minBPT+"", maxBPT+"", meanPois+"", gainU3+"");
		}
		
		File csvFile = new File(outputDir, "branch_aggregated_main_faults.csv");
		csv.writeToFile(csvFile);
	}
	
	private static double calcProbAboveMagFromMFD(EvenlyDiscretizedFunc cmlMFD, double minMag, double duration) {
		cmlMFD = calcProbsFromSummedMFD(cmlMFD, duration);
		Preconditions.checkState(minMag <= cmlMFD.getMaxX());
		return cmlMFD.getClosestY(minMag);
//		for (Point2D pt : calcProbsFromSummedMFD(cmlMFD, duration))
//			if (pt.getX() >= minMag)
//				return pt.getY();
//		return 0;
	}
	
	private static double weightedAvgNonZero(List<Double> vals, List<Double> weights) {
		double runningTot = 0;
		double totWeight = 0d;
		
		for (int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			if (val > 0) {
				// this will fail on NaNs which is also desired
				double weight = weights.get(i);
				runningTot += val*weight;
				totWeight += weight;
			}
		}
		if (runningTot == 0)
			return Double.NaN;
		
		runningTot /= totWeight;
		
		return runningTot;
	}
	
	private static double minNonZero(List<Double> vals) {
		double min = Double.POSITIVE_INFINITY;
		for (double val : vals)
			if (val > 0 && val < min)
				min = val;
		if (Double.isInfinite(min))
			return Double.NaN;
		return min;
	}
	
	private static double maxNonZero(List<Double> vals) {
		double max = 0;
		for (double val : vals)
			if (val > 0 && val > max)
				max = val;
		if (max == 0)
			return Double.NaN;
		return max;
	}
	
	private static class FaultTraceComparable implements Comparable<FaultTraceComparable> {
		private String name;
		private int parentID;
		private FaultTrace trace;
		
		public FaultTraceComparable(String name, int parentID, FaultTrace trace) {
			this.name = name;
			this.parentID = parentID;
			this.trace = trace;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + parentID;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FaultTraceComparable other = (FaultTraceComparable) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (parentID != other.parentID)
				return false;
			return true;
		}

		@Override
		public int compareTo(FaultTraceComparable o) {
			return name.compareTo(o.name);
		}
		
	}
	
	private static class SectProbGainResults {
		double recurrInt, openInt, pPois, pBPT, pGain, implGain;

		public SectProbGainResults(double recurrInt, double openInt,
				double pPois, double pBPT, double pGain, double implGain) {
			super();
			this.recurrInt = recurrInt;
			this.openInt = openInt;
			this.pPois = pPois;
			this.pBPT = pBPT;
			this.pGain = pGain;
			this.implGain = implGain;
		}
	}

	private static List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
	Map<LogicTreeBranch, SectProbGainResults[]>>> loadBranchCSVVals(
			File file, int[] magRangeIndexes, boolean parents) throws ZipException, IOException {
		return loadBranchCSVVals(new File[] {file}, magRangeIndexes, parents);
	}

	private static List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
	Map<LogicTreeBranch, SectProbGainResults[]>>> loadBranchCSVVals(
			File[] files, int[] magRangeIndexes, boolean parents) throws ZipException, IOException {
		// first '2' is for subsection indexes
		// the other '1' is for the blank col at the start of each mag range
		int[] colStarts = new int[magRangeIndexes.length];
		List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
		Map<LogicTreeBranch, SectProbGainResults[]>>> maps = Lists.newArrayList();
		for (int i=0; i<magRangeIndexes.length; i++) {
			colStarts[i] = 2 + magRangeIndexes[i]*7 + 1;
			Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
				Map<LogicTreeBranch, SectProbGainResults[]>> table = HashBasedTable.create();
			maps.add(table);
		}
		
		for (File file : files) {
			ZipFile zip = new ZipFile(file);
			
			for (ZipEntry entry : Collections.list(zip.entries())) {
				String name = entry.getName().trim();
//				System.out.println("File: "+name);
				if (parents && !name.endsWith("parents.csv"))
					continue;
				if (!parents && !name.endsWith("subs.csv"))
					continue;
				
				// remove first directory name
				int covEnd = name.lastIndexOf("/");
				String namePrefix = name.substring(0, covEnd);
				name = name.substring(covEnd+1);
				// find the cov value
				MagDependentAperiodicityOptions cov = null;
				for (MagDependentAperiodicityOptions testCOV : MagDependentAperiodicityOptions.values()) {
					if (namePrefix.contains(testCOV.name())) {
						cov = testCOV;
						break;
					}
				}
				Preconditions.checkNotNull(cov);
				BPTAveragingTypeOptions aveType = null;
				for (BPTAveragingTypeOptions testType : BPTAveragingTypeOptions.values()) {
					String dirName = MPJ_ERF_ProbGainCalcScriptWriter.getAveDirName(testType);
					if (namePrefix.contains(dirName) || file.getName().startsWith(dirName)) {
						aveType = testType;
						break;
					}
				}
				Preconditions.checkNotNull(aveType);
				LogicTreeBranch branch = LogicTreeBranch.fromFileName(name);
				Preconditions.checkNotNull(branch);
//				System.out.println("Loading "+branch.buildFileName()+", cov="+cov.name());
				CSVFile<String> csv = CSVFile.readStream(zip.getInputStream(entry), true);
				for (int i=0; i<magRangeIndexes.length; i++) {
					int colStart = colStarts[i];
					Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
						Map<LogicTreeBranch, SectProbGainResults[]>> table = maps.get(i);
					
					Preconditions.checkState(csv.get(0, colStart).startsWith("Recur"));
					SectProbGainResults[] vals = new SectProbGainResults[csv.getNumRows()-1];
					double recurrInt, openInt, pPois, pBPT, pGain, implGain;
					for (int row=1; row<csv.getNumRows(); row++) {
						recurrInt = Double.parseDouble(csv.get(row, colStart));
						openInt = Double.parseDouble(csv.get(row, colStart+1));
						pPois = Double.parseDouble(csv.get(row, colStart+2));
						pBPT = Double.parseDouble(csv.get(row, colStart+3));
						pGain = Double.parseDouble(csv.get(row, colStart+4));
						implGain = Double.parseDouble(csv.get(row, colStart+5));
						vals[row-1] = new SectProbGainResults(recurrInt, openInt, pPois, pBPT, pGain, implGain);
					}
					
					Map<LogicTreeBranch, SectProbGainResults[]> branchVals = table.get(cov, aveType);
					if (branchVals == null) {
						branchVals = Maps.newHashMap();
						table.put(cov, aveType, branchVals);
					}
					Preconditions.checkState(!branchVals.containsKey(branch));
					branchVals.put(branch, vals);
				}
			}
		}
		
		return maps;
	}

	private static List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
	Map<LogicTreeBranch, SectProbGainResults[]>>>
	loadBranchFaultCSVVals(File[] files, int[] magRangeIndexes) throws ZipException, IOException {
		int[] colStarts = new int[magRangeIndexes.length];
		List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
		Map<LogicTreeBranch, SectProbGainResults[]>>> maps = Lists.newArrayList();
		for (int i=0; i<magRangeIndexes.length; i++) {
			// first '1' is for name
						// the other '1' is for the blank col at the start of each mag range
						colStarts[i] = 1 + magRangeIndexes[i]*3 + 1;
			Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions,
				Map<LogicTreeBranch, SectProbGainResults[]>> table = HashBasedTable.create();
			maps.add(table);
		}
		
		for (File file : files) {
			ZipFile zip = new ZipFile(file);
			
			for (ZipEntry entry : Collections.list(zip.entries())) {
				String name = entry.getName().trim();
//				System.out.println("File: "+name);
				if (!name.endsWith("main_faults.csv"))
					continue;
				
				// remove first directory name
				int covEnd = name.lastIndexOf("/");
				String namePrefix = name.substring(0, covEnd);
				name = name.substring(covEnd+1);
				// find the cov value
				MagDependentAperiodicityOptions cov = null;
				for (MagDependentAperiodicityOptions testCOV : MagDependentAperiodicityOptions.values()) {
					if (namePrefix.contains(testCOV.name())) {
						cov = testCOV;
						break;
					}
				}
				Preconditions.checkNotNull(cov);
				BPTAveragingTypeOptions aveType = null;
				for (BPTAveragingTypeOptions testType : BPTAveragingTypeOptions.values()) {
					String dirName = MPJ_ERF_ProbGainCalcScriptWriter.getAveDirName(testType);
					if (namePrefix.contains(dirName) || file.getName().startsWith(dirName)) {
						aveType = testType;
						break;
					}
				}
				Preconditions.checkNotNull(aveType);
				LogicTreeBranch branch = LogicTreeBranch.fromFileName(name);
				Preconditions.checkNotNull(branch);
//				System.out.println("Loading "+branch.buildFileName()+", cov="+cov.name());
				CSVFile<String> csv = CSVFile.readStream(zip.getInputStream(entry), true);
				for (int i=0; i<magRangeIndexes.length; i++) {
					int colStart = colStarts[i];
					Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions, Map<LogicTreeBranch, SectProbGainResults[]>> table = maps.get(i);
					
//					System.out.println("Col start: "+colStart);
					
					Preconditions.checkState(csv.get(0, colStart).startsWith("U3 pBPT"));
					SectProbGainResults[] vals = new SectProbGainResults[csv.getNumRows()-1];
					double recurrInt = Double.NaN, openInt = Double.NaN, pPois, pBPT, pGain, implGain = Double.NaN;
					for (int row=1; row<csv.getNumRows(); row++) {
						pBPT = Double.parseDouble(csv.get(row, colStart));
						pPois = Double.parseDouble(csv.get(row, colStart+1));
						pGain = pBPT/pPois;
						vals[row-1] = new SectProbGainResults(recurrInt, openInt, pPois, pBPT, pGain, implGain);
//						System.out.println(csv.get(row, 0)+": pBPT="+pBPT+"\tpPois="+pPois);
					}
//					System.exit(0);
					
					Map<LogicTreeBranch, SectProbGainResults[]> branchVals = table.get(cov, aveType);
					if (branchVals == null) {
						branchVals = Maps.newHashMap();
						table.put(cov, aveType, branchVals);
					}
					Preconditions.checkState(!branchVals.containsKey(branch));
					branchVals.put(branch, vals);
				}
			}
		}
		
		return maps;
	}
	
	public static void writeTimeDepPlotsForWeb(List<BPTAveragingTypeOptions> aveTypes, boolean skipAvgMethods,
			String dirPrefix, File outputDir)
			throws IOException, DocumentException, GMT_MapException, RuntimeException {
		if (!outputDir.exists())
			outputDir.mkdir();
		
		FaultSystemSolution meanSol = FaultSystemIO.loadSol(
				new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
						"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		
		double[] minMags = { 0d, 6.7d, 7.7d };
		int[] csvMagRangeIndexes = { 4, 0, 2 };
		int[] csvFaultMagRangeIndexes = { 0, 1, 3 };
		double[] durations = { 5d, 30d };
		
		Preconditions.checkState(aveTypes.size() >= 1);
		String[] csvZipNames = new String[aveTypes.size()];
		for (int i=0; i<aveTypes.size(); i++)
			csvZipNames[i] = MPJ_ERF_ProbGainCalcScriptWriter.getAveDirName(aveTypes.get(i))+".zip";
		
		File[] csvDirs = { new File(dirPrefix+"-5yr"), new File(dirPrefix+"-30yr")};
		File[] csvMainFaultDirs = { new File(dirPrefix+"-main-5yr"), new File(dirPrefix+"-main-30yr")};
		
//		File[] csvDirs = { new File("/home/kevin/OpenSHA/UCERF3/probGains/2013_12_14-ucerf3-prob-gains-open1875-5yr"),
//				new File("/home/kevin/OpenSHA/UCERF3/probGains/2013_12_14-ucerf3-prob-gains-open1875-30yr")};
//		File[] csvMainFaultDirs = { new File("/home/kevin/OpenSHA/UCERF3/probGains/2013_12_14-ucerf3-prob-gains-open1875-main-5yr"),
//				new File("/home/kevin/OpenSHA/UCERF3/probGains/2013_12_14-ucerf3-prob-gains-open1875-main-30yr")};
		int def_hist_open_ref = 1875;
//		int def_hist_open_ref = FaultSystemSolutionERF.START_TIME_DEFAULT;
		
		// write metadata file
		FileWriter fw = new FileWriter(new File(outputDir, "metadata.txt"));
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss z");
		fw.write("Directory and plots generated by "+FaultSysSolutionERF_Calc.class.getName()+".writeTimeDepPlotsForWeb()\n");
		fw.write("Which calls and aggregates plots from "+FaultSysSolutionERF_Calc.class.getName()
				+".writeBranchAggregatedFigs(...)\n");
		fw.write("Date: "+df.format(new Date())+"\n");
		fw.write("\n");
		for (int i=0; i<durations.length; i++)
			fw.write((int)durations[i]+"yr data loaded from "+csvDirs[i].getName()+"\n");
		fw.close();
		
		List<String> labels = Lists.newArrayList();
		List<File> parentSectFiles = Lists.newArrayList();
		List<File> mainFaultFiles = Lists.newArrayList();
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		CPT tightRatioCPT = getScaledLinearRatioCPT(0.02, 0.8d, 1.2d);
		CPT wideRatioCPT = getScaledLinearRatioCPT(0.02);
		
		for (int i = 0; i < durations.length; i++) {
			double duration = durations[i];
			File[] csvZipFiles = new File[csvZipNames.length];
			File[] csvMainFualtZipFiles = new File[csvZipNames.length];
			for (int j=0; j<csvZipNames.length; j++) {
				String csvZipName = csvZipNames[j];
				csvZipFiles[j] = new File(csvDirs[i], csvZipName);
				csvMainFualtZipFiles[j] = new File(csvMainFaultDirs[i], csvZipName);
			}
			
			File avgTempDir = null;
			if (!skipAvgMethods) {
				FaultSystemSolutionERF meanERF = new FaultSystemSolutionERF(meanSol);
				meanERF.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
				meanERF.setParameter(HistoricOpenIntervalParam.NAME, (double)(FaultSystemSolutionERF.START_TIME_DEFAULT-def_hist_open_ref));
				meanERF.getTimeSpan().setDuration(duration);
				
				// TRUE: RI, NTS
				// FALSE: Rate, TS
				List<BPTAveragingTypeOptions> avgTypes = Lists.newArrayList(BPTAveragingTypeOptions.values());
				avgTempDir = FileUtils.createTempDir();
//				File testDir = new File("/tmp/avg_test_"+(int)duration);
//				testDir.mkdir();
//				makeAvgMethodProbGainMaps(meanERF, testDir, "tester");
				while (avgTypes.size() >= 2) {
					int refIndex = 0;
					makeAvgMethodProbGainMaps(meanERF, avgTempDir, null, avgTypes, refIndex);
					avgTypes.remove(0);
				}
			}
			
			// average cov's
			System.out.println("Loading all parent sect results from "+csvDirs[i].getAbsolutePath()
					+" ("+Joiner.on(",").join(csvZipNames)+")");
			List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions, Map<LogicTreeBranch, SectProbGainResults[]>>> parentMaps =
					loadBranchCSVVals(csvZipFiles, csvMagRangeIndexes, true);
			System.out.println("Loading all sub sect results from "+csvDirs[i].getAbsolutePath()
					+" ("+Joiner.on(",").join(csvZipNames)+")");
			List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions, Map<LogicTreeBranch, SectProbGainResults[]>>> subSectMaps =
					loadBranchCSVVals(csvZipFiles, csvMagRangeIndexes, false);
			System.out.println("Loading all main fault results from "+csvMainFaultDirs[i].getAbsolutePath()
					+" ("+Joiner.on(",").join(csvZipNames)+")");
			List<Table<MagDependentAperiodicityOptions, BPTAveragingTypeOptions, Map<LogicTreeBranch, SectProbGainResults[]>>> mainFaultMaps =
					loadBranchFaultCSVVals(csvMainFualtZipFiles, csvFaultMagRangeIndexes);
			
			for (int j = 0; j < minMags.length; j++) {
				FaultSystemSolutionERF meanERF = new FaultSystemSolutionERF(meanSol);
				meanERF.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
				meanERF.setParameter(HistoricOpenIntervalParam.NAME, (double)(FaultSystemSolutionERF.START_TIME_DEFAULT-def_hist_open_ref));
				if (csvZipNames.length == 1)
					// we have only one avg method, make sure mean erf uses that one. otherwise use default
					meanERF.setParameter(BPTAveragingTypeParam.NAME, aveTypes.get(0));
				meanERF.getTimeSpan().setDuration(duration);
				
				double minMag = minMags[j];
				String fileLabel, label;
				
				if (minMag == 0) {
					label = "All Events";
					fileLabel = "all";
				} else {
					label = "M>="+(float)minMag;
					fileLabel = "m"+(float)minMag;
				}
				
				label += ", "+(int)duration+"yr forecast";
				fileLabel += "_"+(int)duration+"yr";
				
				File subDir = new File(outputDir, fileLabel);
				if (!subDir.exists())
					subDir.mkdir();
				
				File branchDir = new File(subDir, "BranchAveragedResults");
				if (!branchDir.exists())
					branchDir.mkdir();
				
				File tmpResultsDir = FileUtils.createTempDir();
				System.out.println("Making "+label+" sub section plots");
				Map<Integer, List<Double>> meanVals =
						writeBranchAggregatedTimeDepFigs(subSectMaps.get(j), tmpResultsDir, false,
								minMag, duration);
				
				System.out.println("Copying "+label+" sub section plots");
				
				// copy over branch averaged appropriate files
				Files.copy(new File(tmpResultsDir, "mean_bpt_prob.pdf"), new File(branchDir, "U3_BPT_Mean.pdf"));
				Files.copy(new File(tmpResultsDir, "min_bpt_prob.pdf"), new File(branchDir, "U3_BPT_Min.pdf"));
				Files.copy(new File(tmpResultsDir, "max_bpt_prob.pdf"), new File(branchDir, "U3_BPT_Max.pdf"));
				Files.copy(new File(tmpResultsDir, "gain_u3.pdf"), new File(branchDir, "U3_Gain.pdf"));
				
				// copy over branch sensitivity maps
				File branchSensDir = new File(subDir, "BranchSensitivityMaps");
				if (!branchSensDir.exists())
					branchSensDir.mkdir();
				for (File file : new File(tmpResultsDir, "branch_ratios").listFiles()) {
					String name = file.getName();
					if (!name.endsWith(".pdf"))
						continue;
					Files.copy(file, new File(branchSensDir, name));
				}
				
				// copy over sub sect vals
				Files.copy(new File(tmpResultsDir, "branch_aggregated_subs.csv"), new File(subDir, "sub_section_probabilities.csv"));
				
				FileUtils.deleteRecursive(tmpResultsDir);
				
				// now parent sections
				tmpResultsDir = FileUtils.createTempDir();
				System.out.println("Making "+label+" parent section plots");
				writeBranchAggregatedTimeDepFigs(parentMaps.get(j), tmpResultsDir, true, minMag, duration);
				System.out.println("Copying "+label+" parent section plots");
				File parentsDestCSV = new File(subDir, "parent_section_probabilities.csv");
				Files.copy(new File(tmpResultsDir, "branch_aggregated_parents.csv"), parentsDestCSV);
				Files.copy(new File(tmpResultsDir, "gain_u3_u2.pdf"), new File(branchDir, "U3_U2_BPT_Ratio.pdf"));
				labels.add(label);
				parentSectFiles.add(parentsDestCSV);
				
				FileUtils.deleteRecursive(tmpResultsDir);
				
				// now main faults sections
				tmpResultsDir = FileUtils.createTempDir();
				System.out.println("Making "+label+" main fault plots");
				writeBranchAggregatedFaultResults(mainFaultMaps.get(j), tmpResultsDir, minMag, duration);
				System.out.println("Copying "+label+" main fault plots");
				File mainFaultsDestCSV = new File(subDir, "main_fault_probabilities.csv");
				Files.copy(new File(tmpResultsDir, "branch_aggregated_main_faults.csv"), mainFaultsDestCSV);
				mainFaultFiles.add(mainFaultsDestCSV);
				
				FileUtils.deleteRecursive(tmpResultsDir);
				System.out.println("Done with "+label);
				
				// OtherSensitivityTests
				File sensTestDir = new File(subDir, "OtherSensitivityTests");
				if (!sensTestDir.exists())
					sensTestDir.mkdir();
				// ratio of branch-averaged mean to mean FSS
				meanERF.getTimeSpan().setDuration(duration);
				meanERF.updateForecast();
				EvenlyDiscretizedFunc[] branchAvgResults = calcSubSectSupraSeisMagProbDists(meanERF, minMag, 1, 0.5d);
				double[] ratios = new double[branchAvgResults.length];
				double[] baProbs = new double[branchAvgResults.length];
				int prevParent = -1;
				int indexInParent = -1;
				List<LocationList> faults = Lists.newArrayList();
				for (FaultSectionPrefData sect : meanSol.getRupSet().getFaultSectionDataList())
					faults.add(sect.getFaultTrace());
				for (int k=0; k<ratios.length; k++) {
					double baProb = branchAvgResults[k].getY(0);
					int myParent = meanSol.getRupSet().getFaultSectionData(k).getParentSectionId();
					if (myParent == prevParent) {
						indexInParent++;
					} else {
						prevParent = myParent;
						indexInParent = 0;
					}
					double meanProb = meanVals.get(myParent).get(indexInParent);
					ratios[k] = baProb / meanProb;
					baProbs[k] = baProb;
				}
				FaultBasedMapGen.makeFaultPlot(wideRatioCPT, faults, ratios, region,
						sensTestDir, "Branch_Averaged_vs_Mean_Ratio", false, true,
						"UCERF3 BPT Branch Averaged / True Mean");
				
				// mean ratios for each averaging method
				File avgMethodDir = new File(sensTestDir, "AveragingMethods");
				if (!avgMethodDir.exists())
					avgMethodDir.mkdir();
				
				// copy results
				String magStr;
				if (minMag > 0)
					magStr = (float)minMag+"";
				else
					magStr = "supra_seis";
				if (avgTempDir != null) {
					for (File file : avgTempDir.listFiles())
						if (file.getName().endsWith(".pdf") && file.getName().contains(magStr))
							Files.copy(file, new File(avgMethodDir, file.getName()));
				}
				
				int[] comps = { FaultSystemSolutionERF.START_TIME_DEFAULT, 1850, 1900 };
				
				for (int comp : comps) {
					// historic open interval (ratio of none to 1850)
					meanERF.setParameter(HistoricOpenIntervalParam.NAME, (double)(FaultSystemSolutionERF.START_TIME_DEFAULT-comp));
					meanERF.updateForecast();
					EvenlyDiscretizedFunc[] histOpenResults = calcSubSectSupraSeisMagProbDists(meanERF, minMag, 1, 0.5d);
					ratios = new double[baProbs.length];
					for (int k=0; k<baProbs.length; k++)
						ratios[k] = histOpenResults[k].getY(0) / baProbs[k];
					String compStr;
					if (comp == FaultSystemSolutionERF.START_TIME_DEFAULT)
						compStr = "None";
					else
						compStr = comp+""; 
					FaultBasedMapGen.makeFaultPlot(wideRatioCPT, faults, ratios, region,
							sensTestDir, "Hist_Open_Interval_Test_"+compStr, false, true,
							"UCERF3 BPT Hist Open Interval "+compStr+" / "+def_hist_open_ref);
				}
				// clear out png files
				for (File file : sensTestDir.listFiles())
					if (file.getName().endsWith(".png"))
						file.delete();
			}
			if (avgTempDir != null)
				FileUtils.deleteRecursive(avgTempDir);
		}
		
		// now aggregate parent section files into one excel file
		HSSFWorkbook wb = new HSSFWorkbook();
		for (int i=0; i<labels.size(); i++) {
			HSSFSheet sheet = wb.createSheet();
//			sheet.set
			wb.setSheetName(i, labels.get(i));
			CSVFile<String> csv = CSVFile.readFile(parentSectFiles.get(i), true);
			
			// header
			HSSFRow header = sheet.createRow(0);
			for (int col=0; col<csv.getNumCols(); col++)
				header.createCell(col).setCellValue(csv.get(0, col));
			
			for (int row=1; row<csv.getNumRows(); row++) {
				HSSFRow r = sheet.createRow(row);
				// first col is name
				r.createCell(0).setCellValue(csv.get(row, 0));
				for (int col=1; col<csv.getNumCols(); col++)
					r.createCell(col).setCellValue(Double.parseDouble(csv.get(row, col)));
			}
		}
		wb.setActiveSheet(0);
		
		FileOutputStream out = new FileOutputStream(new File(outputDir, "parent_section_probabilities.xls"));
		wb.write(out);
		out.close();
		
		// now aggregate main fault files into one excel file
		wb = new HSSFWorkbook();
		for (int i=0; i<labels.size(); i++) {
			HSSFSheet sheet = wb.createSheet();
//			sheet.set
			wb.setSheetName(i, labels.get(i));
			CSVFile<String> csv = CSVFile.readFile(mainFaultFiles.get(i), true);
			
			// header
			HSSFRow header = sheet.createRow(0);
			for (int col=0; col<csv.getNumCols(); col++)
				header.createCell(col).setCellValue(csv.get(0, col));
			
			for (int row=1; row<csv.getNumRows(); row++) {
				HSSFRow r = sheet.createRow(row);
				// first col is name
				r.createCell(0).setCellValue(csv.get(row, 0));
				for (int col=1; col<csv.getNumCols(); col++)
					r.createCell(col).setCellValue(Double.parseDouble(csv.get(row, col)));
			}
		}
		wb.setActiveSheet(0);
		
		out = new FileOutputStream(new File(outputDir, "main_fault_probabilities.xls"));
		wb.write(out);
		out.close();
	}
	
	private static void debugAvgMethods() throws IOException, DocumentException {
		debugAvgMethods(FaultSystemIO.loadSol(
				new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
						"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip")));
	}
	
	private static void debugAvgMethods(FaultSystemSolution sol) {
		// choose a subsection, lets do first subsection on Mojave as that lights up in the map
		// debugging: http://opensha.usc.edu/ftp/kmilner/ucerf3/TimeDependent_preview/m6.7_30yr/
		// 		OtherSensitivityTests/AveragingMethods/30.0yr_6.7+_AveRate_AveNormTS_vs_AveRI_AveTS.pdf
		int subSectIndex = -1;
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList()) {
			if (sect.getName().contains("Mojave")) {
				subSectIndex = sect.getSectionId();
				break;
			}
		}
		Preconditions.checkState(subSectIndex >= 0);
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(sol);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(30d);
		
		// TRUE: RI, NTS
		// FALSE: Rate, TS
		
		// first for AveRI&AveTS, the denominator
		erf.setParameter(BPTAveragingTypeParam.NAME, BPTAveragingTypeOptions.AVE_RI_AVE_TIME_SINCE);
		erf.updateForecast();
		double denomProb = calcSubSectSupraSeisMagProbDists(erf, 6.7d, 1, 0.1)[subSectIndex].getY(0);
		
		// start with a clean slate to be safe
		erf = new FaultSystemSolutionERF(sol);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(30d);
		
		// TRUE: RI, NTS
		// FALSE: Rate, TS
		
		// first for AveRate&AveNTS, the numerator
		erf.setParameter(BPTAveragingTypeParam.NAME, BPTAveragingTypeOptions.AVE_RATE_AVE_NORM_TIME_SINCE);
		erf.updateForecast();
		double numProb = calcSubSectSupraSeisMagProbDists(erf, 6.7d, 1, 0.1)[subSectIndex].getY(0);
		
		double probGain = numProb / denomProb;
		
		System.out.println("Subsection "+subSectIndex+" results: "+numProb+"/"+denomProb+" = "+probGain);
	}
	
	
	/**
	 * This generates test plots for three of the averaging methods
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public static void testAveragingMethodsForProbMaps() throws GMT_MapException, RuntimeException, IOException {
		double minMag = 6.7;
		int numMag = 4;
		double deltaMag = 0.5;
		
		String prefix = "aveMethodsMidAper";
		String dirName = "AveMethods_tests_MidAper";
		File saveDir = new File(dirName);
		if (!saveDir.exists())
			saveDir.mkdir();
		
		
		FaultSystemSolution meanSol=null;
		try {
			meanSol = FaultSystemIO.loadSol(
					new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
							"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double duration = 30d;
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		erf.getTimeSpan().setDuration(duration);

		
		FaultSystemSolution sol = erf.getSolution();
		
		// NoOpenInterval
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.setParameter(MagDependentAperiodicityParam.NAME, MagDependentAperiodicityOptions.MID_VALUES);
		erf.setParameter(HistoricOpenIntervalParam.NAME, 0d);
//		erf.setParameter(HistoricOpenIntervalParam.NAME, 2014d-1875d);

		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);

		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] aveRI_aveNTS_Funcs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] aveRI_aveNTS_AllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
	
		// Change calc type
		aveType = BPTAveragingTypeOptions.AVE_RI_AVE_TIME_SINCE;
		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] aveRI_aveTS_Funcs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] aveRI_aveTS_AllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);

		// Change calc type
		aveType = BPTAveragingTypeOptions.AVE_RATE_AVE_NORM_TIME_SINCE;
		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] aveRate_aveNTS_Funcs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] aveRate_aveNTS_AllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);

		// log space
		CPT diffCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-0.1, 0.1);
		CPT probCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-4, 0);
//		CPT ratioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-0.5, 0.5);
//		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT();
		CPT ratioCPT = getScaledLinearRatioCPT(0.02);
//		CPT ratioCPT = getScaledLinearRatioCPT(0.02, 0.8d, 1.2d);
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		prefix += (int)duration+"yr";
		
		for (int i=0; i<numMag+1; i++) {
			
			double[] aveRI_aveNTS_Vals;
			double[] aveRI_aveTS_Vals;
			double[] aveRate_aveNTS_Vals;
			String myPrefix;
			String magStr;
			if (i == numMag) {
				aveRI_aveNTS_Vals = extractYVals(aveRI_aveNTS_AllMags, 0);
				aveRI_aveTS_Vals =  extractYVals(aveRI_aveTS_AllMags, 0);
				aveRate_aveNTS_Vals = extractYVals(aveRate_aveNTS_AllMags, 0);
				myPrefix = prefix+"_supra_seis";
				magStr = "Supra Seis";
			} else {
				aveRI_aveNTS_Vals = extractYVals(aveRI_aveNTS_Funcs, i);
				aveRI_aveTS_Vals =  extractYVals(aveRI_aveTS_Funcs, i);
				aveRate_aveNTS_Vals = extractYVals(aveRate_aveNTS_Funcs, i);

				double mag = aveRI_aveNTS_Funcs[0].getX(i);
				myPrefix = prefix+"_"+(float)mag+"+";
				magStr = "M>="+(float)mag;
			}
			
			double[] aveRI_aveTS_over_aveRI_aveNTS_ratio = new double[aveRI_aveNTS_Vals.length];
			double[] aveRate_aveNTS_over_aveRI_aveNTS_ratio = new double[aveRI_aveNTS_Vals.length];
			double[] aveRI_aveTS_over_aveRI_aveNTS_diff = new double[aveRI_aveNTS_Vals.length];
			double[] aveRate_aveNTS_over_aveRI_aveNTS_diff = new double[aveRI_aveNTS_Vals.length];
			for (int j=0; j<aveRI_aveTS_over_aveRI_aveNTS_ratio.length; j++) {
				aveRI_aveTS_over_aveRI_aveNTS_ratio[j] = aveRI_aveTS_Vals[j]/aveRI_aveNTS_Vals[j];
				aveRate_aveNTS_over_aveRI_aveNTS_ratio[j] = aveRate_aveNTS_Vals[j]/aveRI_aveNTS_Vals[j];
				aveRI_aveTS_over_aveRI_aveNTS_diff[j] = aveRI_aveTS_Vals[j]-aveRI_aveNTS_Vals[j];
				aveRate_aveNTS_over_aveRI_aveNTS_diff[j] = aveRate_aveNTS_Vals[j]-aveRI_aveNTS_Vals[j];
			}
			
			// 
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(aveRI_aveNTS_Vals), region,
					saveDir, myPrefix+"_aveRI_aveNTS", false, true,
					"Log10("+(float)duration+" yr "+magStr+" aveRI_aveNTS)");
			// 
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(aveRI_aveTS_Vals), region,
					saveDir, myPrefix+"_aveRI_aveTS", false, true,
					"Log10("+(float)duration+" yr "+magStr+" aveRI_aveTS)");
			// 
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(aveRate_aveNTS_Vals), region,
					saveDir, myPrefix+"_aveRate_aveNTS", false, true,
					"Log10("+(float)duration+" yr "+magStr+" aveRate_aveNTS)");
			// 
			FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, aveRI_aveTS_over_aveRI_aveNTS_ratio, region,
					saveDir, myPrefix+"_aveRI_aveTS_over_aveRI_aveNTS_ratio", false, true,
					(float)duration+" yr "+magStr+" aveRI_aveTS_over_aveRI_aveNTS_ratio");
			// 
			FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, aveRate_aveNTS_over_aveRI_aveNTS_ratio, region,
					saveDir, myPrefix+"_aveRate_aveNTS_over_aveRI_aveNTS_ratio", false, true,
					(float)duration+" yr "+magStr+" aveRate_aveNTS_over_aveRI_aveNTS_ratio");
			FaultBasedMapGen.makeFaultPlot(diffCPT, faults, aveRI_aveTS_over_aveRI_aveNTS_diff, region,
					saveDir, myPrefix+"_aveRI_aveTS_minus_aveRI_aveNTS_diff", false, true,
					(float)duration+" yr "+magStr+" aveRI_aveTS_minus_aveRI_aveNTS_diff");
			// 
			FaultBasedMapGen.makeFaultPlot(diffCPT, faults, aveRate_aveNTS_over_aveRI_aveNTS_diff, region,
					saveDir, myPrefix+"_aveRate_aveNTS_minus_aveRI_aveNTS_diff", false, true,
					(float)duration+" yr "+magStr+" aveRate_aveNTS_minus_aveRI_aveNTS_diff");
			
		}
	}

	
	/**
	 * This generates test plots for historic open interval
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public static void testHistOpenIntervalFaultProbMaps() throws GMT_MapException, RuntimeException, IOException {
		double minMag = 6.7;
		int numMag = 4;
		double deltaMag = 0.5;
		
		String prefix = "openIntTest_MidAper";
		String dirName = "OpenInterval_tests_MidAper";
		File saveDir = new File(dirName);
		if (!saveDir.exists())
			saveDir.mkdir();
		
		
		FaultSystemSolution meanSol=null;
		try {
			meanSol = FaultSystemIO.loadSol(
					new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
							"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double duration = 30d;
		String durStr = (int)duration+"yr";
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		erf.getTimeSpan().setDuration(duration);

		
		FaultSystemSolution sol = erf.getSolution();
		
		// NoOpenInterval
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.setParameter(MagDependentAperiodicityParam.NAME, MagDependentAperiodicityOptions.MID_VALUES);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] noOpenIntFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] noOpenIntAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);
	
		// Open Since 1850
		erf.setParameter(HistoricOpenIntervalParam.NAME, 2014d-1850d);
		erf.updateForecast();
		
		EvenlyDiscretizedFunc[] openIntFuncs = calcSubSectSupraSeisMagProbDists(erf, minMag, numMag, deltaMag);
		EvenlyDiscretizedFunc[] openIntAllMags = calcSubSectSupraSeisMagProbDists(erf, 0d, 1, deltaMag);

		// log space
		CPT probCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-4, 0);
//		CPT ratioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-0.5, 0.5);
//		CPT ratioCPT = FaultBasedMapGen.getLinearRatioCPT();
		CPT ratioCPT = getScaledLinearRatioCPT(0.02);
		
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_COLLECTION();
		
		if (prefix == null)
			prefix = "";
		if (!prefix.isEmpty() && !prefix.endsWith("_"))
			prefix += "_";
		
		prefix += (int)duration+"yr";
		
		for (int i=0; i<numMag+1; i++) {
			
			double[] poissonVals;
			double[] noOpenIntVals;
			double[] openIntVals;
			String myPrefix;
			String magStr;
			if (i == numMag) {
				noOpenIntVals = extractYVals(noOpenIntAllMags, 0);
				openIntVals =  extractYVals(openIntAllMags, 0);
				myPrefix = prefix+"_supra_seis";
				magStr = "Supra Seis";
			} else {
				noOpenIntVals = extractYVals(noOpenIntFuncs, i);
				openIntVals =  extractYVals(openIntFuncs, 0);
				double mag = noOpenIntFuncs[0].getX(i);
				myPrefix = prefix+"_"+(float)mag+"+";
				magStr = "M>="+(float)mag;
			}
			
			double[] openIntOverNoOpenIntRatio = new double[noOpenIntVals.length];
			for (int j=0; j<openIntOverNoOpenIntRatio.length; j++) {
				openIntOverNoOpenIntRatio[j] = openIntVals[j]/noOpenIntVals[j];
			}
			
			// no open int
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(noOpenIntVals), region,
					saveDir, myPrefix+"_NoOpenInt", false, true,
					"Log10("+(float)duration+" yr "+magStr+" NoOpenInt)");
			// no open int
			FaultBasedMapGen.makeFaultPlot(probCPT, faults, FaultBasedMapGen.log10(openIntVals), region,
					saveDir, myPrefix+"_OpenInt", false, true,
					"Log10("+(float)duration+" yr "+magStr+" OpenInt)");
			// ratio
			FaultBasedMapGen.makeFaultPlot(ratioCPT, faults, openIntOverNoOpenIntRatio, region,
					saveDir, myPrefix+"_OpenIntOverNoOpenIntRatio", false, true,
					(float)duration+" yr "+magStr+" OpenIntOverNoOpenIntRatio");
			
			if(magStr.equals("M>=6.7")) {
				for(int s=0;s<openIntOverNoOpenIntRatio.length;s++) {
					System.out.println(s+"\t"+openIntOverNoOpenIntRatio[s]+"\t"+meanSol.getRupSet().getFaultSectionData(s).getName());
				}
			}
		}
	}

	
	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws RuntimeException 
	 * @throws GMT_MapException 
	 */
	public static void main(String[] args) throws IOException, DocumentException, GMT_MapException, RuntimeException {

//		writeDiffAveragingMethodsRupProbGains();
//		writeDiffAveragingMethodsSubSectionTimeDependenceCSV(null);
//		writeDiffAveragingMethodsRupProbGains(1837);	// Mojave section
//		writeDiffAveragingMethodsRupProbGains(1486);

		testAveragingMethodsForProbMaps();
//		testHistOpenIntervalFaultProbMaps();

//		makeWG02_FaultProbMaps();
		
//		testProbSumMethods();
		System.exit(0);
//		loadBranchFaultCSVVals(new File("/home/kevin/OpenSHA/UCERF3/probGains/"
//				+ "2013_12_03-ucerf3-prob-gains-main-30yr/aveRI_aveNTS.zip"), new int[] { 0, 1, 3 }, null);
//		System.exit(0);
//		debugAvgMethods();
//		System.exit(0);
		
//		String dirPrefix = "/home/kevin/OpenSHA/UCERF3/probGains/2013_12_14-ucerf3-prob-gains-open1875";
		String dirPrefix = "/home/kevin/OpenSHA/UCERF3/probGains/2013_12_17-ucerf3-prob-gains-open1875";
		// each individually
		// default
		writeTimeDepPlotsForWeb(Lists.newArrayList(BPTAveragingTypeOptions.AVE_RATE_AVE_NORM_TIME_SINCE), true,
				dirPrefix, new File("/home/kevin/OpenSHA/UCERF3/TimeDependent_AVE_RATE_AVE_NORM_TIME_SINCE"));
		writeTimeDepPlotsForWeb(Lists.newArrayList(BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE), true,
				dirPrefix, new File("/home/kevin/OpenSHA/UCERF3/TimeDependent_AVE_RI_AVE_NORM_TIME_SINCE"));
		writeTimeDepPlotsForWeb(Lists.newArrayList(BPTAveragingTypeOptions.AVE_RI_AVE_TIME_SINCE), true,
				dirPrefix, new File("/home/kevin/OpenSHA/UCERF3/TimeDependent_AVE_RI_AVE_TIME_SINCE"));
		// do all of them including avg sensitivity plots
		writeTimeDepPlotsForWeb(Lists.newArrayList(BPTAveragingTypeOptions.values()), false,
				dirPrefix, new File("/home/kevin/OpenSHA/UCERF3/TimeDependent_AVE_ALL"));
		System.exit(0);
		
//		File zipsDir = new File("/home/kevin/OpenSHA/UCERF3/probGains/2013_11_21-ucerf3-prob-gains-5yr");
//		String opsStr = "aveRI_aveNTS";
//		File branchOutput = new File(z ipsDir, opsStr);
//		if (!branchOutput.exists())
//			branchOutput.mkdir();
//		File zipFile = new File(zipsDir, opsStr+".zip");
		
//		Map<MagDependentAperiodicityOptions, Map<LogicTreeBranch, double[]>> vals =
//				loadBranchCSVVals(zipFile, 0, 4, true, null);
//		writeBranchAggregatedTimeDepFigs(zipFile, branchOutput, false, 0, null);
//		writeBranchAggregatedTimeDepFigs(zipFile, branchOutput, true, 0, null);
//		System.exit(0);

//		scratch.UCERF3.utils.RELM_RegionUtils.printNumberOfGridNodes();
		
//		writeDiffAveragingMethodsRupProbGains();
//		System.exit(0);
		
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
		
		double duration = 30d;
		String durStr = (int)duration+"yr";
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(meanSol);
		erf.getTimeSpan().setDuration(duration);
		
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
		
		MagDependentAperiodicityOptions[] covFuncs = { MagDependentAperiodicityOptions.LOW_VALUES,
				MagDependentAperiodicityOptions.MID_VALUES, MagDependentAperiodicityOptions.HIGH_VALUES,
				MagDependentAperiodicityOptions.ALL_PT3_VALUES, MagDependentAperiodicityOptions.ALL_PT4_VALUES,
				MagDependentAperiodicityOptions.ALL_PT5_VALUES };
		
		File saveDir = new File("/tmp/ucerf3_time_dep_erf_plots");
		if (!saveDir.exists())
			saveDir.mkdir();
		
//		writeDiffAveragingMethodsSubSectionTimeDependenceCSV(saveDir);
//		System.exit(0);
		
		for (MagDependentAperiodicityOptions cov : covFuncs) {
			String dirName = "cov_"+cov.name();
			File covSaveDir = new File(saveDir, dirName);
			if (!covSaveDir.exists())
				covSaveDir.mkdir();
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
			erf.setParameter(MagDependentAperiodicityParam.NAME, cov);
//			// this will write the parent section CSV file
//			writeParentSectionTimeDependenceCSV(erf, new File(covSaveDir, dirName+"_parent_probs_"+durStr+".csv"));
//			writeSubSectionTimeDependenceCSV(erf, new File(covSaveDir, dirName+"_sub_probs_"+durStr+".csv"));
//			
//			File fltGainDir = new File(covSaveDir, "fault_prob_gains");
//			if (!fltGainDir.exists())
//				fltGainDir.mkdir();
//			
//			makeFaultProbGainMaps(erf, fltGainDir, dirName);

			// this will generate prob gain maps for BPT parameters
			File bptGainDir = new File(covSaveDir, "bpt_calc_prob_gains");
			if (!bptGainDir.exists())
				bptGainDir.mkdir();
			makeAvgMethodProbGainMaps(erf, bptGainDir, dirName);
		}
		
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
