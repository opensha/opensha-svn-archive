package scratch.kevin.ucerf3.eal;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sra.calc.parallel.MPJ_CondLossCalc;

import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter;

public class UCERF3_EAL_ScriptGen {

	public static void main(String[] args) throws IOException {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/eal");
		if (!writeDir.exists())
			writeDir.mkdir();
		
//		String runSubDirName = "2013_11_04-eal-calc-small-test";
		String runSubDirName = "2014_01_15-ucerf3-eal-calc-NGA2s-2013";
		
		writeDir = new File(writeDir, runSubDirName);
		if (!writeDir.exists())
			writeDir.mkdir();
		
//		BatchScriptWriter pbsWrite = new USC_HPCC_ScriptWriter();
//		File remoteDir = new File("/auto/scec-02/kmilner/ucerf3/curves/MeanUCERF3-curves");
//		File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;
//		File mpjHome = USC_HPCC_ScriptWriter.FMPJ_HOME;
//		int maxHeapMB = 9000;
		
		BatchScriptWriter pbsWrite = new StampedeScriptWriter();
		File remoteMainDir = new File("/work/00950/kevinm/ucerf3/eal");
		File remoteSubDir = new File(remoteMainDir, runSubDirName);
		File javaBin = StampedeScriptWriter.JAVA_BIN;
		File mpjHome = StampedeScriptWriter.FMPJ_HOME;
		int maxHeapMB = 26000;
		
		String meanSolFileName = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL_WITH_MAPPING.zip";
		File meanSolFile = new File(remoteMainDir, meanSolFileName);
		
		String vulnFileName = "2012_01_02_VUL06.txt";
		File vulnFile = new File(remoteMainDir, vulnFileName);
		
		String portfolioFileName = "Porter (30 Oct 2013) CEA proxy portfolio.csv";
//		String portfolioFileName = "small_test_port.csv";
		File portfolioFile = new File(remoteMainDir, portfolioFileName);
		
		FastMPJShellScriptWriter javaWrite = new FastMPJShellScriptWriter(javaBin, maxHeapMB,
				LogicTreePBSWriter.getClasspath(remoteMainDir, remoteSubDir), mpjHome, false);
		
//		JavaShellScriptWriter javaWrite = new JavaShellScriptWriter(javaBin, maxHeapMB,
//				LogicTreePBSWriter.getClasspath(remoteDir, remoteDir));
		
		int mins = 1000;
		int nodes = 40;
		int ppn = 8;
		String queue = null;
		
		String className = MPJ_CondLossCalc.class.getName();
		
//		AttenRelRef[] imrs = { AttenRelRef.CB_2013, AttenRelRef.CY_2013,
//				AttenRelRef.ASK_2013, AttenRelRef.BSSA_2013, AttenRelRef.Idriss_2013 };
//		AttenRelRef[] imrs = { AttenRelRef.CB_2013 };
		AttenRelRef[] imrs = { AttenRelRef.CY_2013,
				AttenRelRef.ASK_2013, AttenRelRef.BSSA_2013, AttenRelRef.Idriss_2013 };
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF();
		erf.setParameter(FaultSystemSolutionERF.FILE_PARAM_NAME, meanSolFile);
		erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.INCLUDE);
		erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.CROSSHAIR);
		
		for (AttenRelRef ref : imrs) {
			String name = ref.name();
			File localXML = new File(writeDir, name+".xml");
			File remoteXML = new File(remoteSubDir, name+".xml");
			
			Document doc = XMLUtils.createDocumentWithRoot();
			Element root = doc.getRootElement();
			erf.toXMLMetadata(root);
			ScalarIMR imr = ref.instance(null);
			imr.toXMLMetadata(root);
			
			XMLUtils.writeDocumentToFile(localXML, doc);
			
			File remoteOutput = new File(remoteSubDir, name+".bin");
			
			String jobArgs = "--vuln-file \""+vulnFile.getAbsolutePath()+"\" \""+portfolioFile.getAbsolutePath()+"\" "
					+remoteXML.getAbsolutePath()+" "+remoteOutput.getAbsolutePath();
			
			File jobFile = new File(writeDir, name+".pbs");
			pbsWrite.writeScript(jobFile, javaWrite.buildScript(className, jobArgs), mins, nodes, ppn, queue);
		}
	}

}
