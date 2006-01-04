package org.opensha.sha.calc;


import java.util.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.text.DecimalFormat;

import org.opensha.data.function.*;
import org.opensha.data.Site;
import org.opensha.sha.imr.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.param.DistanceRupParameter;


import org.opensha.sha.calc.sort.DisaggregationSourceRuptureInfo;
import org.opensha.sha.calc.sort.DisaggregationSourceRuptureComparator;
import java.io.FileWriter;
import java.io.*;
import org.opensha.data.Location;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.calc.RelativeLocation;
import java.net.URL;
import org.opensha.data.XYZ_DataSetAPI;
import java.net.URLConnection;


/**
 * <p>Title: DisaggregationCalculator </p>
 * <p>Description: This class disaggregates a hazard curve based on the
 * input parameters imr, site and eqkRupforecast.  See Bazzurro and Cornell
 * (1999, Bull. Seism. Soc. Am., 89, pp. 501-520) for a complete discussion
 * of disaggregation.  The Dbar computed here is for rupture distance.  This
 * assumes all sources in the ERF are Poissonian</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Oct 28, 2002
 * @version 1.0
 */

public class DisaggregationCalculator extends UnicastRemoteObject
 implements DisaggregationCalculatorAPI{

  protected final static String C = "DisaggregationCalculator";
  protected final static boolean D = false;


  // maximum permitted distance between fault and site to consider source in hazard analysis for that site
  protected double MAX_DISTANCE = HazardCurveCalculator.MAX_DISTANCE_DEFAULT;


  // disaggregation stuff - MIN and MAX are centers of first and last bins
  private double MIN_MAG = 5.0;
  private double MAX_MAG = 9.0;
  private double deltaMag = 0.5;
  private int NUM_MAG = (int)((MAX_MAG - MIN_MAG)/deltaMag +1);

  private double deltaDist = 10;
  private double MIN_DIST = 5;
  private double MAX_DIST = 105;
  private int NUM_DIST = (int)((MAX_DIST - MIN_DIST)/deltaDist +1);

  private int NUM_E = 8;
  private double[][][] pdf3D;
  private double maxContrEpsilonForDisaggrPlot;


  private int iMag, iDist, iEpsilon;
  private double mag, dist, epsilon;
  private boolean withinBounds;

  private double Mbar, Dbar, Ebar;
  private double M_mode3D, D_mode3D; //E_mode3D;

  //gets the Epsilon Range
  private String epsilonRangeString;

  private double totalRate, outOfBoundsRate;

  private int currRuptures = -1;
  private int totRuptures=0;

  private boolean calcSourceDissaggList = false;

  //stores the source Disagg info
  private String sourceDisaggInfo;

  //Disaggregation Plot Img Name
  public static final String DISAGGREGATION_PLOT_IMG = "DisaggregationPlot.jpg";
  //Address to the disaggregation plot img
  private String disaggregationPlotImgWebAddr;

  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException{
    MAX_DISTANCE = distance;
  }


  String[] epsilonColors = {
      "-G215/38/3",
      "-G252/94/62",
      "-G252/180/158",
      "-G254/220/210",
      "-G217/217/255",
      "-G151/151/255",
      "-G0/0/255",
      "-G0/0/170"};


  /**
   * creates the DisaggregationCalculator object
   *
   * @throws java.rmi.RemoteException
   * @throws IOException
   */
  public DisaggregationCalculator()throws java.rmi.RemoteException {}

  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  //public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException{
    //MAX_DISTANCE = distance;
  //}


  /**
   * this function performs the disaggregation
   *
   * @param iml: the intensity measure level to disaggregate
   * @param site: site parameter
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public void disaggregate(double iml, Site site,
        AttenuationRelationshipAPI imr, EqkRupForecast eqkRupForecast) throws java.rmi.RemoteException{

    double rate, mean, stdDev, condProb;

    pdf3D = new double[NUM_DIST][NUM_MAG][NUM_E];

    DistanceRupParameter distRup = new DistanceRupParameter();

    String S = C + ": disaggregate(): ";

    if( D ) System.out.println(S + "STARTING DISAGGREGATION");

    if( D ) System.out.println(S + "iml = " + iml);

//    if( D )System.out.println(S + "deltaMag = " + deltaMag + "; deltaDist = " + deltaDist + "; deltaE = " + deltaE);

    ArrayList disaggSourceList = new ArrayList();

    //resetting the Parameter change Listeners on the AttenuationRelationship
    //parameters. This allows the Server version of our application to listen to the
    //parameter changes.
    ((AttenuationRelationship)imr).resetParameterEventListeners();

    // set the maximum distance in the attenuation relationship
     // (Note- other types of IMRs may not have this method so we should really check type here)
     imr.setUserMaxDistance(MAX_DISTANCE);

    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();

    HashMap sourceDissaggMap = new HashMap();

    // compute the total number of ruptures for updating the progress bar
    totRuptures = 0;
    for(int i=0;i<numSources;++i)
      totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

    // init the current rupture number (also for progress bar)
    currRuptures = 0;

    try {
      // set the site in IMR
      imr.setSite(site);
     }catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }

    // initialize
    Ebar = 0;
    Mbar = 0;
    Dbar = 0;
    totalRate = 0;
    outOfBoundsRate= 0;

    // initialize the PDF
    for(int i=0; i<NUM_DIST; i++)
      for(int j=0; j<NUM_MAG; j++)
        for(int k=0; k<NUM_E; k++)
          pdf3D[i][j][k] = 0;


    for(int i=0;i < numSources ;i++) {

      double sourceRate = 0;
      // get source and get its distance from the site
      ProbEqkSource source = eqkRupForecast.getSource(i);

      String sourceName = source.getName();
      int numRuptures = eqkRupForecast.getNumRuptures(i);

      // check the distance of the source
      double distance = source.getMinDistance(site);
      if(distance > MAX_DISTANCE) {
       currRuptures+=numRuptures;
        continue;
      }

      if(calcSourceDissaggList)
        sourceDissaggMap.put(sourceName,new ArrayList());

      // loop over ruptures
      for(int n=0; n < numRuptures ; n++,++currRuptures) {

          // get the rupture
          ProbEqkRupture rupture = source.getRupture(n);

          double qkProb = rupture.getProbability();

          // set the rupture in the imr
          try {
            imr.setEqkRupture(rupture);
          } catch (Exception ex) {
            System.out.println("Parameter change warning caught");
          }

          // get the cond prob
          condProb = imr.getExceedProbability(iml);
          // should the following throw an exception?
          if(condProb == 0 && D)
              System.out.println(S + "Exceedance probability is zero! (thus the NaNs below)");

          // get the mean, stdDev, epsilon, dist, and mag
          mean = imr.getMean();
          stdDev = imr.getStdDev();
          epsilon = (iml-mean)/stdDev;
          distRup.setValue(rupture,site);
          dist = ((Double) distRup.getValue()).doubleValue();
          mag = rupture.getMag();

          // get the equiv. Poisson rate over the time interval (not annualized)
          rate = - condProb * Math.log(1-qkProb);

          /*
          if( D ) System.out.println("disaggregation():" + " rupture #" + currRuptures +
                                         " qkProb=" + qkProb +
                                         " condProb=" + condProb +
                                         " mean=" + mean +
                                         " stdDev=" + stdDev +
                                         " epsilon=" + epsilon +
                                         " dist=" + dist +
                                         " rate=" + rate);
*/
          // set the 3D array indices & check that all are in bounds
          setIndices();
          if (withinBounds)
              pdf3D[iDist][iMag][iEpsilon] += rate;
          else {
            if (D) System.out.println("disaggregation(): Some bin is out of range");
            outOfBoundsRate += rate;
          }

//          if( D ) System.out.println("disaggregation(): bins: " + iMag + "; " + iDist + "; " + iEpsilon);

          totalRate += rate;

          Mbar += rate * mag;
          Dbar += rate * dist;
          Ebar += rate * epsilon;
          sourceRate +=rate;

          // create and add rupture info to source list
          if(calcSourceDissaggList){
            double eventRate = -Math.log(1 - qkProb);  // this event rate is not annualized!
            DisaggregationSourceRuptureInfo rupInfo = new
                DisaggregationSourceRuptureInfo(null, eventRate, (float) rate, n);
            ( (ArrayList) sourceDissaggMap.get(sourceName)).add(rupInfo);
          }
      }
      if(calcSourceDissaggList){
        // sort the ruptures in this source according to contribution
        ArrayList sourceRupList = (ArrayList) sourceDissaggMap.get(sourceName);
        Collections.sort(sourceRupList,
                         new DisaggregationSourceRuptureComparator());
        // create the total rate info for this source
        DisaggregationSourceRuptureInfo disaggInfo = new
            DisaggregationSourceRuptureInfo(sourceName, (float) sourceRate, i);
        disaggSourceList.add(disaggInfo);
      }
    }
    // sort the disaggSourceList according to contribution
    if(calcSourceDissaggList){
      Collections.sort(disaggSourceList, new DisaggregationSourceRuptureComparator());
      sourceDisaggInfo =
          "Source#\tTotExceedRate\t% Contribution\tSourceName\n";
      int size = disaggSourceList.size();
      size = 100;  // overide to only give the top 100 sources (otherwise can be to big and cause crash)
      for (int i = 0; i < size; ++i) {
        DisaggregationSourceRuptureInfo disaggInfo = (
            DisaggregationSourceRuptureInfo)
            disaggSourceList.get(i);
        sourceDisaggInfo += disaggInfo.getId() +
            "\t" + (float) disaggInfo.getRate() + "\t" +
            (float) (disaggInfo.getRate()/totalRate * 100) +
            "\t" + disaggInfo.getName() + "\n";
      }
    }
    /*try {
      FileWriter fw = new FileWriter("Source_Rupture_OpenSHA.txt");

      String sourceRupDisaggregationInfo =
          "#Source-Id  Source-Rate   Rupture-Id    Rupture-Exceed-Rate Rupture-Rate  Source-Name\n";
      fw.write(sourceRupDisaggregationInfo);
      size = disaggSourceList.size();
      for (int i = 0; i < size; ++i) {
        DisaggregationSourceRuptureInfo disaggInfo = (
            DisaggregationSourceRuptureInfo)
            disaggSourceList.get(i);
        String sourceName = disaggInfo.getName();
        String sourceInfo = disaggInfo.getId() + "\t" +
            (float)disaggInfo.getRate();
        ArrayList rupList = (ArrayList) sourceDissaggMap.get(sourceName);
        int rupListSize = rupList.size();
        for (int j = 0; j < rupListSize; ++j) {
          DisaggregationSourceRuptureInfo disaggRupInfo =
              (DisaggregationSourceRuptureInfo) rupList.get(j);
          sourceRupDisaggregationInfo = sourceInfo + "\t"+disaggRupInfo.getId() +
              "\t" +(float)disaggRupInfo.getRate() +"\t"+(float)disaggRupInfo.getEventRate()
              +"\t"+sourceName+"\n";
          fw.write(sourceRupDisaggregationInfo);
        }
      }

      fw.close();
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
    }*/

    Mbar /= totalRate;
    Dbar /= totalRate;
    Ebar /= totalRate;
    if( D ) System.out.println(S + "Mbar = " + Mbar);
    if( D ) System.out.println(S + "Dbar = " + Dbar);
    if( D ) System.out.println(S + "Ebar = " + Ebar);

    maxContrEpsilonForDisaggrPlot = -1;
    int modeMagBin=-1, modeDistBin=-1, modeEpsilonBin=-1;
    double maxContrBinRate =-1;
    for(int i=0; i<NUM_DIST; i++) {
      for(int j=0; j<NUM_MAG; j++) {
        double contrEpsilonSum =0;
        for(int k=0; k<NUM_E; k++) {
          pdf3D[i][j][k] = pdf3D[i][j][k]/totalRate*100;  // convert to
          //summing over all the contributing Epsilon for a given dist and Mag.
          contrEpsilonSum +=pdf3D[i][j][k];
          if(pdf3D[i][j][k] > maxContrBinRate) {
              maxContrBinRate = pdf3D[i][j][k] ;
              modeDistBin = i;
              modeMagBin = j;
              modeEpsilonBin = k;
          }
        }
        if(contrEpsilonSum > maxContrEpsilonForDisaggrPlot)
          maxContrEpsilonForDisaggrPlot = contrEpsilonSum;
      }
    }
    M_mode3D = mag(modeMagBin);
    D_mode3D = dist(modeDistBin);
    epsilonRangeString = this.getEpsilonRange(modeEpsilonBin);
    //E_mode3D = eps(modeEpsilonBin);

    if( D ) System.out.println(S + "MagMode = "  + M_mode3D + "; binNum = " + modeMagBin);
    if( D ) System.out.println(S + "DistMode = " + D_mode3D + "; binNum = " + modeDistBin);
    if( D ) System.out.println(S + "EpsMode = "  + epsilonRangeString + "; binNum = " + modeEpsilonBin);
    //if( D ) System.out.println(S + "EpsMode = "  + E_mode3D + "; binNum = " + modeEpsilonBin);

    // check the final pdf
    double testSum = 0;
    for(int i=0; i<NUM_DIST; i++)
      for(int j=0; j<NUM_MAG; j++)
        for(int k=0; k<NUM_E; k++) {
          testSum += pdf3D[i][j][k];
          System.out.println(dist(i)+"\t"+mag(j)+"\t"+k+"\t"+pdf3D[i][j][k]);
        }
    System.out.println("TestSum = "+testSum+"; should = "+ (100*(totalRate-outOfBoundsRate)/totalRate));


  }


  /**
   *
   * Returns the disaggregated source list with following info ( in each line)
   * 1)Source Id as given by OpenSHA
   * 2)Name of the Source
   * 3)Rate Contributed by that source
   * 4)Percentage Contribution of the source in Hazard at the site.
   *
   * @return String
   * @throws RemoteException
   */
  public String getDisaggregationSourceInfo() throws java.rmi.RemoteException{
    if(calcSourceDissaggList)
      return sourceDisaggInfo;
    return "";
  }



  /**
   * Sets the boolean to generate the Source Disaggregation list.
   * @param isSourceList boolean
   * @throws RemoteException
   */
  public void generateSourceDisaggregationList(boolean isSourceList)throws
      java.rmi.RemoteException {
    calcSourceDissaggList = isSourceList;
  }

  /**
   * Setting up the Mag Range
   * @param minMag double - this is the center of the first bin
   * @param numMags int
   * @param deltaMag double
   */
  public void setMagRange(double minMag, int numMags, double deltaMag) throws
      java.rmi.RemoteException {
    MIN_MAG = minMag;
    NUM_MAG = numMags;
    this.deltaMag = deltaMag;
    MAX_MAG = MIN_MAG + (NUM_MAG-1) * deltaMag;
  }

  /**
   * Setting up the Distance Range
   * @param minDist double - this is the center of the first bin
   * @param numDist int
   * @param deltaDist double
   */
  public void setDistanceRange(double minDist, int numDist, double deltaDist) throws
      java.rmi.RemoteException {
    MIN_DIST = minDist;
    NUM_DIST = numDist;
    this.deltaDist = deltaDist;
    MAX_DIST = MIN_DIST + (NUM_DIST-1) * deltaDist;
  }

  /**
   * gets the number of current rupture being processed
   * @return
   */
  public int getCurrRuptures() throws java.rmi.RemoteException{
    return this.currRuptures;
  }

  /**
   * gets the total number of ruptures
   * @return
   */
  public int getTotRuptures() throws java.rmi.RemoteException{
    return this.totRuptures;
  }

  /**
   * Checks to see if disaggregation calculation for the selected site
   * have been completed.
   * @return
   */
  public boolean done() throws java.rmi.RemoteException{
    return (currRuptures==totRuptures);
  }

  /**
   *
   * @returns resultant disaggregation in a String format.
   * @throws java.rmi.RemoteException
   */
  public String getResultsString(boolean disaggUsingIML, double value) throws java.rmi.RemoteException{


    float mm_l = (float) (M_mode3D-deltaMag/2.0);
    float mm_u = (float) (M_mode3D+deltaMag/2.0);
    float dm_l = (float) (D_mode3D-deltaDist/2.0);
    float dm_u = (float) (D_mode3D+deltaDist/2.0);
    //float em_l = (float) (E_mode3D-deltaE/2.0);
    //float em_u = (float) (E_mode3D+deltaE/2.0);
    String results;
    if(disaggUsingIML)
      results = "Disaggregation Results for IML@"+value;
    else
      results = "Disaggregation Results for Prob@"+value;
    results += "\n" +
              "\n  Mbar = " + (float) Mbar +
              "\n  Dbar = " + (float) Dbar +
              "\n  Ebar = " + (float) Ebar + "\n" +
              "\n  " + mm_l+" ² Mmode < " + mm_u +
              "\n  " + dm_l+" ² Dmode < " + dm_u;
    /*if( E_mode3D == Double.NEGATIVE_INFINITY || E_mode3D == Double.POSITIVE_INFINITY)
      results += "\n  Emode = " + E_mode3D;
    else
      results += "\n  " + em_l+" ² Emode < " + em_u;*/
    results += "\n"+epsilonRangeString;

    if(totalRate == 0.0)
      results += "\n\nNote:\n" +
                 "The above NaN values result from the chosen IML\n" +
                 "(or that interpolated from the chosen probability)\n" +
                 "never being exceeded.";

/*
        results = "Disaggregation Result:\n\n\tMbar = " + Mbar + "\n\tDbar = " +
              Dbar + "\n\tEbar = " + Ebar + "\n\n\tMmode = " + M_mode3D +
              "\n\tDmode = " + D_mode3D + "\n\tEmode = " + E_mode3D;
*/

    return results;

  }


  private void setIndices() {
    withinBounds= true;
    iMag     =  Math.round((float)((mag-MIN_MAG)/deltaMag) );
    iDist    =  Math.round((float) ((dist-MIN_DIST)/deltaDist));

    if (epsilon <= -2)
      iEpsilon = 0;
    else if (epsilon > -2 && epsilon <= -1)
      iEpsilon = 1;
    else if (epsilon > -1 && epsilon <= -0.5)
      iEpsilon = 2;
    else if (epsilon > -0.5 && epsilon <= 0)
      iEpsilon = 3;
    else if (epsilon > 0 && epsilon <= 0.5)
      iEpsilon = 4;
    else if (epsilon > 0.5 && epsilon <= 1.0)
      iEpsilon = 5;
    else if (epsilon > 1.0 && epsilon <= 2.0)
      iEpsilon = 6;
    else if (epsilon > 2.0)
      iEpsilon = 7;

    if( iMag < 0 || iMag >= NUM_MAG) withinBounds= false;
    if( iDist < 0 || iDist >= NUM_DIST) withinBounds = false;

  }


  /**
   * Gets the Epsilon range String based on the index of the epsilon
   * @param iEpsilon int
   * @return String
   */
  private String getEpsilonRange(int iEpsilon){

    switch (iEpsilon){
      case 0:
        return "Emode <= -2";
      case 1:
        return "-2 < Emode <= -1";
      case 2:
        return "-1 < Emode <= -0.5";
      case 3:
        return "-0.5 < Emode <= 0.0";
      case 4:
        return "0.0 < Emode <= 0.5";
      case 5:
        return "0.5 < Emode <= 1.0";
      case 6:
        return "1.0 < Emode <= 2.0";
      case 7:
        return "2.0 < Emode ";
      default:
        return "Incorrect Index";
    }
  }




  /**
   * Gets the plot image for the Disaggregation
   * @param metadata String
   * @return String
   */
  public String getDisaggregationPlotUsingServlet(String metadata) throws java.
      rmi.RemoteException {
    ArrayList gmtScriptLines = createGMTScriptForDisaggregationPlot();
    disaggregationPlotImgWebAddr = openServletConnection(gmtScriptLines, metadata);
    return disaggregationPlotImgWebAddr;
  }


  /**
   * Creates the GMT_Script lines
   */
  private ArrayList createGMTScriptForDisaggregationPlot(){

    int numTicksToDrawForZAxis = 5;
    DecimalFormat format = new DecimalFormat("0.0");
    // compute z-axis tick spacing & max z value
    double gdZGridVal = Double.parseDouble(format.format(maxContrEpsilonForDisaggrPlot/(numTicksToDrawForZAxis )));
    double maxZVal = Double.parseDouble(format.format(gdZGridVal * (numTicksToDrawForZAxis +1)));
    ArrayList gmtScriptLines = new ArrayList();

    float min_dist = (float) (MIN_DIST - deltaDist/2);
    float max_dist = (float) (MAX_DIST + deltaDist/2);
    float min_mag = (float) (MIN_MAG - deltaMag/2);
    float max_mag = (float) (MAX_MAG + deltaMag/2);

    try{
    String region = "-R"+min_dist+"/"+max_dist+
        "/"+min_mag+"/"+max_mag+"/"+0+"/"+maxZVal;
    String imagePixelSize = "-JX4.5i/4i";
    String imageAngle = "-E150/30";
    String boundarySize = "-W0.5p";  // pen width for drawing boxes
    String verticalScaling = "-JZ2.5i";
    String gmt_const_comands = "gmtset PAGE_COLOR 180/180/180 \n gmtset X_ORIGIN 1.0i \n"+
        "gmtset Y_ORIGIN 1.0i\n";
    String img_ps_file = "DisaggregationPlot.ps";

    String axisBoundaryTicksBounds = "-B"+deltaDist+":Distance:"+"/"+deltaMag+":Magnitude:"+
        "/"+gdZGridVal+":%Contribution:"+"wSnEZ";
    String gridLines = "cat << END > temp_segments";
    ArrayList segLineList = new ArrayList();
    segLineList.add(gridLines);
    //creating the grid lines on Z axis.
    for (double k = gdZGridVal; k <= maxZVal; k += gdZGridVal) {
      segLineList.add(">\n" +min_dist+"  "+ min_mag+" "+k);
      segLineList.add(min_dist+"  "+max_mag+"  "+k);
      segLineList.add(">\n" +min_dist+"  "+ max_mag+"  "+k);
      segLineList.add(+max_dist+"   "+max_mag+"  "+k);
    }
    segLineList.add(">\n" + min_dist +"   "+ max_mag+"  " + 0);
    segLineList.add( min_dist + "  "+max_mag + "  " + maxZVal);
    segLineList.add(">\n"+ max_dist + "  "+ max_mag + " "  + 0);
    segLineList.add(  + max_dist + "  " +max_mag+ " "+ maxZVal);
    segLineList.add("END\n");

    //creating the GMT_Script for the plot
    gmtScriptLines.add(gmt_const_comands);
    gmtScriptLines.addAll(segLineList);
    gmtScriptLines.add("psxyz temp_segments -P "+
                       region+" -M  " +imagePixelSize +"  "+verticalScaling+" -K -G0/0/0 "+
                       imageAngle + "  "+boundarySize+"  "+axisBoundaryTicksBounds +" >  "+img_ps_file);

    float contribution, base, top;
    float d = (float) MIN_DIST;
    for (int i = 0; i < NUM_DIST; ++i) {
      double m = (float) MAX_MAG;
      for (int j = NUM_MAG - 1; j >= 0; --j) {
        base = 0;
        top = 0;
        for (int k = 0; k < NUM_E; ++k) {
          contribution = (float) pdf3D[i][j][k];
          top = base + contribution;
          if (contribution > 0.0) {
            gmtScriptLines.add("echo " + "\"" + d + " " + m + " " + top +
                               "\"" +
                               " > junk_data ; psxyz junk_data "
                               + "-P " + region + " " + imagePixelSize + " " +
                               verticalScaling + " -So0.3ib" + base +
                               " -K -O " + epsilonColors[k] + "  " +
                               imageAngle +
                               "  " + boundarySize + " >> " + img_ps_file);
            base = top;
          }

        }
        m -= deltaMag;
      }
      d += deltaDist;
    }

    gmtScriptLines.add("echo " + "\"3.85 3.6 18 0.0 1 11 Label\"" +
                                 " | pstext " + " -P " + region + " " + imagePixelSize + " " +
                                 " -O " + " >> " + img_ps_file);

    gmtScriptLines.add("cat "+img_ps_file+ " |"+ "gs -sDEVICE=jpeg -sOutputFile=temp.jpg"+" -");
    gmtScriptLines.add("convert -crop 0x0 temp.jpg "+DISAGGREGATION_PLOT_IMG);
    gmtScriptLines.add("rm junk_data temp.jpg temp_segments");
    }catch(Exception e){
      e.printStackTrace();
    }

    return gmtScriptLines;
  }


  /**
   * sets up the connection with the servlet on the server (gravity.usc.edu)
   */
  private String openServletConnection(ArrayList gmtFileLines,
                                       String metadata) throws RuntimeException{

    String webaddr=null;
    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL gmtPlotServlet = new
                             URL("http://gravity.usc.edu/OpenSHA/servlet/DisaggregationPlotServlet");


      URLConnection servletConnection = gmtPlotServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the ArrayList of the gmt Script Lines
      outputToServlet.writeObject(gmtFileLines);
      //sending the contents of the Metadata file to the server.
      outputToServlet.writeObject(metadata);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      Object messageFromServlet = inputToServlet.readObject();
      inputToServlet.close();
      if(messageFromServlet instanceof String){
        webaddr = (String) messageFromServlet;
        if (D) System.out.println("Receiving the Input from the Servlet:" +
                                  webaddr);
      }
      else
        throw (RuntimeException)messageFromServlet;
    }catch(RuntimeException e){
      e.printStackTrace();
     throw new RuntimeException(e.getMessage());
    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return webaddr;
  }


  private double mag(int iMag) {
      if( iMag >=0 && iMag <= NUM_MAG)
          return  MIN_MAG + iMag*deltaMag;
      else return Double.NaN;
  }

  private double dist(int iDist) {
    if (iDist >= 0 && iDist <= NUM_DIST)
      return MIN_DIST + iDist * deltaDist;
    else return Double.NaN;
  }


}
