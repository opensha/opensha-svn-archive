package org.opensha.nshmp.sha.gui.beans;


import java.util.ArrayList;

import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.commons.data.region.RectangularGeographicRegion;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.nshmp.exceptions.AnalysisOptionNotSupportedException;
import org.opensha.nshmp.sha.gui.api.ProbabilisticHazardApplicationAPI;
import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.nshmp.util.RegionUtil;

/**
 * <p>Title: ASCE7_NFPA_GuiBean</p>
 *
 * <p>Description: </p>
 * @author Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public class ASCE7_GuiBean
    extends NEHRP_GuiBean {
  
  private static final long serialVersionUID = 0x35C204F;
  
  public ASCE7_GuiBean(ProbabilisticHazardApplicationAPI api) {
    super(api);
  }

  /**
   *
   * @return RectangularGeographicRegion
   */
  protected GeographicRegion getRegionConstraint() throws
      RegionConstraintException {

    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES) ||
        selectedRegion.equals(GlobalConstants.ALASKA) ||
        selectedRegion.equals(GlobalConstants.HAWAII) ||
		  selectedEdition.equals(GlobalConstants.ASCE_2005)) {

      return RegionUtil.getRegionConstraint(selectedRegion);
    }

    return null;
  }

  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList<String> supportedEditionList = new ArrayList<String>();

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
        getSupportedGeographicalRegions(GlobalConstants.ASCE_7);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }
}
