package org.opensha.commons.data.region;
import static org.opensha.commons.calc.RelativeLocation.PI_BY_2;
import static org.opensha.commons.calc.RelativeLocation.TO_RAD;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.text.DecimalFormat;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.math.util.MathUtils;
import org.dom4j.Element;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.sha.earthquake.EqkRupture;

/**
 * A <code>Region</code> is a polygonal area on the surface of the earth. The
 * vertices comprising the border of each region are stored internally as 
 * latitude-longitude coordinate pairs in an {@link java.awt.geom.Area}, 
 * facilitating operations such as union, intersect, and contains. Insidedness
 * rules follow those defined in the {@link java.awt.Shape} interface.<br/>
 * <br/>
 * Some constructors require the specification of a {@link BorderType}. If one
 * wishes to define a geographic region that represents a rectangle in a
 * Mercator projection, {@link BorderType#MERCATOR_LINEAR} should be used,
 * otherwise, the border will follow a {@link BorderType#GREAT_CIRCLE} between
 * two points. Over small distances, great circle paths are approximately the 
 * same as linear, Mercator paths. Over longer distances, a great circle is a
 * better representation of a line on a globe. Internally, great circles
 * are approximated by multple straight line segments that have a maximum
 * length of 100km.<br/>
 * <br/>
 * 
 * TODO make border immutable; collection.unmodifiablelist; make LocatioList 
 * extend arrayList, not wrap it. Perhaps when initing border, create new list
 * of immutable location objects and make the list itself immutable.
 * <br/><br/>
 * <b>Note:</b> The current implementation does not support regions that are
 * intended to span &#177;180&deg;. Any such regions will wrap the
 * long way around the earth and results are undefined. This also applies to
 * regions that encircle either pole.
 * 
 * @author Peter Powers
 * @version $Id$
 * @see Area
 * @see BorderType
 */
public class GeographicRegion implements 
		Serializable, XMLSaveable, NamedObjectAPI {

	private static final long serialVersionUID = 1L;

	// although border vertices can be accessed by path-iterating over
	// area, an immutable list is stored for convenience
	//private List<Location> border;
	// TODO implement LocationList  AL subclass changes to 
	// facilitate unmodifiable list creation.
	// TODO need to make copy of provided borders/LocLists
	private LocationList border;
	
	// Internal representation of region
	private Area area;

	// Default angle used to subdivide a circular region: 10 deg
	private static final double WEDGE_WIDTH = 10;
	
	// Default segment length for great circle splitting: 100km
	private static final double GC_SEGMENT = 100;
	
	public final static String XML_METADATA_NAME = "GeographicRegion";
	public final static String XML_METADATA_OUTLINE_NAME = "OutlineLocations";

	// name for this region TODO possibly kill
	private String name = "Unnamed Region";

	/**
	 * TODO delete default empty constructor; has about 8 dependencies
	 * ACTUALLY, make private or package private; used by intersect(r1,r2)
	 */
	public GeographicRegion() {
	}

	
	/**
	 * Initializes a <code>Region</code> from a pair of <code>Location
	 * </code>s. When viewed in a Mercator projection, the region
	 * will be a rectangle. If either both latitude or both longitude
	 * values in the <code>Location</code>s are the same, an exception
	 * is thrown.<br/>
	 * <br/>
	 * <b>Note:</b> Internally, a very small value (~1m) is added to the
	 * maximum latitude and longitude of the locations provided. This
	 * ensures that calls to {@link GeographicRegion#isLocationInside(Location)} 
	 * for any <code>Location</code> on the north or east border of the region 
	 * will return <code>true</code>. See also the rules governing insidedness
	 * in the {@link Shape} interface.
	 * 
	 * @param loc1 the first <code>Location</code>
	 * @param loc2 the second <code>Location</code>
	 * @throws IllegalArgumentException if the latitude or longitude values
	 * 		in the <code>Location</code>s provided are the same
	 * @throws NullPointerException if either <code>Location</code> argument
	 * 		is <code>null</code>
	 */
	public GeographicRegion(Location loc1, Location loc2) {
		
		if (loc1 == null || loc2 == null) {
			throw new NullPointerException();
		}
		
		double lat1 = loc1.getLatitude();
		double lat2 = loc2.getLatitude();
		double lon1 = loc1.getLongitude();
		double lon2 = loc2.getLongitude();
		
		if (lat1 == lat2 || lon1 == lon2) {
			throw new IllegalArgumentException(
					"Input lats or lons cannot be the same");
		}

		LocationList ll = new LocationList();
		double offset = 0.00001; // in degrees ~1m
		// NOTE: see notes at LL_PRECISION
		// TODO: increase value with move to jdk6
		double minLat = Math.min(lat1,lat2);
		double minLon = Math.min(lon1,lon2);
		double maxLat = Math.max(lat1,lat2) + offset;
		double maxLon = Math.max(lon1,lon2) + offset;
		ll.addLocation(new Location(minLat, minLon));
		ll.addLocation(new Location(minLat, maxLon));
		ll.addLocation(new Location(maxLat, maxLon));
		ll.addLocation(new Location(maxLat, minLon));
		
		initBorderedRegion(ll, BorderType.MERCATOR_LINEAR);
	}
	
	/**
	 * Initializes a <code>Region</code> from a list of border locations. The 
	 * border type specifies whether lat-lon values are treated as points in an
	 * orthogonal coordinate system or as connecting great circles. The
	 * border <code>LocationList</code> does not need to repeat the first
	 * <code>Location</code> at the end of the list.
	 * 
	 * @param border <code>Locations</code>
	 * @param type the {@link BorderType} to use when initializing; 
	 * 		a <code>null</code> value defaults to 
	 * 		<code>BorderType.MERCATOR_LINEAR</code>
	 * @throws IllegalArgumentException if the <code>border</code> does not 
	 * 		have at least 3 points
	 * @throws NullPointerException if the <code>border</code> is 
	 * 		<code>null</code>
	 */
	public GeographicRegion(LocationList border, BorderType type) {
		if (border == null) {
			throw new NullPointerException();
		} else if (border.size() < 3) {
			throw new IllegalArgumentException(
					"Border must have at least 3 vertices");
		} else if (type == null) {
			type = BorderType.MERCATOR_LINEAR;
		}
		initBorderedRegion(border, type);
	}

	/**
	 * Initializes a circular <code>Region</code>. Internally, the centerpoint
	 * and radius are used to create a circular region composed of straight
	 * line segments that span 10&deg; wedges.
	 * 
	 * @param center of the circle
	 * @param radius of the circle
	 * @throws IllegalArgumentException if <code>radius</code> is outside the
	 * 		range 0 km &lt; <code>radius</code> &le; 1000 km
	 * @throws NullPointerException if <code>center</code> is <code>null</code>
	 */
	public GeographicRegion(Location center, double radius) {
		if (radius <= 0 || radius > 1000) {
			throw new IllegalArgumentException(
					"Radius is out of [0 1000] km range");
		} else if (center == null) {
			throw new NullPointerException();
		}
		initCircularRegion(center, radius);
	}
	
	/**
	 * Initializes a <code>Region</code> as a buffered area around a line.
	 * 
	 * @param line at center of buffered region
	 * @param buffer distance from line
	 * @throws NullPointerException if <code>line</code> is <code>null</code>
	 * @throws IllegalArgumentException if <code>buffer</code> is outside the
	 * 		range 0 km &lt; <code>buffer</code> &le; 500 km
	 */
	public GeographicRegion(LocationList line, double buffer) {
		if (buffer <= 0 || buffer > 500) {
			throw new IllegalArgumentException(
					"Buffer is out of [0 500] km range");
		} else if (line == null) {
			throw new NullPointerException();
		} else if (line.size() == 0) {
			throw new IllegalArgumentException(
					"LocationList argument is empty");
		}
		initBufferedRegion(line, buffer);
	}
	
	/**
	 * Initializes a <code>Region</code> with another <code>Region</code>.
	 * Internally the border of the provided region is copied and used for
	 * the new region.
	 * 
	 * @param region to use as border for new region
	 */
	public GeographicRegion(GeographicRegion region) {
		this(region.getRegionOutline(), BorderType.MERCATOR_LINEAR);
	}
	
	/**
	 * Initializes a <code>Region</code> around an earthquake rupture.
	 * <br/>
	 * TODO build me<br/>
	 * TODO is there any kind of Rupture in OpenSHA that is not an EQ Rupture<br/>
	 * 
	 * @param rupture
	 * @param buffer
	 */
	public GeographicRegion(EqkRupture rupture, double buffer) {
		
	}
	
	/**
	 * Creates a Geographic with the given list of locations.
	 * 
	 * @param locs LocationList
	 * TODO rename and make private; kill
	 */
//	@Deprecated
//	public void createGeographicRegion(LocationList locs) {
//		border = locs;
//
//		// calls the private method of the class to precompute the min..max lat
//		// & lon.
//		//setMinMaxLatLon();
//
//		// create the polygon used for determining whether points are inside
//		area = createArea(border);
//
//	}

	/**
	 * Returns whether the given location is inside this region following the
	 * rules of insidedness defined in the {@link Shape} interface.
	 * TODO rename contains()
	 * 
	 * @param loc the <code>Location</code> to verify
	 * @return <code>true</code> if the <code>Location</code> is inside the 
	 * 		region, <code>false</code> otherwise
	 * @see java.awt.Shape
	 */
	public boolean isLocationInside(Location loc) {

//		if (poly.contains(
//				(int) (location.getLatitude() * DEGREES_TO_INT_FACTOR),
//				(int) (location.getLongitude() * DEGREES_TO_INT_FACTOR)))
//			return true;
		return area.contains(loc.getLongitude(), loc.getLatitude());
	}

	/**
	 * Tests whether another region is entirely contained within this region.
	 * @param region to check
	 * @return <code>true</code> if this contains the <code>region</code>; 
	 * 		<code>false</code> otherwise
	 */
	public boolean contains(GeographicRegion region) {
		Area areaUnion = (Area) area.clone();
		areaUnion.add(region.area);
		return area.equals(areaUnion);
	}
	
	/**
	 * Returns whether this region is rectangular in shape when represented in
	 * a Mercator projection.
	 * @return <code>true</code> if rectangular, <code>false</code> otherwise
	 */
	public boolean isRectangular() {
		return area.isRectangular();
	}
	
	/*
	 * Meter scale precision is imposed on the min-max methods below. For
	 * whatever reason, values used to create an Area are altered more than 
	 * typical rounding error on retrieval: -125.4000015258789 vs -125.4.
	 * This may be to facilitate correct results for insidedness testing.
	 * Alternatively, these methods could be updated to query the location
	 * list, requiring changes to the LocationList class to quickly return
	 * min-max values. 
	 */

	/**
	 * Returns the minimum latitude in this region's border.
	 * @return the minimum latitude
	 */
	public double getMinLat() {
		double val = area.getBounds2D().getMinY();
		return MathUtils.round(val, RelativeLocation.LL_PRECISION);
	}

	/**
	 * Returns the maximum latitude in this region's border.
	 * @return the maximum latitude
	 */
	public double getMaxLat() {
		double val = area.getBounds2D().getMaxY();
		return MathUtils.round(val, RelativeLocation.LL_PRECISION);
	}

	/**
	 * Returns the minimum longitude in this region's border.
	 * @return the minimum longitude
	 */
	public double getMinLon() {
		double val = area.getBounds2D().getMinX();
		return MathUtils.round(val, RelativeLocation.LL_PRECISION);
	}

	/**
	 * Returns the maximum longitude in this region's border.
	 * @return the maximum longitude
	 */
	public double getMaxLon() {
		double val = area.getBounds2D().getMaxX();
		return MathUtils.round(val, RelativeLocation.LL_PRECISION);
	}


//	/**
//	 * 
//	 * @return the LocationList size
//	 */
//	public int getNumRegionOutlineLocations() {
//		return border.size();
//	}

	/**
	 * 
	 * @returns the ListIterator to the LocationList
	 */
//	public ListIterator getRegionOutlineIterator() {
//		return border.listIterator();
//	}


	/**
	 * Returns an unmodifiable <code>java.util.List</code> view of the
	 * internal <code>LocationList</code> of points that decribe the border
	 * of this region. Note that the <code>Location</code>s in the list
	 * are also immutable.
	 * @return the border <code>LocationList</code> TODO fix when changed
	 */
	public LocationList getRegionOutline() {
		return border;
		// TODO rename to getBorder()
	}

	/**
	 * this method finds the minLat,maxLat,minLon and maxLon.
	 */
//	protected void setMinMaxLatLon() {
//		ListIterator it = getRegionOutlineIterator();
//		Location l = (Location) it.next();
//		getMin = l.getLatitude();
//		minLon = l.getLongitude();
//		maxLat = l.getLatitude();
//		maxLon = l.getLongitude();
//		while (it.hasNext()) {
//			l = (Location) it.next();
//			if (l.getLatitude() < minLat) minLat = l.getLatitude();
//			if (l.getLatitude() > maxLat) maxLat = l.getLatitude();
//			if (l.getLongitude() < minLon) minLon = l.getLongitude();
//			if (l.getLongitude() > maxLon) maxLon = l.getLongitude();
//		}

//		if (D)
//			System.out.println(C + ": minLat=" + minLat + "; maxLat=" + maxLat
//					+ "; minLon=" + minLon + "; maxLon=" + maxLon);
//	}

	/**
	 * This computes the minimum horizonatal distance (km) from the location the
	 * region outline. Zero is returned if the given location is inside the
	 * polygon. This distance is approximate in that it uses the
	 * RelativeLocation.getApproxHorzDistToLine(*) method to compute the
	 * distance to each line segment in the region outline.
	 * 
	 * @return
	 * TODO clean, update
	 */
	public double getMinHorzDistToRegion(Location loc) {
		if (isLocationInside(loc))
			return 0.0;
		else {
			double min = border.getMinHorzDistToLine(loc);
			// now check the segment defined by the last and first points
			double temp =
					RelativeLocation.getApproxHorzDistToLine(loc, border
							.getLocationAt(border.size() - 1), border
							.getLocationAt(0));
			if (temp < min)
				return temp;
			else
				return min;
		}
	}

	/* implementation */
	public String getName() {
		return name;
	}

	/**
	 * Set the name for this region.
	 * @param name for the region
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(GeographicRegion.XML_METADATA_NAME);
		LocationList list = this.getRegionOutline();
		xml = list.toXMLMetadata(xml);
		return root;
	}

	// TODO verify that that xml io is working
	public static GeographicRegion fromXMLMetadata(Element geographicElement) {
		LocationList list =
				LocationList.fromXMLMetadata(geographicElement
						.element(LocationList.XML_METADATA_NAME));
		return new GeographicRegion(list, BorderType.MERCATOR_LINEAR);
	}

	// TODO update
	// will return true if region is a rectangle with boundaries parallel to 
	// lines of latitude and longitude when viewed using a Mercator
	// projection, 
//	public boolean isRectangular() {
//		if (this.border.size() == 4) { // it might be a rectangular region
//			int minLatHits = 0;
//			int maxLatHits = 0;
//			int minLonHits = 0;
//			int maxLonHits = 0;
//
//			double minLat = this.getMinLat();
//			double maxLat = this.getMaxLat();
//			double minLon = this.getMinLon();
//			double maxLon = this.getMaxLon();
//
//			for (int i = 0; i < 4; i++) {
//				Location loc = border.getLocationAt(i);
//				double lat = loc.getLatitude();
//				double lon = loc.getLongitude();
//				if (lat == minLat)
//					minLatHits++;
//				else if (lat == maxLat) maxLatHits++;
//
//				if (lon == minLon)
//					minLonHits++;
//				else if (lon == maxLon) maxLonHits++;
//			}
//			// it is a rectangular region if the location list contains exactly
//			// 2 of each min/max lat/lon
//			if (minLatHits == 2 && maxLatHits == 2 && minLonHits == 2
//					&& maxLonHits == 2) return true;
//		}
//		return false;
//	}

	@Override
	public String toString() {
		String str =
				"GeographicRegion\n" + "\tMinimum Lat: " + this.getMinLat()
						+ "\n" + "\tMinimum Lon: " + this.getMinLon() + "\n"
						+ "\tMaximum Lat: " + this.getMaxLat() + "\n"
						+ "\tMaximum Lon: " + this.getMaxLon();
		return str;
	}

//	public static void main(String[] args) {
//		Area ar =
//				new Area(new Polygon(
//						new int[] { -120, -115, -115, -118, -121 }, new int[] {
//								40, 40, 44, 48, 46 }, 5));
//		PathIterator pi = ar.getPathIterator(null);
//		double[] vertex = new double[6];
//		while (!pi.isDone()) {
//			int segType = pi.currentSegment(vertex);
//			System.out.println(segType);
//			// StringBuffer sb = new StringBuffer();
//			for (double v : vertex) {
//				String s =
//						new ToStringBuilder(vertex).append(vertex).toString();
//				System.out.println(s);
//			}
//			System.out.println("----");
//			pi.next();
//		}
//		System.out.println("\u00B1180&deg; \u00b1180&deg;");
//		System.out.println(new GeographicRegion(new LocationList(), null));
//	}

//	public Area getArea() {
//		return area;
//	}
	public static void main(String[] args) {
//		double tmp = 5.64352783407;
//		Location loc = new Location(tmp,tmp,0);
//		System.out.println(loc);
//		
//		DecimalFormat fmt1 = new DecimalFormat("0.0######");
//		DecimalFormat fmt2 = new DecimalFormat("0.0#####");
//		DecimalFormat fmt3 = new DecimalFormat("0.0####");
//		System.out.println(MathUtils.round(tmp, RelativeLocation.LL_PRECISION));
//		System.out.println(fmt1.format(tmp));
//		System.out.println(fmt2.format(tmp));
//		System.out.println(fmt3.format(tmp));
//		System.out.println((float) tmp);
		
//		LocationList ll = new LocationList();
//		ll.addLocation(new Location(40, -120));
//		ll.addLocation(new Location(44, -120));
//		ll.addLocation(new Location(44, -115));
//		ll.addLocation(new Location(40, -115));
//		System.out.println("-180\u00B0");
//		
//		Area ar =createArea(ll);
//		System.out.println("-180\u00B0");
//		LocationList ll2 = createBorder(ar);
//		
////		String s =
////			new ToStringBuilder(vertex).append(vertex).toString();
//		System.out.println("-180\u00B0");
	}
	
	/**
	 * Returns the intersection of two regions. If the regions do not overlap,
	 * the method returns <code>null</code>.
	 * 
	 * @param r1 the first region
	 * @param r2 the second region
	 * @return a new regions defined by the intersection of <code>r1</code> 
	 * 		and <code>r2</code> or <code>null</code> if they do not overlap
	 */
	public static GeographicRegion intersect(
			GeographicRegion r1,
			GeographicRegion r2) {
		Area newArea = (Area) r1.area.clone();
		newArea.intersect(r2.area);
		if (newArea.isEmpty()) return null;
		GeographicRegion newRegion = new GeographicRegion();
		newRegion.area = newArea;
		newRegion.border = GeographicRegion.createBorder(newArea, true);
		return newRegion;
	}

	/**
	 * Returns the union of two regions. If the regions do not overlap,
	 * the method returns <code>null</code>.
	 * 
	 * @param r1 the first region
	 * @param r2 the second region
	 * @return a new region defined by the union of <code>r1</code> 
	 * 		and <code>r2</code> or <code>null</code> if they do not overlap
	 */
	public static GeographicRegion union(
			GeographicRegion r1,
			GeographicRegion r2) {
		Area newArea = (Area) r1.area.clone();
		newArea.add(r2.area);
		if (!newArea.isSingular()) return null;
		GeographicRegion newRegion = new GeographicRegion();
		newRegion.area = newArea;
		newRegion.border = GeographicRegion.createBorder(newArea, true);
		return newRegion;
	}

	/**
	 * Convenience method to return a region spanning the entire globe.
	 * @return a region extending from -180&#176; to +180&#176; longitude and
	 * 		-90&#176; to +90&#176; latitude
	 */
	public static GeographicRegion getGlobalRegion() {
		LocationList gll = new LocationList();
		gll.addLocation(new Location(-90, -180));
		gll.addLocation(new Location(-90, 180));
		gll.addLocation(new Location(90, 180));
		gll.addLocation(new Location(90, -180));
		return new GeographicRegion(gll, BorderType.MERCATOR_LINEAR);
	}
	
	/*
	 * Initialize a region from a list of border locations. Internal
	 * java.awt.geom.Area is generated from the border.
	 */
	private void initBorderedRegion(LocationList border, BorderType type) {
		
		// first remove last point in list if it is the same as
		// the first point
		int lastIndex = border.size()-1;
		if (border.getLocationAt(lastIndex).equalsLocation(
				border.getLocationAt(0))) {
			border.remove(lastIndex);
		}
		
		if (type.equals(BorderType.GREAT_CIRCLE)) {
			LocationList gcBorder = new LocationList();
			// process each border pair [start end]; so that the entire
			// border is traversed, set the first 'start' Location as the
			// last point in the gcBorder
			Location start = border.getLocationAt(border.size()-1);
			for (int i=0; i<border.size(); i++) {
				gcBorder.addLocation(start);
				Location end = border.getLocationAt(i);
				double distance = RelativeLocation.surfaceDistance(start, end);
				// subdivide as necessary
//				System.out.println("Vertex: " + i + "  Dist: " + distance); TODO clean
				while (distance > GC_SEGMENT) {
					// find new Location, GC_SEGMENT km away from start
					double azRad = RelativeLocation.azimuthRad(start, end);
					Location segLoc = RelativeLocation.location(
							start, azRad, GC_SEGMENT);
					gcBorder.addLocation(segLoc);
					start = segLoc;
					distance = RelativeLocation.surfaceDistance(start, end);
//					System.out.println("             P1: " + start); //TODO clean
//					System.out.println("             P2: " + end);
//					System.out.println("           Dist: " + distance + "  Az: " + azRad);
				}
				start = end;
			}
			//this.border = Collections.unmodifiableList(gcBorder); TODO uncomment
			this.border = gcBorder; // TODO comment
		} else {
			this.border = border;
			//this.border = Collections.unmodifiableList(gcBorder); TODO uncomment
			// TODO break long great circles into smaller segments and test
			// TODO gc processing need to return to start point; ensure that input
			// TODO make copy and then wrap unmodifiable
			// border has start popint
		}
		area = createArea(this.border);
	}
	
	/*
	 * Initialize a circular region by creating an circular border of shorter
	 * straight line segments. Internal java.awt.geom.Area is generated from 
	 * the border.
	 */
	private void initCircularRegion(Location center, double radius) {
		border = createLocationCircle(center, radius);
		area = createArea(this.border);
	}
	
	/*
	 * Initialize a buffered region by creating box areas of 2x buffer width 
	 * around each line segment and circle areas around each vertex and union
	 * all of them. The border is then be derived from the Area.
	 */
	private void initBufferedRegion(LocationList line, double buffer) {
		// init an empty Area
		area = new Area();
		// for each point segment, create a circle area
		Location prevLoc = null;
		for (Location loc: line) {
			// starting out only want to create circle area for first point
			if (area.isEmpty()) {
				area.add(createArea(createLocationCircle(loc, buffer)));
				prevLoc = loc;
				continue;
			}
			area.add(createArea(createLocationBox(prevLoc, loc, buffer)));
			area.add(createArea(createLocationCircle(loc, buffer)));
			prevLoc = loc;
		}
		border = createBorder(area, true);
	}

	/* 
	 * Creates a LocationList border from a java.awt.geom.Area. The clean
	 * flag is used to post-process list to remove repeated identical
	 * locations, which are common after intersect and union operations.
	 */
	private static LocationList createBorder(Area area, boolean clean) {
		PathIterator pi = area.getPathIterator(null);
		LocationList ll = new LocationList();
		// placeholder vertex for path iteration
		double[] vertex = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(vertex);
			// impose meter scale precision; narrowing conversions that occur
			// when creating an area from a GeneralPath (only uses floats)
			// have strange effects on values on retreival:
			//  -125.4000015258789 vs -125.4
			// NOTE: see notes with LL_PRECISION
			double lon = MathUtils.round(vertex[0], RelativeLocation.LL_PRECISION);
			double lat = MathUtils.round(vertex[1], RelativeLocation.LL_PRECISION);
			// skip the final closing segment which just repeats
			// the previous vertex but indicates SEG_CLOSE
			if (type != PathIterator.SEG_CLOSE) {
				ll.addLocation(Location.immutableLocation(lat,lon));
			}
			pi.next();
		}
		
		if (clean) {
			LocationList llClean = new LocationList();
			Location prev = ll.getLocationAt(ll.size()-1);
			for (Location loc:ll) {
				if (loc.equals(prev)) continue;
				llClean.addLocation(loc);
				prev = loc;
			}
			ll = llClean;
		}
		return ll;
		//return Collections.unmodifiableList(ll); TODO uncomment
	}
	
	/*
	 * Creates a java.awt.geom.Area from a LocationList border
	 * NOTE: see notes with LL_PRECISION
	 */
	private static Area createArea(LocationList border) {
		
		GeneralPath path = new GeneralPath(
				GeneralPath.WIND_EVEN_ODD,
				border.size());
		
		boolean starting = true;
		for (Location loc: border) {
			//System.out.println(loc.getLatitude() + " " + loc.getLongitude());
			float lat = (float) loc.getLatitude();
			float lon = (float) loc.getLongitude();
			//System.out.println(lat+ " " + lon); TODO clean
			// if just starting, then moveTo
			if (starting) {
				path.moveTo(lon, lat);
				starting = false;
				continue;
			}
			path.lineTo(lon, lat);
		}
		path.closePath();
		return new Area(path);
	}
	
	/*
	 * Utility method returns a LocationList that approximates the 
	 * circle represented by the center location and radius provided.
	 */
	private static LocationList createLocationCircle(
			Location center, double radius) {
		
		LocationList ll = new LocationList();
	    for (double angle=0; angle<360; angle += WEDGE_WIDTH) {
	    	ll.addLocation(
	    			Location.immutableLocation(
	    					RelativeLocation.location(
	    							center, angle * TO_RAD, radius)));
	    }
	    return ll;
		//return Collections.unmodifiableList(ll); TODO uncomment
	}
	
	/*
	 * Utility method returns a LocationList representing a box that is as
	 * long as the line between p1 and p2 and extends on either side of
	 * that line some 'distance'.
	 */
	private static LocationList createLocationBox(
			Location p1, Location p2, double distance) {
		
		// get the azimuth and back-azimuth between the points
		double az12 = RelativeLocation.azimuthRad(p1, p2);
		double az21 = RelativeLocation.azimuthRad(p2, p1); // back azimuth
		
		// add the four corners
		LocationList ll = new LocationList();
		// corner 1 is azimuth p1 to p2 - 90 from p1
		ll.addLocation(RelativeLocation.location(p1, az12-PI_BY_2, distance));
		// corner 2 is azimuth p1 to p2 + 90 from p1
		ll.addLocation(RelativeLocation.location(p1, az12+PI_BY_2, distance));
		// corner 3 is azimuth p2 to p1 - 90 from p2
		ll.addLocation(RelativeLocation.location(p2, az21-PI_BY_2, distance));
		// corner 4 is azimuth p2 to p1 + 90 from p2
		ll.addLocation(RelativeLocation.location(p2, az21+PI_BY_2, distance));
		
		return ll;
		//return Collections.unmodifiableList(ll); TODO uncomment
	}

}
