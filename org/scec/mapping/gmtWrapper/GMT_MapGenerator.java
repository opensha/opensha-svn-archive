package org.scec.mapping.gmtWrapper;

import java.io.*;
import java.util.*;
import javax.activation.*;
import java.text.DecimalFormat;
import java.net.*;
import java.io.*;

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
  private String GMT_SCRIPT_NAME = "gmtScript.txt";
  private String DEFAULT_XYZ_FILE_NAME = "xyz_data.txt";
  private String XYZ_FILE_NAME = DEFAULT_XYZ_FILE_NAME;
  private String PS_FILE_NAME = "map.ps";
  private String JPG_FILE_NAME = "map.jpg";
  private String GMT_PATH;

  XYZ_DataSetAPI xyzDataSet;

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
  private final static Double COLOR_SCALE_MIN_PARAM_DEFAULT = new Double(-2.2);
  private final static String COLOR_SCALE_MIN_PARAM_INFO = "Lower limit on color scale (values below are the same color)";
  public final static String COLOR_SCALE_MAX_PARAM_NAME = "Color-Scale Max";
  private final static Double COLOR_SCALE_MAX_PARAM_DEFAULT = new Double(-1);
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

  private String gmtFileName;

  protected ParameterList adjustableParams;

  //GMT files web address(if the person is using the gmt webService)
  String imgWebAddr=null;

  FileWriter fw =null;
  BufferedWriter br=null;



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
   * this function generates a GMT map from an XYZ data set using the current
   * parameter settings, and using the version of GMT on the local computer.
   *
   * This returns the name of the jpg file
   */
  public String makeMapLocally(XYZ_DataSetAPI xyzDataSet){

    // where to find GMT on the local computer
    // THIS SHOULD BE DETERMINED DYNAMICALLY (e.g., w/ "which psxy" command?)
    GMT_PATH="/sw/bin/";

    this.xyzDataSet = xyzDataSet;

    // make the local XYZ data file
    makeXYZ_File();

    // get the GMT script lines
    Vector gmtLines = getGMT_ScriptLines();

    // add a command line to convert the ps file to a jpg file
    gmtLines.add(GMT_PATH+"convert " + PS_FILE_NAME + " " + JPG_FILE_NAME+"\n");

    // Add time stamp to script name and make the script
    gmtFileName=GMT_SCRIPT_NAME.substring(0,GMT_SCRIPT_NAME.indexOf("."))+System.currentTimeMillis()+".txt";
    makeFileFromLines(gmtLines,gmtFileName);

    // now run the GMT script file
    String[] command ={"sh","-c","sh "+gmtFileName};
    RunScript.runScript(command);

    return JPG_FILE_NAME;
  }



  /**
   * This generates GMT map for the given XYZ dataset and for the current parameter setting,
   * using the GMT Servlet on the SCEC server (the map is made on the SCEC server).
   *
   * This returns the full web address to the resulting jpg file.
   */
  public String makeMapUsingServlet(XYZ_DataSetAPI xyzDataSet){

    // Where to find the GMT code on the SCEC server
    // THIS SHOULD BE SET DYNAMICALLY
    GMT_PATH="/opt/install/gmt/bin/";

    this.xyzDataSet = xyzDataSet;

    // check the xyz data set
    if(!xyzDataSet.checkXYZ_NumVals())
      throw new RuntimeException("X, Y and Z dataset does not have equal size");

    // get the GMT script lines
    Vector gmtLines = getGMT_ScriptLines();

    // add a command line to convert the ps file to a jpg file
    gmtLines.add(COMMAND_PATH+"cat "+ PS_FILE_NAME + " | gs -sDEVICE=jpeg -sOutputFile=" + JPG_FILE_NAME + " -"+"\n");

    imgWebAddr = this.openServletConnection(xyzDataSet,gmtLines);

    return imgWebAddr+JPG_FILE_NAME;
  }


  /**
   * This generates GMT map for the given XYZ dataset and for the current parameter setting,
   * using the GMT Web Service on the SCEC server (the map is made on the SCEC server).
   *
   * This returns the full web address to the resulting jpg file.
   */
  public String makeMapUsingWebServer(XYZ_DataSetAPI xyzDataSet){

    // where to find GMT on the SCEC server
    // THIS SHOULD BE SET DYNAMICALLY
    GMT_PATH="/opt/install/gmt/bin/";

    this.xyzDataSet = xyzDataSet;

    // make the local XYZ data file
    makeXYZ_File();

    // get the GMT script lines
    Vector gmtLines = getGMT_ScriptLines();

    // add a command line to convert the ps file to a jpg file
    gmtLines.add(COMMAND_PATH+"cat "+ PS_FILE_NAME + " | gs -sDEVICE=jpeg -sOutputFile=" + JPG_FILE_NAME + " -"+"\n");

    // Add time stamp to script name and make the script
    gmtFileName=GMT_SCRIPT_NAME.substring(0,GMT_SCRIPT_NAME.indexOf("."))+System.currentTimeMillis()+".txt";
    makeFileFromLines(gmtLines,gmtFileName);

    //put files in String array which are to be sent to the server as the attachment
    String[] fileNames = new String[2];
    //getting the GMT script file name
    fileNames[0] = gmtFileName;
    //getting the XYZ file Name
    fileNames[1] = XYZ_FILE_NAME;
    openWebServiceConnection(fileNames);
    return imgWebAddr+JPG_FILE_NAME;
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
    return this.JPG_FILE_NAME;
  }

  // make the local XYZ file
  private void makeXYZ_File() {
    Vector lines = new Vector();
    Vector xVals = xyzDataSet.getX_DataSet();
    Vector yVals = xyzDataSet.getY_DataSet();
    Vector zVals = xyzDataSet.getZ_DataSet();

    if(xyzDataSet.checkXYZ_NumVals()){
      int size = xVals.size();
      for(int i=0;i<size;++i)
        lines.add(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
    }
    else
      throw new RuntimeException("X, Y and Z dataset does not have equal size");

    makeFileFromLines(lines, XYZ_FILE_NAME);
  }

  // make a local file from a vector of strings
  private void makeFileFromLines(Vector lines, String fileName) {
    try{
        fw = new FileWriter(fileName);
        br = new BufferedWriter(fw);
        for(int i=0;i<lines.size();++i)
          br.write((String) lines.get(i));
        br.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }


  //For the webservices Implementation
  private void openWebServiceConnection(String[] fileName){
    int size=fileName.length;

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
  }



  /**
   * sets up the connection with the servlet on the server (gravity.usc.edu)
   */
  private String openServletConnection(XYZ_DataSetAPI xyzDataVals, Vector gmtFileLines) {

    String webaddr=null;
    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL gmtMapServlet = new
                             URL("http://gravity.usc.edu/OpenSHA/servlet/GMT_MapGeneratorServlet");


      URLConnection servletConnection = gmtMapServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the Vector of the gmt Script Lines
      outputToServlet.writeObject(gmtFileLines);


      //sending the contents of the XYZ data set to the servlet
      outputToServlet.writeObject(xyzDataVals);

      //sending the xyz file name to the servlet
      outputToServlet.writeObject(this.XYZ_FILE_NAME);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      webaddr=inputToServlet.readObject().toString();
      if(D) System.out.println("Receiving the Input from the Servlet:"+webaddr);
      inputToServlet.close();

    }
    catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
    return webaddr;
  }



  /**
   * This function is used to make the map from XYZ file.
   * This function is called from CME framework. This function was needed because
   * in CME, there is need that we should be able to specify the name of ps file name
   * and jpeg filename.
   *
   * @param xyzFileName name of the xyz file for which map will be generated
   * @return
   */
  public void makeMapLocally(String xyzFileName) {

    // where to find GMT on SCEC server
    // THIS SHOULD BE SET DYNAMICALLY
    GMT_PATH="/opt/install/gmt/bin/";

    XYZ_FILE_NAME = xyzFileName;

    // contstruct the xyz datatset using the xyz file
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
    this.xyzDataSet = new org.scec.data.ArbDiscretizedXYZ_DataSet(xVals, yVals, zVals) ;

    // get the GMT script lines
    Vector gmtLines = getGMT_ScriptLines();

    // add a command line to convert the ps file to a jpg file
    gmtLines.add(COMMAND_PATH+"cat "+ PS_FILE_NAME + " | gs -sDEVICE=jpeg -sOutputFile=" + JPG_FILE_NAME + " -"+"\n");

    // Add time stamp to script name and make the script
    gmtFileName=GMT_SCRIPT_NAME.substring(0,GMT_SCRIPT_NAME.indexOf("."))+System.currentTimeMillis()+".txt";
    makeFileFromLines(gmtLines,gmtFileName);

    // now run the GMT script file
    String[] command ={"sh","-c","sh "+gmtFileName};
    RunScript.runScript(command);

    // set XYZ filename back to the default
    XYZ_FILE_NAME = DEFAULT_XYZ_FILE_NAME;
  }


  /**
   * This method allows one to set an adjustable parameter.
   *
   * @param paramName - the name of the Parameter to be set
   * @param value - the desired parameter value
   */
  public void setParameter(String paramName, Object value) {
    this.adjustableParams.getParameter(paramName).setValue(value);
  }


  /**
   *
   * @returns the WebAddress to the files if the person used the GMT webservice,
   * to download all the files
   */
  public String getGMTFilesWebAddress(){
    return this.imgWebAddr;
  }


  /**
   * This method generates a list of strings needed for the GMT script
   */
  private Vector getGMT_ScriptLines() {

    String commandLine;

    Vector gmtCommandLines = new Vector();

    // Get the limits and discretization of the map
    double minLat = ((Double) minLatParam.getValue()).doubleValue();
    double maxTempLat = ((Double) maxLatParam.getValue()).doubleValue();
    double minLon = ((Double) minLonParam.getValue()).doubleValue();
    double maxTempLon = ((Double) maxLonParam.getValue()).doubleValue();
    double gridSpacing = ((Double) gridSpacingParam.getValue()).doubleValue();

    // adjust the max lat and lon to be an exact increment (needed for xyz2grd)

    double maxLat = Math.rint(((maxTempLat-minLat)/gridSpacing))*gridSpacing +minLat;
    double maxLon = Math.rint(((maxTempLon-minLon)/gridSpacing))*gridSpacing +minLon;

    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;
    if(D) System.out.println(C+" region = "+region);

    // this is the prefixed used for temporary files
    String fileName = "xyz_data";

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

    // command line to convert xyz file to grd file
    commandLine =GMT_PATH+"xyz2grd "+ XYZ_FILE_NAME+" -G"+ grdFileName+ " -I"+gridSpacing+" "+ region +" -D/degree/degree/amp/=/=/=  -: -H0";
    gmtCommandLines.add(commandLine+"\n");

    // get color scale limits
    double colorScaleMin, colorScaleMax;
    if( colorScaleMode.equals(COLOR_SCALE_MODE_MANUALLY) ) {
      colorScaleMin = ((Double) this.colorScaleMinParam.getValue()).doubleValue();
      colorScaleMax = ((Double) this.colorScaleMaxParam.getValue()).doubleValue();
    }
    else {
      colorScaleMin = xyzDataSet.getMinZ();
      colorScaleMax = xyzDataSet.getMaxZ();
    }

    // make the cpt file
    float inc = (float) ((colorScaleMax-colorScaleMin)/20);
    commandLine=GMT_PATH+"makecpt -C" + cptFile + " -T" + colorScaleMin +"/"+ colorScaleMax +"/" + inc + " -Z > "+fileName+".cpt";
    gmtCommandLines.add(commandLine+"\n");

    // set some defaults
    commandLine = GMT_PATH+"gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR 0/0/0 PAGE_ORIENTATION portrait";
    gmtCommandLines.add(commandLine+"\n");

    // generate the image depending on whether topo relief is desired
    if( resolution.equals(TOPO_RESOLUTION_NONE) ) {
      commandLine=GMT_PATH+"grdimage "+grdFileName+" -X0.75i " + yOff + " " + projWdth + " -C"+fileName+".cpt -K -E70 "+ region + " > " + PS_FILE_NAME;
      gmtCommandLines.add(commandLine+"\n");
    }
    else {
      commandLine=GMT_PATH+"grdsample "+grdFileName+" -G"+fileName+"HiResData.grd -I" + resolution + "c -Q";
      gmtCommandLines.add(commandLine+"\n");
      commandLine=GMT_PATH+"grdcut " + topoIntenFile + " -G"+fileName+"Inten.grd "+region;
      gmtCommandLines.add(commandLine+"\n");
      commandLine=GMT_PATH+"grdimage "+fileName+"HiResData.grd -X0.75i " + yOff + " " + projWdth + " -I"+fileName+"Inten.grd -C"+fileName+".cpt -K -E70 "+ region + " > " + PS_FILE_NAME;
      gmtCommandLines.add(commandLine+"\n");
    }

    // add highways if desired
    if ( !showHiwys.equals(SHOW_HIWYS_NONE) ) {
      commandLine=GMT_PATH+"psxy  "+region+" " + projWdth + " -K -O -W5/125/125/125 -: -Ms " + SCEC_GMT_DATA_PATH + showHiwys + " >> " + PS_FILE_NAME;
      gmtCommandLines.add(commandLine+"\n");
    }

    // add coast and fill if desired
    if(coast.equals(COAST_FILL)) {
      commandLine=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W1/17/73/71 -P -S17/73/71 -Dh >> " + PS_FILE_NAME;
      gmtCommandLines.add(commandLine+"\n");
    }
    else if(coast.equals(COAST_DRAW)) {
      commandLine=GMT_PATH+"pscoast  "+region+" " + projWdth + " -K -O -W4/0/0/0 -P -Dh >> " + PS_FILE_NAME;
      gmtCommandLines.add(commandLine+"\n");
    }

    // set some defaults
    commandLine=GMT_PATH+"gmtset BASEMAP_FRAME_RGB 255/255/255 DEGREE_FORMAT 5 FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
    gmtCommandLines.add(commandLine+"\n");

    // add the color scale
    DecimalFormat df2 = new DecimalFormat("0.E0");
    Float tickInc = new Float(df2.format((colorScaleMax-colorScaleMin)/4.0));
    inc = tickInc.floatValue();
    String tempLabel = "IML";
    commandLine=GMT_PATH+"psscale -Ba"+inc+":"+tempLabel+": -D3.5i/-0.5i/6i/0.3ih -C"+fileName+".cpt -K -O -N70 >> " + PS_FILE_NAME;

    // add the basemap
    commandLine=GMT_PATH+"psbasemap -B1/1eWNs " + projWdth + " "+region+" -Lfx1.25i/0.6i/33.0/50 -O >> " + PS_FILE_NAME;
    gmtCommandLines.add(commandLine+"\n");

    return gmtCommandLines;
  }

}

