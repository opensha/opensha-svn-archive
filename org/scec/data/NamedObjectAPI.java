package org.scec.data;

/**
 *  <b>Title:</b> NamedObjectAPI<br>
 *  <b>Description:</b> This interface flags all implementing classes as being
 *  NamedObjects, i.e. they all have a name field. Used in all Parameters, among
 *  other areas.<br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public interface NamedObjectAPI {

    /**
     *  Returns the name of this object
     *
     * @return    The name value
     */
    public String getName();
}
