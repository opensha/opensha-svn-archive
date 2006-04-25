package org.opensha.sha.surface;

import java.util.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.surface.*;
import org.opensha.sha.fault.*;
import org.opensha.exceptions.*;
import org.opensha.data.*;

/**
 * <b>Title:</b> StirlingGriddedSurface.  This creates an EvenlyGriddedSurface
 * representation of the fault using a scheme described by Mark Stirling
 * to Ned Field in 2001, where grid points are projected down dip at
 * an angle perpendicular to the end-points of the faultTrace.  Use the setAveDipDir()
 * method to over ride this dipping direction.<br>
 * <b>Description:</b> <br>
 * @author Ned Field.
 * @version 1.0
 */

public class StirlingGriddedSurface extends EvenlyGriddedSurfFromSimpleFaultData {

    protected final static String C = "StirlingGriddedSurface";
    protected final static boolean D = false;

    protected double aveDipDir = Double.NaN;

    protected final static double PI_RADIANS = Math.PI / 180;
    protected final static String ERR = " is null, unable to process.";

    public StirlingGriddedSurface() { super(); }

    public StirlingGriddedSurface(SimpleFaultData simpleFaultData,
                                  double gridSpacing) throws FaultException {

      super(simpleFaultData, gridSpacing);
      createEvenlyGriddedSurface();
    }

    public StirlingGriddedSurface(FaultTrace faultTrace,
                                  double aveDip,
                                  double upperSeismogenicDepth,
                                  double lowerSeismogenicDepth,
                                  double gridSpacing) throws FaultException {

      super(faultTrace, aveDip, upperSeismogenicDepth, lowerSeismogenicDepth,
            gridSpacing);
      createEvenlyGriddedSurface();
    }

    public StirlingGriddedSurface( SimpleFaultData simpleFaultData,
                                        double gridSpacing,double aveDipDir)
                                        throws FaultException {

        super(simpleFaultData, gridSpacing);
        setAveDipDir(aveDipDir);
        createEvenlyGriddedSurface();
    }


    public StirlingGriddedSurface( FaultTrace faultTrace,
                                        double aveDip,
                                        double upperSeismogenicDepth,
                                        double lowerSeismogenicDepth,
                                        double gridSpacing,double aveDipDir )
                                        throws FaultException {

        super(faultTrace, aveDip, upperSeismogenicDepth, lowerSeismogenicDepth, gridSpacing);
        setAveDipDir(aveDipDir);
        createEvenlyGriddedSurface();
    }



    /**
     * Creates the Stirling Gridded Surface from the Simple Fault Data
     * @throws FaultException
     */
    public void createEvenlyGriddedSurface() throws FaultException {

        String S = C + ": createEvenlyGriddedSurface():";
        if( D ) System.out.println(S + "Starting");

        assertValidData();


        final int numSegments = faultTrace.getNumLocations() - 1;
        final double avDipRadians = aveDip * PI_RADIANS;
        final double gridSpacingCosAveDipRadians = gridSpacing * Math.cos( avDipRadians );
        final double gridSpacingSinAveDipRadians = gridSpacing * Math.sin( avDipRadians );

        double[] segmentLenth = new double[numSegments];
        double[] segmentAzimuth = new double[numSegments];
        double[] segmentCumLenth = new double[numSegments];

        double cumDistance = 0;
        int i = 0;

        Location firstLoc;
        Location lastLoc;
        double aveDipDirection;

       // Find ave dip direction (defined by end locations):
        if( Double.isNaN(aveDipDir) ) {
          firstLoc = faultTrace.getLocationAt(0);
          lastLoc = faultTrace.getLocationAt(faultTrace.getNumLocations() - 1);;
          Direction aveDir = RelativeLocation.getDirection(firstLoc, lastLoc);
          if (D) System.out.println("aveDir.getAzimuth(): = " + aveDir.getAzimuth());
          aveDipDirection = ( aveDir.getAzimuth() + 90 );
        }
        else {
          aveDipDirection = aveDipDir;
        }

        if (D) System.out.println("aveDipDirection = " + aveDipDirection);


        // Iterate over each Location in Fault Trace
        // Calculate distance, cumulativeDistance and azimuth for
        // each segment
        ListIterator it = faultTrace.listIterator();
        firstLoc = (Location)it.next();
        lastLoc = firstLoc;
        Location loc = null;
        Direction dir = null;
        while( it.hasNext() ){

            loc = (Location)it.next();
            dir = RelativeLocation.getDirection(lastLoc, loc);

            double azimuth = dir.getAzimuth();
            double distance = dir.getHorzDistance();
            cumDistance += distance;

            segmentLenth[i] = distance;
            segmentAzimuth[i] = azimuth;
            segmentCumLenth[i] = cumDistance;

            i++;
            lastLoc = loc;

        }

        // Calculate down dip width
        double downDipWidth = (lowerSeismogenicDepth-upperSeismogenicDepth)/Math.sin( avDipRadians );

        // Calculate the number of rows and columns
        int rows = 1 + Math.round((float) (downDipWidth/gridSpacing));
        int cols = 1 + Math.round((float) (segmentCumLenth[numSegments - 1] / gridSpacing));


        if(D) System.out.println("numLocs: = " + faultTrace.getNumLocations());
        if(D) System.out.println("numSegments: = " + numSegments);
        if(D) System.out.println("firstLoc: = " + firstLoc);
        if(D) System.out.println("lastLoc(): = " + lastLoc);
        if(D) System.out.println("downDipWidth: = " + downDipWidth);
        if(D) System.out.println("totTraceLength: = " + segmentCumLenth[ numSegments - 1]);
        if(D) System.out.println("numRows: = " + rows);
        if(D) System.out.println("numCols: = " + cols);


        // Create GriddedSurface
        int segmentNumber, ith_row, ith_col = 0;
        double distanceAlong, distance, hDistance, vDistance;
        //location object
        Location location1;


        //initialize the num of Rows and Cols for the container2d object that holds
        setRowsAndColsInContainer2d(rows,cols);


        // Loop over each column - ith_col is ith grid step along the fault trace
        if( D ) System.out.println(S + "Iterating over columns up to " + cols );
        while( ith_col < cols ){

            if( D ) System.out.println(S + "ith_col = " + ith_col);

            // calculate distance from column number and grid spacing
            distanceAlong = ith_col * gridSpacing;
            if( D ) System.out.println(S + "distanceAlongFault = " + distanceAlong);

            // Determine which segment distanceAlong is in
            segmentNumber = 1;
            while( segmentNumber <= numSegments && distanceAlong > segmentCumLenth[ segmentNumber - 1] ){
                segmentNumber++;
            }
            // put back in last segment if grid point has just barely stepped off the end
            if( segmentNumber == numSegments+1) segmentNumber--;

            if( D ) System.out.println(S + "segmentNumber " + segmentNumber );

            // Calculate the distance from the last segment point
            if ( segmentNumber > 1 ) distance = distanceAlong - segmentCumLenth[ segmentNumber - 2 ];
            else distance = distanceAlong;
            if( D ) System.out.println(S + "distanceFromLastSegPt " + distance );

            // Calculate the grid location along fault trace and put into grid
            location1 = faultTrace.getLocationAt( segmentNumber - 1 );
            dir = new Direction(0, distance, segmentAzimuth[ segmentNumber - 1 ], 0);

            // location on the trace
            Location traceLocation = RelativeLocation.getLocation( location1, dir  );

            // get location at the top of the fault surface
            Location topLocation;
            if(traceLocation.getDepth() < upperSeismogenicDepth) {
                vDistance = upperSeismogenicDepth - traceLocation.getDepth();
                hDistance = vDistance / Math.tan( avDipRadians );
                dir = new Direction(vDistance, hDistance, aveDipDirection, 0);
                topLocation = RelativeLocation.getLocation( traceLocation, dir );
            }
            else
                topLocation = traceLocation;

            setLocation(0, ith_col, (Location)topLocation.clone());
            if( D ) System.out.println(S + "(x,y) topLocation = (0, " + ith_col + ") " + topLocation );

            // Loop over each row - calculating location at depth along the fault trace
            ith_row = 1;
            while(ith_row < rows){

                if( D ) System.out.println(S + "ith_row = " + ith_row);

                // Calculate location at depth and put into grid
                hDistance = ith_row * gridSpacingCosAveDipRadians;
                vDistance = -ith_row * gridSpacingSinAveDipRadians;

                dir = new Direction(vDistance, hDistance, aveDipDirection, 0);

                Location depthLocation = RelativeLocation.getLocation( topLocation, dir );
                setLocation(ith_row, ith_col, (Location)depthLocation.clone());
                if( D ) System.out.println(S + "(x,y) depthLocation = (" + ith_row + ", " + ith_col + ") " + depthLocation );

                ith_row++;
            }
            ith_col++;
        }

        if( D ) System.out.println(S + "Ending");
    }


    /**
     * This method allows one to set the averate dip direction (points going down dip will
     * be parallel to this direction).  Set this as Double.NaN to compute this direction as perpendicular
     * to the end points of the fault (Double.NaN is the default setting).
     * @param aveDipDir
     */
    public void setAveDipDir(double aveDipDir) {
      this.aveDipDir = aveDipDir;
    }
}
