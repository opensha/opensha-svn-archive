package org.scec.sha.earthquake.STEP;

import java.io.*;
/**
 * <p>Title: RunScript</p>
 * <p>Description : Accepts the command and runs the shell script
 * @author: Nitin Gupta and Vipin Gupta
 * @created:Dec 27, 2002
 * @version 1.0
 */

public class RunScript {


  /**
   * accepts the command and executes on java runtime environment
   *
   * @param command : command to execute
   */
  public static void runScript(String[] command) {
    try {
      // wait for the shell script to end
      System.out.println("Command to execute: " +command[2]);
      Process p=Runtime.getRuntime().exec(command);
      p.waitFor();
      int i=p.exitValue();

      // check the process status after the process ends
      if ( i == 0 ) {
        // Display the normal o/p if script completed successfully.
        System.out.println("script exited with i =" + i);
        displayOutput(p.getInputStream());
      }
      else {
        // Display the normal and error o/p if script failed.
        System.out.println("script exited with i =" + i);
        displayOutput(p.getErrorStream());
        displayOutput(p.getInputStream());
      }

    } catch(Exception e) {
      // if there is some other exception, print the detailed explanation
      System.out.println("Exception in Executing Shell Script:"+e);
      e.printStackTrace();
    }
  }

  /**
   * display the input stream
   * @param is inputstream
   * @throws Exception
   */
  public static void displayOutput(InputStream is) throws Exception {
    String s;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      while ((s = br.readLine()) != null)
        System.out.println(s);
    } catch (Exception e) {
      System.out.println("Exception in RunCoreCode:displayOutput:"+e);
      e.printStackTrace();
    }
  }
}
