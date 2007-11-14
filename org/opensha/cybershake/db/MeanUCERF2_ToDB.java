/**
 * 
 */
package org.opensha.cybershake.db;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2;

/**
 * This Class creates an instances of Mean UCERF2 ERF to insert into the database
 * 
 * @author vipingupta
 *
 */
public class MeanUCERF2_ToDB extends ERF2DB {
	
	public MeanUCERF2_ToDB(DBAccess db){
		super(db);
		createUCERF2ERF();
	}
	
	 /**
	  * Create NSHMP 02 ERF instance
	  *
	  */
	  private void createUCERF2ERF() {

	    
		eqkRupForecast = new MeanUCERF2();
		
		// exclude Background seismicity
	    eqkRupForecast.getAdjustableParameterList().getParameter(
	    		UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_EXCLUDE);
	  
	    // Rup offset
	    eqkRupForecast.getAdjustableParameterList().getParameter(
	    		MeanUCERF2.RUP_OFFSET_PARAM_NAME).setValue(
	        new Double(5.0));
	    
	    // Cybershake DDW(down dip correction) correction
	    eqkRupForecast.getAdjustableParameterList().getParameter(
	    		MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME).setValue(
	        new Boolean(true));
	    
	    // Set Poisson Probability model
	    eqkRupForecast.getAdjustableParameterList().getParameter(
	    		UCERF2.PROB_MODEL_PARAM_NAME).setValue(
	    				UCERF2.PROB_MODEL_POISSON);
	    
	    // duration
	    eqkRupForecast.getTimeSpan().setDuration(1.0);
	    eqkRupForecast.updateForecast();
	  }
}
