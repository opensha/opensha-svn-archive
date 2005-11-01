package javaDevelopers.vipin;

import java.util.ArrayList;
import org.opensha.data.Location;

/**
 * <p>Title: Node.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Node {
  private int id;
  private Location loc;
  private String faultSectionName;
  private ArrayList next; // locations connecting with this node.
  private boolean isRoot;

  public Node() {
  }

  public boolean isRoot() {
    return isRoot;
  }



  public Node(int id, String faultSecName, Location loc) {
    this.setFaultSectionName(faultSecName);
    this.setLoc(loc);
    setId(id);
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFaultSectionName() {
    return faultSectionName;
  }

  public Location getLoc() {
    return loc;
  }

  public ArrayList getNext() {
    return next;
  }

  public void setFaultSectionName(String faultSectionName) {
    this.faultSectionName = faultSectionName;
  }

  public void setLoc(Location loc) {
    this.loc = loc;
  }

  public void addNext(Node nextNode) {
    if(this.next==null) this.next = new ArrayList();
    next.add(nextNode);
  }

  /**
   * Get all the nodes connected with this node
   * @return
   */
  public ArrayList getAllNodes() {
    ArrayList nodes = new ArrayList();
    addNodes(nodes, this);
    return nodes;
  }

  public void addNodes(ArrayList nodes, Node node) {
    nodes.add(node);
    for(int i=0; next!=null && i<next.size(); ++i) addNodes(nodes, (Node)node.next.get(i));
  }
}