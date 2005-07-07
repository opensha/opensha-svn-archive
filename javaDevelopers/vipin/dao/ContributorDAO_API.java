package javaDevelopers.vipin.dao;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.Contributor;
import javaDevelopers.vipin.dao.exception.*;

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
  public int addContributor(Contributor contributor) throws InsertException;

  /**
   * Update the contributor info
   *
   * @param contributorId  Id of the contributor which need to be updated
   * @param contributor updated info about the contributor
   */
  public boolean updateContributor(int contributorId, Contributor contributor) throws UpdateException;

  /**
   * Get the contributor info for a particular contributor Id
   * @param contributorId
   * @return
   */
  public Contributor getContributor(int contributorId) throws QueryException;

  /**
   * Remove this contributor from the list
   * @param contributorId
   */
  public boolean removeContributor(int contributorId) throws UpdateException;

  /**
   * Returns a list of all Contributors.
   *
   * @return ArrayList containing list of Contributor objects
   */
  public ArrayList getAllContributors() throws QueryException;
}