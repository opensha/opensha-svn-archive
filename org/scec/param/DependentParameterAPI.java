package org.scec.param;

import java.util.ListIterator;
import org.scec.exceptions.ParameterException;

/**
 * <b>Title:</b> DependentParameterAPI<p>
 *
 * <b>Description:</b> Implementation classes of this interface are
 * known as dependent parameters, i.e. it's values and/or constraints
 * depend on other independent parametes. An implementation class will
 * maintain a list of parameters that it depends on. <p>
 *
 * This interface simply states the functions for list accessors
 * to maintain this list of independent parameters. Standard list functions
 * are implemented (paraphrasing) such as get() , set(), remove() iterator(),
 * etc. <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public interface DependentParameterAPI extends ParameterAPI {

    // ListIterator guarantees the order that you add parameters

    /**
     * Returns an iterator of all indepenedent parameters this parameter
     * depends upon. Returns the parametes in the order they were added.
     */
    public ListIterator getIndependentParametersIterator();

    /**
     * Locates and returns an independent parameter from the list if it
     * exists. Throws a parameter excpetion if the requested parameter
     * is not one of the independent parameters.
     *
     * @param name  Parameter name to lookup.
     * @return      The found independent Parameter.
     * @throws ParameterException   Thrown if not one of the independent parameters.
     */
    public ParameterAPI getIndependentParameter(String name)throws ParameterException;

    /** Set's a complete list of independent parameters this parameter requires */
    public void setIndependentParameters(ParameterList list);

    /** Adds the parameter if it doesn't exist, else throws exception */
    public void addIndependentParameter(ParameterAPI parameter) throws ParameterException;

    /** Returns true if this parameter is one of the independent ones */
    public boolean containsIndependentParameter(String name);

    /** Removes the parameter if it exist, else throws exception */
    public void removeIndependentParameter(String name) throws ParameterException;

    /** Returns all the names of the independent parameters concatenated */
    public String getIndependentParametersKey();

}
