package org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData;

import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.util.TectonicRegionType;

public class GEMFaultSourceData extends GEMSourceData {

	private FaultTrace trace;
	private double dip;
	private double rake;
	private double seismDepthLow;
	private double seismDepthUpp;
	private IncrementalMagFreqDist mfd;
	private boolean floatRuptureFlag;
	//private EvenlyDiscretizedFunc mfd;
	
	/**
	 * 
	 */
	public GEMFaultSourceData() {
	}

	/**
	 * 
	 * @param trc
	 * @param dip
	 * @param rake
	 * @param sdepLo
	 * @param sdepUp
	 */
	public GEMFaultSourceData(String id, String name, TectonicRegionType tectReg, 
			IncrementalMagFreqDist mfd, FaultTrace trc, double dip, 
			double rake, double seismDepthLow, double seismDepthUpp, boolean floatRuptureFlag){
		this.id = id;
		this.name = name;
		this.tectReg = tectReg;
		this.mfd = mfd;
		this.trace = trc;
		this.dip = dip;
		this.rake = rake;
		this.seismDepthLow = seismDepthLow;
		this.seismDepthUpp = seismDepthUpp;
		this.floatRuptureFlag = floatRuptureFlag;
	}
	
	public double getDip() {
		return dip;
	}

	public double getRake() {
		return rake;
	}

	public double getSeismDepthLow() {
		return seismDepthLow;
	}

	public double getSeismDepthUpp() {
		return seismDepthUpp;
	}

	//public IncrementalMagFreqDist getMfd() {
	public IncrementalMagFreqDist getMfd() {
		return mfd;
	}

	public FaultTrace getTrace() {
		return this.trace;
	}
	
	public boolean getFloatRuptureFlag() {
		return floatRuptureFlag;
	}

	
}
