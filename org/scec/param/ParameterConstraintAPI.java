package org.scec.param;

import org.scec.util.*;
import org.scec.exceptions.EditableException;
import org.scec.data.*;

/**
 * <b>Title:</b> ParameterConstraintAPI<p>
 *
 * <b>Description:</b> This is the interface that all
 * constraints must implement. Constraints store such information
 * as if a value is allowed, if the data is editable, i.e. functions
 * that restrict or allow setting new values on parameters.<p>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface ParameterConstraintAPI extends NamedObjectAPI{

    /**  Every parameter constraint has a name, this function returns that name.  */
    public String getName();

    /**  Every parameter constraint has a name, this function sets that name.  */
    public void setName(String name) throws EditableException ;

    /**
     *  Determine if the new value being set is allowed.
     * @param  obj  Object to check if allowed via constraints.
     * @return      True if the value is allowed.
     */
    public boolean isAllowed( Object obj );


    /**
     *  Determines if the value can be edited, i.e. changed once set.
     * @return    The editable value.
     */
    public boolean isEditable();


    /** Disables editing the value once it is set. */
    public void setNonEditable();


    /**
     *  Returns a copy so you can't edit or damage the origial.
     * @return    Exact copy of this object's state.
     */
    public Object clone();

    /** A parameter may or may not allow null values. That permission is set here. */
    public void setNullAllowed(boolean nullAllowed) throws EditableException;
    /** A parameter may or may not allow null values. That permission is checked here. */
    public boolean isNullAllowed();

}
