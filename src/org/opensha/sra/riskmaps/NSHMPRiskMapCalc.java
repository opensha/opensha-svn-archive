package org.opensha.sra.riskmaps;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.EvenlyDiscretizedXYZ_DataSet;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.util.ArrayUtils;
import org.opensha.sra.calc.LossCurveCalculator;
import org.opensha.sra.riskmaps.func.DiscreteInterpExterpFunc;

public class NSHMPRiskMapCalc {
	
	public static final double gridSpacing = 0.05;
	public static final double minLat = 24.6;
	public static final double minLon = -125;
	public static final int ncols = 1201;
	public static final int nrows = 509;
	
	private static boolean interp = true;
	
	private BinaryHazardCurveReader curveReader;
	private DiscreteInterpExterpFunc fragilityCurve;
	
	private static long interpTime = 0;
	private static long calcTime = 0;
	private static long fetchTime = 0;
	
	public NSHMPRiskMapCalc(BinaryHazardCurveReader curveReader, DiscreteInterpExterpFunc fragilityCurve) {
		this.curveReader = curveReader;
		this.fragilityCurve = fragilityCurve;
	}
	
	public static ArbitrarilyDiscretizedFunc[] getInterpolatedCurvesFast(ArbitrarilyDiscretizedFunc hazCurve,
			ArbitrarilyDiscretizedFunc fragilityCurve) {
		long start = System.currentTimeMillis();
		DiscreteInterpExterpFunc interpHazFunc = DiscreteInterpExterpFunc.fromArbDistFunc(hazCurve);
		DiscreteInterpExterpFunc interpFragFunc = DiscreteInterpExterpFunc.fromArbDistFunc(fragilityCurve);
		
		ArbitrarilyDiscretizedFunc interpHazCurve = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc interpFragCurve = new ArbitrarilyDiscretizedFunc();
		double xVals[] = ArrayUtils.boundedMerge(hazCurve.getXVals(), fragilityCurve.getXVals());
		for (double x : xVals) {
			interpHazCurve.set(x, interpHazFunc.valueOf(x));
			interpFragCurve.set(x, interpFragFunc.valueOf(x));
		}
		ArbitrarilyDiscretizedFunc result[]  = { interpHazCurve, interpFragCurve};
		long end = System.currentTimeMillis();
		interpTime += (end - start);
		return result;
	}
	
	private double[] getXVals(ArbitrarilyDiscretizedFunc hazCurve,
			ArbitrarilyDiscretizedFunc fragilityCurve) {
		return getXVals(DiscreteInterpExterpFunc.fromArbDistFunc(hazCurve),
				DiscreteInterpExterpFunc.fromArbDistFunc(fragilityCurve));
	}
	
	private double[] getXVals(DiscreteInterpExterpFunc hazCurve,
			DiscreteInterpExterpFunc fragilityCurve) {
		return ArrayUtils.boundedMerge(hazCurve.getXVals(), fragilityCurve.getXVals());
	}
	
	private ArbitrarilyDiscretizedFunc interp(ArbitrarilyDiscretizedFunc func, double xvals[]) {
		DiscreteInterpExterpFunc dfunc = DiscreteInterpExterpFunc.fromArbDistFunc(func);
		dfunc = interp(dfunc, xvals);
		return getArbDist(dfunc);
	}
	
	private DiscreteInterpExterpFunc interp(DiscreteInterpExterpFunc func, double xvals[]) {
		long start = System.currentTimeMillis();
		double yvals[] = new double[xvals.length];
		for (int i=0; i<xvals.length; i++) {
			double x = xvals[i];
			yvals[i] = func.valueOf(x);
		}
		long end = System.currentTimeMillis();
		interpTime += (end - start);
		return new DiscreteInterpExterpFunc(xvals, yvals);
	}
	
	public static DiscreteInterpExterpFunc[] getInterpolatedCurvesFast(DiscreteInterpExterpFunc hazCurve,
			DiscreteInterpExterpFunc fragilityCurve) {
		long start = System.currentTimeMillis();
		
		double xVals[] = ArrayUtils.boundedMerge(hazCurve.getXVals(), fragilityCurve.getXVals());
		double hazYVals[] = new double[xVals.length];
		double fragYVals[] = new double[xVals.length];
		for (int i=0; i<xVals.length; i++) {
			double x = xVals[i];
//			interpHazCurve.set(x, interpHazFunc.valueOf(x));
//			interpFragCurve.set(x, interpFragFunc.valueOf(x));
			hazYVals[i] = hazCurve.valueOf(x);
			fragYVals[i] = fragilityCurve.valueOf(x);
		}
		DiscreteInterpExterpFunc result[]  = { new DiscreteInterpExterpFunc(xVals, hazYVals),
				new DiscreteInterpExterpFunc(xVals, fragYVals) };
		long end = System.currentTimeMillis();
		interpTime += (end - start);
		return result;
	}
	
	public static ArbitrarilyDiscretizedFunc[] getInterpolatedCurves(ArbitrarilyDiscretizedFunc hazCurve,
			ArbitrarilyDiscretizedFunc fragilityCurve) {
		long start = System.currentTimeMillis();
		ArbitrarilyDiscretizedFunc interpHazCurve = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc interpFragCurve = new ArbitrarilyDiscretizedFunc();
		double xVals[] = ArrayUtils.boundedMerge(hazCurve.getXVals(), fragilityCurve.getXVals());
		for (double x : xVals) {
			interpHazCurve.set(x, hazCurve.getInterpExterpY_inLogYDomain(x));
			interpFragCurve.set(x, fragilityCurve.getInterpExterpY_inLogYDomain(x));
		}
		ArbitrarilyDiscretizedFunc result[]  = { interpHazCurve, interpFragCurve};
		long end = System.currentTimeMillis();
		interpTime += (end - start);
		return result;
	}
	
	private DiscreteInterpExterpFunc getNextCurve() throws Exception {
		long start = System.currentTimeMillis();
		DiscreteInterpExterpFunc hazCurve = curveReader.nextDiscreteCurve();
		long end = System.currentTimeMillis();
		fetchTime += (end - start);
		return hazCurve;
	}
	
	private static void printSingleTime(String desc, long val, long total) {
		double secs = val / 1000d;
		double precent = (double)val / (double)total * 100d;
		System.out.println(desc + ": " + secs + "s (" + precent + " %)");
	}
	
	private static void printTimes() {
		long total = interpTime + calcTime + fetchTime;
		
		printSingleTime("interpolate", interpTime, total);
		printSingleTime("calculate", calcTime, total);
		printSingleTime("fetch", fetchTime, total);
	}
	
	public EvenlyDiscretizedXYZ_DataSet calcRiskMap() throws Exception {
		DiscreteInterpExterpFunc hazCurve = getNextCurve();
		DiscreteInterpExterpFunc fragilityCurve = this.fragilityCurve;
		double loc[];
		
		double xvals[] = getXVals(hazCurve, fragilityCurve);
		DiscreteInterpExterpFunc interpDiscreteFrag = interp(fragilityCurve, xvals);
		ArbitrarilyDiscretizedFunc interpFrag = getArbDist(interpDiscreteFrag);
		
		LossCurveCalculator calc = new LossCurveCalculator();
		
		EvenlyDiscretizedXYZ_DataSet result = new EvenlyDiscretizedXYZ_DataSet(ncols, nrows,
				minLon, minLat, gridSpacing);
		
		int count = 0;
		System.out.println("Starting calc!");
		while (hazCurve != null) {
			loc = curveReader.currentLocationArray();
			
//			if (count % 1000 == 0) {
//				System.out.println("Calculating site " + count);
//				printTimes();
//			}
//			if (count == 10000) {
//				break;
//			}
			
			/*			BEGIN CALC			*/
			
//			System.out.println("site " + count + " hazCurve: " + hazCurve.getNum() + " xVals");
			
			// interpolate the curves
			if (interp) {
//				ArbitrarilyDiscretizedFunc interpCurves[] = getInterpolatedCurves(hazCurve, fragilityCurve);
				hazCurve = interp(hazCurve, xvals);
			}
			
			long calcStart = System.currentTimeMillis();
			double val = calc.getLossExceedance(getArbDist(hazCurve), interpFrag);
			long calcEnd = System.currentTimeMillis();
			calcTime += (calcEnd - calcStart);
			
//			System.out.println("X: " + loc[1] + " Y: " + loc[0] + " Z: " + val);
			
			result.addValue(loc[1], loc[0], val);
			
			count++;
			
			/*			END CALC			*/
			
			hazCurve = getNextCurve();
		}
		
		System.out.println("Calculated " + count + " sites!");
		printTimes();
		
		return result;
	}
	
	public static ArbitrarilyDiscretizedFunc getArbDist(DiscreteInterpExterpFunc func) {
		ArbitrarilyDiscretizedFunc afunc = new ArbitrarilyDiscretizedFunc();
		
		double xvals[] = func.getXVals();
		double yvals[] = func.getYVals();
		for (int i=0; i<xvals.length; i++) {
			afunc.set(xvals[i], yvals[i]);
		}
		
		return afunc;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String curveFile = "/home/kevin/OpenSHA/nico/curve_input_file.bin";
		BinaryHazardCurveReader curveReader = new BinaryHazardCurveReader(curveFile);
//		ArbitrarilyDiscretizedFunc fragilityCurve = new ArbitrarilyDiscretizedFunc();
		
		// temp random fragility function
//		int total = 400;
		int total = 100;
//		int total = 20;
		interp = total != 20;
		double addAmount = 1.0/(total-1d);
		double xVals[] = new double[total];
		double yVals[] = new double[total];
		int cnt = 0;
		for (double x=0.0; x<=1.0; x+=addAmount) {
//			fragilityCurve.set(x, Math.random());
			xVals[cnt] = x;
			yVals[cnt] = Math.random();
			cnt++;
		}
		DiscreteInterpExterpFunc fragilityCurve = new DiscreteInterpExterpFunc(xVals, yVals);
		
		long start = System.currentTimeMillis();
		NSHMPRiskMapCalc calc = new NSHMPRiskMapCalc(curveReader, fragilityCurve);
		
		EvenlyDiscretizedXYZ_DataSet result = calc.calcRiskMap();
		long end = System.currentTimeMillis();
		
		System.out.println("Took " + ((end - start) / 1000d) + " secs");
		
		result.writeXYZBinFile("/home/kevin/OpenSHA/nico/testData");
	}

}
