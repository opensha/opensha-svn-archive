package scratchJavaDevelopers.kevin;

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

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.gui.servlets.HazardMapCalcServlet;
import org.opensha.util.FileUtils;

public class MakeXYZFromHazardMapDir {
	
	public static int WRITES_UNTIL_FLUSH = 100;

	public MakeXYZFromHazardMapDir(String dirName, boolean isProbAt_IML, double val, String outFileName, boolean sort) throws IOException {
		// get and list the dir
		File dir = new File(dirName);
		File[] fileList=dir.listFiles();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
		
		int count = 0;
		
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE;
		double maxLon = -9999;
		
		if (sort)
			Arrays.sort(fileList, new FileComparator());

		// for each file in the list
		for(File file : fileList){
			//only taking the files into consideration
			if(file.isFile()){
				String fileName = file.getName();
				//files that ends with ".txt"
				if(fileName.endsWith(".txt")){
					int index = fileName.indexOf("_");
					int firstIndex = fileName.indexOf(".");
					int lastIndex = fileName.lastIndexOf(".");
					// Hazard data files have 3 "." in their names
					//And leaving the rest of the files which contains only 1"." in their names
					if(firstIndex != lastIndex){

						//getting the lat and Lon values from file names
						Double latVal = new Double(fileName.substring(0,index).trim());
						Double lonVal = new Double(fileName.substring(index+1,lastIndex).trim());
						// handle the file
						double writeVal = handleFile(latVal, lonVal, file.getAbsolutePath(), isProbAt_IML, val);
//						out.write(latVal + "\t" + lonVal + "\t" + writeVal + "\n");
						out.write(lonVal + "     " + latVal + "     " + writeVal + "\n");
						
						if (latVal < minLat)
							minLat = latVal;
						else if (latVal > maxLat)
							maxLat = latVal;
						if (lonVal < minLon)
							minLon = lonVal;
						else if (lonVal > maxLon)
							maxLon = lonVal;
						
						if (count % MakeXYZFromHazardMapDir.WRITES_UNTIL_FLUSH == 0) {
							System.out.println("Processed " + count + " curves");
							out.flush();
						}
					}
				}
			}
			count++;
		}
		
		out.close();
		System.out.println("DONE");
		System.out.println("MinLat: " + minLat + " MaxLat: " + maxLat + " MinLon: " + minLon + " MaxLon " + maxLon);
	}


	public double handleFile(double lat, double lon, String fileName, boolean isProbAt_IML, double val) {
		try {
			ArrayList fileLines = FileUtils.loadFile(fileName);
			String dataLine;
			StringTokenizer st;
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();

			for(int i=0;i<fileLines.size();++i) {
				dataLine=(String)fileLines.get(i);
				st=new StringTokenizer(dataLine);
				//using the currentIML and currentProb we interpolate the iml or prob
				//value entered by the user.
				double currentIML = Double.parseDouble(st.nextToken());
				double currentProb= Double.parseDouble(st.nextToken());
				func.set(currentIML, currentProb);
			}
			
			double interpolatedVal = 0;
			if (isProbAt_IML)
	            //final iml value returned after interpolation in log space
				return func.getInterpolatedY_inLogXLogYDomain(val);
	            // for  IML_AT_PROB
	          else //interpolating the iml value in log space entered by the user to get the final iml for the
	            //corresponding prob.
	        	  return func.getFirstInterpolatedX_inLogXLogYDomain(val);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Double.NaN;
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
			MakeXYZFromHazardMapDir maker = new MakeXYZFromHazardMapDir("/home/kevin/OpenSHA/condor/test_results", true, 0.05, "xyzCurves.txt", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
