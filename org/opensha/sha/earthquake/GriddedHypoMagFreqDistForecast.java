package org.opensha.sha.earthquake;

import org.opensha.data.*;
import org.opensha.param.ParameterList;

/**
 * <p>Title: GriddedHypoMagFreqForecast</p>
 *
 * <p>Description: This class </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GriddedHypoMagFreqDistForecast
    implements GriddedHypoMagFreqDistAtLocAPI {


  /**
   *
   */
  private TimeSpan timeSpan;
  private int numHypoLocation;
  private ParameterList adjustableParameters;
  private boolean parameterChangeFlag = true;

  public GriddedHypoMagFreqDistForecast() {
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
  public HypoMagFreqDistAtLoc getHypRatesAtLoc(int ithLocation) {
    return null;
  }

  /**
   * getNumHypoLocation
   *
   * @return int
   * @todo Implement this
   *   org.opensha.sha.earthquake.GriddedHypoMagFreqDistAtLocAPI method
   */
  public int getNumHypoLocation() {
    return 0;
  }

  public TimeSpan getTimeSpan() {
    return timeSpan;
  }

  public void setTimeSpan(TimeSpan timeSpan) {
    this.timeSpan = timeSpan;
  }

  public void setNumHypoLocation(int numHypoLocation) {
    this.numHypoLocation = numHypoLocation;
  }

  public void updateForecast(){
    if(parameterChangeFlag){

    }
  }

}
