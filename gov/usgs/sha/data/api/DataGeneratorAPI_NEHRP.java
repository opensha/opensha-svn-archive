package gov.usgs.sha.data.api;

import java.util.*;

import gov.usgs.exceptions.*;

/**
 * <p>Title: DataGeneratorAPI_NEHRP</p>
 *
 * <p>Description: this interface provides the minimum functionality that a
 * DataGenerator classes must provide. </p>
 * @author : Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public interface DataGeneratorAPI_NEHRP {

  /**
   * Returns the calculated data as individual Arbitrary Discretized function
   * in a ArrayList.
   * @return ArrayList
   */
  public ArrayList getData();

  /**
   * Removes all the calculated data.
   */
  public void clearData();

  /**
   * Returns the Data and all the metadata associated with it in a String.
   * @return String
   */
  public String getDataInfo();

  /**
   * Gets the data for SsS1 in case Territory.
   * Territory is when user is not allowed to enter any zip code or Lat-Lon
   * for the location or if it is GAUM and TAUTILLA.
   */
  public void calculateSsS1();

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon);

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException;

  /**
   * Sets the selected geographic region.
   * @param region String
   */
  public void setRegion(String region);

  /**
   * Sets the selected data edition.
   * @param edition String
   */
  public void setEdition(String edition);

  /**
   * Sets the Fa value.
   * @param fa double
   */
  public void setFa(double fa);

  /**
   * Sets the Fv value.
   * @param fv double
   */
  public void setFv(double fv);

}
