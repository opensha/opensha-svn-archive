package org.opensha.data;

import java.util.*;
import org.opensha.exceptions.*;
import org.opensha.calc.RelativeLocation;

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

public class LocationList implements java.io.Serializable{

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
     *  exception. Recall that these locations are stored in a ArrayList, which is
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
   *  Replaces the location in the list at the specified index if it is a valid index.
   *  An exception is thrown if the specified index is invalid.
   *
   * @param  location  Location that is to be added
   * @param  index     location in the list at which this event needs to be added.
   */

    public void replaceLocationAt( Location location, int index ) throws InvalidRangeException  {
        checkIndex(index);
        locations.set(index, location);
    }


    /**
     * Sorts the list of locations based on Latitude. If Latitude is same then
     * sorts on Longitude.
     */
    public void sort(){
      Collections.sort(locations, new LocationComparator());
    }

    /**
     *  Adds the Locatrion to the end of the list.
     *
     * @param  location  The feature to be added to the Location attribute
     */
    public void addLocation( Location location ) { locations.add(location); }

    /**
     * Inserts the location at the given index.
     *
     * @param  location  The feature to be added to the Location attribute
     * @param index int Index at which location is to be added
     *
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     */
    public void addLocationAt( Location location, int index ) {
      locations.add(index,location);
    }




    /**
     *  Returns a list iterator of all Locations in this list, in the order they
     *  were added to the list
     *
     * @return    Description of the Return Value
     */
    public ListIterator listIterator() { return locations.listIterator(); }


    /**  Removes all Locations from this list */
    public void clear() { locations.clear(); }


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

    /**
     * This computes the distance (in km) between the given loc and the closest point in the LocationList
     * @param loc
     * @return
     */
    public double getHorzDistToClosestLocation(Location loc) {
      double min = Double.MAX_VALUE, temp;
      Iterator it = this.listIterator();
      while(it.hasNext()) {
        temp = RelativeLocation.getHorzDistance(loc,(Location) it.next());
        if (temp < min) min = temp;
      }
      return min;
    }


    /**
     * Compares if the 2 LocationList.
     * It checks if both locationlist object contains the same Locations objects.
     * @param obj LocationList Obj
     * @return 0 if both object are same else return -1
     */
    public int compareTo(Object obj){

      boolean compareFlag = true;
      if (! (obj instanceof LocationList)) {
        throw new ClassCastException(C +
                                     "Object not a LocationList, unable to compare");
      }

      LocationList locList = (LocationList) obj;

      ListIterator it = locList.listIterator();
      ListIterator it1 = listIterator();

      if (size() != locList.size())
        return -1;

      while (it.hasNext()) {
        Location loc = (Location) it.next();
        Location loc1 = (Location) it1.next();
        compareFlag =loc1.equals(loc);
        if(compareFlag == false)
          break;
      }

      if(!compareFlag)
        return -1;

      return 0;
    }


    /**
     * Returns the index of location with the given location list.
     * @param loc Location finds index of this location
     * @return int index in the List.
     */
    public int getLocationIndex(Location loc){
      return locations.indexOf(loc);
    }


    /**
     * This computes the shortest horizontal distance (in km) from the given loc
     * to any point on the line defined by connecting the points in this location list.
     * This is approximate in that iy uses the RelativeLocation.getApproxHorzDistToLine(*) method
     * @param loc
     * @return
     */
    public double getMinHorzDistToLine(Location loc) {
      double min = Double.MAX_VALUE, temp;

      // loop over each line segment
      for(int i = 1; i < size(); i++) {
        temp = RelativeLocation.getApproxHorzDistToLine(loc,getLocationAt(i-1),getLocationAt(i));
        if (temp < min) min = temp;
      }
      return min;
    }

}
