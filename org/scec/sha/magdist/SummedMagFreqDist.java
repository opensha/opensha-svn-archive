package org.scec.sha.magdist;

import org.scec.data.function.DiscretizedFuncAPI;

/**
 *  <b>Title:</b> SummedMagFreqDist<br>
 *  <b>Description:</b> This will sum up the rates of a bunch of different
 *  DiscreteMagFreqDist objects (e.g., to produce what will get passed back by
 *  EarthquakeSource.getMagFreqDist())<br>
 *  <b>Methods:</b>
 *  <ul>
 *    <li> add(descretMagFreqDist)
 *    <li> subtract(descretMagFreqDist)
 *  </ul>
 *  <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    March 18, 2002
 * @version    1.0
 */

public class SummedMagFreqDist extends DiscreteMagFreqDist {



    /**
     *  Description of the Method
     *
     * @param  discreteMagFreqDist  Description of the Parameter
     */
    public void add( DiscreteMagFreqDist discreteMagFreqDist ) { }


    /**
     *  Description of the Method
     *
     * @param  discreteMagFreqDist  Description of the Parameter
     */
    public void remove( DiscreteMagFreqDist discreteMagFreqDist ) { }



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
