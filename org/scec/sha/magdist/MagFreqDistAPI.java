package org.scec.sha.magdist;

import java.lang.*;
import java.util.*;

/**
 * <b>Title:</b> MagFreqDistAPI<br>
 * <b>Description:</b> This interface defines the common API that all
 * MagFreqDistributions must provide. <br><br>
 *
 * This API is necessary if in the future we implement a non-discretized
 * distribution and want to be able to treat it the same as the
 * discretized versions. <br><br>
 *
 * A Magnitude Frequency Distribution represents the rate of earthquakes (per year)
 * as a function of magnitude (not a cumulative distribution).<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public interface MagFreqDistAPI {


    public double getTotalMomentRate();

    /** tells rate of events above this magnitude */
    public double getCumulativeRate(double mag);



}
