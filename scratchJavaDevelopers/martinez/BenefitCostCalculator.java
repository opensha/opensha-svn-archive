package scratchJavaDevelopers.martinez;

/**
 * <strong>Title:</strong> BenefitCostCalculator<br />
 * <strong>Description:</strong> Calculates Benefit, Cost, and the Benefit-Cost Ratio (BCR)
 * of a structure given a current and retrofitted cost/expected annualized loss.  Use this in
 * conjunction with the BenefitCostBean to gather information required.
 * 
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 * @author Keith Porter
 */
public class BenefitCostCalculator {
	private double EAL0;
	private double EAL1;
	private double netCost;
	private double rate;
	private double years;
	
	////////////////////////////////////////////////////////////////////////////////
	//                                Constructors                                //
	////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new Benefit Cost Calculator and sets all parameters equal to
	 * zero.  You must then set the values using the get/set methods else an
	 * exception will be thrown upon attempt to calculate BCR.
	 */
	public BenefitCostCalculator() {
		this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	}
	
	/**
	 * Creates a new Benefit Cost Calculator with the given parameters.
	 * 
	 * @param eal0 The EAL for the Current Structure.
	 * @param eal1 The EAL for the Retrofitted Structure.
	 * @param rate The Discout Rate to apply.
	 * @param years The number of years to calculate for.
	 * @param cost0 The cost of building the structure "As-Is".
	 * @param cost1 The cost of building the structure "What-If".
	 */
	public BenefitCostCalculator(double eal0, double eal1, double rate,
			double years, double cost0, double cost1) {
		this.EAL0 = eal0;
		this.EAL1 = eal1;
		this.netCost = cost1 - cost0;
		this.rate = rate;
		this.years = years;
	}
	
	public BenefitCostCalculator(double eal0, double eal1, double rate,
			double years, double netCost) {
		this.EAL0 = eal0;
		this.EAL1 = eal1;
		this.netCost = netCost;
		this.rate = rate;
		this.years = years;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                               Public Functions                             //
	////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Computes the benefit of retrofitting the structure by using the current values
	 * for EAL0, EAL1, rate, and years.
	 * 
	 * @return The benefit of retrofitting the structure.
	 */
	public double computeBenefit() {
		double diff = EAL0 - EAL1;
		double numer = (1 - Math.exp( (0-(rate*years)) ));
		double answer = diff * (numer / rate);
		return answer;
	}
	
	/**
	 * Computes the cost of retrofitting the structure by using the current values for
	 * InitialCost and RetroCost.
	 * 
	 * @return The cost of retrofitting the structure.
	 */
	public double computeCost() {
		return netCost;
	}
	
	/**
	 * Computes the Benefit Cost Ration of retrofitting the structure by using the current
	 * objects parameters.
	 * 
	 * @return The BCR of retrofitting.
	 */
	public double computeBCR() {
		return (computeBenefit() / computeCost());
	}
	////////////////////////////////////////////////////////////////////////////////
	//                   Static Accessors to Calculation Methods                  //
	////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Computes the benefit for retrofitted a structure for the given EAL values over <code>years</code>
	 * at <code>rate</code>.
	 * 
	 * @param eal0 The Expected Annualized Loss for the structure for the current construction conditions.
	 * @param eal1 The Expexted Annualized Loss for the structure for the retrofitted construction conditions.
	 * @param rate The Discount Rate to use for calculations.
	 * @param years The number of years to calculate over.
	 */
	public static double computeBenefit(double eal0, double eal1, double rate, double years) {
		BenefitCostCalculator static_calc = new BenefitCostCalculator(eal0, eal1, rate, years, 0.0, 0.0);
		return static_calc.computeBenefit();
	}
	
	/**
	 * Computes the cost of retrofitting a structure.
	 * 
	 * @param cost0 The cost of building the structure "As-Is".
	 * @param cost1 The cost of building the structure "What-If"
	 * @return The difference between cost1 and cost0.
	 */
	public static double computeCost(double cost0, double cost1) {
		return (cost1 - cost0);
	}
	
	/**
	 * Computes the Benefit Cost Ratio of retrofitting a structure.
	 * 
	 * @param eal0 The EAL for the Current Structure.
	 * @param eal1 The EAL for the Retrofitted Structure.
	 * @param rate The Discout Rate to apply.
	 * @param years The number of years to calculate for.
	 * @param cost0 The cost of building the structure "As-Is".
	 * @param cost1 The cost of building the structure "What-If".
	 * @return The Benefit-Cost ratio for the given parameters.
	 */
	public static double computeBCR(double eal0, double eal1, double rate,
			double years, double cost0, double cost1) {
		BenefitCostCalculator static_calc = new BenefitCostCalculator(eal0, eal1, rate, years, cost0, cost1);
		return ( static_calc.computeBenefit() / static_calc.computeCost() );
	}

	
	////////////////////          Setters          ////////////////////
	public void setNetCost(double netCost) {this.netCost = netCost;}
	public void setNetCost(double cost0, double cost1) {this.netCost = cost1 - cost0;}
	public void setInitialEAL(double eal0) {EAL0 = eal0;}
	public void setRetroEAL(double eal1) {EAL1 = eal1;}
	public void setRate(double rate) {this.rate = rate;}
	public void setYears(double years) {this.years = years;}

	////////////////////          Getters          ////////////////////
	public double getNetCost() {return netCost;}
	public double getInitialEAL() {return EAL0;}
	public double getRetroEAL() {return EAL1;}
	public double getRate() {return rate;}
	public double getYears() {return years;}
}
