package org.scec.sha.surface;

import java.util.Iterator;
import java.util.Vector;
/**
 * <p>Title:EvenlyGriddedSurface </p>
 * <p>Description: This class gives the list of subset surfaces on the fault</p>
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
     * @param  numRows  Number of grid points along width of fault
     * @param  numCols  Number of grid points along length of fault
     */
    public EvenlyGriddedSurface( int numRows, int numCols,double gridSpacing ) {
        super( numRows, numCols );
        this.gridSpacing = gridSpacing;
    }

    /**
     * returns the grid spacing
     *
     * @return
     */
    public double getGridSpacing() {
      return this.gridSpacing;
    }



    /**
     * Gets the Nth subSurface on the surface
     *
     * @param numSubSurfaceCols  Number of grid points along length
     * @param numSubSurfaceRows  Number of grid points along width
     * @param numSubSurfaceOffset Number of grid poits for offset
     * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
     *
     */
    public GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols, int numSubSurfaceRows, int numSubSurfaceOffset, int n) {

        Iterator it = getSubsetSurfacesIterator(numSubSurfaceCols, numSubSurfaceRows, numSubSurfaceOffset);
        int count = 0;

        // while there are more subset surfaces
        while(it.hasNext()) {
          // if this is the desired surface
          if(count==n)
            return (GriddedSubsetSurface)it.next();
          ++count;
        }

        throw new RuntimeException("EvenlyGriddeddsurface:getNthSubsetSurface::Inavlid n value for subSurface");
    }


    /**
     * Gets the Nth subSurface on the surface
     *
     * @param subSurfaceLength  subsurface length in km
     * @param subSurfaceWidth  subsurface width in km
     * @param subSurfaceOffset offset in km
     * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
     *
     */
    public GriddedSubsetSurface getNthSubsetSurface(double subSurfaceLength, double subSurfaceWidth, double subSurfaceOffset, int n) {
       return getNthSubsetSurface((int)Math.rint(subSurfaceLength/gridSpacing),
                                  (int)Math.rint(subSurfaceWidth/gridSpacing),
                                  (int)Math.rint(subSurfaceOffset/gridSpacing),
                                  n);
    }

    /**
     * Get the subSurfaces on this fault
     *
     * @param numSubSurfaceCols  Number of grid points according to length
     * @param numSubSurfaceRows  Number of grid points according to width
     * @param numSubSurfaceOffset Number of grid poits for offset
     *
     */
    public Iterator getSubsetSurfacesIterator(int numSubSurfaceCols, int numSubSurfaceRows, int numSubSurfaceOffset) {

        // number of subSurfaces along the length of fault
        int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffset +1);

        // there is only one subSurface
        if(nSubSurfaceAlong <=1) {
          nSubSurfaceAlong=1;
          numSubSurfaceCols = numCols;
        }

        // nnmber of subSurfaces along fault width
        int nSubSurfaceDown =  (int)Math.floor((numRows-numSubSurfaceRows)/numSubSurfaceOffset +1);

        // one subSurface along width
        if(nSubSurfaceDown <=1) {
          nSubSurfaceDown=1;
          numSubSurfaceRows = numRows;
        }


        // save the subSurfaces in a vector
        int col = 0;
        int row =0;
        v.clear();
        for(int j=0; j < nSubSurfaceDown; ++j, row=row+numSubSurfaceOffset) {
          col = 0;
          for(int i=0;i < nSubSurfaceAlong ; ++i, col=col+numSubSurfaceOffset) {
             GriddedSubsetSurface subsetSurfaces =
                  new GriddedSubsetSurface((int)numSubSurfaceRows,(int)numSubSurfaceCols,row,col,this);
             v.add(subsetSurfaces);
          }
       }
       return v.iterator();
   }


   /**
    * Get the subSurfaces on this fault
    *
    * @param subSurfaceLength  Sub Surface length in km
    * @param subSurfaceWidth   Sub Surface width in km
    * @param subSurfaceOffset  Sub Surface offset
    * @return           Iterator over all subSurfaces
    */
    public Iterator getSubsetSurfacesIterator(double subSurfaceLength,
                                              double subSurfaceWidth,
                                              double subSurfaceOffset) {

       return getSubsetSurfacesIterator((int)Math.rint(subSurfaceLength/gridSpacing),
                                        (int)Math.rint(subSurfaceWidth/gridSpacing),
                                        (int)Math.rint(subSurfaceOffset/gridSpacing));

    }


    /**
     *
     * @param subSurfaceLength subSurface length in km
     * @param subSurfaceWidth  subSurface Width in km
     * @param subSurfaceOffset subSurface offset
     * @return total number of subSurface along the fault
     */
    public int getNumSubsetSurfaces(double subSurfaceLength,double subSurfaceWidth,double subSurfaceOffset){
      int lengthCols =  (int)Math.rint(subSurfaceLength/gridSpacing);
      int widthCols =    (int)Math.rint(subSurfaceWidth/gridSpacing);
      int offsetCols =   (int)Math.rint(subSurfaceOffset/gridSpacing);
      int totalSubSurfaces =1;
      // number of subSurfaces along the length of fault
       int nSubSurfaceAlong = (int)Math.floor((numCols-lengthCols)/offsetCols +1);

       // there is only one subSurface
       if(nSubSurfaceAlong <=1) {
         nSubSurfaceAlong=1;
       }

       // nnmber of subSurfaces along fault width
       int nSubSurfaceDown =  (int)Math.floor((numRows-widthCols)/offsetCols +1);

       // one subSurface along width
       if(nSubSurfaceDown <=1) {
         nSubSurfaceDown=1;
       }
     totalSubSurfaces =   nSubSurfaceAlong * nSubSurfaceDown;
     return totalSubSurfaces;
    }

}