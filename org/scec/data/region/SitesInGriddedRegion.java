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
     if(this.setSiteParamsFromCVM)
       try{
       siteTranslator.setSiteParams(site,((Double)vs30.get(index)).doubleValue(),
                                    ((Double)basinDepth.get(index)).doubleValue());
       }catch(Exception e){
         System.out.println(" Site was in water");
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
}