package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;

public class TestSaveToZipFile {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		File rupXMLFile = new File("D:\\Documents\\temp\\Inversion Results\\" +
		"dsa_4threads_50nodes_FAST_SA_dSub200_sub100_run3.xml");
		System.out.println("Loading XML");
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromXMLFile(rupXMLFile);
//		File rupXMLFile = new File("C:\\Projects\\workspace\\OpenSHA\\dev\\scratch\\UCERF3\\preComputedData\\rupSet.xml");
//		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromXMLFile(rupXMLFile);
//		System.out.println("Saving ZIP");
		File zipFile = new File("D:\\Documents\\temp\\Inversion Results\\" +
		"dsa_4threads_50nodes_FAST_SA_dSub200_sub100_run3.zip");
//		sol.toZipFile(zipFile);
		System.out.println("Loading ZIP");
		SimpleFaultSystemSolution loadedSol = SimpleFaultSystemSolution.fromZipFile(zipFile);
		System.out.println("DONE");
	}

}
