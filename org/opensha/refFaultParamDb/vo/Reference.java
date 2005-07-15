package org.opensha.refFaultParamDb.vo;

/**
 * <p>Title: Reference.java </p>
 * <p>Description: This class has information about the references </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Reference {

  private int referenceId=-1; // reference ID
  private String referenceName; // reference name

  public Reference() {
  }

  public Reference(int referenceId, String referenceName) {
    setReferenceId(referenceId);
    setReferenceName(referenceName);
  }

  public Reference(String referenceName) {
    setReferenceName(referenceName);
  }

  public int getReferenceId() {
    return referenceId;
  }
  public void setReferenceId(int referenceId) {
    this.referenceId = referenceId;
  }
  public void setReferenceName(String referenceName) {
    this.referenceName = referenceName;
  }
  public String getReferenceName() {
    return referenceName;
  }
}
