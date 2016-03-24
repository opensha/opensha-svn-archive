package scratch.aftershockStatistics;

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
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * This represents a Reasenberg-Jones (1989, 1994) aftershock model that assumes the magnitude completeness
 * value is constant (does not change with time).
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
	double aValueMean, aValueSigma, bValue, pValue, cValue;


	public RJ_AftershockModel_Generic(double magMain, GenericRJ_Parameters params) {
		this(magMain, params.get_aValueMean(), params.get_aValueSigma(), -4.5, -0.5, 
				params.get_bValue(), params.get_pValue(), params.get_cValue());
	}
	

	public RJ_AftershockModel_Generic(double magMain, double aValueMean, double aValueSigma, double aValueMin, double aValueMax,
											double bValue, double pValue, double cValue) {
		
		this.magMain= magMain;
		this.aValueMean=aValueMean;
		this.aValueSigma=aValueSigma;

		if(aValueMin>aValueMax) {
			throw new RuntimeException("Problem: aValueMin > aValueMax");
		}
		if(aValueSigma<=0){
			throw new RuntimeException("Problem: aValueSigma must be greater than 0");
		}

		this.delta_a=0.01;	// this is so one of the discrete values is within 0.005 of aValueMean
		this.min_a = ((double)Math.round(aValueMin*100))/100d;	// round to nearest 100th value
		this.max_a = ((double)Math.round(aValueMax*100))/100d;	// round to nearest 100th value
		this.num_a = (int)Math.round((max_a-min_a)/delta_a) + 1;

		this.num_p=1;
		this.min_p=pValue;
		this.max_p=pValue;
		this.max_p_index=0;		
		
		this.num_c=1;
		this.min_c=cValue;
		this.max_c=cValue;
		this.max_c_index=0;
		
		this.b=bValue;
		
		if(D) {
			System.out.println("a-values range:\t"+min_a+"\t"+max_a+"\t"+num_a+"\t"+delta_a);
		}
		
		array = new double[num_a][num_p][num_c];
		double maxWt= Double.NEGATIVE_INFINITY;
		double totWt=0;
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			double wt = gaussWt(get_a(aIndex));
			array[aIndex][0][0] = wt;

			totWt+=wt;
			if(wt>maxWt) {
				max_a_index=aIndex;
				maxWt=wt;
			}
		}
		// normalize so that it sums to 1.0
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			array[aIndex][0][0] /= totWt;
		}
		
		if (D) {
			System.out.println("totWt="+totWt);
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a()+"\taValueMean="+aValueMean+"\tratio="+(float)(aValueMean/getMaxLikelihood_a()));
			System.out.println("getMaxLikelihood_p()="+getMaxLikelihood_p());
			System.out.println("getMaxLikelihood_c()="+getMaxLikelihood_c());
		}
	}
	
	private double gaussWt(double a) {
		return Math.exp(-(aValueMean-a)*(aValueMean-a)/(2*aValueSigma*aValueSigma));
	}
	
	/**
	 * Not needed here
	 */
	public double getRateAboveMagCompleteAtTime(double numDays) {
		return Double.NaN;
	}
	
	

	public static void main(String[] args) {
		RJ_AftershockModel_Generic gen = new RJ_AftershockModel_Generic(7.0, -2.5, 1.0, -4.5, -0.5, 1.0, 1.12, 0.018);
//		System.out.println("gen.getPDF_a():\n"+gen.getPDF_a());

	}

}
