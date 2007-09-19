package scratchJavaDevelopers.matt.calc;

import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.Location;

import java.io.FileWriter;
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
  private static RegionDefaults rDefs;
  private static GregorianCalendar currentTime;
  private final static String BACKGROUND_RATES_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/step/AllCal96ModelDaily.txt";


  /**
   * First load the active aftershock sequence objects from the last run
   * load: ActiveSTEP_AIC_AftershockForecastList
   * each object is a STEP_AftershockHypoMagFreqDistForecast
   */
  private static ArrayList STEP_AftershockForecastList =  new ArrayList();
  public STEP_main() {
	 System.out.println("11111");
     calc_STEP();
  }


  /**
  *
  * @param args String[]
  */
 public static void main(String[] args) {
	 STEP_main step = new STEP_main();
 }

  /**
   * calc_STEP
   */
  public  void calc_STEP() {
    
	 ArrayList New_AftershockForecastList = null;
	 BackGroundRatesGrid bgGrid = null;

	 System.out.println("Starting STEP");

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
    
      Calendar curTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      int year = curTime.get(Calendar.YEAR);
      int month = curTime.get(Calendar.MONTH);
      int day = curTime.get(Calendar.DAY_OF_MONTH);
      int hour24 = curTime.get(Calendar.HOUR_OF_DAY);
      int min = curTime.get(Calendar.MINUTE);
      int sec = curTime.get(Calendar.SECOND);

      currentTime = new GregorianCalendar(year, month,
          day, hour24, min, sec);

    /**
     * load background rates/grid list
     * BackgroundRatesList
     */
    
    bgGrid = new BackGroundRatesGrid(BACKGROUND_RATES_FILE_NAME);
    ArrayList<HypoMagFreqDistAtLoc> hypList = initHypoMagFreqDistForBGGrid(bgGrid);
    
    System.out.println("Read background rates");
    
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
      System.out.println("new mainshock mag = "+newMag);

      System.out.println("number of main shock="+numMainshocks);
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
            	
        	/***
        	 * getting the mainshock index which had
        	 * maximum magnitude upto this point, setting that static 
        	 * as it no longer has maximum magnitude. 
        	 */
           //   staticModel =
            //(STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);
              staticModel = 
            (STEP_CombineForecastModels)STEP_AftershockForecastList.get(maxMagInd);
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
        //added as  aftershock to a main shock.
        isAftershock = true;
      }

      // add the new event to the list of mainshocks if it is greater than
      // magnitude 3.0 (or what ever mag is defined)
      if (newMag >= RegionDefaults.minMagForMainshock) {
          System.out.println("Creating new main shock model");

      STEP_CombineForecastModels newForecastMod =
           new STEP_CombineForecastModels(newEvent,bgGrid,currentTime);

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
      ListIterator backGroundIt = bgGrid.getEvenlyGriddedGeographicRegion().getGridLocationsIterator();

      while (backGroundIt.hasNext()){
    	  bgLoc = (Location)backGroundIt.next();
    	  while (seqIt.hasNext()){
    		  seqLoc = (Location)seqIt.next();
    		  if (bgLoc.equalsLocation(seqLoc)){
    			  int nextSeqInd = seqIt.nextIndex()-1;
    			  seqDistAtLoc = forecastModel.getHypoMagFreqDistAtLoc(nextSeqInd);
    			  int next_bgInd = backGroundIt.nextIndex()-1;
    			 
    			  bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(next_bgInd);
    			  bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);
    			  seqSumOver5 = seqDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);;
    			  if (seqSumOver5 > bgSumOver5) {
    				  HypoMagFreqDistAtLoc hypoMagDistAtLoc= hypList.get(next_bgInd);
    				  Location loc= hypoMagDistAtLoc.getLocation();
    				  hypList.set(next_bgInd, new HypoMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),loc));
    				  bgGrid.setMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),next_bgInd);
    			      // record the index of this aftershock sequence in an array in
    				  // the background so we know to save the sequence (or should it just be archived somehow now?)
    				  bgGrid.setSeqIndAtNode(next_bgInd,modelLoop);
    			  }
    		  }
    	  }
      }
    }
    ArrayList<PointEqkSource> sourceList = createStepSources(hypList);
    createRateFile(sourceList);
  }

  private void createRateFile(ArrayList<PointEqkSource> sourcelist){
	  int size = sourcelist.size();
	  FileWriter fw = null;
	  System.out.println("Writing file");
	  try {
		fw = new FileWriter("STEP_Rates");
      } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  }
      System.out.println("NumSources = "+size);
	  for(int i=0;i<size;++i){
		  PointEqkSource source = sourcelist.get(i);
		  Location loc = source.getLocation();
		  int numRuptures = source.getNumRuptures();
		  for(int j=0;j<numRuptures;++j){
			  ProbEqkRupture rupture = source.getRupture(j);
			  double prob = rupture.getProbability();
			  double rate = -Math.log(1-prob);
			  try {
				fw.write(loc.toString()+"   "+rate+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
	  try {
		fw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  
  private ArrayList<PointEqkSource> createStepSources(ArrayList<HypoMagFreqDistAtLoc> hypoMagDist){
	  System.out.println("Creating STEP sources");
	  ArrayList<PointEqkSource> sourceList = new ArrayList<PointEqkSource>();
	  int size = hypoMagDist.size();
	  for(int i=0;i<size;++i){
		  HypoMagFreqDistAtLoc hypoLocMagDist = hypoMagDist.get(i);
		  Location loc = hypoLocMagDist.getLocation();
		  IncrementalMagFreqDist magDist = hypoLocMagDist.getFirstMagFreqDist();
		  double rate = magDist.getY(0);
		  if(rate ==0)
			  continue;
		  PointEqkSource source = new PointEqkSource(loc,magDist,
				                  RegionDefaults.forecastLengthDays,RegionDefaults.RAKE,
				                  RegionDefaults.DIP,RegionDefaults.minForecastMag);
		  sourceList.add(source);      
	  }
	  return sourceList;
  }
  
  private ArrayList<HypoMagFreqDistAtLoc> initHypoMagFreqDistForBGGrid(BackGroundRatesGrid bgGrid){
  	ArrayList<HypoMagFreqDistAtLoc> hypForecastList = bgGrid.getMagDistList();
  	ArrayList<HypoMagFreqDistAtLoc> stepHypForecastList = new ArrayList <HypoMagFreqDistAtLoc>();
  	int size = hypForecastList.size();
  	for(int i=0;i<size;++i){
  		HypoMagFreqDistAtLoc hypForcast = hypForecastList.get(i);
  		Location loc = hypForcast.getLocation();
  		IncrementalMagFreqDist magDist = hypForcast.getFirstMagFreqDist();
  		IncrementalMagFreqDist HypForecastMagDist = new IncrementalMagFreqDist(magDist.getMinX(),
  				magDist.getNum(),magDist.getDelta());
  		for(int j=0;j<HypForecastMagDist.getNum();++j)
  			HypForecastMagDist.set(j, 0.0);
  		stepHypForecastList.add(new HypoMagFreqDistAtLoc(HypForecastMagDist,loc));
  	}
  	return stepHypForecastList;
  }

  
  private void createRatesFile(){
	  
  }

}
