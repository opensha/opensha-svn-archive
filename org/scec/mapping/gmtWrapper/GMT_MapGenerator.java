package org.scec.mapping.gmtWrapper;

import java.io.*;
import java.util.*;
import org.scec.param.*;

/**
 * <p>Title: GMT_MapGenerator</p>
 * <p>Description: This class generates Maps using the java wrapper around GMT</p>
 * @author: Ned Field, Nitin Gupta, & Vipin Gupta
 * @created:Dec 21,2002
 * @version 1.0
 */

public class GMT_MapGenerator {

  // PATH where the gmt commands and some others exist.
  private static String GMT_PATH = "/sw/bin/";

  // this is the path where general data (e.g., topography) are found:
  private static String SCEC_GMT_DATA_PATH = "/usr/scec/data/gmt/";


  // this is the path to find the "cat" command
  private static String COMMAND_PATH = "/bin/";

  // for map boundary parameters
  public final static String MIN_LAT_PARAM_NAME = "Min Latitude";
  public final static String MAX_LAT_PARAM_NAME = "Max Latitude";
  public final static String MIN_LON_PARAM_NAME = "Min Longitude";
  public final static String MAX_LON_PARAM_NAME = "Max Longitude";
  private final static String LAT_LON_PARAM_UNITS = "Degrees";
  private final static String LAT_LON_PARAM_INFO = "Corner point of mapped region";
  private final static Double MIN_LAT_PARAM_DEFAULT = new Double(32.5);
  private final static Double MAX_LAT_PARAM_DEFAULT = new Double(35.5);
  private final static Double MIN_LON_PARAM_DEFAULT = new Double(-121);
  private final static Double MAX_LON_PARAM_DEFAULT = new Double(-115);
  DoubleParameter minLatParam;
  DoubleParameter maxLatParam;
  DoubleParameter minLonParam;
  DoubleParameter maxLonParam;

  public final static String GRD_INPUT_FILE_PARAM_NAME = "Input GRD file name";
  private final static String GRD_INPUT_FILE_PARAM_DEFAULT = "testData.grd";
  private final static String GRD_INPUT_FILE_PARAM_INFO = "Name of data file to use for making the map.";
  private StringParameter grdInputFileParam;

  public final static String OUTPUT_FILE_PREFIX_PARAM_NAME = "Output file prefix";
  private final static String OUTPUT_FILE_PREFIX_PARAM_DEFAULT = "test";
  private final static String OUTPUT_FILE_PREFIX_PARAM_INFO = "Name of prefix for output files (.ps and .jpg will be added)";
  private StringParameter outputFilePrefixParam;

  public final static String CPT_FILE_PARAM_NAME = "Color Scheme";
  private final static String CPT_FILE_PARAM_DEFAULT = "MaxSpectrum.cpt";
  private final static String CPT_FILE_PARAM_INFO = "Color scheme for the scale";
  private final static String CPT_FILE_MAX_SPECTRUM = "MaxSpectrum.cpt";
  private final static String CPT_FILE_GERSTENBERGER = "Gerstenberger.cpt";
  private final static String CPT_FILE_SHAKEMAP = "Shakemap.cpt";
  StringParameter cptFileParam;

  // auto versus manual color scale setting
  public final static String COLOR_SCALE_MODE_NAME = "Color Scale Limits";
  public final static String COLOR_SCALE_MODE_INFO = "Set manually or from max/min of the data";
  public final static String COLOR_SCALE_MODE_MANUALLY = "Manually";
  public final static String COLOR_SCALE_MODE_FROMDATA = "From Data";
  public final static String COLOR_SCALE_MODE_DEFAULT = "From Data";
  StringParameter colorScaleModeParam;

  // for color scale limits
  public final static String COLOR_SCALE_MIN_PARAM_NAME = "Color-Scale Min";
  private final static Double COLOR_SCALE_MIN_PARAM_DEFAULT = new Double(-6);
  private final static String COLOR_SCALE_MIN_PARAM_INFO = "Lower limit on color scale (values below are the same color)";
  public final static String COLOR_SCALE_MAX_PARAM_NAME = "Color-Scale Max";
  private final static Double COLOR_SCALE_MAX_PARAM_DEFAULT = new Double(0);
  private final static String COLOR_SCALE_MAX_PARAM_INFO = "Upper limit on color scale (values above are the same color)";
  DoubleParameter colorScaleMaxParam;
  DoubleParameter colorScaleMinParam;

  // shaded relief resolution
  public final static String TOPO_RESOLUTION_PARAM_NAME = "Topo Resolution";
  private final static String TOPO_RESOLUTION_PARAM_UNITS = "arc-sec";
  private final static String TOPO_RESOLUTION_PARAM_DEFAULT = "18";
  private final static String TOPO_RESOLUTION_PARAM_INFO = "Resolution of the shaded relief";
  private final static String TOPO_RESOLUTION_03 = "03";
  private final static String TOPO_RESOLUTION_06 = "06";
  private final static String TOPO_RESOLUTION_18 = "18";
  private final static String TOPO_RESOLUTION_30 = "30";
  private final static String TOPO_RESOLUTION_NONE = "No Topo";
  StringParameter topoResolutionParam;

  // highways to plot parameter
  public final static String SHOW_HIWYS_PARAM_NAME = "Highways in plot";
  private final static String SHOW_HIWYS_PARAM_DEFAULT = "None";
  private final static String SHOW_HIWYS_PARAM_INFO = "Select the highways you'd like to be shown";
  private final static String SHOW_HIWYS_ALL = "ca_hiwys.all.xy";
  private final static String SHOW_HIWYS_MAIN = "ca_hiwys.main.xy";
  private final static String SHOW_HIWYS_OTHER = "ca_hiwys.other.xy";
  private final static String SHOW_HIWYS_NONE = "None";
  StringParameter showHiwysParam;


  protected ParameterList adjustableParams;

  public static int i = 0;


  public GMT_MapGenerator() {

    minLatParam = new DoubleParameter(MIN_LAT_PARAM_NAME,-90,90,LAT_LON_PARAM_UNITS,MIN_LAT_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    maxLatParam = new DoubleParameter(MAX_LAT_PARAM_NAME,-90,90,LAT_LON_PARAM_UNITS,MAX_LAT_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    minLonParam = new DoubleParameter(MIN_LON_PARAM_NAME,-360,360,LAT_LON_PARAM_UNITS,MIN_LON_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    maxLonParam = new DoubleParameter(MAX_LON_PARAM_NAME,-360,360,LAT_LON_PARAM_UNITS,MAX_LON_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);

    StringConstraint cptFileConstraint = new StringConstraint();
    cptFileConstraint.addString( CPT_FILE_MAX_SPECTRUM );
    cptFileConstraint.addString( CPT_FILE_GERSTENBERGER );
    cptFileConstraint.addString( CPT_FILE_SHAKEMAP );
    cptFileParam = new StringParameter( CPT_FILE_PARAM_NAME, cptFileConstraint, CPT_FILE_PARAM_DEFAULT );
    cptFileParam.setInfo( CPT_FILE_PARAM_INFO );

    StringConstraint colorScaleModeConstraint = new StringConstraint();
    colorScaleModeConstraint.addString( COLOR_SCALE_MODE_FROMDATA );
    colorScaleModeConstraint.addString( COLOR_SCALE_MODE_MANUALLY );
    colorScaleModeParam = new StringParameter( COLOR_SCALE_MODE_NAME, colorScaleModeConstraint, COLOR_SCALE_MODE_DEFAULT );
    colorScaleModeParam.setInfo( COLOR_SCALE_MODE_INFO );

    StringConstraint inputFileConstraint = new StringConstraint();
    inputFileConstraint.addString( GRD_INPUT_FILE_PARAM_DEFAULT );
    grdInputFileParam = new StringParameter( GRD_INPUT_FILE_PARAM_NAME, inputFileConstraint, GRD_INPUT_FILE_PARAM_DEFAULT );
    grdInputFileParam.setInfo( GRD_INPUT_FILE_PARAM_INFO );

    StringConstraint outputFilePrefixConstraint = new StringConstraint();
    outputFilePrefixConstraint.addString( OUTPUT_FILE_PREFIX_PARAM_DEFAULT );
    outputFilePrefixParam = new StringParameter( OUTPUT_FILE_PREFIX_PARAM_NAME, outputFilePrefixConstraint, OUTPUT_FILE_PREFIX_PARAM_DEFAULT );
    outputFilePrefixParam.setInfo( OUTPUT_FILE_PREFIX_PARAM_INFO );

    colorScaleMinParam = new DoubleParameter(COLOR_SCALE_MIN_PARAM_NAME, COLOR_SCALE_MIN_PARAM_DEFAULT);
    colorScaleMinParam.setInfo(COLOR_SCALE_MIN_PARAM_INFO);

    colorScaleMaxParam = new DoubleParameter(COLOR_SCALE_MAX_PARAM_NAME, COLOR_SCALE_MAX_PARAM_DEFAULT);
    colorScaleMaxParam.setInfo(COLOR_SCALE_MAX_PARAM_INFO);

    StringConstraint topoResolutionConstraint = new StringConstraint();
    topoResolutionConstraint.addString( TOPO_RESOLUTION_03 );
    topoResolutionConstraint.addString( TOPO_RESOLUTION_06 );
    topoResolutionConstraint.addString( TOPO_RESOLUTION_18 );
    topoResolutionConstraint.addString( TOPO_RESOLUTION_30 );
    topoResolutionConstraint.addString( TOPO_RESOLUTION_NONE );
    topoResolutionParam = new StringParameter( TOPO_RESOLUTION_PARAM_NAME, topoResolutionConstraint,TOPO_RESOLUTION_PARAM_UNITS, TOPO_RESOLUTION_PARAM_DEFAULT );
    topoResolutionParam.setInfo( TOPO_RESOLUTION_PARAM_INFO );


    StringConstraint showHiwysConstraint = new StringConstraint();
    showHiwysConstraint.addString( SHOW_HIWYS_NONE );
    showHiwysConstraint.addString( SHOW_HIWYS_ALL );
    showHiwysConstraint.addString( SHOW_HIWYS_MAIN );
    showHiwysConstraint.addString( SHOW_HIWYS_OTHER );
    showHiwysParam = new StringParameter( SHOW_HIWYS_PARAM_NAME, showHiwysConstraint, SHOW_HIWYS_PARAM_DEFAULT );
    showHiwysParam.setInfo( SHOW_HIWYS_PARAM_INFO );


    // create adjustable parameter list
    adjustableParams = new ParameterList();
    adjustableParams.addParameter(grdInputFileParam);
    adjustableParams.addParameter(outputFilePrefixParam);
    adjustableParams.addParameter(minLatParam);
    adjustableParams.addParameter(maxLatParam);
    adjustableParams.addParameter(minLonParam);
    adjustableParams.addParameter(maxLonParam);
    adjustableParams.addParameter(cptFileParam);
    adjustableParams.addParameter(colorScaleModeParam);
    adjustableParams.addParameter(colorScaleMinParam);
    adjustableParams.addParameter(colorScaleMaxParam);
    adjustableParams.addParameter(topoResolutionParam);
    adjustableParams.addParameter(showHiwysParam);

  }


  /**
   * main function to test this class
   *
   * @param args
   */
  public static void main(String[] args) {
    // to test this class, it should create a temp.jpg
    GMT_MapGenerator mapGen = new GMT_MapGenerator();
    mapGen.makeMap();
  }

  /**
   * this function generates GMT map
   * It is a wrapper function around GMT tool
   */
  public void makeMap(){

    String grdInputDataFileName = (String) grdInputFileParam.getValue();

    String outputFilePrefix = (String) outputFilePrefixParam.getValue();

    double minLat = ((Double) minLatParam.getValue()).doubleValue();
    double maxLat = ((Double) maxLatParam.getValue()).doubleValue();
    double minLon = ((Double) minLonParam.getValue()).doubleValue();
    double maxLon = ((Double) maxLonParam.getValue()).doubleValue();

    String cptFile = SCEC_GMT_DATA_PATH + (String) cptFileParam.getValue();

    String colorScaleMode = (String) colorScaleModeParam.getValue();

    // Set resolution according to the topoInten file chosen (options are 3, 6, 18, or 30):
    String resolution = (String) topoResolutionParam.getValue();
    String topoIntenFile = SCEC_GMT_DATA_PATH + "calTopoInten" + resolution+".grd";

    // Set highways String
    String showHiwys = (String) showHiwysParam.getValue();

    String out_ps = outputFilePrefix + ".ps";
    String out_jpg = outputFilePrefix + i + ".jpg";

    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;

    // plot size parameter
    double plotWdth = 6.5;
    String projWdth = "-JM"+plotWdth+"i";
    double plotHght = ((maxLat-minLat)/(maxLon-minLon))*plotWdth/Math.cos(Math.PI*(maxLat+minLat)/(2*180));

    int imageWdthPix = (int) (8.5*72);

    int imageHghtPix = (int) (( plotHght + 2.25 )*72);

    System.out.println("plot height = " + plotHght + " imageHightPix = "+ imageHghtPix);


    try {

       //command to be executed during the runtime.
//       String[] command ={"sh","-c",GMT_PATH+"xyz2grd LatLonAmpData.txt -Gdata.grd -I0.05 "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
//       RunScript.runScript(command);

//     xyz2grd LatLonAmpData.txt -GtestData.grd -I0.05 -R-121/-115/32.5/35.5 -D/degree/degree/amp/=/=/= -V -:

       String[] command ={"sh","-c",GMT_PATH + "grdcut " + grdInputDataFileName +" -GtempData.grd " + region};
       RunScript.runScript(command);


       double colorScaleMin, colorScaleMax;
       if( colorScaleMode.equals(COLOR_SCALE_MODE_MANUALLY) ) {
         colorScaleMin = ((Double) this.colorScaleMinParam.getValue()).doubleValue();
         colorScaleMax = ((Double) this.colorScaleMaxParam.getValue()).doubleValue();
       }
       else {
         GRD_InfoFromFile grdInfo = new GRD_InfoFromFile("tempData.grd");
         colorScaleMin = grdInfo.get_z_min();
         colorScaleMax = grdInfo.get_z_max();
       }

       float inc = (float) ((colorScaleMax-colorScaleMin)/20);
       command[2]=GMT_PATH+"makecpt -C" + cptFile + " -T" + colorScaleMin +"/"+ colorScaleMax +"/" + inc + " -Z > temp.cpt";
       RunScript.runScript(command);

       command[2]=GMT_PATH+"gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR 0/0/0 PAGE_ORIENTATION portrait";
       RunScript.runScript(command);

       if( resolution.equals(TOPO_RESOLUTION_NONE) ) {
         command[2]=GMT_PATH+"grdimage tempData.grd -X0.75i -Y2i " + projWdth + " -Ctemp.cpt -K -E70 "+ region + " > " + out_ps;
         RunScript.runScript(command);
       }
       else {
         command[2]=GMT_PATH+"grdsample tempData.grd -GtempHiResData.grd -I" + resolution + "c -Q";
         RunScript.runScript(command);

         command[2]=GMT_PATH+"grdcut " + topoIntenFile + " -GtempInten.grd "+region;
         RunScript.runScript(command);

         command[2]=GMT_PATH+"grdimage tempHiResData.grd -X0.75i -Y2i " + projWdth + " -ItempInten.grd -Ctemp.cpt -K -E70 "+ region + " > " + out_ps;
         RunScript.runScript(command);
       }

       if ( !showHiwys.equals(SHOW_HIWYS_NONE) ) {
         command[2]=GMT_PATH+"psxy  "+region+" " + projWdth + " -K -O -W5/125/125/125 -: -Ms " + SCEC_GMT_DATA_PATH + showHiwys + " >> " + out_ps;
         RunScript.runScript(command);
       }

       command[2]=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W1/17/73/71 -P -S17/73/71 -Di >> " + out_ps;
       RunScript.runScript(command);

       command[2]=GMT_PATH+"gmtset BASEMAP_FRAME_RGB 255/255/255 DEGREE_FORMAT 4 FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
       RunScript.runScript(command);

       command[2]=GMT_PATH+"psscale -B1:Log_Prob: -D3.5i/-0.5i/6i/0.3ih -Ctemp.cpt -K -O -N70 >> " + out_ps;
       RunScript.runScript(command);

       command[2]=GMT_PATH+"psbasemap -B1/1eWNs " + projWdth + " "+region+" -Lfx1.25i/0.6i/33.0/100 -O >> " + out_ps;
       RunScript.runScript(command);
/*
       command[2] =COMMAND_PATH+"cat "+ out_ps + " | "+GMT_PATH+"gs -sDEVICE=jpeg -sOutputFile=" + out_jpg + " -";
       RunScript.runScript(command);
*/
       command[2] = GMT_PATH+"convert "+ out_ps + " " + out_jpg;
       RunScript.runScript(command);

       // increment jpg file index
       ++i;


//       command[2] = "/Applications/Preview.app/Contents/MacOS/Preview " + out_jpg + " &";
//       RunScript.runScript(command);

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
  }

  /**
  * method to get the adjustable parameters
  */
  public ListIterator getAdjustableParamsList() {
    return adjustableParams.getParametersIterator();
  }
}