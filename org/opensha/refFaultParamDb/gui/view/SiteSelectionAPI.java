package org.opensha.refFaultParamDb.gui.view;

/**
 * <p>Title: SiteSelectionAPI.java </p>
 * <p>Description: this API is implemented by any class which needs to listen
 * to site selection events. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface SiteSelectionAPI {
  /**
   * Whenever a user selects a site, this function is called in the listener class
   * @param siteName
   */
  public void siteSelected(String siteName);
}