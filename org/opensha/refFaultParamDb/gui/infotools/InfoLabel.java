package org.opensha.refFaultParamDb.gui.infotools;
import javax.swing.JLabel;
import java.awt.Color;
import org.opensha.data.estimate.*;
import org.opensha.data.function.DiscretizedFunc;

/**
 * <p>Title: InfoLabel.java </p>
 * <p>Description: This class constructs the JLabel to view the information about
 * a parameter or an estimate</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class InfoLabel extends JLabel {
  // color for JLabels
  private Color labelColor = new Color( 80, 80, 133 );
  private final static String ESTIMATE_TYPE = "Estimate Type";

  /**
   * Make  a JLabel for a param name-value pair
   * @param paramName
   * @param paramValue
   */
  public InfoLabel(String paramName, String paramValue) {
    this.setForeground(labelColor);
    String label  = "<html><b>"+paramName+":</b>"+paramValue+"</html>";
    setText(label);
  }

  /**
   * Make a JLabel to provide info about a estimate
   *
   * @param estimate
   */
  public InfoLabel(Estimate estimate) {
    this.setForeground(labelColor);
    String text;
    if(estimate instanceof NormalEstimate)
      text = getTextForNormalEstimate((NormalEstimate)estimate);
    else if(estimate instanceof LogNormalEstimate)
      text = getTextForLogNormalEstimate((LogNormalEstimate)estimate);
    else if(estimate instanceof DiscretizedFuncEstimate)
      text = getTextForDiscretizedFuncEstimate((DiscretizedFuncEstimate)estimate);
  }

  /**
   * Information for normal estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForNormalEstimate(NormalEstimate estimate) {
    return "<html><b>"+ESTIMATE_TYPE+":</b>"+estimate.getName()+"<br>"+
        "<b>Mean:</b>"+estimate.getMean()+"<br>"+
        "<b>StdDev:</b>"+estimate.getStdDev();
  }

  /**
   * Information for lognormal estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForLogNormalEstimate(LogNormalEstimate estimate) {
    return "<html><b>"+ESTIMATE_TYPE+":</b>"+estimate.getName()+"<br>"+
        "<b>Linear Median:</b>"+estimate.getMedian()+"<br>"+
        "<b>StdDev:</b>"+estimate.getStdDev();
  }

  /**
   * Information for Discretized func estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForDiscretizedFuncEstimate(DiscretizedFuncEstimate estimate) {
    String text =  "<html><b>"+ESTIMATE_TYPE+":</b>"+estimate.getName()+"<br>"+
        "<b>X  Y</b> <br>";
    DiscretizedFunc func = estimate.getValues();
    for(int i=0; i<func.getNum(); ++i)
        text+=  func.getX(i)+" "+func.getY(i)+"<br>";
    return text;
  }



}