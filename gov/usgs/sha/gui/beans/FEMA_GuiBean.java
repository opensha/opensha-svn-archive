package gov.usgs.sha.gui.beans;

import java.util.*;

import org.scec.data.region.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import gov.usgs.exceptions.*;
import gov.usgs.sha.data.*;
import gov.usgs.sha.gui.api.*;
import gov.usgs.util.*;

/**
 * <p>Title: FEMA_GuiBean</p>
 *
 * <p>Description: </p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class FEMA_GuiBean
    extends NEHRP_GuiBean {

  public FEMA_GuiBean(ProbabilisticHazardApplicationAPI api) {
    super(api);
    dataGenerator = new DataGenerator_FEMA();
  }

  protected void createGroundMotionParameter() {

    ArrayList supportedGroundMotion = getSupportedSpectraTypes();
    groundMotionParam = new StringParameter(GROUND_MOTION_PARAM_NAME,
                                            supportedGroundMotion,
                                            (String) supportedGroundMotion.get(
                                                0));
    groundMotionParamEditor = new ConstrainedStringParameterEditor(
        groundMotionParam);

    groundMotionParam.addParameterChangeListener(this);
    spectraType = (String) groundMotionParam.getValue();
  }

  protected ArrayList getSupportedSpectraTypes() {
    ArrayList supportedSpectraTypes = new ArrayList();

    supportedSpectraTypes.add(GlobalConstants.MCE_GROUND_MOTION);
    supportedSpectraTypes.add(GlobalConstants.PE_10);

    return supportedSpectraTypes;
  }

  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    supportedEditionList.add(GlobalConstants.FEMA_273_DATA);
    supportedEditionList.add(GlobalConstants.FEMA_356_DATA);
    supportedEditionList.add(GlobalConstants.IEBC_2003);
    datasetGui.createEditionSelectionParameter(supportedEditionList);
    datasetGui.getEditionSelectionParameter().addParameterChangeListener(this);
    selectedEdition = datasetGui.getSelectedDataSetEdition();
  }

  /**
   * If GuiBean parameter is changed.
   * @param event ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent event) {

    String paramName = event.getParameterName();
    if (paramName.equals(GROUND_MOTION_PARAM_NAME)) {
      spectraType = (String) groundMotionParam.getValue();
      groundMotionParamEditor.refreshParamEditor();
    }
    else {
      super.parameterChange(event);
    }

  }

  /**
   *
   * @return RectangularGeographicRegion
   */
  protected RectangularGeographicRegion getRegionConstraint() {

    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES) ||
        selectedRegion.equals(GlobalConstants.ALASKA) ||
        selectedRegion.equals(GlobalConstants.HAWAII)) {

      return RegionUtil.getRegionConstraint(selectedRegion);
    }
    return null;
  }

  /**
   *
   * Creating the parameter that allows user to choose the geographic region list
   * if selected Analysis option is NEHRP.
   *
   */
  protected void createGeographicRegionSelectionParameter() throws
      AnalysisOptionNotSupportedException {

    ArrayList supportedRegionList = RegionUtil.
        getSupportedGeographicalRegions(GlobalConstants.FEMA_IEBC_2003);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }

}
