package org.scec.param.editor;

import java.util.*;
import java.awt.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.scec.gui.SidesBorder;
import org.scec.param.DoubleDiscreteConstraint;
import org.scec.param.DoubleDiscreteParameter;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterConstraintAPI;
import org.scec.exceptions.ConstraintException;

/**
 * <b>Title:</b> ConstrainedDoubleDiscreteParameterEditor<br>
 * <b>Description:</b> This editor is for editing DoubleDiscreteParameter. The widget is
 * simply a picklist of all possible constrained values you can choose from.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ConstrainedDoubleDiscreteParameterEditor
    extends ParameterEditor
    implements ItemListener
{


    protected static final String C = "ConstrainedDoubleDiscreteParameterEditor";
    protected static final boolean D = false;


    /**
     * No-Arg constructor calls super();
     */
    public ConstrainedDoubleDiscreteParameterEditor() { super(); }

    /**
     * Sets the model in this constructor. The parameter is checked that it is a
     * DoubleDiscreteParameter, and the constraint is checked that it is a
     * DoubleDiscreteConstraint. Then the constraints are checked that
     * there is at least one. If any of these fails an error is thrown.
     * <P>
     * The widget is then added to this editor, based on the number of
     * constraints. If only one the editor is made into a non-editable label,
     * else a picklist of values to choose from are presented to the user.
     * A tooltip is given to the name label if model info is available.
     */
    public ConstrainedDoubleDiscreteParameterEditor(ParameterAPI model)
	    throws ConstraintException
    {
        super(model);

        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        verifyModel(model);
        addWidget();

        // Set the tool tip
        setNameLabelToolTip(model.getInfo());

        if(D) System.out.println(S + "Ending");
    }

    /**
     * Can change the parameter of this editor. Usually called once when
     * initializing the editor. The parameter is checked that it is a
     * DoubleDiscreteParameter, and the constraint is checked that it is a
     * DoubleDiscreteConstraint. Then the constraints are checked that
     * there is at least one. If any of these fails an error is thrown.
     * <P>
     * The widget is then added to this editor, based on the number of
     * constraints. If only one the editor is made into a non-editable label,
     * else a picklist of values to choose from are presented to the user.
     * A tooltip is given to the name label if model info is available.
     */
    public void setParameter(ParameterAPI model) throws ConstraintException{

        String S = C + ": setParameter(): ";

        verifyModel(model);
        this.model = model;

        String name = model.getName();
        Object value = model.getValue();

        removeWidget();
	    addWidget();

        setWidgetObject(name, value);

        // Set the tool tip
        setNameLabelToolTip(model.getInfo());

    }


    /**
     * The parameter is checked that it is a
     * DoubleDiscreteParameter, and the constraint is checked that it is a
     * DoubleDiscreteConstraint. Then the constraints are checked that
     * there is at least one. If any of these fails an error is thrown.
     */
    private void verifyModel(ParameterAPI model) throws ConstraintException{

        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        if (model == null) {
            throw new ConstraintException(S + "Input Parameter model cannot be null");
        }
        if (!(model instanceof DoubleDiscreteParameter))
            throw new ConstraintException
                  (S + "Input model parameter must be a DoubleDiscreteParameter.");

        ParameterConstraintAPI constraint = model.getConstraint();

        if (!(constraint instanceof DoubleDiscreteConstraint))
            throw new ConstraintException(S + "Input model constraints must be a DoubleDiscreteConstraint.");

        int numConstriants = ((DoubleDiscreteConstraint)constraint).size();
        if(numConstriants < 1)
            throw new ConstraintException(S + "There are no constraints present, unable to build editor selection list.");


        if(D) System.out.println(S + "Ending");
    }


    /**
     * Not implemented
     */
    public void setAsText(String string) throws IllegalArgumentException { }

    /**
     * Set's the name label, and the picklist value from the passed in values,
     * i.e. model sets the gui
     */
    protected void setWidgetObject(String name, Object obj) {

        String S  = C + ": setWidgetObject(): ";
        if(D) System.out.println(S + "Starting");

        super.setWidgetObject(name, obj);

        if ( ( obj != null ) &&  ( valueEditor != null ) && ( valueEditor instanceof JComboBox ) )
            ((JComboBox) valueEditor).setSelectedItem(obj.toString());

        if(D) System.out.println(S + "Ending");
    }


     public void setWidgetBorder(Border b){
    }

    /**
     * Adds the editor widget, a jcombo box if the discrete constraints are
     * larger than one, else sets to a non-editable label if there is only one value
     * in the constraint.
     */
    protected void addWidget() {

        String S = C + ": addWidget(): ";
        if(D) System.out.println(S + "Starting");

        if (model != null) {
            DoubleDiscreteConstraint con = ((DoubleDiscreteConstraint)
               ((DoubleDiscreteParameter) model).getConstraint());

            ListIterator it = con.listIterator();
            Vector strs = new Vector();
            while (it.hasNext()) {
                String str = it.next().toString();
                if (!strs.contains(str)) strs.add(str);
            }

            if(strs.size() > 1){

                valueEditor = new JComboBox(strs);
                valueEditor.setPreferredSize(JCOMBO_DIM);
                valueEditor.setMinimumSize(JCOMBO_DIM);
                valueEditor.setFont(JCOMBO_FONT);
                ((JComboBox) valueEditor).addItemListener(this);
                ((JComboBox) valueEditor).addFocusListener( this );

            }
            else{
                valueEditor = makeConstantEditor( strs.get(0).toString() );
                widgetPanel.setBackground(STRING_BACK_COLOR);
            }

            widgetPanel.add(valueEditor, WIDGET_GBC);
        }

        if(D) System.out.println(S + "Ending");
    }

    public void synchToModel(){

        if( valueEditor instanceof JComboBox ){

            Object obj = model.getValue();
            if( obj != null )
                ((JComboBox)valueEditor).setSelectedItem( obj.toString() );

        }
    }

    /**
     * Called whenever a user picks a new value in the picklist, i.e.
     * synchronizes the model to the new GUI value. This is where the
     * picklist value is set in the ParameterAPI of this editor.
     */
    public void itemStateChanged(ItemEvent e) {
        String S = C + ": itemStateChanged(): ";
        if(D) System.out.println(S + "Starting: " + e.toString());

        String value = ((JComboBox) valueEditor).getSelectedItem().toString();
        Double d = new Double(value);
        this.setValue(d);

        if(D) System.out.println(S + "Ending");
    }

    public void focusGained(FocusEvent e) {

        String S = C + ": focusGained(): ";
        if(D) System.out.println(S + "Starting: " + e.toString());

        super.focusGained(e);
    }

    public void focusLost(FocusEvent e) {

        String S = C + ": focusLost(): ";
        if(D) System.out.println(S + "Starting: " + e.toString());

        super.focusLost(e);

    }
}
