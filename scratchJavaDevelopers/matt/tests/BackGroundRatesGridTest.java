package scratchJavaDevelopers.matt.tests;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.opensha.data.DataPoint2D;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratchJavaDevelopers.matt.calc.BackGroundRatesGrid;
import scratchJavaDevelopers.matt.calc.RegionDefaults;

/**
 * test the BackGroundRatesGrid class
 * @author baishan
 *
 */
public class BackGroundRatesGridTest  extends TestCase {
	private static Logger logger = Logger.getLogger(BackGroundRatesGridTest.class);
	private BackGroundRatesGrid bgGrid1;
	private BackGroundRatesGrid bgGrid2;
	protected void setUp() {
		//simple bg grid as in STEP_Main, with only one mag freq value
		bgGrid1 = new BackGroundRatesGrid(RegionDefaults.TEST_Path + "/AllCal96ModelDaily.txt");
		//BackGroundRatesGrid with a range of mag freq values
		bgGrid2 = new BackGroundRatesGrid(4, 8, 0.1);
		bgGrid2.setBgGridFilename(RegionDefaults.TEST_Path + "/AllCal96ModelDaily.txt");
		
	}

	protected void tearDown() {
	}
	
	public void testBgRatesGridStart() {
		int start1 = bgGrid1.getForecastMagStart();
		int start2 = bgGrid2.getForecastMagStart();
		logger.info("start1 " + start1 + " start2 " + start2);
		assertTrue(start1 == 0);
		assertTrue(start2 == 20);
		
		assertTrue(bgGrid1.isBackgroundRatesFileAlreadyRead());
		assertTrue(bgGrid1.isBackgroundSourcesAlreadyMade());
		//bgGrid2 is not initialized yet
		assertFalse(bgGrid2.isBackgroundRatesFileAlreadyRead());
		assertFalse(bgGrid2.isBackgroundSourcesAlreadyMade());
	}
	
	public void testBgRatesGrid1() {
		logger.info(">>>> testBgRatesGrid1 " );
		
		List<HypoMagFreqDistAtLoc>  hypoMagFreqDistAtLoc = bgGrid1.getHypoMagFreqDist();
		logger.info("hypoMagFreqDistAtLoc.size() " +  hypoMagFreqDistAtLoc.size());
		//get for first location
		HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc0 = bgGrid1.getHypoMagFreqDistAtLoc(0);
		IncrementalMagFreqDist[]  hypoMagFreqDist = hypoMagFreqDistAtLoc0.getMagFreqDist();
		logger.info("hypoMagFreqDist.length " +  hypoMagFreqDist.length);
		 //1. there is only 1 mag freq dist
		 assertTrue(hypoMagFreqDist.length == 1);	
		 
		//get first mag freq distrribution
		IncrementalMagFreqDist hypoMagFreqDist0 = hypoMagFreqDist[0];
		int num = hypoMagFreqDist0.getNum(); //1
		logger.info("num " +  num);
		//2. there is only 1 mag 
		assertTrue(num == 1);
		logger.info("hypoMagFreqDist0 " +  hypoMagFreqDist0);
		org.opensha.data.DataPoint2D point = hypoMagFreqDist0.get(0);
		//3. the mag==0
		assertTrue( point.getX()  == 0);
		
		//4. test SeqIndAtNode
		bgGrid1.getSeqIndAtNode();
		assertTrue(bgGrid1.getSeqIndAtNode().length  == bgGrid1.getNumHypoLocs());
		logger.info("bgGrid1.getSeqIndAtNode() "  + bgGrid1.getSeqIndAtNode().length);
		for(double val:bgGrid1.getSeqIndAtNode()){
			assertTrue(val  == -1);
		}
		logger.info("<<<< testBgRatesGrid1" );
	}
	
	public void testBgRatesGrid2() {
		logger.info(">>>> testBgRatesGrid2 " );		
		bgGrid2.initialize();		
		List<HypoMagFreqDistAtLoc>  hypoMagFreqDistAtLoc = bgGrid2.getHypoMagFreqDist();
		logger.info("hypoMagFreqDistAtLoc.size() " +  hypoMagFreqDistAtLoc.size());
		//get for first location
		HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc0 = bgGrid2.getHypoMagFreqDistAtLoc(0);
		IncrementalMagFreqDist[]  hypoMagFreqDist = hypoMagFreqDistAtLoc0.getMagFreqDist();
		 //1. there is only 1 mag freq dist
		 assertTrue(hypoMagFreqDist.length == 1);	
		logger.info("hypoMagFreqDist.length " +  hypoMagFreqDist.length);
		//get first mag freq distrribution
		IncrementalMagFreqDist hypoMagFreqDist0 = hypoMagFreqDist[0];
		int num = hypoMagFreqDist0.getNum(); //1
		logger.info("num " +  num);
		//bgGrid2 = new BackGroundRatesGrid(4, 8, 0.1);
		int testNum = (int) Math.round((8-4)/0.1);
		logger.info("testNum " +  testNum);
		//2. test num of mag freq values
		assertTrue(num == testNum + 1);
		logger.info("hypoMagFreqDist0 " +  hypoMagFreqDist0);
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
			bgGrid1.getSeqIndAtNode();
			assertTrue(bgGrid2.getSeqIndAtNode().length  == bgGrid2.getNumHypoLocs());
			logger.info("bgGrid2.getSeqIndAtNode() "  + bgGrid2.getSeqIndAtNode().length);
			for(double val:bgGrid1.getSeqIndAtNode()){
				assertTrue(val  == -1);
			}
			
		logger.info("<<<< testBgRatesGrid2 " );		
	}
	
}
