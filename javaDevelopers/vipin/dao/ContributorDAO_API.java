package javaDevelopers.vipin.dao;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.Contributor;

/**
 * <p>Title: ContributorDAO_API.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ContributorDAO_API {

  /**
   * Add a new contributor to the contributor list
   *
   * @param contributor
   * @return int primary key of the added contributor.
   *
   */
  public int addContributor(Contributor contributor);

  /**
   * Update the contributor info
   *
   * @param contributorId  Id of the contributor which need to be updated
   * @param contributor updated info about the contributor
   */
  public void updateContributor(int contributorId, Contributor contributor);

  /**
   * Get the contributor info for a particular contributor Id
   * @param contributorId
   * @return
   */
  public Contributor getContributor(int contributorId);

  /**
   * Remove this contributor from the list
   * @param contributorId
   */
  public void removeContributor(int contributorId);

  /**
   * Returns a list of all Contributors.
   *
   * @return ArrayList containing list of Contributor objects
   */
  public ArrayList getAllContributors();
}