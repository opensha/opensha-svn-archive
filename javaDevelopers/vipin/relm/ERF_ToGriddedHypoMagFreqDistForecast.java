package javaDevelopers.vipin.relm;

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
import org.opensha.exceptions.DataPoint2DException;
import org.opensha.sha.calc.ERF2GriddedSeisRatesCalc;
import java.util.ArrayList;

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

    ERF2GriddedSeisRatesCalc erfToGriddedSeisRatesCalc = new ERF2GriddedSeisRatesCalc();
    ArrayList incrementalMFD_List  = erfToGriddedSeisRatesCalc.calcMFD_ForGriddedRegion(minMag, maxMag, numMagBins,
        eqkRupForecast, region);
    // make HypoMagFreqDist for each location in the region
    magFreqDistForLocations = new HypoMagFreqDistAtLoc[this.getNumHypoLocs()];
    for(int i=0; i<magFreqDistForLocations.length; ++i ) {
      IncrementalMagFreqDist[] magFreqDistArray = new IncrementalMagFreqDist[1];
      magFreqDistArray[0] = (IncrementalMagFreqDist)incrementalMFD_List.get(i);
      magFreqDistForLocations[i] = new HypoMagFreqDistAtLoc(magFreqDistArray,griddedRegion.getGridLocation(i));
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
}