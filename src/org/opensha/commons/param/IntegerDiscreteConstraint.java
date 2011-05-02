package org.opensha.commons.param;

import java.util.ArrayList;

import com.google.common.base.Preconditions;

public class IntegerDiscreteConstraint extends ParameterConstraint<Integer> {
	private static final String S = "IntegerDiscreteConstraint";
	
	private ArrayList<Integer> allowed;
	
	public IntegerDiscreteConstraint(ArrayList<Integer> allowed) {
		setAllowed(allowed);
	}
	
	public void setAllowed(ArrayList<Integer> allowed) {
		checkEditable(S);
		Preconditions.checkNotNull(allowed, "Allowed cannot be null!");
		this.allowed = allowed;
	}

	@Override
	public boolean isAllowed(Integer obj) {
		if (obj == null)
			return isNullAllowed();
		return allowed.contains(obj);
	}

	@Override
	public Object clone() {
		IntegerDiscreteConstraint iconst = new IntegerDiscreteConstraint(allowed);
		iconst.setNullAllowed(isNullAllowed());
		if (!this.isEditable())
			iconst.setNonEditable();
		return iconst;
	}
	
	public ArrayList<Integer> getAllowed() {
		return allowed;
	}
	
	public int size() {
		return allowed.size();
	}

}
