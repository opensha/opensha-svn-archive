package scratch.UCERF3;

import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

/**
 * 
 * This assumes that the ith FaultSectionPrefData object in the sectionDataList list has an Id equal 
 * to i (sectionDataList.get(i).getSectionId() = i); this is not checked.
 * 
 * @author field
 *
 */
public class SectionCluster extends ArrayList<Integer> {
	
	ArrayList<FaultSectionPrefData> sectionDataList;
	ArrayList<Integer> allSectionsIdList = null;
	ArrayList<ArrayList<Integer>> sectionConnectionsListList;
	ArrayList<ArrayList<Integer>> rupListIndices;			// elements here are section IDs (same as indices in sectonDataList)
	int minNumSectInRup;
	int numRupsAdded;
	double maxAzimuthChange, maxTotAzimuthChange;
	double[][] sectionAzimuths;
	
	
	/**
	 * 
	 * @param sectionDataList - this assumes that index in this list is equal to the Id of the contained FaultSectionPrefData (i = sectionDataList.get(i).getSectionId())
	 * @param minNumSectInRup
	 * @param sectionConnectionsListList
	 * @param sectionAzimuths
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 */
	public SectionCluster(ArrayList<FaultSectionPrefData> sectionDataList, int minNumSectInRup, 
			ArrayList<ArrayList<Integer>> sectionConnectionsListList, double[][] sectionAzimuths,
			double maxAzimuthChange, double maxTotAzimuthChange) {
		this.sectionDataList = sectionDataList;
		this.minNumSectInRup = minNumSectInRup;
		this.sectionConnectionsListList = sectionConnectionsListList;
		this.sectionAzimuths = sectionAzimuths;
		this.maxAzimuthChange = maxAzimuthChange;
		this.maxTotAzimuthChange = maxTotAzimuthChange;
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

	
	int rupCounterProgress =1000000;
	int rupCounterProgressIncrement = 1000000;
	  
	
	private void addRuptures(ArrayList<Integer> list) {
		int sectIndex = list.get(list.size()-1);
		int lastSect;
		if(list.size()>1)
			lastSect = list.get(list.size()-2);
		else
			lastSect = -1;   // bogus index because ectIndex is first in list
		
		// loop over branches at the present section
		ArrayList<Integer> branches = sectionConnectionsListList.get(sectIndex);
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
			/*
			// check the azimuth change
			if(list.size()>2) { // make sure there are enough points to compute an azimuth change (change 2 to 3 to get previousAzimuth below)
				double newAzimuth = sectionAzimuths[sectIndex][newSect];
				double lastAzimuth = sectionAzimuths[lastSect][sectIndex];
//				double previousAzimuth = sectionAzimuths[list.get(list.size()-3)][lastSect];
				double newLastAzimuthDiff = Math.abs(getAzimuthDifference(newAzimuth,lastAzimuth));
//				double newPreviousAzimuthDiff = Math.abs(getAzimuthDifference(newAzimuth,previousAzimuth));
//				System.out.println("newAzimuth=\t"+(int)newAzimuth+"\t"+sectIndex+"\t"+newSect);
//				System.out.println("lastAzimuth=\t"+(int)lastAzimuth+"\t"+lastSect+"\t"+sectIndex);
//				System.out.println("previousAzimuth=\t"+(int)previousAzimuth+"\t"+list.get(list.size()-3)+"\t"+lastSect);
//				System.out.println("newLastAzimuthDiff=\t"+(int)newLastAzimuthDiff);
//				System.out.println("newPreviousAzimuthDiff=\t"+(int)newPreviousAzimuthDiff);

//				if(newLastAzimuthDiff<maxAzimuthChange && newPreviousAzimuthDiff>=maxAzimuthChange) {
				if(newLastAzimuthDiff<maxAzimuthChange) {
//					ArrayList<Integer> lastRup = rupListIndices.get(rupListIndices.size()-1);
//					if(lastRup.get(lastRup.size()-1) == lastSect) {
						//stop it from going down bad branch, and remove previous rupture since it headed this way
//						System.out.println("removing: "+rupListIndices.get(rupListIndices.size()-1));
//						rupListIndices.remove(rupListIndices.size()-1);
//						numRupsAdded -= 1;
						continue;						
//					}
				}
			}
*/
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
				// Debugging exist after 100 ruptures
//				if(numRupsAdded>100) return;
			}
			
			// Iterate
			addRuptures(newList);
		}
	}

	
	private void computeRupList() {
//		System.out.println("Cluster: "+this);
		rupListIndices = new ArrayList<ArrayList<Integer>>();
		int progress = 0;
		int progressIncrement = 5;
		numRupsAdded=0;
		System.out.print("% Done:\t");
		// loop over every section as the first in the rupture
//		for(int s=0;s<size();s++) {
		for(int s=0;s<1;s++) {	// Debugging: only compute ruptures from first section
			// show progress
			if(s*100/size() > progress) {
				System.out.print(progress+"\t");
				progress += progressIncrement;
			}
			ArrayList<Integer> sectList = new ArrayList<Integer>();
			int sectIndex = get(s);
			sectList.add(sectIndex);
			addRuptures(sectList);
//			System.out.println(rupList.size()+" ruptures after section "+s);
		}
		System.out.print("\n");

		// now filter out duplicates (which would exist in reverse order)
		ArrayList<ArrayList<Integer>> newRupList = new ArrayList<ArrayList<Integer>>();
		for(int r=0; r< rupListIndices.size();r++) {
			ArrayList<Integer> rup = rupListIndices.get(r);
			ArrayList<Integer> reverseRup = new ArrayList<Integer>();
			for(int i=rup.size()-1;i>=0;i--) reverseRup.add(rup.get(i));
			if(!newRupList.contains(reverseRup)) { // keep if we don't already have
				newRupList.add(rup);
			}
		}
		rupListIndices = newRupList;
		numRupsAdded = rupListIndices.size();
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