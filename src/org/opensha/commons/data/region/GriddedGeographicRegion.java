package org.opensha.commons.data.region;

import java.util.Arrays;
import java.util.ListIterator;

import org.apache.commons.math.util.MathUtils;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * A <code>GriddedGeopgraphicRegion</code> is a {@link GeographicRegion} that
 * contains evenly spaced (in decimal degrees) location nodes. These nodes may
 * be used for reference ...
 *
 * <br/>
 * <br/>
 * Internally, nodes in a gridded region are referenced to points within the
 * bounding box of the region. Only nodes that fall 
 * 
 * @author Peter Powers
 * @version $Id:$
 * TODO delete
 */
@Deprecated
public class GriddedGeographicRegion extends GeographicRegion {
//
//	// the lat-lon arrays of node points
//	// TODO may not need the nodes if LocList is always initialized
//	private double[] lonNodes;
//	private double[] latNodes;
//	
//	// the lat-lon arrays of node edges
//	private double[] lonNodeEdges;
//	private double[] latNodeEdges;
//
//	private Location anchor;
//	
//	// lookup array for grid nodes; has length of master grid spanning
//	// region bounding box; all nodes outside region have values of -1;
//	// all valid nodes point to position in nodeList; gridIndices increase
//	// across and then up
//	private int[] gridIndices;
//	
//	// list of nodes
//	private LocationList nodeList;
//	
//	// dimensions
//	double spacing;
//	int nodeCount;
//	
//	public GriddedGeographicRegion(GeographicRegion gr, double spacing) {
//		super(gr.getRegionOutline(), BorderType.MERCATOR_LINEAR);
//		this.spacing = spacing;
//		initLatLonArrays();
//		initNodes();
//	}
//	
//	public GriddedGeographicRegion(
//			LocationList border, BorderType type, double spacing) {
//		super(border, type);
//		
//	}
	
//	/* implementation */
//	public LocationList createRegionLocationsList(EvenlyGriddedGeographicRegionAPI region) {
//		return null;
//		// TODO do nothing
//		
//	}

	/* implementation */
//	public Location getGridLocation(int index) {
//		return nodeList.getLocationAt(index);
//		// TODO rename to getNode
//	}
	
	/* implementation */
//	public int getNearestLocationIndex(Location loc) {
//		int lonIndex = getNodeIndex(lonNodeEdges, loc.getLongitude());
//		if (lonIndex == -1) return -1;
//		int latIndex = getNodeIndex(latNodeEdges, loc.getLatitude());
//		if (latIndex == -1) return -1;
//		int gridIndex = ((latIndex) * lonNodes.length) + lonIndex;
//		return gridIndices[gridIndex];
//		
//		// TODO rename to getNodeIndex
//		
//	}

//	/* implementation */
//	public int getNumGridLocs() {
//		return nodeCount;
//		// TODO rename getNodeCount
//		
//	}

	/* implementation */
//	public ListIterator<Location> getGridLocationsIterator() {
//		return nodeList.listIterator();
//		// TODO kill?
//	}

//	/* implementation */
//	public LocationList getGridLocationsList() {
//		return nodeList;
//		// TODO investigate whether this is really the most efficient way;
//		// prior implementation waited for a nodeList request before building
//	}

//	/* implementation */
//	public double getGridSpacing() {
//		return spacing;
//	}
	
	/* implementation */
//	public void setGridSpacing(double degrees) {
//		// TODO reimplement in callers and kill
//	}

//	/* implementation */
//	public double getMinGridLat() {
//		return latNodes[0];
//	}
//
//	/* implementation */
//	public double getMaxGridLat() {
//		return latNodes[latNodes.length-1];
//	}
//
//	/* implementation */
//	public double getMinGridLon() {
//		return lonNodes[0];
//	}
//
//	/* implementation */
//	public double getMaxGridLon() {
//		return lonNodes[lonNodes.length-1];
//	}

	
	/*
	 * Given an index in the master bounding grid, return the Location of the
	 * associated node. Does not check if idx falls outside region border
	 */
//	private Location getLocationForIndex(int idx) {
//		if 
//	}
	
//	public Location getNode(Location loc) {
//		
//	}
	
	
//	/*
//	 * Initilize the grid index array; a LocationList is not built until
//	 * explicitely requested.
//	 */
//	private void initNodes() {
//		int numGridPoints = lonNodes.length * latNodes.length;
//		gridIndices = new int[numGridPoints];
//		nodeList = new LocationList();
//		Location dummy = new Location();
//		int idx = 0;
//		for (double lat:latNodes) {
//			for (double lon:lonNodes) {
//				dummy.setLatitude(lat);
//				dummy.setLongitude(lon);
//				if (isLocationInside(dummy)) {
//					gridIndices[idx] = idx++;
//					nodeList.addLocation(dummy.clone());
//				}
//				gridIndices[idx] = isLocationInside(dummy) ? idx++ : -1;
//				
//			}
//		}
//		nodeCount = idx;
//	}
	
//	/* Initialize internal grid node center and edge arrays */
//	private void initLatLonArrays() {
//		lonNodes = initNodeCenters(getMinLon(), getMaxLon(), spacing);
//		latNodes = initNodeCenters(getMinLat(), getMaxLat(), spacing);
//		lonNodeEdges = initNodeEdges(getMinLon(), getMaxLon(), spacing);
//		latNodeEdges = initNodeEdges(getMinLat(), getMaxLat(), spacing);
//	}
//	
//	/*
//	 * Returns the node index of the value or -1 if the value is 
//	 * out of range. Expects the array of edge values.
//	 */
//	private static int getNodeIndex(double[] edgeVals, double value) {
//		// If a value exists in an array, binary search returns the index
//		// of the value. If the value is less than the lowest array value,
//		// binary search returns -1. If the value is within range or 
//		// greater than the highest array value, binary search returns
//		// (-insert_point-1). The SHA rule of thumb follows the java rules
//		// of insidedness, so any exact node edge value is associated with 
//		// the node above. Therefore, the negative within range values are 
//		// adjusted to the correct node index with (-idx-2). Below range
//		// values are already -1; above range values are corrected to -1.
//		int idx = Arrays.binarySearch(edgeVals, value);
//		return (idx < -1) ? (-idx - 2) : (idx == edgeVals.length-1) ? -1 : idx;
//	}
	
//	/*
//	 * Initializes an array of node centers. The first (lowest) bin is 
//	 * centered on the min value.
//	 */
//	private static double[] initNodeCenters(
//			double min, double max, double width) {
//		// nodeCount is num intervals between min and max + 1
//		int nodeCount = (int) Math.floor((max - min) / width) + 1;
//		double firstCenterVal = min;
//		return buildArray(firstCenterVal, nodeCount, width);
//	}
//	
//	/* 
//	 * Initializes an array of node edges which can be used to associate
//	 * a value with a particular node using binary search.
//	 */
//	private static double[] initNodeEdges(
//			double min, double max, double width) {
//		// edges is binCount + 1
//		int edgeCount = (int) Math.floor((max - min) / width) + 2;
//		// offset first bin edge half a binWidth
//		double firstEdgeVal = min - (width / 2);
//		return buildArray(firstEdgeVal, edgeCount, width);
//	}
//	
//	/* Node edge and center array builder. */
//	private static double[] buildArray(
//			double startVal, int count, double interval) {
//		 
//		double[] values = new double[count];
//		double val = startVal;
//		// store 'clean' values that do not reflect realities of
//		// decimal precision, e.g. 34.5 vs. 34.499999999997, by forcing
//		// meter-scale rounding precision.
//		int scale = 5;
//		for (int i=0; i<count; i++) {
//			startVal = MathUtils.round(startVal, scale);
//			values[i] = val;
//			val += interval;
//		}
//		return values;
//	}

	// TODO clean
	public static void main(String[] args) {
		int j = 6;
		System.out.println(j++);
		System.out.println(j);
	}
}
