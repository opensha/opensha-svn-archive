package org.opensha.commons.data.region;

import java.util.*;
import java.io.IOException;
import java.io.Serializable;


import org.opensha.commons.data.Site;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.exceptions.RegionConstraintException;


import org.opensha.sha.util.*;
import org.opensha.sha.gui.infoTools.ConnectToCVM;


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

public interface SitesInGriddedRegionAPI extends EvenlyGriddedGeographicRegionAPI{


  /**
   * Files get the site information from.
   */
  //public static final String WILLS_SITE_CLASS_FILE = "cvmfiles/usgs_cgs_geology_60s_mod.txt";
  //public static final String BASIN_DEPTH_FILE = "cvmfiles/basindepth_OpenSHA.txt";


  /**
   * Gets the site at specified index.
   * @param index
   * @returns site at the index
   */
  public Site getSite(int index) throws RegionConstraintException;

  /**
   * Add this site-type parameter to all the sites in the gridded region
   * @param it
   */
 public void addSiteParams(Iterator it) ;


 /**
  * This function removes the site types params from the site
  * @param it
  */
 public void removeSiteParams();


 /**
  * This function craetes the iterator of all the site within that region and
  * return its iterator
  * @return
  */ // not currently used
 //public Iterator getSitesIterator();


 /**
  * This function is called if same type has to be applied to all sites in the gridded region.
  */
 public void setSameSiteParams();


 /**
  * Sets the default Site Parameters in case CVM don't cover the regions
  * @param defaultSiteParamsIt : Iterator for the Site Params and their Values
  */
 public void setDefaultSiteParams(ArrayList defaultSiteParams);

 /**
  * Sets the list for Site Data providers for region.
  *
  * After calling this function one should also call setDefaultSiteParams() , in
  * order to the default value for the site parameters, in case we don't get
  * any value from servlet.
  * 
  * @param providers
  * @throws IOException
  */
 public void setSiteParamsForRegion(OrderedSiteDataProviderList providers) throws IOException;
 
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
 @Deprecated
  public void setSiteParamsForRegionFromServlet(boolean connectForBasinDepth);

  /**
   * Gets the list for Site Params for region from application called this function.
   * @param willsSiteClass : String Array of Wills Site Class Values
   * @param bd : double Array of Basin Depth Values
   */
  @Deprecated
  public void setSiteParamsForRegion(String[] willsSiteClass, double[] bd);

}
