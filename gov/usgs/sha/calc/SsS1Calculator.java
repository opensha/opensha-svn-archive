package gov.usgs.sha.calc;

import gov.usgs.sha.io.DataFileNameSelector;
import gov.usgs.sha.io.DataFileReader;
import gov.usgs.sha.data.NEHRP_Record;
import gov.usgs.util.*;
import gov.usgs.exceptions.ZipCodeErrorException;

import org.scec.data.function.DiscretizedFuncList;
import org.scec.data.Location;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;


import java.util.StringTokenizer;
import java.io.*;
import java.text.DecimalFormat;


/**
 * <p>Title: SsS1Calculator</p>
 *
 * <p>Description: Computes the values for the Ss and S1 for the given location or
 * territory in USA.</p>
 * @author  Ned Field, Nitin Gupta , E.V.Leyendecker
 * @version 1.0
 */
public class SsS1Calculator {

  private float minLat;
  private float minLon;
  private float maxLat;
  private float maxLon;
  private float gridSpacing;
  private int gridPointsPerLatitude;

  private float[] saPeriods;
  private int numPeriods;

  /**
   * Some static String for the data printing
   */
  private static final String SsS1_TITLE =
      "Spectral Response Accelerations Ss and S1\n\n";
  private static final String SsS1_SubTitle =
      "Ss and S1 = Mapped Spectral Acceleration Values";

  private static final String Ss_Text = "Ss";
  private static final String S1_Text = "S1";
  private static final String SA = "Sa";
  private static final String CENTROID_SA = "Centroid Sa";
  private static final String MINIMUM_SA = "Minimum Sa";
  private static final String MAXIMUM_SA = "Maximum Sa";
  private static final double Fa = 1.0;
  private static final double Fv = 1.0;


  private DecimalFormat periodFormat = new DecimalFormat("0.0#");
  private DecimalFormat saValFormat = new DecimalFormat("0.###");
  private DecimalFormat gridSpacingFormat = new DecimalFormat("0.0#");
  private DecimalFormat latLonFormat = new DecimalFormat("0.0000##");


  /**
   *
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String selectedRegion,
                                            String selectedEdition,
                                            double latitude, double longitude) {
    float lat = 0;
    float lon = 0;
    float[] saArray = null;

    DataFileNameSelector dataFileSelector = new DataFileNameSelector();
    //getting the fileName to be read for the selected location
    String fileName = dataFileSelector.getFileName(selectedRegion,
        selectedEdition, latitude, longitude);
    getRegionEndPoints(fileName);
    if ( (latitude == minLat && longitude == minLon) ||
        (latitude == maxLat && longitude == minLon) ||
        (latitude == minLat && longitude == maxLon) ||
        (latitude == maxLat && longitude == maxLon)) {
      lat = (float) latitude;
      lon = (float) longitude;
      int recNum = getRecordNumber(lat, lon);
      saArray = getPeriodValues(fileName,recNum);
    }
    else if ( (latitude == minLat || latitude == maxLat) &&
             (longitude > minLon && longitude < maxLon)) {
      lat = (float) latitude;
      lon = ( (int) (longitude / gridSpacing)) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + 1;
      float[] vals1 = getPeriodValues(fileName,recNum1);
      float[] vals2 = getPeriodValues(fileName,recNum2);
      float flon = (float) (longitude - lon) / gridSpacing;
      saArray = getPeriodValues(vals1, vals2, flon);
    }
    else if ( (longitude == minLon || longitude == maxLon) &&
             (latitude > minLat && latitude < maxLat)) {
      lon = (float) longitude;
      lat = ( (int) (latitude / gridSpacing) + 1) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + gridPointsPerLatitude;
      float[] vals1 = getPeriodValues(fileName,recNum1);
      float[] vals2 = getPeriodValues(fileName,recNum2);
      float flat = (float) (lat - latitude) / gridSpacing;
      saArray = getPeriodValues(vals1, vals2, flat);
    }
    else if (latitude > minLat && latitude < maxLat &&
             longitude > minLon && longitude < maxLon) {
      lat = ( (int) (latitude / gridSpacing) + 1) * gridSpacing;
      lon = ( (int) (longitude / gridSpacing)) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + 1;
      int recNum3 = recNum1 + gridPointsPerLatitude;
      int recNum4 = recNum3 + 1;
      float[] vals1 = getPeriodValues(fileName,recNum1);
      float[] vals2 = getPeriodValues(fileName,recNum2);
      float[] vals3 = getPeriodValues(fileName,recNum3);
      float[] vals4 = getPeriodValues(fileName,recNum4);
      float flon = (float) (longitude - lon) / gridSpacing;
      float flat = (float) (lat - latitude) / gridSpacing;
      float[] periodVals1 = getPeriodValues(vals1, vals2, flon);
      float[] periodVals2 = getPeriodValues(vals3, vals4, flon);
      saArray = getPeriodValues(periodVals1, periodVals2, flat);
    }
    else {
      new RuntimeException("Latitude and Longitude outside the Region bounds");
    }

    ArbitrarilyDiscretizedFunc function = createFunction(saArray);

    //set the info for the function being added
    String info = "";
    info += SsS1_TITLE + "\n";

    info += "Latitude = " + latLonFormat.format(latitude) + "\n";
    info += "Longitude = " + latLonFormat.format(longitude) + "\n";
    info += createSubTitleString();
    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    info += createFunctionInfoString(function, SA);
    function.setInfo(info);
    return function;
  }

  /**
   *
   * @param lat float
   * @param lon float
   * @return int
   */
  private int getRecordNumber(float lat, float lon) {
    int colIndex = (int) ( (lon - minLon) / gridSpacing) + 1;
    int rowIndex = (int) ( (maxLat - lat) / gridSpacing) + 1;
    int recNum = (rowIndex - 1) * gridPointsPerLatitude + (colIndex - 1) + 1;
    return recNum + 3;
  }

  /**
   * Gets the end points for the region for the selected region and edition
   * @param fileName String
   * @return EvenlyGriddedRectangularGeographicRegion
   */
  private void getRegionEndPoints(String fileName) {

    DataFileReader reader = new DataFileReader();
    NEHRP_Record nwRecord = reader.getRecord(fileName,1);
    NEHRP_Record seRecord = reader.getRecord(fileName,2);
    minLat = seRecord.latitude;
    maxLon = seRecord.longitude;
    maxLat = nwRecord.latitude;
    minLon = nwRecord.longitude;
    NEHRP_Record record_4 = reader.getRecord(fileName,4);
    NEHRP_Record record_5 = reader.getRecord(fileName,5);
    gridSpacing = Math.abs(record_4.longitude - record_5.longitude);

    gridPointsPerLatitude = (int) ( (maxLon - minLon) / gridSpacing) + 1;

    NEHRP_Record record_3 = reader.getRecord(fileName,3);
    numPeriods = record_3.numValues;
    saPeriods = new float[numPeriods];
    for (int i = 0; i < numPeriods; ++i)
      saPeriods[i] = record_3.values[i];
  }

  /**
    *
    * @param periodVals1 float[]
    * @param periodVals2 float[]
    * @param val float
    * @return float[]
    */
   private float[] getPeriodValues(float[] periodVals1,float[] periodVals2,float val){
     float[] periodsVal = new float[numPeriods];
     for(int i=0;i<numPeriods;++i)
       periodsVal[i] = periodVals1[i]+val*(periodVals2[i]-periodVals1[i]);

     return periodsVal;
   }

   /**
    * returns the Ss and S1 for Territory
    * @param territory String
    * @return DiscretizedFuncList
    */
   public ArbitrarilyDiscretizedFunc getSsS1ForTerritory(String territory){
     ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
     if(territory.equals(GlobalConstants.PUERTO_RICO) ||
         territory.equals(GlobalConstants.TUTUILA)){
       function.set(0.2,100.0/GlobalConstants.DIVIDING_FACTOR_HUNDRED);
       function.set(1.0,40.0/GlobalConstants.DIVIDING_FACTOR_HUNDRED);
     }
     else {
       function.set(0.2,150.0/GlobalConstants.DIVIDING_FACTOR_HUNDRED);
       function.set(1.0,60.0/GlobalConstants.DIVIDING_FACTOR_HUNDRED);
     }
     DiscretizedFuncList functionList = new DiscretizedFuncList();
     functionList.add(function);
     //set the info for the function being added
     String info="";
     info += SsS1_TITLE+"\n";
     info +="Spectral values are constant for the region\n";
     info +=createSubTitleString()+"\n";


      info +=createFunctionInfoString(function,SA)+"\n" ;
     function.setInfo(info);
     return function;
   }

   /**
    *
    * @param zipCode
    * @return
    */
   public DiscretizedFuncList getSsS1(String selectedRegion,
                                      String selectedEdition, String zipCode) throws
       ZipCodeErrorException {
     Location loc = ZipCodeToLatLonConvertor.getLocationForZipCode(zipCode);
     LocationUtil.checkZipCodeValidity(loc, selectedRegion);
     double lat = loc.getLatitude();
     double lon = loc.getLongitude();
     //getting the SA Period values for the lat lon for the selected Zip code.
     ArbitrarilyDiscretizedFunc function = getSsS1(selectedRegion,selectedEdition,lat, lon);
     DiscretizedFuncList funcList = new DiscretizedFuncList();
     funcList.add(function);
     try {
       DataFileNameSelector dataFileSelector = new DataFileNameSelector();
       //getting the fileName to be read for the selected location
       String zipCodeFileName=dataFileSelector.getFileName(selectedRegion,selectedEdition);

       FileReader fin = new FileReader(zipCodeFileName);
       BufferedReader bin = new BufferedReader(fin);
       // ignore the first 5 lines in the files
       for (int i = 0; i < 5; ++i) bin.readLine();

       // read the number of periods  and value of those periods
       String str = bin.readLine();
       StringTokenizer tokenizer = new StringTokenizer(str);
       this.numPeriods = Integer.parseInt(tokenizer.nextToken());
       this.saPeriods = new float[numPeriods];
       for (int i = 0; i < numPeriods; ++i)
         saPeriods[i] = Float.parseFloat(tokenizer.nextToken());

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
           for (int i = 0; i < 4; ++i)
             tokenizer.nextToken();

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


           funcList.add(func1);
           funcList.add(func2);
           funcList.add(func3);

           String info = "";
           info += SsS1_TITLE + "\n";
           info += "Zip Code - " + zipCode + "\n";
           info += "Zip Code Latitude = " + latLonFormat.format(lat) + "\n";
           info += "Zip Code Longitude = " + latLonFormat.format(lon) + "\n";
           info += createSubTitleString() + "\n";
           info += "Data are based on a " + gridSpacing + " deg grid spacing";
           info +=
               createFunctionInfoString( (ArbitrarilyDiscretizedFunc) funcList.
                                        get(0), this.CENTROID_SA);
           info +=
               createFunctionInfoString( (ArbitrarilyDiscretizedFunc) funcList.
                                        get(1), this.MINIMUM_SA);
           info +=
               createFunctionInfoString( (ArbitrarilyDiscretizedFunc) funcList.
                                        get(2), this.MAXIMUM_SA);
           info +=
               createFunctionInfoString( (ArbitrarilyDiscretizedFunc) funcList.
                                        get(3), this.SA);
           funcList.setInfo(info);
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
     return funcList;
   }

   private String createSubTitleString() {
     String dataInfo = "";
     dataInfo += SsS1_SubTitle + "\n";
     dataInfo += GlobalConstants.SITE_CLASS_B + " - " + " Fa = " + Fa + " ,Fv = " + Fv + "\n";
     return dataInfo;
   }

   private String createFunctionInfoString(ArbitrarilyDiscretizedFunc function,String saString){
     String dataInfo="";
     dataInfo += "\nPeriod\t"+saString+"\n";
     dataInfo += "(sec)\t (g)\n";

     dataInfo +=periodFormat.format(function.getX(0))+"\t"+
         saValFormat.format(function.getY(0))+"  "+Ss_Text+","+GlobalConstants.SITE_CLASS_B+"\n";
     dataInfo +=periodFormat.format(function.getX(1))+"\t"+
         saValFormat.format(function.getY(1))+"  "+S1_Text+","+GlobalConstants.SITE_CLASS_B+"\n";

     return dataInfo;
    }

   private ArbitrarilyDiscretizedFunc createFunction(float[] saVals){
     ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
     for(int i=0;i<numPeriods;++i)
       function.set(saPeriods[i], saVals[i]);
     return function;
   }


  /**
   *
   * @param recNo int
   * @return float[]
   */
  private float[] getPeriodValues(String fileName,int recNo) {
    DataFileReader reader = new DataFileReader();
    NEHRP_Record record = reader.getRecord(fileName,recNo);

    float[] vals = new float[numPeriods];
    for (int i = 0; i < numPeriods; ++i)
      vals[i] = record.values[i] / GlobalConstants.DIVIDING_FACTOR_HUNDRED;

    return vals;
  }

}
