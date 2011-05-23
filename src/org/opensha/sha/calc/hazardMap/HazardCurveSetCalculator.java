package org.opensha.sha.calc.hazardMap;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.components.CalculationSettings;
import org.opensha.sha.calc.hazardMap.components.CurveMetadata;
import org.opensha.sha.calc.hazardMap.components.CurveResultsArchiver;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.TRTUtils;
import org.opensha.sha.util.TectonicRegionType;

/**
 * This class calculates a set of hazard curves, typically for use in Condor.
 * 
 * @author kevin
 *
 */
public class HazardCurveSetCalculator {
	
	private EqkRupForecastAPI erf;
	private List<HashMap<TectonicRegionType, ScalarIMR>> imrMaps;
	private List<Parameter<Double>> imts;
	private CurveResultsArchiver archiver;
	private CalculationSettings calcSettings;
	private HazardCurveCalculator calc;
	
	public HazardCurveSetCalculator(EqkRupForecastAPI erf,
			List<HashMap<TectonicRegionType, ScalarIMR>> imrMaps,
			CurveResultsArchiver archiver,
			CalculationSettings calcSettings) {
		this(erf, imrMaps, null, archiver, calcSettings);
	}
	
	public HazardCurveSetCalculator(EqkRupForecastAPI erf,
			List<HashMap<TectonicRegionType, ScalarIMR>> imrMaps,
			List<Parameter<Double>> imts,
			CurveResultsArchiver archiver,
			CalculationSettings calcSettings) {
		this.erf = erf;
		this.imrMaps = imrMaps;
		this.imts = imts;
		this.archiver = archiver;
		this.calcSettings = calcSettings;
		
		if (imts != null && imts.size() != imrMaps.size())
			throw new IllegalArgumentException("If IMTs are specified for each IMR map, there must me exactly one" +
					" for every IMR map.");
		
		try {
			this.calc = new HazardCurveCalculator();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		erf.updateForecast();
	}
	
	public void calculateCurves(List<Site> sites) throws IOException {
		System.out.println("Calculating " + sites.size() + " hazard curves");
		System.out.println("ERF: " + erf.getName());
		System.out.println("Num IMR Maps: " + imrMaps.size());
		calc.setMaxSourceDistance(calcSettings.getMaxSourceDistance());
		System.out.println("Max Source Cutoff: " + calcSettings.getMaxSourceDistance());
		// looop over all sites
		int siteCount = 0;
		for (Site site : sites) {
			siteCount++;
			if (siteCount % 10 == 0)
				System.gc();
			int imrMapCount = 0;
			for (HashMap<TectonicRegionType, ScalarIMR> imrMap : imrMaps) {
				if (imts != null) {
					// if a different IMT has been specified for each imr map then we must set it
					Parameter<Double> newIMT = imts.get(imrMapCount);
//					System.out.println("Setting IMT to " + newIMT.getName());
					for (TectonicRegionType trt : imrMap.keySet()) {
						ScalarIMR imr = imrMap.get(trt);
						imr.setIntensityMeasure(newIMT.getName());
						Parameter<Double> imt = (Parameter<Double>) imr.getIntensityMeasure();
						ListIterator<Parameter<?>> it = newIMT.getIndependentParametersIterator();
						while (it.hasNext()) {
							Parameter<?> depParam = it.next();
							imt.getIndependentParameter(depParam.getName()).setValue(depParam.getValue());
						}
					}
				}
				imrMapCount++;
				Parameter<Double> imtParam =
					(Parameter<Double>) TRTUtils.getFirstIMR(imrMap).getIntensityMeasure();
				String imt = imtParam.getName();
				String imtMeta = imtParam.getName();
				if (imtParam instanceof SA_Param) {
					imtMeta += " (Period: "+SA_Param.getPeriodInSA_Param(imtParam)+" sec)";
				}
				System.out.println("Calculating curve(s) for site " + siteCount + "/" + sites.size()
						+ " IMR Map " + imrMapCount + "/" + imrMaps.size() + " IMT: " + imtMeta);
				CurveMetadata meta = new CurveMetadata(site, imrMap, "imrs" + imrMapCount);
				if (archiver.isCurveCalculated(meta, calcSettings.getXValues(imt))) {
					System.out.println("Curve already calculated, skipping...");
					continue;
				}
				ArbitrarilyDiscretizedFunc calcFunction;
				boolean logSpace = calcSettings.isCalcInLogSpace();
				if (logSpace)
					calcFunction = getLogFunction(calcSettings.getXValues(imt));
				else
					calcFunction = calcSettings.getXValues(imt).deepClone();
				long curveStart = System.currentTimeMillis();
				System.out.println("Calculating Hazard Curve. timestamp=" + curveStart);
				// actually calculate the curve from the log hazard function, site, IMR, and ERF
				calc.getHazardCurve(calcFunction,site,imrMap,erf);
				long curveEnd = System.currentTimeMillis();
				float curveSecs = (float)(curveEnd - curveStart) / 1000f;
				System.out.println("Calculated a curve! timestamp=" + curveEnd + " ("+curveSecs+" secs)");
				ArbitrarilyDiscretizedFunc hazardCurve;
				if (logSpace)
					hazardCurve = unLogFunction(calcSettings.getXValues(imt), calcFunction);
				else
					hazardCurve = calcFunction;
				// archive the curve;
				archiver.archiveCurve(hazardCurve, meta);
			}
		}
		System.out.println("DONE!");
	}
	
	/**
	 * Takes the log of the X-values of the given function
	 * @param arb
	 * @return A function with points (Log(x), 1)
	 */
	public static ArbitrarilyDiscretizedFunc getLogFunction(DiscretizedFuncAPI arb) {
		ArbitrarilyDiscretizedFunc new_func = new ArbitrarilyDiscretizedFunc();
		// TODO: we need to check if the log should be taken for all IMTs GEM will be using!
		// take log only if it is PGA, PGV or SA
//		if (this.xLogFlag) {
			for (int i = 0; i < arb.getNum(); ++i)
				new_func.set(Math.log(arb.getX(i)), 1);
			return new_func;
//		}
//		else
//			throw new RuntimeException("Unsupported IMT");
	}
	
	public static ArbitrarilyDiscretizedFunc unLogFunction(
			ArbitrarilyDiscretizedFunc oldHazFunc, ArbitrarilyDiscretizedFunc logHazFunction) {
		int numPoints = oldHazFunc.getNum();
		ArbitrarilyDiscretizedFunc hazFunc = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
//		if (this.xLogFlag) {
			for (int i = 0; i < numPoints; ++i) {
				hazFunc.set(oldHazFunc.getX(i), logHazFunction.getY(i));
			}
			return hazFunc;
//		}
//		else
//			throw new RuntimeException("Unsupported IMT");
	}

}
