package org.scec.sha.gui.infoTools;

import java.io.*;
import sun.net.smtp.SmtpClient;
import org.scec.util.MailUtil;

/**
 * <p>Title: HazardMapCalcPostProcessing.java </p>
 * <p>Description: This class will count the number of files created using grid computing
 * and compare it using expected number of files. Also, it will mail the user
 * that the computation is complete</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta, Ned Field
 * @date Apr 12, 2004
 * @version 1.0
 */


public class HazardMapCalcPostProcessing {
  static final String FROM = "OpenSHA-CME";
  static final String HOST = "email.usc.edu";


  /**
   * It will
   *
   * @param fileName Name of file which will contain single line which gives us
   * actual nuimber of files generated.
   * @param expectedNumOfFiles
   * @param emailAddr
   */
  public HazardMapCalcPostProcessing(String fileName,
                                     int expectedNumOfFiles,
                                     String emailAddr,
                                     String datasetId,
                                     String startTime) {
    try {
      FileReader file = new FileReader(fileName);
      BufferedReader reader = new BufferedReader(file);
      int actualFiles = Integer.parseInt(reader.readLine().trim());
      reader.close();
      file.close();
      String mailSubject = "Grid Job Status";
      String mailMessage = "THIS IS A AUTOMATED GENERATED EMAIL. PLEASE DO NOT REPLY BACK TO THIS ADDRESS.\n\n\n"+
                  "Grid Computation complete\n"+
                  "Expected Num of Files="+expectedNumOfFiles+"\n"+
                  "Files Generated="+actualFiles+"\n"+
                  "Dataset Id="+datasetId+"\n"+
                  "Simulation Start Time="+startTime+"\n"+
                  "Simulation End Time="+java.util.Calendar.getInstance().getTime().toString().replaceAll(" ","_");
      MailUtil.sendMail(HOST, FROM, emailAddr, mailSubject, mailMessage);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * It will
   *
   * @param fileName Name of file which will contain single line which gives us
   * actual nuimber of files generated.
   * @param expectedNumOfFiles
   * @param emailAddr
   */
  public HazardMapCalcPostProcessing(int expectedNumOfFiles,
                                     String emailAddr,
                                     String datasetId,
                                     String startTime) {
    try {
      String mailSubject = "Grid Job Status";
      String mailMessage = "THIS IS A AUTOMATED GENERATED EMAIL. PLEASE DO NOT REPLY BACK TO THIS ADDRESS.\n\n\n"+
                  "Grid Computation complete\n"+
                  "Expected Num of Files="+expectedNumOfFiles+"\n"+
                  "Dataset Id="+datasetId+"\n"+
                  "Simulation Start Time="+startTime+"\n"+
                  "Simulation End Time="+java.util.Calendar.getInstance().getTime().toString().replaceAll(" ","_");
      MailUtil.sendMail(HOST, FROM, emailAddr, mailSubject, mailMessage);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * It will accept 3 command line arguments
   * 1. Name of the file which will contain the actual number of files generated by hazard
   * map calculator
   * 2. Expected number of output files
   * 3. Email address to whom the status has to be sent
   * @param args
   */
  public static void main(String[] args) {
    HazardMapCalcPostProcessing hazardMapCalcPostProcessing1 =
        new HazardMapCalcPostProcessing(args[0], Integer.parseInt(args[1]),
                                        args[2], args[3],
                                        args[4]);
  }

}