package gov.usgs.sha.calc;


import gov.usgs.sha.io.DataFileNameSelectorForUHS;

import gov.usgs.sha.io.UHS_Record;
import gov.usgs.util.*;
import gov.usgs.sha.data.SiteInterpolation;
import gov.usgs.exceptions.ZipCodeErrorException;


import org.scec.data.Location;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncList;
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
  protected static final String SA_TITLE =
      "Uniform Hazard Spectrum (UHS) for ";

  private static final String BC_BOUNDARY_STRING = "B/C Boundary";

  protected DecimalFormat latLonFormat = new DecimalFormat("0.0000##");

  /*
   * Computes the Std Displacement function using the SA function.
   */
  private ArbitrarilyDiscretizedFunc calcSDTFunction(ArbitrarilyDiscretizedFunc saFunction){
    StdDisplacementCalc calc = new StdDisplacementCalc();
    ArbitrarilyDiscretizedFunc sdTFunction = calc.getStdDisplacement(saFunction);
    return sdTFunction;
  }


  /**
   *
   * @param latitude double
   * @param longitude double
   * @return ArbitrarilyDiscretizedFunc
   */
  public DiscretizedFuncList getSA(String selectedRegion,
                                          String selectedEdition,
                                          double latitude, double longitude,
                                          String spectraType) {

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
    function.setName(GlobalConstants.UNIFORM_HAZARD_SPECTRUM_NAME + " of " +
                     GlobalConstants.SA_Vs_T_GRAPH_NAME);
    ArbitrarilyDiscretizedFunc sdTFunction = calcSDTFunction(function);
    gridSpacing = siteSaVals.getGridSpacing();


    DiscretizedFuncList funcList = new DiscretizedFuncList();
    funcList.add(sdTFunction);
    funcList.add(function);
    funcList.setInfo(setInfo(funcList,latitude,longitude,spectraType));
    return funcList;
  }

  private String setInfo(DiscretizedFuncList funcList,
      double latitude,double longitude, String spectraType){
    //set the info for the function being added
    String info = "";
    info += SA_TITLE+spectraType+ "\n\n";

    info += "Latitude = " + latLonFormat.format(latitude) + "\n";
    info += "Longitude = " + latLonFormat.format(longitude) + "\n";
    info +=  BC_BOUNDARY_STRING+"\n";

    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    info +=
        DataDisplayFormatter.createFunctionInfoString(funcList,GlobalConstants.SITE_CLASS_B);

    return info;
  }


  private String setInfoForZipCode(DiscretizedFuncList funcList, String zipCode,
                                   double lat, double lon, String spectraType) {
    //adding the info for each function
    //set the info for the function being added
    String info = "";
    info += SA_TITLE + spectraType + "\n\n";

    info += "Zip Code - " + zipCode + "\n";
    info += "Zip Code Latitude = " + latLonFormat.format(lat) + "\n";
    info += "Zip Code Longitude = " + latLonFormat.format(lon) + "\n";
    info += BC_BOUNDARY_STRING + "\n";

    info += "Data are based on a " + gridSpacing + " deg grid spacing";
    info +=
        DataDisplayFormatter.createFunctionInfoString(funcList,
        GlobalConstants.SITE_CLASS_B);
    return info;

  }

  /**
   *
   * @param zipCode
   * @return
   */
  public DiscretizedFuncList getSA(String selectedRegion,
                                            String selectedEdition,
                                            String zipCode,String spectraType) throws
      ZipCodeErrorException {
    Location loc = ZipCodeToLatLonConvertor.getLocationForZipCode(zipCode);
    LocationUtil.checkZipCodeValidity(loc, selectedRegion);
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //getting the SA Period values for the lat lon for the selected Zip code.
    DiscretizedFuncList funcList = getSA(selectedRegion,
                                                  selectedEdition, lat, lon,spectraType);
    funcList.setInfo(setInfoForZipCode(funcList,zipCode,lat, lon,spectraType));
    return funcList;
  }

}
