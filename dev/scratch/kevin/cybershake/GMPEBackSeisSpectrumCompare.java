package scratch.kevin.cybershake;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.calc.mcer.DeterministicResult;
import org.opensha.sha.cybershake.calc.mcer.GMPEDeterministicComparisonCalc;
import org.opensha.sha.cybershake.calc.mcer.MCERDataProductsCalc;
import org.opensha.sha.cybershake.calc.mcer.RTGMCalc;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.MultiIMR_Averaged_AttenRel;
import org.opensha.sha.imr.attenRelImpl.NGAWest_2014_Averaged_AttenRel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class GMPEBackSeisSpectrumCompare {

	public static void main(String[] args) throws DocumentException, InvocationTargetException, IOException {
		boolean ddwCorr = false;
		File outputDir = new File("/home/kevin/CyberShake/MCER/gmpe_back_seis_compare");
		
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
//		AttenuationRelationship meanGMPE = AttenRelRef.BSSA_2014.instance(null);
//		AttenuationRelationship meanGMPE = new NGAWest_2014_Averaged_AttenRel(null, false);
		
		List<Integer> runIDs = Lists.newArrayList(
				2657, 3037, 2722, 3022, 3030, 3027, 2636,
				2638, 2660, 2703, 3504, 2988, 2965, 3007);
		
//				2636);

		ERF backSeisERF = MeanUCERF2_ToDB.createUCERF2ERF();
		backSeisERF.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		backSeisERF.setParameter(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME, ddwCorr);
		backSeisERF.updateForecast();
		ERF noBackSeisERF = MeanUCERF2_ToDB.createUCERF2ERF();
		noBackSeisERF.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		noBackSeisERF.setParameter(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME, ddwCorr);
		noBackSeisERF.updateForecast();
		List<AttenuationRelationship> attenRels = Lists.newArrayList();

		String attenFiles = "src/org/opensha/sha/cybershake/conf/cb2014.xml,src/org/opensha/sha/cybershake/conf/cy2014.xml"
				+ ",src/org/opensha/sha/cybershake/conf/bssa2014.xml,src/org/opensha/sha/cybershake/conf/ask2014.xml";
		for (String attenRelFile : HazardCurvePlotter.commaSplit(attenFiles)) {
			AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
			attenRels.add(attenRel);
		}
		Preconditions.checkArgument(!attenRels.isEmpty(), "Must specify at least 1 GMPE");
		MultiIMR_Averaged_AttenRel meanGMPE = new MultiIMR_Averaged_AttenRel(attenRels);
		
		meanGMPE.setParamDefaults();
		List<AttenuationRelationship> meanGMPEList = Lists.newArrayList();
		meanGMPEList.add(meanGMPE);

		List<Double> periods = Lists.newArrayList(0.01,0.02,0.03,0.05,0.075,0.1,0.15,0.2,0.25,0.3,0.4,
				0.5,0.75,1.0,1.5,2.0,3.0,4.0,5.0,7.5,10.0);
//		List<Double> periods = Lists.newArrayList(2.0,3.0,4.0,5.0,7.5,10.0);
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		Runs2DB runs2db = new Runs2DB(db);
		CybershakeSiteInfo2DB sites2db = new CybershakeSiteInfo2DB(db);
		CyberShakeComponent comp = CyberShakeComponent.RotD100;
		
		List<CybershakeIM> forceAddIMs = Lists.newArrayList();
		for (Double period : periods)
			if (period < 2d || period == 6d)
				forceAddIMs.add(new CybershakeIM(-1, IMType.SA, period, null, comp));
		
		for (int runID : runIDs) {
			System.out.println("**************** RUN ID: "+runID);
			
			CybershakeRun run = runs2db.getRun(runID);
			CybershakeSite site = sites2db.getSiteFromDB(run.getSiteID());
			
			int velModelID = run.getVelModelID();
			OrderedSiteDataProviderList providers = HazardCurvePlotter.createProviders(velModelID);
			List<SiteDataValue<?>> origSiteDatas = providers.getBestAvailableData(site.createLocation());
			
			DiscretizedFunc backSeisFunc = null;
			DiscretizedFunc noBackSeisFunc = null;
			
			for (boolean backSeis : new boolean[] { true, false }) {
				for (AttenuationRelationship attenRel : attenRels)
					attenRel.setParamDefaults();
				
				RTGMCalc rtgmCalc = new RTGMCalc(runID, comp, null, db);
				rtgmCalc.setSiteDatas(origSiteDatas);
				if (backSeis)
					rtgmCalc.setGMPEs(backSeisERF, meanGMPEList);
				else
					rtgmCalc.setGMPEs(noBackSeisERF, meanGMPEList);
				rtgmCalc.setForceAddIMs(forceAddIMs);
				Preconditions.checkState(rtgmCalc.calc());

				DiscretizedFunc probFunc = RTGMCalc.saToPsuedoVel(rtgmCalc.getGMPESpectrumMap().get(comp).get(0));
				if (backSeis)
					backSeisFunc = probFunc;
				else
					noBackSeisFunc = probFunc;
			}
			backSeisFunc.setName("Including Background");
			noBackSeisFunc.setName("Excluding Background");
			List<DiscretizedFunc> funcs = Lists.newArrayList();
			funcs.add(backSeisFunc);
			funcs.add(noBackSeisFunc);
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
			
			PlotSpec spec = new PlotSpec(funcs, chars, site.short_name, "Period (s)", "RTGM PSV (cm/sec)");
			spec.setLegendVisible(true);
			
			String name = site.short_name+"_run"+run.getRunID()+"_GMPE_BackSeisCompare_"+comp.getShortName();
			
			File outputFile = new File(outputDir, name);
			System.out.println("**************** WRITING RESULTS TO "+outputFile.getAbsolutePath());
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			gp.setBackgroundColor(Color.WHITE);
			
			gp.drawGraphPanel(spec, true, true);
//			gp.drawGraphPanel(spec);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPNG(outputFile.getAbsolutePath()+".png");
			gp.saveAsPNG(outputFile.getAbsolutePath()+".pdf");
		}
		
		System.exit(0);
	}

}
