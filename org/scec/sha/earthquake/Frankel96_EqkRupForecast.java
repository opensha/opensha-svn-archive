package org.scec.sha.earthquake;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.scec.util.*;
import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.FrankelGriddedFaultFactory;
import org.scec.sha.fault.GriddedFaultFactory;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.magdist.GuttenbergRichterMagFreqDist;
import org.scec.exceptions.FaultException;


/**
 * <p>Title: Frankel96_EqkRupForecast</p>
 * <p>Description:Frankel 1996 Earthquake Rupture Forecast. This class
 * reads the 3 given files namely Frankel96_CALA.char, Frankel96_CALA.char
 *  and Frankel96_CALB.gr .
 * Then it creates earthquake rupture objects according to the rates.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @Date : Aug 31, 2002
 * @version 1.0
 */

public class Frankel96_EqkRupForecast extends EqkRupForecast {

  /**
   * @todo variables
   */

  //for Debug purposes
  private static String  C = new String("Frankel96_EqkRupForecast");
  private boolean D =false;

  /**
   * used for error checking
   */
  protected final static FaultException ERR = new FaultException(
           C + ": loadFaultTraces(): Missing metadata from trace, file bad format."
    );

  /**
   * These are the decelaration of the static varibles to determine the type
   * and name of the file to be read
   */
  private final static int TYPE_A_CHAR_EQK = 0;
  private final static int TYPE_B_CHAR_EQK = 1;
  private final static int TYPE_B_GR_EQK = 2;
  private final static String FILE_A_CHAR_EQK="Frankel96_CALA.char";
  private final static String FILE_B_CHAR_EQK="Frankel96_CALB.char";
  private final static String FILE_B_GR_EQK="Frankel96_CALB.gr";

  /**
   * definition of the vectors for storing the data corresponding to the file type
   */
  private Vector FrankelA_CharEqkSources;
  private Vector FrankelB_CharEqkSources;
  private Vector FrankelB_GR_EqkSources;

  public Frankel96_EqkRupForecast() {


  }

  /**
   * Read the Characteristic files
   *
   * @param eqkType : It can be TYPE_A_CHAR_EQK or TYPE_B_CHAR_EQK or TYPE_B_GR_EQK
   * @throws FaultException
   */
  private  void readFrankel96_Char(int eqkType) throws FaultException{

    // Debug
    String S = C + ": readFrankel96_Char: ";
    if( D ) System.out.println(S + "Starting");
    String fileName="";
    GriddedFaultFactory factory;
    GuttenbergRichterMagFreqDist guttenbergRichter;
    //file to be read based on the flag that is received as the parameter
    // variable declaration
    ArrayList rawFaultTraceData = null;
    String  faultName="", temp;
    int i ;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon;
    int rake=0;
    double mag=0,rate=0,dip=0,downDipWidth=0,depthToTop=0;
    double aValue=0,bValue=0,magLower=0,magUpper=0,deltaMag=0;

    // Load in from file the data
    if(eqkType == TYPE_A_CHAR_EQK)
      fileName = new String (FILE_A_CHAR_EQK);
    else if(eqkType == TYPE_B_CHAR_EQK)
      fileName = new String(FILE_B_CHAR_EQK);
    else if(eqkType == TYPE_B_GR_EQK)
      fileName = new String(FILE_B_GR_EQK);

    if( D ) System.out.println(S + "Loading file = " + fileName );
    try{ rawFaultTraceData = FileUtils.loadInCharFile( fileName ); }
    catch( FileNotFoundException e){ System.out.println(S + e.toString()); }
    catch( IOException e){ System.out.println(S + e.toString());}

        // Exit if no data found in list
        if( rawFaultTraceData == null) throw new
            FaultException(S + "No data loaded from file. File may be empty or doesn't exist.");

        // Loop over data parsing and building traces, then add to list
        ListIterator it = rawFaultTraceData.listIterator();
        //reading the first line from the file
        while( it.hasNext() ){
          StringTokenizer st = new StringTokenizer(it.next().toString());
          while(st.hasMoreTokens()){
            //skipping the first word(that tells that it is char type faults)
            st.nextToken();
            //taking the 2nd word that tells that it is what fault type
            String token= new String(st.nextToken());

            //for Strike slip fault
            if(Integer.parseInt(token) == 1)
              rake =0;

            //for reverse fault
            if(Integer.parseInt(token) == 2)
              rake =90;

            //for normal fault
            if(Integer.parseInt(token) == 3)
              rake =-90;

            //reading the fault name
            faultName = new String(st.nextToken());
          }

          //reading the next line from the file
          st = new StringTokenizer(it.next().toString());

          // if we are reading the characteristic file
          if(eqkType==TYPE_A_CHAR_EQK || eqkType==TYPE_B_CHAR_EQK) {
            while(st.hasMoreTokens()){
            //reading the mag
             mag=Double.parseDouble(st.nextToken());
             //reading the rate
             rate=Double.parseDouble(st.nextToken());
            }
          }
          // if we are reading the GuttenbergRichter file
          else if(eqkType==TYPE_B_GR_EQK) {
            while(st.hasMoreTokens()){
            //reading the a-value
             aValue=Double.parseDouble(st.nextToken());
             //reading the b-value
             bValue=Double.parseDouble(st.nextToken());
             //reading the MagLower
             magLower=Double.parseDouble(st.nextToken());
             //reading the MagUpper
             magUpper=Double.parseDouble(st.nextToken());
             //reading the DeltaMag
             deltaMag=Double.parseDouble(st.nextToken());

             //FIX:FIX(Min,Num,Delta) constructor calling for the GuttenbergRichterMagFreqDist class
             guttenbergRichter = new GuttenbergRichterMagFreqDist(0,101,deltaMag);

             //FIX: totalCumRate is assumed to be a-value
             guttenbergRichter.setAllButTotMoRate(magLower,magUpper,aValue,bValue);
            }
          }


          //reading the third line from  the file
          st=new StringTokenizer(it.next().toString());
          while(st.hasMoreTokens()){
            // reading the dip
            dip=Double.parseDouble(st.nextToken());
            //redaing the downDipWidth
            downDipWidth=Double.parseDouble(st.nextToken());
            //reading the Depth to top
            depthToTop=Double.parseDouble(st.nextToken());
          }

          //reading the 4 line from the file that tells about the data points for faults
          int numOfDataLines=Integer.parseInt(it.next().toString().trim());
          // Calculate derived variables
          upperSeismoDepth = depthToTop;
          lowerSeismoDepth = depthToTop + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));

          FaultTrace faultTrace= new FaultTrace(faultName);

          //based on the num of the data lines reading the lat and long points for rthe faults
          for(i=0;i<numOfDataLines;++i) {
              if( !it.hasNext() ) throw ERR;
              st =new StringTokenizer(it.next().toString().trim());

              try{ lat = new Double(st.nextToken()).doubleValue(); }
              catch( NumberFormatException e){ throw ERR; }
              try{ lon = new Double(st.nextToken()).doubleValue(); }
              catch( NumberFormatException e){ throw ERR; }

              Location loc = new Location(lat, lon, upperSeismoDepth);
              faultTrace.addLocation( (Location)loc.clone());
              if( D ) System.out.println(S + "Location" + loc.toString());
          }
         // reverse data ordering if dip negative, make positive and reverse trace order
          if( dip < 0 ) {
             faultTrace.reverse();
             dip *= -1;
          }

          System.out.println(C+":faultTrace::"+faultTrace.toString());

          //FIX this is very very temp for the time being, has to ask Ned when he returns on Monday
          double prob = 1-Math.exp(-rate*10000);

          // we have to FIX the value of gridspacing here
          factory = new FrankelGriddedFaultFactory(faultTrace,
                                                   dip,
                                                   upperSeismoDepth,
                                                   lowerSeismoDepth,
                                                   1);

          GriddedSurfaceAPI surface = factory.getGriddedSurface();

          // we are not specifying the hypocenter location at this time
          ProbEqkRupture probEqkRup = new ProbEqkRupture();
          probEqkRup.setMag(mag);
          probEqkRup.setAveRake(rake);
          probEqkRup.setProbability(prob);
          probEqkRup.setRuptureSurface(surface);
        }

    // Done
    if( D ) System.out.println(S + "Ending");

  }

  public static  void main(String args[]){
    Frankel96_EqkRupForecast f= new Frankel96_EqkRupForecast();
    f.readFrankel96_Char(2);
  }
}