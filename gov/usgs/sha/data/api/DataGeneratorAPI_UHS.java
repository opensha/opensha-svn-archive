package gov.usgs.sha.data.api;

import java.util.*;

import gov.usgs.exceptions.*;


/**
 * <p>Title: DataGeneratorAPI_UHS</p>
 *
 * <p>Description: this interface provides the minimum functionality that a
 * DataGenerator classes must provide for Uniform Hazard Spectra. </p>
 * @author : Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public interface DataGeneratorAPI_UHS {


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
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateUHS(double lat, double lon);

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateUHS(String zipCode) throws ZipCodeErrorException;


  /**
   * Sets the selected site class
   * @param siteClass String
   */
  public void setSiteClass(String siteClass);

  /**
   * Returns the site class
   * @return String
   */
  public String getSelectedSiteClass();


  /**
   * Returns the list of functions for plotting.
   * @param isSDSpectrumFunctionNeeded boolean true if user has clicked the SD spectrum button
   * @param isSMSpectrumFunctionNeeded boolean true if user has clicked the SM spectrum button
   * @return ArrayList
   */
  public ArrayList getFunctionsToPlotForSA(boolean isSDSpectrumFunctionNeeded,
                                           boolean isSMSpectrumFunctionNeeded);


  /**
   * Plots the Periods Vs SA and SD when UHS and approx UHS button are clicked
   * @param isUHSFunctionNeeded boolean
   * @param isApproxUHSFunctionNeeded boolean
   * @return ArrayList
   */
  public ArrayList getFunctionsToPlotForUHS(boolean isUHSFunctionNeeded,
                                            boolean isApproxUHSFunctionNeeded);


  /**
   *
   */
  public void calculateApproxUHS();


  /**
   *
   */
  public void calculateSMSpectrum();


  /**
   *
   */
  public void calculateSDSpectrum();


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
  public void setFa(float fa);

  /**
   * Sets the Fv value.
   * @param fv double
   */
  public void setFv(float fv);

  /**
   * Sets the Spectra type
   * @param spectraType String
   */
  public void setSpectraType(String spectraType);

}
