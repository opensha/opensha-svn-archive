package gov.usgs.sha.data;

import java.util.*;

import gov.usgs.exceptions.*;
import gov.usgs.sha.data.api.*;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import gov.usgs.util.GlobalConstants;

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
  public ArbitrarilyDiscretizedFunc calculateHazardCurve(double lat, double lon,String hazCurveType) {
    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getBasicHazardcurve(geographicRegion,dataEdition,
        lat,lon,hazCurveType);
    String location = "Lat - "+lat+"  Lon - "+lon;
    createMetadataForPlots(location,hazCurveType);
    addDataInfo(function.getInfo());
    function.setInfo(metadataForPlots);
    function.setName(GlobalConstants.BASIC_HAZARD_CURVE);
    function.setXAxisName(GlobalConstants.HAZARD_CURVE_X_AXIS_NAME+"("+GlobalConstants.SA_UNITS+")");
    function.setYAxisName(GlobalConstants.HAZARD_CURVE_Y_AXIS_NAME);

    return function;
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
  public ArbitrarilyDiscretizedFunc calculateHazardCurve(String zipCode,String hazCurveType) throws
      ZipCodeErrorException {
    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getBasicHazardcurve(geographicRegion,dataEdition,
        zipCode,hazCurveType);
    String location = "Zipcode - "+zipCode;
    createMetadataForPlots(location,hazCurveType);
    addDataInfo(function.getInfo());
    function.setInfo(metadataForPlots);
    function.setName(GlobalConstants.BASIC_HAZARD_CURVE);
    function.setXAxisName(GlobalConstants.HAZARD_CURVE_X_AXIS_NAME+"("+GlobalConstants.SA_UNITS+")");
    function.setYAxisName(GlobalConstants.HAZARD_CURVE_Y_AXIS_NAME);
    return function;
  }



  private void createMetadataForPlots(String location,String hazCurveType){
    metadataForPlots =  hazCurveType+"\n";
    metadataForPlots += geographicRegion + "\n";
    metadataForPlots += dataEdition + "\n";
    metadataForPlots += location +"\n";
  }



  /**
   * Removes all the calculated data.
   *
   * @todo Implement this gov.usgs.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void clearData() {
    dataInfo ="";
  }

  private void addDataInfo(String data){
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo +=data+"\n\n";
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
