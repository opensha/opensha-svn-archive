package org.opensha.sha.cybershake.calc.mcer;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.Interpolate;
import org.opensha.sha.calc.mcer.AbstractMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.AbstractMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.CurveBasedMCErProbabilisitCalc;
import org.opensha.sha.calc.mcer.WeightProvider;

import com.google.common.collect.Lists;

public class CyberShakeWeightProvider implements WeightProvider {
	
	private static double p1 = 2d;
	private static double cs_weight_p1 = 0.2;
	private static double p2 = 5d;
	private static double cs_weight_p2 = 0.5;
	
	static double calcGMPEWeight(double period) {
//		if (period <= p1)
//			return 1;
//		if (period >= p2)
//			return 0d;
//		return Interpolate.findY(p1, 1d, p2, 0d, period);
		return 1d - calcCyberShakeWeight(period);
	}
	
	static double calcCyberShakeWeight(double period) {
		if (period < p1)
			return 0d;
		if (period >= p2)
			return cs_weight_p2;
		return Interpolate.findY(p1, cs_weight_p1, p2, cs_weight_p2, period);
	}
	
	private CurveBasedMCErProbabilisitCalc csProbCalc;
	private HashSet<CurveBasedMCErProbabilisitCalc> gmpeProbCalcs;
	private AbstractMCErDeterministicCalc csDetCalc;
	private AbstractMCErDeterministicCalc gmpeDetCalc;
	
	private double gmpeProbWeightEach;
	
	public CyberShakeWeightProvider(CurveBasedMCErProbabilisitCalc csProbCalc,
			List<? extends CurveBasedMCErProbabilisitCalc> gmpeProbCalcs,
			AbstractMCErDeterministicCalc csDetCalc,
			AbstractMCErDeterministicCalc gmpeDetCalc) {
		this.csProbCalc = csProbCalc;
		this.gmpeProbWeightEach = 1d/gmpeProbCalcs.size();
		this.gmpeProbCalcs = new HashSet<CurveBasedMCErProbabilisitCalc>(gmpeProbCalcs);
		
		this.csDetCalc = csDetCalc;
		this.gmpeDetCalc = gmpeDetCalc;
	}
	
	@Override
	public double getProbWeight(AbstractMCErProbabilisticCalc calc, double period) {
		if (calc == csProbCalc)
			return calcCyberShakeWeight(period);
		else if (gmpeProbCalcs.contains(calc))
			return gmpeProbWeightEach*calcGMPEWeight(period);
		throw new IllegalStateException("Calc of type "+ClassUtils.getClassNameWithoutPackage(calc.getClass())
				+" is not supplied CS or GMPE");
	}
	
	@Override
	public double getDetWeight(AbstractMCErDeterministicCalc calc,
			double period) {
		if (calc == csDetCalc)
			return calcCyberShakeWeight(period);
		else if (calc == gmpeDetCalc)
			return calcGMPEWeight(period);
		throw new IllegalStateException("Calc of type "+ClassUtils.getClassNameWithoutPackage(calc.getClass())
				+" is not supplied CS or GMPE");
	}
	
	public static void main(String[] args) {
		double[] periods = { 1d, 1.5d, 2d, 3d, 4d, 5d, 7.5d, 10d };
		
		ArbitrarilyDiscretizedFunc csFunc = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc gmpeFunc = new ArbitrarilyDiscretizedFunc();
		
		for (double period : periods) {
			csFunc.set(period, calcCyberShakeWeight(period));
			gmpeFunc.set(period, calcGMPEWeight(period));
		}
		
		List<ArbitrarilyDiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(csFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		
		funcs.add(gmpeFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		
		GraphWindow gw = new GraphWindow(funcs, "Weights", chars);
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
	}

}
