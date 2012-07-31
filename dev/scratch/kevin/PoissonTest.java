package scratch.kevin;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

public class PoissonTest {
	
	public static void main(String[] args) throws MathException {
		double mean = 10;
		double testMax = 20;
		
		PoissonDistributionImpl p = new PoissonDistributionImpl(mean);
		
		System.out.println("PROBABLITIES!");
		for (int i=0; i<testMax; i++)
			System.out.println(i+": "+p.probability(i));
		
		System.out.println("\nCUMULATIVE PROBABLITIES!");
		for (int i=0; i<testMax; i++)
			System.out.println(i+": "+p.cumulativeProbability(i));
		
		System.out.println("\nMY PROBABILITIES!");
		for (int i=0; i<testMax; i++) {
			double cml = p.cumulativeProbability(i);
			double inv = 1 - cml;
			System.out.println(i+": "+inv);
		}
	}

}
