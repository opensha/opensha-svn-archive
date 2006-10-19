package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.rupCalc;

import java.util.ArrayList;

/**
 * <p>Title: TreeBranch.java </p>
 * <p>Description: This refers to a branch of a tree </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TreeBranch {
  private String subSectionName;
  private ArrayList<String> adjacentSubSections = new ArrayList<String>();

  public TreeBranch(String subSectionName) {
	  this.subSectionName = subSectionName;
  }

  /**
   * Get sub-section name
   * @return
   */
  public String getSubSectionName() {
    return subSectionName;
  }

  /**
   * Get number of adjacent subsections to this section
   * @return
   */
  public int getNumAdjacentSubsections() {
	  return this.adjacentSubSections.size();
  }
  
  
  /**
   * Get the adjancet subsection at the specified index
   * @param index
   * @return
   */
  public String getAdjacentSubSection(int index) {
	  return adjacentSubSections.get(index);
  }
  
  /**
   * Add adjacent sub section (if it does not exist already)
   * @param subSectionName
   */
  public void addAdjacentSubSection(String subSectionName) {
	  if(!adjacentSubSections.contains(subSectionName))
		  adjacentSubSections.add(subSectionName);
  }
  
  /**
   * Is the specified sub section name adjacent ?
   * 
   * @param subSectionName
   * @return
   */
  public boolean isAdjacentSubSection(String subSectionName) {
	  return adjacentSubSections.contains(subSectionName);
  }

}