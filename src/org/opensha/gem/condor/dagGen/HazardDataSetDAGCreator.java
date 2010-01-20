package org.opensha.gem.condor.dagGen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.gridComputing.condor.DAG;
import org.opensha.commons.gridComputing.condor.SubmitScriptForDAG;
import org.opensha.commons.gridComputing.condor.SubmitScript.Universe;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.RunScript;
import org.opensha.commons.util.XMLUtils;
import org.opensha.gem.condor.calc.HazardCurveDriver;
import org.opensha.gem.condor.calc.components.AsciiFileCurveArchiver;
import org.opensha.gem.condor.calc.components.CalculationInputsXMLFile;
import org.opensha.gem.condor.calc.components.CalculationSettings;
import org.opensha.gem.condor.calc.components.CurveResultsArchiver;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.util.TectonicRegionType;

public class HazardDataSetDAGCreator {

	public static final String ERF_SERIALIZED_FILE_NAME = "erf.obj";

	private EqkRupForecastAPI erf;
	private List<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps;
	private List<Site> sites;
	private CalculationSettings calcSettings;
	private CurveResultsArchiver archiver;
	private String javaExec;
	private String jarFile;

	private DecimalFormat curveIndexFormat;

	private Universe universe = Universe.VANILLA;
	
	public static int DAGMAN_MAX_IDLE = 50;
	public static int DAGMAN_MAX_PRE = 3;
	public static int DAGMAN_MAX_POST = 5;

	public HazardDataSetDAGCreator(CalculationInputsXMLFile inputs, String javaExec, String jarFile) {
		this(inputs.getERF(), inputs.getIMRMaps(), inputs.getSites(), inputs.getCalcSettings(),
				inputs.getArchiver(), javaExec, jarFile);
	}

	public HazardDataSetDAGCreator(EqkRupForecastAPI erf,
			List<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps,
			List<Site> sites,
			CalculationSettings calcSettings,
			CurveResultsArchiver archiver,
			String javaExec,
			String jarFile) {
		this.erf = erf;
		this.imrMaps = imrMaps;
		this.sites = sites;
		this.calcSettings = calcSettings;
		this.archiver = archiver;
		String fstr = "";
		for (int i=0; i<(sites.size() + "").length(); i++)
			fstr += "0";
		curveIndexFormat = new DecimalFormat(fstr);
		this.javaExec = javaExec;
		this.jarFile = jarFile;
	}

	public void writeDAG(File outputDir, int curvesPerJob, boolean run) throws IOException {
		if (curvesPerJob < 1)
			throw new IllegalArgumentException("curvesPerJob must be >= 1");
		// create the output dir
		if (!outputDir.exists()) {
			if (!outputDir.mkdir())
				throw new IOException("Output directory '" + outputDir.getPath() + "' does not exist" +
				" and could not be created.");
		}
		String odir = outputDir.getAbsolutePath();
		if (!odir.endsWith(File.separator))
			odir += File.separator;

		String serializedERFFile = null;
		if (calcSettings.isSerializeERF())
			serializedERFFile = serializeERF(odir);

		int numSites = sites.size();

		DAG dag = new DAG();

		new File(odir + "log").mkdir();
		new File(odir + "out").mkdir();
		new File(odir + "err").mkdir();

		for (int startIndex=0; startIndex<numSites; startIndex+=curvesPerJob) {
			int endIndex = startIndex + curvesPerJob - 1;
			if (endIndex > numSites - 1)
				endIndex = numSites - 1;

			String jobName = "Curves_" + curveIndexFormat.format(startIndex) + "_" + curveIndexFormat.format(endIndex);
			String executable = javaExec;
			String xmlFile = writeCurveJobXML(odir, startIndex, endIndex, jobName, serializedERFFile);
			String arguments = " -classpath " + jarFile + " " + HazardCurveDriver.class.getName() + " " + xmlFile;
			SubmitScriptForDAG job = new SubmitScriptForDAG(jobName, executable, arguments,
					"/tmp", universe, true);

			job.writeScriptInDir(odir);
			job.setComment("Calculates curves " + startIndex + "->" + endIndex + ", inclusive");

			dag.addJob(job);
		}

		String dagFileName = odir + "main.dag";

		dag.writeDag(dagFileName);
		
		createSubmitDAGScript(odir, run);
	}

	private String writeCurveJobXML(String odir, int startIndex, int endIndex, String jobName,
			String serializedERFFile) throws IOException {
		String fileName = odir + jobName + "_input.xml";

		// get subset of sites for job
		List<Site> newSites = sites.subList(startIndex, endIndex+1);

		// create inputs XML file
		CalculationInputsXMLFile xml = new CalculationInputsXMLFile(erf, imrMaps, newSites, calcSettings, archiver);

		xml.setSerialized(serializedERFFile);

		// write to XML
		Document doc = XMLUtils.createDocumentWithRoot();
		xml.toXMLMetadata(doc.getRootElement());

		XMLUtils.writeDocumentToFile(fileName, doc);

		return fileName;
	}

	private String serializeERF(String odir) throws IOException {
		erf.updateForecast();
		String serializedERFFile = odir + ERF_SERIALIZED_FILE_NAME;
		FileUtils.saveObjectInFileThrow(serializedERFFile, erf);
		return serializedERFFile;
	}

	public Universe getUniverse() {
		return universe;
	}

	public void setUniverse(Universe universe) {
		this.universe = universe;
	}

	public void createSubmitDAGScript(String odir, boolean run) throws IOException {
		String scriptFileName = odir + "submit_DAG.sh";
		FileWriter fw = new FileWriter(scriptFileName);
		fw.write("#!/bin/bash\n");
		fw.write("" + "\n");
		fw.write("if [ -f ~/.bash_profile ]; then" + "\n");
		fw.write("\t. ~/.bash_profile" + "\n");
		fw.write("fi" + "\n");
		fw.write("" + "\n");
		fw.write("cd "+odir+"\n");
		String dagArgs = "-maxidle " + DAGMAN_MAX_IDLE + " -MaxPre " + DAGMAN_MAX_PRE + 
		" -MaxPost " + DAGMAN_MAX_POST + 
		" -OldRescue 0 -AutoRescue 1";
		fw.write("condor_submit_dag " + dagArgs + " main.dag" + "\n");
		fw.close();
		if (run) {
			String outFile = scriptFileName + ".subout";
			String errFile = scriptFileName + ".suberr";
			int retVal = RunScript.runScript(new String[]{"sh", "-c", "sh "+scriptFileName}, outFile, errFile);
			System.out.println("Command executed with status " + retVal);
		}
	}
	
	public static void main(String args[]) {
		if (args.length != 5) {
			System.err.println("USAGE: HazardDataSetDAGCreator <Input XML> <Curves Per Job> <Calc Dir> <Java Path> <Jar Path>");
			System.exit(2);
		}
		String inputFile = args[0];
		int curvesPerJob = Integer.parseInt(args[1]);
		String calcDir = args[2];
		String javaPath = args[3];
		String jarPath = args[4];
		
		try {
			Document doc = XMLUtils.loadDocument(inputFile);
			CalculationInputsXMLFile inputs = CalculationInputsXMLFile.loadXML(doc);
			
			if (!calcDir.endsWith(File.separator))
				calcDir += File.separator;
			String curvesDir = calcDir + "curves";
			
			((AsciiFileCurveArchiver)inputs.getArchiver()).setOutputDir(curvesDir);
			
			HazardDataSetDAGCreator dagCreator = new HazardDataSetDAGCreator(inputs, javaPath, jarPath);
			
			File calcDirFile = new File(calcDir);
			
			dagCreator.setUniverse(Universe.SCHEDULER);
			dagCreator.writeDAG(calcDirFile, curvesPerJob, false);
			
			System.exit(0);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
}
