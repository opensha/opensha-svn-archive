package gov.usgs.sha.io;

import java.io.IOException;
import gov.usgs.util.ByteSwapUtil;
import java.io.RandomAccessFile;

/**
 * <p>Title: UHS_Record </p>
 *
 * <p>Description: Creates the record type for the UHS</p>
 * @author Ned Field , Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class UHS_Record extends DataRecord{


  //Hazard Period
  public float uhsFex;

  //SA values
  public float[] values = new float[7];

  //Record Length
  public static final int recordLength = 4 + 4 + 4 + 4 + 4 + (7*4);

  public void getRecord(String fileName, long recordNum) {
  RandomAccessFile fin = null;
  try {
    fin = new RandomAccessFile(fileName, "r");
    fin.seek( (recordNum - 1) * recordLength);
    recordNumber = ByteSwapUtil.swap(fin.readInt());
    latitude = ByteSwapUtil.swap(fin.readFloat());
    longitude = ByteSwapUtil.swap(fin.readFloat());
    numValues = ByteSwapUtil.swap(fin.readShort());
    values[0] = ByteSwapUtil.swap(fin.readFloat());
    values[1] = ByteSwapUtil.swap(fin.readFloat());

    fin.close();
  }    catch (IOException ex) {
    ex.printStackTrace();
  }
}

}
