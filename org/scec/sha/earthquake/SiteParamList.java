package org.scec.sha.earthquake;

import java.util.*;
import java.lang.reflect.*;


import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.data.Site;
import org.scec.data.Location;

/**
 * <p>Title:SiteParamList </p>
 * <p>Description: this class will make the site parameter editor based on selected
 * IMRs. Here duplicate site params in multiple IMRs are removed.
 * Also parameters are the cloned parameters. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 29, 2002
 * @version 1.0
 */



public class SiteParamList extends ParameterList
     implements ParameterChangeListener {

// for debug purposes
 protected final static String C = "SiteParamList";

  /**
   * Latitude and longitude are added to the site paraattenRelImplmeters
   */
  public final static String LONGITUDE = "Longitude";
  public final static String LATITUDE = "Latitude";

  /**
   * Site object
   */
  private Site site;

  // title for site paramter panel
  protected final static String SITE_PARAMS = "Set Site Params";

  /**
   * Longitude and Latitude paramerts to be added to the site params list
   */
  private DoubleParameter longitude = new DoubleParameter(LONGITUDE,new Double(-118));
  private DoubleParameter latitude = new DoubleParameter(LATITUDE,new Double(34.0));


  /**
   * editor to hold the site
   */
  private ParameterListEditor siteEditor;

  /**
   * Vector of list of IMR objects for which site is displayed
   */
  private Vector selectedIMRs;


  // it maps the IMR names and supported sites for each IMR
  protected ArrayList[] siteMap;


  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String BJF_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.BJF_1997_AttenRel";
  protected final static String AS_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.AS_1997_AttenRel";
  protected final static String C_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
  protected final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.SCEMY_1997_AttenRel";
  protected final static String F_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel";
  protected final static String CB_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.CB_2003_AttenRel";


  // add the IMR class names to a vector
  Vector classNames = new Vector();

  // vector of IMR objects
  Vector imrObjects = new Vector();

  /**
   * constuctor which builds up mapping between IMRs and their related sites
   */
  public SiteParamList(ParameterChangeListener changeListener,
                     ParameterChangeWarningListener warningListener,
                     ParameterChangeFailListener failListener) {

   // vector of IMR objects
    imrObjects = new Vector();

   // add the IMR class names to a vector
    classNames = new Vector();
    classNames.add(BJF_CLASS_NAME);
    classNames.add(AS_CLASS_NAME);
    classNames.add(C_CLASS_NAME);
    classNames.add(SCEMY_CLASS_NAME);
    classNames.add(F_CLASS_NAME);
    classNames.add(CB_CLASS_NAME);

    // listen for latitude and longitude so that site object can be updated
    this.latitude.addParameterChangeListener(this);
    this.longitude.addParameterChangeListener(this);

    // siteMap mantains mapping of each IMR with its supported sites
    siteMap =new ArrayList[classNames.size()];

    AttenuationRelationshipAPI imr;

    // number of IMR
    int numOfIMR = 0;

    // iterator over class names
    Iterator it = classNames.iterator();
    while (it.hasNext()) {

      // sitemap mantains the mapping of each IMR with its supported sites
      siteMap[numOfIMR] = new ArrayList();

      // create the imr instance
      imr = ( AttenuationRelationshipAPI ) createIMRClassInstance((String) it.next() , warningListener);
      imrObjects.add(imr);

      // get the list of sites supported by this IMR
      ListIterator listIt = imr.getSiteParamsIterator();

      // save the Site Types supported by this IMR in a list
      Parameter tempParam;
      while(listIt.hasNext()) {
        tempParam = (Parameter)listIt.next();
        Parameter cloneParam = (Parameter)tempParam.clone();
        cloneParam.addParameterChangeFailListener(failListener);

        siteMap[numOfIMR].add(cloneParam);

        if(tempParam instanceof StringParameter) {
          StringParameter strConstraint = (StringParameter)tempParam;
          cloneParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
      }
      ++numOfIMR;
    }

  }


  /**
   * this function when longitude or latitude are updated
   * So, we update the site object as well
   *
   * @param e
   */
 public void parameterChange(ParameterChangeEvent e) {
   site.setLocation(new Location(((Double)latitude.getValue()).doubleValue(),
                                 ((Double)longitude.getValue()).doubleValue()));
 }


  /**
   * Add the site parameters for the list of IMR names passed as the parameter
   *
   * @param imr : IMR for which site needs to be added
   */
  public ParameterListEditor replaceSiteParamsWithIMRs(Vector imrNames) {

   clear();

   // make a paramter variable as it is used frequently in this function
   Parameter paramTemp;


   // get the number of IMRs
   int numOfIMRs = imrObjects.size();

  // add the longitude and latitude paramters
   addParameter(longitude);
   addParameter(latitude);
   site= new Site(new Location(((Double)latitude.getValue()).doubleValue(),
                                 ((Double)longitude.getValue()).doubleValue()));

   // check which IMR has been selected
   for(int i=0; i <numOfIMRs ; ++i) {
     int index = imrNames.indexOf(((AttenuationRelationshipAPI)imrObjects.get(i)).getName());
     //if ith IMR is selected then add its site params
     if(index!=-1) {
         // number of sites for this IMR
         int numSites = siteMap[i].size();
         for(int j=0; j < numSites; ++j) {
           paramTemp = (Parameter)siteMap[i].get(j);
           // set the initial balue for the String Parameter
           if(paramTemp instanceof StringParameter) {
             StringParameter strConstraint = (StringParameter)paramTemp;
             paramTemp.setValue(strConstraint.getAllowedStrings().get(0));
           }
           //if this paramter has not been added till now
           if(!containsParameter(paramTemp.getName())) {
              addParameter(paramTemp);
              site.addParameter(paramTemp);
           }
         }
     }

   }

   // update the site editor based on the paramlist
   this.siteEditor = new ParameterListEditor(this);
   siteEditor.setTitle(SITE_PARAMS);
   return siteEditor;
  }

  /**
   * get the site object from the site params
   *
   * @return
   */
 public Site getSite() {
   return site;
 }


 /**
  * get the clone of site object from the site params
  *
  * @return
  */
 public Site getSiteClone() {
   Site newSite = new Site(new Location(((Double)latitude.getValue()).doubleValue(),
                                 ((Double)longitude.getValue()).doubleValue()));
   Iterator it  = site.getParametersIterator();

   // clone the paramters
   while(it.hasNext())
     newSite.addParameter( (ParameterAPI) ((ParameterAPI)it.next()).clone());
   return site;
 }


 /**
  * Display the site params based on the site passed as the parameter
  */
 public void setSite(Site site) {
   this.site = site;

   clear();

   // set the latitude and longitude values passed into site object
  longitude.setValue(site.getLocation().getLongitude());
  latitude.setValue(site.getLocation().getLatitude());

  // add the longitude and latitude paramters
  addParameter(longitude);
  addParameter(latitude);

  ListIterator it  = site.getParametersIterator();

  // add the parameters
  while(it.hasNext())
    addParameter((ParameterAPI)it.next());
 }




 /**
  * returns the logitude for this site
  *
  * @return
  */
 public double getLongitude() {
   return ((Double)getValue(this.LONGITUDE)).doubleValue();
 }

 /**
  * returns the latitude for this site
  *
  * @return
  */
 public double getLatitude() {
   return ((Double)getValue(this.LATITUDE)).doubleValue();
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
    public Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
        String S = C + ": createIMRClassInstance(): ";
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



}