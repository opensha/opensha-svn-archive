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

public interface SitesInGriddedRegionAPI {


  /**
   * Files get the site information from.
   */
  public static final String WILLS_SITE_CLASS_FILE = "cvmfiles/usgs_cgs_geology_60s_mod.txt";
  public static final String BASIN_DEPTH_FILE = "cvmfiles/basindepth_OpenSHA.txt";


  /**
   * Gets the site at specified index.
   * @param index
   * @returns site at the index
   */
  public Site getSite(int index);

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
  */
 public Iterator getSitesIterator();

 /**
  * This function is called if the site Params need to be set using WILLS site type.
  * As Wills Site type provide no value for the Basin depth so we set it to Double.Nan
  */
 public void setSiteParamsUsing_WILLS_VS30();

 /**
  * This function is called if the site Params need to be set using WILLS site type
  * and basin depth from the SCEC basin depth values.
  */
 public void setSiteParamsUsing_WILLS_VS30_AndBasinDepth();

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
  *
  * @returns the Wills Class Values for each site
  */
 public ArrayList getWillsClassVector();

 /**
  *
  * @returns the basin depth values for each site
  */
 public ArrayList getBasinDepthVector();

}