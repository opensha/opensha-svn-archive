package org.opensha.sha.cybershake.calc.mcer;

public class DeterministicResult {
	
	private int sourceID;
	private int rupID;
	private double mag;
	private String sourceName;
	private double val;
	
	public DeterministicResult(int sourceID, int rupID, double mag,
			String sourceName, double val) {
		super();
		this.sourceID = sourceID;
		this.rupID = rupID;
		this.mag = mag;
		this.sourceName = sourceName;
		this.val = val;
	}

	public int getSourceID() {
		return sourceID;
	}

	public int getRupID() {
		return rupID;
	}

	public double getMag() {
		return mag;
	}

	public String getSourceName() {
		return sourceName;
	}

	public double getVal() {
		return val;
	}
	
	public void setVal(double val) {
		this.val = val;
	}
	
	@Override
	public String toString() {
		return "DetResult: "+val+", src("+getSourceID()+","+getRupID()+"): "+getSourceName()+" (M="+(float)getMag()+")";
	}

}
