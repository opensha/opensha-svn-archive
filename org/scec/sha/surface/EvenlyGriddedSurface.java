package org.scec.sha.surface;

import java.util.Iterator;
import java.util.Vector;
/**
 * <p>Title:EvenlyGriddedSurface </p>
 * <p>Description: This class gives the list of ruptures on the fault</p>
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
     * Get the ruptures on this fault
     *
     * @param numRupCols  Number of grid points according to length
     * @param numRupRows  Number of grid points according to width
     * @param numRupOffset Number of grid poits for offset
     *
     */
    public Iterator getSubsetSurfacesIterator(int numRupCols, int numRupRows, int numRupOffset) {

        // number of ruptures along the length of fault
        int nRupAlong = (int)Math.floor((numCols-numRupCols)/numRupOffset +1);
        if(nRupAlong <1) nRupAlong=1;

        // nnmber of ruptures along fault width
        int nRupDown =  (int)Math.floor((numRows-numRupRows)/numRupOffset +1);
        if(nRupDown <1) nRupDown=1;

        // save the ruptures in a vector
        int col = 0;
        int row =0;
        v.clear();
        for(int j=0; j < nRupDown; ++j, row=row+numRupOffset) {
          col = 0;
          for(int i=0;i < nRupAlong ; ++i, col=col+numRupOffset) {
             GriddedSubsetSurface subsetSurfaces =
                  new GriddedSubsetSurface((int)numRupRows,(int)numRupCols,row,col,this);
             v.add(subsetSurfaces);
          }
       }
       return v.iterator();
   }


   /**
    * Get the ruptures on this fault
    *
    * @param rupLength  Rupture length in km
    * @param rupWidth   Rupture width in km
    * @param rupOffset  Rupture offset
    * @return           Iterator over all ruptures
    */
    public Iterator getSubsetSurfacesIterator(double rupLength,double rupWidth,double rupOffset) {
       return getSubsetSurfacesIterator(Math.rint(rupLength/gridSpacing),
                                        Math.rint(rupWidth/gridSpacing),
                                        Math.rint(rupOffset/gridSpacing));

    }


}