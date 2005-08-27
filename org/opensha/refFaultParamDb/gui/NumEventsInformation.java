package org.opensha.refFaultParamDb.gui;

import org.opensha.param.IntegerParameter;
import javax.swing.*;


/**
 * <p>Title: NumEventsInformation.java </p>
 * <p>Description: This GUI allows the user to enter the information about
 * number of events(at a site) in a particular time period </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NumEventsInformation extends JFrame {
  // title for this frame
  private final static String TITLE = "Num Events Information";

  // allows user to enter the number of events
  private final static String NUM_EVENTS_PARAM_NAME = "Num Events";
  private final static int NUM_EVENTS_PARAM_MIN = 0;
  private final static int NUM_EVENTS_PARAM_MAX = Integer.MAX_VALUE;
  private final static Integer NUM_EVENTS_PARAM_DEFAULT = new Integer(0);
  private IntegerParameter numEventsParam ;

  private JTextArea eventNumberList;
  private JTextArea eventsProbList;

  public NumEventsInformation()  {
  }

  public static void main(String[] args)  {
    NumEventsInformation numEventsInformation1 = new NumEventsInformation();
  }
}