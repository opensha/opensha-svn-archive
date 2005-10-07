package org.opensha.cybershake;


import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import org.opensha.data.Location;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.imr.*;
import org.opensha.util.*;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.param.WarningParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.*;

import org.opensha.sha.util.SiteTranslator;
import org.opensha.param.ParameterAPI;

import org.opensha.data.LocationList;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;


/**
 * <p>Title: PagerShakeMapCalc</p>
 *
 * <p>Description: </p>
 *
 * @author Nitin Gupta, Vipin Gupta, and Ned Field
 * @version 1.0
 */
public class ObsExceedProbCalculator implements ParameterChangeWarningListener{


  /**
   * Parameters from the input file
   */
  private Location loc; //Geographic Location
  private int sourceIndex, ruptureIndex; //EqkRupture
  private AttenuationRelationshipAPI attenRel; //Attenunation Relationship to be used.

  private String imt;
  private String defaultSiteType; //in case we are not able to get the site type for any site
  //in the region.

  //Cybershake SA values
  private ArrayList saVals;
  private  Frankel02_AdjustableEqkRupForecast frankelForecast = null;
  private EqkRupture rupture;


  private void parseFile(String fileName) throws FileNotFoundException,IOException{

      ArrayList fileLines = null;

      fileLines = FileUtils.loadFile(fileName);

      int j = 0;
      for(int i=0; i<fileLines.size(); ++i) {
        String line = ((String)fileLines.get(i)).trim();
        // if it is comment skip to next line
        if(line.startsWith("#") || line.equals("")) continue;

        //
        if(j==0) getLocation(line); // first line sets the region Params
        else if(j==1) setRupture(line) ; // set the rupture params
        else if(j==2) setIMR(line); // set the imr
        else if(j==3) setIMT(line);  // set the IMT

        ++j;
      }

  }

  /**
   * Setting the Region parameters
   * @param str String
   */
  private void getLocation(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    double lat = Double.parseDouble(tokenizer.nextToken());
    double lon = Double.parseDouble(tokenizer.nextToken());
    loc = new Location(lat,lon);
  }


  private void setRupture(String str) {
    rupture = frankelForecast.getRupture(sourceIndex, ruptureIndex);
  }

  private void setIMR(String str) {
    createIMRClassInstance(str.trim());
  }


  /**
  * Creates a class instance from a string of the full class name including packages.
  * This is how you dynamically make objects at runtime if you don't know which\
  * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
  * it the normal way:<P>
  *
  * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
  *
  * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
  * instead to create the same class by:<P>
  *
  * <code>BJF_1997_AttenRel imr =
  * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.opensha.sha.imt.attenRelImpl.BJF_1997_AttenRel");
  * </code><p>
  *
  */

  private void createIMRClassInstance(String AttenRelClassName){
    String attenRelClassPackage = "org.opensha.sha.imr.attenRelImpl.";
      try {
        Class listenerClass = Class.forName( "org.opensha.param.event.ParameterChangeWarningListener" );
        Object[] paramObjects = new Object[]{ this };
        Class[] params = new Class[]{ listenerClass };
        Class imrClass = Class.forName(attenRelClassPackage+AttenRelClassName);
        Constructor con = imrClass.getConstructor( params );
        attenRel = (AttenuationRelationshipAPI)con.newInstance( paramObjects );
        //setting the Attenuation with the default parameters
        attenRel.setParamDefaults();
      } catch ( ClassCastException e ) {
        e.printStackTrace();
      } catch ( ClassNotFoundException e ) {
       e.printStackTrace();
      } catch ( NoSuchMethodException e ) {
       e.printStackTrace();
      } catch ( InvocationTargetException e ) {
        e.printStackTrace();
      } catch ( IllegalAccessException e ) {
        e.printStackTrace();
      } catch ( InstantiationException e ) {
        e.printStackTrace();
      }
  }


  /**
   * Setting the intensity Measure in the Attenuation Relationship
   * @param str String
   */
  private void setIMT(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    imt = tokenizer.nextToken().trim();
    try{
      attenRel.setIntensityMeasure(imt);
    }catch(Exception e){
      System.out.println(imt+" not supported by attenuation relationship "+attenRel.getName());
      System.exit(0);
    }
    if(imt.equalsIgnoreCase("SA")){
      double period = Double.parseDouble(tokenizer.nextToken());
      try{
        attenRel.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new
            Double(period));
      }
      catch (Exception e) {
        System.out.println("SA Period = "+period + " not supported by attenuation relationship " +
                           attenRel.getName());
        System.exit(0);
      }
      imt += "-"+period;
    }
  }







  /**
   * Getting the default site type in case region is outside the California region
   * @param str String
   */
  private void setDefaultWillsSiteType(String str) {
    defaultSiteType = str.trim();
  }




  /**
   * set the site params in IMR according to basin Depth and vs 30
   * @param imr
   * @param lon
   * @param lat
   * @param willsClass
   * @param basinDepth
   */
  private void setSiteParamsInIMR(String willsClass, double basinDepth) {

    Iterator it = attenRel.getSiteParamsIterator(); // get site params for this IMR
    SiteTranslator siteTranslator = new SiteTranslator();
    while(it.hasNext()) {
      ParameterAPI tempParam = (ParameterAPI)it.next();
      //adding the site Params from the CVM, if site is out the range of CVM then it
      //sets the site with whatever site Parameter Value user has choosen in the application
      boolean flag = siteTranslator.setParameterValue(tempParam,willsClass,basinDepth);
      if( !flag ) {
        String message = "cannot set the site parameter \""+tempParam.getName()+"\" from Wills class \""+willsClass+"\""+
                         "\n (no known, sanctioned translation - please set by hand)";

        System.out.println(message);
      }
    }

  }

  /**
   * This method is called when user presses the button to set the params from CVM
   * for choosen IMR's
   * @param e
   */
  void setSiteParam() {
    LocationList locList = new LocationList();
    locList.addLocation(new Location(loc.getLatitude(),loc.getLongitude()));
    String willsClass = "NA";
    double basinDepth = Double.NaN;
    // get the vs 30 and basin depth from cvm
    try {
      willsClass = (String) (ConnectToCVM.getWillsSiteTypeFromCVM(locList)).get(
          0);
      basinDepth = ( (Double) (ConnectToCVM.getBasinDepthFromCVM(locList)).get(0)).
          doubleValue();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    if(willsClass.equals("NA"))
      willsClass = defaultSiteType;
    setSiteParamsInIMR(willsClass, basinDepth);
  }



  private void createObsExceedProbFile(){


  }




  /**
   *  Function that must be implemented by all Listeners for
   *  ParameterChangeWarnEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChangeWarning(ParameterChangeWarningEvent e) {

    String S = " : parameterChangeWarning(): ";

    WarningParameterAPI param = e.getWarningParameter();

    //System.out.println(b);
    param.setValueIgnoreWarning(e.getNewValue());

  }


  private void createERFInstance(){
    frankelForecast = new
        Frankel02_AdjustableEqkRupForecast();

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
                                 BACK_SEIS_INCLUDE);

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME).
        setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_NAME).setValue(
            frankelForecast.FAULT_MODEL_STIRLING);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_NAME).setValue(
            new Double(5.0));

    frankelForecast.getTimeSpan().setDuration(1.0);
    frankelForecast.updateForecast();
  }


  /**
   * Main Methid to run the application
   * @param args String[]
   */
  public static void main(String[] args) {


    ObsExceedProbCalculator calc = new ObsExceedProbCalculator();
    try {
      calc.parseFile(args[0]);
    }
    catch (FileNotFoundException ex) {
      System.out.println("Input File "+ args[0] +" not found");
      System.exit(0);
    }
    catch (Exception ex) {
      System.out.println("Unable to parse the input file"+ args[0]);
      System.out.println("Please provide correct input file.");
      System.exit(0);
    }

//    ObsExceedProbCalculator.getSiteParamsForRegion();
    calc.createObsExceedProbFile();
  }
}
