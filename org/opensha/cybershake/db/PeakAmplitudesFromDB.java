package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PeakAmplitudesFromDB implements PeakAmplitudesFromDBAPI {

	
	private DBAccess dbaccess;

	
	public PeakAmplitudesFromDB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	public ArrayList<Integer> getPeakAmpSites() {
		return getDistinctIntVal("Site_ID", null);
	}
	
	public ArrayList<Integer> getDistinctIntVal(String selectCol, String whereClause) {
		ArrayList<Integer> vals = new ArrayList<Integer>();
		
		String sql = "SELECT distinct " + selectCol + " FROM PeakAmplitudes";
		
		if (whereClause != null && whereClause.length() > 0) {
			sql += " WHERE " + whereClause;
		}
		
//		System.out.println(sql);
		
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
				int id = rs.getInt(selectCol);
				vals.add(id);
				rs.next();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public boolean hasAmps(int siteID, int erfID, int rupVarScenID, int sgtVarID) {
		String sql = "SELECT Site_ID FROM PeakAmplitudes WHERE Site_ID=" + siteID + " AND ERF_ID=" + erfID
					+ " AND Rup_Var_Scenario_ID=" + rupVarScenID + " AND SGT_Variation_ID=" + sgtVarID
					+ " LIMIT 1";
		
//		System.out.println(sql);
		
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rs.first();
			if (rs.isAfterLast())
				return false;
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public ArrayList<CybershakePeakAmplitudeSiteRecord> getPeakAmpSiteRecords() {
		ArrayList<CybershakePeakAmplitudeSiteRecord> amps = new ArrayList<CybershakePeakAmplitudeSiteRecord>();
		
		ArrayList<Integer> sites = this.getPeakAmpSites();
		
		ArrayList<Integer> rupVarScenIDs = this.getDistinctIntVal("Rup_Var_Scenario_ID", null);
		ArrayList<Integer> sgtIDs = this.getDistinctIntVal("SGT_Variation_ID", null);
		
		for (int siteID : sites) {
			// get all of the ERF_IDs
			String whereClause = "Site_ID=" + siteID;
			ArrayList<Integer> erfIDs = this.getDistinctIntVal("ERF_ID", whereClause);
			for (int erfID : erfIDs) {
				// get all rup var scen IDs
				for (int rvID : rupVarScenIDs) {
					// get all of the SGT var IDs
					for (int sgtID : sgtIDs) {
						if (hasAmps(siteID, erfID, rvID, sgtID)) {
							CybershakePeakAmplitudeSiteRecord record = new CybershakePeakAmplitudeSiteRecord(siteID, erfID, rvID, sgtID);
							amps.add(record);
							System.out.println("Loaded " + record);
						}
					}
				}
			}
		}
		
		return amps;
	}
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<CybershakeIM>  getSupportedIMs(){
		String sql = "SELECT I.IM_Type_ID,I.IM_Type_Measure,I.IM_Type_Value,I.Units from IM_Types I JOIN (";
		sql += "SELECT distinct IM_Type_ID from PeakAmplitudes";
		sql += ") A ON A.IM_Type_ID=I.IM_Type_ID";
		
		System.out.println(sql);

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
				String units = rs.getString("IM_Type_Units");
				ims.add(new CybershakeIM(id, measure, value, units));
				rs.next();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ims;
	}
	
	public int countAmps(int siteID, int erfID, int sgtVariation, int rupVarID, CybershakeIM im) {
		String sql = "SELECT count(*) from PeakAmplitudes where ERF_ID=" + erfID +" and Site_ID=" + siteID
				+" and Rup_Var_Scenario_ID="+rupVarID+" and SGT_Variation_ID=" + sgtVariation;
		if (im != null)
			sql += " and IM_Type_ID="+im.getID();
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
			int count = rs.getInt(1);
			rs.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<CybershakeIM>  getSupportedIMs(int siteID, int erfID, int sgtVariation, int rupVarID) {
		String whereClause = "Site_ID="+siteID + " AND ERF_ID="+erfID + " AND SGT_Variation_ID="+sgtVariation + 
			" AND Rup_Var_Scenario_ID="+rupVarID;
		long startTime = System.currentTimeMillis();
		String sql = "SELECT I.IM_Type_ID,I.IM_Type_Measure,I.IM_Type_Value,I.Units from IM_Types I JOIN (";
		sql += "SELECT distinct IM_Type_ID from PeakAmplitudes WHERE " + whereClause;
		sql += ") A ON A.IM_Type_ID=I.IM_Type_ID";
		
		System.out.println(sql);
		
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
				ims.add(new CybershakeIM(id, measure, value, units));
				rs.next();
			}
			rs.close();
		} catch (SQLException e) {
//			e.printStackTrace();
		}
		long duration = System.currentTimeMillis() - startTime;
		System.out.println("Total SA Period Select Time: " + ((double)duration / 1000) + " sec");
		return ims;
	}
	
	/**
	 * 
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @returns the rupture variation ids for the rupture
	 */
	public ArrayList<Integer> getRupVarationsForRupture(int erfId,int srcId, int rupId){
		String sql = "SELECT Rup_Var_ID from Rupture_Variations where Source_ID = '"+srcId+"' "+
		             "and ERF_ID =  '"+erfId +"' and Rup_Var_Scenario_ID='3' and Rupture_ID = '"+rupId+"'";
		
		ArrayList<Integer> rupVariationList = new ArrayList<Integer>();
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
			  String rupVariation = rs.getString("Rup_Var_ID");	
			  rupVariationList.add(Integer.parseInt(rupVariation));
			  rs.next();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rupVariationList;
	}
	
	/**
	 * 
	 * @param siteId
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @param rupVarId
	 * @returns the IM Value for the particular IM type
	 */
	public double getIM_Value(int siteId,int erfId,int sgtVariation, int rvid, int srcId,int rupId,int rupVarId, CybershakeIM im){
		String sql = "SELECT distinct IM_Value from PeakAmplitudes where Source_ID = '"+srcId+"' "+
        "and ERF_ID =  '"+erfId +"' and Rupture_ID = '"+rupId+"'  and  Site_ID =  '"+siteId+"' "+
        "and IM_Type_ID = '"+im.getID()+"' and Rup_Var_ID = '"+rupVarId+"' and SGT_Variation_ID= '" + sgtVariation + "' and Rup_Var_Scenario_ID='" + rvid + "'";
//		System.out.println(sql);
		double imVal = Double.NaN;
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rs.first();
			imVal = Double.parseDouble(rs.getString("IM_Value"));	
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return imVal;
	}
	
	/**
	 * 
	 * @param siteId
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @throws SQLException 
	 * @returns the a list of IM Values for the particular IM type
	 */
	public ArrayList<Double> getIM_Values(int siteId,int erfId,int sgtVariation, int rvid, int srcId,int rupId, CybershakeIM im) throws SQLException{
		String sql = "SELECT IM_Value from PeakAmplitudes where Source_ID = '"+srcId+"' "+
        "and ERF_ID =  '"+erfId +"' and Rupture_ID = '"+rupId+"'  and  Site_ID =  '"+siteId+"' "+
        "and IM_Type_ID = '"+im.getID()+"' and SGT_Variation_ID= '" + sgtVariation + "' and Rup_Var_Scenario_ID='" + rvid + "'";
//		System.out.println(sql);
		double imVal = Double.NaN;
		ResultSet rs = null;
		ArrayList<Double> vals = new ArrayList<Double>();
		try {
			rs = dbaccess.selectData(sql);
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		rs.first();
		vals.add(rs.getDouble("IM_Value"));
//		vals.add(Double.parseDouble(rs.getString("IM_Value")));
		while (rs.next()) {
			vals.add(rs.getDouble("IM_Value"));
//			vals.add(Double.parseDouble(rs.getString("IM_Value")));
		}
		rs.close();
		return vals;
	}
	
	 /**
	  * @return all possible SGT Variation IDs
	  */
	public ArrayList<Integer> getSGTVarIDs() {
		ArrayList<Integer> vars = new ArrayList<Integer>();
		
		String sql = "SELECT SGT_Variation_ID from SGT_Variation_IDs order by SGT_Variation_ID desc";
		
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
			  int id = rs.getInt("SGT_Variation_ID");
			  vars.add(id);
			  rs.next();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return vars;
	}
	
	/**
	 * @return all possible Rup Var Scenario IDs
	 */
	public ArrayList<Integer> getRupVarScenarioIDs() {
		ArrayList<Integer> vars = new ArrayList<Integer>();
		
		String sql = "SELECT Rup_Var_Scenario_ID from Rupture_Variation_Scenario_IDs order by Rup_Var_Scenario_ID desc";
		
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
			  int id = rs.getInt("Rup_Var_Scenario_ID");
			  vars.add(id);
			  rs.next();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return vars;
	}
	
	public CybershakeIM getIMForPeriod(double period, int siteID, int erfID, int sgtVariation, int rupVarID) {
		return this.getIMForPeriod(period, siteID, erfID, sgtVariation, rupVarID, null);
	}
	
	public CybershakeIM getIMForPeriod(double period, int siteID, int erfID, int sgtVariation, int rupVarID, HazardCurve2DB curve2db) {
		ArrayList<Double> periods = new ArrayList<Double>();
		periods.add(period);
		
		return getIMForPeriods(periods, siteID, erfID, sgtVariation, rupVarID, curve2db).get(0);
	}
	
	public ArrayList<CybershakeIM> getIMForPeriods(ArrayList<Double> periods, int siteID, int erfID, int sgtVariation, int rupVarID) {
		return this.getIMForPeriods(periods, siteID, erfID, sgtVariation, rupVarID, null);
	}
	
	public ArrayList<CybershakeIM> getIMForPeriods(ArrayList<Double> periods, int siteID, int erfID, int sgtVariation, int rupVarID, HazardCurve2DB curve2db) {
		ArrayList<CybershakeIM> supported = this.getSupportedIMs(siteID, erfID, sgtVariation, rupVarID);
		if (curve2db != null) {
			supported.addAll(curve2db.getSupportedIMs(siteID, erfID, rupVarID, sgtVariation));
		}
		
		ArrayList<CybershakeIM> matched = new ArrayList<CybershakeIM>();
		
		if (supported.size() == 0)
			return null;
		
		double maxDist = 0.5;
		
		for (double period : periods) {
			CybershakeIM closest = null;
			double dist = Double.POSITIVE_INFINITY;
			
			for (CybershakeIM im : supported) {
				double val = Math.abs(period - im.getVal());
//				System.out.println("Comparing " + val + " to " + im.getVal());
				if (val < dist && val <= maxDist) {
					closest = im;
					dist = val;
				}
			}
			if (dist != Double.POSITIVE_INFINITY)
				System.out.println("Matched " + period + " with " + closest.getVal());
			else
				System.out.println("NO MATCH FOR " + period + "!!!");
			matched.add(closest);
		}
		
		return matched;
	}
	
	public int deleteAmpsForSite(int siteId,int erfId,int sgtVariation, int rvid) {
		String sql = "DELETE FROM PeakAmplitudes WHERE ERF_ID="+erfId +" AND Site_ID="+siteId+
			" AND SGT_Variation_ID=" + sgtVariation + " and Rup_Var_Scenario_ID=" + rvid;
		System.out.println(sql);
		try {
			return dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
//			TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	public static void main(String args[]) {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		PeakAmplitudesFromDB amps = new PeakAmplitudesFromDB(db);
		
		amps.getPeakAmpSiteRecords();
		
		db.destroy();
		
		System.exit(0);
	}
	
}
