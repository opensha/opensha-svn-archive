package org.opensha.commons.param.editor;


import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.opensha.commons.param.ParameterAPI;





/**
 * <p>Title: BooleanParameterEditor</p>
 * <p>Description: </p>
 * @author : Nitin Gupta
 * @created : Dec 28,2003
 * @version 1.0
 */

public class BooleanParameterEditor extends ParameterEditor implements
    ItemListener{

  /** Class name for debugging. */
  protected final static String C = "BooleanParameterEditor";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  //default class constructor
  public BooleanParameterEditor() {}

  public BooleanParameterEditor(ParameterAPI model){
    super(model);
  }



  /** Allows customization of the IntegerTextField border */
  public void setWidgetBorder(Border b){
      ((JCheckBox)valueEditor).setBorder(b);
  }

  /** This is where the JTextField is defined and configured. */
  protected void addWidget(){

      valueEditor = new JCheckBox(model.getName());;
      valueEditor.setPreferredSize(LABEL_DIM);
          valueEditor.setMinimumSize(LABEL_DIM);
      valueEditor.setBorder(ETCHED);

      ((JCheckBox)valueEditor).addItemListener(this);
      ((JCheckBox)valueEditor).setSelected(((Boolean)model.getValue()).booleanValue());
      widgetPanel.add(valueEditor, ParameterEditor.WIDGET_GBC);
  }

  /** Passes in a new Parameter with name to set as the parameter to be editing */
  protected void setWidgetObject(String name, Object obj) {
      String S = C + ": setWidgetObject(): ";
      if(D) System.out.println(S + "Starting");


      super.setWidgetObject(null, obj);

      if ( ( obj != null ) &&  ( valueEditor != null ) )
        ((JCheckBox) valueEditor).setSelected(((Boolean)obj).booleanValue());
      else if(obj == null)
        ((JCheckBox) valueEditor).setSelected(((JCheckBox) valueEditor).isSelected());

      if(D) System.out.println(S + "Ending");
    }

  /** Sets the parameter to be edited. */
  public void setParameter(ParameterAPI model) {
      String S = C + ": setParameter(): ";
      if(D) System.out.println(S + "Starting");

      super.setParameter(model);

      String info = model.getInfo();
      if( (info != null ) && !( info.equals("") ) )
        ((JCheckBox)valueEditor).setToolTipText(info);
      else ((JCheckBox)valueEditor).setToolTipText(null);


      if(D) System.out.println(S + "Ending");
    }
  /**
   * Updates the JCheckBox  with the parameter value. Used when
   * the parameter is set for the first time, or changed by a background
   * process independently of the GUI. This could occur with a ParameterChangeFail
   * event.
   */
  public void refreshParamEditor(){
      Object obj = model.getValue();

      if ( obj != null )
          ((JCheckBox)valueEditor).setSelected( ((Boolean)obj).booleanValue());
      else
        throw new RuntimeException(C+": Boolean Parameter cannot be null");
    }


  /**
   * This function is called when the user click for the BooleanParameterEditor Button
   *
   * @param ae
   */
  public void itemStateChanged(ItemEvent ae ) {
    boolean isSelected = ((JCheckBox) valueEditor).isSelected();
    this.setValue(new Boolean(isSelected));
  }

}
