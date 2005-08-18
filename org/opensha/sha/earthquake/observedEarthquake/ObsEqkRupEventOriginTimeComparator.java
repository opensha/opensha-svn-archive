package org.opensha.sha.earthquake.observedEarthquake;

import java.util.*;

/**
 * <p>Title: ObsEqkRupEventOriginTimeComparator</p>
 *
 * <p>Description: It compares 2 observed EqkRupture events Object \
 * based on their origin time.
 * </p>
 *
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class ObsEqkRupEventOriginTimeComparator
    implements Comparator {

  /**
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
    ObsEqkRupture rupEvent1 = (ObsEqkRupture)object1;
    ObsEqkRupture rupEvent2 = (ObsEqkRupture)object2;
    if(rupEvent1.getOriginTime().before(rupEvent2.getOriginTime()))
      return -1;
    else if(rupEvent1.getOriginTime().equals(rupEvent2.getOriginTime()))
      return 0;
    else
      return 1;
  }

}
