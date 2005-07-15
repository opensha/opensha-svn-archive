package org.opensha.refFaultParamDb.dao;

/**
 * <p>Title: LogTypeDAO_API.java </p>
 * <p>Description: Access the Log_Type table</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface LogTypeDAO_API {
  public int getLogTypeId(String base);
  public String getLogBase(int logTypeId);
}