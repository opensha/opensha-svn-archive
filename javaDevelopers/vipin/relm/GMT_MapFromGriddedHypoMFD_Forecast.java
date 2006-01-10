package javaDevelopers.vipin.relm;

import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.data.XYZ_DataSetAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import java.io.FileWriter;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.data.Location;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.data.region.EvenlyGriddedRELM_Region;


/**
 * <p>Title: ViewGriddedHypoMFD_Forecast.java </p>
 * <p>Description: This class accepts Gridded Hypo Mag Freq Dist forecast and makes
 * a map or generates a file in RELM format</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GMT_MapFromGriddedHypoMFD_Forecast {
  private GriddedHypoMagFreqDistForecast griddedHypoMFD;


  /**
   * constructor accepts GriddedHypoMagFreqDistForecast
   * @param griddedHypoMagFreqDistForecast
   */
  public GMT_MapFromGriddedHypoMFD_Forecast(
      GriddedHypoMagFreqDistForecast griddedHypoMagFreqDistForecast) {
    setGriddedHypoMagFreqDistForecast( griddedHypoMagFreqDistForecast);
  }

  /**
   * Set the GriddedHypoMagFreqDistForecast
   * @param griddedHypoMagFreqDistForecast
   */
  public void setGriddedHypoMagFreqDistForecast(
      GriddedHypoMagFreqDistForecast griddedHypoMagFreqDistForecast) {
    this.griddedHypoMFD = griddedHypoMagFreqDistForecast;
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
    mapGenerator.setParameter(GMT_MapGenerator.LOG_PLOT_NAME, new Boolean(false));
    mapGenerator.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);

    try {
      String metadata = "Rate Above magnitude " + mag;
      String imageFileName = mapGenerator.makeMapUsingServlet(xyzData, "Rates",
                                         metadata, null);
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
    //eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME,
    //                            Frankel02_AdjustableEqkRupForecast.BACK_SEIS_INCLUDE);
    //eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME,
    //                            Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
    eqkRupForecast.updateForecast();
    try {
      // region to view the rates
      EvenlyGriddedRELM_Region evenlyGriddedRegion  = new EvenlyGriddedRELM_Region();
      // min mag, maxMag, These are Centers of first and last bin
      double minMag=5, maxMag=9;
      int numMag = 9; // number of Mag bins
      // make GriddedHypoMFD Forecast from the EqkRupForecast
      GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
          new ERF_ToGriddedHypoMagFreqDistForecast(eqkRupForecast, evenlyGriddedRegion,
          minMag, maxMag, numMag);

      /*GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
          new ReadRELM_FileIntoGriddedHypoMFD_Forecast("alm.forecast", evenlyGriddedRegion,
          minMag, maxMag, numMag);
      */

      // Make GMT map of rates
      GMT_MapFromGriddedHypoMFD_Forecast viewRates = new GMT_MapFromGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
      viewRates.makeMap(5.5);

      // write into RELM formatted file
      WriteRELM_FileFromGriddedHypoMFD_Forecast writeRELM_File = new WriteRELM_FileFromGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
      writeRELM_File.setModelVersionAuthor("NSHMP-2002", "OpenSHA version", "Rewritten by Field in Java");
      writeRELM_File.setIssueDate(2006, 0,0,0,0,0,0);
      writeRELM_File.setForecastStartDate(2002, 0,0,0,0,0,0);
      writeRELM_File.setDuration(1, "years");
      writeRELM_File.makeFileInRELM_Format("testrelm.txt");

    }catch(Exception e) {
      e.printStackTrace();
    }
  }

}