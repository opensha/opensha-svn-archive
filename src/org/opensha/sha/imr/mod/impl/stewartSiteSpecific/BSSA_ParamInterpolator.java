package org.opensha.sha.imr.mod.impl.stewartSiteSpecific;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jfree.data.Range;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.Interpolate;
import org.opensha.sha.imr.attenRelImpl.ngaw2.BSSA_2014;
import org.opensha.sha.imr.attenRelImpl.ngaw2.IMT;
import org.opensha.sha.imr.mod.impl.stewartSiteSpecific.NonErgodicSiteResponseMod.Params;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class BSSA_ParamInterpolator implements ParamInterpolator<Params> {
	
	BSSA_2014 bssa;
	
	public BSSA_ParamInterpolator() {
		bssa = new BSSA_2014();
	}
	
	double calcEmpirical(Params param, double period, double vs30, double z1p0) {
		return calcEmpirical(param, IMT.getSA(period), vs30, z1p0);
	}
	
	double calcEmpirical(Params param, IMT imt, double vs30, double z1p0) {
		Preconditions.checkNotNull(imt);
		switch (param) {
		case F1:
			return bssa.calcLnFlin(imt, vs30) + bssa.calcFdz1(imt, vs30, z1p0);
		
		case F2:
			return bssa.calcF2(imt, vs30);
			
		default:
			return Double.NaN;
		}
	}

	@Override
	public double[] getInterpolated(PeriodDependentParamSet<Params> periodParams, double period,
			double tSite, Site site) {
		Params[] params = periodParams.getParams();
		return getInterpolated(periodParams, params, period, tSite, site);
	}
	
	public double[] getInterpolated(PeriodDependentParamSet<Params> periodParams, Params[] params,
			double period, double tSite, Site site) {
		List<Double> periods = periodParams.getPeriods();
		
		Preconditions.checkState(site.containsParameter(Vs30_Param.NAME));
		double vs30 = site.getParameter(Double.class, Vs30_Param.NAME).getValue();
		Double z1p0 = site.getParameter(Double.class, DepthTo1pt0kmPerSecParam.NAME).getValue();
		if (z1p0 == null)
			z1p0 = Double.NaN;
		else
			// OpenSHA has Z1.0 in m instead of km, need to convert
			z1p0 /= 1000d;
		
		int periodIndex = Collections.binarySearch(periods, period);
		if (periodIndex >= 0) {
			// exact match
			double[] ret = periodParams.get(params, periodIndex);
			for (int i=0; i<params.length; i++) {
				if (params[i] == Params.F1 || params[i] == Params.F2) {
					double empirical = calcEmpirical(params[i], period, vs30, z1p0);
					double interpVal = ret[i];
					if (period <= tSite || Double.isNaN(tSite)) {
						ret[i] = interpVal;
					} else if (period >= 2*tSite) {
						ret[i] = empirical;
					} else {
						// transition zone
						ret[i] = interpVal * Math.log(2*tSite/period)/Math.log(2)
								+ empirical * Math.log(period/tSite)/Math.log(2);
					}
				}
			}
			return ret;
		}
		if (period < periods.get(0)) {
			// below, use first value
			return periodParams.get(params, 0);
		}

		// this means that it's not an exact match and is above the min period
		int insertionIndex = -(periodIndex + 1);
		Preconditions.checkState(insertionIndex > 0 && insertionIndex <= periods.size());

		double x1 = periods.get(insertionIndex-1);
		double[] y1 = periodParams.getValues(params, insertionIndex-1);
		double x2;
		double[] y2;
		if (insertionIndex == periods.size()) {
			// it's above, repeat last point
			x2 = period;
			y2 = y1;
		} else {
			x2 = periods.get(insertionIndex);
			y2 = periodParams.getValues(params, insertionIndex);
		}
		
		// calculate weights
		double w1 = Math.log(x2/period)/Math.log(x2/x1);
		double w2 = Math.log(period/x1)/Math.log(x2/x1);

		double[] ret = new double[params.length];
		for (int i=0; i<params.length; i++) {
			Params param = params[i];
			double val;
			if (param == Params.F1 || param == Params.F2) {
				double empiricalBelow = calcEmpirical(param, x1, vs30, z1p0);
				double empirical = calcEmpirical(param, period, vs30, z1p0);
				double empiricalAbove = calcEmpirical(param, x2, vs30, z1p0);
				double diffBelow = y1[i] - empiricalBelow;
				double diffAbove = y2[i] - empiricalAbove;
				
				double interpVal = empirical + w1*diffBelow + w2*diffAbove;
				
				// now check against Tsite
				if (period <= tSite || Double.isNaN(tSite)) {
					val = interpVal;
				} else if (period >= 2*tSite) {
					val = empirical;
				} else {
					// transition zone
					val = interpVal * Math.log(2*tSite/period)/Math.log(2)
							+ empirical * Math.log(period/tSite)/Math.log(2);
				}
			} else {
				if (x1 > 0)
					val = Interpolate.findY(Math.log(x1), y1[i], Math.log(x2), y2[i], Math.log(period));
				else
					// can happen when first point is period=0 (PGA)
					val = Interpolate.findY(x1, y1[i], x2, y2[i], period);
//				System.out.println("Interpolated "+param.name()+" at T="+period+": ("+x1+","+y1[i]+") "+val+" ("+x2+","+y2[i]+")");
			}
			ret[i] = val;
		}
		return ret;
	}
	
	public void plotInterpolation(PeriodDependentParamSet<Params> periodParams,
			List<Double> periods, double tSite, Site site) {
		List<PlotSpec> specs = getInterpolationPlot(periodParams, periods, tSite, site);
		
		for (PlotSpec spec : specs) {
			GraphWindow gw = new GraphWindow(spec);
			gw.setXLog(true);
		}
	}
	
	public void writeInterpolationPlot(PeriodDependentParamSet<Params> periodParams,
			List<Double> periods, double tSite, Site site, File outputFile) throws IOException {
		Collections.sort(periods);
		List<PlotSpec> specs = getInterpolationPlot(periodParams, periods, tSite, site);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		List<Range> xRanges = Lists.newArrayList(new Range(periods.get(0), periods.get(periods.size()-1)));
		
		gp.drawGraphPanel(specs, true, false, xRanges, null);
		gp.getCartPanel().setSize(1000, 800);
		String name = outputFile.getName().toLowerCase();
		if (name.endsWith(".png"))
			gp.saveAsPNG(outputFile.getAbsolutePath());
		else if (name.endsWith(".pdf"))
			gp.saveAsPDF(outputFile.getAbsolutePath());
		else if (name.endsWith(".txt"))
			gp.saveAsTXT(outputFile.getAbsolutePath());
		else
			throw new IllegalStateException("Unknown plot extention: "+outputFile.getName());
	}
	
	private List<PlotSpec> getInterpolationPlot(PeriodDependentParamSet<Params> periodParams,
			List<Double> periods, double tSite, Site site) {
		Params[] paramsToPlot = { Params.F1, Params.F2 };
		
		ArbitrarilyDiscretizedFunc[] interpolatedFunc = new ArbitrarilyDiscretizedFunc[paramsToPlot.length];
		ArbitrarilyDiscretizedFunc[] empiricalFunc = new ArbitrarilyDiscretizedFunc[paramsToPlot.length];
		ArbitrarilyDiscretizedFunc[] preferredFunc = new ArbitrarilyDiscretizedFunc[paramsToPlot.length];
		for (int i=0; i<interpolatedFunc.length; i++) {
			interpolatedFunc[i] = new ArbitrarilyDiscretizedFunc();
			interpolatedFunc[i].setName("Interpolated");
			empiricalFunc[i] = new ArbitrarilyDiscretizedFunc();
			empiricalFunc[i].setName("Empirical");
			preferredFunc[i] = new ArbitrarilyDiscretizedFunc();
			preferredFunc[i].setName("Preferred");
		}
		
		Preconditions.checkState(site.containsParameter(Vs30_Param.NAME));
		double vs30 = site.getParameter(Double.class, Vs30_Param.NAME).getValue();
		Double z1p0 = site.getParameter(Double.class, DepthTo1pt0kmPerSecParam.NAME).getValue();
		if (z1p0 == null)
			z1p0 = Double.NaN;
		else
			// OpenSHA has Z1.0 in m instead of km, need to convert
			z1p0 /= 1000d;
		
		for (double period : periods) {
//			double[] vals = paramSet.getInterpolated(paramsToPlot, period);
			double[] preferredVals = getInterpolated(periodParams, paramsToPlot, period, tSite, site);
			double[] interpVals = getInterpolated(periodParams, paramsToPlot, period, Double.NaN, site);
			
			for (int i=0; i<paramsToPlot.length; i++) {
				interpolatedFunc[i].set(period, interpVals[i]);
				preferredFunc[i].set(period, preferredVals[i]);
				empiricalFunc[i].set(period, calcEmpirical(paramsToPlot[i], period, vs30, z1p0));
				System.out.println("Empirical "+paramsToPlot[i].name()+", "+(float)period+"s: "
						+empiricalFunc[i].getY(period));
			}
		}
		
		List<PlotSpec> specs = Lists.newArrayList();
		
		for (int i=0; i<paramsToPlot.length; i++) {
			List<XY_DataSet> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			funcs.add(empiricalFunc[i]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
			
			funcs.add(interpolatedFunc[i]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
			
			funcs.add(preferredFunc[i]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, PlotSymbol.CIRCLE, 4f, Color.RED));
			
			ArbitrarilyDiscretizedFunc input = new ArbitrarilyDiscretizedFunc();
			input.setName("Input");
			for (double period : periodParams.getPeriods())
				input.set(period, periodParams.get(paramsToPlot[i], period));
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
			
			PlotSpec spec = new PlotSpec(funcs, chars,
					"Parameter Interpolation", "Period", paramsToPlot[i].toString());
			if (i == paramsToPlot.length-1)
				spec.setLegendVisible(true);
			specs.add(spec);
		}
		
		return specs;
	}
	
	public static void main(String[] args) throws IOException {
		PeriodDependentParamSet<Params> periodParams = PeriodDependentParamSet.loadCSV(
				Params.values(), PeriodDependentParamSet.class.getResourceAsStream("params.csv"));
		File outputDir = new File("/tmp");
//		plotInterpolation(periodParams, paramsToPlot, outputDir);
		BSSA_ParamInterpolator interp = new BSSA_ParamInterpolator();
		
		Site site = new Site();
		Vs30_Param vs30 = new Vs30_Param();
		vs30.setValue(197);
		site.addParameter(vs30);
		DepthTo1pt0kmPerSecParam z10 = new DepthTo1pt0kmPerSecParam();
		z10.setValue(4.2/1000d);
		site.addParameter(z10);
		
		double tSite = 0.7;
		
		List<Double> periods = Lists.newArrayList();
		for (IMT imt : interp.bssa.getSupportedIMTs())
			if (imt.isSA())
				periods.add(imt.getPeriod());
		Collections.sort(periods);
		System.out.println("Periods: "+Joiner.on(",").join(periods));
		
		interp.writeInterpolationPlot(periodParams, periods, tSite, site, new File("/tmp/param_interpolation.png"));
	}

}
