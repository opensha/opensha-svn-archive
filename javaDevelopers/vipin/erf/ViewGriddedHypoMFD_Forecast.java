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
import java.io.FileWriter;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.data.Location;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

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
  private final static double DEPTH_MIN = 0;
  private final static double DEPTH_MAX = 30;
  private final static int MASK = 1;
  private final static String BEGIN_FORECAST = "begin_forecast";
  private final static String END_FORECAST = "end_forecast";

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
   * Writes the GriddedHypoMagFreqDistForecast into an output file. The format
   * of the output file is in RELM format
   */
  public void makeFileInRELM_Format(String outputFileName) {
    try {
      FileWriter fw = new FileWriter(outputFileName);
      // TODO: Write the header lines


      // write the data lines
      fw.write(BEGIN_FORECAST+"\n");
      EvenlyGriddedGeographicRegionAPI region  = griddedHypoMFD.getEvenlyGriddedGeographicRegion();
      int numLocs  = region.getNumGridLocs();
      double lat1, lat2, lon1, lon2;
      double mag1, mag2;
      double gridSpacing = region.getGridSpacing();
      // Iterrate over all locations and write Magnitude frequency distribution for each location
      for(int i=0; i<numLocs; ++i ) {
        HypoMagFreqDistAtLoc hypoMFD_AtLoc = griddedHypoMFD.getHypoMagFreqDistAtLoc(i);
        Location loc = hypoMFD_AtLoc.getLocation();
        lat1 = loc.getLatitude()-gridSpacing/2;
        lat2 = loc.getLatitude()+gridSpacing/2;
        lon1 = loc.getLongitude()-gridSpacing/2;
        lon2 = loc.getLongitude()+gridSpacing/2;
        // write magnitude frequency distribution for each location
        IncrementalMagFreqDist incrementalMFD = hypoMFD_AtLoc.getMagFreqDist()[0];
        for(int j=0; j<incrementalMFD.getNum(); ++j) {
          mag1  = incrementalMFD.getX(j)-incrementalMFD.getDelta()/2;
          mag2  = incrementalMFD.getX(j)+incrementalMFD.getDelta()/2;
          fw.write((float)lon1+" "+(float)lon2+" "+(float)lat1+" "+(float)lat2+" "+DEPTH_MIN+" "+
                   DEPTH_MAX+" "+mag1+" "+mag2+" "+incrementalMFD.getIncrRate(j)+ " "+MASK+"\n");
        }
      }
      fw.write(END_FORECAST+"\n");
      fw.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
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

    /*eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME,
                                Frankel02_AdjustableEqkRupForecast.BACK_SEIS_INCLUDE);
    eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME,
                                Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
        */
    eqkRupForecast.updateForecast();
    try {
      // region to view the rates
      EvenlyGriddedGeographicRegion evenlyGriddedRegion =
          new EvenlyGriddedRectangularGeographicRegion(33, 35, -119, -117.5, 0.01);

      // min mag, maxMag, These are Centers of first and last bin
      double minMag=5, maxMag=9;
      int numMag = 9; // number of Mag bins
      GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
          new ERF_ToGriddedHypoMagFreqDistForecast(eqkRupForecast, evenlyGriddedRegion,
          minMag, maxMag, numMag);
      // NOW VIEW THE MAP
      ViewGriddedHypoMFD_Forecast viewRates = new ViewGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
      viewRates.makeMap(5.0);
      viewRates.makeFileInRELM_Format("testrelm.txt");
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

}