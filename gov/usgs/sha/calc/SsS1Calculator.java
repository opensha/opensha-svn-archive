package gov.usgs.sha.calc;

import org.scec.data.function.DiscretizedFuncList;
import gov.usgs.sha.io.NEHRP_FileReader;
import gov.usgs.exceptions.ZipCodeErrorException;

/**
 * <p>Title: SsS1Calculator</p>
 *
 * <p>Description: This class computes the Ss and S1 based on the location inputs
 * provided by the user in the application.</p>
 *
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class SsS1Calculator {

  /**
   * Class default constructor
   */
  public SsS1Calculator() {
  }

  /**
   * Calculates the Ss and S1 when location is provided using the Lat and Lon
   * @param geographicRegion String
   * @param dataEdition String
   * @param lat double
   * @param lon double
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList getSsS1(String geographicRegion,
                                     String dataEdition, double lat, double lon) {

    NEHRP_FileReader fileReader = new NEHRP_FileReader();
    return fileReader.getSsS1(geographicRegion, dataEdition, lat, lon);
  }


  /**
   * Calculates the Ss and S1 when location is provided using the zipCode
   * @param geographicRegion String
   * @param dataEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public DiscretizedFuncList getSsS1(String geographicRegion,
                                     String dataEdition, String zipCode) throws
      ZipCodeErrorException {

    NEHRP_FileReader fileReader = new NEHRP_FileReader();
    return fileReader.getSsS1(geographicRegion, dataEdition, zipCode);
  }

  /**
   * Calculates the Ss and S1 when geographic region provided is  a territory.
   * @param geographicRegion String
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList getSsS1(String geographicRegion) {

    NEHRP_FileReader fileReader = new NEHRP_FileReader();
    return fileReader.getSsS1ForTerritory(geographicRegion);
  }
}
