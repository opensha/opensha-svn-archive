package org.opensha.sha.calc.IM_EventSet.v03.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.commons.data.DataPoint2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetCalc_v3_0_API;
import org.opensha.sha.calc.IM_EventSet.v03.outputImpl.HAZ01Writer;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;

import junit.framework.TestCase;

public class IM_EventSetHazardCurveTest extends TestCase implements IM_EventSetCalc_v3_0_API {
	
	public static final double TOL_PERCENT = 0.05;
	
	File outputDir;
	EqkRupForecastAPI erf;
	ScalarIntensityMeasureRelationshipAPI imr;
	Site site;
	ArrayList<Site> sites;
	ArrayList<ArrayList<SiteDataValue<?>>> sitesData;
	
	String imt = "SA 1.0";

	public IM_EventSetHazardCurveTest(String name) {
		super(name);
		
		outputDir = IM_EventSetTest.getTempDir();
		erf = new Frankel96_AdjustableEqkRupForecast();
		imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		site = new Site(new Location(34d, -118d));
		
		ListIterator<ParameterAPI<?>> it = imr.getSiteParamsIterator();
		while (it.hasNext()) {
			ParameterAPI<?> param = it.next();
			site.addParameter(param);
		}
		sites = new ArrayList<Site>();
		sites.add(site);
		sitesData = new ArrayList<ArrayList<SiteDataValue<?>>>();
		sitesData.add(new ArrayList<SiteDataValue<?>>());
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	private void runHAZ01A() throws IOException {
		HAZ01Writer writer = new HAZ01Writer(this);
		
//		ArrayList<ScalarIntensityMeasureRelationshipAPI> attenRels = new
		System.out.println("Writing HAZ01A files.");
		writer.writeFiles(erf, imr, imt);
		System.out.println("done.");
	}
	
	public void testHazardCurve() throws IOException {
		runHAZ01A();
		String fileName = outputDir.getAbsolutePath() + File.separator + HAZ01Writer.HAZ01A_FILE_NAME;
		ScalarIntensityMeasureRelationshipAPI hIMR = new HAZ01A_FakeAttenRel(fileName);
		EqkRupForecastAPI hERF = new HAZ01A_FakeERF(erf);
		hERF.updateForecast();
		
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		ArbitrarilyDiscretizedFunc hCurve = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
		System.out.println("Calculating IM based curve");
		calc.getHazardCurve(hCurve, site, hIMR, hERF);
		
		ArbitrarilyDiscretizedFunc realCurve = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
		System.out.println("Calculating regular curve");
		calc.getHazardCurve(realCurve, site, hIMR, hERF);
		
		double maxDiff = 0;
		double maxPDiff = 0;
		
		for (int i=0; i<hCurve.getNum(); i++) {
			DataPoint2D hPt = hCurve.get(i);
			DataPoint2D rPt = realCurve.get(i);
			
			assertEquals(hPt.getX(), rPt.getX());
			
			System.out.println("Comparing real point: " + rPt);
			System.out.println("to HAZ01A point: " + hPt);
			
			if (hPt.getY() == 0 && rPt.getY() == 0)
				continue;
			
			double absDiff = Math.abs(hPt.getY() - rPt.getY());
			if (absDiff > maxDiff)
				maxDiff = absDiff;
			double absPDiff = absDiff / hPt.getY() * 100d;
			if (absPDiff > maxPDiff)
				maxPDiff = absPDiff;
			
			boolean success = absPDiff < TOL_PERCENT;
			if (!success) {
				System.out.println("FAIL! absDiff: " + absDiff + ", abs % diff: " + absPDiff);
				System.out.println("HAZ01A pt: " + hPt);
				System.out.println("Expected pt: " + rPt);
			}
			assertTrue(success);
		}
		
		System.out.println("Max Diff: " + maxDiff);
		System.out.println("Max Diff %: " + maxPDiff);
	}

	public int getNumSites() {
		return sites.size();
	}

	public File getOutputDir() {
		return outputDir;
	}

	public OrderedSiteDataProviderList getSiteDataProviders() {
		return null;
	}

	public Location getSiteLocation(int i) {
		return site.getLocation();
	}

	public ArrayList<Site> getSites() {
		return sites;
	}

	public ArrayList<ArrayList<SiteDataValue<?>>> getSitesData() {
		return sitesData;
	}

	public ArrayList<SiteDataValue<?>> getUserSiteDataValues(int i) {
		return sitesData.get(i);
	}

}
