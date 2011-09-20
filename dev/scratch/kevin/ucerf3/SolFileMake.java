package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;
import org.dom4j.DocumentException;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;

public class SolFileMake {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, IOException {
		File dir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_09_08-morgan-CS_fixed");
		File rupSetFile = new File(dir, "rupSet.xml");
		File binFile = new File(dir, "run1.mat");
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
		
		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromXMLFile(rupSetFile);
		SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rupRateSolution);
		
		sol.toZipFile(outputFile);
	}

}
