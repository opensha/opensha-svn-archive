package org.scec.param.editor;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.param.event.*;

/**
 * <p>Title: TreeBranchWeightsParameterEditor</p>
 * <p>Description: This class is more like a parameterList consisting of only
 * Double Parameters and considering this parameterList as a single Parameter.
 * This parameter editor will show up as the button on th GUI interface and
 * when the user punches the button, all the parameters will pop up in a seperate window
 * showing all the double parameters contained within this parameter.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created : Feb 01,2003
 * @version 1.0
 */

public class TreeBranchWeightsParameterEditor extends ParameterListParameterEditor implements
ActionListener{

  /** Class name for debugging. */
  protected final static String C = "TreeBranchWeightsParameterEditor";

  //default class constructor
  public TreeBranchWeightsParameterEditor() {}

  public TreeBranchWeightsParameterEditor(ParameterAPI model){
    super(model);
  }

  /**
   * This function is called when user punches the button to update the ParameterList Parameter
   * @param e
   */
  protected void button_actionPerformed(ActionEvent e) {
    ParameterList paramList = editor.getParameterList();
    boolean doSumToOne =((TreeBranchWeightsParameter)param).doWeightsSumToOne(paramList);
    if(doSumToOne){
      if(parameterChangeFlag){
        param.setValue(paramList);
        parameterChangeFlag = false;
      }
      frame.dispose();
    }
    else{
      JOptionPane.showMessageDialog(frame,"Parameters Value should sum to One",
                                    "Incorrect Input",JOptionPane.ERROR_MESSAGE);
    }
  }
}
