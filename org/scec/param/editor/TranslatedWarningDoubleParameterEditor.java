package org.scec.param.editor;

import java.util.*;
import java.awt.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.gui.SidesBorder;
import org.scec.param.*;
import org.scec.util.ParamUtils;
import org.scec.exceptions.*;
import org.scec.param.translate.*;

/**
 * <p>Title: TranslatedConstrainedDoubleParameterEditor</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TranslatedWarningDoubleParameterEditor extends ConstrainedDoubleParameterEditor {

    protected static final String C = "TranslatedWarningDoubleParameterEditor";
    protected static final boolean D = true;

    public TranslatedWarningDoubleParameterEditor() { super(); }

    public TranslatedWarningDoubleParameterEditor(ParameterAPI model)
	    throws Exception
    { super(model); }

    public void setParameter(ParameterAPI model) throws ParameterException {

        String S = C + ": setParameter(): ";
        if(D)System.out.println(S + "Starting");

        if ( model == null ) throw new NullPointerException( S + "Input Parameter data cannot be null" );
        else this.model = model;

        String name = "";
        name = model.getName();
        Object value = model.getValue();

        removeWidget();
        addWidget();

        setWidgetObject( name, value );


        DoubleConstraint constraint;

        if( model instanceof TranslatedWarningDoubleParameter){

            TranslatedWarningDoubleParameter param1 = (TranslatedWarningDoubleParameter)model;
            try{
                valueEditor.setToolTipText( "Min = " + param1.getWarningMin().toString() + "; Max = " + param1.getWarningMax().toString() );
                this.setNameLabelToolTip(model.getInfo());
            }
            catch( Exception e ){
                throw new ParameterException(e.toString());
            }



        }
        else if( ParamUtils.isWarningParameterAPI( model ) ){
            constraint = ((WarningParameterAPI)model).getWarningConstraint();
            if( constraint == null ) constraint = (DoubleConstraint) model.getConstraint();

            valueEditor.setToolTipText( "Min = " + constraint.getMin().toString() + "; Max = " + constraint.getMax().toString() );
            this.setNameLabelToolTip(model.getInfo());

        }

        else {
            constraint = (DoubleConstraint) model.getConstraint();
            valueEditor.setToolTipText( "Min = " + constraint.getMin().toString() + "; Max = " + constraint.getMax().toString() );
            this.setNameLabelToolTip(model.getInfo());
        }



        if(D) System.out.println(S + "Ending");
    }

    public void setWidgetBorder(Border b){
        ((NumericTextField)valueEditor).setBorder(b);
    }




    /**
     *  Needs to be called by subclasses when editable widget field change fails
     *  due to constraint problems
     *
     * @param  value                    Description of the Parameter
     * @exception  ConstraintException  Description of the Exception
     */
    public void unableToSetValue( Object value ) throws ConstraintException {

        String S = C + ": unableToSetValue():";
        if(D) System.out.println(S + "New Value = " + value.toString());


        if( value instanceof String){
            try{ value = new Double(value.toString()); }
            catch( NumberFormatException ee){}
        }

        if ( ( value != null ) && ( model != null ) && value instanceof Double) {


            Object obj = model.getValue();

            if( obj != null && obj instanceof Double && model instanceof TranslatedWarningDoubleParameter){

                TranslatedWarningDoubleParameter param = (TranslatedWarningDoubleParameter)model;
                TranslatorAPI trans = param.getTrans();

                if( trans != null || param.isTranslate() ){

                    Double dUntranslated = (Double)value;
                    Double dTranslated = new Double( trans.translate( dUntranslated.doubleValue() ) );
                    Double oldUntranslated = (Double)param.getValue();

                    if ( D ) System.out.println( S + "Old Value = " + obj.toString() );

                    if ( !dUntranslated.toString().equals( oldUntranslated.toString() ) ) {
                        org.scec.param.event.ParameterChangeFailEvent event = new org.scec.param.event.ParameterChangeFailEvent(
                            param,
                            param.getName(),
                            oldUntranslated,
                            dUntranslated
                        );

                        firePropertyChangeFailed( event );
                    }
                }
            }
        }
    }



}


