package gov.usgs.sha.data.api;

import gov.usgs.exceptions.*;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;


/**
 * <p>Title: DataGeneratorAPI_HazardCurves</p>
 *
 * <p>Description: this interface provides the minimum functionality that a
 * DataGenerator class must provide for retriving the Basic Hazard Curves.</p>
 *
 * @author : Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public interface DataGeneratorAPI_HazardCurves {


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
   * Gets the data for Hazard Curve in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public ArbitrarilyDiscretizedFunc calculateHazardCurve(double lat, double lon,String selectedHazCurveType);

  /**
   * Gets the data for Hazard Curve in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public ArbitrarilyDiscretizedFunc calculateHazardCurve(String zipCode,String selectedHazCurveType) throws ZipCodeErrorException;

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


}
