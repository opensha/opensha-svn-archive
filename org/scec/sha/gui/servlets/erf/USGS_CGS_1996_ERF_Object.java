package org.scec.sha.gui.servlets.erf;

import java.io.*;
import java.util.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.*;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.util.FileUtils;
import org.scec.exceptions.FaultException;
import org.scec.calc.MomentMagCalc;
import org.scec.param.ParameterList;
import org.scec.sha.gui.servlets.erf.*;

/**
 * <p>Title: USGS_CGS_1996_ERF_Object</p>
 * <p>Description: Make the ERF Object for the USGS/CGS 1996 ERF as the Servlet mode</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class USGS_CGS_1996_ERF_Object implements ERF_API,java.io.Serializable{


  private String FAULT_CLASS_A = "A";
  private String FAULT_CLASS_B = "B";
  private String FAULTING_STYLE_SS = "SS";
  private String FAULTING_STYLE_R = "R";
  private String FAULTING_STYLE_N = "N";

  private double GRID_SPACING = 1.0;
  private double B_VALUE =0.9;
  private double MAG_LOWER = 6.5;
  private double DELTA_MAG = 0.1;


  /**
   * Vectors for holding the various sources, separated by type
   */
  private Vector FrankelA_CharEqkSources;
  private Vector FrankelB_CharEqkSources;
  private Vector FrankelB_GR_EqkSources;
  private Vector FrankelBackgrSeisSources;
  private Vector allSources;

  //timeSpan Object

  private TimeSpan timeSpan;

  /**
   * used for error checking
   */
  protected final static FaultException ERR = new FaultException(": loadFaultTraces(): Missing metadata from trace, file bad format.");

  //gets the Adjustable Param List
  ParameterList parameterList;
  ArrayList inputBackSeisFileLines, inputFaultFileLines;

  //default class constructor
  public USGS_CGS_1996_ERF_Object(ParameterList param,ArrayList backSeisFileLines,ArrayList faultFileLines, TimeSpan time) {

    parameterList =param;
    this.inputBackSeisFileLines = backSeisFileLines;
    this.inputFaultFileLines = faultFileLines;

    // get value of background seismicity paramter
    String backSeis = (String) parameterList.getParameter(USGS_CGS_1996_ERF_AdjustableParamsClass.BACK_SEIS_NAME).getValue();
    this.setTimeSpan(time);
    allSources = new Vector();
    System.out.println("BackSies is:"+backSeis);
    if (backSeis.equalsIgnoreCase(USGS_CGS_1996_ERF_AdjustableParamsClass.BACK_SEIS_INCLUDE)) {
      makeFaultSources();
      makeBackSeisSources();
      System.out.println("Back Sies included");
      // now create the allSources list:
      allSources.addAll(FrankelA_CharEqkSources);
      allSources.addAll(FrankelB_CharEqkSources);
      allSources.addAll(FrankelB_GR_EqkSources);
      allSources.addAll(FrankelBackgrSeisSources);
    }
    else if (backSeis.equalsIgnoreCase(USGS_CGS_1996_ERF_AdjustableParamsClass.BACK_SEIS_EXCLUDE)) {
      System.out.println("Back Sies excluded");
      makeFaultSources();
      // now create the allSources list:
      allSources.addAll(FrankelA_CharEqkSources);
      allSources.addAll(FrankelB_CharEqkSources);
      allSources.addAll(FrankelB_GR_EqkSources);
    }
    else {// only background sources
      System.out.println("Only BackSies");
      makeBackSeisSources();
      // now create the allSources list:
      allSources.addAll(FrankelBackgrSeisSources);
    }

    System.out.println("Number of sources are:"+allSources.size());


  }


  /**
   * Returns the number of earthquake sources
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return allSources.size();
  }


  /**
   *  This returns a list of sources (contains only one here)
   *
   * @return Vector of Prob Earthquake sources
   */
  public Vector  getSourceList(){
    return allSources;
  }



  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public ProbEqkRupture getRupture(int iSource, int nRupture) {
    return getSource(iSource).getRupture(nRupture);
  }

  /**
   * Return the earhthquake source at index i.   Note that this returns a
   * pointer to the source held internally, so that if any parameters
   * are changed, and this method is called again, the source obtained
   * by any previous call to this method will no longer be valid.
   *
   * @param iSource : index of the desired source .
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public ProbEqkSource getSource(int iSource) {

    return (ProbEqkSource)allSources.get(iSource);
  }

  /**
   * Get number of ruptures for source at index iSource
   * This method iterates through the list of 3 vectors for charA , charB and grB
   * to find the the element in the vector to which the source corresponds
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource){
    return getSource(iSource).getNumRuptures();
  }


  /**
   * set the TimeSpan in the ERF
   * @param timeSpan : TimeSpan object
   */
  public void setTimeSpan(TimeSpan time) {
    this.timeSpan=time;
  }

  /**
   * Read the Background Seismicity file and make the sources
   *
   */
  private  void makeBackSeisSources() {

    // Debug
    String S = ": makeBackSeisSources(): ";
    //if( D ) System.out.println(S + "Starting");

    FrankelBackgrSeisSources = new Vector();

    double lat, lon, rate, rateAtMag5;

    double aveRake=0.0;
    double aveDip=90;
    double tempMoRate = 1.0;
    double bValue = B_VALUE;
    double magUpper=7.0;
    double magDelta=0.2;
    double magLower1=0.0;
    int    numMag1=36;
    double magLower2=5.0;
    int    numMag2=11;

    // GR dist between mag 0 and 7, delta=0.2
    GutenbergRichterMagFreqDist grDist1 = new GutenbergRichterMagFreqDist(magLower1,numMag1,magDelta,
        tempMoRate,bValue);

    // GR dist between mag 5 and 7, delta=0.2
    GutenbergRichterMagFreqDist grDist2;

    PointPoissonEqkSource pointPoissonSource;

    // set timespan
    double timeDuration = timeSpan.getDuration();

    // Get iterator over input-file lines
    ListIterator it = inputBackSeisFileLines.listIterator();

    // skip first five header lines
    StringTokenizer st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    System.out.println("time duration:"+timeDuration);
    while( it.hasNext() ){
      String readString = it.next().toString();
      //System.out.println("String read: "+readString);
      // get next line
      st = new StringTokenizer(readString);

      lon =  Double.parseDouble(st.nextToken());
      lat =  Double.parseDouble(st.nextToken());
      rate = Double.parseDouble(st.nextToken());
      System.out.println("2222:rate="+rate);
      if (rate > 0.0) {  // ignore locations with a zero rate

        // scale all so the incremental rate at mag=0 index equals rate
        grDist1.scaleToIncrRate((int) 0,rate);

        // now get the rate at the mag=5 index
        rateAtMag5 = grDist1.getIncrRate((int) 25);

        // now scale all in the dist we want by rateAtMag5 (index 0 here)
        grDist2 = new GutenbergRichterMagFreqDist(magLower2,numMag2,magDelta,tempMoRate,bValue);
        grDist2.scaleToIncrRate((int) (0),rateAtMag5);

        // now make the source
        pointPoissonSource = new PointPoissonEqkSource(new Location(lat,lon),
            grDist2, timeDuration, aveRake,aveDip);

        // add the source
        FrankelBackgrSeisSources.add(pointPoissonSource);
        System.out.println("333333333333333333333");
      }
    }
    System.out.println("backseissources:"+FrankelBackgrSeisSources.size());

  }


  /**
   * Read the Fault file and make the sources
   *
   * @throws FaultException
   */
  private  void makeFaultSources() throws FaultException{

    FrankelA_CharEqkSources = new Vector();
    FrankelB_CharEqkSources = new Vector();
    FrankelB_GR_EqkSources = new Vector();

    // Debug
    String S =  ": makeSoureces(): ";

    GriddedFaultFactory factory;
    String  faultClass="", faultingStyle, faultName="";
    int i;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon, rake=0;
    double mag=0;  // used for magChar and magUpper (latter for the GR distributions)
    double charRate=0, dip=0, downDipWidth=0, depthToTop=0;

    // get adjustable parameters values
    double fracGR = ((Double) parameterList.getParameter(USGS_CGS_1996_ERF_AdjustableParamsClass.FRAC_GR_PARAM_NAME).getValue()).doubleValue();
    String faultModel = (String) parameterList.getParameter(USGS_CGS_1996_ERF_AdjustableParamsClass.FAULT_MODEL_NAME).getValue();
    double rupOffset = ((Double) parameterList.getParameter(USGS_CGS_1996_ERF_AdjustableParamsClass.RUP_OFFSET_PARAM_NAME).getValue()).doubleValue();

    double timeDuration =  timeSpan.getDuration();

    // Loop over lines of input file and create each source in the process
    ListIterator it = inputFaultFileLines.listIterator();
    while( it.hasNext() ){
      String stringRead = it.next().toString();
      //System.out.println("Fault source :"+stringRead);
      StringTokenizer st = new StringTokenizer(stringRead);

      //first word of first line is the fault class (A or B)
      faultClass = new String(st.nextToken());

      // 2nd word is the faulting style; set rake accordingly
      faultingStyle = new String(st.nextToken());

      //for Strike slip fault
      if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_SS))
        rake =0;

      //for reverse fault
      if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_R))
        rake =90;

      //for normal fault
      if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_N))
        rake =-90;

      //reading the fault name
      faultName = new String(st.nextToken());



      // get the 2nd line from the file
      st = new StringTokenizer(it.next().toString());

      // 1st word is magnitude
      mag=Double.parseDouble(st.nextToken());

      // 2nd word is charRate
      charRate=Double.parseDouble(st.nextToken());


      // get the third line from the file
      st=new StringTokenizer(it.next().toString());

      // 1st word is dip
      dip=Double.parseDouble(st.nextToken());
      // 2nd word is down dip width
      downDipWidth=Double.parseDouble(st.nextToken());
      // 3rd word is the depth to top of fault
      depthToTop=Double.parseDouble(st.nextToken());

      // Calculate upper and lower seismogenic depths
      upperSeismoDepth = depthToTop;
      lowerSeismoDepth = depthToTop + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));

      // get the 4th line from the file that gives the number of points on the fault trace
      int numOfDataLines = Integer.parseInt(it.next().toString().trim());

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
      }

      // reverse data ordering if dip negative, make positive and reverse trace order
      if( dip < 0 ) {
        faultTrace.reverse();
        dip *= -1;
      }

      if(faultModel.equals(USGS_CGS_1996_ERF_AdjustableParamsClass.FAULT_MODEL_FRANKEL)) {
        factory = new FrankelGriddedFaultFactory( faultTrace, dip, upperSeismoDepth,
            lowerSeismoDepth, GRID_SPACING);
      }
      else {
        factory = new StirlingGriddedFaultFactory( faultTrace, dip, upperSeismoDepth,
            lowerSeismoDepth, GRID_SPACING);
      }
      GriddedSurfaceAPI surface = factory.getGriddedSurface();

      // Now make the source(s)
      if(faultClass.equalsIgnoreCase(FAULT_CLASS_B) && mag>6.5){
        // divide the rate according the faction assigned to GR dist
        double rate = (1.0-fracGR)*charRate;
        double moRate = fracGR*charRate*MomentMagCalc.getMoment(mag);

        // make the GR source
        if(moRate>0.0) {
          Frankel96_GR_EqkSource frankel96_GR_src = new Frankel96_GR_EqkSource(rake,B_VALUE,MAG_LOWER,
              mag,moRate,DELTA_MAG,rupOffset,(EvenlyGriddedSurface)surface, faultName);
          frankel96_GR_src.setTimeSpan(timeDuration);
          FrankelB_GR_EqkSources.add(frankel96_GR_src);
        }
        // now make the Char source
        if(rate>0.0) {
          Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,rate,
              (EvenlyGriddedSurface)surface, faultName);
          frankel96_Char_src.setTimeSpan(timeDuration);
          FrankelB_CharEqkSources.add(frankel96_Char_src);
        }
      }
      else if (faultClass.equalsIgnoreCase(FAULT_CLASS_B)) {    // if class B and mag<=6.5, it's all characteristic
        Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,charRate,
            (EvenlyGriddedSurface)surface, faultName);
        frankel96_Char_src.setTimeSpan(timeDuration);
        FrankelB_CharEqkSources.add(frankel96_Char_src);

      }
      else if (faultClass.equalsIgnoreCase(FAULT_CLASS_A)) {   // class A fault
        Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,charRate,
            (EvenlyGriddedSurface)surface, faultName);
        frankel96_Char_src.setTimeSpan(timeDuration);
        FrankelA_CharEqkSources.add(frankel96_Char_src);
      }
      else {
        throw new FaultException(" Error - Bad fault Class :"+faultClass);
      }

    }  // bottom of loop over input-file lines

  }



}