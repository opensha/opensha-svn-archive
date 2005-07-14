package javaDevelopers.vipin.vo;
import java.util.Date;

/**
 * <p>Title: PaleoSite.java </p>
 * <p>Description: This class saves the information about a paleo site in the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSite {
  private int siteId=-1;
  private int  faultId;
  private Date entryDate;
  private Contributor siteContributor;
  private SiteType siteType;
  private String siteName;
  private float siteLat1;
  private float siteLon1;
  private float siteElevation1;
  private float siteLat2;
  private float siteLon2;
  private float siteElevation2;
  private int representativeStrandIndex;
  private String generalComments;
  private String entryComments;
  private int oldSiteId;

  public PaleoSite() {
  }

  public void setSiteId(int siteId) { this.siteId = siteId; }
  public int getSiteId() { return this.siteId; }


  public Contributor getSiteContributor() { return this.siteContributor; }
  public void setSiteContributor(Contributor siteContributor) { this.siteContributor = siteContributor; }

  public void setSiteType(SiteType siteType) { this.siteType = siteType; }
  public SiteType getSiteType() { return this.siteType; }

  public void setSiteName(String siteName) { this.siteName = siteName;}
  public String getSiteName() { return this.siteName; }



  public int getRepresentativeStrandIndex() { return this.representativeStrandIndex; }
  public void setRepresentativeStrandIndex(int repStrandIndex) { this.representativeStrandIndex = repStrandIndex; }

  public void setOldSiteId(int oldSiteId) { this.oldSiteId = oldSiteId; }
  public int getOldSiteId() { return this.oldSiteId; }
  public float getSiteLon2() {
    return siteLon2;
  }
  public float getSiteLon1() {
    return siteLon1;
  }
  public float getSiteLat2() {
    return siteLat2;
  }
  public float getSiteLat1() {
    return siteLat1;
  }
  public void setSiteLat1(float siteLat1) {
    this.siteLat1 = siteLat1;
  }
  public void setSiteLat2(float siteLat2) {
    this.siteLat2 = siteLat2;
  }
  public void setSiteLon1(float siteLon1) {
    this.siteLon1 = siteLon1;
  }
  public void setSiteLon2(float siteLon2) {
    this.siteLon2 = siteLon2;
  }
  public float getSiteElevation2() {
    return siteElevation2;
  }
  public float getSiteElevation1() {
    return siteElevation1;
  }
  public void setSiteElevation1(float siteElevation1) {
    this.siteElevation1 = siteElevation1;
  }
  public void setSiteElevation2(float siteElevation2) {
    this.siteElevation2 = siteElevation2;
  }
  public String getEntryComments() {
    return entryComments;
  }
  public void setEntryComments(String entryComments) {
    this.entryComments = entryComments;
  }
  public Date getEntryDate() {
    return entryDate;
  }
  public void setEntryDate(Date entryDate) {
    this.entryDate = entryDate;
  }
  public int getFaultId() {
    return faultId;
  }
  public void setFaultId(int faultId) {
    this.faultId = faultId;
  }
  public void setGeneralComments(String generalComments) {
    this.generalComments = generalComments;
  }
  public String getGeneralComments() {
    return generalComments;
  }
}