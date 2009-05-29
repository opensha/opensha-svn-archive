/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.EvenlyGriddedRELM_Region;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.sha.earthquake.calc.ERF2GriddedSeisRatesCalc;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data.EmpiricalModelDataFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;

/**
 * Analyze the rate in various polygons as defined in Appendix I
 * 
 * @author vipingupta
 *
 */
public class PolygonRatesAnalysis {

	private UCERF2 ucerf2 = new UCERF2();
	private EvenlyGriddedRELM_Region relmRegion = new EvenlyGriddedRELM_Region();
	private EmpiricalModelDataFetcher empiricalModelFetcher = new EmpiricalModelDataFetcher();
	private ERF2GriddedSeisRatesCalc erf2GriddedSeisRatesCalc = new ERF2GriddedSeisRatesCalc(); 
	private final static double MIN_MAG = 5.0;
	private final static String PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/data/";
	private final static String A_FAULT_FILENAME = PATH+"A_FaultsPolygonFractions.txt";
	private final static String B_FAULT_FILENAME = PATH+"B_FaultsPolygonFractions.txt";
	private final static String C_ZONES_FILENAME = PATH+"C_ZonesPolygonFractions.txt";
	
	public PolygonRatesAnalysis() {
		ucerf2.updateForecast();
	}		

	/**
	 * Caluclate rates in polygons
	 *
	 */
	public void calcRatesInPolygons() {
		double totRate = erf2GriddedSeisRatesCalc.getTotalSeisRateInRegion(MIN_MAG, ucerf2, relmRegion);
		int numPolygons = empiricalModelFetcher.getNumRegions();
		System.out.println("Total rate in RELM region:"+totRate);
		double rateInPoly;
		double rateRestOfRegion = totRate;
		for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
			GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
			if(polygon.getRegionOutline()==null) continue;
			rateInPoly = erf2GriddedSeisRatesCalc.getTotalSeisRateInRegion(MIN_MAG, ucerf2, polygon);
			rateRestOfRegion-=rateInPoly;
			System.out.println("Rate in region "+polygon.getName()+" is "+rateInPoly);
		}
		System.out.println("Rate in rest of region is "+rateRestOfRegion);
	}
	
	/**
	 * For each A-Fault, find the fraction that lies in each polygon
	 *
	 */
	public void computeA_SourcesFraction() {
		ArrayList aFaultGenerators = ucerf2.get_A_FaultSourceGenerators();
		int numA_Faults = aFaultGenerators.size();
		
		try {
			FileWriter fw = new FileWriter(A_FAULT_FILENAME);
			fw.write(getHeader());
			// iterate over all source generators
			for(int i=0; i<numA_Faults; ++i) {
			
				// for segmented source
				if(aFaultGenerators.get(i) instanceof A_FaultSegmentedSourceGenerator) {
					A_FaultSegmentedSourceGenerator srcGen = (A_FaultSegmentedSourceGenerator)aFaultGenerators.get(i);
					ArrayList<FaultRuptureSource> aFaultSources = srcGen.getTimeIndependentSources(1.0);
					int numSrc = aFaultSources.size();
					// iterate over all sources
					for(int srcIndex=0; srcIndex<numSrc; ++srcIndex) {
						FaultRuptureSource faultRupSrc = aFaultSources.get(srcIndex);
						EvenlyGriddedSurfaceAPI surface  = faultRupSrc.getSourceSurface();
						findFractionOfPointsInPolygons(fw, srcGen.getFaultSegmentData().getFaultName(), srcIndex, surface);
					}
				} else { // unsegmented source
					UnsegmentedSource unsegmentedSource = (UnsegmentedSource)aFaultGenerators.get(i);
					EvenlyGriddedSurfaceAPI surface  = unsegmentedSource.getSourceSurface();
					findFractionOfPointsInPolygons(fw, unsegmentedSource.getName(), i, surface);

				}
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getHeader() {
		String header = "#FaultName, Index,RELM Region";
		int numPolygons = empiricalModelFetcher.getNumRegions();
		for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
			GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
			header+=","+polygon.getName();
		}
		header+="\n";
		return header;
	}
	
	
	/**
	 * For each B-Fault, find the fraction that lies in each polygon
	 *
	 */
	public void computeB_SourcesFraction() {
		ArrayList bFaultSources = ucerf2.get_B_FaultSources();
		int numB_Faults = bFaultSources.size();
		
		try {
			FileWriter fw = new FileWriter(B_FAULT_FILENAME);
			fw.write(getHeader());
			// iterate over all sources
			for(int i=0; i<numB_Faults; ++i) {
				UnsegmentedSource unsegmentedSource = (UnsegmentedSource)bFaultSources.get(i);
				EvenlyGriddedSurfaceAPI surface  = unsegmentedSource.getSourceSurface();
				findFractionOfPointsInPolygons(fw, unsegmentedSource.getName(), i, surface);
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Fro each C-zone, find the fraction that lies in each polygon
	 *
	 */
	public void computeC_SourcesFraction() {
		NSHMP_GridSourceGenerator nshmpGridSrcGen = new NSHMP_GridSourceGenerator();
		String PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/";
		try {
			FileWriter fw = new FileWriter(C_ZONES_FILENAME);
			fw.write(getHeader());
			double [] area1new_agrid  = nshmpGridSrcGen.readGridFile(PATH+"area1new.agrid.asc",true);
			int cZoneIndex=0;
			calcFractionC_Zone(nshmpGridSrcGen, fw, "Area 1", area1new_agrid, cZoneIndex);
			double [] area2new_agrid = nshmpGridSrcGen.readGridFile(PATH+"area2new.agrid.asc",true);
			calcFractionC_Zone(nshmpGridSrcGen, fw, "Area 2", area2new_agrid, ++cZoneIndex);
			double [] area3new_agrid = nshmpGridSrcGen.readGridFile(PATH+"area3new.agrid.asc",true);
			calcFractionC_Zone(nshmpGridSrcGen, fw, "Area 3", area3new_agrid, ++cZoneIndex);
			double [] area4new_agrid = nshmpGridSrcGen.readGridFile(PATH+"area4new.agrid.asc",true);
			calcFractionC_Zone(nshmpGridSrcGen, fw, "Area 4", area4new_agrid, ++cZoneIndex);
			double [] mojave_agrid = nshmpGridSrcGen.readGridFile(PATH+"mojave.agrid.asc",true);
			calcFractionC_Zone(nshmpGridSrcGen, fw, "Mojave", mojave_agrid, ++cZoneIndex);
			double [] sangreg_agrid = nshmpGridSrcGen.readGridFile(PATH+"sangreg.agrid.asc",true);
			calcFractionC_Zone(nshmpGridSrcGen, fw, "San Gregonio", sangreg_agrid, ++cZoneIndex);
			fw.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		

	}

	private void calcFractionC_Zone(NSHMP_GridSourceGenerator nshmpGridSrcGen, FileWriter fw, String cZoneName, double[] area1new_agrid, int cZoneIndex) throws IOException {
		int numPolygons = empiricalModelFetcher.getNumRegions();
		int []pointInEachPolygon = new int[numPolygons];
		int totPointsInRELM_Region = 0;
		for(int i=0; i<area1new_agrid.length; ++i) {
			if(area1new_agrid[i]==0) continue; // if the rate is 0 at this location
			++totPointsInRELM_Region;
			Location loc = nshmpGridSrcGen.getGridLocation(i);
			for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
				GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
				if(polygon.getRegionOutline()==null) continue;
				if(polygon.isLocationInside(loc)) {
					++pointInEachPolygon[regionIndex];
					break;
				}
			}
		}
		fw.write(cZoneName+","+cZoneIndex+","+ 
				totPointsInRELM_Region/(float)totPointsInRELM_Region);	
		int pointsOutsidePolygon = totPointsInRELM_Region;
		for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
			GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
			pointsOutsidePolygon-=pointInEachPolygon[regionIndex];
			if(polygon.getRegionOutline()!=null)
				fw.write(","+pointInEachPolygon[regionIndex]/(float)totPointsInRELM_Region);
		}
		fw.write(","+pointsOutsidePolygon/(float)totPointsInRELM_Region+"\n");
	}

	/**
	 * Find the fraction of points in each polygon
	 * 
	 * @param fw
	 * @param srcIndex
	 * @param surface
	 * @throws IOException
	 */
	private void findFractionOfPointsInPolygons(FileWriter fw, String faultName, int srcIndex, EvenlyGriddedSurfaceAPI surface) throws IOException {
		int numPolygons = empiricalModelFetcher.getNumRegions();
		int []pointInEachPolygon = new int[numPolygons];
		int numPoints = surface.getNumCols();
		int totPointsInRELM_Region = 0;
		// iterate over all surface point locations
		for(int ptIndex=0; ptIndex<numPoints; ++ptIndex) {
			Location loc = surface.getLocation(0, ptIndex);
			if(this.relmRegion.isLocationInside(loc)) ++totPointsInRELM_Region;
			for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
				GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
				if(polygon.getRegionOutline()==null) continue;
				if(polygon.isLocationInside(loc)) {
					++pointInEachPolygon[regionIndex];
					break;
				}
			}
		}
		fw.write(faultName+","+srcIndex+","+ 
				totPointsInRELM_Region/(float)numPoints);	
		int pointsOutsidePolygon = totPointsInRELM_Region;
		for(int regionIndex=0; regionIndex<numPolygons; ++regionIndex) {
			GeographicRegion polygon = empiricalModelFetcher.getRegion(regionIndex);
			pointsOutsidePolygon-=pointInEachPolygon[regionIndex];
			if(polygon.getRegionOutline()!=null)
				fw.write(","+pointInEachPolygon[regionIndex]/(float)numPoints);
		}
		fw.write(","+pointsOutsidePolygon/(float)numPoints+"\n");
	}



	public static void main(String[] args) {
		PolygonRatesAnalysis polygonRatesAnalysis = new PolygonRatesAnalysis();
		polygonRatesAnalysis.computeA_SourcesFraction();
		polygonRatesAnalysis.computeB_SourcesFraction();
		polygonRatesAnalysis.computeC_SourcesFraction();
	}



}
