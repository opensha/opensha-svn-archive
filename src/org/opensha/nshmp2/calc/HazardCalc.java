package org.opensha.nshmp2.calc;

import static org.opensha.nshmp2.util.SourceType.CLUSTER;
import static org.opensha.nshmp2.util.SourceType.GRIDDED;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.nshmp2.erf.NSHMP_ListERF;
import org.opensha.nshmp2.erf.source.ClusterERF;
import org.opensha.nshmp2.erf.source.ClusterSource;
import org.opensha.nshmp2.erf.source.FaultSource;
import org.opensha.nshmp2.erf.source.GridERF;
import org.opensha.nshmp2.erf.source.NSHMP_ERF;
import org.opensha.nshmp2.imr.NSHMP08_CEUS_Grid;
import org.opensha.nshmp2.imr.NSHMP08_SUB_SlabGrid;
import org.opensha.nshmp2.imr.NSHMP08_WUS_Grid;
import org.opensha.nshmp2.util.FocalMech;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.SiteTypeParam;
import org.opensha.nshmp2.util.SourceIMR;
import org.opensha.nshmp2.util.SourceType;
import org.opensha.nshmp2.util.Utils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.faultSurface.utils.PtSrcDistCorr;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.EqkRuptureParams.FaultTypeParam;
import org.opensha.sha.imr.param.EqkRuptureParams.RupTopDepthParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Standalone calculator class for NSHMP_ListERFs. Assumes Poissonian.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalc implements Callable<HazardResult> {

	private NSHMP_ListERF erfList;
	private Site site;
	private Period period;
	private DiscretizedFunc curve;
	private Map<SourceIMR, ScalarIMR> imrMap;
	
	private HazardCurveCalculator calc;
		
	/**
	 * Creates a new calculation instance. Currently, NSHMP calculations use a
	 * single shared reference to a threadsafe ERF. Note, however, that every
	 * calc instance should be created with a new imrMap, as IMRs are not
	 * threadsafe.
	 * 
	 * @param erfList list to use
	 * @param site
	 * @param period
	 * @return a calculation instance
	 */
	public static HazardCalc create(NSHMP_ListERF erfList, Site site, Period period) {
		HazardCalc hc = new HazardCalc();
		hc.erfList = erfList;
		hc.site = site;
		hc.period = period;
		return hc;
	}
	
	@Override
	public HazardResult call() {
		init();
		DiscretizedFunc utilFunc = period.getFunction();
		for (NSHMP_ERF erf : erfList.asFilteredIterable(site.getLocation())) {
			
			// if (!erf.getBounds().contains(site.getLocation())) continue;
			
			ScalarIMR imr = imrMap.get(erf.getSourceIMR());
			SourceType st = erf.getSourceType();
			
			if (st == GRIDDED) initGridIMR(imr, (GridERF) erf); // set tables
			
			if (st == CLUSTER) {
				utilFunc = clusterCalc(utilFunc, site, imr, (ClusterERF) erf);
			} else {
				utilFunc = basicCalc(calc, utilFunc, site, imr, erf);
			}

			utilFunc.scale(erf.getSourceWeight());
			System.out.println(erf.getName());
			System.out.println(utilFunc);
			System.out.println("-------------");
			Utils.addFunc(curve, utilFunc);
		}
		return new HazardResult(curve, site.getLocation());
	}
	
	private void init() {
		initSite(site); // ensures required parameters are set
		imrMap = SourceIMR.map(period); // map inited to period of interest
		curve = period.getFunction();
		Utils.zeroFunc(curve);
		calc = new HazardCurveCalculator();
		calc.setPtSrcDistCorrType(PtSrcDistCorr.Type.NSHMP08);
		// set site
		for (ScalarIMR imr : imrMap.values()) {
			imr.setSite(site);
		}
	}
	
	private static void initGridIMR(ScalarIMR imr, final GridERF erf) {
		
		if (imr instanceof NSHMP08_CEUS_Grid) {
			NSHMP08_CEUS_Grid ceusIMR = (NSHMP08_CEUS_Grid) imr;
			ceusIMR.setTable(erf.getFaultCode());
			
		} else if (imr instanceof NSHMP08_WUS_Grid) {
			NSHMP08_WUS_Grid wusIMR = (NSHMP08_WUS_Grid) imr;
			
			// turn off uncertainty for testing because NSHMP stores
			// epi+ and epi- separately from mean
			wusIMR.getParameter(NSHMP08_WUS_Grid.IMR_UNCERT_PARAM_NAME).setValue(false);

			// set fault type ; kinda KLUDGY
			Map<FocalMech, Double> mechMap = erf.getFocalMechs();
			double sWt = mechMap.get(FocalMech.STRIKE_SLIP);
			double rWt = mechMap.get(FocalMech.REVERSE);
			double nWt = mechMap.get(FocalMech.NORMAL);
			String fltType = (rWt > 0) ? "Reverse" : (nWt > 0) ? "Normal"
				: "Strike-Slip";
			wusIMR.getParameter(FaultTypeParam.NAME).setValue(fltType);
			wusIMR.setGridHybrid(sWt > 0 && sWt < 1);
			
			// set mag dependent depth function
			Function<Double, Double> func = new Function<Double, Double>() {
				double[] depths = erf.getDepths();
				@Override public Double apply(Double mag) {
					return (mag < 6.5) ? depths[0] : depths[1];
				}
			};
			wusIMR.setTable(func);

		} else if (imr instanceof NSHMP08_SUB_SlabGrid) {
			NSHMP08_SUB_SlabGrid slabIMR = (NSHMP08_SUB_SlabGrid) imr;
			slabIMR.getParameter(RupTopDepthParam.NAME).setValue(erf.getDepths()[0]);
			slabIMR.setTable();
			
		} 
//		else {
//			System.out.println(" imr skipped" + imr);
//			// allow this to pass through if no imr match as fixed strike
//			// sources will be marked as gridded but use fault based erfs
//		}
	}
	
	private static DiscretizedFunc basicCalc(
			HazardCurveCalculator c,
			DiscretizedFunc f,
			Site s,
			ScalarIMR imr,
			NSHMP_ERF erf) {
		
		c.getAdjustableParams().getParameter(MaxDistanceParam.NAME)
			.setValue(erf.getMaxDistance());
		c.getHazardCurve(f, s, imr, erf);


		// convert to annual rate
		for (Point2D p : f) {
			f.set(p.getX(), NSHMP_Utils.probToRate(p.getY(), 1));
		}
		return f;
	}
	
	
	private static DiscretizedFunc clusterCalc(
			DiscretizedFunc f, 
			Site s,
			ScalarIMR imr, 
			ClusterERF erf) {
		
		double maxDistance = erf.getMaxDistance();
		int clustTotal = erf.getNumSources();
		int clustCount = 0;
		Utils.zeroFunc(f); //zero for aggregating results
		DiscretizedFunc peFunc = (ArbitrarilyDiscretizedFunc) f.deepClone();
		
		for (ClusterSource cs : erf.getSources()) { // geom variants
//			System.out.println(cs.getName());
			
			// apply distance cutoff to source
			double dist = cs.getMinDistance(s);
			if (dist > maxDistance) {
				continue;
			}

			// assemble list of PE curves for each cluster segment
			List<DiscretizedFunc> fltFuncList = Lists.newArrayList();

			for (FaultSource fs : cs.getFaultSources()) { // segments
				DiscretizedFunc fltFunc = (ArbitrarilyDiscretizedFunc) peFunc.deepClone();
				Utils.zeroFunc(fltFunc);
				// agregate weighted PE curves for mags on each segment
				for (int i=0; i < fs.getNumRuptures(); i++) { // mag variants
					imr.setEqkRupture(fs.getRupture(i));
					imr.getExceedProbabilities(peFunc);
					double weight = fs.getMFDs().get(i).getY(0) * cs.getRate();
					peFunc.scale(weight);
					Utils.addFunc(fltFunc, peFunc);
				} // end mag
//				System.out.println("fFt: " + fltFunc);
				fltFuncList.add(fltFunc);
			} // end segments
			
			// compute joint PE, scale by geom weight, scale by rate (1/RP),
			// and add to final result
			DiscretizedFunc fOut = calcClusterExceedProb(fltFuncList);
			double rateAndWeight = cs.getWeight() / cs.getRate();
			fOut.scale(rateAndWeight);
			Utils.addFunc(f, fOut);
		} // end geom
		return f;
	}
	
	/*
	 * Computes joint probability of exceedence given the occurrence of a
	 * cluster of events: [1 - [(1-PE1) * (1-PE2) * ...]]. The probability of
	 * exceedance of each individual event is given in the supplied curves.
	 * WARNING: Method modifies curves in place and returns result in the first
	 * supplied curve.s
	 */
	private static DiscretizedFunc calcClusterExceedProb(List<DiscretizedFunc> fList) {
		DiscretizedFunc firstFunc = fList.get(0);
		// set all to complement and multiply into first
		for (int i=0; i < fList.size(); i++) {
			DiscretizedFunc f = fList.get(i);
			Utils.complementFunc(f);
			if (i == 0) continue;
			Utils.multiplyFunc(firstFunc, f);
		}
		Utils.complementFunc(firstFunc);
		return firstFunc;
	}
	
	private void initSite(Site s) {
		
		// CY AS
		DepthTo1pt0kmPerSecParam d10p = new DepthTo1pt0kmPerSecParam(null,
			0, 1000, true);
		d10p.setValueAsDefault();
		s.addParameter(d10p);
		// CB
		DepthTo2pt5kmPerSecParam d25p = new DepthTo2pt5kmPerSecParam(null,
			0, 1000, true);
		d25p.setValueAsDefault();
		s.addParameter(d25p);
		// all
		Vs30_Param vs30p = new Vs30_Param(760);
		vs30p.setValueAsDefault();
		s.addParameter(vs30p);
		// AS CY
		Vs30_TypeParam vs30tp = new Vs30_TypeParam();
		vs30tp.setValueAsDefault();
		s.addParameter(vs30tp);
		
		// CEUS only (TODO imrs need to be changed to accept vs value)
		SiteTypeParam siteTypeParam = new SiteTypeParam();
		s.addParameter(siteTypeParam);
	}

	private static void throwCalcException(Exception e, int srcIdx, int rupIdx,
			ERF erf, ScalarIMR imr, Site s, DiscretizedFunc f) {
		// TODO this should be logged
		StringBuffer sb = new StringBuffer();
		sb.append("Calculator error in: ");
		sb.append("Source [").append(srcIdx).append("] ");
		sb.append("Rupture [").append(rupIdx).append("]");
		sb.append(IOUtils.LINE_SEPARATOR);
		sb.append("  ERF: " + erf.getName());
		sb.append(IOUtils.LINE_SEPARATOR);
		sb.append("  IMR: " + imr.getName());
		sb.append(IOUtils.LINE_SEPARATOR);
		sb.append("  Site: " + s);
		sb.append(IOUtils.LINE_SEPARATOR);
		sb.append("  Curve: " + f);
		sb.append(IOUtils.LINE_SEPARATOR);
		System.err.println(sb.toString());
		Throwables.propagate(e);
	}
	
	public static void main(String[] args) {
		Stopwatch sw = new Stopwatch();
		sw.start();
		
		TimeUnit tu = TimeUnit.MILLISECONDS;
//		WUS_ERF erf = new WUS_ERF();
		NSHMP2008 erf = NSHMP2008.create();
//		NSHMP2008 erf = NSHMP2008.createSingleSource("bFault.gr.in");
		erf.updateForecast();
		System.out.println(erf);
		sw.stop();
		System.out.println("Seconds: " + sw.elapsedTime(tu));
		Period p = Period.GM0P00;

		sw.reset().start();
		Site site = new Site(NEHRP_TestCity.LOS_ANGELES.shiftedLocation());
		HazardCalc hc = HazardCalc.create(erf, site, p);
		HazardResult result = hc.call();
		System.out.println(result.curve());
		sw.stop();
		System.out.println("Seconds: " + sw.elapsedTime(tu));
		
//		Set<NEHRP_TestCity> cities = EnumSet.of(
//			NEHRP_TestCity.LOS_ANGELES,
//			NEHRP_TestCity.SEATTLE,
//			NEHRP_TestCity.SALT_LAKE_CITY,
//			NEHRP_TestCity.MEMPHIS);
//		
//		for (NEHRP_TestCity city : cities) {
//			sw.reset().start();
//			Site site = new Site(city.shiftedLocation());
//			System.out.println(city.name() + " " + site.getLocation());
//			HazardCalc2 hc = HazardCalc2.create(erf, site, p);
//			HazardCalcResult result = hc.call();
//			System.out.println(result.curve());
//			sw.stop();
//			System.out.println("Time: " + sw.elapsedTime(TimeUnit.SECONDS) + " sec");
//		}
		
		System.exit(0);

	}
	
}
