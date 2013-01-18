package scratch.peter.ucerf3.calc;

import static org.opensha.nshmp.NEHRP_TestCity.*;
import static org.opensha.nshmp2.util.Period.*;
import static scratch.peter.curves.ProbOfExceed.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.Site;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.bugReports.BugReport;
import org.opensha.commons.util.bugReports.BugReportDialog;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardCalc;
import org.opensha.nshmp2.calc.HazardResult;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.SiteTypeParam;
import org.opensha.nshmp2.util.SourceIMR;
import org.opensha.sha.calc.disaggregation.DisaggregationCalculator;
import org.opensha.sha.calc.params.IncludeMagDistFilterParam;
import org.opensha.sha.calc.params.MagDistCutoffParam;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.calc.params.NonSupportedTRT_OptionsParam;
import org.opensha.sha.calc.params.PtSrcDistanceCorrectionParam;
import org.opensha.sha.calc.params.SetTRTinIMR_FromSourceParam;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.faultSurface.utils.PtSrcDistCorr;
import org.opensha.sha.gui.infoTools.DisaggregationPlotViewerWindow;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.peter.curves.ProbOfExceed;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_DeaggWrapper {

	static final String COMP_SOL = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2012_10_29-tree-fm31_x7-fm32_x1_COMPOUND_SOL.zip";
	static final String SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";
	static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/RTGM/deaggTmp";
	static final String S = File.separator;

	// updateForecast should have been called by here
	UC3_DeaggWrapper(AbstractERF erf, String outDir, Map<String, Location> locMap,
		String id, Period[] periods, boolean epiUncert, ProbOfExceed[] PEs) {

//		LocationList locs = new LocationList();
		
		for (String locName : locMap.keySet()) {
			Location loc = locMap.get(locName);
			System.out.println("Starting site: " + loc);
		
			for (Period period : periods) {
				
				// init site
				Site s = new Site(loc);
				// initSite(s); this is taken care of when site is passed into
				// hazard calc below
	
				// hazard calc
				EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
				HazardCalc hc = HazardCalc.create(wrappedERF, s, period, epiUncert);
				HazardResult hr = hc.call();
	//			System.out.println(hr.curve());
	//			double iml = hr.curve().getFirstInterpolatedX_inLogXLogYDomain(0.0202);
					
				for (ProbOfExceed pe : PEs) {
					
					String outPath = outDir + S + locName + S + "SHA-" + pe + 
							"-" + period + "-" + id + S;
	
					double iml = ProbOfExceed.get(hr.curve(), pe);
					System.out.println("IML: " + iml);
					
					DisaggregationCalculator deagg = new DisaggregationCalculator();
					deagg.setNumSourcestoShow(100);
					deagg.setShowDistances(true);
					
	//				ScalarIMR imr = AttenRelRef.CB_2008.instance(null);
	//				imr.setParamDefaults();
	//				imr.setIntensityMeasure((period == GM0P00) ? PGA_Param.NAME : SA_Param.NAME);
					ScalarIMR imr = SourceIMR.WUS_FAULT.instance(period);
					imr.getParameter(NSHMP08_WUS.IMR_UNCERT_PARAM_NAME).setValue(
						epiUncert);
	
					deagg.disaggregate(Math.log(iml), s, imr, erf, deaggParams());
					
					showDisaggregationResults(deagg, 100, true, iml, 0.02, outPath);
				}
			}
		}
	}
	
	
	private ParameterList deaggParams() {
		ParameterList pList = new ParameterList();
		
		MaxDistanceParam maxDistParam = new MaxDistanceParam();
		pList.addParameter(maxDistParam);
		
		MagDistCutoffParam magDistCutoffParam = new MagDistCutoffParam();
		pList.addParameter(magDistCutoffParam);
		
		PtSrcDistanceCorrectionParam ptSrcParam = new PtSrcDistanceCorrectionParam();
		ptSrcParam.setValueFromTypePtSrcDistCorr(PtSrcDistCorr.Type.NSHMP08);
		pList.addParameter(ptSrcParam);
		
		IncludeMagDistFilterParam magDistParam = new IncludeMagDistFilterParam();
		pList.addParameter(magDistParam);
		
		SetTRTinIMR_FromSourceParam setTrtParam = new SetTRTinIMR_FromSourceParam();
		pList.addParameter(setTrtParam);
		
		NonSupportedTRT_OptionsParam notSupParam = new NonSupportedTRT_OptionsParam();
		pList.addParameter(notSupParam);
		
		return pList;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		// SONGS
		Location loc = new Location(33.4, -117.55);
		
		Period[] periods = { GM0P00 }; //, GM0P20, GM1P00 };
		ProbOfExceed[] PEs = { PE2IN50 }; //, PE10IN50};
		String solSetPath = SOL_PATH;
		int idx = 0;
		boolean epiUnc = false;

//		String sitePath = "/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/AFsites.txt";
//		Map<String,Location> siteMap = UC3_CalcDriver.readSiteFile(sitePath);

//		String outPath = OUT_DIR + "/SONGS/UC3FM3P1/";
//		AbstractERF erf = getUC3_ERF(solSetPath, idx);

//		String outPath = OUT_DIR + "/SONGS/UC3FM3P1/";
//		String branchID = "FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3";
//		AbstractERF erf = erfFromBranch(branchID); // excludes bg seis

//		String outPath = OUT_DIR + "/TMP/UC3FM3P2/";
//		String branchID = "FM3_2_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3";
//		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(
//			COMP_SOL, branchID, IncludeBackgroundOption.EXCLUDE,
//			false, true, 1.0);
//		erf.updateForecast();
		

		String outPath = OUT_DIR + "/TMP/UC3FM3P1/";
		String sitePath = "/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/AFsites.txt";
		Map<String,Location> siteMap = UC3_CalcUtils.readSiteFile(sitePath);
		for (int i=0; i<100; i++) {
			UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(
				solSetPath, i, IncludeBackgroundOption.EXCLUDE,
				false, true, 1.0);
			erf.updateForecast();
//			try {
			new UC3_DeaggWrapper(erf, outPath, siteMap, Integer.toString(i),
				periods, epiUnc, PEs);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

		}

//		String outPath = OUT_DIR + "/SONGS/NSHMP_CA/";
//		AbstractERF erf = NSHMP2008.createCalifornia();
//		erf.updateForeecast();
		
//		try {
//			new UC3_DeaggWrapper(erf, outPath, loc, periods, epiUnc, PEs);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	
//	private void initSite(Site s) {
//		
//		// CY AS
//		DepthTo1pt0kmPerSecParam d10p = new DepthTo1pt0kmPerSecParam(null,
//			0, 1000, true);
//		d10p.setValueAsDefault();
//		s.addParameter(d10p);
//		// CB
//		DepthTo2pt5kmPerSecParam d25p = new DepthTo2pt5kmPerSecParam(null,
//			0, 1000, true);
//		d25p.setValueAsDefault();
//		s.addParameter(d25p);
//		// all
//		Vs30_Param vs30p = new Vs30_Param(760);
//		vs30p.setValueAsDefault();
//		s.addParameter(vs30p);
//		// AS CY
//		Vs30_TypeParam vs30tp = new Vs30_TypeParam();
//		vs30tp.setValueAsDefault();
//		s.addParameter(vs30tp);
//		
//		// CEUS only (TODO imrs need to be changed to accept vs value)
//		SiteTypeParam siteTypeParam = new SiteTypeParam();
//		s.addParameter(siteTypeParam);
//	}
	
	private static void showDisaggregationResults(
			DisaggregationCalculator disaggCalc,
			int numSourceToShow,
			boolean imlBasedDisaggr, double imlVal, double probVal,
			String dlDirPath) {
		// String sourceDisaggregationListAsHTML = null;
		disaggCalc.setMaxZAxisForPlot(Double.NaN);
		String sourceDisaggregationList = disaggCalc.getDisaggregationSourceInfo();
//		if (numSourceToShow > 0) {
//			sourceDisaggregationList = 
//			File srcListFile = new File(dlDir, "sources.txt");
//			Files.write(sourceDisaggregationList, srcListFile, Charsets.US_ASCII);
//		}
//		System.out.println("DEAGG list: \n" + sourceDisaggregationList);
		String binData = null;
//		boolean binDataToShow = disaggregationControlPanel.isShowDisaggrBinDataSelected();
//		if (binDataToShow) {
//			try {
//				binData = disaggCalc.getBinData();
//				// binDataAsHTML = binDataAsHTML.replaceAll("\n", "<br>");
//				// binDataAsHTML = binDataAsHTML.replaceAll("\t",
//				// "&nbsp;&nbsp;&nbsp;");
//			} catch (RuntimeException ex) {
//				setButtonsEnable(true);
//				ex.printStackTrace();
//				BugReport bug = new BugReport(ex, getParametersInfoAsString(), appShortName, getAppVersion(), this);
//				BugReportDialog bugDialog = new BugReportDialog(this, bug, false);
//				bugDialog.setVisible(true);
//			}
//		}
		String modeString = "";
		if (imlBasedDisaggr)
			modeString = "Disaggregation Results for IML = " + imlVal
			+ " (for Prob = " + (float) probVal + ")";
		else
			modeString = "Disaggregation Results for Prob = " + probVal
			+ " (for IML = " + (float) imlVal + ")";
		modeString += "\n" + disaggCalc.getMeanAndModeInfo();

		String url = null;
//		String metadata;
		// String pdfImageLink;
		try {
			url = disaggCalc.getDisaggregationPlotUsingServlet("no param string");
			/*
			 * pdfImageLink = "<br>Click  " + "<a href=\"" +
			 * disaggregationPlotWebAddr +
			 * DisaggregationCalculator.DISAGGREGATION_PLOT_PDF_NAME + "\">" +
			 * "here" + "</a>" +
			 * " to view a PDF (non-pixelated) version of the image (this will be deleted at midnight)."
			 * ;
			 */

//			metadata = getMapParametersInfoAsHTML();
//			metadata += "<br><br>Click  " + "<a href=\""
//			+ disaggregationPlotWebAddr + "\">" + "here" + "</a>"
//			+ " to download files. They will be deleted at midnight";
			File dlDir = new File(dlDirPath);
			dlDir.mkdirs();
			
			File zipFile = new File(dlDir, "allFiles.zip");
			Files.createParentDirs(zipFile);
			// construct zip URL
			String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
			FileUtils.downloadURL(zipURL, zipFile);
			FileUtils.unzipFile(zipFile, dlDir);

			File srcListFile = new File(dlDir, "sources.txt");
			System.out.println();
			Files.write(sourceDisaggregationList, srcListFile, Charsets.US_ASCII);
			
		} catch (Exception e) {
			e.printStackTrace();
//			JOptionPane.showMessageDialog(this, e.getMessage(),
//					"Server Problem", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// adding the image to the Panel and returning that to the applet
		// new DisaggregationPlotViewerWindow(imgName,true,modeString,
		// metadata,binData,sourceDisaggregationList);

		new DisaggregationPlotViewerWindow(url
				+ DisaggregationCalculator.DISAGGREGATION_PLOT_PDF_NAME, true,
				modeString, url, binData, sourceDisaggregationList);
	}


}
