package scratchJavaDevelopers.matt.calc;



import org.opensha.sha.earthquake.ProbEqkRupture;

import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;

import org.opensha.sha.earthquake.observedEarthquake.CubeToObsEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;

import org.opensha.sha.magdist.IncrementalMagFreqDist;

//tested
import org.opensha.data.region.SitesInGriddedRectangularRegion;

//tested
import org.opensha.data.Location;
import org.opensha.data.LocationList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.log4j.Logger;


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
	private static Logger logger = Logger.getLogger(STEP_main.class);

	private static RegionDefaults rDefs;
	private static GregorianCalendar currentTime;
	private final static String BACKGROUND_RATES_FILE_NAME = RegionDefaults.TEST_Path + "/AllCal96ModelDaily.txt";
	private BackGroundRatesGrid bgGrid = null;
	private ArrayList<PointEqkSource> sourceList;
	private DecimalFormat locFormat = new DecimalFormat("0.0000");

	private String eventsFilePath = RegionDefaults.cubeFilePath;
	private String bgRatesFilePath = BACKGROUND_RATES_FILE_NAME;

	/**
	 * First load the active aftershock sequence objects from the last run
	 * load: ActiveSTEP_AIC_AftershockForecastList
	 * each object is a STEP_AftershockHypoMagFreqDistForecast
	 * ?? 1. any reason for using a static ???
	 * ?? 2. how long period this List is to store shocks??
	 */
	private static ArrayList <STEP_CombineForecastModels> STEP_AftershockForecastList =  new ArrayList <STEP_CombineForecastModels> ();


	public STEP_main() {
		// System.out.println("11111");
		// calc_STEP();
	}


	/**
	 *
	 * @param args String[]
	 */
	public static void main(String[] args) {
		STEP_main step = new STEP_main();
		step.calc_STEP();
	}

	/**
	 * Returns the Gridded Region applicable for STEP forecast
	 * @return
	 */
	public SitesInGriddedRectangularRegion getGriddedRegion(){
		return (SitesInGriddedRectangularRegion)bgGrid.getEvenlyGriddedGeographicRegion();
	}


	public static ArrayList <STEP_CombineForecastModels> getSTEP_AftershockForecastList(){
		return STEP_AftershockForecastList;
	}

	/**
	 * Returns the List of PointEqkSources
	 * @return
	 */
	public ArrayList<PointEqkSource> getSourceList(){
		return sourceList;
	}

	/**
	 * calc_STEP
	 */
	public  void calc_STEP() {

		//ArrayList New_AftershockForecastList = null;	 

		System.out.println("Starting STEP");

		/**
		 * 1. Now obtain all events that have occurred since the last time the code
		 *    was run:
		 * NewObsEqkRuptureList
		 */
		ObsEqkRupList newObsEqkRuptureList = loadNewEvents(); 


		/**
		 * 
		 * this sets the forecast start time as the current time.
		 */
		currentTime = getCurrentTime();


		/**
		 * 2. load background rates/grid list
		 * BackgroundRatesList
		 */    
		ArrayList<HypoMagFreqDistAtLoc> hypList = loadBgGrid();

		System.out.println("Read background rates");

		/**
		 * 3. now loop over all new events and assign them as an aftershock to
		 *    a previous event if appropriate (loop thru all existing mainshocks)
		 */
		processAfterShocks(currentTime, newObsEqkRuptureList);



		/**
		 * 4. Next loop over the list of all forecast model objects and create
		 *    a forecast for each object
		 */
		processForcasts(hypList );   

		/**
		 * 5. results output
		 */
		createRateFile(bgGrid);
		createStepSources(hypList);//add to sourceList
		createRateFile(sourceList);
		/**
		 * now remove all model elements that are newer than
		 * 7 days (or whatever is defined in RegionDefaults)
		 * -OR- did not produce rates higher than the background
		 * level anywhere.
		 */
		// PurgeMainshockList.removeModels(STEP_AftershockForecastList);
		//createRateFile(sourceList);
	}



	/**
	 * @param hypList -- 
	 */
	public void processForcasts(ArrayList<HypoMagFreqDistAtLoc> hypList) {
		int numAftershockModels = STEP_AftershockForecastList.size();

		logger.info("processForcasts0 numAftershockModels  " + numAftershockModels);

		//System.out.println("Number of Aftershock Models ="+numAftershockModels);
		STEP_CombineForecastModels forecastModel;

		synchronized(bgGrid) {//lock bgGrid
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


				LocationList bgLocList = bgGrid.getEvenlyGriddedGeographicRegion().getGridLocationsList();
				int bgRegionSize = bgLocList.size();
				LocationList aftershockZoneList = forecastModel.getAfterShockZone().getGridLocationsList();
				int asZoneSize = aftershockZoneList.size();


				double t_seqSumOver4 = 0;
				if (forecastModel.getMainShock().getMag() > 7.0){
					HypoMagFreqDistAtLoc t_seqDistAtLoc;    	  
					for (int as = 0; as < asZoneSize; ++as){
						//t_seqLoc = aftershockZoneList.getLocationAt(as);
						t_seqDistAtLoc = forecastModel.getHypoMagFreqDistAtLoc(as);
						t_seqSumOver4 += t_seqDistAtLoc.getFirstMagFreqDist().getCumRate(0);
						//System.out.println(t_seqDistAtLoc.getFirstMagFreqDist().getCumRate(0));
					}
					//System.out.println("Total Forecast " +t_seqSumOver4); 
				}


				for(int k=0;k<bgRegionSize;++k){
					bgLoc = bgLocList.getLocationAt(k);
					// ListIterator seqIt = forecastModel.getAfterShockZone().getGridLocationsIterator();
					for(int g=0;g<asZoneSize;++g){
						seqLoc = aftershockZoneList.getLocationAt(g);
						if (seqLoc != null){
							if (bgLoc.equalsLocation(seqLoc)){
								seqDistAtLoc = forecastModel.getHypoMagFreqDistAtLoc(g);
								bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(k);
								bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);
								seqSumOver5 = seqDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);;
								if (seqSumOver5 > bgSumOver5) {
									HypoMagFreqDistAtLoc hypoMagDistAtLoc= hypList.get(k);
									Location loc= hypoMagDistAtLoc.getLocation();
									hypList.set(k, new HypoMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),loc));
									bgGrid.setMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),k);
									// record the index of this aftershock sequence in an array in
									// the background so we know to save the sequence (or should it just be archived somehow now?)
									bgGrid.setSeqIndAtNode(k,modelLoop);
									// The above may not be necessary here I set a flag
									// to true that the model has been used in a forecast
									forecastModel.set_UsedInForecast(true);
								}
							}
						}
					}
				}
				//System.out.println("222222Size of Hype List =" + hypList.size());
			}//end of ModelLoop
		}

	}


	/**
	 * @param currtTime--current time
	 * @param newObsEqkRuptureList--new eq events
	 * @return
	 */
	public int processAfterShocks(GregorianCalendar currtTime, ObsEqkRupList newObsEqkRuptureList) {
		ObsEqkRupture newEvent, mainshock;
		STEP_CombineForecastModels mainshockModel, foundMsModel, staticModel;
		ListIterator newIt = newObsEqkRuptureList.listIterator();
		logger.info("newObsEqkRuptureList size " + newObsEqkRuptureList.size());
		boolean isAftershock = false;
		int indexNum, maxMagInd = -1;
		int numMainshocks = STEP_AftershockForecastList.size();	    
		logger.info("numMainshocks  " + numMainshocks);	    
		double maxMag = 0, msMag, newMag;

		synchronized(STEP_AftershockForecastList) {//lock STEP_AftershockForecastList
			// loop over new events
			while (newIt.hasNext()) {
				newEvent = (ObsEqkRupture) newIt.next();
				newMag = newEvent.getMag();
				//System.out.println("new mainshock mag = "+newMag);

				//System.out.println("number of main shock="+numMainshocks);
				//loop over existing mainshocks
				for (int msLoop = 0; msLoop < numMainshocks; ++msLoop) {
					mainshockModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);
					mainshock = mainshockModel.getMainShock();
					msMag = mainshock.getMag();
					//if (msMag >= 7.0)
					//	System.out.println(msLoop);

					// update the current time (as defined at the start of STEP_Main)
					// in this mainshock while we're at it.
					mainshockModel.set_CurrentTime(currtTime);

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
						 * to be a mainshock an event must be most recent and largest "mainshock"
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
				}//end of loop 2--mainshocks

				// now add the new event to the aftershock list of the largest appropriate
				// mainshock - if one has been found
				if (maxMagInd > -1) {
					foundMsModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(maxMagInd);
					foundMsModel.addToAftershockList(newEvent);
					//added as  aftershock to a main shock.
					isAftershock = true;
				}

				// add the new event to the list of mainshocks if it is greater than
				// magnitude 3.0 (or what ever mag is defined)
				if (newMag >= RegionDefaults.minMagForMainshock) {
					//System.out.println("Creating new main shock model");

					STEP_CombineForecastModels newForecastMod =
						new STEP_CombineForecastModels(newEvent,bgGrid,currtTime);

					// if the new event is already an aftershock to something else
					// set it as a secondary event.  Default is true
					if (isAftershock) {
						newForecastMod.set_isPrimary(false);
					}

					//if (newMag >= 7.0)
					//	System.out.println(newMag);

					// add the new event to the list of mainshocks and increment the number
					// of total mainshocks (for the loop)
					STEP_AftershockForecastList.add(newForecastMod);
					++numMainshocks;
				}
			}//end of loop1 -- new events
		}
		return numMainshocks;

	}


	public ArrayList<HypoMagFreqDistAtLoc>  loadBgGrid() {
		bgGrid = new BackGroundRatesGrid(bgRatesFilePath);
		//System.out.println("Number of BG locs = "+
		//bgGrid.getEvenlyGriddedGeographicRegion().getNumGridLocs());
		return  initHypoMagFreqDistForBGGrid(bgGrid);

	}


	public GregorianCalendar getCurrentTime() {
		Calendar curTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		int year = curTime.get(Calendar.YEAR);
		int month = curTime.get(Calendar.MONTH);
		int day = curTime.get(Calendar.DAY_OF_MONTH);
		int hour24 = curTime.get(Calendar.HOUR_OF_DAY);
		int min = curTime.get(Calendar.MINUTE);
		int sec = curTime.get(Calendar.SECOND);

		return  new GregorianCalendar(year, month,
				day, hour24, min, sec);

	}


	public ObsEqkRupList  loadNewEvents() {
		CubeToObsEqkRupture getNewEvents = null;
		try {
			logger.info("eventsFilePath  " + eventsFilePath );
			getNewEvents = new CubeToObsEqkRupture(eventsFilePath );
			return  getNewEvents.getAllObsEqkRupEvents(); 
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;	
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


	private void createStepSources(ArrayList<HypoMagFreqDistAtLoc> hypoMagDist){
		System.out.println("Creating STEP sources");
		if(sourceList == null){
			sourceList = new ArrayList<PointEqkSource>();
		}else{
			sourceList.clear();
		}
		int size = hypoMagDist.size();
		System.out.println("NumSources in hypList = "+size);
		synchronized(sourceList){
			for(int i=0;i<size;++i){
				HypoMagFreqDistAtLoc hypoLocMagDist = hypoMagDist.get(i);
				Location loc = hypoLocMagDist.getLocation();
				IncrementalMagFreqDist magDist = hypoLocMagDist.getFirstMagFreqDist();
				double rate = magDist.getY(0);
				if(rate ==0)
					continue;
				else if(rate !=0){
					//System.out.println("Writing out sources with rates not zero");
					PointEqkSource source = new PointEqkSource(loc,magDist,
							RegionDefaults.forecastLengthDays,RegionDefaults.RAKE,
							RegionDefaults.DIP,RegionDefaults.minForecastMag);
					sourceList.add(source);     
				}
			}
		}
	}

	/**
	 * @param bgGrid -- backgroud rates
	 * @return
	 */
	private ArrayList<HypoMagFreqDistAtLoc> initHypoMagFreqDistForBGGrid(BackGroundRatesGrid bgGrid){
		ArrayList<HypoMagFreqDistAtLoc> hypForecastList = bgGrid.getMagDistList();
		ArrayList<HypoMagFreqDistAtLoc> stepHypForecastList = new ArrayList <HypoMagFreqDistAtLoc>();
		int size = hypForecastList.size();
		//System.out.println("Size of BG magDist list = "+size);
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

	private void createRateFile(BackGroundRatesGrid bggrid){
		Location bgLoc;
		HypoMagFreqDistAtLoc bgDistAtLoc;
		double bgSumOver5;

		try{
			FileWriter fr = new FileWriter(RegionDefaults.outputAftershockRatePath);

			LocationList bgLocList = bgGrid.getEvenlyGriddedGeographicRegion().getGridLocationsList();
			int bgRegionSize = bgLocList.size();
			for(int k=0;k<bgRegionSize;++k){
				bgLoc = bgLocList.getLocationAt(k);
				bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(k);
				bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);  

				fr.write(locFormat.format(bgLoc.getLatitude())+"    "+locFormat.format(bgLoc.getLongitude())+"      "+bgSumOver5+"\n");
			}
			fr.close();
		}catch(IOException ee){
			ee.printStackTrace();
		}      

	}


	public String getEventsFilePath() {
		return eventsFilePath;
	}


	public void setEventsFilePath(String eventsFilePath) {
		this.eventsFilePath = eventsFilePath;
	}


	public String getBgRatesFilePath() {
		return bgRatesFilePath;
	}


	public void setBgRatesFilePath(String bgRatesFilePath) {
		this.bgRatesFilePath = bgRatesFilePath;
	}


	public BackGroundRatesGrid getBgGrid() {
		return bgGrid;
	}


}

