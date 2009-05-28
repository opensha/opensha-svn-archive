package org.opensha.commons.mapping.gmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.mapping.gmt.raster.RasterExtractor;
import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.IntegerParameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.util.RunScript;

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
	protected final static boolean D = false;

	// name of the file which contains all the GMT commands that we want to run on server
	protected final static String DEFAULT_GMT_SCRIPT_NAME = "map_GMT_Script.txt";
	protected String GMT_SCRIPT_NAME = DEFAULT_GMT_SCRIPT_NAME;
	protected final static String DEFAULT_XYZ_FILE_NAME = "map_data.txt";
	protected String XYZ_FILE_NAME = DEFAULT_XYZ_FILE_NAME;
	protected final static String DEFAULT_METADATA_FILE_NAME = "map_info.html";
	protected String METADATA_FILE_NAME = DEFAULT_METADATA_FILE_NAME;
	protected final static String DEFAULT_PS_FILE_NAME = "map.ps";
	protected String PS_FILE_NAME = DEFAULT_PS_FILE_NAME;
	protected final static String DEFAULT_JPG_FILE_NAME = "map.jpg";
	protected String JPG_FILE_NAME = DEFAULT_JPG_FILE_NAME;
	protected final static String DEFAULT_PNG_FILE_NAME = "map.png";
	protected String PNG_FILE_NAME = DEFAULT_PNG_FILE_NAME;
	protected final static String DEFAULT_PDF_FILE_NAME = "map.pdf";
	protected String PDF_FILE_NAME = DEFAULT_PDF_FILE_NAME;


	protected String SCALE_LABEL; // what's used to label the color scale

	// paths to needed code
	protected String GMT_PATH;
	protected String GS_PATH;
	protected String CONVERT_PATH;
	protected String PS2PDF_PATH;
	protected static String COMMAND_PATH = "/bin/";

	/*				opensha.usc.edu paths				*/
	public static final String OPENSHA_GMT_PATH="/usr/local/GMT4.2.1/bin/";
	public static final String OPENSHA_GS_PATH="/usr/bin/gs";
	public static final String OPENSHA_PS2PDF_PATH = "/usr/bin/ps2pdf";
	public static final String OPENSHA_CONVERT_PATH="/usr/bin/convert";
	public static final String OPENSHA_GMT_DATA_PATH = "/export/opensha/data/gmt/";
	public static final String OPENSHA_SERVLET_URL = "http://opensha.usc.edu:8080/OpenSHA_dev/GMT_MapGeneratorServlet";
	public static final String OPENSHA_CLASSPATH = "/usr/local/tomcat/default/webapps/OpenSHA_dev/WEB-INF/classes";

	/*				gravity.usc.edu paths				*/
	public static final String GRAVITY_GMT_PATH="/opt/install/gmt/bin/";
	public static final String GRAVITY_GS_PATH="/usr/local/bin/gs";
	public static final String GRAVITY_PS2PDF_PATH = "/usr/local/bin/ps2pdf";
	public static final String GRAVITY_CONVERT_PATH="/usr/bin/convert";
	public static final String GRAVITY_GMT_DATA_PATH = "/usr/scec/data/gmt/";
	public static final String GRAVITY_SERVLET_URL = "http://gravity.usc.edu/OpenSHA/servlet/GMT_MapGeneratorServlet";
	public static final String GRAVITY_CLASSPATH = "";

	// this is the path where general data (e.g., topography) are found:
	private static String SCEC_GMT_DATA_PATH = OPENSHA_GMT_DATA_PATH;
	private static String SERVLET_URL = OPENSHA_SERVLET_URL;
	private static String JAVA_CLASSPATH = OPENSHA_CLASSPATH;

	protected XYZ_DataSetAPI xyzDataSet;

	// common GMT command-line strings
	protected String xOff;
	protected String yOff;
	protected String region;
	protected String projWdth;

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

	// for the final image width:
	public final static String IMAGE_WIDTH_NAME = "Image Width";
	private final static String IMAGE_WIDTH_UNITS = "inches";
	private final static String IMAGE_WIDTH_INFO = "Width of the final jpg image (ps file width is always 8.5 inches)";
	private final static double IMAGE_WIDTH_MIN = 1.0;
	private final static double IMAGE_WIDTH_MAX = 20.0;
	private final static Double IMAGE_WIDTH_DEFAULT = new Double(6.5);
	DoubleParameter imageWidthParam;

	public final static String CPT_FILE_PARAM_NAME = "Color Scheme";
	private final static String CPT_FILE_PARAM_DEFAULT = "MaxSpectrum.cpt";
	private final static String CPT_FILE_PARAM_INFO = "Color scheme for the scale";
	public final static String CPT_FILE_MAX_SPECTRUM = "MaxSpectrum.cpt";
	public final static String CPT_FILE_STEP = "STEP.cpt";
	public final static String CPT_FILE_SHAKEMAP = "Shakemap.cpt";
	public final static String CPT_FILE_RELM = "relm_color_map.cpt";
	public final static String CPT_FILE_GMT_POLAR = "GMT_polar.cpt";
	StringParameter cptFileParam;

	public final static String COAST_PARAM_NAME = "Coast";
	public final static String COAST_DRAW = "Draw Boundary";
	public final static String COAST_FILL = "Draw & Fill";
	public final static String COAST_NONE = "Draw Nothing";
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

	// DPI
	public final static String DPI_PARAM_NAME = "DPI";
	private final static String DPI_PARAM_INFO = "Dots per inch for PS file";
	private final static Integer DPI_DEFAULT = new Integer(72);
	private final static Integer DPI_MIN = new Integer(0);
	private final static Integer DPI_MAX = new Integer(Integer.MAX_VALUE);
	private IntegerParameter dpiParam;

	// Apply GMT smoothing
	public final static String GMT_SMOOTHING_PARAM_NAME = "Apply GMT Smoothing?";
	private final static String GMT_SMOOTHING_PARAM_INFO = "Apply GMT Smoothing?";
	private final static boolean GMT_SMOOTHING_DEFAULT = true;
	private BooleanParameter gmtSmoothingParam; 


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
	public final static String SHOW_HIWYS_PARAM_DEFAULT = "None";
	public final static String SHOW_HIWYS_PARAM_INFO = "Select the highways you'd like to be shown";
	public final static String SHOW_HIWYS_ALL = "ca_hiwys.all.xy";
	public final static String SHOW_HIWYS_MAIN = "ca_hiwys.main.xy";
	public final static String SHOW_HIWYS_OTHER = "ca_hiwys.other.xy";
	public final static String SHOW_HIWYS_NONE = "None";
	StringParameter showHiwysParam;

	//Boolean parameter to see if user wants GMT from the GMT webservice
	public  final static String GMT_WEBSERVICE_NAME = "Use GMT WebService";
	private final static String GMT_WEBSERVICE_INFO= "Use server-mode GMT (rather than on this computer)";
	BooleanParameter gmtFromServer;

	//Boolean parameter to see if user wants linear or log plot
	public final static String LOG_PLOT_NAME = "Plot Log";
	private final static String LOG_PLOT_INFO = "Plot Log or Linear Map";
	protected BooleanParameter logPlotParam;


	//Boolean parameter to see if user wants to give a custom Label
	public final static String CUSTOM_SCALE_LABEL_PARAM_CHECK_NAME = "Custom Scale Label";
	private final static String CUSTOM_SCALE_LABEL_PARAM_CHECK_INFO = "Allows to give a custom scale label to the map";
	protected BooleanParameter customScaleLabelCheckParam;


	public final static String SCALE_LABEL_PARAM_NAME = "Scale Label";
	private final static String SCALE_LABEL_PARAM_INFO = "Map Scale Label(Don't give any brackets in label)";
	protected StringParameter scaleLabelParam;


	protected ParameterList adjustableParams;

	//GMT files web address(if the person is using the gmt webService)
	protected String imgWebAddr=null;



	public GMT_MapGenerator() {

		minLatParam = new DoubleParameter(MIN_LAT_PARAM_NAME,-90,90,LAT_LON_PARAM_UNITS,MIN_LAT_PARAM_DEFAULT);
		minLatParam.setInfo(LAT_LON_PARAM_INFO);
		maxLatParam = new DoubleParameter(MAX_LAT_PARAM_NAME,-90,90,LAT_LON_PARAM_UNITS,MAX_LAT_PARAM_DEFAULT);
		minLatParam.setInfo(LAT_LON_PARAM_INFO);
		minLonParam = new DoubleParameter(MIN_LON_PARAM_NAME,-360,360,LAT_LON_PARAM_UNITS,MIN_LON_PARAM_DEFAULT);
		minLatParam.setInfo(LAT_LON_PARAM_INFO);
		maxLonParam = new DoubleParameter(MAX_LON_PARAM_NAME,-360,360,LAT_LON_PARAM_UNITS,MAX_LON_PARAM_DEFAULT);
		minLatParam.setInfo(LAT_LON_PARAM_INFO);
		gridSpacingParam = new DoubleParameter(GRID_SPACING_PARAM_NAME,0.001,100,LAT_LON_PARAM_UNITS,GRID_SPACING_PARAM_DEFAULT);
		minLatParam.setInfo(GRID_SPACING_PARAM_INFO);

		imageWidthParam = new DoubleParameter(IMAGE_WIDTH_NAME,IMAGE_WIDTH_MIN,IMAGE_WIDTH_MAX,IMAGE_WIDTH_UNITS,IMAGE_WIDTH_DEFAULT);
		imageWidthParam.setInfo(IMAGE_WIDTH_INFO);

		StringConstraint cptFileConstraint = new StringConstraint();
		cptFileConstraint.addString( CPT_FILE_MAX_SPECTRUM );
		cptFileConstraint.addString( CPT_FILE_STEP );
		cptFileConstraint.addString( CPT_FILE_SHAKEMAP );
		cptFileConstraint.addString(CPT_FILE_RELM);
		cptFileConstraint.addString(CPT_FILE_GMT_POLAR);
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

		gmtFromServer = new BooleanParameter(GMT_WEBSERVICE_NAME,new Boolean("true"));
		gmtFromServer.setInfo(GMT_WEBSERVICE_INFO);

		logPlotParam = new BooleanParameter(LOG_PLOT_NAME, new Boolean("true"));
		logPlotParam.setInfo(LOG_PLOT_INFO);


		customScaleLabelCheckParam = new BooleanParameter(CUSTOM_SCALE_LABEL_PARAM_CHECK_NAME,new Boolean(false));
		customScaleLabelCheckParam.setInfo(CUSTOM_SCALE_LABEL_PARAM_CHECK_INFO);

		scaleLabelParam = new StringParameter(SCALE_LABEL_PARAM_NAME,"");
		scaleLabelParam.setInfo(SCALE_LABEL_PARAM_INFO);

		// DPI param
		this.dpiParam = new IntegerParameter(DPI_PARAM_NAME, DPI_MIN, DPI_MAX, DPI_DEFAULT);
		dpiParam.setInfo(DPI_PARAM_INFO);

		// whether to apply GMT smoothing
		this.gmtSmoothingParam = new BooleanParameter(GMT_SMOOTHING_PARAM_NAME, GMT_SMOOTHING_DEFAULT);
		gmtSmoothingParam.setInfo(GMT_SMOOTHING_PARAM_INFO);


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
		adjustableParams.addParameter(imageWidthParam);
		adjustableParams.addParameter(customScaleLabelCheckParam);
		adjustableParams.addParameter(scaleLabelParam);
		adjustableParams.addParameter(gmtSmoothingParam);
		adjustableParams.addParameter(dpiParam);
		adjustableParams.addParameter(gmtFromServer);
		adjustableParams.addParameter(logPlotParam);


	}



	/**
	 * this function generates a GMT map from an XYZ data set using the current
	 * parameter settings, and using the version of GMT on the local computer.
	 *
	 * @param xyzDataSet
	 * @param scaleLabel - a string for the label (with no spaces!)
	 * @return - the name of the jpg file
	 */
	public String makeMapLocally(XYZ_DataSetAPI xyzDataSet, String scaleLabel,
			String metadata, String dirName) throws GMT_MapException{

		//creates the metadata file
		createMapInfoFile(metadata);

		// THESE SHOULD BE SET DYNAMICALLY
		// CURRENTLY HARD CODED FOR Ned and Nitin's Macs
		GMT_PATH="/sw/bin/";
		GS_PATH="/sw/bin/gs";
		PS2PDF_PATH = "/sw/bin/ps2pdf";
		CONVERT_PATH="/sw/bin/convert";

		// The color scale label
		SCALE_LABEL = scaleLabel;

		this.xyzDataSet = xyzDataSet;

		// take the log(z) values if necessary (and change label)
		checkForLogPlot();

		// make the local XYZ data file
		makeXYZ_File(XYZ_FILE_NAME);

		// get the GMT script lines
		ArrayList gmtLines = getGMT_ScriptLines();

		// make the script
		makeFileFromLines(gmtLines,GMT_SCRIPT_NAME);

		// now run the GMT script file
		String[] command ={"sh","-c","sh "+GMT_SCRIPT_NAME};
		RunScript.runScript(command);

		return JPG_FILE_NAME;
	}



	/**
	 * This generates GMT map for the given XYZ dataset and for the current parameter setting,
	 * using the GMT Servlet on the SCEC server (the map is made on the SCEC server).
	 *
	 * @param xyzDataSet
	 * @param scaleLabel - a string for the label (with no spaces!)
	 * @return - the name of the jpg file
	 */
	public String makeMapUsingServlet(XYZ_DataSetAPI xyzDataSet,
			String scaleLabel, String metadata, String dirName)
	throws GMT_MapException,RuntimeException{

		// Set paths for the SCEC server (where the Servlet is)
		GMT_PATH = OPENSHA_GMT_PATH;
		GS_PATH = OPENSHA_GS_PATH;
		PS2PDF_PATH = OPENSHA_PS2PDF_PATH;
		CONVERT_PATH = OPENSHA_CONVERT_PATH;

		// The color scale label
		SCALE_LABEL = scaleLabel;

		this.xyzDataSet = xyzDataSet;

		// take the log(z) values if necessary (and change label)
		checkForLogPlot();

		// check the xyz data set
		if(!xyzDataSet.checkXYZ_NumVals())
			throw new RuntimeException("X, Y and Z dataset does not have equal size");

		// get the GMT script lines
		ArrayList gmtLines = getGMT_ScriptLines();
		imgWebAddr = this.openServletConnection(xyzDataSet,gmtLines,metadata, dirName);


		return imgWebAddr+JPG_FILE_NAME;
	}


	/**
	 * This generates GMT map for the given XYZ dataset and for the current parameter setting,
	 * using the GMT Web Service on the SCEC server (the map is made on the SCEC server).
	 *
	 * @param xyzDataSet
	 * @param scaleLabel - a string for the label (with no spaces!)
	 * @return - the name of the jpg file
	 */
	public String makeMapUsingWebServer(XYZ_DataSetAPI xyzDataSet, String scaleLabel, String metadata)
	throws GMT_MapException{
		//creates the metadata file
		createMapInfoFile(metadata);
		// Set paths for the SCEC server (where the Servlet is)
		GMT_PATH = OPENSHA_GMT_PATH;
		GS_PATH = OPENSHA_GS_PATH;
		PS2PDF_PATH = OPENSHA_PS2PDF_PATH;
		CONVERT_PATH = OPENSHA_CONVERT_PATH;

		// The color scale label
		SCALE_LABEL = scaleLabel;

		this.xyzDataSet = xyzDataSet;

		// take the log(z) values if necessary (and change label)
		checkForLogPlot();

		// make the local XYZ data file
		makeXYZ_File(XYZ_FILE_NAME);

		// get the GMT script lines
		ArrayList gmtLines = getGMT_ScriptLines();

		// make the script
		makeFileFromLines(gmtLines,GMT_SCRIPT_NAME);

		//put files in String array which are to be sent to the server as the attachment
		String[] fileNames = new String[3];
		//getting the GMT script file name
		fileNames[0] = GMT_SCRIPT_NAME;
		//getting the XYZ file Name
		fileNames[1] = XYZ_FILE_NAME;

		//metadata file
		fileNames[2] = METADATA_FILE_NAME;
		//openWebServiceConnection(fileNames);
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

	/**
	 *
	 * @returns the ArrayList containing the Metadata Info
	 */
	protected ArrayList getMapInfoLines(){
		ArrayList metadataFilesLines = new ArrayList();
		try{
			FileReader  fr = new FileReader(METADATA_FILE_NAME);
			BufferedReader br = new BufferedReader(fr);
			String fileLines = br.readLine();
			while(fileLines !=null){
				metadataFilesLines.add(fileLines);
				fileLines = br.readLine();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return metadataFilesLines;
	}


	// make the local XYZ file
	protected void makeXYZ_File(String fileName) {
		ArrayList lines = new ArrayList();
		ArrayList xVals = xyzDataSet.getX_DataSet();
		ArrayList yVals = xyzDataSet.getY_DataSet();
		ArrayList zVals = xyzDataSet.getZ_DataSet();

		if(xyzDataSet.checkXYZ_NumVals()){
			int size = xVals.size();
			for(int i=0;i<size;++i)
				if(!Double.isNaN(((Double)zVals.get(i)).doubleValue()))
					lines.add(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i));
		}
		else
			throw new RuntimeException("X, Y and Z dataset does not have equal size");

		makeFileFromLines(lines, fileName);
	}


	// make the xyzDataSet from a local file
	protected void make_xyzDataSet(String fileName) {

		ArrayList xVals = new ArrayList();
		ArrayList yVals =  new ArrayList();
		ArrayList zVals =  new ArrayList();
		try {
			FileReader fr = new FileReader(fileName); //open the xyx file
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
		this.xyzDataSet = new org.opensha.commons.data.ArbDiscretizedXYZ_DataSet(xVals, yVals, zVals) ;
	}


	// make a local file from a vector of strings
	protected void makeFileFromLines(ArrayList lines, String fileName) {
		try{
			FileWriter fw = new FileWriter(fileName);
			BufferedWriter br = new BufferedWriter(fw);
			for(int i=0;i<lines.size();++i)
				br.write((String) lines.get(i)+"\n");
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	//For the webservices Implementation
	/*private void openWebServiceConnection(String[] fileName){
    int size=fileName.length;

    FileDataSource[] fs = new FileDataSource[size+2];
    DataHandler dh[] = new DataHandler[size+2];
    System.out.println("File-0: "+fileName[0]);
    fs[0] =new FileDataSource(fileName[0]);
    dh[0] = new DataHandler(fs[0]);

    System.out.println("File-1: "+fileName[1]);
    fs[1] =new FileDataSource(fileName[1]);
    dh[1] = new DataHandler(fs[1]);


    System.out.println("File-2: "+fileName[2]);
    fs[2] =new FileDataSource(fileName[2]);
    dh[2] = new DataHandler(fs[2]);

    GMT_WebService_Impl client = new GMT_WebService_Impl();
    GMT_WebServiceAPI gmt = client.getGMT_WebServiceAPIPort();
    try{
      imgWebAddr = gmt.runGMT_Script(fileName,dh);
      System.out.println("imgWebAddr: "+imgWebAddr);
    }catch(Exception e){
      e.printStackTrace();
    }
  }*/

	/**
	 * sets the name of the metadata file with fileName( with full path)
	 * @param fileName
	 */
	public void setMetatdataFileName(String fileName){
		METADATA_FILE_NAME = fileName;
	}

	/**
	 * sets up the connection with the servlet on the server (gravity.usc.edu)
	 */
	protected String openServletConnection(XYZ_DataSetAPI xyzDataVals,
			ArrayList gmtFileLines,
			String metadataLines, String dirName) throws RuntimeException{

		String webaddr=null;
		try{

			if(D) System.out.println("starting to make connection with servlet");
			URL gmtMapServlet = new URL(SERVLET_URL);


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

			//sending the directory name to the servlet
			outputToServlet.writeObject(dirName);

			//sending the ArrayList of the gmt Script Lines
			outputToServlet.writeObject(gmtFileLines);


			//sending the contents of the XYZ data set to the servlet
			outputToServlet.writeObject(xyzDataVals);

			//sending the xyz file name to the servlet
			outputToServlet.writeObject(XYZ_FILE_NAME);

			//sending the contents of the Metadata file to the server.
			outputToServlet.writeObject(metadataLines);

			//sending the name of the MetadataFile to the server.
			outputToServlet.writeObject(DEFAULT_METADATA_FILE_NAME);


			outputToServlet.flush();
			outputToServlet.close();

			// Receive the "actual webaddress of all the gmt related files"
			// from the servlet after it has received all the data
			ObjectInputStream inputToServlet = new
			ObjectInputStream(servletConnection.getInputStream());

			Object messageFromServlet = inputToServlet.readObject();
			inputToServlet.close();
			if(messageFromServlet instanceof String){
				webaddr = (String) messageFromServlet;
				if (D) System.out.println("Receiving the Input from the Servlet:" +
						webaddr);
			}
			else
				throw (RuntimeException)messageFromServlet;
		}catch(RuntimeException e){
			throw new RuntimeException(e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Server is down , please try again later");
		}
		return webaddr;
	}



	/**
	 * This function is used to make the map from XYZ file.
	 * This function is called from CME framework (on usc.gravity.edu). This function was needed because
	 * in CME, there is need that we should be able to specify the name of ps file name
	 * and jpeg filename.
	 *
	 * @param xyzDataSet
	 * @param scaleLabel - a string for the label (with no spaces!)
	 * @return - the name of the jpg file
	 */
	public void makeMapForCME(String xyzFileName, String pdfFileName,
			String psFileName, String jpgFileName, String scaleLabel) throws
			GMT_MapException{

		XYZ_FILE_NAME = xyzFileName;
		PS_FILE_NAME = psFileName;
		JPG_FILE_NAME = jpgFileName;
		PDF_FILE_NAME = pdfFileName;

		// THESE SHOULD BE SET DYNAMICALLY
		// CURRENTLY HARD CODED FOR gravity AT SCEC (for Vipin)
		// IF THIS CAN BE DONE WE CAN GENERALIZE THIS METHOD NAME
		GMT_PATH="/opt/install/gmt/bin/";
		GS_PATH="/usr/local/bin/gs";
		PS2PDF_PATH = "/usr/local/bin/ps2pdf";
		CONVERT_PATH="/usr/bin/convert";

		// The color scale label
		SCALE_LABEL = scaleLabel;

		make_xyzDataSet(XYZ_FILE_NAME);

		// take the log(z) values if necessary (and change label)
		checkForLogPlot();

		// save file locally if log-plot is desired
		boolean logPlotCheck = ((Boolean)logPlotParam.getValue()).booleanValue();
		if(logPlotCheck){
			XYZ_FILE_NAME = "Log_"+XYZ_FILE_NAME;
			makeXYZ_File(XYZ_FILE_NAME);
		}

		// get the GMT script lines
		ArrayList gmtLines = getGMT_ScriptLines();

		// make the script
		makeFileFromLines(gmtLines,GMT_SCRIPT_NAME);

		// now run the GMT script file
		String[] command ={"sh","-c","sh "+GMT_SCRIPT_NAME};
		RunScript.runScript(command);

		// set XYZ filename back to the default
		XYZ_FILE_NAME = DEFAULT_XYZ_FILE_NAME;
		PS_FILE_NAME = DEFAULT_PS_FILE_NAME;
		JPG_FILE_NAME = DEFAULT_JPG_FILE_NAME;
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


	// this computes a nice length for the km_scale
	private double getNiceKmScaleLength(double lat,double minLon,double maxLon) {

		double target = (maxLon-minLon)*111*Math.cos(Math.PI*lat/180) / 4;
		double test = 0.1;

		while(target > test) {
			test*=10;
		}
		test /= 10;
		return Math.ceil(target/test)*test;
	}

	// this computes a nice map tick intervale
	private double getNiceMapTickInterval(double minLat,double maxLat,double minLon,double maxLon) {

		double diff, niceTick=Double.NaN;

		// find the minimum range
		if( maxLat-minLat < maxLon-minLon)
			diff = maxLat-minLat;
		else
			diff = maxLon-minLon;

		// now divide this by two to ensureat least two labeled segments
		diff /= 2;

		// now find the first nice value below this one
		boolean finished = false;
		double fact = 100;
		while(!finished) {

			if((niceTick=1.0*fact) <= diff)
				finished = true;
			else if((niceTick=0.5*fact) <= diff)
				finished = true;
			else if((niceTick=0.25*fact) <= diff)
				finished = true;
			else
				fact /= 10.0;

		}
		return (double) ((float) niceTick);

	}

	public EvenlyGriddedRectangularGeographicRegion getEvenlyGriddedGeographicRegion() throws RegionConstraintException {
		// Get the limits and discretization of the map
		double minLat = ((Double) minLatParam.getValue()).doubleValue();
		double maxLat = ((Double) maxLatParam.getValue()).doubleValue();
		double minLon = ((Double) minLonParam.getValue()).doubleValue();
		double maxLon = ((Double) maxLonParam.getValue()).doubleValue();
		double gridSpacing = ((Double) gridSpacingParam.getValue()).doubleValue();

		return new EvenlyGriddedRectangularGeographicRegion(minLat, maxLat, minLon, maxLon, gridSpacing);
	}

	/**
	 * This method generates a list of strings needed for the GMT script
	 */
	protected ArrayList getGMT_ScriptLines() throws GMT_MapException{

		ArrayList<String> rmFiles = new ArrayList<String>();

		String commandLine;

		ArrayList<String> gmtCommandLines = new ArrayList<String>();

		// Get the limits and discretization of the map
		double minLat = ((Double) minLatParam.getValue()).doubleValue();
		double maxTempLat = ((Double) maxLatParam.getValue()).doubleValue();
		double minLon = ((Double) minLonParam.getValue()).doubleValue();
		double maxTempLon = ((Double) maxLonParam.getValue()).doubleValue();
		double gridSpacing = ((Double) gridSpacingParam.getValue()).doubleValue();

		// adjust the max lat and lon to be an exact increment (needed for xyz2grd)
		double maxLat = Math.rint(((maxTempLat-minLat)/gridSpacing))*gridSpacing +minLat;
		double maxLon = Math.rint(((maxTempLon-minLon)/gridSpacing))*gridSpacing +minLon;

		region = " -R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat+" ";
		if(D) System.out.println(C+" region = "+region);

		// this is the prefixed used for temporary files
		String fileName = "temp_junk";

		String grdFileName  = fileName+".grd";
		rmFiles.add(grdFileName);

		String cptFile = SCEC_GMT_DATA_PATH + (String) cptFileParam.getValue();

		String colorScaleMode = (String) colorScaleModeParam.getValue();

		String coast = (String) coastParam.getValue();

		// Set resolution according to the topoInten file chosen (options are 3, 6, 18, or 30):
		String resolution = (String) topoResolutionParam.getValue();
		String topoIntenFile = SCEC_GMT_DATA_PATH + "calTopoInten" + resolution+".grd";

		// hard-code check that lat & lon bounds are in the region where we have topography:
		// this is only temporary until we have worldwide topo data
		if( !resolution.equals(TOPO_RESOLUTION_NONE) &&
				( maxLat > 43 || minLat < 32 || minLon < -126 || maxLon > -115 ))
			throw new GMT_MapException("Topography not available for the chosen region; please select \"" +
					TOPO_RESOLUTION_NONE + "\" for the " + TOPO_RESOLUTION_PARAM_NAME + " parameter");

		// Set highways String
		String showHiwys = (String) showHiwysParam.getValue();

		// plot size parameter
		double plotWdth = 6.5;
		projWdth = " -JM"+plotWdth+"i ";
		double plotHght = ((maxLat-minLat)/(maxLon-minLon))*plotWdth/Math.cos(Math.PI*(maxLat+minLat)/(2*180));

		double yOffset = 11 - plotHght - 0.5;
		yOff = " -Y" + yOffset + "i ";

		// set x-axis offset to 1 inch
		xOff = " -X1.0i ";

		gmtCommandLines.add("#!/bin/bash\n\n");
		gmtCommandLines.add("# path variables\n");
		gmtCommandLines.add("GMT_PATH='" + GMT_PATH + "'\n");
		gmtCommandLines.add("CONVERT_PATH='" + CONVERT_PATH + "'\n");
		gmtCommandLines.add("COMMAND_PATH='" + COMMAND_PATH + "'\n");
		gmtCommandLines.add("PS2PDF_PATH='" + PS2PDF_PATH + "'\n\n");

		// command line to convert xyz file to grd file
		commandLine = "${GMT_PATH}xyz2grd "+ XYZ_FILE_NAME+" -G"+ grdFileName+ " -I"+gridSpacing+ region +" -D/degree/degree/amp/=/=/=  -: -H0";
		gmtCommandLines.add(commandLine+"\n");

		// get color scale limits
		double colorScaleMin, colorScaleMax;
		if( colorScaleMode.equals(COLOR_SCALE_MODE_MANUALLY) ) {
			colorScaleMin = ((Double) this.colorScaleMinParam.getValue()).doubleValue();
			colorScaleMax = ((Double) this.colorScaleMaxParam.getValue()).doubleValue();
			if (colorScaleMin >= colorScaleMax)
				throw new RuntimeException("Error: Color-Scale Min must be less than the Max");
		}
		else {
			colorScaleMin = xyzDataSet.getMinZ();
			colorScaleMax = xyzDataSet.getMaxZ();
			System.out.println(colorScaleMin+","+colorScaleMax);
			if (colorScaleMin == colorScaleMax)
				throw new RuntimeException("Can't make the image plot because all Z values in the XYZ dataset have the same value ");
		}

		// make the cpt file
		float inc = (float) ((colorScaleMax-colorScaleMin)/20);
		commandLine="${GMT_PATH}makecpt -C" + cptFile + " -T" + colorScaleMin +"/"+ colorScaleMax +"/" + inc + " -Z > "+fileName+".cpt";
		gmtCommandLines.add(commandLine+"\n");

		// set some defaults
		commandLine = "${GMT_PATH}gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR 0/0/0 PAGE_ORIENTATION portrait PAPER_MEDIA letter";
		gmtCommandLines.add(commandLine+"\n");

		int dpi = (Integer)this.dpiParam.getValue();
		String gmtSmoothOption="";
		if(!(Boolean)this.gmtSmoothingParam.getValue()) gmtSmoothOption=" -T ";
		// generate the image depending on whether topo relief is desired
		if( resolution.equals(TOPO_RESOLUTION_NONE) ) {
			commandLine="${GMT_PATH}grdimage "+ grdFileName + xOff + yOff + projWdth + " -C"+fileName+".cpt "+gmtSmoothOption+" -K -E"+dpi+ region + " > " + PS_FILE_NAME;
			gmtCommandLines.add(commandLine+"\n");
		}
		else {
			// redefine the region so that maxLat, minLat, and delta fall exactly on the topoIntenFile
			gridSpacing = (new Integer(resolution)).doubleValue()/(3600.0);
			double tempNum = Math.ceil((minLat-32.0)/gridSpacing);
			minLat = tempNum*gridSpacing+32.0;
			tempNum = Math.ceil((minLon-(-126))/gridSpacing);
			minLon = tempNum*gridSpacing+(-126);
			maxLat = Math.floor(((maxLat-minLat)/gridSpacing))*gridSpacing +minLat;
			maxLon = Math.floor(((maxLon-minLon)/gridSpacing))*gridSpacing +minLon;
			region = " -R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat + " ";

			String hiResFile = fileName+"HiResData.grd";
			rmFiles.add(hiResFile);
			commandLine="${GMT_PATH}grdsample "+grdFileName+" -G"+hiResFile+" -I" +
			resolution + "c -Q "+region;
			gmtCommandLines.add(commandLine+"\n");
			String intenFile = fileName+"Inten.grd";
			commandLine="${GMT_PATH}grdcut " + topoIntenFile + " -G"+intenFile+ " " +region;
			rmFiles.add(intenFile);
			gmtCommandLines.add(commandLine+"\n");
			commandLine="${GMT_PATH}grdimage "+hiResFile+" " + xOff + yOff + projWdth +
			" -I"+fileName+"Inten.grd -C"+fileName+".cpt "+ gmtSmoothOption +"-K -E"+dpi+ region + " > " + PS_FILE_NAME;
			gmtCommandLines.add(commandLine+"\n");
		}

		// add highways if desired
		if ( !showHiwys.equals(SHOW_HIWYS_NONE) ) {
			commandLine="${GMT_PATH}psxy  "+region + projWdth + " -K -O -W5/125/125/125 -: -Ms " + SCEC_GMT_DATA_PATH + showHiwys + " >> " + PS_FILE_NAME;
			gmtCommandLines.add(commandLine+"\n");
		}

		// add coast and fill if desired
		if(coast.equals(COAST_FILL)) {
			commandLine="${GMT_PATH}pscoast "+region + projWdth + " -K -O  -W1/17/73/71 -P -S17/73/71 -Dh -N2 >> " + PS_FILE_NAME;
			gmtCommandLines.add(commandLine+"\n");
		}
		else if(coast.equals(COAST_DRAW)) {
			commandLine="${GMT_PATH}pscoast "+region + projWdth + " -K -O  -W4/0/0/0 -P -Dh -N2>> " + PS_FILE_NAME;
			gmtCommandLines.add(commandLine+"\n");
		}


		// This adds intermediate commands
		addIntermediateGMT_ScriptLines(gmtCommandLines);

		// set some defaults
		commandLine="${GMT_PATH}gmtset BASEMAP_FRAME_RGB 255/255/255 PLOT_DEGREE_FORMAT -D FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
		gmtCommandLines.add(commandLine+"\n");

		// add the color scale
		DecimalFormat df2 = new DecimalFormat("0.E0");
		Float tickInc = new Float(df2.format((colorScaleMax-colorScaleMin)/4.0));
		inc = tickInc.floatValue();
		//checks to see if customLabel is selected, then get the custom label
		boolean customLabelSelected = ((Boolean)customScaleLabelCheckParam.getValue()).booleanValue();
		String scaleLabel ="";
		if(customLabelSelected)
			scaleLabel = (String)scaleLabelParam.getValue();
		else
			scaleLabel = SCALE_LABEL;
		commandLine="${GMT_PATH}psscale -Ba"+inc+":"+scaleLabel+": -D3.25i/-0.5i/6i/0.3ih -C"+fileName+".cpt -O -K -N70 >> " + PS_FILE_NAME;
		gmtCommandLines.add(commandLine+"\n");

		// add the basemap
		double niceKmLength = getNiceKmScaleLength(minLat, minLon, maxLon);
		double kmScaleXoffset = plotWdth/2;
		double niceTick = getNiceMapTickInterval(minLat, maxLat, minLon, maxLon);
		commandLine="${GMT_PATH}psbasemap -B"+niceTick+"/"+niceTick+"eWNs " + projWdth +region+
		" -Lfx"+kmScaleXoffset+"i/0.5i/"+minLat+"/"+niceKmLength+" -O >> " + PS_FILE_NAME;
		gmtCommandLines.add(commandLine+"\n");

		// boolean to switch between purely using convert for the ps conversion, 
		// and using gs.
		boolean use_gs_raster = true;

		int heightInPixels = (int) ((11.0 - yOffset + 2.0) * (double) dpi);
		String convertArgs = "-crop 595x"+heightInPixels+"+0+0";
		double imageWidth = ((Double)imageWidthParam.getValue()).doubleValue();
		if (imageWidth != IMAGE_WIDTH_DEFAULT.doubleValue()) {
			int wdth = (int)(imageWidth*(double)dpi);
			convertArgs += " -filter Lanczos -geometry "+wdth;
		}
		if (use_gs_raster) {
			int jpeg_quality= 90;
			gmtCommandLines.add("${COMMAND_PATH}cat "+ PS_FILE_NAME + " | "+GS_PATH+" -sDEVICE=jpeg " + 
					" -dJPEGQ="+jpeg_quality+" -sOutputFile="+JPG_FILE_NAME+" -\n");
			gmtCommandLines.add("${COMMAND_PATH}cat "+ PS_FILE_NAME + " | "+GS_PATH+" -sDEVICE=png16m " + 
					" -sOutputFile="+PNG_FILE_NAME+" -\n");

			gmtCommandLines.add("${CONVERT_PATH} " + convertArgs + " " + JPG_FILE_NAME + " " + JPG_FILE_NAME+"\n");
			gmtCommandLines.add("${CONVERT_PATH} " + convertArgs + " " + PNG_FILE_NAME + " " + PNG_FILE_NAME+"\n");
		} else {
			convertArgs = "-density " + dpi + " " + convertArgs;

			// add a command line to convert the ps file to a jpg file - using convert
			gmtCommandLines.add("${CONVERT_PATH} " + convertArgs + " " + PS_FILE_NAME + " " + JPG_FILE_NAME+"\n");
			gmtCommandLines.add("${CONVERT_PATH} " + convertArgs + " " + PS_FILE_NAME + " " + PNG_FILE_NAME+"\n");
		}

		boolean googleearth = false;

		// Add google earth lines...this doesn't work yet, still need to figure out how to get raste extracter
		// to be called during execution
		if (googleearth) {
			System.out.println("Making Google Earth files!");
			String gEarth_psFileName = "gEarth_" + PS_FILE_NAME;
			rmFiles.add(gEarth_psFileName);
			String gEarth_proj = " -JQ180/"+plotWdth+"i ";
			String gEarth_kmz_name = "map.kmz";
			if( resolution.equals(TOPO_RESOLUTION_NONE) ) {
				commandLine="${GMT_PATH}grdimage "+ grdFileName + xOff + yOff + gEarth_proj +
				" -C"+fileName+".cpt "+gmtSmoothOption+" -K -E"+dpi+ region + " > " + gEarth_psFileName;
				gmtCommandLines.add(commandLine+"\n");
			}
			else {
				commandLine="${GMT_PATH}grdimage "+fileName+"HiResData.grd " + xOff + yOff + gEarth_proj +
				" -I"+fileName+"Inten.grd -C"+fileName+".cpt "+ gmtSmoothOption +"-K -E"+
				dpi+ region + " > " + gEarth_psFileName;
				gmtCommandLines.add(commandLine+"\n");
			}

			commandLine = "java -cp " + JAVA_CLASSPATH + " " + GMT_KML_Generator.class.getName() + PS_FILE_NAME
			+ " " + gEarth_kmz_name + " " + minLat + " " + maxLat + " " + minLon + " " + maxLon;
			gmtCommandLines.add(commandLine+"\n");
		}

		// add a command line to convert the ps file to a jpg file - using gs
		// this looks a bit better than that above (which sometimes shows some horz lines).


		//		commandLine = "${CONVERT_PATH} -crop 595x"+heightInPixels+"+0+0 temp1.jpg temp2.jpg";
		//		gmtCommandLines.add(commandLine+"\n");

		//resize the image if desired
		//		if (imageWidth != IMAGE_WIDTH_DEFAULT.doubleValue()) {
		//			int wdth = (int)(imageWidth*(double)dpi);
		//			commandLine = "${CONVERT_PATH} -filter Lanczos -geometry "+wdth+" temp2.jpg "+JPG_FILE_NAME;
		//			gmtCommandLines.add(commandLine+"\n");
		//		}
		//		else {
		//			commandLine = "${COMMAND_PATH}mv temp2.jpg "+JPG_FILE_NAME;
		//			gmtCommandLines.add(commandLine+"\n");
		//		}

		commandLine = "${PS2PDF_PATH}  "+PS_FILE_NAME+"  "+PDF_FILE_NAME;
		gmtCommandLines.add(commandLine+"\n");
		// clean out temp files
		if (rmFiles.size() > 0) {
			String rmCommand = "${COMMAND_PATH}rm";
			for (String rmFile : rmFiles) {
				rmCommand += " " + rmFile;
			}
			gmtCommandLines.add(rmCommand+"\n");
		}

		// This adds any final commands
		addFinalGMT_ScriptLines(gmtCommandLines);


		return gmtCommandLines;
	}


	/**
	 * This method allows subclasses to add intemediate lines the the GMT script.  For
	 * example, for Scenario ShakeMaps one might want to plot the Earthuake Rupture Surface.
	 * These lines have to be added at an intermediate step because the last layer in GMT
	 * has to have the "-O" but not "-K" option.
	 */
	protected void addIntermediateGMT_ScriptLines(ArrayList gmtLines) {

	}


	/**
	 * Function to adds any final commands desired by a subclass.
	 * @param gmtCommandLines : ArrayList to store the command line
	 */
	protected void addFinalGMT_ScriptLines(ArrayList gmtCommandLines){

	}


	/**
	 * If log-plot has been chosen, this replaces the z-values in the xyzDataSet
	 * with the log (base 10) values.  Zero values are converted to 10e-16.
	 * This also wraps the SCALE_LABEL in "log(*)".
	 * @param xyzVals
	 */
	private void checkForLogPlot(){
		//checks to see if the user wants Log Plot, if so then convert the zValues to the Log Space
		boolean logPlotCheck = ((Boolean)logPlotParam.getValue()).booleanValue();
		if(logPlotCheck){
			//ArrayList of the Original z Values in the linear space
			ArrayList zLinearVals = xyzDataSet.getZ_DataSet();
			int size = zLinearVals.size();
			for(int i=0;i<size;++i){
				double zVal = ((Double)zLinearVals.get(i)).doubleValue();
				if(zVal == 0) zLinearVals.set(i,new Double(Double.NaN));
				//converting the Z linear Vals to the Log space.
				else zLinearVals.set(i,new Double(0.4343 * StrictMath.log(zVal)));
			}
			SCALE_LABEL = "\"log@-10@-\050"+SCALE_LABEL+"\051\"";
		}
	}

	/**
	 * This simply saves the supplied string to an ascii file that is placed in the
	 * same directory where the image, gmt script, etc. are placed.  The name of the file is in
	 * the METADATA_FILE_NAME String.  This is simply a method for saving arbitrary
	 * metatdata associated with a map.
	 */
	public void createMapInfoFile(String mapInfo){
		ArrayList mapInfoLines = new ArrayList();
		StringTokenizer st = new StringTokenizer(mapInfo,"\n");
		while(st.hasMoreTokens())
			mapInfoLines.add(st.nextToken());
		makeFileFromLines(mapInfoLines,METADATA_FILE_NAME);
	}

}

