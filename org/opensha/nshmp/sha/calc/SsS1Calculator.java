package org.opensha.nshmp.sha.calc;

import gov.usgs.db.DBHazardConnection;
import gov.usgs.util.MathUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.nshmp.exceptions.ZipCodeErrorException;
import org.opensha.nshmp.sha.data.SiteInterpolation;
import org.opensha.nshmp.sha.io.DataFileNameSelector;
import org.opensha.nshmp.sha.io.DataFileNameSelectorForFEMA;
import org.opensha.nshmp.sha.io.NEHRP_Record;
import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.nshmp.util.LocationUtil;
import org.opensha.nshmp.util.ZipCodeToLatLonConvertor;
import org.opensha.nshmp.util.ui.DataDisplayFormatter;

/**
 * <p>Title: SsS1Calculator</p>
 *
 * <p>Description: Computes the values for the Ss and S1 for the given location or
 * territory in USA.</p>
 * @author  Ned Field, Nitin Gupta , E.V.Leyendecker
 * @version 1.0
 */
public class SsS1Calculator {

  //grid spacing in file
  protected float gridSpacing;
  
  protected Connection conn = null;
  protected PreparedStatement query = null;
  protected static final String STUB = "SELECT * FROM hc_owner.HC_DATA_2008_VW " +
  		"WHERE LAT >= ? AND LAT <= ? AND LON >= ? AND LON <= ?" +
  		"ORDER BY LAT DESC, LON ASC";
  
  protected static final String SSUH_COL = "SEC_0_2";
  protected static final String S1UH_COL = "SEC_1_0";
  protected static final String SSDET_COL = "SEC_0_2_DET";
  protected static final String S1DET_COL = "SEC_1_0_DET";
  protected static final String SSCR_COL = "SEC_0_2_CR";
  protected static final String S1CR_COL = "SEC_1_0_CR";
  
  protected static final int SS_IDX = 0;
  protected static final int S1_IDX = 1;
  protected static final int SSUH_IDX = 2;
  protected static final int S1UH_IDX = 3;
  protected static final int SSDET_IDX = 4;
  protected static final int S1DET_IDX = 5;
  protected static final int SSCR_IDX = 6;
  protected static final int S1CR_IDX = 7;

  /**
   * Some static String for the data printing
   */
  protected static final String SsS1_TITLE =
      "Spectral Response Accelerations Ss and S1";
  protected static final String SsS1_SubTitle =
      "Ss and S1 = Mapped Spectral Acceleration Values";

  protected static final String Ss_Text = "Ss";
  protected static final String S1_Text = "S1";
  protected static final String SA = "Sa";
  protected static final String CENTROID_SA = "Centroid Sa";
  protected static final String MINIMUM_SA = "Minimum Sa";
  protected static final String MAXIMUM_SA = "Maximum Sa";
  protected static final float Fa = 1;
  protected static final float Fv = 1;

  protected DecimalFormat latLonFormat = new DecimalFormat("0.0000##");

  public SsS1Calculator() {
	  try {
		  conn = (new DBHazardConnection()).getConnection();
		  query = conn.prepareStatement(STUB);
	  } catch (SQLException sqx) {
		  /* Ignore for now. */
	  }
	  
  }
  /**
   *
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String selectedRegion,
                                            String selectedEdition,
                                            double latitude, double longitude) {
    ArbitrarilyDiscretizedFunc function =  null;

	
    if (selectedEdition.equals(GlobalConstants.NEHRP_2009)) {
    	try {
    		gridSpacing = (float) 0.05; // We know this.
    		double minLat = MathUtils.precisionFloor(latitude, gridSpacing);
    		double maxLat = MathUtils.precisionCeil(latitude, gridSpacing);
    		double minLng = MathUtils.precisionFloor(longitude, gridSpacing);
    		double maxLng = MathUtils.precisionCeil(longitude, gridSpacing);
	    	query.setDouble(1, minLat);
	    	query.setDouble(2, maxLat);
	    	query.setDouble(3, minLng);
	    	query.setDouble(4, maxLng);
	    	ResultSet results = query.executeQuery();
	    	int numUsed = 0;
	    	ArbitrarilyDiscretizedFunc [] r = new ArbitrarilyDiscretizedFunc[4];
	    	while(results.next()) {
	    		ArbitrarilyDiscretizedFunc h = new ArbitrarilyDiscretizedFunc();
	    		h.set(0.2, 0.0); // Place holder for Ss value
	    		h.set(1.0, 0.0); // Place holder for S1 value
	    		h.set((double) SSUH_IDX, results.getDouble(SSUH_COL) * 1.1);
	    		h.set((double) S1UH_IDX, results.getDouble(S1UH_COL) * 1.3);
	    		h.set((double) SSDET_IDX, Math.max(1.5,
	    				(results.getDouble(SSDET_COL) / 100) * 1.8 * 1.1)
	    			);
	    		h.set((double) S1DET_IDX, Math.max(0.6,
	    				(results.getDouble(S1DET_COL) / 100) * 1.8 * 1.3)
	    			);
	    		h.set((double) SSCR_IDX, results.getDouble(SSCR_COL));
	    		h.set((double) S1CR_IDX, results.getDouble(S1CR_COL));
	    		r[numUsed++] = h;
	    	}
	    	
	    	if (numUsed == 4) {
	    		// Interpolate horizontally
	    		ArbitrarilyDiscretizedFunc f1 = interpolateFuncs(r[0], r[1],
	    				minLng, maxLng, longitude);
	    		
	    		ArbitrarilyDiscretizedFunc f2 = interpolateFuncs(r[2], r[3],
	    				minLng, maxLng, longitude);
	    		
	    		// Interpolate vertically	    		
	    		function = interpolateFuncs(f1, f2, maxLat, minLat, latitude);
	    		
	    	} else if (numUsed == 2) {
	    		if (minLat == latitude) {
	    			// Latitudes matched, interpolate with respect to longitude
	    			function = interpolateFuncs(r[0], r[1], minLng, maxLng,
	    					longitude);
	    		} else {
	    			// Longitudes matched, interpolate with respect to latitude
	    			function = interpolateFuncs(r[0], r[1], maxLat, minLat,
	    					latitude);
	    		}
	    	} else if (numUsed == 1) {
	    		// Exact match, go with it.
	    		function = r[0];
	    	}
	    	
	    	// Manipulate the function values to match the proposed revisions.
	    	double Ss  = Math.min( function.getY(SSDET_IDX),
	    			function.getY(SSUH_IDX) * function.getY(SSCR_IDX)
	    		);
	    	double S1  = Math.min( function.getY(S1DET_IDX), 
	    			function.getY(S1UH_IDX) * function.getY(S1CR_IDX)
	    		);
	    	
	    	function.set(SS_IDX, Ss);
	    	function.set(S1_IDX, S1);
	    	
	    	StringBuffer info = new StringBuffer(SsS1_TITLE + "\n");
	    	info.append("By definition, Ss and S1 are for Site Class B\n");
	    	info.append("Site Class B - Fa = 1.0, Fv = 1.0 "+
	    			"(As per definition of Ss and S1)\n");
	    	info.append("Data are based on a 0.05 deg grid spacing\n\n");
	    	
	    	info.append("Ss = min(CRs * Ss, SsD)\n");
	    	info.append("S1 = min(CR1 * S1, S1D)\n");
	    	info.append(DataDisplayFormatter.createFunctionInfoString(function,
	    			SA, Ss_Text, S1_Text, GlobalConstants.SITE_CLASS_B, true)
	    		);
	    	
	    	function.setInfo(info.toString());
    	} catch (SQLException sqx) {
    		sqx.printStackTrace(System.err);
    	}
    } else {
    	NEHRP_Record record = new NEHRP_Record();
        DataFileNameSelector dataFileSelector = new DataFileNameSelector();
        String fileName = dataFileSelector.getFileName(selectedRegion,
            selectedEdition, latitude, longitude);
        SiteInterpolation siteSaVals = new SiteInterpolation();
        
    	function = siteSaVals.getPeriodValuesForLocation(fileName, record,
    			latitude, longitude);

	    gridSpacing = siteSaVals.getGridSpacing();
	
	    //set the info for the function being added
	    String info = "";
	    info += SsS1_TITLE + "\n";
	
	    //info += "Latitude = " + latLonFormat.format(latitude) + "\n";
	    //info += "Longitude = " + latLonFormat.format(longitude) + "\n";
	    info +=
	        DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
	                                                  GlobalConstants.SITE_CLASS_B,
	                                                  Fa, Fv);
	    info += "Data are based on a " + gridSpacing + " deg grid spacing";
	    info +=
	        DataDisplayFormatter.createFunctionInfoString(function, SA, Ss_Text,
	        S1_Text,
	        GlobalConstants.SITE_CLASS_B);
	    function.setInfo(info);
    }
    return function;
  }

  private ArbitrarilyDiscretizedFunc interpolateFuncs(
		  ArbitrarilyDiscretizedFunc f1, ArbitrarilyDiscretizedFunc f2,
		  double p1, double p2, double p) {
	  
	  ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
	  double [] y1vals = f1.getYVals();
	  double [] y2vals = f2.getYVals();
	  double weight = (p - p1) / (p2 - p1);
	  int numVals = y1vals.length;
	  
	  for (int i = 0; i < numVals; ++i) {
		  double newVal = y1vals[i] + (weight * (y2vals[i] - y1vals[i]));
		  func.set(i, newVal);
	  }
	  
	  return func;
		  
  }
  
  /**
   *
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String selectedRegion,
                                            String selectedEdition,
                                            double latitude, double longitude,
                                            String spectraType) {
	ArbitrarilyDiscretizedFunc function = null;
	
	if (selectedEdition.equals(GlobalConstants.NEHRP_2009)) {
		function = new ArbitrarilyDiscretizedFunc();
		function.setInfo("Method not implemented for 2009 data");
	} else {
	    NEHRP_Record record = new NEHRP_Record();
	    DataFileNameSelectorForFEMA dataFileSelector = new
	        DataFileNameSelectorForFEMA();
	    String fileName = dataFileSelector.getFileName(selectedRegion,
	        selectedEdition, latitude,
	        longitude, spectraType);
	    SiteInterpolation siteSaVals = new SiteInterpolation();
	    function = siteSaVals.getPeriodValuesForLocation(
	        fileName, record,
	        latitude, longitude);
	
	    gridSpacing = siteSaVals.getGridSpacing();
	
	    //set the info for the function being added
	    String info = "";
	    info += SsS1_TITLE + "\n";
	
	    //info += "Latitude = " + latLonFormat.format(latitude) + "\n";
	    //info += "Longitude = " + latLonFormat.format(longitude) + "\n";
	    info +=
	        DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
	                                                  GlobalConstants.SITE_CLASS_B,
	                                                  Fa, Fv);
	    info += "Data are based on a " + gridSpacing + " deg grid spacing";
	    info +=
	        DataDisplayFormatter.createFunctionInfoString(function, SA, Ss_Text,
	        S1_Text,
	        GlobalConstants.SITE_CLASS_B);
	    function.setInfo(info);
	}
    return function;
  }

  /**
   * returns the Ss and S1 for Territory
   * @param territory String
   * @return DiscretizedFuncList
   */
  public ArbitrarilyDiscretizedFunc getSsS1ForTerritory(String territory) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    if (territory.equals(GlobalConstants.PUERTO_RICO) ||
        territory.equals(GlobalConstants.TUTUILA)) {
      function.set(0.2, 100.0 / GlobalConstants.DIVIDING_FACTOR_HUNDRED);
      function.set(1.0, 40.0 / GlobalConstants.DIVIDING_FACTOR_HUNDRED);
    }
    else {
      function.set(0.2, 150.0 / GlobalConstants.DIVIDING_FACTOR_HUNDRED);
      function.set(1.0, 60.0 / GlobalConstants.DIVIDING_FACTOR_HUNDRED);
    }
    DiscretizedFuncList functionList = new DiscretizedFuncList();
    functionList.add(function);
    //set the info for the function being added
    String info = "";
    info += SsS1_TITLE + "\n";
    info += "Spectral values are constant for the region\n";
    info +=
        DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
                                                  GlobalConstants.SITE_CLASS_B,
                                                  Fa, Fv);

    info +=
        DataDisplayFormatter.createFunctionInfoString(function, SA, Ss_Text,
        S1_Text,
        GlobalConstants.SITE_CLASS_B);
    function.setInfo(info);
    return function;
  }

  public DiscretizedFuncList getSsS1FuncList(String edition, String region,
		  String zipCode) {
	DiscretizedFuncList funcList = new DiscretizedFuncList();
	DataFileNameSelector fileSelector = new DataFileNameSelector();
	String fileName = fileSelector.getFileName(edition);
	BufferedReader bin = null;
	
	try {
		bin = new BufferedReader(new FileReader(fileName));
		// Ignore first 5 lines in file. This is header information.
		for (int i = 0; i < 5; ++i) { bin.readLine(); }
		
		// Get some useful meta information
		String line = bin.readLine();
		String [] tokens = line.split("\\s+");
		int numPeriods = Integer.parseInt(tokens[0]);
		float [] saPeriods = new float[numPeriods];
		for (int i = 1; i <= numPeriods; ++i) {
			saPeriods[i-1] = Float.parseFloat(tokens[i]);
		}
		
		// Find the line of interest
		while ( (line = bin.readLine()) != null) {
			tokens = line.split("\\s+");
			if (tokens[0].equalsIgnoreCase(zipCode)) {
				// This is the line we want, parse the data and break.
				ArbitrarilyDiscretizedFunc funcCen = 
					new ArbitrarilyDiscretizedFunc();
				ArbitrarilyDiscretizedFunc funcMax = 
					new ArbitrarilyDiscretizedFunc();
				ArbitrarilyDiscretizedFunc funcMin = 
					new ArbitrarilyDiscretizedFunc();
				
				funcCen.set(saPeriods[0], Double.parseDouble(tokens[5]) / 
						GlobalConstants.DIVIDING_FACTOR_HUNDRED);
				funcCen.set(saPeriods[1], Double.parseDouble(tokens[6]) / 
						GlobalConstants.DIVIDING_FACTOR_HUNDRED);
				
				funcMax.set(saPeriods[0], Double.parseDouble(tokens[7]) / 
						GlobalConstants.DIVIDING_FACTOR_HUNDRED);
				funcMax.set(saPeriods[1], Double.parseDouble(tokens[8]) / 
						GlobalConstants.DIVIDING_FACTOR_HUNDRED);
				
				funcMin.set(saPeriods[0], Double.parseDouble(tokens[9]) / 
						GlobalConstants.DIVIDING_FACTOR_HUNDRED);
				funcMin.set(saPeriods[1], Double.parseDouble(tokens[10]) / 
						GlobalConstants.DIVIDING_FACTOR_HUNDRED);
				
				funcList.add(funcCen);
				funcList.add(funcMax);
				funcList.add(funcMin);
				break; // Break out of the while loop
			}
		} // END: while(line!=null)
		
	} catch (IOException iox) {
		/* Ignore for now. */
	} finally {
		try{bin.close();}catch(Exception ex){/*Ignore*/}
	}
	
	return funcList;
  }
  
  /**
   *
   * @param zipCode
   * @return
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String selectedRegion,
                                            String selectedEdition,
                                            String zipCode) throws
      ZipCodeErrorException {
    Location loc = ZipCodeToLatLonConvertor.getLocationForZipCode(zipCode);
    LocationUtil.checkZipCodeValidity(loc, selectedRegion);
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //getting the SA Period values for the lat lon for the selected Zip code.
    ArbitrarilyDiscretizedFunc function = getSsS1(selectedRegion,
                                                  selectedEdition, lat, lon);
    try {
      DataFileNameSelector dataFileSelector = new DataFileNameSelector();
      //getting the fileName to be read for the selected location
      String zipCodeFileName = dataFileSelector.getFileName(selectedEdition);

      FileReader fin = new FileReader(zipCodeFileName);
      BufferedReader bin = new BufferedReader(fin);
      // ignore the first 5 lines in the files
      for (int i = 0; i < 5; ++i) {
        bin.readLine();
      }

      // read the number of periods  and value of those periods
      String str = bin.readLine();
      StringTokenizer tokenizer = new StringTokenizer(str);
      int numPeriods = Integer.parseInt(tokenizer.nextToken());
      float[] saPeriods = new float[numPeriods];
      for (int i = 0; i < numPeriods; ++i) {
        saPeriods[i] = Float.parseFloat(tokenizer.nextToken());
      }

      // skip the next 2 lines
      bin.readLine();
      bin.readLine();

      // now read line by line until the zip code is found in file
      str = bin.readLine();
      while (str != null) {
        tokenizer = new StringTokenizer(str);
        String lineZipCode = tokenizer.nextToken();
        if (lineZipCode.equalsIgnoreCase(zipCode)) {
          //skipping the 4 tokens in the file which not required.
          for (int i = 0; i < 4; ++i) {
            tokenizer.nextToken();
          }

          ArbitrarilyDiscretizedFunc func1 = new ArbitrarilyDiscretizedFunc();
          ArbitrarilyDiscretizedFunc func2 = new ArbitrarilyDiscretizedFunc();
          ArbitrarilyDiscretizedFunc func3 = new ArbitrarilyDiscretizedFunc();
          func1.set(saPeriods[0],
                    Double.parseDouble(tokenizer.nextToken()) /
                    GlobalConstants.DIVIDING_FACTOR_HUNDRED);
          func1.set(saPeriods[1],
                    Double.parseDouble(tokenizer.nextToken()) /
                    GlobalConstants.DIVIDING_FACTOR_HUNDRED);
          func2.set(saPeriods[0],
                    Double.parseDouble(tokenizer.nextToken()) /
                    GlobalConstants.DIVIDING_FACTOR_HUNDRED);
          func2.set(saPeriods[1],
                    Double.parseDouble(tokenizer.nextToken()) /
                    GlobalConstants.DIVIDING_FACTOR_HUNDRED);
          func3.set(saPeriods[0],
                    Double.parseDouble(tokenizer.nextToken()) /
                    GlobalConstants.DIVIDING_FACTOR_HUNDRED);
          func3.set(saPeriods[1],
                    Double.parseDouble(tokenizer.nextToken()) /
                    GlobalConstants.DIVIDING_FACTOR_HUNDRED);

          //adding the info for each function
          String info = "";
          info += SsS1_TITLE + "\n";
          //info += "Zip Code - " + zipCode + "\n";
          //info += "Zip Code Latitude = " + latLonFormat.format(lat) + "\n";
          //info += "Zip Code Longitude = " + latLonFormat.format(lon) + "\n";
          //info +=
            //  DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
              //GlobalConstants.SITE_CLASS_B,
              //Fa, Fv);
          info += SsS1_SubTitle + "\n";
          
          info += "Data are based on a " + gridSpacing + " deg grid spacing\n";
          //info +=
          //    DataDisplayFormatter.createFunctionInfoString(function, SA,
          //    Ss_Text, S1_Text, GlobalConstants.SITE_CLASS_B);
          info +=
              DataDisplayFormatter.createFunctionInfoString(func1, CENTROID_SA,
              Ss_Text, S1_Text, "");
          info +=
              DataDisplayFormatter.createFunctionInfoString(func2, MAXIMUM_SA,
              Ss_Text, S1_Text, "");
          info +=
              DataDisplayFormatter.createFunctionInfoString(func3, MINIMUM_SA,
              Ss_Text, S1_Text, "");
          function.setInfo(info);
          break;
        }
        str = bin.readLine();
      }
      bin.close();
      fin.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return function;
  }

  /**
   *
   * @param zipCode
   * @return
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String selectedRegion,
                                            String selectedEdition,
                                            String zipCode, String spectraType) throws
      ZipCodeErrorException {
    Location loc = ZipCodeToLatLonConvertor.getLocationForZipCode(zipCode);
    LocationUtil.checkZipCodeValidity(loc, selectedRegion);
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //getting the SA Period values for the lat lon for the selected Zip code.
    ArbitrarilyDiscretizedFunc function = getSsS1(selectedRegion,
                                                  selectedEdition, lat, lon,
                                                  spectraType);

    //adding the info for each function
    String info = "";
    info += SsS1_TITLE + "\n";
    //info += "Zip Code - " + zipCode + "\n";
    //info += "Zip Code Latitude = " + latLonFormat.format(lat) + "\n";
    //info += "Zip Code Longitude = " + latLonFormat.format(lon) + "\n";
    info +=
        DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
                                                  GlobalConstants.SITE_CLASS_B,
                                                  Fa, Fv);
    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    info +=
        DataDisplayFormatter.createFunctionInfoString(function, SA,
        Ss_Text, S1_Text, GlobalConstants.SITE_CLASS_B);
    function.setInfo(info);

    return function;
  }

}
