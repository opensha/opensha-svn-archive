package org.scec.mapping.gmtWrapper;

import java.io.*;
import java.util.*;
import javax.activation.*;
import java.text.DecimalFormat;

import org.scec.param.*;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.webservices.client.*;
import org.scec.util.RunScript;

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

  // name of the file which contains all the GMT commands that we want to run on server
  private String GMT_FILE_NAME = "gmtScript.txt";

  private String XYZ_FILE_NAME ="xyz.txt";

  // PATH where the gmt commands and some others exist.
  public static String gmtPath = null;

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
  private final static Double MAX_LAT_PARAM_DEFAULT = new Double(36.6);
  private final static Double MIN_LON_PARAM_DEFAULT = new Double(-121.5);
  private final static Double MAX_LON_PARAM_DEFAULT = new Double(-115.0);
  private final static Double GRID_SPACING_PARAM_DEFAULT = new Double(.1);
  DoubleParameter minLatParam;
  DoubleParameter maxLatParam;
  DoubleParameter minLonParam;
  DoubleParameter maxLonParam;
  DoubleParameter gridSpacingParam;

  //output Image file Name
  private String out_jpg= new String();
  private String out_ps;


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
  private final static Double COLOR_SCALE_MIN_PARAM_DEFAULT = new Double(-1.7);
  private final static String COLOR_SCALE_MIN_PARAM_INFO = "Lower limit on color scale (values below are the same color)";
  public final static String COLOR_SCALE_MAX_PARAM_NAME = "Color-Scale Max";
  private final static Double COLOR_SCALE_MAX_PARAM_DEFAULT = new Double(-0.1);
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

  private String gmtFileName;

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
    gridSpacingParam = new DoubleParameter(GRID_SPACING_PARAM_NAME,.001,100,LAT_LON_PARAM_UNITS,GRID_SPACING_PARAM_DEFAULT);
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
   * It acccepts the xyz dataset
   */
  public String makeMap(XYZ_DataSetAPI xyzDataSet){
    String GMT_PATH="/sw/bin/";
    Vector xVals = xyzDataSet.getX_DataSet();
    Vector yVals = xyzDataSet.getY_DataSet();
    Vector zVals = xyzDataSet.getZ_DataSet();

    FileWriter fw =null;
    BufferedWriter br=null;

    //creating the XYZ file from the XYZ dataSet
    try{
      //file follows the convention lat, lon and Z value
      if(xyzDataSet.checkXYZ_NumVals()){
        int size = xVals.size();
        fw = new FileWriter(this.XYZ_FILE_NAME);
        br = new BufferedWriter(fw);
        for(int i=0;i<size;++i)
          br.write(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
        br.close();
      }
      else
        throw new RuntimeException("X, Y and Z dataset does not have equal size");
    }catch(Exception e){
      e.printStackTrace();
    }

    //writing the GMT script into the GMT Script file
    try {
      gmtFileName=GMT_FILE_NAME.substring(0,GMT_FILE_NAME.indexOf("."))+System.currentTimeMillis()+".txt";
      fw = new FileWriter(gmtFileName);
      br = new BufferedWriter(fw);
      String fileName=this.XYZ_FILE_NAME.substring(0,XYZ_FILE_NAME.indexOf("."));
      out_ps = fileName + ".ps";
      out_jpg = fileName+"-"+imageCounter+ ".jpg";

      this.runMapScript(GMT_PATH,br,xyzDataSet);
      String gmtCommandLine = GMT_PATH+"convert " + out_ps + " " + out_jpg;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
      br.close();
    }catch(RuntimeException ee){
      throw new RuntimeException(ee.getMessage());
    }catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }

    //running the GMT script from the file
    String[] command ={"sh","-c","sh "+gmtFileName};
    RunScript.runScript(command);
    ++imageCounter;
    return out_jpg;
  }



  /**
   * this function generates GMT map using the GMT from the SCEC server
   * It is a wrapper function around GMT tool
   * It acccepts the xyz dataset
   */
  public String makeMapUsingServer(XYZ_DataSetAPI xyzDataSet){

    String GMT_PATH="/usr/scec/share/graphics/GMT3.3.6/bin/";
    FileWriter fw = null;
    BufferedWriter br =null;
    Vector xVals = xyzDataSet.getX_DataSet();
    Vector yVals = xyzDataSet.getY_DataSet();
    Vector zVals = xyzDataSet.getZ_DataSet();

    //creating the XYZ file from the XYZ dataSet
    try{
      //file follows the convention lon,lat and Z value
      if(xyzDataSet.checkXYZ_NumVals()){
        int size = yVals.size();
        fw = new FileWriter(this.XYZ_FILE_NAME);
        br = new BufferedWriter(fw);
        for(int i=0;i<size;++i)
          br.write(yVals.get(i)+" "+xVals.get(i)+" "+zVals.get(i)+"\n");
        br.close();
      }
      else
        throw new RuntimeException("X, Y and Z dataset does not have equal size");
    }catch(Exception e){
      e.printStackTrace();
    }

    //writing the GMT commands to the file
    try{
      gmtFileName = GMT_FILE_NAME.substring(0,GMT_FILE_NAME.indexOf("."))+System.currentTimeMillis()+".txt";
      fw = new FileWriter(gmtFileName);
      br = new BufferedWriter(fw);
      String fileName=XYZ_FILE_NAME.substring(0,XYZ_FILE_NAME.indexOf("."));
      out_ps = fileName + ".ps";
      out_jpg = fileName+"-"+imageCounter+ ".jpg";

      runMapScript(GMT_PATH,br,xyzDataSet);
      String gmtCommandLine=COMMAND_PATH+"cat "+ out_ps + " | gs -sDEVICE=jpeg -sOutputFile=" + out_jpg + " -";
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
      br.close();
    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }

    //running the GMT script from the file
    String[] command ={"sh","-c","sh "+gmtFileName};
    RunScript.runScript(command);
    ++imageCounter;
    return out_jpg;
  }


  /**
   * this function generates GMT map using the GMT from the gravity server
   * It is a wrapper function around GMT tool
   * It acccepts the xyz dataset
   */
  public String makeMapUsingWebServer(XYZ_DataSetAPI xyzDataSet){

    String GMT_PATH="/opt/install/gmt/bin/";
    FileWriter fw = null;
    BufferedWriter br =null;
    Vector xVals = xyzDataSet.getX_DataSet();
    Vector yVals = xyzDataSet.getY_DataSet();
    Vector zVals = xyzDataSet.getZ_DataSet();

    //creating the XYZ file from the XYZ dataSet
    try{
      //file follows the convention lat, lon and Z value
      if(xyzDataSet.checkXYZ_NumVals()){
        int size = xVals.size();
        fw = new FileWriter(XYZ_FILE_NAME);
        br = new BufferedWriter(fw);
        for(int i=0;i<size;++i)
          br.write(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
        br.close();
      }
      else
        throw new RuntimeException("X, Y and Z dataset does not have equal size");
    }catch(Exception e){
      e.printStackTrace();
    }

    //writing the GMT commands to the file
    try{
      gmtFileName = GMT_FILE_NAME.substring(0,GMT_FILE_NAME.indexOf("."))+System.currentTimeMillis()+".txt";
      fw = new FileWriter(gmtFileName);
      br = new BufferedWriter(fw);
      String fileName=XYZ_FILE_NAME.substring(0,XYZ_FILE_NAME.indexOf("."));
      out_ps = fileName + ".ps";
      out_jpg = fileName + ".jpg";

      runMapScript(GMT_PATH,br,xyzDataSet);
      String gmtCommandLine=COMMAND_PATH+"cat "+ out_ps + " | gs -sDEVICE=jpeg -sOutputFile=" + out_jpg + " -";
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
      br.close();
    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
    //putting files in String array which are to be sent to the server as the attachment
    String[] fileNames = new String[2];
    //getting the GMT script file name
    fileNames[0] = getGMT_FileName();
    //getting the XYZ file Name
    fileNames[1] = getXYZ_FileName();
    String webAddr = this.openWebServiceConnection(fileNames);
    return webAddr+out_jpg;
  }

  /**
   * This function serves as a common interface for the running GMT on standalone or
   * on the SCEC server.
   * @param GMT_PATH
   * @param xyzFileName
   * @param command= command to run on the command-prompt
   */
  private void runMapScript(String GMT_PATH,BufferedWriter br,
                            XYZ_DataSetAPI xyzData) throws Exception{

    double minLat = ((Double) minLatParam.getValue()).doubleValue();
    double maxTempLat = ((Double) maxLatParam.getValue()).doubleValue();
    double minLon = ((Double) minLonParam.getValue()).doubleValue();
    double maxTempLon = ((Double) maxLonParam.getValue()).doubleValue();
    double gridSpacing = ((Double) gridSpacingParam.getValue()).doubleValue();

    // adjust the max lat and lon to be an exact increment (needed for xyz2grd)

    double maxLat = Math.rint(((maxTempLat-minLat)/gridSpacing))*gridSpacing +minLat;
    double maxLon = Math.rint(((maxTempLon-minLon)/gridSpacing))*gridSpacing +minLon;

    this.gmtPath= GMT_PATH;
    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;

    String gmtCommandLine =null;
    if(D) System.out.println(C+" region = "+region);
    //all the files of the GMT will be created by this fileName
    String fileName=XYZ_FILE_NAME.substring(0,XYZ_FILE_NAME.lastIndexOf("."));
    String grdFileName  = fileName+".grd";
    String cptFile = SCEC_GMT_DATA_PATH + (String) cptFileParam.getValue();

    String colorScaleMode = (String) colorScaleModeParam.getValue();
    String coast = (String) coastParam.getValue();

    // Set resolution according to the topoInten file chosen (options are 3, 6, 18, or 30):
    String resolution = (String) topoResolutionParam.getValue();
    String topoIntenFile = SCEC_GMT_DATA_PATH + "calTopoInten" + resolution+".grd";

    // Set highways String
    String showHiwys = (String) showHiwysParam.getValue();

    // plot size parameter
    double plotWdth = 6.5;
    String projWdth = "-JM"+plotWdth+"i";
    double plotHght = ((maxLat-minLat)/(maxLon-minLon))*plotWdth/Math.cos(Math.PI*(maxLat+minLat)/(2*180));

    double yOffset = 11 - plotHght - 0.5;
    String yOff = "-Y" + yOffset + "i";

    //command to be executed during the runtime.
    gmtCommandLine =GMT_PATH+"xyz2grd "+ XYZ_FILE_NAME+" -G"+ grdFileName+ " -I"+gridSpacing+" "+ region +" -D/degree/degree/amp/=/=/=  -: -H0";
    //RunScript.runScript(command);
    br.write(gmtCommandLine+"\n");

    //gmtCommandLine = GMT_PATH + "grdcut " + grdFileName +" -Gtemp"+grdFileName +" " + region;
    //RunScript.runScript(command);

    double colorScaleMin, colorScaleMax;
    if( colorScaleMode.equals(COLOR_SCALE_MODE_MANUALLY) ) {
      colorScaleMin = ((Double) this.colorScaleMinParam.getValue()).doubleValue();
      colorScaleMax = ((Double) this.colorScaleMaxParam.getValue()).doubleValue();
    }
    else {
      /*GRD_InfoFromFile grdInfo = new GRD_InfoFromFile("temp"+grdFileName, this.getGMT_PATH());
      colorScaleMin = grdInfo.get_z_min();
      colorScaleMax = grdInfo.get_z_max();*/

      colorScaleMin = xyzData.getMinZ();
      colorScaleMax = xyzData.getMaxZ();
    }

    float inc = (float) ((colorScaleMax-colorScaleMin)/20);
    gmtCommandLine=GMT_PATH+"makecpt -C" + cptFile + " -T" + colorScaleMin +"/"+ colorScaleMax +"/" + inc + " -Z > "+fileName+".cpt";
    //RunScript.runScript(command);
    br.write(gmtCommandLine+"\n");
    gmtCommandLine = GMT_PATH+"gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR 0/0/0 PAGE_ORIENTATION portrait";
    //RunScript.runScript(command);
    br.write(gmtCommandLine+"\n");
    if( resolution.equals(TOPO_RESOLUTION_NONE) ) {
      gmtCommandLine=GMT_PATH+"grdimage "+grdFileName+" -X0.75i " + yOff + " " + projWdth + " -C"+fileName+".cpt -K -E70 "+ region + " > " + out_ps;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
    }
    else {
      gmtCommandLine=GMT_PATH+"grdsample "+grdFileName+" -G"+fileName+"HiResData.grd -I" + resolution + "c -Q";
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
      gmtCommandLine=GMT_PATH+"grdcut " + topoIntenFile + " -G"+fileName+"Inten.grd "+region;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
      gmtCommandLine=GMT_PATH+"grdimage "+fileName+"HiResData.grd -X0.75i " + yOff + " " + projWdth + " -I"+fileName+"Inten.grd -C"+fileName+".cpt -K -E70 "+ region + " > " + out_ps;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
    }

    if ( !showHiwys.equals(SHOW_HIWYS_NONE) ) {
      gmtCommandLine=GMT_PATH+"psxy  "+region+" " + projWdth + " -K -O -W5/125/125/125 -: -Ms " + SCEC_GMT_DATA_PATH + showHiwys + " >> " + out_ps;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
    }

    if(coast.equals(COAST_FILL)) {
      gmtCommandLine=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W1/17/73/71 -P -S17/73/71 -Dh >> " + out_ps;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
    }
    else if(coast.equals(COAST_DRAW)) {
      gmtCommandLine=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W4/0/0/0 -P -Dh >> " + out_ps;
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
    }

    gmtCommandLine=GMT_PATH+"gmtset BASEMAP_FRAME_RGB 255/255/255 DEGREE_FORMAT 5 FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
    //RunScript.runScript(command);
    br.write(gmtCommandLine+"\n");

    DecimalFormat df2 = new DecimalFormat("0.E0");
    Float tickInc = new Float(df2.format((colorScaleMax-colorScaleMin)/4.0));
    inc = tickInc.floatValue();
    gmtCommandLine=GMT_PATH+"psscale -Ba"+inc+":IML: -D3.5i/-0.5i/6i/0.3ih -C"+fileName+".cpt -K -O -N70 >> " + out_ps;
    //RunScript.runScript(command);
    br.write(gmtCommandLine+"\n");

    gmtCommandLine=GMT_PATH+"psbasemap -B1/1eWNs " + projWdth + " "+region+" -Lfx1.25i/0.6i/33.0/50 -O >> " + out_ps;
    //RunScript.runScript(command);
    br.write(gmtCommandLine+"\n");
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

  /**
   *
   * @returns the GMT path
   */
  public String getGMT_PATH(){
    return this.gmtPath;
  }


  /**
   *
   * @returns the name of the GMT script file
   */
  public String getGMT_FileName(){
    return this.gmtFileName;
  }

  /**
   *
   * @returns the name of the XYZ file
   */
  public String getXYZ_FileName(){
    return this.XYZ_FILE_NAME;
  }

  //For the webservices Implementation
  private String openWebServiceConnection(String[] fileName){
    int size=fileName.length;
    String imgWebAddr=null;
    FileDataSource[] fs = new FileDataSource[size+2];
    DataHandler dh[] = new DataHandler[size+2];
    System.out.println("File-0: "+fileName[0]);
    fs[0] =new FileDataSource(fileName[0]);
    dh[0] = new DataHandler(fs[0]);

    System.out.println("File-1: "+fileName[1]);
    fs[1] =new FileDataSource(fileName[1]);
    dh[1] = new DataHandler(fs[1]);

    GMT_WebService_Impl client = new GMT_WebService_Impl();
    GMT_WebServiceAPI gmt = client.getGMT_WebServiceAPIPort();
    try{
      imgWebAddr = gmt.runGMT_Script(fileName,dh);
      System.out.println("imgWebAddr: "+imgWebAddr);
    }catch(Exception e){
      e.printStackTrace();
    }
    return imgWebAddr;
  }


  /**
   * This function is used to make the map from XYZ file.
   * This function is called from CME framework. This function was needed because
   * in CME, there is need that we should be able to specify the name of ps file name
   * and jpeg filename.
   *
   * @param xyzFileName name of the xyz file for which map will be generated
   * @param psFileName  ps file name
   * @param jpgFileName jpeg file name
   * @return
   */
  public void makeMapForCME(String xyzFileName, String psFileName, String jpgFileName) {
    String GMT_PATH="/opt/install/gmt/bin/";
    this.XYZ_FILE_NAME = xyzFileName;

    // contruct the xyz datatset using the xyzfilename
    Vector xVals = new Vector();
    Vector yVals =  new Vector();
    Vector zVals =  new Vector();
    try {
      FileReader fr = new FileReader(xyzFileName); //open the xyx file
      BufferedReader bf = new BufferedReader(fr);
      String str=bf.readLine();
      StringTokenizer tokenizer;
      while(str!=null) {
        tokenizer = new StringTokenizer(str);
        xVals.add(new Double(tokenizer.nextToken())); // lat
        yVals.add(new Double(tokenizer.nextToken()));  // lon
        zVals.add(new Double(tokenizer.nextToken()));  // z value
        str = bf.readLine();
      }
      bf.close();
    }catch(Exception e) { e.printStackTrace(); }

    org.scec.data.ArbDiscretizedXYZ_DataSet xyzDataSet =
        new org.scec.data.ArbDiscretizedXYZ_DataSet(xVals, yVals, zVals) ;

    FileWriter fw =null;
    BufferedWriter br=null;

    //writing the GMT script into the GMT Script file
    try {
      gmtFileName=xyzFileName.substring(0,xyzFileName.lastIndexOf("."))+System.currentTimeMillis()+".txt";
      fw = new FileWriter(gmtFileName);
      br = new BufferedWriter(fw);
      out_ps = psFileName;
      out_jpg = jpgFileName;
      this.runMapScript(GMT_PATH,br,xyzDataSet);
      String gmtCommandLine = COMMAND_PATH+"cat "+ out_ps + " | gs -sDEVICE=jpeg -sOutputFile=" + out_jpg + " -";
      //RunScript.runScript(command);
      br.write(gmtCommandLine+"\n");
      br.close();
    }catch(RuntimeException ee){
      throw new RuntimeException(ee.getMessage());
    }catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
    //running the GMT script from the file
    String[] command ={"sh","-c","sh "+gmtFileName};
    RunScript.runScript(command);
  }


  /**
   * Set the parameter values. This function is needed if someone is
   * not using MapGuiBean but wants to generate maps using GMT_MapGenerator
   *
   * @param paramName Parameter name whose value needs to be set
   * @param value Value to be assigned to that parameter
   */
  public void setParameter(String paramName, Object value) {
    this.adjustableParams.getParameter(paramName).setValue(value);
  }
}

