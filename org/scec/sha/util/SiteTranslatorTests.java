package org.scec.sha.util;

import java.io.*;

/**
 * <p>Title: SiteTranslatorTests</p>
 * <p>Description: Test the wills-class and basin-depth servlets,
 * and the siteTranlator, by putting the following on a line of an ascii file
 * for each site in the LA region  </p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class SiteTranslatorTests {

  private FileWriter fw;

  public SiteTranslatorTests(){
    try{
      fw = new FileWriter("region_info.txt");
      fw.write("Lat\t\tLon\t\tWillsClass\t\tBasinDepth\t\tSiteTranslatorFlag\t\tSiteTypeName\t\tSiteTypeValue\n");
      fw.write("---\t\t---\t\t----------\t\t----------\t\t------------------\t\t------------\t\t-------------\n\n");
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void writeToSiteParamFile(String fileLine){
    try{
      fw.write(fileLine);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void closeSiteParamFile(){
    try{
      fw.close();
    }catch(Exception e ){
      e.printStackTrace();
    }
  }

}