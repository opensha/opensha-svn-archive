package org.scec.sha.gui.beans;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.ParameterAPI;
import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;

import java.util.Iterator;

/**
 * <p>Title: TimeSpanGuiBean</p>
 * <p>Description: This creates the Time Span GUI Bean </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TimeSpanGuiBean extends ParameterListEditor{

  // timespan Panel title
  public final static String TIMESPAN_EDITOR_TITLE =  "Time Span";
  // save the TimeSpan instance
  private TimeSpan timeSpan;

  /**
   * default constructor
   */
  public TimeSpanGuiBean() {
    parameterList = new ParameterList();
    // search path needed for making editors
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
  }

  /**
   * Constructor : It accepts the TimeSpan object.
   * This is timeSpan reference as exists in the ERF.
   *
   * @param timeSpan
   */
  public TimeSpanGuiBean(TimeSpan timeSpan) {
    parameterList = new ParameterList();
    // search path needed for making editors
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    setTimeSpan(timeSpan);
  }

  /**
   * It accepts the timespan object and shows it based on adjustable
   * params of this new object
   *
   * @param timeSpan
   */
  public void setTimeSpan(TimeSpan timeSpan) {
    this.parameterList.clear();
    this.timeSpan = timeSpan;
    // get the adjutsbale params and add them to the list
    Iterator it = timeSpan.getAdjustableParamsIterator();
    while(it.hasNext()) {
      ParameterAPI param = (ParameterAPI)it.next();
      this.parameterList.addParameter(param);
    }
    this.editorPanel.removeAll();
    addParameters();
    this.setTitle(TIMESPAN_EDITOR_TITLE);
  }

  /**
   * Return the timeSpan that is shown in gui Bean
   * @return
   */
  public TimeSpan getTimeSpan() {
    return this.timeSpan;
  }
}