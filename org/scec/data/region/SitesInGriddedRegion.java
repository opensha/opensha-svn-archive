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

public class SitesInGriddedRegion extends EvenlyGriddedGeographicRegion
                                          implements SitesInGriddedRegionAPI,Serializable{

  //Debug parameter
  public static final boolean D= false;

  //definition for the Siet Object
  Site site = new Site();


  //set the same site type for each site
  private boolean setSameSiteParams = true;

  //wills site class and basinDepth ArrayList
  ArrayList willsSiteClassList,basinDepth;

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
  public SitesInGriddedRegion(LocationList locList,
                              double gridSpacing) {
   super(locList,gridSpacing);
  }



  /**
   * Gets the list for Site Params for region from application called this function.
   * @param willsSiteClass : String Array of Wills Site Class Values
   * @param bd : double Array of Basin Depth Values
   */
  public void setSiteParamsForRegion(String[] willsSiteClass, double[] bd){

    //as we are getting the values from application and want to set the site params
    if(willsSiteClass != null && bd != null && willsSiteClass.length != bd.length)
      throw new RuntimeException("Invalid Range Site Type Values, both Wills "+
                                 "Site Class and Basindepth should have same number of values");

    //if either wills site class or basin depth are not null
    if(willsSiteClass !=null || bd!=null){
      //either wills site class or basin depth are not null then each site needs
      //to be filled up with actaul site type parameters.
       setSameSiteParams = false;
       //if wills site class vlaues are not null then fill their values
       if(willsSiteClass !=null){
         int size = willsSiteClass.length;
         willsSiteClassList = new ArrayList();
         for(int i=0;i<size;++i)
           willsSiteClassList.add(new String(willsSiteClass[i]));
       }
       //If basin depth Values are not null, then fill in their values
       if(bd !=null){
         int size = bd.length;
         basinDepth = new ArrayList();
         for(int i=0;i<size;++i)
           basinDepth.add(new Double(bd[i]));
       }
       else{ //if basin depth is null then fill the array with double NaN vals.
         int size = willsSiteClassList.size();
         basinDepth = new ArrayList();
         for(int i=0;i<size;++i)
           basinDepth.add(new Double(Double.NaN));
       }
    }
  }



  /**
   * Gets the list for Site Params for region from servlet hosted at web server.
   *
   * After calling this function one should also call setDefaultSiteParams() , in
   * order to the default value for the site parameters, in case we don't get
   * any value from servlet.
   *
   * @param connectForBasinDepth : boolean to know if basin depth also required along with
   * Wills Site class values to the Site Parameters for each location in the region.
   */
  public void setSiteParamsForRegionFromServlet(boolean connectForBasinDepth){
    setSameSiteParams = false;
    //getting the list of Locations in the region
    LocationList locList = getGridLocationsList();
    try{
      //getting the wills site class values from servlet
      willsSiteClassList = ConnectToCVM.getWillsSiteTypeFromCVM(locList);
    }catch(Exception e){
      /*willsSiteClassList = ConnectToCVM.getWillsSiteType(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
          getGridSpacing(),WILLS_SITE_CLASS_FILE);*/
      //throw new RuntimeException(e.getMessage());
    }

    if(!connectForBasinDepth){ //if we don't need to get the basin depth values
      //to set the site parameters.So setting all Values to be Double.NaN
      int size = willsSiteClassList.size();
      basinDepth = new ArrayList();
      for(int i=0;i<size;++i)
        basinDepth.add(new Double(Double.NaN));
    }
    else if(connectForBasinDepth){ //if we need to get the Basin depth values to
      // set the site parameters for each location in the region.
      try{
        willsSiteClassList = ConnectToCVM.getWillsSiteTypeFromCVM(locList);
        basinDepth = ConnectToCVM.getBasinDepthFromCVM(locList);
      }catch(Exception e){
        /*willsSiteClassList = ConnectToCVM.getWillsSiteType(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
        getGridSpacing(),WILLS_SITE_CLASS_FILE);
        basinDepth = ConnectToCVM.getBasinDepth(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
        getGridSpacing(),BASIN_DEPTH_FILE);*/
        //throw new RuntimeException(e.getMessage());
      }
    }

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
       //checking to see if we are getting the correct value for willsSiteClassList and basin depth.
       if(D){
         System.out.println(site.getLocation().toString()+"\t"+willsSiteClassList.get(index)+
                            "\t\t"+((Double)basinDepth.get(index)).doubleValue());
       }
       while(it.hasNext()){
         ParameterAPI tempParam = (ParameterAPI)it.next();

         //Setting the value of each site Parameter from the CVM and translating them into the Attenuation related site
         boolean flag = siteTranslator.setParameterValue(tempParam,(String)willsSiteClassList.get(index),
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
  * This function is called if the site Params need to be set using WILLS site type
  * and basin depth from the SCEC basin depth values.
  */

 /**
  * Calling this function will set the Site Params to whatever their value is currently.
  * All sites will be having the same value for those Site Parameters.
  */
 public void setSameSiteParams(){
   setSameSiteParams = true;
   willsSiteClassList = null;
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
   return this.willsSiteClassList;
 }

 /**
  *
  * @returns the basin depth values for each site
  */
 public ArrayList getBasinDepthVector(){
   return this.basinDepth;
 }

}