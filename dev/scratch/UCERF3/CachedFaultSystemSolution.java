package scratch.UCERF3;

import java.util.HashMap;

import org.opensha.sha.gui.infoTools.CalcProgressBar;

/**
 * This class wraps a FaultSystemSolution in to provide caching for lengthly calculations.
 * 
 * @author kevin
 *
 */
public class CachedFaultSystemSolution extends SimpleFaultSystemSolution {
	
	private boolean showProgress;

	/**
	 * Creates a new CachedFaultSystemSolution wrapper around the given solution. Progress bars will
	 * not be shown.
	 * 
	 * @param solution
	 */
	public CachedFaultSystemSolution(FaultSystemSolution solution) {
		this(solution, false);
	}
	
	/**
	 * Creates a new CachedFaultSystemSolution wrapper around the given solution. Progress bars will
	 * be shown on lengthy calculations if <code>showProgress</code> is true.
	 * 
	 * @param solution
	 * @param showProgress
	 */
	public CachedFaultSystemSolution(FaultSystemSolution solution, boolean showProgress) {
		super(solution);
		this.showProgress = showProgress;
	}
	
	/* PARTICIPATION RATES */
	
	HashMap<String, double[]> particRatesCache = new HashMap<String, double[]>();

	@Override
	public synchronized double calcParticRateForSect(int sectIndex, double magLow,
			double magHigh) {
		return calcParticRateForAllSects(magLow, magHigh)[sectIndex];
	}
	
	@Override
	public synchronized double[] calcParticRateForAllSects(double magLow, double magHigh) {
		String key = (float)magLow+"_"+(float)magHigh;
		if (!particRatesCache.containsKey(key)) {
			double[] particRates = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) p = new CalcProgressBar("Fault System Solution", "Calculating Participation Rates");
			for (int i=0; i<particRates.length; i++) {
				if (showProgress) p.updateProgress(i, particRates.length);
				particRates[i] = super.calcParticRateForSect(i, magLow, magHigh);
			}
			if (showProgress) p.dispose();
			particRatesCache.put(key, particRates);
		}
		return particRatesCache.get(key);
	}
	
	/* TOTAL PARTICIPATION RATES */
	
	private double[] totParticRatesCache;

	@Override
	public synchronized double calcTotParticRateForSect(int sectIndex) {
		return calcTotParticRateForAllSects()[sectIndex];
	}

	@Override
	public synchronized double[] calcTotParticRateForAllSects() {
		if (totParticRatesCache == null) {
			totParticRatesCache = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) p = new CalcProgressBar("Fault System Solution", "Calculating Total Participation Rates");
			for (int i=0; i<totParticRatesCache.length; i++) {
				if (showProgress) p.updateProgress(i, totParticRatesCache.length);
				totParticRatesCache[i] = super.calcTotParticRateForSect(i);
			}
			if (showProgress) p.dispose();
		}
		return totParticRatesCache;
	}
	
	/* PALEO-VISIBLE RATES */
	
	private double[] paleoVisibleRatesCache;

	@Override
	public synchronized double calcTotPaleoVisibleRateForSect(int sectIndex) {
		return calcTotPaleoVisibleRateForAllSects()[sectIndex];
	}

	@Override
	public synchronized double[] calcTotPaleoVisibleRateForAllSects() {
		if (paleoVisibleRatesCache == null) {
			paleoVisibleRatesCache = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) p = new CalcProgressBar("Fault System Solution", "Calculating Paleo Visible Rates");
			for (int i=0; i<paleoVisibleRatesCache.length; i++) {
				if (showProgress) p.updateProgress(i, paleoVisibleRatesCache.length);
				paleoVisibleRatesCache[i] = super.calcTotPaleoVisibleRateForSect(i);
			}
			if (showProgress) p.dispose();
		}
		return paleoVisibleRatesCache;
	}
	
	/* SLIP RATES */
	
	private double[] slipRatesCache;

	@Override
	public synchronized double calcSlipRateForSect(int sectIndex) {
		return calcSlipRateForAllSects()[sectIndex];
	}

	@Override
	public synchronized double[] calcSlipRateForAllSects() {
		if (slipRatesCache == null) {
			slipRatesCache = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) p = new CalcProgressBar("Fault System Solution", "Calculating Slip Rates");
			for (int i=0; i<slipRatesCache.length; i++) {
				if (showProgress) p.updateProgress(i, slipRatesCache.length);
				slipRatesCache[i] = super.calcSlipRateForSect(i);
			}
			if (showProgress) p.dispose();
		}
		return slipRatesCache;
	}

}
