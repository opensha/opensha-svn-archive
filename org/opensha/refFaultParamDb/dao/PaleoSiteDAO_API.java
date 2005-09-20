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
 public ArrayList getPaleoSite(int paleoSiteId) throws QueryException;


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

}
