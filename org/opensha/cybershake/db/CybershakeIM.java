package org.opensha.cybershake.db;

public class CybershakeIM {
	
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
}
