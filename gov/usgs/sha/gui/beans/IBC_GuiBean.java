package gov.usgs.sha.gui.beans;

import java.util.*;

import org.scec.data.region.*;
import org.scec.param.event.*;
import gov.usgs.exceptions.*;
import gov.usgs.sha.gui.api.*;
import gov.usgs.util.*;

/**
 * <p>Title: IBC_GuiBean</p>
 *
 * <p>Description: </p>
 *
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 *
 * @version 1.0
 */
public class IBC_GuiBean
    extends NEHRP_GuiBean {

  public IBC_GuiBean(ProbabilisticHazardApplicationAPI api) {
    super(api);
  }

  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    supportedEditionList.add(GlobalConstants.IBC_2000);
    supportedEditionList.add(GlobalConstants.IBC_2003);
    if (!selectedRegion.equals(GlobalConstants.CONTER_48_STATES) &&
        !selectedRegion.equals(GlobalConstants.ALASKA) &&
        !selectedRegion.equals(GlobalConstants.HAWAII)) {
      supportedEditionList.add(GlobalConstants.IBC_2004);
      supportedEditionList.add(GlobalConstants.IBC_2006);
    }
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

    if (paramName.equals(datasetGui.GEOGRAPHIC_REGION_SELECTION_PARAM_NAME)) {
      selectedRegion = datasetGui.getSelectedGeographicRegion();
      createEditionSelectionParameter();
    }
    super.parameterChange(event);

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
        getSupportedGeographicalRegions(GlobalConstants.INTL_BUILDING_CODE);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }

}
