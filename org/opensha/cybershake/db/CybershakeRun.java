package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CybershakeRun {
	
	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private int runID;
	private int siteID;
	private int erfID;
	private int sgtVarID;
	private int rupVarScenID;
	private Timestamp sgtTime;
	private Timestamp ppTime;
	private String sgtHost;
	private String ppHost;
	
	public CybershakeRun(int runID, int siteID, int erfID, int sgtVarID, int rupVarScenID,
			Timestamp sgtTime, Timestamp ppTime, String sgtHost, String ppHost) {
		this.runID = runID;
		this.siteID = siteID;
		this.erfID = erfID;
		this.sgtVarID = sgtVarID;
		this.rupVarScenID = rupVarScenID;
		this.sgtTime = sgtTime;
		this.ppTime = ppTime;
		this.sgtHost = sgtHost;
		this.ppHost = ppHost;
	}

	public int getRunID() {
		return runID;
	}

	public int getSiteID() {
		return siteID;
	}
	
	public int getERFID() {
		return erfID;
	}

	public int getSgtVarID() {
		return sgtVarID;
	}

	public int getRupVarScenID() {
		return rupVarScenID;
	}

	public Timestamp getSGTTimestamp() {
		return sgtTime;
	}
	
	public Timestamp getPPTimestamp() {
		return ppTime;
	}

	public String getSGTHost() {
		return sgtHost;
	}

	public String getPPHost() {
		return ppHost;
	}
	
	@Override
	public String toString() {
		return "ID: " + getRunID() + ", Site_ID: " + getSiteID() + ", ERF_ID: " + getERFID() +
				", SGT Var ID: " + getSgtVarID() + ", Rup Var Scen ID: " + getRupVarScenID() +
				", SGT Time: " + format.format(getSGTTimestamp()) + ", SGT Host: " + sgtHost +
				", PP Time: " + format.format(getPPTimestamp()) + ", PP Host: " + ppHost;
	}
	
	public static CybershakeRun fromResultSet(ResultSet rs) throws SQLException {
		int runID = rs.getInt("Run_ID");
		int siteID = rs.getInt("Site_ID");
		int erfID = rs.getInt("ERF_ID");
		int sgtVarID = rs.getInt("SGT_Variation_ID");
		int rupVarScenID = rs.getInt("Rup_Var_Scenario_ID");
		Timestamp sgtTime = rs.getTimestamp("SGT_Time");
		Timestamp ppTime = rs.getTimestamp("PP_Time");
		String sgtHost = rs.getString("SGT_Host");
		String ppHost = rs.getString("PP_Host");
		
		return new CybershakeRun(runID, siteID, erfID, sgtVarID, rupVarScenID, sgtTime, ppTime, sgtHost, ppHost);
	}

}
