package org.scec.sha.gui.infoTools;

import java.io.*;
import sun.net.smtp.SmtpClient;

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
  static final String FROM = "sceccme";
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
                                          String emailAddr) {
    try {
      FileReader file = new FileReader(fileName);
      BufferedReader reader = new BufferedReader(file);
      int actualFiles = Integer.parseInt(reader.readLine().trim());
      reader.close();
      file.close();

      // Create a new instance of SmtpClient. Here we assume that
      // the local host machine is the SMTP server
      SmtpClient smtp = new SmtpClient(HOST);
      // Sets the originating e-mail address
      smtp.from(FROM);
      // Sets the recipients' e-mail address
      smtp.to(emailAddr);
      // Create an output stream to the connection
      PrintStream msg = smtp.startMessage();
      msg.println("To: " + emailAddr); // so mailers will display the recipient's e-mail address
      msg.println("From: " + FROM); // so that mailers will display the sender's e-mail address
      msg.println("Subject: Grid Job Status \n");
      msg.println("Grid Computation complete\nExpected Num of Files="+
                  expectedNumOfFiles+"\nFiles Generated="+actualFiles);
      // Close the connection to the SMTP server and send the message out to the recipient
      smtp.closeServer();
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
                                        args[2]);
  }

}