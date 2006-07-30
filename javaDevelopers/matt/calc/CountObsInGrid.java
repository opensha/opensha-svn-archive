/**
 * 
 */
package javaDevelopers.matt.calc;

import java.util.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.Location;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * @author matthew
 *
 */
public class CountObsInGrid {
	
	private int numGridNodes;
	private EvenlyGriddedGeographicRegionAPI region;
	private int[] obsInGrid;

	public CountObsInGrid(ObsEqkRupList obsEvents, EvenlyGriddedGeographicRegionAPI region){
		this.region = region;
		numGridNodes = this.region.getNumGridLocs();
	}
	
	public int getNumGridnodes(){
		return this.region.getNumGridLocs();
	}
	
	public void countEventsInCell(){
		int gLoop = 0;
		while ( gLoop < numGridNodes ){
			
		}
	}

}
