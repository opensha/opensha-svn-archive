package org.opensha.nshmp.sha.data;

import java.text.DecimalFormat;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.nshmp.sha.data.region.RegionBounds;
import org.opensha.nshmp.sha.io.DataRecord;
import org.opensha.nshmp.sha.io.HazardCurves_Record;
import org.opensha.nshmp.util.GlobalConstants;

/**
 * <p>Title: SiteInterpolation</p>
 *
 * <p>Description: This class finds the 4 nearest locations to a given location. Then
 * using the bi-linear interpolation finds the values at this given location.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class SiteInterpolation {

  /**
   *
   */
  private float minLat;
  private float minLon;
  private float maxLat;
  private float maxLon;
  private float gridSpacing;
  private int gridPointsPerLatitude;
  private float[] saPeriods;
  private int numPeriods;
  private DecimalFormat gridSpacingFormat = new DecimalFormat("0.00");

  public ArbitrarilyDiscretizedFunc getPeriodValuesForLocation(String fileName,
      DataRecord record,
      double latitude, double longitude) {
    float lat = 0;
    float lon = 0;
    float[] saArray = null;

    getRegionBounds(record, fileName);
    if ( (latitude == minLat && longitude == minLon) ||
        (latitude == maxLat && longitude == minLon) ||
        (latitude == minLat && longitude == maxLon) ||
        (latitude == maxLat && longitude == maxLon)) { // Any corner
      lat = (float) latitude;
      lon = (float) longitude;
      int recNum = getRecordNumber(lat, lon);
      saArray = getPeriodValues(record, fileName, recNum);
    }
    else if ( (latitude == minLat || latitude == maxLat) &&
             (longitude > minLon && longitude < maxLon)) {
      lat = (float) latitude;
      lon = getNearestGridLon(longitude);
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + 1;
      float[] vals1 = getPeriodValues(record, fileName, recNum1);
      float[] vals2 = getPeriodValues(record, fileName, recNum2);
      float flon = (float) (longitude - lon) / gridSpacing;
      saArray = getPeriodValues(vals1, vals2, flon);
    }
    else if ( (longitude == minLon || longitude == maxLon) &&
             (latitude > minLat && latitude < maxLat)) {
      lon = (float) longitude;
      lat = getNearestGridLat(latitude);
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + gridPointsPerLatitude;
      float[] vals1 = getPeriodValues(record, fileName, recNum1);
      float[] vals2 = getPeriodValues(record, fileName, recNum2);
      float flat = (float) (lat - latitude) / gridSpacing;
      saArray = getPeriodValues(vals1, vals2, flat);
    }
    else if (latitude > minLat && latitude < maxLat &&
             longitude > minLon && longitude < maxLon) {
      lat = getNearestGridLat(latitude);
      lon = getNearestGridLon(longitude);
      int recNum1 = getRecordNumber(lat, lon);
      int recNum2 = recNum1 + 1;
      int recNum3 = recNum1 + gridPointsPerLatitude;
      int recNum4 = recNum3 + 1;
      float[] vals1 = getPeriodValues(record, fileName, recNum1);
      float[] vals2 = getPeriodValues(record, fileName, recNum2);
      float[] vals3 = getPeriodValues(record, fileName, recNum3);
      float[] vals4 = getPeriodValues(record, fileName, recNum4);
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
    return function;

  }

  /**
   * Getting the data from the file which provides the info. about the region bounds.
   * @param fileName String
   */
  private void getRegionBounds(DataRecord record, String fileName) {
    RegionBounds regionBounds = new RegionBounds(record, fileName);
    minLat = regionBounds.getMinLat();
    minLon = regionBounds.getMinLon();
    maxLat = regionBounds.getMaxLat();
    maxLon = regionBounds.getMaxLon();
    gridSpacing = regionBounds.getGridSpacing();
    gridPointsPerLatitude = regionBounds.getNumPointsPerLatitude();
    saPeriods = regionBounds.getSA_Periods();
    //as we are dividing the Periods vals by the dividing factor in case of NEHRP
    // we are multiplying it back by the dividing factor because it is the actuaa
    //SA Periods that we have to get the values for.
    if (! (record instanceof HazardCurves_Record)) {
      int size = saPeriods.length;
      for (int i = 0; i < size; ++i) {
        saPeriods[i] *= GlobalConstants.DIVIDING_FACTOR_HUNDRED;
      }
    }
    numPeriods = regionBounds.getNumPeriods();
  }

	public void printRegionBounds(DataRecord record, String fileName) {
		getRegionBounds(record, fileName);
		System.out.println("(minLat, minLon) = (" + minLat + ", " + minLon + ")");
		System.out.println("(maxLat, maxLon) = (" + maxLat + ", " + maxLon + ")");
	}
		
  /**
   *
   * @param latitude double
   * @return float
   */
  private float getNearestGridLat(double latitude) {

    String latGridVal = gridSpacingFormat.format(latitude / gridSpacing);
    double latVal = Math.ceil(Double.parseDouble(latGridVal));

    return ( (int) latVal) * gridSpacing;
  }

  /**
   *
   * @param recNo int
   * @return float[]
   */
  private float[] getPeriodValues(DataRecord record, String fileName, int recNo) {
    record.getRecord(fileName, recNo);

    return record.getPeriods();

  }

  private ArbitrarilyDiscretizedFunc createFunction(float[] saVals) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    for (int i = 0; i < numPeriods; ++i) {
      function.set(saPeriods[i], saVals[i]);
    }
    return function;
  }

  /**
   *
   * @param lat float
   * @param lon float
   * @return int
   */
  private int getRecordNumber(float lat, float lon) {
    String lonGridVal = gridSpacingFormat.format( (lon - minLon) / gridSpacing);
    String latGridVal = gridSpacingFormat.format( (maxLat - lat) / gridSpacing);
    int colIndex = (int) (Float.parseFloat(lonGridVal)) + 1;
    int rowIndex = (int) (Float.parseFloat(latGridVal)) + 1;
    int recNum = (rowIndex - 1) * gridPointsPerLatitude + (colIndex - 1) + 1;
    return recNum + 3;
  }

  /**
   *
   * @param longitude double
   * @return float
   */
  private float getNearestGridLon(double longitude) {
    String lonGridVal = gridSpacingFormat.format(longitude / gridSpacing);
    double lonVal = Math.floor(Double.parseDouble(lonGridVal));

    return ( (int) lonVal) * gridSpacing;
  }

  /**
   * Returns the gridSpacing
   * @return float
   */
  public float getGridSpacing() {
    return gridSpacing;
  }

  /**
   * Returns the num of Periods
   * @return int
   */
  public int getNumPeriods() {
    return numPeriods;
  }

  /**
   * Returns the Periods Values as read from the file
   * @return float[]
   */
  public float[] getPeriods() {
    return saPeriods;
  }

  /**
   *
   * @param periodVals1 float[]
   * @param periodVals2 float[]
   * @param val float
   * @return float[]
   */
  private float[] getPeriodValues(float[] periodVals1, float[] periodVals2,
                                  float val) {
    float[] periodsVal = new float[numPeriods];
    for (int i = 0; i < numPeriods; ++i) {
      periodsVal[i] = periodVals1[i] + val * (periodVals2[i] - periodVals1[i]);
    }

    return periodsVal;
  }

}
