package org.opensha.nshmp.sha.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.util.ByteSwapUtil;



/**
 * <p>Title: NEHRP_Record </p>
 *
 * <p>Description: Creates the record type for the NEHRP </p>
 * @author Ned Field , Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class NEHRP_Record
    extends DataRecord {

  //Record Length
  public static final int recordLength = 4 + 4 + 4 + 4 + 4 + 4;

	public NEHRP_Record() {
		}

  public void getRecord(String fileName, long recordNum) {
    //SA values
    values = new float[2];
    RandomAccessFile fin = null;
    try {
      fin = new RandomAccessFile(fileName, "r");
      fin.seek( (recordNum - 1) * recordLength);
      recordNumber = ByteSwapUtil.swap(fin.readInt());
      latitude = ByteSwapUtil.swapIntToFloat(fin.readInt());
      longitude = ByteSwapUtil.swapIntToFloat(fin.readInt());
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
