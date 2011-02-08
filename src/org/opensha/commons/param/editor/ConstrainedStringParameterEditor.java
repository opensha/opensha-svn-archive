/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.param.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterConstraintAPI;
import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;

/**
 * <b>Title:</b> ConstrainedStringParameterEditor<p>
 *
 * <b>Description:</b> This editor is for editing
 * ConstrainedStringParameters. Recall a ConstrainedStringParameter
 * contains a list of the only allowed values. Therefore this editor
 * presents a picklist of those allowed values, instead of a
 * JTextField or subclass. <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class ConstrainedStringParameterEditor
extends NewParameterEditor<String>
implements ItemListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Class name for debugging. */
	protected final static String C = "ConstrainedStringParameterEditor";
	/** If true print out debug statements. */
	protected final static boolean D = false;
	
	private JComponent widget;

	/**
	 * Sets the model in this constructor. The parameter is checked that it is a
	 * StringParameter, and the constraint is checked that it is a
	 * StringConstraint. Then the constraints are checked that
	 * there is at least one. If any of these fails an error is thrown. <P>
	 *
	 * The widget is then added to this editor, based on the number of
	 * constraints. If only one the editor is made into a non-editable label,
	 * else a picklist of values to choose from are presented to the user.
	 * A tooltip is given to the name label if model info is available.
	 */
	public ConstrainedStringParameterEditor(ParameterAPI<String> model)
	throws ConstraintException {

		super(model);
	}

//	/**
//	 * The parameter is checked that it is a
//	 * DoubleDiscreteParameter, and the constraint is checked that it is a
//	 * DoubleDiscreteConstraint. Then the constraints are checked that
//	 * there is at least one. If any of these fails an error is thrown.
//	 */
//	protected void verifyModel(ParameterAPI model) throws ConstraintException{
//
//		String S = C + ": Constructor(model): ";
//		if(D) System.out.println(S + "Starting");
//
//		if (model == null) {
//			throw new NullPointerException(S + "Input Parameter model cannot be null");
//		}
//
//		if (!(model instanceof StringParameter))
//			throw new ConstraintException(S + "Input model parameter must be a StringParameter.");
//
//		ParameterConstraintAPI constraint = model.getConstraint();
//
//		if (!(constraint instanceof StringConstraint))
//			throw new ConstraintException(S + "Input model constraints must be a StringConstraint.");
//
//		int numConstriants = ((StringConstraint)constraint).size();
//		if(numConstriants < 1)
//			throw new ConstraintException(S + "There are no constraints present, unable to build editor selection list.");
//
//		if (model.isNullAllowed())
//			throw new ConstraintException("null cannot be allowed for a constrained string parameter editor");
//
//		if (model.getValue() == null)
//			throw new NullPointerException(S + "Input Parameter model's value cannot be null");
//
//		if(D) System.out.println(S + "Ending");
//	}

//	/**
//	 * Set's the name label, and the picklist value from the passed in
//	 * values, i.e. model sets the gui
//	 */
//	protected void setWidgetObject(String name, Object obj) {
//		String S = C + ": setWidgetObject(): ";
//		if(D) System.out.println(S + "Starting: Name = " + name + ": Object = " + obj.toString());
//
//		super.setWidgetObject(name, obj);
//
//		if ( ( obj != null ) && ( valueEditor != null ) && ( valueEditor instanceof JComboBox ) )
//			((JComboBox) valueEditor).setSelectedItem(obj.toString());
//
//		if(D) System.out.println(S + "Ending");
//	}

//	/**
//	 * This is where the JComboBox picklist is defined and configured.
//	 * This function adds a little more intellegence in that if there
//	 * is only one constraint, it only adds a lable instead of a picklist.
//	 * No need to give a list of choices when there is only one allowed
//	 * value.
//	 */
//	protected void addWidget() {
//		String S = C + ": addWidget(): ";
//		if(D) System.out.println(S + "Starting");
//
//		//if(widgetPanel != null) widgetPanel.removeAll();
//		if (model != null) {
//
//			StringConstraint con =
//				(StringConstraint) ((StringParameter) model).getConstraint();
//
//			ListIterator it = con.listIterator();
//			Vector strs = new Vector();
//			while (it.hasNext()) {
//				String str = it.next().toString();
//				if (!strs.contains(str)) strs.add(str);
//			}
//
//			if(strs.size() > 1){
//				JComboBox jcb = new JComboBox(strs);
//				jcb.setMaximumRowCount(32);
//				valueEditor = jcb;
//				valueEditor.setPreferredSize(JCOMBO_DIM);
//				valueEditor.setMinimumSize(JCOMBO_DIM);
//				valueEditor.setFont(JCOMBO_FONT);
//				//valueEditor.setBackground(this.BACK_COLOR);
//				((JComboBox) valueEditor).addItemListener(this);
//				valueEditor.addFocusListener( this );
//				widgetPanel.add(valueEditor, COMBO_WIDGET_GBC);
//				widgetPanel.setBackground(null);
//				widgetPanel.validate();
//				widgetPanel.repaint();
//			}
//			else{
//				valueEditor = makeConstantEditor( strs.get(0).toString() );
//				widgetPanel.setBackground(STRING_BACK_COLOR);
//				widgetPanel.add(valueEditor, WIDGET_GBC);
//			}
//
//
//			//widgetPanel.add(valueEditor,
//			//      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 10, 2,
//			//               new Insets(1, 1, 0, 1), 0,
//			//             0));
//
//		}
//
//		if(D) System.out.println(S + "Ending");
//	}

	/**
	 * Called whenever a user picks a new value in the picklist, i.e.
	 * synchronizes the model to the new GUI value. This is where the
	 * picklist value is set in the ParameterAPI of this editor.
	 */
	public void itemStateChanged(ItemEvent e) {
		String S = C + ": itemStateChanged(): ";
		if(D) System.out.println(S + "Starting: " + e.toString());

		String value = ((JComboBox) widget).getSelectedItem().toString();
		if(D) System.out.println(S + "New Value = " + (value) );
		this.setValue(value);

		if(D) System.out.println(S + "Ending");
	}

	@Override
	public boolean isParameterSupported(ParameterAPI<String> param) {
		if (param == null)
			return false;

		if (!(param instanceof StringParameter))
			return false;

		ParameterConstraintAPI constraint = param.getConstraint();

		if (!(constraint instanceof StringConstraint))
			return false;

		int numConstriants = ((StringConstraint)constraint).size();
		if(numConstriants < 1)
			return false;

		if (param.isNullAllowed())
			return false;

		if (param.getValue() == null)
			return false;
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		widget.setEnabled(enabled);
	}

	@Override
	protected JComponent buildWidget() {
		StringConstraint con =
			(StringConstraint) (getParameter()).getConstraint();
		
		ArrayList<String> strs = con.getAllowedStrings();

		if(strs.size() > 1){
			JComboBox combo = new JComboBox(strs.toArray());
			combo.setMaximumRowCount(32);
			widget = combo;
			widget.setPreferredSize(WIGET_PANEL_DIM);
			widget.setMinimumSize(WIGET_PANEL_DIM);
//			widget.setFont(JCOMBO_FONT);
			//valueEditor.setBackground(this.BACK_COLOR);
			combo.setSelectedIndex(strs.indexOf(getParameter().getValue()));
			combo.addItemListener(this);
		}
		else{
			JLabel label = makeSingleConstraintValueLabel( strs.get(0).toString() );
			widget = new JPanel(new BorderLayout());
			widget.setBackground(Color.LIGHT_GRAY);
			widget.add(label);
//			widget.add(valueEditor, WIDGET_GBC);
		}
		return widget;
	}

	@Override
	protected JComponent updateWidget() {
		StringConstraint con =
			(StringConstraint) (getParameter()).getConstraint();
		
		ArrayList<String> strs = con.getAllowedStrings();
		
		if (strs.size() > 1) {
			if (widget instanceof JComboBox) {
				JComboBox combo = (JComboBox)widget;
				combo.removeItemListener(this);
				combo.setModel(new DefaultComboBoxModel(strs.toArray()));
				combo.setSelectedIndex(strs.indexOf(getParameter().getValue()));
				combo.addItemListener(this);
				return widget;
			} else {
				return buildWidget();
			}
		} else {
			return buildWidget();
		}
	}


}
