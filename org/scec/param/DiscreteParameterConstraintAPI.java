package org.scec.param;
import java.util.ListIterator;

import java.util.Vector;
/**
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author
 * @created    February 20, 2002
 * @version    1.0
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
