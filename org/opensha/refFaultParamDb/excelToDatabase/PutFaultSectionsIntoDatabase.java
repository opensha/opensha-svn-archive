package org.opensha.refFaultParamDb.excelToDatabase;

import org.opensha.util.FileUtils;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import java.util.StringTokenizer;

/**
 * <p>Title: PutFaultSectionsIntoDatabase.java </p>
 * <p>Description: this class reads the 2006_fault_sections.MID and
 * 2006_fault_sections.MIF files. These files contain fault section information
 * and provided by Chris Wills.
 *  It puts the information into database.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PutFaultSectionsIntoDatabase {
  // input files
  private final static String INPUT_FILE1 = "2006_fault_sections.MID";
  private final static String INPUT_FILE2 = "2006_fault_sections.MIF";

  /**
   * Put fault sections into the database
   */
  public PutFaultSectionsIntoDatabase() {
    try {
      ArrayList fileLines1 = FileUtils.loadFile(INPUT_FILE1);
      ArrayList fileLines2 = FileUtils.loadFile(INPUT_FILE2);

    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get fault section from a file line
   *
   * @param line
   * @return
   */
  private FaultSectionVer2 getFaultSection(String line) {
    FaultSectionVer2 faultSection = new FaultSectionVer2();
    StringTokenizer tokenizer = new StringTokenizer(",");
    String faultSectionName = tokenizer.nextToken().trim();
    String model = tokenizer.nextToken().trim();
    String source = tokenizer.nextToken().trim();
    String faultType = tokenizer.nextToken().trim();
    String rake = tokenizer.nextToken().trim();
    String dip = tokenizer.nextToken().trim();
    String dipDirection = tokenizer.nextToken().trim();
    String slipRate = tokenizer.nextToken().trim();
    String slipRateUncert = tokenizer.nextToken().trim();
    String rank = tokenizer.nextToken().trim();
    String upperDepth = tokenizer.nextToken().trim();
    String lowerDepth = tokenizer.nextToken().trim();
    String width = tokenizer.nextToken().trim();
    String fileName = tokenizer.nextToken().trim();
    String rfactor1 = tokenizer.nextToken().trim();
    String rFactor1Wt = tokenizer.nextToken().trim();
    String rfactor2 = tokenizer.nextToken().trim();
    String rFactor2Wt = tokenizer.nextToken().trim();
    String rfactor3 = tokenizer.nextToken().trim();
    String rFactor3Wt = tokenizer.nextToken().trim();
    return faultSection;
  }


  public static void main(String[] args) {
    PutFaultSectionsIntoDatabase putFaultSectionsIntoDatabase1 = new PutFaultSectionsIntoDatabase();
  }

}