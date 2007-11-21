package org.opensha.sha.gui.infoTools;

import java.util.*;
import java.lang.reflect.*;

/**
 * <p>Title: AtteuationRelationshipsInstance </p>
 * <p>Description: Creates the list of the AttenuationRelationship Objects from
 * their classnames.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created March 05,2004
 * @version 1.0
 */

public class AttenuationRelationshipsInstance {


  private static final String C= "AtteuationRelationshipsInstance";

  /**
   *  The object class names for all the supported attenuation ralations (IMRs)
   *  Temp until figure out way to dynamically load classes during runtime
   */
  public final static String BJF_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel";
  public final static String AS_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.AS_1997_AttenRel";
  public final static String C_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
  public final static String SCEMY_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.SadighEtAl_1997_AttenRel";
  public final static String F_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.Field_2000_AttenRel";
  public final static String A_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
  public final static String CB_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CB_2003_AttenRel";
  public final static String SM_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel";
  public final static String USGS04_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel";
  public final static String AS_2005_PRELIM_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.AS_2005_prelim_AttenRel";
  public final static String CB_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel";
  public final static String CB_2008_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel";
  public final static String CY_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CY_2006_AttenRel";
  public final static String Boore_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BA_2006_AttenRel";
  public final static String CS_2005_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CS_2005_AttenRel";
  //public final static String SS_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.SiteSpecific_2006_AttenRel";
  public final static String BS_2003_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BS_2003_AttenRel";
  public final static String BC_2004_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BC_2004_AttenRel";
  public final static String GouletEtAl_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.GouletEtAl_2006_AttenRel";

  //arrayList to store the supported AttenRel Class Names with their full package structure.
  ArrayList supportedAttenRelClasses = new ArrayList();

  /**
   * class default constructor
   */
  public AttenuationRelationshipsInstance(){

    //adds all the AttenRel classes to the ArrayList
    supportedAttenRelClasses.add(BJF_CLASS_NAME);
    supportedAttenRelClasses.add(AS_CLASS_NAME);
    supportedAttenRelClasses.add(C_CLASS_NAME);
    supportedAttenRelClasses.add(SCEMY_CLASS_NAME);
    supportedAttenRelClasses.add(F_CLASS_NAME);
    supportedAttenRelClasses.add(A_CLASS_NAME);
    supportedAttenRelClasses.add(CB_CLASS_NAME);
    supportedAttenRelClasses.add(SM_CLASS_NAME);
    supportedAttenRelClasses.add(USGS04_CLASS_NAME);
    supportedAttenRelClasses.add(CB_2006_CLASS_NAME);
    supportedAttenRelClasses.add(CB_2008_CLASS_NAME);
    supportedAttenRelClasses.add(CY_2006_CLASS_NAME);
    supportedAttenRelClasses.add(Boore_2006_CLASS_NAME);
    supportedAttenRelClasses.add(CS_2005_CLASS_NAME);
    supportedAttenRelClasses.add(BS_2003_CLASS_NAME);
    supportedAttenRelClasses.add(BC_2004_CLASS_NAME);
    supportedAttenRelClasses.add(GouletEtAl_2006_CLASS_NAME);
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
  * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.opensha.sha.imt.attenRelImpl.BJF_1997_AttenRel");
  * </code><p>
  *
  */

  public ArrayList createIMRClassInstance( org.opensha.param.event.ParameterChangeWarningListener listener){
    ArrayList AttenRelObjects = new ArrayList();
    String S = C + ": createIMRClassInstance(): ";
    int size = supportedAttenRelClasses.size();
    for(int i=0;i< size;++i){
      try {
        Class listenerClass = Class.forName( "org.opensha.param.event.ParameterChangeWarningListener" );
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
        e.printStackTrace();
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
