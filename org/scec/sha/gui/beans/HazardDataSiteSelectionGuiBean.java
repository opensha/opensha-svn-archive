package org.scec.sha.gui.beans;

import java.net.*;
import java.io.*;
import java.util.*;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;


/**
 * <p>Title: HazardDataSiteSelectionGuiBean</p>
 * <p>Description: This Gui Bean allows the user to select the site with the constraint
 * based on the range of the Dataset.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class HazardDataSiteSelectionGuiBean extends ParameterListEditor implements ParameterChangeListener{


  public static final String GUI_BEAN_TITLE = "Choose DataSet and Site";

  public static String SERVLET_URL  = "http://gravity.usc.edu/OpenSHA/servlet/HazardMapViewerServlet";
  public static String PLOTTER_CALC_SERVLET = "http://gravity.usc.edu/OpenSHA/servlet/HazardDataSetPlotterCalcServlet";

  private StringParameter dataSetParam;
  public String DATA_SET_PARAM_NAME = "Choose DataSet";


  private DoubleParameter latParam;
  public String LAT_PARAM_NAME = "Latitude";

  private DoubleParameter lonParam;
  public String LON_PARAM_NAME = "Longitude";

  //HashTables for storing the metadata for each dataset
  Hashtable metaDataHash = new Hashtable();
  //Hashtable for storing the lons from each dataSet
  Hashtable lonHash= new Hashtable();
  //Hashtable for storing the lats from each dataSet
  Hashtable latHash= new Hashtable();

  //gets the selected Dataset
  private String selectedDataSet;

  public HazardDataSiteSelectionGuiBean() {
    loadDataSets();
    selectedDataSet = (String)dataSetParam.getValue();
    fillLatLon();
    parameterList = new ParameterList();
    parameterList.addParameter(dataSetParam);
    parameterList.addParameter(latParam);
    parameterList.addParameter(lonParam);
    editorPanel.removeAll();
    addParameters();
    setTitle(GUI_BEAN_TITLE);
    try {
      jbInit();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * sets up the connection with the servlet on the server (scec.usc.edu)
   */
  private ArrayList openConnection() {
    try{

      URL hazardMapViewerServlet = new URL(PLOTTER_CALC_SERVLET);
      URLConnection servletConnection = hazardMapViewerServlet.openConnection();

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream toServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());
      //sending the user which dataSet is selected
      toServlet.writeObject(selectedDataSet);
      //sending the min,max lats and lons and gridspacing to the servlet.
      toServlet.writeObject(latParam.getValue());
      toServlet.writeObject(lonParam.getValue());

      toServlet.flush();
      toServlet.close();

      // Receive the URL of the jpeg file from the servlet after it has received all the data
      ObjectInputStream fromServlet = new ObjectInputStream(servletConnection.getInputStream());

      ArrayList dataValues = (ArrayList)fromServlet.readObject();

      fromServlet.close();
     return dataValues;

    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
    return null;
   }



   public void parameterChange(ParameterChangeEvent e){
     String name = e.getParameterName();
     // if Data set param name is changed param
     if(name.equalsIgnoreCase(DATA_SET_PARAM_NAME)) {
       selectedDataSet = (String)dataSetParam.getValue();
       fillLatLon();
     }
   }

   /**
    * Load all the available data sets by checking the data sets directory
    */
   private void loadDataSets() {
     try{

       URL hazardMapViewerServlet = new URL(this.SERVLET_URL);

       URLConnection servletConnection = hazardMapViewerServlet.openConnection();

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

       // send the flag to servlet indicating to load the names of available datatsets
       outputToServlet.writeObject(org.scec.sha.gui.servlets.HazardMapViewerServlet.GET_DATA);

       outputToServlet.flush();
       outputToServlet.close();

       // Receive the "destroy" from the servlet after it has received all the data
       ObjectInputStream inputToServlet = new
           ObjectInputStream(servletConnection.getInputStream());

       metaDataHash=(Hashtable)inputToServlet.readObject();
       lonHash=(Hashtable)inputToServlet.readObject();
       latHash=(Hashtable)inputToServlet.readObject();

       inputToServlet.close();

     }catch (Exception e) {
       System.out.println("Exception in connection with servlet:" +e);
       e.printStackTrace();
     }

     // fill the combo box with available data sets
     Enumeration enum=metaDataHash.keys();
     ArrayList keys = new ArrayList();
     while(enum.hasMoreElements()) keys.add(enum.nextElement());
     Collections.sort(keys);
     dataSetParam = new StringParameter(DATA_SET_PARAM_NAME,keys,(String)keys.get(0));
     dataSetParam.addParameterChangeListener(this);
   }

   /**
    * It will read the sites.info file and fill the min and max Lat and Lon
    */
   private void fillLatLon() {

     // get the min and max lat and lat spacing
     String latitude=(String)latHash.get(selectedDataSet);
     StringTokenizer tokenizer = new StringTokenizer(latitude);
     double minLat = Double.parseDouble(tokenizer.nextToken());
     double maxLat = Double.parseDouble(tokenizer.nextToken());

     // line in LonHashTable contains the min lon, max lon, discretization interval
     String longitude = (String)lonHash.get(selectedDataSet);
     tokenizer = new StringTokenizer(longitude);
     double minLon = Double.parseDouble(tokenizer.nextToken());
     double maxLon = Double.parseDouble(tokenizer.nextToken());
     double intervalLon = Double.parseDouble(tokenizer.nextToken());

     // make the min and max lat param
     latParam = new DoubleParameter(LAT_PARAM_NAME,minLat,maxLat,new Double(minLat));
     lonParam = new DoubleParameter(LON_PARAM_NAME,minLon,maxLon,new Double(maxLon));
     if(parameterList !=null){
       parameterList = new ParameterList();
       parameterList.addParameter(dataSetParam);
       parameterList.addParameter(latParam);
       parameterList.addParameter(lonParam);
       editorPanel.removeAll();
       addParameters();
       editorPanel.validate();
       editorPanel.repaint();
       setTitle(GUI_BEAN_TITLE);
     }
   }

   /**
    *
    * @returns the selected dataset name
    */
   private String geSelectedDataSetName(){
     String name = "Selected DataSet: " +selectedDataSet+"\t";
     name +="Latitude: "+(Double)latParam.getValue()+"\t";
     name += "Longitude: "+(Double)lonParam.getValue();
     return name;
   }

   /**
    *
    * @return the selected dataset metadata
    */
   private String getMetadataForSelectedDataSet(){
     return (String)metaDataHash.get(selectedDataSet);
   }

   public ArbitrarilyDiscretizedFunc getChoosenFunction(){
     ArrayList dataValues = openConnection();
     ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
     int size = dataValues.size();
     for(int i=0;i<size;++i){
       StringTokenizer st = new StringTokenizer((String)dataValues.get(i));
       double xVal = Double.parseDouble(st.nextToken().trim());
       double yVal = Double.parseDouble(st.nextToken().trim());
       function.set(xVal,yVal);
     }
     function.setName(geSelectedDataSetName());
     //function.setInfo(getMetadataForSelectedDataSet());
     return function;
   }
}