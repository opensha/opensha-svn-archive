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

        // there is only one rupture
        if(nRupAlong <=1) {
          nRupAlong=1;
          numRupCols = numCols;
        }

        // nnmber of ruptures along fault width
        int nRupDown =  (int)Math.floor((numRows-numRupRows)/numRupOffset +1);

        // one rupture along width
        if(nRupDown <=1) {
          nRupDown=1;
          numRupRows = numRows;
        }


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

       return getSubsetSurfacesIterator((int)Math.rint(rupLength/gridSpacing),
                                        (int)Math.rint(rupWidth/gridSpacing),
                                        (int)Math.rint(rupOffset/gridSpacing));

    }


    /**
     *
     * @param rupLength Rupture length in km
     * @param rupWidth  Rupture Width in km
     * @param rupOffset Ruture offset
     * @return total number of ruptures along the fault
     */
    public int getTotalRuptures(double rupLength,double rupWidth,double rupOffset){
      int length =  (int)Math.rint(rupLength/gridSpacing);
      int width =    (int)Math.rint(rupWidth/gridSpacing);
      int offset =   (int)Math.rint(rupOffset/gridSpacing);
      int totalRuptures =1;
      // number of ruptures along the length of fault
       int nRupAlong = (int)Math.floor((numCols-length)/offset +1);

       // there is only one rupture
       if(nRupAlong <=1) {
         nRupAlong=1;
       }

       // nnmber of ruptures along fault width
       int nRupDown =  (int)Math.floor((numRows-width)/offset +1);

       // one rupture along width
       if(nRupDown <=1) {
         nRupDown=1;
       }
     totalRuptures =   nRupAlong * nRupDown;
     return totalRuptures;
    }

}