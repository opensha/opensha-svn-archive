package org.opensha.cybershake.db;

public class CybershakePeakAmplitudeSiteRecord {
	
	private int siteID;
	private int erfID;
	private int rupVarScenID;
	private int sgtVarID;
	
	public CybershakePeakAmplitudeSiteRecord(int siteID, int erfID, int rupVarScenID, int sgtVarID) {
		this.siteID = siteID;
		this.erfID = erfID;
		this.rupVarScenID = rupVarScenID;
		this.sgtVarID = sgtVarID;
	}

	public int getSiteID() {
		return siteID;
	}

	public int getErfID() {
		return erfID;
	}

	public int getRupVarScenID() {
		return rupVarScenID;
	}

	public int getSgtVarID() {
		return sgtVarID;
	}
	
	public String toString() {
		return "Site: " + siteID + ", ERF ID: " + erfID + ", Rup Var Scen ID: " + rupVarScenID + ", SGT Var ID: " + sgtVarID;
	}
}
