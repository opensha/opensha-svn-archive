package org.scec.sha.gui.controls;


import org.scec.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * <p>Title: PlotColorAndLineTypeSelectorControlPanelAPI</p>
 * <p>Description: Application using PlotColorAndLineTypeSelectorControlPanel
 * implements this interface, so if control panel needs to call a
 * method of application, it can do that without knowing which class method
 * needs to be called.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface PlotColorAndLineTypeSelectorControlPanelAPI {

  /**
   * plots the curves with defined color,line width and shape.
   * @param plotFeatures
   */
   public void drawGraph(PlotCurveCharacterstics[] plotFeatures);
}