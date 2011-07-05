package scratch.kevin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.commons.util.FileUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;

public class SolResultFromMorgan {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws DocumentException, FileNotFoundException, IOException {
		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(new File(
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
			sol.toFile(outFile);
		}
		System.out.println("DONE.");
	}

}
