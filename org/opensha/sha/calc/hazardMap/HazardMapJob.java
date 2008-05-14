package org.opensha.sha.calc.hazardMap;

import org.dom4j.Element;
import org.opensha.gridComputing.ResourceProvider;
import org.opensha.gridComputing.SubmitHost;
import org.opensha.metadata.XMLSaveable;

public class HazardMapJob implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "hazardMapJob";
	
	public static final String DEFAULT_SUBMIT_HOST = "scecit18.usc.edu";
	public static final String DEFAULT_SUBMIT_HOST_PATH = "/home/kmilner/hazMapRuns";
	public static final String DEFAULT_DEPENDENCY_PATH = "/home/kmilner/dependencies";
	
	public static final String DEFAULT_REPO_HOST = "hpc.usc.edu";
	public static final String DEFAULT_REPO_STORAGE_PATH = "/TEMP/PATH";
	
	
	public String jobName = "";
	
	public ResourceProvider rp;
	public SubmitHost submitHost;
	
//	public String repo_host = "";
//	public String repo_storagePath = "";
	public int sitesPerJob = 100;
	public int maxWallTime = 240;
	public boolean useCVM = true;
	public boolean saveERF = true;
	public String metadataFileName = "";
	
	public HazardMapJob() {
		
	}
	
	public HazardMapJob(String jobName, ResourceProvider rp, SubmitHost submit,
			int sitesPerJob, int maxWallTime, boolean useCVM, boolean saveERF, String metadataFileName) {
		this.jobName = jobName;
		this.rp = rp;
		this.submitHost = submit;
		this.sitesPerJob = sitesPerJob;
		this.maxWallTime = maxWallTime;
		this.useCVM = useCVM;
		this.saveERF = saveERF;
		this.metadataFileName = metadataFileName;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(HazardMapJob.XML_METADATA_NAME);
		xml.addAttribute("jobName", this.jobName);
		xml.addAttribute("sitesPerJob", this.sitesPerJob + "");
		xml.addAttribute("maxWallTime", this.maxWallTime + "");
		xml.addAttribute("useCVM", this.useCVM + "");
		xml.addAttribute("saveERF", this.saveERF + "");
		xml.addAttribute("metadataFileName", this.metadataFileName);
		
		xml = rp.toXMLMetadata(xml);
		xml = submitHost.toXMLMetadata(xml);
		
		return root;
	}
	
	public static HazardMapJob fromXMLMetadata(Element jobParams) {
		
		String jobName = jobParams.attribute("jobName").getValue();
		int sitesPerJob = Integer.parseInt(jobParams.attribute("sitesPerJob").getValue());
		int maxWallTime = Integer.parseInt(jobParams.attribute("maxWallTime").getValue());
		boolean useCVM = Boolean.parseBoolean(jobParams.attribute("useCVM").getValue());
		boolean saveERF = Boolean.parseBoolean(jobParams.attribute("saveERF").getValue());
		String metadataFileName = jobParams.attribute("metadataFileName").getValue();
		
		Element rpElem = jobParams.element(ResourceProvider.XML_METADATA_NAME);
		ResourceProvider rp = ResourceProvider.fromXMLMetadata(rpElem);
		
		Element submitElem = jobParams.element(SubmitHost.XML_METADATA_NAME);
		SubmitHost submit = SubmitHost.fromXMLMetadata(submitElem);
		
		return new HazardMapJob(jobName, rp, submit, sitesPerJob, maxWallTime, useCVM, saveERF, metadataFileName);
	}
}