package org.scec.param;

import org.scec.util.*;
import org.scec.exceptions.EditableException;
import org.scec.data.*;

/**
 *  <b>Title:</b> ParameterConstraintAPI<br>
 *  <b>Description:</b> This is an Interface that holds a constraint, and
 *  provides a method that checks if an argument passed is within the
 *  constraint. <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Sid Hellman
 * @created    February 21, 2002
 * @version    1.0
 */

public interface ParameterConstraintAPI extends NamedObjectAPI{

    /**
     *  Every parameter constraint has a name, this function returns that name.
     *
     * @return    The name value
     */
    public String getName();

    /**
     *  Every parameter constraint has a name, this function sets that name.
     *
     * @return    The name value
     */
    public void setName(String name) throws EditableException ;

    /**
     *  Determine if the new value being set is allowed.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isAllowed( Object obj );


    /**
     *  Determines if the value can be edited, i.e. changed once set.
     *
     * @return    The editable value
     */
    public boolean isEditable();


    /**
     *  Disables editing the value once it is set.
     */
    public void setNonEditable();


    /**
     *  Returns a copy so you can't edit or damage the origial
     *
     * @return    Exact copy of this object's state
     */
    public Object clone();


    public void setNullAllowed(boolean nullAllowed) throws EditableException;
    public boolean isNullAllowed();

}
