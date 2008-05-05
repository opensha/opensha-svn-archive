package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.calc.RelativeLocation;
import org.opensha.data.Location;
import org.opensha.data.region.CircularGeographicRegion;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.surface.EvenlyGridCenteredSurface;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.surface.PointSurface;

public  class ERF2DB implements ERF2DBAPI{
	
	protected EqkRupForecast eqkRupForecast;
	private DBAccess dbaccess;
	
	public ERF2DB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	/**
	 * Inserts ERF Parameters info in the "ERF_Metadata"
	 * @param erfId
	 * @param attrName
	 * @param attrVal
	 */
	public void insertERFParams(int erfId, String attrName, String attrVal, String attrType,String attrUnits) {
		
		//generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into ERF_Metadata" +
		    "(ERF_ID,ERF_Attr_Name,ERF_Attr_Value,ERF_Attr_Type,ERF_Attr_Units)"+
			"VALUES('"+erfId+"','"+attrName+"','"+
		             attrVal+"','"+attrType+"','"+attrUnits+"')";
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	/**
	 * Inserts source rupture information for the ERF in table "Ruptures"
	 * @param erfName
	 * @param sourceId
	 * @param ruptureId
	 * @param sourceName
	 * @param sourcetype
	 * @param magnitude
	 * @param probability
	 * @param gridSpacing
	 * @param numRows
	 * @param numCols
	 * @param numPoints
	 */
	public void insertERFRuptureInfo(int erfId, int sourceId, int ruptureId, 
			                        String sourceName, String sourceType, double magnitude, 
			                        double probability, double gridSpacing, double surfaceStartLat, 
			                        double surfaceStartLon, double surfaceStartDepth,
			                        double surfaceEndLat, double surfaceEndLon,double surfaceEndDepth, 
			                        int numRows, int numCols, int numPoints) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into Ruptures" +
		             "(ERF_ID,Source_ID,Rupture_ID,Source_Name,Source_Type,Mag,Prob,"+
		             "Grid_Spacing,Num_Rows,Num_Columns,Num_Points,Start_Lat,Start_Lon,"+
		             "Start_Depth,End_Lat,End_Lon,End_Depth)"+
		            "VALUES('"+erfId+"','"+sourceId+"','"+
		             ruptureId+"','"+sourceName+"','"+sourceType+"','"+(float)magnitude+"','"+
		             (float)probability+"','"+(float)gridSpacing+"','"+numRows+"','"+numCols+
		             "','"+numPoints+"','"+(float)surfaceStartLat+"','"+(float)surfaceStartLon+"','"+(float)surfaceStartDepth+
		             "','"+(float)surfaceEndLat+"','"+(float)surfaceEndLon+"','"+(float)surfaceEndDepth+"')";
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Inserts surface locations information for each rupture in table "Points"
	 * @param erfName
	 * @param sourceId
	 * @param ruptureId
	 * @param lat
	 * @param lon
	 * @param depth
	 * @param rake
	 * @param dip
	 * @param strike
	 */
	public void insertRuptureSurface(int erfId, int sourceId, int ruptureId, 
			                         double lat, double lon, double depth, double rake, 
			                         double dip, double strike) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into Points"+ 
		             "(ERF_ID,Source_ID,Rupture_ID,Lat,Lon,Depth,Rake,Dip,Strike)"+
		            "VALUES('"+erfId+"','"+sourceId+"','"+
		             ruptureId+"','"+(float)lat+"','"+(float)lon+"','"+(float)depth+"','"+
		             (float)rake+"','"+(float)dip+"','"+(float)strike+"')";
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Inserts surface locations information for each rupture in table "Points"
	 * @param erfName
	 * @param sourceId
	 * @param ruptureId
	 * @param lat
	 * @param lon
	 * @param depth
	 * @param rake
	 * @param dip
	 * @param strike
	 */
	public void insertRuptureSurface(ArrayList<Integer> erfId, ArrayList<Integer> sourceId, ArrayList<Integer> ruptureId, 
			ArrayList<Double> lat, ArrayList<Double> lon, ArrayList<Double> depth, ArrayList<Double> rake, 
			ArrayList<Double> dip, ArrayList<Double> strike) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into Points"+ 
		             "(ERF_ID,Source_ID,Rupture_ID,Lat,Lon,Depth,Rake,Dip,Strike)";
		int size = erfId.size();
		sql += " VALUES";
		for (int i=0; i<size; i++) {
			sql += "('"+erfId.get(i)+"','"+sourceId.get(i)+"','"+
            ruptureId.get(i)+"','"+lat.get(i).floatValue()+"','"+lon.get(i).floatValue()+"','"+depth.get(i).floatValue()+"','"+
            rake.get(i).floatValue()+"','"+dip.get(i).floatValue()+"','"+strike.get(i).floatValue()+"') ";
			if ((i + 1) == size) { // this is the last one, no comma at end
				
			} else {
				sql += ",";
			}
		}
		
//		System.out.println(sql);
		            
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * Inserts ERF name and description in table ERF_IDs
	 * @param erfName
	 * @param erfDesc
	 * @return Autoincremented Id from the table for the last inserted ERF
	 */
	public int insertERFId(String erfName, String erfDesc) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into ERF_IDs"+ 
		             "(ERF_Name,ERF_Description)"+
		              "VALUES('"+erfName+"','"+erfDesc+"')";
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return getInserted_ERF_ID(erfName);
		
	}

	/**
	 * Retrives the id of the ERF from the table ERF_IDs  for the corresponding ERF_Name.
	 * @param erfName
	 * @return
	 */
	public int getInserted_ERF_ID(String erfName){
		 String sql = "SELECT ERF_ID from ERF_IDs WHERE ERF_Name = "+"'"+erfName+"'";
		 ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		 
			try {
				rs.first();
				String erfId = rs.getString("ERF_ID");
				rs.close();
				return Integer.parseInt(erfId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return -1;
	}

	/**
	 * Retrives the rupture probability
	 * @param erfId
	 * @param sourceId
	 * @param rupId
	 * @return
	 * @throws SQLException 
	 */
	public double getRuptureProb(int erfId,int sourceId,int rupId) {
		String sql = "SELECT Prob from Ruptures WHERE ERF_ID = "+"'"+erfId+"' and "+
		             "Source_ID = '"+sourceId+"' and Rupture_ID = '"+rupId+"'";
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		double rupProb = Double.NaN;
		try{
			rs.first();
			rupProb = Double.parseDouble(rs.getString("Prob"));
			rs.close();
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
	 return rupProb;
	}
	
	public void insertSrcRupInDB(){
		this.insertSrcRupInDB(null, 0, 0);
	}
	
	private boolean isInsideCutoffForRegion(EvenlyGriddedGeographicRegion region, ProbEqkRupture rupture) {
		
		long startTime = System.currentTimeMillis();
		
		CircularGeographicRegion circular;
		
		EvenlyGriddedSurfaceAPI rupSurface = new EvenlyGridCenteredSurface(rupture.getRuptureSurface());
		
		ListIterator it = rupSurface.getAllByRowsIterator();
		
		int numLocs = region.getNumGridLocs();
		
		while (it.hasNext()) {
			Location ptLoc = (Location) it.next();
			
			for (int i=0; i<numLocs; i++) {
				Location loc = region.getGridLocation(i);
				circular = new CircularGeographicRegion(loc, CybershakeSiteInfo2DB.CUT_OFF_DISTANCE);
				
				if (circular.isLocationInside(ptLoc)) {
					System.out.println("Took " + ((System.currentTimeMillis() - startTime) / 1000d) + " secs to FIND...inserting rupture...");
					return true;
				}
			}
		}
		System.out.println("Took " + ((System.currentTimeMillis() - startTime) / 1000d) + " secs to NOT FIND");
		return false;
	}
	
	  public void insertSrcRupInDB(EvenlyGriddedGeographicRegion region, int startSource, int startRup){
		  int numSources = eqkRupForecast.getNumSources();
		  int erfId = this.getInserted_ERF_ID(eqkRupForecast.getName());
		  
		  // surface objects
		  ArrayList<Integer> erfIds;
    	  ArrayList<Integer> sourceIds;
    	  ArrayList<Integer> ruptureIds; 
    	  ArrayList<Double> lats;
    	  ArrayList<Double> lons;
    	  ArrayList<Double> depths;
    	  ArrayList<Double> rakes; 
    	  ArrayList<Double> dips;
    	  ArrayList<Double> strikes;
		  
		  boolean forRegion = (region != null);
		  for(int sourceId = 0;sourceId<numSources;++sourceId){
			  if (sourceId < startSource)
				  continue;
//			 get the ith source
		     ProbEqkSource source  = (ProbEqkSource)eqkRupForecast.getSource(sourceId);
		     int numRuptures = source.getNumRuptures();
		     System.out.println("Insert source "+(sourceId+1)+" of "+numSources + " (" + numRuptures + " rups)");
		     String sourceName = source.getName();
		     for(int ruptureId=0;ruptureId<numRuptures;++ruptureId){
		    	 if (ruptureId < startRup)
		    		 continue;
		    	 else
		    		 startRup = 0;
		    	 System.out.println("Inserting rupture " + (ruptureId+1) + " of " + numRuptures);
		    	 //getting the rupture on the source and its gridCentered Surface
		          ProbEqkRupture rupture = source.getRupture(ruptureId);
		          
		          if (forRegion) {
			    	 if (!this.isInsideCutoffForRegion(region, rupture))
			    		 continue;
		          }
		          
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
		    	  insertERFRuptureInfo(erfId, sourceId, ruptureId, sourceName, null, 
		    			                      mag, prob, gridSpacing, 
		    			                      surfaceStartLat, surfaceStartLon, surfaceStartDepth, 
		    			                      surfaceEndLat, surfaceEndLon, surfaceEndDepth,
		    			                      numRows, numCols, numPoints);
		    	  System.out.println("Inserting Surface...");
		    	  long startTime = System.currentTimeMillis();
		    	  erfIds = new ArrayList<Integer>();
		    	  sourceIds = new ArrayList<Integer>();
		    	  ruptureIds = new ArrayList<Integer>(); 
		    	  lats = new ArrayList<Double>();
		    	  lons = new ArrayList<Double>();
		    	  depths = new ArrayList<Double>();
		    	  rakes = new ArrayList<Double>(); 
		    	  dips = new ArrayList<Double>();
		    	  strikes = new ArrayList<Double>();
		    	  for(int k=0;k<numRows;++k){
		    	      for (int j = 0; j < numCols; ++j) {
		    	        Location loc = rupSurface.getLocation(k,j);
		    	        erfIds.add(erfId);
		    	        sourceIds.add(sourceId);
		    	        ruptureIds.add(ruptureId);
		    	        lats.add(loc.getLatitude());
		    	        lons.add(loc.getLongitude());
		    	        depths.add(loc.getDepth());
		    	        rakes.add(aveRake);
		    	        dips.add(dip);
		    	        strikes.add(localStrikeList[j]);
		    	      }
		    	  }
		    	  insertRuptureSurface(erfIds, sourceIds, ruptureIds, lats, lons, depths, rakes, dips, strikes);
		    	  System.out.println("Inserted! (" + ((System.currentTimeMillis() - startTime) / 1000d) + " sec for surface)");
		     }
		  }
	  }
	  
	/**
	 * Insert the specified rupture from the given forecast
	 * @param forecast
	 * @param erfID
	 * @param sourceID
	 * @param rupID
	 */
	public void insertSrcRupInDB(EqkRupForecastAPI forecast, int erfID, int sourceID, int rupID) {
		ProbEqkSource source  = (ProbEqkSource)forecast.getSource(sourceID);
		String sourceName = source.getName();
		// getting the rupture on the source and its gridCentered Surface
		ProbEqkRupture rupture = source.getRupture(rupID);
		double mag = rupture.getMag();
		double prob = rupture.getProbability();
		double aveRake = rupture.getAveRake();
		EvenlyGriddedSurfaceAPI rupSurface = new EvenlyGridCenteredSurface(
				rupture.getRuptureSurface());
		// Local Strike for each grid centered location on the rupture
		double[] localStrikeList = this.getLocalStrikeList(rupture
				.getRuptureSurface());
		double dip = rupSurface.getAveDip();
		int numRows = rupSurface.getNumRows();
		int numCols = rupSurface.getNumCols();
		int numPoints = numRows * numCols;
		double gridSpacing = rupSurface.getGridSpacing();
		Location surfaceStartLocation = (Location) rupSurface.get(0, 0);
		Location surfaceEndLocation = (Location) rupSurface.get(0, numCols - 1);
		double surfaceStartLat = surfaceStartLocation.getLatitude();
		double surfaceStartLon = surfaceStartLocation.getLongitude();
		double surfaceStartDepth = surfaceStartLocation.getDepth();
		double surfaceEndLat = surfaceEndLocation.getLatitude();
		double surfaceEndLon = surfaceEndLocation.getLongitude();
		double surfaceEndDepth = surfaceEndLocation.getDepth();
		System.out.println("Inserting rupture into database...");
		insertERFRuptureInfo(erfID, sourceID, rupID, sourceName, null, mag,
				prob, gridSpacing, surfaceStartLat, surfaceStartLon,
				surfaceStartDepth, surfaceEndLat, surfaceEndLon,
				surfaceEndDepth, numRows, numCols, numPoints);
		System.out.println("Inserting rupture surface points into database...");
		for (int k = 0; k < numRows; ++k) {
			for (int j = 0; j < numCols; ++j) {
				Location loc = rupSurface.getLocation(k, j);
				insertRuptureSurface(erfID, sourceID, rupID, loc
						.getLatitude(), loc.getLongitude(), loc.getDepth(),
						aveRake, dip, localStrikeList[j]);
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
	  
	  public void insertForecaseInDB(String erfDescription, EvenlyGriddedGeographicRegion region){
			 int erfId = insertERFId(eqkRupForecast.getName(), erfDescription);
			  
			  ListIterator it = eqkRupForecast.getAdjustableParamsIterator();
			  //adding the forecast parameters
			  while(it.hasNext()){
				  ParameterAPI param = (ParameterAPI)it.next();
				  Object paramValue = param.getValue();
				  if(paramValue instanceof String)
					  paramValue = ((String)paramValue).replaceAll("'", "");
				  String paramType = param.getType();
				  paramType = paramType.replaceAll("Parameter", "");
				  insertERFParams(erfId, param.getName(), paramValue.toString(), paramType,param.getUnits());
			  }
			  it = eqkRupForecast.getTimeSpan().getAdjustableParamsIterator();
			  //adding the timespan parameters
			  while(it.hasNext()){
				  ParameterAPI param = (ParameterAPI)it.next();
				  String paramType = param.getType();
				  paramType = paramType.replaceAll("Parameter", "");
				  insertERFParams(erfId, param.getName(), param.getValue().toString(), paramType,param.getUnits());
			  }
			  //inserts the rupture information in the database
			  insertSrcRupInDB(region, 0, 0);
		  }
	  
	  public void insertForecaseInDB(String erfDescription){
			 this.insertForecaseInDB(erfDescription, null);
		  }
	  
	  /**
	   * returns the instance of the last inserted ERF
	   * @return
	   */
	  public EqkRupForecastAPI getERF_Instance(){
		  return this.eqkRupForecast;
	  }
	
}
