package gov.usgs.sha.calc;


import gov.usgs.sha.io.DataFileNameSelectorForUHS;

import gov.usgs.sha.io.UHS_Record;
import gov.usgs.util.*;
import gov.usgs.sha.data.SiteInterpolation;
import gov.usgs.exceptions.ZipCodeErrorException;


import org.scec.data.Location;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import gov.usgs.util.ui.DataDisplayFormatter;


import java.text.DecimalFormat;


/**
 * <p>Title: UHS_SACalculator</p>
 *
 * <p>Description: Computes the values for the Ss and S1 for the given location or
 * territory in USA.</p>
 * @author  Ned Field, Nitin Gupta , E.V.Leyendecker
 * @version 1.0
 */
public class UHS_SACalculator {

  //grid spacing in file
  protected float gridSpacing;


  /**
   * Some static String for the data printing
   */
  protected static final String SsS1_TITLE =
      "Spectral Response Accelerations Ss and S1\n\n";
  protected static final String SsS1_SubTitle =
      "Ss and S1 = Mapped Spectral Acceleration Values";

  protected static final String Ss_Text = "Ss";
  protected static final String S1_Text = "S1";
  protected static final String SA = "Sa";
  protected static final float Fa = 1;
  protected static final float Fv = 1;

  protected DecimalFormat latLonFormat = new DecimalFormat("0.0000##");




  /**
   *
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getSA(String selectedRegion,
                                            String selectedEdition,
                                            double latitude, double longitude,String spectraType) {

    UHS_Record record = new UHS_Record();
    DataFileNameSelectorForUHS dataFileSelector = new
        DataFileNameSelectorForUHS();
    String fileName = dataFileSelector.getFileName(selectedRegion,
                                                   selectedEdition, latitude,
                                                   longitude, spectraType);
    SiteInterpolation siteSaVals = new SiteInterpolation();
    ArbitrarilyDiscretizedFunc function = siteSaVals.getPeriodValuesForLocation(
        fileName, record,
        latitude, longitude);

    gridSpacing = siteSaVals.getGridSpacing();

    //set the info for the function being added
    String info = "";
    info += SsS1_TITLE + "\n";

    info += "Latitude = " + latLonFormat.format(latitude) + "\n";
    info += "Longitude = " + latLonFormat.format(longitude) + "\n";
    info +=
        DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
                                                  GlobalConstants.SITE_CLASS_B,
                                                  Fa, Fv);
    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    info +=
        DataDisplayFormatter.createFunctionInfoString(function, SA, Ss_Text, S1_Text,
        GlobalConstants.SITE_CLASS_B);
    function.setInfo(info);
    return function;
  }





  /**
   *
   * @param zipCode
   * @return
   */
  public ArbitrarilyDiscretizedFunc getSA(String selectedRegion,
                                            String selectedEdition,
                                            String zipCode,String spectraType) throws
      ZipCodeErrorException {
    Location loc = ZipCodeToLatLonConvertor.getLocationForZipCode(zipCode);
    LocationUtil.checkZipCodeValidity(loc, selectedRegion);
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //getting the SA Period values for the lat lon for the selected Zip code.
    ArbitrarilyDiscretizedFunc function = getSA(selectedRegion,
                                                  selectedEdition, lat, lon,spectraType);

    //adding the info for each function
    String info = "";
    info += SsS1_TITLE + "\n";
    info += "Zip Code - " + zipCode + "\n";
    info += "Zip Code Latitude = " + latLonFormat.format(lat) + "\n";
    info += "Zip Code Longitude = " + latLonFormat.format(lon) + "\n";
    info +=
        DataDisplayFormatter.createSubTitleString(SsS1_SubTitle,
                                                  GlobalConstants.SITE_CLASS_B,
                                                  Fa, Fv);
    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    info +=
        DataDisplayFormatter.createFunctionInfoString(function, SA,
        Ss_Text, S1_Text, GlobalConstants.SITE_CLASS_B);
    function.setInfo(info);

    return function;
  }

}
