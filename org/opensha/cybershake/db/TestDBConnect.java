package org.opensha.cybershake.db;
import java.sql.ResultSet;
import java.sql.SQLException;



public class TestDBConnect {

	public static void main(String[] args) {
		DBAccess dbc = new DBAccess("intensity.usc.edu","CyberShake");
		ResultSet rs = dbc.selectData("SHOW TABLES");
		
		//dbc.deleteOrUpdateData("Delete from ERF_Metadata");
		dbc.deleteOrUpdateData("Delete from CyberShake_Site_Ruptures");
		dbc.deleteOrUpdateData("Delete from CyberShake_Site_Regions");
		//dbc.deleteOrUpdateData("Delete from Ruptures");
		//dbc.deleteOrUpdateData("Delete from Points");	
		dbc.deleteOrUpdateData("Delete from CyberShake_Sites");
		//dbc.deleteOrUpdateData("Delete from ERF_IDs");
		try {
			System.out.println(rs.getMetaData().getColumnCount());
			rs.first();
			while (!rs.isAfterLast()) {
				System.out.println(rs.getString(1));
				rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

