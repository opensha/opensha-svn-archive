package org.opensha.sha.earthquake.param;

/**
 * Probability model options.
 * @author Ned Field
 * @version $Id:$
 */
@SuppressWarnings("javadoc")
public enum ProbabilityModelOptions {
	POISSON("Poisson"),
	BPT("BPT");
	
	private String label;
	private ProbabilityModelOptions(String label) {
		this.label = label;
	}
	
	@Override public String toString() {
		return label;
	}

}
