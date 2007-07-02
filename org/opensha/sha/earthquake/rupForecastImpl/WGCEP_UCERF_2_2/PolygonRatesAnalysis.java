/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2;

import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.data.region.GeographicRegion;
import org.opensha.sha.calc.ERF2GriddedSeisRatesCalc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data.EmpiricalModelDataFetcher;

/**
 * Analyze the rate in various polygons as defined in Appendix I
 * 
 * @author vipingupta
 *
 */
public class PolygonRatesAnalysis {

	private EqkRateModel2_ERF eqkRateModelERF = new EqkRateModel2_ERF();
	private EvenlyGriddedRELM_Region relmRegion = new EvenlyGriddedRELM_Region();
	private EmpiricalModelDataFetcher empiricalModelFetcher = new EmpiricalModelDataFetcher();
	private ERF2GriddedSeisRatesCalc erf2GriddedSeisRatesCalc = new ERF2GriddedSeisRatesCalc(); 
	private final static double MIN_MAG = 5.0;
	
	public PolygonRatesAnalysis() {
		eqkRateModelERF.updateForecast();
		double totRate = erf2GriddedSeisRatesCalc.getTotalSeisRateInRegion(MIN_MAG, eqkRateModelERF, relmRegion);
		int numPolygons = empiricalModelFetcher.getNumRegions();
		System.out.println("Total rate in RELM region:"+totRate);
		double rateInPoly;
		for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
			GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
			if(polygon.getRegionOutline()==null) continue;
			rateInPoly = erf2GriddedSeisRatesCalc.getTotalSeisRateInRegion(MIN_MAG, eqkRateModelERF, polygon);
			System.out.println("Rate in region "+polygon.getName()+" is "+rateInPoly);
		}
	}		
	public static void main(String[] args) {
		PolygonRatesAnalysis polygonRatesAnalysis = new PolygonRatesAnalysis();
	}
		

	
}
