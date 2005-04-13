package org.opensha.util;

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
  public static int runScript(String[] command) {
    int i=0;
    try {
      // wait for the shell script to end
      System.out.println("Command to execute: " +command[2]);
      Process p=Runtime.getRuntime().exec(command);
      i=displayProcessStatus(p);
    } catch(Exception e) {
      // if there is some other exception, print the detailed explanation
      System.out.println("Exception in Executing Shell Script:"+e);
      e.printStackTrace();
    }
    return i;
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

  /**
   * Display the process status while it is executing
   *
   * @param pr
   * @return
   */
  public static int displayProcessStatus(Process pr) {
    InputStream is = pr.getErrorStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    InputStream es = pr.getInputStream();
    InputStreamReader esr = new InputStreamReader(es);
    BufferedReader ebr = new BufferedReader(esr);
    String processStr=null, errStr=null;
    try {
      // get the error and process output strings
      while ( ( (errStr = ebr.readLine()) != null) ||
             ( (processStr = br.readLine()) != null)) {
        if (processStr != null)
          System.out.println(processStr);
        if (errStr != null)
          System.out.println(errStr);
        if ( (processStr == null) && (errStr == null)) {
          break;
        }
      }
      int exit = pr.waitFor();
      return exit;
    }catch(Exception e) { e.printStackTrace(); }
    return -1;
  }
}
