package org.opensha.nshmp.sha.pager;


import java.util.*;

import org.opensha.data.Location;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.imr.*;
import org.opensha.sha.surface.*;
import org.opensha.util.*;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.param.WarningParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import java.lang.reflect.*;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.param.ParameterAPI;

/**
 * <p>Title: PagerShakeMapCalc</p>
 *
 * <p>Description: </p>
 *
 * @author Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public class PagerShakeMapCalc implements ParameterChangeWarningListener{


  /**
   * Parameters from the input file
   */
  private SitesInGriddedRectangularRegion region; //Geographic Region
  private EqkRupture rupture; //EqkRupture
  private AttenuationRelationshipAPI attenRel; //Attenunation Relationship to be used.
  private boolean imlAtProb; //checks what to plot IML_At_Prob or Prob_At_IML
  private double imlProbVal; //if IML@Prob needs to be calculated the Prob val
  //will be given,else IML val will be given
  private boolean pointSourceCorrection; //if point source corrcetion needs to be applied
  private boolean gmtMapToGenerate; // if GMT map needs to be geberated
  private String defaultSiteType; //in case we are not able to get the site type for any site
  //in the region.

  private String outputFilePrefix;


  public PagerShakeMapCalc() {
  }


  public void parseFile(String fileName) {
    try {
      ArrayList fileLines = FileUtils.loadFile(fileName);
      int j = 0;
      for(int i=0; i<fileLines.size(); ++i) {
        String line = ((String)fileLines.get(i)).trim();
        // if it is comment skip to next line
        if(line.startsWith("#") || line.equals("")) continue;
        ++j;
        //
        if(j==0) setRegionParams(line); // first line sets the region Params
        else if(j==1) setRupture(line) ; // set the rupture params
        else if(j==2) setIMR(line); // set the imr
        else if(j==3) setIMT(line);  // set the IMT
        else if(j==4) setMapType(line); // map type iml at Prob or Prob at IML
        else if(j==5) setPointSrcCorrection(line); // whether point source correction is needed or not needed
        else if(j==6) setDefaultWillsSiteType(line); // default site to use if site parameters are not known for a site
        else if(j==7) setMapRequested(line) ; // whether to generate GMT map
        else if(j==8) setOutputFileName(line); // set the output file name
      }
    }catch(Exception e ) {
      e.printStackTrace();
    }
  }

  private void setRegionParams(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    double minLat = Double.parseDouble(tokenizer.nextToken());
    double maxLat = Double.parseDouble(tokenizer.nextToken());
    double minLon = Double.parseDouble(tokenizer.nextToken());
    double maxLon = Double.parseDouble(tokenizer.nextToken());
    double gridSpacing = Double.parseDouble(tokenizer.nextToken());
    region = new SitesInGriddedRectangularRegion(minLat,maxLat,minLon,maxLon,gridSpacing);
  }

  private void setRupture(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    double rupLat = Double.parseDouble(tokenizer.nextToken());
    double rupLon = Double.parseDouble(tokenizer.nextToken());
    double rupDepth = Double.parseDouble(tokenizer.nextToken());
    double rupMag = Double.parseDouble(tokenizer.nextToken());
    double rupRake = Double.parseDouble(tokenizer.nextToken());
    double rupDip = Double.parseDouble(tokenizer.nextToken());
    rupture = new EqkRupture();
    rupture.setPointSurface(new Location(rupLat,rupLon,rupDepth),rupDip);
    rupture.setMag(rupMag);
    rupture.setAveRake(rupRake);
  }

  private void setIMR(String str) {
    String imrName =  str;
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

  public void createIMRClassInstance(String AttenRelClassName){
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
    String imt = tokenizer.nextToken().trim();
    attenRel.setIntensityMeasure(imt);
    if(imt.equalsIgnoreCase("SA")){
      double period = Double.parseDouble(tokenizer.nextToken());
      attenRel.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(period));
    }
  }


  /**
   * Getting what user wants to plot. IML@Prob or Prob@IML.
   * Then getting the IML or Prob value
   * @param str String
   */
  private void setMapType(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    int mapType = Integer.parseInt(tokenizer.nextToken());
    if (mapType == 0)
      this.imlAtProb = true;
    else
      this.imlAtProb = false;
    imlProbVal = Double.parseDouble(tokenizer.nextToken());
  }


  /**
   * Checking if the point source corection needs to be applied for the calculation
   * @param str String
   */
  private void setPointSrcCorrection(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    int intVal = Integer.parseInt(tokenizer.nextToken());
    if(intVal==0) pointSourceCorrection = false;
    else pointSourceCorrection = true;
  }

  /**
   * Getting the default site type in case region is outside the California region
   * @param str String
   */
  private void setDefaultWillsSiteType(String str) {
    defaultSiteType = str.trim();
  }

  /**
   * Checking if GMT map to be generated
   * @param str String
   */
  private void setMapRequested(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    int intVal = Integer.parseInt(tokenizer.nextToken());
    if(intVal==0) gmtMapToGenerate = false;
    else gmtMapToGenerate = true;
  }

  /**
   * Name of the output file
   * @param str String
   */
  private void setOutputFileName(String str) {
    outputFilePrefix = str.trim();
  }


  /**
   * Gets the wills  site class for the given sites
   */
  private void getSiteParamsForRegion() {
    region.addSiteParams(attenRel.getSiteParamsIterator());
    //getting the Attenuation Site Parameters Liat
    ListIterator it = attenRel.getSiteParamsIterator();
    //creating the list of default Site Parameters, so that site parameter values can be filled in
    //if Site params file does not provide any value to us for it.
    ArrayList defaultSiteParams = new ArrayList();
    SiteTranslator siteTrans = new SiteTranslator();
    while (it.hasNext()) {
      //adding the clone of the site parameters to the list
      ParameterAPI tempParam = (ParameterAPI) ( (ParameterAPI) it.next()).clone();
      //getting the Site Param Value corresponding to the Will Site Class "DE" for the seleted IMR  from the SiteTranslator
      siteTrans.setParameterValue(tempParam, siteTrans.WILLS_DE, Double.NaN);
      defaultSiteParams.add(tempParam);
    }
    region.setDefaultSiteParams(defaultSiteParams);
  }


  private void pagerShakeMapCalc(){

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


  public static void main(String[] args) {
    PagerShakeMapCalc pagershakemapcalc = new PagerShakeMapCalc();
  }
}
