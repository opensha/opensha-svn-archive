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
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
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
  private double B_VALUE =0.9;
  private double MAG_LOWER = 6.5;
  private double DELTA_MAG = 0.1;

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
  private final static String FAULT_FILE_1 = "ca-a-other-fixed-char";
  private final static double FAULT_FILE_WT_1 = 1.0;
  private final static String FAULT_FILE_2 = "ca-a-other-norm-char";
  private final static double FAULT_FILE_WT_2 = 1.0;
  private final static String FAULT_FILE_3 = "ca-amod1-char";
  private final static double FAULT_FILE_WT_3 = 0.5;
  private final static String FAULT_FILE_4 = "ca-amod2-char";
  private final static double FAULT_FILE_WT_4 = 0.5;
  private final static String FAULT_FILE_5 = "ca-b-fullwt-norm-ell-65";
  private final static double FAULT_FILE_WT_5 = 0.5;
  private final static String FAULT_FILE_6 = "ca-b-fullwt-norm-ell-char";
  private final static double FAULT_FILE_WT_6 = 0.333;
  private final static String FAULT_FILE_7 = "ca-b-fullwt-norm-ell-gr";
  private final static double FAULT_FILE_WT_7 = 0.167;
  private final static String FAULT_FILE_8 = "ca-b-fullwt-norm-hank-65";
  private final static double FAULT_FILE_WT_8 = 0.5;
  private final static String FAULT_FILE_9 = "ca-b-fullwt-norm-hank-char";
  private final static double FAULT_FILE_WT_9 = 0.333;
  private final static String FAULT_FILE_10 = "ca-b-fullwt-norm-hank-gr";
  private final static double FAULT_FILE_WT_10 = 0.167;
  private final static String FAULT_FILE_11 = "ca-b-fullwt-ss-ell-65";
  private final static double FAULT_FILE_WT_11 = 0.5;
  private final static String FAULT_FILE_12 = "ca-b-fullwt-ss-ell-char";
  private final static double FAULT_FILE_WT_12 = 0.333;
  private final static String FAULT_FILE_13 = "ca-b-fullwt-ss-ell-gr";
  private final static double FAULT_FILE_WT_13 = 0.167;
  private final static String FAULT_FILE_14 = "ca-b-fullwt-ss-hank-65";
  private final static double FAULT_FILE_WT_14 = 0.5;
  private final static String FAULT_FILE_15 = "ca-b-fullwt-ss-hank-char";
  private final static double FAULT_FILE_WT_15 = 0.333;
  private final static String FAULT_FILE_16 = "ca-b-fullwt-ss-hank-gr";
  private final static double FAULT_FILE_WT_16 = 0.167;
  private final static String FAULT_FILE_17 = "ca-bflt-25weight-ell-char";
  private final static double FAULT_FILE_WT_17 = 0.083;
  private final static String FAULT_FILE_18 = "ca-bflt-25weight-ell-gr";
  private final static double FAULT_FILE_WT_18 = 0.042;
  private final static String FAULT_FILE_19 = "ca-bflt-25weight-hank-char";
  private final static double FAULT_FILE_WT_19 = 0.083;
  private final static String FAULT_FILE_20 = "ca-bflt-25weight-hank-gr";
  private final static double FAULT_FILE_WT_20 = 0.042;
  private final static String FAULT_FILE_21 = "ca-bflt-50weight-ell-65";
  private final static double FAULT_FILE_WT_21 = 0.25;
  private final static String FAULT_FILE_22 = "ca-bflt-50weight-ell-char";
  private final static double FAULT_FILE_WT_22 = 0.167;
  private final static String FAULT_FILE_23 = "ca-bflt-50weight-ell-gr";
  private final static double FAULT_FILE_WT_23 = 0.083;
  private final static String FAULT_FILE_24 = "ca-bflt-50weight-hank-65";
  private final static double FAULT_FILE_WT_24 = 0.25;
  private final static String FAULT_FILE_25 = "ca-bflt-50weight-hank-char";
  private final static double FAULT_FILE_WT_25 = 0.167;
  private final static String FAULT_FILE_26 = "ca-bflt-50weight-hank-gr";
  private final static double FAULT_FILE_WT_26 = 0.083;
  private final static String FAULT_FILE_27 = "ca-bflt-fix-norm-ell-65";
  private final static double FAULT_FILE_WT_27 = 0.5;
  private final static String FAULT_FILE_28 = "ca-bflt-fix-norm-ell-char";
  private final static double FAULT_FILE_WT_28 = 0.333;
  private final static String FAULT_FILE_29 = "ca-bflt-fix-norm-ell-gr";
  private final static double FAULT_FILE_WT_29 = 0.167;
  private final static String FAULT_FILE_30 = "ca-bflt-fix-norm-hank-65";
  private final static double FAULT_FILE_WT_30 = 0.5;
  private final static String FAULT_FILE_31 = "ca-bflt-fix-norm-hank-char";
  private final static double FAULT_FILE_WT_31 = 0.333;
  private final static String FAULT_FILE_32 = "ca-bflt-fix-norm-hank-gr";
  private final static double FAULT_FILE_WT_32 = 0.167;
  private final static String FAULT_FILE_33 = "ca-bflt-fix-ss-ell-65";
  private final static double FAULT_FILE_WT_33 = 0.5;
  private final static String FAULT_FILE_34 = "ca-bflt-fix-ss-ell-char";
  private final static double FAULT_FILE_WT_34 = 0.333;
  private final static String FAULT_FILE_35 = "ca-bflt-fix-ss-ell-gr";
  private final static double FAULT_FILE_WT_35 = 0.167;
  private final static String FAULT_FILE_36 = "ca-bflt-fix-ss-hank-65";
  private final static double FAULT_FILE_WT_36 = 0.5;
  private final static String FAULT_FILE_37 = "ca-bflt-fix-ss-hank-char";
  private final static double FAULT_FILE_WT_37 = 0.333;
  private final static String FAULT_FILE_38 = "ca-bflt-fix-ss-hank-gr";
  private final static double FAULT_FILE_WT_38 = 0.167;
  private final static String FAULT_FILE_39 = "ca-wg99-dist-char";
  private final static double FAULT_FILE_WT_39 = 1.0;
  private final static String FAULT_FILE_40 = "ca-wg99-dist-float";
  private final static double FAULT_FILE_WT_40 = 1.0;
  private final static String FAULT_FILE_41 = "creepflt";
  private final static double FAULT_FILE_WT_41 = 1.0;
  private final static String FAULT_FILE_42 = "ext-norm-65";
  private final static double FAULT_FILE_WT_42 = 1.0;
  private final static String FAULT_FILE_43 = "ext-norm-char";
  private final static double FAULT_FILE_WT_43 = 0.5;
  private final static String FAULT_FILE_44 = "ext-norm-gr";
  private final static double FAULT_FILE_WT_44 = 0.5;
  private final static String FAULT_FILE_45 = "wa_or-65";
  private final static double FAULT_FILE_WT_45 = 1.0;
  private final static String FAULT_FILE_46 = "wa_or-char";
  private final static double FAULT_FILE_WT_46 = 0.5;
  private final static String FAULT_FILE_47 = "wa_or-gr";
  private final static double FAULT_FILE_WT_47 = 0.5;

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

    mkFileListVectors();
    for(int i=0;i<faultFiles.size();i++) {
      makeFaultSources((String)faultFiles.get(i));
    }

    for(int i=0;i<allSourceNames.size();i++)
      for(int k=i+1;k<allSourceNames.size();k++)
        if(((String)allSourceNames.get(k)).equals((String)allSourceNames.get(i)))
           System.out.println("i="+i+"; k="+k+";  "+allSourceNames.get(i));

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

  private void mkFileListVectors() {
    faultFiles = new Vector();
    faultFileWts = new Vector();

    faultFiles.add(FAULT_FILE_1);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_2);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_3);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_4);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_5);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_6);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_7);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_8);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_9);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_10);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_11);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_12);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_13);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_14);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_15);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_16);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_17);
    faultFileWts.add(new Double(0.083));
    faultFiles.add(FAULT_FILE_18);
    faultFileWts.add(new Double(0.042));
    faultFiles.add(FAULT_FILE_19);
    faultFileWts.add(new Double(0.083));
    faultFiles.add(FAULT_FILE_20);
    faultFileWts.add(new Double(0.042));
    faultFiles.add(FAULT_FILE_21);
    faultFileWts.add(new Double(0.25));
    faultFiles.add(FAULT_FILE_22);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_23);
    faultFileWts.add(new Double(0.083));
    faultFiles.add(FAULT_FILE_24);
    faultFileWts.add(new Double(0.25));
    faultFiles.add(FAULT_FILE_25);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_26);
    faultFileWts.add(new Double(0.083));
    faultFiles.add(FAULT_FILE_27);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_28);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_29);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_30);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_31);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_32);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_33);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_34);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_35);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_36);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_37);
    faultFileWts.add(new Double(0.333));
    faultFiles.add(FAULT_FILE_38);
    faultFileWts.add(new Double(0.167));
    faultFiles.add(FAULT_FILE_39);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_40);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_41);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_42);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_43);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_44);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_45);
    faultFileWts.add(new Double(1.0));
    faultFiles.add(FAULT_FILE_46);
    faultFileWts.add(new Double(0.5));
    faultFiles.add(FAULT_FILE_47);
    faultFileWts.add(new Double(0.5));

  }



  /**
   * Read the Fault file and make the sources
   *
   * @throws FaultException
   */
  private  void makeFaultSources(String fileName) throws FaultException{

    // Debuggin stuff
    String S = C + ": makeFaultSoureces(): ";
//    if( D ) System.out.println(S + "Starting");

    // read the lines of the input file into a list
    ArrayList inputFaultFileLines=null;
    try{ inputFaultFileLines = FileUtils.loadFile(IN_FILE_PATH + fileName ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}
    if( D ) System.out.println("fileName = " + IN_FILE_PATH + fileName);
//    if( D ) System.out.println(S + "num input-file lines = " + inputFaultFileLines.size());

    FrankelA_CharEqkSources = new Vector();
    FrankelB_CharEqkSources = new Vector();
    FrankelB_GR_EqkSources = new Vector();

    String  magFreqDistType = "", faultingStyle, sourceName="";

    double dlen, dmove;                 // fault discretization and floater offset, respectively
    int numBranches;                    // num branches for mag epistemic uncertainty
    Vector branchDmags = new Vector();  // delta mags for epistemic uncertainty
    Vector branchWts = new Vector();    // wts for epistemic uncertainty
    double aleStdDev, aleWidth;         // aleatory mag uncertainties

    GriddedFaultFactory factory;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon, rake=Double.NaN;
    double mag=0;  // used for magChar and magUpper (latter for the GR distributions)
    double aVal=0, bVal=0, magLower, deltaMag;
    double charRate=0, dip=0, downDipWidth=0, depthToTop=0;

    // get adjustable parameters values
    String faultModel = (String) faultModelParam.getValue();
    double rupOffset = ((Double) rupOffset_Param.getValue()).doubleValue();

    double timeDuration =  timeSpan.getDuration();

    ListIterator it = inputFaultFileLines.listIterator();

    // get first line
    StringTokenizer st = new StringTokenizer(it.next().toString());
    // first line has the fault discretization & floater offset
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



      // get the next line from the file
      st = new StringTokenizer(it.next().toString());

      // if it's a characteristic distribution:
      if(magFreqDistType.equals(CHAR_MAG_FREQ_DIST)) {

          mag=Double.parseDouble(st.nextToken());
          charRate=Double.parseDouble(st.nextToken());
          // if the file is "ca-wg99-dist-char" add the magnitude to the name to make source names unique
          if(fileName.equals("ca-wg99-dist-char")) sourceName += " M="+mag;
          sourceName += " Char";

          // calculate moment rate & itest here
      }
      else { // It's a GR distribution

          aVal=Double.parseDouble(st.nextToken());
          bVal=Double.parseDouble(st.nextToken());
          magLower=Double.parseDouble(st.nextToken());
          mag=Double.parseDouble(st.nextToken());
          deltaMag=Double.parseDouble(st.nextToken());
          // Set name according to the number of mags
          if( mag == magLower )
            sourceName += " fl-Char";
          else
            sourceName += " GR";
//System.out.println(sourceName+"  aVal="+aVal+"  bVal="+bVal+"  magLower="+magLower+"  mag="+mag+"  deltaMag="+deltaMag);
          //calculate moment rate & itest here
      }

      if(D) System.out.println("    "+sourceName);

      allSourceNames.add(sourceName);

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

      //based on the num of the data lines reading the lat and long points for rthe faults
      for(int i=0;i<numOfDataLines;++i) {
        if( !it.hasNext() ) throw ERR;
        st =new StringTokenizer(it.next().toString().trim());
        lat = new Double(st.nextToken()).doubleValue();
        lon = new Double(st.nextToken()).doubleValue();
        Location loc = new Location(lat, lon, upperSeismoDepth);
        faultTrace.addLocation( (Location)loc.clone());
      }

      // reverse data ordering if dip negative, make positive and reverse trace order
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
