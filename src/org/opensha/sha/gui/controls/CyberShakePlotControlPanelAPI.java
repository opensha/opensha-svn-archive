package org.opensha.sha.gui.controls;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.gui.infoTools.ButtonControlPanel;
import org.opensha.sha.gui.infoTools.GraphPanel;

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
     */
    public void setCurveType(String curveType);


    /**
     * This returns the Earthquake Forecast GuiBean which allows the the cybershake
     * control panel to set the forecast parameters from cybershake control panel,
     * similar to what they are set when calculating cybershaks curves.
     */
    public ERF_GuiBean getEqkRupForecastGuiBeanInstance();

    /**
     * This returns instance to the EqkRupSelectorGuiBean, this allows the cybershake
     * control panel to set the forecast parameters and select the same source
     * and rupture as in the cybershake control panel.
     */
    public EqkRupSelectorGuiBean getEqkSrcRupSelectorGuiBeanInstance();

    /**
     * This returns the Site Guibean using which allows to set the site locations
     * in the OpenSHA application from cybershake control panel.
     */
    public Site_GuiBean getSiteGuiBeanInstance();

    /**
     * It returns the IMT Gui bean, which allows the Cybershake control panel
     * to set the same SA period value in the main application
     * similar to selected for Cybershake.
     */
    public IMT_GuiBean getIMTGuiBeanInstance();
    
    /**
     * It returns the IMT Gui bean, which allows the Cybershake control panel
     * to set the same SA period value in the main application
     * similar to selected for Cybershake.
     */
    public IMR_GuiBean getIMRGuiBeanInstance();
    
    /**
     * Returns the Set Site Params from Web Services control panel.
     */
    public SiteDataControlPanel getCVMControl();
    
    /**
     * sets the range for X and Y axis
     * @param xMin : minimum value for X-axis
     * @param xMax : maximum value for X-axis
     * @param yMin : minimum value for Y-axis
     * @param yMax : maximum value for Y-axis
     *
     */
    public void setAxisRange(double xMin,double xMax, double yMin, double yMax);

    /**
     * Sets the hazard curve x-axis values (if user wants custom values x-axis values).
     * Note that what's passed in is not cloned (the y-axis values will get modified).
     * @param func
     */
    public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func);
    
    /**
     * tells the application if the yLog is selected
     * @param yLog : boolean
     */
    public void setY_Log(boolean yLog);
    
    /**
     * tells the application if the xLog is selected
     * @param xLog : boolean
     */
    public void setX_Log(boolean xLog);
    
    /**
    *
    * sets  X Axis Label
    */
   public void setXAxisLabel(String xAxisLabel);

   /**
    *
    * sets Y Axis Label
    */
   public void setYAxisLabel(String yAxisLabel);

   /**
    *
    * sets plot Title
    */
   public void setPlotLabel(String plotTitle);
   
   public ArrayList getPlottingFeatures();
   
   public ButtonControlPanel getButtonControlPanel();
   
   public GraphPanel getGraphPanel();
    
//    public void setProgressCheckBoxSelected(boolean selected);
}
