package gov.usgs.sha.data;

import gov.usgs.sha.data.api.DataGeneratorAPI_NEHRP;
import java.util.ArrayList;
import gov.usgs.exceptions.ZipCodeErrorException;
import org.scec.data.function.DiscretizedFuncList;
import gov.usgs.util.GlobalConstants;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: DataGenerator_NEHRP</p>
 *
 * <p>Description: </p>
 * @author Ned Field, Nitin Gupta , E.V.Leyendecker
 * @version 1.0
 */
public class DataGenerator_NEHRP
    implements DataGeneratorAPI_NEHRP {

  //gets the selected region
  private String geographicRegion;
  //gets the selected edition
  private String dataEdition;

  private ArbitrarilyDiscretizedFunc saFunction;


  private float faVal;
  private float fvVal;
  private String siteClass = GlobalConstants.SITE_CLASS_B;
  private ArbitrarilyDiscretizedFunc sdSpectrumSaSdFunction;
  private ArbitrarilyDiscretizedFunc smSpectrumSaSdFunction;
  private ArbitrarilyDiscretizedFunc mapSpectrumSaSdFunction;
  private ArbitrarilyDiscretizedFunc sdSpectrumSaTFunction;
  private ArbitrarilyDiscretizedFunc smSpectrumSaTFunction;
  private ArbitrarilyDiscretizedFunc mapSpectrumSaTFunction;


  //holds all the data and its info in a String format.
  private String dataInfo = "";

  //metadata to be shown when plotting the curves
  private String metadataForPlots;

  /**
   * Default class constructor
   */
  public DataGenerator_NEHRP() {}



  /**
   * Removes all the calculated data.
   */
  public void clearData() {
    dataInfo = "";
  }

  /**
   * Returns the Data and all the metadata associated with it in a String.
   * @return String
   */
  public String getDataInfo() {
    return dataInfo;
  }


  private void addDataInfo(String data){
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo +=data+"\n\n";
  }


  /**
   * Returns the SA at .2sec
   * @return double
   */
  public double getSs(){
    return saFunction.getY(0);
  }

  /**
   * Returns the SA at 1 sec
   * @return double
   */
  public double getSa(){
    return saFunction.getY(1);
  }


  /**
   * Gets the data for SsS1 in case Territory.
   * Territory is when user is not allowed to enter any zip code or Lat-Lon
   * for the location or if it is GAUM and TAUTILLA.
   */
  public void calculateSsS1() {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion);
    String location = "Spectral values are constant for the region";
    createMetadataForPlots(location);
    addDataInfo(function.getInfo());
    saFunction =function;

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon) {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                 lat, lon);
    String location = "Lat - "+lat+"  Lon - "+lon;
    createMetadataForPlots(location);
    addDataInfo(function.getInfo());
    saFunction = function;

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                 zipCode);
    String location = "Zipcode - "+zipCode;
    createMetadataForPlots(location);
    addDataInfo(function.getInfo());
    saFunction = function;
  }

  private void createMetadataForPlots(String location){
    metadataForPlots = GlobalConstants.SA_DAMPING +"\n";
    metadataForPlots += geographicRegion + "\n";
    metadataForPlots += dataEdition + "\n";
    metadataForPlots += location +"\n";
    metadataForPlots += "Site Class -"+siteClass+"\n";
    metadataForPlots += "Fa = "+faVal+" Fv = "+fvVal+"\n";
  }


  /**
   *
   */
  public void calculateSMSsS1() {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSMSsS1(saFunction,faVal,fvVal,siteClass);
    addDataInfo(function.getInfo());
  }


  /**
   *
   *
   */
  public void calculatedSDSsS1() {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSDSsS1(saFunction, faVal,
        fvVal,siteClass);
    addDataInfo(function.getInfo());
  }


  /**
   *
   *
   */
  public void calculateMapSpectrum(){
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getMapSpectrum(saFunction);
    addDataInfo(functions.getInfo());
    getFunctionsForMapSpectrum(functions);
  }



  /**
   *
   * @param mapSpectrumFunctions DiscretizedFuncList
   */
  private void getFunctionsForMapSpectrum(DiscretizedFuncList mapSpectrumFunctions){


    int numFunctions = mapSpectrumFunctions.size();

    int i=0;
    for(;i<numFunctions;++i){
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)mapSpectrumFunctions.get(i);
      if(tempFunction.getName().equals(GlobalConstants.MCE_SPECTRUM_SA_Vs_T_GRAPH)){
        mapSpectrumSaTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction =(ArbitrarilyDiscretizedFunc)mapSpectrumFunctions.get(1-i);

    mapSpectrumSaSdFunction= tempSDFunction.getYY_Function(mapSpectrumSaTFunction);
    mapSpectrumSaSdFunction.setName(GlobalConstants.MCE_SPECTRUM_SA_Vs_SD_GRAPH);
    mapSpectrumSaSdFunction.setInfo(metadataForPlots);
    mapSpectrumSaTFunction.setInfo(metadataForPlots);
    mapSpectrumSaSdFunction.setYAxisName(GlobalConstants.SA);
    mapSpectrumSaSdFunction.setXAxisName(GlobalConstants.SD);
    mapSpectrumSaTFunction.setYAxisName(GlobalConstants.SA);
    mapSpectrumSaTFunction.setXAxisName(GlobalConstants.PERIOD_NAME);
  }


  /**
   *
   * @param smSpectrumFunctions DiscretizedFuncList
   */
  private void getFunctionsForSMSpectrum(DiscretizedFuncList smSpectrumFunctions){

    int numFunctions = smSpectrumFunctions.size();
    int i=0;
    for(;i<numFunctions;++i){
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)smSpectrumFunctions.get(i);
      if(tempFunction.getName().equals(GlobalConstants.SITE_MODIFIED_SA_Vs_T_GRAPH)){
        smSpectrumSaTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction =(ArbitrarilyDiscretizedFunc)smSpectrumFunctions.get(1-i);
    smSpectrumSaSdFunction = tempSDFunction.getYY_Function(smSpectrumSaTFunction);
    smSpectrumSaSdFunction.setName(GlobalConstants.SITE_MODIFIED_SA_Vs_SD_GRAPH);
    smSpectrumSaSdFunction.setInfo(metadataForPlots);
    smSpectrumSaTFunction.setInfo(metadataForPlots);
    smSpectrumSaSdFunction.setYAxisName(GlobalConstants.SA);
    smSpectrumSaSdFunction.setXAxisName(GlobalConstants.SD);
    smSpectrumSaTFunction.setYAxisName(GlobalConstants.SA);
    smSpectrumSaTFunction.setXAxisName(GlobalConstants.PERIOD_NAME);

  }

  /**
   *
   * @param sdSpectrumFunctions DiscretizedFuncList
   */
  private void getFunctionsForSDSpectrum(DiscretizedFuncList sdSpectrumFunctions){

    int numFunctions = sdSpectrumFunctions.size();
    int i=0;
    for(;i<numFunctions;++i){
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)sdSpectrumFunctions.get(i);
      if(tempFunction.getName().equals(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_T_GRAPH)){
        sdSpectrumSaTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSMFunction =(ArbitrarilyDiscretizedFunc)sdSpectrumFunctions.get(1-i);
    sdSpectrumSaSdFunction = tempSMFunction.getYY_Function(sdSpectrumSaTFunction);
    sdSpectrumSaSdFunction.setName(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_SD_GRAPH);
    sdSpectrumSaSdFunction.setInfo(metadataForPlots);
    sdSpectrumSaTFunction.setInfo(metadataForPlots);
    sdSpectrumSaSdFunction.setYAxisName(GlobalConstants.SA);
    sdSpectrumSaSdFunction.setXAxisName(GlobalConstants.SD);
    sdSpectrumSaTFunction.setYAxisName(GlobalConstants.SA);
    sdSpectrumSaTFunction.setXAxisName(GlobalConstants.PERIOD_NAME);
  }



  /**
   * Returns the list of functions for plotting.
   * @param isMapSpectrumFunctionNeeded boolean true if user has clicked the map spectrum button
   * @param isSDSpectrumFunctionNeeded boolean true if user has clicked the SD spectrum button
   * @param isSMSpectrumFunctionNeeded boolean true if user has clicked the SM spectrum button
   * @return ArrayList
   */
  public ArrayList getFunctionsToPlotForSA(boolean
                                        isMapSpectrumFunctionNeeded,
                                        boolean isSDSpectrumFunctionNeeded,
                                        boolean isSMSpectrumFunctionNeeded) {

    ArrayList functions = new ArrayList();

    if(isMapSpectrumFunctionNeeded && isSDSpectrumFunctionNeeded &&
       isSMSpectrumFunctionNeeded){
      functions.add(mapSpectrumSaTFunction);
      functions.add(mapSpectrumSaSdFunction);
      functions.add(smSpectrumSaTFunction);
      functions.add(smSpectrumSaSdFunction);
      functions.add(sdSpectrumSaTFunction);
      functions.add(sdSpectrumSaSdFunction);
    }
    else if(isMapSpectrumFunctionNeeded && isSDSpectrumFunctionNeeded){
      functions.add(mapSpectrumSaTFunction);
      functions.add(mapSpectrumSaSdFunction);
      functions.add(sdSpectrumSaTFunction);
      functions.add(sdSpectrumSaSdFunction);
    }
    else if(isSDSpectrumFunctionNeeded && isSMSpectrumFunctionNeeded){
      functions.add(smSpectrumSaTFunction);
      functions.add(smSpectrumSaSdFunction);
      functions.add(sdSpectrumSaTFunction);
      functions.add(sdSpectrumSaSdFunction);
    }
    else if(isMapSpectrumFunctionNeeded && isSMSpectrumFunctionNeeded){
      functions.add(mapSpectrumSaTFunction);
      functions.add(mapSpectrumSaSdFunction);
      functions.add(smSpectrumSaSdFunction);
      functions.add(smSpectrumSaTFunction);
    }
    else if(isMapSpectrumFunctionNeeded){
      functions.add(mapSpectrumSaTFunction);
      functions.add(mapSpectrumSaSdFunction);
    }
    else if(isSDSpectrumFunctionNeeded){
      functions.add(sdSpectrumSaTFunction);
      functions.add(sdSpectrumSaSdFunction);
    }
    else if(isSMSpectrumFunctionNeeded){
      functions.add(smSpectrumSaTFunction);
      functions.add(smSpectrumSaSdFunction);
    }
    return functions;
  }

  /**
   *
   *
   */
  public void calculateSMSpectrum(){
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSMSpectrum(saFunction, faVal,
        fvVal,siteClass);
    addDataInfo(functions.getInfo());
    getFunctionsForSMSpectrum(functions);
  }

  /**
   *
   */
  public void calculateSDSpectrum(){
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSDSpectrum(saFunction, faVal,
        fvVal,siteClass);
    addDataInfo(functions.getInfo());
    getFunctionsForSDSpectrum(functions);
  }

  /**
   * Sets the selected geographic region.
   * @param region String
   */
  public void setRegion(String region) {
    geographicRegion = region;
  }

  /**
   * Sets the selected data edition.
   * @param edition String
   */
  public void setEdition(String edition) {
    dataEdition = edition;
  }

  /**
   * Sets the Fa value.
   * @param fa double
   */
  public void setFa(float fa) {
    faVal = fa;
  }

  /**
   * Sets the Fv value.
   * @param fv double
   */
  public void setFv(float fv) {
    fvVal = fv;
  }


  /**
   * Sets the selected site class
   * @param siteClass String
   */
  public void setSiteClass(String siteClass){
    this.siteClass = siteClass;
  }

  /**
   * Returns the site class
   * @return String
   */
  public String getSelectedSiteClass(){
    return siteClass;
  }
}
