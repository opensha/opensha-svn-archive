package org.scec.sha.gui.controls;


import org.scec.data.XYZ_DataSetAPI;
import org.scec.sha.imr.AttenuationRelationship;

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

  /**
   *
   * @returns the selected Attenuationrelationship model within the application
   */
  public AttenuationRelationship getSelectedAttenuationRelationship();


  /**
   * This function sets the Gridded region Sites and the type of plot user wants to see
   * IML@Prob or Prob@IML and it value.
   */
  public void getGriddedSitesAndMapType();
}
