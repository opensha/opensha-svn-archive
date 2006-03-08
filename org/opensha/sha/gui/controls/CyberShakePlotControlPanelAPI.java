package org.opensha.sha.gui.controls;

import org.opensha.data.function.DiscretizedFuncAPI;
/**
 * <p>Title: CyberShakePlotControlPanelAPI</p>
 *
 * <p>Description: This interface allows the application to add the curve data from
 * Cybershake results.</p>
 * @author Nitin Gupta
 * @version 1.0
 */
public interface CyberShakePlotControlPanelAPI {

    /**
     * Returns the Cybershake curve data back to the user.
     * @return DiscretizedFuncAPI
     */
    public void addCybershakeCurveData(DiscretizedFuncAPI function);
}
