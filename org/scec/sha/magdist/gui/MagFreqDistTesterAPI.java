package org.scec.sha.magdist.gui;

/**
 * <p>Title: MagFreqDistTesterAPI </p>
 * <p>Description: This interface is needed for getting the MagFreqDist functionality
 * in a GUI.  Any class needing this functinality needs to implement this interface
 *  and then instantiate the MagDistGUIBean</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public interface MagFreqDistTesterAPI {

  /**
    *  Used for synch applet with new Mag Dist choosen. Updates lables and
    *  initializes the Mag Dist if needed.
    */
   public void updateChoosenMagDist();
}