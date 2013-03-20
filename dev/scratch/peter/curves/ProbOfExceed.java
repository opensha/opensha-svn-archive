package scratch.peter.curves;

import org.opensha.commons.data.function.DiscretizedFunc;

public enum ProbOfExceed {

	PE2IN50(0.000404),
	PE5IN50(0.001026),
	PE10IN50(0.002107),
	PE40IN50(0.010217);
	
	private double annualRate;
	private ProbOfExceed(double annualRate) {
		this.annualRate = annualRate;
	}
	
	public double annualRate() {
		return annualRate;
	}
	
	public static double get(DiscretizedFunc f, ProbOfExceed pe) {
		return f.getFirstInterpolatedX_inLogXLogYDomain(pe.annualRate());
	}
}
