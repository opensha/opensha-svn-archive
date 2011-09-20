package scratch.kevin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.XMLUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;

public class SolResultFromMorgan {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws DocumentException, FileNotFoundException, IOException {
		File posterDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/poster");
		File ncal_input = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/Northern CA Models/rupRateSolutions/Model1A.xml");
		File state_input = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_08_17-morgan/rupSet.xml");
		
		Element ncal_input_el = XMLUtils.loadDocument(ncal_input).getRootElement()
			.element(SimpleFaultSystemSolution.XML_METADATA_NAME)
			.element(SimpleFaultSystemRupSet.XML_METADATA_NAME);
		SimpleFaultSystemRupSet ncalRS = SimpleFaultSystemRupSet.fromXMLMetadata(ncal_input_el);
		SimpleFaultSystemRupSet stateRS = SimpleFaultSystemRupSet.fromXMLFile(state_input);
		
		ncalRS.toZipFile(new File(posterDir, "ncal_rupSet.zip"));
		stateRS.toZipFile(new File(posterDir, "state_rupSet.zip"));
		System.exit(0);
		
		File myDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_08_17-morgan");
		SimpleFaultSystemRupSet myRS = SimpleFaultSystemRupSet.fromXMLFile(new File(myDir, "rupSet.xml"));
		SimpleFaultSystemSolution mySol = new SimpleFaultSystemSolution(myRS,
				MatrixIO.doubleArrayFromFile(new File(myDir, "dsa_4threads_20nodes_FAST_SA_dSub200_sub200_run0.mat")));
		mySol.toZipFile(new File(myDir, "dsa_4threads_20nodes_FAST_SA_dSub200_sub200_run0.zip"));
		System.exit(0);
		
		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromXMLFile(new File(
		"/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/rupSet.xml"));
		
		File dir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/Northern CA Models/rupRateSolutions");
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				continue;
			if (!file.getName().endsWith(".txt"))
				continue;
			System.out.println("converting: "+file.getName());
			double[] rates = new double[rupSet.getNumRuptures()];
			int cnt = 0;
			for (String line : FileUtils.loadFile(file.getAbsolutePath())) {
				if (line.isEmpty())
					continue;
				line = line.trim();
				double val = Double.parseDouble(line);
				rates[cnt++] = val;
			}
			Preconditions.checkState(cnt == rates.length, "file '"+file.getName()
					+" has "+cnt+" values, "+rates.length+" expected!");
			
			SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rates);
			File outFile = new File(dir, file.getName().replace(".txt", ".xml"));
			System.out.println("exporting to: "+outFile.getName());
			sol.toXMLFile(outFile);
		}
		System.out.println("DONE.");
	}

}
