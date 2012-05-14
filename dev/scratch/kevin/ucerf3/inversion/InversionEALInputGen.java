package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.commons.util.FileNameComparator;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sra.calc.parallel.MPJ_EAL_Calc;
import org.opensha.sra.calc.parallel.treeTrimming.LogicTreeInputFileGen;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;

import com.google.common.collect.Lists;

public class InversionEALInputGen {
	
	private static List<File> loadRemotePaths(File localDir, File remoteDir) {
		ArrayList<File> files = Lists.newArrayList();
		
		File[] dirFiles = localDir.listFiles();
		Arrays.sort(dirFiles, new FileNameComparator());
		
		for (File file : dirFiles) {
			String name = file.getName();
			
			if (!name.endsWith(".zip"))
				continue;
			
			files.add(new File(remoteDir, name));
		}
		
//		files.add(new File("/home/kevin/OpenSHA/UCERF3/inversions/2012_05_02-fm2-cooling-tests/results/asdf_run00.zip"));
		
		return files;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, InvocationTargetException {
		int batchSize = 5;
		int normalJobMins = 900;
		
		File localDir = new File("/home/kevin/OpenSHA/UCERF3/eal/2012_05_03-eal-tests-apriori-1000");
		File remoteDir = new File("/auto/scec-02/kmilner/ucerf3/eal/2012_05_03-eal-tests-apriori-1000");
		
//		File portfolio = new File(remoteDir, "Porter-23-Jan-2012-CA99ptcPof.txt");
//		File vulnFile = new File(remoteDir, "2012_01_02_VUL06.txt");
		File portfolio = new File(remoteDir, "portfolio.csv");
		File vulnFile = new File(remoteDir, "2011_11_07_VUL06.txt");
		
//		ScalarIMR[] imrs = { AttenRelRef.CB_2008.instance(null), AttenRelRef.BA_2008.instance(null),
//				AttenRelRef.CY_2008.instance(null), AttenRelRef.AS_2008.instance(null)};
		
		ScalarIMR[] imrs = { AttenRelRef.CB_2008.instance(null) };
		
		ArrayList<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir, "OpenSHA_complete.jar"));
		classpath.add(new File(remoteDir, "commons-cli-1.2.jar"));
		
		MPJShellScriptWriter mpjWrite = new MPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, 7000,
				classpath, USC_HPCC_ScriptWriter.MPJ_HOME, false);
		
		List<File> solFiles = loadRemotePaths(localDir, remoteDir);
		
		for (ScalarIMR imr : imrs) {
			
			File imrLocalDir = new File(localDir, imr.getShortName());
			imrLocalDir.mkdir();
			File imrRemoteDir = new File(remoteDir, imr.getShortName());
			imrRemoteDir.mkdir();
			
			imr.setParamDefaults();
			if (imr.getSiteParams().containsParameter(DepthTo1pt0kmPerSecParam.NAME))
				imr.getSiteParams().setValue(DepthTo1pt0kmPerSecParam.NAME, null);
			if (imr.getSiteParams().containsParameter(DepthTo2pt5kmPerSecParam.NAME))
				imr.getSiteParams().setValue(DepthTo2pt5kmPerSecParam.NAME, null);
			
			int cnt = 0;
			
			while (!solFiles.isEmpty()) {
				Document doc = XMLUtils.createDocumentWithRoot();
				Element root = doc.getRootElement();
				
				String numStr = cnt+"";
				while (numStr.length() < (solFiles.size()+"").length())
					numStr = "0"+numStr;
				
				String name = imr.getShortName()+"_"+numStr;
				
				for (int i=0; i<batchSize && !solFiles.isEmpty(); i++) {
					Element el = root.addElement(MPJ_EAL_Calc.BATCH_ELEMENT_NAME);
					File solFile = solFiles.remove(0);
					
					FaultSystemSolutionPoissonERF erf = new FaultSystemSolutionPoissonERF();
					erf.getParameter(FaultSystemSolutionPoissonERF.FILE_PARAM_NAME).setValue(solFile.getAbsolutePath());
					
					erf.toXMLMetadata(el);
					imr.toXMLMetadata(el);
					
					String myName = imr.getShortName()+"_"+solFile.getName().replaceAll(".zip", "");
					
					el.addAttribute("outputFile", new File(imrRemoteDir, myName+".txt").getAbsolutePath());
					
					cnt++;
				}
				
				XMLUtils.writeDocumentToFile(new File(imrLocalDir, name+".xml"), doc);
				LogicTreeInputFileGen.writeJob(mpjWrite, portfolio, vulnFile, imrLocalDir, imrRemoteDir,
						name, null, normalJobMins, 5, "nbns", true);
			}
		}
	}

}
