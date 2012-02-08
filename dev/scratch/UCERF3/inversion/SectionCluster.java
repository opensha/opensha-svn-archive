package scratch.UCERF3.inversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.StatUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.utils.IDPairing;

/**
 * 
 * This assumes that the ith FaultSectionPrefData object in the sectionDataList list has an Id equal 
 * to i (sectionDataList.get(i).getSectionId() = i); this is not checked.
 * 
 * @author field
 *
 */
public class SectionCluster extends ArrayList<Integer> {

	protected final static boolean D = true;  // for debugging

	ArrayList<FaultSectionPrefData> sectionDataList;
	ArrayList<Integer> allSectionsIdList = null;
	List<List<Integer>> sectionConnectionsListList;
	ArrayList<ArrayList<Integer>> rupListIndices;			// elements here are section IDs (same as indices in sectonDataList)
	int minNumSectInRup;
	int numRupsAdded;
	double maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff;
	Map<IDPairing, Double> sectionAzimuths;
	Map<Integer, Double> rakesMap;


	/**
	 * 
	 * @param sectionDataList - this assumes that index in this list is equal to the Id of the contained FaultSectionPrefData (i = sectionDataList.get(i).getSectionId())
	 * @param minNumSectInRup
	 * @param sectionConnectionsListList
	 * @param subSectionAzimuths
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 * @param maxRakeDiff
	 */
	public SectionCluster(ArrayList<FaultSectionPrefData> sectionDataList, int minNumSectInRup, 
			List<List<Integer>> sectionConnectionsListList, Map<IDPairing, Double> subSectionAzimuths,
			Map<Integer, Double> rakesMap, double maxAzimuthChange, double maxTotAzimuthChange, double maxRakeDiff) {
		this.sectionDataList = sectionDataList;
		this.minNumSectInRup = minNumSectInRup;
		this.sectionConnectionsListList = sectionConnectionsListList;
		this.sectionAzimuths = subSectionAzimuths;
		this.rakesMap = rakesMap;
		this.maxTotAzimuthChange = maxTotAzimuthChange;
		this.maxAzimuthChange = maxAzimuthChange;
		this.maxRakeDiff = maxRakeDiff;
	}


	/**
	 * This returns the number of sections in the cluster
	 * @return
	 */
	public int getNumSections() {
		return this.size();
	}


	/**
	 * This returns a list of the IDs of all  sections in the cluster
	 * @return
	 */
	public ArrayList<Integer> getAllSectionsIdList() {
		if(allSectionsIdList==null) computeAllSectionsIdList();
		return allSectionsIdList;
	}


	private void computeAllSectionsIdList() {
		allSectionsIdList = new ArrayList<Integer>();
		for(int i=0; i<size();i++) allSectionsIdList.add(sectionDataList.get(get(i)).getSectionId());
	}


	public int getNumRuptures() {
		if(rupListIndices== null)  computeRupList();
		//		return rupListIndices.size();
		return numRupsAdded;
	}


	public ArrayList<ArrayList<Integer>> getSectionIndicesForRuptures() {
		if(rupListIndices== null)
			computeRupList();
		return rupListIndices;
	}


	public ArrayList<Integer> getSectionIndicesForRupture(int rthRup) {
		if(rupListIndices== null)
			computeRupList();
		return rupListIndices.get(rthRup);
	}



//	int rupCounterProgress =1000000;
//	int rupCounterProgressIncrement = 1000000;
	int rupCounterProgress =100000;
	int rupCounterProgressIncrement = 100000;


	private void addRuptures(ArrayList<Integer> list) {
		int sectIndex = list.get(list.size()-1);
		int lastSect;
		if(list.size()>1)
			lastSect = list.get(list.size()-2);
		else
			lastSect = -1;   // bogus index because ectIndex is first in list

		// loop over branches at the present section
		List<Integer> branches = sectionConnectionsListList.get(sectIndex);
		for(int i=0; i<branches.size(); i++) { 
			Integer newSect = branches.get(i);

			// avoid looping back on self or to previous section
			if(list.contains(newSect) || newSect == lastSect) continue;

			// debugging stuff:
			/*			if(list.size()>1)
				System.out.println("lastSect=\t"+lastSect+"\t"+this.sectionDataList.get(lastSect).getName());
			System.out.println("sectIndex=\t"+sectIndex+"\t"+this.sectionDataList.get(sectIndex).getName());
			System.out.println("newSect=\t"+newSect+"\t"+this.sectionDataList.get(newSect).getName());
			 */	

			// check the azimuth change   
			if(list.size()>2) { // make sure there are enough points to compute an azimuth change (change 2 to 3 to get previousAzimuth below)
				double newAzimuth;
				try {
					newAzimuth = sectionAzimuths.get(new IDPairing(sectIndex, newSect));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//					FaultSectionPrefData data1 = sectionDataList.get(sectIndex);
//					FaultSectionPrefData data2 = sectionDataList.get(newSect);
//					double dist = data1.getStirlingGriddedSurface(1.0, false, false)
//							.getMinDistance(data2.getStirlingGriddedSurface(1.0, false, false));
//					System.out.println("Dist from "+sectIndex+" to "+newSect+": "+dist);
//					System.out.println("Az size: "+sectionAzimuths.size());
//					for (int[] ind : sectionAzimuths.keySet()) {
//						if (ind[0] == sectIndex && ind[1] == newSect) {
//							System.out.println("WTF? It's here: "+sectionAzimuths.get(ind));
//							System.out.println("contains? "+sectionAzimuths.containsKey(ind1));
//							System.out.println("mine: "+ind1[0]+" "+ind1[1]);
//							System.out.println("theirs: "+ind[0]+" "+ind[1]);
//							System.out.println("Arrays.equals? " +Arrays.equals(ind1, ind));
//							System.out.println("hash mine: " +Arrays.hashCode(ind1));
//							System.out.println("hash theirs: " +Arrays.hashCode(ind));
//							System.out.println("ind1.equals(ind)? " +ind1.equals(ind));
//							System.out.println("ind.equals(ind1)? " +ind.equals(ind1));
//							break;
//						}
//					}
					System.exit(0);
					throw new RuntimeException();
				}
//				double lastAzimuth = sectionAzimuths[lastSect][sectIndex];
				double lastAzimuth = sectionAzimuths.get(new IDPairing(lastSect, sectIndex));
//				double firstAzimuth = sectionAzimuths[list.get(0)][list.get(1)];
				double firstAzimuth = sectionAzimuths.get(new IDPairing(list.get(0), list.get(1)));
				double TotAzimuthChange = Math.abs(getAzimuthDifference(firstAzimuth,newAzimuth));
				//				double previousAzimuth = sectionAzimuths[list.get(list.size()-3)][lastSect];
				double newLastAzimuthDiff = Math.abs(getAzimuthDifference(newAzimuth,lastAzimuth));
				//				double newPreviousAzimuthDiff = Math.abs(getAzimuthDifference(newAzimuth,previousAzimuth));


				//				if(newLastAzimuthDiff<maxAzimuthChange && newPreviousAzimuthDiff>=maxAzimuthChange) {
				if(newLastAzimuthDiff>maxAzimuthChange) {
					//		ArrayList<Integer> lastRup = rupListIndices.get(rupListIndices.size()-1);
					continue;
					//		if(lastRup.get(lastRup.size()-1) == lastSubSect) {
					//			//stop it from going down bad branch, and remove previous rupture since it headed this way
					//			System.out.println("removing: "+rupListIndices.get(rupListIndices.size()-1));
					//			rupListIndices.remove(rupListIndices.size()-1);
					//			numRupsAdded -= 1;
					//			continue;						
					//		}
				} 
				if(TotAzimuthChange>maxTotAzimuthChange) {
					continue;
				}

			}

			ArrayList<Integer> newList = (ArrayList<Integer>)list.clone();
			newList.add(newSect);
			if(newList.size() >= minNumSectInRup)  {// it's a rupture
				rupListIndices.add(newList);
				numRupsAdded += 1;
				// show progress
				if(numRupsAdded >= rupCounterProgress) {
					System.out.println(numRupsAdded);
					rupCounterProgress += rupCounterProgressIncrement;
				}
				//				System.out.println("adding: "+newList);
				// Debugging exit after 100 ruptures
				//				if(numRupsAdded>100) return;
			}

			// Iterate
			// if(numRupsAdded<100)	// FOR DEBUGGING!
			addRuptures(newList);
		}
	}

	private void computeRupList() {
		//		if(D) System.out.println("Computing Rupture List in SectionCluster");
		//		System.out.println("Cluster: "+this);
		rupListIndices = new ArrayList<ArrayList<Integer>>();
		int progress = 0;
		int progressIncrement = 5;
		numRupsAdded=0;
		//		System.out.print("% Done:\t");
		// loop over every section as the first in the rupture
		for(int s=0;s<size();s++) {
			//		for(int s=0;s<1;s++) {	// Debugging: only compute ruptures from first section
			// show progress
			//if(s*100/size() > progress) {
			//	System.out.print(progress+"\t");
			//	progress += progressIncrement;
			//}
			ArrayList<Integer> sectList = new ArrayList<Integer>();
			int sectIndex = get(s);
			sectList.add(sectIndex);
			addRuptures(sectList);
			//			System.out.println(rupList.size()+" ruptures after section "+s);
		}
		System.out.print("\n");
		if (D) System.out.println("Added "+numRupsAdded+" rups so far!");

		if (D) System.out.print("\nFiltering out duplicates...");
		// now filter out duplicates (which would exist in reverse order)
		ArrayList<ArrayList<Integer>> newRupList = new ArrayList<ArrayList<Integer>>();
//		for(int r=0; r< rupListIndices.size();r++) {
//			ArrayList<Integer> rup = rupListIndices.get(r);
//			ArrayList<Integer> reverseRup = new ArrayList<Integer>();
//			for(int i=rup.size()-1;i>=0;i--) reverseRup.add(rup.get(i));
//			if(!newRupList.contains(reverseRup)) { // keep if we don't already have
//				newRupList.add(rup);
//			}
//		}
		// TODO we need to unit test this
		HashSet<ArrayList<Integer>> hashedSorted = new HashSet<ArrayList<Integer>>();
		ArrayList<Integer> sortedList;
		for (ArrayList<Integer> rup : rupListIndices) {
			sortedList = new ArrayList<Integer>();
			sortedList.addAll(rup);
			Collections.sort(sortedList);
			if (hashedSorted.contains(sortedList)) {
				// this means it's already in here, do nothing
				continue;
			}
			newRupList.add(rup);
			hashedSorted.add(sortedList);
		}
		hashedSorted = null;
		if (D) System.out.println("DONE.");
		rupListIndices = newRupList;
		numRupsAdded = rupListIndices.size();

		if (D) System.out.println(numRupsAdded + " potential ruptures");

		/*
		// Remove ruptures where subsection strikes have too big a spread
		// System.out.println("maxStrikeDiff = "+maxStrikeDiff); //maximum allowed difference in strikes between any two subsections in the same rupture, in degrees
		ArrayList<ArrayList<Integer>> toRemove = new ArrayList<ArrayList<Integer>>();
		for(int r=0; r< numRupsAdded;r++) {
			ArrayList<Integer> rup = rupListIndices.get(r);
			//System.out.println("rup = " + rup);
			ArrayList<Double> strikes = new ArrayList<Double>(rup.size());
			for (int i=0; i<rup.size(); i++) {
			//  System.out.println("Avg. strike = " + subSectionPrefDataList.get(rup.get(i)).getFaultTrace().getAveStrike());
				if (sectionDataList.get(rup.get(i)).getFaultTrace().getAveStrike()<0)
					System.out.println("Error:										Strike < 0 !!!");
				if (sectionDataList.get(rup.get(i)).getFaultTrace().getAveStrike()<180)
					strikes.add(sectionDataList.get(rup.get(i)).getFaultTrace().getAveStrike());	
				else
					strikes.add(sectionDataList.get(rup.get(i)).getFaultTrace().getAveStrike()-180);	

			}
		    Collections.sort(strikes);
			ArrayList<Double> anglediffs = new ArrayList<Double>(rup.size());
		    for (int i=0; i<rup.size()-1; i++) {
		    	anglediffs.add(strikes.get(i+1)-strikes.get(i));
		    }
		    anglediffs.add(strikes.get(0)+180-strikes.get(rup.size()-1));
		    double strikeDiff = 180-Collections.max(anglediffs);
		    //System.out.println("strikeDiff = " + strikeDiff);
		    if (strikeDiff>maxStrikeDiff) {
		    	toRemove.add(rup);
		    }
		}
		for(int i=0; i< toRemove.size();i++) {
			newRupList.remove(toRemove.get(i));
		}

		rupListIndices = newRupList;
		numRupsAdded = rupListIndices.size();

		System.out.println(numRupsAdded + " ruptures that pass subsection strikes test");
		 */

		// Remove ruptures where subsection rakes have too big a spread
		// System.out.println("maxRakeDiff = "+maxRakeDiff); //maximum allowed difference in strikes between any two subsections in the same rupture, in degrees
		if (D) System.out.print("Filtering via rake...");
		double[] rakes, anglediffs2;
		for(int r=rupListIndices.size()-1; r>=0;r--) {
			ArrayList<Integer> rup = rupListIndices.get(r);
			rakes = new double[rup.size()];
			if (rakesMap == null)
				for (int i=0; i<rup.size(); i++)
					rakes[i] = sectionDataList.get(rup.get(i)).getAveRake();
			else
				for (int i=0; i<rup.size(); i++)
					rakes[i] = rakesMap.get(rup.get(i));
			Arrays.sort(rakes);
			anglediffs2 = new double[rup.size()];
			for (int i=0; i<rup.size()-1; i++) {
				anglediffs2[i] = rakes[i+1]-rakes[i];
			}
			anglediffs2[anglediffs2.length-1] = rakes[0]+360-rakes[rup.size()-1];
			double rakeDiff = 360-StatUtils.max(anglediffs2);
			if (rakeDiff>maxRakeDiff) {
				rupListIndices.remove(r);
			}
		}
		if (D) System.out.println("DONE.");

		rupListIndices = newRupList;
		numRupsAdded = rupListIndices.size();

		if (D) System.out.println(numRupsAdded + " ruptures that pass subsection rakes test");


	}


	public void writeRuptureSectionNames(int index) {
		ArrayList<Integer> rupture = rupListIndices.get(index);
		System.out.println("Rutpure "+index);
		for(int i=0; i<rupture.size(); i++ ) {
			System.out.println("\t"+this.sectionDataList.get(rupture.get(i)).getName());
		}

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