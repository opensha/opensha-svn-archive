package gov.usgs.sha.data;

import gov.usgs.exceptions.ZipCodeErrorException;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;

import gov.usgs.util.GlobalConstants;

/**
 * <p>Title: DataGenerator_FEMA</p>
 *
 * <p>Description: </p>
 * @author Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public class DataGenerator_FEMA
    extends DataGenerator_NEHRP {

    /**
     * Returns the SA at .2sec
     * @return double
     */
    public double getSs(){
      return saFunction.getY(0);
    }

    /**
     * Returns the SA at 1 sec
     * @return double
     */
    public double getSa(){
      return saFunction.getY(1);
    }


    /**
     * Gets the data for SsS1 in case region specified is not a Territory and user
     * specifies Lat-Lon for the location.
     */
    public void calculateSsS1(double lat, double lon) {

      HazardDataMiner miner = new HazardDataMiner();
      ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                   lat, lon,selectedSpectraType);
      String location = "Lat - "+lat+"  Lon - "+lon;
      createMetadataForPlots(location);
      addDataInfo(function.getInfo());
      saFunction = function;
    }

    /**
     * Gets the data for SsS1 in case region specified is not a Territory and user
     * specifies zip code for the location.
     */
    public void calculateSsS1(String zipCode) throws ZipCodeErrorException {

      HazardDataMiner miner = new HazardDataMiner();
      ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                   zipCode,selectedSpectraType);
      String location = "Zipcode - "+zipCode;
      createMetadataForPlots(location);
      addDataInfo(function.getInfo());
      saFunction = function;
    }

}
