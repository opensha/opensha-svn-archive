package org.scec.param;
import java.util.ListIterator;

import java.util.ArrayList;
import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.EditableException;
import org.scec.data.Location;

/**
 * <b>Title:</b> LocationConstraint<p>
 *
 * <b>Description:</b> This constraint contains a list of possible allowed
 * location values. These can typically be presented in a GUI picklist. This is
 * the same fucntionality for all StringConstraint.
 *
 * @author     Nitin Gupta
 * @created    January 25,2005
 * @version    1.0
 */

public class LocationConstraint
        extends ParameterConstraint
        implements DiscreteParameterConstraintAPI
{

    /** Class name for debugging. */
    protected final static String C = "LocationConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    /** ArrayList list of possible locations, i.e. allowed values. */
    private ArrayList locations = new ArrayList();


    /** No-Arg constructor for the LocationConstraint object. Calls the super() constructor. */
    public LocationConstraint() { super(); }


    /**
     *  Constructor for the LocationConstraint object. Sets all allowed locations
     *  via a ArrayList, which is copied into this object's internal storage
     *  structure.
     *
     * @param  locations       ArrayList of allowed locations
     * @exception  ConstraintException  Thrown if the passed in vector size is 0
     */
    public LocationConstraint( ArrayList locations ) throws ConstraintException {
        if ( locations.size() > 0 ) this.locations = locations;
        else {
            String S = C + ": Constructor(ArrayList locations): ";
            throw new ConstraintException( S + "Input vector of constraint values cannot be empty" );
        }
    }


    /**
     *  Sets all allowed locations via a ArrayList, which is copied into this
     *  object's internal storage structure.
     *
     * @param  locations                  ArrayList of allowed locations
     * @exception  ConstraintException  Thrown if the passed in vector size is 0
     */
    public void setLocations( ArrayList locations ) throws ConstraintException, EditableException {

        String S = C + ": setLocations(): ";
        checkEditable(S);
        if ( ( locations != null ) && ( locations.size() > 0 ) ) this.locations = locations;
        else throw new ConstraintException( S + "Input vector of constraint values cannot be null or empty" );

    }

    /** Returns a cloned ArrayList of the allowed Locations. */
    public ArrayList getAllowedLocations() { return ( ArrayList ) locations.clone(); }

    /** Returns a cloned ArrayList of the allowed Locations. */
    public ArrayList getAllowedValues() { return getAllowedLocations(); }


    /**
     *  Determine if the new value being set is allowed. First checks
     * if null and if nulls are allowed. Then verifies the Object is
     * a Location. Finally the code verifies that the Location is
     * in the allowed locations vector. If any of these checks fails, false
     * is returned.
     *
     * @param  obj  Object to check if allowed Location
     * @return      True if the value is allowed
     */
    public boolean isAllowed( Object obj ) {

        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof Location ) ) return false;
        else if ( !containsLocation((Location)obj) ) return false;
        else return true;
    }

    /** Returns an Iterator over allowed values.*/
    public ListIterator listIterator() { return locations.listIterator(); }

    /** Adds a Location to the list of allowed values, if this constraint is editable. */
    public void addLocation( Location loc ) throws EditableException {
        checkEditable(C + ": addLocation(): ");
        if ( !containsLocation( loc ) ) locations.add( loc );
    }


    /** Removes a Location from the list of allowed values, if this constraint is editable. */
     public void removeLocation( Location loc ) throws EditableException {
        checkEditable(C + ": removeLocation(): ");
        if ( containsLocation( loc ) ) locations.remove( loc );

    }


    /** Returns true if the location is in the allowed list, false otherwise*/
    public boolean containsLocation( Location loc ) {
        if ( locations.contains( loc) ) return true;
        else return false;
    }



    /** Returns number of allowed values. */
    public int size() { return locations.size(); }


    /**
     *  Prints out the current state of this parameter, i.e. classname and
     *  allowed values. Useful for debugging.
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( C );

        if( name != null) b.append( TAB + "Name = " + name + '\n' );
        //b.append( TAB + "Is Editable = " + this.editable + '\n' );
        b.append( TAB + "Allowed values = " );

        boolean first = true;
        ListIterator it = locations.listIterator();
        while ( it.hasNext() ) {
            if ( !first ) {
                b.append( TAB + ", " + it.next().toString() );
            } else {
                b.append( TAB + it.next().toString() );
                first = false;
            }
        }
        b.append( TAB + "Null Allowed = " + this.nullAllowed+ '\n' );
        return b.toString();
    }


    /** Returns a copy so you can't edit or damage the origial. */
    public Object clone() {

        LocationConstraint c1 = new LocationConstraint();
        c1.name = name;
        ArrayList v = getAllowedLocations();
        ListIterator it = v.listIterator();
        while ( it.hasNext() ) {
            Location loc = ( Location ) it.next();
            c1.addLocation( loc );
        }

        c1.setNullAllowed( nullAllowed );
        c1.editable = true;
        return c1;
    }
}
