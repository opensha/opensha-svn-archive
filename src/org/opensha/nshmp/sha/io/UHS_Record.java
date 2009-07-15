package org.opensha.nshmp.sha.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.opensha.commons.util.ByteSwapUtil;
import org.opensha.nshmp.util.GlobalConstants;


/**
 * <p>Title: UHS_Record </p>
 *
 * <p>Description: Creates the record type for the UHS</p>
 * @author Ned Field , Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class UHS_Record
    extends DataRecord {

  //Hazard Period
  public float uhsFex;

  //Record Length
  public static final int recordLength = 4 + 4 + 4 + 4 + 4 + (7 * 4);

  public void getRecord(String fileName, long recordNum) {
    values = new float[7];
    RandomAccessFile fin = null;
    try {
      fin = new RandomAccessFile(fileName, "r");
      fin.seek( (recordNum - 1) * recordLength);
      recordNumber = ByteSwapUtil.swap(fin.readInt());
      latitude = ByteSwapUtil.swapIntToFloat(fin.readInt());
      longitude = ByteSwapUtil.swapIntToFloat(fin.readInt());
      uhsFex = ByteSwapUtil.swapIntToFloat(fin.readInt());
      numValues = ByteSwapUtil.swap(fin.readShort());
      for (int i = 0; i < numValues; ++i) {
        values[i] = ByteSwapUtil.swapIntToFloat(fin.readInt());
        values[i] /= GlobalConstants.DIVIDING_FACTOR_HUNDRED;
      }
      fin.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

}
