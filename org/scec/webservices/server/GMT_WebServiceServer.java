package org.scec.webservices.server;

import javax.activation.*;
import java.io.*;

import org.scec.webservices.GMT_WebServiceAPI;
import org.scec.mapping.gmtWrapper.RunScript;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class GMT_WebServiceServer implements GMT_WebServiceAPI{

  private final static String FILE_PATH="/opt/install/jakarta-tomcat-4.1.24/webapps/gmtWS/";
  private final static String GMT_DATA_DIR ="gmtData/" ;
  private final static String DATA_PATH ="/usr/scec/data/gmt/";
  private final static String GMT_SCRIPT_FILE = "gmtScript.txt";
  public GMT_WebServiceServer() {
  }

  public String runGMT_Script(String[] fileName , DataHandler[] dh) throws java.rmi.RemoteException{
    //string that decides the name of the output gmt files
    String outFile = null;
    //gets the current time in milliseconds to be the new director for each user
    String currentMilliSec ="";
    currentMilliSec += System.currentTimeMillis();

    //Assuming that first file that we add is the GMT Script file and second
    //is the XYZ dataSet file and rest are the data file if any.
    try{
      //Name of the directory in which we are storing all the gmt data for the user
      String newDir= null;
      //all the user gmt stuff will be stored in this directory
      File mainDir = new File(FILE_PATH+GMT_DATA_DIR);
      //create the main directory if it does not exist already
      if(!mainDir.isDirectory()){
        boolean success = (new File(FILE_PATH+GMT_DATA_DIR)).mkdir();
      }
      newDir = FILE_PATH+GMT_DATA_DIR+currentMilliSec;
      //create a gmt directory for each user in which all his gmt files will be stored
      boolean success =(new File(newDir)).mkdir();

      //writing the GMT file to the server
      dh[0].writeTo(new FileOutputStream(fileName[0]));

      //reading the gmtScript file that user sent as the attachment and create
      //a new gmt script inside the directory created for the user.
      //The new gmt script file created also has one minor modification
      //at the top of the gmt script file I am adding the "cd ... " command so
      //that it should pick all the gmt related files from the directory cretade for the user.
      //reading the gmt script file sent by user as te attchment
      FileReader fr = new FileReader(fileName[0]);
      BufferedReader br = new BufferedReader(fr);
      String gmtScriptFile = newDir+"/"+this.GMT_SCRIPT_FILE;
      //creating a new gmt script for the user and writing it ot the directory created for the user
      FileWriter fw = new FileWriter(gmtScriptFile);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write("cd "+newDir+"/"+"\n");
      String gmtCommand = br.readLine();

      while(gmtCommand != null){
        bw.write(gmtCommand+"\n");
        gmtCommand = br.readLine();
      }
      br.close();
      bw.close();
      //writing the XYZ dataSet file to the disk
      dh[1].writeTo(new FileOutputStream(newDir+"/"+fileName[1]));

      System.out.println("GMT file: "+fileName[0]);
      System.out.println("XYZ file: "+fileName[1]);

      //writing the rest of the data files to the disk
      for(int i=2;i<fileName.length;++i)
        dh[i].writeTo(new FileOutputStream(DATA_PATH+fileName[i]));

      //running the gmtScript file
      String[] command ={"sh","-c","sh "+gmtScriptFile};
      RunScript.runScript(command);
      //name of the outputfiles
      outFile = fileName[1].substring(0,fileName[1].indexOf("."));
      // remove the temporary files created
      command[2]="rm "+fileName[0];
      RunScript.runScript(command);
      command[2]="rm temp"+outFile+".grd";
      RunScript.runScript(command);
      command[2]="rm temp_temp"+outFile+".grd_info";
      RunScript.runScript(command);
      command[2]="rm "+outFile+".cpt";
      RunScript.runScript(command);
    }catch(Exception e){
      e.printStackTrace();
    }
    //return the full path of the output JPEG file
    return "http://gravity.usc.edu/gmtWS/"+this.GMT_DATA_DIR+currentMilliSec+"/";
  }
}