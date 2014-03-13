package scratch.kevin.ucerf3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.DataUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.mean.MeanUCERF3;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class MeanUCERF3_CurveCompareTest {
	
	public static void main(String[] args) throws IOException, DocumentException {
		File dir = new File("/home/kevin/OpenSHA/UCERF3/MeanUCERF3-timedep-tests");
		
		FaultModels fm = FaultModels.FM3_1;
		double udTol = 10d;
		boolean branchAveragedSol = false;
		
		FaultSystemSolutionERF erf;
		if (branchAveragedSol) {
			udTol = 0d;
			fm = FaultModels.FM3_1;
			FaultSystemSolution baSol = FaultSystemIO.loadSol(
					new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
							"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
			erf = new FaultSystemSolutionERF(baSol);
		} else {
			erf = new MeanUCERF3();
//			erf.setMeanParams(0d, false, 0d, MeanUCERF3.RAKE_BASIS_NONE);
			((MeanUCERF3)erf).setMeanParams(udTol, false, 0d, MeanUCERF3.RAKE_BASIS_NONE);
			if (fm != null)
				erf.getParameter(MeanUCERF3.FAULT_MODEL_PARAM_NAME).setValue(fm.name());
		}
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
		
		ScalarIMR imr = AttenRelRef.CB_2008.instance(null);
		imr.setParamDefaults();
		imr.setIntensityMeasure(PGA_Param.NAME);
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(PGA_Param.NAME);
		Site site = new Site(new Location(34.055, -118.2467)); // downtown LA
		site.addParameterList(imr.getSiteParams());
		
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		String fmAdd;
		if (fm == null)
			fmAdd = "";
		else
			fmAdd = fm.name()+"_";
		
		String udAdd;
		if (udTol == 0)
			udAdd = "";
		else
			udAdd = "ud"+(float)udTol+"_";
		
		String baAdd;
		if (branchAveragedSol)
			baAdd = "baSol_";
		else
			baAdd = "";
		
		File meanPoissonFile = new File(dir, fmAdd+baAdd+udAdd+"meanucerf3_poisson.txt");
		File meanTimeDepFile = new File(dir, fmAdd+baAdd+udAdd+"meanucerf3_timedep.txt");
		
		DiscretizedFunc meanTimeDepCurve, meanPoissonCurve;
		
		erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
		if (meanTimeDepFile.exists()) {
			meanTimeDepCurve = ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile(meanTimeDepFile.getAbsolutePath());
		} else {
			erf.getParameter(ProbabilityModelParam.NAME).setValue(
					ProbabilityModelOptions.U3_PREF_BLEND);
			erf.setParameter(HistoricOpenIntervalParam.NAME,
					(double)(FaultSystemSolutionERF.START_TIME_DEFAULT-1875));
			erf.setParameter(BPTAveragingTypeParam.NAME,
					BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE);
			
			erf.getTimeSpan().setDuration(30d);
			erf.updateForecast();
			
			System.out.println("Calculating Time Dep Curve with "+erf.getNumSources()+" sources");
			
			meanTimeDepCurve = xVals.deepClone();
			calc.getHazardCurve(meanTimeDepCurve, site, imr, erf);
			
			ArbitrarilyDiscretizedFunc.writeSimpleFuncFile(meanTimeDepCurve, meanTimeDepFile);
		}
		
		if (meanPoissonFile.exists()) {
			meanPoissonCurve = ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile(meanPoissonFile.getAbsolutePath());
		} else {
			erf.getParameter(ProbabilityModelParam.NAME).setValue(
					ProbabilityModelOptions.POISSON);
			
			erf.getTimeSpan().setDuration(30d);
			erf.updateForecast();
			
			System.out.println("Calculating Time Indep Curve with "+erf.getNumSources()+" sources");
			
			meanPoissonCurve = xVals.deepClone();
			calc.getHazardCurve(meanPoissonCurve, site, imr, erf);
			
			ArbitrarilyDiscretizedFunc.writeSimpleFuncFile(meanPoissonCurve, meanPoissonFile);
		}
		
		DiscretizedFunc averagedTimeDepCurve = calcMean(new File(dir, "curves_timedep"), fm);
		DiscretizedFunc averagedPoissonCurve = calcMean(new File(dir, "curves_poisson"), fm);
		
		System.out.println("Poisson Diagnostics:");
		writeDiagnostics(averagedPoissonCurve, meanPoissonCurve);
		
		System.out.println("\n\nTime Dep Diagnostics:");
		writeDiagnostics(averagedTimeDepCurve, meanTimeDepCurve);
	}
	
	private static DiscretizedFunc calcMean(File curvesDir, FaultModels fm) throws FileNotFoundException, IOException {
		DiscretizedFunc curve = null;
		double totWeight = 0;
		for (File file : curvesDir.listFiles()) {
			String name = file.getName();
			if (!name.endsWith(".txt"))
				continue;
			LogicTreeBranch branch = LogicTreeBranch.fromFileName(name);
			FaultModels fileFM = branch.getValue(FaultModels.class);
			if (fm != null && fileFM != fm)
				continue;
			DiscretizedFunc func = ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile(file.getAbsolutePath());
			if (curve == null) {
				curve = new ArbitrarilyDiscretizedFunc();
				for (int i=0; i<func.getNum(); i++)
					curve.set(func.getX(i), 0d);
			}
			
			double fmWeight = 1;
			if (fm == null)
				fmWeight = LogicTreeBranch.getNormalizedWt(fileFM,
					InversionModels.CHAR_CONSTRAINED);
			double dmWeight = LogicTreeBranch.getNormalizedWt(branch.getValue(DeformationModels.class),
					InversionModels.CHAR_CONSTRAINED);
			double scaleWeight = LogicTreeBranch.getNormalizedWt(branch.getValue(ScalingRelationships.class),
					InversionModels.CHAR_CONSTRAINED);
			
			double weight = fmWeight * dmWeight * scaleWeight;
			totWeight += weight;
			
			for (int i=0; i<curve.getNum(); i++)
				curve.set(i, curve.getY(i) + weight*func.getY(i));
		}
		
		System.out.println("Tot weight for "+curvesDir.getName()+": "+totWeight);
		
		return curve;
	}
	
	private static void writeDiagnostics(DiscretizedFunc avgCurve, DiscretizedFunc meanU3Curve) {
		double maxDiscrep = 0;
		double maxDiscrepPercent = 0;
		for (int i=0; i<avgCurve.getNum(); i++) {
			double x = avgCurve.getX(i);
			double avgY = avgCurve.getY(i);
			double meanU3Y = meanU3Curve.getY(i);
			double discrep = Math.abs(avgY - meanU3Y);
			double pDiff = DataUtils.getPercentDiff(meanU3Y, avgY);
			
			if (discrep > maxDiscrep)
				maxDiscrep = discrep;
			if (pDiff > maxDiscrepPercent)
				maxDiscrepPercent = pDiff;
			
			System.out.println("X: "+(float)x+"\tavg: "+(float)avgY+"\tmeanU3: "+(float)meanU3Y+"\tdiff: "
					+(float)discrep+"\tpDiff: "+(float)pDiff+" %");
		}
		
		System.out.println("Max discrep: "+(float)+maxDiscrep);
		System.out.println("Max pDiff: "+(float)+maxDiscrepPercent+" %");
	}

}
