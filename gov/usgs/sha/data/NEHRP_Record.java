package gov.usgs.sha.data;

/**
 * <p>Title: NEHRP_Record </p>
 *
 * <p>Description: Creates the record type for the NEHRP </p>
 * @author Ned Field , Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class NEHRP_Record {

  //record number
  public int recordNumber;

  //Location latitude
  public float latitude;

  //Location longitude
  public float longitude;

  //number of values
  public short numValues;

  //SA values
  public float[] values = new float[2];

  //Record Length
  public static final int recordLength = 4 + 4 + 4 + 4 + 4 + 4;
}
