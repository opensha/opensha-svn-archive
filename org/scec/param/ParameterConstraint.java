package org.scec.param;

import org.scec.exceptions.EditableException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class ParameterConstraint implements ParameterConstraintAPI {

    /**
     *  Class name for debugging.
     */
    protected final static String C = "ParameterConstraint";
    /**
     *  If true print out debug statements.
     */
    protected final static boolean D = false;

    public ParameterConstraint() {}

    /**
     *  This value indicates if the value is editable after it is first set.
     */
    protected boolean editable = true;
    protected String name = null;

    protected boolean nullAllowed = false;

    /**
     *  Every parameter constraint has a name, this function returns that name.
     *
     * @return    The name value
     */
    public String getName(){ return name; }

    /**
     *  Every parameter constraint has a name, this function sets that name.
     *
     * @return    The name value
     */
    public void setName(String name) throws EditableException{
        checkEditable(C + ": setName(): ");
        this.name = name;
    }

    /**
     *  Disables editing units, info, constraints, et. Basically all set()s disabled
     *  except for setValue()
     */
    public void setNonEditable() { editable = false; }

    /**
     *  Determines if the value can be edited, i.e. changed once set.
     *
     * @return    The editable value
     */
    public boolean isEditable() { return editable; }

    protected void checkEditable(String S) throws EditableException{
        if( !editable ) throw new EditableException( S +
            "This parameter is currently not editable"
        );
    }

    /**
     *  Returns a copy so you can't edit or damage the origial
     *
     * @return    Exact copy of this object's state
     */
    public abstract Object clone();


    public void setNullAllowed(boolean nullAllowed) throws EditableException {
        checkEditable(C + ": setNullAllowed(): ");
        this.nullAllowed = nullAllowed;
    }
    public boolean isNullAllowed() {
        return nullAllowed;
    }
}
