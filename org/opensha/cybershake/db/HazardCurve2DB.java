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
		
		String sql = "SELECT Hazard_Curve_ID FROM Hazard_Curves WHERE ERF_ID=" + erfID + 
					" AND Rup_Var_Scenario_ID=" + rupVarScenarioID + " AND SGT_Variation_ID=" + sgtVarID + 
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
		this.insertHazardCurve(id, hazardFunc);
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
	
	private void insertHazardCurve(int id, DiscretizedFuncAPI hazardFunc) {
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
	
	public static void main(String args[]) {
		HazardCurve2DB hc = new HazardCurve2DB(Cybershake_OpenSHA_DBApplication.db);
		
		System.out.println("ID: " + hc.getHazardCurveID(2, 34, 3, 5, 21));
		System.out.println("ID: " + hc.getHazardCurveID(26, 34, 3, 5, 21));
		
		DiscretizedFuncAPI hazardFunc = hc.getHazardCurve(1);
		
		System.out.println(hazardFunc.toString());
		
		for (int id : hc.getAllHazardCurveIDs(34, 3, 5, 21)) {
			System.out.println("Haz Curve For: " + id);
		}
	}
	
}
