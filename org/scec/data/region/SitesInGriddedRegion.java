package org.scec.data.region;

import java.util.*;
import java.io.Serializable;
import java.net.*;
import java.io.Serializable;
import java.io.*;

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

  //flag to check if the site Params needs to be set from the WILLS VS30 site type,
  //basin depth is null in this case
  private boolean setSiteParamsUsing_WILLS_VS30= false;

  //flag to set the site Params from the WILLS VS30 site type servlet and scec basin depth servlet.
  private boolean setSiteParamsUsingVs30AndBasinDepth = false;

  //set the same site type for each site
  private boolean setSameSiteParams = true;

  //Vs30 and basinDepth Vector
  Vector vs30,basinDepth;

  //Iterator that contains the default Values for the Site parameters if CVM do not cover that site
  private Iterator defaultSiteParams;

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
     if(!setSameSiteParams){
       //getting the Site Parameters Iterator
       Iterator it = site.getParametersIterator();
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
  * This function is called if the site Params need to be set using WILLS site type.
  * As Wills Site type provide no value for the Basin depth so we set it to Double.Nan
  */
 public void setSiteParamsUsing_WILLS_VS30(){
   setSiteParamsUsing_WILLS_VS30 = true;
   setSiteParamsUsingVs30AndBasinDepth = false;
   setSameSiteParams = false;
   try{
     getVS30FromCVM(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),getGridSpacing());
   }catch(Exception e){
     throw new RuntimeException(e.getMessage());
   }
   int size = getNumGridLocs();
   basinDepth = new Vector();
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
     getVS30FromCVM(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),getGridSpacing());
     getBasinDepthFromCVM(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),getGridSpacing());
   }catch(Exception e){
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
 public void setDefaultSiteParams(Iterator defaultSiteParamsIt){
   defaultSiteParams = defaultSiteParamsIt;
 }

 /**
  * Gets the VS30 from the CVM servlet
  */
 private void getVS30FromCVM(double lonMin,double lonMax,double latMin,double latMax,
                             double gridSpacing) {

   // if we want to the paramter from the servlet
   try{

     // make connection with servlet
     URL cvmServlet = new URL("http://gravity.usc.edu/OpenSHA/servlet/WillsSiteClassServlet");
     URLConnection servletConnection = cvmServlet.openConnection();

     servletConnection.setDoOutput(true);

     // Don't use a cached version of URL connection.
     servletConnection.setUseCaches (false);
     servletConnection.setDefaultUseCaches (false);

     // Specify the content type that we will send binary data
     servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

     // send the student object to the servlet using serialization
     ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());


     outputToServlet.writeObject(new Double(lonMin));
     outputToServlet.writeObject(new Double(lonMax));
     outputToServlet.writeObject(new Double(latMin));
     outputToServlet.writeObject(new Double(latMax));
     outputToServlet.writeObject(new Double(gridSpacing));

     outputToServlet.flush();
     outputToServlet.close();

     // now read the connection again to get the vs30 as sent by the servlet
     ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
     //vectors of lat and lon for the Vs30
     vs30=(Vector)ois.readObject();
     ois.close();
   }catch (Exception exception) {
     System.out.println("Exception in connection with servlet:" +exception);
   }
 }


 /**
  * Gets the Basin Depth from the CVM servlet
  */
 private void getBasinDepthFromCVM(double lonMin,double lonMax,double latMin,double latMax,
                                   double gridSpacing) {

   // if we want to the paramter from the servlet
   try{

     // make connection with servlet
     URL cvmServlet = new URL("http://gravity.usc.edu/OpenSHA/servlet/SCEC_BasinDepthServlet");
     URLConnection servletConnection = cvmServlet.openConnection();

     servletConnection.setDoOutput(true);

     // Don't use a cached version of URL connection.
     servletConnection.setUseCaches (false);
     servletConnection.setDefaultUseCaches (false);

     // Specify the content type that we will send binary data
     servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

     // send the student object to the servlet using serialization
     ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

     outputToServlet.writeObject(new Double(lonMin));
     outputToServlet.writeObject(new Double(lonMax));
     outputToServlet.writeObject(new Double(latMin));
     outputToServlet.writeObject(new Double(latMax));
     outputToServlet.writeObject(new Double(gridSpacing));

     outputToServlet.flush();
     outputToServlet.close();

     // now read the connection again to get the vs30 as sent by the servlet
     ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());

     //vectors of Basin Depth for each gridded site
     basinDepth=(Vector)ois.readObject();
     ois.close();
   }catch (Exception exception) {
     System.out.println("Exception in connection with servlet:" +exception);
   }
 }

}