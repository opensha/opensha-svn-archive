package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math.stat.StatUtils;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

import com.google.common.collect.Lists;

import scratch.UCERF3.utils.MatrixIO;

public class NonzeroWaterlevelPlot {
	
	private static ArbitrarilyDiscretizedFunc loadCounts(File dir) throws IOException {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		double[] nonZeros = null;
		int cnt = 0;
		for (File file : dir.listFiles()) {
			if (!file.getName().endsWith(".bin"))
				continue;
			double[] rates = MatrixIO.doubleArrayFromFile(file);
			
			if (nonZeros == null)
				nonZeros = new double[rates.length];
			cnt++;
			for (int i=0; i<rates.length; i++)
				if (rates[i] > 0)
					nonZeros[i] = 1d;
			
			func.set((double)cnt, StatUtils.sum(nonZeros));
		}
		return func;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File startZerosDir = new File("/home/kevin/OpenSHA/UCERF3/nomins_startzero");
		File startUCERF2Dir = new File("/home/kevin/OpenSHA/UCERF3/nomins_startucerf2");
		
		ArbitrarilyDiscretizedFunc startZeroFunc = loadCounts(startZerosDir);
		startZeroFunc.setName("Starting with zeros");
		ArbitrarilyDiscretizedFunc startUCERF2Func = loadCounts(startUCERF2Dir);
		startUCERF2Func.setName("Starting with UCERF2");
		
		ArrayList<ArbitrarilyDiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(startZeroFunc);
		funcs.add(startUCERF2Func);
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, "Num Non Zeros");
	}

}
