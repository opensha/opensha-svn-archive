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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.border.Border;

import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.exceptions.WarningException;
import org.opensha.commons.param.IntegerParameter;
import org.opensha.commons.param.ParameterAPI;



/**
 * <b>Title:</b> IntegerParameterEditor<p>
 *
 * <b>Description:</b> Subclass of ParameterEditor for editing IntegerParameters.
 * The widget is an IntegerTextField so that only integers can be typed in. <p>
 *
 * The main functionality overidden from the parent class to achive Integer
 * cusomization are the setWidgetObject() and AddWidget() functions. The parent's
 * class JComponent valueEditor field becomes an IntegerTextField,  a subclass
 * of a JTextField. <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */
public class IntegerParameterEditor extends NewParameterEditor<Integer> implements FocusListener, KeyListener
{

	private boolean focusLostProcessing = false;
	private boolean keyTypeProcessing = false;

	private IntegerTextField widget;

	/** Class name for debugging. */
	protected final static String C = "IntegerParameterEditor";
	/** If true print out debug statements. */
	protected final static boolean D = true;

	/** No-Arg constructor calls parent constructor */
	public IntegerParameterEditor() { super(); }

	/**
	 * Constructor that sets the parameter that it edits. An
	 * Exception is thrown if the model is not an IntegerParameter.
	 * It it is then the updateNameLabel is called which
	 * inderectly calls addWidget. <p>
	 *
	 * Note: When calling the super() constuctor addWidget() is called
	 * which configures the IntegerTextField as the editor widget. <p>
	 */
	public IntegerParameterEditor(ParameterAPI model) throws Exception {

		super(model);

		String S = C + ": Constructor(model): ";
		if(D) System.out.println(S + "Starting");

		// TODO move to validate step
		if ( (model != null ) && !(model instanceof IntegerParameter))
			throw new Exception( S + "Input model parameter must be a IntegerParameter.");

		//addWidget();
		//        updateNameLabel( model.getName() );
		//        this.setParameter(model);
		if(D) System.out.println(S.concat("Ending"));

	}

	//    /** Currently does nothing */
	//    public void setAsText(String string) throws IllegalArgumentException { }
	//
	//    /** Passes in a new Parameter with name to set as the parameter to be editing */
	//    protected void setWidgetObject(String name, Object obj) {
	//        String S = C + ": setWidgetObject(): ";
	//        if(D) System.out.println(S + "Starting");
	//
	//        super.setWidgetObject(name, obj);
	//
	//        if ( ( obj != null ) && ( valueEditor != null ) )
	//            ((IntegerTextField) valueEditor).setText(obj.toString());
	//        else if ( valueEditor != null )
	//            ((IntegerTextField) valueEditor).setText(" ");
	//
	//
	//        if(D) System.out.println(S.concat("Ending"));
	//    }
	//
	//
	//    /** Allows customization of the IntegerTextField border */
	//    public void setWidgetBorder(Border b){
	//        ((IntegerTextField)valueEditor).setBorder(b);
	//    }
	//
	//    /** This is where the IntegerTextField is defined and configured. */
	//    protected void addWidget() {
	//
	//        String S = C + ": addWidget(): ";
	//        if(D) System.out.println(S + "Starting");
	//
	//        valueEditor = new IntegerTextField();
	//        valueEditor.setMinimumSize( LABEL_DIM );
	//        valueEditor.setPreferredSize( LABEL_DIM );
	//        valueEditor.setBorder(ETCHED);
	//        valueEditor.setFont(this.DEFAULT_FONT);
	//
	//        valueEditor.addFocusListener( this );
	//        valueEditor.addKeyListener( this );
	//
	//        widgetPanel.add(valueEditor, ParameterEditor.WIDGET_GBC);
	//        widgetPanel.setBackground(null);
	//        widgetPanel.validate();
	//        widgetPanel.repaint();
	//        if(D) System.out.println(S + "Ending");
	//    }


	/**
	 * Called everytime a key is typed in the text field to validate it
	 * as a valid integer character ( digits and - sign in first position ).
	 */
	public void keyTyped(KeyEvent e) throws NumberFormatException {

		String S = C + ": valueEditor_keyTyped(): ";

		keyTypeProcessing = false;
		if( focusLostProcessing == true ) return;

		if (e.getKeyChar() == '\n') {

			keyTypeProcessing = true;
			if(D) System.out.println(S + "Return key typed");
			String value = ((IntegerTextField) widget).getText();

			if(D) System.out.println(S + "New Value = " + value);

			try {
				Integer d = null;
				if( !value.trim().equals( "" ) ) d = new Integer(value);
				setValue(d);
				refreshParamEditor();
				widget.validate();
				widget.repaint();
			}
			catch (ConstraintException ee) {
				if(D) System.out.println(S + "Error = " + ee.toString());

				Object obj = getValue();
				if( obj != null )
					((IntegerTextField) widget).setText(obj.toString());
				else ((IntegerTextField) widget).setText( " " );

				//                if( !catchConstraint ){
				unableToSetValue(value);
				//                } // TODO add this back?
				keyTypeProcessing = false;
			}
			catch (WarningException ee){
				keyTypeProcessing = false;
				refreshParamEditor();
				widget.validate();
				widget.repaint();
			}
		}
	}

	/**
	 * Called when the user clicks on another area of the GUI outside
	 * this editor panel. This synchornizes the editor text field
	 * value to the internal parameter reference.
	 */
	public void focusLost(FocusEvent e) throws ConstraintException {

		String S = C + ": focusLost(): ";
		if(D) System.out.println(S + "Starting");

		focusLostProcessing = false;
		if( keyTypeProcessing == true ) return;
		focusLostProcessing = true;

		String value = ((IntegerTextField) widget).getText();
		try {

			Integer d = null;
			if( !value.trim().equals( "" ) ) d = new Integer(value);
			setValue(d);
			refreshParamEditor();
			widget.validate();
			widget.repaint();
		}
		catch (ConstraintException ee) {
			if(D) System.out.println(S + "Error = " + ee.toString());

			Object obj = getValue();
			if( obj != null )
				((IntegerTextField) widget).setText(obj.toString());
			else ((IntegerTextField) widget).setText( " " );

			//            if( !catchConstraint ){
			this.unableToSetValue(value);
			//            }
			focusLostProcessing = false;
		}
		catch (WarningException ee){
			focusLostProcessing = false;
			refreshParamEditor();
			widget.validate();
			widget.repaint();
		}


		focusLostProcessing = false;
		if(D) System.out.println(S + "Ending");


	}

	@Override
	public void setEnabled(boolean enabled) {
		if (widget != null)
			widget.setEnabled(enabled);
	}

	@Override
	protected JComponent buildWidget() {
		if (D) System.out.println(C+": buildWidget starting");

		widget = new IntegerTextField();
		widget.setMinimumSize( LABEL_DIM );
		widget.setPreferredSize( LABEL_DIM );
		widget.setBorder(ETCHED);
		widget.setFont(DEFAULT_FONT);

		widget.addFocusListener( this );
		widget.addKeyListener( this );
		
		updateWidget();

		if (D) System.out.println(C+": buildWidget ending");
		return widget;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	protected JComponent updateWidget() {
		if (D) System.out.println(C+": updateWidget starting");
		Integer value = getValue();
		if( value != null )
			((IntegerTextField) widget).setText(value.toString());
		else ((IntegerTextField) widget).setText( "" );
		if (D) System.out.println(C+": updateWidget ending");
		return widget;
	}

	@Override
	public boolean isParameterSupported(ParameterAPI<Integer> param) {
		return param == null || param instanceof IntegerParameter;
	}

}
