/**
 * 
 */
package org.opensha.commons.data.region;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;



/**
 * This is a Box around LA with the same dimensions as the WG02 region. The Lats and 
 * Lons come from calculations in the main method of EvenlyGriddedWG02_Region
 * @author ned field
 *
 */
@Deprecated
public class EvenlyGriddedWG07_LA_Box_Region extends EvenlyGriddedGeographicRegion {
	private final static boolean D = true;
	protected final static double GRID_SPACING = 0.1;
	
	Location laBox_LocE;
	Location laBox_LocS;
	Location laBox_LocW;
	Location laBox_LocN;


	public EvenlyGriddedWG07_LA_Box_Region() {

		LocationList locList = getLocationList();
		// make polygon from the location list
		//createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);
		
		if(D)
			for(int l=0; l<locList.size();l++)
				System.out.println(locList.getLocationAt(l).toString());
		

	}

	/**
	 * Location list which forms the outline of the WGCEP 2007 LA Box
	 */
	protected LocationList getLocationList() {
		LocationList locList = new LocationList();
		
		// WG02 Region
		Location wg02_LocE = new Location(37.19,-120.61);
		Location wg02_LocS = new Location(36.43,-122.09);
		Location wg02_LocW = new Location(38.23,-123.61);
		Location wg02_LocN = new Location(39.02,-122.08);
		
		double aveHeight = (RelativeLocation.getHorzDistance(wg02_LocS, wg02_LocE)+
							RelativeLocation.getHorzDistance(wg02_LocW, wg02_LocN))/2;
		double aveLength = (RelativeLocation.getHorzDistance(wg02_LocS, wg02_LocW)+
							RelativeLocation.getHorzDistance(wg02_LocE, wg02_LocN))/2;
		
		if(D) System.out.println(aveLength+"\t"+aveHeight);
		
		// LA center location from Google Earth
		double laLat = (7.87/60.0 + 3.0)/60.0 + 34;
		double laLon = -((36.31/60.0 + 14.0)/60.0 + 118);
		Location laCenterLoc = new Location(laLat,laLon);
		
		// Mojave trend (first and last point on Mojave south section):
		Location mojaveN_Loc = new Location(34.698495,-118.508948);
		Location mojaveS_Loc = new Location(34.3163,-117.549);
		double mojaveStrike = RelativeLocation.getAzimuth(mojaveS_Loc, mojaveN_Loc);
		// override with slightly better fitting value
		mojaveStrike = -65;

		if(D) System.out.println("Mojave Strike = \t"+mojaveStrike);
		
		Direction dir = new Direction(0, aveLength/2, mojaveStrike, 0.0);
		Location centerLoc = RelativeLocation.getLocation(laCenterLoc, dir);
		dir.setHorzDistance(aveHeight/2);
		dir.setAzimuth(mojaveStrike+90);
		
		laBox_LocN = RelativeLocation.getLocation(centerLoc, dir);
		dir.setAzimuth(mojaveStrike-90);
		laBox_LocW = RelativeLocation.getLocation(centerLoc, dir);
		
		dir.setAzimuth(mojaveStrike+180);
		dir.setHorzDistance(aveLength/2);
		centerLoc = RelativeLocation.getLocation(laCenterLoc, dir);
		
		dir.setHorzDistance(aveHeight/2);
		dir.setAzimuth(mojaveStrike+90);
		laBox_LocE = RelativeLocation.getLocation(centerLoc, dir);
		dir.setAzimuth(mojaveStrike-90);
		laBox_LocS = RelativeLocation.getLocation(centerLoc, dir);

		double aveFinalLength = (RelativeLocation.getHorzDistance(laBox_LocS, laBox_LocE)+
				RelativeLocation.getHorzDistance(laBox_LocW, laBox_LocN))/2;
		double aveFinalHeight = (RelativeLocation.getHorzDistance(laBox_LocS, laBox_LocW)+
				RelativeLocation.getHorzDistance(laBox_LocE, laBox_LocN))/2;

		if(D) {
			System.out.println(aveFinalLength+"\t"+aveFinalHeight);
			System.out.println(laBox_LocN.getLatitude()+"\t"+laBox_LocN.getLongitude());
			System.out.println(laBox_LocE.getLatitude()+"\t"+laBox_LocE.getLongitude());
			System.out.println(laBox_LocS.getLatitude()+"\t"+laBox_LocS.getLongitude());
			System.out.println(laBox_LocW.getLatitude()+"\t"+laBox_LocW.getLongitude());			
		}

		locList.addLocation(laBox_LocN);
		locList.addLocation(laBox_LocE);
		locList.addLocation(laBox_LocS);
		locList.addLocation(laBox_LocW);

		return locList;
	}
	
	public static void main(String[] args) {
		CaliforniaRegions.WG07_GRIDDED la_box = new CaliforniaRegions.WG07_GRIDDED();
	}
}