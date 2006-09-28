package org.opensha.nshmp.sha.data;

import java.rmi.*;
import java.text.*;
import java.util.*;

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

  public ArrayList getHazardCurveFunction() {
    ArrayList functionList = new ArrayList();
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
}
