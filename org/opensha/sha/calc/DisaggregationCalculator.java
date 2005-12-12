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
 * of disaggregation.  The Dbar computed here is for rupture distance.</p>
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
  protected double MAX_DISTANCE = 250;

  // disaggregation stuff - MIN and MAX are centers of first and last bins
  private double MIN_MAG = 5.0;
  private double MAX_MAG = 9.0;
  private int NUM_MAG = 41;
  private double deltaMag = (MAX_MAG-MIN_MAG)/(NUM_MAG-1);

  private double MIN_DIST = 5;
  private double MAX_DIST = 295;
  private int NUM_DIST = 30;
  private double deltaDist = (MAX_DIST-MIN_DIST)/(NUM_DIST-1);

  private double MIN_E = -5.0;
  private double MAX_E = 5;
  private int NUM_E = 53;
  private double deltaE = (MAX_E-MIN_E)/(NUM_E-3);
  // Note: the last two bins here are for -infinity & infinity (if stdDev = 0)

  private double[][][] pmf = new double[NUM_MAG][NUM_DIST][NUM_E];

  private double MIN_MAG_plot = 5.0;
  private double MAX_MAG_plot = 9.0;
  private double deltaMag_plot = 0.5;
  private int NUM_MAG_plot = (int)((MAX_MAG_plot-MIN_MAG_plot)/deltaMag_plot +1);

  private double deltaDist_plot = 10;
  private double MIN_DIST_plot = 0;
  private double MAX_DIST_plot = 110;
  private int NUM_DIST_plot = (int)((MAX_DIST_plot - MIN_DIST_plot)/deltaDist_plot +1);

  private int NUM_E_plot = 8;
  private double[][][] disaggr_plt = new double[NUM_MAG_plot][NUM_DIST_plot][NUM_E_plot];
  //indices for plot
  private int iMag_plt,iDist_plt, iEpsilon_plt;
  private boolean withinBoundsForPlt;

  private double maxContrEpsilonForDisaggrPlot;


  private int iMag, iDist, iEpsilon;
  private double mag, dist, epsilon;
  private boolean withinBounds;

  private double Mbar, Dbar, Ebar;
  private double M_mode1D, D_mode1D, E_mode1D;
  private double M_mode3D, D_mode3D, E_mode3D;

  private double iml, prob, totalRate;

  private int currRuptures = -1;
  private int totRuptures=0;

  //gets the source Disagg info
  private String sourceDisaggInfo;

  //Disaggregation Plot Img Name
  public static final String DISAGGREGATION_PLOT_IMG = "DisaggregationPlot.jpg";
  //Address to the disaggregation plot img
  private String disaggregationPlotImgWebAddr;

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
  public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException{
    MAX_DISTANCE = distance;
  }


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

    DistanceRupParameter distRup = new DistanceRupParameter();

    String S = C + ": disaggregate(): ";

    if( D ) System.out.println(S + "STARTING DISAGGREGATION");

    if( D ) System.out.println(S + "iml = " + iml);

    if( D )System.out.println(S + "deltaMag = " + deltaMag + "; deltaDist = " + deltaDist + "; deltaE = " + deltaE);

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
    HashMap map = new HashMap();


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

    for(int i=0; i<NUM_MAG; i++)
      for(int j=0; j<NUM_DIST; j++)
        for(int k=0; k<NUM_E; k++)
          pmf[i][j][k] = 0;

    for(int i=0; i<NUM_MAG_plot; i++)
      for(int j=0; j<NUM_DIST_plot; j++)
        for(int k=0; k<NUM_E_plot; k++)
          disaggr_plt[i][j][k] = 0;


    for(int i=0;i < numSources ;i++) {

      double sourceRate = 0;
      // get source and get its distance from the site
      ProbEqkSource source = eqkRupForecast.getSource(i);
      double distance = source.getMinDistance(site);
      String sourceName = source.getName();
      // for each source, get the number of ruptures
      int numRuptures = eqkRupForecast.getNumRuptures(i);

      // if source is greater than the MAX_DISTANCE, ignore the source
      if(distance > MAX_DISTANCE) {
       this.currRuptures+=numRuptures;
        continue;
      }

      map.put(sourceName,new ArrayList());

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

          // get the cond prob, mean, stdDev, and epsilon
          condProb = imr.getExceedProbability(iml);

          // should the following throw and exception?
          if(condProb == 0 && D)
              System.out.println(S + "Exceedance probability is zero! (thus the NaNs below)");


          mean = imr.getMean();
          stdDev = imr.getStdDev();
          epsilon = (iml-mean)/stdDev;

          distRup.setValue(rupture,site);
          dist = ((Double) distRup.getValue()).doubleValue();

          mag = rupture.getMag();

          // get the equiv. Poisson rate over the time interval
          rate = condProb * Math.log(1-qkProb);

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
          setIndicesForPlot();
          if (withinBounds)
              pmf[iMag][iDist][iEpsilon] += rate;
          else
              if( D ) System.out.println("disaggregation(): Some bin is out of range");

          if(withinBoundsForPlt)
            this.disaggr_plt[iMag_plt][iDist_plt][iEpsilon_plt] +=rate;

//          if( D ) System.out.println("disaggregation(): bins: " + iMag + "; " + iDist + "; " + iEpsilon);

          totalRate += rate;


          Mbar += rate * mag;
          Dbar += rate * dist;
          Ebar += rate * epsilon;
          sourceRate +=rate;
          DisaggregationSourceRuptureInfo rupInfo = new DisaggregationSourceRuptureInfo(null,(float)-rate,n);
          ((ArrayList)map.get(sourceName)).add(rupInfo);
      }
      ArrayList sourceRupList = (ArrayList)map.get(sourceName);
      Collections.sort(sourceRupList,new DisaggregationSourceRuptureComparator());
      DisaggregationSourceRuptureInfo disaggInfo = new DisaggregationSourceRuptureInfo(sourceName, (float)-sourceRate,i);
      disaggSourceList.add(disaggInfo);
    }

    Collections.sort(disaggSourceList,new DisaggregationSourceRuptureComparator());
    sourceDisaggInfo = "#Source-Id , Source-Name , Source-Rate , Total-Contribution-in-%\n";
    int size = disaggSourceList.size();
    for (int i = 0; i < size; ++i) {
      DisaggregationSourceRuptureInfo disaggInfo = (DisaggregationSourceRuptureInfo)
          disaggSourceList.get(i);
      sourceDisaggInfo +=disaggInfo.getId() + " ,  " + disaggInfo.getName() +
               "  , " +
               (float)disaggInfo.getRate() + " ,  " +
               (float)( -disaggInfo.getRate() / totalRate * 100) + "\n";
    }

    /*try {
      FileWriter fw = new FileWriter("Source_Rupture_OpenSHA.txt");

      String sourceRupDisaggregationInfo =
          "#Source-Id , Source Name, Source Rate  , Rupture-Id   ,  Rupture Rate\n";
      fw.write(sourceRupDisaggregationInfo);
      size = disaggSourceList.size();
      for (int i = 0; i < size; ++i) {
        DisaggregationSourceRuptureInfo disaggInfo = (
            DisaggregationSourceRuptureInfo)
            disaggSourceList.get(i);
        String sourceName = disaggInfo.getName();
        String sourceInfo = disaggInfo.getId() + " ,  " +
            sourceName +
            "  , " +
            (float)disaggInfo.getRate();
        ArrayList rupList = (ArrayList) map.get(sourceName);
        int rupListSize = rupList.size();
        for (int j = 0; j < rupListSize; ++j) {
          DisaggregationSourceRuptureInfo disaggRupInfo =
              (DisaggregationSourceRuptureInfo) rupList.get(j);
          sourceRupDisaggregationInfo = sourceInfo + " ,  "+disaggRupInfo.getId() +
              "  ,  " +
              (float)disaggRupInfo.getRate() + "\n";
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


    double maxRate = -1;
    int modeMagBin=-1, modeDistBin=-1, modeEpsilonBin=-1;
    for(int i=0; i<NUM_MAG; i++) {
      for(int j=0; j<NUM_DIST; j++) {
        for(int k=0; k<NUM_E; k++) {
          pmf[i][j][k] /= totalRate;
          if(pmf[i][j][k] > maxRate) {
              maxRate = pmf[i][j][k];
              modeMagBin = i;
              modeDistBin = j;
              modeEpsilonBin = k;
          }
        }
      }
    }
    maxContrEpsilonForDisaggrPlot =0;
    //creating the rate array for the diassgregation for ploting purposes
    for(int i=0;i<NUM_MAG_plot;++i)
      for(int j=0; j<NUM_DIST_plot; j++)
        for(int k=0; k<NUM_E_plot; k++) {
          disaggr_plt[i][j][k] = (float) (disaggr_plt[i][j][k] * 100.0 / totalRate);
          //if some of the Array elements are zero then there must be negative
          //sign in front of them, so removing the negative sign.
          if(disaggr_plt[i][j][k] == -0)
            disaggr_plt[i][j][k] = 0;
          if(disaggr_plt[i][j][k] > maxContrEpsilonForDisaggrPlot)
            maxContrEpsilonForDisaggrPlot = disaggr_plt[i][j][k];
        }

    M_mode3D = mag(modeMagBin);
    D_mode3D = dist(modeDistBin);
    E_mode3D = eps(modeEpsilonBin);

    if( D ) System.out.println(S + "MagMode = "  + M_mode3D + "; binNum = " + modeMagBin);
    if( D ) System.out.println(S + "DistMode = " + D_mode3D + "; binNum = " + modeDistBin);
    if( D ) System.out.println(S + "EpsMode = "  + E_mode3D + "; binNum = " + modeEpsilonBin);
    createGMTScriptForDisaggregationPlot();

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
    return sourceDisaggInfo;
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
  public String getResultsString() throws java.rmi.RemoteException{

    String results;

    float mm_l = (float) (M_mode3D-deltaMag/2.0);
    float mm_u = (float) (M_mode3D+deltaMag/2.0);
    float dm_l = (float) (D_mode3D-deltaDist/2.0);
    float dm_u = (float) (D_mode3D+deltaDist/2.0);
    float em_l = (float) (E_mode3D-deltaE/2.0);
    float em_u = (float) (E_mode3D+deltaE/2.0);

    results = "Disaggregation Results:\n" +
              "\n  Mbar = " + (float) Mbar +
              "\n  Dbar = " + (float) Dbar +
              "\n  Ebar = " + (float) Ebar + "\n" +
              "\n  " + mm_l+" ² Mmode < " + mm_u +
              "\n  " + dm_l+" ² Dmode < " + dm_u;
    if( E_mode3D == Double.NEGATIVE_INFINITY || E_mode3D == Double.POSITIVE_INFINITY)
      results += "\n  Emode = " + E_mode3D;
    else
      results += "\n  " + em_l+" ² Emode < " + em_u;

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
      withinBounds = true;
      iMag     =  Math.round( (float) ((mag-MIN_MAG)/deltaMag) );
      iDist    =  Math.round((float) ((dist-MIN_DIST)/deltaDist));
      if(epsilon == Double.POSITIVE_INFINITY)
          iEpsilon = NUM_E-1;
      else if (epsilon == Double.NEGATIVE_INFINITY)
          iEpsilon = NUM_E-2;
      else {
          iEpsilon = Math.round((float) ((epsilon-MIN_E)/deltaE));
          // check to make sure it didn't fall onto the last two bins here
          if(iEpsilon == NUM_E-1 || iEpsilon == NUM_E-2)
              iEpsilon = NUM_E + 1;  // make it fall outside
      }

      if( iMag < 0 || iMag >= NUM_MAG ) withinBounds = false;
      if( iDist < 0 || iDist >= NUM_DIST ) withinBounds = false;
      if( iEpsilon < 0 || iEpsilon >= NUM_E ) withinBounds = false;
  }


  private void setIndicesForPlot() {
    withinBoundsForPlt = true;
    iMag_plt     =  Math.round( (float) ((mag-MIN_MAG_plot)/deltaMag_plot) );
    iDist_plt    =  Math.round((float) ((dist-MIN_DIST_plot)/deltaDist_plot));
    if (epsilon >= -40 && epsilon <= -2)
      iEpsilon_plt = 0;
    else if (epsilon > -2 && epsilon <= -1)
      iEpsilon_plt = 1;
    else if (epsilon > -1 && epsilon <= -0.5)
      iEpsilon_plt = 2;
    else if (epsilon > -0.5 && epsilon <= 0)
      iEpsilon_plt = 3;
    else if (epsilon > 0 && epsilon <= 0.5)
      iEpsilon_plt = 4;
    else if (epsilon > 0.5 && epsilon <= 1.0)
      iEpsilon_plt = 5;
    else if (epsilon > 1.0 && epsilon <= 2.0)
      iEpsilon_plt = 6;
    else if (epsilon > 2.0 && epsilon <= 40.0)
      iEpsilon_plt = 7;

    if( iMag_plt < 0 || iMag_plt >= NUM_MAG_plot ) withinBoundsForPlt = false;
    if( iDist_plt < 0 || iDist_plt >= NUM_DIST_plot ) withinBoundsForPlt = false;
    if( iEpsilon_plt < 0 || iEpsilon_plt >= NUM_E_plot ) withinBoundsForPlt = false;
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
    double gdZGridVal = Double.parseDouble(format.format(maxContrEpsilonForDisaggrPlot/(numTicksToDrawForZAxis)));
    double maxZVal = Double.parseDouble(format.format(gdZGridVal * numTicksToDrawForZAxis));
    ArrayList gmtScriptLines = new ArrayList();
    try{
    String region = "-R"+MIN_MAG_plot+"/"+MAX_MAG_plot+"/"+MIN_DIST_plot+"/"+MAX_DIST_plot+
        "/"+0+"/"+maxZVal;
    String imagePixelSize = "-JX-4i/6i";
    String imageAngle = "-E60/30";
    String boundarySize = "-W0.5p";
    String verticalScaling = "-JZ2.5i";
    String gmt_const_comands = "gmtset PAGE_COLOR 180/180/180 \n gmtset X_ORIGIN 0.5i \n"+
        "gmtset Y_ORIGIN 0.5i\n";
    String img_ps_file = "DisaggregationPlot.ps";
    String img_jpg_file = DISAGGREGATION_PLOT_IMG;

    String axisBoundaryTicksBounds = "-B"+deltaMag_plot+"/"+deltaDist_plot+"/"+gdZGridVal+"wsNEZ";
    String tickLabelsLines = "cat << END > temp_segments";
    ArrayList segLineList = new ArrayList();
    segLineList.add(tickLabelsLines);
    //creating the grid lines on Z axis.
    for (double k = gdZGridVal; k <= maxZVal; k += gdZGridVal) {
      segLineList.add(">\n" + MIN_MAG_plot+" "+MIN_DIST_plot+"  "+k);
      segLineList.add(+ MAX_MAG_plot+" "+MIN_DIST_plot+"  "+k);
      segLineList.add(">\n" + MAX_MAG_plot+" "+MIN_DIST_plot+"  "+k);
      segLineList.add(+ MAX_MAG_plot+" "+MAX_DIST_plot+"  "+k);
    }
    segLineList.add(">\n " + MAX_MAG_plot + " " + MIN_DIST_plot + "  " + 0);
    segLineList.add( +MAX_MAG_plot + " " + MIN_DIST_plot + "  " + maxZVal);
    segLineList.add(">\n" + MAX_MAG_plot + " " + MAX_DIST_plot + "  " + 0);
    segLineList.add( +MAX_MAG_plot + " " + MAX_DIST_plot + "  " + maxZVal);
    segLineList.add("END\n");

    //creating the GMT_Script for the plot
    gmtScriptLines.add(gmt_const_comands);
    gmtScriptLines.addAll(segLineList);
    gmtScriptLines.add("psxyz temp_segments -P "+
                       region+" -M  " +imagePixelSize +"  "+verticalScaling+" -K -G0/0/0 "+
                       imageAngle + "  "+boundarySize+"  "+axisBoundaryTicksBounds +" >  "+img_ps_file);

    //creating the data array
    ArrayList dataArray = new ArrayList();
    float x = (float)(MIN_MAG_plot + (float)deltaMag_plot/2);
    for(int i= 0 ;i< NUM_MAG_plot ;++i){
      double y = (float)(MIN_DIST_plot + (float)this.deltaDist_plot/2);
      for (int j = 0; j < NUM_DIST_plot; ++j) {
        boolean rateZero = true;
        for (int k = 0; k < NUM_E_plot; ++k) {
          if (disaggr_plt[i][j][k] != 0)
            rateZero = false;
          if(disaggr_plt[i][j][k] ==0 && k!=0)
            dataArray.add(x+" "+y+" "+(float)disaggr_plt[i][j][k-1]);
          else
            dataArray.add(x+" "+y+" "+(float)disaggr_plt[i][j][k]);

      }
        //If the rate of all Epsilon is zero then put the no need to plot that
        if(rateZero){
          //as items in the arraylist are added to the end so removing it from
          //zero contribution from the end.
          for (int k = 0; k < NUM_E_plot; ++k)
            dataArray.remove(dataArray.size()-1);
        }
        y +=deltaDist_plot;
      }
      x += deltaMag_plot;
    }

    int size = dataArray.size();
    for(int i= size -1;i>=0;i -=NUM_E_plot){
      String data = (String)dataArray.get(i);

      gmtScriptLines.add("echo "+"\""+(String)dataArray.get(i)+"\""+" > junk_data ; psxyz junk_data "
                         +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib0"+
          " -K -O -G0/0/170 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);
      double barHt = getBarWidth(data);

      data = (String)dataArray.get(i-1);
      gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                         +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
          " -K -O -G0/0/255 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);

      barHt = getBarWidth(data);

      data = (String)dataArray.get(i-2);
      gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                         +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
          " -K -O  -G151/151/255 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);

      barHt = getBarWidth(data);

      data = (String)dataArray.get(i-3);
      gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                         +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
          " -K -O  -G217/217/255 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);

      barHt = getBarWidth(data);

      data = (String)dataArray.get(i-4);
            gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                               +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
                " -K -O  -G254/220/210 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);

      barHt = getBarWidth(data);

      data = (String)dataArray.get(i-5);
      gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                         +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
                         " -K -O  -G252/180/158 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);
      barHt = getBarWidth(data);

      data = (String)dataArray.get(i-6);
      gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                         +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
                         " -K -O  -G252/94/62 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);
      barHt = getBarWidth(data);

      data = (String)dataArray.get(i-7);
      if(i-7 == 0)//we don't have to add the "-O" option in the last line of GMT script.
        gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                           +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
                           " -O -G215/38/3 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);
      else
        gmtScriptLines.add("echo "+"\""+data+"\""+" > junk_data ; psxyz junk_data "
                           +"-P "+region+" "+imagePixelSize+" "+verticalScaling+ " -So0.3ib"+barHt+
                           " -K -O -G215/38/3 "+imageAngle+"  "+boundarySize+" >> "+img_ps_file);
    }

    gmtScriptLines.add("cat "+img_ps_file+ " |"+ "gs -sDEVICE=jpeg -sOutputFile="+img_jpg_file +" -");
    gmtScriptLines.add("rm junk_data temp_segments");
    }catch(Exception e){
      e.printStackTrace();
    }
    /*FileWriter fw = null;
    try {
      fw = new FileWriter("OpenSHA_disaggrePlot");
      int size = gmtScriptLines.size();
      for(int i=0;i<size;++i)
        fw.write(gmtScriptLines.get(i)+"\n");
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }*/
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


  private double getBarWidth(String data){
    StringTokenizer st = new StringTokenizer(data);
    st.nextToken();
    st.nextToken();
    return Double.parseDouble(st.nextToken().trim());
  }

  private double mag(int iMag) {
      if( iMag >=0 && iMag <= NUM_MAG)
          return  MIN_MAG + iMag*deltaMag;
      else return Double.NaN;
  }

  private double dist(int iDist) {
      if( iDist >=0 && iDist <= NUM_DIST)
          return  MIN_DIST + iDist*deltaDist;
      else return Double.NaN;
      }

  private double eps(int iEpsilon) {
      if(iEpsilon >= 0 && iEpsilon < NUM_E) {
          if(iEpsilon == NUM_E-1) return Double.POSITIVE_INFINITY;
          else if(iEpsilon == NUM_E-2) return Double.NEGATIVE_INFINITY;
          else return  MIN_E + iEpsilon*deltaE;
      }
      else return Double.NaN;
  }


}
