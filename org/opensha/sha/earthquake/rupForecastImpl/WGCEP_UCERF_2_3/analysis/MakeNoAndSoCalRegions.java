/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.util.Geometry;


/**
 * Find the NoCal and SoCal regions
 * @author vipingupta
 *
 */
public class MakeNoAndSoCalRegions {
	//public static double X0 = -121.025;
	//public static double Y0 = 34.8626;
	//Original X0 Y0 were given in email forwarded to me by Ned on Sep 14, 2007. Since that point lied inside RELM region, 
	// I recalulated a point outside the region on that line
	public static double X0 = -123.0;
	public static double Y0 = 32.745823;
	
	// these were given in email forwarded to me by Ned on Sep 14, 2007
	public static double X1 = -116.8264;
	public static double Y1 = 39.3626;
	
	
	public MakeNoAndSoCalRegions() {
		EvenlyGriddedRELM_Region relmRegion = new EvenlyGriddedRELM_Region();
		//System.out.println(relmRegion.getMinGridLon()+","+relmRegion.getMaxGridLon());
		System.out.println(relmRegion.isLocationInside(new Location(Y0, X0)));
		System.out.println(relmRegion.isLocationInside(new Location(Y1, X1)));
		LocationList locList = relmRegion.getRegionOutline();
		int numLocs = locList.size();
		double x[] = new double[numLocs];
		double y[] = new double[numLocs];
		for(int i=0; i<numLocs; ++i) {
			x[i] = locList.getLocationAt(i).getLongitude();
			y[i] = locList.getLocationAt(i).getLatitude();
		}
		
		//double[] points = Geometry.findLinePolygonIntersections (x, y, X0,  Y0, X1, Y1);
		double[] points = Geometry.findLinePolygonIntersections (x, y, X0,  Y0, X1, Y1);
		
		for(int i=0; i<points.length; ++i) System.out.println(points[i]);
		
		// after printing the intersection points, we can calculate the lat/lon intersection points
		// -121.622, 34.2223
		// -117.664, 38.4644
	}
	
	
	public static void main(String[] args) {
		new MakeNoAndSoCalRegions();
	}

}


