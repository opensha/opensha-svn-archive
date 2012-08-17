package scratch.UCERF3.utils.paleoRateConstraints;

import java.util.List;
import java.util.Map;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.inversion.InversionInputGenerator;

/**
 * This loads in Glenn's paleoseismic trench probabilities.
 * 
 * @author Kevin
 *
 */
public abstract class PaleoProbabilityModel {
	
	private Map<Integer, Double> traceLengthCache = Maps.newHashMap();
	
	public abstract double getProbPaleoVisible(FaultSystemRupSet rupSet, int rupIndex, int sectIndex);
	
	public abstract double getProbPaleoVisible(double mag, double distAlongRup);
	
	double getDistAlongRup(FaultSystemRupSet rupSet, int rupIndex, int sectIndex) {
		return InversionInputGenerator.getDistanceAlongRupture(
				rupSet.getFaultSectionDataForRupture(rupIndex), sectIndex, traceLengthCache);
	}

}