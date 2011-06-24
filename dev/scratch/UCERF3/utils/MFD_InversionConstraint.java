package scratch.UCERF3.utils;

import org.opensha.commons.geo.Region;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This class contains an MFD and Region, used as a constraint in the Grand Inversion
 * @author field
 *
 */
public class MFD_InversionConstraint {
	
	IncrementalMagFreqDist mfd;
	Region region;
	
	
	public MFD_InversionConstraint(IncrementalMagFreqDist mfd, Region region) {
		this.mfd=mfd;
		this.region=region;
	}
	
	
	public void setMagFreqDist(IncrementalMagFreqDist mfd) {
		this.mfd=mfd;
	}
	
	
	public IncrementalMagFreqDist getMagFreqDist() {
		return mfd;
	}
	
	
	public void setRegion(Region region) {
		this.region=region;
	}
	
	
	public Region getRegion() {
		return region;
	}

}
