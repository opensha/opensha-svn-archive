package org.scec.data;

import java.util.*;
import org.scec.exceptions.*;


/**
 *  <b>Title:</b> LocationList<br>
 *  <b>Description:</b> List container for location. Specialized version of
 *  ArrayList. Can add special SortedIterator that returns the Locations in
 *  geographical order<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class LocationList {

    protected final static String C = "LocationList";
    protected final static boolean D = false;

    /**
     *  Contains the list of Locations
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

    private void checkIndex(int index) throws InvalidRangeException {

        if( size() < index + 1 ) throw new InvalidRangeException(
            C + ": getLocationAt(): " +
            "Specified index larger than array size."
        );

    }

    /**
     *  adds the parameter if it doesn't exist, else throws exception
     *
     * @param  location  Description of the Parameter
     * @param  index     Description of the Parameter
     */
    public void replaceLocationAt( Location location, int index ) throws InvalidRangeException  {
        checkIndex(index);
        locations.add(index, location);
    }


    /**
     *  adds the parameter to the end of the list
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


    /** reverses the order of the points */
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
