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

public class GMT_MapGenerator implements Serializable{

  /**
   * Name of the class
   */
  protected final static String C = "GMT_MapGenerator";

  // for debug purpose
  protected final static boolean D = true;

  // PATH where the gmt commands and some others exist.
  public static String GMT_PATH = "/sw/bin/";

  // this is the path where general data (e.g., topography) are found:
  private static String SCEC_GMT_DATA_PATH = "/usr/scec/data/gmt/";


  // this is the path to find the "cat" command
  private static String COMMAND_PATH = "/bin/";

  // for map boundary parameters
  public final static String MIN_LAT_PARAM_NAME = "Min Latitude";
  public final static String MAX_LAT_PARAM_NAME = "Max Latitude";
  public final static String MIN_LON_PARAM_NAME = "Min Longitude";
  public final static String MAX_LON_PARAM_NAME = "Max Longitude";
  public final static String GRID_SPACING_PARAM_NAME = "Grid Spacing";
  private final static String LAT_LON_PARAM_UNITS = "Degrees";
  private final static String LAT_LON_PARAM_INFO = "Corner point of mapped region";
  private final static String GRID_SPACING_PARAM_INFO = "Grid interval in the Region";
  private final static Double MIN_LAT_PARAM_DEFAULT = new Double(32.5);
  private final static Double MAX_LAT_PARAM_DEFAULT = new Double(35.5);
  private final static Double MIN_LON_PARAM_DEFAULT = new Double(-121);
  private final static Double MAX_LON_PARAM_DEFAULT = new Double(-115);
  private final static Double GRID_SPACING_PARAM_DEFAULT = new Double(.05);
  DoubleParameter minLatParam;
  DoubleParameter maxLatParam;
  DoubleParameter minLonParam;
  DoubleParameter maxLonParam;
  DoubleParameter gridSpacingParam;

  //output Image file Name
  private String out_jpg= new String();


  public final static String CPT_FILE_PARAM_NAME = "Color Scheme";
  private final static String CPT_FILE_PARAM_DEFAULT = "MaxSpectrum.cpt";
  private final static String CPT_FILE_PARAM_INFO = "Color scheme for the scale";
  private final static String CPT_FILE_MAX_SPECTRUM = "MaxSpectrum.cpt";
  private final static String CPT_FILE_GERSTENBERGER = "Gerstenberger.cpt";
  private final static String CPT_FILE_SHAKEMAP = "Shakemap.cpt";
  StringParameter cptFileParam;

  public final static String COAST_PARAM_NAME = "Coast";
  private final static String COAST_DRAW = "Draw Boundary";
  private final static String COAST_FILL = "Draw & Fill";
  private final static String COAST_NONE = "Draw Nothing";
  private final static String COAST_DEFAULT = COAST_FILL;
  private final static String COAST_PARAM_INFO = "Specifies how bodies of water are drawn";
  StringParameter coastParam;


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
  public final static String TOPO_RESOLUTION_NONE = "No Topo";
  StringParameter topoResolutionParam;

  // highways to plot parameter
  public final static String SHOW_HIWYS_PARAM_NAME = "Highways in plot";
  private final static String SHOW_HIWYS_PARAM_DEFAULT = "None";
  private final static String SHOW_HIWYS_PARAM_INFO = "Select the highways you'd like to be shown";
  private final static String SHOW_HIWYS_ALL = "ca_hiwys.all.xy";
  private final static String SHOW_HIWYS_MAIN = "ca_hiwys.main.xy";
  private final static String SHOW_HIWYS_OTHER = "ca_hiwys.other.xy";
  public final static String SHOW_HIWYS_NONE = "None";
  StringParameter showHiwysParam;

  //image counter
  private static int imageCounter=0;


  protected ParameterList adjustableParams;

  public GMT_MapGenerator() {

    minLatParam = new DoubleParameter(MIN_LAT_PARAM_NAME,-90,90,LAT_LON_PARAM_UNITS,MIN_LAT_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    maxLatParam = new DoubleParameter(MAX_LAT_PARAM_NAME,-90,90,LAT_LON_PARAM_UNITS,MAX_LAT_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    minLonParam = new DoubleParameter(MIN_LON_PARAM_NAME,-360,360,LAT_LON_PARAM_UNITS,MIN_LON_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    maxLonParam = new DoubleParameter(MAX_LON_PARAM_NAME,-360,360,LAT_LON_PARAM_UNITS,MAX_LON_PARAM_DEFAULT);
    minLatParam.setInfo(LAT_LON_PARAM_INFO);
    gridSpacingParam = new DoubleParameter(GRID_SPACING_PARAM_NAME,.01,100,LAT_LON_PARAM_UNITS,GRID_SPACING_PARAM_DEFAULT);
    minLatParam.setInfo(GRID_SPACING_PARAM_INFO);

    StringConstraint cptFileConstraint = new StringConstraint();
    cptFileConstraint.addString( CPT_FILE_MAX_SPECTRUM );
    cptFileConstraint.addString( CPT_FILE_GERSTENBERGER );
    cptFileConstraint.addString( CPT_FILE_SHAKEMAP );
    cptFileParam = new StringParameter( CPT_FILE_PARAM_NAME, cptFileConstraint, CPT_FILE_PARAM_DEFAULT );
    cptFileParam.setInfo( CPT_FILE_PARAM_INFO );

    StringConstraint coastConstraint = new StringConstraint();
    coastConstraint.addString(COAST_FILL);
    coastConstraint.addString(COAST_DRAW);
    coastConstraint.addString(COAST_NONE);
    coastParam = new StringParameter(COAST_PARAM_NAME,coastConstraint,COAST_DEFAULT );
    coastParam.setInfo(COAST_PARAM_INFO);

    StringConstraint colorScaleModeConstraint = new StringConstraint();
    colorScaleModeConstraint.addString( COLOR_SCALE_MODE_FROMDATA );
    colorScaleModeConstraint.addString( COLOR_SCALE_MODE_MANUALLY );
    colorScaleModeParam = new StringParameter( COLOR_SCALE_MODE_NAME, colorScaleModeConstraint, COLOR_SCALE_MODE_DEFAULT );
    colorScaleModeParam.setInfo( COLOR_SCALE_MODE_INFO );



    StringConstraint outputFilePrefixConstraint = new StringConstraint();

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

    adjustableParams.addParameter(minLatParam);
    adjustableParams.addParameter(maxLatParam);
    adjustableParams.addParameter(minLonParam);
    adjustableParams.addParameter(maxLonParam);
    adjustableParams.addParameter(gridSpacingParam);
    adjustableParams.addParameter(cptFileParam);
    adjustableParams.addParameter(colorScaleModeParam);
    adjustableParams.addParameter(colorScaleMinParam);
    adjustableParams.addParameter(colorScaleMaxParam);
    adjustableParams.addParameter(topoResolutionParam);
    adjustableParams.addParameter(showHiwysParam);
    adjustableParams.addParameter(coastParam);

  }




  /**
   * this function generates GMT map
   * It is a wrapper function around GMT tool
   * It acccepts the name of xyz file
   */
  public String makeMap(String xyzFileName){


    double minLat = ((Double) minLatParam.getValue()).doubleValue();
    double maxLat = ((Double) maxLatParam.getValue()).doubleValue();
    double minLon = ((Double) minLonParam.getValue()).doubleValue();
    double maxLon = ((Double) maxLonParam.getValue()).doubleValue();
    double gridSpacing = ((Double) gridSpacingParam.getValue()).doubleValue();
    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;

    if(D) System.out.println(C+" region = "+region);
    //all the files of the GMT will be created by this fileName
    String fileName=xyzFileName.substring(0,xyzFileName.indexOf("."));

    String grdFileName  = fileName+".grd";

    String cptFile = SCEC_GMT_DATA_PATH + (String) cptFileParam.getValue();

    String colorScaleMode = (String) colorScaleModeParam.getValue();
    String coast = (String) coastParam.getValue();

    // Set resolution according to the topoInten file chosen (options are 3, 6, 18, or 30):
    String resolution = (String) topoResolutionParam.getValue();
    String topoIntenFile = SCEC_GMT_DATA_PATH + "calTopoInten" + resolution+".grd";

    // Set highways String
    String showHiwys = (String) showHiwysParam.getValue();

    String out_ps = fileName + ".ps";
    out_jpg = fileName+"-"+imageCounter+ ".jpg";


    // plot size parameter
    double plotWdth = 6.5;
    String projWdth = "-JM"+plotWdth+"i";
    double plotHght = ((maxLat-minLat)/(maxLon-minLon))*plotWdth/Math.cos(Math.PI*(maxLat+minLat)/(2*180));

    double yOffset = 11 - plotHght - 0.5;
    String yOff = "-Y" + yOffset + "i";
//    int imageWdthPix = (int) (8.5*72);
//    int imageHghtPix = (int) (( plotHght + 2.5 )*72);
//    System.out.println("plot height = " + plotHght + " imageHightPix = "+ imageHghtPix);


    try {

       //command to be executed during the runtime.
//       String[] command ={"sh","-c",GMT_PATH+"xyz2grd LatLonAmpData.txt -Gdata.grd -I0.05 "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
//       RunScript.runScript(command);

//     xyz2grd LatLonAmpData.txt -GtestData.grd -I0.05 -R-121/-115/32.5/35.5 -D/degree/degree/amp/=/=/= -V -:

      //command to be executed during the runtime.
      String[] command ={"sh","-c",GMT_PATH+"xyz2grd "+ xyzFileName+" -G"+ grdFileName+ " -I"+gridSpacing+" "+ region +" -D/degree/degree/amp/=/=/=  -:"};
      RunScript.runScript(command);

       command[2] = GMT_PATH + "grdcut " + grdFileName +" -Gtemp"+grdFileName +" " + region;
       RunScript.runScript(command);


       double colorScaleMin, colorScaleMax;
       if( colorScaleMode.equals(COLOR_SCALE_MODE_MANUALLY) ) {
         colorScaleMin = ((Double) this.colorScaleMinParam.getValue()).doubleValue();
         colorScaleMax = ((Double) this.colorScaleMaxParam.getValue()).doubleValue();
       }
       else {
         GRD_InfoFromFile grdInfo = new GRD_InfoFromFile("temp"+grdFileName);
         colorScaleMin = grdInfo.get_z_min();
         colorScaleMax = grdInfo.get_z_max();
       }

       float inc = (float) ((colorScaleMax-colorScaleMin)/20);
       command[2]=GMT_PATH+"makecpt -C" + cptFile + " -T" + colorScaleMin +"/"+ colorScaleMax +"/" + inc + " -Z > "+fileName+".cpt";
       RunScript.runScript(command);

       command[2]=GMT_PATH+"gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR 0/0/0 PAGE_ORIENTATION portrait";
       RunScript.runScript(command);

       if( resolution.equals(TOPO_RESOLUTION_NONE) ) {
         command[2]=GMT_PATH+"grdimage temp"+grdFileName+" -X0.75i " + yOff + " " + projWdth + " -C"+fileName+".cpt -K -E70 "+ region + " > " + out_ps;
         RunScript.runScript(command);
       }
       else {
         command[2]=GMT_PATH+"grdsample temp"+grdFileName+" -G"+fileName+"HiResData.grd -I" + resolution + "c -Q";
         RunScript.runScript(command);

         command[2]=GMT_PATH+"grdcut " + topoIntenFile + " -G"+fileName+"Inten.grd "+region;
         RunScript.runScript(command);

         command[2]=GMT_PATH+"grdimage "+fileName+"HiResData.grd -X0.75i " + yOff + " " + projWdth + " -I"+fileName+"Inten.grd -C"+fileName+".cpt -K -E70 "+ region + " > " + out_ps;
         RunScript.runScript(command);
       }

       if ( !showHiwys.equals(SHOW_HIWYS_NONE) ) {
         command[2]=GMT_PATH+"psxy  "+region+" " + projWdth + " -K -O -W5/125/125/125 -: -Ms " + SCEC_GMT_DATA_PATH + showHiwys + " >> " + out_ps;
         RunScript.runScript(command);
       }

       if(coast.equals(COAST_FILL)) {
         command[2]=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W1/17/73/71 -P -S17/73/71 -Dh >> " + out_ps;
         RunScript.runScript(command);
       }
       else if(coast.equals(COAST_DRAW)) {
         command[2]=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W4/0/0/0 -P -Dh >> " + out_ps;
         RunScript.runScript(command);
       }

       command[2]=GMT_PATH+"gmtset BASEMAP_FRAME_RGB 255/255/255 DEGREE_FORMAT 4 FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
       RunScript.runScript(command);

       command[2]=GMT_PATH+"psscale -B1:LogIML: -D3.5i/-0.5i/6i/0.3ih -C"+fileName+".cpt -K -O -N70 >> " + out_ps;
       RunScript.runScript(command);

       command[2]=GMT_PATH+"psbasemap -B1/1eWNs " + projWdth + " "+region+" -Lfx1.25i/0.6i/33.0/50 -O >> " + out_ps;
       RunScript.runScript(command);
/*
       command[2] =COMMAND_PATH+"cat "+ out_ps + " | "+GMT_PATH+"gs -sDEVICE=jpeg -sOutputFile=" + out_jpg + " -";
       RunScript.runScript(command);
*/
//       command[2] = GMT_PATH+"convert -crop "+ imageWdthPix + "x" + imageHghtPix + " " + out_ps + " " + out_jpg;
       command[2] = GMT_PATH+"convert " + out_ps + " " + out_jpg;
       RunScript.runScript(command);

//       command[2] = "/Applications/Preview.app/Contents/MacOS/Preview " + out_jpg + " &";
//       RunScript.runScript(command);

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
    ++imageCounter;
     return out_jpg;
  }

  /**
  * method to get the adjustable parameters
  */
  public ListIterator getAdjustableParamsIterator() {
    return adjustableParams.getParametersIterator();
  }


  /**
   *
   * @returns the GMT Params List
   */
  public ParameterList getAdjustableParamsList(){
    return adjustableParams;
  }

  /**
   *
   * @returns the image file name
   */
  public String getImageFileName(){
    return this.out_jpg;
  }
}