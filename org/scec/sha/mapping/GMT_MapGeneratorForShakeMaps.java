package org.scec.sha.mapping;

import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.*;
import org.scec.sha.earthquake.EqkRupture;

/**
 * <p>Title: GMT_MapGeneratorForShakeMaps</p>
 * <p>Description: This class extends the GMT_MapGenerator to extend the
 * GMT functionality for the shakeMaps.</p>
 * @author : Edward (Ned) Field , Nitin Gupta
 * @dated Dec 31,2003
 */

public class GMT_MapGeneratorForShakeMaps extends GMT_MapGenerator{

  /**
   * Makes scenarioshake maps locally using the GMT on the users own computer
   * @param xyzDataSet: XYZ Data
   * @param eqkRup : EarthRupture Object
   * @param hypLoc :Hypocenter Location
   * @param scaleLabel
   * @return
   */
  public String makeMapLocally(XYZ_DataSetAPI xyzDataSet, EqkRupture eqkRupture,
                               Location hypLoc,String scaleLabel){

    return super.makeMapLocally(xyzDataSet,scaleLabel);
  }

  /**
   * Makes scenarioshake maps using the GMT on the gravity.usc.edu server(Linux server).
   * Implemented as the servlet, using which we can actual java serialized object.
   * @param xyzDataSet: XYZ Data
   * @param eqkRup : EarthRupture Object
   * @param hypLoc : Hypocenter Location
   * @param scaleLabel
   * @return: URL to the image
   */
  public String makeMapUsingServlet(XYZ_DataSetAPI xyzDataSet, EqkRupture eqkRupture,
                                    Location hypLoc,String scaleLabel){
    return super.makeMapUsingServlet(xyzDataSet, scaleLabel);
  }

  /**
   * Makes scenarioshake maps using the GMT on the gravity.usc.edu server(Linux server).
   * Implemented as the webservice, using which we can send files as the attachment.
   * @param xyzDataSet: XYZ Data
   * @param eqkRup : EarthRupture Object
   * @param hypLoc :Hypocenter Location
   * @param scaleLabel
   * @return: URL to the image
   */
  public String makeMapUsingWebServer(XYZ_DataSetAPI xyzDataSet, EqkRupture eqkRupture,
                                      Location hypLoc,String scaleLabel){
    return super.makeMapUsingWebServer(xyzDataSet, scaleLabel);
  }

}