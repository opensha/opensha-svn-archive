package org.opensha.refFaultParamDb.gui.infotools;
import javax.swing.JLabel;
import java.awt.Color;
import org.opensha.data.estimate.*;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.refFaultParamDb.data.*;
import java.util.GregorianCalendar;
import java.util.ArrayList;

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
  private final static String TIME = "Time";
  public final static String NOT_AVAILABLE = "Not Available";

  /**
   * default constructor
   */
  public InfoLabel() {
     this.setForeground(labelColor);
  }

  /**
   * Make  a JLabel for a param name-value pair
   * @param paramName
   * @param paramValue
   */
  public InfoLabel(String paramName, String paramValue) {
    this();
    setTextAsHTML(paramName, paramValue);
  }

  /**
  *  JLabel text for a param name-value pair
  * @param paramName
  * @param paramValue
  */

  public void setTextAsHTML(String paramName, String paramValue) {
    if(paramValue==null) paramValue = NOT_AVAILABLE;
    String label  = "<html><b>"+paramName+":&nbsp;</b>"+paramValue+"</html>";
    setText(label);
  }

  /**
   * Make  a JLabel for a String
   *
   * @param paramName
   * @param paramValue
   */
  public InfoLabel(String value) {
    this();
    setTextAsHTML(value);
  }

  /**
   *  JLabel text for a String
   *
   * @param paramName
   * @param paramValue
   */

  public void setTextAsHTML(String value) {
    if(value==null) value = NOT_AVAILABLE;
    String label  = "<html>"+value+"</html>";
    setText(label);
  }


  /**
   * Make  a JLabel for a Arraylist of Strings
   *
   * @param paramName
   * @param paramValue
   */
  public InfoLabel(ArrayList values) {
    this();
    setTextAsHTML(values);
  }

  /**
   * JLabel text for a Arraylist of Strings
   *
   * @param paramName
   * @param paramValue
   */
  public void setTextAsHTML(ArrayList values) {
    String label  = "<html>";
    if(values==null || values.size()==0) label += NOT_AVAILABLE;
    else {
      for (int i = 0; i < values.size(); ++i)
        label += values.get(i).toString() + "<br>";
    }
    label = label+"</html>";
    setText(label);
  }



  /**
   * Make a JLabel to provide info about a estimate
   *
   * @param estimate
   */
  public InfoLabel(Estimate estimate) {
    this();
    setTextAsHTML(estimate);
  }

  /**
   * JLabel text to provide info about a estimate
   *
   * @param estimate
   */

  public void setTextAsHTML(Estimate estimate) {
    String text;
    if(estimate==null) text = "<html>"+NOT_AVAILABLE+"</html>";
    else  text="<html>"+getTextForEstimate(estimate)+"</html>";
    setText(text);
  }

  /**
   * Get Text For estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForEstimate(Estimate estimate) {
    String text = "";
    if(estimate instanceof NormalEstimate)
      text = getTextForNormalEstimate((NormalEstimate)estimate);
    else if(estimate instanceof LogNormalEstimate)
      text = getTextForLogNormalEstimate((LogNormalEstimate)estimate);
    else if(estimate instanceof DiscretizedFuncEstimate)
      text = getTextForDiscretizedFuncEstimate((DiscretizedFuncEstimate)estimate);
    return text;
  }

  /**
   * Make a JLabel to provide info about Time (Time can be exact time or
   * time estimate)
   *
   * @param estimate
   */
  public InfoLabel(TimeAPI time) {
    this.setForeground(labelColor);
    setTextAsHTML(time);
  }

  /**
   *
   * @param time
   */
  public void setTextAsHTML(TimeAPI time) {
    String text="";
    if(time == null ) // if time is not available
      text = "<html>"+NOT_AVAILABLE+"</html>";
    else if(time instanceof TimeEstimate)
      text = getTextForTimeEstimate((TimeEstimate)time);
    else if(time instanceof ExactTime)
      text = getTextForExactTime((ExactTime)time);
    setText(text);
  }

  /**
   * Get the Information to be displayed in case of exact estimate
   *
   * @param exactTime
   * @return
   */
  private String getTextForExactTime(ExactTime exactTime) {
    return "<html><b>"+TIME+":&nbsp;</b>Exact Time"+"<br>"+
        "<b>Year:&nbsp;</b>"+exactTime.getYear()+exactTime.getEra()+"<br>"+
        "<b>Month:&nbsp;</b>"+exactTime.getMonth()+"<br>"+
        "<b>Date:&nbsp;</b>"+exactTime.getDay()+"<br>"+
        "<b>Hour:&nbsp;</b>"+exactTime.getHour()+"<br>"+
        "<b>Second:&nbsp;</b>"+exactTime.getSecond()+"<br></html>";
  }

  /**
   * get the information to be displayed in case of time estimate
   *
   * @param timeEstimate
   * @return
   */
  private String getTextForTimeEstimate(TimeEstimate timeEstimate) {
    // whether user provided ka values estimate/ calendar year estimates
    boolean isKaSelected = timeEstimate.isKaSelected();
    String text="";
    if(isKaSelected) { // if KA is selected
      text = "<html><b>"+TIME+":&nbsp;</b>Time Estimate"+"<br>"+
          "<html><b>Units:&nbsp;</b>ka"+"<br>"+
          "<b>Zero Year:&nbsp;</b>"+timeEstimate.getZeroYear()+"AD<br>"+
          getTextForEstimate(timeEstimate.getEstimate())+"</html>";
    } else { // if calendar year is selected for estimate
      text = "<html><b>"+TIME+":&nbsp;</b>Time Estimate"+"<br>"+
       "<html><b>Units:&nbsp;</b>Calendar Years"+"<br>"+
       getTextForEstimate(timeEstimate.getEstimate())+"</html>";
    }
    return text;
  }


  /**
   * Information for normal estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForNormalEstimate(NormalEstimate estimate) {
    return "<b>"+ESTIMATE_TYPE+":&nbsp;</b>"+estimate.getName()+"<br>"+
        "<b>Mean:&nbsp;</b>"+estimate.getMean()+"<br>"+
        "<b>StdDev:&nbsp;</b>"+estimate.getStdDev();
  }

  /**
   * Information for lognormal estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForLogNormalEstimate(LogNormalEstimate estimate) {
    return "<b>"+ESTIMATE_TYPE+":&nbsp;</b>"+estimate.getName()+"<br>"+
        "<b>Linear Median:&nbsp;</b>"+estimate.getLinearMedian()+"<br>"+
        "<b>StdDev:&nbsp;</b>"+estimate.getStdDev();
  }

  /**
   * Information for Discretized func estimate
   *
   * @param estimate
   * @return
   */
  private String getTextForDiscretizedFuncEstimate(DiscretizedFuncEstimate estimate) {
    DiscretizedFunc func = estimate.getValues();
    String text =  "<b>"+ESTIMATE_TYPE+":&nbsp;</b>"+estimate.getName()+"<br>"+
        "<b>"+func.getXAxisName()+"&nbsp;&nbsp;"+func.getYAxisName()+"</b> <br>";
    for(int i=0; i<func.getNum(); ++i)
        text+=  func.getX(i)+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+func.getY(i)+"<br>";
    return text;
  }

}
