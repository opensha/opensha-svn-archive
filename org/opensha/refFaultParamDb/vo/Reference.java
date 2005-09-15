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
  private String shortCitation; // short citation
  private String fullBiblioReference; // full bibliographic reference

  public Reference() {
  }

  public Reference(int referenceId, String shortCitation, String fullBiblioReference) {
    this(shortCitation, fullBiblioReference);
    setReferenceId(referenceId);
  }

  public Reference(String shortCitation, String fullBiblioReference) {
    this.setShortCitation(shortCitation);
    this.setFullBiblioReference(fullBiblioReference);
  }

  public int getReferenceId() {
    return referenceId;
  }
  public void setReferenceId(int referenceId) {
    this.referenceId = referenceId;
  }

  public String getFullBiblioReference() {
    return fullBiblioReference;
  }
  public void setFullBiblioReference(String fullBiblioReference) {
    this.fullBiblioReference = fullBiblioReference;
  }
  public String getShortCitation() {
    return shortCitation;
  }
  public void setShortCitation(String shortCitation) {
    this.shortCitation = shortCitation;
  }
}
