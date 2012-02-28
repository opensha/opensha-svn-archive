package scratch.UCERF3.inversion;

public class LaughTestFilter {
	
	private double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff,
	maxCmlJumpDist, maxCmlRakeChange, maxCmlAzimuthChange;
	private int minNumSectInRup;
	
	/**
	 * This returns the current default laugh test filter
	 * 
	 * @return
	 */
	public static LaughTestFilter getDefault() {
		double maxAzimuthChange = 90;
		double maxJumpDist = 5d;
		double maxCumJumpDist = 10d;
		double maxTotAzimuthChange = 90d;
		double maxRakeDiff = Double.POSITIVE_INFINITY;
		int minNumSectInRup = 2;
		double maxCmlRakeChange = 360;
		double maxCmlAzimuthChange = 540;
		
		return new LaughTestFilter(maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, maxCumJumpDist,
				maxCmlRakeChange, maxCmlAzimuthChange, minNumSectInRup);
	}
	
	public LaughTestFilter(double maxJumpDist, double maxAzimuthChange,
			double maxTotAzimuthChange, double maxRakeDiff,
			double maxCumJumpDist, double maxCmlRakeChange,
			double maxCmlAzimuthChange, int minNumSectInRup) {
		this.maxJumpDist = maxJumpDist;
		this.maxAzimuthChange = maxAzimuthChange;
		this.maxTotAzimuthChange = maxTotAzimuthChange;
		this.maxRakeDiff = maxRakeDiff;
		this.maxCmlJumpDist = maxCumJumpDist;
		this.maxCmlRakeChange = maxCmlRakeChange;
		this.maxCmlAzimuthChange = maxCmlAzimuthChange;
		this.minNumSectInRup = minNumSectInRup;
	}

	public double getMaxJumpDist() {
		return maxJumpDist;
	}

	public void setMaxJumpDist(double maxJumpDist) {
		this.maxJumpDist = maxJumpDist;
	}

	public double getMaxAzimuthChange() {
		return maxAzimuthChange;
	}

	public void setMaxAzimuthChange(double maxAzimuthChange) {
		this.maxAzimuthChange = maxAzimuthChange;
	}

	public double getMaxTotAzimuthChange() {
		return maxTotAzimuthChange;
	}

	public void setMaxTotAzimuthChange(double maxTotAzimuthChange) {
		this.maxTotAzimuthChange = maxTotAzimuthChange;
	}

	public double getMaxRakeDiff() {
		return maxRakeDiff;
	}

	public void setMaxRakeDiff(double maxRakeDiff) {
		this.maxRakeDiff = maxRakeDiff;
	}

	public double getMaxCmlJumpDist() {
		return maxCmlJumpDist;
	}

	public void setMaxCumJumpDist(double maxCumJumpDist) {
		this.maxCmlJumpDist = maxCumJumpDist;
	}

	public double getMaxCmlRakeChange() {
		return maxCmlRakeChange;
	}

	public void setMaxCmlRakeChange(double maxCmlRakeChange) {
		this.maxCmlRakeChange = maxCmlRakeChange;
	}

	public double getMaxCmlAzimuthChange() {
		return maxCmlAzimuthChange;
	}

	public void setMaxCmlAzimuthChange(double maxCmlAzimuthChange) {
		this.maxCmlAzimuthChange = maxCmlAzimuthChange;
	}

	public int getMinNumSectInRup() {
		return minNumSectInRup;
	}

	public void setMinNumSectInRup(int minNumSectInRup) {
		this.minNumSectInRup = minNumSectInRup;
	}

	@Override
	public String toString() {
		return "LaughTestFilter [maxJumpDist=" + maxJumpDist
				+ ", maxAzimuthChange=" + maxAzimuthChange
				+ ", maxTotAzimuthChange=" + maxTotAzimuthChange
				+ ", maxRakeDiff=" + maxRakeDiff + ", maxCumJumpDist=" + maxCmlJumpDist
				+ ", maxCmlRakeChange=" + maxCmlRakeChange
				+ ", maxCmlAzimuthChange=" + maxCmlAzimuthChange
				+ ", minNumSectInRup=" + minNumSectInRup + "]";
	}

}
