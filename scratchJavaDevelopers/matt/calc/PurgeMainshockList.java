/**
 * 
 */
package scratchJavaDevelopers.matt.calc;

import java.util.*;

import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;

/**
 * @author matt
 *
 */
public class PurgeMainshockList {
	
	private ArrayList <STEP_CombineForecastModels> finalModels ;
	private static double daysToForget;
	private static STEP_CombineForecastModels mainshockModel;
	private static double msAge;
	
	
	public static void removeModels(ArrayList<STEP_CombineForecastModels> finalModels){
		int numMs = finalModels.size();
		int msLoop = 0;
		while (msLoop < numMs){
			mainshockModel = finalModels.get(msLoop);
	           
			msAge = mainshockModel.getDaysSinceMainshockStart();
			//First check if the MS is too recent
			if (msAge <= RegionDefaults.daysFromQDM_Cat)
				finalModels.remove(msLoop);
			// then make sure it is actually being used in a forecast
			else if
				(mainshockModel.get_UsedInForecast())
				finalModels.remove(msLoop);
			else
				++msLoop;
		}	
				
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
