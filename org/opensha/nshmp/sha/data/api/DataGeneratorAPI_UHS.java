package org.opensha.nshmp.sha.data.api;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.nshmp.exceptions.ZipCodeErrorException;

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
  public void calculateUHS(double lat, double lon) throws RemoteException;

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateUHS(String zipCode) throws ZipCodeErrorException,
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
   * @param isUHSFunctionNeeded boolean
   * @param isApproxUHSFunctionNeeded boolean
   * @param isSDSpectrumFunctionNeeded boolean
   * @param isSMSpectrumFunctionNeeded boolean
   * @return ArrayList
   */
  public ArrayList getFunctionsToPlotForSA(boolean isUHSFunctionNeeded,
                                           boolean isApproxUHSFunctionNeeded,
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
  public void calculateApproxUHS() throws RemoteException;

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

  /**
   * Computes the UHS for an array of locations
   * 
   * @param locations
   * @param outFile
   */
  public void calculateUHS(ArrayList<Location> locations, String outFile);

}
