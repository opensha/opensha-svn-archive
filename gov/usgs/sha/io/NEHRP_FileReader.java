package gov.usgs.sha.io;

import java.io.*;
import java.util.StringTokenizer;
import gov.usgs.util.GlobalConstants;
import java.text.DecimalFormat;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncList;
import org.scec.data.Location;
import gov.usgs.exceptions.ZipCodeErrorException;
import java.text.DecimalFormat;
import gov.usgs.sha.io.DataFileNameSelector;

/**
 * <p>Title: NEHRP_FileReader</p>
 * <p>Description: This class allows user to read the data for NEHRP from the data files.</p>
 * @author : Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class NEHRP_FileReader {
  private String fileName;

  private float minLat ;
  private  float minLon ;
  private  float maxLat ;
  private  float maxLon ;
  private float gridSpacing;
  private int gridPointsPerLatitude;

  private float[] saPeriods;

  /**
   * Some static String for the data printing
   */
  private static final String SsS1_TITLE = "Spectral Response Accelerations Ss and S1\n\n";
  private static final String SsS1_SubTitle =   "Ss and S1 = Mapped Spectral Acceleration Values";
  private static final String SiteClass_SsS1 = "Site Class B";
  private static final String Ss_Text = "Ss";
  private static final String S1_Text = "S1";
  private static final String SA ="Sa";
  private static final String CENTROID_SA ="Centroid Sa";
  private static final String MINIMUM_SA ="Minimum Sa";
  private static final String MAXIMUM_SA ="Maximum Sa";


  private static final double Fa = 1.0;
  private static final double Fv = 1.0;


  private int numPeriods;


  private DecimalFormat periodFormat = new DecimalFormat("0.0#");
  private DecimalFormat saValFormat = new DecimalFormat("0.###");
  private DecimalFormat gridSpacingFormat = new DecimalFormat("0.0#");
  private DecimalFormat latLonFormat = new DecimalFormat("0.0000##");

  public NEHRP_FileReader() {


  }

  private void setFileName(String fileName){
    this.fileName = fileName;
  }

  /**
   * Gets the end points for the region for the selected region and edition
   * @param fileName String
   * @return EvenlyGriddedRectangularGeographicRegion
   */
  private void getRegionEndPoints() {

    MCE_Record nwRecord = getRecord(1);
    MCE_Record seRecord = getRecord(2);
    minLat = seRecord.latitude;
    maxLon = seRecord.longitude;
    maxLat = nwRecord.latitude;
    minLon = nwRecord.longitude;
    MCE_Record record_4 = getRecord(4);
    MCE_Record record_5 = getRecord(5);
    gridSpacing =  Math.abs(record_4.longitude - record_5.longitude);

    gridSpacing =Float.parseFloat(gridSpacingFormat.format(gridSpacing));
    gridPointsPerLatitude = (int)((maxLon-minLon)/gridSpacing) +1;

    MCE_Record record_3 = getRecord(3);
    numPeriods = record_3.numValues;
    saPeriods = new float[numPeriods];
    for(int i=0;i<numPeriods;++i)
      saPeriods[i] = record_3.values[i];
  }

  /**
   *
   * @param recNo int
   * @return float[]
   */
  private float[] getPeriodValues(int recNo){
    MCE_Record record =getRecord(recNo);

    float [] vals = new float[numPeriods];
    for(int i=0;i<numPeriods;++i)
      vals[i] = record.values[i]/GlobalConstants.DIVIDING_FACTOR_HUNDRED;

    return vals;
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
  public DiscretizedFuncList getSsS1ForTerritory(String territory){
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

    int numFunctions=functionList.size();
    for(int i=0;i<numFunctions;++i)
     info +=createFunctionInfoString((ArbitrarilyDiscretizedFunc)functionList.get(i),SA)+"\n" ;
    functionList.setInfo(info);
    return functionList;
  }


  private String createSubTitleString(){
    String dataInfo="";
    dataInfo += SsS1_SubTitle+"\n\n";
    dataInfo +=SiteClass_SsS1+" - "+" Fa = "+Fa+" ,Fv = "+Fv+"\n";
    return dataInfo;
  }


  private String createFunctionInfoString(ArbitrarilyDiscretizedFunc function,String saString){
    String dataInfo="";
    dataInfo += "\nPeriod\t"+saString+"\n";
    dataInfo += "(sec)\t (g)\n";

    dataInfo +=periodFormat.format(function.getX(0))+"\t"+saValFormat.format(function.getY(0))+"  "+Ss_Text+","+SiteClass_SsS1+"\n";
    dataInfo +=periodFormat.format(function.getX(1))+"\t"+saValFormat.format(function.getY(1))+"  "+S1_Text+","+SiteClass_SsS1+"\n";

    return dataInfo;
  }

  /**
   * Returns the location for the selected zip code
   * @param zipCode String
   * @return Location
   * @throws ZipCodeNotFoundException
   */
  private Location getLocationForZipCode(String zipCode)throws ZipCodeErrorException{
    Location loc= null;
    boolean zipCodeFound = false;
    try {

      FileReader fin = new FileReader(DataFileNameSelector.getZipCodeToLatLonFileName());
      BufferedReader bin = new BufferedReader(fin);

      // now read line by line until the zip code is found in file
      String str = bin.readLine();
      while (str != null) {
        StringTokenizer tokenizer = new StringTokenizer(str);
        String lineZipCode = tokenizer.nextToken();
        if (lineZipCode.equalsIgnoreCase(zipCode)) {
          zipCodeFound = true;
          double lat = Float.parseFloat(tokenizer.nextToken().trim());
          double lon = Float.parseFloat(tokenizer.nextToken().trim());
          loc = new Location(lat,lon);
          break;
        }
        str = bin.readLine();
      }
      if (zipCodeFound == false)
        throw new ZipCodeErrorException(
            "The Zip Code is not in the data file. Try another or use Lat-Lon for the location.");
      bin.close();
      fin.close();
    }catch(IOException e){
      e.printStackTrace();
    }
    return loc ;
  }

  /**
   * Checks if the zip code entered is a valid for the region the user has selected
   * @param loc Location
   * @param selectedRegion String
   */
  private void checkZipCodeValidity(Location loc, String selectedRegion) throws
      ZipCodeErrorException {
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //if selected Region is Counterminous 48 states
    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      if (lat >= 24.6 && lat <= 50 && lon >= -125 && lon <= -65)
        return;
      else if (lat >= 48 && lat <= 72 && lon >= -200 && lon <= -125) {
        throw new ZipCodeErrorException(
            "The Zip Code is outside the geographic region of counterminous 48 states.\n" +
            "It is in Alaska");
      }
      else if (lat >= 18 && lat <= 23 && lon >= -161 && lon <= -154) {
        throw new ZipCodeErrorException(
            "The Zip Code is outside the geographic region of counterminous 48 states.\n" +
            "It is in Hawaii");

      }
    }
    //if selected region is Alaska
    else if (selectedRegion.equals(GlobalConstants.ALASKA)) {
      if (lat >= 24.6 && lat <= 50 && lon >= -125 && lon <= -65)
        throw new ZipCodeErrorException(
            "The Zip Code is outside the geographic region of Alaska." +
            "It is in counterminous 48 states");
      else if (lat >= 48 && lat <= 72 && lon >= -200 && lon <= -125) {
        return;
      }
      else if (lat >= 18 && lat <= 23 && lon >= -161 && lon <= -154) {
        throw new ZipCodeErrorException(
            "The Zip Code is outside the geographic region of Alaska." +
            "It is in Hawaii.");

      }
    }
    //if selected region is Hawaii
    else if (selectedRegion.equals(GlobalConstants.HAWAII)) {
      if (lat >= 24.6 && lat <= 50 && lon >= -125 && lon <= -65)
        throw new ZipCodeErrorException(
            "The Zip Code is outside the geographic region of Hawaii." +
            "It is in counterminous 48 states");
      else if (lat >= 48 && lat <= 72 && lon >= -200 && lon <= -125) {
        throw new ZipCodeErrorException(
            "The Zip Code is outside the geographic region of Hawaii." +
            "It is in Alaska.");
      }
      else if (lat >= 18 && lat <= 23 && lon >= -161 && lon <= -154) {
        return;
      }
    }

  }

  /**
   *
   * @param zipCode
   * @return
   */
  public DiscretizedFuncList getSsS1(String selectedRegion,
                                     String selectedEdition, String zipCode) throws
      ZipCodeErrorException {
    Location loc = getLocationForZipCode(zipCode);
    checkZipCodeValidity(loc, selectedRegion);
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //getting the SA Period values for the lat lon for the selected Zip code.
    DiscretizedFuncList functions = getSsS1(selectedRegion,selectedEdition,lat, lon);
    DiscretizedFuncList funcList = new DiscretizedFuncList();
    funcList.addAll(functions);
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
          info += SsS1_TITLE + "\n\n";
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

  /**
   *
   * @param latitude double
   * @param longitude double
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList getSsS1(String selectedRegion,
                                     String selectedEdition,
                                     double latitude, double longitude) {
    float lat = 0;
    float lon = 0;
    float[] saArray = null;

    DataFileNameSelector dataFileSelector = new DataFileNameSelector();
    //getting the fileName to be read for the selected location
    setFileName(dataFileSelector.getFileName(selectedRegion, selectedEdition,
                                             latitude, longitude));
    getRegionEndPoints();
    if ( (latitude == minLat && longitude == minLon) ||
        (latitude == maxLat && longitude == minLon) ||
        (latitude == minLat && longitude == maxLon) ||
        (latitude == maxLat && longitude == maxLon)) {
      lat = (float) latitude;
      lon = (float) longitude;
      int recNum = getRecordNumber(lat, lon);
      saArray = getPeriodValues(recNum);
    }
    else if ( (latitude == minLat || latitude == maxLat) &&
             (longitude > minLon && longitude < maxLon)) {
      lat = (float) latitude;
      lon = ( (int) (longitude / gridSpacing)) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + 1;
      float[] vals1 = getPeriodValues(recNum1);
      float[] vals2 = getPeriodValues(recNum2);
      float flon = (float) (longitude - lon) / gridSpacing;
      saArray = getPeriodValues(vals1, vals2, flon);
    }
    else if ( (longitude == minLon || longitude == maxLon) &&
             (latitude > minLat && latitude < maxLat)) {
      lon = (float) longitude;
      lat = ( (int) (latitude / gridSpacing) + 1) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + gridPointsPerLatitude;
      float[] vals1 = getPeriodValues(recNum1);
      float[] vals2 = getPeriodValues(recNum2);
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
      float[] vals1 = getPeriodValues(recNum1);
      float[] vals2 = getPeriodValues(recNum2);
      float[] vals3 = getPeriodValues(recNum3);
      float[] vals4 = getPeriodValues(recNum4);
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
    DiscretizedFuncList functionList = new DiscretizedFuncList();
    functionList.add(function);
    //set the info for the function being added
    String info = "";
    info += SsS1_TITLE + "\n\n";

    info += "Latitude = " + latLonFormat.format(latitude) + "\n";
    info += "Longitude = " + latLonFormat.format(longitude) + "\n";
    info += createSubTitleString();
    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    int numFunctions = functionList.size();
    for (int i = 0; i < numFunctions; ++i)
      info +=
          createFunctionInfoString( (ArbitrarilyDiscretizedFunc) functionList.
                                   get(i), SA);
    functionList.setInfo(info);
    return functionList;
  }


  private ArbitrarilyDiscretizedFunc createFunction(float[] saVals){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    for(int i=0;i<numPeriods;++i)
      function.set(saPeriods[i], saVals[i]);
    return function;
  }


  private  ArbitrarilyDiscretizedFunc calculateSMSsS1(ArbitrarilyDiscretizedFunc saVals, float fa, float fv) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(saVals.getX(0), fa*saVals.getY(0));
    function.set(saVals.getX(1), fv*saVals.getY(1));
    String title = "Spectral Response Accelerations SMs and SM1";
    String subTitle = "SMs = FaSs and SM1 = FvS1";
    String text1 = "SMs";
    String text2 = "SM1";
    return function;
  }


  private  ArbitrarilyDiscretizedFunc calculateSDSsS1(ArbitrarilyDiscretizedFunc saVals, float fa, float fv) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(saVals.getX(0), fa*saVals.getY(0)*2.0/3.0);
    function.set(saVals.getX(1), fv*saVals.getY(1)*2.0/3.0);
    String title = "Spectral Response Accelerations SDs and SD1";
    String subTitle = "SDs = 2/3 x SMs and SD1 = 2/3 x SM1";
    String text1 = "SDs";
    String text2 = "SD1";
    return function;
  }


  private DiscretizedFuncList approxSaSd(ArbitrarilyDiscretizedFunc saVals,
                                         double fa, double fv) {
    DiscretizedFuncList funcList = new DiscretizedFuncList();
    ArbitrarilyDiscretizedFunc saSdfunction = new ArbitrarilyDiscretizedFunc();
    ArbitrarilyDiscretizedFunc saTfunction = new ArbitrarilyDiscretizedFunc();
    funcList.add(saSdfunction);
    funcList.add(saTfunction);

    double tAcc = saVals.getX(0);
    double sAcc = fa*saVals.getY(0);
    double tVel = saVals.getX(0);
    double sVel = fv*saVals.getY(1);
    double spga = 0.4*fa*saVals.getX(0);
    double tPga = 0;
    double tMaxVel = 2;
    double tInc = 0.1;
    double tVelTransition = sVel/sAcc;
    double tPgaTransition = 0.2*tVelTransition;

    saTfunction.set(spga,tPga);
    saTfunction.set(sAcc,tPgaTransition);

    if(tPgaTransition <= tAcc) {
      saTfunction.set(sAcc, tAcc);
    }
    saTfunction.set(sAcc, tVelTransition);
    double lastT = ((int)(tVelTransition * 10.0)) / 10.0;
    double nextT = lastT + tInc;
    while(nextT<tMaxVel) {
        saTfunction.set(sVel/nextT,nextT);
        nextT+=tInc;
    }

    for(int i=0; i < saTfunction.getNum(); ++i) {
      saSdfunction.set(saTfunction.getX(0), 9.77*saTfunction.getX(0)*Math.pow(saTfunction.getY(0),2));
    }

    String title = "MCE Response Spectra for Site Class B";
    String subTitle = "Ss and S1 = Mapped Spectral Acceleration Values";


    return funcList;
  }





  /**
   *
   * @param lat float
   * @param lon float
   * @return int
   */
  private int getRecordNumber(float lat, float lon) {
    int colIndex = (int)((lon-minLon)/gridSpacing)+1;
    int rowIndex = (int)((maxLat-lat)/gridSpacing)+1;
    int recNum = (rowIndex-1)*gridPointsPerLatitude+(colIndex-1)+1;
    return recNum+3;
  }


  private MCE_Record getRecord(long recordNum){
    MCE_Record record = new MCE_Record();
    RandomAccessFile fin = null;
    try {
      fin = new RandomAccessFile(fileName, "r");
      fin.seek((recordNum-1)*record.recordLength);
      record.recordNumber = GlobalConstants.swap(fin.readInt());
      record.latitude = GlobalConstants.swap(fin.readFloat());
      record.longitude = GlobalConstants.swap(fin.readFloat());
      record.numValues = GlobalConstants.swap(fin.readShort());
      record.values[0] = GlobalConstants.swap(fin.readFloat());
      record.values[1] = GlobalConstants.swap(fin.readFloat());

      fin.close();
      return record;
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }




}

class MCE_Record{
  public int recordNumber;
  public float latitude;
  public float longitude;
  public short numValues;
  public float[] values= new float[2];
  public static final int recordLength = 4+4+4+4+4+4;
}
