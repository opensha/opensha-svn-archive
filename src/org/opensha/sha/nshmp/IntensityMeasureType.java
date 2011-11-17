package org.opensha.sha.nshmp;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum IntensityMeasureType {

	/** Peak Ground Acceleration (PGA) */
	PGA,
	/** Peak Ground Velocity (PGV) */
	PGV,
	/** Spectral Acceleration (SA). 0.1 sec or 10 Hz */
	SA0P10,
	/** Spectral Acceleration (SA). 0.2 sec or 5 Hz */
	SA0P20,
	/** Spectral Acceleration (SA). 0.3 sec or 3.3 Hz */
	SA0P30,
	/** Spectral Acceleration (SA). 0.5 sec or 2 Hz */
	SA0P50,
	/** Spectral Acceleration (SA). 0.75 sec or 1.5 Hz */
	SA0P75,
	/** Spectral Acceleration (SA). 1.0 sec or 1 Hz */
	SA1P00,
	/** Spectral Acceleration (SA). 2.0 sec or 0.5 Hz */
	SA2P00,
	/** Spectral Acceleration (SA). 3.0 sec or 0.33 Hz */
	SA3P00,
	/** Spectral Acceleration (SA). 4.0 sec or 0.25 Hz */
	SA4P00,
	/** Spectral Acceleration (SA). 5.0 sec or 0.2 Hz */
	SA5P00;

}
