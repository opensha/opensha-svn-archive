package scratch.kevin.simulators;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * This is the simplest rupture identifier implementation - it defines a match as any rupture that includes
 * the given section and is within the specified magnitude range.
 * @author kevin
 *
 */
public class ElementMagRangeDescription extends AbstractRuptureIdentifier {
	
	private List<Integer> elementIDs;
	private double minMag, maxMag;
	
	public ElementMagRangeDescription(int elementID, double minMag, double maxMag) {
		this(Lists.newArrayList(elementID), minMag, maxMag);
	}
	
	public ElementMagRangeDescription(List<Integer> elementIDs, double minMag, double maxMag) {
		this.elementIDs = elementIDs;
		this.minMag = minMag;
		this.maxMag = maxMag;
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		double mag = event.getMagnitude();
		if (mag < minMag || mag >= maxMag)
			return false;
		for (int elementID : elementIDs)
			if (!Ints.contains(event.getAllElementIDs(), elementID))
				return false;
		return true;
	}
	
	public List<Integer> getElementIDs() {
		return elementIDs;
	}

	public void setElementID(int elementID) {
		this.elementIDs = Lists.newArrayList(elementID);
	}

	public void addElementID(int elementID) {
		this.elementIDs.add(elementID);
	}
	
	public int removeElementID(int elementID) {
		int ind = elementIDs.indexOf(elementID);
		if (ind < 0)
			return -1;
		this.elementIDs.remove(ind);
		return ind;
	}

	public void setElementID(List<Integer> elementIDs) {
		this.elementIDs = elementIDs;
	}

	public double getMinMag() {
		return minMag;
	}

	public void setMinMag(double minMag) {
		this.minMag = minMag;
	}

	public double getMaxMag() {
		return maxMag;
	}

	public void setMaxMag(double maxMag) {
		this.maxMag = maxMag;
	}

	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		
		Preconditions.checkState(geomFile.exists());
		Preconditions.checkState(eventFile.exists());
		
		General_EQSIM_Tools simTools = new General_EQSIM_Tools(geomFile);
		simTools.read_EQSIMv04_EventsFile(eventFile);
		
		List<EQSIM_Event> events = simTools.getEventsList();
		
		ElementMagRangeDescription descr = new ElementMagRangeDescription(1267, 7.2, 7.5);
		
		List<EQSIM_Event> matches = descr.getMatches(events);
		
		System.out.println("Got "+matches.size()+" matches!");
		HashSet<Integer> matchIDs = new HashSet<Integer>();
		for (EQSIM_Event match : matches) {
			matchIDs.add(match.getID());
			System.out.println(match.getID()+". mag="+match.getMagnitude()+", years="+match.getTimeInYears());
		}
		
		System.out.println("Quickly Triggered Events (1 day):");
		double day = 24*60*60;
		for (EQSIM_Event e : events) {
			if (matchIDs.contains(e.getID()))
				continue;
			if (e.getMagnitude() < 6.5)
				continue;
			double time = e.getTime();
			for (EQSIM_Event m : matches) {
				double mtime = m.getTime();
				if (time >= mtime && time <= (mtime + day)) {
					System.out.println(e.getID()+". mag="+e.getMagnitude()+", years="+e.getTimeInYears());
					break;
				}
			}
		}
	}

}
