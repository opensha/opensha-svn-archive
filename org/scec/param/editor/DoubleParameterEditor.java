package org.scec.param.editor;

import java.awt.event.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import javax.swing.border.*;

// Fix - Needs more comments

/**
 * <b>Title:</b> DoubleParameterEditor<p>
 *
 * <b>Description:</b> Special ParameterEditor for editing DoubleParameters. The widget
 * is a NumericTextField so that only numbers can be typed in.<p>
 * @author Steven W. Rock
 * @version 1.0
 */

public class DoubleParameterEditor extends ParameterEditor
{
    protected static final String C = "DoubleParameterEditor";
    protected static final boolean D = false;

    /**
     * No-Arg constructor calls super();
     */
    public DoubleParameterEditor() { super(); }

    /**
     * Sets the model in this constructor. The parameter is checked that it is a
     * DoubleParameter. An error is thrown if not true.
     * <P>
     * The widget is then added to this editor as a DoubleTextField that
     * only allows double values to be typed in.
     */public DoubleParameterEditor(ParameterAPI model) throws Exception {

        super(model);

        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        // verifyModel(model);
        // addWidget();

        if(D) System.out.println(S + "Ending");

    }

    /**
     * The parameter is checked that it is not null and a
     * DoubleDiscreteParameter. If any of these fails an error is thrown.
     */
    private void verifyModel(ParameterAPI model) throws ConstraintException{

        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        if (model == null) {
            throw new NullPointerException(S + "Input Parameter model cannot be null");
        }

        if (!(model instanceof DoubleParameter))
            throw new ConstraintException(S + "Input model parameter must be a DoubleParameter.");

        if(D) System.out.println(S + "Ending");
    }


    /**
     * Not implemented
     */
    public void setAsText(String string) throws IllegalArgumentException { }

    /**
     * Set's the name label, and the textfield value from the passed in value,
     * i.e. model sets the gui
     */
    protected void setWidgetObject(String name, Object obj) {
        String S = C + ": setWidgetObject(): ";
        if(D) System.out.println(S + "Starting");


        super.setWidgetObject(name, obj);

        if ( ( obj != null ) &&  ( valueEditor != null ) )
            ((NumericTextField) valueEditor).setText(obj.toString());

        if(D) System.out.println(S + "Ending");
    }

    public void setWidgetBorder(Border b){
        ((NumericTextField)valueEditor).setBorder(b);
    }

    protected void addWidget() {
        String S = C + "DoubleParameterEditor: addWidget(): ";
        if(D) System.out.println(S + "Starting");

        valueEditor = new NumericTextField();
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

        String S = C + ": keyTyped(): ";
        if(D) System.out.println(S + "Starting");
        super.keyTyped(e);

        keyTypeProcessing = false;
        if( focusLostProcessing == true ) return;


        if (e.getKeyChar() == '\n') {
            keyTypeProcessing = true;
            if(D) System.out.println(S + "Return key typed");
            String value = ((NumericTextField) valueEditor).getText();

            if(D) System.out.println(S + "New Value = " + value);
            try {
                Double d = null;
                 if( !value.equals( "" ) ) d = new Double(value);
                setValue(d);
                synchToModel();
                valueEditor.validate();
                valueEditor.repaint();
            }
            catch (ConstraintException ee) {
                if(D) System.out.println(S + "Error = " + ee.toString());

                Object obj = getValue();
                if( obj != null )
                    ((NumericTextField) valueEditor).setText(obj.toString());
                else ((NumericTextField) valueEditor).setText( "" );

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

    public void focusLost(FocusEvent e)throws ConstraintException {


        String S = C + ": focusLost(): ";
        if(D) System.out.println(S + "Starting");


        //if( true ) return;
        super.focusLost(e);

        focusLostProcessing = false;
        if( keyTypeProcessing == true ) return;
        focusLostProcessing = true;

        String value = ((NumericTextField) valueEditor).getText();
        try {

            Double d = null;
            if( !value.equals( "" ) ) d = new Double(value);
            setValue(d);
            synchToModel();
            valueEditor.validate();
            valueEditor.repaint();
        }
        catch (ConstraintException ee) {
            if(D) System.out.println(S + "Error = " + ee.toString());

            Object obj = getValue();
            if( obj != null )
                ((NumericTextField) valueEditor).setText(obj.toString());
            else ((NumericTextField) valueEditor).setText( "" );

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
        if(D) System.out.println(S + "Starting");

        super.setParameter(model);
        ((NumericTextField) valueEditor).setToolTipText("No Constraints");

        String info = model.getInfo();
        if( (info != null ) && !( info.equals("") ) ){
            this.nameLabel.setToolTipText( info );
        }
        else this.nameLabel.setToolTipText( null);


        if(D) System.out.println(S + "Ending");
    }

    public void synchToModel(){

        Object obj = model.getValue();
        if( obj != null )
            ((NumericTextField) valueEditor).setText( obj.toString() );

        else ((NumericTextField) valueEditor).setText( "" ) ;

    }
}
