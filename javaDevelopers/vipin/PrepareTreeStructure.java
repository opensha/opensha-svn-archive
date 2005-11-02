package javaDevelopers.vipin;

import java.util.HashMap;
import java.util.Iterator;
import org.opensha.data.LocationList;
import java.util.ArrayList;
import org.opensha.calc.RelativeLocation;
import java.io.FileWriter;
import java.io.*;
import org.opensha.exceptions.*;

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
    createTreesForFaultSections(faultTraceMapping);
    try {
      // now attach fault sections with other sections which are within cutoff distance
      Iterator it = faultTree.keySet().iterator();
      FileWriter fw = new FileWriter(OUT_FILENAME);
      // do for all fault sections
      while (it.hasNext()) {
        HashMap sectionNearestNodeMap = new HashMap();
        String faultSectionName = (String) it.next();
        System.out.println(faultSectionName);
        fw.write("#" + faultSectionName + "\n");
        Node node = (Node) faultTree.get(faultSectionName);
        while (node != null) { // loop over all locations on this fault section
          /* get a list of section names and their distance near this location.
          Only include sections which are within FAULT_JUMP_CUTOFF_DIST of the location*/
          HashMap sectionNameAndDist = getAdjacentFaultSectionNode(node);
          // If same section is near to more than 1 node, then find the neaest node
          compareSectionsWithPreviousNodes(sectionNearestNodeMap, sectionNameAndDist);
          // next location on this fault section
          ArrayList nextNodes = (ArrayList) node.getNext();
          if (nextNodes == null || nextNodes.size() == 0)
            break;
          node = (Node) nextNodes.get(0);
        }
        // write nearby sections to file
        writeNearbySectionsToFile(fw, sectionNearestNodeMap);
      }
      fw.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void createTreesForFaultSections(HashMap faultTraceMapping) throws
      InvalidRangeException {
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
  }

  /**
   * If same section is near to more than 1 node, then find the neaest node
   *
   * @param sectionNearestNodeMap
   * @param sectionNameAndDist
   */
  private void compareSectionsWithPreviousNodes(HashMap sectionNearestNodeMap,
                                                HashMap sectionNameAndDist) {
    Iterator sectionNameDistIt = sectionNameAndDist.keySet().iterator();
    while(sectionNameDistIt.hasNext()) {
      String sectionName = (String)sectionNameDistIt.next();
      SectionNodeDist currentNodeSectionDist = (SectionNodeDist)sectionNameAndDist.get(sectionName);
      if(!sectionNearestNodeMap.containsKey(sectionName))
        sectionNearestNodeMap.put(sectionName, currentNodeSectionDist);
      else {
        SectionNodeDist previousNodeSectionDist = (SectionNodeDist)sectionNearestNodeMap.get(sectionName);
        if(previousNodeSectionDist.getDistance()>currentNodeSectionDist.getDistance())
          sectionNearestNodeMap.put(sectionName, currentNodeSectionDist);
      }
    }
  }

  /**
   * Write nearby sections to file
   *
   * @param fw
   * @param sectionNearestNodeMap
   * @throws IOException
   */
  private void writeNearbySectionsToFile(FileWriter fw,
                                         HashMap sectionNearestNodeMap) throws IOException {
    // write the nearby sections and the nearest locations into a file
    Iterator sectionNearestNodeMapIt= sectionNearestNodeMap.keySet().iterator();
    while(sectionNearestNodeMapIt.hasNext()) {
      String sectionName = (String)sectionNearestNodeMapIt.next();
      SectionNodeDist sectionNodeDist = (SectionNodeDist)sectionNearestNodeMap.get(sectionName);
      fw.write("\t"+sectionName+" at "+sectionNodeDist.getNode().getLoc()+"\n");
    }
  }

  /**
   * Get all the faults within interFaultCutOffDistance kms of the location loc
   * This allows to find adjacent fault for fault to fault jumps
   * @param loc
   * @param interFaultCutOffDistance
   * @param adjacentFaultNames
   */
  private HashMap getAdjacentFaultSectionNode(Node node) {
    Iterator it = faultTree.keySet().iterator();
    HashMap adjacentSectionsAndDist = new HashMap();
    while(it.hasNext()) {
      String sectionName = (String)it.next();
      if(sectionName.equalsIgnoreCase(node.getFaultSectionName())) continue;
      Node locationNode = (Node)this.faultTree.get(sectionName);
      double minDist= Double.MAX_VALUE;
      // loop over all locations of this section to find nearest location
      while(locationNode!=null) {
        double currDistance = RelativeLocation.getHorzDistance(node.getLoc(), locationNode.getLoc());
        if(currDistance<=FAULT_JUMP_CUTOFF_DIST && currDistance<minDist)
          minDist = currDistance;
        ArrayList nextNodes = (ArrayList)locationNode.getNext();
        if(nextNodes==null || nextNodes.size()==0) break;
        locationNode = (Node)nextNodes.get(0);
      }
      // if there is atleast on location on this section which is near, then put it in hashmap
      if(minDist<Double.MAX_VALUE) {
        adjacentSectionsAndDist.put(sectionName, new SectionNodeDist(sectionName, node, minDist));
      }
    }
    return adjacentSectionsAndDist;
  }

  public static void main(String args[]) {
    new PrepareTreeStructure();
  }
}


/**
 * <p>Title: SectionNodeDist.java </p>
 * <p>Description: This class saves the section near to a location and the distance
 * between location and nearest location on other section</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
class SectionNodeDist {
  private String sectionName;
  private Node node;
  private double distance;

  public SectionNodeDist(String sectionName, Node node, double dist) {
    setSectionName(sectionName);
    setNode(node);
    setDistance(dist);
  }

  public String getSectionName() { return this.sectionName; }
  public Node getNode() { return this.node; }
  public double getDistance() { return this.distance; }

  public void setSectionName(String sectionName) { this.sectionName = sectionName; }
  public void setNode(Node node) { this.node = node; }
  public void setDistance(double dist) { this.distance = dist; }

}