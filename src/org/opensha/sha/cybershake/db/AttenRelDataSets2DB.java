package org.opensha.sha.cybershake.db;

import java.util.Date;

public class AttenRelDataSets2DB {
	
	private static final String TABLE_NAME = "AR_Hazard_Datasets";
	
	private DBAccess db;
	
	public AttenRelDataSets2DB(DBAccess db) {
		this.db = db;
	}
	
	public int getDataSetID(int attenRelID, int erfID, int probModelID, int timeSpanID, Date date) {
		String sql = "SELECT AR_Hazard_Dataset_ID FROM "+TABLE_NAME+" WHERE"
						+" AR_ID="+attenRelID
						+" AND ERF_ID="+erfID
						+" AND Prob_Model_ID="+probModelID
						+" AND Time_Span_ID="+timeSpanID;
		if (date == null)
			sql += " AND Time_Span_Start_Date IS NULL";
		else
			sql += " AND Time_Span_Start_Date='"+DBAccess.SQL_DATE_FORMAT.format(date)+"'";
		
		return DB_Utils.getSingleInt(db, sql);
	}

}
