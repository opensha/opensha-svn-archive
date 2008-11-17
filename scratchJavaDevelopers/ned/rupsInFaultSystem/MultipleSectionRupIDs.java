/**
 * 
 */
package scratchJavaDevelopers.ned.rupsInFaultSystem;

import java.util.ArrayList;



/**
 * @author Ned Field
 *
 */
public class MultipleSectionRupIDs {
	
	public ArrayList<Integer> subSectionIdsList;
	
	public  MultipleSectionRupIDs(ArrayList<Integer> subSectionList) {
		subSectionIdsList = (ArrayList<Integer>) subSectionList.clone();
	}
	
	/**
	 * Get the number of subsections in this rupture
	 * @return
	 */
	public int getNumSubSections() {
		return this.subSectionIdsList.size();
	}
	
	/**
	 * Get subsection at ith index
	 * 
	 * @param index
	 * @return
	 */
	public int getSubSectionID(int index) {
		return subSectionIdsList.get(index);
	}
	
	
	/**
	   * Finds whether 2 ruptures are same or not. It checks:
	   *  1. Number of subsections in both ruptures are same
	   *  2. First and last subsections are same in both
	   *  
	   * @param rup
	   * @return
	   */
	  public boolean equals(Object obj) {
	    if(! (obj instanceof MultipleSectionRupIDs)) return false;
	    MultipleSectionRupIDs rup = (MultipleSectionRupIDs) obj;
	    
	    // check that both contain same number of subsections
	    if(rup.getNumSubSections()!=this.getNumSubSections()) return false;
	    
	    // check that first and last subsections are same
	    int firstSubSec1 = this.getSubSectionID(0);
	    int lastSubSec1 = this.getSubSectionID(this.getNumSubSections()-1);
	    int firstSubSec2 = rup.getSubSectionID(0);
	    int lastSubSec2 = rup.getSubSectionID(rup.getNumSubSections()-1);
	    
	    if((firstSubSec1==firstSubSec2 ||  firstSubSec1==lastSubSec2) &&
	    		(lastSubSec1==firstSubSec2 ||  lastSubSec1==lastSubSec2)) {
	    	return true;
	    }
	    return false;
	  }
	
}
