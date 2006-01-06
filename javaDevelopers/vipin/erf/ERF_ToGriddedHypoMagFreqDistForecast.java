package javaDevelopers.vipin.erf;

import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import java.util.ListIterator;
import org.opensha.data.Location;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * <p>Title: ERF_ToGriddedHypoMagFreqDistForecast.java </p>
 * <p>Description: this class accepts any ERF and converts into GriddedHypoMagFreDistForecast.
 * This class can be considered as implementation of GriddedHypoMagFreqDistForecastWrappedERF
 * but we have to confirm with Ned regarding this. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ERF_ToGriddedHypoMagFreqDistForecast  extends GriddedHypoMagFreqDistForecast {
  private EqkRupForecast eqkRupForecast;
  private HypoMagFreqDistAtLoc magFreqDistForLocations[];

  /**
   * Accepts a forecast and a region. It calculates Magnitude-Freq distribution for
   * each location within the region.
   *
   * @param forecast - EqkRupForecast which need to be converted to GriddedHypoMagFreqDistForecast
   * @param griddedRegion - EvenlyGriddedRegion for calculating magnitude frequency distribution
   * @param minMag - Center of first magnitude bin to make IncrementalMagFreqDist.
   * @param maxMag - Center of last magnitude bin to make IncrementalMagFreqDist
   * @param numMags - Total number of  magnitude bins in IncrementalMagFreqDist
   *
   *
   */
  public ERF_ToGriddedHypoMagFreqDistForecast(EqkRupForecast eqkRupForecast,
                                              EvenlyGriddedGeographicRegionAPI griddedRegion,
                                              double minMag,
                                              double maxMag,
                                              int numMagBins) {
    this.eqkRupForecast = eqkRupForecast;
    this.region = griddedRegion;

    // make HypoMagFreqDist for each location in the region
    magFreqDistForLocations = new HypoMagFreqDistAtLoc[this.getNumHypoLocs()];
    for(int i=0; i<magFreqDistForLocations.length; ++i ) {
      IncrementalMagFreqDist magFreqDist = new IncrementalMagFreqDist(minMag, maxMag, numMagBins);
      magFreqDist.setTolerance(magFreqDist.getDelta()/2);
      IncrementalMagFreqDist []magFreqDistArray = new IncrementalMagFreqDist[1];
      magFreqDistArray[0] = magFreqDist;
      magFreqDistForLocations[i] = new HypoMagFreqDistAtLoc(magFreqDistArray,griddedRegion.getGridLocation(i));
    }

    // calculate mag-freq dist for each location within the provided region
    calculateHypoMagFreDistForEachLocation();
  }


  /*
   * computes the Mag-Rate distribution for each location within the provided region.
   */
  private void calculateHypoMagFreDistForEachLocation() {
    int numLocations = region.getNumGridLocs();
    // get all sources with this ERF
    int numSources = eqkRupForecast.getNumSources();

    //Going over each source within ERF
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();
      //going over all the ruptures wiithin the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);
        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        long numPts = rupSurface.size();

        //getting the rate at each Point on the rupture( calculated by first
        //getting the rate of the rupture and then dividing by number of points
        //on that rupture.
        double ptRate = getRupturePtRate(eqkRupForecast, rupture, numPts);

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();

        //looping over all the rupture pt location and finding the nearest location
        //to them in the Geographical Region.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          //discard the pt location on the rupture if outside the region polygon
          int locIndex = 0;
          //returns -1 if location not in the region
          locIndex = region.getNearestLocationIndex(ptLoc);
          //continue if location not in the region
          if (locIndex < 0) continue;
          double rupMag = rupture.getMag();
          IncrementalMagFreqDist incrMagFreqDist = magFreqDistForLocations[
              locIndex].getMagFreqDist()[0];
          double delta = incrMagFreqDist.getDelta();
          // check if rupture magnitude is within range
          if ((incrMagFreqDist.getMinX()-delta/2)<=rupMag && rupMag<=(incrMagFreqDist.getMaxX()+delta/2)) {
            int index = incrMagFreqDist.getXIndex(rupMag);
            incrMagFreqDist.set(index, incrMagFreqDist.getY(index)+ptRate);
          }
        }
      }
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


  /*
   * Computing the Rate for each location on the rupture
   * @param eqkRupForecast EqkRupForecastAPI
   * @param rupture ProbEqkRupture
   * @param numPts long
   * @return double
   */
  private double getRupturePtRate(EqkRupForecast eqkRupForecast,
                                  ProbEqkRupture rupture, long numPts) {
    return ( -Math.log(1 - rupture.getProbability()) /
             eqkRupForecast.getTimeSpan().getDuration()) / numPts;
 }

}