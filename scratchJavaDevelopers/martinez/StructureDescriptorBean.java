package scratchJavaDevelopers.martinez;

import org.opensha.param.editor.*;
import org.opensha.param.*;

public class StructureDescriptorBean extends GuiBeanAPI {
	private ParameterListEditor applicationEditor = null;
	private StringParameter vulnParam = null;
	private DoubleParameter initialCost = null;
	private DoubleParameter replaceCost = null;
	private VulnerabilityBean vulnBean = null;
	private String descriptorName = "";
	
	////////////////////////////////////////////////////////////////////////////////
	//                              Public Functions                              //
	////////////////////////////////////////////////////////////////////////////////
	
	public StructureDescriptorBean() {
		this("");
	}
	public StructureDescriptorBean(String name) {
		descriptorName = name;
		vulnBean = new VulnerabilityBean();
		vulnParam = vulnBean.getParameter();
		initialCost = new DoubleParameter("Initial Construction Cost", 0, 10E+10, "Dollars");
		replaceCost = new DoubleParameter("Replacement Construction Cost", 0, 10E+10, "Dollars");
	}
	
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
			plist.addParameter(vulnParam);
			plist.addParameter(initialCost);
			plist.addParameter(replaceCost);
			applicationEditor = new ParameterListEditor(plist);
			applicationEditor.setTitle(descriptorName);
		}
		return applicationEditor;
	}
}
