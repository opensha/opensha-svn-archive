package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.opensha.data.DataPoint2D;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;

public class HazardCurve2DB {

	
	private DBAccess dbaccess;
	
	public HazardCurve2DB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	public ArrayList<Integer> getAllHazardCurveIDs(int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		ArrayList<Integer> erfIDs = new ArrayList<Integer>();
		erfIDs.add(erfID);
		
		return this.getAllHazardCurveIDs(erfIDs, rupVarScenarioID, sgtVarID, imTypeID);
	}
	
	public ArrayList<CybershakeHazardCurveRecord> getHazardCurveRecordsForSite(int siteID) {
		return getAllHazardCurveRecords("Site_ID=" + siteID);
	}
	
	public ArrayList<CybershakeHazardCurveRecord> getAllHazardCurveRecords() {
		return getAllHazardCurveRecords(null);
	}
	
	private ArrayList<CybershakeHazardCurveRecord> getAllHazardCurveRecords(String whereClause) {
		
		String sql = "SELECT * FROM Hazard_Curves";
		
		if (whereClause != null && whereClause.length() > 0) {
			sql += " WHERE " + whereClause;
		}
		
//		System.out.println(sql);
		
		ArrayList<CybershakeHazardCurveRecord> curves = new ArrayList<CybershakeHazardCurveRecord>();
		
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		try {
			rs.first();
			while (!rs.isAfterLast()) {
				int curveID = rs.getInt("Hazard_Curve_ID");
				int siteID = rs.getInt("Site_ID");
				int erfID = rs.getInt("ERF_ID");
				int rupVarScenID = rs.getInt("Rup_Var_Scenario_ID");
				int sgtVarID = rs.getInt("SGT_Variation_ID");
				int imTypeID = rs.getInt("IM_Type_ID");
				Date date = rs.getDate("Curve_Date");
				
				curves.add(new CybershakeHazardCurveRecord(curveID, siteID, erfID, rupVarScenID, sgtVarID, imTypeID, date));
				
				rs.next();
			}
			rs.close();
			
			return curves;
		} catch (SQLException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<Integer> getAllHazardCurveIDs(ArrayList<Integer> erfIDs, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		
		String sql = "SELECT Hazard_Curve_ID FROM Hazard_Curves WHERE ERF_ID in (";
		boolean first = true;
		for (int erfID : erfIDs) {
			if (first)
				first = false;
			else
				sql += ",";
			sql += erfID;
		}
		sql += 	") AND Rup_Var_Scenario_ID=" + rupVarScenarioID + " AND SGT_Variation_ID=" + sgtVarID + 
					" AND IM_Type_ID=" + imTypeID + " ORDER BY Curve_Date desc";
		
//		System.out.println(sql);
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		try {
			rs.first();
			while (!rs.isAfterLast()) {
				int id = rs.getInt("Hazard_Curve_ID");
				boolean skip = false;
				for (int oldID : ids) {
					if (oldID == id) {
						// this means that it's a duplicate and the newest one is already in there
						skip = true;
						break;
					}
				}
				if (!skip)
					ids.add(id);
				rs.next();
			}
			rs.close();
			
			return ids;
		} catch (SQLException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<Integer> getAllHazardCurveIDsForSite(int siteID, int erfID, int rupVarScenarioID, int sgtVarID) {
		
		String sql = "SELECT Hazard_Curve_ID FROM Hazard_Curves WHERE ERF_ID=" + erfID;
		sql += 	" AND Rup_Var_Scenario_ID=" + rupVarScenarioID + " AND SGT_Variation_ID=" + sgtVarID + 
					" AND Site_ID=" + siteID + " ORDER BY Curve_Date desc";
		
//		System.out.println(sql);
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		try {
			rs.first();
			while (!rs.isAfterLast()) {
				int id = rs.getInt("Hazard_Curve_ID");
				boolean skip = false;
				for (int oldID : ids) {
					if (oldID == id) {
						// this means that it's a duplicate and the newest one is already in there
						skip = true;
						break;
					}
				}
				if (!skip)
					ids.add(id);
				rs.next();
			}
			rs.close();
			
			return ids;
		} catch (SQLException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public int getHazardCurveID(int siteID, int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		
		String sql = "SELECT Hazard_Curve_ID FROM Hazard_Curves WHERE Site_ID=" + siteID + " AND ERF_ID=" + erfID + 
					" AND Rup_Var_Scenario_ID=" + rupVarScenarioID + " AND SGT_Variation_ID=" + sgtVarID + 
					" AND IM_Type_ID=" + imTypeID + " ORDER BY Curve_Date";
		
//		System.out.println(sql);
		
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		}
		
		try {
			rs.first();
			if (rs.isAfterLast())
				return -1;
			int id = rs.getInt("Hazard_Curve_ID");
			rs.close();
			
			return id;
		} catch (SQLException e) {
//			e.printStackTrace();
			return -1;
		}
	}
	
	public int getSiteIDFromCurveID(int hcID) {
		String sql = "SELECT Site_ID FROM Hazard_Curves WHERE Hazard_Curve_ID=" + hcID;

//		System.out.println(sql);

		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
//			TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		}

		try {
			rs.first();
			if (rs.isAfterLast())
				return -1;
			int id = rs.getInt("Site_ID");
			rs.close();

			return id;
		} catch (SQLException e) {
//			e.printStackTrace();
			return -1;
		}
	}
	
	public Date getDateForCurve(int hcID) {
		String sql = "SELECT Curve_Date FROM Hazard_Curves WHERE Hazard_Curve_ID=" + hcID;

//		System.out.println(sql);

		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
//			TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		try {
			rs.first();
			if (rs.isAfterLast())
				return null;
			Date date = rs.getDate("Curve_Date");
			rs.close();

			return date;
		} catch (SQLException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public DiscretizedFuncAPI getHazardCurve(int id) {
		DiscretizedFuncAPI hazardFunc = new ArbitrarilyDiscretizedFunc();
		
		String sql = "SELECT X_Value, Y_Value FROM Hazard_Curve_Points WHERE Hazard_Curve_ID=" + id + 
						" ORDER BY X_Value";
//		System.out.println(sql);

		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			rs.first();
			while (!rs.isAfterLast()) {
				double x = rs.getDouble("X_Value");
				double y = rs.getDouble("Y_Value");
				hazardFunc.set(x, y);
				rs.next();
			}
			rs.close();
			return hazardFunc;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void insertHazardCurve(int siteID, int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID, DiscretizedFuncAPI hazardFunc) {
		int id = this.insertHazardCurveID(siteID, erfID, rupVarScenarioID, sgtVarID, imTypeID);
		this.insertHazardCurvePoints(id, hazardFunc);
	}
	
	public boolean deleteHazardCurve(int curveID) {
		int ptRows = deleteHazardCurvePoints(curveID);
		int idRows = deleteHazardCurveID(curveID);
		
		return ptRows > 0 || idRows > 0;
	}
	
	public int deleteHazardCurveID(int curveID) {
		String sql = "DELETE FROM Hazard_Curves WHERE Hazard_Curve_ID=" + curveID;
		System.out.println(sql);
		try {
			return dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
//			TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	public int deleteHazardCurvePoints(int curveID) {
		String sql = "DELETE FROM Hazard_Curve_Points WHERE Hazard_Curve_ID=" + curveID;
		System.out.println(sql);
		try {
			return dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
//			TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	public void replaceHazardCurve(int curveID, DiscretizedFuncAPI hazardFunc) {
		this.deleteHazardCurvePoints(curveID);
		
		this.insertHazardCurvePoints(curveID, hazardFunc);
		
		// update the curve date
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(now);
		
		String sql = "UPDATE Hazard_Curves SET Curve_Date='" + date + "' WHERE Hazard_Curve_ID="+curveID;
		System.out.println(sql);
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
//			TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	private int insertHazardCurveID(int siteID, int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(now);
		
		String sql = "INSERT into Hazard_Curves"+ 
		"(Site_ID,ERF_ID,Rup_Var_Scenario_ID,SGT_Variation_ID,IM_Type_ID,Curve_Date)"+
		"VALUES("+siteID+","+erfID+","+rupVarScenarioID+","+sgtVarID+","+imTypeID+",'"+date+"')";
		System.out.println(sql);
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
//			TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		return this.getHazardCurveID(siteID, erfID, rupVarScenarioID, sgtVarID, imTypeID);
	}
	
	private void insertHazardCurvePoints(int id, DiscretizedFuncAPI hazardFunc) {
		String sql = "INSERT into Hazard_Curve_Points "+ 
				"(Hazard_Curve_ID,X_Value,Y_Value) "+
				"VALUES";
		int numPoints = hazardFunc.getNum();
		for (int i=0; i<numPoints; i++) {
			DataPoint2D pt = hazardFunc.get(i);
			sql += " (" + id + "," + pt.getX() + "," + pt.getY() + ")";
			if (i < numPoints -1)
				sql += ",";
		}
		System.out.println(sql);
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
//			TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getIMTypeIDForCurve(int curveID) {
		String sql = "SELECT IM_Type_ID FROM Hazard_Curves WHERE Hazard_Curve_ID=" + curveID;

//		System.out.println(sql);

		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
//			TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		}

		try {
			rs.first();
			if (rs.isAfterLast())
				return -1;
			int id = rs.getInt("IM_Type_ID");
			rs.close();

			return id;
		} catch (SQLException e) {
//			e.printStackTrace();
			return -1;
		}
	}
	
	public CybershakeIM getIMForCurve(int curveID) {
		int imTypeID = getIMTypeIDForCurve(curveID);
		
		return this.getIMFromID(imTypeID);
	}
	
	public CybershakeIM getIMFromID(int imTypeID) {
		String sql = "SELECT IM_Type_ID, IM_Type_Measure, IM_Type_Value, Units FROM IM_Types WHERE IM_Type_ID=" + imTypeID;

//		System.out.println(sql);

		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
//			TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		try {
			rs.first();
			if (rs.isAfterLast())
				return null;
			int id = rs.getInt("IM_Type_ID");
			String measure = rs.getString("IM_Type_Measure");
			Double value = rs.getDouble("IM_Type_Value");
			String units = rs.getString("Units");
			CybershakeIM im = new CybershakeIM(id, measure, value, units);
			
			rs.close();

			return im;
		} catch (SQLException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<CybershakeIM>  getSupportedIMs(int siteID, int erfID, int rupVarID, int sgtVariation) {
		String whereClause = "Site_ID="+siteID + " AND ERF_ID="+erfID + " AND SGT_Variation_ID="+sgtVariation + 
			" AND Rup_Var_Scenario_ID="+rupVarID;
		long startTime = System.currentTimeMillis();
		String sql = "SELECT I.IM_Type_ID,I.IM_Type_Measure,I.IM_Type_Value,I.Units from IM_Types I JOIN (";
		sql += "SELECT distinct IM_Type_ID from Hazard_Curves WHERE " + whereClause;
		sql += ") A ON A.IM_Type_ID=I.IM_Type_ID";
		
//		System.out.println(sql);
		
//		System.out.println(sql);
		ArrayList<CybershakeIM> ims = new ArrayList<CybershakeIM>();
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rs.first();
			while(!rs.isAfterLast()){
				int id = rs.getInt("IM_Type_ID");
				String measure = rs.getString("IM_Type_Measure");
				Double value = rs.getDouble("IM_Type_Value");
				String units = rs.getString("Units");
				CybershakeIM im = new CybershakeIM(id, measure, value, units);
				ims.add(im);
//				System.out.println(im);
				rs.next();
			}
			rs.close();
		} catch (SQLException e) {
//			e.printStackTrace();
		}
		long duration = System.currentTimeMillis() - startTime;
//		System.out.println("Total SA Period Select Time: " + ((double)duration / 1000) + " sec");
		return ims;
	}
	
	public static void main(String args[]) {
		HazardCurve2DB hc = new HazardCurve2DB(Cybershake_OpenSHA_DBApplication.db);
		
		System.out.println("ID: " + hc.getHazardCurveID(2, 34, 3, 5, 21));
		System.out.println("ID: " + hc.getHazardCurveID(26, 34, 3, 5, 21));
		
		DiscretizedFuncAPI hazardFunc = hc.getHazardCurve(1);
		
		System.out.println(hazardFunc.toString());
		
		for (int id : hc.getAllHazardCurveIDs(34, 3, 5, 21)) {
			System.out.println("Haz Curve For: " + id);
		}
		
		for (CybershakeIM im : hc.getSupportedIMs(33, 34, 3, 5)) {
			System.out.println(im);
		}
	}
	
}
