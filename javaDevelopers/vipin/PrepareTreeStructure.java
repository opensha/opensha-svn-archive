package javaDevelopers.vipin;

import java.util.HashMap;
import java.util.Iterator;
import org.opensha.data.LocationList;
import java.util.ArrayList;
import org.opensha.calc.RelativeLocation;
import java.io.FileWriter;

/**
 * <p>Title: PrepareTreeStructure.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PrepareTreeStructure {
  private final static double FAULT_JUMP_CUTOFF_DIST = 5;
  private final static String OUT_FILENAME = "FaultSectionsConnect.txt";
  private HashMap faultTree = new HashMap();
  public PrepareTreeStructure() {
    FaultSections faultSections = new FaultSections();
    HashMap faultTraceMapping = faultSections.getAllFaultSections();
    Iterator it = faultTraceMapping.keySet().iterator();

    // create individual trees for each section
    int id =1;
    while(it.hasNext()) {
      String faultSectionName = (String)it.next();
      LocationList locList = (LocationList)faultTraceMapping.get(faultSectionName);
      Node node = new Node(id++, faultSectionName, locList.getLocationAt(0));
      faultTree.put(faultSectionName, node);
      for(int i=1; i<locList.size(); ++i) {
        Node newNode = new Node(id++, faultSectionName, locList.getLocationAt(i));
        node.addNext(newNode);
        node = newNode;
      }
    }
    try {
      // now attach fault sections with other sections which are within cutoff distance
      it = faultTree.keySet().iterator();
      FileWriter fw = new FileWriter(OUT_FILENAME);
      while (it.hasNext()) {
        HashMap sectionNearestNodeMap = new HashMap();
        String faultSectionName = (String) it.next();
        System.out.println(faultSectionName);
        fw.write("#" + faultSectionName + "\n");
        Node node = (Node) faultTree.get(faultSectionName);
        while (node != null) {
          String sectionName = getAdjacentFaultSectionNode(node);
          if (sectionName != null) fw.write("\t" + sectionName + " at " + node.getLoc()+"\n");
          ArrayList nextNodes = (ArrayList) node.getNext();
          if (nextNodes == null || nextNodes.size() == 0)
            break;
          node = (Node) nextNodes.get(0);
        }
      }
      fw.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get all the faults within interFaultCutOffDistance kms of the location loc
   * This allows to find adjacent fault for fault to fault jumps
   * @param loc
   * @param interFaultCutOffDistance
   * @param adjacentFaultNames
   */
  private String getAdjacentFaultSectionNode(Node node) {
    Iterator it = faultTree.keySet().iterator();
    while(it.hasNext()) {
      String sectionName = (String)it.next();
      if(sectionName.equalsIgnoreCase(node.getFaultSectionName())) continue;
      Node locationNode = (Node)this.faultTree.get(sectionName);
      while(locationNode!=null) {
        if(RelativeLocation.getHorzDistance(node.getLoc(), locationNode.getLoc())<=FAULT_JUMP_CUTOFF_DIST)
          return sectionName;
        ArrayList nextNodes = (ArrayList)locationNode.getNext();
        if(nextNodes==null || nextNodes.size()==0) break;
        locationNode = (Node)nextNodes.get(0);
      }
    }
    return null;
  }



  public static void main(String args[]) {
    new PrepareTreeStructure();
  }
}