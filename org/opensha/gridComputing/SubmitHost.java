package org.opensha.gridComputing;

import org.dom4j.Element;
import org.opensha.metadata.XMLSaveable;

public class SubmitHost implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "SubmitHost";
	
	// presets
	public static final SubmitHost SCECIT18 = new SubmitHost("scecit18", "scecit18.usc.edu",
			"/home/kmilner/hazMapRuns", "/home/kmilner/dependencies", "jobmanager-fork",
			"GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;",
			"-n transfer -N VDS::transfer:1.0 -i - -R local /usr/local/vds-1.4.7/bin/transfer  -f  base-uri se-mount-point",
			"/usr/local/vds-1.4.7/bin/kickstart");
	
	public static final SubmitHost INTENSITY = new SubmitHost("Intensity", "intensity.usc.edu",
			"/scratch/opensha/kmilner/hazMapRuns", "/scratch/opensha/kmilner/dependencies", "jobmanager-fork",
			"GLOBUS_LOCATION=/usr/scec/globus-4.0.4;LD_LIBRARY_PATH=/usr/scec/globus-4.0.4/lib;",
			"-n transfer -N pegasus::transfer:1.0 -i - -R local /usr/scec/pegasus/pegasus-2.1.0cvs-20080130/bin/transfer  -f  base-uri se-mount-point",
			"/usr/scec/pegasus/pegasus-2.1.0cvs-20080130/bin/kickstart");
	
	public static final SubmitHost AFTERSHOCK = new SubmitHost("Aftershock", "aftershock.usc.edu", "/scratch/opensha/tera3d/hazMapRuns", "/scratch/opensha/tera3d/dependencies", "jobmanager-fork",
			"GLOBUS_LOCATION=/usr/local/globus/default;LD_LIBRARY_PATH=/usr/local/globus/default/lib;",
			"-n transfer -N pegasus::transfer:1.0 -i - -R local /usr/local/pegasus/default/bin/transfer  -f  base-uri se-mount-point",
			"/usr/local/pegasus/default/bin/kickstart");
	
	public String name = "";
	public String hostName = "";
	public String path = "";
	public String dependencyPath = "";
	public String forkScheduler = "";
	public String transferEnvironment = "";
	public String transferArguments = "";
	public String transferExecutable = "";
	
	public SubmitHost(String name, String hostName, String path, String dependencyPath, String forkScheduler,
			String transferEnvironment, String transferArguments, String transferExecutable) {
		this.name = name;
		this.hostName = hostName;
		this.path = path;
		this.dependencyPath = dependencyPath;
		this.forkScheduler = forkScheduler;
		this.transferEnvironment = transferEnvironment;
		this.transferArguments = transferArguments;
		this.transferExecutable = transferExecutable;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(SubmitHost.XML_METADATA_NAME);
		
		xml.addAttribute("name", this.name);
		xml.addAttribute("hostName", this.hostName);
		xml.addAttribute("path", this.path);
		xml.addAttribute("dependencyPath", this.dependencyPath);
		xml.addAttribute("forkScheduler", this.forkScheduler);
		xml.addAttribute("transferEnvironment", this.transferEnvironment);
		xml.addAttribute("transferArguments", this.transferArguments);
		xml.addAttribute("transferExecutable", this.transferExecutable);
		
		return root;
	}
	
	public static SubmitHost fromXMLMetadata(Element submitHostElem) {
		String name = submitHostElem.attribute("name").getValue();
		String submitHost = submitHostElem.attribute("hostName").getValue();
		String submitHostPath = submitHostElem.attribute("path").getValue();
		String submitHostPathToDependencies = submitHostElem.attribute("dependencyPath").getValue();
		String forkScheduler = submitHostElem.attribute("forkScheduler").getValue();
		String submitHostTransfer_env = submitHostElem.attribute("transferEnvironment").getValue();
		String submitHostTransfer_args = submitHostElem.attribute("transferArguments").getValue();
		String submitHostTransfer_exec = submitHostElem.attribute("transferExecutable").getValue();
		
		return new SubmitHost(name, submitHost, submitHostPath, submitHostPathToDependencies, forkScheduler,
				submitHostTransfer_env, submitHostTransfer_args, submitHostTransfer_exec);
	}
}
