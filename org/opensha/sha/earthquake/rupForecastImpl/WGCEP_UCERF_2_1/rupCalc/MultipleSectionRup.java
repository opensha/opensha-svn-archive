/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.rupCalc;

import java.util.ArrayList;



/**
 * @author vipingupta
 *
 */
public class MultipleSectionRup {
	
	public ArrayList<String> subSectionNamesList;
	
	public  MultipleSectionRup(ArrayList<String> subSectionList) {
		subSectionNamesList = (ArrayList<String>) subSectionList.clone();
	}
	
	/**
	 * Get the number of subsections in this rupture
	 * @return
	 */
	public int getNumSubSections() {
		return this.subSectionNamesList.size();
	}
	
	/**
	 * Get subsection at ith index
	 * 
	 * @param index
	 * @return
	 */
	public String getSubSection(int index) {
		return subSectionNamesList.get(index);
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
	    if(! (obj instanceof MultipleSectionRup)) return false;
	    MultipleSectionRup rup = (MultipleSectionRup) obj;
	    
	    // check that both contain same number of subsections
	    if(rup.getNumSubSections()!=this.getNumSubSections()) return false;
	    
	    // check that first and last subsections are same
	    String firstSubSec1 = this.getSubSection(0);
	    String lastSubSec1 = this.getSubSection(this.getNumSubSections()-1);
	    String firstSubSec2 = rup.getSubSection(0);
	    String lastSubSec2 = rup.getSubSection(rup.getNumSubSections()-1);
	    
	    if((firstSubSec1.equalsIgnoreCase(firstSubSec2) ||  firstSubSec1.equalsIgnoreCase(lastSubSec2)) &&
	    		(lastSubSec1.equalsIgnoreCase(firstSubSec2) ||  lastSubSec1.equalsIgnoreCase(lastSubSec2))) {
	    	return true;
	    }
	    return false;
	  }
	
}
