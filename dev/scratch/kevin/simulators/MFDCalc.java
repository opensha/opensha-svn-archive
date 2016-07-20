package scratch.kevin.simulators;

import java.util.HashSet;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.SimulatorElement;

public class MFDCalc {
	
	public static IncrementalMagFreqDist calcMFD(
			List<? extends SimulatorEvent> events, HashSet<Integer> elementsInRegion,
			double duration, double minMag, int num, double delta) {
		
		IncrementalMagFreqDist mfd = new IncrementalMagFreqDist(minMag, num, delta);
		double myMin = minMag-0.5*delta;
		double myMax = mfd.getMaxX()+0.5*delta;
		for (SimulatorEvent e : events) {
			double mag = e.getMagnitude();
			if (mag < myMin || mag > myMax)
				continue;
			int ind = mfd.getClosestXIndex(mag);
			double eventRate;
			if (elementsInRegion == null)
				eventRate = 1;
			else
				eventRate = getFractInsideRegion(e, elementsInRegion);
			mfd.set(ind, mfd.getY(ind)+eventRate);
		}
		if (duration > 0)
			for (int i=0; i<mfd.size(); i++)
				mfd.set(i, mfd.getY(i) / duration);
		
		return mfd;
	}
	
	public static double getFractInsideRegion(SimulatorEvent e, HashSet<Integer> elementsInRegion) {
		double tot = e.getNumElements();
		double inside = 0d;
		for (int elemID : e.getAllElementIDs())
			if (elementsInRegion.contains(elemID))
				inside++;
		return inside / tot;
	}
	
	public static HashSet<Integer> getElementsInsideRegion(
			List<SimulatorElement> elements, Region region) {
		HashSet<Integer> elementsInRegion = new HashSet<Integer>();
		for (SimulatorElement elem : elements) {
			double lat = 0; 
			double lon = 0;
			int num = 0;
			// just averaging to get middle, should be fine for this use
			for (Location loc : elem.getVertices()) {
				lat += loc.getLatitude();
				lon += loc.getLongitude();
				num++;
			}
			lat /= (double)num;
			lon /= (double)num;
			if (region.contains(new Location(lat, lon)))
				elementsInRegion.add(elem.getID());
		}
		return elementsInRegion;
	}
	
	public static double calcMinBelow(double eventMin, double delta) {
		double min;
		for (min=0; min<=eventMin; min+=delta);
		return min - delta;
	}
	
	public static int calcNum(double min, double eventMax, double delta) {
		int num = 0;
		for (double max=min; max<=eventMax; max+=delta)
			num++;
		return num;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
