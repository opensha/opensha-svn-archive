package scratchJavaDevelopers.martinez.beans;

import java.awt.*;
import java.util.EventListener;

import javax.swing.*;

import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;

import scratchJavaDevelopers.martinez.VulnerabilityModels.VulnerabilityModel;


public class BenefitCostBean extends GuiBeanAPI {
	/** Request the Current structure conditions **/
	public static final int CURRENT = 0;
	/** Request the "What-If" structure conditions **/
	public static final int RETRO = 1;
	
	private String description = "";
	private double discountRate = 0.0;
	private double designLife = 0.0;

	private static final String DESC_PARAM = "BCR Description";
	private static final String DISCOUNT_PARAM = "Discount Rate";
	private static final String DESIGN_PARAM = "Design Life";
	
	private StructureDescriptorBean structNow = null;
	private StructureDescriptorBean structRetro = null;
	private EventListener listener =  null;
	private StringParameter descParam = null;
	private DoubleParameter discRateParam = null;
	private DoubleParameter dsgnLifeParam = null;
	
	////////////////////////////////////////////////////////////////////////////////
	//                              Public Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	
	public BenefitCostBean() {
		structNow = new StructureDescriptorBean("Current Construction Conditions");
		structRetro = new StructureDescriptorBean("What-If Construction Conditions");
		listener = new BenefitCostParameterListener();
		
		descParam = new StringParameter(DESC_PARAM, "Describe this BCR Action");
		descParam.addParameterChangeListener((ParameterChangeListener) listener);
		descParam.addParameterChangeFailListener((ParameterChangeFailListener) listener);
		
		discRateParam = new DoubleParameter(DISCOUNT_PARAM, 0.0, 200.0, "%");
		discRateParam.addParameterChangeListener((ParameterChangeListener) listener);
		discRateParam.addParameterChangeFailListener((ParameterChangeFailListener) listener);
		
		dsgnLifeParam = new DoubleParameter(DESIGN_PARAM, 0.0, 10E+5, "Years");
		dsgnLifeParam.addParameterChangeListener((ParameterChangeListener) listener);
		dsgnLifeParam.addParameterChangeFailListener((ParameterChangeFailListener) listener);
	}

	public String getDescription() { return description; }
	public double getDiscountRate() { return discountRate; }
	public double getDesignLife() { return designLife; }
	
	public VulnerabilityModel getCurrentVulnModel() { return getVulnModel(CURRENT); }
	public double getCurrentInitialCost() { return getInitialCost(CURRENT); }
	public double getCurrentReplaceCost() { return getReplaceCost(CURRENT); }
	
	public VulnerabilityModel getRetroVulnModel() { return getVulnModel(RETRO); }
	public double getRetroInitialCost() { return getInitialCost(RETRO); }
	public double getRetroReplaceCost() { return getReplaceCost(RETRO); }
	
	/**
	 * @param design One of CURRENT or RETRO depending on which Vulnerability Model
	 * is of interest.
	 * @return The Vulnerability Model for the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public VulnerabilityModel getVulnModel(int design) {
		if(design == CURRENT)
			return structNow.getVulnerabilityModel();
		else if (design == RETRO)
			return structRetro.getVulnerabilityModel();
		else
			throw new IllegalArgumentException("The given design is not currently supported.");
	}
	
	/**
	 * @param design One of CURRENT or RETRO depending on which initial cost if of interest.
	 * @return The initial cost of construction either under the current construction conditions,
	 * or that of the "what-if" retrofitted conditions depeding on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public double getInitialCost(int design) {
		if(design == CURRENT)
			return structNow.getInitialCost();
		else if (design == RETRO)
			return structRetro.getInitialCost();
		else
			throw new IllegalArgumentException("The given design is not currently supported.");
	}
	
	/**
	 * @param design One of CURRENT or RETRO depending on which replacement cost
	 * is of interest.
	 * @return The expected replacement cost of the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public double getReplaceCost(int design) {
		if(design == CURRENT)
			return structNow.getReplaceCost();
		else if (design == RETRO)
			return structRetro.getReplaceCost();
		else
			throw new IllegalArgumentException("The given design is not currently supported.");
	}

	
	/**
	 * Only used when <code>getIntensityMeasure(design)</code> returns Spectral Acceleration (SA).
	 * @param design One of CURRENT or RETRO depending on which IMT period is of interest.
	 * @return The Vulnerability Model's IMT period for the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public double getIntensityMeasurePeriod(int design) {
		if(design == CURRENT) {
			return structNow.getVulnerabilityModel().getPeriod();
		} else if (design == RETRO) {
			return structRetro.getVulnerabilityModel().getPeriod();
		} else {
			throw new IllegalArgumentException("The given design is not currently supported");
		}
	}
	
	/**
	 * @param design One of CURRENT or RETRO depending on which Vulnerability Model
	 * is of interest.
	 * @return A template Hazard Curve (IML values) for the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public DiscretizedFuncAPI getSupportedIMLevels(int design) {
		if(design == CURRENT)
			return structNow.getVulnerabilityModel().getHazardTemplate();
		else if (design == RETRO)
			return structRetro.getVulnerabilityModel().getHazardTemplate();
		else
			throw new IllegalArgumentException("The given design is not currently supported");
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                     Minimum Functions to Extend GuiBeanAPI                 //
	////////////////////////////////////////////////////////////////////////////////
	@Override
	/**
	 * See the general contract specified in GuiBeanAPI.
	 */
	public Object getVisualization(int type) {
		if(!isVisualizationSupported(type))
			throw new IllegalArgumentException("That type of visualization is not yet supported.");
		if(type == GuiBeanAPI.APPLICATION)
			return getApplicationVisualization();
		return null;
	}
	@Override
	/**
	 * See the general contract specified in GuiBeanAPI.
	 */
	public String getVisualizationClassName(int type) {
		if(type == GuiBeanAPI.APPLICATION)
			return "javax.swing.JPanel";
		else
			return "";
	}
	@Override
	/**
	 * See the general contract specified in GuiBeanAPI.
	 */
	public boolean isVisualizationSupported(int type) {
		return type == GuiBeanAPI.APPLICATION;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                             Private Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	private JPanel getApplicationVisualization() {
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add((JComponent) structNow.getVisualization(APPLICATION), new GridBagConstraints(
				0, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 2, 2)
		);
		panel.add( (JComponent) structRetro.getVisualization(APPLICATION), new GridBagConstraints(
				1, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 2, 2)
		);
		try {
			panel.add((JComponent) new DoubleParameterEditor(discRateParam), new GridBagConstraints(
					0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2)
			);
			panel.add((JComponent) new DoubleParameterEditor(dsgnLifeParam), new GridBagConstraints(
					1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2)
			);
			panel.add((JComponent) new StringParameterEditor(descParam), new GridBagConstraints(
					0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2)
			);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		panel.setPreferredSize(new Dimension(480, 500));
		panel.setMinimumSize(new Dimension(200, 200));
		panel.setMaximumSize(new Dimension(10000, 10000));
		panel.setSize(panel.getPreferredSize());
		return panel;
	}
	
	private void handleDescriptionChangeEvent(ParameterChangeEvent event) {
		description = (String) event.getNewValue();
	}
	
	private void handleDiscountChangeEvent(ParameterChangeEvent event) {
		discountRate = (Double) event.getNewValue();
	}
	
	private void handleDesignChangeEvent(ParameterChangeEvent event) {
		designLife = (Double) event.getNewValue();
	}
	
	private class BenefitCostParameterListener implements ParameterChangeListener, ParameterChangeFailListener {

		public void parameterChange(ParameterChangeEvent event) {
			if(DESC_PARAM.equals(event.getParameterName()))
				handleDescriptionChangeEvent(event);
			else if(DISCOUNT_PARAM.equals(event.getParameterName()))
				handleDiscountChangeEvent(event);
			else if(DESIGN_PARAM.equals(event.getParameterName()))
				handleDesignChangeEvent(event);
		}

		public void parameterChangeFailed(ParameterChangeFailEvent event) {
			String message = "The given value of " + event.getBadValue() + " is out of range.";
			JOptionPane.showMessageDialog(null, message, "Failed to Change Value", JOptionPane.ERROR_MESSAGE);
		}
		
	}

}
