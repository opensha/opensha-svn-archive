package org.scec.param.editor;

import java.awt.event.*;
import javax.swing.border.*;
import org.scec.exceptions.*;
import org.scec.param.*;

/**
 * <b>Title:</b> IntegerParameterEditor<br>
 * <b>Description:</b> Special ParameterEditor for editing IntegetParameters. The widget
 * is an IntegerTextField so that only integers can be typed in. <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class IntegerParameterEditor extends ParameterEditor
{
    protected static final String C = "IntegerParameterEditor";
    protected static final boolean D = false;

    public IntegerParameterEditor() { super(); }

    public IntegerParameterEditor(ParameterAPI model) throws Exception {

        super(model);

        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        if ( (model != null ) && !(model instanceof IntegerParameter))
            throw new Exception( S + "Input model parameter must be a IntegerParameter.");

        //addWidget();
        updateNameLabel( model.getName() );

        if(D) System.out.println(S.concat("Ending"));

    }

    public void setAsText(String string) throws IllegalArgumentException { }

    protected void setWidgetObject(String name, Object obj) {
        String S = C + ": setWidgetObject(): ";
        if(D) System.out.println(S + "Starting");

        super.setWidgetObject(name, obj);

        if ( ( obj != null ) && ( valueEditor != null ) )
            ((IntegerTextField) valueEditor).setText(obj.toString());
        else if ( valueEditor != null )
            ((IntegerTextField) valueEditor).setText("");


        if(D) System.out.println(S.concat("Ending"));
    }


    public void setWidgetBorder(Border b){
        ((IntegerTextField)valueEditor).setBorder(b);
    }

    protected void addWidget() {

        String S = C + ": addWidget(): ";
        if(D) System.out.println(S + "Starting");

        valueEditor = new IntegerTextField();
        valueEditor.setMinimumSize( LABEL_DIM );
        valueEditor.setPreferredSize( LABEL_DIM );
        valueEditor.setBorder(ETCHED);
        valueEditor.setFont(this.DEFAULT_FONT);

        valueEditor.addFocusListener( this );
        valueEditor.addKeyListener( this );

        widgetPanel.add(valueEditor, ParameterEditor.WIDGET_GBC);

        if(D) System.out.println(S + "Ending");
    }

    public void keyTyped(KeyEvent e) throws NumberFormatException {

        String S = C + ": valueEditor_keyTyped(): ";
        super.keyTyped(e);

        keyTypeProcessing = false;
        if( focusLostProcessing == true ) return;

        if (e.getKeyChar() == '\n') {

            keyTypeProcessing = true;
            if(D) System.out.println(S + "Return key typed");
            String value = ((IntegerTextField) valueEditor).getText();

            if(D) System.out.println(S + "New Value = " + value);

            try {
                Integer d = null;
                if( !value.equals( "" ) ) d = new Integer(value);
                setValue(d);
                synchToModel();
                valueEditor.validate();
                valueEditor.repaint();
            }
            catch (ConstraintException ee) {
                if(D) System.out.println(S + "Error = " + ee.toString());

                Object obj = getValue();
                if( obj != null )
                    ((IntegerTextField) valueEditor).setText(obj.toString());
                else ((IntegerTextField) valueEditor).setText( "" );

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
    }

    public void focusLost(FocusEvent e) throws ConstraintException {

        String S = C + ": focusLost(): ";
        if(D) System.out.println(S + "Starting");

        super.focusLost(e);

        focusLostProcessing = false;
        if( keyTypeProcessing == true ) return;
        focusLostProcessing = true;

        String value = ((IntegerTextField) valueEditor).getText();
        try {

            Integer d = null;
            if( !value.equals( "" ) ) d = new Integer(value);
            setValue(d);
            synchToModel();
            valueEditor.validate();
            valueEditor.repaint();
        }
        catch (ConstraintException ee) {
            if(D) System.out.println(S + "Error = " + ee.toString());

            Object obj = getValue();
            if( obj != null )
                ((IntegerTextField) valueEditor).setText(obj.toString());
            else ((IntegerTextField) valueEditor).setText( "" );

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
        String S = C + ": setParameter(): ";
        if(D) System.out.println(S.concat("Starting"));

        super.setParameter(model);
        ((IntegerTextField) valueEditor).setToolTipText("No Constraints");

        String info = model.getInfo();
        if( (info != null ) && !( info.equals("") ) ){
            this.nameLabel.setToolTipText( info );
        }
        else this.nameLabel.setToolTipText( null);


        if(D) System.out.println(S.concat("Ending"));
    }

    public void synchToModel(){
        Object obj = model.getValue();
        if ( obj != null )
            ((IntegerTextField) valueEditor).setText( obj.toString() );

        else ((IntegerTextField) valueEditor).setText( "" );

    }
}
