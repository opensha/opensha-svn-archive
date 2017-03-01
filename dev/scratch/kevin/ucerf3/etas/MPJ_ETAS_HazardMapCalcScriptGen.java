package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJExpressShellScriptWriter;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;

import com.google.common.base.Preconditions;

import scratch.kevin.ucerf3.MPJ_UCERF3_ShakeMapPrecalcScriptGen;

public class MPJ_ETAS_HazardMapCalcScriptGen {
	
	public static final DateFormat df = new SimpleDateFormat("yyyy_MM_dd");

	public static void main(String[] args) throws IOException {
		File localMainDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard");
		File remoteMainDir = new File("/home/scec-02/kmilner/ucerf3/etas_hazard");
		
		File remoteShakemapDir = new File("/home/scec-02/kmilner/ucerf3/shakemap_precalc");
		
		File remoteETASDir = new File("/home/scec-00/kmilner/ucerf3_etas_results_stampede/");
		File remoteFSSFile = new File("/home/scec-02/kmilner/ucerf3/inversion_compound_plots/2013_05_10-ucerf3p3-production-10runs/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
		
		String etasSimName = "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k";
		String etasFileName = "results_descendents_m5_preserve.bin";
		String etasShortName = "mojave_m7_fulltd_descendents";
		File remoteEtasCatalogFile = new File(new File(remoteETASDir, etasSimName), etasFileName);
		
		// --------------------
		// for fault precalc
//		String shakemapRunName = "2017_02_23-NGAWest_2014_NoIdr-spacing0.05-site-effects-with-basin";
//		String gmpeFileName = "NGAWest_2014_NoIdr.xml";
//		boolean siteEffects = true;
//		boolean basinDepth = true;
//		AttenuationRelationship gmpe = null;
		// for faults on the fly
		String shakemapRunName = null;
		AttenuationRelationship gmpe = AttenRelRef.NGAWest_2014_AVG_NOIDRISS.instance(null);
		gmpe.setParamDefaults();
		String gmpeFileName = gmpe.getShortName()+".xml";
		boolean siteEffects = true;
		boolean basinDepth = true;
		// --------------------
		String[] imts = { "pgv", "pga" };
		double[] periods = { Double.NaN, Double.NaN };
		double spacing = 0.02;
		String shakemapShortName = "NGA2-"+(float)spacing;
		if (siteEffects) {
			shakemapShortName += "-site-effects";
			if (basinDepth)
				shakemapShortName += "-with-basin";
			else
				shakemapShortName += "-no-basin";
		} else {
			shakemapShortName += "-no-site-effects";
		}
		
		double griddedSpacing = 0.01;
		
		String dateStr = df.format(new Date());
		String jobName = dateStr+"-"+etasShortName+"-"+shakemapShortName;
		
		int threads = 20;
		String queue = "scec";
		
		int nodes = 34;
		int hours = 24;
		
		int minDispatch = threads;
		if (minDispatch < MPJTaskCalculator.MIN_DISPATCH_DEFAULT)
			minDispatch = MPJTaskCalculator.MIN_DISPATCH_DEFAULT;
		int maxDispatch = MPJTaskCalculator.MAX_DISPATCH_DEFAULT;
		if (spacing <= 0.05)
			maxDispatch = 1000;
		
		File localDir = new File(localMainDir, jobName);
		Preconditions.checkState(localDir.exists() || localDir.mkdir());
		
		System.out.println("Job name: "+jobName);
		System.out.println("Dir: "+localDir.getAbsolutePath());
		
		JavaShellScriptWriter mpjWrite;
		BatchScriptWriter pbsWrite;
		
		boolean stampede = false;
		int memGigs;
		int ppn;
		if (stampede) {
			memGigs = 26;
			ppn = 16;
			mpjWrite = new FastMPJShellScriptWriter(StampedeScriptWriter.JAVA_BIN, memGigs*1024,
					null, StampedeScriptWriter.FMPJ_HOME);
			((FastMPJShellScriptWriter)mpjWrite).setUseLaunchWrapper(true);
			pbsWrite = new StampedeScriptWriter();
		} else {
			if (queue == null) {
				memGigs = 9;
				ppn = 8;
			} else {
				memGigs = 60;
				ppn = 20;
			}
			boolean fmpj = nodes < 25;
			fmpj = false;
			if (fmpj) {
				mpjWrite = new FastMPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
						null, USC_HPCC_ScriptWriter.FMPJ_HOME);
				((FastMPJShellScriptWriter)mpjWrite).setUseLaunchWrapper(true);
			} else {
				mpjWrite = new MPJExpressShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
						null, USC_HPCC_ScriptWriter.MPJ_HOME);
			}
			pbsWrite = new USC_HPCC_ScriptWriter();
		}
		File remoteJobDir = new File(remoteMainDir, jobName);
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteMainDir, "commons-cli-1.2.jar"));
		classpath.add(new File(remoteJobDir, "OpenSHA_complete.jar"));
		mpjWrite.setClasspath(classpath);
		
		File sitesFile, gmpeFile;
		File remoteShakemapRunDir = null;
		if (shakemapRunName == null) {
			// calculating on the fly
			ArrayList<SiteData<?>> siteData = null;
			if (siteEffects)
				siteData = MPJ_UCERF3_ShakeMapPrecalcScriptGen.getSiteDataProviders(basinDepth);
			File localSitesFile = new File(localDir, "sites.xml");
			GriddedRegion reg = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
			MPJ_UCERF3_ShakeMapPrecalcScriptGen.writeSitesXML(reg, siteData, gmpe, localSitesFile);
			File localGMPEFile = new File(localDir, gmpeFileName);
			MPJ_UCERF3_ShakeMapPrecalcScriptGen.writeGMPEFile(gmpe, localGMPEFile);
			sitesFile = new File(remoteJobDir, localSitesFile.getName());
			gmpeFile = new File(remoteJobDir, gmpeFileName);
		} else {
			remoteShakemapRunDir = new File(remoteShakemapDir, shakemapRunName);
			sitesFile = new File(remoteShakemapRunDir, "sites.xml");
			gmpeFile = new File(remoteShakemapRunDir, gmpeFileName);
		}
		
		for (int i=0; i<imts.length; i++) {
			String imt = imts[i];
			double period = periods[i];
			String imtName = imt;
			if (!Double.isNaN(period) && period > 0)
				imtName += "_"+(float)period+"s";
			
			String argz = "--min-dispatch "+minDispatch+" --max-dispatch "+maxDispatch;
			if (threads > 0)
				argz += " --threads "+threads;
			argz += " --catalogs "+remoteEtasCatalogFile.getAbsolutePath();
			if (remoteShakemapRunDir == null) {
				// calc on the fly
				argz += " --solution-file "+remoteFSSFile.getAbsolutePath();
			} else {
				// use precalc
				File remoteShakemapFile = new File(remoteShakemapRunDir, "results_"+imtName+".bin");
				argz += " --fault-data-file "+remoteShakemapFile.getAbsolutePath();
			}
			argz += " --spacing "+(float)spacing;
			argz += " --gmpe-file "+gmpeFile.getAbsolutePath();
			argz += " --sites-file "+sitesFile.getAbsolutePath();
			argz += " --imt "+imt;
			if (!Double.isNaN(period) && period > 0)
				argz += " --period"+(float)period;
			argz += " --gridded-spacing "+(float)griddedSpacing;
			argz += " --output-dir "+remoteJobDir.getAbsolutePath();
			
			List<String> script = mpjWrite.buildScript(MPJ_ETAS_HazardMapCalc.class.getName(), argz);
			
			int mins = hours*60;
			script = pbsWrite.buildScript(script, mins, nodes, ppn, queue);
			pbsWrite.writeScript(new File(localDir, imt+".pbs"), script);
		}
	}

}
