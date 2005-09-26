package org.opensha.refFaultParamDb.excelToDatabase;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;

/**
 * <p>Title:ReadReferencesFile.java </p>
 * <p>Description: This class reads the references file and puts the references into
 * the database. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ReadReferencesFile {
  private final static String FILENAME = "org\\opensha\\refFaultParamDb\\References.txt";
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  public ReadReferencesFile() {
    try {
      ArrayList referencesList = org.opensha.util.FileUtils.loadFile(FILENAME);
      String line;
      StringTokenizer tokenizer;
      for (int i = 0; i < referencesList.size(); ++i) {
        line = (String)referencesList.get(i);
        tokenizer = new StringTokenizer(line,";");
        // make the reference VO
        Reference reference = new Reference(tokenizer.nextToken().trim(),
                                            tokenizer.nextToken().trim());
        // put reference into database
        referenceDAO.addReference(reference);
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

}