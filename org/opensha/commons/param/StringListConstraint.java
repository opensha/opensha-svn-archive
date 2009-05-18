package org.opensha.commons.param;

import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.commons.exceptions.ConstraintException;

/**
 * <p>Title: StringListConstraint.java </p>
 * <p>Description: This constraint is used for allowing multiple String selections
 * by the user. It is typically presented in GUI as a JList where multiple item
 * selection is allowed. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StringListConstraint extends StringConstraint {


  /** No-Arg constructor for the StringConstraint object. Calls the super() constructor. */
    public StringListConstraint() { super(); }

  /**
   * Accepts a list of strings to be displayed in the list. User can choose
   * one or more items from this list
   * @param strings
   * @throws ConstraintException
   */
  public StringListConstraint(ArrayList strings) throws ConstraintException {
    super(strings);
  }

  /**
    * Determine if the new values being set are allowed. First checks
    * if null and if nulls are allowed. Then verifies the Object is
    * a ArrayList or String object. Finally the code verifies that the Strings in the ArrayList are
    * in the allowed strings vector. If any of these checks fails, false
    * is returned.
    *
    * @param  obj  Object to check if allowed String
    * @return      True if the value is allowed
    */
   public boolean isAllowed( Object obj ) {
       if( nullAllowed && ( obj == null ) ) return true;
       else if ( ( obj instanceof ArrayList ) ) return isAllowed((ArrayList)obj);
       else if ( ( obj instanceof String ) ) return isAllowed((String)obj);
       else  return false;
   }

   /**
    * Determines whether Strings in the ArrayList are allowed.
    * It first checks that all the elements in arraylist should be String objects.
    * Then it checks that all String objects in ArrayList are included in
    * the list of allowed Strings
    *
    * @param valsList
    * @return
    */
   private boolean isAllowed(ArrayList valsList) {
     int size = valsList.size();
     for(int i=0; i<size; ++i) {
       if(!(valsList.get(i) instanceof String)) return false;
       if(!isAllowed((String)valsList.get(i))) return false;
     }
     return true;
   }

   /**
    * Then it checks that string is included in
    * the list of allowed Strings
    *
    * @return
    */
   private boolean isAllowed(String str) {
     return this.containsString(str);
   }


   /** Returns a copy so you can't edit or damage the origial. */
   public Object clone() {
       StringListConstraint c1 = new StringListConstraint();
       c1.name = name;
       ArrayList v = getAllowedStrings();
       ListIterator it = v.listIterator();
       while ( it.hasNext() ) {
           String val = ( String ) it.next();
           c1.addString( val );
       }
       c1.setNullAllowed( nullAllowed );
       c1.editable = true;
       return c1;
   }



}