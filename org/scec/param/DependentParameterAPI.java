package org.scec.param;

import java.util.ListIterator;
import org.scec.exceptions.ParameterException;

// Fix - Needs more comments

/**
 * <b>Title:</b> DependentParameterAPI<p>
 * <b>Description:</b> <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public interface DependentParameterAPI extends ParameterAPI {

    // ListIterator guarantees the order that you add parameters
    public ListIterator getIndependentParametersIterator();
    public ParameterAPI getIndependentParameter(String name)throws ParameterException;

    // This will clone the parameters of the list that you pass in
    public void setIndependentParameters(ParameterList list);

    /** Adds the parameter if it doesn't exist, else throws exception */
    public void addIndependentParameter(ParameterAPI parameter) throws ParameterException;

    public boolean containsIndependentParameter(String name);
    public void removeIndependentParameter(String name) throws ParameterException;

    public String getIndependentParametersKey();

}
