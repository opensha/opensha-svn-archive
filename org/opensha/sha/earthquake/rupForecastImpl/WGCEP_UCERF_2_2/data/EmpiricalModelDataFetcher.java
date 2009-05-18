/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.util.FileUtils;

/**
 * This class reads EmpiricalModelData.txt for time dependent forecast
 * 
 * 
 * @author vipingupta
 *
 */
public class EmpiricalModelDataFetcher {
	public static String FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/data/EmpiricalModelData.txt";
	private ArrayList<GeographicRegion> geographicRegionList = new ArrayList<GeographicRegion>();
	private ArrayList<Double> rates = new ArrayList<Double>();
	private ArrayList<Double> stdDevs = new ArrayList<Double>();
	
	/**
	 * Read EmpiricalModelData.txt file 
	 *
	 */
	public EmpiricalModelDataFetcher() {
		try {
			ArrayList<String> fileLines = FileUtils.loadJarFile(FILE_NAME);
			int numLines = fileLines.size();
			GeographicRegion region;
			for(int i=0; i<numLines; ++i) {
				String line = fileLines.get(i);
				if(line.startsWith("#")) continue; // ignore comment lines
				if(line.startsWith("-")) {
					 region = new GeographicRegion();
					 String regionName = line.substring(1).trim(); 
					 region.setName(regionName);
					 ++i;
					 StringTokenizer rateTokenizer = new StringTokenizer(fileLines.get(i),",");
					 rates.add(Double.parseDouble(rateTokenizer.nextToken()));
					 stdDevs.add(Double.parseDouble(rateTokenizer.nextToken()));
					 ++i;
					 int numLocPoints = Integer.parseInt(fileLines.get(i));
					 LocationList locList = new LocationList();
					 for(int locIndex=0; locIndex<numLocPoints; ++locIndex) {
						 ++i;
						 StringTokenizer locTokenizer = new StringTokenizer(fileLines.get(i),",");
						 double latitude = Double.parseDouble(locTokenizer.nextToken());
						 double longitude = Double.parseDouble(locTokenizer.nextToken());
						 locList.addLocation(new Location(latitude, longitude));
					 }
					 if(locList.size()!=0)
						 region.createGeographicRegion(locList);
					 geographicRegionList.add(region);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the number of polygons
	 * 
	 * @return
	 */
	public int getNumRegions() {
		return this.geographicRegionList.size();
	}
	
	/**
	 * Get region at specified index
	 * 
	 * @param index
	 * @return
	 */
	public GeographicRegion getRegion(int index) {
		return this.geographicRegionList.get(index);
	}
	
	/**
	 * Get the rate for region at specified index
	 * 
	 * @param index
	 * @return
	 */
	public double getRate(int index) {
		return this.rates.get(index);
	}
	
	/**
	 * Get uncertanity for region at specified index
	 * 
	 * @param index
	 * @return
	 */
	public double getStdDev(int index) {
		return this.stdDevs.get(index);
	}
	
	public static void main(String args[]) {
		EmpiricalModelDataFetcher empModelDataFetcher = new EmpiricalModelDataFetcher();
		int numRegions = empModelDataFetcher.getNumRegions();
		System.out.println(numRegions);
		for(int i=0; i<numRegions; ++i) {
			System.out.println(empModelDataFetcher.getRegion(i).getName());
			System.out.println(empModelDataFetcher.getRate(i));
			System.out.println(empModelDataFetcher.getStdDev(i));
		}
	}
	
}
