package gov.usgs.sha.io;

import gov.usgs.util.GlobalConstants;
/**
 * <p>Title: DataFileNameSelector</p>
 *
 * <p>Description: This class gives  name of the datafile to read based on the
 * analysis option, edition and location filled in by the user.
 * </p>
 * @author Ned Field, Nitin Gupta and E.V. Leyendecker
 * @version 1.0
 */
public class DataFileNameSelector {


  public DataFileNameSelector() {}

  private final static String filePath = GlobalConstants.DATA_FILE_PATH;



  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @param lat double
   * @param lon double
   * @return String
   */
  public String getFileName(String selectedRegion, String selectedEdition,
      double lat, double lon){
    if(selectedRegion.equals(GlobalConstants.CONTER_48_STATES))
      return getFileNameFor48States(selectedEdition,lat,lon);
    else if(selectedRegion.equals(GlobalConstants.ALASKA))
      return getFileNameForAlaska(selectedEdition);
    else if(selectedRegion.equals(GlobalConstants.HAWAII))
      return getFileNameForHawaii(selectedEdition);
    else
      return getFileNameForPRVI(selectedEdition);
  }


  /**
   *
   * @param selectedRegion String
   * @param selectedEdition String
   * @return String
   */
  public String getFileName(String selectedEdition) {
    return getZipCodeFileName(selectedEdition);
  }


  private String getZipCodeFileName(String selectedEdition) {
    if (selectedEdition.equals(GlobalConstants.NEHRP_1997) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2000) ||
        selectedEdition.equals(GlobalConstants.ASCE_1998) ||
        selectedEdition.equals(GlobalConstants.ASCE_2002) ||
        selectedEdition.equals(GlobalConstants.IBC_2000) ||
        selectedEdition.equals(GlobalConstants.IBC_2003)) {
        String fileName = "1997-ZipCode-MCE-data-SsS1.txt";
        return filePath + fileName;
    }
    else if (selectedEdition.equals(GlobalConstants.NEHRP_2003) ||
        selectedEdition.equals(GlobalConstants.ASCE_2005)) {
        String fileName = "2003-ZipCode-MCEdata-SsS1.txt";
        return filePath + fileName;
    }
    return null;
  }


  /**
   *
   * @param selectedEdition String
   * @return String
   */
  private String getFileNameForAlaska(String selectedEdition){

    if (selectedEdition.equals(GlobalConstants.NEHRP_1997) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2000) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2003) ||
        selectedEdition.equals(GlobalConstants.ASCE_1998) ||
        selectedEdition.equals(GlobalConstants.ASCE_2002) ||
        selectedEdition.equals(GlobalConstants.ASCE_2005) ||
        selectedEdition.equals(GlobalConstants.IBC_2000) ||
        selectedEdition.equals(GlobalConstants.IBC_2003)) {
      String fileName = "1997-AK-MCE-R1a.rnd";
      return filePath + fileName;
    }
    return null;
  }

  private String getFileNameForPRVI(String selectedEdition){
    if (selectedEdition.equals(GlobalConstants.NEHRP_2003) ||
        selectedEdition.equals(GlobalConstants.ASCE_2005)) {
      String fileName = "2003-PVRI-MCE-R1a.rnd";
      return filePath + fileName;
    }
    return null;
  }

  private String getFileNameForHawaii(String selectedEdition){
    if (selectedEdition.equals(GlobalConstants.NEHRP_1997) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2000) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2003)||
        selectedEdition.equals(GlobalConstants.ASCE_1998) ||
        selectedEdition.equals(GlobalConstants.ASCE_2002) ||
        selectedEdition.equals(GlobalConstants.ASCE_2005) ||
        selectedEdition.equals(GlobalConstants.IBC_2000) ||
        selectedEdition.equals(GlobalConstants.IBC_2003)) {
      String fileName = "1997-HI-MCE-R1a.rnd";
      return filePath + fileName;
    }
    return null;
  }


  private String getFileNameFor48States(String selectedEdition, double lat,
                             double lon) {
    if (selectedEdition.equals(GlobalConstants.NEHRP_1997) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2000) ||
        selectedEdition.equals(GlobalConstants.ASCE_1998) ||
        selectedEdition.equals(GlobalConstants.ASCE_2002) ||
        selectedEdition.equals(GlobalConstants.IBC_2000) ||
        selectedEdition.equals(GlobalConstants.IBC_2003)) {
      if (lon >= -125 && lon <= -111 && lat >= 32 && lat <= 43) {
        String fileName = "1997-CANV-MCE-R2.rnd";
        return filePath + fileName;
      }
      else {
        String fileName = "1997-US-MCE-R1a.rnd";
        return filePath + fileName;
      }
    }
    else if (selectedEdition.equals(GlobalConstants.NEHRP_2003) ||
        selectedEdition.equals(GlobalConstants.ASCE_2005)) {
      if (lon >= -125 && lon <= -115 && lat <= 42 && lat >= 32) {
        String fileName = "2003-CANV-MCE-r1a.rnd";
        return filePath + fileName;
      }
      else if (lon >= -125 && lon <= -123 && lat <= 49 && lat >= 41) {
        String fileName = "2003-PacNW-MCE-R1a.rnd";
        return filePath + fileName;
      }
      else if (lon >= -112 && lon <= -110 && lat <= 45 && lat >= 40) {
        String fileName = "2003-SLC-MCE-R1a.rnd";
        return filePath + fileName;
      }
      else if (lon >= -92 && lon <= -88 && lat <= 38 && lat >= 35) {
        String fileName = "2003-CEUS-MCE-R1a.rnd";
        return filePath + fileName;
      }
      else{
        String fileName = "2003-US-MCE-R1a.rnd";
        return filePath + fileName;
      }
    }
    return null;
  }


}


