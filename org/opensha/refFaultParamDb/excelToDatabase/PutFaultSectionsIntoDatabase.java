package org.opensha.refFaultParamDb.excelToDatabase;

import org.opensha.util.FileUtils;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import java.util.StringTokenizer;
import org.opensha.data.estimate.MinMaxPrefEstimate;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.data.estimate.NormalEstimate;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.estimate.DiscreteValueEstimate;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.data.Location;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import java.io.FileWriter;
import org.opensha.calc.RelativeLocation;

/**
 * <p>Title: PutFaultSectionsIntoDatabase.java </p>
 * <p>Description: this class reads the 2006_fault_sections.MID and
 * 2006_fault_sections.MIF files. These files contain fault section information
 * and provided by Chris Wills.
 *  It puts the information into database.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PutFaultSectionsIntoDatabase {
  // input files
  private final static String INPUT_FILE1 = "2006_fault_sections.MID";
  private final static String INPUT_FILE2 = "2006_fault_sections.MIF";
  // units for various paramaters
  private final static String DIP_UNITS = "degrees";
  private final static String DIP_DIRECTION_UNITS = "degrees";
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static String DEPTH_UNITS = "km";
  private final static String RAKE_UNITS = "degrees";
  private final static String ASEISMIC_SLIP_FACTOR_UNITS = " ";

  // default values for missing fields
  private final static double DEFAULT_AVE_RAKE = 0;
  private final static double DEFAULT_AVE_SLIP_RATE=0;
  private final static double DEFAULT_AVE_SLIP_UNCERT=0;
  private final static double DEFAULT_AVE_DIP_EST=0;
  private final static double DEFAULT_UPPER_DEPTH=0;
  private final static double DEFAULT_LOWER_DEPTH=0;

  // Strings that mark the start and end of each fault trace in the file
  private final static String PLINE = "Pline";
  private final static String  PEN = "Pen";

  // fault section trace file
  private ArrayList faultSectionTraceLines;
  private int nextTraceStartIndex=0;

  // DAO to put fault sections to database
  private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);

  // filename to compare the dips
  private final static String DIP_FILENAME = "DipComparisons.txt";
  private FileWriter fwDip;

  /**
   * Put fault sections into the database
   */
  public PutFaultSectionsIntoDatabase() {
    try {
      ArrayList fileLines1 = FileUtils.loadFile(INPUT_FILE1);
      ArrayList faultSectionsList = new ArrayList();
      // load fault trace
      faultSectionTraceLines = FileUtils.loadFile(INPUT_FILE2);

      fwDip = new FileWriter(this.DIP_FILENAME);
      // load all the fault sections and their properties (except Fault Trace)
      for(int i=0; i<fileLines1.size(); ++i) {
        try {
          FaultTrace faultSectionTrace = getNextTrace("temp");
          FaultSectionVer2 faultSection = getFaultSection( (String) fileLines1.get(i));
          faultSectionTrace.setName(faultSection.getSectionName());
          faultSectionsList.add(faultSection);
          faultSection.setFaultTrace(faultSectionTrace);
          double dipDirection = 90+RelativeLocation.getDirection(faultSectionTrace.getLocationAt(0),
              faultSectionTrace.getLocationAt(faultSectionTrace.getNumLocations()-1)).getAzimuth();
          if(dipDirection<0) dipDirection+=360;
          else if(dipDirection>360) dipDirection-=360;
          fwDip.write(dipDirection+"\n");
          // add fault section to the database
          //faultSectionDAO.addFaultSection(faultSection);
        }catch(Exception e) {
          e.printStackTrace();
          //System.exit(0);
          //System.out.println("Problem "+ e.getMessage());
        }
      }
      fwDip.close();
      fileLines1 = null;
      faultSectionTraceLines = null;
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get next fault trace from the file
   * @param fileLines ArrayList
   * @param startIndex int
   * @return FaultTrace
   */
  private FaultTrace getNextTrace(String faultSectionName) {
    boolean found = false;
    double lat=0, lon;
    FaultTrace sectionTrace = new FaultTrace(faultSectionName);
    for(; !found ;++nextTraceStartIndex) {
      String line = ((String)faultSectionTraceLines.get(nextTraceStartIndex)).trim();
      if(line.startsWith(PLINE)) {
        found = true;
        String locString = ((String)faultSectionTraceLines.get(++nextTraceStartIndex)).trim();
        while(!locString.startsWith(PEN)) {
          StringTokenizer tokenizer = new StringTokenizer(locString);
          lon = Double.parseDouble(tokenizer.nextToken());
          try {
            lat = Double.parseDouble(tokenizer.nextToken());
          }catch(Exception e) {
            System.out.println(locString);
            //System.exit(0);
          }
          sectionTrace.addLocation(new Location(lat,lon));
          locString = ((String)faultSectionTraceLines.get(++nextTraceStartIndex)).trim();
        }
      }
    }
    return sectionTrace;
  }


  /**
   * Get fault section from a file line
   *
   * @param line
   * @return
   */
  private FaultSectionVer2 getFaultSection(String line) {
    FaultSectionVer2 faultSection = new FaultSectionVer2();
    String comments = "";
    int index = line.indexOf("\",\"");
    // fault section name
    faultSection.setSectionName(line.substring(1, index));
    System.out.println(faultSection.getSectionName());
    String line2 = line.substring(index+2);
    index = line2.indexOf("\",\"");
    //System.out.println(line2);
    //System.out.println(index);

    //used only when 2 incompatible models exist, lists which model includes this fault
    String model = removeQuotes(line2.substring(0, index+1));
    //System.out.println(model);
    //System.exit(0);
    if(!model.equalsIgnoreCase("")) {
      comments = comments + "Model="+model+"\n";
    }
    StringTokenizer tokenizer = new StringTokenizer(line2.substring(index+2),",");
    //2002 or CFM
    faultSection.setSource(removeQuotes(tokenizer.nextToken().trim()));
    //from 2002 model, text representation of rake, blank if not available
    String faultType = removeQuotes(tokenizer.nextToken().trim());
    if(!faultType.equalsIgnoreCase("")) {
      comments = comments+"FaultType="+faultType+"\n";
    }
    //converted from sense of movement field in 2002 model using Aki and Richards convention, blank if not available
    String rake = tokenizer.nextToken().trim();
    // set rake to default if it is not present
    if(rake.equalsIgnoreCase("") || rake.equalsIgnoreCase("\"\"")) {
      System.out.println("Default rake set for "+faultSection.getSectionName());
      rake = "\"" + this.DEFAULT_AVE_RAKE+"\"";
    }
    faultSection.setAveRakeEst(this.getMinMaxPrefEstimateInstance(
          removeQuotes(rake), this.RAKE_UNITS));



    //from CFM when available, 2002 if not, average of dips of "panels" in CFM-R
    String dip = removeQuotes(tokenizer.nextToken().trim());
    if(dip.equalsIgnoreCase("")) {
      System.out.println("Default Dip set for "+faultSection.getSectionName());
      dip = "" + this.DEFAULT_AVE_DIP_EST;
    }
    faultSection.setAveDipEst(this.getMinMaxPrefEstimateInstance(dip, this.DIP_UNITS));

    // from CFM when available, 2002 if not
    String dipDirection = removeQuotes(tokenizer.nextToken().trim());
    if(!dipDirection.equalsIgnoreCase("")) {
      comments = comments+"Dip Direction="+dipDirection+"\n";
    }
    try {
      fwDip.write(faultSection.getSectionName() + ";" + dip+";"+dipDirection + ";");
    }catch(Exception e) {
      e.printStackTrace();
    }
    //from 2002 model, blank if not available
    String slipRate = removeQuotes(tokenizer.nextToken().trim());
    if(slipRate.equalsIgnoreCase(""))  {
      System.out.println("Default slip rate set for "+faultSection.getSectionName());
      slipRate = "" + this.DEFAULT_AVE_SLIP_RATE;
    }

    //from 2002 model, blank if not available
    String slipRateUncert = removeQuotes(tokenizer.nextToken().trim());
    if(slipRateUncert.equalsIgnoreCase("")) {
      System.out.println("Default slip rate Uncertainity set for "+faultSection.getSectionName());
      slipRateUncert = "" + this.DEFAULT_AVE_SLIP_UNCERT;
    }

    Estimate slipRateEst = new NormalEstimate(Double.parseDouble(slipRate),
        Double.parseDouble(slipRateUncert));
    faultSection.setAveLongTermSlipRateEst(new EstimateInstances(slipRateEst, this.SLIP_RATE_UNITS));

    // from 2002 model
    String rank = removeQuotes(tokenizer.nextToken().trim());
    if( !rank.equalsIgnoreCase("")) {
      comments=comments+"Rank="+rank+"\n";
    }
    //from CFM when available, 2002 if not
    String upperDepth = removeQuotes(tokenizer.nextToken().trim());
    if(upperDepth.equalsIgnoreCase("")) {
      System.out.println("Default upper depth set for "+faultSection.getSectionName());
      upperDepth=""+this.DEFAULT_UPPER_DEPTH;
    }
    faultSection.setAveUpperDepthEst(getMinMaxPrefEstimateInstance(upperDepth, this.DEPTH_UNITS));

    //from CFM when available, 2002 if not
    String lowerDepth = removeQuotes(tokenizer.nextToken().trim());
    if(lowerDepth.equalsIgnoreCase("")) {
      System.out.println("Default lower depth set for "+faultSection.getSectionName());
      lowerDepth=""+this.DEFAULT_LOWER_DEPTH;
    }
    faultSection.setAveLowerDepthEst(getMinMaxPrefEstimateInstance(lowerDepth, this.DEPTH_UNITS));

    // calculated from dip and top and bottom depths
    String width = removeQuotes(tokenizer.nextToken().trim());
    if( !width.equalsIgnoreCase("")) {
      comments=comments+"Width="+width+"\n";
    }
    //from 2002 model
    String fileName = removeQuotes(tokenizer.nextToken().trim());
    if(!fileName.equalsIgnoreCase("")) {
      comments=comments+"FileName="+fileName+"\n";
    }

    // Aseimsic Slip Factor
    double rfactor1=0,rfactor1Wt=0, rfactor2=0, rfactor2Wt=0, rfactor3=0, rfactor3Wt=0;
    String str=null;
    //from Working Group 2003 report, 0 if not available
    str = tokenizer.nextToken().trim();
    if(str.equalsIgnoreCase("")) str="0";
    rfactor1 = Double.parseDouble(str);
    //from Working Group 2003 report, 0 if not available
    str = tokenizer.nextToken().trim();
    if(str.equalsIgnoreCase("")) str="0";
    rfactor1Wt = Double.parseDouble(str);
    //from Working Group 2003 report, 1 if not available
    str = tokenizer.nextToken().trim();
    if(str.equalsIgnoreCase("")) str="1";
    rfactor2 = Double.parseDouble(str);
    //from Working Group 2003 report, 1 if not available
    str = tokenizer.nextToken().trim();
    if(str.equalsIgnoreCase("")) str="1";
    rfactor2Wt = Double.parseDouble(str);
    //from Working Group 2003 report, 0 if not available
    str = tokenizer.nextToken().trim();
    if(str.equalsIgnoreCase("")) str="0";
    rfactor3 = Double.parseDouble(str);
    //from Working Group 2003 report, 0 if not available
    str = tokenizer.nextToken().trim();
    if(str.equalsIgnoreCase("")) str="0";
    rfactor3Wt = Double.parseDouble(str);

    faultSection.setAseismicSlipFactorEst(getAsesmicEstimate(rfactor1, rfactor1Wt,
        rfactor2, rfactor2Wt, rfactor3, rfactor3Wt, this.ASEISMIC_SLIP_FACTOR_UNITS));
    return faultSection;
  }

  /**
   * Removes the double quotes from the string ends
   * @param str
   * @return
   */
  private String removeQuotes(String str) {
    if(str.charAt(0)=='"') return str.substring(1, str.length()-1);
    else return str;
  }

  /**
   * Get aseismic slip estimate
   * @param rfactor1
   * @param rfactor1Wt
   * @param rfactor2
   * @param rfactor2Wt
   * @param rfactor3
   * @param rfactor3Wt
   * @param units
   * @return
   */
  private EstimateInstances getAsesmicEstimate(double rfactor1, double rfactor1Wt,
                                      double rfactor2, double rfactor2Wt, double rfactor3, double rfactor3Wt,
                                      String units) {
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    func.set(1-rfactor1, rfactor1Wt);
    func.set(1-rfactor2, rfactor2Wt);
    func.set(1-rfactor3, rfactor3Wt);
    DiscreteValueEstimate estimate = new DiscreteValueEstimate(func, false);
    return new EstimateInstances(estimate, units);
  }


  /**
   * Make Min-Max-Pref estimate from a single value.
   * That single value can be the Pref Estimate
   *
   * @param value
   * @return
   */
  private EstimateInstances getMinMaxPrefEstimateInstance(String value, String units) {
    Estimate estimate =  new MinMaxPrefEstimate(Double.NaN, Double.NaN,
                                                Double.parseDouble(value),
                                                Double.NaN, Double.NaN, Double.NaN);
    return new EstimateInstances(estimate, units);
  }

  public static void main(String[] args) {
     new PutFaultSectionsIntoDatabase();
  }


}
