package org.opensha.data.siteType;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.param.ParameterList;

public interface SiteDataAPI<Element> {
	
	/* ************ Site Data Types ************ */
	
	/**
	 * Vs 30 data type - Shear Wave velocity at 30 meter depth (m/sec)
	 */
	public static final String TYPE_VS30 = "Vs30";
	/**
	 * Wills site classification data type - Can be translated to Vs30
	 */
	public static final String TYPE_WILLS_CLASS = "Wills Class";
	/**
	 * Depth to first Vs30 = 2.5 km/sec (km)
	 */
	public static final String TYPE_DEPTH_TO_2_5 = "Depth to Vs = 2.5 km/sec";
	/**
	 * Depth to first Vs30 = 1.0 km/sec (km)
	 */
	public static final String TYPE_DEPTH_TO_1_0 = "Depth to Vs = 1.0 km/sec";
	
	/* ************ Type Flags ************ */
	
	/**
	 * Flag for site data with measured values
	 */
	public static final String TYPE_FLAG_MEASURED = "Measured";
	/**
	 * Flag for site data with inferred values
	 */
	public static final String TYPE_FLAG_INFERRED = "Inferred";
	
	/* ************ Site Data API ************ */
	
	/**
	 * This gives the applicable region for this data set.
	 * @return GeographicRegion
	 */
	public GeographicRegion getApplicableRegion();
	
	/**
	 * This gives the resolution of the dataset in degrees, or 0 for infinite resolution.
	 * 
	 * We could possibly add a 'units' field to allow for resolution in KM
	 * @return
	 */
	public double getResolution();
	
	/**
	 * Get the name of this dataset
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the short name of this dataset
	 * 
	 * @return
	 */
	public String getShortName();
	
	/** 
	 * Get the type of this dataset
	 * 
	 * @return
	 */
	public String getType();
	
	/**
	 * Get the flag for this type, such as "Measured" or "Inferred"
	 * 
	 * @return
	 */
	public String getTypeFlag();
	
	/**
	 * Get the location of the closest data point
	 * 
	 * @param loc
	 * @return
	 */
	public Location getClosestDataLocation(Location loc) throws IOException;
	
	/**
	 * Get the value at the closest location
	 * 
	 * @param loc
	 * @return
	 */
	public Element getValue(Location loc) throws IOException;
	
	/**
	 * Get the value, with metadata, at the closest location
	 * 
	 * @param loc
	 * @return
	 * @throws IOException
	 */
	public SiteDataValue<Element> getAnnotatedValue(Location loc) throws IOException;
	
	/**
	 * Get the value for each location in the given location list
	 * 
	 * @param loc
	 * @return
	 */
	public ArrayList<Element> getValues(LocationList locs) throws IOException;
	
	/**
	 * Returns true if the value is valid, and not NaN, N/A, or equivelant for the data type
	 * 
	 * @param el
	 * @return
	 */
	public boolean isValueValid(Element el);
	
	/**
	 * Returns true if there is data for the given site
	 * 
	 * @param loc - The location
	 * @param checkValid - Boolean for checking the validity of the value at the specified location
	 * @return
	 */
	public boolean hasDataForLocation(Location loc, boolean checkValid);
	
	/**
	 * Returns a list of adjustable parameters. For many types of site data, this will be empty, but for
	 * more complex ones like WaldGlobalVs2007, it's more complicated.
	 * 
	 * @return
	 */
	public ParameterList getAdjustableParameterList();
	
	/**
	 * Returns the metadata for this dataset.
	 * 
	 * @return
	 */
	public String getMetadata();
	
}
