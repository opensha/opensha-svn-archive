package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.scec.param.*;
import org.scec.calc.MomentMagCalc;
import org.scec.util.*;
import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.*;
import org.scec.sha.fault.GriddedFaultFactory;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.magdist.*;
import org.scec.exceptions.FaultException;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.TimeSpan;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel02.*;
import org.scec.sha.earthquake.rupForecastImpl.*;


/**
 * <p>Title: Frankel02_EqkRupForecast</p>
 * <p>Description:Frankel 2002 Earthquake Rupture Forecast. This class
 * creates the USGS/CGS 2002 California ERF.
 * This does not yet include any C zones.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Edward Field
 * @Date : Feb, 2004
 * @version 1.0
 */

public class Frankel02_AdjustableEqkRupForecast extends EqkRupForecast
    implements ParameterChangeListener{

  //for Debug purposes
  private static String  C = new String("Frankel02_EqkRupForecast");
  private boolean D = false;

  // name of this ERF
  public final static String NAME = new String("USGS/CGS 2002 Adj. Cal. ERF");


  Vector allSourceNames;

  private double GRID_SPACING = 1.0;

  private String CHAR_MAG_FREQ_DIST = "1";
  private String GR_MAG_FREQ_DIST = "2";
  private String FAULTING_STYLE_SS = "1";
  private String FAULTING_STYLE_R = "2";
  private String FAULTING_STYLE_N = "3";

  /**
   * used for error checking
   */
  protected final static FaultException ERR = new FaultException(
           C + ": loadFaultTraces(): Missing metadata from trace, file bad format.");

  /*
   * Static variables for input files
   */

  private final static String IN_FILE_PATH = "org/scec/sha/earthquake/rupForecastImpl/Frankel02/InputFiles/";

// The input files for hazFXv3 and hazFXv3a (and wts):

  // Input files for hazgridXca (gridded seismicity)
  private final static String AREA_GRID_FILE_1 = "CAmapC.inv3";
  private final static double AREA_GRID_FILE_WT_1 = 0.667;
  private final static String AREA_GRID_FILE_2 = "CAmapG.inv3";
  private final static double AREA_GRID_FILE_WT_2 = 0.333;
  private final static String AREA_GRID_FILE_3 = "EXTmapC.inv3";
  private final static double AREA_GRID_FILE_WT_3 = 0.5;
  private final static String AREA_GRID_FILE_4 = "EXTmapGW.inv3";
  private final static double AREA_GRID_FILE_WT_4 = 0.5;
  private final static String AREA_GRID_FILE_5 = "WUSmapC.inv3";
  private final static double AREA_GRID_FILE_WT_5 = 0.5;
  private final static String AREA_GRID_FILE_6 = "WUSmapG.inv3";
  private final static double AREA_GRID_FILE_WT_6 = 0.5;
  private final static String AREA_GRID_FILE_7 = "brawmap.inv3";
  private final static double AREA_GRID_FILE_WT_7 = 1.0;
  private final static String AREA_GRID_FILE_8 = "cadeepAB.inv3";
  private final static double AREA_GRID_FILE_WT_8 = 0.5;
  private final static String AREA_GRID_FILE_9 = "cadeepY.inv3";
  private final static double AREA_GRID_FILE_WT_9 = 0.5;
  private final static String AREA_GRID_FILE_10 = "creepmap.inv3";
  private final static double AREA_GRID_FILE_WT_10 = 1.0;
  private final static String AREA_GRID_FILE_11 = "shear1.inv1";
  private final static double AREA_GRID_FILE_WT_11 = 1.0;
  private final static String AREA_GRID_FILE_12 = "shear2.inv1";
  private final static double AREA_GRID_FILE_WT_12 = 1.0;
  private final static String AREA_GRID_FILE_13 = "shear3.inv1";
  private final static double AREA_GRID_FILE_WT_13 = 1.0;
  private final static String AREA_GRID_FILE_14 = "shear4.inv1";
  private final static double AREA_GRID_FILE_WT_14 = 1.0;

  // input files for hazSUBXv3 (subduction-zone events)
  private final static String SUBD_ZONE_FILE_1 = "cascadia.bot.83.in";
  private final static double SUBD_ZONE_FILE_WT_1 = 0.1;
  private final static String SUBD_ZONE_FILE_2 = "cascadia.bot.9N.in";
  private final static double SUBD_ZONE_FILE_WT_2 = 0.1;
  private final static String SUBD_ZONE_FILE_3 = "cascadia.mid.83.in";
  private final static double SUBD_ZONE_FILE_WT_3 = 0.1;
  private final static String SUBD_ZONE_FILE_4 = "cascadia.mid.9N.in";
  private final static double SUBD_ZONE_FILE_WT_4 = 0.1;
  private final static String SUBD_ZONE_FILE_5 = "cascadia.old.83.in";
  private final static double SUBD_ZONE_FILE_WT_5 = 0.25;
  private final static String SUBD_ZONE_FILE_6 = "cascadia.old.9N.in";
  private final static double SUBD_ZONE_FILE_WT_6 = 0.25;
  private final static String SUBD_ZONE_FILE_7 = "cascadia.top.83.in";
  private final static double SUBD_ZONE_FILE_WT_7 = 0.05;
  private final static String SUBD_ZONE_FILE_8 = "cascadia.top.9N.in";
  private final static double SUBD_ZONE_FILE_WT_8 = 0.05;


  Vector faultFiles, faultFileWts, areaGridFiles, areaGridFileWts;

  /**
   * Vectors for holding the various sources, separated by type
   */
  private Vector FrankelA_CharEqkSources;
  private Vector FrankelB_CharEqkSources;
  private Vector FrankelB_GR_EqkSources;
  private Vector FrankelBackgrSeisSources;
  private Vector allSources;

  // This is an array holding each line of the input file
  private ArrayList inputBackSeisFileLines = null;

  // fault-model parameter stuff
  public final static String FAULT_MODEL_NAME = new String ("Fault Model");
  public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
  public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");
  // make the fault-model parameter
  Vector faultModelNamesStrings = new Vector();
  StringParameter faultModelParam;

  // fault-model parameter stuff
  public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
  public final static String BACK_SEIS_INCLUDE = new String ("Include");
  public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
  public final static String BACK_SEIS_ONLY = new String ("Only Background");
  // make the fault-model parameter
  Vector backSeisOptionsStrings = new Vector();
  StringParameter backSeisParam;

  // For rupture offset lenth along fault parameter
  public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
  private Double DEFAULT_RUP_OFFSET_VAL= new Double(10);
  private final static String RUP_OFFSET_PARAM_UNITS = "km";
  private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
  private final static double RUP_OFFSET_PARAM_MIN = 1;
  private final static double RUP_OFFSET_PARAM_MAX = 100;
  DoubleParameter rupOffset_Param;

  /**
   *
   * No argument constructor
   */
  public Frankel02_AdjustableEqkRupForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // create and add adj params to list
    initAdjParams();


    // add the change listener to parameters so that forecast can be updated
    // whenever any paramater changes
    faultModelParam.addParameterChangeListener(this);
    rupOffset_Param.addParameterChangeListener(this);
    backSeisParam.addParameterChangeListener(this);

    allSourceNames = new Vector();

    this.makeAllFaultSources();

/*
    try{ inputBackSeisFileLines = FileUtils.loadFile( INPUT_BACK_SEIS_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    // Exit if no data found in list
    if( inputFaultFileLines == null) throw new
           FaultException(C + "No data loaded from "+INPUT_FAULT_FILE_NAME+". File may be empty or doesn't exist.");

    // Exit if no data found in list
    if( inputBackSeisFileLines == null) throw new
           FaultException(C + "No data loaded from "+INPUT_BACK_SEIS_FILE_NAME+". File may be empty or doesn't exist.");
*/
  }

// make the adjustable parameters & the list
  private void initAdjParams() {

    faultModelNamesStrings.add(FAULT_MODEL_FRANKEL);
    faultModelNamesStrings.add(FAULT_MODEL_STIRLING);
    faultModelParam = new StringParameter(FAULT_MODEL_NAME, faultModelNamesStrings,
        (String)faultModelNamesStrings.get(0));

    backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);
    backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
    backSeisOptionsStrings.add(BACK_SEIS_ONLY);
    backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,BACK_SEIS_EXCLUDE);

    rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
        RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
    rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);


// add adjustable parameters to the list
    adjustableParams.addParameter(faultModelParam);
    adjustableParams.addParameter(rupOffset_Param);
    adjustableParams.addParameter(backSeisParam);


  }



  /**
   * This makes the sources for the input files of hazFXv3 and hazFXv3a (and wts):
   */
  private void makeAllFaultSources() {
    makeFaultSources("ca-a-other-fixed-char", 1.0,null,0);
    makeFaultSources("ca-a-other-norm-char", 1.0,null,0);
    makeFaultSources("ca-amod1-char", 0.5,null,0);
    makeFaultSources("ca-amod2-char", 0.5,null,0);
    makeFaultSources("ca-b-fullwt-norm-ell-65", 0.5, "ca-b-fullwt-norm-hank-65", 0.5);
    makeFaultSources("ca-b-fullwt-norm-ell-char", 0.333, "ca-b-fullwt-norm-hank-char", 0.333);
    makeFaultSources("ca-b-fullwt-norm-ell-gr", 0.167, "ca-b-fullwt-norm-hank-gr", 0.167);
    makeFaultSources("ca-b-fullwt-ss-ell-65", 0.5, "ca-b-fullwt-ss-hank-65", 0.5);
    makeFaultSources("ca-b-fullwt-ss-ell-char", 0.333, "ca-b-fullwt-ss-hank-char", 0.333);
    makeFaultSources("ca-b-fullwt-ss-ell-gr", 0.167, "ca-b-fullwt-ss-hank-gr", 0.167);
    makeFaultSources("ca-bflt-25weight-ell-char", 0.083, "ca-bflt-25weight-hank-char", 0.083);
    makeFaultSources("ca-bflt-25weight-ell-gr", 0.042, "ca-bflt-25weight-hank-gr", 0.042);
    makeFaultSources("ca-bflt-50weight-ell-65", 0.25, "ca-bflt-50weight-hank-65", 0.25);
    makeFaultSources("ca-bflt-50weight-ell-char", 0.167, "ca-bflt-50weight-hank-char", 0.167);
    makeFaultSources("ca-bflt-50weight-ell-gr", 0.083, "ca-bflt-50weight-hank-gr", 0.083);
    makeFaultSources("ca-bflt-fix-norm-ell-65", 0.5, "ca-bflt-fix-norm-hank-65", 0.5);
    makeFaultSources("ca-bflt-fix-norm-ell-char", 0.333, "ca-bflt-fix-norm-hank-char", 0.333);
    makeFaultSources("ca-bflt-fix-norm-ell-gr", 0.167, "ca-bflt-fix-norm-hank-gr", 0.167);
    makeFaultSources("ca-bflt-fix-ss-ell-65", 0.5, "ca-bflt-fix-ss-hank-65", 0.5);
    makeFaultSources("ca-bflt-fix-ss-ell-char", 0.333, "ca-bflt-fix-ss-hank-char", 0.333);
    makeFaultSources("ca-bflt-fix-ss-ell-gr", 0.167, "ca-bflt-fix-ss-hank-gr", 0.167);
    makeFaultSources("ca-wg99-dist-char", 1.0,null,0);
    makeFaultSources("ca-wg99-dist-float", 1.0,null,0);
    makeFaultSources("creepflt", 1.0,null,0);

// not sure if the rest are needed
/* */
    makeFaultSources("ext-norm-65", 1.0,null,0);
    makeFaultSources("ext-norm-char", 0.5,null,0);
    makeFaultSources("ext-norm-gr", 0.5,null,0);
    makeFaultSources("wa_or-65", 1.0,null,0);
    makeFaultSources("wa_or-char", 0.5,null,0);
    makeFaultSources("wa_or-gr", 0.5,null,0);


  }

  /**
   * This reads the given filename(s) and makes the sources (equivalent to
   * Frankel's hazFXv3 and hazFXv3a Fortran programs).  If the second fileName
   * is not null, then its assumed that everything is identical except the mag-freq-dist
   * parameter lines.  This allows us to have fewer sources by treating empistemic
   * uncertainties as aleatory.  The two files generally differ only by whether Hanks
   * or Ellsworth's Mag-Area relationship was used.
   *
   * @throws FaultException
   */
  private  void makeFaultSources(String fileName1, double wt1, String fileName2, double wt2) throws FaultException{

    // Debuggin stuff
    String S = C + ": makeFaultSoureces(): ";

    // read the lines of the 1st input file into a list
    ArrayList inputFaultFileLines1=null;
    try{ inputFaultFileLines1 = FileUtils.loadFile(IN_FILE_PATH + fileName1 ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}
    if( D ) System.out.println("fileName1 = " + IN_FILE_PATH + fileName1);

    // read second file's lines if necessary
    ArrayList inputFaultFileLines2=null;
    if(fileName2 != null) {
      try{ inputFaultFileLines2 = FileUtils.loadFile(IN_FILE_PATH + fileName2 ); }
      catch( FileNotFoundException e){ System.out.println(e.toString()); }
      catch( IOException e){ System.out.println(e.toString());}
      if( D ) System.out.println("fileName2 = " + IN_FILE_PATH + fileName2);
    }

    FrankelA_CharEqkSources = new Vector();
    FrankelB_CharEqkSources = new Vector();
    FrankelB_GR_EqkSources = new Vector();

    String  magFreqDistType = "", faultingStyle, sourceName="";
    double dlen, dmove;                 // fault discretization and floater offset, respectively
    int numBranches, numMags, numMags2;                    // num branches for mag epistemic uncertainty
    Vector branchDmags = new Vector();  // delta mags for epistemic uncertainty
    Vector branchWts = new Vector();    // wts for epistemic uncertainty
    double aleStdDev, aleWidth;         // aleatory mag uncertainties

    GriddedFaultFactory factory;
    SummedMagFreqDist totalMagFreqDist;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon, rake=Double.NaN;
    double mag=0, mag2=0;  // used for magChar and magUpper (latter for the GR distributions)
    double aVal=0, bVal=0, magLower, deltaMag, moRate;
    double aVal2=0, bVal2=0, magLower2=0,deltaMag2=0, moRate2=0;

    double charRate=0,charRate2, dip=0, downDipWidth=0, depthToTop=0;
    double minMag, maxMag, minMag2=0, maxMag2=0;

    double test;
    boolean itest; // true means

    // get adjustable parameters values
    String faultModel = (String) faultModelParam.getValue();
    double rupOffset = ((Double) rupOffset_Param.getValue()).doubleValue();

    // get an iterator for the input file lines
    ListIterator it = inputFaultFileLines1.listIterator();

    // get first line
    StringTokenizer st = new StringTokenizer(it.next().toString());
    // first line has the fault discretization & floater offset
    // (these are 1.0 & 1.0 in all the files)
    dlen = Double.parseDouble(st.nextToken());
    dmove = Double.parseDouble(st.nextToken());

    // get the 2nd line from the file
    st = new StringTokenizer(it.next().toString());
    numBranches = Integer.parseInt(st.nextToken());

    // get the dMags from the 3rd line
    st = new StringTokenizer(it.next().toString());
    for(int n=0;n<numBranches;n++) branchDmags.add(new Double(st.nextToken()));

    // get branch wts from the 4rd line
    st = new StringTokenizer(it.next().toString());
    for(int n=0;n<numBranches;n++) branchWts.add(new Double(st.nextToken()));

    // get aleatory stddev and truncation width from 5th line
    st = new StringTokenizer(it.next().toString());
    aleStdDev = Double.parseDouble(st.nextToken());
    aleWidth = Double.parseDouble(st.nextToken());

    // Loop over lines of input file and create each source in the process
    while( it.hasNext() ){

      st = new StringTokenizer(it.next().toString());

      //first element is the magFreqDist type
      magFreqDistType = new String(st.nextToken());

      // 2nd element is the faulting style; set rake accordingly
      faultingStyle = new String(st.nextToken());

      if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_SS))
        rake =0;
      else if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_R))
        rake =90;
      else if (faultingStyle.equalsIgnoreCase(FAULTING_STYLE_N))
        rake =-90;
      else
        throw new RuntimeException("Unrecognized faulting style");

      // the rest of the line is the fault name
      sourceName = "";
      while(st.hasMoreElements()) sourceName += st.nextElement()+" ";

      // get source name from second file if necessary
      String sourceName2="";
      if(fileName2 != null) {
        // get the same line from the second file
        st = new StringTokenizer((String) inputFaultFileLines2.get(it.nextIndex()-1));
        st.nextToken(); // skip first two
        st.nextToken();
        while(st.hasMoreElements()) sourceName2 += st.nextElement()+" ";
//        System.out.println("source1: "+sourceName+"\nsource2: "+sourceName2);
      }

      // get the next line from the file
      st = new StringTokenizer(it.next().toString());

      // if it's a characteristic distribution:
      if(magFreqDistType.equals(CHAR_MAG_FREQ_DIST)) {

          mag=Double.parseDouble(st.nextToken());
          charRate=Double.parseDouble(st.nextToken());
          moRate = charRate*MomentMagCalc.getMoment(mag);
          minMag = mag + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
          maxMag = mag + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue() + aleWidth*0.05;

          // if the file is "ca-wg99-dist-char" add the magnitude to the name to make source names unique
          if(fileName1.equals("ca-wg99-dist-char")) sourceName += " M="+mag;

          // add "Char" to the source name
          sourceName += " Char";

          // get the same from the second file if necessary
          if(fileName2 != null) {
            // get the same line from the second file
            st = new StringTokenizer((String) inputFaultFileLines2.get(it.nextIndex()-1));
            mag2=Double.parseDouble(st.nextToken());
            charRate2=Double.parseDouble(st.nextToken());
            moRate2 = charRate2*MomentMagCalc.getMoment(mag2);
            minMag2 = mag2 + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
            maxMag2 = mag2 + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue() + aleWidth*0.05;
          }

          // make the Char magFreqDist (depending on whether uncertainties should be considered)
          double mLow, mHigh;
          if(minMag < 5.8 || aleStdDev == 0.0) {   // the no-uncertainty case:
            if(fileName2 == null){
              SingleMagFreqDist tempDist = new SingleMagFreqDist(mag,1,0.1,mag,moRate*wt1);
              totalMagFreqDist = new SummedMagFreqDist(mag,1,0.1, false, false);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
            }
            else {
              if(mag > mag2) {
                mLow = mag2;
                mHigh = mag;
              }
              else {
                mLow =mag;
                mHigh = mag2;
              }
              int numMag = Math.round((float) ((mHigh-mLow)/0.05+1));
              totalMagFreqDist = new SummedMagFreqDist(mLow,mHigh,numMag, false, false);
              SingleMagFreqDist tempDist = new SingleMagFreqDist(mLow,mHigh,numMag);
              tempDist.setMagAndMomentRate(mag,moRate*wt1);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
              tempDist.setMagAndMomentRate(mag2,moRate2*wt2);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
            }
          }
          else { // Apply both aleatory and epistemic uncertainties
            //find the lower and upper magnitudes
            if(mag < mag2) {
              mLow = minMag;
              mHigh = maxMag2;
            }
            else {
              mLow = minMag2;
              mHigh = maxMag;
            }
            int numMag = Math.round((float)((maxMag-minMag)/0.05 + 1));
            totalMagFreqDist = new SummedMagFreqDist(minMag,maxMag,numMag, false, false);
            // loop over epistemic uncertianties
            GaussianMagFreqDist tempDist = new GaussianMagFreqDist(minMag,maxMag,numMag);
            double magEp, wtEp;
            for(int i=0;i<branchDmags.size();i++) {
              magEp = mag + ((Double)branchDmags.get(i)).doubleValue();
              wtEp = ((Double)branchWts.get(i)).doubleValue();
              tempDist.setAllButCumRate(magEp,aleStdDev,moRate*wtEp*wt1,aleWidth*0.05/aleStdDev,2);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
            }
            // now add those from the second file if necessary
            if(fileName2 != null){
              for(int i=0;i<branchDmags.size();i++) {
                magEp = mag2 + ((Double)branchDmags.get(i)).doubleValue();
                wtEp = ((Double)branchWts.get(i)).doubleValue();
                tempDist.setAllButCumRate(magEp,aleStdDev,moRate2*wtEp*wt2,aleWidth*0.05/aleStdDev,2);
                totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
              }
            }
          }
      }
      else { // It's a GR distribution

          aVal=Double.parseDouble(st.nextToken());
          bVal=Double.parseDouble(st.nextToken());
          magLower=Double.parseDouble(st.nextToken());
          mag=Double.parseDouble(st.nextToken());
          deltaMag=Double.parseDouble(st.nextToken());
          // mover lower and upper mags to be bin centered
          if(mag != magLower){
            magLower += deltaMag/2.0;
            mag -= deltaMag/2.0;
          }
          numMags = Math.round( (float)((mag-magLower)/deltaMag + 1.0) );
          //calculate moment rate (the exact same way Frankel does it)
          moRate = getMomentRate(magLower, numMags,deltaMag,aVal,bVal);

          // get the same from the second file if necessary
          if(fileName2 != null) {
            // get the same line from the second file
            st = new StringTokenizer((String) inputFaultFileLines2.get(it.nextIndex()-1));
            aVal2=Double.parseDouble(st.nextToken());
            bVal2=Double.parseDouble(st.nextToken());
            magLower2=Double.parseDouble(st.nextToken());
            mag2=Double.parseDouble(st.nextToken());
            deltaMag2=Double.parseDouble(st.nextToken());
            // mover lower and upper mags to be bin centered
            if(mag2 != magLower2){
              magLower2 += deltaMag2/2.0;
              mag2 -= deltaMag2/2.0;
            }
            numMags2 = Math.round( (float)((mag-magLower)/deltaMag + 1.0) );
            //calculate moment rate (the exact same way Frankel does it)
            moRate2 = getMomentRate(magLower2, numMags2,deltaMag2,aVal2,bVal2);
          }

          // Set name suffix
          if( mag == magLower )
            sourceName += " fl-Char";
          else {
            sourceName += " GR";
          }

          if(numMags == 1) {
            test = mag + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
            if(test >= 5.8 && aleStdDev != 0.0) {
              // Gaussian dist - both aleatory and epistemic uncertainty

              // check that the other file won't fall out of this case
              if (fileName2 != null) {
                double test2 = mag2 + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
                if (test2 < 5.8) {
                  System.out.println("PROBLEM: test="+test+"; test2="+test2+" ---"+sourceName);
                }
              }
            }
            else {
              // single mag dist; no uncertainty
            }
          }
          else {
            test = mag + ((Double)branchDmags.get(0)).doubleValue();
            if(test >= 6.5 && aleStdDev != 0.0) {
              // GR dist with epistemic uncertainty

              // check that the other file won't fall out of this case
              if (fileName2 != null) {
                double test2 = mag2 + ((Double)branchDmags.get(0)).doubleValue();
                if (test2 < 6.5) {
                  System.out.println("PROBLEM: test="+test+"; test2="+test2+" ---"+sourceName);
                }
              }

            }
            else {
              // GR dist with no epistemic uncertainty
            }
          }
/*
if (fileName2 != null) {
  test = mag + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
  double test2 = mag2 + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
  if(numMags == 1) {
    if(test < 6.5 || test2 < 6.5)
      System.out.println("test1="+test+"; test2="+test2+ "  --- "+sourceName);
  }
}
*/

/*
          // set itest
          itest = false;

          test = mag + ((Double)branchDmags.get(0)).doubleValue();
          if( test < 6.5 && numMags > 1 )  itest = true;

          test = mag + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
          if (numMags == 1 && test < 5.8) itest = true;

          // over ride if aleatory uncertainty is zero (not sure why they do this)
          if (aleStdDev == 0.0) itest = true;



          // make mag-freq-dist (w/ both aleatory and epistemic)
*/
      }

      if(D) System.out.println("    "+sourceName);

      // KEEP THIS?
      allSourceNames.add(sourceName);

      // NOW GET THE FAULT SURFACE
      // next line has dip, ...
      st=new StringTokenizer(it.next().toString());
      dip=Double.parseDouble(st.nextToken());
      downDipWidth=Double.parseDouble(st.nextToken());
      depthToTop=Double.parseDouble(st.nextToken());
      // Calculate upper and lower seismogenic depths
      upperSeismoDepth = depthToTop;
      lowerSeismoDepth = depthToTop + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));

      // next line gives the number of points on the fault trace
      int numOfDataLines = Integer.parseInt(it.next().toString().trim());

      FaultTrace faultTrace= new FaultTrace(sourceName);

      //based on the num of the data lines reading the lat and long points for the faults
      for(int i=0;i<numOfDataLines;++i) {
        if( !it.hasNext() ) throw ERR;
        st =new StringTokenizer(it.next().toString().trim());
        lat = new Double(st.nextToken()).doubleValue();
        lon = new Double(st.nextToken()).doubleValue();
        Location loc = new Location(lat, lon, upperSeismoDepth);
        faultTrace.addLocation( (Location)loc.clone());
      }

      // reverse data ordering if dip negative (and make the dip positive)
      if( dip < 0 ) {
        faultTrace.reverse();
        dip *= -1;
      }

//      if( D ) System.out.println(C+":faultTrace::"+faultTrace.toString());

      if(faultModel.equals(FAULT_MODEL_FRANKEL)) {
        factory = new FrankelGriddedFaultFactory( faultTrace, dip, upperSeismoDepth,
                                                   lowerSeismoDepth, GRID_SPACING);
      }
      else {
        factory = new StirlingGriddedFaultFactory( faultTrace, dip, upperSeismoDepth,
                                                   lowerSeismoDepth, GRID_SPACING);
      }

      GriddedSurfaceAPI surface = factory.getGriddedSurface();

/*
      // Now make the source(s)
      if(faultClass.equalsIgnoreCase(FAULT_CLASS_B) && mag>6.5){
            // divide the rate according the faction assigned to GR dist
//            double rate = (1.0-fracGR)*charRate;
//            double moRate = fracGR*charRate*MomentMagCalc.getMoment(mag);
              double rate = charRate;
              double moRate = charRate*MomentMagCalc.getMoment(mag);
            // make the GR source
            if(moRate>0.0) {
              Frankel02_GR_EqkSource frankel96_GR_src = new Frankel02_GR_EqkSource(rake,B_VALUE,MAG_LOWER,
                                                   mag,moRate,DELTA_MAG,rupOffset,(EvenlyGriddedSurface)surface, faultName);
              frankel96_GR_src.setTimeSpan(timeDuration);
              FrankelB_GR_EqkSources.add(frankel96_GR_src);
            }
            // now make the Char source
            if(rate>0.0) {
              Frankel02_CharEqkSource frankel96_Char_src = new  Frankel02_CharEqkSource(rake,mag,rate,
                                                      (EvenlyGriddedSurface)surface, faultName);
              frankel96_Char_src.setTimeSpan(timeDuration);
              FrankelB_CharEqkSources.add(frankel96_Char_src);
            }
      }
      else if (faultClass.equalsIgnoreCase(FAULT_CLASS_B)) {    // if class B and mag<=6.5, it's all characteristic
            Frankel02_CharEqkSource frankel96_Char_src = new  Frankel02_CharEqkSource(rake,mag,charRate,
                                                      (EvenlyGriddedSurface)surface, faultName);
            frankel96_Char_src.setTimeSpan(timeDuration);
            FrankelB_CharEqkSources.add(frankel96_Char_src);

      }
      else if (faultClass.equalsIgnoreCase(FAULT_CLASS_A)) {   // class A fault
            Frankel02_CharEqkSource frankel96_Char_src = new  Frankel02_CharEqkSource(rake,mag,charRate,
                                                      (EvenlyGriddedSurface)surface, faultName);
            frankel96_Char_src.setTimeSpan(timeDuration);
            FrankelA_CharEqkSources.add(frankel96_Char_src);
      }
      else {
            throw new FaultException(C+" Error - Bad fault Class :"+faultClass);
      }
*/
    }  // bottom of loop over input-file lines

  }









  /**
  * Read the Background Seismicity file and make the sources
  *
  */
  private  void makeBackSeisSources() {

    // Debug
    String S = C + ": makeBackSeisSources(): ";
    if( D ) System.out.println(S + "Starting");

    FrankelBackgrSeisSources = new Vector();

    double lat, lon, rate, rateAtMag5;

    double aveRake=0.0;
    double aveDip=90;
    double tempMoRate = 1.0;
    double bValue = 0.9;
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

    while( it.hasNext() ){

      // get next line
      st = new StringTokenizer(it.next().toString());

      lon =  Double.parseDouble(st.nextToken());
      lat =  Double.parseDouble(st.nextToken());
      rate = Double.parseDouble(st.nextToken());

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
      }
    }
  }


    /**
     * Returns the  ith earthquake source
     *
     * @param iSource : index of the source needed
    */
    public ProbEqkSource getSource(int iSource) {

      // apply this here
      double timeDuration =  timeSpan.getDuration();

      return (ProbEqkSource) allSources.get(iSource);
    }

    /**
     * Get the number of earthquake sources
     *
     * @return integer
     */
    public int getNumSources(){
      return allSources.size();
    }


     /**
      * Get the list of all earthquake sources.
      *
      * @return Vector of Prob Earthquake sources
      */
     public Vector  getSourceList(){

       return allSources;
     }


    /**
     * Return the name for this class
     *
     * @return : return the name for this class
     */
   public String getName(){
     return NAME;
   }


   /**
    * update the forecast
    **/

   public void updateForecast() {

     // make sure something has changed
     if(parameterChangeFlag) {

       // get value of background seismicity paramter
       String backSeis = (String) backSeisParam.getValue();

       allSources = new Vector();

       if (backSeis.equalsIgnoreCase(BACK_SEIS_INCLUDE)) {
//         makeFaultSources();
         makeBackSeisSources();
         // now create the allSources list:
         allSources.addAll(FrankelA_CharEqkSources);
         allSources.addAll(FrankelB_CharEqkSources);
         allSources.addAll(FrankelB_GR_EqkSources);
         allSources.addAll(FrankelBackgrSeisSources);
       }
       else if (backSeis.equalsIgnoreCase(BACK_SEIS_EXCLUDE)) {
 //        makeFaultSources();
         // now create the allSources list:
         allSources.addAll(FrankelA_CharEqkSources);
         allSources.addAll(FrankelB_CharEqkSources);
         allSources.addAll(FrankelB_GR_EqkSources);
       }
       else {// only background sources
        makeBackSeisSources();
        // now create the allSources list:
        allSources.addAll(FrankelBackgrSeisSources);
       }

       parameterChangeFlag = false;
/*
String tempName;
for(int i=0;i<allSources.size();i++) {
  tempName = ((ProbEqkSource) allSources.get(i)).getName();
  System.out.println("source "+ i +"is "+tempName);
}
*/
     }

   }

   /**
    *  This is the main function of this interface. Any time a control
    *  paramater or independent paramater is changed by the user in a GUI this
    *  function is called, and a paramater change event is passed in.
    *
    *  This sets the flag to indicate that the sources need to be updated
    *
    * @param  event
    */
   public void parameterChange( ParameterChangeEvent event ) {
     parameterChangeFlag=true;
   }


   /**
    * this computes the moment for the GR distribution exactly the way frankel does is
    */
   private double getMomentRate(double magLower, int numMag, double deltaMag, double aVal, double bVal) {
     double mo = 0;
     double mag;
     for(int i = 0; i <numMag; i++) {
       mag = magLower + i*deltaMag;
       mo += Math.pow(10,aVal-bVal*mag+1.5*mag+9.05);
     }
     return mo;
   }


   // this is temporary for testing purposes
   public static void main(String[] args) {

     Frankel02_AdjustableEqkRupForecast frankCast = new Frankel02_AdjustableEqkRupForecast();
/*
     frankCast.updateForecast();
     System.out.println("num sources="+frankCast.getNumSources());
//     for(int i=0; i<frankCast.getNumSources(); i++)
//       System.out.println(frankCast.getSource(i).getName());

     double totRate=0, totProb=1, prob;
     int i,j, totRup;
     int totSrc= frankCast.getNumSources();
     for(i=0; i<totSrc; i++){
       ProbEqkSource src = (ProbEqkSource) frankCast.getSource(i);
       totRup=src.getNumRuptures();
       if(i==0) System.out.println("numRup for src0 ="+totRup);
       for(j=0; j<totRup; j++) {
         prob = src.getRupture(j).getProbability();
         totProb *= (1-prob);
         totRate += -1*Math.log(1-prob)/50;
         if(j==0 && i==0)
           System.out.println("mag, prob for src0, rup 0="+src.getRupture(j).getMag()+"; "+prob);
         if(j==0 && i==1)
           System.out.println("mag, prob for src1, rup 0="+src.getRupture(j).getMag()+"; "+prob);
         if(j==0 && i==2)
           System.out.println("mag, prob for src2, rup 0="+src.getRupture(j).getMag()+"; "+prob);

       }
     }
       System.out.println("main(): totRate="+totRate+"; totProb="+totProb);
*/
  }

}
