package org.opensha.sha.magdist;

import org.opensha.calc.MomentMagCalc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.exceptions.*;

/**
 * <p>Title: ArbIncrementalMagFreqDist.java </p>
 *
 * <p>Description: This class allows the user to create a Arbitrary
 * IncrementalMagFreqDist.</p>
 *
 * @author Nitin Gupta , Ned Field
 * @version 1.0
 */
public class ArbIncrementalMagFreqDist
    extends IncrementalMagFreqDist {

  public  static String NAME = "Arb Mag Freq Dist";


  public ArbIncrementalMagFreqDist(double min, double max, int num) throws
      DiscretizedFuncException, InvalidRangeException {
    super(min, max, num);
  }


  /**
   * This sets each mag & rate value here according to the function passed in.  Each x-axis value of
   * the function passed in is rounded to the nearest x-axis value here, and rates are summed if more
   * than one x-axis value of the input function fall on the same x-axis value here.  X-axis values from
   * the input function that are out of the bounds of the present MFD are ignored.  Zeros will be present
   * at any x-axis values where there wasn't a corresponding value in the input function.
   * If the preserveRates boolean is false, then the moment rate of each point
   * is preserved (although the total moment rates of the two functions may differ if any
   * endpoints were ignored).  Otherwise total rates are preserved (assuming no endpoints are
   * ignored).
   * 
   * @param func the new Magnitude Frequency distribution to be added
   * @param preserveRates specifies whether to preserve rates or moment rates
   */

  public void setResampledMagFreqDist(DiscretizedFuncAPI func, boolean preserveRates) {

	  for (int i=0;i<func.getNum();++i) {     // add the y values from this new distribution
		  addResampledMagRate(func.getX(i), func.getY(i), preserveRates);
	  }
  }
  
  /**
   * This and adds the rate & mag passed in to the MFD after rounding to the nearest x-axis
   * value (ignoring those out of range).  If the preserveRates boolean is false, then the moment 
   * rate of the point is preserved (assuming it's in range).  Otherwise the rate of that point 
   * is preserved.
   * @param mag & rate to be added
   * @param preserveRates specifies whether to preserve rates or moment rates
   */

  public void addResampledMagRate(double mag, double rate, boolean preserveRates) {

	  int index = (int)Math.round((mag-minX)/delta);
	  if(index<0 || index>num) return;
	  if(preserveRates)
		  set(index,this.getY(index)+ rate);
	  else {
		  double newRate = rate*MomentMagCalc.getMoment(mag)/MomentMagCalc.getMoment(this.getX(index));
		  set(index,this.getY(index)+ newRate);
	  }
  }

  /**
   * returns the name of the class
   * @return
   */
  public String getDefaultName() {
    return NAME;
  }

  /**
   * Returns the default Info String for the Distribution
   * @return String
   */
  public String getDefaultInfo() {
    return "Arbitrary Incremental Magnitude Frequency Dististribution";
  }
}
