package org.scec.data;

import java.util.*;
import org.scec.exceptions.*;

/**
 *  <b>Title:</b> LocationList<p>
 *
 * <b>Description:</b> List container for location objects. This class is a
 * specialized version of an ArrayList. Can add special SortedIterator
 * that returns the Locations in geographical order<p>
 *
 * As with an ArrayList this class provides all the standard methods to
 * opereate on elements within a list, such as (paraphrasing)
 * add(), get(), delete(), rep/ace(), num(), Iterator(), clearAll(). <p>
 *
 * Note: It is good design that the the internal data structure (ArrayList)
 * is hidden to the "outside world". This is the idea of encapsulation in
 * object oriented programming. The internal complexity of an object
 * should not be exposed. There is no need for calling classes to know this
 * information so it keeps the API simpler.<p>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class LocationList {

    /** Class name used for debugging purposes */
    protected final static String C = "LocationList";
    /** if true print out debugging statements */
    protected final static boolean D = false;

    /**
     *  Internal data structure (ArrayList ) that contains the
     *  list of Locations.
     */
    protected ArrayList locations = new ArrayList();


    /**
     *  Returns parameter at the specified index if exist, else throws
     *  exception. Recall that these locations are stored in a Vector, which is
     *  like an array. Therefore you can access items by index.
     *
     * @param  index  Description of the Parameter
     * @return        The locationAt value
     */
    public Location getLocationAt( int index ) throws InvalidRangeException {
        checkIndex(index);
        return (Location)locations.get( index );
    }

    /** Validates that an index falls within the internal data structure range */
    private void checkIndex(int index) throws InvalidRangeException {

        if( size() < index + 1 ) throw new InvalidRangeException(
            C + ": getLocationAt(): " +
            "Specified index larger than array size."
        );

    }

    /**
     *  Adds the Location at the specified index if it is a valid index.
     *  All subsequent Locations are shifted by one index.
     *  An exception is thrown if the specified index is invalid.
     *
     * @param  location  Description of the Parameter
     * @param  index     Description of the Parameter
     */
    public void replaceLocationAt( Location location, int index ) throws InvalidRangeException  {
        checkIndex(index);
        locations.add(index, location);
    }


    /**
     *  Adds the Locatrion to the end of the list.
     *
     * @param  location  The feature to be added to the Location attribute
     */
    public void addLocation( Location location ) { locations.add(location); }


    /**
     *  Returns a list iterator of all Locations in this list, in the order they
     *  were added to the list
     *
     * @return    Description of the Return Value
     */
    public ListIterator listIterator() {
        return locations.listIterator();
    }


    /**
     *  Removes all Locations from this list
     */
    public void clear() {
        locations.clear();
    }


    /**
     * Reverses the order of Locations. Has the
     * effect of reversing the Iterator.
     */
    public void reverse(){

        int size = locations.size();
        int reverseIndex = size - 1;

        ArrayList newList = new ArrayList();
        for( int i = reverseIndex; i >= 0; i--){
            newList.add( locations.get(i) );
        }

        this.locations = newList;
    }


    /**
     *  Returns the number of Locations in this list
     *
     * @return    Description of the Return Value
     */
    public int size() {
        return locations.size();
    }

    private final static String TAB = "  ";
    /** Helper debugging method that prints out all Locations in this list */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append('\n');
        b.append(TAB + "Size = " + size());

        ListIterator it = listIterator();
        while( it.hasNext() ){

            Location location = (Location)it.next();
            b.append(TAB + location.toString());
        }
        return b.toString();
    }

}
