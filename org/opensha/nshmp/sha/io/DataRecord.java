package org.opensha.nshmp.sha.io;

/**
 * <p>Title: DataRecord</p>
 *
 * <p>Description: Creates the record type.</p>
 * @author Ned Field , Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public abstract class DataRecord {
  //record number
  protected int recordNumber;

  //Location latitude
  protected float latitude;

  //Location longitude
  protected float longitude;

  //number of values
  protected short numValues;

  //SA values
  protected float[] values;

  /**
   * Reads the Record
   * @param fileName String
   * @param recordNum long
   */
  public abstract void getRecord(String fileName, long recordNum);

  /**
   * Returns the Latitude of the record
   * @return float
   */
  public float getLatitude() {
    return latitude;
  }

  /**
   * Returns the Longitude of the record
   * @return float
   */
  public float getLongitude() {
    return longitude;
  }

  /**
   * Returns the number of periods
   * @return short
   */
  public short getNumPeriods() {
    return numValues;
  }

  /**
   * Returns the Periods
   * @return float[]
   */
  public float[] getPeriods() {
    return values;
  }
}
