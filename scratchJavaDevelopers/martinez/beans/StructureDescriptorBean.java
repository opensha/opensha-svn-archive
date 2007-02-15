package scratchJavaDevelopers.martinez.beans;

import java.util.EventListener;

import javax.swing.JOptionPane;

import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.param.*;

import scratchJavaDevelopers.martinez.VulnerabilityModels.VulnerabilityModel;

/**
 * <strong>Title:</strong> StructureDescriptorBean<br />
 * <strong>Description:</strong> A bean to gather and store information about a structure.
 * While this can be expanding upon to include more specific information about a structure,
 * its current implementation holds only the information for the purposes of the BenefitCostRatio
 * application.
 * 
 * @see BRC_Application
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 */
public class StructureDescriptorBean extends GuiBeanAPI {
	private ParameterListEditor applicationEditor = null;
	private DoubleParameter initialCost = null;
	private DoubleParameter replaceCost = null;
	private VulnerabilityBean vulnBean = null;
	private String descriptorName = "";
	
	private double initialVal = 0.0;
	private double replaceVal = 0.0;
	
	private EventListener listener = null;
	private static final String INITIAL_PARAM = "Initial Construction Cost";
	private static final String REPLACE_PARAM = "Replacement Construction Cost";
	
	////////////////////////////////////////////////////////////////////////////////
	//                              Public Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	
	public StructureDescriptorBean() {
		this("");
	}
	
	/**
	 * @param name The title of this bean.
	 */
	public StructureDescriptorBean(String name) {
		descriptorName = name;
		vulnBean = new VulnerabilityBean();
		listener = new StructureDescriptorParameterListener();
		
		initialCost = new DoubleParameter("Initial Construction Cost", 0, 10E+10, "$$$");
		initialCost.addParameterChangeListener((ParameterChangeListener) listener);
		initialCost.addParameterChangeFailListener((ParameterChangeFailListener) listener);
		
		replaceCost = new DoubleParameter("Replacement Construction Cost", 0, 10E+10, "$$$");
		replaceCost.addParameterChangeListener((ParameterChangeListener) listener);
		replaceCost.addParameterChangeFailListener((ParameterChangeFailListener) listener);
	}
	
	public VulnerabilityModel getVulnerabilityModel() { return vulnBean.getCurrentModel(); }
	public double getInitialCost() { return initialVal; }
	public double getReplaceCost() { return replaceVal; }

	////////////////////////////////////////////////////////////////////////////////
	//                  Minimum Functions to Extend GuiBeanAPI                    //
	////////////////////////////////////////////////////////////////////////////////
	@Override
	/**
	 * See the general contract in GuiBeanAPI.
	 */
	public Object getVisualization(int type) {
		if(!isVisualizationSupported(type))
			throw new IllegalArgumentException("Only the Application type is supported at this time.");
		if(type == GuiBeanAPI.APPLICATION) {
			return getApplicationVisualization();
		}
		return null;
	}
	@Override
	/**
	 * See the general contract in GuiBeanAPI.
	 */
	public String getVisualizationClassName(int type) {
		String cname = "";
		if(type == GuiBeanAPI.APPLICATION) {
			cname = "org.opensha.param.editor.ParameterListEditor";
		}
		
		return cname;
	}
	@Override
	/**
	 * See the general contract in GuiBeanAPI.
	 */
	public boolean isVisualizationSupported(int type) {
		return type == GuiBeanAPI.APPLICATION;
	}

	////////////////////////////////////////////////////////////////////////////////
	//                             Private Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	
	private ParameterListEditor getApplicationVisualization() {
		if(applicationEditor == null) {
			ParameterList plist = new ParameterList();
			plist.addParameter(vulnBean.getParameter());
			plist.addParameter(initialCost);
			plist.addParameter(replaceCost);
			applicationEditor = new ParameterListEditor(plist);
			applicationEditor.setTitle(descriptorName);
		}
		return applicationEditor;
	}

	private void handleInitialCostChange(ParameterChangeEvent event) {
		initialVal = (Double) event.getNewValue();
	}
	private void handleReplaceCostChange(ParameterChangeEvent event) {
		replaceVal = (Double) event.getNewValue();
	}
	private class StructureDescriptorParameterListener implements ParameterChangeListener, ParameterChangeFailListener {

		public void parameterChange(ParameterChangeEvent event) {
			if(INITIAL_PARAM.equals(event.getParameterName()))
				handleInitialCostChange(event);
			else if(REPLACE_PARAM.equals(event.getParameterName()))
				handleReplaceCostChange(event);
		}
		
		public void parameterChangeFailed(ParameterChangeFailEvent event) {
			JOptionPane.showMessageDialog(null, "The given value of " + event.getBadValue() +
					" is out of range.", "Failed to Change Value", JOptionPane.ERROR_MESSAGE);
		}
	}
}
