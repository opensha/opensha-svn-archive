package org.scec.sha.fault;

import java.util.*;
import java.io.*;
import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.data.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class FaultTraceFactory {

    protected final static String C = "FaultTraceFactory";
    protected final static boolean D = false;

    protected final static FaultException ERR = new FaultException(
           C + ": loadFaultTraces(): Missing metadata from trace, file bad format."
    );

    public static FaultTraceList loadFaultTraces(ArrayList fileNames) throws FaultException{

        // Debug
        String S = C + ": loadFaultTraces(): ";
        if( D ) System.out.println(S + "Starting");

        // variable declaration
        FaultTraceList list = new FaultTraceList();
        ArrayList rawFaultTraceData = null;
        String l1, l2, l3, l4, dataLine, faultName, latStr, lonStr, temp;
        int i, index, numPoints = 0;
        double dip, downDipWidth, depthToTop, lowerSeismoDepth, upperSeismoDepth;
        double lat, lon;


        for( int j = 0; j < fileNames.size(); j++){

            // Load in from file the data
            if( D ) System.out.println(S + "Loading file = " + fileNames.get(j).toString());
            try{ rawFaultTraceData = FileUtils.loadInCharFile( fileNames.get(j).toString() ); }
            catch( FileNotFoundException e){ System.out.println(S + e.toString()); }
            catch( IOException e){ System.out.println(S + e.toString());}

            // Exit if no data found in list
            if( rawFaultTraceData == null) throw new
                FaultException(S + "No data loaded from file. File may be empty or doesn't exist.");

            // Loop over data parsing and building traces, then add to list
            ListIterator it = rawFaultTraceData.listIterator();
            while( it.hasNext() ){

                // Get the file name as third token in this line
                // Extract metadata header line1
                if( !it.hasNext() ) throw ERR;
                else l1 = it.next().toString().trim();

                index =  l1.indexOf(' ');
                if( index < 1) throw ERR;
                l1 = l1.substring(index + 1).trim();

                index =  l1.indexOf(' ');
                if( index < 1) throw ERR;
                l1 = l1.substring(index + 1).trim();


                // skip this line
                // Extract metadata header line1
                if( !it.hasNext() ) throw ERR;
                else l2 = it.next().toString().trim();


                // extract all three tokens from this line
                // Extract metadata header line1
                if( !it.hasNext() ) throw ERR;
                else l3 = it.next().toString().trim();


                // first column
                index =  l3.indexOf(' ');
                if( index < 1) throw ERR;
                temp = l3.substring(0, index).trim();
                l3 = l3.substring(index + 1).trim();

                try{ dip = new Double(temp).doubleValue(); }
                catch( NumberFormatException e){ throw ERR; }



                // Second and third column
                index =  l3.indexOf(' ');
                if( index < 1) throw ERR;
                temp = l3.substring(0, index).trim();
                l3 = l3.substring(index + 1).trim();

                try{ downDipWidth = new Double(temp).doubleValue(); }
                catch( NumberFormatException e){ throw ERR; }

                try{ depthToTop = new Double(l3).doubleValue(); }
                catch( NumberFormatException e){ throw ERR; }


                // Calculate derived variables
                upperSeismoDepth = depthToTop;
                lowerSeismoDepth = depthToTop + downDipWidth*Math.sin( Math.abs(dip) * Math.PI / 180 );

                // extract number of locations as first token in this line
                // Extract metadata header line1
                if( !it.hasNext() ) throw ERR;
                else l4 = it.next().toString().trim();

                try{ numPoints = new Integer(l4).intValue(); }
                catch( NumberFormatException e){ throw ERR; }


                FaultTrace trace = new FaultTrace(l1);
                trace.setAveDip(dip);
                trace.setLowerSeismogenicDepth( lowerSeismoDepth );
                trace.setUpperSeismogenicDepth( upperSeismoDepth );

                if( D ) System.out.println(S + "Fault Name " + l1);
                if( D ) System.out.println(S + "dip" + dip);
                if( D ) System.out.println(S + "lowerSeismoDepth = " + lowerSeismoDepth);
                if( D ) System.out.println(S + "upperSeismoDepth" + upperSeismoDepth);

                // Extract all data lines
                for( i = 0; i < numPoints; i++){

                    if( !it.hasNext() ) throw ERR;
                    else dataLine = it.next().toString().trim();

                    // Etract lat & lon
                    index =  dataLine.indexOf(' ');
                    if( index < 1) throw ERR;
                    latStr = dataLine.substring(0, index).trim();
                    lonStr = dataLine.substring(index + 1).trim();

                    try{ lat = new Double(latStr).doubleValue(); }
                    catch( NumberFormatException e){ throw ERR; }

                    try{ lon = new Double(lonStr).doubleValue(); }
                    catch( NumberFormatException e){ throw ERR; }

                    Location loc = new Location(lat, lon, upperSeismoDepth);
                    trace.addLocation( (Location)loc.clone() );

                    if( D ) System.out.println(S + "Location" + loc.toString());

                }

                // reverse data ordering if dip negative, make positive and reverse trace order
                if( dip < 0 ) {
                    trace.setAveDip(-dip);
                    trace.reverse();
                }

                // All done processing trace, add
                list.addFaultTrace( trace );

            }
        }

        // Done
        if( D ) System.out.println(S + "Ending");
        return list;
    }


    public static FaultTrace getSierraMadre(){

        FaultTrace trace = new FaultTrace("Sierra Madre");
        trace.addLocation( new Location(34.12310, -117.73972, 0) );
        trace.addLocation( new Location(34.12188, -117.75504, 0) );
        trace.addLocation( new Location(34.13169, -117.76914, 0) );
        trace.addLocation( new Location(34.13046, -117.80714, 0) );
        trace.addLocation( new Location(34.13230, -117.81757, 0) );
        trace.addLocation( new Location(34.15866, -117.85986, 0) );
        trace.addLocation( new Location(34.14701, -117.88071, 0) );
        trace.addLocation( new Location(34.15008, -117.94017, 0) );
        trace.addLocation( new Location(34.16111, -117.98492, 0) );
        trace.addLocation( new Location(34.17521, -118.00270, 0) );
        trace.addLocation( new Location(34.17582, -118.06829, 0) );
        trace.addLocation( new Location(34.20096, -118.11182, 0) );
        trace.addLocation( new Location(34.20280, -118.14921, 0) );
        trace.addLocation( new Location(34.21260, -118.21419, 0) );
        trace.addLocation( new Location(34.22793, -118.24606, 0) );
        trace.addLocation( new Location(34.25797, -118.27978, 0) );
        trace.addLocation( new Location(34.27513, -118.28959, 0) );

        return trace;
    }

    /**
     *
     */
    public static void main(String[] args) {

        String S = C + " ; (): ";
        if( D ) System.out.println(S + "Starting");

        ArrayList files = new ArrayList();
        files.add("CALA.char");
        files.add("CALB.char");

        FaultTraceList list = FaultTraceFactory.loadFaultTraces(files);

        if( D ) System.out.println(S + list.toString());

        if( D ) System.out.println(S + "Ending");
    }
}
