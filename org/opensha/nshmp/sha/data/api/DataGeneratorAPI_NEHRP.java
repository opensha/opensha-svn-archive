package org.opensha.nshmp.sha.data.api;

import java.rmi.*;
import java.util.*;

import org.opensha.nshmp.exceptions.*;

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
   * Removes all the calculated data.
   */
  public void clearData();

  /**
   * Clears the lat/lon/zip values
	*/
  public void setNoLocation();

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
  public void calculateSsS1() throws RemoteException;

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon) throws RemoteException;

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException,
      RemoteException;

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
   * @param isMapSpectrumFunctionNeeded boolean true if user has clicked the map spectrum button
   * @param isSDSpectrumFunctionNeeded boolean true if user has clicked the SD spectrum button
   * @param isSMSpectrumFunctionNeeded boolean true if user has clicked the SM spectrum button
   * @return ArrayList
   */
  public ArrayList getFunctionsToPlotForSA(boolean
                                           isMapSpectrumFunctionNeeded,
                                           boolean isSDSpectrumFunctionNeeded,
                                           boolean isSMSpectrumFunctionNeeded);

  /**
   * Returns the SA at .2sec
   * @return double
   */
  public double getSs();

  /**
   * Returns the SA at 1 sec
   * @return double
   */
  public double getSa();

  /**
   *
   */
  public void calculateSMSsS1() throws RemoteException;

  /**
   *
   */
  public void calculatedSDSsS1() throws RemoteException;

  /**
   *
   */
  public void calculateMapSpectrum() throws RemoteException;

  /**
   *
   */
  public void calculateSMSpectrum() throws RemoteException;

  /**
   *
   */
  public void calculateSDSpectrum() throws RemoteException;

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
