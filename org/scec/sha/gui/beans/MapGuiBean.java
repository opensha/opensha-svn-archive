package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.mapping.gmtWrapper.*;
import org.scec.sha.gui.controls.GMT_SettingsControlPanel;
import org.scec.param.*;

/**
 * <p>Title: GMT_MapGenerator</p>
 * <p>Description: This class takes the GMT parameters and generates the image file
 * from the GMT script. It returns that image file in Panel to the Applet
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class MapGuiBean extends JPanel {


  //instance of the GMT Control Panel to get the GMT parameters value.
  private GMT_SettingsControlPanel gmtMap;

  // PATH where the gmt commands and some others exist.
  private static String GMT_PATH = "/sw/bin/";

  // this is the path where general data (e.g., topography) are found:
  private static String SCEC_GMT_DATA_PATH = "/usr/scec/data/gmt/";


  // this is the path to find the "cat" command
  private static String COMMAND_PATH = "/bin/";

  //counter that keeps tracks of the outputfile jpg file generated
  //it is declared static so that all objects of this class share the same variable.
  private static int outputFilePrefixCounter=0;


  //Label to show the imageFile
  private JLabel gmtMapLabel = new JLabel();
  private Border border;
  private GridBagLayout gridBagLayout = new GridBagLayout();


  /**
   * Clas constructor accepts the GMT parameters list
   * @param gmtMap
   */
  public MapGuiBean(GMT_SettingsControlPanel gmtMap) {
    this.gmtMap=gmtMap;
  }


  /**
   * this function generates GMT map
   * It is a wrapper function around GMT tool
   * @param fileName: name of the XYZ file
   */
  public void makeMap(String fileName){


    ParameterList gmtParamList=gmtMap.getParameterList();


    String outputFilePrefix = gmtParamList.getParameter(gmtMap.OUTPUT_FILE_PREFIX_PARAM_NAME).getValue().toString();

    double minLat = ((Double)gmtParamList.getParameter(gmtMap.MIN_LAT_PARAM_NAME).getValue()).doubleValue();
    double maxLat = ((Double)gmtParamList.getParameter(gmtMap.MAX_LAT_PARAM_NAME).getValue()) .doubleValue();
    double minLon = ((Double)gmtParamList.getParameter(gmtMap.MIN_LON_PARAM_NAME).getValue()).doubleValue();
    double maxLon = ((Double)gmtParamList.getParameter(gmtMap.MIN_LAT_PARAM_NAME).getValue()).doubleValue();

    String cptFile = SCEC_GMT_DATA_PATH + gmtParamList.getParameter(gmtMap.CPT_FILE_PARAM_NAME).getValue().toString();

    String colorScaleMode = gmtParamList.getParameter(gmtMap.COLOR_SCALE_MODE_NAME).getValue().toString();

    // Set resolution according to the topoInten file chosen (options are 3, 6, 18, or 30):
    String resolution = gmtParamList.getParameter(gmtMap.TOPO_RESOLUTION_PARAM_NAME).getValue().toString();
    String topoIntenFile = SCEC_GMT_DATA_PATH + "calTopoInten" + resolution+".grd";

    // Set highways String
    String showHiwys = gmtParamList.getParameter(gmtMap.SHOW_HIWYS_PARAM_NAME).getValue().toString();

    String out_ps = outputFilePrefix + ".ps";
    String out_jpg = outputFilePrefix + outputFilePrefixCounter + ".jpg";

    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;

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
       String[] command ={"sh","-c",GMT_PATH+"xyz2grd "+ fileName+" -Gdata.grd -I0.05 "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
       RunScript.runScript(command);

//     xyz2grd LatLonAmpData.txt -GtestData.grd -I0.05 -R-121/-115/32.5/35.5 -D/degree/degree/amp/=/=/= -V -:

      command[2] =GMT_PATH + "grdcut data.grd -GtempData.grd " + region;
      RunScript.runScript(command);


      double colorScaleMin, colorScaleMax;
      if( colorScaleMode.equals(gmtMap.COLOR_SCALE_MODE_MANUALLY) ) {
        colorScaleMin = ((Double)gmtParamList.getParameter(gmtMap.COLOR_SCALE_MIN_PARAM_NAME).getValue()).doubleValue();
        colorScaleMax = ((Double) gmtParamList.getParameter(gmtMap.COLOR_SCALE_MAX_PARAM_NAME).getValue()).doubleValue();
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

      if( resolution.equals(gmtMap.TOPO_RESOLUTION_NONE) ) {
        command[2]=GMT_PATH+"grdimage tempData.grd -X0.75i " + yOff + " " + projWdth + " -Ctemp.cpt -K -E70 "+ region + " > " + out_ps;
        RunScript.runScript(command);
      }
      else {
        command[2]=GMT_PATH+"grdsample tempData.grd -GtempHiResData.grd -I" + resolution + "c -Q";
        RunScript.runScript(command);

        command[2]=GMT_PATH+"grdcut " + topoIntenFile + " -GtempInten.grd "+region;
        RunScript.runScript(command);

        command[2]=GMT_PATH+"grdimage tempHiResData.grd -X0.75i " + yOff + " " + projWdth + " -ItempInten.grd -Ctemp.cpt -K -E70 "+ region + " > " + out_ps;
        RunScript.runScript(command);
      }

      if ( !showHiwys.equals(gmtMap.SHOW_HIWYS_NONE) ) {
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
//       command[2] = GMT_PATH+"convert -crop "+ imageWdthPix + "x" + imageHghtPix + " " + out_ps + " " + out_jpg;
      command[2] = GMT_PATH+"convert " + out_ps + " " + out_jpg;
      RunScript.runScript(command);


//       command[2] = "/Applications/Preview.app/Contents/MacOS/Preview " + out_jpg + " &";
//       RunScript.runScript(command);

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }

    //adding the image to the Panel and returning that to the applet
    String imgName= out_jpg+outputFilePrefixCounter;
    gmtMapLabel.setBorder(border);
    gmtMapLabel.setMaximumSize(new Dimension(0, 800));
    gmtMapLabel.setMinimumSize(new Dimension(0, 600));
    gmtMapLabel.setPreferredSize(new Dimension(0, 600));
    this.setLayout(gridBagLayout);
    this.setMinimumSize(new Dimension(0, 800));
    this.setPreferredSize(new Dimension(0, 800));
    this.add(gmtMapLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 557, 200));
    gmtMapLabel.setIcon(new ImageIcon(imgName));

    // increment jpg file index
      ++outputFilePrefixCounter;
  }
}