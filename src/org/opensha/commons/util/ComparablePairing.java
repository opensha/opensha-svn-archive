package org.opensha.commons.util;

/**
 * Utility class for sorting a list of one object via another object.
 * @author kevin
 *
 * @param <C> the comparable object used for sorting
 * @param <E> the object that you want sorted via the given comparable
 */
public class ComparablePairing<C extends Comparable<C>, E> implements Comparable<ComparablePairing<C,E>> {
	
	private C comparable;
	private E data;
	
	public ComparablePairing(C comparable, E data) {
		this.comparable = comparable;
		this.data = data;
	}

	@Override
	public int compareTo(ComparablePairing<C,E> o) {
		return comparable.compareTo(o.comparable);
	}
	
	public E getData() {
		return data;
	}
	
	public C getComparable() {
		return comparable;
	}

}
