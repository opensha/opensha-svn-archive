package javaDevelopers.vipin;

import java.util.HashMap;
import java.util.Iterator;
import org.opensha.data.LocationList;
import java.util.ArrayList;
import org.opensha.calc.RelativeLocation;
import java.io.FileWriter;
import java.io.*;
import org.opensha.exceptions.*;
import org.opensha.data.Location;

/**
 * <p>Title: PrepareTreeStructure.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PrepareTreeStructure {
  private final static double FAULT_JUMP_CUTOFF_DIST = 6;
  private final static double RUP_LENGTH = 50;
  private final static double RUP_OFFSET=5;
  private final static int DISCRETIZATION=5;

  private final static String OUT_FILENAME = "FaultSectionsConnect.txt";
  private final static String RUP_OUT_FILENAME = "Ruptures.txt";
  private HashMap faultTree ;
  private ArrayList rupList;
  private int totalRups =0;
  public PrepareTreeStructure() {
    FaultSections faultSections = new FaultSections();
    HashMap faultTraceMapping = faultSections.getAllFaultSections(); // get all the fault sections
    createTreesForFaultSections(faultTraceMapping); // discretize the section in 5km
    try {
      // now attach fault sections with other sections which are within cutoff distance
      Iterator it = faultTree.keySet().iterator();
      FileWriter fw = new FileWriter(OUT_FILENAME);
      FileWriter fwRupFile = new FileWriter(RUP_OUT_FILENAME);
      rupList = new ArrayList();
      // do for all fault sections
      while (it.hasNext()) {
        String faultSectionName = (String) it.next();
        System.out.println(faultSectionName);
        //fw.write("#" + faultSectionName + "\n");
        //fwRupFile.write("#" + faultSectionName + "\n");
        addSecondaryLinks(faultSectionName, new ArrayList());
        //find the ruptures
        getRuptures(faultSectionName);
        removeSecondaryLinks();
        // write nearby sections to file
        //writeNearbySectionsToFile(fw, sectionNearestNodeMap);

        // write ruptures to file
        //writeRupsToFile(fwRupFile);
      }
      fw.close();
      fwRupFile.close();
      System.out.println("Total ruptures="+totalRups);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  // write the ruptures to the file
  private void writeRupsToFile(FileWriter fw) {
    try {
      int numRups = 0;
      if (rupList != null)
        numRups = rupList.size();
      totalRups +=numRups;
      fw.write("#Num Ruptures=" + numRups + "\n");
      for (int i = 0; i < numRups; ++i) {
        fw.write("\t#Rupture " + i + "\n");
        ArrayList locList = (ArrayList)rupList.get(i);
        for (int j = 0; j < locList.size(); ++j)
          fw.write("\t" + (Location) locList.get(j)+"\n");
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * traverse the tree to get the ruptures
   *
   * @param faultSectionName
   * @param nearSectionsMap
   * @return
   */
  private void getRuptures(String faultSectionName) {
    // write the nearby sections and the nearest locations into a file
    Node node = (Node) faultTree.get(faultSectionName);
    ArrayList nodesList = new ArrayList();
    nodesList.add(node);
    traverse(node, nodesList, 0.0);
    // WRITE CODE TO INCLUDE RUP OFFSET
    // remove the links
    while (node != null) { // loop over all locations on this fault section to remove links
      node.removeSecondayLinks();
      node = node.getPrimaryLink();
    }
  }

  /**
   * remove the connection between sections
   */
  private void removeSecondaryLinks() {
    Iterator it = faultTree.keySet().iterator();
    // disconnect fault section
    while (it.hasNext()) {
      String faultSectionName = (String) it.next();
      Node node = (Node) faultTree.get(faultSectionName);
      while (node != null) { // loop over all locations on this fault section to remove links
        node.removeSecondayLinks();
        node = node.getPrimaryLink();
      }
    }
  }

  /**
   * connect the sections to form a tree like structure
   * @param faultSectionName
   */
  private void addSecondaryLinks(String faultSectionName, ArrayList secondaryLinksDoneSections) {
    HashMap sectionNearestNodeMap = new HashMap();
    if(secondaryLinksDoneSections.contains(faultSectionName)) return;
    secondaryLinksDoneSections.add(faultSectionName);
    Node node = (Node) faultTree.get(faultSectionName);
    // first find the nearby sections
    while (node != null) { // loop over all locations on this fault section
     /* get a list of section names and their distance near this location.
     Only include sections which are within FAULT_JUMP_CUTOFF_DIST of the location*/
      HashMap sectionNameAndDist = getAdjacentFaultSectionNode(node);
      // If same section is near to more than 1 node, then find the nearest node
      compareSectionsWithPreviousNodes(sectionNearestNodeMap, sectionNameAndDist);
      // next location on this fault section
      node = node.getPrimaryLink();
    }
    // add links to nearby section
    Iterator sectionNearestNodeMapIt= sectionNearestNodeMap.keySet().iterator();
    while(sectionNearestNodeMapIt.hasNext()) {
      String sectionName = (String)sectionNearestNodeMapIt.next();
      addSecondaryLinks(sectionName, secondaryLinksDoneSections);
      SectionNodeDist sectionNodeDist = (SectionNodeDist)sectionNearestNodeMap.get(sectionName);
      sectionNodeDist.getNode().addSecondayLink(sectionNodeDist.getSectionNode());
    }
  }

  // traverse the tree to find ruptures
  private void traverse(Node node, ArrayList nodesList, double rupLen) {

    if(rupLen>this.RUP_LENGTH)  { // if rup length is found
      // check if rupture already exists in the list
      MultiSectionRupture multiSectionRup = new MultiSectionRupture((ArrayList)nodesList.clone());
      // if rupture does not exist already, then add it
      if(!rupList.contains(multiSectionRup)) rupList.add(multiSectionRup);
      nodesList = new ArrayList();
      nodesList.add(node);
      rupLen=0.0;
      traverse(node, nodesList, 0.0);
      nodesList.remove(node);
    } else { // if more locations are required to complete the rup Length

      // first select the primary link
      Node nextNode = node.getPrimaryLink();
      if(nextNode!=null) {
        Location loc = nextNode.getLoc();
        nodesList.add(nextNode);
        traverse(nextNode, nodesList, rupLen+RelativeLocation.getApproxHorzDistance(loc, node.getLoc()));
        nodesList.remove(nextNode);
      }

      // access the secondary links
      ArrayList secondaryLinks = node.getSecondaryLinks();
      for(int i=0; secondaryLinks!=null && i<secondaryLinks.size(); ++i) {
        nextNode = (Node)secondaryLinks.get(i);
        Location loc = nextNode.getLoc();
        nodesList.add(nextNode);
        traverse(nextNode, nodesList, rupLen+RelativeLocation.getApproxHorzDistance(loc, node.getLoc()));
        nodesList.remove(nextNode);
      }

    }
  }


  private void createTreesForFaultSections(HashMap faultTraceMapping) throws
      InvalidRangeException {
    Iterator it = faultTraceMapping.keySet().iterator();
    faultTree = new HashMap();
    // create individual trees for each section
    int id =1;
    while(it.hasNext()) {
      String faultSectionName = (String)it.next();
      LocationList locList = (LocationList)faultTraceMapping.get(faultSectionName);
      Node node = new Node(id++, faultSectionName, locList.getLocationAt(0));
      faultTree.put(faultSectionName, node);
      // discretization is 5km
      int i;
      for(i=DISCRETIZATION; i<locList.size(); i=i+DISCRETIZATION) {
        Node newNode = new Node(id++, faultSectionName, locList.getLocationAt(i));
        node.addPrimaryLink(newNode);
        node = newNode;
      }
      // include the last point in the fault trace
      if((i-DISCRETIZATION)!=(locList.size()-1)) {
        Node newNode = new Node(id++, faultSectionName, locList.getLocationAt(locList.size()-1));
        node.addPrimaryLink(newNode);
        node = newNode;
      }
    }
  }

  /**
   * If same section is near more than 1 node, then find the nearest node
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
      Node minNode = null;
      // loop over all locations of this section to find nearest location
      while(locationNode!=null) {
        double currDistance = RelativeLocation.getApproxHorzDistance(node.getLoc(), locationNode.getLoc());
        if(currDistance<=FAULT_JUMP_CUTOFF_DIST && currDistance<minDist) {
          minDist = currDistance;
          minNode = locationNode;
        }
        locationNode = locationNode.getPrimaryLink();
      }
      // if there is atleast on location on this section which is near, then put it in hashmap
      if(minDist<Double.MAX_VALUE) {
        adjacentSectionsAndDist.put(sectionName,  new SectionNodeDist(sectionName, minNode, node, minDist));
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
  private Node sectionNode;
  private Node node;
  private double distance;

  public SectionNodeDist(String sectionName, Node sectionNode, Node node, double dist) {
    setSectionName(sectionName);
    setSectionNode(sectionNode);
    setNode(node);
    setDistance(dist);
  }

  public String getSectionName() { return this.sectionName; }
  public Node getNode() { return this.node; }
  public double getDistance() { return this.distance; }
  public Node getSectionNode() { return this.sectionNode; }

  public void setSectionName(String sectionName) { this.sectionName = sectionName; }
  public void setNode(Node node) { this.node = node; }
  public void setDistance(double dist) { this.distance = dist; }
  public void setSectionNode(Node sectionNode) { this.sectionNode = sectionNode; }

}