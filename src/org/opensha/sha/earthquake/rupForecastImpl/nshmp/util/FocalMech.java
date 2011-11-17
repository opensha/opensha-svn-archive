package org.opensha.sha.earthquake.rupForecastImpl.nshmp.util;

/**
 * Identifier for different focal mechanism types.
 * 
 * TODO this could move to commons
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum FocalMech {

		/** A strike-slip focal mechanism. */
		STRIKE_SLIP(1, 0.0),

		/** A reverse slip (or thrust) focal mechanism. */
		REVERSE(2, 90.0),
		
		/** A normal slip focal mechanism. */
		NORMAL(3, -90.0);
		
		private int id;
		private double rake;
		private FocalMech(int id, double rake) {
			this.id = id;
			this.rake = rake;
		}
		
		/**
		 * Returns the focal mechanism associated with the supplied id value.
		 * @param id to lookup
		 * @return the associated <code>FocalMech</code>
		 */
		public static FocalMech typeForID(int id) {
			for (FocalMech fm : FocalMech.values()) {
				if (fm.id == id) return fm;
			}
			return null;
		}
		
		/**
		 * Returns the rake value for this mechanism.
		 * @return the rake
		 */
		public double rake() {
			return rake;
		}

}
