package org.opensha.nshmp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.opensha.commons.data.Location;
import org.opensha.nshmp.exceptions.ZipCodeErrorException;

/**
 * <p>Title: ZipCodeToLatLonConvertor</p>
 *
 * <p>Description: This class converts the ZipCode for the location
 * to a Latitude and Longitude</p>
 * @author Ned Field,Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public final class ZipCodeToLatLonConvertor {

  private static final String ZIP_CODE_TO_LAT_LON_FILE = GlobalConstants.
  DATA_FILE_PATH + "2003-ZipCodes.txt";

  /**
   * Returns the location for the selected zip code
   * @param zipCode String
   * @return Location
   * @throws ZipCodeNotFoundException
   */
  public static Location getLocationForZipCode(String zipCode) throws
      ZipCodeErrorException {
    Location loc = null;
    boolean zipCodeFound = false;
    try {

      FileReader fin = new FileReader(ZIP_CODE_TO_LAT_LON_FILE);
      BufferedReader bin = new BufferedReader(fin);

      // now read line by line until the zip code is found in file
      String str = bin.readLine();
      while (str != null) {
        StringTokenizer tokenizer = new StringTokenizer(str);
        String lineZipCode = tokenizer.nextToken();
        if (lineZipCode.equalsIgnoreCase(zipCode)) {
          zipCodeFound = true;
          double lat = Float.parseFloat(tokenizer.nextToken().trim());
          double lon = Float.parseFloat(tokenizer.nextToken().trim());
          loc = new Location(lat, lon);
          break;
        }
        str = bin.readLine();
      }
      if (zipCodeFound == false) {
        throw new ZipCodeErrorException(
            "The Zip Code is not in the data file. Try another or use Lat-Lon for the location.");
      }
      bin.close();
      fin.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return loc;
  }
}
