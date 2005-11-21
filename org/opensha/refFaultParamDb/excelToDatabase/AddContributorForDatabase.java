package org.opensha.refFaultParamDb.excelToDatabase;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.ContributorDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.vo.Contributor;

/**
 * <p>Title: AddContributorForDatabase.java </p>
 * <p>Description: Add a new contributor to the database </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddContributorForDatabase {
  private final static DB_AccessAPI dbConnPas= new DB_ConnectionPool();

  public static void main(String[] args) {
    ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbConnPas);
    Contributor contributor = new Contributor();
    contributor.setEmail("fault_sandbox");
    contributor.setFirstName("Fault");
    contributor.setLastName("Sandbox");
    contributor.setName("fault_sandbox");
    String password = "";
    contributorDAO.addContributor(contributor,password);
  }

}