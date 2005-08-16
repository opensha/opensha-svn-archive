package org.opensha.sha.earthquake.observedEarthquake;

import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.data.Location;
import org.opensha.sha.surface.PointSurface;

/**
 * <p>Title: ObsEqkRupture </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ObsEqkRupture
    extends EqkRupture {


  private String eventId;
  private String dataSource;
  private char eventVersion;
  private GregorianCalendar originTime;
  private double hypoLocHorzErr;
  private double hypoLocVertErr;
  private double magError ;
  private String magType;


  public ObsEqkRupture(){}

  public ObsEqkRupture(String eventId, String dataSource, char eventVersion,
                       GregorianCalendar originTime, double hypoLocHorzErr,
                       double hypoLocVertErr,double magError,
                      String magType, Location hypoLoc, double mag) {

   super(mag,0,null,hypoLoc);
   //making the Obs Rupture Surface to just be the hypocenter location.
   PointSurface surface = new PointSurface(hypoLoc);
   this.setRuptureSurface(surface);
   this.setObsEqkRup(eventId,dataSource,eventVersion,originTime,hypoLocHorzErr,
                     hypoLocVertErr, magError, magType);
  }

  public String getDataSource() {
    return dataSource;
  }

  public String getEventId() {
    return eventId;
  }

  public char getEventVersion() {
    return eventVersion;
  }

  public double getHypoLocHorzErr() {
    return hypoLocHorzErr;
  }

  public double getHypoLocVertErr() {
    return hypoLocVertErr;
  }

  public GregorianCalendar getOriginTime() {
    return originTime;
  }

  public double getMagError() {
    return magError;
  }

  public String getMagType() {
    return magType;
  }

  public void setObsEqkRup(String eventId, String dataSource, char eventVersion,
                       GregorianCalendar originTime, double hypoLocHorzErr,
                       double hypoLocVertErr, double magError,
  String magType){
    this.eventId = eventId;
    this.dataSource = dataSource;
    this.eventVersion = eventVersion;
    this.originTime = originTime;
    this.hypoLocHorzErr = hypoLocHorzErr;
    this.hypoLocVertErr = hypoLocVertErr;
    this.magError = magError;
    this.magType = magType;

  }
  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public void setEventVersion(char eventVersion) {
    this.eventVersion = eventVersion;
  }

  public void setHypoLocHorzErr(double hypoLocHorzErr) {
    this.hypoLocHorzErr = hypoLocHorzErr;
  }

  public void setHypoLocVertErr(double hypoLocVertErr) {
    this.hypoLocVertErr = hypoLocVertErr;
  }

  public void setOriginTime(GregorianCalendar originTime) {
    this.originTime = originTime;
  }

  public void setMagError(double magError) {
    this.magError = magError;
  }

  public void setMagType(String magType) {
    this.magType = magType;
  }


  /**
   * Creates the XML representation for the Eqk Rupture Object
   * @return String
   */
  public String ruptureXML_String() {
    String rupInfo = "<ObsEqkRupture>\n";
    rupInfo += "<EventId>" + eventId + "</EventId>\n";
    rupInfo += "<DataSource>" + dataSource + "</DataSource>\n";
    rupInfo += "<EventVersion>" + eventVersion + "</EventVersion>\n";
    rupInfo += "<OriginTime>" + originTime.toString() + "</OriginTime>\n";
    rupInfo += "<HypoLocHorzErr>" + hypoLocHorzErr + "</HypoLocHorzErr>\n";
    rupInfo += "<HypoLocVertErr>" + hypoLocVertErr + "</HypoLocVertErr>\n";
    rupInfo += "<HypoLocVertErr>" + hypoLocVertErr + "</HypoLocVertErr>\n";
    rupInfo += "<MagError>" + magError + "</MagError>\n";
    rupInfo += "<magType>" + magType + "</magType>\n";
    rupInfo += super.ruptureXML_String();
    rupInfo += "</ObsEqkRupture>\n";
    return rupInfo;
  }

  /**
   * Gets the Info for the Observed EqkRupture
   * @return String
   */
  public String getInfo(){
    String obsEqkInfo = super.getInfo();
    obsEqkInfo += "EventId ="+eventId+"\n";
    obsEqkInfo += "DataSource ="+dataSource+"\n";
    obsEqkInfo += "EventVersion ="+eventVersion+"\n";
    obsEqkInfo += "OriginTime ="+originTime.toString()+"\n";
    obsEqkInfo += "HypoLocHorzErr ="+hypoLocHorzErr+"\n";
    obsEqkInfo += "HypoLocVertErr ="+hypoLocVertErr+"\n";
    obsEqkInfo += "MagError ="+magError+"\n";
    obsEqkInfo += "MagType ="+magType+"\n";
    return obsEqkInfo;
  }

}
