package org.opensha.sha.calc.hazardMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.util.FileUtils;

public class MakeXYZFromHazardMapDir {
	
	public static int WRITES_UNTIL_FLUSH = 1000;
	
	private boolean latFirst;
	private boolean sort;
	private String dirName;

	public MakeXYZFromHazardMapDir(String dirName, boolean sort, boolean latFirst) {
		this.dirName = dirName;
		this.latFirst = latFirst;
		this.sort = sort;
	}
	
	public void writeXYZFile(boolean isProbAt_IML, double level, String fileName) throws IOException {
		parseFiles(isProbAt_IML, level, fileName, false);
	}
	
	public XYZ_DataSetAPI getXYZDataset(boolean isProbAt_IML, double level) throws IOException {
		return parseFiles(isProbAt_IML, level, null, false);
	}
	
	public XYZ_DataSetAPI getXYZDataset(boolean isProbAt_IML, double level, String fileName) throws IOException {
		return parseFiles(isProbAt_IML, level, fileName, true);
	}
	
	private XYZ_DataSetAPI parseFiles(boolean isProbAt_IML, double level, String fileName,
			boolean forceLoad) throws IOException {
		// get and list the dir
		System.out.println("Generating XYZ dataset for dir: " + dirName);
		File masterDir = new File(dirName);
		File[] dirList=masterDir.listFiles();
		
		BufferedWriter out = null;
		ArbDiscretizedXYZ_DataSet xyz = null;
		
		if (fileName != null && fileName.length() > 0) {
			out = new BufferedWriter(new FileWriter(fileName));
		}
		if (out == null || forceLoad) {
			xyz = new ArbDiscretizedXYZ_DataSet();
		}
		
		int count = 0;
		
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE;
		double maxLon = -9999;
		
		if (sort)
			Arrays.sort(dirList, new FileComparator());

		// for each file in the list
		for(File dir : dirList){
			// make sure it's a subdirectory
			if (dir.isDirectory() && !dir.getName().endsWith(".")) {
				File[] subDirList=dir.listFiles();
				for(File file : subDirList) {
					//only taking the files into consideration
					if(file.isFile()){
						String curveFileName = file.getName();
						//files that ends with ".txt"
						if(curveFileName.endsWith(".txt")){
							Location loc = decodeFileName(curveFileName);
							if (loc != null) {
								double latVal = loc.getLatitude();
								double lonVal = loc.getLongitude();
								//System.out.println("Lat: " + latVal + " Lon: " + lonVal);
								// handle the file
								double writeVal = handleFile(file.getAbsolutePath(), isProbAt_IML, level);
//								out.write(latVal + "\t" + lonVal + "\t" + writeVal + "\n");
								if (latFirst) {
									if (out != null)
										out.write(latVal + "     " + lonVal + "     " + writeVal + "\n");
									if (xyz != null)
										xyz.addValue(latVal, lonVal, writeVal);
								} else {
									if (out != null)
										out.write(lonVal + "     " + latVal + "     " + writeVal + "\n");
									if (xyz != null)
										xyz.addValue(lonVal, latVal, writeVal);
								}
								
								if (latVal < minLat)
									minLat = latVal;
								else if (latVal > maxLat)
									maxLat = latVal;
								if (lonVal < minLon)
									minLon = lonVal;
								else if (lonVal > maxLon)
									maxLon = lonVal;
								
								if (out != null && count % MakeXYZFromHazardMapDir.WRITES_UNTIL_FLUSH == 0) {
									System.out.println("Processed " + count + " curves");
									out.flush();
								}
							}
						}
					}
					count++;
				}
			}
			
			
		}
		
		if (out != null)
			out.close();
		System.out.println("DONE");
		System.out.println("MinLat: " + minLat + " MaxLat: " + maxLat + " MinLon: " + minLon + " MaxLon " + maxLon);
		System.out.println(count + " curves processed!");
		
		return xyz;
	}
	
	/**
	 * Decodes a filename of the format lat_lon.txt
	 * 
	 * @param fileName
	 * @return
	 */
	public static Location decodeFileName(String fileName) {
		int index = fileName.indexOf("_");
		int firstIndex = fileName.indexOf(".");
		int lastIndex = fileName.lastIndexOf(".");
		// Hazard data files have 3 "." in their names
		//And leaving the rest of the files which contains only 1"." in their names
		if(firstIndex != lastIndex){

			//getting the lat and Lon values from file names
			Double latVal = new Double(fileName.substring(0,index).trim());
			Double lonVal = new Double(fileName.substring(index+1,lastIndex).trim());
			return new Location(latVal, lonVal);
		} else {
			return null;
		}
	}


	public double handleFile(String fileName, boolean isProbAt_IML, double val) {
		try {
			ArbitrarilyDiscretizedFunc func = DiscretizedFunc.loadFuncFromSimpleFile(fileName);
			
			return getCurveVal(func, isProbAt_IML, val);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Double.NaN;
	}
	
	public static double getCurveVal(DiscretizedFuncAPI func, boolean isProbAt_IML, double val) {
		if (isProbAt_IML)
			//final iml value returned after interpolation in log space
			return func.getInterpolatedY_inLogXLogYDomain(val);
		// for  IML_AT_PROB
		else { //interpolating the iml value in log space entered by the user to get the final iml for the
			//corresponding prob.
			double out;
			try {
				out = func.getFirstInterpolatedX_inLogXLogYDomain(val);
				return out;
			} catch (RuntimeException e) {
				System.err.println("WARNING: Probability value doesn't exist, setting IMT to NaN");
				//return 0d;
				return Double.NaN;
			}
		}
	}

	private static class FileComparator implements Comparator {
		private Collator c = Collator.getInstance();

		public int compare(Object o1, Object o2) {
			if(o1 == o2)
				return 0;

			File f1 = (File) o1;
			File f2 = (File) o2;

			if(f1.isDirectory() && f2.isFile())
				return -1;
			if(f1.isFile() && f2.isDirectory())
				return 1;
			
			

			return c.compare(invertFileName(f1.getName()), invertFileName(f2.getName()));
		}
		
		public String invertFileName(String fileName) {
			int index = fileName.indexOf("_");
			int firstIndex = fileName.indexOf(".");
			int lastIndex = fileName.lastIndexOf(".");
			// Hazard data files have 3 "." in their names
			//And leaving the rest of the files which contains only 1"." in their names
			if(firstIndex != lastIndex){

				//getting the lat and Lon values from file names
				String lat = fileName.substring(0,index).trim();
				String lon = fileName.substring(index+1,lastIndex).trim();
				
				return lon + "_" + lat;
			}
			return fileName;
		}
	}
	
	public static void main(String args[]) {
		try {
//			String curveDir = "/home/kevin/OpenSHA/condor/test_results";
//			String curveDir = "/home/kevin/OpenSHA/condor/oldRuns/statewide/test_30000_2/curves";
//			String curveDir = "/home/kevin/OpenSHA/condor/frankel_0.1";
			String curveDir = "/home/kevin/CyberShake/baseMaps/ave2008/curves_3sec";
//			String outfile = "xyzCurves.txt";
//			String outfile = "/home/kevin/OpenSHA/condor/oldRuns/statewide/test_30000_2/xyzCurves.txt";
			String outfile = "/home/kevin/CyberShake/baseMaps/ave2008/xyzCurves.txt";
//			boolean isProbAt_IML = true;
//			double level = 0.2;
			boolean isProbAt_IML = false;
			double level = 0.002;		// 10% in 50
//			double level = 0.0004;		// 	2% in 50
			boolean latFirst = true;
			MakeXYZFromHazardMapDir maker = new MakeXYZFromHazardMapDir(curveDir, false, latFirst);
			maker.writeXYZFile(isProbAt_IML, level, outfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
