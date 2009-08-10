package org.opensha.sha.calc.IM_EventSet.v03.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jfree.chart.ChartUtilities;
import org.opensha.commons.data.DataPoint2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetCalc_v3_0_API;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetOutputWriter;
import org.opensha.sha.calc.IM_EventSet.v03.outputImpl.HAZ01Writer;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.PlotControllerAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

import junit.framework.TestCase;

public class IM_EventSetHazardCurveTest extends TestCase implements IM_EventSetCalc_v3_0_API, GraphPanelAPI, PlotControllerAPI {
	
	public static final double TOL_PERCENT = 0.05;
	
	File outputDir;
	EqkRupForecastAPI erf;
	ScalarIntensityMeasureRelationshipAPI imr;
	Site site;
	ArrayList<Site> sites;
	ArrayList<ArrayList<SiteDataValue<?>>> sitesData;
	GraphPanel gp;
	
	String imt = "SA 1.0";

	public IM_EventSetHazardCurveTest(String name) {
		super(name);
		
		outputDir = IM_EventSetTest.getTempDir();
		erf = new Frankel96_AdjustableEqkRupForecast();
		erf.getAdjustableParameterList()
				.getParameter(Frankel96_AdjustableEqkRupForecast.BACK_SEIS_NAME)
				.setValue(Frankel96_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
		erf.updateForecast();
		imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		IM_EventSetOutputWriter.setIMTFromString(imt, imr);
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
		
		gp = new GraphPanel(this);
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
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		ArbitrarilyDiscretizedFunc realCurve = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
		ArbitrarilyDiscretizedFunc rLogHazFunction = getLogFunction(realCurve);
		System.out.println("IMR Params: " + imr.getAllParamMetadata());
		System.out.println("Calculating regular curve");
		calc.getHazardCurve(rLogHazFunction, site, imr, erf);
		realCurve = unLogFunction(realCurve, rLogHazFunction);
		
		runHAZ01A();
		String fileName = outputDir.getAbsolutePath() + File.separator + HAZ01Writer.HAZ01A_FILE_NAME;
		ScalarIntensityMeasureRelationshipAPI hIMR = new HAZ01A_FakeAttenRel(fileName);
		EqkRupForecastAPI hERF = new HAZ01A_FakeERF(erf);
		hERF.updateForecast();
		
		ArbitrarilyDiscretizedFunc hCurve = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
		System.out.println("Calculating IM based curve");
		ArbitrarilyDiscretizedFunc hLogHazFunction = getLogFunction(hCurve);
		calc.getHazardCurve(hLogHazFunction, site, hIMR, hERF);
		hCurve = unLogFunction(hCurve, hLogHazFunction);
//		ArbitrarilyDiscretizedFunc realCurve =
//			ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile("/tmp/imEventSetTest/curve.txt");
		
		ArrayList<ArbitrarilyDiscretizedFunc> curves = new ArrayList<ArbitrarilyDiscretizedFunc>();
		curves.add(realCurve);
		curves.add(hCurve);
		
		boolean xLog = false;
		boolean yLog = false;
		boolean customAxis = true;
		this.gp.drawGraphPanel(imt, "", curves, xLog, yLog, customAxis, "Curves", this);
		this.gp.setVisible(true);
		
		this.gp.togglePlot(null);
		
		this.gp.validate();
		this.gp.repaint();
		
		ChartUtilities.saveChartAsPNG(new File(outputDir.getAbsolutePath() + File.separator + "curves.png"),
				gp.getCartPanel().getChart(), 800, 600);
		
		double maxDiff = 0;
		double maxPDiff = 0;
		
		for (int i=0; i<hCurve.getNum(); i++) {
			DataPoint2D hPt = hCurve.get(i);
			DataPoint2D rPt = realCurve.get(i);
			
			assertEquals(hPt.getX(), rPt.getX());
			
			System.out.println("Comparing point: " + i);
			
			System.out.println("\"Real\" point:\t" + rPt.getX() + ", " + rPt.getY());
			System.out.println("HAZ01A point:\t" + hPt.getX() + ", " + hPt.getY());
			
			if (hPt.getY() == 0 && rPt.getY() == 0)
				continue;
			
			double absDiff = Math.abs(hPt.getY() - rPt.getY());
			if (absDiff > maxDiff)
				maxDiff = absDiff;
			double absPDiff = absDiff / rPt.getY() * 100d;
			if (absPDiff > maxPDiff)
				maxPDiff = absPDiff;
			
			System.out.println("absDiff: " + absDiff + ", abs % diff: " + absPDiff);
			
			boolean success = absPDiff < TOL_PERCENT;
			if (!success) {
				System.out.println("FAIL!");
			}
			assertTrue(success);
		}
		
		System.out.println("Max Diff: " + maxDiff);
		System.out.println("Max Diff %: " + maxPDiff);
	}
	
	private static ArbitrarilyDiscretizedFunc getLogFunction(DiscretizedFuncAPI arb) {
		ArbitrarilyDiscretizedFunc new_func = new ArbitrarilyDiscretizedFunc();
		for (int i = 0; i < arb.getNum(); ++i)
			new_func.set(Math.log(arb.getX(i)), 1);
		return new_func;
	}
	
	private static ArbitrarilyDiscretizedFunc unLogFunction(
			DiscretizedFuncAPI oldHazFunc, DiscretizedFuncAPI logHazFunction) {
		int numPoints = oldHazFunc.getNum();
		ArbitrarilyDiscretizedFunc hazFunc = new ArbitrarilyDiscretizedFunc();
		for (int i = 0; i < numPoints; ++i) {
			hazFunc.set(oldHazFunc.getX(i), logHazFunction.getY(i));
		}
		return hazFunc;
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

	public double getMaxX() {
		return 1;
	}

	public double getMaxY() {
		return 1;
	}

	public double getMinX() {
		return 0;
	}

	public double getMinY() {
		return 0;
	}

	public int getAxisLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	public int getPlotLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	public int getTickLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	public void setXLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	public void setYLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

}
