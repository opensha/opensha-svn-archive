package gov.usgs.sha.calc;

import gov.usgs.exceptions.ZipCodeErrorException;
import gov.usgs.sha.io.DataFileNameSelector;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncList;

/**
 * <p>Title: HazardDataCalc</p>
 *
 * <p>Description: This class computes the Hazard Data.</p>
 * @author : Ned Field, Nitin Gupta and E.V.Leyendecker
 *
 * @version 1.0
 */
public class HazardDataCalc {

  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc computeSsS1(String selectedRegion,
                                                String selectedEdition,
                                                double latitude,
                                                double longitude) {
    SsS1Calculator ssS1Calc = new SsS1Calculator();
    return ssS1Calc.getSsS1(selectedRegion, selectedEdition, latitude,
                            longitude);
  }


  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public DiscretizedFuncList computeSsS1(String selectedRegion,
                                         String selectedEdition,
                                         String zipCode) throws
      ZipCodeErrorException {
    SsS1Calculator ssS1Calc = new SsS1Calculator();
    return ssS1Calc.getSsS1(selectedRegion, selectedEdition, zipCode);
  }

  /**
   *
   * @param selectedRegion String
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc computeSsS1(String selectedRegion) {
    SsS1Calculator ssS1Calc = new SsS1Calculator();
    return ssS1Calc.getSsS1ForTerritory(selectedRegion);
  }




}
