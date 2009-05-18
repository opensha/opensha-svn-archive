package org.opensha.commons.data;

import java.io.Serializable;

/**
 *  <b>Title:</b> NamedObjectAPI<p>
 *
 *  <b>Description:</b> This interface flags all implementing classes as being
 *  NamedObjects, i.e. they all have a name field and implements getName().
 *  Used in all Parameters, among other areas.<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public interface NamedObjectAPI extends Serializable {
    /** Returns the name of this object */
    public String getName();
}
