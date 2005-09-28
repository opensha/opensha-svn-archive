package javaDevelopers.matt.calc;

import java.util.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.Location;
import java.io.IOException;
import java.io.FileNotFoundException;

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
public class STEP_main {
  private RegionDefaults rDefs;

  public STEP_main() {
    // get various default values for the region.
    RegionDefaults rDefs = new RegionDefaults();

     calc_STEP();
  }



  /**
   * calc_STEP
   */
  public void calc_STEP() {
    /**
     * First load the active aftershock sequence objects from the last run
     * load: ActiveSTEP_AIC_AftershockForecastList
     * each object is a STEP_AftershockHypoMagFreqDistForecast
     */
    ArrayList STEP_AftershockForecastList = null;
    ArrayList New_AftershockForecastList = null;

    /**
     * Now obtain all events that have occurred since the last time the code
     * was run:
     * NewObsEqkRuptureList
     */
    CubeToObsEqkRupture getNewEvents = null;
    try {
      getNewEvents = new CubeToObsEqkRupture(rDefs.cubeFilePath);
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    ObsEqkRupList newObsEqkRuptureList = getNewEvents.getAllObsEqkRupEvents();



    /**
     * load background rates/grid list
     * BackgroundRatesList
     */

    EvenlyGriddedGeographicRegionAPI backGroundRatesGrid = null;

    /**
     * now loop over all new events and assign them as an aftershock to
     * a previous event if appropriate (loop thru all existing mainshocks)
     */
    ObsEqkRupture newEvent, mainshock;
    STEP_AftershockForecast mainshockModel, foundMsModel, staticModel;
    ListIterator newIt = newObsEqkRuptureList.listIterator();
    boolean isAftershock = false;
    int indexNum, maxMagInd = -1;
    int numMainshocks = STEP_AftershockForecastList.size();
    double maxMag = 0, msMag, newMag;

    while (newIt.hasNext()) {
      newEvent = (ObsEqkRupture) newIt.next();
      newMag = newEvent.getMag();

      for (int msLoop = 0; msLoop < numMainshocks; ++msLoop) {
        mainshockModel =
            (STEP_AftershockForecast)STEP_AftershockForecastList.get(msLoop);
        mainshock = mainshockModel.getMainShock();
        msMag = mainshock.getMag();

        // returns boolean if event is in zone, but does not set anything
        IsAftershockToMainshock_Calc seeIfAftershock =
            new IsAftershockToMainshock_Calc(newEvent, mainshockModel);
        isAftershock = seeIfAftershock.get_isAftershock();

        if (isAftershock) {
          // if the new event is larger than the mainshock, make the mainshock
          // static so that it will no longer accept aftershocks.
          if (newMag >= msMag) {
            mainshockModel.set_isStatic(true);
          }

          /**
          * to be a mainshock an eveny must be most recent and largest "mainshock"
          * with this new event as an aftershock.
          * Check to see if this mainshock is the largest mainshock for this event
          * (it will be the newest as the ms are in chrono order) if it is, keep
          * the index for the mainshock so we can add the aftershock later.
          * Also any older mainshock that had this new event as an aftershock
          * should be set to static (as the aftershock zones apparently overlap)
          */
          if (msMag > maxMag) {
            if (maxMagInd > -1){
              staticModel =
            (STEP_AftershockForecast)STEP_AftershockForecastList.get(msLoop);
              staticModel.set_isStatic(true);
            }
            maxMagInd = msLoop;
          }
        }
      }
      // now add the new event to the aftershock list of the largest appropriate
      // mainshock - if one has been found
      if (maxMagInd > -1) {
        foundMsModel =
            (STEP_AftershockForecast)STEP_AftershockForecastList.get(maxMagInd);
        foundMsModel.addToAftershockList(newEvent);
      }

      // add the new event to the list of mainshocks if it is greater than
      // magnitude 3.0 (or what ever mag is defined)
      if (newMag >= rDefs.minMagForMainshock) {
      STEP_AftershockForecast newGenForecastMod =
           new GenericAfterHypoMagFreqDistForecast(newEvent,backGroundRatesGrid,rDefs);

        // if the new event is already an aftershock to something else
        // set it as a secondary event.
        if (isAftershock) {
          newGenForecastMod.set_isPrimary(false);
        }
        else {
          newGenForecastMod.set_isPrimary(true);
        }


        // add the new event to the list of mainshocks and increment the number
        // of total mainshocks (for the loop)
        STEP_AftershockForecastList.add(newGenForecastMod);
        ++numMainshocks;

      }
    }

    /**
     * Next loop over the list of all forecast model objects and create
     * a forecast for each object
     */

    int numAftershockModels = STEP_AftershockForecastList.size();
    GenericAfterHypoMagFreqDistForecast forecastModel;

    for (int modelLoop = 0; modelLoop < numAftershockModels; ++modelLoop){
      forecastModel =
          (GenericAfterHypoMagFreqDistForecast)STEP_AftershockForecastList.get(modelLoop);
      //UpdateSTEP_Forecast(forecastModel,backGroundRatesGrid);
    }



  }

}
