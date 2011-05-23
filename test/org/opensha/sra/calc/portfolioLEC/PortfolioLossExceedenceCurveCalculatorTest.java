package org.opensha.sra.calc.portfolioLEC;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.earthquake.ERFTestSubset;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sra.asset.Asset;
import org.opensha.sra.asset.AssetCategory;
import org.opensha.sra.asset.MonetaryHighLowValue;
import org.opensha.sra.asset.MonetaryValue;
import org.opensha.sra.asset.Portfolio;
import org.opensha.sra.calc.portfolioLEC.PortfolioLossExceedenceCurveCalculator;
import org.opensha.sra.vulnerability.Vulnerability;
import org.opensha.sra.vulnerability.models.servlet.VulnerabilityServletAccessor;

public class PortfolioLossExceedenceCurveCalculatorTest {

	private static EqkRupForecastAPI erf;
	private static ScalarIMR imr;
	private static Portfolio portfolio;
	
	private static boolean smallERF = true;
	
	private static HashMap<ProbEqkRupture, ArbitrarilyDiscretizedFunc> refExceedProbs;
	
	@BeforeClass
	public static void setUp() throws Exception {
		if (smallERF) {
			ERFTestSubset erf = new ERFTestSubset(new Frankel96_AdjustableEqkRupForecast());
			erf.updateForecast();
			erf.includeSource(281);
			erf.includeSource(22);
			erf.includeSource(48);
//			erf.includeSource(179);
//			erf.includeSource(63);
//			erf.includeSource(172);
			erf.includeSource(0);
//			erf.includeSource(1);
//			erf.includeSource(282);
//			erf.includeSource(181);
			erf.includeSource(51);
			PortfolioLossExceedenceCurveCalculatorTest.erf = erf;
		} else {
			Frankel96_AdjustableEqkRupForecast erf = new Frankel96_AdjustableEqkRupForecast();
			erf.updateForecast();
			PortfolioLossExceedenceCurveCalculatorTest.erf = erf;
		}

		
		int rupCount = 0;
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			rupCount += erf.getNumRuptures(sourceID);
		}
		
		System.out.println("num sources: " + erf.getNumSources() + ", num rups: " + rupCount);
		
		imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		
		portfolio = new Portfolio("Test Portfolio");
		
		VulnerabilityServletAccessor accessor = new VulnerabilityServletAccessor();
		
		MonetaryValue value1 = new MonetaryHighLowValue(220000.0, 330000.0, 110000.0, 2007);
		Site site1 = new Site(new Location(34, -118));
		Vulnerability vuln1 = accessor.getVuln("C1H-h-AGR1-DF");
		
		MonetaryValue value2 = new MonetaryHighLowValue(200000.0, 300000.0, 100000.0, 2004);
		Site site2 = new Site(new Location(34.1, -117.9));
		Vulnerability vuln2 = accessor.getVuln("C1H-h-COM10-DF");
		
		ListIterator<Parameter<?>> it = imr.getSiteParamsIterator();
		while (it.hasNext()) {
			Parameter<?> param = it.next();
			site1.addParameter((Parameter)param.clone());
			site2.addParameter((Parameter)param.clone());
		}
		
		Asset asset1 = new Asset(0, "House 1", AssetCategory.BUILDING, value1, vuln1, site1);
		portfolio.add(asset1);
		Asset asset2 = new Asset(1, "House 2", AssetCategory.BUILDING, value2, vuln2, site2);
		portfolio.add(asset2);
		
		// ********** REFERENCE RESULTS FROM KEITH'S SPREADSHEET **********
		ArrayList<ProbEqkRupture> rupList = new ArrayList<ProbEqkRupture>();
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			rupList.addAll(erf.getSource(sourceID).getRuptureList());
		}
		
		refExceedProbs = new HashMap<ProbEqkRupture, ArbitrarilyDiscretizedFunc>();
		
		// TODO: actually use vals from spreadsheet, not my vals
	}
	
	private void testRupResult(PortfolioRuptureResults rupResult, ProbEqkRupture rup) {
		
	}
	
	@Test
	public void testCalc() {
		PortfolioLossExceedenceCurveCalculator calc = new PortfolioLossExceedenceCurveCalculator();
		
		PortfolioRuptureResults[][] rupResults = calc.calculateCurve(imr, erf, portfolio, null);
		
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				PortfolioRuptureResults rupResult = rupResults[sourceID][rupID];
				
				testRupResult(rupResult, rup);
			}
		}
		
		ArbitrarilyDiscretizedFunc curve = calc.calcProbabilityOfExceedanceCurve(rupResults, erf);
		System.out.println(curve);
	}
	
//	public static void write

}
