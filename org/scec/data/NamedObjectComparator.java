package org.scec.data;

import java.util.Comparator;
import org.scec.exceptions.NamedObjectException;



/**
 *  <b>Title:</b> NamedObjectComparator<br>
 *  <b>Description:</b> This class can compare any two objects that implement
 *  the NamedObjectAPI and sort them alphabetically. This is useful for passing
 *  into a Collections.sort(Collection, Comparator) function call to sort a list
 *  alphabetically by named. One example is it's use in the ParameterEditorSheet
 *  to edit Parameters.<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public class NamedObjectComparator implements Comparator {

    /**
     *  Class name for debugging.
     */
    final static String C = "NamedObjectComparator";
    /**
     *  If true print out debug statements.
     */
    final static boolean D = false;

    /**
     *  If true sort ascending, else sort descending
     */
    private boolean ascending = true;


    /**
     *  Sets the ascending attribute of the NamedObjectComparator object
     *
     * @param  a  The new ascending value
     */
    public void setAscending( boolean a ) {
        ascending = a;
    }


    /**
     *  Gets the ascending attribute of the NamedObjectComparator object
     *
     * @return    The ascending value
     */
    public boolean isAscending() {
        return ascending;
    }


    /**
     *  Compares two NamedObject objects by name, which both implement
     *  comparable. Throws an exception if either comparing object is not an
     *  NamedObjects. Only the names of these objects are examined for
     *  comparison. This function allows sorting of named objects
     *  alphabetically.
     *
     * @param  o1                        First object to compare
     * @param  o2                        Second object to compare
     * @return                           +1 if the first object name > second
     *      object name, 0 if the two names are equal, and -1 if the first
     *      object name is < the second object's name.
     * @exception  NamedObjectException  Is thrown if either object doesn't
     *      implement NamedObjectAPI.
     * @see                              Comparable
     * @see                              NamedObjectAPI
     */
    public int compare( Object o1, Object o2 ) throws NamedObjectException {

        String S = C + ":compare(): ";
        if ( D ) {
            System.out.println( S + "Starting" );
        }
        int result = 0;

        if ( !( o1 instanceof NamedObjectAPI ) ) {
            throw new NamedObjectException( S + "First object doesn't implement NamedObjectAPI, unable to use. " + o1.getClass().getName() );
        }

        if ( !( o2 instanceof NamedObjectAPI ) ) {
            throw new NamedObjectException
                    ( S + "Second object doesn't implement NamedObjectAPI, unable to use. " + o2.getClass().getName() );
        }

        if ( D ) {
            System.out.println( S + "O1 = " + o1.toString() );
        }
        if ( D ) {
            System.out.println( S + "O2 = " + o2.toString() );
        }

        if ( D ) {
            System.out.println( S + "Getting the names: " + o1.getClass().getName() + ", " + o2.getClass().getName() );
        }

        NamedObjectAPI no1 = ( NamedObjectAPI ) o1;
        NamedObjectAPI no2 = ( NamedObjectAPI ) o2;

        String n1 = no1.getName().toString();
        String n2 = no2.getName().toString();

        result = n1.compareTo( n2 );

        if ( ascending ) {
            return result;
        } else {
            return -result;
        }
    }

}
