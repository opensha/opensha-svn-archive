package org.opensha.sha.cybershake.db;

public class CybershakeIM implements Comparable<CybershakeIM> {
	
	private int id;
	private String measure;
	private double val;
	private String units;
	
	public CybershakeIM(int id, String measure, double val, String units) {
		this.id = id;
		this.measure = measure;
		this.val = val;
		this.units = units;
	}

	public int getID() {
		return id;
	}

	public String getMeasure() {
		return measure;
	}

	public double getVal() {
		return val;
	}

	public String getUnits() {
		return units;
	}
	
	public String toString() {
		return this.measure + ": " + this.val + " (" + this.units + ")";
	}
	
	public boolean equals(CybershakeIM im) {
		return id == im.id;
	}

	public int compareTo(CybershakeIM im) {
		if (val > im.val)
			return 1;
		if (val < im.val)
			return -1;
		return 0;
	}
}
