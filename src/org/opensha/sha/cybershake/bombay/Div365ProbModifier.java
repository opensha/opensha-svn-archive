package org.opensha.sha.cybershake.bombay;

import org.opensha.sha.calc.RuptureProbabilityModifier;

public class Div365ProbModifier implements RuptureProbabilityModifier {

	public double getModifiedProb(int sourceID, int rupID, int rupVarID,
			double origProb) {
		return origProb / 365d;
	}

}
