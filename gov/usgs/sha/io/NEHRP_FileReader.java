package gov.usgs.sha.io;

import org.scec.data.region.EvenlyGriddedRectangularGeographicRegion;
import java.io.*;
import gov.usgs.util.GlobalConstants;
import java.text.DecimalFormat;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;

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

  private int numPeriods;
  public NEHRP_FileReader() {

  }


  public void setFileName(String fileName){
    this.fileName = fileName;
  }

  /**
   * Gets the end points for the region for the selected region and edition
   * @param fileName String
   * @return EvenlyGriddedRectangularGeographicRegion
   */
  private void getRegionEndPoints(String fileName) {

    MCE_Record nwRecord = getRecord(1);
    MCE_Record seRecord = getRecord(2);
    minLat = seRecord.latitude;
    minLon = seRecord.longitude;
    maxLat = nwRecord.latitude;
    maxLon = nwRecord.longitude;
    MCE_Record record_4 = getRecord(4);
    MCE_Record record_5 = getRecord(5);
    gridSpacing =  Math.abs(record_4.longitude - record_5.longitude);
    DecimalFormat format = new DecimalFormat("0.00##");
    gridSpacing =Float.parseFloat(format.format(gridSpacing));
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
      vals[i] = record.values[i]/100;

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
   *
   * @param latitude double
   * @param longitude double
   */
  public ArbitrarilyDiscretizedFunc getSsS1(double latitude, double longitude) {
    float lat = 0;
    float lon = 0;
    float[] saArray=null;
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
      int recNum2 = recNum1+1;
      float[] vals1 = getPeriodValues(recNum1);
      float[] vals2 = getPeriodValues(recNum2);
      float flon = (float)(longitude - lon)/gridSpacing;
      saArray =getPeriodValues(vals1,vals2,flon);
    }
    else if ( (longitude == minLon || longitude == maxLon) &&
             (latitude > minLat && latitude < maxLat)) {
      lon = (float) longitude;
      lat = ( (int) (latitude / gridSpacing) + 1) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1+gridPointsPerLatitude;
      float[] vals1 = getPeriodValues(recNum1);
      float[] vals2 = getPeriodValues(recNum2);
      float flat = (float)(latitude - lat)/gridSpacing;
      saArray =getPeriodValues(vals1,vals2,flat);
    }
    else if (longitude > minLon && longitude < maxLon &&
             longitude > minLon && longitude < maxLon) {
      lat = ( (int) (latitude / gridSpacing) + 1) * gridSpacing;
      lon = ( (int) (longitude / gridSpacing)) * gridSpacing;
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1+1;
      int recNum3 = recNum1+gridPointsPerLatitude;
      int recNum4 = recNum3+1;
      float[] vals1 = getPeriodValues(recNum1);
      float[] vals2 = getPeriodValues(recNum2);
      float[] vals3 = getPeriodValues(recNum3);
      float[] vals4 = getPeriodValues(recNum4);
      float flon = (float)(longitude - lon)/gridSpacing;
      float flat = (float)(latitude - lat)/gridSpacing;
      float[] periodVals1 =getPeriodValues(vals1,vals2,flon);
      float[] periodVals2 =getPeriodValues(vals3,vals4,flon);
      saArray = getPeriodValues(periodVals1,periodVals2,flat);
    }
    else{
      new RuntimeException("Latitude and Longitude outside the Region bounds");
    }

    return createFunction(saArray);
  }


  private ArbitrarilyDiscretizedFunc createFunction(float[] saVals){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    for(int i=0;i<numPeriods;++i)
      function.set(saPeriods[i],saVals[i]);
    return function;
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
    int recNum = (rowIndex-1)*gridPointsPerLatitude+colIndex+(colIndex-1)+1;
    return recNum;
  }





  public static void main(String[] args) {
    NEHRP_FileReader NEHRP_FileReader1 =new NEHRP_FileReader();
    NEHRP_FileReader1.getRegionEndPoints("/Users/nitingupta/projects/USGS_DataFiles/USGS_DataFiles/1997-CANV-MCE-R2.rnd");
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
      record.numValues = GlobalConstants.swap(fin.readInt());
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
  public long recordNumber;
  public float latitude;
  public float longitude;
  public int numValues;
  public float[] values= new float[2];
  public static final int recordLength = 32+32+32+16+32+32;
}
