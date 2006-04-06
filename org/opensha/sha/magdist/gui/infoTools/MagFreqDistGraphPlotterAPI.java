package org.opensha.sha.magdist.gui.infoTools;


/**
 * <p>Title: MagFreqDistGraphPlotterAPI</p>
 *
 * <p>Description: This interface listens for any changes to plot pref. for any
 * mag freq. dist plots.</p>
 * @author Nitin Gupta
 * @version 1.0
 */
public interface MagFreqDistGraphPlotterAPI {

    /**
     * Updates all the windows with the modified plot prefs.
     * @param toUpdate boolean : if plot prefs updated
     */
    public void updateMagFreqDistPlotPrefs(boolean toUpdate);
}
