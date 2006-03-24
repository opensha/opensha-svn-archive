package org.opensha.sha.gui.controls;

import org.opensha.data.function.DiscretizedFuncAPI;
import java.util.ArrayList;

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

    /**
     * Returns the IML values to the application.
     */
    public ArrayList getIML_Values();

    /**
     * Sets the application with the curve type chosen by the Cybershake application
     * @param isDeterministic boolean :If deterministic calculation then make the
     * applicaton to plot deterministic curves.
     */
    public void setCurveType(boolean isDeterministic);
}
