package scratch.kevin.cybershake;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.CVMHBasinDepth;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.hazardMap.components.AsciiFileCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CalculationInputsXMLFile;
import org.opensha.sha.calc.hazardMap.components.CalculationSettings;
import org.opensha.sha.calc.hazardMap.components.CurveResultsArchiver;
import org.opensha.sha.calc.hazardMap.mpj.MPJHazardCurveDriver;
import org.opensha.sha.calc.hazus.parallel.HardCodedTest;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.TectonicRegionType;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CyberShakeBaseMapGen {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 9) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(CyberShakeBaseMapGen.class)
					+" <IMRs> <SA period> <spacing> <CVM4/CVMH> <constrainBasinMin> <jobName> <minutes> <nodes> <queue>");
			System.exit(2);
		}
		
		// TODO args
		String imrNames = args[0];
		double period = Double.parseDouble(args[1]);
		double spacing = Double.parseDouble(args[2]);
		String cvmName = args[3];
		boolean constrainBasinMin = Boolean.parseBoolean(args[4]);
		String jobName = args[5];
		int mins = Integer.parseInt(args[6]);
		int nodes = Integer.parseInt(args[7]);
		String queue = args[8];
		if (queue.toLowerCase().equals("null"))
			queue = null;
		
		File hazMapsDir = new File("/home/scec-02/kmilner/hazMaps");
		
		File jobDir = new File(hazMapsDir, jobName);
		if (!jobDir.exists())
			jobDir.mkdir();
		
		ArrayList<ScalarIMR> imrs = Lists.newArrayList();
		for (String imrName : Splitter.on(",").split(imrNames)) {
			ScalarIMR imr = AttenRelRef.valueOf(imrName).instance(null);
			imr.setParamDefaults();
			imr.setIntensityMeasure(SA_Param.NAME);
			SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), period);
			imrs.add(imr);
		}
		
		ArrayList<SiteData<?>> provs = Lists.newArrayList();
		provs.add(new WillsMap2006());
		boolean nullBasin = false;
		if (cvmName.toLowerCase().equals("cvm4")) {
			provs.add(new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_2_5));
			provs.add(new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_1_0));
		} else if (cvmName.toLowerCase().equals("cvmh")) {
			provs.add(new CVMHBasinDepth(SiteData.TYPE_DEPTH_TO_2_5));
			provs.add(new CVMHBasinDepth(SiteData.TYPE_DEPTH_TO_1_0));
		} else if (cvmName.toLowerCase().equals("null")){
			nullBasin = true;
		} else {
			System.err.println("Unknown basin model: "+cvmName);
		}
		
		GriddedRegion region = new CaliforniaRegions.CYBERSHAKE_MAP_GRIDDED(spacing);
		ArrayList<Site> sites = HardCodedTest.loadSites(provs, region.getNodeList(), imrs, nullBasin, constrainBasinMin, null);
		
		ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		
		ArbitrarilyDiscretizedFunc xValues = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
		double maxSourceDistance = 200;
		
		CalculationSettings calcSettings = new CalculationSettings(xValues, maxSourceDistance);
		
		File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;
		File svnDir = new File(hazMapsDir, "svn");
		File distDir = new File(svnDir, "dist");
		File libDir = new File(svnDir, "lib");
		File jarFile = new File(distDir, "OpenSHA_complete.jar");
		
		ArrayList<File> classpath = new ArrayList<File>();
		classpath.add(jarFile);
		classpath.add(new File(libDir, "commons-cli-1.2.jar"));
		
		MPJShellScriptWriter mpj = new MPJShellScriptWriter(javaBin, 7000, classpath,
				USC_HPCC_ScriptWriter.MPJ_HOME, false);
		
		for (ScalarIMR imr : imrs) {
			List<HashMap<TectonicRegionType, ScalarIMR>> imrMaps = Lists.newArrayList();
			
			HashMap<TectonicRegionType, ScalarIMR> map = Maps.newHashMap();
			map.put(TectonicRegionType.ACTIVE_SHALLOW, imr);
			imrMaps.add(map);
			
			File imrDir = new File(jobDir, imr.getShortName());
			if (!imrDir.exists())
				imrDir.mkdir();
			
			String curveDir = new File(imrDir, "curves").getAbsolutePath()+File.separator;
			CurveResultsArchiver archiver = new AsciiFileCurveArchiver(curveDir, true, false);
			
			CalculationInputsXMLFile inputs = new CalculationInputsXMLFile(erf, imrMaps, sites, calcSettings, archiver);
			
			File inputsFile = new File(imrDir, "inputs.xml");
			XMLUtils.writeObjectToXMLAsRoot(inputs, inputsFile);
			
			String cliArgs = inputsFile.getAbsolutePath();
			
			List<String> script = mpj.buildScript(MPJHazardCurveDriver.class.getName(), cliArgs);
			USC_HPCC_ScriptWriter writer = new USC_HPCC_ScriptWriter();
			
			script = writer.buildScript(script, mins, nodes, 0, queue);
			
			File pbsFile = new File(imrDir, imr.getShortName().toLowerCase()+".pbs");
			JavaShellScriptWriter.writeScript(pbsFile, script);
		}
	}

}