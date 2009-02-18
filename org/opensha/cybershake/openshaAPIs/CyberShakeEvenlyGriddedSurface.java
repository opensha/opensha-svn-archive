package org.opensha.cybershake.openshaAPIs;

import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.sha.surface.EvenlyGriddedSurface;

public class CyberShakeEvenlyGriddedSurface extends EvenlyGriddedSurface {

	public CyberShakeEvenlyGriddedSurface( int numRows, int numCols, double gridSpacing) {
		super(numRows, numCols, gridSpacing);
	}
	
	public void setAllLocations(ArrayList<Location> locs) {
		int num = numRows * numCols;
		if (num != locs.size())
			throw new RuntimeException("ERROR: Not the right amount of locations! (expected " + num + ", got " + locs.size() + ")");
		
		int count = 0;
		
		for (int i=0; i<numRows; i++) {
			for (int j=0; j<numCols; j++) {
				this.set(i, j, locs.get(count));
				
				count++;
			}
		}
	}

	public void set( int row, int column, Location loc ) throws ArrayIndexOutOfBoundsException {

		String S = C + ": set(): ";
		checkBounds( row, column, S );
		data[row * numCols + column] = loc;
	}

}