package org.opensha.sha.simulators.eqsim_v04.iden;

import java.util.Collection;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

import com.google.common.collect.Lists;

public class LogicalAndRupIden extends AbstractRuptureIdentifier {
	
	private Collection<RuptureIdentifier> rupIdens;

	public LogicalAndRupIden(RuptureIdentifier... rupIdens) {
		this(Lists.newArrayList(rupIdens));
	}
	
	public LogicalAndRupIden(Collection<RuptureIdentifier> rupIdens) {
		this.rupIdens = rupIdens;
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		for (RuptureIdentifier rupIden : rupIdens)
			if (!rupIden.isMatch(event))
				return false;
		return true;
	}

	@Override
	public String getName() {
		return "Logical and rupture identfier with "+rupIdens.size()+" idens.";
	}
	
	public Collection<RuptureIdentifier> getSubIdens() {
		return rupIdens;
	}

}
