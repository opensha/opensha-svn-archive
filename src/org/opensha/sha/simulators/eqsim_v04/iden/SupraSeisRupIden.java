package org.opensha.sha.simulators.eqsim_v04.iden;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

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
