package gov.usgs.sha.data;

import gov.usgs.exceptions.ZipCodeErrorException;
import gov.usgs.sha.data.calc.FaFvCalc;
import gov.usgs.sha.data.calc.ResidentialSiteCalc;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import gov.usgs.util.GlobalConstants;
import java.text.DecimalFormat;

/**
 * <p>Title: DataGenerator_IRC</p>
 *
 * <p>Description: This class acts as the modal for the application. It computes
 * data needed by the application.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class DataGenerator_IRC
    extends DataGenerator_NEHRP {


  private double residentialSiteVal ;
  private String residentilaSeismicDesignVal;

  private static DecimalFormat siteValFormat = new DecimalFormat("0.00");
  private static final String RESIDENTIAL_SITE_DESIGN = "Residential Seismic Design Category";
  private static final String Ss_S1 = "Ss and S1 = Mapped Spectral Acceleration Values";
  private static final String Site_D = "Site Class D - ";
  private static final String MCE_MAP_VALUES = "MCE MAP VALUES";
  private static final String Ss_Val_String = "Short Period Map Value - Ss = ";
  private static final String S1_Val_String = "1.0 sec Period Map Value - S1 = ";
  private static final String RESIDENTIAL_DESIGN_STRING = "RESIDENTIAL DESIGN INFORMATION";
  private static final String SOIL_FACTOR_STRING = "Soil factor for "+Site_D;
  private static final String RESIDENTIAL_SITE_VAL = "Residential Site Value = 2/3 * Fa * Ss = ";
  private static final String RESIDENTIAL_SEIS_DESIGN_VAL ="Residential Seismic Design Category = ";



  private String createInfoString(){
    String info=RESIDENTIAL_SITE_DESIGN+"\n";
    info += Ss_S1+"\n";
    info += Site_D+"Fa = "+ siteValFormat.format(faVal)+", Fv = "+siteValFormat.format(fvVal)+"\n";
    info += MCE_MAP_VALUES+"\n";
    info += Ss_Val_String+siteValFormat.format(getSs())+GlobalConstants.SA_UNITS+"\n";
    info += S1_Val_String+siteValFormat.format(getSa())+GlobalConstants.SA_UNITS+"\n\n";

    info += RESIDENTIAL_DESIGN_STRING+"\n";
    info +=Ss_Val_String+siteValFormat.format(getSs())+GlobalConstants.SA_UNITS+"\n";
    info +=SOIL_FACTOR_STRING+" Fa = "+siteValFormat.format(faVal)+"\n";
    info +=RESIDENTIAL_SITE_VAL+siteValFormat.format(residentialSiteVal)+GlobalConstants.SA_UNITS+"\n";
    info +=RESIDENTIAL_SEIS_DESIGN_VAL+residentilaSeismicDesignVal;
    return info;
  }


  /*
   * Sets the Fa and Fv
   */
  private void setFaFv() {
    FaFvCalc calc = new FaFvCalc();
    String fa = "" + calc.getFa(GlobalConstants.SITE_CLASS_D, getSs());
    faVal = Float.parseFloat(fa);
    String fv = "" + calc.getFv(GlobalConstants.SITE_CLASS_D, getSa());
    fvVal = Float.parseFloat(fv);
  }

  /**
   * Sets the Residential Site values
   */
  private void setResidentialSiteValues() {
    ResidentialSiteCalc calc = new ResidentialSiteCalc();
    residentialSiteVal = calc.calcResidentialSiteValue(faVal, getSs());
    residentilaSeismicDesignVal = calc.getResidentialSeismicDesignCategory(
        residentialSiteVal, dataEdition);
  }

  /**
   * Gets the data for SsS1 in case Territory.
   * Territory is when user is not allowed to enter any zip code or Lat-Lon
   * for the location or if it is GAUM and TAUTILLA.
   */
  public void calculateSsS1() {
    super.calculateSsS1();
    clearData();
    setFaFv();
    setResidentialSiteValues();
    addDataInfo(createInfoString());
  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon) {
    super.calculateSsS1(lat,lon);
    clearData();
    setFaFv();
    setResidentialSiteValues();
    addDataInfo(createInfoString());
  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException {
    super.calculateSsS1(zipCode);
    clearData();
    setFaFv();
    setResidentialSiteValues();
    addDataInfo(createInfoString());
  }

}
