package org.scec.util;

import java.util.*;
import java.text.*;
import org.scec.sha.surface.*;

import org.scec.sha.calc.RelativeLocation;
import org.scec.sha.fault.*;
import org.scec.exceptions.*;


import org.scec.param.*;
import org.scec.data.function.*;
import org.scec.data.*;
import org.scec.gui.plot.jfreechart.*;

/**
 * <p>Title: FaultUtils</p>
 * <p>Description: Collection of static utilities used in conjunction with
 * strike, dip and rake angles of faults.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Steven W. Rock
 * @version 1.0
 */

public final class FaultUtils {

    protected final static String C = "FaultUtils";
    protected final static boolean D = false;

    private static final String S1 = C + ": assertValidStrike(): ";
    private static final String S2 = C + ": assertValidDip(): ";
    private static final String S3 = C + ": assertValidRake(): ";




    /**
     * Checks that the strike angle fits within the definition<p>
     * <code>0 <= strike <= 360</code><p>
     * @param strike                    Angle to validate
     * @throws InvalidRangeException    Thrown if not valid angle
     */
    public static void assertValidStrike( double strike)
        throws InvalidRangeException
    {

        if( strike < 0 ) throw new InvalidRangeException( S1 +
            "Strike angle cannot be less than zero"
        );
        if( strike > 360 ) throw new InvalidRangeException( S1 +
            "Strike angle cannot be greater than 360"
        );
    }


    /**
     * Checks that the dip angle fits within the definition<p>
     * <code>0 <= dip <= 90</code><p>
     * @param dip                       Angle to validate
     * @throws InvalidRangeException    Thrown if not valid angle
     */
    public static void assertValidDip( double dip)
        throws InvalidRangeException
    {
        if( dip < 0 ) throw new InvalidRangeException( S2 +
            "Dip angle cannot be less than zero"
        );
        if( dip > 90 ) throw new InvalidRangeException( S2 +
            "Dip angle cannot be greater than 90"
        );
    }


    /**
     * Checks that the rake angle fits within the definition<p>
     * <code>-180 <= rake <= 180</code><p>
     * @param rake                      Angle to validate
     * @throws InvalidRangeException    Thrown if not valid angle
     */
    public static void assertValidRake( double rake)
        throws InvalidRangeException
    {
        if( rake < -180 ) throw new InvalidRangeException( S3 +
            "Strike angle cannot be less than -180"
        );
        if( rake > 180 ) throw new InvalidRangeException( S3 +
            "Strike angle cannot be greater than 180"
        );
    }



    /*
     7/17/2002: SWR: Not needed anymore since made FaultTraceXYDataSet. FaultTrace
     can be passed right to JFreeChart. No longer need conversion to ArbDiscrFunction2DWithParams.


     * Converts a GriddedSurface into a ArbDiscrFunction2DWithParams so that it
     * can be plotted in a JFreeChart plotter. <P>
     * Note: SWR - May need to redo this framework so that no conversion is
     * needed. Hold off for now until this becomes a problem.
     * @param surface
     * @return
     * /
    public static ArbDiscrFuncWithParams getFaultTraceFunction2DWithParams(FaultTrace faultTrace){

        StringParameter level = new StringParameter("Trace");
        level.setValue( faultTrace.getName() );

        ParameterList paramList = new ParameterList();
        paramList.addParameter(level);

        ArbDiscrFuncWithParams function = new ArbDiscrFuncWithParams("Latitude", "Longitude", paramList);

        ListIterator it = faultTrace.listIterator();
        while( it.hasNext() ){

            Object obj = it.next();
            if( obj instanceof Location){

                Location loc = (Location)obj;

                function.set(
                    new Double( loc.getLongitude() ),
                    new Double( loc.getLatitude() )
                );
            }

        }
        return function;
    }
    */


    /*
     7/17/2002: SWR: Not needed anymore since made GriddedSurfaceXYDataSet. GriddedSurfaces
     can be passed right to JFreeChart. No longer need conversion to ArbDiscrFunction2DWithParams.

     *
     * Converts a GriddedSurface into a ArbDiscrFunction2DWithParams so that it
     * can be plotted in a JFreeChart plotter. <P>
     * Note: SWR - May need to redo this framework so that no conversion is
     * needed. Hold off for now until this becomes a problem.
     * @param surface
     * @return
     * /
    public static ListOfArbDiscrFuncWithParams getGriddedSurfaceFunctions2DWithParams(GriddedSurfaceAPI surface, String faultName){

        String S = C + ": getGriddedSurfaceFunctions2DWithParams(): ";
        if( D ) System.out.println(S + "Starting");

        int rows = surface.getNumRows();

        if( D ) System.out.println(S + "Rows = " + rows);
        if( D ) System.out.println(S + "Cols = " + surface.getNumCols());

        ListOfArbDiscrFuncWithParams list = new ListOfArbDiscrFuncWithParams();
        list.setName(faultName);
        list.setXAxisName("Longitude");
        list.setYAxisName("Latitude");

        DecimalFormat format = new DecimalFormat("#,###.##");

        // int cols = surface.getNumCols();
        boolean first = true;
        int counter = 0;
        ListIterator it = null;
        for( int i = 0; i < rows; i++){

            if( D ) System.out.println(S + "Row " + i);

            StringParameter level = new StringParameter("Depth");
            level.setValue( "" + i );
            ParameterList paramList = new ParameterList();
            paramList.addParameter(level);

            ArbDiscrFuncWithParams function = new ArbDiscrFuncWithParams("Longitude", "Latitude", paramList);


            it = surface.getColumnIterator( i );
            counter = 0;
            first = true;
            while( it.hasNext() ){

                Object obj = it.next();
                counter++;

                //if( D ) System.out.println(S + "Col " + counter);
                // if( D && obj != null )if( D ) System.out.println(S + "Object " + obj.toString());

                if( obj instanceof Location){
                    Location loc = (Location)obj;
                    function.set( new Double( loc.getLongitude() ), new Double( loc.getLatitude() ) );

                    if( first ){
                        first = false;
                        double depth = loc.getDepth();

                        String depthStr = format.format(depth);
                        level.setValue( "" + depthStr);
                    }

                }

            }

            list.addArbDiscrFunction2DWithParams(function);

        }
        if( D ) System.out.println(S + "Ending");
        return list;
    }
    */



}
