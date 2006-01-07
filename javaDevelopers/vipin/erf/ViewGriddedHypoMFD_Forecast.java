package javaDevelopers.vipin.erf;

import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.data.XYZ_DataSetAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;

/**
 * <p>Title: ViewGriddedHypoMFD_Forecast.java </p>
 * <p>Description: This class accepts Gridded Hypo Mag Freq Dist forecast and makes
 * a map or generates a file in RELM format</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewGriddedHypoMFD_Forecast {
  private GriddedHypoMagFreqDistForecast griddedHypoMFD;


  /**
   * constructor accepts GriddedHypoMagFreqDistForecast
   * @param griddedHypoMagFreqDistForecast
   */
  public ViewGriddedHypoMFD_Forecast(
      GriddedHypoMagFreqDistForecast griddedHypoMagFreqDistForecast) {
    this.griddedHypoMFD = griddedHypoMagFreqDistForecast;
  }

  /**
   * Set the GriddedHypoMagFreqDistForecast
   * @param griddedHypoMagFreqDistForecast
   */
  public void setGriddedHypoMagFreqDistForecast(
      GriddedHypoMagFreqDistForecast griddedHypoMagFreqDistForecast) {
  }

  /**
   * Calculate the rates above the specified magnitude for each location
   * and display in a map
   *
   * @param mag
   */
  public void makeMap(double mag) {
    XYZ_DataSetAPI xyzData = griddedHypoMFD.getXYZ_DataAboveMag(mag);
    EvenlyGriddedGeographicRegionAPI region  = griddedHypoMFD.getEvenlyGriddedGeographicRegion();
    // make GMT_MapGenerator to make the map
    GMT_MapGenerator mapGenerator = new GMT_MapGenerator();

    // TODO :   SET VARIOUS GMT PARAMETERS TO MAKE MAP
    mapGenerator.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME, new Double(region.getMinGridLat()));
    mapGenerator.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME, new Double(region.getMaxGridLat()));
    mapGenerator.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME, new Double(region.getMinGridLon()));
    mapGenerator.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME, new Double(region.getMaxGridLon()));
    mapGenerator.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, new Double(region.getGridSpacing()));


    try {
      String metadata = "Rate Above magnitude " + mag;
      String imageFileName = mapGenerator.makeMapUsingWebServer(xyzData, "Rates",
                                         metadata);
      new ImageViewerWindow(imageFileName, metadata, true);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * This test will make Frankel02 ERF, convert it into GriddedHypoMagFreqDistForecast and
   * then view it.
   *
   * @param args
   */
  public static void main(String[] args) {
    EqkRupForecast eqkRupForecast = new Frankel02_AdjustableEqkRupForecast();
    // include background sources as point sources
    eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME,
                                Frankel02_AdjustableEqkRupForecast.BACK_SEIS_INCLUDE);
    eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME,
                                Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_POINT);
    eqkRupForecast.updateForecast();
    try {
      // region to view the rates
      EvenlyGriddedGeographicRegion evenlyGriddedRegion =
          new EvenlyGriddedRectangularGeographicRegion(33, 34, -118, -117, 0.1);
      // min mag. maxMag, numMag
      double minMag=5, maxMag=9;
      int numMag = 9;
      GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
          new ERF_ToGriddedHypoMagFreqDistForecast(eqkRupForecast, evenlyGriddedRegion,
          minMag, maxMag, numMag);
      // NOW VIEW THE MAP
      ViewGriddedHypoMFD_Forecast viewRates = new ViewGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
      viewRates.makeMap(5.0);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

}