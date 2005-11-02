package javaDevelopers.vipin;

import java.util.ArrayList;

/**
 * <p>Title: MultiSectionRupture.java  </p>
 * <p>Description: Data structure to represent the multi fault section rupture</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MultiSectionRupture {
  private ArrayList nodeList;

  public MultiSectionRupture(ArrayList nodesList) {
    this.nodeList = nodesList;
  }

  public ArrayList getNodesList() {
    return this.nodeList;
  }

  /**
   * Finds whether 2 ruptures are same or not. It checks:
   *  1. number of points on both ruptures are same.
   *  2. the locations on 1 rupture also exist for the second rupture
   * @param rup
   * @return
   */
  public boolean equals(MultiSectionRupture rup) {
    ArrayList rupNodesList = rup.getNodesList();
    // check that number of points in both ruptures are same
    if(this.nodeList.size()!=rupNodesList.size()) return false;
    // check that locations on one ruptures also exist on other rupture
    for(int i=0; i<nodeList.size(); ++i) {
      Node node = (Node)nodeList.get(i);
      boolean found = false;
      for(int j=0; j<rupNodesList.size() && !found; ++j) {
        if(node.getId()==((Node)rupNodesList.get(j)).getId()) found = true;
      }
      if(!found) return false;
    }
    return true;
  }


}