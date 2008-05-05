package org.opensha.nshmp.sha.data;

import org.apache.commons.codec.binary.Base64;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.nshmp.exceptions.ZipCodeErrorException;
import org.opensha.nshmp.sha.calc.HazardDataCalc;
import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.nshmp.util.AppProperties;

/**
 * <p>Title:HazardDataMinerServletMode.java </p>
 * <p>Description: This class connects with the servlet to calulate the results
 * for EV's application. This is in contrast with HazardDataMiner class which
 * connects using the RMI</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class HazardDataMinerServletMode implements HazardDataMinerAPI {
	private final static String SERVLET_PATH = GlobalConstants.getServletPath();
  //private final static String SERVLET_PATH = "http://gldweb.cr.usgs.gov/GroundMotionTool/servlet/HazardCalcServlet";
  //private final static String SERVLET_PATH = "http://gldjanus.cr.usgs.gov/GroundMotionTool/servlet/HazardCalcServlet";
  public final static String COMPUTE_EXCEED_PROB = "computeExceedProb";
  public final static String COMPUTE_RETURN_PERIOD = "computeReturnPeriod";
  public final static String COMPUTE_HAZARD_CURVE = "computeHazardCurve";
  public final static String  COMPUTE_SS_S1= "computeSsS1";
  public final static String COMPUTE_SA = "computeSA";
  public final static String COMPUTE_SD_SS_S1 = "computeSDSsS1";
  public final static String COMPUTE_SM_SS_S1 = "computeSMSsS1";
  public final static String COMPUTE_SM_SPECTRUM = "computeSMSpectrum";
  public final static String COMPUTE_SD_SPECTRUM = "computeSDSpectrum";
  public final static String COMPUTE_MAP_SPECTRUM  = "computeMapSpectrum";
  public final static String COMPUTE_SM_UHS_SPECTRUM = "computeSM_UHSpectrum";
  public final static String COMPUTE_SD_UHS_SPECTRUM = "computeSD_UHSpectrum";
  public final static String COMPUTE_APPROX_UHS_SPECTRUM = "computeApproxUHSpectrum";

  public HazardDataMinerServletMode() {
  }
  /**
  *
  * @param hazardCurveFunction ArbitrarilyDiscretizedFunc
  * @param fex double Frequency of exceedance = 1/ReturnPd
  * @param expTime double
  * @return double
  */
 public double getExceedProb(double fex, double expTime)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(new Double(fex));
   objectList.add(new Double(expTime));
   Double result = (Double)connectToServlet(HazardDataMinerServletMode.COMPUTE_EXCEED_PROB, objectList);
   return result.doubleValue();
 }

 /**
  *
  * @param exceedProb double
  * @param expTime double
  * @return double
  */
 public double getReturnPeriod(double exceedProb, double expTime) {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(new Double(exceedProb));
   objectList.add(new Double(expTime));
   Double result = (Double)connectToServlet(HazardDataMinerServletMode.COMPUTE_RETURN_PERIOD, objectList);
   return result.doubleValue();
 }

 /**
  * Gets the Basic Hazard Curve using the Lat and Lon
  * @param geographicRegion String
  * @param dataEdition String
  * @param lat double
  * @param lon double
  * @return ArbitrarilyDiscretizedFunc
  */
 public ArbitrarilyDiscretizedFunc getBasicHazardcurve(String geographicRegion,
     String dataEdition, double lat, double lon, String hazCurveType)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(new Double(lat));
   objectList.add(new Double(lon));
   objectList.add(hazCurveType);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_HAZARD_CURVE, objectList);
 }

 /**
  * Gets the Basic Hazard Curve using the Lat and Lon
  * @param geographicRegion String
  * @param dataEdition String
  * @param zipCode String
  * @return DiscretizedFuncList
  * @throws ZipCodeErrorException
  */
 public ArbitrarilyDiscretizedFunc getBasicHazardcurve(String geographicRegion,
     String dataEdition, String zipCode,
     String hazCurveType) throws
     ZipCodeErrorException {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(zipCode);
   objectList.add(hazCurveType);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_HAZARD_CURVE, objectList);
 }

 /**
  * Gets the Ss and S1 when location is provided using the Lat and Lon
  * @param geographicRegion String
  * @param dataEdition String
  * @param lat double
  * @param lon double
  * @return ArbitrarilyDiscretizedFunc
  */
 public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                           String dataEdition, double lat,
                                           double lon)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(new Double(lat));
   objectList.add(new Double(lon));
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SS_S1, objectList);
 }

 /**
  *
  * @param geographicRegion String
  * @param dataEdition String
  * @param lat double
  * @param lon double
  * @param selectedSpectraType String
  * @return ArbitrarilyDiscretizedFunc
  */
 public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                           String dataEdition, double lat,
                                           double lon,
                                           String selectedSpectraType) {

   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(new Double(lat));
   objectList.add(new Double(lon));
   objectList.add(selectedSpectraType);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SS_S1, objectList);

 }

 /**
  *
  * @param geographicRegion String
  * @param dataEdition String
  * @param lat double
  * @param lon double
  * @param selectedSpectraType String
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSA(String geographicRegion,
                                  String dataEdition, double lat,
                                  double lon, String selectedSpectraType) {

   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(new Double(lat));
   objectList.add(new Double(lon));
   objectList.add(selectedSpectraType);
   return (DiscretizedFuncList)this.connectToServlet(HazardDataMinerServletMode.COMPUTE_SA, objectList);
 }

 /**
  * Gets the Ss and S1 when location is provided using the zipCode
  * @param geographicRegion String
  * @param dataEdition String
  * @param zipCode String
  * @return DiscretizedFuncList
  * @throws ZipCodeErrorException
  */
 public DiscretizedFuncList getSA(String geographicRegion,
                                  String dataEdition, String zipCode,
                                  String spectraType) throws ZipCodeErrorException {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(zipCode);
   objectList.add(spectraType);
   return (DiscretizedFuncList)this.connectToServlet(HazardDataMinerServletMode.COMPUTE_SA, objectList);
 }

 /**
  * Gets the Ss and S1 when location is provided using the zipCode
  * @param geographicRegion String
  * @param dataEdition String
  * @param zipCode String
  * @return DiscretizedFuncList
  * @throws ZipCodeErrorException
  */
 public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                           String dataEdition, String zipCode,
                                           String spectraType) throws
     ZipCodeErrorException {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(zipCode);
   objectList.add(spectraType);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SS_S1, objectList);

 }

 /**
  * Gets the Ss and S1 when location is provided using the zipCode
  * @param geographicRegion String
  * @param dataEdition String
  * @param zipCode String
  * @return DiscretizedFuncList
  * @throws ZipCodeErrorException
  */
 public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion,
                                           String dataEdition, String zipCode) throws
     ZipCodeErrorException {

   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   objectList.add(dataEdition);
   objectList.add(zipCode);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SS_S1, objectList);
 }

 /**
  * Gets the Ss and S1 when geographic region provided is  a territory.
  * @param geographicRegion String
  * @return ArbitrarilyDiscretizedFunc
  */
 public ArbitrarilyDiscretizedFunc getSsS1(String geographicRegion){

   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(geographicRegion);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SS_S1, objectList);

 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return ArbitrarilyDiscretizedFunc
  */
 public ArbitrarilyDiscretizedFunc getSDSsS1(ArbitrarilyDiscretizedFunc func,
                                             float fa, float fv,
                                             String siteClass)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   objectList.add(new Float(fa));
   objectList.add(new Float(fv));
   objectList.add(siteClass);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SD_SS_S1, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return ArbitrarilyDiscretizedFunc
  */
 public ArbitrarilyDiscretizedFunc getSMSsS1(ArbitrarilyDiscretizedFunc func,
                                             float fa, float fv,
                                             String siteClass)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   objectList.add(new Float(fa));
   objectList.add(new Float(fv));
   objectList.add(siteClass);
   return (ArbitrarilyDiscretizedFunc)connectToServlet(HazardDataMinerServletMode.COMPUTE_SM_SS_S1, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSMSpectrum(ArbitrarilyDiscretizedFunc func,
                                          float fa, float fv, String siteClass) {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   objectList.add(new Float(fa));
   objectList.add(new Float(fv));
   objectList.add(siteClass);
   return (DiscretizedFuncList)connectToServlet(HazardDataMinerServletMode.COMPUTE_SM_SPECTRUM, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSDSpectrum(ArbitrarilyDiscretizedFunc func,
                                          float fa, float fv, String siteClass)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   objectList.add(new Float(fa));
   objectList.add(new Float(fv));
   objectList.add(siteClass);
   return (DiscretizedFuncList)connectToServlet(HazardDataMinerServletMode.COMPUTE_SD_SPECTRUM, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getMapSpectrum(ArbitrarilyDiscretizedFunc func) {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   return (DiscretizedFuncList)connectToServlet(HazardDataMinerServletMode.COMPUTE_MAP_SPECTRUM, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSM_UHSpectrum(ArbitrarilyDiscretizedFunc func,
                                             float fa, float fv,
                                             String siteClass)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   objectList.add(new Float(fa));
   objectList.add(func);
   objectList.add(func);
   return (DiscretizedFuncList)connectToServlet(HazardDataMinerServletMode.COMPUTE_SM_UHS_SPECTRUM, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @param fa double
  * @param fv double
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getSD_UHSpectrum(ArbitrarilyDiscretizedFunc func,
                                             float fa, float fv,
                                             String siteClass)  {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   objectList.add(new Float(fa));
   objectList.add(new Float(fv));
   objectList.add(siteClass);
   return (DiscretizedFuncList)connectToServlet(HazardDataMinerServletMode.COMPUTE_SD_UHS_SPECTRUM, objectList);
 }

 /**
  *
  * @param func ArbitrarilyDiscretizedFunc
  * @return DiscretizedFuncList
  */
 public DiscretizedFuncList getApprox_UHSpectrum(ArbitrarilyDiscretizedFunc
                                                 func) {
   ArrayList<Object> objectList = new ArrayList<Object>();
   objectList.add(func);
   return (DiscretizedFuncList)connectToServlet(HazardDataMinerServletMode.COMPUTE_APPROX_UHS_SPECTRUM, objectList);
 }

 /**
  * Connect with the servlet and get the results
  * @param funcName
  * @param objectList
  * @return
  */
 private Object connectToServlet(String funcName, ArrayList objectList) {
	 try {
     // make connection with servlet
     URL hazCalcServlet = new URL(HazardDataMinerServletMode.SERVLET_PATH);
     URLConnection servletConnection = hazCalcServlet.openConnection();

     servletConnection.setDoOutput(true);

     // Don't use a cached version of URL connection.
     servletConnection.setUseCaches (false);
     servletConnection.setDefaultUseCaches (false);

     // Specify the content type that we will send binary data
     servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

     // Optionally use authentication.
     // At this time, user must specify authentication manually in config file.
     	if(AppProperties.getProperty("useAuth") != null) {
     		String username = AppProperties.getProperty("username");
     		String password = AppProperties.getProperty("password");
     		
     		String asciiAuth = username + ":" + password;
     	
     		System.err.println("Setting proxy authentication: " + asciiAuth);
     		
     		String enc64Auth = new String(
     				Base64.encodeBase64(asciiAuth.getBytes())
     			);
     		
     		servletConnection.setRequestProperty("Proxy-Authorization", "Basic " +
     				enc64Auth);
     	}
     	
	  // Modify the funcName to notify server that you have current version
	  funcName = funcName + "_V8";

	  System.out.println("Func is: " + funcName);
	  System.out.println("Object is: " + objectList);
	  
     // send the student object to the servlet using serialization
     ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

     outputToServlet.writeObject(funcName);
     outputToServlet.writeObject(objectList);

     outputToServlet.flush();
     outputToServlet.close();

     // now read the connection again to get the vs30 as sent by the servlet
     ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
     //ArrayList of Wills Site Class Values translated from the Vs30 Values.
     Object obj =  ois.readObject();
     ois.close();
     return obj;
   }catch(Exception e) {
     e.printStackTrace();
   }
   return null;
 }

}
