package org.scec.sha.magdist;

import java.lang.*;
import org.scec.data.function.DiscretizedFuncAPI;

/**
 *  <b>Title:</b> SingleMagFreqDist<br>
 *  <b>Description:</b> This is a subclass of MagFreqDist where only one
 *  discrete magnitude value (Mag) has a nonzero Rate. Thus, there are three
 *  relevant parameters: Mag, Rate, and TotMomentRate, only two of which are
 *  unique (the third being computed from the other two).<br>
 *  <b>Note:</b> Default: MinMag = MaxMag = Mag, NumMag = 1, and DeltaMag = null
 *  <br>
 *  <b>Note:</b> Check that MinMag?Mag?MaxMag whenever one changes. Decide what
 *  to do if (Mag-MinMag)/DeltaMag != Int (if Mag does not fall on one of the
 *  mag increments)<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    March 18, 2002
 * @version    1.0
 */

public class SingleMagFreqDist extends IncrementalMagFreqDist {
    /*
     *  implements MagFreqDistAPI
     */

    /*
     *  ******************
     */
    /**
     * @todo    Variables
     */

    /*
     *  ******************
     */
    protected double mag = 0;

    /**
     *  Description of the Field
     */
    protected double rate = 0;




    public SingleMagFreqDist() {
        super();
    }


    /**
     *  Constructor for the SingleMagFreqDist object
     *
     * @param  mag   Description of the Parameter
     * @param  rate  Description of the Parameter
     */
    public SingleMagFreqDist( double mag, double rate ) {
        setMagAndRate( mag, rate );
    }


    /**
     *  Constructor for the SingleMagFreqDist object
     *
     * @param  minMag    Description of the Parameter
     * @param  deltaMag  Description of the Parameter
     * @param  numMag    Description of the Parameter
     * @param  mag       Description of the Parameter
     * @param  rate      Description of the Parameter
     */
    public SingleMagFreqDist(
            double minMag,
            double deltaMag,
            int numMag,
            double mag,
            double rate ) {

        super( minMag, deltaMag, numMag );

        setMagAndRate( mag, rate );

    }


    /**
     *  Sets the mag attribute of the SingleMagFreqDist object
     *
     * @param  mag  The new mag value
     */
    public void setMag( double mag ) {
        this.mag = mag;
    }


    /**
     *  Sets the rate attribute of the SingleMagFreqDist object
     *
     * @param  rate  The new rate value
     */
    public void setRate( double rate ) {
        this.rate = rate;
    }


    /**
     *  Sets the magAndRate attribute of the SingleMagFreqDist object
     *
     * @param  mag   The new magAndRate value
     * @param  rate  The new magAndRate value
     */
    public void setMagAndRate( double mag, double rate ) {

        setMag( mag );

        setRate( rate );

    }


    /**
     *  Sets the magAndTotMomentRate attribute of the SingleMagFreqDist object
     *
     * @param  mag            The new magAndTotMomentRate value
     * @param  totMomentRate  The new magAndTotMomentRate value
     */
    public void setMagAndTotMomentRate( double mag, double totMomentRate ) {

        setMag( mag );

        //setTotalMomentRate(totMomentRate);

    }


    /**
     *  Sets the rateAndTotMomentRate attribute of the SingleMagFreqDist object
     *
     * @param  rate           The new rateAndTotMomentRate value
     * @param  totMomentRate  The new rateAndTotMomentRate value
     */
    public void setRateAndTotMomentRate( double rate, double totMomentRate ) {

        setRate( rate );

        //setTotalMomentRate(totMomentRate);

    }


    /*
     *  **************************
     */
    /**
     * @return    The mag value
     * @todo      Getters / Setters
     */

    /*
     *  **************************
     */
    public double getMag() {
        return mag;
    }


    /**
     *  in addition to the "Vector getRate()" in parent. We should probably
     *  rename this.
     *
     * @return    The rate value
     */

    public double getRate() {
        return rate;
    }


    /**
     *  FIX *** FIX *** Not implemented yet
     *
     * @param  function  Description of the Parameter
     * @return           Description of the Return Value
     */
    public boolean equals( DiscretizedFuncAPI function ) {
        return true;
    }


    /**
     *  FIX *** FIX *** Not implemented yet
     *
     * @return    Description of the Return Value
     */
    public Object clone() {
        return null;
    }
}

