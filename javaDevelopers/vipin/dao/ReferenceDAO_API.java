package javaDevelopers.vipin.dao;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.Reference;
import javaDevelopers.vipin.dao.exception.*;

/**
 * <p>Title: ReferenceDAO_API.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ReferenceDAO_API {

  /**
   * Add a new reference
   *
   *
   */
  public int addReference(Reference reference) throws InsertException;

  /**
   * Update the reference info
   */
  public boolean updateReference(int referenceId, Reference reference) throws UpdateException;

  /**
   * Get the reference info for a particular reference Id
   */
  public Reference getReference(int referenceId) throws QueryException;

  /**
   * Remove this reference from the list
   * @param referenceId
   */
  public boolean removeReference(int referenceId) throws UpdateException;

  /**
   * Returns a list of all References
   */
  public ArrayList getAllReferences() throws QueryException;
}
