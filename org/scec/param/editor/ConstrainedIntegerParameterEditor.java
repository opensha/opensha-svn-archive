package org.scec.param.editor;

import org.scec.param.IntegerConstraint;
import org.scec.param.ParameterAPI;

/**
 * <b>Title:</b> ConstrainedIntegerParameterEditor<br>
 * <b>Description:</b> Special ParameterEditor for editing IntegetParameters. The widget
 * is an IntegerTextField so that only integers can be typed in. When hitting <enter> or moving
 * the mouse away from the IntegerTextField, the value will change back to the original
 * if the new number is outside the constraints range. The constraints also appear as
 * a tool tip when you hold the mouse cursor over the IntegerTextField<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ConstrainedIntegerParameterEditor extends IntegerParameterEditor
{
    protected static final String C = "ConstrainedIntegerParameterEditor";
    protected static final boolean D = false;

    public ConstrainedIntegerParameterEditor() { super(); }

    public ConstrainedIntegerParameterEditor(ParameterAPI model)
	    throws Exception
    { super(model); }

    public void setParameter(ParameterAPI model) {

        String S = C + ": setParameter(): ";
        if(D) System.out.println(S.concat("Starting"));

        super.setParameter(model);

        IntegerConstraint constraint
            = (IntegerConstraint) model.getConstraint();

        valueEditor.setToolTipText( "Min = " + constraint.getMin().toString() + "; Max = " + constraint.getMax().toString() );

        this.setNameLabelToolTip(model.getInfo());

        if(D) System.out.println(S.concat("Ending"));
    }
}
