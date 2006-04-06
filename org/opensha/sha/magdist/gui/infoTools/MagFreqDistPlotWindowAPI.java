package org.opensha.sha.magdist.gui.infoTools;

import org.opensha.sha.gui.infoTools.*;

/**
 * <p>Title:MagFreqDistPlotWindowAPI </p>
 *
 * <p>Description: Listens to the events thriwn by MagFreqDistPlotWindow.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public interface MagFreqDistPlotWindowAPI
    extends GraphWindowAPI {

    /**
     * Sets in the application if the plot preferences have changed
     * @param isChanged boolean
     */
    public void setPlotPreferencesChanged(boolean isChanged);
}
