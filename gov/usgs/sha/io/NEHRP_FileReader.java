package gov.usgs.sha.io;
import org.scec.data.region.RectangularGeographicRegion;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NEHRP_FileReader {
  private String fileName;
  private RectangularGeographicRegion regionEndPoints;

  public NEHRP_FileReader(String fileName) {
    this.fileName = fileName;
    RandomAccessFile fin = null;
    try {
      fin = new RandomAccessFile(fileName, "r");
      byte[]  b = new byte[4];
      int recordNum = fin.readInt();
      System.out.println("recordNum="+recordNum);
      System.out.println("swap recordNum="+swap(recordNum));
      float lat = fin.readFloat();
      System.out.println("latitude="+lat);
      System.out.println("swap latitude="+swap(lat));

      fin.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

  }


  public static int swap (int value)
  {
    int b1 = (value >>  0) & 0xff;
    int b2 = (value >>  8) & 0xff;
    int b3 = (value >> 16) & 0xff;
    int b4 = (value >> 24) & 0xff;

    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
  }


    public static float swap (float value)
    {
      int intValue = Float.floatToIntBits (value);
      intValue = swap (intValue);
      return Float.intBitsToFloat (intValue);
    }

  public RectangularGeographicRegion getRegionEndPoints() {
    return regionEndPoints;
  }



  public static void main(String[] args) {
    NEHRP_FileReader NEHRP_FileReader1 =
        new NEHRP_FileReader("D:\\2004CD-MasterDataFiles-Files2\\1997-CANV-MCE-R2.rnd");
  }

}