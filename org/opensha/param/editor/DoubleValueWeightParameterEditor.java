package org.opensha.param.editor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;


import org.opensha.data.ValueWeight;
import org.opensha.param.DoubleConstraint;
import org.opensha.param.DoubleParameter;
import org.opensha.param.DoubleValueWeightConstraint;
import org.opensha.param.ParameterAPI;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;

/**
 *<b>Title:</b> DoubleValueWeightParameterEditor<p>
 *
 * <b>Description:</b>Subclass of ParameterEditor for editing DoubleValueWeightParameters.
 * The widget has two DoubleTextFields (one to enter value and other for weight)
 *  so that only numbers can be typed in. <p>
 *
 * The main functionality overidden from the parent class to achive Double
 * cusomization are the setWidgetObject() and AddWidget() functions. 
 * 
 * Note: We have to create a double parameter with constraints if we want to reflect the constarints
 *       as the tooltip text in the GUI. Because when we editor is created for that
 *       double parameter, it creates a constraint double parameter and then we can
 *       change the constraint and it will be reflected in the tool tip text.
 * <p> <p>
 *
 * @author vipingupta
 *
 */
public class DoubleValueWeightParameterEditor extends ParameterEditor 
												implements ParameterChangeListener {
	private final static boolean D = false;
	private final static String VALUE = "Value";
	private final static String WEIGHT = "Weight";
	protected DoubleParameter valueParameter;
	protected DoubleParameter weightParameter;
	private ParameterEditor valueParameterEditor, weightParameterEditor;
	protected final static Dimension PANEL_DIM = new Dimension( 100, 50);

	/**
	 * Default (no-argument) constructor
	 * Calls parent constructor
	 */
	public DoubleValueWeightParameterEditor() { super(); }
	
	 /**
     * Constructor that sets the parameter that it edits.
     *
     */
     public DoubleValueWeightParameterEditor(ParameterAPI model) throws Exception {
        super(model);
        //this.setParameter(model);
    }
     


     
     /**
      *  Set's the parameter to be edited by this editor. The editor is
      *  updated with the name of the parameter as well as the widget
      *  component value. It attempts to use the Constraint name if
      *  different from the parameter and present, else uses the
      *  parameter name. This function actually just calls
      *  removeWidget() then addWidget() then setWidgetObject().
      */
     public void setParameter( ParameterAPI model ) {
    	 this.model = model;
    	 // create params for value and weight
    	 createValueAndWeightParams();
    	 // create param editors 
    	 createParamEditors();
    	 this.setLayout(GBL);
    	 // add editors to the GUI
    	 //JPanel panel = new JPanel(new GridBagLayout());
    	  this.titledBorder1.setTitle(model.getName());
    	 //panel.add(new JLabel(model.getName()), new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0, 
    		//	 GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );	 
    	  widgetPanel.add(this.valueParameterEditor, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0, 
    			 GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    	 widgetPanel.add(this.weightParameterEditor, new GridBagConstraints( 1, 0, 1, 1, 1.0, 1.0, 
    			 GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    	 widgetPanel.setMinimumSize(PANEL_DIM);
    	 widgetPanel.setPreferredSize(PANEL_DIM);
    	 //add(panel,  new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
    		//        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    	
     }

    /**
     * Create parameter editors
     *
     */ 
    private void createParamEditors() {
    	try {
			valueParameterEditor = ParameterEditorFactory.getEditor(this.valueParameter);
			weightParameterEditor = ParameterEditorFactory.getEditor(this.weightParameter);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    /**
     * It enables/disables the editor according to whether user is allowed to
     * fill in the values. THIS METHOD NEEDS TO BE OVERRIDDEN FOR COMPLEX ParameterEditors
     */
    public void setEnabled(boolean isEnabled) {
    	valueParameterEditor.setEnabled(isEnabled);
    	weightParameterEditor.setEnabled(isEnabled);
    }
 
     /**
      * Create parameters for value and weight
      *
      */
	private void createValueAndWeightParams() {
		// make parameter for value
		 DoubleValueWeightConstraint constraint = (DoubleValueWeightConstraint)model.getConstraint();
    	 DoubleConstraint valConstraint=null, weightConstraint = null;
    	 if(constraint!=null) {
    		 valConstraint = new DoubleConstraint(constraint.getMinVal(), constraint.getMaxVal());
    		 valConstraint.setNullAllowed(true);
    		 weightConstraint = new DoubleConstraint(constraint.getMinWt(), constraint.getMaxWt());
    		 weightConstraint.setNullAllowed(true);
    	 }
    	 valueParameter = new DoubleParameter(VALUE, valConstraint, model.getUnits());
    	 valueParameter.addParameterChangeListener(this);
    	 // make paramter for weight
    	 weightParameter = new DoubleParameter(WEIGHT, weightConstraint);
    	 weightParameter.addParameterChangeListener(this);
    	 // set initial values in value and weight
    	 ValueWeight valueWeight = (ValueWeight)this.model.getValue();
    	 if(valueWeight!=null)  {
    		 valueParameter.setValue(valueWeight.getValue());
    		 weightParameter.setValue(valueWeight.getWeight());
    	 }
	}
	
	/**
	 * This function is called whenever value or weight 
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		ValueWeight valueWeight  = (ValueWeight)this.model.getValue();
		// set the value in the parameter
		if(valueWeight==null) {
			valueWeight = new ValueWeight();
			model.setValue(valueWeight);
		}
		
		// update the parameter value
		if(paramName.equalsIgnoreCase(VALUE)) {
			//set the Value in ValueWeight object
			Double value = (Double)valueParameter.getValue();
			if(value==null) value = new Double(Double.NaN);
			valueWeight.setValue(value.doubleValue());
		} else if(paramName.equalsIgnoreCase(WEIGHT)) {
			//set the weight in ValueWeight object
			Double weight = (Double)weightParameter.getValue();
			if(weight==null) weight = new Double(Double.NaN);
			valueWeight.setWeight(weight.doubleValue());
		}
	}
	
}
