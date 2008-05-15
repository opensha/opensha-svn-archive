package org.opensha.gridComputing;

import org.dom4j.Element;
import org.opensha.metadata.XMLSaveable;

public class ResourceProvider implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "ResourceProvider";
	
	/**
	 * Preset for running on HPC as kmilner
	 * @return
	 */
	public static final ResourceProvider HPC() {
		GlobusRSL rsl = new GlobusRSL(GlobusRSL.SINGLE_JOB_TYPE, 240);
		rsl.setQueue("scec");
		ResourceProvider HPC = new ResourceProvider("HPC", "hpc.usc.edu", "jobmanager-pbs", "jobmanager-fork",
				"/usr/bin/java", "/home/scec-00/kmilner/hazMaps",
				"", "hpc.usc.edu", "globus", rsl);
		return HPC;
	}
	
	private static String ABE_HOST = "grid-abe.ncsa.teragrid.org";
	private static String ABE_BATCH = "jobmanager-pbs";
	private static String ABE_FORK = "jobmanager-fork";
	private static String ABE_JAVA = "/usr/local/jdk1.5.0_12/bin/java";
	private static String ABE_DIR = "/cfs/scratch/users/kmilner/hazMaps";
	private static String ABE_REQS = "(FileSystemDomain==\"abe.ncsa.teragrid.org\")&&(Arch==\"X86_64\")&&(Disk>=0)&&(Memory>=0)&&(OpSys==\"LINUX\")";
	private static String ABE_GRID_FTP = "login-abe.ncsa.teragrid.org:2811";
	
	/**
	 * Preset for running on ABE with Glide-Ins as kmilner
	 * @return
	 */
	public static final ResourceProvider ABE_GLIDE_INS() {
		GlobusRSL rsl = new GlobusRSL(GlobusRSL.SINGLE_JOB_TYPE, 240);
		ResourceProvider ABE_GLIDE_INS = new ResourceProvider("Abe (w/ Glide-Ins)", ABE_HOST, ABE_BATCH, ABE_FORK,
				ABE_JAVA, ABE_DIR, ABE_REQS, ABE_GRID_FTP, "vanilla", rsl);
		return ABE_GLIDE_INS;
	}
	
	/**
	 * Preset for running on ABE without Glide-Ins as kmilner
	 * @return
	 */
	public static final ResourceProvider ABE_NO_GLIDE_INS() {
		GlobusRSL rsl = new GlobusRSL(GlobusRSL.SINGLE_JOB_TYPE, 240);
		ResourceProvider ABE_NO_GLIDE_INS = new ResourceProvider("Abe (w/o Glide-Ins)", ABE_HOST, ABE_BATCH, ABE_FORK,
				ABE_JAVA, ABE_DIR, ABE_REQS, ABE_GRID_FTP, "globus", rsl);
		return ABE_NO_GLIDE_INS;
	}
	
	/**
	 * Preset for running on Dynamic as kmilner
	 * @return
	 */
	public static final ResourceProvider DYNAMIC() {
		GlobusRSL rsl = new GlobusRSL(GlobusRSL.SINGLE_JOB_TYPE, 240);
		rsl.setQueue("mpi");
		ResourceProvider DYNMAIC = new ResourceProvider("Dynamic", "dynamic.usc.edu", "jobmanager-pbs", "jobmanager-fork",
				"/usr/java/jdk1.5.0_10/bin/java", "/nfs/dynamic-1/opensha/kmilner/hazMaps",
				"", "dynamic.usc.edu", "globus", rsl);
		return DYNMAIC;
	}
	
	/**
	 * Preset for running on ORNL as kmilner
	 * @return
	 */
	public static final ResourceProvider ORNL() {
		GlobusRSL rsl = new GlobusRSL(GlobusRSL.SINGLE_JOB_TYPE, 240);
		ResourceProvider DYNMAIC = new ResourceProvider("Oak Ridge Ntnl Labs", "tg-login.ornl.teragrid.org:2119", "jobmanager-pbs", "jobmanager-fork",
				"/usr/bin/java", "/scratch/kevinm/hazMaps",
				"", "tg-gridftp.ornl.teragrid.org:2811", "globus", rsl);
		return DYNMAIC;
	}
	
	public String name = "";
	public String hostName = "";
	public String batchScheduler = "";
	public String forkScheduler = "";
	public String javaPath = "";
	public String storagePath = "";
	public String requirements = "";
	public String gridFTPHost = "";
	public String universe = "";
	public GlobusRSL globusRSL;
	
	public ResourceProvider(String name, String hostName, String batchScheduler, String forkScheduler,
			String javaPath, String storagePath, String requirements,
			String gridFTPHost, String universe, GlobusRSL globusRSL) {
		this.name = name;
		this.hostName = hostName;
		this.batchScheduler = batchScheduler;
		this.forkScheduler = forkScheduler;
		this.javaPath = javaPath;
		this.storagePath = storagePath;
		this.requirements = requirements;
		this.gridFTPHost = gridFTPHost;
		this.universe = universe;
		this.globusRSL = globusRSL;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(ResourceProvider.XML_METADATA_NAME);
		
		xml.addAttribute("name", this.name);
		xml.addAttribute("hostName", this.hostName);
		xml.addAttribute("batchScheduler", this.batchScheduler);
		xml.addAttribute("forkScheduler", this.forkScheduler);
		xml.addAttribute("javaPath", this.javaPath);
		xml.addAttribute("storagePath", this.storagePath);
		xml.addAttribute("requirements", this.requirements);
		xml.addAttribute("gridFTPHost", this.gridFTPHost);
		xml.addAttribute("universe", this.universe);
		
		xml = this.globusRSL.toXMLMetadata(xml);
		
		return root;
	}
	
	public static ResourceProvider fromXMLMetadata(Element resourceProviderElem) {
		
		String name = resourceProviderElem.attribute("name").getValue();
		String rp_host = resourceProviderElem.attribute("hostName").getValue();
		String rp_batchScheduler = resourceProviderElem.attribute("batchScheduler").getValue();
		String rp_forkScheduler = resourceProviderElem.attribute("forkScheduler").getValue();
		String rp_javaPath = resourceProviderElem.attribute("javaPath").getValue();
		String rp_storagePath = resourceProviderElem.attribute("storagePath").getValue();
		String rp_requirements = resourceProviderElem.attribute("requirements").getValue();
		String rp_globus_ftp_host = resourceProviderElem.attribute("gridFTPHost").getValue();
		String rp_universe = resourceProviderElem.attribute("universe").getValue();
		
		Element rslElem = resourceProviderElem.element(GlobusRSL.XML_METADATA_NAME);
		GlobusRSL globusRSL = GlobusRSL.fromXMLMetadata(rslElem);
		
		return new ResourceProvider(name, rp_host, rp_batchScheduler, rp_forkScheduler,
				rp_javaPath, rp_storagePath, rp_requirements,
				rp_globus_ftp_host, rp_universe, globusRSL);
	}
}
