package org.scec.param.editor;

import org.scec.param.*;
import org.scec.util.ParamUtils;
import javax.swing.border.*;

// Fix - Needs more comments

/**
 * <b>Title:</b> ConstrainedDoubleParameterEditor<p>
 *
 * <b>Description:</b> Special ParameterEditor for editing Constrained DoubleParameters. The widget
 * is a NumericTextField so that only numbers can be typed in. When hitting <enter> or moving the
 * mouse away from the NumericField, the value will change back to the original
 * if the new number is outside the constraints range. The constraints also appear as a tool tip
 * when you hold the mouse cursor over the NumericTextField<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class ConstrainedDoubleParameterEditor extends DoubleParameterEditor{

    protected static final String C = "ConstrainedDoubleParameterEditor";
    protected static final boolean D = false;

    public ConstrainedDoubleParameterEditor() { super(); }

    public ConstrainedDoubleParameterEditor(ParameterAPI model)
	    throws Exception
    { super(model); }

    public void setParameter(ParameterAPI model) {

        String S = C + ": setParameter(): ";
        if(D)System.out.println(S + "Starting");

        super.setParameter(model);

        DoubleConstraint constraint;
        if( ParamUtils.isWarningParameterAPI( model ) ){
            constraint = ((WarningParameterAPI)model).getWarningConstraint();
            if( constraint == null ) constraint = (DoubleConstraint) model.getConstraint();
        }
        else constraint = (DoubleConstraint) model.getConstraint();

        valueEditor.setToolTipText( "Min = " + constraint.getMin().toString() + "; Max = " + constraint.getMax().toString() );

        this.setNameLabelToolTip(model.getInfo());

        if(D) System.out.println(S + "Ending");
    }

    public void setWidgetBorder(Border b){
        ((NumericTextField)valueEditor).setBorder(b);
    }
}
