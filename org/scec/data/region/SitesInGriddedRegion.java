package org.scec.data.region;

import java.util.*;

import org.scec.data.*;
import org.scec.param.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SitesInGriddedRegion extends EvenlyGriddedRectangularGeographicRegion {

  Site site = new Site();

  /**
   *
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
   * get the site at specified index
   * @param index
   * @return
   */
  public Site getSite(int index){
     site.setLocation(getGridLocation(index));
     return site;
  }

  /**
   * Add this parameter to all the sites
   * @param it
   */
 public void addSiteParams(Iterator it) {
   while(it.hasNext()){
     ParameterAPI tempParam=(ParameterAPI)it.next();
   if(!site.containsParameter(tempParam))
   site.addParameter(tempParam);
   }
 }


 /**
  *
  * @param it
  */
 public void removeSiteParams(){

   ListIterator it1=site.getParametersIterator();
   while(it1.hasNext())
       site.removeParameter((ParameterAPI)it1.next());
 }

 /**
  *
  * @return
  */
 public Iterator getSitesIterator(){
   Vector sitesVector=new Vector();
   ListIterator it=this.getGridLocationsIterator();
   ListIterator siteParamsIt = site.getParametersIterator();
   while(it.hasNext()){
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
}