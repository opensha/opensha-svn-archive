package org.opensha.sha.gui.infoTools;

import java.util.ArrayList;
import org.opensha.data.estimate.*;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import java.awt.Color;
import org.opensha.data.function.DiscretizedFunc;

/**
 * <p>Title: EstimateViewer.java </p>
 * <p>Description: This class helps in viewing the various estimates </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta, Nitin Gupta
 * @version 1.0
 */

public class EstimateViewer implements GraphWindowAPI {
  private final static String X_AXIS_LABEL = "X Values";
  private final static String Y_AXIS_LABEL = "Probability";
  private Estimate estimate;
  private String xAxisLabel, yAxisLabel;
  private GraphWindow graphWindow;
  private final PlotCurveCharacterstics PDF_PLOT_CHAR_HISTOGRAM = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
      Color.RED, 2);
  private final PlotCurveCharacterstics PDF_PLOT_CHAR= new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
      Color.GREEN, 2);
  private final PlotCurveCharacterstics CDF_PLOT_CHAR = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
      Color.BLUE, 2);
  private final PlotCurveCharacterstics CDF_USING_FRACTILE_PLOT_CHAR = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
      Color.BLACK, 2);


  public EstimateViewer(Estimate estimate) {
    setEstimate(estimate);
    setXAxisLabel(X_AXIS_LABEL);
    setYAxisLabel(Y_AXIS_LABEL);
    graphWindow = new GraphWindow(this);
    //graphWindow.pack();
    graphWindow.show();
  }

  public void setXAxisLabel(String label) {
    this.xAxisLabel = label;
  }

  public void setYAxisLabel(String label) {
    this.yAxisLabel = label;
  }

  public void setEstimate(Estimate estimate) {
    this.estimate = estimate;
  }

  public boolean getXLog() { return false; }
  public boolean getYLog() { return false; }
  public String getXAxisLabel() { return this.xAxisLabel;}
  public String getYAxisLabel() { return this.yAxisLabel; }
  public boolean isCustomAxis() {return false; }

  public ArrayList getCurveFunctionList() {
   ArrayList list = new ArrayList();
   DiscretizedFunc func = estimate.getPDF_Test();
   list.add(func); // draw the histogram for PDF
   if(!(estimate instanceof DiscreteValueEstimate))
    list.add(func); // draw the continuous line with PDF if not a discrete distribution
   list.add(estimate.getCDF_Test());
   list.add(estimate.getCDF_TestUsingFractile());
   //list.add(estimate.getCDF());
   return list;
 }

  public ArrayList getPlottingFeatures() {
    ArrayList list = new ArrayList();
    list.add(PDF_PLOT_CHAR_HISTOGRAM);
    if(!(estimate instanceof DiscreteValueEstimate))
      list.add(PDF_PLOT_CHAR);
    list.add(CDF_PLOT_CHAR);
    list.add(CDF_USING_FRACTILE_PLOT_CHAR);
    return list;
  }

  /***
   * the methods getMinX(), getMaxX(), getMinY(), getMaxY() do not need to be
   * implemented as we go with default X values limit here.
   */

  public double getMinX() {

    throw new java.lang.UnsupportedOperationException("Method getMinX() not yet implemented.");
  }
  public double getMaxX() {

    throw new java.lang.UnsupportedOperationException("Method getMaxX() not yet implemented.");
  }
  public double getMinY() {
    throw new java.lang.UnsupportedOperationException("Method getMinY() not yet implemented.");
  }
  public double getMaxY() {

    throw new java.lang.UnsupportedOperationException("Method getMaxY() not yet implemented.");
  }

}