package org.scec.sha.fault;

import java.util.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.surface.*;
import org.scec.sha.fault.*;
import org.scec.exceptions.*;


import org.scec.param.*;
import org.scec.data.function.*;
import org.scec.data.*;

/**
 * <b>Title:</b> StirlingGriddedFaultFactory<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class StirlingGriddedFaultFactory extends SimpleGriddedFaultFactory {

    protected final static String C = "StirlingGriddedFaultFactory";
    protected final static boolean D = false;

    protected final static double PI_RADIANS = Math.PI / 180;
    protected final static String ERR = " is null, unable to process.";

    public StirlingGriddedFaultFactory() { super(); }

    public StirlingGriddedFaultFactory( SimpleFaultData simpleFaultData,
                                        double gridSpacing)
                                        throws FaultException {

        super(simpleFaultData.getFaultTrace(),
              simpleFaultData.getAveDip(),
              simpleFaultData.getUpperSeismogenicDepth(),
              simpleFaultData.getLowerSeismogenicDepth(),
              gridSpacing);
    }


    public StirlingGriddedFaultFactory( FaultTrace faultTrace,
                                        double aveDip,
                                        double upperSeismogenicDepth,
                                        double lowerSeismogenicDepth,
                                        double gridSpacing )
                                        throws FaultException {

        super(faultTrace, aveDip, upperSeismogenicDepth, lowerSeismogenicDepth, gridSpacing);
    }





    public GriddedSurfaceAPI getGriddedSurface() throws FaultException {

        String S = C + ": getGriddedSurface():";
        if( D ) System.out.println(S + "Starting");

        if( faultTrace == null  ) throw new FaultException(S + "Fault Trace" + ERR);
        if( aveDip == Double.NaN ) throw new FaultException(S + "aveDip" + ERR);
        if( upperSeismogenicDepth == Double.NaN  ) throw new FaultException(S + "upperSeismogenicDepth" + ERR);
        if( lowerSeismogenicDepth == Double.NaN  ) throw new FaultException(S + "lowerSeismogenicDepth" + ERR);
        if( gridSpacing == Double.NaN  ) throw new FaultException(S + "gridSpacing" + ERR);


        final int numSegments = faultTrace.getNumLocations() - 1;
        final double gridSpacingValue = gridSpacing;
        final double avDipRadians = aveDip * PI_RADIANS;
        final double gridSpacingCosAveDipRadians = gridSpacingValue * Math.cos( avDipRadians );
        final double gridSpacingSinAveDipRadians = gridSpacingValue * Math.sin( avDipRadians );

        double[] sementLenth = new double[numSegments];
        double[] sementAzimuth = new double[numSegments];
        double[] sementCumLenth = new double[numSegments];

        double cumDistance = 0;
        int i = 0;

        // Iterate over each Location in Fault Trace
        // Calculate distance, cumulativeDistance and azimuth for
        // each segment
        ListIterator it = faultTrace.listIterator();
        Location firstLoc = (Location)it.next();
        Location lastLoc = firstLoc;
        Location loc = null;
        Direction dir = null;
        while( it.hasNext() ){

            loc = (Location)it.next();
            dir = RelativeLocation.getDirection(lastLoc, loc);

            double azimuth = dir.getAzimuth();
            double distance = dir.getHorzDistance();
            cumDistance += distance;

            sementLenth[i] = distance;
            sementAzimuth[i] = azimuth;
            sementCumLenth[i] = cumDistance;

            // prep for next loop
            i++;
            lastLoc = loc;

        }

        // Calculate down dipth width
        double denominator = Math.sin( avDipRadians );

        double downDipWidth = Math.abs(upperSeismogenicDepth - lowerSeismogenicDepth);
        downDipWidth /= denominator;

        // Calculate the number of rows and columns
        int rows = ( new Double( Math.ceil( downDipWidth / gridSpacingValue ) ) ).intValue();
        int cols = ( new Double( Math.ceil( sementCumLenth[numSegments - 1] / gridSpacingValue ) ) ).intValue();


        // Calculate Average Dip Direction
        if(D) System.out.println("firstLoc: = " + firstLoc);
        if(D) System.out.println("lastLoc(): = " + lastLoc);


        // Find ave dip (defined by end locations):
        Direction aveDir = RelativeLocation.getDirection(firstLoc, lastLoc);
        if(D) System.out.println("aveDir.getAzimuth(): = " + aveDir.getAzimuth());
        double aveDipDirection = ( aveDir.getAzimuth() + 90 );


        // Create GriddedSurface
        int segmentNumber, ith_row, ith_col = 0;
        double distanceAlong, distance, hDistance, vDistance;
        Location location1;
        EvenlyGriddedSurface surface = new EvenlyGriddedSurface(rows, cols, this.getGridSpacing());


        // Loop over each column - ith_col is ith grid step along the fault trace
        if( D ) System.out.println(S + "Iterating over columns up to " + cols );
        while( ith_col < cols ){

            if( D ) System.out.println(S + "ith_col = " + ith_col);

            // calculate distance from column number and grid spacing
            distanceAlong = ith_col * gridSpacingValue;
            if( D ) System.out.println(S + "distanceAlong = " + distanceAlong);

            // Determine which segment distanceAlong is in
            segmentNumber = 1;
            while( sementCumLenth[ segmentNumber - 1] < distanceAlong ){
                segmentNumber++;
            }
            if( D ) System.out.println(S + "segmentNumber " + segmentNumber );

            // Calculate the distance from the last segment point
            if ( segmentNumber > 1 ) distance = distanceAlong - sementCumLenth[ segmentNumber - 2 ];
            else distance = distanceAlong;
            if( D ) System.out.println(S + "distanceAlong " + distanceAlong );

            // Calculate the grid location along fault trace and put into grid
            location1 = faultTrace.getLocationAt( segmentNumber - 1 );
            dir = new Direction(0, distance, sementAzimuth[ segmentNumber - 1 ], 0);
            Location surfaceLocation = RelativeLocation.getLocation( location1, dir  );
            surface.setLocation(0, ith_col, (Location)surfaceLocation.clone());
            if( D ) System.out.println(S + "(x,y) surfaceLocation = (0, " + ith_col + ") " + surfaceLocation );

            // Loop over each row - calculating location at depth along the fault trace
            ith_row = 1;
            while(ith_row < rows){

                if( D ) System.out.println(S + "ith_row = " + ith_row);

                // Calculate location at depth and put into grid
                hDistance = ith_row * gridSpacingCosAveDipRadians;
                vDistance = -ith_row * gridSpacingSinAveDipRadians;

                dir = new Direction(vDistance, hDistance, aveDipDirection, 0);

                Location depthLocation = RelativeLocation.getLocation( surfaceLocation, dir );
                surface.setLocation(ith_row, ith_col, (Location)depthLocation.clone());
                if( D ) System.out.println(S + "(x,y) depthLocation = (" + ith_row + ", " + ith_col + ") " + depthLocation );

                ith_row++;
            }
            ith_col++;
        }

        if( D ) System.out.println(S + "Ending");
        surface.setAveDip(aveDip);
        return surface;
    }

}
