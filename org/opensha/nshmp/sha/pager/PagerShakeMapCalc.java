package org.opensha.nshmp.sha.pager;


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
import org.opensha.data.XYZ_DataSetAPI;
import org.opensha.sha.param.PropagationEffect;
import org.opensha.sha.calc.ScenarioShakeMapCalculator;
import org.opensha.sha.gui.beans.MapGuiBean;

import java.text.DecimalFormat;
import org.opensha.sha.gui.infoTools.IMT_Info;

/**
 * <p>Title: PagerShakeMapCalc</p>
 *
 * <p>Description: </p>
 *
 * @author Nitin Gupta, Vipin Gupta, and Ned Field
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
  private String imt;
  private boolean pointSourceCorrection; //if point source corrcetion needs to be applied
  private boolean gmtMapToGenerate; // if GMT map needs to be geberated
  private String defaultSiteType; //in case we are not able to get the site type for any site
  //in the region.

  //instance to the Scenario ShakeMap Calc
  private ScenarioShakeMapCalculator calc;


  private DecimalFormat latLonFormat  = new DecimalFormat("0.000##");
  private DecimalFormat meanSigmaFormat = new DecimalFormat("0.000##");

  //Map Making Gui Bean
  private MapGuiBean mapGuiBean;

  private String outputFilePrefix;


  public PagerShakeMapCalc() {
  }


  public void parseFile(String fileName) throws FileNotFoundException,IOException{

      ArrayList fileLines = null;

      fileLines = FileUtils.loadFile(fileName);

      int j = 0;
      for(int i=0; i<fileLines.size(); ++i) {
        String line = ((String)fileLines.get(i)).trim();
        // if it is comment skip to next line
        if(line.startsWith("#") || line.equals("")) continue;

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
        ++j;
      }

  }

  /**
   * Setting the Region parameters
   * @param str String
   */
  private void setRegionParams(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    double minLat = Double.parseDouble(tokenizer.nextToken());
    double maxLat = Double.parseDouble(tokenizer.nextToken());
    double minLon = Double.parseDouble(tokenizer.nextToken());
    double maxLon = Double.parseDouble(tokenizer.nextToken());
    double gridSpacing = Double.parseDouble(tokenizer.nextToken());
    if(minLat >= maxLat){
      System.out.println("MinLat must be less than MaxLat");
      System.exit(0);
    }
    if(minLon >= maxLon){
      System.out.println("MinLon must be less than MaxLon");
      System.exit(0);
    }

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
    Location rupLoc = new Location(rupLat,rupLon,rupDepth);
    rupture.setPointSurface(rupLoc,rupDip);
    //    rupture.setHypocenterLocation(rupLoc);    // this will put a star at the hypocenter
    rupture.setMag(rupMag);
    rupture.setAveRake(rupRake);
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
    attenRel.setIntensityMeasure(imt);
    if(imt.equalsIgnoreCase("SA")){
      double period = Double.parseDouble(tokenizer.nextToken());
      attenRel.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(period));
      imt += "-"+period;
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
   * Gets the wills site class for the given sites
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

  /**
   *
   * @return XYZ_DataSetAPI
   */
  private XYZ_DataSetAPI pagerShakeMapCalc() {

    PropagationEffect propagationEffect = new PropagationEffect();

    ParameterList paramList = propagationEffect.getAdjustableParameterList();
    paramList.getParameter(propagationEffect.APPROX_DIST_PARAM_NAME).setValue(new
        Boolean(true));

    if (pointSourceCorrection)
      paramList.getParameter(propagationEffect.POINT_SRC_CORR_PARAM_NAME).
          setValue(new Boolean(true));
    else
      paramList.getParameter(propagationEffect.POINT_SRC_CORR_PARAM_NAME).
          setValue(new Boolean(false));

    //Calls the ScenarioShakeMap Calculator to generate Median File
    calc = new ScenarioShakeMapCalculator(propagationEffect);
    ArrayList attenRelsSupported = new ArrayList();
    attenRelsSupported.add(attenRel);
    ArrayList attenRelWts = new ArrayList();
    attenRelWts.add(new Double(1.0));
    XYZ_DataSetAPI xyzDataSet = calc.getScenarioShakeMapData(attenRelsSupported,attenRelWts,region,rupture,!imlAtProb,imlProbVal);
    //if the IMT is log supported then take the exponential of the Value if IML @ Prob
    if (IMT_Info.isIMT_LogNormalDist(attenRel.getIntensityMeasure().getName()) && imlAtProb) {
      ArrayList zVals = xyzDataSet.getZ_DataSet();
      int size = zVals.size();
      for (int i = 0; i < size; ++i) {
        double tempVal = Math.exp( ( (Double) (zVals.get(i))).doubleValue());
        zVals.set(i, new Double(tempVal));
      }
    }
    return xyzDataSet;
  }


  private void createMedianFile(XYZ_DataSetAPI xyzData){

    ArrayList xVals = xyzData.getX_DataSet();
    ArrayList yVals = xyzData.getY_DataSet();
    ArrayList zVals = xyzData.getZ_DataSet();
    try {
      FileWriter fw = new FileWriter(this.outputFilePrefix + "_data.txt");
      int size = xVals.size();
      for(int i=0;i<size;++i)
        fw.write(latLonFormat.format(xVals.get(i))+"  "+latLonFormat.format(yVals.get(i))+"  "+
                 meanSigmaFormat.format(zVals.get(i))+"\n");
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * This method creates the Scenario ShakeMap
   * @param xyzDataSet XYZ_DataSetAPI
   */
  private void createMap(XYZ_DataSetAPI xyzDataSet){
    if(gmtMapToGenerate){
     mapGuiBean = new MapGuiBean();
     mapGuiBean.setVisible(false);
     mapGuiBean.setRegionParams(region.getMinLat(),region.getMaxLat(),
                                region.getMinLon(),region.getMaxLon(),region.getGridSpacing());
      String label = "";
      if (imlAtProb)
        label = imt;
      else
        label = "prob";

      mapGuiBean.makeMap(xyzDataSet, rupture, label, getMapParametersInfo());
    }
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

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  private String getMapParametersInfo() {

    String imrMetadata = "Selected Attenuation Relationship:<br>\n " +
        "---------------<br>\n";

    imrMetadata += attenRel.getName() + "\n";

    String imtMetadata = "Selected IMT :<br>\n "+
        "---------------<br>\n";
    imtMetadata += imt+"<br>\n";

    //getting the metadata for the Calculation setting Params
    String calculationSettingsParamsMetadata =
        "<br><br>Calculation Param List:<br>\n " +
        "------------------<br>\n" + getCalcParamMetadataString() + "\n";

    return imrMetadata +imtMetadata+
        "<br><br>Region Info: <br>\n" +
        "----------------<br>\n" +
        "Min Lat = "+region.getMinLat()+"<br>\n"+
        "Max Lat = "+region.getMaxLat()+"<br>\n"+
        "Min Lon = "+region.getMinLon()+"<br>\n"+
        "Max Lon = "+region.getMaxLon()+"<br>\n"+
        "Default Wills Site Class Value = "+defaultSiteType+"<br>"+
        "\n" +
        "<br> Rupture Info: <br>\n"+
        rupture.getInfo()+ "\n" +
        "<br><br>GMT Param List: <br>\n" +
        "--------------------<br>\n" +
        mapGuiBean.getParameterList().getParameterListMetadataString() + "\n" +
        calculationSettingsParamsMetadata;
  }
  /**
   *
   * @returns the Adjustable parameters for the ScenarioShakeMap calculator
   */
  private ParameterList getCalcAdjustableParams(){
    return calc.getAdjustableParams();
  }


  /**
   *
   * @returns the Metadata string for the Calculation Settings Adjustable Params
   */
  private String getCalcParamMetadataString(){
    return getCalcAdjustableParams().getParameterListMetadataString();
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Must provide the input file name\n");
      System.out.println("Usage :\n\t" +
          "java -jar [jarfileName] [inputFileName]\n\n");
      System.out.println("jarfileName : Name of the executable jar file, by default it is PagerShakeMapCalc.jar");
      System.out.println(
          "inputFileName :Name of the input file,For eg: see \"inputFile.txt\". ");
      System.exit(0);
    }

    PagerShakeMapCalc pagershakemapcalc = new PagerShakeMapCalc();
    try {
      pagershakemapcalc.parseFile(args[0]);
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

    pagershakemapcalc.getSiteParamsForRegion();
    XYZ_DataSetAPI xyzDataSet =pagershakemapcalc.pagerShakeMapCalc();
    pagershakemapcalc.createMedianFile(xyzDataSet);
    pagershakemapcalc.createMap(xyzDataSet);
  }
}
