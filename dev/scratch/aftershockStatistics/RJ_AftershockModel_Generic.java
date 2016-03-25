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
 * This represents a Reasenberg-Jones (1989, 1994) aftershock model where a-values are assumed to 
 * be Gaussian distributed and p and c are held fixed at the values given.
 * 
 * a-value discretizatio is hard-coded as 0.01 (the resolution of value given for various regions by
 * Page et al. (2016).
 * 
 * Note also that the Gaussian distribution is renormalized so that values sum to 1.0 over the range of
 * a-values represented.
 * 
 * TODO:
 * 
 *  1) Carefully define jUnit tests that cover all cases.
 *
 * @author field
 *
 */
public class RJ_AftershockModel_Generic extends RJ_AftershockModel {
	
	Boolean D=true;	// debug flag
	double mean_a, sigma_a, b, p, c;


	/**
	 * This instantiates a generic RJ model from a GenericRJ_Parameters object, where aValueMin and aValueMax
	 * are set as -4.5 and -0.5, respectively, and
	 * @param magMain
	 * @param params
	 */
	public RJ_AftershockModel_Generic(double magMain, GenericRJ_Parameters params) {
		
		this(magMain, params.get_aValueMean(), params.get_aValueSigma(), -4.5, -0.5, 
				params.get_bValue(), params.get_pValue(), params.get_cValue());
	}
	
	
	/**
	 * This instantiates a generic GR model for the values given
	 * @param magMain - main shock magnitude
	 * @param mean_a - mean a-value for the Gaussian distribution
	 * @param sigma_a - a-value standard deviation for the Gaussian distribution
	 * @param min_a - minimum a-value for the Gaussian distribution, which gets rounded to nearest 0.01 value
	 * @param max_a - maximum a-value for the Gaussian distribution, which gets rounded to nearest 0.01 value
	 * @param b - b-value
	 * @param p - p-value
	 * @param c - c-value
	 */
	public RJ_AftershockModel_Generic(double magMain, double mean_a, double sigma_a, double min_a, double max_a,
											double b, double p, double c) {
		
		this.magMain= magMain;
		this.b=b;

		this.mean_a=mean_a;
		this.sigma_a=sigma_a;

		if(min_a>max_a) {
			throw new RuntimeException("Problem: aValueMin > aValueMax");
		}
		if(sigma_a<=0){
			throw new RuntimeException("Problem: sigma_a must be greater than 0");
		}
		
		this.delta_a=0.01;	// this is so one of the discrete values is within 0.005 of mean_a
		this.min_a = ((double)Math.round(min_a*100))/100d;	// round to nearest 100th value
		this.max_a = ((double)Math.round(max_a*100))/100d;	// round to nearest 100th value
		this.num_a = (int)Math.round((max_a-min_a)/delta_a) + 1;
		EvenlyDiscretizedFunc aValueFunc = new EvenlyDiscretizedFunc(min_a, max_a, num_a);
		for(int i=0;i<aValueFunc.size();i++) {
			double wt = Math.exp(-(mean_a-aValueFunc.getX(i))*(mean_a-aValueFunc.getX(i))/(2*sigma_a*sigma_a));
			aValueFunc.set(i,wt);
		}
		
		setArrayAndMaxLikelyValuesFrom_aValueFunc(aValueFunc, b, p, c);

		if(D) {
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a()+"\tmean_a="+mean_a+"\tratio="+(float)(mean_a/getMaxLikelihood_a()));
		}
		
		
	}
	
		

	public static void main(String[] args) {
		RJ_AftershockModel_Generic gen = new RJ_AftershockModel_Generic(7.0, -2.5, 1.0, -4.5, -0.5, 1.0, 1.12, 0.018);
		System.out.println("gen.getPDF_a():\n"+gen.getPDF_a());
		
		ArrayList<HistogramFunction> funcList = new ArrayList<HistogramFunction>();
		funcList.add(gen.getPDF_a());
		ArrayList<PlotCurveCharacterstics> curveCharList = new ArrayList<PlotCurveCharacterstics>();
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLACK));
		GraphWindow numVsTimeGraph = new GraphWindow(funcList, "PDF"); 
		numVsTimeGraph.setX_AxisLabel("a-value");
		numVsTimeGraph.setY_AxisLabel("Density");
	}

}
