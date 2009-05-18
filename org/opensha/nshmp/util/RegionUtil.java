package org.opensha.nshmp.util;

import java.util.ArrayList;

import org.opensha.commons.data.region.RectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.nshmp.exceptions.AnalysisOptionNotSupportedException;



/**
 * <p>Title: RegionUtil</p>
 *
 * <p>Description: </p>

 * @author not attributable
 * @version 1.0
 */
public final class RegionUtil {

  /**
   *
   * @param selectedAnalysisOption String
   * @return ArrayList
   */
  public static ArrayList getSupportedGeographicalRegions(String
      selectedAnalysisOption) throws AnalysisOptionNotSupportedException {
    ArrayList<String> supportedRegionList = new ArrayList<String>();
    if (selectedAnalysisOption.equals(GlobalConstants.NEHRP) ||
        selectedAnalysisOption.equals(GlobalConstants.ASCE_7) ||
        selectedAnalysisOption.equals(GlobalConstants.NFPA) ||
        selectedAnalysisOption.equals(GlobalConstants.INTL_BUILDING_CODE)) {
      supportedRegionList.add(GlobalConstants.CONTER_48_STATES);
      supportedRegionList.add(GlobalConstants.ALASKA);
      supportedRegionList.add(GlobalConstants.HAWAII);
      supportedRegionList.add(GlobalConstants.PUERTO_RICO);
      supportedRegionList.add(GlobalConstants.CULEBRA);
      supportedRegionList.add(GlobalConstants.ST_CROIX);
      supportedRegionList.add(GlobalConstants.ST_JOHN);
      supportedRegionList.add(GlobalConstants.ST_THOMAS);
      supportedRegionList.add(GlobalConstants.VIEQUES);
      supportedRegionList.add(GlobalConstants.TUTUILA);
      supportedRegionList.add(GlobalConstants.GUAM);
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.PROB_HAZ_CURVES) ||
             selectedAnalysisOption.equals(GlobalConstants.PROB_UNIFORM_HAZ_RES) ||
				 selectedAnalysisOption.equals(GlobalConstants.INTL_RESIDENTIAL_CODE)) {
      supportedRegionList.add(GlobalConstants.CONTER_48_STATES);
      supportedRegionList.add(GlobalConstants.ALASKA);
      supportedRegionList.add(GlobalConstants.HAWAII);
      // Add this only for Hazard Curves
      /*if(selectedAnalysisOption.equals(GlobalConstants.PROB_HAZ_CURVES)) {
  		supportedRegionList.add(GlobalConstants.INDONESIA);
  	  }*/
      supportedRegionList.add(GlobalConstants.PUERTO_RICO);
      supportedRegionList.add(GlobalConstants.CULEBRA);
      supportedRegionList.add(GlobalConstants.ST_CROIX);
      supportedRegionList.add(GlobalConstants.ST_JOHN);
      supportedRegionList.add(GlobalConstants.ST_THOMAS);
      supportedRegionList.add(GlobalConstants.VIEQUES);
    } 	
    /*else if (selectedAnalysisOption.equals(GlobalConstants.FEMA_IEBC_2003)) {
      supportedRegionList.add(GlobalConstants.CONTER_48_STATES);
      supportedRegionList.add(GlobalConstants.ALASKA);
      supportedRegionList.add(GlobalConstants.HAWAII);
    }*/
    else {
      throw new AnalysisOptionNotSupportedException(
          "This " + selectedAnalysisOption +
          " analysis option not supported!!\nPlease provide correct option.");
    }

    return supportedRegionList;
  }

  /**
   *
   * @return RectangularGeographicRegion
   */
  public static RectangularGeographicRegion getRegionConstraint(String
      selectedGeographicRegion) throws RegionConstraintException {

    if (selectedGeographicRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      return new RectangularGeographicRegion(24.7, 50, -125, -65);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.ALASKA)) {
      return new RectangularGeographicRegion(48, 72, -200, -125);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.HAWAII)) {
      return new RectangularGeographicRegion(18, 23, -161, -154);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.PUERTO_RICO)) {
      return new RectangularGeographicRegion(17.89, 18.55, -67.36, -65.47);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.CULEBRA)) {
      return new RectangularGeographicRegion(18.27, 18.36, -65.39, -65.21);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.ST_CROIX)) {
      return new RectangularGeographicRegion(17.67, 17.8, -64.93, -64.54);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.ST_JOHN)) {
      return new RectangularGeographicRegion(18.29, 18.38, -64.85, -64.65);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.ST_THOMAS)) {
      return new RectangularGeographicRegion(18.26, 18.43, -65.10, -64.80);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.VIEQUES)) {
      return new RectangularGeographicRegion(18.07, 18.17, -65.6, -65.25);
    }
    /*else if (selectedGeographicRegion.equals(GlobalConstants.INDONESIA)) {
    	return new RectangularGeographicRegion(-10.0, 8.00, 92, 118);
    }*/

    return null;
  }

  /**
   * Returns the IMT periods for the selected Region.
   * Used in the Prob Hazard Curves
   * @param selectedRegion String
   * @return ArrayList
   */
  public static ArrayList getSupportedIMT_PERIODS(String selectedRegion) {
    ArrayList<String> supportedImtPeriods = new ArrayList<String>();
    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      supportedImtPeriods.add(GlobalConstants.PGA);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_ONE_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_TWO_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_THREE_SEC);
      //supportedImtPeriods.add(GlobalConstants.IMT_POINT_FOUR_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_FIVE_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_ONE_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_TWO_SEC);
    }
    else if (selectedRegion.equals(GlobalConstants.HAWAII) ||
             selectedRegion.equals(GlobalConstants.ALASKA)) {
      supportedImtPeriods.add(GlobalConstants.PGA);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_TWO_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_THREE_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_ONE_SEC);
    }
    else {
      supportedImtPeriods.add(GlobalConstants.PGA);
      supportedImtPeriods.add(GlobalConstants.IMT_POINT_TWO_SEC);
      supportedImtPeriods.add(GlobalConstants.IMT_ONE_SEC);
    }
    return supportedImtPeriods;
  }

}
