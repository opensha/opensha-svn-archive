/**
 * 
 */
package javaDevelopers.matt.calc;

import java.util.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.data.region.EvenlyGriddedCircularGeographicRegion;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.region.EvenlyGriddedSausageGeographicRegion;
import org.opensha.data.Location;
import org.opensha.data.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.opensha.data.region.*;

/**
 * @author matthew
 *
 */
public class CountObsInGrid {
	
	private int numGridNodes;
	private EvenlyGriddedGeographicRegionAPI region;
	private int[] numObsInGrid;
	private ObsEqkRupList obsEvents;
	private EvenlyGriddedCircularGeographicRegion castCircularRegion;
	private EvenlyGriddedSausageGeographicRegion castSausageRegion;
	private boolean useCircle = false, useSausage = false;

	public CountObsInGrid(ObsEqkRupList obsEvents, EvenlyGriddedGeographicRegionAPI region){
		
		this.obsEvents = obsEvents;
		this.region = region;
		if(region instanceof EvenlyGriddedCircularGeographicRegion){
	    	this.castCircularRegion = (EvenlyGriddedCircularGeographicRegion)this.region;
	    	this.useCircle = true;
	    	this.numGridNodes = castCircularRegion.getNumGridLocs();
		}
		else {
	    	this.castSausageRegion = (EvenlyGriddedSausageGeographicRegion)this.region;
		    this.useSausage = true;
		    this.numGridNodes = castSausageRegion.getNumGridLocs();
		}
    	this.numObsInGrid = new int[numGridNodes];
	    
    	countEventsInCell();
	}
	
	public int getNumGridnodes(){
		if (useCircle)
			return this.castCircularRegion.getNumGridLocs();
		else //if is sausage
			return this.castSausageRegion.getNumGridLocs();
	}
	
	/**
	 * getNumObsInGridList
	 * @return int[]
	 * 
	 */
	public int[] getNumObsInGridList(){
		return this.numObsInGrid;
	}
	
	/**
	 * countEventsInCell
	 * count the number of observed events w/in each grid cell
	 */
	public void countEventsInCell(){
		Location gridCenter;
		LocationList cellLoc;
		double gLong,gLat;
		Location gridCorner[] = new Location[4];
		ObsEqkRupList cellEvents;
		
		
		cellLoc = new LocationList();
		int gLoop = 0;
		while ( gLoop < numGridNodes ){
			if (useCircle)
				gridCenter = this.castCircularRegion.getGridLocation(gLoop);
			else
				gridCenter = this.castSausageRegion.getGridLocation(gLoop);
			gLat = gridCenter.getLatitude();
			gLong = gridCenter.getLongitude();
			//create a Location for each corner of the cell
			// add the corner to a LocationList and create a GeographicRegion
			// so that we can see what events are inside the region.
			gridCorner[0].setLatitude(gLat + RegionDefaults.gridSpacing/2);
			gridCorner[0].setLongitude(gLong + RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner[0],0);
			gridCorner[1].setLatitude(gLat - RegionDefaults.gridSpacing/2);
			gridCorner[1].setLongitude(gLong + RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner[1],1);
			gridCorner[2].setLatitude(gLat - RegionDefaults.gridSpacing/2);
			gridCorner[2].setLongitude(gLong - RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner[2],2);
			gridCorner[3].setLatitude(gLat + RegionDefaults.gridSpacing/2);
			gridCorner[3].setLongitude(gLong - RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner[3],3);
			//this creates a GeographicRegion that is the cell for Loc gLoop
			GeographicRegion gridRegion = new GeographicRegion(cellLoc);
			
		    // find ObsEqkRupList of events w/in this grid cell
			cellEvents = obsEvents.getObsEqkRupsInside(gridRegion);
			this.numObsInGrid[gLoop++] = cellEvents.size();	
			
		}
	}

}
