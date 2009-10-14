/**
 * 
 */
package org.opensha.sha.faultSurface;

import java.io.FileWriter;
import java.util.Iterator;

import org.opensha.commons.data.Location;

/**
 * @author field
 *
 */
public class ApproxEvenlyGriddedSurface extends EvenlyGriddedSurface {
	
	public ApproxEvenlyGriddedSurface(int numRows,int numCols) {
		this.setNumRowsAndNumCols(numRows, numCols);
	}
	
    /**
     *  Add a Location to the grid - does the same thing as set except that it
     *  ensures the object is a Location object.
     *
     * @param  row                                 The row to set this Location at.
     * @param  column                              The column to set this Location at.
     * @param  location                            The new location value.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
     */
    public void setLocation( int row, int column, Location location ) {
        super.setLocation( row, column, location );
    }
    
    
    
	  public void writeXYZ_toFile(String fileName) {
			try{
				FileWriter fw = new FileWriter(fileName);
				fw.write("lat\tlon\tdepth\n");
				Iterator it = this.getLocationsIterator();
				while(it.hasNext()) {
					Location loc = (Location) it.next();
					fw.write(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth()+"\n");
				}	
				fw.close();
			}catch(Exception e) {
				e.printStackTrace();
			}

	  }



}
