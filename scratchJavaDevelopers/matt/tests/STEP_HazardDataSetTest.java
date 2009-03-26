package scratchJavaDevelopers.matt.tests;

import java.util.ListIterator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.imr.AttenuationRelationship;

import scratchJavaDevelopers.matt.calc.STEP_HazardDataSet;
import scratchJavaDevelopers.matt.calc.STEP_main;

public class STEP_HazardDataSetTest  extends TestCase {
	private static Logger logger = Logger.getLogger(BackGroundRatesGridTest.class);
	private STEP_main stepmain  ;
	private STEP_HazardDataSet step_HazardDataSet;
	
	
	protected void setUp() {
		step_HazardDataSet = new STEP_HazardDataSet(false);
		step_HazardDataSet.runStepmain();		
		step_HazardDataSet.createShakeMapAttenRelInstance();
	}

	protected void tearDown() {
	}
	
	/**
	 * attenuation relationship, test initial params
	 * 
	 */
	public void testAttenRel() {
		AttenuationRelationship attenrel = step_HazardDataSet.getAttenRel();
		ListIterator itSite = attenrel.getSiteParamsIterator();
		
		while(itSite.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI param = (ParameterAPI)((ParameterAPI)itSite.next());
			assertTrue(param.getValue() != null);
			logger.info("site param " +  param.getInfo() + " = " + param.getValue() );			
		}
		
		ListIterator itRupt = attenrel.getEqkRuptureParamsIterator();
		while(itRupt.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI param = (ParameterAPI)((ParameterAPI)itRupt.next());
			assertTrue(param.getValue() != null);
			logger.info("rupture param " +  param.getInfo() + " = " + param.getValue()  );			
		}
		
		ListIterator itOther = attenrel.getOtherParamsIterator();
		while(itOther.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI param = (ParameterAPI)((ParameterAPI)itOther.next());
			assertTrue(param.getValue() != null);
			logger.info("other param " +  param.getInfo()  + " = " + param.getValue() );			
		}
		
		ListIterator itExceed = attenrel.getExceedProbIndependentParamsIterator();
		while(itExceed.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI param = (ParameterAPI)((ParameterAPI)itExceed.next());
			assertTrue(param.getValue() != null);
			logger.info("ExceedProbIndependent param " +  param.getInfo()  + " = " + param.getValue() );			
		}
		
		ListIterator itIML = attenrel.getIML_AtExceedProbIndependentParamsIterator();
		while(itIML.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI param = (ParameterAPI)((ParameterAPI)itIML.next());
			logger.info("IML_AtExceedProbIndependent param " +  param.getInfo()  + " = " + param.getValue() );			
		}
		
		ListIterator itPropagation = attenrel.getPropagationEffectParamsIterator();
		while(itPropagation.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI param = (ParameterAPI)((ParameterAPI)itPropagation.next());
			assertTrue(param.getValue() != null);
			logger.info("PropagationEffect param " +  param.getInfo()  + " = " + param.getValue() );			
		}
		
		/// .....
		
		
	}
	
	/**
	 *  test calculating step probabilties
	 * 
	 */
	public void testCalcStepProbValues() {
		logger.info("testCalcStepProbValues " );
		SitesInGriddedRectangularRegion region = step_HazardDataSet.getDefaultRegion();//
		//logger.info("region.getNumGridLocs " + region.getNumGridLocs());
		double[] stepBothProbVals = step_HazardDataSet.calcStepProbValues(region);
		//logger.info("stepBothProbVals "  + stepBothProbVals.length);		
		assertTrue(stepBothProbVals.length == region.getNumGridLocs());
		for(double val: stepBothProbVals){
			//logger.info("val "  + val);
			assertTrue(val>=0 && val <=1);
		}	
	}
}
