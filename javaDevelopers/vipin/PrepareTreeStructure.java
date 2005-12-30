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
import java.text.DecimalFormat;

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
  private final static int DISCRETIZATION=5; // fault section discretization
  private final static Location LOCATION = new Location(31.5, -115.0);
  private final static DecimalFormat decimalFormat = new DecimalFormat("0.00###");

  public final static String DEFAULT_FAULT_SECTIONS_OUT_FILENAME = "javaDevelopers\\vipin\\FaultSections.txt";
  public final static String DEFAULT_RUP_OUT_FILENAME = "javaDevelopers\\vipin\\Ruptures_Allkm.txt";
  public String rupOutFilename = DEFAULT_RUP_OUT_FILENAME;
  public String faultSectionsOutFilename = DEFAULT_FAULT_SECTIONS_OUT_FILENAME;
  private HashMap faultTree ;
  private ArrayList rupList;
  private ArrayList faultSectionPrintOrder;
  //private double rupLength;
  private int rupCounter =0;
  private boolean doOneSection= false; // whether we need to do just a particular section
  private int sectionIndex=-1;
  private String faultSectionFilename1, faultSectionFilename2, faultSectionFilename3;
  private boolean writeSectionsToFile = true;

  public PrepareTreeStructure() {
  }

  public void doProcessing()  {
    try {
      rupList = new ArrayList();
      faultSectionPrintOrder = new ArrayList();
      FaultSections faultSections;
      if(faultSectionFilename1==null) faultSections = new FaultSections();
      else faultSections = new FaultSections(faultSectionFilename1, faultSectionFilename2, faultSectionFilename3);
      HashMap faultTraceMapping = faultSections.getAllFaultSections(); // get all the fault sections
      createTreesForFaultSections(faultTraceMapping); // discretize the section in 5km
      findAllRuptures();
      System.out.println("Total ruptures="+rupList.size());

      // write ruptures to file
      FileWriter fwRupFile = new FileWriter(rupOutFilename);
      fwRupFile.write("#Num Ruptures=" + rupList.size() + "\n");
      RuptureFileReaderWriter.writeRupsToFile(fwRupFile, rupList);
      fwRupFile.close();
      // write fault sections to file
      if(writeSectionsToFile) {
        FileWriter fw = new FileWriter(faultSectionsOutFilename);
        writeFaultSectionsToFile(fw, faultTraceMapping);
        fw.close();
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Whether we need to write sections to file or not
   *
   * @param isWriteSection
   */
  public void writeSectionsToFile(boolean isWriteSection) {
    this.writeSectionsToFile = isWriteSection;
  }

  /**
   * Set the filename where output ruptures will be saved
   *
   * @param filename
   */
  public void setRupOutputFilename(String filename) {
    this.rupOutFilename = filename;
  }

  /**
   * Output file where sub sampled sections will be saved
   *
   * @param filename
   */
  public void setSectionsOutputFilename(String filename) {
    this.faultSectionsOutFilename = filename;
  }

  public void doForTheSectionIndex(int sectionIndex) {
    this.sectionIndex = sectionIndex;
    this.doOneSection = true;
  }

  /**
   * Set file names so that fault sections can be loaded
   * @param file1
   * @param file2
   * @param file3
   */
  public void setFaultSectionFilenames(String file1, String file2, String file3) {
    faultSectionFilename1 = file1;
    faultSectionFilename2= file2;
    faultSectionFilename3=file3;
  }

  /**
   * Write the fault sections into a file
   * @param fw
   * @param faultTraceMapping
   */
  private void writeFaultSectionsToFile(FileWriter fw, HashMap faultTraceMapping) {
    try {
      Iterator it = faultTree.keySet().iterator();
      while (it.hasNext()) {
        String faultSectionName = (String) it.next();
        fw.write("#" + faultSectionName + "\n");
        Node node = (Node) faultTree.get(faultSectionName);
        while (node != null) {
          fw.write("\t" + node.getLoc() + "\n");
          node = node.getPrimaryLink();
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }

  }



  /**
   * It finds the fault section closest to LOCATION. It is put first into the list
   * Then it finds fault section nearest to previous found section and it goes on
   *
   * @return
   */
  private ArrayList sortFaultSectionsByLocation() {
    ArrayList sortedSectionNames = new ArrayList();
    Iterator it = faultTree.keySet().iterator();
    // save the section names and lat of first location into arraylists
    double minDist = Double.MAX_VALUE;
    String firstSectionName = null;
    // find the first section that we will process
    while(it.hasNext()) {
      String  faultSectionName = (String) it.next();
      sortedSectionNames.add(faultSectionName);
      Location loc = ((Node)faultTree.get(faultSectionName)).getLoc();
      double distance = RelativeLocation.getApproxHorzDistance(loc, LOCATION);
      if(distance<minDist) {
        minDist = distance;
        firstSectionName = faultSectionName;
      }
    }
    // put the first section on first location in arraylist
    sortedSectionNames.remove(firstSectionName);
    sortedSectionNames.add(0, firstSectionName);
    // now find the subsequent sections based on their distance from previously found section
    for(int i=0; i<(sortedSectionNames.size()-1); ++i) {
      String nextSectionName = null;
      minDist = Double.MAX_VALUE;
      Location loc = ((Node)faultTree.get((String)sortedSectionNames.get(i))).getLoc();
      for(int j=i+1; j<sortedSectionNames.size(); ++j) {
        Location loc1 = ((Node)faultTree.get((String)sortedSectionNames.get(j))).getLoc();
        double distance = RelativeLocation.getApproxHorzDistance(loc, loc1);
        if(distance<minDist) {
          minDist = distance;
          nextSectionName = (String)sortedSectionNames.get(j);
        }
      }
      sortedSectionNames.remove(nextSectionName);
      sortedSectionNames.add(i+1, nextSectionName);
    }

    return sortedSectionNames;

  }


  private void findAllRuptures()  {
    // SORT THE FAULT SECTIONS BASED ON THE LATITUDES OF FIRST POINT of Fault trace.
    // It will help in better visualization so that there are few jumps in viz
    ArrayList sortedSectionNames = sortFaultSectionsByLocation();

    try {
      // now attach fault sections with other sections which are within cutoff distance
      Iterator it = sortedSectionNames.iterator();
      // do for all fault sections
      ArrayList processedFaultSections = new ArrayList();
      int i=0;
      while (it.hasNext()) {
        String faultSectionName = (String) it.next();
        // if this fault section has already been processed, do not process it again
        if(processedFaultSections.contains(faultSectionName)) continue;
        // if we  want this to process just one fault section
        System.out.println((++i)+"\t"+faultSectionName);
        if(i!=this.sectionIndex && this.doOneSection) continue;
        System.out.println("Processing "+faultSectionName+" .........");
        processFaultSection(faultSectionName);
        processedFaultSections.add(faultSectionName);
        for(int j=0; j< this.faultSectionPrintOrder.size() && !doOneSection; ++j) {
          faultSectionName = (String)faultSectionPrintOrder.get(j);
          if(processedFaultSections.contains(faultSectionName)) continue;
          System.out.println((++i)+"\t"+faultSectionName);
          processFaultSection(faultSectionName);
          j=0;
          processedFaultSections.add(faultSectionName);
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void processFaultSection(String faultSectionName) {
    // add the links to nearby fault sections
    addSecondaryLinks(faultSectionName, new ArrayList());
    // get all the ruptures
    getRuptures(faultSectionName);
    // remove the secondary links
    removeSecondaryLinks();
  }



  /**
   * traverse the tree to get the ruptures
   *
   * @param faultSectionName
   * @param nearSectionsMap
   * @return
   */
  private void getRuptures(String faultSectionName) {
    // get the first node and start finding ruptures recursively
    Node rootNode = (Node) faultTree.get(faultSectionName);
    traverseStartingFromRoot(rootNode);
  }

  /**
   * Find the ruptures starting from each location on this fault section
   * @param node
   */
  private void traverseStartingFromRoot(Node node) {
    /* loop over all locations on this faultsection to find the ruptures which
    start from each location*/
    while(node!=null) {
      ArrayList nodesList = new ArrayList();
      nodesList.add(node);
      traverse(node, nodesList, 0.0f);
      double offsetDist=0;
      Location loc1 = node.getLoc();
      Location loc2;
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
    Node prevNode = null;
    // first find the nearby sections
    while (node != null) { // loop over all locations on this fault section
      // add a link to the previous location as well to make a bi-directional tree
      if(prevNode!=null) node.addSecondayLink(prevNode);
     /* get a list of section names and their distance near this location.
     Only include sections which are within FAULT_JUMP_CUTOFF_DIST of the location*/
      HashMap sectionNameAndDist = getAdjacentFaultSectionNode(node);
      // If same section is near to more than 1 node, then find the nearest node
      compareSectionsWithPreviousNodes(sectionNearestNodeMap, sectionNameAndDist);
      // next location on this fault section
      prevNode = node;
      node = node.getPrimaryLink();
    }
    // add links to nearby section
    Iterator sectionNearestNodeMapIt= sectionNearestNodeMap.keySet().iterator();
    while(sectionNearestNodeMapIt.hasNext()) {
      String sectionName = (String)sectionNearestNodeMapIt.next();
      if(secondaryLinksDoneSections.contains(sectionName)) continue;
      addSecondaryLinks(sectionName, secondaryLinksDoneSections);
      SectionNodeDist sectionNodeDist = (SectionNodeDist)sectionNearestNodeMap.get(sectionName);
      sectionNodeDist.getNode().addSecondayLink(sectionNodeDist.getSectionNode());
    }
  }

  // traverse the tree to find ruptures
  private void traverse(Node node, ArrayList nodesList, float rupLen) {

    if(nodesList.size()>1) { // sinle location ruptures are excluded
        // check if rupture already exists in the list
        //MultiSectionRupture multiSectionRup = new MultiSectionRupture( (ArrayList)
         //   nodesList.clone());
        Location startLoc = ( (Node) nodesList.get(0)).getLoc();
        Location endLoc = ( (Node) nodesList.get(nodesList.size()-1)).getLoc();

        // strategy to eliminate duplcate ruptures
        if((endLoc.getLatitude()>startLoc.getLatitude()) ||
           (endLoc.getLatitude()==startLoc.getLatitude() && endLoc.getLongitude()>startLoc.getLongitude())) {
           MultiSectionRupture multiSectionRup = new MultiSectionRupture( (ArrayList)nodesList.clone());
           multiSectionRup.setLength(rupLen);
           rupList.add(multiSectionRup);
           for (int i = 0; i < nodesList.size() && !this.doOneSection; ++i)
            this.addToFaultSectionPrintOrder( ( (Node) nodesList.get(i)).
                                             getFaultSectionName());
        }

        // if rupture does not exist already, then add it
        /*if (!rupList.contains(multiSectionRup)) {
          multiSectionRup.setLength(rupLen);
          // add the section names involved in this rupture to a list so that these sections can be processed next
          for (int i = 0; i < nodesList.size(); ++i)
            this.addToFaultSectionPrintOrder( ( (Node) nodesList.get(i)).
                                             getFaultSectionName());
          rupList.add(multiSectionRup);
        }*/
      }

      // first select the primary link
      Node nextNode;

      // first select the primary link
      nextNode = node.getPrimaryLink();
      if(nextNode!=null && !nodesList.contains(nextNode)) {
        Location loc = nextNode.getLoc();
        nodesList.add(nextNode);
        traverse(nextNode, nodesList, rupLen+(float)RelativeLocation.getApproxHorzDistance(loc, node.getLoc()));
        nodesList.remove(nextNode);
      }

      // access the secondary links
      ArrayList secondaryLinks = node.getSecondaryLinks();
      for(int i=0; secondaryLinks!=null && i<secondaryLinks.size(); ++i) {
        nextNode = (Node)secondaryLinks.get(i);
        if(nodesList.contains(nextNode)) continue;
        Location loc = nextNode.getLoc();
        nodesList.add(nextNode);
        float dist = 0.0f;
        // calculate distance only if locations lie on same fault section
        if(node.getFaultSectionName().equalsIgnoreCase(nextNode.getFaultSectionName()))
           dist = (float)RelativeLocation.getApproxHorzDistance(loc, node.getLoc());
        traverse(nextNode, nodesList, rupLen+dist);
        nodesList.remove(nextNode);
      }
  }

  /**
   * List maintaining the ordering according to which fault sections will be processed
   * @param sectionName
   */
  private void addToFaultSectionPrintOrder(String sectionName) {
    if(!faultSectionPrintOrder.contains(sectionName)) {
      faultSectionPrintOrder.add(sectionName);
    }
  }

  /**
   * Divide each section to subsections after sub sampling.
   * Also create a tree for each section
   *
   * @param faultTraceMapping
   * @throws InvalidRangeException
   */
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
        node.setPrimaryLink(newNode);
        node = newNode;
      }
      // include the last point in the fault trace
      if((i-DISCRETIZATION)!=(locList.size()-1)) {
        Node newNode = new Node(id++, faultSectionName, locList.getLocationAt(locList.size()-1));
        node.setPrimaryLink(newNode);
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


  /**
   * It either accepts 6 arguments or no arguments at all.
   * If 3 arguments are provided :
   * 1. Name of Rupture output file
   * 2. Name of Sections output file
   * 3. Whether we need to write sections to the file
   * 4. Index of Fault Section to process
   * 5. Filename1 for fault section
   * 6. Filename2 for fault section
   * 7. Filename3 for fault section
   * @param args
   */
  public static void main(String args[]) {
    PrepareTreeStructure prepareTreeStruct= new PrepareTreeStructure();
    if(args.length!=0){
      prepareTreeStruct.setRupOutputFilename(args[0]); // rupture output file name
      prepareTreeStruct.setSectionsOutputFilename(args[1]); // section output file
      // whether we need to write sections to a file
      prepareTreeStruct.writeSectionsToFile(Boolean.valueOf(args[2]).booleanValue());
      // section index for which processing needs to be done, if -1, we do for all sections
      int sectionIndex = Integer.parseInt(args[3]);
      if(sectionIndex!=-1)
        prepareTreeStruct.doForTheSectionIndex(Integer.parseInt(args[3]));
      // filenames to read the intial fault sections
      prepareTreeStruct.setFaultSectionFilenames(args[4], args[5], args[6]);
    }
    prepareTreeStruct.doProcessing();
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