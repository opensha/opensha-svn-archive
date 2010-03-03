package org.opensha.sha.cybershake.db;

import java.util.Date;

public class CybershakeHazardCurveRecord implements Comparable<CybershakeHazardCurveRecord> {
	
	private int curveID;
	private int runID;
	private int imTypeID;
	private Date date;
	
	public CybershakeHazardCurveRecord(int curveID, int runID, int imTypeID, Date date) {
		this.curveID = curveID;
		this.runID = runID;
		this.imTypeID = imTypeID;
		this.date = date;
	}

	public int getCurveID() {
		return curveID;
	}

	public int getRunID() {
		return runID;
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

	public String toString() {
		return "curveID: " + curveID + ", runID: " + runID + ", imTypeID: " + imTypeID + ", date: " + date;
	}
}
