package org.opensha.commons.param;

import java.util.Collection;

public class ListBasedConstraint<E> extends ParameterConstraint<E> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<E> allowed;
	
	public ListBasedConstraint(Collection<E> allowed) {
		this.allowed = allowed;
	}
	
	public void setAllowed(Collection<E> allowed) {
		this.allowed = allowed;
	}
	
	public Collection<E> getAllowed() {
		return allowed;
	}

	@Override
	public boolean isAllowed(E obj) {
		return allowed.contains(obj);
	}

	@Override
	public Object clone() {
		return new ListBasedConstraint<E>(allowed);
	}

}
