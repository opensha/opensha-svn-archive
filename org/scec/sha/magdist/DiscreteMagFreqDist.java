package org.scec.sha.magdist;

import org.scec.data.function.EvenlyDiscretizedFunc;

import java.lang.*;
import java.util.*;

/**
 * <b>Title:</b> DiscreteMagFreqDist<br>
 * <b>Description:</b> This abstract class represents the rate of earthquakes (per year)
 * as a function of magnitude (not a cumulative distribution).
 * Each subclass with have additional constructors. <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Sid Hellman
 * @version 1.0
 */

public abstract class DiscreteMagFreqDist
    extends EvenlyDiscretizedFunc
    implements MagFreqDistAPI
{

    /* *******************/
    /** @todo  Variables */
    /* *******************/


    /** 1D array giving the rate (per year) at each discrete mag defined above. The Ordiantes from the DiscreteFunctionAPI. */
    protected double totalMomentRate=Double.NaN;

    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** Fix - this will need to be taken out */
    public DiscreteMagFreqDist() {
        this(0.0, 1.0, 10);
    }

    public DiscreteMagFreqDist(double minMag, double deltaMag, int numMag) {
        super(minMag, numMag, deltaMag);
    }

    public double getCumulativeRate(double mag){
        double d = -1;
        return d;
    }

    public double getTotalMomentRate(){
        return totalMomentRate;
    }


    /** scales all the rates so that the total moment rate is equal to the passed in scale */
    public void scaleToTotalMomentRate(double scale){

    }


    /**
     * Inherited from DiscretizedFunction2D
     */
    public int getNum() { return -1;}

    public double getMinMag(){ return getMinX(); }
    public double getMaxMag(){ return getMaxX(); }

    public double getMinRate(){ return null; }
    public double getMaxRate(){ return null; }
    public double getDelta(){ return null; }



    public Iterator getMagFreqIterator(){ return super.getPointsIterator(); }

    // public HashMap getRates() {return rates;}

    /** Checks mag withing tolerance to return rate */
    public double getRate(double mag) {
	    //return (Double)(rates.get(mag));
        return null;
    }

    /** Bypasses tolerance check when calling class knows the exact index, i.e. x-value index, they want */
    public double getRate(int index) {
	    //return (Double)(rates.get(mag));
        return null;
    }


}
