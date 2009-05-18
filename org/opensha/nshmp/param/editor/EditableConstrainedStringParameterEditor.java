package org.opensha.nshmp.param.editor;

import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.ParameterAPI;
import org.opensha.commons.exceptions.ConstraintException;

import javax.swing.JComboBox;

/**
 * <p>Title: EditableConstrainedStringParameterEditor.java </p>
 * <p>Description: This is same as ConstrainedStringParameterEditor. Only difference
 * here is that the comboBox is editable in this case. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EditableConstrainedStringParameterEditor extends ConstrainedStringParameterEditor {

  /**
   * Constructor
   */
  public EditableConstrainedStringParameterEditor() {
  }

  /**
   *
   * @param model
   * @throws ConstraintException
   */
  public EditableConstrainedStringParameterEditor(ParameterAPI model) throws ConstraintException {
    super(model);
  }

  /**
   * The implementation is same as for ConstrainedStringParameterEditor. The only
   * addition here is that JComboBox has been made editable in this case.
   */
  protected void addWidget() {
    super.addWidget();
		this.valueEditor.setSize(1000,50);
    if(this.valueEditor instanceof JComboBox) {
      JComboBox pickList = (JComboBox)valueEditor;
      pickList.setEditable(true);
    }
  }
}
