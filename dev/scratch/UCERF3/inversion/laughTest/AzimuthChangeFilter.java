package scratch.UCERF3.inversion.laughTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;

import scratch.UCERF3.utils.IDPairing;

/**
 * This restricts the maximum azimuth change of any junction in the rupture. Azimuth
 * changes are computed as the azimuth change between the midpoints of two sections
 * on the same fault. For this reason, 2 sections are required per fault to compute
 * accurate azimuths.
 * 
 * @author kevin
 *
 */
public class AzimuthChangeFilter extends AbstractLaughTest {
	
	public static boolean INCLUDE_UCERF3p3_NEW_LL = false;
	
	private boolean applyGarlockPintoMtnFix;
	private HashSet<Integer> leftLateralFixParents;
	private Map<IDPairing, Double> sectionAzimuths;
	
	private double maxAzimuthChange;
	private double maxTotAzimuthChange;
	
	public AzimuthChangeFilter(double maxAzimuthChange, double maxTotAzimuthChange,
			boolean applyGarlockPintoMtnFix, Map<IDPairing, Double> sectionAzimuths) {
		this.maxAzimuthChange = maxAzimuthChange;
		this.maxTotAzimuthChange = maxTotAzimuthChange;
		this.applyGarlockPintoMtnFix = applyGarlockPintoMtnFix;
		this.sectionAzimuths = sectionAzimuths;
		if (applyGarlockPintoMtnFix) {
			leftLateralFixParents = new HashSet<Integer>();
			leftLateralFixParents.add(48);
			leftLateralFixParents.add(49);
			leftLateralFixParents.add(93);
			leftLateralFixParents.add(341);
			if (INCLUDE_UCERF3p3_NEW_LL) {
				leftLateralFixParents.add(47);
				leftLateralFixParents.add(169);
			}
		}
	}

	@Override
	public boolean doesLastSectionPass(List<FaultSectionPrefData> rupture,
			List<IDPairing> pairings,
			List<Integer> junctionIndexes) {
		// there must be at least 4 sections and at least one junction
		if (rupture.size() < 4 || junctionIndexes.isEmpty())
			return true;
		
		// this makes sure that a junction happened last time, so 2 sections
		// from the new parent have been added to this rupture
		int lastIndexInRup = rupture.size()-1;
		if (!junctionIndexes.contains(lastIndexInRup-1))
//		if (junctionIndexes.get(junctionIndexes.size()-1) != lastIndexInRup-1)
			return true;
		
		IDPairing newSectPairing = pairings.get(pairings.size()-1);
		// we go 2 pairings back because we want the azimuth of the last two subsections on the
		// previous parent
		IDPairing prevSectPairing = pairings.get(pairings.size()-3);
		if (applyGarlockPintoMtnFix) {
			int newSectParent = rupture.get(lastIndexInRup-1).getParentSectionId();
			int prevSectParent = rupture.get(lastIndexInRup-2).getParentSectionId();
			Preconditions.checkState(newSectParent != prevSectParent);
			
			if (leftLateralFixParents.contains(newSectParent))
				newSectPairing = newSectPairing.getReversed();
			
			if (leftLateralFixParents.contains(prevSectParent))
				prevSectPairing = prevSectPairing.getReversed();
		}
		
		// make sure there are enough points to compute an azimuth change
		double newAzimuth = sectionAzimuths.get(newSectPairing);
		double prevAzimuth = sectionAzimuths.get(prevSectPairing);
		
		// check change
		double azimuthChange = Math.abs(getAzimuthDifference(prevAzimuth,newAzimuth));
		if (azimuthChange>maxAzimuthChange)
			return false;	// don't add rupture

		// check total change
		double firstAzimuth = sectionAzimuths.get(pairings.get(0));
		double totAzimuthChange = Math.abs(getAzimuthDifference(firstAzimuth,newAzimuth));
		if (totAzimuthChange>maxTotAzimuthChange)
			return false;	// don't add rupture
		
		return true;
	}

	@Override
	public boolean isContinueOnFaulure() {
		return false;
	}

	@Override
	public boolean isApplyJunctionsOnly() {
		return false;
	}
	
	/**
	 * This returns the change in strike direction in going from this azimuth1 to azimuth2,
	 * where these azimuths are assumed to be defined between -180 and 180 degrees.
	 * The output is between -180 and 180 degrees.
	 * @return
	 */
	public static double getAzimuthDifference(double azimuth1, double azimuth2) {
		double diff = azimuth2 - azimuth1;
		if(diff>180)
			return diff-360;
		else if (diff<-180)
			return diff+360;
		else
			return diff;
	}

}
