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
 * <p>Title:SiteParamListEditor </p>
 * <p>Description: this class will make the site parameter editor.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 29, 2002
 * @version 1.0
 */



public class SiteParamListEditor extends ParameterListEditor implements ParameterChangeListener{

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
   * constuctor which builds up mapping between IMRs and their related sites
   */
  public SiteParamListEditor() {
    // Build package names search path
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    // make the new siteParamList
    parameterList  = new ParameterList();
    // add the longitude and latitude paramters
    parameterList.addParameter(longitude);
    parameterList.addParameter(latitude);
    latitude.addParameterChangeListener(this);
    longitude.addParameterChangeListener(this);

    // maake the new site object
    site= new Site(new Location(((Double)latitude.getValue()).doubleValue(),
                                ((Double)longitude.getValue()).doubleValue()));
    addParameters();
    setTitle(SITE_PARAMS);
  }


  /**
   * This function adds the site params to the existing list.
   * Parameters are NOT cloned.
   * If paramter with same name already exists, then it is not added
   *
   * @param it : Iterator over the site params in the IMR
   */
 public void addSiteParams(Iterator it) {
   Parameter tempParam;
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       parameterList.addParameter(tempParam);
       if(tempParam instanceof StringParameter) { // if it Stringparamter, set its initial values
         StringParameter strConstraint = (StringParameter)tempParam;
         tempParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
     }
     if(!site.containsParameter(tempParam))
       site.addParameter(tempParam);
   }

  this.editorPanel.removeAll();
  this.addParameters();
 }

 /**
  * This function adds the site params to the existing list.
  * Parameters are cloned.
  * If paramter with same name already exists, then it is not added
  *
  * @param it : Iterator over the site params in the IMR
  */
 public void addSiteParamsClone(Iterator it) {
   Parameter tempParam;
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       Parameter cloneParam = (Parameter)tempParam.clone();
       if(tempParam instanceof StringParameter) {
         StringParameter strConstraint = (StringParameter)tempParam;
         cloneParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
       parameterList.addParameter(cloneParam);
       site.addParameter(cloneParam);
     }
   }
   this.editorPanel.removeAll();
   this.addParameters();
 }

 /**
  * This function removes the previous site parameters and adds as passed in iterator
  *
  * @param it
  */
 public void replaceSiteParams(Iterator it) {

   // make the new site object
   site= new Site(new Location(((Double)latitude.getValue()).doubleValue(),
                                ((Double)longitude.getValue()).doubleValue()));
   // first remove all the parameters ewxcept latitude and longitude
   Iterator siteIt = parameterList.getParameterNamesIterator();
   while(siteIt.hasNext()) { // remove all the parameters except latitdue and longitude
     String paramName = (String)siteIt.next();
     if(!paramName.equalsIgnoreCase(LATITUDE) &&
        !paramName.equalsIgnoreCase(LONGITUDE))
       parameterList.removeParameter(paramName);
   }

   // now add all the new params
   addSiteParams(it);

 }

 /**
  * Display the site params based on the site passed as the parameter
  */
 public void setSite(Site site) {
   this.site = site;
   Iterator it  = site.getParametersIterator();
   replaceSiteParams(it);
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
   * this function when longitude or latitude are updated
   * So, we update the site object as well
   *
   * @param e
   */
  public void parameterChange(ParameterChangeEvent e) {
    site.setLocation(new Location(((Double)latitude.getValue()).doubleValue(),
                                  ((Double)longitude.getValue()).doubleValue()));
  }

}