package gov.usgs.sha.data;

import org.scec.data.function.DiscretizedFuncList;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import gov.usgs.exceptions.ZipCodeErrorException;
import gov.usgs.sha.calc.HazardDataCalc;



/**
 * <p>Title: HazardDataMiner</p>
 *
 * <p>Description: This class computes the Ss and S1 based on the location inputs
 * provided by the user in the application.</p>
 *
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class HazardDataMiner {

  /**
   * Class default constructor
   */
  public HazardDataMiner() {
  }



  /**
   *
   * @param hazardCurveFunction ArbitrarilyDiscretizedFunc
   * @param fex double Frequency of exceedance = 1/ReturnPd
   * @param expTime double
   * @return double
   */
  public double getExceedProb(
      double fex, double expTime) {
      HazardDataCalc calc = new HazardDataCalc();
      return calc.computeExceedProb(fex,expTime);
  }

  /**
   *
   * @param exceedProb double
   * @param expTime double
   * @return double
   */
  public double getReturnPeriod(double exceedProb, double expTime) {
      HazardDataCalc calc = new HazardDataCalc();
      return calc.computeReturnPeriod(exceedProb,expTime);
  }




  /**
   * Gets the Basic Hazard Curve using the Lat and Lon
   * @param geographicRegion String
   * @param dataEdition String
   * @param lat double
   * @param lon double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getBasicHazardcurve(String geographicRegion,
                                     String dataEdition, double lat, double lon,
                                     String hazCurveType) {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeHazardCurve(geographicRegion, dataEdition, lat, lon,hazCurveType);
  }


  /**
   * Gets the Basic Hazard Curve using the Lat and Lon
   * @param geographicRegion String
   * @param dataEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc getBasicHazardcurve(String geographicRegion,
                                     String dataEdition, String zipCode,
                                     String hazCurveType) throws
      ZipCodeErrorException {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeHazardCurve(geographicRegion, dataEdition, zipCode,hazCurveType);
  }



  /**
   * Gets the Ss and S1 when location is provided using the Lat and Lon
   * @param geographicRegion String
   * @param dataEdition String
   * @param lat double
   * @param lon double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                     String dataEdition, double lat, double lon) {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSsS1(geographicRegion, dataEdition, lat, lon);
  }



  /**
   *
   * @param geographicRegion String
   * @param dataEdition String
   * @param lat double
   * @param lon double
   * @param selectedSpectraType String
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                            String dataEdition, double lat,
                                            double lon,String selectedSpectraType) {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSsS1(geographicRegion, dataEdition, lat, lon,selectedSpectraType);
  }

  /**
   *
   * @param geographicRegion String
   * @param dataEdition String
   * @param lat double
   * @param lon double
   * @param selectedSpectraType String
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSA(String geographicRegion,
                                            String dataEdition, double lat,
                                            double lon,String selectedSpectraType) {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSA(geographicRegion, dataEdition, lat, lon,selectedSpectraType);
  }

  /**
   * Gets the Ss and S1 when location is provided using the zipCode
   * @param geographicRegion String
   * @param dataEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc getSA(String geographicRegion,
                                     String dataEdition, String zipCode,String spectraType) throws
      ZipCodeErrorException {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSA(geographicRegion, dataEdition, zipCode,spectraType);
  }


  /**
   * Gets the Ss and S1 when location is provided using the zipCode
   * @param geographicRegion String
   * @param dataEdition String
   * @param zipCode String
   * @return DiscretizedFuncList
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                     String dataEdition, String zipCode,String spectraType) throws
      ZipCodeErrorException {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSsS1(geographicRegion, dataEdition, zipCode,spectraType);
  }


  /**
    * Gets the Ss and S1 when location is provided using the zipCode
    * @param geographicRegion String
    * @param dataEdition String
    * @param zipCode String
    * @return DiscretizedFuncList
    * @throws ZipCodeErrorException
    */
   public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                      String dataEdition, String zipCode) throws
       ZipCodeErrorException {

     HazardDataCalc calc = new HazardDataCalc();
     return calc.computeSsS1(geographicRegion, dataEdition, zipCode);
  }




  /**
   * Gets the Ss and S1 when geographic region provided is  a territory.
   * @param geographicRegion String
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion) {

    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSsS1(geographicRegion);
  }



  /**
   *
   * @param func ArbitrarilyDiscretizedFunc
   * @param fa double
   * @param fv double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSDSsS1(ArbitrarilyDiscretizedFunc func,
                                              float fa,float fv,String siteClass){
     HazardDataCalc calc = new HazardDataCalc();
     return calc.computeSDSsS1(func,fa,fv,siteClass);
  }


  /**
   *
   * @param func ArbitrarilyDiscretizedFunc
   * @param fa double
   * @param fv double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSMSsS1(ArbitrarilyDiscretizedFunc func,
                                             float fa,float fv,String siteClass){
    HazardDataCalc calc = new HazardDataCalc();
    return calc.computeSMSsS1(func,fa,fv,siteClass);
 }


 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSMSpectrum(ArbitrarilyDiscretizedFunc func,
                                                 float fa, float fv,String siteClass) {
   HazardDataCalc calc = new HazardDataCalc();
   return calc.computeSMSpectrum(func, fa, fv,siteClass);
 }


 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSDSpectrum(ArbitrarilyDiscretizedFunc func,
                                                 float fa, float fv,String siteClass) {
   HazardDataCalc calc = new HazardDataCalc();
   return calc.computeSDSpectrum(func, fa, fv,siteClass);
 }


 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getMapSpectrum(ArbitrarilyDiscretizedFunc func) {
   HazardDataCalc calc = new HazardDataCalc();
   return calc.computeMapSpectrum(func);
 }


}
