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

  private final static String filePath="/opt/install/jakarta-tomcat-4.1.24/webapps/gmtWS/data/";
  private final static String dataPath ="/usr/scec/data/gmt/";
  public GMT_WebServiceServer() {
  }

  public String runGMT_Script(String[] fileName , DataHandler[] dh) throws java.rmi.RemoteException{
    String outFile = null;
    try{

      dh[0].writeTo(new FileOutputStream(fileName[0]));
      //writing the XYZ dataSet file to the disk
      dh[1].writeTo(new FileOutputStream(fileName[1]));

      System.out.println("GMT file: "+fileName[0]);
      System.out.println("XYZ file: "+fileName[1]);

      //writing the rest of the data files to the disk
      for(int i=2;i<fileName.length;++i)
        dh[i].writeTo(new FileOutputStream(dataPath+fileName[i]));

      //Assuming that first file that we add is the GMT Script file and second
      //is the XYZ dataSet file and rest are the data file if any.
      FileReader fr = new FileReader(fileName[0]);
      BufferedReader br = new BufferedReader(fr);
      String[] command ={"sh","-c","sh "+fileName[0]};
      RunScript.runScript(command);
      /*command[2] =  br.readLine();
      while(command[2] !=null){
        RunScript.runScript(command);
        command[2] = br.readLine();
      }*/

      //moving the .jpg , .ps and .xyz
      command[2] = "mv "+fileName[1]+" "+filePath;
      RunScript.runScript(command);
      outFile = fileName[1].substring(0,fileName[1].indexOf("."));
      command[2] = "mv "+outFile+".ps "+filePath;
      RunScript.runScript(command);
      command[2] = "mv "+outFile+".jpg "+filePath;
      RunScript.runScript(command);

      //command[2] = "mv "+imgName+" webpages/scenariomapimagefiles/";
      RunScript.runScript(command);
      // remove the temporary files created
      command[2]="rm "+outFile+".grd";
      RunScript.runScript(command);
      command[2]="rm temp"+outFile+".grd";
      RunScript.runScript(command);
      command[2]="rm temp_temp"+outFile+".grd_info";
      RunScript.runScript(command);
      command[2]="rm "+outFile+".cpt";
      RunScript.runScript(command);
      command[2]="rm "+outFile+"HiResData.grd";
      RunScript.runScript(command);
      command[2]="rm "+outFile+"Inten.grd";
      RunScript.runScript(command);


    }catch(Exception e){
      e.printStackTrace();
    }
    //return the full path of the output JPEG file
    return "http://gravity.usc.edu/gmtWS/data/"+outFile+".jpg";
  }
}