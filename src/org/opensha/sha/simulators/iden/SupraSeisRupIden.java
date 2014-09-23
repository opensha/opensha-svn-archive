package org.opensha.sha.simulators.iden;

import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

public class SupraSeisRupIden extends AbstractRuptureIdentifier {
	private General_EQSIM_Tools tools;
	
	public SupraSeisRupIden(General_EQSIM_Tools tools) {
		this.tools = tools;
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		return tools.isEventSupraSeismogenic(event, Double.NaN);
	}

	@Override
	public String getName() {
		return "Supra Seismogenic Rupture Identifier";
	}

}
