package org.scec.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.ArrayList;

import org.scec.mapping.gmtWrapper.*;

/**
 * <p>Title: GMT_MapGeneratorServlet </p>
 * <p>Description: this servlet runs the GMT script based on the parameters and generates the
 * image file and returns that back to the calling application applet </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author :Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GMT_MapGeneratorServlet extends HttpServlet {

  private static final String IMAGE_URL_PATH="http://scec.usc.edu:9999/";
  //Process the HTTP Get request
 public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

   try {

     // get an input stream from the applet
     ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

     //gets the object for the GMT_MapGenerator script
     GMT_MapGenerator gmtMapInfo = (GMT_MapGenerator) inputFromApplet.readObject();

     //ArrayList that contains the contents of xyz file required by the  GMT
     ArrayList fileLines = (ArrayList)inputFromApplet.readObject();

     //name of the XYZ file that user had wanted to create
     String fileName = (String)inputFromApplet.readObject();

     //creating the xyz file from the contents in the ArrayList
     FileWriter fileWriter = new FileWriter(fileName);
     for(int i=0;i<fileLines.size();++i){
       fileWriter.write((String)fileLines.get(i));
       fileWriter.write("\n");
     }

     //jpg image name returned back by the gmt
     String imgName=gmtMapInfo.makeMap(fileName);


     //moving the .jpg , .ps and .xyz
     String command[] = {"sh","-c","mv "+fileName+" webpages/scenariomapimagefiles/"};
     RunScript.runScript(command);
     fileName = fileName.substring(0,fileName.indexOf("."));
     command[2] = "mv "+fileName+".ps webpages/scenariomapimagefiles/";
     RunScript.runScript(command);
     command[2] = "mv "+fileName+" webpages/scenariomapimagefiles/";
     RunScript.runScript(command);
     // remove the temporary files created
     command[2]="rm "+fileName+".grd";
     RunScript.runScript(command);
     command[2]="rm temp"+fileName+".grd";
     RunScript.runScript(command);
     command[2]="rm temp_temp"+fileName+".grd_info";
     RunScript.runScript(command);
     command[2]="rm "+fileName+".cpt";
     RunScript.runScript(command);
     command[2]="rm "+fileName+"HiResData.grd";
     RunScript.runScript(command);
     command[2]="rm "+fileName+"Inten.grd";
     RunScript.runScript(command);



     // report to the user whether the operation was successful or not
     // get an ouput stream from the applet
     ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
     //name of the image file as the URL
     outputToApplet.writeObject(IMAGE_URL_PATH+imgName);
     outputToApplet.close();

   } catch (Exception e) {
     // report to the user whether the operation was successful or not
     e.printStackTrace();
   }
 }


 //Process the HTTP Post request
 public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
   // call the doPost method
   doGet(request,response);
 }

}