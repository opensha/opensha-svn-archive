package javaDevelopers.matt.calc;

import org.opensha.data.*;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.sha.earthquake.observedEarthquake.*;

/**
 * <p>Title: </p>
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
public class IsAftershockToMainshock_Calc {
  private ObsEqkRupture newEvent;
  private STEP_AftershockForecast mainshockModel;
  private boolean isInZone;

  public IsAftershockToMainshock_Calc(ObsEqkRupture newEvent,
                                      STEP_AftershockForecast
                                      mainshockModel) {
    Calc_IsAftershockToMainshock();
  }

  /**
   * get_isAftershock
   */
  public boolean get_isAftershock() {
    return isInZone;
  }

  /**
   * Calc_IsAftershockToPrev
   */
  private boolean Calc_IsAftershockToMainshock() {
    boolean isInZone;
    EvenlyGriddedGeographicRegionAPI aftershockZone;
    aftershockZone = mainshockModel.getAfterShockZone();
    ObsEqkRupture mainshock = mainshockModel.getMainShock();
    Location newEventLoc = newEvent.getHypocenterLocation();
    boolean isStatic = mainshockModel.get_isStatic();

    // if this event is not accepting aftershocks any more stop
    // here and return a false
    if (isStatic)
      isInZone = false;

    // if it is accepting look and see if this event fits
    else {
      isInZone = aftershockZone.isLocationInside(newEventLoc);

      if (isInZone) {
        //if ( (double) mainshock.getMag() > (double) newEvent.getMag()) {
        //mainshockModel.addToAftershockList(newEvent);
      }
      else {
        mainshockModel.set_isStatic(true);
      }
    }

    return isInZone;
  }
}

