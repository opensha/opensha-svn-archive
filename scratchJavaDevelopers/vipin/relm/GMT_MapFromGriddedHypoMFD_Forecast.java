package scratchJavaDevelopers.vipin.relm;

import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.data.XYZ_DataSetAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.EvenlyGriddedRELM_TestingRegion;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import java.io.FileWriter;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.data.Location;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.exceptions.DataPoint2DException;

import java.util.ArrayList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast;
import org.opensha.param.ParameterList;
import java.util.Iterator;


/**
 * <p>Title: GMT_MapFromGriddedHypoMFD_Forecast.java </p>
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
  public void makeMap(double mag, String dirName) {
    XYZ_DataSetAPI xyzData = getRELM_XYZ_DataAboveMag(mag);
    EvenlyGriddedGeographicRegionAPI region  = griddedHypoMFD.getEvenlyGriddedGeographicRegion();
    // make GMT_MapGenerator to make the map
    GMT_MapGenerator mapGenerator = new GMT_MapGenerator();

    // TODO :   SET VARIOUS GMT PARAMETERS TO MAKE MAP
    mapGenerator.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME, new Double(region.getMinGridLat()));
    mapGenerator.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME, new Double(region.getMaxGridLat()));
    mapGenerator.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME, new Double(region.getMinGridLon()));
    mapGenerator.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME, new Double(region.getMaxGridLon()));
    mapGenerator.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, new Double(region.getGridSpacing()));
    mapGenerator.setParameter(GMT_MapGenerator.LOG_PLOT_NAME, new Boolean(true));
    mapGenerator.setParameter(GMT_MapGenerator.COAST_PARAM_NAME, GMT_MapGenerator.COAST_DRAW);
    mapGenerator.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
    mapGenerator.setParameter(GMT_MapGenerator.CPT_FILE_PARAM_NAME, GMT_MapGenerator.CPT_FILE_MAX_SPECTRUM);

    //manual color scale
    mapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME, GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
    mapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, new Double(-5.0));
    mapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, new Double(-1.0));

    try {
      String metadata = "Rate Above magnitude " + mag;
      String imageFileName = mapGenerator.makeMapUsingServlet(xyzData, "Rates",
                                         metadata, dirName);
      new ImageViewerWindow(imageFileName, metadata, true);

    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * For each location, return the rate above the specified magnitude
   *
   * It returns the XYZ data above a specific magnitude
   * It returns a list of XYZ values where each X,Y,Z are lat,lon and rate respectively.
   * The rate is the rate above the given magnitude
   *
   * @return
   */
  public  XYZ_DataSetAPI getRELM_XYZ_DataAboveMag(double mag) {
    ArbDiscretizedXYZ_DataSet xyzDataSet = new ArbDiscretizedXYZ_DataSet();
    ArrayList xVals = new ArrayList();
    ArrayList yVals = new ArrayList();
    ArrayList zVals = new ArrayList();
    // iterate over all locations.
    double rateAboveMag=0.0, totalIncrRate;
    int numLocs = griddedHypoMFD.getNumHypoLocs();
    for(int i=0; i<numLocs; ++i) {
      HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc = griddedHypoMFD.getHypoMagFreqDistAtLoc(i);
      Location loc = hypoMagFreqDistAtLoc.getLocation();
      xVals.add(new Double(loc.getLatitude()-0.05)); 
      yVals.add(new Double(loc.getLongitude()-0.05));
      IncrementalMagFreqDist magFreqDist = hypoMagFreqDistAtLoc.getMagFreqDist()[0];
      // rate above magnitude
      try {
        rateAboveMag = magFreqDist.getCumRate(mag);
      }catch(DataPoint2DException dataPointException) {
        // if magnitude is less than least magnitude in this Mag-Freq dist
        if(mag<magFreqDist.getMinX()) rateAboveMag = magFreqDist.getTotalIncrRate();
        // if this mag is above highest mag in this MagFreqDist
        else if(mag>magFreqDist.getMaxX()) rateAboveMag = 0.0;
        else dataPointException.printStackTrace();
      }
      zVals.add(new Double(rateAboveMag));
    }
    xyzDataSet.setXYZ_DataSet(xVals, yVals, zVals);
    return xyzDataSet;
  }

  /**
   * This test will make Frankel02 ERF, convert it into GriddedHypoMagFreqDistForecast and
   * then view it.
   * This function was tested for Frankel02 ERF.
   * Following testing procedure was applied(Region used was RELM Gridded region and
   *  min mag=5, max Mag=9, Num mags=41):
   * 1. Choose an arbitrary location say 31.5, -117.2
   * 2. Make Frankel02 ERF with Background only sources
   * 3. Modify Frankel02 ERF for testing purposes to use ONLY CAmapC_OpenSHA input file
   * 4. Now print the Magnitude Frequency distribution in Frankel02 ERF for that location
   * 5. Using the same ERF settings, get the Magnitude Frequency distribution using
   * this function and it should be same as we printed out in ERF.
   * 6. In another test, we printed out cum dist above Mag 5.0 for All locations.
   * The cum dist from Frankel02 ERF and from MFD retured from this function should
   * be same.
   * 7. Another test done was to make 3 files: One with only background, another with
   * only foregound and another with both. The rates in the "both file" was sum of
   * background and foreground
   *
   * @param args
   */
  public static void main(String[] args) {
   /* EqkRupForecast eqkRupForecast = new Frankel02_AdjustableEqkRupForecast();
    // include background sources as point sources
    eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_NAME,
                                new Double(10.0));
    eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME,
                                Frankel02_AdjustableEqkRupForecast.BACK_SEIS_INCLUDE);
    eqkRupForecast.setParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME,
                               Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_POINT);
    eqkRupForecast.getTimeSpan().setDuration(1.0);
    */
   /*EqkRupForecast eqkRupForecast = new WGCEP_UCERF1_EqkRupForecast();
   // include background sources as point sources
   eqkRupForecast.setParameter(WGCEP_UCERF1_EqkRupForecast.RUP_OFFSET_PARAM_NAME,
                               new Double(10.0));
   eqkRupForecast.setParameter(WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_NAME,
                               Frankel02_AdjustableEqkRupForecast.BACK_SEIS_INCLUDE);
   eqkRupForecast.setParameter(WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_RUP_NAME,
                               Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_POINT);
   eqkRupForecast.setParameter(WGCEP_UCERF1_EqkRupForecast.FAULT_MODEL_NAME,
                               Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_STIRLING);
   eqkRupForecast.setParameter(WGCEP_UCERF1_EqkRupForecast.TIME_DEPENDENT_PARAM_NAME,
                               new Boolean(true));
   eqkRupForecast.getTimeSpan().setDuration(5.0);

   eqkRupForecast.updateForecast();
   */try {
     // region to view the rates
     EvenlyGriddedRELM_TestingRegion evenlyGriddedRegion  = new EvenlyGriddedRELM_TestingRegion();
     // min mag, maxMag, These are Centers of first and last bin
     double minMag=5.0, maxMag=9.00;
     int numMag = 41; // number of Mag bins
     // make GriddedHypoMFD Forecast from the EqkRupForecast
     //GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
      //  new ERF_ToGriddedHypoMagFreqDistForecast(eqkRupForecast, evenlyGriddedRegion,
       // minMag, maxMag, numMag);

     /* Following names can be used for RELM files:
      * ward.geodetic81.forecast
      * helmstetter_et_al.hkj.forecast
      * helmstetter_et_al.hkj.aftershock.forecast
      * ward.simulation.forecast
      * ward.combo81.forecast
      * ward.geologic81.forecast
      * ward.geodetic85.forecast
      * ward.seismic81.forecast
      * wiemer_schorlemmer.alm.forecast
      * bird_liu.neokiname.forecast
      * ebel.mainshock.forecast
      * ebel.aftershock.forecast
      * holliday.pi.forecast
      * UCERF1.0_AnnualRates.txt
      * NSHMP2002_AnnualRates.txt
      * shen_et_al.geodetic.forecast
      * shen_et_al.geodetic.aftershock
      */
     ArrayList<String> relmFileNames = new ArrayList<String>();
     relmFileNames.add("ward.geodetic81.forecast");
     relmFileNames.add("helmstetter_et_al.hkj.forecast");
     relmFileNames.add("helmstetter_et_al.hkj.aftershock.forecast");
     relmFileNames.add("ward.simulation.forecast");
     relmFileNames.add("ward.combo81.forecast");
     relmFileNames.add("ward.geologic81.forecast");
     relmFileNames.add("ward.seismic81.forecast");
     relmFileNames.add("wiemer_schorlemmer.alm.forecast");
     relmFileNames.add("bird_liu.neokiname.forecast");
     relmFileNames.add("ebel.mainshock.forecast");
     relmFileNames.add("ebel.aftershock.forecast");
     relmFileNames.add("holliday.pi.forecast");
     relmFileNames.add("UCERF1.forecast");
     relmFileNames.add("NSHMP2002.forecast");
     relmFileNames.add("shen_et_al.geodetic.forecast");
     relmFileNames.add("shen_et_al.geodetic.aftershock");
     relmFileNames.add("kagan_et_al.mainshock.forecast");
     relmFileNames.add("kagan_et_al.aftershock.forecast");
   
    /* for(int i=0; i<relmFileNames.size(); ++i) {
     
    	 GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
    		 new ReadRELM_FileIntoGriddedHypoMFD_Forecast(relmFileNames.get(i), evenlyGriddedRegion,
    				 minMag, maxMag, numMag);
      
    	 // Make GMT map of rates
    	 GMT_MapFromGriddedHypoMFD_Forecast viewRates = new GMT_MapFromGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
    	 viewRates.makeMap(5.0,relmFileNames.get(i));
     } */
     GriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
		 new ReadRELM_FileIntoGriddedHypoMFD_Forecast("ebel.mainshock.forecast", evenlyGriddedRegion,
				 minMag, maxMag, numMag);
  
	 // Make GMT map of rates
	 GMT_MapFromGriddedHypoMFD_Forecast viewRates = new GMT_MapFromGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
	 viewRates.makeMap(5.0,"ebel.mainshock.forecast");
     
     /*XYZ_DataSetAPI xyzData = griddedHypoMagFeqDistForecast.getXYZ_DataAboveMag(5.0);
      FileWriter fw = new FileWriter("FG_GriddedHypoFrankel02.txt");
      ArrayList xVals = xyzData.getX_DataSet();
      ArrayList yVals = xyzData.getY_DataSet();
      ArrayList zVals = xyzData.getZ_DataSet();
      for(int i=0; i<xVals.size(); ++i) {
        fw.write(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
      }
      fw.close();*/

     // write into RELM formatted file
     /*WriteRELM_FileFromGriddedHypoMFD_Forecast writeRELM_File = new WriteRELM_FileFromGriddedHypoMFD_Forecast(griddedHypoMagFeqDistForecast);
     String version="1.0 (";
     // write the adjustable params
     ParameterList paramList = eqkRupForecast.getAdjustableParameterList();
     Iterator it= paramList.getParameterNamesIterator();
     while(it.hasNext()) {
       String paramName=(String)it.next();
        version=version+paramName+"="+paramList.getValue(paramName).toString()+",";
      }
      // write the timespan adjustable params
      paramList  = eqkRupForecast.getTimeSpan().getAdjustableParams();
       it= paramList.getParameterNamesIterator();
      while(it.hasNext()) {
        String paramName=(String)it.next();
        version=version+paramName+"="+paramList.getValue(paramName).toString()+",";
      }
      version=version+")";
      //for(int i=0; i<paramList
      writeRELM_File.setModelVersionAuthor("UCERF1.0", version, "Ned Field");
      writeRELM_File.setIssueDate(2006, 0,0,0,0,0,0);
      writeRELM_File.setForecastStartDate(2006, 0,0,0,0,0,0);
      writeRELM_File.setDuration(5, "years");
      writeRELM_File.makeFileInRELM_Format("UCERF1.0_AnnualRates.txt");
*/
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

}