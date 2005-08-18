package org.opensha.data;

import java.util.*;

/**
 * <p>Title: EqkRuptureMagComparator</p>
 *
 * <p>Description: It compares 2 location Object based on their Lattitudes.
 * </p>
 *
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class LocationComparator
    implements Comparator, java.io.Serializable{

  /**
   * Compares 2 location objects. Comparision is done based on location latitudes.
   * If location latitudes are equal then it will comparison will be done based on
   * Longitudes.
   *
   * Compares its two arguments for order. Returns a negative integer, zero, or
   * a positive integer as the first argument is less than, equal to, or greater than the second.
   * The implementor must ensure that sgn(compare(x, y)) == -sgn(compare(y, x))
   * for all x and y. (This implies that compare(x, y) must throw an exception if
   * and only if compare(y, x) throws an exception.)
   * The implementor must also ensure that the relation is transitive:
   * ((compare(x, y)>0) && (compare(y, z)>0)) implies compare(x, z)>0.
   * Finally, the implementer must ensure that compare(x, y)==0 implies that
   * sgn(compare(x, z))==sgn(compare(y, z)) for all z.
   * It is generally the case, but not strictly required that
   * (compare(x, y)==0) == (x.equals(y)). Generally speaking, any
   * comparator that violates this condition should clearly indicate this fact.
   * The recommended language is "Note: this comparator imposes orderings that
   * are inconsistent with equals."
   * @param object1 Object the first object to be compared.
   * @param object2 Object the second object to be compared.
   * @return int a negative integer, zero, or a positive integer
   *  as the first argument is less than, equal to, or greater
   * @todo Implement this java.util.Comparator method
   */
  public int compare(Object object1, Object object2) {
    Location loc1 = (Location) object1;
    double loc1Lat = Double.parseDouble(Location.latLonFormat.format(loc1.getLatitude()));
    double loc1Lon = Double.parseDouble(Location.latLonFormat.format(loc1.getLongitude()));

    Location loc2 = (Location) object2;
    double loc2Lat = Double.parseDouble(Location.latLonFormat.format(loc2.getLatitude()));
    double loc2Lon = Double.parseDouble(Location.latLonFormat.format(loc2.getLongitude()));

    if(loc1Lat < loc2Lat)
      return -1;
    else if(loc1Lat > loc2Lat)
      return 1;
    else if(loc1Lat == loc2Lat){
      if(loc1Lon < loc2Lon)
        return -1;
      else if(loc1Lon > loc2Lon)
        return 1;
      else
        return 0;
    }

    return 0;
  }
}
