package scratchJavaDevelopers.martinez;

import java.awt.*;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.param.*;
import org.opensha.param.editor.*;

public class BenefitCostBean extends GuiBeanAPI {
	/** Request the Current structure conditions **/
	public static final int CURRENT = 0;
	/** Request the "What-If" structure conditions **/
	public static final int RETRO = 1;
	
	private String description = "";
	private double discountRate = 0.0;
	private double designLife = 0.0;
	private double replacementCostNow = 0.0;
	private double replacementCostRetro = 0.0;
	private double initialCostNow = 0.0;
	private double initialCostRetro = 0.0;
	private VulnerabilityModel vulnModelNow = null;
	private VulnerabilityModel vulnModelRetro = null;

	private StructureDescriptorBean structNow = null;
	private StructureDescriptorBean structRetro = null;
	private StringParameter descParam = null;
	private DoubleParameter discRateParam = null;
	private DoubleParameter dsgnLifeParam = null;
	
	
	////////////////////////////////////////////////////////////////////////////////
	//                              Public Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	
	public BenefitCostBean() {
		structNow = new StructureDescriptorBean("Current Construction Conditions");
		structRetro = new StructureDescriptorBean("What-If Construction Conditions");
		descParam = new StringParameter("BCR Description", "Describe this BCR Action");
		discRateParam = new DoubleParameter("Discount Rate", 0.0, 200.0, "%");
		dsgnLifeParam = new DoubleParameter("Design Life", 0.0, 10E+5, "Years");
	}
	
	public String getDescription() {
		return description;
	}
	
	public double getDiscountRate() {
		return discountRate;
	}
	
	public double getDesignLife() {
		return designLife;
	}
	
	/**
	 * @param design One of CURRENT or RETRO depending on which replacement cost
	 * is of interest.
	 * @return The expected replacement cost of the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public double getReplacementCost(int design) {
		if(design == CURRENT)
			return replacementCostNow;
		else if (design == RETRO)
			return replacementCostRetro;
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
			return initialCostNow;
		else if (design == RETRO)
			return initialCostRetro;
		else
			throw new IllegalArgumentException("The given design is not currently supported.");
	}
	
	/**
	 * @param design One of CURRENT or RETRO depending on which Vulnerability Model
	 * is of interest.
	 * @return The Vulnerability Model for the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public VulnerabilityModel getVulnerabilityModel(int design) {
		if(design == CURRENT)
			return vulnModelNow;
		else if (design == RETRO)
			return vulnModelRetro;
		else
			throw new IllegalArgumentException("The given design is not currently supported");
	}
	
	/**
	 * @param design One of CURRENT or RETRO depending on which IMT is of interest.
	 * @return The Vulnerability Model's IMT for the structure either under
	 * current construction conditions, or that of the "what-if" retrofitted conditions
	 * depending on the value of <code>design</code>.
	 * @throws IllegalArgumentException if the given <code>design</code> is not supported.
	 */
	public String getIntensityMeasure(int design) {
		if(design == CURRENT)
			return vulnModelNow.getIMT();
		else if (design == RETRO)
			return vulnModelRetro.getIMT();
		else
			throw new IllegalArgumentException("The given design is not currently supported");
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
			return vulnModelNow.getPeriod();
		} else if (design == RETRO) {
			return vulnModelRetro.getPeriod();
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
			return vulnModelNow.getHazardTemplate();
		else if (design == RETRO)
			return vulnModelRetro.getHazardTemplate();
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
}
