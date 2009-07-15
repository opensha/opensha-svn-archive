package org.opensha.nshmp.sha.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.opensha.commons.util.ByteSwapUtil;

/**
 * <p>Title: HazardCurves_Record </p>
 *
 * <p>Description: Creates the record type for the Hazard Curves </p>
 * @author Ned Field , Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class HazardCurves_Record
    extends DataRecord {

  //Hazard Period
  public float hazPeriod;

  //Record Length
  public static final int recordLength = 4 + 4 + 4 + 4 + 4 + (20 * 4);

  public void getRecord(String fileName, long recordNum) {
    values = new float[20];
    RandomAccessFile fin = null;
    try {
      fin = new RandomAccessFile(fileName, "r");
      fin.seek( (recordNum - 1) * recordLength);
      recordNumber = ByteSwapUtil.swap(fin.readInt());
      latitude = ByteSwapUtil.swapIntToFloat(fin.readInt());
      longitude = ByteSwapUtil.swapIntToFloat(fin.readInt());
      hazPeriod = ByteSwapUtil.swapIntToFloat(fin.readInt());
      numValues = ByteSwapUtil.swap(fin.readShort());
      
      for (int i = 0; i < numValues; ++i) {
         values[i] = ByteSwapUtil.swapIntToFloat(fin.readInt());
      }

      fin.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

}
