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
  private Date effectiveDate;
  private Contributor siteContributor;
  private SiteType siteType;
  private String siteName;
  private float siteLat;
  private float siteLon;
  private float siteElevation;
  private int representativeStrandIndex;
  private String comments;
  private int oldSiteId;

  public PaleoSite() {
  }

  public void setSiteId(int siteId) { this.siteId = siteId; }
  public int getSiteId() { return this.siteId; }

  public Date getEffectiveDate() { return this.effectiveDate; }
  public void setEffectiveDate(Date effectDate) { this.effectiveDate = effectDate; }

  public Contributor getSiteContributor() { return this.siteContributor; }
  public void setContributor(Contributor siteContributor) { this.siteContributor = siteContributor; }

  public void setSiteType(SiteType siteType) { this.siteType = siteType; }
  public SiteType getSiteType() { return this.siteType; }

 public void setSiteName(String siteName) { this.siteName = siteName;}
 public String getSiteName() { return this.siteName; }

 public float getSiteLat() { return this.siteLat; }
 public void setSiteLat(float siteLatitude) { this.siteLat = siteLatitude; }

 public float getSiteLon() { return this.siteLon; }
 public void setSiteLon(float siteLongitude) { this.siteLon = siteLongitude; }

 public float getSiteElevation() { return this.siteElevation; }
 public void setSiteElevation(float elevation) { this.siteElevation = elevation; }

 public int getRepresentativeStrandIndex() { return this.representativeStrandIndex; }
 public void setRepresentativeStrandIndex(int repStrandIndex) { this.representativeStrandIndex = repStrandIndex; }

 public String getComments() { return this.comments; }
 public void setComments(String comments) { this.comments = comments; }

 public void setOldSiteId(int oldSiteId) { this.oldSiteId = oldSiteId; }
 public int getOldSiteId() { return this.oldSiteId; }
}