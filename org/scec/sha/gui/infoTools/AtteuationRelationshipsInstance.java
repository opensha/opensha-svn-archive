package org.scec.sha.gui.infoTools;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * <p>Title: AtteuationRelationshipsInstance </p>
 * <p>Description: Creates the list of the AttenuationRelationship Objects from
 * their classnames.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created March 05,2004
 * @version 1.0
 */

public class AtteuationRelationshipsInstance {


  private static final String C= "AtteuationRelationshipsInstance";

  /**
   *  The object class names for all the supported attenuation ralations (IMRs)
   *  Temp until figure out way to dynamically load classes during runtime
   */
  public final static String BJF_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.BJF_1997_AttenRel";
  public final static String AS_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.AS_1997_AttenRel";
  public final static String C_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
  public final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.SCEMY_1997_AttenRel";
  public final static String F_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel";
  public final static String A_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
  public final static String CB_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.CB_2003_AttenRel";
  public final static String SM_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel";

  //arrayList to store the supported AttenRel Class Names with their full package structure.
  ArrayList supportedAttenRelClasses = new ArrayList();

  /**
   * class default constructor
   */
  public AtteuationRelationshipsInstance(){

    //adds all the AttenRel classes to the ArrayList
    supportedAttenRelClasses.add(BJF_CLASS_NAME);
    supportedAttenRelClasses.add(AS_CLASS_NAME);
    supportedAttenRelClasses.add(C_CLASS_NAME);
    supportedAttenRelClasses.add(SCEMY_CLASS_NAME);
    supportedAttenRelClasses.add(F_CLASS_NAME);
    supportedAttenRelClasses.add(A_CLASS_NAME);
    supportedAttenRelClasses.add(CB_CLASS_NAME);
    supportedAttenRelClasses.add(SM_CLASS_NAME);

  }

  /**
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

  public ArrayList createIMRClassInstance( org.scec.param.event.ParameterChangeWarningListener listener){
    ArrayList AttenRelObjects = new ArrayList();
    String S = C + ": createIMRClassInstance(): ";
    int size = supportedAttenRelClasses.size();
    for(int i=0;i< size;++i){
      try {
        Class listenerClass = Class.forName( "org.scec.param.event.ParameterChangeWarningListener" );
        Object[] paramObjects = new Object[]{ listener };
        Class[] params = new Class[]{ listenerClass };
        Class imrClass = Class.forName((String)supportedAttenRelClasses.get(i));
        Constructor con = imrClass.getConstructor( params );
        Object obj = con.newInstance( paramObjects );
        AttenRelObjects.add(obj);
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
    return AttenRelObjects;
  }

}