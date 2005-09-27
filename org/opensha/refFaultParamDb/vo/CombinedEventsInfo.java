package org.opensha.refFaultParamDb.vo;

import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.data.estimate.Estimate;

/**
 * <p>Title: CombinedEventsInfo.java </p>
 * <p>Description: Put the combined events info for a particular site into the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedEventsInfo {
  private int siteId;
  private String siteEntryDate;
  private TimeAPI startTime;
  private TimeAPI endTime;
  private Estimate displacementEstimate;
  private Estimate slipRateEstimate;
  private Estimate numEventsEstimate;
  private Estimate aSeismicSlipFactorEstimate;
  private String generalComments;
  private String entryComments;
  private String datedFeatureComments;

  public CombinedEventsInfo() {
  }

}