/**
 * 
 */
package scratch.aftershockStatistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jfree.data.Range;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;

/**
 * @author field
 *
 */
public class AftershockStatsCalc {
	
	/**
	 * This computes the log-likelihood for the given modified Omori parameters according to 
	 * equation (6) of Ogata (1983; J. Phys. Earth, 31,115-124).
	 * @param k
	 * @param c
	 * @param p
	 * @param relativeEventTimes - in order of occurrence relative to main shock
	 * @return
	 */
	public static double getLogLikelihoodForOmoriParams(double k, double c, double p, double[] relativeEventTimes) {
		double funcA=Double.NaN;
		int n=relativeEventTimes.length;
		double t_beg = relativeEventTimes[0];
		double t_end = relativeEventTimes[n-1];
		if(p == 1)
			funcA = Math.log(t_end+c) - Math.log(t_beg+c);
		else
			funcA = (Math.pow(t_end+c,1-p) - Math.pow(t_beg+c,1-p)) / (1-p);
		double sumLn_t = 0;
		for(double t : relativeEventTimes)
			sumLn_t += Math.log(t+c);
		return n*Math.log(k) - p*sumLn_t - k*funcA;
	}
	
	
	private static double[] readAndysFile() {
		
		try {
			BufferedReader buffRead = new BufferedReader(new FileReader("/Users/field/workspace/OpenSHA/dev/scratch/aftershockStatistics/AndysSimulationData.txt"));
			ArrayList<Double> eventTimeList = new ArrayList<Double>();
			String line = buffRead.readLine();
			while (line != null) {
				StringTokenizer tok = new StringTokenizer(line);
				while(tok.hasMoreTokens()) {
					eventTimeList.add(Double.parseDouble(tok.nextToken()));
				}
				line = buffRead.readLine();
			}
			double[] eventTimeArray = new double[eventTimeList.size()];
			for(int i=0;i<eventTimeList.size();i++) {
				eventTimeArray[i] = eventTimeList.get(i);
//				System.out.println(eventTimeArray[i]);
			}
			buffRead.close();
			return eventTimeArray;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	public static void testAndyCalc() {
		
		double c = 0.05;
		
		double k_min = 17;
		double k_max = 34;
		double k_delta = 0.25;

		double p_min = 0.9; 
		double p_max = 1.15; 
		double p_delta = 0.0125;
		
		int k_num = (int)((k_max-k_min)/k_delta)+1;		
		int p_num = (int)((p_max-p_min)/p_delta)+1;
		
		double[] relativeEventTimes;

		// x-axis is k and y-axis is p
		EvenlyDiscrXYZ_DataSet xyzLogLikelihood = new EvenlyDiscrXYZ_DataSet(k_num, p_num, k_min, p_min, k_delta, p_delta);
		
		double maxLike=-Double.MAX_VALUE;
		double maxLike_k=Double.NaN;
		double maxLike_p=Double.NaN;
		for(int x=0;x<xyzLogLikelihood.getNumX();x++) {
			for(int y=0;y<xyzLogLikelihood.getNumY();y++) {
				double logLike = getLogLikelihoodForOmoriParams(xyzLogLikelihood.getX(x), c, xyzLogLikelihood.getY(y), readAndysFile());
				xyzLogLikelihood.set(x, y, logLike);
				if(logLike>maxLike) {
					maxLike=logLike;
					maxLike_k = xyzLogLikelihood.getX(x);
					maxLike_p = xyzLogLikelihood.getY(y);					
				}
			}
		}

		System.out.println("maxLike_k="+maxLike_k+"\nmaxLike_p="+maxLike_p);
		
		CPT cpt=null;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(xyzLogLikelihood.getMinZ(), xyzLogLikelihood.getMaxZ());
		} catch (IOException e) {
			e.printStackTrace();
		}
		XYZPlotSpec logLikeSpec = new XYZPlotSpec(xyzLogLikelihood, cpt, "Log Likelihood", "k", "p", "log-likelihood");
		XYZPlotWindow window_logLikeSpec = new XYZPlotWindow(logLikeSpec, new Range(k_min,k_max), new Range(p_min,p_max));

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		testAndyCalc();
	}

}
