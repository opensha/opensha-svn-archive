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
		ResultSet rs =  dbaccess.selectData(sql);
		try {
			rs.first();
			while(!rs.isAfterLast()){
			  String saPeriod = rs.getString(0);	
			  saPeriodList.add(saPeriod);
			  rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		             "and ERF_ID =  '"+erfId +"' and Rupture_ID = '"+rupId+"'";
		
		ArrayList<Integer> rupVariationList = new ArrayList<Integer>();
		ResultSet rs =  dbaccess.selectData(sql);
		try {
			rs.first();
			while(!rs.isAfterLast()){
			  String rupVariation = rs.getString(0);	
			  rupVariationList.add(Integer.parseInt(rupVariation));
			  rs.next();
			}
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
	public double getIM_Value(int siteId,int erfId,int srcId,int rupId,int rupVarId, String imType){
		String sql = "SELECT IM_Value from PeakAmplitudes where Source_ID = '"+srcId+"' "+
        "and ERF_ID =  '"+erfId +"' and Rupture_ID = '"+rupId+"'  and  Site_ID =  '"+siteId+"' "+
        "and IM_Type = '"+imType+"' and Rup_Var_ID = '"+rupVarId+"'";
		
		double imVal = Double.NaN;
		ResultSet rs =  dbaccess.selectData(sql);
		try {
			rs.first();
			imVal = Double.parseDouble(rs.getString(0));	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return imVal;
	}
	
	
	
}
