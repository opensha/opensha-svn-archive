package gov.usgs.sha.data;

import gov.usgs.sha.data.api.DataGeneratorAPI_NEHRP;
import java.util.ArrayList;
import gov.usgs.exceptions.ZipCodeErrorException;
import org.scec.data.function.DiscretizedFuncList;

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
  //hold the Arbitrary Discritized function list
  private ArrayList functionsList = new ArrayList();

  private double faVal;
  private double fvVal;

  //holds all the data and its info in a String format.
  private String dataInfo = "";

  /**
   * Default class constructor
   */
  public DataGenerator_NEHRP() {}

  /**
   * Returns the calculated data as individual Arbitrary Discretized function
   * in a ArrayList.
   * @return ArrayList
   */

  public ArrayList getData() {
    return functionsList;
  }

  /**
   * Removes all the calculated data.
   */
  public void clearData() {
    functionsList.clear();
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
   * Gets the data for SsS1 in case Territory.
   * Territory is when user is not allowed to enter any zip code or Lat-Lon
   * for the location or if it is GAUM and TAUTILLA.
   */
  public void calculateSsS1() {
    //creates the instance of the Ss and S1
    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion);
    addDataInfo(function.getInfo());
    functionsList.add(function);

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon) {
    //creates the instance of the Ss and S1
    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                 lat, lon);
    addDataInfo(function.getInfo());
    functionsList.add(function);

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException {
    //creates the instance of the Ss and S1
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSsS1(geographicRegion, dataEdition,
                                                 zipCode);
    addDataInfo(functions.getInfo());
    int numFunctions = functions.size();
    for (int i = 0; i < numFunctions; ++i)
      functionsList.add(functions.get(i));

  }


  /**
   *
   */
  public void calculateSMSsS1(){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();

  }


  /**
   *
   */
  public void calculatedSDSsS1(){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
  }


  /**
   *
   */
  public void approxSaSd(){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
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
  public void setFa(double fa) {
    faVal = fa;
  }

  /**
   * Sets the Fv value.
   * @param fv double
   */
  public void setFv(double fv) {
    fvVal = fv;
  }
}
