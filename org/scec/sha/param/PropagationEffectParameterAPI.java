package org.scec.sha.param;

import org.scec.data.*;
import org.scec.sha.earthquake.*;

/**
* <p>Title: PropagationEffectParameterAPI</p>
* <p>Description: Interface that PropagationEffect
* Parameters must implement. </p>
*
* Propagation Effect Parameters are a specific subclass
* of parameters that deal with earthquake probability
* variables. Their defining characteristics are that
* they take two independent variables, a Site and ProbEqkRupture
* and then can calculate their own value. Their use is distinct
* from regular parameters in that setValue() is typically
* not called. That is the only way to set standard
* parameters. <p>
*
* This API defines several gatValue() functions that take
* different combinations of Site and ProbEqkRupture that
* will make this parameter recalculate itself, returning the
* new value. <p>
*
* @author Steven W. Rock
* @version 1.0
*/
public interface PropagationEffectParameterAPI {


     /** Sets the independent variables (Site and ProbEqkRupture) then calculates and returns the value */
    public Object getValue(ProbEqkRupture probEqkRupture, Site site);

    /** Sets the site and recalculates the value. The ProbEqkRupture must have already been set */
    public Object getValue(Site site);

    /** Sets the ProbEqkRupture and recalculates the value. The Site must have already been set */
    public Object getValue(ProbEqkRupture probEqkRupture);

    /** Sets the independent variables (Site and ProbEqkRupture) then calculates the value */
    public void setValue(ProbEqkRupture probEqkRupture, Site site);

    /** The ProbEqkRupture and Site must have already been set */
    public Object getValue();

    /** Sets the Site and the value is recalculated */
    public void setSite(Site site);
    /** Returns the Site that set this value */
    public Site getSite();

    /** Sets the ProbEqkRupture associated with this Parameter, and the value is recalculated */
    public void setProbEqkRupture(ProbEqkRupture probEqkRupture);
    /** Returns the ProbEqkRupture that set this value */
    public ProbEqkRupture getProbEqkRupture();

    /**
     * Standard Java function. Creates a copy of this class instance
     * so originaly can not be modified
     */
    public Object clone();


}
