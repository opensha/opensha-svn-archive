package javaDevelopers.vipin.relm;

import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import java.io.FileWriter;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.data.Location;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * <p>Title: WriteRELM_FileFromGriddedHypoMFD_Forecast.java </p>
 * <p>Description: This class accepts a Gridded Hypo Mag Freq Dist forecast
 * and writes out a file in RELM format . </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class WriteRELM_FileFromGriddedHypoMFD_Forecast {

  private GriddedHypoMagFreqDistForecast griddedHypoMFD;
  private final static double DEPTH_MIN = 0;
  private final static double DEPTH_MAX = 30;
  private final static int MASK = 1;
  public final static String BEGIN_FORECAST = "begin_forecast";
  public final static String END_FORECAST = "end_forecast";

  public final static String MODEL_NAME = "modelname=";
  public final static String VERSION_NAME = "version=";
  public final static String AUTHOR_NAME = "author=";
  public final static String ISSUE_DATE_NAME = "issue_date=";
  public final static String FORECAST_START_DATE_NAME = "forecast_start_date=";
  public final static String FORECAST_DURATION_NAME = "forecast_duration=";

  private String modelName, version, author;
  private int issueYear, issueMonth, issueDay, issueHour, issueMinute, issueSecond, issueMilliSecond;
  private int startYear, startMonth, startDay, startHour, startMinute, startSecond, startMilliSecond;
  private double duration;
  private String durationUnits;

  /**
   *
   * @param griddedHypoMagFreqDistForecast
   */
  public WriteRELM_FileFromGriddedHypoMFD_Forecast(GriddedHypoMagFreqDistForecast
      griddedHypoMagFreqDistForecast) {
    setGriddedHypoMagFreqDistForecast(griddedHypoMagFreqDistForecast);
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
  * set the model name, version and author
  * @param modelName
  */
 public void setModelVersionAuthor(String modelName, String version, String author) {
   this.modelName = modelName;
   this.version = version;
   this.author = author;
 }

 /**
  * Set the issue date
  *
  * @param issueYear
  * @param issueMonth
  * @param issueDay
  * @param issueHour
  * @param issueMinute
  * @param issueSecond
  * @param issueMilliSecond
  */
 public void setIssueDate(int issueYear, int issueMonth, int issueDay,
                          int issueHour, int issueMinute, int issueSecond,
                          int issueMilliSecond) {
   this.issueYear = issueYear;
   this.issueMonth = issueMonth;
   this.issueDay = issueDay;
   this.issueHour = issueHour;
   this.issueMinute = issueMinute;
   this.issueSecond = issueSecond;
   this.issueMilliSecond = issueMilliSecond;
 }

 /**
  * Set forecast start date
  *
  * @param startYear
  * @param startMonth
  * @param startDay
  * @param startHour
  * @param startMinute
  * @param startSecond
  * @param startMilliSecond
  */
 public void setForecastStartDate(int startYear, int startMonth, int startDay,
                          int startHour, int startMinute, int startSecond,
                          int startMilliSecond) {
    this.startYear = startYear;
    this.startMonth = startMonth;
    this.startDay = startDay;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.startSecond = startSecond;
    this.startMilliSecond = startMilliSecond;
  }


  /**
   * Se the forecast duration
   *
   * @param duration
   * @param durationUnits
   */
  public void setDuration(double duration, String durationUnits) {
    this.duration = duration;
    this.durationUnits = durationUnits;
  }




  /**
  * Writes the GriddedHypoMagFreqDistForecast into an output file. The format
  * of the output file is in RELM format
  */
 public void makeFileInRELM_Format(String outputFileName) {
   try {
     FileWriter fw = new FileWriter(outputFileName);

     // Write the header lines. following lines represent header lines in the output file
     /*modelname=John's model
     version=1.0
     author=John Doe
     issue_date=2005,8,1,0,0,0,0
     forecast_start_date=2005,9,1,0,0,0,0
     forecast_duration=5,years*/

     fw.write(MODEL_NAME+modelName+"\n");
     fw.write(VERSION_NAME+version+"\n");
     fw.write(AUTHOR_NAME+author+"\n");
     fw.write(ISSUE_DATE_NAME+issueYear+","+issueMonth+","+issueDay+","+
              issueHour+","+issueMinute+","+issueSecond+","+issueMilliSecond+"\n");
     fw.write(FORECAST_START_DATE_NAME+startYear+","+startMonth+","+startDay+","+
              startHour+","+startMinute+","+startSecond+","+startMilliSecond+"\n");
     fw.write(FORECAST_DURATION_NAME+duration+","+durationUnits+"\n");


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
         fw.write((float)lon1+"\t"+(float)lon2+"\t"+(float)lat1+"\t"+(float)lat2+"\t"+DEPTH_MIN+"\t"+
                  DEPTH_MAX+"\t"+mag1+"\t"+mag2+"\t"+incrementalMFD.getIncrRate(j)+"\t"+MASK+"\n");
       }
     }
     fw.write(END_FORECAST+"\n");
     fw.close();
   }catch(Exception e) {
     e.printStackTrace();
   }
 }
}