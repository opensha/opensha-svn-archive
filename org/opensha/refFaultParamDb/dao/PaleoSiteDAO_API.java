package org.opensha.refFaultParamDb.dao;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: PaleoSiteDAO_API.java </p>
 * <p>Description: This accesses various paleo sites from the database  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface PaleoSiteDAO_API {

  /**
   * Add a new Paleo Site
   *
   * @param paleoSite
   *
   */
  public void addPaleoSite(PaleoSite paleoSite) throws InsertException;


 /**
  * Get the paleo site info for a particular paleo site id
  * @param paleoSiteId
  * @return
  */
 public PaleoSite getPaleoSite(int paleoSiteId) throws QueryException;


 /**
  * Remove this PaleoSite from the list
  * @param paleoSiteId
  */
 public boolean removePaleoSite(int paleoSiteId) throws UpdateException;

 /**
  * Returns a list of all PaleoSites.
  *
  * @return ArrayList containing list of PaleoSite objects
  */
 public ArrayList getAllPaleoSites() throws QueryException;

 /**
  * It returns a list of PaleoSiteSummary objects. Each such object has a name
  * and id. If there is no name corresponding to paleo site in the database,
  * then this function gets the references for the paleo site and sets it as the name
  * which can then be used subsequently.
  *
  * @return
  * @throws QueryException
  */
 public ArrayList getAllPaleoSiteNames() throws QueryException;


}
