package org.opensha.cybershake.db;

import java.util.Date;

public class CybershakeHazardCurveRecord implements Comparable<CybershakeHazardCurveRecord> {
	
	private int curveID;
	private int siteID;
	private int erfID;
	private int rupVarScenID;
	private int sgtVarID;
	private int imTypeID;
	private Date date;
	
	public CybershakeHazardCurveRecord(int curveID, int siteID, int erfID, int rupVarScenID, int sgtVarID, int imTypeID, Date date) {
		this.curveID = curveID;
		this.siteID = siteID;
		this.erfID = erfID;
		this.rupVarScenID = rupVarScenID;
		this.sgtVarID = sgtVarID;
		this.imTypeID = imTypeID;
		this.date = date;
	}

	public int getCurveID() {
		return curveID;
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

	public int getImTypeID() {
		return imTypeID;
	}

	public Date getDate() {
		return date;
	}

	public int compareTo(CybershakeHazardCurveRecord o) {
		if (o.getImTypeID() < this.getImTypeID())
			return -1;
		else if (o.getImTypeID() > this.getImTypeID())
			return 1;
		return 0;
	}

}
