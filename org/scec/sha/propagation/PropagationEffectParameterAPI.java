package org.scec.sha.propagation;

import org.scec.data.*;
import org.scec.sha.earthquake.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public interface PropagationEffectParameterAPI {


    public Object getValue(ProbEqkRupture potentialEarthquake, Site site);

    /** The ProbEqkRupture must have already been set */
    public Object getValue(Site site);

    /** The Site must have already been set */
    public Object getValue(ProbEqkRupture potentialEarthquake);

    /** The ProbEqkRupture and Site must have already been set */
    public Object getValue();

    public void setSite(Site site);
    public Site getSite();

    public void setProbEqkRupture(ProbEqkRupture potentialEarthquake);
    public ProbEqkRupture getProbEqkRupture();

    public Object clone();


}
