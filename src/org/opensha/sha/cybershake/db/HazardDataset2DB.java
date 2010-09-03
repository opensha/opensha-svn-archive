package org.opensha.sha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HazardDataset2DB {
	
	private DBAccess db;
	
	public HazardDataset2DB(DBAccess db) {
		this.db = db;
	}
	
	public int getDefaultProbModelID(int erfID) {
		return getERF_Field("Default_Prob_Model_ID", erfID);
	}
	
	public int getDefaultTimeSpanID(int erfID) {
		return getERF_Field("Default_Time_Span_ID", erfID);
	}
	
	private int getERF_Field(String field, int erfID) {
		String sql = "SELECT "+field;
		sql += " FROM ERF_IDs WHERE ERF_ID="+erfID;
		
//		System.out.println(sql);
		
		return DB_Utils.getSingleInt(db, sql);
	}
	
	public int getDefaultDatasetID(CybershakeRun run) {
		int probModelID = getDefaultProbModelID(run.getERFID());
		int timeSpanID = getDefaultTimeSpanID(run.getERFID());
		
		String sql = "SELECT Hazard_Dataset_ID";
		sql += " FROM Hazard_Datasets";
		sql += " WHERE ERF_ID="+run.getERFID();
		sql += " AND Rup_Var_Scenario_ID="+run.getRupVarScenID();
		sql += " AND SGT_Variation_ID="+run.getSgtVarID();
		sql += " AND Velocity_Model_ID="+run.getVelModelID();
		sql += " AND Prob_Model_ID="+probModelID;
		sql += " AND Time_Span_ID="+timeSpanID;
		
//		System.out.println(sql);
		
		return DB_Utils.getSingleInt(db, sql);
	}
	
	public static void main(String[] args) {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		Runs2DB r2db = new Runs2DB(db);
		CybershakeRun run = r2db.getRun(571);
		HazardDataset2DB hd2db = new HazardDataset2DB(db);
		System.out.println("Dataset: " + hd2db.getDefaultDatasetID(run));
		
		db.destroy();
		
		System.exit(0);
	}

}
