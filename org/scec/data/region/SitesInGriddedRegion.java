package org.scec.data.region;

import java.util.*;
import java.io.Serializable;

import org.scec.data.*;
import org.scec.param.*;
import org.scec.sha.util.*;

/**
 * <p>Title: SitesInGriddedRegion</p>
 * <p>Description: This Class adds and replace the site params to each site for a gridded
 * region. This class fills in the site params for each site in the rectangular gridded
 * region. Right now all the sites have same site-type params, but later each site
 * will be getting the different values once plugged into CVM(community Velocity Model.
 * The Advantage of this class is that one does not have to create the iterator of all
 * sites which consumes a lot od memory to store all those sit, but we can just get one
 * site at a time and perform computation for that site.</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @created : March 15,2003
 * @version 1.0
 */

public class SitesInGriddedRegion extends EvenlyGriddedRectangularGeographicRegion
                                          implements Serializable{

  Site site = new Site();

  //flag to check if the site Params needs to be set from the CVM.
  private boolean setSiteParamsFromCVM= false;
  //Vs30 and basinDepth Vector
  Vector vs30,basinDepth;

  //Iterator that contains the default Values for the Site parameters if CVM do not cover that site
  private Iterator defaultSiteParams;

  //Instance of the site TransLator class
  SiteTranslatorNew siteTranslator = new SiteTranslatorNew();

  /**
   *class constructor
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   * @param gridSpacing
   */
  public SitesInGriddedRegion(double minLat,double maxLat,double minLon,double maxLon,
                              double gridSpacing) {
    super(minLat,maxLat,minLon,maxLon,gridSpacing);
  }


  /**
   * Gets the site at specified index.
   * @param index
   * @returns site at the index
   */
  public Site getSite(int index){
     site.setLocation(getGridLocation(index));
     if(this.setSiteParamsFromCVM){
       //getting the Site Parameters Iterator
       Iterator it = site.getParametersIterator();
       while(it.hasNext()){
         ParameterAPI tempParam = (ParameterAPI)it.next();
         //Setting the value of each site Parameter from the CVM and translating them into the Attenuation related site
         boolean flag = siteTranslator.setParameterValue(tempParam,((Double)vs30.get(index)).doubleValue(),
                                                         ((Double)basinDepth.get(index)).doubleValue());
         //If the value was outside the bounds of CVM
         //and site has no value from CVM then set its value to the default Site Params shown in the application.
         if(!flag){
           //iterating over the default site parameters to set the Site Param if
           //no value has been obtained from the CVM for that site.
           while(defaultSiteParams.hasNext()){
             ParameterAPI param = (ParameterAPI)defaultSiteParams.next();
             if(tempParam.getName().equals(param.getName()))
               tempParam = param;
           }
         }
       }
     }
     return site;
  }

  /**
   * Add this site-type parameter to all the sites in the gridded region
   * @param it
   */
 public void addSiteParams(Iterator it) {
   //iterator of all the site types supported by the selecetd IMR for that gridded region
   while(it.hasNext()){
     ParameterAPI tempParam=(ParameterAPI)it.next();
   if(!site.containsParameter(tempParam))
   site.addParameter(tempParam);
   }
 }


 /**
  * This function removes the site types params from the site
  * @param it
  */
 public void removeSiteParams(){

   ListIterator it1=site.getParametersIterator();
   while(it1.hasNext())
       site.removeParameter((ParameterAPI)it1.next());
 }

 /**
  * This function craetes the iterator of all the site within that region and
  * return its iterator
  * @return
  */
 public Iterator getSitesIterator(){
   Vector sitesVector=new Vector();
   //get the iterator of all the locations within that region
   ListIterator it=this.getGridLocationsIterator();
   //get the iterator for all the site types
   ListIterator siteParamsIt = site.getParametersIterator();
   while(it.hasNext()){
     //create the site object and add it to tbe Vector List
     Site newSite = new Site((Location)it.next());
     while(siteParamsIt.hasNext()){
       ParameterAPI tempParam = (ParameterAPI)siteParamsIt.next();
       if(!newSite.containsParameter(tempParam))
         newSite.addParameter(tempParam);
     }
     sitesVector.add(newSite);
   }
   return sitesVector.iterator();
 }


 /**
  * This function is called if the site Params need to be set from the CVM
  * @param cvmFlag
  * @param vs30
  * @param basinDepth
  */
 public void setSiteParamsFromCVM(boolean cvmFlag,Vector vs30,Vector basinDepth){
   this.setSiteParamsFromCVM=cvmFlag;
   this.basinDepth = basinDepth;
   this.vs30 = vs30;
 }


 /**
  * Sets the default Site Parameters in case CVM don't cover the regions
  * @param defaultSiteParamsIt : Iterator for the Site Params and their Values
  */
 public void setDefaultSiteParams(Iterator defaultSiteParamsIt){
   defaultSiteParams = defaultSiteParamsIt;
 }
}