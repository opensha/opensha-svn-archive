package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils.*;

import java.util.logging.Logger;


/*
 * Wrapper for CH MFD data
 */
class CH_Data {
	
	double mag;
	double rate;
	double weight;
	
	CH_Data(String src, Logger log) {
		double[] chDat = readDoubles(src, 3);
		mag = chDat[0];
		rate = chDat[1];
		weight = chDat[2];
	}
	
	CH_Data(double mag, double rate, double weight, Logger log) {
		this.mag = mag;
		this.rate = rate;
		this.weight = weight;
	}

}
