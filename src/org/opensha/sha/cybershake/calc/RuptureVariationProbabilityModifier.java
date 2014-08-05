package org.opensha.sha.cybershake.calc;

import java.util.List;
import java.util.Map;


/**
 * This interface is for calculating curves for special cases where the UCERF probabilities
 * should be modified for a small subset of rupture variations within a rupture.
 * 
 * It was originally created for a calculation concerning a temporary increase in hazard
 * for southern-nucleating SAF ruptures during the Bombay Beach swarm in Spring, 2009. 
 * 
 * @author kevin
 *
 */
public interface RuptureVariationProbabilityModifier {
	
	public Map<Double, List<Integer>> getVariationProbs(int sourceID, int rupID, double originalProb);
}
