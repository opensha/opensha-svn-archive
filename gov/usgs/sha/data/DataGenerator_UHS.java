package gov.usgs.sha.data;

import java.util.*;

import org.scec.data.function.*;
import gov.usgs.exceptions.*;
import gov.usgs.sha.data.api.*;
import gov.usgs.util.*;

/**
 * <p>Title: DataGenerator_UHS</p>
 *
 * <p>Description: This class acts as the data model for the Uniform Hazard Spectra Option. </p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class DataGenerator_UHS
    implements DataGeneratorAPI_UHS {

  //gets the selected region
  protected String geographicRegion;
  //gets the selected edition
  protected String dataEdition;

  protected ArbitrarilyDiscretizedFunc saFunction;
  protected ArbitrarilyDiscretizedFunc saSdFunction;
  protected ArbitrarilyDiscretizedFunc sdTFunction;

  protected float faVal = 1.0f;
  protected float fvVal = 1.0f;
  protected String siteClass = GlobalConstants.SITE_CLASS_B;
  protected ArbitrarilyDiscretizedFunc sdSpectrumSaSdFunction;
  protected ArbitrarilyDiscretizedFunc smSpectrumSaSdFunction;

  protected ArbitrarilyDiscretizedFunc sdSpectrumSaTFunction;
  protected ArbitrarilyDiscretizedFunc smSpectrumSaTFunction;

  private ArbitrarilyDiscretizedFunc pgaFunction;

  //holds all the data and its info in a String format.
  protected String dataInfo = "";

  //metadata to be shown when plotting the curves
  protected String metadataForPlots;

  //sets the selected spectra type
  protected String selectedSpectraType;

  /**
   *
   * @default class constructor
   */
  public void calculateApproxUHS() {

  }

  /**
   *
   * @param smSpectrumFunctions DiscretizedFuncList
   */
  protected void getFunctionsForSMSpectrum(DiscretizedFuncList
                                           smSpectrumFunctions) {

    int numFunctions = smSpectrumFunctions.size();
    int i = 0;
    for (; i < numFunctions; ++i) {
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)
          smSpectrumFunctions.get(i);
      if (tempFunction.getName().equals(GlobalConstants.
                                        SITE_MODIFIED_SA_Vs_T_GRAPH)) {
        smSpectrumSaTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction = (ArbitrarilyDiscretizedFunc)
        smSpectrumFunctions.get(1 - i);
    smSpectrumSaSdFunction = tempSDFunction.getYY_Function(
        smSpectrumSaTFunction);
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
  protected void getFunctionsForSDSpectrum(DiscretizedFuncList
                                           sdSpectrumFunctions) {

    int numFunctions = sdSpectrumFunctions.size();
    int i = 0;
    for (; i < numFunctions; ++i) {
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)
          sdSpectrumFunctions.get(i);
      if (tempFunction.getName().equals(GlobalConstants.
                                        DESIGN_SPECTRUM_SA_Vs_T_GRAPH)) {
        sdSpectrumSaTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSMFunction = (ArbitrarilyDiscretizedFunc)
        sdSpectrumFunctions.get(1 - i);
    sdSpectrumSaSdFunction = tempSMFunction.getYY_Function(
        sdSpectrumSaTFunction);
    sdSpectrumSaSdFunction.setName(GlobalConstants.
                                   DESIGN_SPECTRUM_SA_Vs_SD_GRAPH);
    sdSpectrumSaSdFunction.setInfo(metadataForPlots);
    sdSpectrumSaTFunction.setInfo(metadataForPlots);
    sdSpectrumSaSdFunction.setYAxisName(GlobalConstants.SA);
    sdSpectrumSaSdFunction.setXAxisName(GlobalConstants.SD);
    sdSpectrumSaTFunction.setYAxisName(GlobalConstants.SA);
    sdSpectrumSaTFunction.setXAxisName(GlobalConstants.PERIOD_NAME);
  }

  /**
   *
   *
   */
  public void calculateSMSpectrum() {
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSMSpectrum(saFunction, faVal,
        fvVal, siteClass);
    addDataInfo(functions.getInfo());
    getFunctionsForSMSpectrum(functions);
  }

  /**
   *
   */
  public void calculateSDSpectrum() {
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSDSpectrum(saFunction, faVal,
        fvVal, siteClass);
    addDataInfo(functions.getInfo());
    getFunctionsForSDSpectrum(functions);
  }

  /**
   *
   * @param sdSpectrumFunctions DiscretizedFuncList
   */
  private void getFunctionsForSDT(DiscretizedFuncList functions) {

    int numFunctions = functions.size();
    int i = 0;
    for (; i < numFunctions; ++i) {
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)
          functions.get(i);
      if (tempFunction.getName().equals(GlobalConstants.
                                        UNIFORM_HAZARD_SPECTRUM_NAME + " of " +
                                        GlobalConstants.SA_Vs_T_GRAPH_NAME)) {
        saFunction = tempFunction;
        break;
      }
    }

    sdTFunction = (ArbitrarilyDiscretizedFunc)
        functions.get(1 - i);

    saSdFunction = sdTFunction.getYY_Function(
        saFunction);
    saSdFunction.setName(GlobalConstants.UNIFORM_HAZARD_SPECTRUM_NAME + " of " +
                         GlobalConstants.SA_Vs_SD_GRAPH_NAME);
    saSdFunction.setInfo(metadataForPlots);
    saFunction.setInfo(metadataForPlots);
    saSdFunction.setYAxisName(GlobalConstants.SA);
    saSdFunction.setXAxisName(GlobalConstants.SD);
    saFunction.setYAxisName(GlobalConstants.SA);
    saFunction.setXAxisName(GlobalConstants.PERIOD_NAME);
  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and
   * user specifies zip code for the location.
   *
   * @param zipCode String
   * @throws ZipCodeErrorException
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_UHS method
   */
  public void calculateUHS(String zipCode) throws ZipCodeErrorException {
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList funcList = miner.getSA(geographicRegion, dataEdition,
                                               zipCode, selectedSpectraType);
    String location = "ZipCode - " + zipCode;
    createMetadataForPlots(location);
    addDataInfo(funcList.getInfo());
    getFunctionsForSDT(funcList);

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and
   * user specifies Lat-Lon for the location.
   *
   * @param lat double
   * @param lon double
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_UHS method
   */
  public void calculateUHS(double lat, double lon) {

    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList funcList = miner.getSA(geographicRegion, dataEdition,
                                               lat, lon, selectedSpectraType);
    String location = "Lat - " + lat + "  Lon - " + lon;
    createMetadataForPlots(location);
    addDataInfo(funcList.getInfo());
    getFunctionsForSDT(funcList);
  }

  protected void createMetadataForPlots(String location) {
    metadataForPlots = GlobalConstants.SA_DAMPING + "\n";
    metadataForPlots += geographicRegion + "\n";
    metadataForPlots += dataEdition + "\n";
    metadataForPlots += location + "\n";
    metadataForPlots += "Site Class -" + siteClass + "\n";
    metadataForPlots += "Fa = " + faVal + " Fv = " + fvVal + "\n";
  }

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

  protected void addDataInfo(String data) {
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo += data + "\n\n";
  }



  /**
   * Returns the list of functions for plotting.
   * @param isSDSpectrumFunctionNeeded boolean true if user has clicked the SD
   *   spectrum button
   * @param isSMSpectrumFunctionNeeded boolean true if user has clicked the SM
   *   spectrum button
   * @return ArrayList
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_UHS method
   */
  public ArrayList getFunctionsToPlotForSA(boolean isSDSpectrumFunctionNeeded,
                                           boolean isSMSpectrumFunctionNeeded) {
    return null;
  }

  /**
   *
   * @param isUHSFunctionNeeded boolean
   * @param isApproxUHSFunctionNeeded boolean
   * @return ArrayList
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_UHS method
   */
  public ArrayList getFunctionsToPlotForUHS(boolean isUHSFunctionNeeded,
                                            boolean isApproxUHSFunctionNeeded) {
    ArrayList functions = new ArrayList();
    if(isUHSFunctionNeeded && isApproxUHSFunctionNeeded){
      functions.add(saFunction);
      functions.add(saSdFunction);
    }
    else if(isUHSFunctionNeeded && !isApproxUHSFunctionNeeded){
      functions.add(saFunction);
      functions.add(saSdFunction);
    }

    return functions;
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
  public void setSiteClass(String siteClass) {
    this.siteClass = siteClass;
  }

  /**
   * Returns the site class
   * @return String
   */
  public String getSelectedSiteClass() {
    return siteClass;
  }

  /**
   * Sets the Spectra type
   * @param spectraType String
   */
  public void setSpectraType(String spectraType) {
    selectedSpectraType = spectraType;
  }
}
