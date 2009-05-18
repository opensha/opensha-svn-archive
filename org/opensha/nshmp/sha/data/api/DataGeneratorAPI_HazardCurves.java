package org.opensha.nshmp.sha.data.api;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.nshmp.exceptions.ZipCodeErrorException;

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
  public void calculateHazardCurve(double lat, double lon,
                                   String selectedHazCurveType) throws
      RemoteException;

  /**
   * Gets the data for Hazard Curve in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateHazardCurve(String zipCode, String selectedHazCurveType) throws
      ZipCodeErrorException, RemoteException;

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
   * Calculates the single value Hazard Curve when user provides with the return period
   * @param returnPeriod double
   */
  public void calcSingleValueHazardCurveUsingReturnPeriod(double returnPeriod,
      boolean logInterpolation) throws RemoteException;

  public void calcSingleValueHazard(ArrayList<Location> locations, String imt,
		  String outFile, double period, boolean logScale) throws RemoteException;
  
  public void calcSingleValueHazard(ArrayList<Location> locations, String imt,
		  String outFile, double prob, double time, boolean logScale) throws RemoteException;
  
  /**
   * Returns the Calculated Hazard curve function
   * @return ArrayList
   */
  public ArrayList getHazardCurveFunction();

  /**
   * Calcutes the single value Hazard curve when user provides with the prob exceedance and
   * Exposure time.
   * @param probExceedProb double
   * @param expTime double
   */
  public void calcSingleValueHazardCurveUsingPEandExptime(double probExceedProb,
      double expTime, boolean logInterpolation) throws RemoteException;

  /**
   * Calculate the hazard curve over an array of locations
   * @param locations
   * @param outputFile
   */
  public void calculateHazardCurve(ArrayList<Location> locations, String imt, String outputFile);

public void calcSingleValueFEX(ArrayList<Location> locations, String imt,
		String outputFile, double groundMotionVal, boolean isLogInterpolation) throws RemoteException;

public void calcSingleValueFEXUsingGroundMotion(double groundMotionVal,
		boolean isLogInterpolation) throws RemoteException;
}
