package org.opensha.gridComputing;

import org.dom4j.Element;
import org.opensha.metadata.XMLSaveable;

public class StorageHost implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "StorageHost";
	
	public static final StorageHost HPC = new StorageHost("HPC", "hpc.usc.edu", "hpc.usc.edu", "/home/scec-00/tera3d/opensha/hazmaps", "jobmanager-fork", "jobmanager-pbs", "/usr/bin/java", "/home/scec-00/kmilner/hazMaps/hazMapProcessing.jar");
	
	public String name = "";
	public String schedulerHostName = "";
	public String gridFTPHostName = "";
	public String path = "";
	public String forkScheduler = "";
	public String batchScheduler = "";
	public String javaPath = "";
	public String jarPath = "";
	
	public StorageHost(String name, String forkHostName, String gridFTPHostName, String path, String forkScheduler, String batchScheduler, String javaPath, String jarPath) {
		this.name = name;
		this.schedulerHostName = forkHostName;
		this.gridFTPHostName = gridFTPHostName;
		this.path = path;
		this.forkScheduler = forkScheduler;
		this.batchScheduler = batchScheduler;
		this.javaPath = javaPath;
		this.jarPath = jarPath;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(StorageHost.XML_METADATA_NAME);
		
		xml.addAttribute("name", this.name);
		xml.addAttribute("schedulerHostName", this.schedulerHostName);
		xml.addAttribute("gridFTPHostName", this.gridFTPHostName);
		xml.addAttribute("path", this.path);
		xml.addAttribute("forkScheduler", this.forkScheduler);
		xml.addAttribute("batchScheduler", this.batchScheduler);
		xml.addAttribute("javaPath", this.javaPath);
		xml.addAttribute("jarPath", this.jarPath);
		
		return root;
	}
	
	public static StorageHost fromXMLMetadata(Element resourceProviderElem) {
		
		String name = resourceProviderElem.attribute("name").getValue();
		String schedulerHostName = resourceProviderElem.attribute("schedulerHostName").getValue();
		String gridFTPHostName = resourceProviderElem.attribute("gridFTPHostName").getValue();
		String path = resourceProviderElem.attribute("path").getValue();
		String forkScheduler = resourceProviderElem.attribute("forkScheduler").getValue();
		String batchScheduler = resourceProviderElem.attribute("batchScheduler").getValue();
		String javaPath = resourceProviderElem.attribute("javaPath").getValue();
		String jarPath = resourceProviderElem.attribute("jarPath").getValue();
		
		return new StorageHost(name, schedulerHostName, gridFTPHostName, path, forkScheduler, batchScheduler, javaPath, jarPath);
	}
}
