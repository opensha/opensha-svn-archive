package org.scec.sha.magdist;

import java.lang.*;
import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;
import org.scec.data.function.DiscretizedFuncAPI;

/**
 * <b>Title:</b> GaussMagFreqDist<br>
 * <b>Description:</b> This is a subclass of MagFreqDist, where the distribution is
 * represented with a truncated Gaussian distribution.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class GaussianMagFreqDist extends IncrementalMagFreqDist {


    /*
      These three parameters, along with a specified TotMomentRate
      define the distribution.
    */
    /**
       the number of standard deviations where the cutoff will be made
       - necessary because a Gaussian is infinite
    */
	protected double numStdDevCutoff;
	protected double meanMag;
	protected double magStdDev;

    public void set(DataPoint2D point, int index) throws DataPoint2DException{}
    public void set(double x, double y, int index) throws DataPoint2DException{}

    /*
      Note Constructor Defaults:

      MinMag=MagChar-NumStdevCutoff*MagStdev

      and

      MaxMag=MagChar+NumStdevCutoff*MagStdev
    */

    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /**
     * Note: Min and Max will be computed from these values where
     * truncation to zero occurs
     */
    public GaussianMagFreqDist(
        double meanMag,
		double magStdDev,
		double totMoRate,
        double deltaMag,
        double numStdDevCutoff
    ) {

        this.delta = deltaMag;
        setMagStdDevAndTotMomentRate(meanMag, magStdDev, totMoRate);
        this.numStdDevCutoff = numStdDevCutoff;
    }


    public GaussianMagFreqDist(
        double minMag,
		double deltaMag,
		int numMag,
		double meanMag,
		double magStdDev,
		double totMoRate,
        double numStdDevCutoff
    ){

        this.minX = minMag;
        this.delta = deltaMag;
        //this.num = numMag;

        // this.min = minMag;
        // this.delta = deltaMag;
        // this.num = numMag;

        setAll(meanMag, magStdDev, totMoRate, numStdDevCutoff);
    }



    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

	public double getMeanMag() { return meanMag; }
    public void setMeanMag(double meanMag) { this.meanMag = meanMag; }

	public double getMagStdDev() { return magStdDev; }
    public void setMagStdDev(double magStdDev) { this.magStdDev = magStdDev; }

	public double getNumStdDevCutoff() { return numStdDevCutoff; }
    public void setNumStdDevCutoff(double numStdDevCutoff) { this.numStdDevCutoff = numStdDevCutoff; }

    public void setMagStdDevAndTotMomentRate(
        double meanMag,
		double magStdDev,
		double totalMomentRate)
    {
        this.totalMomentRate = totalMomentRate;
        //setTotalMomentRate(totMoRate);
        setMeanMag(meanMag);

        setMagStdDev(magStdDev);
	}


    /*
       constructor rules need to apply...

       MinMag=MagChar-NumStdevCutoff*MagStdev

       and

       MaxMag=MagChar+NumStdevCutoff*MagStdev

       Throw an exception if this fails

    */
    public void setAll(
        double meanMag,
		double magStdDev,
		double totMoRate,
        double numStdDevCutoff
        )
    {
        setMagStdDevAndTotMomentRate(meanMag, magStdDev, totMoRate);
        this.numStdDevCutoff = numStdDevCutoff;
    }

    /**
     * FIX *** FIX *** Not implemented yet
     */
    public boolean equals(DiscretizedFuncAPI function){ return true; }

    /**
     * FIX *** FIX *** Not implemented yet
     */
    public Object clone(){ return null; }

}
