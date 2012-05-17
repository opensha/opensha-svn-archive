package scratch.UCERF3;

import java.util.List;

import org.apache.commons.math.stat.StatUtils;

public class AverageFaultSystemSolution extends SimpleFaultSystemSolution {
	
	private double[][] rates;
	
	private static double[][] toArrays(List<double[]> ratesList) {
		int numRups = ratesList.get(0).length;
		int numSols = ratesList.size();
		double[][] rates = new double[numRups][numSols];
		
		for (int s=0; s<numSols; s++) {
			double[] sol = ratesList.get(s);
			for (int r=0; r<numRups; r++) {
				rates[r][s] = sol[r];
			}
		}
		
		return rates;
	}
	
	private static double[] getMeanRates(double[][] rates) {
		double[] mean = new double[rates.length];
		
		for (int r=0; r<rates.length; r++)
			mean[r] = StatUtils.mean(rates[r]);
		
		return mean;
	}

	public AverageFaultSystemSolution(FaultSystemRupSet rupSet,
			List<double[]> ratesList) {
		this(rupSet, toArrays(ratesList));
	}
	
	public AverageFaultSystemSolution(FaultSystemRupSet rupSet,
			double[][] rates) {
		super(rupSet, getMeanRates(rates));
		
		this.rates = rates;
	}
	
	public double getRateStdDev(int rupIndex) {
		return Math.sqrt(StatUtils.variance(rates[rupIndex], getRateForRup(rupIndex)));
	}

}
