package org.opensha.refFaultParamDb.dao;

import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.SiteRepresentation;

/**
 * <p>Title: SiteRepresentationDAO_API.java </p>
 * <p>Description: Get a list of allowed site representations. Site can be represented
 * as "Entire Fault", "Most Significant Strand", "One of Several Strands", "Unknown"</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface SiteRepresentationDAO_API {

  /**
   * Get all the representations with which a site can be associated
   * @return
   */
   public ArrayList getAllSiteRepresentations();

   /**
    * Get a representation based on site representation Id
    * @param siteRepresentationId
    * @return
    */
   public SiteRepresentation getSiteRepresentation(int siteRepresentationId);

   /**
    * Get a  representation based on site representation name
    *
    * @param siteRepresentationName
    * @return
    */
   public SiteRepresentation getSiteRepresentation(String siteRepresentationName);

}