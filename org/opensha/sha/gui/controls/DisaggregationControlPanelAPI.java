package org.opensha.sha.gui.controls;

/**
 * <p>Title: DisaggregationControlPanelAPI </p>
 * <p>Description:  Any applet which uses the DisaggregationControlPanel needs
 * to implement this API</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface DisaggregationControlPanelAPI  {

  /**
   * This function to specify whether disaggregation is selected or not
   * @param isSelected : True if disaggregation is selected , else false
   */
  public void setDisaggregationSelected(boolean isSelected);


}
