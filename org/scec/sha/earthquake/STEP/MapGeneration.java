package org.scec.sha.earthquake.STEP;

import java.io.*;
import java.util.*;

/**
 * <p>Title: MapGeneration</p>
 * <p>Description: This class generates Maps using the java wrapper around GMT</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @created:Dec 21,2002
 * @version 1.0
 */

public class MapGeneration {


  /**
   * main function to test this class
   *
   * @param args
   */
  public static void main(String[] args) {
    // to test this class, it should create a temp.jpg
    MapGeneration.generateMap();
  }

  /**
   * this function generates GMT map
   * It is wrapper function around GMT tool
   */
  public static void generateMap(){

    //file that defines the color scale
    String colorFile1="color.cpt";
    //file that defines the scale (looks better)
    String colorFile2="color2.cpt";
    try {
       String region = "-R-121/-115/32.5/35.5";

       //specifies the postScript filename.
       String filename ="probMap.ps";

       //string used to color file for the GMT maps
       String colorScale1 ="-13    0     0     125     -6    0     0     125\n"+
                         "-6    0     0     125     -5.    0     0     255\n"+
                         "-5.    0     0     255     -4    0     255     255\n"+
                         "-4   0     255    255     -2.5     255     255     0\n"+
                         "-2.5    255   255     0       0     255   0     0\n";

       //string used to generate the scaling of the colors to make them look better.
       String colorScale2 ="-6    0     0     125     -5.    0     0     255\n"+
                           "-5.    0     0     255     -4    0     255     255\n"+
                           "-4   0     255    255     -2.5     255     255     0\n"+
                           "-2.5    255   255     0       0     255   0     0\n";

       //Generate the color.cpt file with the values specified in the colorScale1 string.
       FileWriter file = new FileWriter(colorFile1);
       BufferedWriter oBuf= new BufferedWriter(file);
       oBuf.write(colorScale1);
       oBuf.close();

       //Generate the color2.cpt file with the values specified in the colorScale2 string.
       file=new FileWriter(colorFile2);
       oBuf= new BufferedWriter(file);
       oBuf.write(colorScale2);
       oBuf.close();

       //command to be executed during the runtime.
       String[] command ={"sh","-c","xyz2grd LatLonAmpData.txt -Gdata.grd -I0.05 "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
       RunScript.runScript(command);

       command[2] ="grdsample data.grd -Gdata.hiRes.grd -I0.3m -Q";
       RunScript.runScript(command);

       command[2]="grdcut 18.grd -Gtopo.grd "+region;
       RunScript.runScript(command);

       command[2]="grdgradient topo.grd -A330 -Ginten.grd -Nt -V";
       RunScript.runScript(command);

       command[2] ="grdmath inten.grd -1 ADD 2 DIV  = inten2.grd";
       RunScript.runScript(command);

       command[2]="gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR 0/0/0";
       RunScript.runScript(command);

       command[2]="grdimage data.hiRes.grd -X1.5i -Y2i -JM8i -Iinten2.grd -C"+colorFile1+" -K -E250 "+ region + " > " +filename;
       RunScript.runScript(command);

       //# this will be added later
       //# psxy   ${region} -JM8i -O -K  -W5/0/0/0 -: -Ms ca_hiwys.main.asc >> $filename

       command[2]="pscoast  "+region+" -JM8i -K -O -W1/17/73/71 -P -S17/73/71 -Di >> "+filename;
       RunScript.runScript(command);

       command[2]="gmtset BASEMAP_FRAME_RGB 255/255/255 DEGREE_FORMAT 4 FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
       RunScript.runScript(command);

       command[2]="psscale -B1:Log_Prob: -D4i/-0.5i/8i/0.3ih -C"+colorFile2+" -K -O -N200 >> " +filename;
       RunScript.runScript(command);

       command[2]="psbasemap -B1/1eWNs -JM8i "+region+" -Lfx1.25i/0.6i/33.0/100 -O >> "+filename;
       RunScript.runScript(command);

       command[2] ="cat "+ filename +" |gs -sDEVICE=jpeg -sOutputFile=temp.jpg -";
       RunScript.runScript(command);

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
  }
}