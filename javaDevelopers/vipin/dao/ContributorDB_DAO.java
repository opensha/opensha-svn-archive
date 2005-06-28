package javaDevelopers.vipin.dao;

import java.util.ArrayList;
import javaDevelopers.vipin.vo.Contributor;

/**
 * <p>Title:ContributorDB_DAO.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ContributorDB_DAO implements ContributorDAO_API {
  private final static String TABLE_NAME="Contributors";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String CONTRIBUTOR_NAME="Contributor_Name";


  public int addContributor(Contributor contributor) {
    String sql = "insert into "+TABLE_NAME+"("+ CONTRIBUTOR_NAME+")"+
        " values(\""+contributor.getName()+"\")";
    return -1;
  }


  public void updateContributor(int contributorId, Contributor contributor) {
    String sql = "update "+TABLE_NAME+" set "+CONTRIBUTOR_NAME+"=\""+
        contributor.getName()+"\" where "+CONTRIBUTOR_ID+"="+contributorId;
  }

  public Contributor getContributor(int contributorId) {
    /**@todo Implement this javaDevelopers.vipin.dao.ContributorDAO_API method*/
    throw new java.lang.UnsupportedOperationException("Method getContributor() not yet implemented.");
  }

  public void removeContributor(int contributorId) {
    /**@todo Implement this javaDevelopers.vipin.dao.ContributorDAO_API method*/
    throw new java.lang.UnsupportedOperationException("Method removeContributor() not yet implemented.");
  }

  public ArrayList getAllContributors() {
    /**@todo Implement this javaDevelopers.vipin.dao.ContributorDAO_API method*/
    throw new java.lang.UnsupportedOperationException("Method getAllContributors() not yet implemented.");
  }

}