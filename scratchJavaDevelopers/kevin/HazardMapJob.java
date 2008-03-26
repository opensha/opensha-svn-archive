package scratchJavaDevelopers.kevin;

import org.dom4j.Element;
import org.opensha.metadata.XMLSaveable;

public class HazardMapJob implements XMLSaveable {
	
	public static final String HPC_NAME = "HPC";
	public static final GridJobPreset HPC_PRESET = new GridJobPreset(HazardMapJob.HPC_NAME, "hpc.usc.edu", "jobmanager-pbs", "/usr/bin/java", "/auto/scec-00/kmilner/hazMaps/");
	
	public static final String DYNAMIC_NAME = "Dynamic";
	public static final GridJobPreset DYNAMIC_PRESET = new GridJobPreset(HazardMapJob.DYNAMIC_NAME, "dynamic.usc.edu", "jobmanager-pbs", "/usr/java/jdk1.5.0_10/bin/java", "/nfs/dynamic-1/opensha/kmilner/hazMaps/");
	
	public static final String XML_METADATA_NAME = "hazardMapJob";
	
	
	String jobName;
	String rp_host;
	String rp_batchScheduler;
	String rp_javaPath;
	String rp_storagePath;
	String repo_host;
	String repo_storagePath;
	int sitesPerJob;
	boolean useCVM;
	boolean saveERF;
	String metadataFileName;
	
	public HazardMapJob(String jobName, String rp_host, String rp_batchScheduler,
			String rp_javaPath, String rp_storagePath, String repo_host, String repo_storagePath,
			int sitesPerJob, boolean useCVM, boolean saveERF, String metadataFileName) {
		this.jobName = jobName;
		this.rp_host = rp_host;
		this.rp_batchScheduler = rp_batchScheduler;
		this.rp_javaPath = rp_javaPath;
		this.rp_storagePath = rp_storagePath;
		this.repo_host = repo_host;
		this.repo_storagePath = repo_storagePath;
		this.sitesPerJob = sitesPerJob;
		this.useCVM = useCVM;
		this.saveERF = saveERF;
		this.metadataFileName = metadataFileName;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(HazardMapJob.XML_METADATA_NAME);
		xml.addAttribute("jobName", this.jobName);
		xml.addAttribute("rp_host", this.rp_host);
		xml.addAttribute("rp_storagePath", this.rp_storagePath);
		xml.addAttribute("rp_javaPath", this.rp_javaPath);
		xml.addAttribute("rp_batchScheduler", this.rp_batchScheduler);
		xml.addAttribute("repo_host", this.repo_host);
		xml.addAttribute("repo_storagePath", this.repo_storagePath);
		xml.addAttribute("sitesPerJob", this.sitesPerJob + "");
		xml.addAttribute("useCVM", this.useCVM + "");
		xml.addAttribute("saveERF", this.saveERF + "");
		xml.addAttribute("metadataFileName", this.metadataFileName);
		
		return root;
	}
	
	public static HazardMapJob fromXMLMetadata(Element root) {
		Element jobParams = root.element(HazardMapJob.XML_METADATA_NAME);
		
		String jobName = jobParams.attribute("jobName").getValue();
		String rp_host = jobParams.attribute("rp_host").getValue();
		String rp_storagePath = jobParams.attribute("rp_storagePath").getValue();
		String rp_javaPath = jobParams.attribute("rp_javaPath").getValue();
		String rp_batchScheduler = jobParams.attribute("rp_batchScheduler").getValue();
		String repo_host = jobParams.attribute("repo_host").getValue();
		String repo_storagePath = jobParams.attribute("repo_storagePath").getValue();
		int sitesPerJob = Integer.parseInt(jobParams.attribute("sitesPerJob").getValue());
		boolean useCVM = Boolean.parseBoolean(jobParams.attribute("useCVM").getValue());
		boolean saveERF = Boolean.parseBoolean(jobParams.attribute("saveERF").getValue());
		String metadataFileName = jobParams.attribute("metadataFileName").getValue();
		
		return new HazardMapJob(jobName, rp_host, rp_batchScheduler, rp_javaPath, rp_storagePath, repo_host, repo_storagePath, sitesPerJob, useCVM, saveERF, metadataFileName);
	}
}