package org.opensha.sha.gui.infoTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.sha.calc.GridMetadataHazardMapCalculator;
import org.opensha.sha.calc.hazardMap.HazardMapJob;
import org.opensha.util.FileUtils;
import org.opensha.util.MailUtil;


public class HazardMapXMLPostProcessor {
	
	static final String FROM = "OpenSHA";
	static final String HOST = "email.usc.edu";

	
	public static void main(String args[]) {
		
		if (args.length == 0) {
			System.err.println("RUNNING FROM DEBUG MODE!");
			args = new String[1];
			args[0] = "output.xml";
		}
		
		String metadata = args[0];
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File(metadata));
			Element root = document.getRootElement();
			
			HazardMapJob job = HazardMapJob.fromXMLMetadata(root.element(HazardMapJob.XML_METADATA_NAME));
			
			File masterDir = new File("curves/");
			File[] dirList=masterDir.listFiles();
			
			double minLat = Double.MAX_VALUE;
			double minLon = Double.MAX_VALUE;
			double maxLat = Double.MIN_VALUE;
			double maxLon = -9999;
			
			int actualFiles = 0;
			
			// for each file in the list
			for(File dir : dirList){
				// make sure it's a subdirectory
				if (dir.isDirectory() && !dir.getName().endsWith(".")) {
					File[] subDirList=dir.listFiles();
					for(File file : subDirList) {
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
									
									
									if (latVal < minLat)
										minLat = latVal;
									else if (latVal > maxLat)
										maxLat = latVal;
									if (lonVal < minLon)
										minLon = lonVal;
									else if (lonVal > maxLon)
										maxLon = lonVal;
									
								}
							}
						}
						actualFiles++;
					}
				}
				
				
			}
			
			System.out.println("DONE");
			System.out.println("MinLat: " + minLat + " MaxLat: " + maxLat + " MinLon: " + minLon + " MaxLon " + maxLon);
			
			// get the start time
			long startTime = 0;
			long endTime = System.currentTimeMillis();
			File startTimeFile = new File(GridMetadataHazardMapCalculator.START_TIME_FILE);
			if (startTimeFile.exists()) {
				try {
					ArrayList<String> startTimeLines = FileUtils.loadFile(GridMetadataHazardMapCalculator.START_TIME_FILE);
					if (startTimeLines != null) {
						if (startTimeLines.size() > 0) {
							String startTimeString = startTimeLines.get(0);
							startTime = Long.parseLong(startTimeString);
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			String startTimeString = "";
			Calendar startCal = null;
			if (startTime > 0) {
				startCal = Calendar.getInstance();
				startCal.setTimeInMillis(startTime);
				startTimeString = startCal.getTime().toString();
			} else {
				startTimeString = "Start Time Not Available!";
			}
			
			Calendar endCal = Calendar.getInstance();
			endCal.setTimeInMillis(endTime);
			String endTimeString = endCal.getTime().toString();
			
			Element regionElement = root.element(EvenlyGriddedGeographicRegion.XML_METADATA_NAME);
			EvenlyGriddedGeographicRegion region = EvenlyGriddedGeographicRegion.fromXMLMetadata(regionElement);
			
			Element calcParams = root.element("calculationParameters");
			String emailAddress =  calcParams.attribute("emailAddress").getValue();
			
			String mailSubject = "Grid Job Status";
			String mailMessage = "THIS IS A AUTOMATED GENERATED EMAIL. PLEASE DO NOT REPLY BACK TO THIS ADDRESS.\n\n\n"+
			"Grid Computation complete\n"+
			"Expected Num of Files="+region.getNumGridLocs()+"\n"+
			"Files Generated="+actualFiles+"\n"+
			"Dataset Id="+job.jobName+"\n"+
			"Simulation Start Time="+startTimeString+"\n"+
			"Simulation End Time="+endTimeString;
			
			if (startCal == null) {
				mailMessage += "\nPerformance Statistics not Available";
			} else {
				long millis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
				double secs = (double)millis / 1000d;
				double mins = secs / 60d;
				double hours = mins / 60d;
				
				mailMessage += "\nTotal Run Time (including overhead):\n";
				if (hours > 1)
					mailMessage += new DecimalFormat(	"###.##").format(hours) + " hours = ";
				mailMessage += new DecimalFormat(	"###.##").format(mins) + " minutes";
				double curvesPerHour = (double)actualFiles / hours;
				mailMessage += "\nCurves Per Hour (including overhead): " + curvesPerHour;
			}
			MailUtil.sendMail(HOST, FROM, emailAddress, mailSubject, mailMessage);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
