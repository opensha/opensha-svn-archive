package org.opensha.nshmp.sha.data;

import java.rmi.*;
import java.util.*;

import org.opensha.data.function.*;
import org.opensha.nshmp.exceptions.*;
import org.opensha.nshmp.sha.data.api.*;
import org.opensha.nshmp.util.*;

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
  protected String geographicRegion;
  //gets the selected edition
  protected String dataEdition;

  protected ArbitrarilyDiscretizedFunc saFunction;

  protected float faVal = 1.0f;
  protected float fvVal = 1.0f;
  protected String siteClass = GlobalConstants.SITE_CLASS_B;
  protected ArbitrarilyDiscretizedFunc sdSpectrumSaSdFunction;
  protected ArbitrarilyDiscretizedFunc smSpectrumSaSdFunction;
  protected ArbitrarilyDiscretizedFunc mapSpectrumSaSdFunction;
  protected ArbitrarilyDiscretizedFunc sdSpectrumSaTFunction;
  protected ArbitrarilyDiscretizedFunc smSpectrumSaTFunction;
  protected ArbitrarilyDiscretizedFunc mapSpectrumSaTFunction;

  //holds all the data and its info in a String format.
  protected String dataInfo = "";

  //metadata to be shown when plotting the curves
  protected String metadataForPlots;

  //sets the selected spectra type
  protected String selectedSpectraType;

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

  protected void addDataInfo(String data) {
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo += data + "\n\n";
  }

  /**
   * Returns the SA at .2sec
   * @return double
   */
  public double getSs() {
    return saFunction.getY(0);
  }

  /**
   * Returns the SA at 1 sec
   * @return double
   */
  public double getSa() {
    return saFunction.getY(1);
  }

  /**
   * Gets the data for SsS1 in case Territory.
   * Territory is when user is not allowed to enter any zip code or Lat-Lon
   * for the location or if it is GAUM and TAUTILLA.
   */
  public void calculateSsS1() throws RemoteException {

    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion);
    String location = "Spectral values are constant for the region";
    createMetadataForPlots(location);
    addDataInfo(function.getInfo());
    saFunction = function;

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon) throws RemoteException {

    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion,
        dataEdition,
        lat, lon);
    String location = "Lat - " + lat + "  Lon - " + lon;
    createMetadataForPlots(location);
    addDataInfo(function.getInfo());
    saFunction = function;

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException,
      RemoteException {

    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion,
        dataEdition,
        zipCode);
    String location = "Zipcode - " + zipCode;
    createMetadataForPlots(location);
    addDataInfo(function.getInfo());
    saFunction = function;
  }

  protected void createMetadataForPlots(String location) {
    metadataForPlots = GlobalConstants.SA_DAMPING + "\n";
    metadataForPlots += geographicRegion + "\n";
    metadataForPlots += dataEdition + "\n";
    metadataForPlots += location + "\n";
  }

  /**
   *
   */
  public void calculateSMSsS1() throws RemoteException {

    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getSMSsS1(saFunction, faVal,
        fvVal, siteClass);
    addDataInfo(function.getInfo());
  }

  /**
   *
   *
   */
  public void calculatedSDSsS1() throws RemoteException {

    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getSDSsS1(saFunction, faVal,
        fvVal, siteClass);
    addDataInfo(function.getInfo());
  }

  /**
   *
   *
   */
  public void calculateMapSpectrum() throws RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    DiscretizedFuncList functions = miner.getMapSpectrum(saFunction);
    addDataInfo(functions.getInfo());
    getFunctionsForMapSpectrum(functions);
  }

  /**
   *
   * @param mapSpectrumFunctions DiscretizedFuncList
   */
  protected void getFunctionsForMapSpectrum(DiscretizedFuncList
                                            mapSpectrumFunctions) {

    int numFunctions = mapSpectrumFunctions.size();

    int i = 0;
    for (; i < numFunctions; ++i) {
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)
          mapSpectrumFunctions.get(i);
      if (tempFunction.getName().equals(GlobalConstants.
                                        MCE_SPECTRUM_SA_Vs_T_GRAPH)) {
        mapSpectrumSaTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction = (ArbitrarilyDiscretizedFunc)
        mapSpectrumFunctions.get(1 - i);

    mapSpectrumSaSdFunction = tempSDFunction.getYY_Function(
        mapSpectrumSaTFunction);
    mapSpectrumSaSdFunction.setName(GlobalConstants.MCE_SPECTRUM_SA_Vs_SD_GRAPH);
    String info = metadataForPlots;
    info += "Site Class -" + siteClass + "\n";
    info += "Fa = " + faVal + " Fv = " + fvVal + "\n";

    mapSpectrumSaSdFunction.setInfo(info);
    mapSpectrumSaTFunction.setInfo(info);
    mapSpectrumSaSdFunction.setYAxisName(GlobalConstants.SA);
    mapSpectrumSaSdFunction.setXAxisName(GlobalConstants.SD);
    mapSpectrumSaTFunction.setYAxisName(GlobalConstants.SA);
    mapSpectrumSaTFunction.setXAxisName(GlobalConstants.PERIOD_NAME);
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
    String info = metadataForPlots;
    info += "Site Class -" + siteClass + "\n";
    info += "Fa = " + faVal + " Fv = " + fvVal + "\n";

    smSpectrumSaSdFunction.setInfo(info);
    smSpectrumSaTFunction.setInfo(info);
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
    String info = metadataForPlots;
    info += "Site Class -" + siteClass + "\n";
    info += "Fa = " + faVal + " Fv = " + fvVal + "\n";
    sdSpectrumSaSdFunction.setInfo(info);
    sdSpectrumSaTFunction.setInfo(info);
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

    if (isMapSpectrumFunctionNeeded) {
      functions.add(mapSpectrumSaTFunction);
      functions.add(mapSpectrumSaSdFunction);
    }
    if (isSDSpectrumFunctionNeeded) {
      functions.add(sdSpectrumSaTFunction);
      functions.add(sdSpectrumSaSdFunction);
    }
    if (isSMSpectrumFunctionNeeded) {
      functions.add(smSpectrumSaTFunction);
      functions.add(smSpectrumSaSdFunction);
    }
    return functions;
  }

  /**
   *
   *
   */
  public void calculateSMSpectrum() throws RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    DiscretizedFuncList functions = miner.getSMSpectrum(saFunction, faVal,
        fvVal, siteClass);
    addDataInfo(functions.getInfo());
    getFunctionsForSMSpectrum(functions);
  }

  /**
   *
   */
  public void calculateSDSpectrum() throws RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    DiscretizedFuncList functions = miner.getSDSpectrum(saFunction, faVal,
        fvVal, siteClass);
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
