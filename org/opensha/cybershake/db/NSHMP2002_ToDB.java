package org.opensha.cybershake.db;


import java.rmi.RemoteException;
import java.util.ListIterator;

import org.opensha.sha.earthquake.*;
import org.opensha.data.*;
import org.opensha.sha.surface.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient;
import org.opensha.data.region.CircularGeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.util.SystemPropertiesUtils;

import java.util.Iterator;

public class NSHMP2002_ToDB {

	private EqkRupForecast frankelForecast;
	private static String HOST_NAME = "intensity.usc.edu";
	private static String DATABASE_NAME = "CyberShake";
	private static final DBAccess db = new DBAccess(HOST_NAME,DATABASE_NAME);
	private ERF2DBAPI erf2db;
	public NSHMP2002_ToDB(DBAccess db){
		createFrankelForecast();
		erf2db = new ERF2DB(db);
	}
	 /**
	  * 
	  *
	  */
	  private void createFrankelForecast() {

	    //try {
			frankelForecast = new
			//Frankel02_AdjustableEqkRupForecastClient();
			Frankel02_AdjustableEqkRupForecast();
		//} catch (RemoteException e) {
			 //TODO Auto-generated catch block
		//	e.printStackTrace();
		//}

	    frankelForecast.getAdjustableParameterList().getParameter(
	        Frankel02_AdjustableEqkRupForecast.
	        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
	                                 BACK_SEIS_EXCLUDE);

	    frankelForecast.getAdjustableParameterList().getParameter(
	      Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_NAME).setValue(
	    		  Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_STIRLING);
	    frankelForecast.getAdjustableParameterList().getParameter(
	      Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_NAME).setValue(
	        new Double(5.0));
	    frankelForecast.getTimeSpan().setDuration(1.0);
	    frankelForecast.updateForecast();
	  }
	  
	  private void insertSrcRupInDB(){
		  int numSources = frankelForecast.getNumSources();
		  int erfId = this.getInsertedERF_Id(frankelForecast.getName());
		  for(int sourceId = 0;sourceId<numSources;++sourceId){
//			 get the ith source
		     ProbEqkSource source  = (ProbEqkSource)frankelForecast.getSource(sourceId);
		     int numRuptures = source.getNumRuptures();
		     String sourceName = source.getName();
		     for(int ruptureId=0;ruptureId<numRuptures;++ruptureId){
		    	 //getting the rupture on the source and its gridCentered Surface
		          ProbEqkRupture rupture = source.getRupture(ruptureId);
		          double mag = rupture.getMag();
		          double prob = rupture.getProbability();
		          double aveRake = rupture.getAveRake();
		          EvenlyGriddedSurfaceAPI rupSurface = new EvenlyGridCenteredSurface(rupture.getRuptureSurface());
//		        Local Strike for each grid centered location on the rupture
		          double[] localStrikeList = this.getLocalStrikeList(rupture.getRuptureSurface());
		          double dip =rupSurface.getAveDip();
		          int numRows = rupSurface.getNumRows();
		          int numCols = rupSurface.getNumCols();
		    	  int numPoints = numRows*numCols;
		    	  double gridSpacing = rupSurface.getGridSpacing();
		    	  Location surfaceStartLocation = (Location)rupSurface.get(0, 0);
		    	  Location surfaceEndLocation = (Location)rupSurface.get(0, numCols-1);
		    	  double surfaceStartLat = surfaceStartLocation.getLatitude();
		    	  double surfaceStartLon = surfaceStartLocation.getLongitude();
		    	  double surfaceStartDepth = surfaceStartLocation.getDepth();
		    	  double surfaceEndLat = surfaceEndLocation.getLatitude();
		    	  double surfaceEndLon = surfaceEndLocation.getLongitude();
		    	  double surfaceEndDepth = surfaceEndLocation.getDepth();
		    	  erf2db.insertERFRuptureInfo(erfId, sourceId, ruptureId, sourceName, null, 
		    			                      mag, prob, gridSpacing, 
		    			                      surfaceStartLat, surfaceStartLon, surfaceStartDepth, 
		    			                      surfaceEndLat, surfaceEndLon, surfaceEndDepth,
		    			                      numRows, numCols, numPoints);
		    	  for(int k=0;k<numRows;++k){
		    	      for (int j = 0; j < numCols; ++j) {
		    	        Location loc = rupSurface.getLocation(k,j);
		    	        erf2db.insertRuptureSurface(erfId, sourceId, ruptureId, loc.getLatitude(),
		    	        		                    loc.getLongitude(), loc.getDepth(), aveRake, dip,
		    	        		                    localStrikeList[j]);
		    	      }
		    	  }
		     }
		  }
	  }

	  
	  /**
	   * Returns the local strike list for a given rupture
	   * @param surface GriddedSurfaceAPI
	   * @return double[]
	   */
	  private double[] getLocalStrikeList(EvenlyGriddedSurfaceAPI surface){
	    int numCols = surface.getNumCols();
	    double[] localStrike = null;
	    //if it not a point surface, then get the Azimuth(strike) for 2 neighbouring
	    //horizontal locations on the rupture surface.
	    //if it is a point surface then it will be just having one location so
	    //in that we take the Ave. Strike for the Surface.
	    if(! (surface instanceof PointSurface)){
	      localStrike = new double[numCols - 1];
	      for (int i = 0; i < numCols - 1; ++i) {
	        Location loc1 = surface.getLocation(0, i);
	        Location loc2 = surface.getLocation(0, i + 1);
	        double strike = RelativeLocation.getAzimuth(loc1.getLatitude(),
	            loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());
	        localStrike[i] = strike;
	      }
	    }
	    else if(surface instanceof PointSurface) {
	      localStrike = new double[1];
	      localStrike[0]= surface.getAveStrike();
	    }

	    return localStrike;
	  }	  
	  
	  /**
	   * 
	   * @returns the ERF_ID of the corresponding ERF_Name in the database
	   */
	  public int getInsertedERF_Id(String erfName){
		  return erf2db.getInserted_ERF_ID(erfName);
	  }
	  
	  /**
	   * returns the instance of the last inserted ERF
	   * @return
	   */
	  public EqkRupForecastAPI getERF_Instance(){
		  return this.frankelForecast;
	  }
	  
	  public void insertForecaseInDB(){
		 int erfId = erf2db.insertERFId(frankelForecast.getName(), "NSHMP 2002 (Frankel02) Earthquake Rupture Forecast Model");
		  
		  ListIterator it = frankelForecast.getAdjustableParamsIterator();
		  //adding the forecast parameters
		  while(it.hasNext()){
			  ParameterAPI param = (ParameterAPI)it.next();
			  Object paramValue = param.getValue();
			  if(paramValue instanceof String)
				  paramValue = ((String)paramValue).replaceAll("'", "");
			  String paramType = param.getType();
			  paramType = paramType.replaceAll("Parameter", "");
			  erf2db.insertERFParams(erfId, param.getName(), paramValue.toString(), paramType,param.getUnits());
		  }
		  it = frankelForecast.getTimeSpan().getAdjustableParamsIterator();
		  //adding the timespan parameters
		  while(it.hasNext()){
			  ParameterAPI param = (ParameterAPI)it.next();
			  String paramType = param.getType();
			  paramType = paramType.replaceAll("Parameter", "");
			  erf2db.insertERFParams(erfId, param.getName(), param.getValue().toString(), paramType,param.getUnits());
		  }
		  //inserts the rupture information in the database
		  insertSrcRupInDB();
	  }
}
