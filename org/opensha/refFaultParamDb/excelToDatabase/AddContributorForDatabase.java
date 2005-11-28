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
  private final static DB_AccessAPI dbConn= new DB_ConnectionPool();;

  public static void main(String[] args) {
    ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbConn);
    // add fault _sandbox
    /*Contributor contributor = new Contributor();
    contributor.setEmail("fault_sandbox@usc.edu");
    contributor.setFirstName("Fault");
    contributor.setLastName("Sandbox");
    contributor.setName("fault_sandbox");
    String password = "";
    contributorDAO.addContributor(contributor,password);
    // add vgupta
    contributor = new Contributor();
    contributor.setEmail("vgupta@usc.edu");
    contributor.setFirstName("Vipin");
    contributor.setLastName("Gupta");
    contributor.setName("vgupta");
    password = "";
    contributorDAO.addContributor(contributor,password);
    System.exit(0);*/
    Contributor contributor = new Contributor();
    contributor.setEmail("perry@gps.caltech.edu");
    contributor.setFirstName("Sue");
    contributor.setLastName("Perry");
    contributor.setName("perry");
    String password = "";
    contributorDAO.addContributor(contributor,password);
    System.exit(0);
  }

}