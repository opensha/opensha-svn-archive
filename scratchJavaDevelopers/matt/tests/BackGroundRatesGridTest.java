package scratchJavaDevelopers.matt.tests;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.opensha.commons.data.DataPoint2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.SitesInGriddedRectangularRegion;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratchJavaDevelopers.matt.calc.BackGroundRatesGrid;
import scratchJavaDevelopers.matt.calc.RegionDefaults;

/**
 * test the BackGroundRatesGrid class
 * 
 * N.B.
 * it is an assumption for the hypoMagFreqDistAtLoc list to contain the same number and order of locations as 
 * those in the region grid list, but this is not true for california, may be a map should be used to store the 
 * hypoMagFreqDist
 * 
 * @author baishan
 *
 */
public class BackGroundRatesGridTest  extends TestCase {
	private static Logger logger = Logger.getLogger(BackGroundRatesGridTest.class);
	private BackGroundRatesGrid bgGrid1; //California
	private BackGroundRatesGrid bgGrid2; //california
	private BackGroundRatesGrid bgGrid3; //NZ
	protected void setUp() {
		//simple bg grid as in STEP_Main, with only one mag freq value
		RegionDefaults.setRegion(RegionDefaults.REGION_CF);
		bgGrid1 = new BackGroundRatesGrid(0,0,0);
		bgGrid1.setBgGridFilename(RegionDefaults.TEST_Path + "/AllCal96ModelDaily.txt");
		bgGrid1.initialize();	
		//RegionDefaults.TEST_Path + "/AllCal96ModelDaily.txt"
		//BackGroundRatesGrid with a range of mag freq values
		bgGrid2 = new BackGroundRatesGrid(4, 8, 0.1);
		bgGrid2.setBgGridFilename(RegionDefaults.TEST_Path + "/AllCal96ModelDaily.txt");
		//NZ grid
		RegionDefaults.setRegion(RegionDefaults.REGION_NZ);
		bgGrid3 = new BackGroundRatesGrid(RegionDefaults.TEST_Path + "/NZdailyRates.txt");
			
	}

	protected void tearDown() {
	}
	
	/**
	 * test if BackGrount already initilized
	 */
	public void testBgRatesGridStart() {
		int start1 = bgGrid1.getForecastMagStart();
		int start2 = bgGrid2.getForecastMagStart();
		int start3 = bgGrid3.getForecastMagStart();
		logger.info("start1 " + start1 + " start2 " + start2 + " start3 " + start3);
		assertTrue(start1 == 0);
		assertTrue(start2 == 20);
		assertTrue(start3 == 20);
		
		assertTrue(bgGrid1.isBackgroundRatesFileAlreadyRead());
		assertTrue(bgGrid1.isBackgroundSourcesAlreadyMade());
		//bgGrid2 is not initialized yet
		assertFalse(bgGrid2.isBackgroundRatesFileAlreadyRead());
		assertFalse(bgGrid2.isBackgroundSourcesAlreadyMade());
		//NZ Grid
		assertTrue(bgGrid3.isBackgroundRatesFileAlreadyRead());
		assertTrue(bgGrid3.isBackgroundSourcesAlreadyMade());
	}
	
	/**
	 * Grid1--California
	 */
	public void testBgRatesGrid1() {
		logger.info(">>>> testBgRatesGrid1 " );
		
		List<HypoMagFreqDistAtLoc>  hypoMagFreqDistAtLoc = bgGrid1.getHypoMagFreqDist();		
		//check grid locations
		SitesInGriddedRectangularRegion region  = (SitesInGriddedRectangularRegion)bgGrid1.getEvenlyGriddedGeographicRegion();
		//logger.info("hypoMagFreqDistAtLoc.size()=" +  hypoMagFreqDistAtLoc.size() + " region locs=" + region.getNumGridLocs());
		/*
		 * this is an assumption for the hypoMagFreqDistAtLoc list to contain the same number and order of locations as 
		 * those in the region grid list, but this is not true for california, may be a map should be used to store the 
		 * hypoMagFreqDist
		 * */
//		assertEquals("number of locations in hypoMagFreqDistAtLoc should match grid locations",
//				region.getNumGridLocs(), hypoMagFreqDistAtLoc.size() );
//		for (int i = 0; i < region.getNumGridLocs(); i++){
//			Location loc = region.getGridLocation(i);
//			assertEquals("locations in hypoMagFreqDistAtLoc should match grid locations", loc, hypoMagFreqDistAtLoc.get(i).getLocation());
//		}
		//get MagFreqDistList for first location
		HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc0 = bgGrid1.getHypoMagFreqDistAtLoc(0);
		IncrementalMagFreqDist[]  hypoMagFreqDist = hypoMagFreqDistAtLoc0.getMagFreqDist();
		logger.info("hypoMagFreqDist.length " +  hypoMagFreqDist.length);
		 //1. there is only 1 mag freq dist
		assertEquals("hypoMagFreqDist has one record", 1,hypoMagFreqDist.length);	
		 
		//get first mag freq distrribution
		IncrementalMagFreqDist hypoMagFreqDist0 = hypoMagFreqDist[0];
		int num = hypoMagFreqDist0.getNum(); //1
		//logger.info("num " +  num);
		//2. there is only 1 mag 
		assertEquals("there is only 1 mag ", 1,num );
		logger.info("hypoMagFreqDist0 " +  hypoMagFreqDist0);
		org.opensha.commons.data.DataPoint2D point = hypoMagFreqDist0.get(0);
		//3. the mag==0
		assertEquals("x==0", 0d, point.getX() );
		
		//4. test SeqIndAtNode
		assertEquals("getNumHypoLocs == sequences", bgGrid1.getSeqIndAtNode().length ,bgGrid1.getNumHypoLocs());
		//logger.info("bgGrid1.getSeqIndAtNode() "  + bgGrid1.getSeqIndAtNode().length);
		for(double val:bgGrid1.getSeqIndAtNode()){
			assertEquals("getSeqIndAtNode==-1", -1d,val);
		}
		logger.info("<<<< testBgRatesGrid1" );
	}
	
	/**
	 * California grid
	 */
	public void testBgRatesGrid2() {
		logger.info(">>>> testBgRatesGrid2 " );		
		bgGrid2.initialize();		
		List<HypoMagFreqDistAtLoc>  hypoMagFreqDistAtLoc = bgGrid2.getHypoMagFreqDist();
		//logger.info("hypoMagFreqDistAtLoc.size() " +  hypoMagFreqDistAtLoc.size());
		//get for first location
		HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc0 = bgGrid2.getHypoMagFreqDistAtLoc(0);
		IncrementalMagFreqDist[]  hypoMagFreqDist = hypoMagFreqDistAtLoc0.getMagFreqDist();
		 //1. there is only 1 mag freq dist
		assertEquals("hypoMagFreqDist length incorrect", 1,hypoMagFreqDist.length );	
		logger.info("hypoMagFreqDist.length " +  hypoMagFreqDist.length);
		//get first mag freq distrribution
		IncrementalMagFreqDist hypoMagFreqDist0 = hypoMagFreqDist[0];
		int num = hypoMagFreqDist0.getNum(); //1
		//logger.info("num " +  num);
		//bgGrid2 = new BackGroundRatesGrid(4, 8, 0.1);
		int testNum = (int) Math.round((8-4)/0.1);
		//logger.info("testNum " +  testNum);
		//2. test num of mag freq values
		assertEquals("Number of points incorrect", testNum + 1,num);
		
		// logger.info("check###1"  );
		//logger.info("hypoMagFreqDist0 ==" +  hypoMagFreqDist0);
		
		//org.opensha.data.DataPoint2D point = hypoMagFreqDist0.get(0);
		//logger.info("point " +  point);
		//3. test each mag values
		 Iterator it = hypoMagFreqDist0.getPointsIterator();
		
	      while(it.hasNext()){
	        DataPoint2D point = (DataPoint2D)it.next();
	        //logger.info("point " +  point);
	        //test the magnitude of each point
	        assertTrue( point.getX() >=4 && point.getX() <= 8);		       
	      }		
		
	    //4. test SeqIndAtNode	
	      assertEquals("getNumHypoLocs == sequences", bgGrid2.getSeqIndAtNode().length , bgGrid2.getNumHypoLocs());
			logger.info("bgGrid2.getSeqIndAtNode() "  + bgGrid2.getSeqIndAtNode().length);
			for(double val:bgGrid2.getSeqIndAtNode()){
				assertEquals("getSeqIndAtNode==-1", -1d,val);
			}
			
		//logger.info("<<<< testBgRatesGrid2 " );		
	}
	
	
	/**
	 * NZ grid, initialized
	 */
	public void testBgRatesGrid3() {
		logger.info(">>>> testBgRatesGrid3 " );
		
		List<HypoMagFreqDistAtLoc>  hypoMagFreqDistAtLoc = bgGrid3.getHypoMagFreqDist();
		logger.info("hypoMagFreqDistAtLoc.size() " +  hypoMagFreqDistAtLoc.size());
		//check grid locations
		SitesInGriddedRectangularRegion region  = (SitesInGriddedRectangularRegion)bgGrid3.getEvenlyGriddedGeographicRegion();
		
		/*
		 * this is an assumption for the hypoMagFreqDistAtLoc list to contain the same number and order of locations as 
		 * those in the region grid list, but this is not true for california, may be a map should be used to store the 
		 * hypoMagFreqDist
		 * */
		assertEquals("number of locations in hypoMagFreqDistAtLoc should match grid locations",
				region.getNumGridLocs(), hypoMagFreqDistAtLoc.size() );
		for (int i = 0; i < region.getNumGridLocs(); i++){
			Location loc = region.getGridLocation(i);
			assertEquals("locations in hypoMagFreqDistAtLoc should match grid locations", loc, hypoMagFreqDistAtLoc.get(i).getLocation());
		}	
		
		HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc0 = bgGrid3.getHypoMagFreqDistAtLoc(0);
		IncrementalMagFreqDist[]  hypoMagFreqDist = hypoMagFreqDistAtLoc0.getMagFreqDist();
		logger.info("hypoMagFreqDist.length " +  hypoMagFreqDist.length);
		 //1. there is only 1 mag freq dist
		assertEquals("hypoMagFreqDist has one record", 1,hypoMagFreqDist.length );	
		 
		//get first mag freq distrribution
		IncrementalMagFreqDist hypoMagFreqDist0 = hypoMagFreqDist[0];
		int num = hypoMagFreqDist0.getNum(); //1
		logger.info("num " +  num);
		//2. there is only 1 mag 
		assertEquals("there are 41 mag ", 41,num );
		logger.info("hypoMagFreqDist0 " +  hypoMagFreqDist0);
		org.opensha.commons.data.DataPoint2D point = hypoMagFreqDist0.get(0);
		//3. the mag==0
		//assertTrue( point.getX()  == 0);
		 assertTrue( point.getX() >=4 && point.getX() <= 8);		     
		
		//4. test SeqIndAtNode		
		 assertEquals("getNumHypoLocs == sequences", bgGrid3.getSeqIndAtNode().length , bgGrid3.getNumHypoLocs());
		//logger.info("bgGrid3.getSeqIndAtNode() "  + bgGrid3.getSeqIndAtNode().length);
		for(double val:bgGrid3.getSeqIndAtNode()){
			assertEquals("getSeqIndAtNode==-1", -1d,val);
		}
	}
	
}
