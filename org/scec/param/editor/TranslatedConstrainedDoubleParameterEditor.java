package org.scec.param.editor;

/**
 * <p>Title: </p>
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

    public void setParameter(ParameterAPI model) {

        String S = C + ": setParameter(): ";
        if(D)System.out.println(S + "Starting");

        super.setParameter(model);

        DoubleConstraint constraint;

        if( model instanceof TranslatedWarningDoubleParameter){

            TranslatedWarningDoubleParameter param1 = (TranslatedWarningDoubleParameter)model;

            valueEditor.setToolTipText( "Min = " + param1.getWarningMin().toString() + "; Max = " + constraint.getWarningMax().toString() );
            this.setNameLabelToolTip(model.getInfo());

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
