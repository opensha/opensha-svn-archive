package org.scec.sha.util;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.scec.data.region.*;
import org.scec.data.*;
import org.scec.param.event.*;
import org.scec.sha.imr.*;
import org.scec.param.*;


/**
 * <p>Title: SiteTranslatorTests</p>
 * <p>Description: Test the wills-class and basin-depth servlets,
 * and the siteTranlator, by putting the following on a line of an ascii file
 * for each site in the LA region  </p>
 * @author : Nitin Gupta
 * @version 1.0
 */

class SiteTranslatorTests implements ParameterChangeWarningListener{

  private FileWriter fw;

  private double MIN_LAT= 33.5;
  private double MAX_LAT= 34.7;
  private double MIN_LON= -119.5;
  private double MAX_LON= -117.0;
  private double GRID_SPACING = .05;

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


  //Vector to store the IMR objects
  Vector imrObject = new Vector();

  SitesInGriddedRegion griddedRegion = new SitesInGriddedRegion(MIN_LAT,MAX_LAT,
                                        MIN_LON, MAX_LON,GRID_SPACING);
  SiteTranslator siteTranslator = new SiteTranslator();



  //default class constructor
  public SiteTranslatorTests(){
    griddedRegion.setSiteParamsUsing_WILLS_VS30_AndBasinDepth();
    createIMRs_Object();
    Vector basinDepth = griddedRegion.getBasinDepthVector();
    Vector vs30 = griddedRegion.getWillsClassVector();
    String siteInfo = null;
    LocationList list= griddedRegion.getGridLocationsList();
    try{
      fw = new FileWriter("region_info.txt");
      fw.write("Lat\t\tLon\t\tWillsClass\t\tBasinDepth\t\tSiteTranslatorFlag\t\tSiteTypeName\t\tSiteTypeValue\n");
      fw.write("---\t\t---\t\t----------\t\t----------\t\t------------------\t\t------------\t\t-------------\n\n");
      //Iterating over all the site one at a time
      for(int i=0;i<list.size();++i){
        //Iterating over all the supported IMR's
        for(int j=0;j<imrObject.size();++j){
          ListIterator it = ((AttenuationRelationship)imrObject.get(j)).getSiteParamsIterator();
          //iterating over all the site param for the IMR
          while(it.hasNext()){
            ParameterAPI tempParam = (ParameterAPI)it.next();
            //calling the siteTranslator
            boolean flag = siteTranslator.setParameterValue(tempParam,(String)vs30.get(i),((Double)basinDepth.get(i)).doubleValue());
            //writing the site info to the file to test if we are getting the correct site Paramters
            siteInfo = list.getLocationAt(i).getLatitude()+"\t\t"+list.getLocationAt(i).getLongitude()+"\t\t\t"+
                       (String)vs30.get(i)+"\t\t\t"+((Double)basinDepth.get(i)).doubleValue()+
                       "\t\t\t"+flag+"\t\t\t"+tempParam.getName()+"\t\t\t"+tempParam.getValue().toString()+"\n";
            fw.write(siteInfo);
          }
        }
      }
      fw.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  private void createIMRs_Object(){
    Vector imrClasses = new Vector();
    //     imrClasses.add(this.SM_CLASS_NAME);
    imrClasses.add(this.AS_CLASS_NAME);
    imrClasses.add(this.BJF_CLASS_NAME);
    imrClasses.add(this.C_CLASS_NAME);
    imrClasses.add(this.SCEMY_CLASS_NAME);
    imrClasses.add(this.CB_CLASS_NAME);
    imrClasses.add(this.F_CLASS_NAME);
    imrClasses.add(this.A_CLASS_NAME);
    imrClasses.add(this.SM_CLASS_NAME);
    Iterator it = imrClasses.iterator();
    while(it.hasNext()){
      // make the IMR objects as needed to get the site params later
      AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI ) createIMRClassInstance((String)it.next(),this);
      imr.setParamDefaults();
      imrObject.add(imr);
    }
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
   private Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
       String S = ": createIMRClassInstance(): ";
       try {

           Class listenerClass = Class.forName( "org.scec.param.event.ParameterChangeWarningListener" );
           Object[] paramObjects = new Object[]{ listener };
           Class[] params = new Class[]{ listenerClass };
           Class imrClass = Class.forName( className );
           Constructor con = imrClass.getConstructor( params );
           Object obj = con.newInstance( paramObjects );
           return obj;
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
    *  This is the main function of this interface. Any time a control
    *  paramater or independent paramater is changed by the user in a GUI this
    *  function is called, and a paramater change event is passed in. This
    *  function then determines what to do with the information ie. show some
    *  paramaters, set some as invisible, basically control the paramater
    *  lists.
    *
    * @param  event
    */
   public void parameterChangeWarning(ParameterChangeWarningEvent e){
     return;
   }

   //main to run the class as the standalone and test the SiteTranslator
   public static void main(String args[]){
     SiteTranslatorTests tests = new SiteTranslatorTests();
   }

}