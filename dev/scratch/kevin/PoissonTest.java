package scratch.kevin;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

public class PoissonTest {
	
	public static void main(String[] args) throws MathException {
		double mean = 3;
		double testMax = 8;
		
		PoissonDistributionImpl p = new PoissonDistributionImpl(mean);
		
		System.out.println("PROBABLITIES!");
		for (int i=0; i<=testMax; i++)
			System.out.println(i+": "+p.probability(i));
		
		System.out.println("\nCUMULATIVE PROBABLITIES!");
		for (int i=0; i<=testMax; i++)
			System.out.println(i+": "+p.cumulativeProbability(i));
		
		System.out.println("\nMY PROBABILITIES!");
		for (int i=0; i<=testMax; i++) {
			double prob;
			if (i <= mean)
				prob = p.cumulativeProbability(i);
			else
				prob = 1 - p.cumulativeProbability(i); 
			System.out.println(i+": "+prob);
		}
	}

}
