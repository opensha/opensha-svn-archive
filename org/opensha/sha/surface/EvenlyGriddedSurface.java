package org.opensha.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;

import org.opensha.sha.fault.*;
import org.opensha.data.Location;
import org.opensha.util.FaultUtils;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.exceptions.LocationException;
import org.opensha.data.*;
import org.opensha.calc.RelativeLocation;


/**
 * <b>Title:</b> GriddedSurface<p>
 *
 * <b>Description:</b> Base implementation of the EvenlyGriddedSurfaceAPI.
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */
public abstract class EvenlyGriddedSurface
         extends Container2D
         implements EvenlyGriddedSurfaceAPI {


    /** Class name for debugging. */
    protected final static String C = "EvenlyGriddedSurface";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /** The average strike of this surface on the Earth.  */
    protected double aveStrike=Double.NaN;

    /** The average dip of this surface into the Earth.  */
    protected double aveDip=Double.NaN;

    /**
     * @todo Variables
     */
    protected double gridSpacing;

    /**
     * No Argument constructor, called from classes extending it.
     *
     */
    protected EvenlyGriddedSurface(){}

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
     *  Set an object in the 2D grid. Ensures the object passed in is a Location.
     *
     * @param  row                                 The row to set the Location.
     * @param  column                              The row to set the Location.
     * @param  obj                                 Must be a Location object
     * @exception  throws UnsupportedOperationException      Throws this exception if
     * once created user tries to change any location on the surface.
     */
    public void set( int row, int column, Object obj ) throws UnsupportedOperationException{

      throw new UnsupportedOperationException("EvenlyGriddedSurface does not the user the locations once created.");
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
    protected void setLocation( int row, int column, Location location ) {
        super.set( row, column, location );
    }



    /**
     *  Retrieves a Location in the 2D grid - does the same thing as get except
     *  that it casts the returned object to a Location.
     *
     * @param  row     The row to get this Location from.
     * @param  column  The column to get this Location from.
     * @return         The location stored at the row and column.
     * @exception  LocationException  Thown if the object being retrieved cannot be cast to a Location.
     */
    public Location getLocation( int row, int col )
             throws LocationException {
        String S = C + ": getLocation():";
        if ( exist( row, col ) ) {
            return ( Location ) get( row, col );
        } else {
            throw new LocationException( S + "Requested object doesn't exist in " + row + ", " + col );
        }
    }


    /** Returns the average strike of this surface on the Earth.  */
    public double getAveStrike() { return aveStrike; }

    /** Returns the average dip of this surface into the Earth.  */
    public double getAveDip() { return aveDip; }


    /** Does same thing as listIterator() in super Interface */
    public ListIterator getLocationsIterator() { return super.listIterator(); }

    /**
     * Put all the locations of this surface into a location list
     *
     * @return
     */
    public LocationList getLocationList() {
      LocationList locList = new LocationList();
      Iterator it = this.getLocationsIterator();
      while(it.hasNext()) locList.addLocation((Location)it.next());
      return locList;
    }



    final static char TAB = '\t';
    /** Prints out each location and fault information for debugging */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append( C + '\n');
        if ( aveStrike != Double.NaN ) b.append( "Ave. Strike = " + aveStrike + '\n' );
        if ( aveDip != Double.NaN ) b.append( "Ave. Dip = " + aveDip + '\n' );

        b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

        String superStr = super.toString();
        //int index = superStr.indexOf('\n');
        //if( index > 0 ) superStr = superStr.substring(index + 1);
        b.append( '\n' + superStr );

        return b.toString();
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
    public GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols,
                                                    int numSubSurfaceRows,
                                                    int numSubSurfaceOffset,
                                                    int n) {
      // number of subSurfaces along the length of fault
      int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffset +1);

      // there is only one subSurface
      if(nSubSurfaceAlong <=1) {
        nSubSurfaceAlong=1;
        numSubSurfaceCols = numCols;
      }

      // number of subSurfaces down fault width
      int nSubSurfaceDown =  (int)Math.floor((numRows-numSubSurfaceRows)/numSubSurfaceOffset +1);

      // one subSurface along width
      if(nSubSurfaceDown <=1) {
        nSubSurfaceDown=1;
        numSubSurfaceRows = numRows;
      }

      return getNthSubsetSurface(numSubSurfaceCols, numSubSurfaceRows, numSubSurfaceOffset, nSubSurfaceAlong, n);
   //     throw new RuntimeException("EvenlyGriddeddsurface:getNthSubsetSurface::Inavlid n value for subSurface");
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
    private GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols,int numSubSurfaceRows,
                                                     int numSubSurfaceOffset,int nSubSurfaceAlong,
                                                     int n){
      //getting the row number in which that subsetSurface is present
      int row = n/nSubSurfaceAlong * numSubSurfaceOffset;

      //getting the column from which that subsetSurface starts
      int col = n%nSubSurfaceAlong * numSubSurfaceOffset;

      return (new GriddedSubsetSurface((int)numSubSurfaceRows,(int)numSubSurfaceCols,row,col,this));
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
    public GriddedSubsetSurface getNthSubsetSurface(double subSurfaceLength,
                                                   double subSurfaceWidth,
                                                   double subSurfaceOffset,
                                                   int n) {
       return getNthSubsetSurface((int)Math.rint(subSurfaceLength/gridSpacing+1),
                                  (int)Math.rint(subSurfaceWidth/gridSpacing+1),
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

        //vector to store the GriddedSurface
        ArrayList v= new ArrayList();

        // number of subSurfaces along the length of fault
        int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffset +1);

        // there is only one subSurface
        if(nSubSurfaceAlong <=1) {
          nSubSurfaceAlong=1;
          numSubSurfaceCols = numCols;
        }

        // number of subSurfaces along fault width
        int nSubSurfaceDown =  (int)Math.floor((numRows-numSubSurfaceRows)/numSubSurfaceOffset +1);

        // one subSurface along width
        if(nSubSurfaceDown <=1) {
          nSubSurfaceDown=1;
          numSubSurfaceRows = numRows;
        }

        //getting the total number of subsetSurfaces
        int totalSubSetSurface = nSubSurfaceAlong * nSubSurfaceDown;
        //emptying the vector
        v.clear();

        //adding each subset surface to the ArrayList
        for(int i=0;i<totalSubSetSurface;++i)
          v.add(getNthSubsetSurface(numSubSurfaceCols,numSubSurfaceRows,numSubSurfaceOffset,nSubSurfaceAlong,i));

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

       return getSubsetSurfacesIterator((int)Math.rint(subSurfaceLength/gridSpacing+1),
                                        (int)Math.rint(subSurfaceWidth/gridSpacing+1),
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
      int lengthCols =  (int)Math.rint(subSurfaceLength/gridSpacing+1);
      int widthCols =    (int)Math.rint(subSurfaceWidth/gridSpacing+1);
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



    /**
     * This returns the total length of the surface
     * @return double
     */
    public double getSurfaceLength() {

        return getGridSpacing() * (getNumCols()-1);
    }

    /**
     * This returns the surface width (down dip)
     * @return double
     */
    public double getSurfaceWidth() {
      return getGridSpacing() * (getNumRows()-1);
    }

    /**
     * Returns the Surface Metadata with the following info:
     * <ul>
     * <li>AveDip
     * <li>Surface length
     * <li>Surface DownDipWidth
     * <li>GridSpacing
     * <li>NumRows
     * <li>NumCols
     * <li>Number of locations on surface
     * <p>Each of these elements are represented in Single line with tab("\t") delimitation.
     * <br>Then follows the location of each point on the surface with the comment String
     * defining how locations are represented.</p>
     * <li>#Surface locations (Lat Lon Depth)
     * <p>Then until surface locations are done each line is the point location on the surface.
     *
     * </ul>
     * @return String
     */
    public String getSurfaceMetadata() {
      String surfaceMetadata;
      surfaceMetadata = (float)aveDip + "\t";
      surfaceMetadata += (float)getSurfaceLength() + "\t";
      surfaceMetadata += (float)getSurfaceWidth() + "\t";
      surfaceMetadata += (float)Double.NaN + "\t";
      int numRows = getNumRows();
      int numCols = getNumCols();
      surfaceMetadata += numRows + "\t";
      surfaceMetadata += numCols + "\t";
      surfaceMetadata += (numRows * numCols) + "\n";
      surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
      ListIterator it = getLocationsIterator();
      while (it.hasNext()) {
        Location loc = (Location) it.next();
        surfaceMetadata += (float)loc.getLatitude()+"\t";
        surfaceMetadata += (float)loc.getLongitude()+"\t";
        surfaceMetadata += (float)loc.getDepth()+"\n";
      }
      return surfaceMetadata;
    }
}
