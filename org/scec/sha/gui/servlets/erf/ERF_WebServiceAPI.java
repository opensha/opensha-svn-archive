package org.scec.sha.gui.servlets.erf;

import org.scec.param.ParameterList;
import org.scec.data.region.GeographicRegion;
import org.scec.data.TimeSpan;
import org.scec.data.Location;
import org.scec.sha.earthquake.ERF_API;

/**
 * <p>Title: ERF_WebServiceAPI </p>
 * <p>Description: Interface to the
 * methods implemented by  ERF_WebService</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface ERF_WebServiceAPI {

  /**
   *
   * @returns the name of the selected ERF
   */
  public String getName();

  /**
   *
   * @returns the AdjustableParameterList for ERF's
   */
  public ParameterList getAdjustableParams();

  /**
   *
   * @returns the Object for the Geographic Region for which this ERF is valid
   */
  public GeographicRegion getApplicableRegion();

  /**
   *
   * @returns the TimeSpan object for the ERF.
   */
  public TimeSpan getTimeSpan();

  /**
   * Checks if the location is lies within the region for which this ERF is valid
   * @return
   */
  public boolean isLocWithinApplicableRegion(Location loc);

  /**
   *
   * @param time : TimeSpan parameter
   * @param param : AdjParamList for the ERF
   * @returns the reference to methods that provide details about the source and ruptures
   */
  public ERF_API getERF_API(TimeSpan time, ParameterList adjParam);


}