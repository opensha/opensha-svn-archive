package org.opensha.sha.gui.infoTools;

import java.util.*;
import java.awt.Color;

import org.opensha.commons.data.function.WeightedFuncList;


/**
 * <p>Title: WeightedFuncListforPlotting</p>
 * <p>Description: This class creates the plotting capabilities for Weighted function
 * as required by our wrapper to Jfreechart.</p>
 * @author : Ned Field, Nitin Gupta
 * @version 1.0
 */

public class WeightedFuncListforPlotting extends WeightedFuncList{


  private boolean individualCurvesToPlot = true;
  private boolean fractilesToPlot = true;
  private boolean meantoPlot = true;




  /**
   * Sets boolean based on if application needs to plot individual curves
   * @param toPlot
   */
  public void setIndividualCurvesToPlot(boolean toPlot){
    individualCurvesToPlot = toPlot;
  }


  /**
   * Sets boolean based on if application needs to plot fractiles
   * @param toPlot
   */
  public void setFractilesToPlot(boolean toPlot){
    fractilesToPlot = toPlot;
  }

  /**
   * Sets boolean based on if application needs to plot mean curve
   * @param toPlot
   */
  public void setMeanToPlot(boolean toPlot){
    meantoPlot = toPlot;
  }

  /**
   *
   * @returns true if individual plots need to be plotted , else return false
   */
  public boolean areIndividualCurvesToPlot(){
    return individualCurvesToPlot;
  }

  /**
   *
   * @returns true if fractile plots need to be plotted, else return false
   */
  public boolean areFractilesToPlot(){
    return fractilesToPlot;
  }

  /**
   *
   * @returns true if mean curve needs to be plotted, else return false.
   */
  public boolean isMeanToPlot(){
    return meantoPlot;
  }



}
