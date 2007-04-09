package org.opensha.nshmp.sha.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.*;
import java.text.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.data.Location;
import org.opensha.data.function.*;
import org.opensha.nshmp.exceptions.*;
import org.opensha.nshmp.sha.data.api.*;
import org.opensha.nshmp.util.*;
import org.opensha.nshmp.sha.calc.HazardCurveCalculator;
import org.opensha.exceptions.InvalidRangeException;

/**
 * <p>Title: DataGenerator_HazardCurves</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DataGenerator_HazardCurves
    implements DataGeneratorAPI_HazardCurves {

  //gets the selected region
  private String geographicRegion;
  //gets the selected edition
  private String dataEdition;

  //holds all the data and its info in a String format.
  private String dataInfo = "";

  //metadata to be shown when plotting the curves
  private String metadataForPlots;

  private ArbitrarilyDiscretizedFunc hazardCurveFunction;

  private final static double EXP_TIME = 50.0;
  private final static double FREQ_OF_EXCEED_WARNING = 10E-4;
  private static DecimalFormat percentageFormat = new DecimalFormat("0.00");
  private static DecimalFormat saValFormat = new DecimalFormat("0.0000");
  private static DecimalFormat annualExceedanceFormat = new DecimalFormat(
      "0.000E00#");

  public DataGenerator_HazardCurves() {
  }

  /**
   * Gets the data for Hazard Curve in case region specified is not a Territory
   * and user specifies Lat-Lon for the location.
   *
   * @param lat double
   * @param lon double
   * @return ArrayList
   * @todo Implement this org.opensha.nshmp.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void calculateHazardCurve(double lat, double lon, String hazCurveType) throws
      RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getBasicHazardcurve(
        geographicRegion, dataEdition,
        lat, lon, hazCurveType);
    String location = "Lat - " + lat + "  Lon - " + lon;
    createMetadataForPlots(location, hazCurveType);
    addDataInfo(function.getInfo());
    function.setInfo(metadataForPlots);
    function.setName(GlobalConstants.BASIC_HAZARD_CURVE);
    function.setXAxisName(GlobalConstants.HAZARD_CURVE_X_AXIS_NAME + "(" +
                          GlobalConstants.SA_UNITS + ")");
    function.setYAxisName(GlobalConstants.HAZARD_CURVE_Y_AXIS_NAME);
    hazardCurveFunction = function;
  }

  /**
   * Gets the data for Hazard Curve in case region specified is not a Territory
   * and user specifies zip code for the location.
   *
   * @param zipCode String
   * @throws ZipCodeErrorException
   * @return ArrayList
   * @todo Implement this org.opensha.nshmp.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void calculateHazardCurve(String zipCode, String hazCurveType) throws
      ZipCodeErrorException, RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    ArbitrarilyDiscretizedFunc function = miner.getBasicHazardcurve(
        geographicRegion, dataEdition,
        zipCode, hazCurveType);
    String location = "Zipcode - " + zipCode;
    createMetadataForPlots(location, hazCurveType);
    addDataInfo(function.getInfo());
    function.setInfo(metadataForPlots);
    function.setName(GlobalConstants.BASIC_HAZARD_CURVE);
    function.setXAxisName(GlobalConstants.HAZARD_CURVE_X_AXIS_NAME + "(" +
                          GlobalConstants.SA_UNITS + ")");
    function.setYAxisName(GlobalConstants.HAZARD_CURVE_Y_AXIS_NAME);
    hazardCurveFunction = function;
  }

  public void calculateHazardCurve(ArrayList<Location> locations, String hazCurveType, String outFile) {
	  HSSFWorkbook xlOut = getOutputFile(outFile);
		 // Create the output sheet
		 HSSFSheet xlSheet = xlOut.getSheet("Basic Hazard Curve");
		 if(xlSheet==null)
			 xlSheet = xlOut.createSheet("Basic Hazard Curve");
		 
		 /* Write the header information */
		 int startRow = xlSheet.getLastRowNum();
		 startRow = (startRow==0)?0:startRow+2;  // Put an empty row in case there is already data
		 
		 // Create a header style
		 HSSFFont headerFont = xlOut.createFont();
		 headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		 HSSFCellStyle headerStyle = xlOut.createCellStyle();
		 headerStyle.setFont(headerFont);
		 headerStyle.setWrapText(true);
		 
		 // Geographic Region
		 HSSFRow xlRow = xlSheet.createRow(startRow++);
		 xlRow.createCell((short) 0).setCellValue("Geographic Region:");
		 xlRow.getCell((short) 0).setCellStyle(headerStyle);
		 xlRow.createCell((short) 1).setCellValue(geographicRegion);
		 
		 // Data Edition
		 xlRow = xlSheet.createRow(startRow++);
		 xlRow.createCell((short) 0).setCellValue("Data Edition:");
		 xlRow.getCell((short) 0).setCellStyle(headerStyle);
		 xlRow.createCell((short) 1).setCellValue(dataEdition);
		 
		 // IMT
		 xlRow = xlSheet.createRow(startRow++);
		 xlRow.createCell((short) 0).setCellValue("IMT Description:");
		 xlRow.getCell((short) 0).setCellStyle(headerStyle);
		 xlRow.createCell((short) 1).setCellValue(hazCurveType);
		 
		 // B/C Boundary
		 xlRow = xlSheet.createRow(startRow++);
		 xlRow.createCell((short) 1).setCellValue("Data calculated for the B/C boundary");
		 
		 // Disclaimer
		 xlRow = xlSheet.createRow(startRow++);
		 xlRow.createCell((short) 1).setCellValue("FEX Values < 10E-4 Should Be Used With Caution");
		 
		 headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		 ++startRow; // We would like a blank line.
		 // Column Headers
		 xlRow = xlSheet.createRow(startRow++);
		 String[] headers = {"Latitude (Degrees)", "Longitude (Degrees)", "Site Class", "Grid Spacing Basis", "Ground Motion (g)", "Frequency of Exceedance (per year)"};
		 short[] colWidths = {4500, 4500, 3000, 4500, 4500, 5700};
		 for(short i = 0; i < headers.length; ++i) {
			 xlRow.createCell(i).setCellValue(headers[i]);
			 xlRow.getCell(i).setCellStyle(headerStyle);
			 xlSheet.setColumnWidth(i, colWidths[i]);
		 }
		 
		 // Write the data information
		 for(int i = 0; i < locations.size(); ++i) {
			 xlRow = xlSheet.createRow(i+startRow);
			 double curLat = locations.get(i).getLatitude();
			 double curLon = locations.get(i).getLongitude();
			 ArbitrarilyDiscretizedFunc function = null;
			 String curGridSpacing = "";
			 try {
				 HazardDataMinerAPI miner = new HazardDataMinerServletMode();
				 function = miner.getBasicHazardcurve(
				        geographicRegion, dataEdition,
				        curLat, curLon, hazCurveType);
				String reg1 = "^.*Data are based on a ";
				String reg2 = " deg grid spacing.*$";
				curGridSpacing = Pattern.compile(reg1, Pattern.DOTALL).matcher(function.getInfo()).replaceAll("");
				curGridSpacing = Pattern.compile(reg2, Pattern.DOTALL).matcher(curGridSpacing).replaceAll("");
				curGridSpacing += " Degrees";
			} catch (RemoteException e) {
				JOptionPane.showMessageDialog(null, "Failed to retrieve information for:\nLatitude: " +
						curLat + "\nLongitude: " + curLon, "Data Mining Error", JOptionPane.ERROR_MESSAGE);
				function = new ArbitrarilyDiscretizedFunc();
				curGridSpacing = "Location out of Region";
			} finally {
			 xlRow.createCell((short) 0).setCellValue(curLat);
			 xlRow.createCell((short) 1).setCellValue(curLon);
			 xlRow.createCell((short) 2).setCellValue("B/C");
			 xlRow.createCell((short) 3).setCellValue(curGridSpacing);
			 for(int j = 0; j < function.getNum(); ++j) {
				 xlRow.createCell((short) 4).setCellValue(
						 Double.parseDouble(saValFormat.format(function.getX(j))));
				 xlRow.createCell((short) 5).setCellValue(
						 Double.parseDouble(annualExceedanceFormat.format(function.getY(j))));
				 startRow++;
				 xlRow = xlSheet.createRow(i+startRow);
			 }
			} 
		 }
		 
		 try {
			FileOutputStream fos = new FileOutputStream(outFile);
			xlOut.write(fos);
			fos.close();
			// Let the user know that we are done
			dataInfo += "Batch Completed!\nOutput sent to: " + outFile + "\n\n";
		} catch (FileNotFoundException e) {
			// Just ignore for now...
		} catch (IOException e) {
			// Just ignore for now...
		}
  }
  
  public ArrayList<DiscretizedFuncAPI> getHazardCurveFunction() {
    ArrayList<DiscretizedFuncAPI> functionList = new ArrayList<DiscretizedFuncAPI>();
    functionList.add(hazardCurveFunction);
    return functionList;
  }

  private void createMetadataForPlots(String location, String hazCurveType) {
    metadataForPlots = hazCurveType + "\n";
    metadataForPlots += geographicRegion + "\n";
    metadataForPlots += dataEdition + "\n";
    metadataForPlots += location + "\n";
  }

  public void calcSingleValueHazardCurveUsingReturnPeriod(double returnPeriod,
      boolean logInterpolation) throws RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    double fex = 1 / returnPeriod;
    double exceedProb = miner.getExceedProb(fex, EXP_TIME);
    double saVal = 0.0;
    try {
			if (logInterpolation) {
      	saVal = hazardCurveFunction.getFirstInterpolatedX_inLogXLogYDomain(fex);
    	}
    	else {
      	saVal = hazardCurveFunction.getFirstInterpolatedX(fex);
    	}
		} catch (InvalidRangeException ex) {
				double minY = hazardCurveFunction.getY(0);
				int minRtnPeriod = (int) (1.0 / minY);

				String warnMsg = "\nThe return period entered ("+returnPeriod+") " +
					"is out of range.\nThe nearest return period within the range " +
					"("+minRtnPeriod+") is was used instead.";

				dataInfo += warnMsg;
				fex = minY;
				returnPeriod = minRtnPeriod;
				//fex = 1 / returnPeriod;
				exceedProb = miner.getExceedProb(fex, EXP_TIME);
				saVal = 0.0;

			if (logInterpolation) {
      	saVal = hazardCurveFunction.getFirstInterpolatedX_inLogXLogYDomain(fex);
    	}
    	else {
      	saVal = hazardCurveFunction.getFirstInterpolatedX(fex);
			}
		}
		
    addDataFromSingleHazardCurveValue(fex, returnPeriod, exceedProb, EXP_TIME,
                                      saVal);
  }

  public void calcSingleValueHazardCurveUsingPEandExptime(double probExceed,
      double expTime, boolean logInterpolation) throws RemoteException {
    HazardDataMinerAPI miner = new HazardDataMinerServletMode();
    double returnPd = miner.getReturnPeriod(probExceed, expTime);
    double fex = 1 / returnPd;
    double saVal = 0.0;
    try {
			if (logInterpolation) {
      saVal = hazardCurveFunction.getFirstInterpolatedX_inLogXLogYDomain(fex);
    }
    else {
      saVal = hazardCurveFunction.getFirstInterpolatedX(fex);
    }
		} catch (InvalidRangeException ex) {
				double minY = hazardCurveFunction.getY(0);
				int minRtnPeriod = (int) (1.0 / minY);

				String warnMsg = "\nThe calculated return period ("+returnPd+") " +
					"based on the\nentered probability ("+probExceed+") and time (" +
					expTime+"), is out of range.\nThe nearest valid return period " +
					"("+minRtnPeriod+") was used instead.";
				/*String warnMsg = "\nThe return period entered ("+returnPd+") " +
					"is out of range.\nThe nearest return period within the range " +
					"("+minRtnPeriod+") is was used instead.";
				*/
				dataInfo += warnMsg;
				fex = minY;
				returnPd = minRtnPeriod;

			if (logInterpolation) {
      	saVal = hazardCurveFunction.getFirstInterpolatedX_inLogXLogYDomain(fex);
    	}
    	else {
      	saVal = hazardCurveFunction.getFirstInterpolatedX(fex);
			}

		}
    addDataFromSingleHazardCurveValue(fex, returnPd, probExceed, expTime, saVal);
  }

  private void addDataFromSingleHazardCurveValue(double fex, double returnPd,
                                                 double probExceed,
                                                 double expTime,
                                                 double groundMotion) {
				
		String gmMain = "Ground Motion";
		String gmSub = "(g)";
		String gmDat = "" + saValFormat.format(groundMotion);

		String sFexMain = "Freq. of Exceed.";
		String sFexSub = "(per year)";
		String sFexDat = "" + annualExceedanceFormat.format(fex);

		String rPdMain = "Return Pd.";
		String rPdSub = "(years)";
		String rPdDat = "" + returnPd;

		String pExMain = "P.E.";
		String pExSub = "%";
		String pExDat = "" + percentageFormat.format(probExceed);

		String eTimeMain = "Exp. Time";
		String eTimeSub = "(years)";
		String eTimeDat = "" + expTime;


		String line1 = center(gmMain, 15) + center(sFexMain, 20) +
			center(rPdMain, 14) + center(pExMain, 10) + center(eTimeMain, 11) + "\n";
		
		String line2 = center(gmSub, 15) + center(sFexSub, 20) +
			center(rPdSub, 14) + center(pExSub, 10) + center(eTimeSub, 11) + "\n";

		String line3 = center(gmDat, 15) + center(sFexDat, 20) +
			center(rPdDat, 14) + center(pExDat, 10) + center(eTimeDat, 11) + "\n";
			
    dataInfo += "\n\n" + line1 + line2 + line3;

    if(fex<FREQ_OF_EXCEED_WARNING)
      dataInfo+="\n"+HazardCurveCalculator.EXCEED_PROB_TEXT+"\n";
  }

  private static String center(String str, int width) {
		int strLen = str.length();
		if (strLen >= width ) return str;

		String result = str;
		int dif = width - strLen;
		dif = dif / 2;
		for(int i = 0; i < dif; ++i) {
			result = " " + result;
		}
		while(result.length() < width) {
			result = result + " ";
		}
		return result;
	}
																																	
  /**
   * Removes all the calculated data.
   *
   * @todo Implement this org.opensha.nshmp.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void clearData() {
    dataInfo = "";
  }

  private void addDataInfo(String data) {
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo += data + "\n\n";
  }

  /**
   * Returns the Data and all the metadata associated with it in a String.
   *
   * @return String
   * @todo Implement this org.opensha.nshmp.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public String getDataInfo() {
    return dataInfo;
  }

  /**
   * Sets the selected data edition.
   *
   * @param edition String
   * @todo Implement this org.opensha.nshmp.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void setEdition(String edition) {
    dataEdition = edition;
  }

  /**
   * Sets the selected geographic region.
   *
   * @param region String
   * @todo Implement this org.opensha.nshmp.sha.data.api.DataGeneratorAPI_HazardCurves
   *   method
   */
  public void setRegion(String region) {
    geographicRegion = region;
  }

  private HSSFWorkbook getOutputFile(String outFile) {
	  // Create the Excel output file, or open it if it already exists
	  File outBook = new File(outFile);
	  HSSFWorkbook xlsOut = null;
	  if(outBook.exists()) {
		  try {
			  POIFSFileSystem fs = new
			  	POIFSFileSystem(new FileInputStream(outBook));
			  xlsOut = new HSSFWorkbook(fs);
		  } catch (FileNotFoundException e) {
			  // This won't happen since we just checked that it exists
			  // But we'll alert you anyway.
			  JOptionPane.showMessageDialog(null, "The file "+outFile+" was not found.",
					  "File Not Found", JOptionPane.ERROR_MESSAGE);
			  return new HSSFWorkbook();
		  } catch (IOException e) {
			  JOptionPane.showMessageDialog(null, "Failed to open file: " + outFile,
					  "I/O Failure", JOptionPane.ERROR_MESSAGE);
			  return new HSSFWorkbook();
		}
	  } else {
		  xlsOut = new HSSFWorkbook();
	  }
	  return xlsOut;
  }
}
