package org.scec.data.region;

import java.util.*;
import java.io.Serializable;


import org.scec.data.*;
import org.scec.param.*;
import org.scec.sha.util.*;
import org.scec.sha.gui.infoTools.ConnectToCVM;
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

  //Debug parameter
  public static final boolean D= false;


  //definition for the Siet Object
  Site site = new Site();

  /**
   * Files get the site information from.
   */
  private static final String WILLS_SITE_CLASS_FILE = "cvmfiles/usgs_cgs_geology_60s_mod.txt";
  private static final String BASIN_DEPTH_FILE = "cvmfiles/basindepth_OpenSHA.txt";

  //flag to check if the site Params needs to be set from the WILLS VS30 site type,
  //basin depth is null in this case
  private boolean setSiteParamsUsing_WILLS_VS30= false;

  //flag to set the site Params from the WILLS VS30 site type servlet and scec basin depth servlet.
  private boolean setSiteParamsUsingVs30AndBasinDepth = false;

  //set the same site type for each site
  private boolean setSameSiteParams = true;

  //Vs30 and basinDepth ArrayList
  ArrayList vs30,basinDepth;

  //ArrayList that contains the default Values for the Site parameters if CVM do not cover that site
  private ArrayList defaultSiteParams;

  //Instance of the site TransLator class
  SiteTranslator siteTranslator = new SiteTranslator();



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
     String siteInfo=null;
     if(!setSameSiteParams){
       //getting the Site Parameters Iterator
       Iterator it = site.getParametersIterator();
       //checking to see if we are getting the correct value for vs30 and basin depth.
       if(D){
         System.out.println(site.getLocation().toString()+"\t"+vs30.get(index)+
                            "\t\t"+((Double)basinDepth.get(index)).doubleValue());
       }
       while(it.hasNext()){
         ParameterAPI tempParam = (ParameterAPI)it.next();

         //Setting the value of each site Parameter from the CVM and translating them into the Attenuation related site
         boolean flag = siteTranslator.setParameterValue(tempParam,(String)vs30.get(index),
                                                         ((Double)basinDepth.get(index)).doubleValue());
         //If the value was outside the bounds of CVM
         //and site has no value from CVM then set its value to the default Site Params shown in the application.
         if(!flag){
           //iterating over the default site parameters to set the Site Param if
           //no value has been obtained from the CVM for that site.
           Iterator it1 = defaultSiteParams.iterator();
           while(it1.hasNext()){
             ParameterAPI param = (ParameterAPI)it1.next();
             if(tempParam.getName().equals(param.getName()))
               tempParam.setValue(param.getValue());
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
   ArrayList sitesVector=new ArrayList();
   //get the iterator of all the locations within that region
   ListIterator it=this.getGridLocationsIterator();
   //get the iterator for all the site types
   ListIterator siteParamsIt = site.getParametersIterator();
   while(it.hasNext()){
     //create the site object and add it to tbe ArrayList List
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
  * This function is called if the site Params need to be set using WILLS site type.
  * As Wills Site type provide no value for the Basin depth so we set it to Double.Nan
  */
 public void setSiteParamsUsing_WILLS_VS30(){
   setSiteParamsUsing_WILLS_VS30 = true;
   setSiteParamsUsingVs30AndBasinDepth = false;
   setSameSiteParams = false;
   try{
     vs30 = ConnectToCVM.getWillsSiteTypeFromCVM(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),getGridSpacing());
   }catch(Exception e){
     /*vs30 = ConnectToCVM.getWillsSiteType(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
         getGridSpacing(),WILLS_SITE_CLASS_FILE);*/
     //throw new RuntimeException(e.getMessage());
   }
   int size = getNumGridLocs();
   basinDepth = new ArrayList();
   for(int i=0;i<size;++i)
     basinDepth.add(new Double(Double.NaN));

 }


 /**
  * This function is called if the site Params need to be set using WILLS site type
  * and basin depth from the SCEC basin depth values.
  */
 public void setSiteParamsUsing_WILLS_VS30_AndBasinDepth(){
   setSiteParamsUsing_WILLS_VS30 = false;
   setSiteParamsUsingVs30AndBasinDepth = true;
   setSameSiteParams = false;
   try{
     vs30 = ConnectToCVM.getWillsSiteTypeFromCVM(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),getGridSpacing());
     basinDepth = ConnectToCVM.getBasinDepthFromCVM(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),getGridSpacing());
   }catch(Exception e){
     /*vs30 = ConnectToCVM.getWillsSiteType(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
         getGridSpacing(),WILLS_SITE_CLASS_FILE);
     basinDepth = ConnectToCVM.getBasinDepth(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
         getGridSpacing(),BASIN_DEPTH_FILE);*/
     throw new RuntimeException(e.getMessage());
   }
 }

 /**
  * This function is called if same type has to be applied to all sites in the gridded region.
  */
 public void setSameSiteParams(){
   setSiteParamsUsing_WILLS_VS30 = false;
   setSiteParamsUsingVs30AndBasinDepth = false;
   setSameSiteParams = true;
   vs30 = null;
   basinDepth = null;
 }

 /**
  * Sets the default Site Parameters in case CVM don't cover the regions
  * @param defaultSiteParamsIt : Iterator for the Site Params and their Values
  */
 public void setDefaultSiteParams(ArrayList defaultSiteParams){
   this.defaultSiteParams = defaultSiteParams;
 }


 /**
  *
  * @returns the Wills Class Values for each site
  */
 public ArrayList getWillsClassVector(){
   return this.vs30;
 }

 /**
  *
  * @returns the basin depth values for each site
  */
 public ArrayList getBasinDepthVector(){
   return this.basinDepth;
 }

}