package org.scec.sha.magdist;

import org.scec.data.function.DiscretizedFuncAPI;
import java.lang.*;

/**
 * <b>Title:</b> GR_MagFreqDist<br>
 * <b>Description:</b> This is a subclass of MagFreqDist, where the distribution is
 * Gutenberg Richter<br>
 * <b>Note:</b> Any changes to magLower and magUpper must fall within the bounds
 * of minX and maxX, and must be closer than the tolerance to one of the
 * x-values.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class GR_MagFreqDist extends IncrementalMagFreqDist
{

    /* *******************/
    /** @todo  Variables */
    /* *******************/

	protected double magLower;
	protected double magUpper;
	protected double b;
	protected double a;

    /*
      the following parameters are used

      MagLower
      MagUpper
      b 		(b-value)
      a 		(a-value).

      With TotMomentRate, there are a total of five parameters, four
      of which are unique.  We need to be able to set any four and
      have the fifth comuted.  The constructor will only allow one
      option (since they are all doubles).

    */

    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /**
     *
     */
    public GR_MagFreqDist(
        double magLower,
        double magUpper,
        int num,
        double b,
        double a)
    {


    }

    /**
     *
     */
    public GR_MagFreqDist(
        double minMag,
        double deltaMag,
        int numMag
    ){ super(minMag, deltaMag, numMag); }


    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

	public double getMagLower() { return magLower; }

	public double getMagUpper() { return magUpper; }

	public double get_b() { return b; }

	public double get_a() { return a; }


    /**
     * Note Default: MinMag = MagLower and MaxMag = MagUpper
     * The resultant TotalMomentRate will not be exact due to the
     * magnitude discretization, follow up with scaleToTotalMomentRate()
     * to make the a-value that which is not exact.
     */
    public void setAllButMagLower(
        double magUpper,
        double b,
        double a,
        double totMomentRate)
    {

    }

    /**
     * Note Default: MinMag = MagLower and MaxMag = MagUpper
     * Note: The resultant TotalMomentRate will not be exact due to the
     * magnitude discretization, follow up with scaleToTotalMomentRate()
     * to make the a-value that which is not exact.
     */
    public void setAllButMagUpper(
        double magLower,
        double b,
        double a,
        double totMomentRate)
    {

    }


    /**
     * Note Default: MinMag = MagLower and MaxMag = MagUpper
     */
    public void setAllBut_a(
        double magLower,
        double magUpper,
        double b,
        double totMomentRate)
    {

    }

    /**
     * Note Default: MinMag = MagLower and MaxMag = MagUpper
     */
    public void setAllBut_b(
        double magLower,
        double magUpper,
        double a,
        double totMomentRate)
    {

    }

    /**
     * Note Default: MinMag = MagLower and MaxMag = MagUpper
     */
    public void setAllButTotalMomentRate(
        double magLower,
        double magUpper,
        double b,
        double a)
    {

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
