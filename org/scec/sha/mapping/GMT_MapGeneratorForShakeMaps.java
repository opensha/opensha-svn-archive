package org.scec.sha.mapping;

import java.util.*;
//import javax.activation.*;
//import java.text.DecimalFormat;
//import java.net.*;
//import java.io.*;

import org.scec.param.*;
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
  protected final static boolean D = false;

  private String EQK_RUP_XYZ_FILE_NAME = "eqkRup_data.txt";
  XYZ_DataSetAPI eqkRup_xyzDataSet;

  EqkRupture eqkRup;

  // for the rupture surface plotting parameter
  public final static String RUP_PLOT_PARAM_NAME = "Rupture-Surface Plotting";
  private final static String RUP_PLOT_PARAM_PERIMETER = "Draw Perimeter";
  private final static String RUP_PLOT_PARAM_POINTS = "Draw Discrete Points";
  private final static String RUP_PLOT_PARAM_NOTHING = "Draw Nothing";
  private final static String RUP_PLOT_PARAM_INFO = "The hypocenter will also be plotted (as a star) if it has been set" ;
  StringParameter rupPlotParam;

  public GMT_MapGeneratorForShakeMaps() {

    super();

    StringConstraint rupPlotConstraint = new StringConstraint();
    rupPlotConstraint.addString( RUP_PLOT_PARAM_PERIMETER );
    rupPlotConstraint.addString( RUP_PLOT_PARAM_POINTS );
    rupPlotConstraint.addString( RUP_PLOT_PARAM_NOTHING );
    rupPlotParam = new StringParameter( RUP_PLOT_PARAM_NAME, rupPlotConstraint, RUP_PLOT_PARAM_PERIMETER );
    rupPlotParam.setInfo( RUP_PLOT_PARAM_INFO );

    adjustableParams.addParameter(rupPlotParam);

  }




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

    String rupPlot = (String) rupPlotParam.getValue();

    if(!rupPlot.equals(RUP_PLOT_PARAM_NOTHING)) {

      String commandLine;

      // Get the surface and associated info
      GriddedSurfaceAPI surface = eqkRup.getRuptureSurface();
      Vector fileLines = new Vector();
      Location loc;
      int rows = surface.getNumRows();
      int cols = surface.getNumCols();
      if(D) System.out.println(C+" rows, cols: "+rows+", "+cols);
      int c, r;

      if(rupPlot.equals(RUP_PLOT_PARAM_PERIMETER)) {
        //  This draws separate segments between each neighboring point
        double dMin = surface.getLocation(0,0).getDepth();
        double dMax = surface.getLocation(surface.getNumRows()-1,0).getDepth();
        double maxShade = 235; // up to 255
        double minShade = 20; // as low os 0
        int shade;
        Location lastLoc = surface.getLocation(0,0);

        if(eqkRup.getRuptureSurface().getAveDip() < 90) { // do only if not vertically dipping

          // get points down the far side
          c = cols-1;
          lastLoc = surface.getLocation(0,c);
          for(r=1;r<rows;r++) {
            if(D) System.out.println(C+" row, col: "+r+", "+c);
            shade = 20 + (int)((maxShade-minShade)*(dMax - lastLoc.getDepth())/(dMax-dMin));
            fileLines.add(new String(">  -W8/"+shade+"/"+shade+"/"+shade));
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
            if(D) System.out.println(C+" row, col: "+r+", "+c);
            shade = 20 + (int)((maxShade-minShade)*(dMax - lastLoc.getDepth())/(dMax-dMin));
            fileLines.add(new String(">  -W8/"+shade+"/"+shade+"/"+shade));
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
            if(D) System.out.println(C+" row, col: "+r+", "+c);
            shade = 20 + (int)((maxShade-minShade)*(dMax - lastLoc.getDepth())/(dMax-dMin));
            fileLines.add(new String(">  -W8/"+shade+"/"+shade+"/"+shade));
            fileLines.add(new String((float)lastLoc.getLongitude()+"  "+
                                     (float)lastLoc.getLatitude()));
            loc = surface.getLocation(r,c);
            fileLines.add(new String((float)loc.getLongitude()+"  "+
                                     (float)loc.getLatitude()));
            lastLoc = loc;
          }
        }
        // get points along the top - do this last so vertical faults have the shade of the top
        r=0;
        for(c=1;c<cols;c++) {
          if(D) System.out.println(C+" row, col: "+r+", "+c);
          shade = 20 + (int)((maxShade-minShade)*(dMax - lastLoc.getDepth())/(dMax-dMin));
          fileLines.add(new String(">  -W8/"+shade+"/"+shade+"/"+shade));
          fileLines.add(new String((float)lastLoc.getLongitude()+"  "+
                                   (float)lastLoc.getLatitude()));
          loc = surface.getLocation(r,c);
          fileLines.add(new String((float)loc.getLongitude()+"  "+
                                   (float)loc.getLatitude()));
          lastLoc = loc;
        }

        // make the data file in the script:
        gmtLines.add(COMMAND_PATH+"cat << END > "+EQK_RUP_XYZ_FILE_NAME);
        Iterator it = fileLines.iterator();
        while(it.hasNext()) {
          gmtLines.add((String)it.next());
        }
        gmtLines.add("END\n");

        // plot the rupture surface points
        commandLine = GMT_PATH+"psxy "+ EQK_RUP_XYZ_FILE_NAME + region +
                      projWdth +" -K -O -M >> " + PS_FILE_NAME;
        gmtLines.add(commandLine+"\n");
        gmtLines.add(COMMAND_PATH+"rm "+EQK_RUP_XYZ_FILE_NAME+"\n");
      }
      else {
      // Plot the discrete surface points
        String gmtSymbol = " c0.04i";    // draw a circles of 0.04 inch diameter
        // get points along the top

        for(r=surface.getNumRows()-1;r>=0;r--) {   // reverse order so upper points plot over lower points
          for(c=0;c<surface.getNumCols()-1;c++) {
            loc = surface.getLocation(r,c);
            fileLines.add(new String((float)loc.getLongitude()+"  "+
                                     (float)loc.getLatitude()+"  "+
                                     (float)loc.getDepth()+gmtSymbol));
          }
        }

        // make the file in the script:
        gmtLines.add(COMMAND_PATH+"cat << END > "+EQK_RUP_XYZ_FILE_NAME);
        Iterator it = fileLines.iterator();
        while(it.hasNext()) gmtLines.add((String)it.next());
        gmtLines.add("END\n");

        // make the cpt file for the fault points
        double dep1 = surface.getLocation(0,0).getDepth();
        double dep2 = surface.getLocation(surface.getNumRows()-1,0).getDepth();
        commandLine = COMMAND_PATH+"cat << END > temp_rup_cpt\n"+
                  (float)dep1+" 235 235 253 "+(float)dep2+" 20 20 20\n"+
                  "F 235 235 235\nB 20 20 20\nEND";
        gmtLines.add(commandLine+"\n");

        // plot the rupture surface points
        commandLine = GMT_PATH+"psxy "+ EQK_RUP_XYZ_FILE_NAME + region +
                  projWdth +" -K -O -S -Ctemp_rup_cpt >> " + PS_FILE_NAME;
        gmtLines.add(commandLine+"\n");
        gmtLines.add(COMMAND_PATH+"rm temp_rup_cpt "+EQK_RUP_XYZ_FILE_NAME+"\n");
      }


      // add hypocenter location if it's not null - the data files is generated by the script
      // this has two data lines because GMT needs at least two lines in an XYZ file
      loc = eqkRup.getHypocenterLocation();
      if(loc != null) {
        commandLine = COMMAND_PATH+"cat << END > temp_hyp\n"+
                      (float)loc.getLongitude()+"  "+(float)loc.getLatitude()+"  "+(float)loc.getDepth()+"\n"+
                      (float)loc.getLongitude()+"  "+(float)loc.getLatitude()+"  "+(float)loc.getDepth()+"\n"+
                      "END";
        gmtLines.add(commandLine+"\n");
        commandLine = GMT_PATH+"psxy temp_hyp "+region+
                      projWdth +" -K -O -Sa0.4i -W8/0/0/0 >> " + PS_FILE_NAME;
        gmtLines.add(commandLine+"\n");
        gmtLines.add(COMMAND_PATH+"rm temp_hyp\n");
      }
    }
  }
}