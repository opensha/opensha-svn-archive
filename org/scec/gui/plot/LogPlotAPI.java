package org.scec.gui.plot;

/**
 * <p>Title: LogPlotAPI </p>
 * <p>Description:This API needs to be implemented for each applet
 * which needs the log-log plotting capability
 * This is needed as it is used in PSHALogXYPlot to display message if
 * we cannot have log-log plot due to some negative or 0 data values
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 29 2002
 * @version 1.0
 */

public interface LogPlotAPI {

  /**
   * this function is called by PSHALogXYPlot to display message
   * if the log- log plot is not allowed due to invalid data values
   *
   * @param message
   */
  public void invalidLogPlot(String message);

  /**
   * sets the range for X-axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   */
  public void setXRange(double xMin,double xMax) ;

  /**
   * sets the range for Y-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   */
  public void setYRange(double yMin,double yMax) ;

}