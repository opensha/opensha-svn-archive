package org.scec.sha.mapping;

import java.util.*;
//import javax.activation.*;
//import java.text.DecimalFormat;
//import java.net.*;
//import java.io.*;

import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.*;
import org.scec.sha.earthquake.EqkRupture;
import org.scec.sha.surface.*;


/**
 * <p>Title: GMT_MapGeneratorForShakeMaps</p>
 * <p>Description: This class extends the GMT_MapGenerator to extend the
 * GMT functionality for the shakeMaps.</p>
 * @author : Edward (Ned) Field , Nitin Gupta
 * @dated Dec 31,2003
 */

public class GMT_MapGeneratorForShakeMaps extends GMT_MapGenerator{

  /**
   * Name of the class
   */
  protected final static String C = "GMT_MapGeneratorForShakeMaps";

  // for debug purpose
  protected final static boolean D = true;

  private String EQK_RUP_XYZ_FILE_NAME = "eqkRup_data.txt";
  XYZ_DataSetAPI eqkRup_xyzDataSet;

  EqkRupture eqkRup;


  /**
   * Makes scenarioshake maps locally using the GMT on the users own computer
   * @param xyzDataSet: XYZ Data
   * @param eqkRup : EarthRupture Object
   * @param hypLoc :Hypocenter Location
   * @param scaleLabel
   * @return
   */
  public String makeMapLocally(XYZ_DataSetAPI xyzDataSet, EqkRupture eqkRupture, String scaleLabel){
    eqkRup = eqkRupture;
    return super.makeMapLocally(xyzDataSet,scaleLabel);
  }

  /**
   * Makes scenarioshake maps using the GMT on the gravity.usc.edu server(Linux server).
   * Implemented as the servlet, using which we can actual java serialized object.
   * @param xyzDataSet: XYZ Data
   * @param eqkRup : EarthRupture Object
   * @param hypLoc : Hypocenter Location
   * @param scaleLabel
   * @return: URL to the image
   */
  public String makeMapUsingServlet(XYZ_DataSetAPI xyzDataSet, EqkRupture eqkRupture, String scaleLabel){
    eqkRup = eqkRupture;
    return super.makeMapUsingServlet(xyzDataSet, scaleLabel);
  }

  /**
   * Makes scenarioshake maps using the GMT on the gravity.usc.edu server(Linux server).
   * Implemented as the webservice, using which we can send files as the attachment.
   * @param xyzDataSet: XYZ Data
   * @param eqkRup : EarthRupture Object
   * @param hypLoc :Hypocenter Location
   * @param scaleLabel
   * @return: URL to the image
   */
  public String makeMapUsingWebServer(XYZ_DataSetAPI xyzDataSet, EqkRupture eqkRupture,String scaleLabel){
    eqkRup = eqkRupture;
    return super.makeMapUsingWebServer(xyzDataSet, scaleLabel);
  }

  /**
   * This method adds intermediate script commands to plot the earthquake rupture and hypocenter.
   */
  protected void addIntermediateGMT_ScriptLines(Vector gmtLines) {

    if (D) System.out.println("got into "+C+".addIntermediateGMT_ScriptLines");

    // Make an XYZ file for the rupture surface plot
    GriddedSurfaceAPI surface = eqkRup.getRuptureSurface();
    Vector fileLines = new Vector();
    Location loc;
    int rows = surface.getNumRows();
    int cols = surface.getNumCols();
    if(D) System.out.println(C+" rows, cols: "+rows+", "+cols);
    int c, r;



    String gmtSymbol = " c0.04i";    // draw a circle of 0.04 inch diameter
    // get points along the top
    Location lastLoc = surface.getLocation(0,0);
    r=0;
    for(c=1;c<cols;c++) {
      if(D) System.out.println(C+" row, col: "+r+", "+c);
      fileLines.add(new String("> \n-Z"+(float)lastLoc.getDepth()));
      fileLines.add(new String((float)lastLoc.getLongitude()+"  "+
                           (float)lastLoc.getLatitude()));
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                           (float)loc.getLatitude()));
      lastLoc = loc;
    }
    // get points down the side
    c = cols-1;
    for(r=1;r<rows;r++) {
      if(D) System.out.println(C+" row, col: "+r+", "+c);
      fileLines.add(new String("> \n-Z"+(float)lastLoc.getDepth()));
      fileLines.add(new String((float)lastLoc.getLongitude()+"  "+
                           (float)lastLoc.getLatitude()));
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                           (float)loc.getLatitude()));
      lastLoc = loc;
    }
    // get points along the bottom
    r=rows-1;
    for(c=cols-2;c>=0;c--) {
      fileLines.add(new String("> \n-Z"+(float)lastLoc.getDepth()));
      fileLines.add(new String((float)lastLoc.getLongitude()+"  "+
                           (float)lastLoc.getLatitude()));
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                           (float)loc.getLatitude()));
      lastLoc = loc;
    }
    // get points up the side
    c=0;
    for(r=rows-2;r>=0;r--) {
      fileLines.add(new String("> \n-Z"+(float)lastLoc.getDepth()));
      fileLines.add(new String((float)lastLoc.getLongitude()+"  "+
                           (float)lastLoc.getLatitude()));
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                           (float)loc.getLatitude()));
      lastLoc = loc;
    }




/*

        String gmtSymbol = " c0.04i";    // draw a circle of 0.04 inch diameter
    // get points along the top
    r=0;
    for(c=0;c<cols;c++) {
      if(D) System.out.println(C+" row, col: "+r+", "+c);
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()+gmtSymbol));
    }
    // get points down the side
    c = cols-1;
    for(r=1;r<rows;r++) {
      if(D) System.out.println(C+" row, col: "+r+", "+c);
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()+gmtSymbol));
    }
    // get points along the bottom
    r=rows-1;
    for(c=cols-2;c>=0;c--) {
      if(D) System.out.println(C+" row, col: "+r+", "+c);
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()+gmtSymbol));
    }
    // get points up the side
    c=0;
    for(r=rows-2;r>=0;r--) {
      if(D) System.out.println(C+" row, col: "+r+", "+c);
      loc = surface.getLocation(r,c);
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()+gmtSymbol));
    }

*/



    /*
    // add all points on the fault
    for(int c=0;c<cols;c++) {
      for(int r=0;r<rows;r++) {
        loc = surface.getLocation(r,c);
        fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()));
      }
    */



    /*
    // add all points on the fault
    for(int c=0;c<cols;c++) {
      for(int r=0;r<rows;r++) {
        loc = surface.getLocation(r,c);
        fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()));
      }
    */
/*
    // add hypocenter location if it's not null
    loc = eqkRup.getHypocenterLocation();
    if(loc != null) {
      fileLines.add(new String((float)loc.getLongitude()+"  "+
                                 (float)loc.getLatitude()+"  "+
                                 (float)loc.getDepth()+"  a0.4i"));
    }
*/
    makeFileFromLines(fileLines, EQK_RUP_XYZ_FILE_NAME);

    // command line to convert xyz file to grd file
    String commandLine = GMT_PATH+"psxy "+ EQK_RUP_XYZ_FILE_NAME + region +
                         projWdth +" -K -O -H -M -W10 -CeqkRupSurface.cpt >> " + PS_FILE_NAME;
    gmtLines.add(commandLine+"\n");



  }


}