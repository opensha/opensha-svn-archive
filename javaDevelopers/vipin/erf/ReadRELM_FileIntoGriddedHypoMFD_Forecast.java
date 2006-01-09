package javaDevelopers.vipin.erf;

import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import org.opensha.data.Location;
import org.opensha.exceptions.DataPoint2DException;

/**
 * <p>Title: ReadRELM_FileIntoGriddedHypoMFD_Forecast.java </p>
 * <p>Description: It reads the file given in RELM format and makes a GriddedHypoMagFreqDistForecast
 * from that file. Once we have this object, we can use it as any other forecast
 * or view it in a map.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ReadRELM_FileIntoGriddedHypoMFD_Forecast extends GriddedHypoMagFreqDistForecast{
  private HypoMagFreqDistAtLoc magFreqDistForLocations[];
  private String inputFileName;

  /**
  * This function reads the input file and converts it into GriddedHypoMagFreqDistForecast.
  * @param inputFileName
  * @param griddedRegion
  * @param minMag
  * @param maxMag
  * @param numMagBins
  * @return
  */
 public ReadRELM_FileIntoGriddedHypoMFD_Forecast(String inputFileName,
                                                 EvenlyGriddedGeographicRegionAPI griddedRegion,
                                                 double minMag,
                                                 double maxMag,
                                                 int numMagBins) {
    this.region = griddedRegion;
    this.inputFileName = inputFileName;
   // make HypoMagFreqDist for each location in the region
   magFreqDistForLocations = new HypoMagFreqDistAtLoc[this.getNumHypoLocs()];
   for(int i=0; i<magFreqDistForLocations.length; ++i ) {
     IncrementalMagFreqDist magFreqDist = new IncrementalMagFreqDist(minMag, maxMag, numMagBins);
     magFreqDist.setTolerance(magFreqDist.getDelta()/2);
     IncrementalMagFreqDist []magFreqDistArray = new IncrementalMagFreqDist[1];
     magFreqDistArray[0] = magFreqDist;
     magFreqDistForLocations[i] = new HypoMagFreqDistAtLoc(magFreqDistArray,griddedRegion.getGridLocation(i));
   }
   // read the file and calculate HypoMagFreqDist at each location
   calculateHypoMagFreqDistForEachLocation();
 }

 /*
  * computes the Mag-Rate distribution for each location within the provided region.
  */
  private void calculateHypoMagFreqDistForEachLocation() {
    try {
      FileReader fr = new FileReader(this.inputFileName);
      BufferedReader br = new BufferedReader(fr);
      String line = br.readLine();
      // go upto the line which says "begin_forecast"
      while(line!=null && !line.equalsIgnoreCase(ViewGriddedHypoMFD_Forecast.BEGIN_FORECAST)) {
        line = br.readLine();
      }
      // if it end of file, return
      if(line==null) return;
      // start reading forecast
      line = br.readLine();
      // read forecast until end of file or until "end_forecast" is encountered
      double lat1, lat2, lat;
      double lon1, lon2, lon;
      double mag1, mag2, mag;
      double rate;
      int locIndex;
      while(line!=null && !line.equalsIgnoreCase(ViewGriddedHypoMFD_Forecast.END_FORECAST)) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        lon1= Double.parseDouble(tokenizer.nextToken());
        lon2 =  Double.parseDouble(tokenizer.nextToken());
        lat1 = Double.parseDouble(tokenizer.nextToken());
        lat2 = Double.parseDouble(tokenizer.nextToken());
        tokenizer.nextToken(); // min depth
        tokenizer.nextToken(); // max depth
        mag1= Double.parseDouble(tokenizer.nextToken());
        mag2 =  Double.parseDouble(tokenizer.nextToken());
        rate = Double.parseDouble(tokenizer.nextToken()); // rate
        // calculate the midpoint of lon bin
        lon = (lon1+lon2)/2;
        // midpoint of the lat bin
        lat = (lat1+lat2)/2;
        // calculate midpoint of mag bin
        mag = (mag1+mag2)/2;
        locIndex = this.region.getNearestLocationIndex(new Location(lat,lon));
        //continue if location not in the region
        if (locIndex >= 0)  {
          IncrementalMagFreqDist incrMagFreqDist = magFreqDistForLocations[
              locIndex].getMagFreqDist()[0];
          try {
            int index = incrMagFreqDist.getXIndex(mag);
            incrMagFreqDist.set(index, incrMagFreqDist.getY(index) + rate);
          }
          catch (DataPoint2DException dataPointException) {
            // do not do anything if this mag is not allowed
          }
        }
        line = br.readLine();
      }
      br.close();
      fr.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

 /**
  * gets the Hypocenter Mag.
  *
  * @param ithLocation int : Index of the location in the region
  * @return HypoMagFreqDistAtLoc Object using which user can retrieve the
  *   Magnitude Frequency Distribution.
  * @todo Implement this
  *   org.opensha.sha.earthquake.GriddedHypoMagFreqDistAtLocAPI method
  */
 public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation) {
   return magFreqDistForLocations[ithLocation];
 }


  public static void main(String[] args) {
    //ReadRELM_FileIntoGriddedHypoMFD_Forecast readRELM_FileIntoGriddedHypoMFD_Forecast1 = new ReadRELM_FileIntoGriddedHypoMFD_Forecast();
  }

}