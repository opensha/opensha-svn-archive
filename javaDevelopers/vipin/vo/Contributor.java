package javaDevelopers.vipin.vo;

/**
 * <p>Title: Contributor.java </p>
 * <p>Description: This class has information about contributors</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Contributor {

  private int id=-1; // contributor ID
  private String name; // contributor name

  public Contributor() {
  }

  public int getId() { return id; }
  public String getName() { return this.name; }

  public void setId(int contributorId) {
    this.id = contributorId;
  }
  public void setName(String contributorName) {
    this.name=contributorName;
  }
}