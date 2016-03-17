package org.opensha.sha.imr.mod.impl.stewartSiteSpecific;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

import org.jfree.data.Range;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.sha.imr.attenRelImpl.ngaw2.IMT;
import org.opensha.sha.imr.mod.impl.stewartSiteSpecific.NonErgodicSiteResponseMod.Params;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class BSSA_ParamInterpolationTest {

	private static final double precision_diff = 0.01;
	private static final double precision_percent = 1;
	private static final boolean do_plot = false;
	private static final boolean use_data_emp = false;
	
	private static Site site;
	private static double vs30;
	private static double z1p0;
	private static double tSite;
	
	private enum TestType {
		EMPIRICAL,
		INTERPOLATED,
		RECOMMENDED
	}
	
	private static Table<Params, TestType, ArbitrarilyDiscretizedFunc> testData;
	
	private static Params[] params;
	private static PeriodDependentParamSet<Params> periodParams;
	private static double maxParamPeriod = 0d;
	
	private static List<Double> periods;
	
	private static BSSA_ParamInterpolator interp;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CSVFile<String> csv = CSVFile.readStream(
				BSSA_ParamInterpolationTest.class.getResourceAsStream("TestInterpolation.csv"), false);
		
		// load period params
		params = new Params[] { Params.F1, Params.F2 };
		periodParams = new PeriodDependentParamSet<NonErgodicSiteResponseMod.Params>(params);
		
		int col = 1;
		for (int row=2; row<csv.getNumRows(); row++) {
			String periodStr = csv.get(row, col).trim();
			if (periodStr.isEmpty())
				break;
			double period = Double.parseDouble(periodStr);
			double f1 = Double.parseDouble(csv.get(row, col+1));
			double f2 = Double.parseDouble(csv.get(row, col+2));
			periodParams.set(period, new double[] { f1, f2 });
			maxParamPeriod = Math.max(maxParamPeriod, period);
		}
		
		System.out.println(periodParams);
		
		// load site info
		site = new Site();
		
		vs30 = Double.parseDouble(csv.get(1, 5));
		Vs30_Param vs30Param = new Vs30_Param(vs30);
		vs30Param.setValue(vs30);
		site.addParameter(vs30Param);
		
		z1p0 = Double.parseDouble(csv.get(1, 6));
		DepthTo1pt0kmPerSecParam z1p0Param = new DepthTo1pt0kmPerSecParam(z1p0/1000d, true);
		z1p0Param.setValue(z1p0*1000d);
		site.addParameter(z1p0Param);
		
		tSite = Double.parseDouble(csv.get(1, 7));
		
		// now load test data
		testData = HashBasedTable.create();
		for (TestType type : TestType.values())
			for (Params param : params)
				testData.put(param, type, new ArbitrarilyDiscretizedFunc());
		for (int row=2; row<csv.getNumRows(); row++) {
			col = 9;
			String periodStr = csv.get(row, col++).trim();
			if (periodStr.isEmpty())
				break;
			double period = Double.parseDouble(periodStr);
			for (Params param : params) {
				for (TestType type : TestType.values()) {
					String valStr = csv.get(row, col++).trim();
					if (valStr.isEmpty())
						continue;
					double val = Double.parseDouble(valStr);
					testData.get(param, type).set(period, val);
				}
			}
		}
		System.out.println("Loaded test data for "+testData.values().iterator().next().size()+" periods");
		
		periods = Lists.newArrayList();
		if (use_data_emp) {
			interp = new BSSA_ParamInterpolator() {
				@Override
				double calcEmpirical(Params param, double period, double vs30, double z1p0) {
					return testData.get(param, TestType.EMPIRICAL).getY(period);
				}
				@Override
				double calcEmpirical(Params param, IMT imt, double vs30, double z1p0) {
					return testData.get(param, TestType.EMPIRICAL).getY(imt.getPeriod());
				}
			};
			for (Point2D pt : testData.get(Params.F1, TestType.EMPIRICAL))
				periods.add(pt.getX());
		} else {
			interp = new BSSA_ParamInterpolator();
			for (IMT imt : interp.bssa.getSupportedIMTs())
				if (imt.isSA())
					periods.add(imt.getPeriod());
		}
		
		Collections.sort(periods);
	}

	@Test
	public void testEmpiricalF1() {
		doTest(TestType.EMPIRICAL, Params.F1, true);
	}
	
	@Test
	public void testEmpiricalF2() {
		doTest(TestType.EMPIRICAL, Params.F2, true);
	}
	
	private ArbitrarilyDiscretizedFunc doTest(TestType type, Params param, boolean test) {
		ArbitrarilyDiscretizedFunc data = testData.get(param, type);
		ArbitrarilyDiscretizedFunc calc = new ArbitrarilyDiscretizedFunc();
		
		System.out.println("Testing "+type.name()+", "+param.name());
		
		for (double period : periods) {
			if (!data.hasX(period)) {
				Preconditions.checkState(type == TestType.INTERPOLATED && period >= maxParamPeriod);
				continue;
			}
			double val;
			switch (type) {
			case EMPIRICAL:
				val = interp.calcEmpirical(param, period, vs30, z1p0);
				break;
			case INTERPOLATED:
				val = interp.getInterpolated(periodParams, new Params[] {param}, period, Double.NaN, site)[0];
				break;
			case RECOMMENDED:
				val = interp.getInterpolated(periodParams, new Params[] {param}, period, tSite, site)[0];
				break;

			default:
				throw new IllegalStateException("Unkown test type: "+type);
			}
			calc.set(period, val);
			
			double testVal = data.getY(period);
			double diff = Math.abs(val - testVal);
			double pDiff = DataUtils.getPercentDiff(testVal, val);
			boolean pass = true;
			if (Math.abs(testVal) > 1e-4 && Math.abs(val) > 1e-4 && Math.abs(pDiff) > precision_percent)
				pass = false;
			if (diff > precision_diff)
				pass = false;
			String passStr;
			if (pass)
				passStr = "\tPASS";
			else
				passStr = "\tFAIL";
			String str = "Period: "+(float)period+"\tData: "+(float)testVal+"\tCalc: "+(float)val
					+"\tDiff: "+(float)diff+"\t% Diff: "+(float)pDiff+passStr;
			System.out.println(str);
			if (test && !pass)
				fail(str);
//				assertEquals("Error for T="+period+", diff="+diff, testVal, val, precision_diff);
		}
		return calc;
	}
	
	@Test
	public void createPlot() {
		if (!do_plot)
			return;
		
		for (Params param : params) {
			List<XY_DataSet> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			for (TestType type : TestType.values()) {
				ArbitrarilyDiscretizedFunc data = testData.get(param, type);
				data.setName(type.name()+" Data");
				ArbitrarilyDiscretizedFunc calc = doTest(type, param, false);
				calc.setName(type.name()+" Calc");
				
				PlotSymbol sym = null;
				
				Color c;
				switch (type) {
				case EMPIRICAL:
					c = Color.BLUE;
					break;
				case INTERPOLATED:
					c = Color.RED;
					break;
				case RECOMMENDED:
					c = Color.GREEN;
					sym = PlotSymbol.CIRCLE;
					break;

				default:
					throw new IllegalStateException("Unkown test type: "+type);
				}
				
				funcs.add(data);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, c));
				funcs.add(calc);
				chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, sym, 4f, c.darker().darker()));
			}
			
			ArbitrarilyDiscretizedFunc input = new ArbitrarilyDiscretizedFunc();
			input.setName("Input");
			for (double period : periodParams.getPeriods())
				input.set(period, periodParams.get(param, period));
			funcs.add(input);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 6f, Color.BLACK));
			
			MinMaxAveTracker yTrack = new MinMaxAveTracker();
			for (XY_DataSet func : funcs) {
				yTrack.addValue(func.getMaxY());
				yTrack.addValue(func.getMinY());
			}
			
			Range yRange = new Range(yTrack.getMin()-0.1, yTrack.getMax()+0.1);
			DefaultXY_DataSet tSiteLine = new DefaultXY_DataSet();
			DefaultXY_DataSet t2SiteLine = new DefaultXY_DataSet();
			double t2Site = 2*tSite;
			tSiteLine.set(tSite, yRange.getLowerBound());
			tSiteLine.set(tSite, yRange.getUpperBound());
			t2SiteLine.set(t2Site, yRange.getLowerBound());
			t2SiteLine.set(t2Site, yRange.getUpperBound());
			
			tSiteLine.setName("Tsite");
			funcs.add(tSiteLine);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.GRAY));
			
			t2SiteLine.setName("2Tsite");
			funcs.add(t2SiteLine);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.DARK_GRAY));
			
			PlotSpec spec = new PlotSpec(funcs, chars, "Interpolation Validation", "Period (s)", param.toString());
			spec.setLegendVisible(true);
			GraphWindow gw = new GraphWindow(spec);
			gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
			gw.setXLog(true);
		}
		
		// pause for plots
		while (true)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
