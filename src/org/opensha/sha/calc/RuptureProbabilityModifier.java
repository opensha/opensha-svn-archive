package org.opensha.sha.calc;

/**
 * This interface is for calculating curves for special cases where the UCERF probabilities
 * should be modified for a small subset of ruptures.
 * 
 * It was originally created for a calculation concerning a temporary increase in hazard
 * for southern-nucleating SAF ruptures during the Bombay Beach swarm in Spring, 2009. 
 * 
 * @author kevin
 *
 */
public interface RuptureProbabilityModifier {
	
	/**
	 * This method should return the modified probability for the given source/rup ID
	 * combination.
	 * 
	 * @param sourceID
	 * @param rupID
	 * @param rupVarID
	 * @param origProb
	 * @return
	 */
	public double getModifiedProb(int sourceID, int rupID, int rupVarID, double origProb);

}
