package org.scec.mapping.gmtWrapper;

import java.io.*;
import java.util.*;
import org.scec.param.*;

/**
 * <p>Title: GRD_InfoFromFile </p>
 * <p>Description: This class generates Maps using the java wrapper around GMT</p>
 * @author: Ned Field, Nitin Gupta, & Vipin Gupta
 * @created:Dec 21,2002
 * @version 1.0
 */

public class GRD_InfoFromFile {

  private boolean D = true;

  // PATH where the gmt commands and some others exist.
  private static String GMT_PATH = "/sw/bin/";

  // this is the path to find the "cat" command
  private static String COMMAND_PATH = "/bin/";

  private String filename;


  // to be set from line 6 of the file output from grdinfo
  private double x_min = Double.NaN;
  private double x_max = Double.NaN;
  private double x_inc = Double.NaN;  // the discretization interval (increment)
  private String x_units = null;
  private int nx = 0;

  // to be set from line 7 of the file output from grdinfo
  private double y_min = Double.NaN;
  private double y_max = Double.NaN;
  private double y_inc = Double.NaN;  // the discretization interval (increment)
  private String y_units = null;
  private int ny = 0;

  // to be set from line 8 of the file output from grdinfo
  private double z_min = Double.NaN;
  private double z_max = Double.NaN;
  private String z_units = null;

  // here are the getter methods:
  private double get_x_min() { return x_min; }
  private double get_x_max() { return x_max; }
  private double get_x_inc() { return x_inc; }
  private String get_x_units() { return x_units; }
  private int get_nx() { return nx; }

  private double get_y_min() { return y_min; }
  private double get_y_max() { return y_max; }
  private double get_y_inc() { return y_inc; }
  private String get_y_units() { return y_units; }
  private int get_ny() { return ny; }

  private double get_z_min() { return z_min; }
  private double get_z_max() { return z_max; }
  private String get_z_units() { return z_units; }


  /**
   * empty constructor
   */
  public GRD_InfoFromFile() {}

  /**
   * non-empty constructor
   */
  public GRD_InfoFromFile(String filename) { setFilename(filename); }


  public void setFilename(String filename) {

    this.filename = filename;
    String tempFileName = "temp_" + filename + "_info";
    String[] command ={"sh","-c",GMT_PATH + "grdinfo " + filename + " > " + tempFileName};
    RunScript.runScript(command);

    /* What if multiple instances of this object are working doing this simultaneously with
    the same file; will there be any potential conflicts */

    // Now we have to read that file, put it into a string, and parse each line to set the
    // following info parameters

    // set from line 6 of output file
    x_min = 1;
    x_max = 1;
    x_inc = 1;  // the discretization interval (increment)
    x_units = "testX";
    nx = 1;

    if (D) System.out.println(x_min + "  " + x_max + "  " + x_inc + "  " + x_units + "  " + nx);

    // set from line 7 of the output file
    y_min = 2;
    y_max = 2;
    y_inc = 2;  // the discretization interval (increment)
    y_units = "testY";
    ny = 2;

    if (D) System.out.println(y_min + "  " + y_max + "  " + y_inc + "  " + y_units + "  " + ny);

    // set from line 8 of the output file
    z_min = 3;
    z_max = 3;
    z_units = "testZ";

    if (D) System.out.println(z_min + "  " + z_max + "  " +  z_units);

  }


  /**
   * main function to test this class
   *
   * @param args
   */
  public static void main(String[] args) {
    // to test this class, it should create a temp.jpg
    GRD_InfoFromFile grdInfo = new GRD_InfoFromFile();
    grdInfo.setFilename("testData.grd");
  }


}