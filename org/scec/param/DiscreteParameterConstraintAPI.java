package org.scec.param;
import java.util.ListIterator;

import java.util.Vector;

// Fix - Needs more comments

/**
 * <b>Title:</b> DiscreteParameterConstraintAPI<p>
 * <b>Description:</b> <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public interface DiscreteParameterConstraintAPI extends ParameterConstraintAPI {

    /**
     *  Returns cloned vector of allowed values, unable to modify original
     *  values
     *
     * @return    All allowed values
     */
    public Vector getAllowedValues();


    /**
     *  Returns Iterator over real values, able to modify original
     *
     * @return    Iterator over all allowed values
     */
    public ListIterator listIterator();

    public int size();
}
