package org.scec.sha.gui.beans;

import org.scec.data.Location;
import org.scec.param.ParameterAPI;
import org.scec.param.editor.ParameterEditor;
import org.scec.sha.earthquake.EqkRupture;

/**
 * <p>Title: EqkRupSelectorGuiBeanAPI</p>
 * <p>Description: This class defines methods that any class providing the
 * user the functionality of getting EqkRupture to EqkRupSelectorGuiBean.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @since Dec 03,2004
 * @version 1.0
 */

public interface EqkRupSelectorGuiBeanAPI {

    /**
     *
     * @returns the Hypocenter Location if selected else return null
     */
    public Location getHypocenterLocation();


    /**
     *
     * @returns the panel which allows user to select Eqk rupture from existing
     * ERF models
     */
    public EqkRupSelectorGuiBeanAPI getEqkRuptureSelectorPanel();



    /**
     *
     * @returns the Metadata String of parameters that constitute the making of this
     * ERF_RupSelectorGUI  bean.
     */
    public String getParameterListMetadataString();

    /**
     *
     * @returns the timespan Metadata for the selected Rupture.
     * If no timespan exists for the rupture then it returns the Message:
     * "No Timespan exists for the selected Rupture".
     */
    public String getTimespanMetadataString();

    /**
     *
     * @returns the EqkRupture Object
     */
    public EqkRupture getRupture();

    /**
     *
     * @param paramName
     * @returns the parameter from the parameterList with paramName.
     */
    public ParameterAPI getParameter(String paramName);

    /**
     *
     * @param paramName
     * @returns the ParameterEditor associated with paramName
     */
    public ParameterEditor getParameterEditor(String paramName);

  }
