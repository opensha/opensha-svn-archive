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

/**
 * <p>Title: TranslatedConstrainedDoubleParameterEditor</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TranslatedConstrainedDoubleParameterEditor extends ConstrainedDoubleParameterEditor {

    protected static final String C = "TranslatedConstrainedDoubleParameterEditor";
    protected static final boolean D = false;

    public TranslatedConstrainedDoubleParameterEditor() { super(); }

    public TranslatedConstrainedDoubleParameterEditor(ParameterAPI model)
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
}
