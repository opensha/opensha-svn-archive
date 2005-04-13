package org.opensha.sha.gui.controls;

/**
 * <p>Title:ERF_EpistemicListControlPanelAPI </p>
 * <p>Description: Any applet which uses the ERF_EpistemicListControlPanel needs
 * to implement this API</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface ERF_EpistemicListControlPanelAPI {


  /**
   * This function sets whether all curves are to drawn or only fractiles are to drawn
   * @param drawAllCurves :True if all curves are to be drawn else false
   */
  public void setPlotAllCurves(boolean drawAllCurves);


  /**
   * This function sets the percentils option chosen by the user.
   * User can choose "No Fractiles", "5th, 50th and 95th Fractile" or
   * "Plot Fractile"
   *
   * @param fractileOption : Oprion selected by the user. It can be set by
   * various constant String values in ERF_EpistemicListControlPanel
   */
  public void setFractileOption(String fractileOption);


  /**
   * This function is needed to tell the applet whether avg is selected or not
   *
   * @param isAvgSelected : true if avg is selected else false
   */
  public void setAverageSelected(boolean isAvgSelected);


}
