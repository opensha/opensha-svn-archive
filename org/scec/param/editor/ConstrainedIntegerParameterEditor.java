package org.scec.param.editor;

import org.scec.param.*;

import org.scec.util.ParamUtils;
import java.awt.*;


/**
 * <b>Title:</b> ConstrainedIntegerParameterEditor<pr>
 *
 * <b>Description:</b> Special ParameterEditor for editing
 * ConstrainedIntegetParameters which recall have a minimum and maximum
 * allowed values. The widget is an IntegerTextField
 * so that only integers can be typed in. When hitting <enter> or moving
 * the mouse away from the IntegerTextField, the value will change back
 * to the original if the new number is outside the constraints range.
 * The constraints also appear as a tool tip when you hold the mouse
 * cursor over the IntegerTextField. <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */
public class ConstrainedIntegerParameterEditor extends IntegerParameterEditor
{

    /** Class name for debugging. */
    protected final static String C = "ConstrainedIntegerParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    /** No-Arg constructor calls parent constructtor */
    public ConstrainedIntegerParameterEditor() { super(); }

    /**
     * Constructor that sets the parameter that it edits.
     * Only calls the super() function.
     */
    public ConstrainedIntegerParameterEditor(ParameterAPI model)
	    throws Exception
    { super(model); }


    /**
     * Calls the super().;setFunction() and uses the constraints
     * to set the JTextField tooltip to show the constraint values.
     */
    public void setParameter(ParameterAPI model) {

        String S = C + ": setParameter(): ";
        if(D) System.out.println(S.concat("Starting"));

        super.setParameter(model);

        IntegerConstraint constraint=getConstraint();

        valueEditor.setToolTipText( "Min = " + constraint.getMin().toString() + "; Max = " + constraint.getMax().toString() );

        this.setNameLabelToolTip(model.getInfo());

        if(D) System.out.println(S.concat("Ending"));
    }

    /** This is where the IntegerTextField for the Constraint IntegerParameter
     * It checks if the min and max constraint value are same then change the
     * font and size of the valueEditor and widgetPanel
     * is defined and configured. */
    protected void addWidget() {
      String S = C + "ConstrainedDoubleParameterEditor: addWidget(): ";
      if(D) System.out.println(S + "Starting");
      super.addWidget();
      IntegerConstraint constraint =getConstraint();
      if(constraint.getMax().doubleValue()==constraint.getMin().doubleValue()){
        if (  valueEditor != null ) {
          ((IntegerTextField) valueEditor).setEditable(false);
          ((IntegerTextField) valueEditor).setMinimumSize( LABEL_DIM );
          ((IntegerTextField) valueEditor).setFont( JCOMBO_FONT );
          ((IntegerTextField) valueEditor).setForeground( Color.blue );
          ((IntegerTextField) valueEditor).setBorder( CONST_BORDER );
          widgetPanel.setBackground(STRING_BACK_COLOR);
          widgetPanel.setForeground( Color.blue );
        }
      }

      if(D) System.out.println(S + "Ending");
    }


    /**
     * @returns the IntegerConstraint
     */
    protected IntegerConstraint getConstraint(){
      //Integer constraint declaration
      IntegerConstraint constraint;

      if( ParamUtils.isWarningParameterAPI( model ) ){
        constraint = (IntegerConstraint)((WarningParameterAPI)model).getWarningConstraint();
        if( constraint == null ) constraint = (IntegerConstraint) model.getConstraint();
      }
      else
       constraint = (IntegerConstraint) model.getConstraint();

      return constraint;
    }

}
