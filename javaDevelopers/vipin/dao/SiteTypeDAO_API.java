package javaDevelopers.vipin.dao;
import javaDevelopers.vipin.vo.SiteType;
import javaDevelopers.vipin.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: SiteTypeDAO_API.java </p>
 * <p>Description: This saves the various site types associated with a
 * paleo site. Example of site types are : “trench”, “geologic”, “survey/cultural”  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface SiteTypeDAO_API {

  /**
   * Add a new Site Type to the list
   *
   * @param siteType
   *
   */
  public int addSiteType(SiteType siteType) throws InsertException;

 /**
  * Update the site type info
  *
  * @param siteTypeId  Id of the site type which need to be updated
  * @param siteType updated info about the site type
  */
 public boolean updateSiteType(int siteTypeId, SiteType siteType) throws UpdateException;


 /**
  * Get the site type info for a particular sitetypeId
  * @param siteTypeId
  * @return
  */
 public SiteType getSiteType(int siteTypeId) throws QueryException;


 /**
  * Remove this SiteType from the list
  * @param siteypeId
  */
 public boolean removeSiteType(int siteTypeId) throws UpdateException;

 /**
  * Returns a list of all SiteTypes.
  *
  * @return ArrayList containing list of siteType objects
  */
 public ArrayList getAllSiteTypes() throws QueryException;

}