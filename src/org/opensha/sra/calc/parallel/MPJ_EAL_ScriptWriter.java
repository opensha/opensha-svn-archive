package org.opensha.sra.calc.parallel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;

public class MPJ_EAL_ScriptWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		AbstractERF erf = new MeanUCERF2();
//		ScalarIMR imr = new CB_2008_AttenRel(null);
//		ScalarIMR imr = new BA_2008_AttenRel(null);
//		ScalarIMR imr = new AS_2008_AttenRel(null);
		ScalarIMR imr = new CY_2008_AttenRel(null);
		
		int mins = 500;
		int nodes = 10;
		String queue = "nbns";
		
		File portfolioFile = new File("/home/scec-02/kmilner/tree_trimming/Porter-28-Mar-2012-CEA-proxy-pof.txt");
		File vulnFile = new File("/home/scec-02/kmilner/tree_trimming/2011_11_07_VUL06.txt");
//		String vulnFileName = ""
		
		String jobName = imr.getShortName();
		jobName = new SimpleDateFormat("yyyy_MM_dd").format(new Date())+"-"+jobName;
		
		File localDir = new File("/tmp", jobName);
		File remoteDir = new File("/auto/scec-02/kmilner/tree_trimming", jobName);
		
		localDir.mkdir();
		
		imr.setParamDefaults();
		erf.updateForecast();
		
		ArrayList<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir.getParentFile(), "OpenSHA_complete.jar"));
		classpath.add(new File(remoteDir.getParentFile(), "commons-cli-1.2.jar"));
		
		MPJShellScriptWriter mpjWrite = new MPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, 2048,
				classpath, USC_HPCC_ScriptWriter.MPJ_HOME, false);
		
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		erf.toXMLMetadata(root);
		imr.toXMLMetadata(root);
		
		String xmlName = jobName+".xml";
		File localXML = new File(localDir, xmlName);
		File remoteXML = new File(remoteDir, xmlName);
		
		XMLUtils.writeDocumentToFile(localXML, doc);
		
		File outputFile = new File(remoteDir, jobName+".txt");
		
		String argz = "--vuln-file "+vulnFile.getAbsolutePath()+" "+portfolioFile.getAbsolutePath()+" "
					+remoteXML.getAbsolutePath()+" "+outputFile.getAbsolutePath();
		
		List<String> script = mpjWrite.buildScript(MPJ_EAL_Calc.class.getName(), argz);
		
		File pbsFile = new File(localDir, jobName+".pbs");
		USC_HPCC_ScriptWriter usc = new USC_HPCC_ScriptWriter();
		script = usc.buildScript(script, mins, nodes, 8, queue);
		usc.writeScript(pbsFile, script);
	}

}
