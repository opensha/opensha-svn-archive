package scratch.UCERF3.utils;

import java.util.List;

import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
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
	
	
	/**
	 * This returns the fraction of points inside the region from all the FaultSectionPrefData
	 * objects converted to a StirlingGriddedSurface with 1-km discretization.  Note that 
	 * aseismicity reduces area here.
	 * 
	 * @param faultSectPrefDataList
	 * @return
	 */
	public double getFractionInRegion(List<FaultSectionPrefData> faultSectPrefDataList) {
		double numInside=0, totNum=0;
		double gridSpacing=1;  // in km
		for(FaultSectionPrefData data: faultSectPrefDataList) {
			StirlingGriddedSurface surf = new StirlingGriddedSurface(data.getSimpleFaultData(true), gridSpacing);
			double numPts = (double) surf.size();
			totNum += numPts;
			numInside += numPts*RegionUtils.getFractionInside(region, surf.getLocationList());
			data.getSimpleFaultData(true);
		}
		return numInside/totNum;
	}

}
