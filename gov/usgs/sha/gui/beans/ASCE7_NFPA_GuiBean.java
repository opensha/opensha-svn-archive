package gov.usgs.sha.gui.beans;

import java.util.*;

import org.scec.data.region.*;
import gov.usgs.exceptions.*;
import gov.usgs.sha.gui.api.*;
import gov.usgs.util.*;

/**
 * <p>Title: ASCE7_NFPA_GuiBean</p>
 *
 * <p>Description: </p>
 * @author Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public class ASCE7_NFPA_GuiBean
    extends NEHRP_GuiBean {

  public ASCE7_NFPA_GuiBean(ProbabilisticHazardApplicationAPI api) {
    super(api);
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
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    supportedEditionList.add(GlobalConstants.ASCE_2005);
    supportedEditionList.add(GlobalConstants.ASCE_2002);
    supportedEditionList.add(GlobalConstants.ASCE_1998);
    datasetGui.createEditionSelectionParameter(supportedEditionList);
    datasetGui.getEditionSelectionParameter().addParameterChangeListener(this);
    selectedEdition = datasetGui.getSelectedDataSetEdition();
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
        getSupportedGeographicalRegions(GlobalConstants.ASCE_NFPA);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }
}
