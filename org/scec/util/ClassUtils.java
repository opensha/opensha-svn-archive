package org.scec.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * <b>Title: ClassUtils</b><p>
 *
 * <b>Description:</b> Utility class comprised of static methods for creating classes dynamically at
 * runtime. This means, given the full package class name as a String, this utility class
 * can create an object instance. This allows for adding new classes at runtime and they can
 * be instantiated without recompiling the code. This is real useful for the IMR Tester Applet.
 * A picklist of IMRs are presented in the GUI. Once a user makes a selection, the IMR class name
 * is obtained and the class created at runtime via this utility package.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class ClassUtils {

    /** Class name used for debugging */
    private static final String C = "ClassUtils";

    /**
     * Dynamically creates the class instance with no argument constructor.
     *
     * Creates a class instance from a string of the full class name including packages.
     * This is how you dynamically make objects at runtime if you don't know which\
     * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
     * it the normal way:<P>
     *
     * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
     *
     * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
     * instead to create the same class by:<P>
     *
     * <code>BJF_1997_AttenRel imr =
     * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.scec.sha.imt.attenRelImpl.BJF_1997_AttenRel");
     * </code><p>
     *
     */
    public static Object createNoArgConstructorClassInstance( String className){
        String S = C + ": createNoArgConstructorClassInstance(): ";
        try {
            Class imrClass = Class.forName( className );
            Constructor con = imrClass.getConstructor( new Class[]{} );
            return  con.newInstance( new Object[]{} );
        } catch ( ClassCastException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( ClassNotFoundException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( NoSuchMethodException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( InvocationTargetException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( IllegalAccessException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( InstantiationException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        }

    }


    /**
     * Dynamically creates the class instance using the constructor with arguments.
     * Note: Size the params ArrayList and paramTypes should be the same
     */
    public static Object createNoArgConstructorClassInstance( ArrayList params,ArrayList paramTypes,String className){
      String S = C + ": createNoArgConstructorClassInstance(): ";
      try {

        int size = params.size();
        //creating the class array to store the param types
        Class[] paramTypesClass = new Class[size];

        //creating the Object Array of the parameters that will be used to create the
        // new instance of te class.
        Object[] paramObject = new Object[size];
        for(int i=0;i<size;++i){
          paramObject[i] = params.get(i);
          paramTypesClass[i] = (Class)paramTypes.get(i);
        }

        System.out.println("Size of params:"+size+"   size of paramTypes:"+paramTypesClass.length+
                           "   Name of the class:"+className);
        Class erfClass = Class.forName( className );
        //creating the class constructor for the ERF with the parameter type as paramTypes
        Constructor con = erfClass.getConstructor( paramTypesClass );
        System.out.println("Name of the class whose constructor is created: "+con.getName());
        //returns the new instance of the class
        return  con.newInstance( paramObject );
      } catch ( ClassCastException e ) {
        System.out.println(S + e.toString());
        throw new RuntimeException( S + e.toString() );
      } catch ( ClassNotFoundException e ) {
        System.out.println(S + e.toString());
        throw new RuntimeException( S + e.toString() );
      } catch ( NoSuchMethodException e ) {
        System.out.println(S + e.toString());
        throw new RuntimeException( S + e.toString() );
      } catch ( InvocationTargetException e ) {
        System.out.println(S + e.toString());
        throw new RuntimeException( S + e.toString() );
      } catch ( IllegalAccessException e ) {
        System.out.println(S + e.toString());
        throw new RuntimeException( S + e.toString() );
      } catch ( InstantiationException e ) {
        System.out.println(S + e.toString());
        throw new RuntimeException( S + e.toString() );
      }

    }




    /** Checks that a string is not null, or not equal to empty string "" */
    public static boolean isEmptyString(String val){
        if ( ( val == null ) || ( val.equals( "" ) ) ) return true;
        else return false;
    }

}
