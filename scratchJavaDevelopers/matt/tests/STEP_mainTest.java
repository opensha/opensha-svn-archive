package scratchJavaDevelopers.matt.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratchJavaDevelopers.matt.calc.RegionDefaults;
import scratchJavaDevelopers.matt.calc.STEP_main;

/**
 * test class for the class
 * scratchJavaDevelopers.matt.calc.STEP_main
 * 
 * merge.nts is the input catalog as defined in RegionDefaults.  Magnitude is in 
	column 48-49.  this is Mag*10.  This has only one M6.7 event in it.    
	merge_landers.nts has 3538 events in it with the first one being a M7.3 
	event.

 * 
 * @author baishan
 *
 */
public class STEP_mainTest extends TestCase {
	public static String cubeFilePath_TEST =  RegionDefaults.TEST_Path + "/merge_test.nts";
	public static String cubeFilePath_TEST_1 =  RegionDefaults.TEST_Path + "/merge_landers.nts";

	private static Logger logger = Logger.getLogger(STEP_mainTest.class);
	private STEP_main stepmain;// = new STEP_main();
	public STEP_mainTest() {		
		super();

	}

	protected void setUp() {
		File datadir = new File(RegionDefaults.TEST_Path);
		logger.info("data dir  "  + datadir.getAbsolutePath() );
		stepmain = new STEP_main();
		stepmain.setEventsFilePath(RegionDefaults.cubeFilePath);
	}

	protected void tearDown() {
	}

	/**
	 * load events from the file merge_landers.nts
	 * merge_landers.nts has 3538 events in it with the first one being a M7.3  event.
	 * 
	 */
	public void testLoadEventslander() {
		stepmain.setEventsFilePath(cubeFilePath_TEST_1);
		//double strike1=  -1.0;
		try {
			//set test event file path
			//stepmain.setEventsFilePath(cubeFilePath_TEST);			
			ObsEqkRupList   eqkRupList = stepmain.loadNewEvents();
			assertTrue(eqkRupList.size() == 3538);
			//assertTrue("Should throw Exception with strike : " + strike1,false);
			ListIterator <ObsEqkRupture> newIt = eqkRupList.listIterator ();
			ObsEqkRupture newEvent;
			int index = 0;
			while (newIt.hasNext()) {
				newEvent = (ObsEqkRupture) newIt.next();
				//double newMag = newEvent.getMag();
				if(index++ == 0){
					assertTrue(newEvent.getMag() == 7.3);
				}
				logger.info("newEvent " + newEvent.getInfo());
			}
			assertTrue(index == 3538);
		}
		catch(Exception e)
		{
			// System.err.println("Exception thrown as Expected:  "+e);
			e.printStackTrace();
		}
		
	}

	/**
	 * test all the load process functions as in the 
	 * calc_STEP method in the STEP_main class
	 * as results of some methods are used in following method, it is impossible
	 * to do separate test for each method in STEP_main
	 */
	public void testCalc_STEP() {
		//1. load events
		ObsEqkRupList newObsEqkRuptureList = _testLoadEvents();
		//double strike1=  -1.0;
		try {
			//2. test load background
			ArrayList<HypoMagFreqDistAtLoc> hypList = stepmain.loadBgGrid();

			//logger.info("grid " + stepmain.getBgGrid().getHypoMagFreqDist().size());
			logger.info("hypList " + hypList.size());
			//test size
			assertTrue(hypList.size() > 0);
			//hypList just initialized
			_testHypoMagFreqDist(hypList, true);

			//3. test process aftershocks
			_testProcessAfterShocks(newObsEqkRuptureList);

			//4.test forcasting
			_testProcessForcast(hypList);
			//test Mag Freq again, and freq value may be >0
			_testHypoMagFreqDist(hypList , false);

		}
		catch(Exception e)
		{
			// System.err.println("Exception thrown as Expected:  "+e);
			e.printStackTrace();
		}
	}

	/**
	 *  merge.nts is the input catalog as defined in RegionDefaults.  
	 *  This has only one M6.7 event in it.  
	 * load new events
	 * this can be tested separately
	 * @return
	 */
	public ObsEqkRupList _testLoadEvents() {
		//double strike1=  -1.0;
		try {
			//set test event file path
			//stepmain.setEventsFilePath(cubeFilePath_TEST);			
			ObsEqkRupList   eqkRupList = stepmain.loadNewEvents();
			assertTrue(eqkRupList.size() ==1);
			//assertTrue("Should throw Exception with strike : " + strike1,false);
			ListIterator <ObsEqkRupture> newIt = eqkRupList.listIterator ();
			ObsEqkRupture newEvent;
			while (newIt.hasNext()) {
				newEvent = (ObsEqkRupture) newIt.next();
				//double newMag = newEvent.getMag();
				assertTrue(newEvent.getMag() == 6.7);
				logger.info("newEvent " + newEvent.getInfo());
			}
			return eqkRupList;
		}
		catch(Exception e)
		{
			// System.err.println("Exception thrown as Expected:  "+e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * process aftershocks
	 * this need be run after events loaded
	 * @return
	 */
	public void _testProcessAfterShocks(ObsEqkRupList newObsEqkRuptureList) {
		//double strike1=  -1.0;
		try {
			List stepAfterShocks = stepmain.getSTEP_AftershockForecastList();
			int numBefore = stepAfterShocks.size();
			logger.info("1 stepAfterShocks " + stepAfterShocks.size());

			stepmain.processAfterShocks(stepmain.getCurrentTime(), newObsEqkRuptureList);
			stepAfterShocks = stepmain.getSTEP_AftershockForecastList();
			int numAfter = stepAfterShocks.size();
			logger.info("2 stepAfterShocks " + stepAfterShocks.size());

			assertTrue(numAfter > numBefore);
		}
		catch(Exception e)
		{
			// System.err.println("Exception thrown as Expected:  "+e);
			logger.error(e);
		}
	}

	/**
	 * process broadscasting
	 * this need be run after eq events, bgGrid loaded, and aftershock processed
	 * @return
	 */
	private void _testProcessForcast(ArrayList<HypoMagFreqDistAtLoc> hypList) {
		//double strike1=  -1.0;
		try {
			//assertTrue(true);
			stepmain.processForcasts(hypList );			
		}
		catch(Exception e)
		{
			// System.err.println("Exception thrown as Expected:  "+e);
			e.printStackTrace();
		}
	}


	public void _testHypoMagFreqDist(ArrayList<HypoMagFreqDistAtLoc> hypList, boolean init) {
		LocationList bgLocList = stepmain.getBgGrid().getEvenlyGriddedGeographicRegion().getGridLocationsList();
		ArrayList<HypoMagFreqDistAtLoc> hypForecastList = stepmain.getBgGrid().getMagDistList();

		//LocationList aftershockZoneList = forecastModel.getAfterShockZone().getGridLocationsList();

		int bgRegionSize = bgLocList.size();
		// int asZoneSize = aftershockZoneList.size();
		logger.info("bgRegionSize " + bgRegionSize);
		//2. test locations size
		assertTrue(hypList.size() >= bgRegionSize);

		for(int k=0;k < bgRegionSize;++k){
			Location bgLoc = bgLocList.getLocationAt(k);		    	 
			//logger.info("loc index " + k);
			//logger.info("bgLoc " + bgLoc.toString());		    	
			HypoMagFreqDistAtLoc hypoMagDistAtLoc= hypList.get(k);
			HypoMagFreqDistAtLoc hypoMagDistAtLocBG= hypForecastList.get(k);

			Location hyploc= hypoMagDistAtLoc.getLocation();
			// logger.info("hyploc " + hyploc.toString());
			//3. test locations equal
			assertEquals(hyploc, bgLoc);	
			//4.test mag freq value
			double maxFreqVal = getMaxHypoMagFreqDistVal(hypoMagDistAtLoc );
			if(init){//at init state all  freq value is 0
				assertTrue(maxFreqVal == 0d);
			}else{//value no longer 0, as some events added
				assertTrue(maxFreqVal >= 0d);
			}
		} 

	}

	private double getMaxHypoMagFreqDistVal(HypoMagFreqDistAtLoc hypoMagDistAtLoc ) {
		IncrementalMagFreqDist[] magFreqDists = hypoMagDistAtLoc.getMagFreqDist();
		double maxVal = 0;
		for(IncrementalMagFreqDist magFreqDist:magFreqDists){
			int num = magFreqDist.getNum();
			for( int index=0; index < num; index++){
				double mag = magFreqDist.getX(index);
				double val = magFreqDist.getY(index);
				if(val > maxVal){
					maxVal = val;
					//logger.info("num " + num + " magindex " + index + " mag " + mag);
					//logger.info("val " + val);
				}
				//initial value = 0
				// assertTrue(val == value);
			}					  
		}
		return maxVal;		
	}
}