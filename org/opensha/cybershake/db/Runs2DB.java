package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Runs2DB {
	
	private DBAccess db;
	
	public Runs2DB(DBAccess db) {
		this.db = db;
	}
	
	public int getSiteID(int runID) {
		String sql = "SELECT Site_ID FROM CyberShake_Runs WHERE Run_ID=" + runID;
		
		try {
			ResultSet rs = db.selectData(sql);
			boolean valid = rs.first();
			
			if (valid) {
				int id = rs.getInt("Site_ID");
				
				rs.close();
				
				return id;
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int getLatestRunID(int siteID, int erfID, int sgtVarID, int rupVarScenID,
			Timestamp sgtTime, Timestamp ppTime, String sgtHost, String ppHost) {
		ArrayList<Integer> ids = getRunIDs(siteID, erfID, sgtVarID, rupVarScenID, sgtTime, ppTime, sgtHost, ppHost);
		if (ids == null || ids.size() == 0)
			return -1;
		return ids.get(0);
	}
	
	public ArrayList<Integer> getRunIDs(int siteID) {
		return getRunIDs(siteID, -1, -1, -1, null, null, null, null);
	}
	
	public ArrayList<Integer> getRunIDs(int siteID, int erfID, int sgtVarID, int rupVarScenID,
			Timestamp sgtTime, Timestamp ppTime, String sgtHost, String ppHost) {
		String sql = "SELECT Run_ID FROM CyberShake_Runs";
		String where = generateWhereClause(siteID, erfID, sgtVarID, rupVarScenID, sgtTime, ppTime, sgtHost, ppHost);
		if (where != null && where.length() > 0)
			sql += " WHERE" + where;
		
		sql += " ORDER BY Run_ID desc";
		
		System.out.println(sql);
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		try {
			ResultSet rs = db.selectData(sql);
			boolean valid = rs.first();
			
			while (valid) {
				int id = rs.getInt("Run_ID");
				ids.add(id);
				
				valid = rs.next();
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return ids;
	}
	
	public CybershakeRun getRun(int runID) {
		String sql = "SELECT * FROM CyberShake_Runs WHERE Run_ID=" + runID;
		
		try {
			ResultSet rs = db.selectData(sql);
			boolean valid = rs.first();
			
			CybershakeRun run = null;
			if (valid) {
				run = CybershakeRun.fromResultSet(rs);
			}
			
			rs.close();
			
			return run;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public CybershakeRun getLatestRun(int siteID, int erfID, int sgtVarID, int rupVarScenID,
			Timestamp sgtTime, Timestamp ppTime, String sgtHost, String ppHost) {
		ArrayList<CybershakeRun> ids = getRuns(siteID, erfID, sgtVarID, rupVarScenID, sgtTime, ppTime, sgtHost, ppHost);
		if (ids == null || ids.size() == 0)
			return null;
		return ids.get(0);
	}
	
	public ArrayList<CybershakeRun> getRuns() {
		return getRuns(-1, -1, -1, -1, null, null, null, null);
	}
	
	public ArrayList<CybershakeRun> getRuns(int siteID) {
		return getRuns(siteID, -1, -1, -1, null, null, null, null);
	}
	
	public ArrayList<CybershakeRun> getRuns(int siteID, int erfID, int sgtVarID, int rupVarScenID,
			Timestamp sgtTime, Timestamp ppTime, String sgtHost, String ppHost) {
		String sql = "SELECT * FROM CyberShake_Runs";
		String where = generateWhereClause(siteID, erfID, sgtVarID, rupVarScenID, sgtTime, ppTime, sgtHost, ppHost);
		if (where != null && where.length() > 0)
			sql += " WHERE" + where;
		
		System.out.println(sql);
		
		ArrayList<CybershakeRun> runs = new ArrayList<CybershakeRun>();
		
		try {
			ResultSet rs = db.selectData(sql);
			boolean valid = rs.first();
			
			while (valid) {
				CybershakeRun run = CybershakeRun.fromResultSet(rs);
				runs.add(run);
				
				valid = rs.next();
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return runs;
	}
	
	private String generateWhereClause(int siteID, int erfID, int sgtVarID, int rupVarScenID,
			Timestamp sgtTime, Timestamp ppTime, String sgtHost, String ppHost) {
		String where = "";
		boolean first = true;
		if (siteID >= 0) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " Site_ID=" + siteID;
		}
		if (erfID >= 0) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " ERF_ID=" + erfID;
		}
		if (sgtVarID >= 0) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " SGT_Variation_ID=" + sgtVarID;
		}
		if (rupVarScenID >= 0) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " Rup_Var_Scenario_ID=" + rupVarScenID;
		}
		if (sgtTime != null) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " SGT_Time='" + CybershakeRun.format.format(sgtTime) + "'";
		}
		if (ppTime != null) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " PP_Time='" + CybershakeRun.format.format(ppTime) + "'";
		}
		if (sgtHost != null && sgtHost.length() > 0) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " SGT_Host='" + sgtHost + "'";
		}
		if (ppHost != null && ppHost.length() > 0) {
			if (first)
				first = false;
			else
				where += " AND";
			where += " PP_Host='" + ppHost + "'";
		}
		
		return where;
	}
	
	public static String getRunsWhereStatement(ArrayList<Integer> runIDs) {
		String whereStr = null;
		
		for (int runID : runIDs) {
			if (whereStr == null) {
				whereStr = "Run_ID IN (";
			} else {
				whereStr += ",";
			}
			whereStr += runID;
		}
		if (whereStr != null)
			whereStr += ")";
		
		return whereStr;
	}
	
	public static void main(String args[]) {
		Runs2DB runs2db = new Runs2DB(Cybershake_OpenSHA_DBApplication.db);
		ArrayList<CybershakeRun> runs = runs2db.getRuns();
		
		for (CybershakeRun run : runs) {
			System.out.println(run);
		}
		
		ArrayList<Integer> asdf = new ArrayList<Integer>();
		asdf.add(3);
		asdf.add(1);
		asdf.add(6);
		System.out.println(getRunsWhereStatement(asdf));
		
		System.exit(0);
	}

}
