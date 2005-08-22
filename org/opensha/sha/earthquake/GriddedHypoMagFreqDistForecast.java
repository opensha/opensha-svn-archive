package org.opensha.sha.earthquake;

import org.opensha.data.*;
import org.opensha.param.ParameterList;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;

/**
 * <p>Title: GriddedHypoMagFreqForecast</p>
 *
 * <p>Description: This is a point-poission ERF. This does not provide any info.
 * about the rupture associated with it. It can wrap any ERF.
 * This provide MagFreqDist for a given hypocenter location.
 *  </p>
 *<p>
 * Note : The rate provided by this forecast are always yearly rate.
 *</p>
 * @author Nitin Gupta , Edward (Ned) Field, Vipin Gupta
 * @version 1.0
 */
public class GriddedHypoMagFreqDistForecast
    implements GriddedHypoMagFreqDistAtLocAPI, ParameterChangeListener {



  // Timespan for the given forecast
  private TimeSpan timeSpan;
  //number of hypocenter location.
  private int numHypoLocation;
  //Adjustable parameters fro the given forecast model
  private ParameterList adjustableParameters;
  //Only update the forecast if parameters have been changed.
  private boolean parameterChangeFlag = true;

  //Only allows user to edit the timespan if this is true.
  private boolean editable = true;

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


  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in.
   *
   *  This sets the flag to indicate that the sources need to be updated
   *
   * @param  event
   */
  public void parameterChange(ParameterChangeEvent event) {
    if(editable)
      parameterChangeFlag = true;
  }

  public TimeSpan getTimeSpan() {
    return timeSpan;
  }

  public void setTimeSpan(TimeSpan timeSpan) {
    if(editable)
      this.timeSpan = timeSpan;
  }

  public void setNumHypoLocation(int numHypoLocation) {
    this.numHypoLocation = numHypoLocation;
  }

  public void updateForecast(){
    if(parameterChangeFlag){

    }
  }

  /**
   * This will unable the user to change Timespan and parameters.
   */
  public void makeParameterUneditable(){
    this.editable =false;
  }

}
