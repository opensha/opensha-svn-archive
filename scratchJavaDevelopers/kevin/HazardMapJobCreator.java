package scratchJavaDevelopers.kevin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedRELM_TestingRegion;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.gui.servlets.siteEffect.BasinDepthClass;
import org.opensha.sha.gui.servlets.siteEffect.WillsSiteClass;

public class HazardMapJobCreator {
	
	private String outputDir = "";
	private String remoteJobDir = "";
	
	public static final String OUTPUT_FILES_DIR_NAME = "outfiles";
	public static final String SUBMIT_FILES_DIR_NAME = "submitfiles";
	public static final String SCRIPT_FILES_DIR_NAME = "scriptfiles";
	public static final String METADATA_FILE_NAME = "metadata.txt";
	
	public String globusrsl = "(jobtype=single)(maxwalltime=180)";
	
	// hpc
	public String executable = "/usr/bin/java";
	public String globusscheduler = "hpc.usc.edu/jobmanager-pbs";
	
	// dynamic
//	public String executable = "/usr/java/jdk1.5.0_10/bin/java";
//	public String globusscheduler = "dynamic.usc.edu/jobmanager-pbs";
	
	EqkRupForecastAPI erf;
	SitesInGriddedRegionAPI sites;
	
	boolean loadERFFromFile = false;
	boolean loadSitesFromFile = false;
	
	int startIndex;
	int endIndex;
	int curvesPerJob;
	
	boolean cvmFromFile = true;
	String willsFileName = "etc/cvmfiles/usgs_cgs_geology_60s_mod.txt";
	String basinFileName = "etc/cvmfiles/basindepth_OpenSHA.txt";
	
	ArrayList<String> jobNames = new ArrayList<String>();
	
	public int nameLength = 6;
	
	public HazardMapJobCreator(String outputDir, String remoteJobDir, SitesInGriddedRegionAPI sites, int curvesPerJob) {
		this(outputDir, remoteJobDir, sites, 0, sites.getNumGridLocs() - 1, curvesPerJob);
	}
	
	public HazardMapJobCreator(String outputDir, String remoteJobDir, SitesInGriddedRegionAPI sites, int startIndex, int endIndex, int curvesPerJob) {
//		if (erf != null)
//			loadERFFromFile = true;
//		if (sites != null)
//			loadSitesFromFile = true;
		this.outputDir = outputDir;
		this.remoteJobDir = remoteJobDir;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.curvesPerJob = curvesPerJob;
		this.sites = sites;
	}
	
	public void createJobs(boolean useCVM) throws IOException {
		System.out.println("Creating jobs for " + sites.getNumGridLocs() + " sites!");
		
		File outDir = new File(outputDir);
		if (!outDir.exists())
			outDir.mkdir();
		int jobs = 0;
		long start = System.currentTimeMillis();
		for (int i=startIndex; i<=endIndex; i+=curvesPerJob) {
			int jobEndIndex = i + curvesPerJob;
			String regionName = addLeadingZeros(i) + "_" + addLeadingZeros(jobEndIndex);
			String jobFilePrefix = "Job_" + regionName;
			
			String cvmFileName = "";
			if (useCVM) {
				cvmFileName = createCVMJobFile(regionName, i, jobEndIndex);
			}
			
			String jobFileName = jobFilePrefix + ".sub";
			jobNames.add(jobFileName);
			System.out.println("Creating " + jobFileName);
			FileWriter fr = new FileWriter(outputDir + jobFileName);
			fr.write("universe = globus" + "\n");
			fr.write("executable = " + executable + "\n");
			fr.write("globusrsl = " + globusrsl + "\n");
			fr.write("globusscheduler = " + globusscheduler + "\n");
			fr.write("should_transfer_files = yes" + "\n");
			fr.write("WhenToTransferOutput = ON_EXIT" + "\n");
			fr.write("executable = " + executable + "\n");
			fr.write("arguments = -cp " + remoteJobDir + "/opensha_gridHazMapGenerator.jar org.opensha.sha.calc.GridHardcodedHazardMapCalculator " + i + " " + jobEndIndex + " true " + cvmFileName + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("output = " + jobFilePrefix + ".out" + "\n");
			fr.write("error = " + jobFilePrefix + ".err" + "\n");
			fr.write("log = " + jobFilePrefix + ".log" + "\n");
			fr.write("transfer_executable = false" + "\n");
			fr.write("transfer_error = true" + "\n");
			fr.write("transfer_output = true" + "\n");
			fr.write("notification = never" + "\n");
			fr.write("remote_initialdir = " + remoteJobDir + "\n");
			fr.write("queue" + "\n\n");
			fr.close();
			jobs++;
		}
		System.out.println("Tobal Jobs Created: " + jobs);
		long duration = System.currentTimeMillis() - start;
		//double estimate = average * (double)numSites + (double)overhead * (numSites / curvesPerJob);
		double mins = duration / 60000d;
		String minsStr = new DecimalFormat(	"###.##").format(mins);
		String seconds = new DecimalFormat(	"###.##").format(duration / 1000d);
		System.out.println("Total Job Time: " + seconds + " seconds = " + minsStr + " mins");
		System.out.println("Time Per Job: " + new DecimalFormat(	"###.##").format(duration / (double)jobs / 1000d) + " seconds");
		
		double estimatedMins = (mins / (double)jobs) * (double)sites.getNumGridLocs() / (double)curvesPerJob;
		System.out.println("Estimated time (based on current, " + sites.getNumGridLocs() + " curves): " + new DecimalFormat(	"###.##").format(estimatedMins) + " mins");
		estimatedMins = (mins / (double)jobs) * 200000d / (double)curvesPerJob;
		System.out.println("Estimated time (based on 200,000 curves): " + new DecimalFormat(	"###.##").format(estimatedMins) + " mins");
	}
	
	private String createCVMJobFile(String jobName, int startIndex, int endIndex) {
		String fileName = jobName + ".cvm";
		LocationList locs = new LocationList();
		
		if (endIndex > this.endIndex)
			endIndex = this.endIndex;
		
		System.out.println("Writing CVM info for sites " + startIndex + " to " + endIndex + " into " + fileName);
		
		for (int i=startIndex; i<=endIndex; i++) {
			try {
				locs.addLocation(sites.getSite(i).getLocation());
			} catch (RegionConstraintException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("Locations: " + locs.size());
		try {
			ArrayList willsSiteClassList = null;
			ArrayList basinDepth = null;
			
			if (cvmFromFile) {
				WillsSiteClass wills = new WillsSiteClass(locs, willsFileName);
				willsSiteClassList=  wills.getWillsSiteClass();
				BasinDepthClass basin = new BasinDepthClass(locs, basinFileName);
				basinDepth = basin.getBasinDepth();
			} else {
				willsSiteClassList= ConnectToCVM.getWillsSiteTypeFromCVM(locs);
				basinDepth= ConnectToCVM.getBasinDepthFromCVM(locs);
			}
			
			if (willsSiteClassList == null) {
				System.err.println("Wills is NULL!");
				willsSiteClassList = new ArrayList();
				for (int i=0; i<(endIndex - startIndex); i++) {
					willsSiteClassList.add("NA");
				}
			}
			
			if (basinDepth == null) {
				System.err.println("Basin is NULL!");
				basinDepth = new ArrayList();
				for (int i=0; i<(endIndex - startIndex); i++) {
					basinDepth.add(1.0);
				}
			}
			
			if (willsSiteClassList.size() != basinDepth.size() || basinDepth.size() != (endIndex - startIndex + 1)) {
				System.err.println("ERROR: not the same size!!!!!");
				return "";
			}
			
			int cvmVals = 0;
			FileWriter fr = new FileWriter(outputDir + fileName);
//			System.out.println("Wills Size: " + willsSiteClassList.size());
			for (int i=0; i< willsSiteClassList.size(); i++) {
				//System.out.println("Site Type: " + willsSiteClassList.get(i));
				//System.out.println("Site Basin: " + basinDepth.get(i));
				fr.write(locs.getLocationAt(i).getLatitude() + "\t");
				fr.write(locs.getLocationAt(i).getLongitude() + "\t");
				fr.write(willsSiteClassList.get(i) + "\t" + basinDepth.get(i) + "\n");
				fr.flush();
				cvmVals++;
			}
			fr.close();
//			System.out.println("Wrote " + cvmVals + " values!");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return fileName;
	}
	
	public void createSubmitScripts(int jobsPerScript) throws IOException {
		int i = 0;
		int scripts = 0;
		FileWriter fr = null;
		while (i < jobNames.size()) {
			if (i % jobsPerScript == 0) {
				if (fr != null)
					fr.close();
				String fileName = outputDir + "submit_" + addLeadingZeros(scripts) + ".sh";
				System.out.println("Creating " + fileName);
				fr = new FileWriter(fileName);
				scripts++;
			}
			fr.write("condor_submit " + jobNames.get(i) + "\n");
			fr.write("sleep 2\n");
			i++;
		}
		if (fr != null)
			fr.close();
	}
	
	public String addLeadingZeros(int num) {
		String str = num + "";
		if (str.length() > nameLength)
			return str;
		
		while (str.length() < nameLength)
			str = "0" + str;
		
		return str;
	}
	
	public static void main(String args[]) {
        try {
        	String metadataFileName = args[0];
    		SAXReader reader = new SAXReader();
			Document document = reader.read(new File(metadataFileName));
			Element jobParams = document.getRootElement().element("gridJobParameters");
			
			String rp_host = jobParams.attribute("rp_host").getValue();
			String rp_storagePath = jobParams.attribute("rp_storagePath").getValue();
			String rp_javaPath = jobParams.attribute("rp_javaPath").getValue();
			String rp_batchScheduler = jobParams.attribute("rp_batchScheduler").getValue();
			String repo_host = jobParams.attribute("repo_host").getValue();
			String repo_storagePath = jobParams.attribute("repo_storagePath").getValue();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (DocumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		String outputDir = "/home/kevin/OpenSHA/condor/jobs/jobTest/";
		// SDSC
//		String remoteJobDir = "/gpfs/projects/scec/CyberShake2007/opensha/kevin/meetingMap";
		// HPC
		String remoteJobDir = "/auto/scec-00/dpmeyers/proj/openSHA/kevin/verificationMap0.02";
		// Dynamic
//		String remoteJobDir = "/nfs/dynamic-1/opensha/kmilner/verification_0.02";
		boolean useCVM = true;
		EvenlyGriddedRELM_TestingRegion region = new EvenlyGriddedRELM_TestingRegion(); 
		SitesInGriddedRegionAPI sites = new SitesInGriddedRegion(region.getRegionOutline(), 1);
		
		try {
			sites = new SitesInGriddedRectangularRegion(33.5, 34.8, -120.0, -116.0, .02);
		} catch (RegionConstraintException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		HazardMapJobCreator creator = new HazardMapJobCreator(outputDir, remoteJobDir, sites, 100);
//		HazardMapJobCreator creator = new HazardMapJobCreator(outputDir, remoteJobDir, sites, 1700, 2199, 100);
		
		try {
			creator.createJobs(useCVM);
			creator.createSubmitScripts(24);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
