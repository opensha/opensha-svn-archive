package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PeakAmplitudesFromDB implements PeakAmplitudesFromDBAPI {

	
	private DBAccess dbaccess;

	
	public PeakAmplitudesFromDB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<String>  getSupportedSA_PeriodList(){
		String sql = "SELECT IM_Type from PeakAmplitudes group by IM_Type";
		
		ArrayList<String> saPeriodList = new ArrayList<String>();
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
			  String saPeriod = rs.getString("IM_Type");	
			  saPeriodList.add(saPeriod);
			  rs.next();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return saPeriodList;
	}
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<String>  getSupportedSA_PeriodList(int siteID, int erfID, int sgtVariation, int rupVarID) {
		String whereClause = "Site_ID="+siteID + " AND ERF_ID="+erfID + " AND SGT_Variation_ID="+sgtVariation + 
			" AND Rup_Var_Scenario_ID="+rupVarID;
		long startTime = System.currentTimeMillis();
		String sql = "SELECT distinct IM_Type from PeakAmplitudes WHERE " + whereClause + " group by IM_Type";
//		System.out.println(sql);
		ArrayList<String> saPeriodList = new ArrayList<String>();
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
			  String saPeriod = rs.getString("IM_Type");
			  saPeriodList.add(saPeriod);
			  rs.next();
			}
			rs.close();
		} catch (SQLException e) {
//			e.printStackTrace();
		}
		long duration = System.currentTimeMillis() - startTime;
		System.out.println("Total SA Period Select Time: " + ((double)duration / 1000) + " sec");
		return saPeriodList;
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
	public double getIM_Value(int siteId,int erfId,int sgtVariation, int rvid, int srcId,int rupId,int rupVarId, String imType){
		String sql = "SELECT distinct IM_Value from PeakAmplitudes where Source_ID = '"+srcId+"' "+
        "and ERF_ID =  '"+erfId +"' and Rupture_ID = '"+rupId+"'  and  Site_ID =  '"+siteId+"' "+
        "and IM_Type = '"+imType+"' and Rup_Var_ID = '"+rupVarId+"' and SGT_Variation_ID= '" + sgtVariation + "' and Rup_Var_Scenario_ID='" + rvid + "'";
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
	public ArrayList<Double> getIM_Values(int siteId,int erfId,int sgtVariation, int rvid, int srcId,int rupId, String imType) throws SQLException{
		String sql = "SELECT IM_Value from PeakAmplitudes where Source_ID = '"+srcId+"' "+
        "and ERF_ID =  '"+erfId +"' and Rupture_ID = '"+rupId+"'  and  Site_ID =  '"+siteId+"' "+
        "and IM_Type = '"+imType+"' and SGT_Variation_ID= '" + sgtVariation + "' and Rup_Var_Scenario_ID='" + rvid + "'";
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
	
}
