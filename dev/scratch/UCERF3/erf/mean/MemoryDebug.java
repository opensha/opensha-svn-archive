package scratch.UCERF3.erf.mean;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.dom4j.DocumentException;
import org.opensha.sha.earthquake.ProbEqkSource;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.utils.FaultSystemIO;

public class MemoryDebug {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, DocumentException, InterruptedException {
		File meanTotalSolFile = new File(MeanUCERF3.getStoreDir(), MeanUCERF3.TRUE_MEAN_FILE_NAME);
		System.out.println("Loading sol");
		FaultSystemSolution meanTotalSol = FaultSystemIO.loadSol(meanTotalSolFile);
		System.out.println("Done");
//		while (1 < 10) {
//			meanTotalSol.getRateForAllRups()[0] = Math.random();
//			Thread.sleep(10000);
//		}
		System.out.println("Creating ERF");
		MeanUCERF3 erf = new MeanUCERF3(meanTotalSol);
		erf.setCachingEnabled(false);
//		String rakeBasisStr = DeformationModels.GEOLOGIC.name();
		String rakeBasisStr = MeanUCERF3.RAKE_BASIS_MEAN;
		erf.setMeanParams(1d, false, 0.1d, rakeBasisStr);
		System.out.println("Updating forecast");
		erf.updateForecast();
		System.out.println("Done");
		int charCount = 0;
		int infoCount = 0;
		for (ProbEqkSource src : erf) {
			charCount += src.getName().length();
			if (src.getInfo() != null)
				infoCount += src.getInfo().length();
		}
		System.out.println("Total Name Char count: "+charCount);
		System.out.println("Total Info Char count: "+infoCount);
		while (1 < 10) {
			erf.getSource((int)(Math.random()*erf.getNumSources()-1));
			Thread.sleep(10000);
		}
	}

}
