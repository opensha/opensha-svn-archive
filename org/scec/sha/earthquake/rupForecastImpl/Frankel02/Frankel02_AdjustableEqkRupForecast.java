package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.scec.param.*;
import org.scec.calc.MomentMagCalc;
import org.scec.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.scec.util.*;
import org.scec.data.Location;
import org.scec.sha.fault.*;
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

//  ArrayList allSourceNames;

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


  private FrankelGriddedFaultFactory frankelFaultFactory = new FrankelGriddedFaultFactory();
  private StirlingGriddedFaultFactory stirlingFaultFactory = new StirlingGriddedFaultFactory();

  /*
   * Static variables for input files
   */

  //private final static String IN_FILE_PATH = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/frankel02_inputfiles/";
  private final static String IN_FILE_PATH = "org/scec/sha/earthquake/rupForecastImpl/Frankel02/InputFiles/";

  /**
   * Vectors for holding the various sources, separated by type
   */
  private ArrayList charFaultSources;
  private ArrayList grFaultSources;
  private ArrayList frankelBackgrSeisSources;
  private ArrayList allSources;

  private WC1994_MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();

  // fault-model parameter stuff
  public final static String FAULT_MODEL_NAME = new String ("Fault Model");
  public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
  public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");
  // make the fault-model parameter
  ArrayList faultModelNamesStrings = new ArrayList();
  StringParameter faultModelParam;

  // fault-model parameter stuff
  public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
  public final static String BACK_SEIS_INCLUDE = new String ("Include");
  public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
  public final static String BACK_SEIS_ONLY = new String ("Only Background");
  // make the fault-model parameter
  ArrayList backSeisOptionsStrings = new ArrayList();
  StringParameter backSeisParam;

  // fault-model parameter stuff
  public final static String BACK_SEIS_RUP_NAME = new String ("Treat Background Seismicity As");
  public final static String BACK_SEIS_RUP_POINT = new String ("Point Sources");
  public final static String BACK_SEIS_RUP_FINITE = new String ("Finite Sources");
  // make the fault-model parameter
  ArrayList backSeisRupStrings = new ArrayList();
  StringParameter backSeisRupParam;

  // For rupture offset lenth along fault parameter
  public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
  private Double DEFAULT_RUP_OFFSET_VAL= new Double(10);
  private final static String RUP_OFFSET_PARAM_UNITS = "km";
  private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
  private final static double RUP_OFFSET_PARAM_MIN = 1;
  private final static double RUP_OFFSET_PARAM_MAX = 100;
  DoubleParameter rupOffset_Param;

/*
  // fault file parameter for testing
  public final static String FAULT_FILE_NAME = new String ("Fault File");
  // make the fault-model parameter
  ArrayList faultFileNamesStrings = new ArrayList();
  StringParameter faultFileParam;
*/

  /**
   *
   * No argument constructor
   */
  public Frankel02_AdjustableEqkRupForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);
    timeSpan.setDuration(50);

    // create and add adj params to list
    initAdjParams();


    // add the change listener to parameters so that forecast can be updated
    // whenever any paramater changes
    faultModelParam.addParameterChangeListener(this);
    rupOffset_Param.addParameterChangeListener(this);
    backSeisParam.addParameterChangeListener(this);
    backSeisRupParam.addParameterChangeListener(this);
//    faultFileParam.addParameterChangeListener(this);

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

    backSeisRupStrings.add(BACK_SEIS_RUP_POINT);
    backSeisRupStrings.add(BACK_SEIS_RUP_FINITE);
    backSeisRupParam = new StringParameter(BACK_SEIS_RUP_NAME, backSeisRupStrings,BACK_SEIS_RUP_POINT);

    rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
        RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
    rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);


// add adjustable parameters to the list
    adjustableParams.addParameter(faultModelParam);
    adjustableParams.addParameter(rupOffset_Param);
    adjustableParams.addParameter(backSeisParam);
    adjustableParams.addParameter(backSeisRupParam);

/*
// this was for testing:
        faultFileNamesStrings.add("CAmapC_OpenSHA");
        faultFileNamesStrings.add("CAmapG_OpenSHA");
        faultFileNamesStrings.add("EXTmapC_OpenSHA");
        faultFileNamesStrings.add("EXTmapGW_OpenSHA");
        faultFileNamesStrings.add("WUSmapC_OpenSHA");
        faultFileNamesStrings.add("WUSmapG_OpenSHA");
        faultFileNamesStrings.add("brawmap_OpenSHA");
        faultFileNamesStrings.add("cadeepAB_OpenSHA");
        faultFileNamesStrings.add("cadeepY_OpenSHA");
        faultFileNamesStrings.add("creepmap_OpenSHA");
        faultFileNamesStrings.add("shear1_OpenSHA");
        faultFileNamesStrings.add("shear2_OpenSHA");
        faultFileNamesStrings.add("shear3_OpenSHA");
        faultFileNamesStrings.add("shear4_OpenSHA");
        faultFileParam = new StringParameter(FAULT_FILE_NAME, faultFileNamesStrings,
        (String)faultFileNamesStrings.get(0));
        adjustableParams.addParameter(faultFileParam);
*/
/*
    faultFileNamesStrings.add("ca-a-other-fixed-char");
    faultFileNamesStrings.add("ca-a-other-norm-char");
    faultFileNamesStrings.add("ca-amod1-char");
    faultFileNamesStrings.add("ca-amod2-char");
    faultFileNamesStrings.add("ca-b-fullwt-norm-ell-65");
    faultFileNamesStrings.add("ca-b-fullwt-norm-ell-char");
    faultFileNamesStrings.add("ca-b-fullwt-norm-ell-gr");
    faultFileNamesStrings.add("ca-b-fullwt-norm-hank-65");
    faultFileNamesStrings.add("ca-b-fullwt-norm-hank-char");
    faultFileNamesStrings.add("ca-b-fullwt-norm-hank-gr");
    faultFileNamesStrings.add("ca-b-fullwt-ss-ell-65");
    faultFileNamesStrings.add("ca-b-fullwt-ss-ell-char");
    faultFileNamesStrings.add("ca-b-fullwt-ss-ell-gr");
    faultFileNamesStrings.add("ca-b-fullwt-ss-hank-65");
    faultFileNamesStrings.add("ca-b-fullwt-ss-hank-char");
    faultFileNamesStrings.add("ca-b-fullwt-ss-hank-gr");
    faultFileNamesStrings.add("ca-bflt-25weight-ell-char");
    faultFileNamesStrings.add("ca-bflt-25weight-ell-gr");
    faultFileNamesStrings.add("ca-bflt-25weight-hank-char");
    faultFileNamesStrings.add("ca-bflt-25weight-hank-gr");
    faultFileNamesStrings.add("ca-bflt-50weight-ell-65");
    faultFileNamesStrings.add("ca-bflt-50weight-ell-char");
    faultFileNamesStrings.add("ca-bflt-50weight-ell-gr");
    faultFileNamesStrings.add("ca-bflt-50weight-hank-65");
    faultFileNamesStrings.add("ca-bflt-50weight-hank-char");
    faultFileNamesStrings.add("ca-bflt-50weight-hank-gr");
    faultFileNamesStrings.add("ca-bflt-fix-norm-ell-65");
    faultFileNamesStrings.add("ca-bflt-fix-norm-ell-char");
    faultFileNamesStrings.add("ca-bflt-fix-norm-ell-gr");
    faultFileNamesStrings.add("ca-bflt-fix-norm-hank-65");
    faultFileNamesStrings.add("ca-bflt-fix-norm-hank-char");
    faultFileNamesStrings.add("ca-bflt-fix-norm-hank-gr");
    faultFileNamesStrings.add("ca-bflt-fix-ss-ell-65");
    faultFileNamesStrings.add("ca-bflt-fix-ss-ell-char");
    faultFileNamesStrings.add("ca-bflt-fix-ss-ell-gr");
    faultFileNamesStrings.add("ca-bflt-fix-ss-hank-65");
    faultFileNamesStrings.add("ca-bflt-fix-ss-hank-char");
    faultFileNamesStrings.add("ca-bflt-fix-ss-hank-gr");
    faultFileNamesStrings.add("ca-wg99-dist-char");
    faultFileNamesStrings.add("ca-wg99-dist-float");
    faultFileNamesStrings.add("creepflt");
    faultFileParam = new StringParameter(FAULT_FILE_NAME, faultFileNamesStrings,
        (String)faultFileNamesStrings.get(0));
*/
  }

  /**
   * This makes the sources for the input files of hazgridX.f (and wts):
   */
  private void makeAllGridSources() {



//    String tempName = (String)faultFileParam.getValue();
//    makeGridSources(tempName,1.0,null,0.0);

    makeGridSources("CAmapC_OpenSHA", 0.667, "CAmapG_OpenSHA", 0.333);
    makeGridSources("EXTmapC_OpenSHA", 0.5, "EXTmapGW_OpenSHA", 0.5);
    makeGridSources("WUSmapC_OpenSHA", 0.5, "WUSmapG_OpenSHA", 0.5);
    makeGridSources("brawmap_OpenSHA", 1.0, null, 0.0);
    makeGridSources("cadeepAB_OpenSHA", 1.0, null, 0.0); // this file is identical to cadeepY_OpenSHA"
    makeGridSources("creepmap_OpenSHA", 1.0, null, 0.0);
    makeGridSources("shear1_OpenSHA", 1.0, null, 0.0);
    makeGridSources("shear2_OpenSHA", 1.0, null, 0.0);
    makeGridSources("shear3_OpenSHA", 1.0, null, 0.0);
    makeGridSources("shear4_OpenSHA", 1.0, null, 0.0);

  }


  /**
   * This makes the sources for the input files of hazFXv3 and hazFXv3a (and wts):
   */
  private void makeAllFaultSources() {

// these were for testing
//    makeFaultSources(test1,1.0,null,0.0);

//    String tempName = (String)faultFileParam.getValue();
//    makeFaultSources(tempName,1.0,null,0.0);


    makeFaultSources("ca-a-other-fixed-char", 1.0, null, 1.0);
    makeFaultSources("ca-a-other-norm-char", 1.0, null, 1.0);
    makeFaultSources("ca-amod1-char", 0.5, null, 1.0);
    makeFaultSources("ca-amod2-char", 0.5, null, 1.0);
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
    makeFaultSources("ca-wg99-dist-char", 1.0, null, 1.0);
    makeFaultSources("ca-wg99-dist-float", 1.0, null, 1.0);
    makeFaultSources("creepflt", 1.0, null, 1.0);

// not sure if the rest are needed
/*
    makeFaultSources("ext-norm-65", 1.0,null,0);
    makeFaultSources("ext-norm-char", 0.5,null,0);
    makeFaultSources("ext-norm-gr", 0.5,null,0);
    makeFaultSources("wa_or-65", 1.0,null,0);
    makeFaultSources("wa_or-char", 0.5,null,0);
    makeFaultSources("wa_or-gr", 0.5,null,0);
*/

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

    String  magFreqDistType = "", faultingStyle, sourceName="";
    double gridSpacing, dmove;                 // fault discretization and floater offset, respectively
    int numBranches, numMags, numMags2;                    // num branches for mag epistemic uncertainty
    ArrayList branchDmags = new ArrayList();  // delta mags for epistemic uncertainty
    ArrayList branchWts = new ArrayList();    // wts for epistemic uncertainty
    double aleStdDev, aleWidth;         // aleatory mag uncertainties

    SummedMagFreqDist totalMagFreqDist;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon, rake=Double.NaN;
    double mag=0, mag2=0;  // used for magChar and magUpper (latter for the GR distributions)
    double aVal=0, bVal=0, magLower, deltaMag, moRate;
    double aVal2=0, bVal2=0, magLower2=0,deltaMag2=0, moRate2=0;

    double charRate=0,charRate2, dip=0, downDipWidth=0, depthToTop=0;
    double minMag, maxMag, minMag2=0, maxMag2=0;

    double mLow, mHigh;


    double test, test2=0;
    double magEp, wtEp;

    GriddedSurfaceAPI surface;

    // get adjustable parameters values
    String faultModel = (String) faultModelParam.getValue();
    double rupOffset = ((Double) rupOffset_Param.getValue()).doubleValue();

    // get the duration
    double duration = timeSpan.getDuration();

    // get an iterator for the input file lines
    ListIterator it = inputFaultFileLines1.listIterator();

    // get first line
    StringTokenizer st = new StringTokenizer(it.next().toString());
    // first line has the fault discretization & floater offset
    // (these are 1.0 & 1.0 in all the files)
    gridSpacing = Double.parseDouble(st.nextToken());
    dmove = Double.parseDouble(st.nextToken());  // this is ignored since we have the rupOffset parameter

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
      }

      // get the next line from the file
      st = new StringTokenizer(it.next().toString());

      // MAKE THE MAG-FREQ-DIST

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

          // get the same info from the second file if necessary
          if(fileName2 != null) {
            // get the same line from the second file
            st = new StringTokenizer((String) inputFaultFileLines2.get(it.nextIndex()-1));
            mag2=Double.parseDouble(st.nextToken());
            charRate2=Double.parseDouble(st.nextToken());
            moRate2 = charRate2*MomentMagCalc.getMoment(mag2);
            minMag2 = mag2 + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
            maxMag2 = mag2 + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue() + aleWidth*0.05;
          }

          // make the Char magFreqDist for case where no  uncertainties should be considered
          if(minMag < 5.8 || aleStdDev == 0.0) {   // the no-uncertainty case:
            if(fileName2 == null){
              SingleMagFreqDist tempDist = new SingleMagFreqDist(mag,1,0.1,mag,moRate*wt1);
              totalMagFreqDist = new SummedMagFreqDist(mag,1,0.1, false, false);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
            }
            // the case when the second filename is not null
            else {
              // make sure minMag2 does not violate the if statement above
              // (comment this out after it's run once since files won't change)
              if(minMag2 >= 5.8) throw new RuntimeException(C+" Problem: minMag of second file conflicts");
              // find the min/max mags for the combined distribution
              SingleMagFreqDist tempDist;
              if(mag > mag2) {
                totalMagFreqDist = new SummedMagFreqDist(mag2,mag,2, false, false);
                tempDist = new SingleMagFreqDist(mag2,mag,2);
              }
              else {
                totalMagFreqDist = new SummedMagFreqDist(mag,mag2,2, false, false);
                tempDist = new SingleMagFreqDist(mag,mag2,2);
              }
              tempDist.setMagAndMomentRate(mag,moRate*wt1);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
              tempDist.setMagAndMomentRate(mag2,moRate2*wt2);
              totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
            }
          }
          else { // Apply both aleatory and epistemic uncertainties
            //find the lower and upper magnitudes
            if(fileName2 == null){
              mLow = minMag;
              mHigh = maxMag;
            }
            else {
              if(mag < mag2) {
                mLow = minMag;
                mHigh = maxMag2;
              }
              else {
                mLow = minMag2;
                mHigh = maxMag;
              }
            }
            int numMag = Math.round((float)((mHigh-mLow)/0.05 + 1));
            totalMagFreqDist = new SummedMagFreqDist(mLow,mHigh,numMag, false, false);
            // loop over epistemic uncertianties
            GaussianMagFreqDist tempDist = new GaussianMagFreqDist(mLow,mHigh,numMag);
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

          // read the GR parameters
          aVal=Double.parseDouble(st.nextToken());
          bVal=Double.parseDouble(st.nextToken());
          magLower=Double.parseDouble(st.nextToken());
          mag=Double.parseDouble(st.nextToken());
          deltaMag=Double.parseDouble(st.nextToken());
          // move lower and upper mags to be bin centered (if they aren't the same)
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
            numMags2 = Math.round( (float)((mag2-magLower2)/deltaMag2 + 1.0) );
            //calculate moment rate (the exact same way Frankel does it)
            moRate2 = getMomentRate(magLower2, numMags2,deltaMag2,aVal2,bVal2);
          }

          // Set the source-name suffix
          if( mag == magLower )
            sourceName += " fl-Char";  // this is to ensure a unique source name
          else {
            sourceName += " GR";
          }

          // Do the single-magnitude case first
          if(numMags == 1) {
            minMag = mag + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
            maxMag = mag + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue() + aleWidth*0.05;

            // Do Gaussian dist w/ aleatory and epistemic uncertainty first
            if(minMag >= 5.8 && aleStdDev != 0.0) {

              // get mLow and mHigh for distribution
              if (fileName2 != null) {
                minMag2 = mag2 + ((Double)branchDmags.get(0)).doubleValue() - aleWidth*0.05;
                maxMag2 = mag2 + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue() + aleWidth*0.05;
                // throw execption if minMag2 fails the above test
                if (minMag2 < 5.8) {   // (comment out after initial run since input files won't change)
                  throw new RuntimeException(C+" PROBLEM: conflicting treatment of file2");
                }
                if(mag < mag2) {
                  mLow = minMag;
                  mHigh = maxMag2;
                }
                else {
                  mLow = minMag2;
                  mHigh = maxMag;
                }
              }
              else {
                mLow = minMag;
                mHigh = maxMag;
              }

              int numMag = Math.round((float)((mHigh-mLow)/0.05 + 1));
              totalMagFreqDist = new SummedMagFreqDist(mLow,mHigh,numMag, false, false);
              // loop over epistemic uncertianties
              GaussianMagFreqDist tempDist = new GaussianMagFreqDist(mLow,mHigh,numMag);
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
            // Do Single mag dist w/ no uncertainties
            else {
              if(fileName2 == null){
                SingleMagFreqDist tempDist = new SingleMagFreqDist(mag,1,0.1,mag,moRate*wt1);
                totalMagFreqDist = new SummedMagFreqDist(mag,1,0.1, false, false);
                totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
              }
              else {
                SingleMagFreqDist tempDist;
                if(mag > mag2) {
                  totalMagFreqDist = new SummedMagFreqDist(mag2,mag,2, false, false);
                  tempDist = new SingleMagFreqDist(mag2,mag,2);
                }
                else {
                  totalMagFreqDist = new SummedMagFreqDist(mag,mag2,2, false, false);
                  tempDist = new SingleMagFreqDist(mag,mag2,2);
                 }
                tempDist.setMagAndMomentRate(mag,moRate*wt1);
                totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
                tempDist.setMagAndMomentRate(mag2,moRate2*wt2);
                totalMagFreqDist.addIncrementalMagFreqDist(tempDist);
              }
            }
          }

          // GR distribution case
          else {

            // get mLow and mHigh of the total mag-freq-dist
            test = mag + ((Double)branchDmags.get(0)).doubleValue();
            if(test >= 6.5 && aleStdDev != 0.0) {
              maxMag = mag + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue();
            }
            else {
              maxMag = mag;
            }
            if (fileName2 != null) {
              test2 = mag2 + ((Double)branchDmags.get(0)).doubleValue();
              if(test2 >= 6.5 && aleStdDev != 0.0) {
                maxMag2 = mag2 + ((Double)branchDmags.get(branchDmags.size()-1)).doubleValue();
              }
              else {
                maxMag2 = mag2;
              }
              if(maxMag2 > maxMag){
                mHigh = maxMag2;
              }
              else {
                mHigh = maxMag;
              }
              mLow = magLower;
              // Check that magLower and deltaMag are same for both files
              // (this only needs to be done once)
              if (magLower != magLower2 || deltaMag != deltaMag2) {
                throw new RuntimeException(C + ": Error - magLower & deltaMag must be same for both files");
              }
            }
            else {
              mLow = magLower;
              mHigh = maxMag;
            }

            // make the GR distributions
            int numMag = Math.round((float)((mHigh-mLow)/deltaMag + 1));
            totalMagFreqDist = new SummedMagFreqDist(mLow,mHigh,numMag, false, false);
            GutenbergRichterMagFreqDist tempGR_dist = new GutenbergRichterMagFreqDist(mLow,mHigh,numMag);

            // GR with epistemic uncertainties
            if(test >= 6.5 && aleStdDev != 0.0) {
              for(int i=0;i<branchDmags.size();i++) {
                magEp = mag + ((Double)branchDmags.get(i)).doubleValue();
                wtEp = ((Double)branchWts.get(i)).doubleValue();
                tempGR_dist.setAllButTotCumRate(magLower,magEp,moRate*wtEp*wt1,bVal);
                totalMagFreqDist.addIncrementalMagFreqDist(tempGR_dist);
              }
            }
            // GR w/ no epistemic uncertainties
            else {
              tempGR_dist.setAllButTotCumRate(magLower,mag,moRate*wt1,bVal);
              totalMagFreqDist.addIncrementalMagFreqDist(tempGR_dist);
            }

            // now do same for second file if necessary
            if(fileName2 != null) {
              // GR with epistemic uncertainties
              if(test2 >= 6.5 && aleStdDev != 0.0) {
                for(int i=0;i<branchDmags.size();i++) {
                  magEp = mag2 + ((Double)branchDmags.get(i)).doubleValue();
                  wtEp = ((Double)branchWts.get(i)).doubleValue();
                  tempGR_dist.setAllButTotCumRate(magLower2,magEp,moRate2*wtEp*wt2,bVal2);
                  totalMagFreqDist.addIncrementalMagFreqDist(tempGR_dist);
                }
              }
              // GR w/ no epistemic uncertainties
              else {
                tempGR_dist.setAllButTotCumRate(magLower2,mag2,moRate2*wt2,bVal2);
                totalMagFreqDist.addIncrementalMagFreqDist(tempGR_dist);
              }
            }
          }
       }

      if(D) System.out.println("    "+sourceName);

//      allSourceNames.add(sourceName);

      // MAKE THE FAULT SURFACE

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

      // Make the fault surface
      if(faultModel.equals(FAULT_MODEL_FRANKEL)) {
        frankelFaultFactory.setAll(faultTrace, dip, upperSeismoDepth,
                                   lowerSeismoDepth, gridSpacing);
        surface = frankelFaultFactory.getGriddedSurface();
      }
      else {
        stirlingFaultFactory.setAll(faultTrace, dip, upperSeismoDepth,
                                   lowerSeismoDepth, gridSpacing);
        surface = stirlingFaultFactory.getGriddedSurface();
      }

      if(D) {
        System.out.println(totalMagFreqDist);
        for(int n=0;n< totalMagFreqDist.getNum();n++)
          System.out.println("\t"+(float)totalMagFreqDist.getX(n)+"  "+(float)totalMagFreqDist.getY(n));
      }

      // MAKE THE SOURCES (adding to the appropriate list)
      if(magFreqDistType.equals(CHAR_MAG_FREQ_DIST)) {
        SimpleFaultRuptureSource frs = new SimpleFaultRuptureSource(totalMagFreqDist,
                                                                    (EvenlyGriddedSurface) surface,
                                                                    rake,duration);
        frs.setName(sourceName);
        charFaultSources.add(frs);
      }
      else {
        Frankel02_GR_EqkSource fgrs = new Frankel02_GR_EqkSource(totalMagFreqDist,
                                                                 (EvenlyGriddedSurface) surface,
                                                                 rupOffset, rake, duration, sourceName);
        grFaultSources.add(fgrs);
      }

    }  // bottom of loop over input-file lines

  }



  /**
   *  This assumes the second file differs only in the max mag (third column)
   */
  private void makeGridSources(String fileName1, double wt1, String fileName2, double wt2) {

    // Debuggin stuff
    String S = C + ": makeGridSources(): ";

    double bVal, magMin, magMax, deltaMag, magRef, locMagMax1, locMagMax2=0;
    double strike = Double.NaN;
    int iflt, maxmat;

    // read the lines of the 1st input file into a list
    ArrayList inputGridFileLines1=null;
    try{ inputGridFileLines1 = FileUtils.loadFile(IN_FILE_PATH + fileName1 ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}
    if( D ) System.out.println("fileName1 = " + IN_FILE_PATH + fileName1);

    // read second file's lines if necessary
    ArrayList inputGridFileLines2=null;
    if(fileName2 != null) {
      try{ inputGridFileLines2 = FileUtils.loadFile(IN_FILE_PATH + fileName2 ); }
      catch( FileNotFoundException e){ System.out.println(e.toString()); }
      catch( IOException e){ System.out.println(e.toString());}
      if( D ) System.out.println("fileName2 = " + IN_FILE_PATH + fileName2);
    }

    // get the duration
    double duration = timeSpan.getDuration();

    // get an iterator for the input file lines
    ListIterator it = inputGridFileLines1.listIterator();

    // get first line (default GR dist stuff)
    StringTokenizer st = new StringTokenizer(it.next().toString());
    bVal = Double.parseDouble(st.nextToken());
    magMin = Double.parseDouble(st.nextToken());
    magMax = Double.parseDouble(st.nextToken());
    deltaMag = Double.parseDouble(st.nextToken());
    magRef = Double.parseDouble(st.nextToken());  // this is ignored in Frankel's code

    if(D)  System.out.println(fileName1);

    // get the 2nd line
    st = new StringTokenizer(it.next().toString());
    iflt = Integer.parseInt(st.nextToken());    // source type:
                                                // 0 for point source;
                                                // 1 for finite source with random strike
                                                // 2 for finite source with fixed strike
                                                // others not (yet?) supported

    st.nextToken(); // skip this one (site specific b-value never used)

    maxmat = Integer.parseInt(st.nextToken()); // indicates whether there are site-specific max mags
                                               // (0 if no adn 1 if yes)

    // get fixed strike from next line if necessary
    if(iflt == 2) {
      st = new StringTokenizer(it.next().toString());
      strike = Double.parseDouble(st.nextToken());
    }

    if(D) {
    System.out.println("bVal="+bVal+"  magMin="+magMin+"  magMax="+ magMax+"  deltaMag"+deltaMag+
                         "  iflt="+iflt+"  maxmat="+maxmat);
    }

    // make magMin bin centered
    if(magMin != magMax) magMin += deltaMag/2.0;

    double lat,lon,aVal, moRate, rateAtZeroMag;
    Location loc;
    Point2Vert_SS_FaultPoisSource src;
    IncrementalMagFreqDist magFreqDist;
    while( it.hasNext() ){

      // get next line
      st = new StringTokenizer(it.next().toString());

      lon =  Double.parseDouble(st.nextToken());
      lat =  Double.parseDouble(st.nextToken());
      rateAtZeroMag = Double.parseDouble(st.nextToken());
      aVal = 0.434294*Math.log(rateAtZeroMag);
      loc = new Location(lat,lon);

      // get max-mag element(s) if necessary
      if(maxmat == 1) {
        locMagMax1 = Double.parseDouble(st.nextToken());
        magMax = locMagMax1;
        if(fileName2 != null){
          // get the same line from the second file
          st = new StringTokenizer((String) inputGridFileLines2.get(it.nextIndex()-1));
          st.nextToken();
          st.nextToken();
          st.nextToken();
          locMagMax2 = Double.parseDouble(st.nextToken());
          if(locMagMax2>magMax) magMax = locMagMax2;
        }
      }
      else {
        locMagMax1 = magMax;
      }

      // Note - In Frankel's code there are some checks (and actions) if siteMaxMag < 0.0
      // I'm ignoring these because this is never the case for the CA input files


      if(fileName2 == null) {
        locMagMax1 -= deltaMag/2.0;
        int numMag = Math.round((float) ((locMagMax1-magMin)/deltaMag))+1;
        moRate = getMomentRate(magMin, numMag, deltaMag, aVal, bVal);
        magFreqDist = new GutenbergRichterMagFreqDist(magMin,numMag,deltaMag,moRate*wt1,bVal);
      }
      else {
        magMax -= deltaMag/2.0;
        int numMag = Math.round((float) ((magMax-magMin)/deltaMag))+1;
        SummedMagFreqDist tempDist = new SummedMagFreqDist(magMin,numMag,deltaMag,false,false);
        // do the first one
        locMagMax1 -= deltaMag/2.0;
        int numMag1 = Math.round((float) ((locMagMax1-magMin)/deltaMag))+1;
        moRate = getMomentRate(magMin, numMag1, deltaMag, aVal, bVal);
        tempDist.addIncrementalMagFreqDist(new GutenbergRichterMagFreqDist(magMin,numMag,deltaMag,magMin,
                                                                           locMagMax1,moRate*wt1,bVal));
        // do the second one
        locMagMax2 -= deltaMag/2.0;
        int numMag2 = Math.round((float) ((locMagMax2-magMin)/deltaMag))+1;
        moRate = getMomentRate(magMin, numMag2, deltaMag, aVal, bVal);
        tempDist.addIncrementalMagFreqDist(new GutenbergRichterMagFreqDist(magMin,numMag,deltaMag,magMin,
                                                                           locMagMax2,moRate*wt2,bVal));
        magFreqDist = tempDist;
      }

      String backSeisRup = (String) backSeisRupParam.getValue();
      double magCutOff;
      if (backSeisRup.equals(this.BACK_SEIS_RUP_FINITE))
        magCutOff = 6.0;
      else
        magCutOff = 10.0;

      // now make the source
      if(iflt == 2)
        src = new Point2Vert_SS_FaultPoisSource(loc,magFreqDist,magLenRel,strike,
                                                duration,magCutOff,frankelFaultFactory);
      else
        src = new Point2Vert_SS_FaultPoisSource(loc,magFreqDist,magLenRel, duration,
                                                magCutOff, frankelFaultFactory);

      // add the source
      frankelBackgrSeisSources.add(src);

    }

//System.out.println("tot rate = "+tempRate);
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
      * @return ArrayList of Prob Earthquake sources
      */
     public ArrayList  getSourceList(){

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

       allSources = new ArrayList();
       charFaultSources = new ArrayList();
       grFaultSources = new ArrayList();
       frankelBackgrSeisSources = new ArrayList();


       if (backSeis.equalsIgnoreCase(BACK_SEIS_INCLUDE)) {
         makeAllFaultSources();
         makeAllGridSources();
         // now create the allSources list:
         allSources.addAll(charFaultSources);
         allSources.addAll(grFaultSources);
         allSources.addAll(frankelBackgrSeisSources);

       }
       else if (backSeis.equalsIgnoreCase(BACK_SEIS_EXCLUDE)) {
         // now create the allSources list:
         makeAllFaultSources();
         frankelBackgrSeisSources = null;
         allSources.addAll(charFaultSources);
         allSources.addAll(grFaultSources);
       }
       else {// only background sources
        makeAllGridSources();
        charFaultSources = null;
        grFaultSources = null;
        // now create the allSources list:
        allSources.addAll(frankelBackgrSeisSources);
       }

       parameterChangeFlag = false;
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
    * this computes the moment for the GR distribution exactly the way frankel's code does it
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
     frankCast.updateForecast();
     System.out.println("num sources="+frankCast.getNumSources());
/*     int n;
     for(int i=0; i<frankCast.getNumSources(); i++) {
       n=i+1;
       System.out.println(n+"th source: "+frankCast.getSource(i).getName());
     }
/*
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
