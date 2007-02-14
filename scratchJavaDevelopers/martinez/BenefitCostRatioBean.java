package scratchJavaDevelopers.martinez;

import javax.swing.JPanel;

import org.opensha.data.function.DiscretizedFuncAPI;

public class BenefitCostRatioBean extends GuiBeanAPI {
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

	
	////////////////////////////////////////////////////////////////////////////////
	//                              Public Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	
	public BenefitCostRatioBean() {
		
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
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	/**
	 * See the general contract specified in GuiBeanAPI.
	 */
	public boolean isVisualizationSupported(int type) {
		// TODO Auto-generated method stub
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                             Private Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	private JPanel getApplicationVisualization() {
		return null;
	}
}
