package scratch.ned.ETAS_Tests;

import java.util.ArrayList;

import org.apache.commons.math.util.MathUtils;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

public class ETAS_Utils {
	
	double k_DEFAULT = 0.008;	// units are number of events > magMain per day
	double p_DEFAULT = 1.34;
	double magMin_DEFAULT = 2.5;
	double c_DEFAULT = 0.095;
	
	/**
	 * This returns the expected number of primary aftershocks between time tMin and tMax for an ETAS sequence
	 * from an integration of Equation (1) in Felzer (2009, SRL, v 80, p 21-25, doi: 10.1785/gssrl.80.1.21)
	 * @param k
	 * @param p - must be > 1.0 (not checked)
	 * @param magMain - main shock magnitude
	 * @param magMin - minimum magnitude
	 * @param c
	 * @param tMin - beginning of forecast time window (since origin time)
	 * @param tMax - end of forecast time window (since origin time)
	 * @return
	 */
	public static double getExpectedNumEvents(double k, double p, double magMain, double magMin, double c, double tMin, double tMax) {
		double oneMinusP= 1-p;
		double lambda = k*Math.pow(10,magMain-magMin)/oneMinusP;
		lambda *= Math.pow(c+tMax,oneMinusP) - Math.pow(c+tMin,oneMinusP);
		return lambda;
	}
	
	/**
	 * This applies the default ETAS parameter values for CA from Hardebeck et al. 
	 * (2008, JGR, v 113, B08310, doi:10.1029/2007JB005410): 
	 * k=0.008, p=1.34, c=0.095, and magMin=2.5
	 * @param magMain
	 * @param t1
	 * @param t2
	 * @return
	 */
	public double getDefaultExpectedNumEvents(double magMain, double tMin, double tMax) {
		return getExpectedNumEvents(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, tMin, tMax);
	}
	
	
	/**
	 * Knuth's algorithm for generating random Poisson distributed numbers
	 * @param lambda - the expected number of events
	 * @return
	 */
	public int getPoissonRandomNumber(double lambda) {
	    double L = Math.exp(-lambda);
	    int k = 0;
	    double p = 1;
	    do {
	        k = k + 1;
	        double u = Math.random();
	        p = p * u;
	    } while (p > L);
	    return k - 1;
	}
	
	
	
	/**
	 * This gives a random event time for an ETAS sequence.  This algorithm was provided by 
	 * Karen Felzer (from her Matlab code).
	 * @param c
	 * @param tMin
	 * @param tMax
	 * @return
	 */
	public double getRandomTimeOfEvent(double c, double p, double tMin, double tMax) {
		double r= Math.random();
		double t;
		if(p != 1.0) {
		    double a1 = Math.pow(tMax + c,1-p);
		    double a2 = Math.pow(tMin + c,1-p);
		    double a3 = r*a1 + (1-r)*a2;
		    t = Math.pow(a3,1/(1-p)) - c;
		}
		else {
			double a1 = Math.log(tMax+c);
			double a2 = Math.log(tMin + c);
			double a3 = r*a1 + (1-r)*a2;
			t = Math.exp(a3) - c;
		}
		return t;
	}
	
	/**
	 * This gives a random event time for an ETAS sequence using the 
	 * default ETAS parameter values for CA from Hardebeck et al. 
	 * (2008, JGR, v 113, B08310, doi:10.1029/2007JB005410): 
	 * p=1.34, c=0.095.  
	 * @param tMin
	 * @param tMax
	 * @return
	 */
	public double getDefaultRandomTimeOfEvent(double tMin, double tMax) {
		return getRandomTimeOfEvent(c_DEFAULT, p_DEFAULT, tMin, tMax);
	}
	
	
	/**
	 * This returns the expected number of primary aftershocks as a function of time using
	 * Equation (1) of Felzer (2009, SRL, v 80, p 21-25, doi: 10.1785/gssrl.80.1.21)
	 * @param k
	 * @param p
	 * @param magMain
	 * @param magMin
	 * @param c
	 * @param tMin
	 * @param tMax
	 * @param tDelta
	 * @return
	 */
	public EvenlyDiscretizedFunc getNumWithTimeFunc(double k, double p, double magMain, double magMin, double c, double tMin, double tMax, double tDelta) {
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(tMin+tDelta/2, tMax-tDelta/2, (int)Math.round((tMax-tMin)/tDelta));
		for(int i=0;i<func.getNum();i++) {
			double yVal = k*Math.pow(10,magMain-magMin)*Math.pow(c+func.getX(i), -p);
			func.set(i,yVal);
		}
		return func;
	}

	/**
	 * This returns the expected number of primary aftershocks as a function of 
	 * time using the default ETAS parameter values for CA from Hardebeck et al. 
	 * (2008, JGR, v 113, B08310, doi:10.1029/2007JB005410):
	 * @param magMain
	 * @param tMin
	 * @param tMax
	 * @param tDelta
	 * @return
	 */
	public EvenlyDiscretizedFunc getDefaultNumWithTimeFunc(double magMain, double tMin, double tMax, double tDelta) {
		return getNumWithTimeFunc(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, tMin, tMax, tDelta);
	}

}
