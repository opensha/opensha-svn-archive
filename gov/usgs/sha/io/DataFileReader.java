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
import gov.usgs.sha.data.NEHRP_Record;



/**
 * <p>Title: DataFileReader</p>
 * <p>Description: This class allows user to read the data for NEHRP from the data files.</p>
 * @author : Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */

public class DataFileReader {




  public NEHRP_Record getRecord(String fileName,long recordNum){
    NEHRP_Record record = new NEHRP_Record();
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
