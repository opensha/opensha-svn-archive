package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;
import org.dom4j.DocumentException;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.utils.MatrixIO;

public class SolFileMake {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, IOException {
		File dir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_10_17-morgan-ncal2");
		
//		File rupSetFile = new File(dir, "rupSet.xml");
//		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromXMLFile(rupSetFile);
		FaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.NCAL.getRupSet();
		
		File binFile = new File(dir, "dsa_4threads_10nodes_FAST_SA_dSub200_sub200_run0.mat");
		File outputFile = new File(dir, dir.getName()+".zip");
		
		double[] rupRateSolution = MatrixIO.doubleArrayFromFile(binFile);
		
//		int numNonZero = 0;
//		for (double rate : rupRateSolution)
//			if (rate > 0)
//				 numNonZero++;
//		double[] nonZeros = new double[numNonZero];
//		int cnt = 0;
//		for (double rate : rupRateSolution) {
//			if (rate > 0)
//				nonZeros[cnt++] = rate;
//		}
//		System.out.println("min: "+StatUtils.min(nonZeros));
//		System.out.println("max: "+StatUtils.max(nonZeros));
//		System.out.println("mean: "+StatUtils.mean(nonZeros));
//		Arrays.sort(nonZeros);
//		System.out.println("median: "+nonZeros[nonZeros.length/2]);
//		double var = StatUtils.variance(nonZeros);
//		System.out.println("variance: "+var);
//		System.out.println("std dev: "+Math.sqrt(var));
//		System.exit(0);
		
		SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rupRateSolution);
		
		sol.toZipFile(outputFile);
	}

}
