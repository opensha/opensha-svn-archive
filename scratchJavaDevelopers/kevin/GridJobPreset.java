package scratchJavaDevelopers.kevin;

public class GridJobPreset {
	
	public String name;
	public String rp_host;
	public String rp_batchScheduler;
	public String rp_javaPath;
	public String rp_storagePath;
	public String rp_globusrsl;
	
	public GridJobPreset(String name, String rp_host, String rp_batchScheduler, String rp_javaPath, String rp_storagePath, String globusrsl) {
		this.name = name;
		this.rp_host = rp_host;
		this.rp_batchScheduler = rp_batchScheduler;
		this.rp_javaPath = rp_javaPath;
		this.rp_storagePath = rp_storagePath;
		this.rp_globusrsl = globusrsl;
	}
}