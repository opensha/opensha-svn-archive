package javaDevelopers.vipin.dao;
import javaDevelopers.vipin.vo.QFault2002B;
import javaDevelopers.vipin.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: QFault2002B_DAO_API.java </p>
 * <p>Description: Get the fault section info from Qfault2002B table</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface QFault2002B_DAO_API {



 /**
  * Get the fault section  info for a particular Id
  * @param sectionId
  * @return
  */
 public QFault2002B getFaultSection(String sectionId) throws QueryException;


 /**
  * Returns a list of all Fault Sections
  *
  * @return ArrayList containing list of QFault2002B objects
  */
 public ArrayList getAllFaultSections() throws QueryException;

}
