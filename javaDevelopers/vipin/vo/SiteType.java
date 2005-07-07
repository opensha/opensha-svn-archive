package javaDevelopers.vipin.vo;

/**
 * <p>Title: SiteType.java </p>
 * <p>Description: This saves the various site types associated with a
 * paloe site. Example of site types are : “trench”, “geologic”, “survey/cultural” </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SiteType {
  private int siteTypeId=-1;
  private Contributor contributor;
  private String siteType;

  public SiteType() {
  }

  public SiteType(int siteTypeId, String siteTypeName, Contributor contributor) {
    setSiteTypeId(siteTypeId);
    setContributor(contributor);
    setSiteType(siteTypeName);
  }

  public SiteType(String siteTypeName, Contributor contributor) {
   setContributor(contributor);
   setSiteType(siteTypeName);
 }


  public void setSiteTypeId(int siteTypeId) {
    this.siteTypeId = siteTypeId;
  }
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }
  public void setSiteType(String siteType) {
    this.siteType=siteType;
  }

  public int getSiteTypeId() { return this.siteTypeId; }
  public Contributor getContributor() { return this.contributor; }
  public String getSiteType() { return this.siteType; }

}