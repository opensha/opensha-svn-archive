package gov.usgs.sha.io;

import gov.usgs.util.*;

/**
 * <p>Title: DataFileNameSelector</p>
 *
 * <p>Description: This class gives  name of the datafile to read based on the
 * analysis option, edition, location and type of Hazard Curve filled in by the user.
 * </p>
 * @author Ned Field, Nitin Gupta and E.V. Leyendecker
 * @version 1.0
 */
public class DataFileNameSelectorForHazardCurves {

  public DataFileNameSelectorForHazardCurves() {}

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
                            double lat, double lon, String hazardCurveType) {
    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      return getFileNameFor48States(selectedEdition, lat, lon, hazardCurveType);
    }
    else if (selectedRegion.equals(GlobalConstants.ALASKA)) {
      return getFileNameForAlaska(selectedEdition, hazardCurveType);
    }
    else if (selectedRegion.equals(GlobalConstants.HAWAII)) {
      return getFileNameForHawaii(selectedEdition, hazardCurveType);
    }
    else {
      return getFileNameForPRVI(selectedEdition, hazardCurveType);
    }
  }

  /**
   * Gets the filename for the region of Alaska
   * @param selectedEdition String
   * @return String
   */
  private String getFileNameForAlaska(String selectedEdition,
                                      String selectedHazardCurveType) {

    if (selectedHazardCurveType.equals(GlobalConstants.PGA)) {
      String fileName = "1998-AK-HAZ-0_0sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_TWO_SEC)) {
      String fileName = "1998-AK-HAZ-0_2sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_THREE_SEC)) {
      String fileName = "1998-AK-HAZ-0_3sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_ONE_SEC)) {
      String fileName = "1998-AK-HAZ-1_0sec-R3a.rnd";
      return filePath + fileName;
    }

    return null;
  }

  private String getFileNameForPRVI(String selectedEdition,
                                    String selectedHazardCurveType) {
    if (selectedHazardCurveType.equals(GlobalConstants.PGA)) {
      String fileName = "2003-PVRI-HAZ-0_0sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_TWO_SEC)) {
      String fileName = "2003-PVRI-HAZ-0_2sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_ONE_SEC)) {
      String fileName = "2003-PVRI-HAZ-1_0sec-R3a.rnd";
      return filePath + fileName;
    }
    return null;
  }

  /**
   * Getting the filenames for the region of Hawaii
   * @param selectedEdition String
   * @param selectedHazardCurveType String
   * @return String
   */
  private String getFileNameForHawaii(String selectedEdition,
                                      String selectedHazardCurveType) {
    if (selectedHazardCurveType.equals(GlobalConstants.PGA)) {
      String fileName = "1998-HI-HAZ-0_0sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_TWO_SEC)) {
      String fileName = "1998-HI-HAZ-0_2sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_THREE_SEC)) {
      String fileName = "1998-HI-HAZ-0_3sec-R3a.rnd";
      return filePath + fileName;
    }
    else if (selectedHazardCurveType.equals(GlobalConstants.IMT_ONE_SEC)) {
      String fileName = "1998-HI-HAZ-1_0sec-R3a.rnd";
      return filePath + fileName;
    }
    return null;
  }

  /**
   * Gets the filename for the Counter 48 states with selected analysis option being
   * Hazard curves.
   * @param selectedEdition String
   * @param lat double
   * @param lon double
   * @param selectedHazardCurveType String
   * @return String
   */
  private String getFileNameFor48States(String selectedEdition, double lat,
                                        double lon,
                                        String selectedHazardCurveType) {
    if (selectedEdition.equals(GlobalConstants.data_1996)) {
      if (lon >= -125 && lon <= -111 && lat >= 32 && lat <= 43) { //getting the file for CA and NV
        if (selectedHazardCurveType.equals(GlobalConstants.PGA)) {
          String fileName = "1996-CANV-HAZ-0_0sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_ONE_SEC)) {
          String fileName = "1996-CANV-HAZ-0_1sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_TWO_SEC)) {
          String fileName = "1996-CANV-HAZ-0_2sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_THREE_SEC)) {
          String fileName = "1996-CANV-HAZ-0_3sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_FIVE_SEC)) {
          String fileName = "1996-CANV-HAZ-0_5sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.IMT_ONE_SEC)) {
          String fileName = "1996-CANV-HAZ-1_0sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.IMT_TWO_SEC)) {
          String fileName = "1996-CANV-HAZ-2_0sec-R2a.rnd";
          return filePath + fileName;
        }
      }
      else { //getting the filename for whole of US other then CA and NV
        if (selectedHazardCurveType.equals(GlobalConstants.PGA)) {
          String fileName = "1996-US-HAZ-0_0sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_ONE_SEC)) {
          String fileName = "1996-US-HAZ-0_1sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_TWO_SEC)) {
          String fileName = "1996-US-HAZ-0_2sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_THREE_SEC)) {
          String fileName = "1996-US-HAZ-0_3sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.
                                                IMT_POINT_FIVE_SEC)) {
          String fileName = "1996-US-HAZ-0_5sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.IMT_ONE_SEC)) {
          String fileName = "1996-US-HAZ-1_0sec-R2a.rnd";
          return filePath + fileName;
        }
        else if (selectedHazardCurveType.equals(GlobalConstants.IMT_TWO_SEC)) {
          String fileName = "1996-US-HAZ-2_0sec-R2a.rnd";
          return filePath + fileName;
        }
      }
    } //End for the 48 states and data edition to be 1996
    else if (selectedEdition.equals(GlobalConstants.data_2002)) {
      if (selectedHazardCurveType.equals(GlobalConstants.PGA)) {
        String fileName = "2002-US-HAZ-0_0sec-R2a.rnd";
        return filePath + fileName;
      }
      else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_ONE_SEC)) {
        String fileName = "2002-US-HAZ-0_1sec-R2a.rnd";
        return filePath + fileName;
      }
      else if (selectedHazardCurveType.equals(GlobalConstants.IMT_POINT_TWO_SEC)) {
        String fileName = "2002-US-HAZ-0_2sec-R2a.rnd";
        return filePath + fileName;
      }
      else if (selectedHazardCurveType.equals(GlobalConstants.
                                              IMT_POINT_THREE_SEC)) {
        String fileName = "2002-US-HAZ-0_3sec-R2a.rnd";
        return filePath + fileName;
      }
      else if (selectedHazardCurveType.equals(GlobalConstants.
                                              IMT_POINT_FIVE_SEC)) {
        String fileName = "2002-US-HAZ-0_5sec-R2a.rnd";
        return filePath + fileName;
      }
      else if (selectedHazardCurveType.equals(GlobalConstants.IMT_ONE_SEC)) {
        String fileName = "2002-US-HAZ-1_0sec-R2a.rnd";
        return filePath + fileName;
      }
      else if (selectedHazardCurveType.equals(GlobalConstants.IMT_TWO_SEC)) {
        String fileName = "2002-US-HAZ-2_0sec-R2a.rnd";
        return filePath + fileName;
      }
    }
    return null;
  }

}
