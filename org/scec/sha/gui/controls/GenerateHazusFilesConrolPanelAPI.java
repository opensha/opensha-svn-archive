package org.scec.sha.gui.controls;


import org.scec.data.XYZ_DataSetAPI;

/**
 * <p>Title: GenerateHazusFilesConrolPanelAPI</p>
 * <p>Description: This interface is the acts as the broker between the
 * application and the GenerateHazusFilesControlPanel</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public interface GenerateHazusFilesConrolPanelAPI {


  /**
   * This method calculates the probablity or the IML for the selected Gridded Region
   * and stores the value in each vectors(lat-Vector, Lon-Vector and IML or Prob Vector)
   * The IML or prob vector contains value based on what the user has selected in the Map type
   */
  public XYZ_DataSetAPI generateShakeMap();
}
