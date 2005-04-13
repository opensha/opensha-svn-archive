package gov.usgs.sha.data;

import java.rmi.*;
import java.text.*;
import java.util.*;

import org.opensha.data.function.*;
import gov.usgs.exceptions.*;
import gov.usgs.sha.data.api.*;
import gov.usgs.util.*;

/**
 * <p>Title: DataGenerator_HazardCurves</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DataGenerator_HazardCurves
    implements DataGeneratorAPI_HazardCurves {

  //gets the selected region
  private String geographicRegion;
  //gets the selected edition
  private String dataEdition;

  //holds all the data and its info in a String format.
  private String dataInfo = "";

  //metadata to be shown when plotting the curves
  private String metadataForPlots;

  private ArbitrarilyDiscretizedFunc hazardCurveFunction;

  private final static double EXP_TIME = 50.0;

  private static DecimalFormat percentageFormat = new DecimalFormat("0.00");
  private static DecimalFormat saValFormat = new DecimalFormat("0.0000");
  private static DecimalFormat annualExceedanceFormat = new DecimalFormat(
      "0.000E00#");

  public DataGenerator_HazardCurves() {
  }

  /**
   * Gets the data for Hazard Curve in case region specified is not a Territory
   * and user specifies Lat-Lon for the location.
   *
   * @param lat double
   * @param lon double
   * @return ArrayList
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void calculateHazardCurve(double lat, double lon, String hazCurveType) throws
      RemoteException {
    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getBasicHazardcurve(
        geographicRegion, dataEdition,
        lat, lon, hazCurveType);
    String location = "Lat - " + lat + "  Lon - " + lon;
    createMetadataForPlots(location, hazCurveType);
    addDataInfo(function.getInfo());
    function.setInfo(metadataForPlots);
    function.setName(GlobalConstants.BASIC_HAZARD_CURVE);
    function.setXAxisName(GlobalConstants.HAZARD_CURVE_X_AXIS_NAME + "(" +
                          GlobalConstants.SA_UNITS + ")");
    function.setYAxisName(GlobalConstants.HAZARD_CURVE_Y_AXIS_NAME);
    hazardCurveFunction = function;
  }

  /**
   * Gets the data for Hazard Curve in case region specified is not a Territory
   * and user specifies zip code for the location.
   *
   * @param zipCode String
   * @throws ZipCodeErrorException
   * @return ArrayList
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void calculateHazardCurve(String zipCode, String hazCurveType) throws
      ZipCodeErrorException, RemoteException {
    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getBasicHazardcurve(
        geographicRegion, dataEdition,
        zipCode, hazCurveType);
    String location = "Zipcode - " + zipCode;
    createMetadataForPlots(location, hazCurveType);
    addDataInfo(function.getInfo());
    function.setInfo(metadataForPlots);
    function.setName(GlobalConstants.BASIC_HAZARD_CURVE);
    function.setXAxisName(GlobalConstants.HAZARD_CURVE_X_AXIS_NAME + "(" +
                          GlobalConstants.SA_UNITS + ")");
    function.setYAxisName(GlobalConstants.HAZARD_CURVE_Y_AXIS_NAME);
    hazardCurveFunction = function;
  }

  public ArrayList getHazardCurveFunction() {
    ArrayList functionList = new ArrayList();
    functionList.add(hazardCurveFunction);
    return functionList;
  }

  private void createMetadataForPlots(String location, String hazCurveType) {
    metadataForPlots = hazCurveType + "\n";
    metadataForPlots += geographicRegion + "\n";
    metadataForPlots += dataEdition + "\n";
    metadataForPlots += location + "\n";
  }

  public void calcSingleValueHazardCurveUsingReturnPeriod(double returnPeriod,
      boolean logInterpolation) throws RemoteException {
    HazardDataMiner miner = new HazardDataMiner();
    double fex = 1 / returnPeriod;
    double exceedProb = miner.getExceedProb(fex, EXP_TIME);
    double saVal = 0.0;
    if (logInterpolation) {
      saVal = hazardCurveFunction.getFirstInterpolatedX_inLogXLogYDomain(fex);
    }
    else {
      saVal = hazardCurveFunction.getFirstInterpolatedX(fex);
    }
    addDataFromSingleHazardCurveValue(fex, returnPeriod, exceedProb, EXP_TIME,
                                      saVal);
  }

  public void calcSingleValueHazardCurveUsingPEandExptime(double probExceed,
      double expTime, boolean logInterpolation) throws RemoteException {
    HazardDataMiner miner = new HazardDataMiner();
    double returnPd = miner.getReturnPeriod(probExceed, expTime);
    double fex = 1 / returnPd;
    double saVal = 0.0;
    if (logInterpolation) {
      saVal = hazardCurveFunction.getFirstInterpolatedX_inLogXLogYDomain(fex);
    }
    else {
      saVal = hazardCurveFunction.getFirstInterpolatedX(fex);
    }
    addDataFromSingleHazardCurveValue(fex, returnPd, probExceed, expTime, saVal);
  }

  private void addDataFromSingleHazardCurveValue(double fex, double returnPd,
                                                 double probExceed,
                                                 double expTime,
                                                 double groundMotion) {
    dataInfo += "\n\n";
    dataInfo +=
        "Ground Motion \t Freq. of Exceed. \t Return Pd. \t P.E. \t Exp.time \n";
    dataInfo += "(g)\t\t(per year)\t\t(years)\t%\t(years)\n";
    dataInfo += saValFormat.format(groundMotion) + "\t\t" +
        annualExceedanceFormat.format(fex) +
        "\t\t" + returnPd + "\t" + percentageFormat.format(probExceed) + "\t" +
        expTime + "\n";
  }

  /**
   * Removes all the calculated data.
   *
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void clearData() {
    dataInfo = "";
  }

  private void addDataInfo(String data) {
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo += data + "\n\n";
  }

  /**
   * Returns the Data and all the metadata associated with it in a String.
   *
   * @return String
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public String getDataInfo() {
    return dataInfo;
  }

  /**
   * Sets the selected data edition.
   *
   * @param edition String
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void setEdition(String edition) {
    dataEdition = edition;
  }

  /**
   * Sets the selected geographic region.
   *
   * @param region String
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void setRegion(String region) {
    geographicRegion = region;
  }
}
