package scratch.stirling;

import static org.opensha.sha.util.TectonicRegionType.*;
import org.opensha.sha.util.TectonicRegionType;

/**
 * Add comments here
 *
 * @author Peter Powers
 */
public enum NZ_SourceID {

	RV(90.0, ACTIVE_SHALLOW),
	IF(90.0, SUBDUCTION_INTERFACE),

	SS(0.0, ACTIVE_SHALLOW),
	NN(-90.0, ACTIVE_SHALLOW),
	NV(-90.0, VOLCANIC),
	
	SR(30.0, ACTIVE_SHALLOW),
	RS(60.0, ACTIVE_SHALLOW),
	NS(-60.0, ACTIVE_SHALLOW),
	SN(-30.0, ACTIVE_SHALLOW);	
	
	private double rake;
	private TectonicRegionType trt;
	
	private NZ_SourceID(double rake, TectonicRegionType trt) {
		this.rake = rake;
		this.trt = trt;
	}
	
	/**
	 * Convert 'id' from source file to enum type.
	 * @param id
	 * @return the source identifier
	 */
	public static NZ_SourceID fromString(String id) {
		return NZ_SourceID.valueOf(id.toUpperCase());
	}
	
	/**
	 * Returns the rake.
	 * @return the rake
	 */
	public double rake() {
		return rake;
	}
	
	/**
	 * Returns the {code TectonicRegionType}.
	 * @return the {code TectonicRegionType}
	 */
	public TectonicRegionType tectonicType() {
		return trt;
	}
}
