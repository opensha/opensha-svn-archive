package scratchJavaDevelopers.martinez;

public class BenefitCostCalculator {
	private double EAL0;
	private double EAL1;
	private double Cost0;
	private double Cost1;
	private double rate;
	private double years;
	
	////////////////////////////////////////////////////////////////////////////////
	//                                Constructors                                //
	////////////////////////////////////////////////////////////////////////////////
	public BenefitCostCalculator() {
		this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	}
	
	public BenefitCostCalculator(double eal0, double eal1, double rate,
			double years, double cost0, double cost1) {
		this.EAL0 = eal0;
		this.EAL1 = eal1;
		this.Cost0 = cost0;
		this.Cost1 = cost1;
		this.rate = rate;
		this.years = years;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                               Public Functions                             //
	////////////////////////////////////////////////////////////////////////////////
	
	public double computeBenefit() {
		double diff = EAL0 - EAL1;
		double numer = (1 - Math.exp( (0-(rate*years)) ));
		double answer = diff * (numer / rate);
		return answer;
	}
	
	public double computeCost() {
		return (Cost1 - Cost0);
	}
	
	public double computeBCR() {
		return (computeBenefit() / computeCost());
	}
	////////////////////////////////////////////////////////////////////////////////
	//                   Static Accessors to Calculation Methods                  //
	////////////////////////////////////////////////////////////////////////////////
	public static double computeBenefit(double eal0, double eal1, double rate, double years) {
		BenefitCostCalculator static_calc = new BenefitCostCalculator(eal0, eal1, rate, years, 0.0, 0.0);
		return static_calc.computeBenefit();
	}
	
	public static double computeCost(double cost0, double cost1) {
		return (cost1 - cost0);
	}
	
	public static double computeBCR(double eal0, double eal1, double rate,
			double years, double cost0, double cost1) {
		BenefitCostCalculator static_calc = new BenefitCostCalculator(eal0, eal1, rate, years, cost0, cost1);
		return ( static_calc.computeBenefit() / static_calc.computeCost() );
	}
}
