package org.opensha.sha.gui.beans;


import java.util.Iterator;
import java.awt.*;

import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.ParameterAPI;
import org.opensha.data.TimeSpan;
import org.opensha.param.ParameterList;
import javax.swing.*;




/**
 * <p>Title: TimeSpanGuiBean</p>
 * <p>Description: This creates the Time Span GUI Bean </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TimeSpanGuiBean extends JPanel{

  // timespan Panel title
  public final static String TIMESPAN_EDITOR_TITLE =  "Set Time Span";
  // save the TimeSpan instance
  private TimeSpan timeSpan;

  private ParameterListEditor editor;
  private ParameterList parameterList;
  private JEditorPane nullTimespanWindow = new JEditorPane();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  /**
   * default constructor
   */
  public TimeSpanGuiBean() {
    parameterList = new ParameterList();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Constructor : It accepts the TimeSpan object.
   * This is timeSpan reference as exists in the ERF.
   *
   * @param timeSpan
   */
  public TimeSpanGuiBean(TimeSpan timeSpan) {
    this();
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
    if(editor !=null)
      this.remove(editor);
    if(timeSpan !=null){
      // get the adjustable params and add them to the list
      Iterator it = timeSpan.getAdjustableParamsIterator();
      while(it.hasNext()) {
        ParameterAPI param = (ParameterAPI)it.next();
        this.parameterList.addParameter(param);
      }
      this.remove(nullTimespanWindow);
      editor = new ParameterListEditor(parameterList);
      editor.setTitle(TIMESPAN_EDITOR_TITLE);
      this.add(editor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
      this.validate();
      this.repaint();
    }
    else{
      this.add(nullTimespanWindow,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));

    }
  }

  /**
   * Return the timeSpan that is shown in gui Bean
   * @return
   */
  public TimeSpan getTimeSpan() {
    return this.timeSpan;
  }
  private void jbInit() throws Exception {
    String text = "This ERF does not have any Timespan\n";
    this.setLayout(gridBagLayout1);

    nullTimespanWindow.setEditable(false);
    nullTimespanWindow.setText(text);
    this.setMinimumSize(new Dimension(0, 0));
    this.add(nullTimespanWindow,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH,  new Insets(4, 4, 4, 4), 0,0));
  }

  /**
   *
   * @returns the ParameterList
   */
  public ParameterList getParameterList(){
    return this.parameterList;
  }

  /**
   *
   * @returns the ParameterListEditor
   */
  public ParameterListEditor getParameterListEditor(){
    return this.editor;
  }

  /**
   *
   * @returns the Visible parameters metadata
   */
  public String getParameterListMetadataString(){
    if(timeSpan !=null)
      return editor.getVisibleParametersCloned().getParameterListMetadataString();
    else
      return "No Timespan";
  }

}
