package gov.usgs.sha.calc;

import gov.usgs.exceptions.ZipCodeErrorException;
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
     * @param hazardCurveFunction ArbitrarilyDiscretizedFunc
     * @param fex double Frequency of exceedance = 1/ReturnPd
     * @param expTime double
     * @return double
     */
    public double computeExceedProb(
      double fex, double expTime) {
      SingleValueHazardCurveCalculator calc = new SingleValueHazardCurveCalculator();
      return calc.calculateProbExceed(fex,expTime);

    }

  /**
   *
   * @param exceedProb double
   * @param expTime double
   * @return double
   */
  public double computeReturnPeriod(double exceedProb, double expTime) {
      SingleValueHazardCurveCalculator calc = new SingleValueHazardCurveCalculator();
      return calc.calculateReturnPeriod(exceedProb,expTime);
  }



  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc computeHazardCurve(String selectedRegion,
      String selectedEdition,
      double latitude,
      double longitude,String hazCurveType) {

    HazardCurveCalculator calc = new HazardCurveCalculator();
    return calc.getBasicHazardCurve(selectedRegion, selectedEdition, latitude,
                                    longitude,hazCurveType);

  }

  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param zipCode String
   * @return ArbitrarilyDiscretizedFunc
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc computeHazardCurve(String selectedRegion,
      String selectedEdition,
      String zipCode,String hazCurveType) throws ZipCodeErrorException {
    HazardCurveCalculator calc = new HazardCurveCalculator();
    return calc.getBasicHazardCurve(selectedRegion, selectedEdition, zipCode,hazCurveType);
  }


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
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc computeSsS1(String selectedRegion,
                                                String selectedEdition,
                                                double latitude,
                                                double longitude,
                                                String spectraType) {
    SsS1Calculator ssS1Calc = new SsS1Calculator();
    return ssS1Calc.getSsS1(selectedRegion, selectedEdition, latitude,
                            longitude,spectraType);
  }


  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc computeSsS1(String selectedRegion,
                                         String selectedEdition,
                                         String zipCode) throws
      ZipCodeErrorException {
    SsS1Calculator ssS1Calc = new SsS1Calculator();
    return ssS1Calc.getSsS1(selectedRegion, selectedEdition, zipCode);
  }


  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc computeSsS1(String selectedRegion,
                                         String selectedEdition,
                                         String zipCode,String spectraType) throws
      ZipCodeErrorException {
    SsS1Calculator ssS1Calc = new SsS1Calculator();
    return ssS1Calc.getSsS1(selectedRegion, selectedEdition, zipCode,spectraType);
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


  /**
   *
   * @param function ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc computeSMSsS1(ArbitrarilyDiscretizedFunc
                                                  function,
                                                  float fa, float fv,
                                                  String siteClass) {

    SMSsS1Calculator calc = new SMSsS1Calculator();
    return calc.calculateSMSsS1(function,fa,fv,siteClass);
  }


  /**
   *
   * @param function ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc computeSDSsS1(ArbitrarilyDiscretizedFunc
                                                  function,
                                                  float fa, float fv,
                                                  String siteClass) {
    SDSsS1Calculator calc = new SDSsS1Calculator();
    return calc.calculateSDSsS1(function,fa,fv,siteClass);
  }


  /**
   *
   * @param function ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList computeMapSpectrum(ArbitrarilyDiscretizedFunc function) {

      SpectrumCalculator calc = new SpectrumCalculator();
      return calc.calculateMapSpectrum(function);
  }

  /**
   *
   * @param function ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList computeSMSpectrum(ArbitrarilyDiscretizedFunc
                                               function, float fa, float fv,
                                                  String siteClass) {

    SpectrumCalculator calc = new SpectrumCalculator();
    return calc.calculateSMSpectrum(function,fa,fv,siteClass);
  }


  /**
   *
   * @param function ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList computeSDSpectrum(ArbitrarilyDiscretizedFunc
                                               function, float fa, float fv,
                                                  String siteClass) {

    SpectrumCalculator calc = new SpectrumCalculator();
    return calc.calculateSDSpectrum(function,fa,fv,siteClass);
  }


}
