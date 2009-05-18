package org.opensha.nshmp.sha.gui.beans;

import java.util.ArrayList;

import org.opensha.commons.data.region.RectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.nshmp.sha.gui.api.ProbabilisticHazardApplicationAPI;
import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.nshmp.util.RegionUtil;

/**
 * <p>Title NFPA_GuiBean_Wrapper</p>
 *
 *<p>Description</p>
 *<p>This is a simple wrapper class that only overrides one function from
 *   the ASCE_7 gui bean.  The desired effect is that NFPA behaves just like
 *   ASCE_7.
 *@author Eric Martinez
 *@version 1.0
 */
public class NFPA_GuiBean_Wrapper extends ASCE7_GuiBean {
  public NFPA_GuiBean_Wrapper(ProbabilisticHazardApplicationAPI api) {
	  super(api);
  }

  /**
	 * Creates the Parameter that allows user to select the Editions based
	 * on the selected Analysis and chosen Geographic region.
	 */
	 protected void createEditionSelectionParameter() {
	   ArrayList supportedEditionList = new ArrayList();

		 supportedEditionList.add(GlobalConstants.NFPA_2006);
		 supportedEditionList.add(GlobalConstants.NFPA_2003);

	   datasetGui.createEditionSelectionParameter(supportedEditionList);
		 datasetGui.getEditionSelectionParameter().addParameterChangeListener(this);
		 selectedEdition = datasetGui.getSelectedDataSetEdition();
	}

	/**
	*
	* @return RectangularGeographicRegion
	*/
	protected RectangularGeographicRegion getRegionConstraint() throws
		RegionConstraintException {

		if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES) ||
			selectedRegion.equals(GlobalConstants.ALASKA) ||
			selectedRegion.equals(GlobalConstants.HAWAII) ||
			selectedEdition.equals(GlobalConstants.NFPA_2006)) {
			return RegionUtil.getRegionConstraint(selectedRegion);
		}

		return null;
	}
}

