/**
 * 
 */
package scratch.pagem;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;

/**
 * 
  */
public class MeanUCERF2_RandomEvents {


	public MeanUCERF2_RandomEvents() {
		
		
	}

	
	
	

	
	/**
	 * It gets all the subsections for SoSAF and prints them on console
	 * @param args
	 */
	public static void main(String []args) {
		
		
		
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.getTimeSpan().setDuration(3000.0);
		meanUCERF2.updateForecast();
//		for(int i=0;i<5;i++) {
			ArrayList<ProbEqkRupture> rupList = meanUCERF2.drawRandomEventSet();
			System.out.println("Num Random Events ="+rupList.size());			
//		}
		
		for(int r=0;r<rupList.size();r++){
			ProbEqkRupture rup = rupList.get(r);
			LocationList locs = rup.getRuptureSurface().getLocationList();
			Location loc = locs.getLocationAt((int)Math.floor(Math.random()*locs.size()));
			System.out.println((float)rup.getMag()+"\t"+(float)loc.getLatitude()+"\t"+
					(float)loc.getLongitude()+"\t"+(float)loc.getDepth());
		}

		
	}

}
