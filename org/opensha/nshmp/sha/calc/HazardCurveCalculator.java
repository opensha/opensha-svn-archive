package org.opensha.nshmp.sha.calc;

import java.text.DecimalFormat;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.nshmp.exceptions.ZipCodeErrorException;
import org.opensha.nshmp.sha.data.SiteInterpolation;
import org.opensha.nshmp.sha.io.DataFileNameSelectorForHazardCurves;
import org.opensha.nshmp.sha.io.HazardCurves_Record;
import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.nshmp.util.LocationUtil;
import org.opensha.nshmp.util.ZipCodeToLatLonConvertor;
import org.opensha.nshmp.util.ui.DataDisplayFormatter;

/**
 * <p>Title: HazardCurveCalculator</p>
 *
 * <p>Description: Gets the Basic Hazard Curve Values for the given Lat-Lon or
 * a given zipcode.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class HazardCurveCalculator {


  private float gridSpacing;

  private DecimalFormat latLonFormat = new DecimalFormat("0.0000##");

  public final static String EXCEED_PROB_TEXT =
      "Frequency of Exceedance values " +
      "less than\n1E-4 should be used with caution.";
  private final static String PGA_TEXT = "Ground Motion";
  private final static String PROB_TEXT = "Frequency of Exceedance";

  /**
   * Gets the Hazard curves values for the given Lat and Lon from the data file.
   * @param selectedRegion String
   * @param selectedEdition String
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getBasicHazardCurve(String selectedRegion,
      String selectedEdition,
      double latitude, double longitude, String hazCurveType) {

    HazardCurves_Record record = new HazardCurves_Record();
    DataFileNameSelectorForHazardCurves dataFileSelector = new
        DataFileNameSelectorForHazardCurves();
    String fileName = dataFileSelector.getFileName(selectedRegion,
        selectedEdition, latitude, longitude, hazCurveType);
    SiteInterpolation siteSaVals = new SiteInterpolation();
    ArbitrarilyDiscretizedFunc function = siteSaVals.getPeriodValuesForLocation(
        fileName,
        record, latitude, longitude);

    gridSpacing = siteSaVals.getGridSpacing();
    //set the info for the function being added
    String info = "";
    info += hazCurveType+ "\n";

    info += "Latitude = " + latLonFormat.format(latitude) + "\n";
    info += "Longitude = " + latLonFormat.format(longitude) + "\n";

    info += "Data are based on a " + gridSpacing + " deg grid spacing" + "\n";
    info +=
        DataDisplayFormatter.createFunctionInfoString_HazardCurves(function,
        PGA_TEXT, PROB_TEXT, GlobalConstants.SA_UNITS,
        GlobalConstants.ANNUAL_FREQ_EXCEED_UNITS, EXCEED_PROB_TEXT);

    function.setInfo(info);

    return function;
  }

  /**
   * Gets the Hazard Curves values from the given Zipcode from the data file.
   * @param selectedRegion String
   * @param selectedEdition String
   * @param zipCode String
   * @return ArbitrarilyDiscretizedFunc
   * @throws ZipCodeErrorException
   */
  public ArbitrarilyDiscretizedFunc getBasicHazardCurve(String selectedRegion,
      String selectedEdition, String zipCode, String hazCurveType) throws
      ZipCodeErrorException {

    Location loc = ZipCodeToLatLonConvertor.getLocationForZipCode(zipCode);
    LocationUtil.checkZipCodeValidity(loc, selectedRegion);
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //getting the SA Period values for the lat lon for the selected Zip code.
    ArbitrarilyDiscretizedFunc function = getBasicHazardCurve(selectedRegion,
        selectedEdition,
        lat, lon, hazCurveType);
    //set the info for the function being added
    String info = "";
    info += hazCurveType+ "\n";

    info += "Zip Code - " + zipCode + "\n";
    info += "Zip Code Latitude = " + latLonFormat.format(lat) + "\n";
    info += "Zip Code Longitude = " + latLonFormat.format(lon) + "\n";
    info += "Data are based on a " + gridSpacing + " deg grid spacing" + "\n";
    info +=
        DataDisplayFormatter.createFunctionInfoString_HazardCurves(function,
        PGA_TEXT, PROB_TEXT, GlobalConstants.SA_UNITS,
        GlobalConstants.ANNUAL_FREQ_EXCEED_UNITS, EXCEED_PROB_TEXT);

    function.setInfo(info);

    return function;

  }

}
