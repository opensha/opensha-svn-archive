package scratch.peter.curves;

public enum ProbOfExceed {

	PE2IN50(0.000404),
	PE5IN50(0.001026),
	PE10IN50(0.02107);
	
	private double annualRate;
	private ProbOfExceed(double annualRate) {
		this.annualRate = annualRate;
	}
	
	public double annualRate() {
		return annualRate;
	}
}
