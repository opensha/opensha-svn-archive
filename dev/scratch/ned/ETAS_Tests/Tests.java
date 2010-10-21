package scratch.ned.ETAS_Tests;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.calc.ERF2GriddedSeisRatesCalc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.faultSurface.FaultTrace;

import scratch.vipin.relm.RELM_ERF_ToGriddedHypoMagFreqDistForecast;

/**
 * @author field
 *
 */
public class Tests extends ArrayList<Integer> {
	
	
	public Tests() {
		


	}

	/**
	 * This creates data for a figure in our project plan, where one set is the rate of M≥5 events,
	 * and the other is this multiplied by a distance decay to get the probability that any cell will
	 * host an aftershock
	 */
	public void mkProjectPlanETAS_FigureData() {
		double[] m5_RatesInRELM_Region, m5_modRates;
		Location[] locs;
		int numLocs;
		
		NSHMP_GridSourceGenerator nshmpGen = new NSHMP_GridSourceGenerator();
		GriddedRegion region = nshmpGen.getGriddedRegion();	
//		GriddedRegion region = new GriddedRegion(new Location(32.5,-118), new Location(35.5,-114), 0.1, null);
		numLocs = region.getNodeCount();
		System.out.println("numLocs="+numLocs);

		m5_RatesInRELM_Region = new double[numLocs];
		m5_modRates = new double[numLocs];
		locs = new Location[numLocs];
		FaultTrace trace = new FaultTrace(null);
		trace.add(new Location(33.9,-115.4));
		trace.add(new Location(34.5,-116.1));
		double length = trace.getTraceLength();
		
		// UCERF 2
		int duration = 1;
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2.getTimeSpan().setDuration(duration);
		meanUCERF2.updateForecast();
		
		ERF2GriddedSeisRatesCalc erfToGriddedSeisRatesCalc = new ERF2GriddedSeisRatesCalc();
		// using 5.5 below to show more of the faults
		m5_RatesInRELM_Region =erfToGriddedSeisRatesCalc.getTotalSeisRateAtEachLocationInRegion(5.5, meanUCERF2, region);
		for(int i=0;i<numLocs;i++) {
			m5_RatesInRELM_Region[i] *= Math.pow(10,0.5);  // this gets it back to the rate of M>5
			locs[i] = region.locationForIndex(i);
			double distToTrace = trace.minDistToLine(locs[i]);
			double dist = Math.sqrt(distToTrace*distToTrace+5*5);
			m5_modRates[i] = m5_RatesInRELM_Region[i]*Math.pow(dist, -1.4)*(1/(2*Math.PI*dist+length));
		}
		
		double tot =0;
		for(int i=0;i<numLocs;i++) tot += m5_modRates[i];
		for(int i=0;i<numLocs;i++) m5_modRates[i] /= tot;
		tot =0;
		for(int i=0;i<numLocs;i++) tot += m5_modRates[i];
		System.out.println("normalization test ="+(float)tot+"\t(should be 1.0)");

		
		
		// write file
		try{
			FileWriter fw1 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/mag5_rate_data.txt");
			FileWriter fw2 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/mag5_modRate_data.txt");
			String outputString1 = new String();
			String outputString2 = new String();
			outputString1+= "lat\tlon\trate\n";
			outputString2+= "lat\tlon\tmodRate\n";
			for(int i=0;i<numLocs;i++) {
				outputString1 += (float)locs[i].getLatitude()+"\t"+(float)locs[i].getLongitude()+"\t"+(float)Math.log10(m5_RatesInRELM_Region[i])+"\n";
				outputString2 += (float)locs[i].getLatitude()+"\t"+(float)locs[i].getLongitude()+"\t"+(float)Math.log10(m5_modRates[i])+"\n";
			}
			fw1.write(outputString1);
			fw2.write(outputString2);
			fw1.close();
			fw2.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void main(String[] args) {
		long startTime=System.currentTimeMillis();
		Tests tests = new Tests();
		tests.mkProjectPlanETAS_FigureData();
		int runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("ETAS Tests Run took "+runtime+" seconds");
	}
}