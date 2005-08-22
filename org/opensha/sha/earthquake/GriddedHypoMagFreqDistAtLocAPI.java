package org.opensha.sha.earthquake;

/**
 * <p>Title: GriddedHypoMagFreqDistAtLocAPI </p>
 *
 * <p>Description: </p>
 *
 * Note : Additiional info needs to be added like binning of Lat, Lon and Depth).
 * @author Nitin Gupta, Vipin Gupta and Edward (Ned) Field
 *
 * @version 1.0
 */
public interface GriddedHypoMagFreqDistAtLocAPI {




  /**
   * gets the Hypocenter Mag. Freq. Dist. for ith location.
   * User will have to call this function for each location in the region
   * to get the Mag Freq. Dist. at each location.
   * @param ithLocation int : Index of the location in the region
   * @return HypoMagFreqDistAtLoc Object using which user can retrieve the
   * Magnitude Frequency Distribution.
   *
   * Note : This always gives out yearly Rate.
   */
  public HypoMagFreqDistAtLoc getHypRatesAtLoc(int ithLocation);

  public int getNumHypoLocation();

}
