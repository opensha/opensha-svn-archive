package org.opensha.commons.data.region;

import java.awt.Shape;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.math.util.MathUtils;
import org.dom4j.Element;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.exceptions.InvalidRangeException;


/**
 * A <code>GriddedRegion</code> is a <code>Region</code> that has been evenly
 * discretized in latitude and longitude. Each node in a gridded region
 * represents a small area that is an equal number of degrees in width and
 * height and is identified by a unique {@link Location} at the geographic
 * (lat-lon) center of the node. <img style="padding: 30px 40px; float: right;" 
 * src="{@docRoot}/img/gridded_regions_border.jpg"/> In the adjacent figure,
 * the heavy black line marks the border of the <code>Region</code> . The 
 * light gray dots mark the <code>Location</code>s of nodes outside the region,
 * and black dots those inside the region. The dashed grey line marks the
 * border, inside which, a <code>Location</code> will be associated with a
 * grid node. See {@link 
 * GriddedRegion#indexForLocation(Location)} 
 * for more details on rules governing whether a grid node is inside a region
 * and whether a <code>Location</code> will be associated with a grid node.<br/>
 * <br/>
 * A <code>GriddedRegion</code> may be initialized several ways (e.g. as a
 * circle, an area of uniform degree-width and -height, or a buffer around
 * a linear feature). See individual constructors for illustrative examples.
 * The <code>Location</code>s of the grid nodes are indexed
 * internally in order of increasing longitude then latitude starting with
 * the node at the lowest latitude and longitude in the region.<br/>
 * <br/>
 * To ensure grid nodes fall on specific lat-lon values, all constructors
 * take an anchor <code>Location</code> argument. This location can be
 * anywhere in- or outside the region to be gridded. If the region contains
 * the anchor location, the anchor will coincide with a grid node.  For
 * example, given a grid spacing of 1&deg; and an anchor <code>Location</code>
 * of 22.1&deg;N -134.7&deg;W, grid nodes within any region will fall at
 * whole valued latitudes + 0.1&deg; and longitudes - 0.7&deg;. If an anchor
 * <code>Location</code> is <code>null</code>, it is automatically set as
 * the Location defined by the minimum latitude and longitude of the region's
 * border.<br/>
 * <br/>
 * <br/>
 * 
 * @author Nitin Gupta
 * @author Vipin Gupta 
 * @author Peter Powers
 * @version $Id$
 * @see Region
 */

public class GriddedRegion extends Region implements Iterable<Location> {

	private static final long serialVersionUID = 1L;

	public final static String XML_METADATA_NAME = "evenlyGriddedGeographicRegion";
	public final static String XML_METADATA_GRID_SPACING_NAME = "spacing";
	public final static String XML_METADATA_ANCHOR_NAME = "anchor";
	public final static String XML_METADATA_NUM_POINTS_NAME = "numPoints";

	public final static Location ANCHOR_0_0 = Location.immutableLocation(0,0);
	
	// the lat-lon arrays of node points
	// TODO may not need the nodes if LocList is always initialized
	private double[] lonNodes;
	private double[] latNodes;
	
	// the lat-lon arrays of node edges
	private double[] lonNodeEdges;
	private double[] latNodeEdges;

	// Location at lower left corner of region bounding rect
	private Location anchor;
	
	// lookup array for grid nodes; has length of master grid spanning
	// region bounding box; all nodes outside region have values of -1;
	// all valid nodes point to position in nodeList; gridIndices increase
	// across and then up
	private int[] gridIndices;
	private BitSet validIndices;
	
	// list of nodes
	private LocationList nodeList;
	
	// dimensions
	private double spacing;
	private int nodeCount;

	/**
	 * Initializes a <code>GriddedRegion</code> from a pair of <code>
	 * Location</code>s. When viewed in a Mercator projection, the 
	 * region will be a rectangle. If either both latitude or both longitude
	 * values are the same, an exception is thrown.<br/>
	 * <br/>
	 * <b>Note:</b> In an exception to the rules of insidedness defined
	 * in the {@link Shape} interface, <code>Location</code>s that fall on
	 * northern or eastern borders of this region are considered inside. See 
	 * {@link Region#Region(Location, Location)} for
	 * implementation details.
	 * 
	 * @param loc1 the first <code>Location</code>
	 * @param loc2 the second <code>Location</code>
	 * @param spacing of grid nodes
	 * @param anchor <code>Location</code> for grid; may be <code>null</code>
	 * @throws IllegalArgumentException if the latitude or longitude values
	 * 		in the <code>Location</code>s provided are the same or 
	 * 		<code>spacing</code> is outside the range 0&deg; &lt; <code>spacing
	 * 		</code> &le; 5&deg;
	 * @throws NullPointerException if either <code>Location</code> argument
	 * 		is <code>null</code>
	 * @see Region#Region(Location, Location)
	 */
	public GriddedRegion(
			Location loc1, 
			Location loc2, 
			double spacing, 
			Location anchor) {
		super(loc1, loc2);
		initGrid(spacing, anchor);
	}

	/**
	 * Initializes a <code>GriddedRegion</code> from a list of border locations.
	 * The border type specifies whether lat-lon values are treated as points
	 * in an orthogonal coordinate system or as connecting great circles.
	 * 
	 * @param border Locations
	 * @param type the {@link BorderType} to use when initializing;
	 * 		a <code>null</code> value defaults to 
	 * 		<code>BorderType.MERCATOR_LINEAR</code>
	 * @param spacing of grid nodes
	 * @param anchor <code>Location</code> for grid; may be <code>null</code>
	 * @throws IllegalArgumentException if the <code>border</code> does not 
	 * 		have at least 3 points or <code>spacing</code> is outside the 
	 * 		range 0&deg; &lt; <code>spacing</code> &le; 5&deg;
	 * @throws NullPointerException if the <code>border</code> is 
	 * 		<code>null</code>
	 * @see Region#Region(LocationList, BorderType)
	 */
	public GriddedRegion(
			LocationList border, 
			BorderType type, 
			double spacing, 
			Location anchor) {
		super(border, type);
		initGrid(spacing, anchor);
	}
	
	/**
	 * Initializes a circular <code>GriddedRegion</code>. Internally,
	 * the centerpoint and radius are used to create a circular region
	 * composed of straight line segments that span 10&deg; wedges. 
	 * <img style="padding: 30px 40px; float: right;" 
	 * src="{@docRoot}/img/gridded_regions_circle.jpg"/> In 
	 * the adjacent figure, the heavy black line marks the border of the 
	 * <code>Region</code>. The light gray dots mark the <code>Location</code>s
	 * of nodes outside the region, and black dots those inside the region.
	 * The dashed grey line marks the border, inside which, a 
	 * <code>Location</code> will be associated with a grid node. See {@link 
	 * GriddedRegion#indexForLocation(Location)} 
	 * for more details on rules governing whether a grid node is inside
	 * a region and whether a <code>Location</code> will be associated 
	 * with a grid node.<br/>
	 * <br/>
	 * 
	 * @param center of the circle
	 * @param radius of the circle
	 * @param spacing of grid nodes
	 * @param anchor <code>Location</code> for grid; may be <code>null</code>
	 * @throws IllegalArgumentException if <code>radius</code> is outside the
	 * 		range 0 km &lt; <code>radius</code> &le; 1000 km or <code>spacing
	 * 		</code> is outside the range 0&deg; &lt; <code>spacing</code> 
	 * 		&le; 5&deg;
	 * @throws NullPointerException if <code>center</code> is null
	 * @see Region#Region(Location, double)
	 */
	public GriddedRegion(
			Location center, 
			double radius, 
			double spacing, 
			Location anchor) {
		super(center, radius);
		initGrid(spacing, anchor);
	}

	/**
	 * Initializes a <code>GriddedRegion</code> as a buffered area around a
	 * line. In the adjacent figure, the heavy black line marks the border of 
	 * the <code>Region</code>. <img style="padding: 30px 40px; float: right;" 
	 * src="{@docRoot}/img/gridded_regions_buffer.jpg"/> The light gray 
	 * dots mark the <code>Location</code>s of nodes
	 * outside the region, and black dots those inside the region.
	 * The dashed grey line marks the border, inside which, a 
	 * <code>Location</code> will be associated with a grid node. See {@link 
	 * GriddedRegion#indexForLocation(Location)} 
	 * for more details on rules governing whether a grid node is inside
	 * a region and whether a <code>Location</code> will be associated 
	 * with a grid node.<br/><br/>
	 * <br/>
	 * 
	 * @param line at center of buffered region
	 * @param buffer distance from line
	 * @param spacing of grid nodes
	 * @param anchor <code>Location</code> for grid; may be <code>null</code>
	 * @throws NullPointerException if <code>line</code> is null
	 * @throws IllegalArgumentException if <code>buffer</code> is outside the
	 * 		range 0 km &lt; <code>buffer</code> &le; 500 km or <code>spacing
	 * 		</code> is outside the range 0&deg; &lt; <code>spacing</code> 
	 * 		&le; 5&deg;
	 * @see Region#Region(LocationList, double)
	 */
	public GriddedRegion(
			LocationList line, 
			double buffer, 
			double spacing, 
			Location anchor) {
		super(line, buffer);
		initGrid(spacing, anchor);
	}
	
	/**
	 * Initializes a <code>GriddedRegion</code> with a 
	 * <code>Region</code>.
	 * 
	 * @param region to use as border for new gridded region
	 * @param spacing of grid nodes
	 * @param anchor <code>Location</code> for grid; may be <code>null</code>
	 * @throws IllegalArgumentException if <code>spacing
	 * 		</code> is outside the range 0&deg; &lt; <code>spacing</code> 
	 * 		&le; 5&deg;
	 * @throws NullPointerException if <code>region</code> is <code>null</code>
	 * @see Region#Region(Region)
	 */
	public GriddedRegion(
			Region region, 
			double spacing,
			Location anchor) {
		super(region.getBorder(), BorderType.MERCATOR_LINEAR);
		initGrid(spacing, anchor);
	}


	/**
	 * Returns the grid node spacing for this region.
	 * @return the grid node spacing (in degrees)
	 */
	public double getSpacing() {
		return spacing;
	}

	/**
	 * Returns the total number of grid nodes in this region.
	 * @return the number of grid nodes
	 */
	public int getNodeCount() {
		return nodeCount;
	}

	/**
	 * Returns whether this region contains any grid nodes. If a regions
	 * dimensions are smaller than the grid spacing, it may be empty.
	 * @return <code>true</code> if region has no grid nodes; 
	 * 		<code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return nodeCount == 0;
	}
	
    /**
     * Returns whether this <code>GriddedRegion</code> and another are of equal 
     * aerial extent and have the same set of grid nodes.
     * 
     * @param gr the <code>Region</code> to compare this <code>Region</code> to
     * @return <code>true</code> if the two Regions are the same;
     *		<code>false</code> otherwise.
	 */
	public boolean equals(GriddedRegion gr) {
		if (!super.equals(gr)) return false;
		if (!anchor.equals(gr.anchor)) return false;
		if (spacing != gr.spacing) return false;
		return true;
	}

	/**
	 * Creates a new <code>GriddedRegion</code> from this (the parent) and 
	 * another <code>Region</code>. The border of the new region is the 
	 * intersection of the borders of the parent and the passed-in region.
	 * The new region also inherits the grid spacing and node-alignment of 
	 * the parent. The method returns <code>null</code> if the new gridded 
	 * region is devoid of grid nodes or if the two regions do not overlap.
	 * 
	 * @param region to use as border for sub-region
	 * @return a new GriddedRegion
	 */
	// TODO wrtite tests
	public GriddedRegion subRegion(Region region) {
		Region newRegion = Region.intersect(this, region);
		if (newRegion == null) return null;
		GriddedRegion newGriddedRegion = 
			new GriddedRegion(newRegion, spacing, anchor);
		return (newGriddedRegion.isEmpty()) ? null : newGriddedRegion;
	}
	
	/**
	 * Returns the iterator.
	 * TODO kill; users can get this once they've gotten the location list
	 * @return an iterator
	 */
	public ListIterator<Location> getGridLocationsIterator() {
		return nodeList.listIterator();
	}
	/* implementation: iterator traverses bitset of valid nodes */
	public Iterator<Location> iterator() {
		
		Iterator<Location> it = new Iterator<Location>() {
			
			private Location loc = new Location();
			private int idx = 0;
			private int lastIdx = nodeCount - 1;
			private int latIdx,lonIdx;
			
			/* implementation */
			public boolean hasNext() {
				return idx < lastIdx;
			}
			
			/* implementation */
			public Location next() {
				idx = validIndices.nextSetBit(idx);
				if (idx == -1) throw new NoSuchElementException();
				latIdx = idx / lonNodes.length;
				lonIdx = idx % lonNodes.length;
				loc.setLatitude(latNodes[latIdx]);
				loc.setLongitude(lonNodes[lonIdx]);
				idx += 1;
				return loc;
			}
			
			/* implementation */
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
		return it;
	}

	/**
	 * Returns the locations of all the nodes in the region as a
	 * <code>LocationList</code>.
	 * @return a list of all the node locations in the region.
	 */
	public LocationList getNodeList() {
		return nodeList;
	}
	
	/**
	 * Returns the <code>Location</code> at a given grid index.
	 * TODO InvalidRangeException should be retired in favor of default IndexOutOfBounds
	 * 
	 * This method is intended for random access of nodes in this gridded
	 * region; to cycle over all nodes, iterate over the region.
	 * 
	 * @param index of location to retrieve
	 * @return the <code>Location</code> or <code>null</code> if index is
	 * 		out of range
	 */
	public Location locationForIndex(int index) {
		try {
			return nodeList.getLocationAt(index);
		} catch (InvalidRangeException e) {
			return null;
		}
	}

	/**
	 * Returns the index of the grid node associated with a given
	 * <code>Location</code> or -1 if the <code>Location</code> is 
	 * ouside this gridded region. For a <code>Location</code> to be 
	 * associated with a node it must fall within the square region 
	 * represented by the node on which the node is centered. Note that 
	 * this allows for some <code>Location</code>s that are outside the 
	 * region border to still be associated with a node.
	 * <br/><br/>
	 * The figure and table below indicate the results produced 
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
	 * 
	 * @param loc the <code>Location</code> to match to a grid node index
	 * @return the index of the associated node or -1 if no such node exists
	 */
	public int indexForLocation(Location loc) {
		int lonIndex = getNodeIndex(lonNodeEdges, loc.getLongitude());
		if (lonIndex == -1) return -1;
		int latIndex = getNodeIndex(latNodeEdges, loc.getLatitude());
		if (latIndex == -1) return -1;
		int gridIndex = ((latIndex) * lonNodes.length) + lonIndex;
		return gridIndices[gridIndex];
	}

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
	
	@Override
	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(GriddedRegion.XML_METADATA_NAME);
		xml.addAttribute(GriddedRegion.XML_METADATA_GRID_SPACING_NAME, this.getSpacing()+"");
		Element xml_anchor = root.addElement(GriddedRegion.XML_METADATA_ANCHOR_NAME);
		xml = anchor.toXMLMetadata(xml_anchor);
		xml.addAttribute(GriddedRegion.XML_METADATA_NUM_POINTS_NAME, this.getNodeCount()+"");
		xml = super.toXMLMetadata(xml);

		return root;
	}

	/**
	 * Initializes a new <code>Region</code> from stored metadata.
	 * @param root metadata element
	 * @return a <code>GriddedRegion</code>
	 */
	public static GriddedRegion fromXMLMetadata(Element root) {
		double gridSpacing = Double.parseDouble(root.attribute(GriddedRegion.XML_METADATA_GRID_SPACING_NAME).getValue());
		Region geoRegion = Region.fromXMLMetadata(root.element(Region.XML_METADATA_NAME));
		LocationList outline = geoRegion.getBorder();
		Location xml_anchor = Location.fromXMLMetadata(root.element(XML_METADATA_ANCHOR_NAME).element(Location.XML_METADATA_NAME));
		return new GriddedRegion(
				outline,BorderType.MERCATOR_LINEAR, gridSpacing, xml_anchor);
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

	/* grid setup */
	private void initGrid(double spacing, Location anchor) {
		setSpacing(spacing);
		setAnchor(anchor);
		initLatLonArrays();
		initNodes();
	}

	/* Sets the gid node spacing. */
	private void setSpacing(double spacing) {
		if (spacing <= 0 || spacing > 5) {
			throw new IllegalArgumentException(
					"Grid spacing must be in the range " + 
					"0\u00B0 \u003E S \u2265 5\u00B0");
		}
		this.spacing = spacing;
	}

	/*
	 * Sets the grid anchor value. If null, the anchor is the min lat and lon 
	 * of the region. If not null, the Location provided is adjusted to be the
	 * lower left corner (min lat-lon) of the region bounding grid. If the
	 * region grid extended infinitely, both the input and adjusted anchor
	 * Locations would coincide with grid nodes.
	 */
	private void setAnchor(Location anchor) {
		if (anchor == null) {
			this.anchor = Location.immutableLocation(getMinLat(), getMinLon());
		} else {
			double newLat = computeAnchor(
					getMinLat(), anchor.getLatitude(), spacing);
			double newLon = computeAnchor(
					getMinLon(), anchor.getLongitude(), spacing);
			this.anchor = Location.immutableLocation(newLat, newLon);
		}
	}

	/* Computes adjusted anchor values. */
	private static double computeAnchor(
			double min, double anchor, double spacing) {
		double delta = anchor - min;
		double num_div = Math.floor(delta/spacing);
		double offset = delta - num_div*spacing;
		double newAnchor = min + offset;
		return (newAnchor < min) ? newAnchor + spacing : newAnchor;
	}

	/*
	 * Initilize the grid index array.
	 * TODO do away with storing loc list; most requests for the grid
	 * location list pass it off to some other method that expects and
	 * then iterates over a LocationList. Such methods should be reconfigured
	 * to accept Iterable<Location> 
	 */
	private void initNodes() {
		int numGridPoints = lonNodes.length * latNodes.length;
		gridIndices = new int[numGridPoints];
		nodeList = new LocationList();
		Location dummy = new Location(); // utility Location
		int node_idx = 0;
		int grid_idx = 0;
		for (double lat:latNodes) {
			for (double lon:lonNodes) {
				dummy.setLatitude(lat);
				dummy.setLongitude(lon);
				if (contains(dummy)) {
					nodeList.addLocation(dummy.copy());
					gridIndices[grid_idx] = node_idx++;
				} else {
					gridIndices[grid_idx] = -1;
				}
				grid_idx++;
			}
		}
		nodeCount = node_idx;
	}

	/* Initialize internal grid node center and edge arrays */
	private void initLatLonArrays() {
		lonNodes = initNodeCenters(anchor.getLongitude(), getMaxLon(), spacing);
		latNodes = initNodeCenters(anchor.getLatitude(), getMaxLat(), spacing);
		lonNodeEdges = initNodeEdges(getMinLon(), getMaxLon(), spacing);
		latNodeEdges = initNodeEdges(getMinLat(), getMaxLat(), spacing);
		
		// TODO clean
//		ToStringBuilder tsb = new ToStringBuilder(lonNodes);
//		System.out.println(tsb.append(lonNodes).toString());
//		tsb = new ToStringBuilder(latNodes);
//		System.out.println(tsb.append(latNodes).toString());
//		tsb = new ToStringBuilder(lonNodeEdges);
//		System.out.println(tsb.append(lonNodeEdges).toString());
//		tsb = new ToStringBuilder(latNodeEdges);
//		System.out.println(tsb.append(latNodeEdges).toString());

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
		int scale = 5;
		double val = startVal;
		for (int i=0; i<count; i++) {
			// store 'clean' values that do not reflect realities of
			// decimal precision, e.g. 34.5 vs. 34.499999999997, by forcing
			// meter-scale rounding precision.
			values[i] = MathUtils.round(val, scale);
			val += interval;
		}
		return values;
	}
	
	/*
	 * Main method to run the this class and produce a file with
	 * evenly gridded location.
	 */
	public static void main(String[] args) {
		
		//TODO use this as indexing test
		LocationList ll = new LocationList();
		ll.addLocation(new Location(35.0,-123.8));
		ll.addLocation(new Location(35.0,-123.4));
		ll.addLocation(new Location(35.4,-123.0));
		ll.addLocation(new Location(35.8,-123.0));
		ll.addLocation(new Location(37.0,-124.2));
		ll.addLocation(new Location(37.0,-124.6));
		ll.addLocation(new Location(36.6,-125.0));
		ll.addLocation(new Location(36.2,-125.0));
		GriddedRegion eggr = new GriddedRegion(
				ll, null, 0.5, null);
		
		ToStringBuilder tsb = new ToStringBuilder(eggr.gridIndices).append(eggr.gridIndices);
		System.out.println(tsb.toString());
		
		CaliforniaRegions.RELM_GRIDDED crg = new CaliforniaRegions.RELM_GRIDDED();
		Location tmp = new Location(42.7,-125.2);
		System.out.println(tmp);
		System.out.println(crg.contains(tmp));

//		PathIterator pi = crg.area.getPathIterator(null);
//		// an Area throws a double[] at a GeneralPath. The PathIterator provided
//		// by GeneralPath upconverts it's floats to double. blech.
//		double[] vertex = new double[6]; 
//		while (!pi.isDone()) {
//			int segType = pi.currentSegment(vertex);
//			System.out.println(new ToStringBuilder(vertex).append(vertex).toString());
//			pi.next();
//		}

		
		
		
//		double anchorLat = -34.67;
//		double anchorLon = 45.71;
//
////		double anchorLat = 22.6;
////		double anchorLon = -122.2;
//
//		double minLat = 20;
//		double minLon = 20;
//		
//		double spacing = 0.2;
		
		// expecting 20.07 20.11
//		double startLat = adjustAnchor(minLat,anchorLat,spacing);
//		double startLon = adjustAnchor(minLon,anchorLon,spacing);
		
			
//		System.out.println(startLat);
//		System.out.println(startLon);
		
		/*LocationList locList = new LocationList();
    locList.addLocation(new Location(37.19, -120.61, 0.0));
    locList.addLocation(new Location(36.43, -122.09, 0.0));
    locList.addLocation(new Location(38.23, -123.61, 0.0));
    locList.addLocation(new Location(39.02, -122.08, 0.0));
    GriddedRegion gridReg = new GriddedRegion(
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
//		//em.out.println(index1+","+index2+","+index3);\
		
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
//		GriddedRegion eggr1 = new GriddedRegion(LL1,BorderType.MERCATOR_LINEAR,0.1);
//		GriddedRegion eggr2 = new GriddedRegion(LL2,BorderType.MERCATOR_LINEAR,0.1);
//		GriddedRegion eggr3 = new GriddedRegion(LL3,BorderType.MERCATOR_LINEAR,0.1);
//		
//		System.out.println(eggr1.getNearestLocationIndex(new Location(40.16,-117.44)));
//		System.out.println(eggr2.getGridLocation(3));
//		System.out.println(eggr3.getGridLocation(3));
//		
//		Location pp = new Location(40.16,-117.44);
//		System.out.println((int)Math.rint((pp.getLatitude() - eggr1.niceMinLat)/eggr1.gridSpacing));
//		System.out.println(new ToStringBuilder(eggr1.locsBelowLat).append(eggr1.locsBelowLat).toString());

		// farting around with bitsets
//		BitSet bs = new BitSet(17);
//		bs.set(3, 8);
//		bs.set(12, 15);
//		bs.set(0);
//		//bs.set(17);
//		System.out.println(bs.get(15)); // false
//		System.out.println(bs.get(14)); // true
//		System.out.println(bs.get(17)); // true
//		System.out.println(bs.cardinality());  // 8
//		System.out.println(bs.size()); // 17
//		System.out.println(bs.length()); //15 
//		System.out.println(bs.nextSetBit(0));
		//TODO clean
}


	
}
