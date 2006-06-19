package org.opensha.sha.magdist;

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
