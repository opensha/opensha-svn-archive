package org.scec.sha.surface;

import java.util.Iterator;
import java.util.Vector;
/**
 * <p>Title:EvenlyGriddedSurface </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta & Vipin Gupta    Date: Aug,23,2002
 * @version 1.0
 */

public class EvenlyGriddedSurface extends GriddedSurface {

    /**
     * @todo Variables
     */
     private double gridSpacing;
     Vector v= new Vector();

    /**
     *  Constructor for the GriddedSurface object
     *
     * @param  numRows  Description of the Parameter
     * @param  numCols  Description of the Parameter
     */
    public EvenlyGriddedSurface( int numRows, int numCols,double gridSpacing ) {
        super( numRows, numCols );
        this.gridSpacing = gridSpacing;
    }

    /**
     *
     * @return
     */
    public double getGridSpacing() {
      return this.gridSpacing;
    }

    /**
     *
     * @param length
     * @param width
     * @param offset
     */
    public Iterator getSubsetSurfacesIterator(double length, double width, int offset) {
        int nRupAlong = (int)Math.floor((numCols-length)/offset +1);
        if(nRupAlong <1) nRupAlong=1;
        int nRupDown =  (int)Math.floor((numRows-width)/offset +1);
        if(nRupDown <1) nRupDown=1;
        int col = 0;
        int row =0;
        v.clear();
        for(int j=0; j < nRupDown; ++j, row=row+offset) {
          col = 0;
          for(int i=0;i < nRupAlong ; ++i, col=col+offset) {
             GriddedSubsetSurface subsetSurfaces =
                  new GriddedSubsetSurface((int)width,(int)length,row,col,this);
             v.add(subsetSurfaces);
          }
       }

       return v.iterator();
   }


   /**
    *
    * @param numCols
    * @param numRows
    * @param offset
    * @return

    public Iterator getSubsetSurfacesIterator(int numCols,int numRows,int offset) {

        int nRupAlong = (int)Math.floor((numCols-this.numCols)/offset +1);
        if(nRupAlong <1) nRupAlong=1;
        int nRupDown =  (int)Math.floor((numRows-this.numRows)/offset +1);
        if(nRupDown <1) nRupDown=1;

        GriddedSubsetSurface subsetSurfaces =
              new GriddedSubsetSurface(nRupDown,nRupAlong,0,0,this);

        return subsetSurfaces.listIterator();
    }*/


}