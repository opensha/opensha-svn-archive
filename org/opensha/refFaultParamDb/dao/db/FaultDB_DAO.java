package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.dao.FaultDAO_API;
import org.opensha.refFaultParamDb.vo.Fault;
import java.util.ArrayList;

/**
 * <p>Title: FaultDB_DAO.java </p>
 * <p>Description: this class interacts with the database to get the fault information </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultDB_DAO implements FaultDAO_API {


  private DB_AccessAPI dbAccessAPI;


  public FaultDB_DAO(DB_AccessAPI dbAccessAPI) {
    setDB_Connection(dbAccessAPI);
  }

  /**
   *
   * @param dbAccessAPI
   */
  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
  * Get the information about a fault based on fault Id
  * @param faultId
  * @return
  */
  public Fault getFault(int faultId) {
    Fault fault = new Fault(1,"fault1");
    return fault;
  }

  /**
   * Get information about a fault based on fault name
   * @param faultName
   * @return
   */
  public Fault getFault(String faultName) {
    Fault fault = new Fault(1,"fault1");
    return fault;
  }

  /**
  * Get a list of all the faults existing itn database
  * @return
  */
 public ArrayList getAllFaults() {
   ArrayList faultsList = new ArrayList();
   faultsList.add(new Fault(1,"Fault1"));
   faultsList.add(new Fault(2,"Fault2"));
   faultsList.add(new Fault(3,"Fault3"));
   return faultsList;
 }

}