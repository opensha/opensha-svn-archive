package org.opensha.sha.cybershake.calc.mcer;

import java.util.HashSet;
import java.util.List;

import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.Interpolate;
import org.opensha.sha.calc.mcer.AbstractMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.AbstractMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.CurveBasedMCErProbabilisitCalc;
import org.opensha.sha.calc.mcer.WeightProvider;

public class CyberShakeWeightProvider implements WeightProvider {
	
	private static double p1 = 2d;
	private static double p2 = 10d;
	
	private static double calcGMPEWeight(double period) {
		if (period <= p1)
			return 1;
		if (period >= p2)
			return 0d;
		return Interpolate.findY(p1, 1d, p2, 0d, period);
	}
	
	private static double calcCyberShakeWeight(double period) {
		if (period <= p1)
			return 0;
		if (period >= p2)
			return 1d;
		return Interpolate.findY(p1, 0d, p2, 1d, period);
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

}
