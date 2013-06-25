package scratch.kevin.ucerf3.erf;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.dom4j.DocumentException;

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
		erf.setMeanParams(5d, true, 0.1d, DeformationModels.GEOLOGIC.name());
		System.out.println("Updating forecast");
		erf.updateForecast();
		System.out.println("Done");
		while (1 < 10) {
			erf.getSource((int)(Math.random()*erf.getNumSources()-1));
			Thread.sleep(10000);
		}
	}

}
