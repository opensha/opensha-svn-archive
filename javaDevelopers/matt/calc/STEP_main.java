package javaDevelopers.matt.calc;

import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.Location;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.opensha.sha.magdist.*;

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
  private BackGroundRatesGrid bgGrid;
  private GregorianCalendar currentTime;

  public STEP_main() {
   
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
      getNewEvents = new CubeToObsEqkRupture(RegionDefaults.cubeFilePath);
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    ObsEqkRupList newObsEqkRuptureList = getNewEvents.getAllObsEqkRupEvents();
    
    
    /**
     * 
     * this sets the forecast start time as the current time.
     */
    
      Calendar curTime = new GregorianCalendar(TimeZone.getTimeZone(
          "UTC"));
      int year = curTime.get(Calendar.YEAR);
      int month = curTime.get(Calendar.MONTH);
      int day = curTime.get(Calendar.DAY_OF_MONTH);
      int hour24 = curTime.get(Calendar.HOUR_OF_DAY);
      int min = curTime.get(Calendar.MINUTE);
      int sec = curTime.get(Calendar.SECOND);

      this.currentTime = new GregorianCalendar(year, month,
          day, hour24, min, sec);

    /**
     * load background rates/grid list
     * BackgroundRatesList
     */
    
    bgGrid = new BackGroundRatesGrid();
    
    /**
     * now loop over all new events and assign them as an aftershock to
     * a previous event if appropriate (loop thru all existing mainshocks)
     */
    ObsEqkRupture newEvent, mainshock;
    STEP_CombineForecastModels mainshockModel, foundMsModel, staticModel;
    ListIterator newIt = newObsEqkRuptureList.listIterator();
    boolean isAftershock = false;
    int indexNum, maxMagInd = -1;
    int numMainshocks = STEP_AftershockForecastList.size();
    double maxMag = 0, msMag, newMag;

    // loop over new events
    while (newIt.hasNext()) {
      newEvent = (ObsEqkRupture) newIt.next();
      newMag = newEvent.getMag();

      //loop over existing mainshocks
      for (int msLoop = 0; msLoop < numMainshocks; ++msLoop) {
        mainshockModel =
            (STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);
        mainshock = mainshockModel.getMainShock();
        msMag = mainshock.getMag();
        
        // update the current time (as defined at the start of STEP_Main)
        // in this mainshock while we're at it.
        mainshockModel.set_CurrentTime(currentTime);

        // returns boolean if event is in aftershockzone, but does not set anything
        IsAftershockToMainshock_Calc seeIfAftershock =
            new IsAftershockToMainshock_Calc(newEvent, mainshockModel);
        if (seeIfAftershock.get_isAftershock()) {
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
            (STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);
              staticModel.set_isStatic(true);
            }
            // set the index and mag of the new ms so it can be compared against
            // Correct?!?!
            maxMagInd = msLoop;
            maxMag = msMag;
          }
        }
      }
      // now add the new event to the aftershock list of the largest appropriate
      // mainshock - if one has been found
      if (maxMagInd > -1) {
        foundMsModel =
            (STEP_CombineForecastModels)STEP_AftershockForecastList.get(maxMagInd);
        foundMsModel.addToAftershockList(newEvent);
      }

      // add the new event to the list of mainshocks if it is greater than
      // magnitude 3.0 (or what ever mag is defined)
      if (newMag >= RegionDefaults.minMagForMainshock) {
      STEP_CombineForecastModels newForecastMod =
           new STEP_CombineForecastModels(newEvent,this.bgGrid,this.currentTime);

        // if the new event is already an aftershock to something else
        // set it as a secondary event.  Default is true
        if (isAftershock) {
          newForecastMod.set_isPrimary(false);
        }
        
        // add the new event to the list of mainshocks and increment the number
        // of total mainshocks (for the loop)
        STEP_AftershockForecastList.add(newForecastMod);
        ++numMainshocks;

      }
    }

    /**
     * Next loop over the list of all forecast model objects and create
     * a forecast for each object
     */

    int numAftershockModels = STEP_AftershockForecastList.size();
    STEP_CombineForecastModels forecastModel;

    for (int modelLoop = 0; modelLoop < numAftershockModels; ++modelLoop){
      forecastModel =
          (STEP_CombineForecastModels)STEP_AftershockForecastList.get(modelLoop);
      // update the combined model
      UpdateSTEP_Forecast updateModel = new UpdateSTEP_Forecast(forecastModel);
      updateModel.updateAIC_CombinedModelForecast();
      
      /**
       * after the forecasts have been made, compare the forecast to
       *  the background at each location and keep whichever total 
       *  is higher
       */
      
      Location bgLoc, seqLoc;
      HypoMagFreqDistAtLoc seqDistAtLoc,bgDistAtLoc;
      //IncrementalMagFreqDist seqDist, bgDist;
      double bgSumOver5, seqSumOver5;
      
      ListIterator seqIt = forecastModel.getAfterShockZone().getGridLocationsIterator();
      ListIterator backGroundIt = this.bgGrid.getEvenlyGriddedGeographicRegion().getGridLocationsIterator();

      while (backGroundIt.hasNext()){
    	  bgLoc = (Location)backGroundIt.next();
    	  while (seqIt.hasNext()){
    		  seqLoc = (Location)seqIt.next();
    		  if (bgLoc.equalsLocation(seqLoc)){
    			  int nextSeqInd = seqIt.nextIndex();
    			  seqDistAtLoc = forecastModel.getHypoMagFreqDistAtLoc(nextSeqInd);
    			  int next_bgInd = backGroundIt.nextIndex();
    			 
    			  bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(next_bgInd);
    			  bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);
    			  seqSumOver5 = seqDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);;
    			  if (seqSumOver5 > bgSumOver5) {
    				  bgGrid.setMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),next_bgInd);
    			      // record the index of this aftershock sequence in an array in
    				  // the background so we know to save the sequence (or should it just be archived somehow now?)
    				  bgGrid.setSeqIndAtNode(next_bgInd,modelLoop);
    			  }
    		  }
    	  }
      }
    }

   

  }


}
