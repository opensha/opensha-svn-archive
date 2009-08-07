package org.opensha.commons.data.region;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.math.util.MathUtils;
import org.dom4j.Element;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.metadata.XMLSaveable;

import com.sun.servicetag.UnauthorizedAccessException;


/**
 * A <code>GriddedRegion</code> is a <code>Region</code> that has been evenly
 * discretized in latitude and longitude. Each node in a gridded region
 * represents a small area that is an equal number of degrees in width and
 * height and is identified by a unique <code>Location</code> at the geographic
 * (lat-lon) center of the node.<br/>
 * <br/>
 * A <code>GriddedRegion</code> may be initialized several ways (e.g. as a
 * circle, an area of uniform degree-width and -height, or a buffer around
 * a linear feature). The <code>Location</code>s of the grid nodes are stored
 * internally in order of increasing longitude then latitude starting with
 * the node at the lowest latitude in the region.
 * 
 * (add fig)
 * 
 * A <code>GriddedRegion</code> also provides methods to associate any
 * <code>Location</code> with the closest grid node. See 
 * {@link EvenlyGriddedGeographicRegion#getNearestLocationIndex(Location)} for
 * more details.<br/>
 * <br/>
 * 
 * @author Nitin Gupta
 * @author Vipin Gupta 
 * @author Peter Powers
 * @version $Id$
 * @see GeographicRegion
 * 
 * TODO rename to GriddedRegion
 * QUESTION Would there ever be a need for gridded regions where the gridding
 * 		axes are not parallel to lines of lat-lon
 */

public class EvenlyGriddedGeographicRegion extends GeographicRegion {

	private static final long serialVersionUID = 1L;
//	private final static String C = "EvenlyGriddedGeographicRegion";
//	private final static boolean D = false;

	public final static String XML_METADATA_NAME = "evenlyGriddedGeographicRegion";
	public final static String XML_METADATA_GRID_SPACING_NAME = "gridSpacing";
	public final static String XML_METADATA_NUM_POINTS_NAME = "numPoints";

	
	// the lat-lon arrays of node points
	// TODO may not need the nodes if LocList is always initialized
	private double[] lonNodes;
	private double[] latNodes;
	
	// the lat-lon arrays of node edges
	private double[] lonNodeEdges;
	private double[] latNodeEdges;

	private Location anchor;
	
	// lookup array for grid nodes; has length of master grid spanning
	// region bounding box; all nodes outside region have values of -1;
	// all valid nodes point to position in nodeList; gridIndices increase
	// across and then up
	private int[] gridIndices;
	
	// list of nodes
	private LocationList nodeList;
	
	// dimensions
	private double spacing;
	private int nodeCount;

	//----------------------
	//protected double spacing;

//	// this makes the first lat and lon grid points nice in that niceMinLat/gridSpacing
//	// is an integer and the point is within the polygon
//	protected double niceMinLat;
//	protected double niceMinLon;
//
//	//this makes the last lat and Lon grid points nice so that niceMaxLat/gridSpacing
//	// is an integer
//	protected double niceMaxLat;
//	protected double niceMaxLon;

//	//list of of location in the given region
//	protected LocationList gridLocsList;
//
//	//This array stores the number of locations below a given latitude
//	protected int[] locsBelowLat;
//
//	//List for storing each for a given latitude
//	private ArrayList lonsPerLatList;

	/**
	 * Initializes a gridded region from a pair of latitude and a pair of
	 * longitude values. When viewed in a Mercator projection, the region
	 * will be a rectangle. Input longitude values will be normalized to
	 * \u00B1180\u00B0. Input latitude values outside the range \u00B190\u00B0
	 * will throw an exception. If either both latitude or both longitude
	 * values are the same, an exception is thrown.
	 * 
	 * @param lat1 the first latitude value
	 * @param lat2 the second latitude value
	 * @param lon1 the first longitude value
	 * @param lon2 the second longitude value
	 * @param spacing of grid nodes
	 * @throws IllegalArgumentException if both lats or lons are the same, or
	 * 		if lat is outside the range \u00B190\u00B0
	 * @see GeographicRegion#GeographicRegion(double, double, double, double)
	 */
	public EvenlyGriddedGeographicRegion(
			double lat1, double lat2, 
			double lon1, double lon2, 
			double spacing) {
		super(lat1, lat2, lon1, lon2);
		this.spacing = spacing;
		initLatLonArrays();
		initNodes();
	}

	/**
	 * Initializes a gridded region from a list of border locations. The 
	 * border type specifies whether lat-lon values are treated as points in an
	 * orthogonal coordinate system or as connecting great circles.
	 * 
	 * @param border Locations
	 * @param type the {@link BorderType} to use when initializing
	 * @param spacing of grid nodes
	 * @throws NullPointerException if the border is null
	 * @see GeographicRegion#GeographicRegion(LocationList, BorderType)
	 */
	public EvenlyGriddedGeographicRegion(
			LocationList border, BorderType type, double spacing) {
		super(border, type);
		this.spacing = spacing;
		initLatLonArrays();
		initNodes();
	}

	/**
	 * Initializes a circular gridded region. Internally, the centerpoint and
	 * radius are used to create a circular region composed of straight line
	 * segments that span 10\u00B0 wedges.
	 * 
	 * @param center of the circle
	 * @param radius of the circle
	 * @param spacing of grid nodes
	 * @throws IllegalArgumentException if 0 \u2264 <code>radius</code> 
	 * 		\u003C 1000 km
	 * @throws NullPointerException if <code>center</code> is null
	 * @see GeographicRegion#GeographicRegion(Location, double)
	 */
	public EvenlyGriddedGeographicRegion(
			Location center, double radius, double spacing) {
		super(center, radius);
		this.spacing = spacing;
		initLatLonArrays();
		initNodes();
	}

	/**
	 * Initializes a gridded region as a buffered area around a line.
	 * 
	 * @param line at center of buffered region
	 * @param buffer distance from line
	 * @param spacing of grid nodes
	 * @throws NullPointerException if <code>line</code> is null
	 * @see GeographicRegion#GeographicRegion(LocationList, double)
	 */
	public EvenlyGriddedGeographicRegion(
			LocationList line, double buffer, double spacing) {
		super(line, buffer);
		this.spacing = spacing;
		initLatLonArrays();
		initNodes();
	}
	
	/**
	 * Initializes a gridded region with a <code>GeographicRegion</code>.
	 * 
	 * @param region to use as border for new gridded region
	 * @param spacing of grid nodes
	 */
	public EvenlyGriddedGeographicRegion(
			GeographicRegion region, 
			double spacing) {
		
		super(region.getRegionOutline(), BorderType.MERCATOR_LINEAR);
		this.spacing = spacing;
		initLatLonArrays();
		initNodes();
	}

	// TODO add constructor to initialize with  GeoRegion (would want a copy of the border)
	/**
	 * Class default constructor
	 */
	// TODO delete empty constructor
	public EvenlyGriddedGeographicRegion(){}

	/**
	 * Class constructor that accepts the region boundary loactions and grid spacing for the
	 * region.
	 * @param locList LocationList Region boundary locations
	 * @param gridSpacing double GridSpacing
	 */
//	public EvenlyGriddedGeographicRegion(LocationList locList, double gridSpacing) {
//		//sets the region min/max lat/lon aswell creates the region boundary.
//		createEvenlyGriddedGeographicRegion(locList,gridSpacing);
//	}


//	/**
//	 * Class constructor that accepts the region boundary loactions,grid spacing
//	 * and EvenlyGriddedGeographicRegionAPI,for creating the list of locations
//	 * in this region from passed in EvenlyGriddedGeographicRegionAPI, for the
//	 * region.
//	 *
//	 * This class constructor allows the user to create list of locations for this
//	 * EvenlyGriddedGeographic object from passed in EvenlyGriddedGeographicRegionAPI.
//	 * Please refer to EvenlyGriddedGeographicRegionAPI for more details.
//	 *
//	 * @param locList LocationList LocationList Region boundary locations
//	 * @param gridSpacing double GridSpacing
//	 * @param region EvenlyGriddedGeographicRegionAPI
//	 * @see EvenlyGriddedGeographicRegionAPI.createRegionLocationsList(EvenlyGriddedGeographicRegionAPI)
//	 * @see EvenlyGriddedGeographicRegionAPI
//	 */
//	public EvenlyGriddedGeographicRegion(LocationList locList, double gridSpacing,
//			EvenlyGriddedGeographicRegionAPI region) {
//		this(locList,gridSpacing);
//		createRegionLocationsList(region);
//	}

	/**
	 * This function allows the user to create list of location in the region from
	 * another EvenlyGriddedGeographic region.
	 * If the region passed in as the argument to this function is smaller and do not
	 * cover the this EvenlyGriddedRegion then this list of locations for the region
	 * can have "null" object. Once user has created the region locations using this
	 * function then care must be taken to look for "null" objects in the list explicitly.
	 *
	 * This function creates the list of locations for this EvenlyGriddedRegion, with
	 * each location being corresponding to the location in the passed in EvenlyGriddedGeographic region.
	 * But if this region does not completely lies within the range of the passed in region
	 * then some of its locations can be null, if user uses this method to create the
	 * list of locations for a given region.
	 * User has to dealt with the null location objects explicitly.
	 *
	 * If user passes in the region to this function that do not overlap with bounds
	 * of this region then all the locations in the list of locations for this region
	 * will be null.
	 * If passed in region overlaps with this region then locations within the list
	 * of locations will be null for the part of this that does not overlap with passed
	 * in region.
	 *
	 * This method is helpful as avoid creating same location more then once and just
	 * refer to the location object in the passed in EvenlyGriddedGeographicRegionAPI.
	 *
	 * @param region EvenlyGriddedGeographicRegionAPI EvenGriddedGeographicRegion.
	 * Locations created will be locations in this passed in region. So locations
	 * will be added to list only for that part of region that overlaps with this
	 * passed in region.
	 *
	 * @see EvenlyGriddedGeographicRegionAPI.createRegionLocationsList(EvenlyGriddedGeographicRegionAPI)
	 * @see EvenlyGriddedGeographicRegionAPI
	 */
	// TODO headache -- subregion implementation
//	public LocationList createRegionLocationsList(EvenlyGriddedGeographicRegion region){
//		int numLocations = this.getNumGridLocs();
//
//		for(int i=0;i<numLocations;++i){
//			//Location loc = this.getNearestGridLocation(i,region);
//			Location loc = region.getNearestLocation(getGridLocationClone(i));
//			
////			//gets the location in the EvenlyGriddedRectangularRegion at a given index
////			Location loc = getGridLocationClone(index);
////			//finding the nearest location in the EvenlyGriddedGeographicRegionAPI to the
////			//location in the EvenlyGriddedRectangularRegion location.
////			return region.getNearestLocation(getGridLocationClone(index));
//
//			
//			if(gridLocsList == null)
//				gridLocsList = new LocationList();
//			gridLocsList.addLocation(loc);
//		}
//		return gridLocsList;
//	}





//	/**
//	 * Creates a EvenlyGriddedGeographicRegion with the given locationlist and gridSpacing.
//	 * @param locList LocationList creates the region  boundary using this location list.
//	 * @param gridSpacing double
//	 */
//	public void createEvenlyGriddedGeographicRegion(LocationList locList, double gridSpacing){
//		createGeographicRegion(locList);
//		//set the gridSpacing for the region and creates the minimum information required
//		//for retreiving the any location or index in the gridded region.
//		setGridSpacing(gridSpacing); // this also sets the max and min grid lats and lons.
//	}


	/*
	 * this function creates a Lon Array for each gridLat. It also creates a
	 * int array which tells how many locations are there below a given lat
	 */
//	private void initLatLonArray() {
//		//getting the number of grid lats in the given region
//		int numLats = (int) Math.rint((niceMaxLat - niceMinLat) / spacing) + 1;
//		//initialising the array for storing number of location below a given lat
//		//first element is 0 as first lat has 0 locations below it and last num is the
//		//total number of locations.
//		locsBelowLat = new int[numLats+1];
//
//		lonsPerLatList = new ArrayList();
//
//		int locBelowIndex = 0;
//		//initializing the first element of number of locations to be 0 location for
//		//min lat.
//		//For each lat the number of locations keeps increasing.
//		locsBelowLat[locBelowIndex++] = 0;
//		//looping over all grid lats in the region to get longitudes at each lat and
//		// and number of locations below each starting lat.
//		for(int iLat = 0;iLat<numLats;++iLat) {
//			double lat = niceMinLat + iLat*spacing;
//			double lon = niceMinLon;
//			int iLon=0;
//			ArrayList lonList = new ArrayList();
//			while (lon <= niceMaxLon) {
//				//creating the location object for the lat and lon that we got
//				Location loc = new Location(lat, lon);
//				//if(lat==36.1) System.out.println("lat="+lat+"; lon="+lon);
//				//checking if this location lies in the given gridded region
//				if (this.isLocationInside(loc))
//					lonList.add(new Double(lon));
//				//lon = Math.round((lon+gridSpacing)/gridSpacing)*gridSpacing;
//				++iLon;
//				lon = niceMinLon+iLon*spacing;
//
//			}
//			//assigning number of locations below a grid lat to the grid Lat above this lat.
//			locsBelowLat[locBelowIndex] = locsBelowLat[locBelowIndex - 1];
//			locsBelowLat[locBelowIndex] += lonList.size();
//			lonsPerLatList.add(lonList);
//			//incrementing the index counter for number of locations below a given latitude
//			++locBelowIndex;
//		}
//	}



//	/*
//	 * Sets the gid node spacing value and triggers the creation of internal
//	 * arrays used for node lookup and location storage. TODO not
//	 */
//	private void setGridSpacing(double spacing) {
//		this.spacing = spacing;
//		initLatLonArray();
//	}

	/**
	 * Returns the grid node spacing for this region.
	 * @return the grid node spacing (in degrees)
	 */
	public double getGridSpacing() {
		return spacing;
	}

	/**
	 * Returns the total number of grid nodes in this region.
	 * @return the number of grid nodes
	 * TODO rename to getNodeCount
	 */
	public int getNumGridLocs() {
		return nodeCount;
	}


	/**
	 * Returns the iterator.
	 * TODO kill; users can get this once they've gotten the location list
	 * @return
	 */
	public ListIterator<Location> getGridLocationsIterator() {
		return nodeList.listIterator();
	}

	/**
	 * Returns the locations of all the nodes in teh region as a
	 * <code>LocationList</code>.
	 * @return a list of all the node locations in the region.
	 * TODO rename to getNodeList
	 */
	public LocationList getGridLocationsList() {
		return nodeList;
	}

//	/**
//	 * Returns the Gridded Location at a given index.
//	 * @see EvenlyGriddedGeographicRegionAPI.getGridLocationClone(int).
//	 */
//	private Location getGridLocationClone(int index) {
//
//		//getting the size of array that maintains number of locations at each lat
//		int size = locsBelowLat.length;
//		int locIndex = 0;
//		//iterating over all the lonsPerLat array to get the Lat index where given
//		//index lies.
//		int latIndex =0;
//		boolean locationFound = false;
//		for(int i=0;i<size-1;++i){
//			int locsIndex2 = locsBelowLat[i + 1];
//			if(index < locsIndex2){
//				locIndex = locsBelowLat[i];
//				latIndex = i;
//				locationFound = true;
//				break;
//			}
//		}
//
//		if(!locationFound) return null;
//		ArrayList lonList = (ArrayList)lonsPerLatList.get(latIndex);
//		double lon = ((Double)lonList.get(index - locIndex)).doubleValue();
//		double lat = niceMinLat+latIndex*spacing;
//		return new Location(lat,lon);
//	}

//	/**
//	 * Returns the nearest Location  in the Region to a given location.
//	 * This method will create a list of locations in the region if not already created
//	 * and then find the location at a given index.
//	 * @param index int
//	 * @return Location nearest Location  in the Region to a given location
//	 * @see EvenlyGriddedGeographicRegionAPI.getNearestLocation(Location)
//	 */
//	private Location getNearestLocation(Location loc){
//		LocationList locList = getGridLocationsList();
//		int index = 0;
//		index = getNearestLocationIndex(loc);
//		if(index < 0)
//			return null;
//		return locList.getLocationAt(index);
//	}


//	/**
//	 * Clears the gridded region lsit of location if already exists.
//	 * @see EvenlyGriddedGeographicRegionAPI.clearRegionLocations()
//	 */
//	public void clearRegionLocations(){
//		if(gridLocsList !=null){
//			gridLocsList.clear();
//			gridLocsList = null;
//		}
//	}


//	/**
//	 * Returns the nearest location in the region for the index in the Region
//	 * on which this function is called.
//	 * @see EvenlyGriddedGeographicRegionAPI.getNearestGridLocation(int ,
//	 * EvenlyGriddedGeographicRegionAPI).
//	 */
//	public Location getNearestGridLocation(int index,
//			EvenlyGriddedGeographicRegionAPI region) {
//		//gets the location in the EvenlyGriddedRectangularRegion at a given index
//		Location loc = getGridLocationClone(index);
//		//finding the nearest location in the EvenlyGriddedGeographicRegionAPI to the
//		//location in the EvenlyGriddedRectangularRegion location.
//		return region.getNearestLocation(loc);
//	}

//	/**
//	 * Returns the nearest location index in the region for the index in the Region
//	 * on which this function is called.
//	 * @see EvenlyGriddedGeographicRegionAPI getNearestGridLocationIndex(int ,
//	 * EvenlyGriddedGeographicRegionAPI)
//	 */
//	public int getNearestGridLocationIndex(int index,
//			EvenlyGriddedGeographicRegionAPI region) {
//		//gets the location in the EvenlyGriddedRectangularRegion at a given index
//		Location loc = getGridLocationClone(index);
//		//finding the nearest location index in the EvenlyGriddedGeographicRegionAPI to the
//		//location in the EvenlyGriddedRectangularRegion location.
//		return region.getNearestLocationIndex(loc);
//	}

	
	/**
	 * Returns the <code>Location</code> at a given index.
	 * 
	 * TODO rename getNode or getLocation
	 * TODO InvalidRangeException should be retired in favor of default IndexOutOfBounds
	 * 
	 * @param index of location to retrieve
	 * @return the <code>Location</code> or <code>null</code> if index is
	 * 		out of range
	 */
	public Location getGridLocation(int index) {
		try {
			return nodeList.getLocationAt(index);
		} catch (InvalidRangeException e) {
			return null;
		}
	}


//	/**
//	 * Returns the nearest location in the gridded region to the provided Location.
//	 * @param loc Location Location to which we have to find the nearest location.
//	 * @return Location Nearest Location
//	 * @see EvenlyGriddedGeographicRegionAPI.getNearestLocationClone(Location)
//	 */
//	public Location getNearestLocationClone(Location loc){
//		//Getting the nearest Location to the rupture point location
//
//
//		//getting the nearest Latitude.
//		double lat = Math.rint(loc.getLatitude() / gridSpacing) *
//		gridSpacing;
//		//getting the nearest Longitude.
//		double lon = Math.rint(loc.getLongitude() / gridSpacing) *
//		gridSpacing;
//
//		//throw exception if location is outside the region lat bounds.
//		if (!this.isLocationInside(loc))
//			return null;
//		else { //location is inside the polygon bounds but is outside the nice min/max lat/lon
//			//constraints then assign it to the nice min/max lat/lon.
//			if (lat < niceMinLat)
//				lat = niceMinLat;
//			else if (lat > niceMaxLat)
//				lat = niceMaxLat;
//			if (lon < niceMinLon)
//				lon = niceMinLon;
//			else if (lon > niceMaxLon)
//				lon = niceMaxLon;
//		}
//
//		return new Location(lat, lon);
//	}

	/**
	 * This returns the index of the grid location that is nearest to the one provided. 
	 * A value of -1 is returned if none is found.  
	 * Note that we convert the input lat & lon to the nearest equivalent grid lat & lon 
	 * (e.g., latNew = Math.round(lat/gridSpacing)*gridSpacing), and then return that index if the converted
	 *  location is inside the region.  This means that a location that originally is not in the region
	 * might get assigned to a grid point that's in the region, and a grid point that originally
	 * is in the region might get converted to a point that is not in the region.  Thus, a return
	 * of -1 here (grid point not found) does not necessarily mean that isLocationInside() will return
	 * false.
	 * @param loc Location Location to which we have to find the nearest location.
	 * @return int nearest location index in the gridded region.
	 * @see EvenlyGriddedGeographicRegionAPI.getNearestLocationIndex(Location)
	 */
//	public int getNearestLocationIndex(Location loc) {
//
//		double lat = Math.round(loc.getLatitude()/gridSpacing)*gridSpacing;
//		double lon = Math.round(loc.getLongitude()/gridSpacing)*gridSpacing;
//		//    double lon = loc.getLongitude();
//
//		//throw exception if location is outside the region lat bounds.
//		if (!isLocationInside(new Location(lat,lon)))
//			return -1;
//		else{ //location is inside the polygon bounds but is outside the nice min/max lat/lon
//			//constraints then assign it to the nice min/max lat/lon.
//			if (lat < niceMinLat)
//				lat = niceMinLat;
//			else if(lat > niceMaxLat)
//				lat = niceMaxLat;
//			if(lon < niceMinLon)
//				lon = niceMinLon;
//			else if(lon > niceMaxLon)
//				lon = niceMaxLon;
//		}
//
//		//getting the lat index
//		int latIndex = (int)Math.rint((lat - niceMinLat)/gridSpacing);
//		//number of locations below this latitude
//		int locIndex = locsBelowLat[latIndex];
//		ArrayList lonList = (ArrayList)lonsPerLatList.get(latIndex);
//
//		//System.out.println("Latindex="+latIndex);
//		// System.out.println("locIndex="+locIndex);
//
//		int size = lonList.size();
//		//iterating over all the lons for a given lat and finding the lon to the given lon.
//		for(int i=0;i<size;++i){
//			double latLon = ((Double)lonList.get(i)).doubleValue();
//			//System.out.println("Latlon="+latLon);
//			if (Math.abs(latLon - lon) <= gridSpacing/2) {
//				//System.out.println("Latlon="+latLon+", lon="+lon+",i="+i);
//				locIndex += i;
//				break;
//			}
//		}
//
//		return locIndex;
//	}

	/**
	 * Returns the index of the grid node associated with a given
	 * <code>Location</code> or -1 if the <code>Location</code> is 
	 * ouside this gridded region. For a <code>Location</code> to be 
	 * associated with a node it must fall within the square region 
	 * represented by the node on which the node is centered. Note that 
	 * this allows for some <code>Location</code>s that are outside the 
	 * region border to still be associated with a node.
	 * <br/><br/>
	 * The figure to the right and table below indicate the results produced 
	 * by calling <code>contains()</code> or <code>indexForLocation()</code>.
	 * The arrows in the figure point towards the interior of the 
	 * <code>Region</code>. The dots mark the centered <code>Location</code> 
	 * of each grid node and the numbers indicate the index value of each.
	 * Remember that both methods test for insidedness according to the
	 * rules defined in the {@link Shape} interface.
	 * <br/>
	 * <img style="padding: 20px; display: block; margin-left:auto; 
	 * margin-right:auto;" src="{@docRoot}/img/node_association.jpg"/>
	 * <br/>
	 * <table id="table-a">
	 *   <thead>
	 *     <tr>
	 *       <th>Location</th>
	 *       <th><code>contains(Location)</code></th>
	 *       <th><code>indexForLocation(Location)</code></th>
	 *     </tr>
	 *   <thead>
	 *   <tbody>
	 *     <tr><td><b>A</b></td><td><code>true</code></td><td>-1</td></tr>
	 *     <tr><td><b>B</b></td><td><code>false</code></td><td>3</td></tr>
	 *     <tr><td><b>C</b></td><td><code>false</code></td><td>3</td></tr>
	 *     <tr><td><b>D</b></td><td><code>false</code></td><td>-1</td></tr>
	 *     <tr><td><b>E</b></td><td><code>true</code></td><td>3</td></tr>
	 *     <tr><td><b>F</b></td><td><code>true</code></td><td>3</td></tr>
	 *     <tr><td><b>G</b></td><td><code>true</code></td><td>4</td></tr>
	 *   </tbody>
	 * </table>
	 * TODO rename indexForLocation
	 * 
	 * @param loc the <code>Location</code> to match to a grid node index
	 * @return the index of the associated node or -1 if no such node exists
	 */
	public int getNearestLocationIndex(Location loc) {
		int lonIndex = getNodeIndex(lonNodeEdges, loc.getLongitude());
		if (lonIndex == -1) return -1;
		int latIndex = getNodeIndex(latNodeEdges, loc.getLatitude());
		if (latIndex == -1) return -1;
		int gridIndex = ((latIndex) * lonNodes.length) + lonIndex;
		return gridIndices[gridIndex];
	}

	/*
	 * Creates tha list of the location for the Gridded Geographic Region.
	 */
	/*private void createGriddedLocationList(){
    double minLat=getMinLat();
    double maxLat=getMaxLat();
    double minLon=getMinLon();
    double maxLon=getMaxLon();


    Location tempLoc;

    //creates a instance of new locationList
    gridLocsList=new LocationList();

    // now loop over all grid points inside the max/min lat/lon and keep only those inside
    minLat = niceMinLat;

    while(minLat <= maxLat){
      minLon = niceMinLon;
      while(minLon <= maxLon){
        tempLoc = new Location(minLat,minLon);
        if (this.isLocationInside(tempLoc)) gridLocsList.addLocation(tempLoc);
        minLon+=gridSpacing;
      }
      minLat+=gridSpacing;
    }

    int i;
    if(D)
      for(i = 0; i < gridLocsList.size(); i++)
        System.out.println((float)gridLocsList.getLocationAt(i).getLatitude()+"  "+(float)gridLocsList.getLocationAt(i).getLongitude());

     }*/




	/**
	 * Creates the list of location in the gridded region and keeps it in the
	 * memory until cleared.
	 */
//	private void createGriddedLocationList() {
//
//		//creates a instance of new locationList
//		gridLocsList = new LocationList();
//		//number of gridLats
//		int lonsPerLatSize = lonsPerLatList.size();
//		//initialising the lat with the nice min lat
//		double lat = niceMinLat;
//		//iterating over all lons for each lat, and creating a Location list from it.
//		for (int i = 0; i < lonsPerLatSize; ++i) {
//			ArrayList lonList = (ArrayList) lonsPerLatList.get(i);
//			int numLons = lonList.size();
//			for (int j = 0; j < numLons; ++j) {
//				double lon = ( (Double) lonList.get(j)).doubleValue();
//				//creating a new location
//				Location loc = new Location(lat, lon);
//				gridLocsList.addLocation(loc);
//			}
//			//getting the next grid lat.
//			lat += spacing;
//		}
//	}

	/* implementation */
	/**
	 * Returns the minimum grid latitude.
	 * @return the minimum grid latitude
	 */
	public double getMinGridLat() {
		return latNodes[0];
	}

	/**
	 * Returns the maximum grid latitude.
	 * @return the maximum grid latitude
	 */
	public double getMaxGridLat() {
		return latNodes[latNodes.length-1];
	}

	/**
	 * Returns the minimum grid longitude.
	 * @return the minimum grid longitude
	 */
	public double getMinGridLon() {
		return lonNodes[0];
	}

	/**
	 * Returns the maximum grid longitude.
	 * @return the maximum grid longitude
	 */
	public double getMaxGridLon() {
		return lonNodes[lonNodes.length-1];
	}
	
	
//	/**
//	 * Returns the minimum Lat so that this gridLat/gridSpacing is an int,
//	 * and this min Lat is within the polygon;
//	 * @return double
//	 */
//	public double getMinGridLat() {
//		return niceMinLat;
//	}
//
//	/**
//	 * Returns the maximum Lat so that this gridLat/gridSpacing is an int,
//	 * and this max Lat is within the polygon;
//	 * @return double
//	 */
//	public double getMaxGridLat() {
//		return niceMaxLat;
//	}
//
//	/**
//	 * Returns the minimum Lon so that this gridLon/gridSpacing is an int,
//	 * and this min Lon is within the polygon;
//	 * @return double
//	 */
//	public double getMinGridLon() {
//		return niceMinLon;
//	}


	// TODO need to make sure this is writing properly before reintegration
	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(EvenlyGriddedGeographicRegion.XML_METADATA_NAME);
		xml.addAttribute(EvenlyGriddedGeographicRegion.XML_METADATA_GRID_SPACING_NAME, this.getGridSpacing()+"");
		xml.addAttribute(EvenlyGriddedGeographicRegion.XML_METADATA_NUM_POINTS_NAME, this.getNumGridLocs()+"");
		xml = super.toXMLMetadata(xml);

		return root;
	}

	// TODO need to make sure this is initializing properly before reintegration
	public static EvenlyGriddedGeographicRegion fromXMLMetadata(Element root) {
		double gridSpacing = Double.parseDouble(root.attribute(EvenlyGriddedGeographicRegion.XML_METADATA_GRID_SPACING_NAME).getValue());
		GeographicRegion geoRegion = GeographicRegion.fromXMLMetadata(root.element(GeographicRegion.XML_METADATA_NAME));
		LocationList outline = geoRegion.getRegionOutline();

		if (geoRegion.isRectangular()) {
			double minLat = geoRegion.getMinLat();
			double maxLat = geoRegion.getMaxLat();
			double minLon = geoRegion.getMinLon();
			double maxLon = geoRegion.getMaxLon();
//			try {
				return new EvenlyGriddedGeographicRegion(minLat, maxLat, minLon, maxLon, gridSpacing);
//			} catch (RegionConstraintException e) {
//				return new EvenlyGriddedGeographicRegion(outline, gridSpacing);
//			}
		}

		return new EvenlyGriddedGeographicRegion(outline,BorderType.MERCATOR_LINEAR, gridSpacing);
	}

	/*
	 * Returns the node index of the value or -1 if the value is 
	 * out of range. Expects the array of edge values.
	 */
	private static int getNodeIndex(double[] edgeVals, double value) {
		// If a value exists in an array, binary search returns the index
		// of the value. If the value is less than the lowest array value,
		// binary search returns -1. If the value is within range or 
		// greater than the highest array value, binary search returns
		// (-insert_point-1). The SHA rule of thumb follows the java rules
		// of insidedness, so any exact node edge value is associated with 
		// the node above. Therefore, the negative within range values are 
		// adjusted to the correct node index with (-idx-2). Below range
		// values are already -1; above range values are corrected to -1.
		int idx = Arrays.binarySearch(edgeVals, value);
		return (idx < -1) ? (-idx - 2) : (idx == edgeVals.length-1) ? -1 : idx;
	}

	/*
	 * Initilize the grid index array; a LocationList is not built until
	 * explicitely requested.
	 */
	private void initNodes() {
		int numGridPoints = lonNodes.length * latNodes.length;
		gridIndices = new int[numGridPoints];
		nodeList = new LocationList();
		Location dummy = new Location();
		int idx = 0;
		for (double lat:latNodes) {
			for (double lon:lonNodes) {
				dummy.setLatitude(lat);
				dummy.setLongitude(lon);
				if (isLocationInside(dummy)) {
					gridIndices[idx] = idx++;
					nodeList.addLocation(dummy.clone());
				}
				gridIndices[idx] = isLocationInside(dummy) ? idx++ : -1;
				
			}
		}
		nodeCount = idx;
	}

	/* Initialize internal grid node center and edge arrays */
	private void initLatLonArrays() {
		lonNodes = initNodeCenters(getMinLon(), getMaxLon(), spacing);
		latNodes = initNodeCenters(getMinLat(), getMaxLat(), spacing);
		lonNodeEdges = initNodeEdges(getMinLon(), getMaxLon(), spacing);
		latNodeEdges = initNodeEdges(getMinLat(), getMaxLat(), spacing);
	}

	/*
	 * Initializes an array of node centers. The first (lowest) bin is 
	 * centered on the min value.
	 */
	private static double[] initNodeCenters(
			double min, double max, double width) {
		// nodeCount is num intervals between min and max + 1
		int nodeCount = (int) Math.floor((max - min) / width) + 1;
		double firstCenterVal = min;
		return buildArray(firstCenterVal, nodeCount, width);
	}
	
	/* 
	 * Initializes an array of node edges which can be used to associate
	 * a value with a particular node using binary search.
	 */
	private static double[] initNodeEdges(
			double min, double max, double width) {
		// edges is binCount + 1
		int edgeCount = (int) Math.floor((max - min) / width) + 2;
		// offset first bin edge half a binWidth
		double firstEdgeVal = min - (width / 2);
		return buildArray(firstEdgeVal, edgeCount, width);
	}
	
	/* Node edge and center array builder. */
	private static double[] buildArray(
			double startVal, int count, double interval) {
		 
		double[] values = new double[count];
		double val = startVal;
		// store 'clean' values that do not reflect realities of
		// decimal precision, e.g. 34.5 vs. 34.499999999997, by forcing
		// meter-scale rounding precision.
		int scale = 5;
		for (int i=0; i<count; i++) {
			startVal = MathUtils.round(startVal, scale);
			values[i] = val;
			val += interval;
		}
		return values;
	}

	/*
	 * Main method to run the this class and produce a file with
	 * evenly gridded location.
	 */
	public static void main(String[] args) {
		/*LocationList locList = new LocationList();
    locList.addLocation(new Location(37.19, -120.61, 0.0));
    locList.addLocation(new Location(36.43, -122.09, 0.0));
    locList.addLocation(new Location(38.23, -123.61, 0.0));
    locList.addLocation(new Location(39.02, -122.08, 0.0));
    EvenlyGriddedGeographicRegion gridReg = new EvenlyGriddedGeographicRegion(
        locList, 0.05);
      try {
        FileWriter fw = new FileWriter("GeoRegionFile.txt");
        ListIterator it = gridReg.getGridLocationsIterator();
        while (it.hasNext()) {
          Location loc = (Location) it.next();
          fw.write(loc.toString() +"\n");
        }
        fw.close();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }*/
//		EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();
//		int index1 = region.getNearestLocationIndex(new Location(36.099999999999994, -122.8)); // 3117
//		int index2 = region.getNearestLocationIndex(new Location(36.099999999999994, -114.5)); // 3117
//		int index3 = region.getNearestLocationIndex(new Location(36.1, -114.5)); 
//		//int index1 = region.getNearestLocationIndex(new Location(36.099999999999994, -122.7)); // 3118
//		//int index2 = region.getNearestLocationIndex(new Location(36.099999999999994, -114.6)); // 3199
//		System.out.println(index1+","+index2+","+index3);\
		
//		LocationList LL1 = new LocationList();
//		LL1.addLocation(new Location(39.97,-117.62));
//		LL1.addLocation(new Location(39.97,-117.58));
//		LL1.addLocation(new Location(40.07,-117.52));
//		LL1.addLocation(new Location(40.07,-117.38));
//		LL1.addLocation(new Location(40.21,-117.38));
//		LL1.addLocation(new Location(40.21,-117.52));
//		LL1.addLocation(new Location(40.12,-117.62));
//
//		LocationList LL2 = new LocationList();
//		LL2.addLocation(new Location(39.97,-117.62));
//		LL2.addLocation(new Location(39.97,-117.5));
//		LL2.addLocation(new Location(40.10,-117.38));
//		LL2.addLocation(new Location(40.21,-117.5));
//		LL2.addLocation(new Location(40.21,-117.62));
//		LL2.addLocation(new Location(40.18,-117.62));
//		LL2.addLocation(new Location(40.10,-117.5));
//		LL2.addLocation(new Location(40.18,-117.52));
//
//		LocationList LL3 = new LocationList();
//		LL3.addLocation(new Location(39.97,-117.62));
//		LL3.addLocation(new Location(39.97,-117.58));
//		LL3.addLocation(new Location(40.07,-117.5));
//		LL3.addLocation(new Location(39.97,-117.42));
//		LL3.addLocation(new Location(39.97,-117.38));
//		LL3.addLocation(new Location(40.10,-117.38));
//		LL3.addLocation(new Location(40.22,-117.5));
//		LL3.addLocation(new Location(40.10,-117.62));
//
//		EvenlyGriddedGeographicRegion eggr1 = new EvenlyGriddedGeographicRegion(LL1,BorderType.MERCATOR_LINEAR,0.1);
//		EvenlyGriddedGeographicRegion eggr2 = new EvenlyGriddedGeographicRegion(LL2,BorderType.MERCATOR_LINEAR,0.1);
//		EvenlyGriddedGeographicRegion eggr3 = new EvenlyGriddedGeographicRegion(LL3,BorderType.MERCATOR_LINEAR,0.1);
//		
//		System.out.println(eggr1.getNearestLocationIndex(new Location(40.16,-117.44)));
//		System.out.println(eggr2.getGridLocation(3));
//		System.out.println(eggr3.getGridLocation(3));
//		
//		Location pp = new Location(40.16,-117.44);
//		System.out.println((int)Math.rint((pp.getLatitude() - eggr1.niceMinLat)/eggr1.gridSpacing));
//		System.out.println(new ToStringBuilder(eggr1.locsBelowLat).append(eggr1.locsBelowLat).toString());
}

	
}
