package scratch.ISTI.portfolioeal.gui;
import java.util.ArrayList;

import org.opensha.sha.gui.beans.ERF_GuiBean;

/**
 * This class creates an instance of <code>ERF_GuiBean</code>, and implements it as a 
 * JPanel.
 * 
 * @author Jeremy Leakakos
 */
public class ERFPanel {
	// The object class names for all the supported Eqk Rup Forecasts 
	// The two that are commented out are not compatible with the current way of doing
	// calculations, and if no formal specifications of how to do those calculations
	// are provided, they will need to stay commented out.
	private final String FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
	private final String FRANKEL02_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
	private final String WGCEP_UCERF1_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast";
	private final String POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF";
	private final String SIMPLE_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF";
	//private final static String POINT_SRC_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.PointSourceERF";
	//private final static String POINT2MULT_VSS_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF";
	private final String WG02_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
	private String[] erfNames = {FRANKEL_ADJ_FORECAST_CLASS_NAME, FRANKEL02_ADJ_FORECAST_CLASS_NAME,WGCEP_UCERF1_CLASS_NAME, POISSON_FAULT_ERF_CLASS_NAME, 
								 SIMPLE_FAULT_ERF_CLASS_NAME, WG02_ERF_CLASS_NAME };
	
	private ArrayList<String> erfList = new ArrayList<String>();
	private ERF_GuiBean erfPanel;
	
	/**
	 * The default constructor.  An ERF_GuiBean is created, which is called by the main view.  Since
	 * the class ERF_GuiBean is already a JPanel, there is no UI formatting done in this class.
	 */
	public ERFPanel() {
		
		//* Add the object class names for the forecasts to an ArrayList
		for ( int i = 0; i < erfNames.length; i++ ) {
			erfList.add(erfNames[i]);
		}
		
		try {
			erfPanel = new ERF_GuiBean(erfList);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
		erfPanel.getParameter("Eqk Rup Forecast").addParameterChangeListener(BCR_ApplicationFacade.getBCR());
	}
	
	/**
	 * @return This is the instance of ERF_GuiBean that is to be used in the main program
	 */
	public ERF_GuiBean getPanel() {
		return erfPanel;
	}

}
