package scratch.aftershockStatistics;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.LegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.jfree.data.Range;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * This represents a Reasenberg-Jones (1989, 1994) aftershock model where the a-value distribution is the Bayesian
 * combination of two given models, and where b, p, and c are held fixed (and they must be the same in each model).
 * 
 * a-value discretizatio is the smallest between the two given models, and the a-value range if union of the two.
 * 
 * Note also that the Gaussian distribution is renormalized so that values sum to 1.0 over the final range of
 * a-values represented.
 * 
 * TODO:
 * 
 *  1) Carefully define jUnit tests that cover all cases.
 *
 * @author field
 *
 */
public class RJ_AftershockModel_Bayesian extends RJ_AftershockModel {
	
	Boolean D=true;	// debug flag
	
	/**
	 * This instantiates a Bayesian combination of the two given RJ models
	 * @param model1
	 * @param model2
	 */
	public RJ_AftershockModel_Bayesian(RJ_AftershockModel model1, RJ_AftershockModel model2) {
		
		this.magMain = model1.getMainShockMag();
		this.b = model1.get_b();
		double p = model1.getMaxLikelihood_p();
		double c = model1.getMaxLikelihood_c();
		
		// check similarity of these with the second model
		if(magMain != model2.getMainShockMag())
			throw new RuntimeException("Error: Main shock magnitudes differ between the two input models");
		if(b != model2.get_b())
			throw new RuntimeException("Error: b-values differ between the two input models");
		if(p != model2.getMaxLikelihood_p())
			throw new RuntimeException("Error: p-values differ between the two input models");
		if(c != model2.getMaxLikelihood_c())
			throw new RuntimeException("Error: c-values differ between the two input models:\t"+c+"\t"+model2.getMaxLikelihood_c());
		
		this.min_p=p;
		this.max_p=p;
		this.num_p=1;
		this.min_c=c;
		this.max_c=c;
		this.num_c=1;
		
		
		HistogramFunction aValFunc1 =  model1.getPDF_a();
		HistogramFunction aValFunc2 =  model2.getPDF_a();
		this.min_a = Math.max(aValFunc1.getMinX(), aValFunc2.getMinX());
		this.max_a = Math.min(aValFunc1.getMaxX(), aValFunc2.getMaxX());
		double minDelta = Math.min(aValFunc1.getDelta(), aValFunc2.getDelta());
		this.num_a = (int)Math.ceil((max_a-min_a)/minDelta) + 1;
		EvenlyDiscretizedFunc aValueFuncBayes = new EvenlyDiscretizedFunc(min_a, max_a, num_a);
		this.delta_a = aValueFuncBayes.getDelta();

		if(min_a>max_a) {
			throw new RuntimeException("Problem: aValueMin > aValueMax");
		}
		
		for(int i=0;i<aValueFuncBayes.size();i++) {
			double a = aValueFuncBayes.getX(i);
			double wt = aValFunc1.getInterpolatedY(a)*aValFunc2.getInterpolatedY(a);
			aValueFuncBayes.set(i,wt);
		}
		
		setArrayAndMaxLikelyValuesFrom_aValueFunc(aValueFuncBayes, b, p, c);
		
	}
	
		

	public static void main(String[] args) {
		RJ_AftershockModel_Generic gen1 = new RJ_AftershockModel_Generic(7.0, -2, 0.3, -4.5, -0.5, 1.0, 1.12, 0.018);
		RJ_AftershockModel_Generic gen2 = new RJ_AftershockModel_Generic(7.0, -3, 0.3, -4.5, -0.5, 1.0, 1.12, 0.018);
		RJ_AftershockModel_Bayesian bayes = new RJ_AftershockModel_Bayesian(gen1,gen2);
		
		ArrayList<HistogramFunction> funcList = new ArrayList<HistogramFunction>();
		funcList.add(gen1.getPDF_a());
		funcList.add(gen2.getPDF_a());
		funcList.add(bayes.getPDF_a());
		ArrayList<PlotCurveCharacterstics> curveCharList = new ArrayList<PlotCurveCharacterstics>();
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow numVsTimeGraph = new GraphWindow(funcList, "PDF"); 
		numVsTimeGraph.setX_AxisLabel("a-value");
		numVsTimeGraph.setY_AxisLabel("Density");
	}

}
