package org.scec.param.editor;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;


import org.scec.exceptions.*;
import org.scec.param.*;


/**
 * <b>Title:</b> StringParameterEditor<br>
 * <b>Description:</b> Simplist Editor in that the widget is a JTExtField with no
 * Constraints on it.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class StringParameterEditor
    extends ParameterEditor
{

    protected static final String C = "StringParameterEditor";
    protected static final boolean D = false;


    public StringParameterEditor() { super(); }

    public StringParameterEditor(ParameterAPI model) throws Exception{

        super(model);

        if ( ( model != null ) &&  !( model instanceof StringParameter) ) {
            String S = C + ": Constructor(model): ";
            throw new Exception(S + "Input model parameter must be a StringParameter.");
        }

        //addWidget();
    }

    public void setAsText(String string) throws IllegalArgumentException{}

    protected void setWidgetObject(String name, Object obj){

        super.setWidgetObject(name, obj);
        if( ( obj != null ) && ( valueEditor != null ) ) ((JTextField)valueEditor).setText(obj.toString());

    }

    public void setWidgetBorder(Border b){
        ((JTextField)valueEditor).setBorder(b);
    }

    protected void addWidget(){

        valueEditor = new JTextField();
        valueEditor.setPreferredSize(LABEL_DIM);
	    valueEditor.setMinimumSize(LABEL_DIM);
        valueEditor.setBorder(ETCHED);

        valueEditor.addFocusListener( this );
        valueEditor.addKeyListener(this);

        ((JTextField)valueEditor).setText(ParameterEditor.DATA_TEXT);
        widgetPanel.add(valueEditor, ParameterEditor.WIDGET_GBC);

    }

    public void keyTyped(KeyEvent e) {


        String S = C + ": keyTyped(): ";
        if(D) System.out.println(S + "Starting");
        super.keyTyped(e);

        keyTypeProcessing = false;
        if( focusLostProcessing == true ) return;


        if (e.getKeyChar() == '\n') {
            keyTypeProcessing = true;
            if(D) System.out.println(S + "Return key typed");
            String value = ((JTextField) valueEditor).getText();

            if(D) System.out.println(S + "New Value = " + value);
            try {
                String d = "";
                if( !value.equals( "" ) ) d = value;
                setValue(d);
                synchToModel();
                valueEditor.validate();
                valueEditor.repaint();
            }
            catch (ConstraintException ee) {
                if(D) System.out.println(S + "Error = " + ee.toString());

                Object obj = getValue();
                if( obj != null )
                    ((JTextField) valueEditor).setText(obj.toString());
                else ((JTextField) valueEditor).setText( "" );

                if( !catchConstraint ){ this.unableToSetValue(value); }
                keyTypeProcessing = false;
            }
            catch (WarningException ee){
                keyTypeProcessing = false;
                synchToModel();
                valueEditor.validate();
                valueEditor.repaint();
            }
        }

        keyTypeProcessing = false;
        if(D) System.out.println(S + "Ending");


    }

    public void focusLost(FocusEvent e) {

        String S = C + ": focusLost(): ";
        if(D) System.out.println(S + "Starting");

        super.focusLost(e);

        focusLostProcessing = false;
        if( keyTypeProcessing == true ) return;
        focusLostProcessing = true;

        String value = ((JTextField) valueEditor).getText();
        try {

            String d = "";
            if( !value.equals( "" ) ) d = value;
            setValue(d);
            synchToModel();
            valueEditor.validate();
            valueEditor.repaint();
        }
        catch (ConstraintException ee) {
            if(D) System.out.println(S + "Error = " + ee.toString());

            Object obj = getValue();
            if( obj != null )
                ((JTextField) valueEditor).setText(obj.toString());
            else ((JTextField) valueEditor).setText( "" );

            if( !catchConstraint ){ this.unableToSetValue(value); }
            focusLostProcessing = false;
        }
        catch (WarningException ee){
            focusLostProcessing = false;
            synchToModel();
            valueEditor.validate();
            valueEditor.repaint();
        }


        focusLostProcessing = false;
        if(D) System.out.println(S + "Ending");

    }

    public void setParameter(ParameterAPI model) {

        String S = "StringParameterEditor: setParameter(): ";
        if(D) System.out.println(S + "Starting");
        super.setParameter(model);
        ((JTextField) valueEditor).setToolTipText("No Constraints");

        if(D) System.out.println(S + "Ending");
    }

    public void synchToModel(){
        Object obj = model.getValue();

        if ( obj != null )
            ((JTextField)valueEditor).setText( obj.toString() );
    }
}
