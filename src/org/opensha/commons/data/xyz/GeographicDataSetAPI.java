package org.opensha.commons.data.xyz;

import org.opensha.commons.geo.Location;

/**
 * Interface for a geographic XYZ dataset. These datasets are backed by <code>Location</code> objects
 * instead of <code>Point2D</code> objects. They also have the capability of storing lat, lon values
 * as x, y or y, x dependent on the latitudeX parameter.
 * 
 * @author kevin
 *
 */
public interface GeographicDataSetAPI extends XYZ_DataSetAPI {
	
	/**
	 * Set latitudeX. If true, latitude will be stored as X, otherwise as Y.
	 * 
	 * @param latitudeX
	 */
	public void setLatitudeX(boolean latitudeX);
	
	/**
	 * Returns true if latitude will be stored as X, otherwise false if as Y.
	 * 
	 * @return
	 */
	public boolean isLatitudeX();
	
	/**
	 * Set the value at the given <code>Location</code>. If the location doesn't exist in the
	 * dataset then it will be added.
	 * 
	 * @param loc
	 * @param value
	 */
	public void set(Location loc, double value);
	
	/**
	 * Get the value at the given <code>Location</code>, or null if it doesn't exist.
	 * 
	 * @param loc
	 * @return
	 */
	public double get(Location loc);
	
	/**
	 * Returns the index of the given location, or -1 if it doesn't exist.
	 * 
	 * @param loc
	 * @return
	 */
	public int indexOf(Location loc);
	
	/**
	 * Returns the location at the given index. If index < 0 or index >= size(), an
	 * exception will be thrown.
	 * 
	 * @param index
	 * @return
	 */
	public Location getLocation(int index);
	
	/**
	 * Returns true if the dataset contains the given Location, false, otherwise.
	 * 
	 * @param loc
	 * @return
	 */
	public boolean contains(Location loc);

}
