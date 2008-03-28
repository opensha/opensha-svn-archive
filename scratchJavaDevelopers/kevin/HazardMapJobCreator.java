package scratchJavaDevelopers.kevin;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.gui.servlets.siteEffect.BasinDepthClass;
import org.opensha.sha.gui.servlets.siteEffect.WillsSiteClass;
import org.opensha.util.FileUtils;

public class HazardMapJobCreator {

	private String outputDir = "";
	
	public static final String OUTPUT_FILES_DIR_NAME = "outfiles";
	public static final String SUBMIT_FILES_DIR_NAME = "submitfiles";
	public static final String SCRIPT_FILES_DIR_NAME = "scriptfiles";
	
	private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
	
	// hpc
//	public String executable = "/usr/bin/java";
//	public String globusscheduler = "hpc.usc.edu/jobmanager-pbs";
//	public String globusrsl = "(jobtype=single)(maxwalltime=180)(project=TG-MCA03T012)";
	public String globusrsl = "(queue=mpi)(jobtype=single)(maxwalltime=60)";
	
	// dynamic
//	public String executable = "/usr/java/jdk1.5.0_10/bin/java";
//	public String globusscheduler = "dynamic.usc.edu/jobmanager-pbs";
//	public String globusrsl = "(jobtype=single)(maxwalltime=180)(queue=MPI)";
	
	// ornl
//	public String executable = "/usr/bin/java";
//	public String globusscheduler = "tg-login.ornl.teragrid.org/jobmanager-pbs";
//	public String globusrsl = "(jobtype=single)(project=TG-MCA03T012)(maxwalltime=180)";
	
	HazardMapJob job;
	
	SitesInGriddedRegionAPI sites;
	
	int startIndex;
	int endIndex;
	
	boolean cvmFromFile = true;
	String willsFileName = "etc/cvmfiles/usgs_cgs_geology_60s_mod.txt";
	String basinFileName = "etc/cvmfiles/basindepth_OpenSHA.txt";
	
	ArrayList<String> jobNames = new ArrayList<String>();
	
	public int nameLength = 6;
	
	public HazardMapJobCreator(String outputDir, SitesInGriddedRegionAPI sites, HazardMapJob job) {
		this(outputDir, sites, 0, sites.getNumGridLocs() - 1, job);
	}
	
	public HazardMapJobCreator(String outputDir, SitesInGriddedRegionAPI sites, int startIndex, int endIndex, HazardMapJob job) {
		this.job = job;
		this.sites = sites;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.outputDir = outputDir;
//		this.executable = rp_javaPath;
//		this.globusscheduler = rp_host + "/" + rp_batchScheduler;
	}
	
	public void createJobs() throws IOException {
		System.out.println("Creating jobs for " + sites.getNumGridLocs() + " sites!");
		
		File outDir = new File(outputDir);
		if (!outDir.exists())
			outDir.mkdir();
		int jobs = 0;
		long start = System.currentTimeMillis();
		for (int i=startIndex; i<=endIndex; i+=job.sitesPerJob) {
			int jobEndIndex = i + job.sitesPerJob;
			String regionName = addLeadingZeros(i) + "_" + addLeadingZeros(jobEndIndex);
			String jobFilePrefix = "Job_" + regionName;
			String cvmFileName = "";
			if (job.useCVM) {
				cvmFileName = createCVMJobFile(regionName, i, jobEndIndex);
			}
			
			String globusscheduler = job.rp_host + "/" + job.rp_batchScheduler;
			
			String jobFileName = jobFilePrefix + ".sub";
			jobNames.add(jobFileName);
			System.out.println("Creating " + jobFileName);
			FileWriter fr = new FileWriter(outputDir + jobFileName);
			fr.write("universe = globus" + "\n");
			fr.write("globusrsl = " + job.rp_globusrsl + "\n");
			fr.write("globusscheduler = " + globusscheduler + "\n");
			fr.write("should_transfer_files = yes" + "\n");
			fr.write("WhenToTransferOutput = ON_EXIT" + "\n");
			fr.write("executable = " + job.rp_javaPath + "\n");
			fr.write("arguments = -cp " + job.rp_storagePath + "/opensha_gridHazMapGenerator.jar org.opensha.sha.calc.GridMetadataHazardMapCalculator " + i + " " + jobEndIndex + " " + job.metadataFileName + " " + cvmFileName + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("output = " + jobFilePrefix + ".out" + "\n");
			fr.write("error = " + jobFilePrefix + ".err" + "\n");
			fr.write("log = " + jobFilePrefix + ".log" + "\n");
			fr.write("transfer_executable = false" + "\n");
			fr.write("transfer_error = true" + "\n");
			fr.write("transfer_output = true" + "\n");
			fr.write("notification = never" + "\n");
			fr.write("remote_initialdir = " + job.rp_storagePath + "\n");
			fr.write("queue" + "\n\n");
			
//			fr.write("universe = java" + "\n");
//			fr.write("executable = /opt/jdk1.6.0/jre/bin/java\n");
//			fr.write("jar_files = /usr/rmt_share/scratch96/k/kevinm/benchmark_RELM_UCERF_0.1_3/opensha_gridHazMapGenerator.jar\n");
//			fr.write("should_transfer_files = yes" + "\n");
//			fr.write("WhenToTransferOutput = ON_EXIT" + "\n");
//			fr.write("arguments = org.opensha.sha.calc.GridHardcodedHazardMapCalculator " + i + " " + jobEndIndex + " true " + cvmFileName + "\n");
//			fr.write("requirements = (HasCTSS=?=True) && (CanReachInternet==True) && (JavaVersion==\"1.6.0\")\n");
//			fr.write("copy_to_spool = false" + "\n");
//			fr.write("output = " + jobFilePrefix + ".out" + "\n");
//			fr.write("error = " + jobFilePrefix + ".err" + "\n");
//			fr.write("log = " + jobFilePrefix + ".log" + "\n");
//			fr.write("transfer_executable = false" + "\n");
//			fr.write("transfer_error = true" + "\n");
//			fr.write("transfer_output = true" + "\n");
//			fr.write("periodic_release = (NumSystemHolds <= 3)\n");
//			fr.write("periodic_remove = (NumSystemHolds > 3)\n");
//			fr.write("notification = never" + "\n");
//			fr.write("remote_initialdir = /usr/rmt_share/scratch96/k/kevinm/benchmark_RELM_UCERF_0.1_3/\n");
//			fr.write("queue" + "\n\n");
			
			
			fr.close();
			jobs++;
			
			// create the expected files
			int curveJobEndIndex = jobEndIndex-1;
			if (curveJobEndIndex > this.endIndex)
				curveJobEndIndex = this.endIndex;
			fr = new FileWriter(outputDir + jobFilePrefix + ".txt");
			fr.write("# globusscheduler = " + globusscheduler + "\n");
			fr.write("# remote_initialdir = " + job.rp_storagePath + "\n");
			for (int j=i; j<=curveJobEndIndex; j++) {
				try {
					Location loc = sites.getSite(j).getLocation();
					String lat = decimalFormat.format(loc.getLatitude());
					String lon = decimalFormat.format(loc.getLongitude());
					String jobDir = lat + "/";
					fr.write(jobDir + lat + "_" + lon + ".txt\n");
				} catch (RegionConstraintException e) {
					e.printStackTrace();
				}
			}
			fr.close();
		}
		System.out.println("Tobal Jobs Created: " + jobs);
		long duration = System.currentTimeMillis() - start;
		//double estimate = average * (double)numSites + (double)overhead * (numSites / curvesPerJob);
		double mins = duration / 60000d;
		String minsStr = new DecimalFormat(	"###.##").format(mins);
		String seconds = new DecimalFormat(	"###.##").format(duration / 1000d);
		System.out.println("Total Job Time: " + seconds + " seconds = " + minsStr + " mins");
		System.out.println("Time Per Job: " + new DecimalFormat(	"###.##").format(duration / (double)jobs / 1000d) + " seconds");
		
		double estimatedMins = (mins / (double)jobs) * (double)sites.getNumGridLocs() / (double)job.sitesPerJob;
		System.out.println("Estimated time (based on current, " + sites.getNumGridLocs() + " curves): " + new DecimalFormat(	"###.##").format(estimatedMins) + " mins");
		estimatedMins = (mins / (double)jobs) * 200000d / (double)job.sitesPerJob;
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
		FileWriter all = new FileWriter(outputDir + "submit_all.sh");
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
			all.write("condor_submit " + jobNames.get(i) + "\n");
			fr.write("sleep 2\n");
			all.write("sleep 1\n");
			i++;
		}
		if (fr != null)
			fr.close();
		all.close();
	}
	
	public String addLeadingZeros(int num) {
		String str = num + "";
		if (str.length() > nameLength)
			return str;
		
		while (str.length() < nameLength)
			str = "0" + str;
		
		return str;
	}
	
	
	public void createJarTransferInputFile (String outputDir, String remoteJobDir) {
		try {
			FileWriter fr = new FileWriter(outputDir + "/test.in");
			String hostnameandpath = "scecit18.usc.edu/home/dmeyers/proj/ogale/run_3_24_08/";
			
			fr.write("\n");
			fr.write("gsiftp://"+ hostnameandpath +"opensha_gridHazMapGenerator.jar");
			fr.write("\n");
			fr.write("gsiftp://");
			fr.write(job.rp_host);
			fr.write(remoteJobDir+"/");
			fr.write("opensha_gridHazMapGenerator.jar");
			fr.write("\n");
			fr.write("\n");
			
			fr.write("gsiftp://"+ hostnameandpath +"ERF.jar");
			fr.write("\n");
			fr.write("gsiftp://");
			fr.write(job.rp_host);
			fr.write(remoteJobDir+"/");
			fr.write("ERF.jar");			
			fr.write("\n");
			fr.write("\n");
			
			fr.write("gsiftp://"+ hostnameandpath +"erf.obj");
			fr.write("\n");
			fr.write("gsiftp://");
			fr.write(job.rp_host);
			fr.write(remoteJobDir+"/");
			fr.write("erf.obj");			
			fr.write("\n");
			fr.write("\n");
			fr.write("\n");

			File dir = new File(outputDir);
			String[] children = dir.list();
			
			FilenameFilter filter = new FilenameFilter() {
		        public boolean accept(File dir, String name) {
		            return name.endsWith(".cvm");
		        }
		    };
		    children = dir.list(filter);
		    
		    ArrayList arr = new ArrayList ();
		    for (int i = 0; i < children.length; i++) {
		    	arr.add(children[i]);
		    }
		    
		    Collections.sort(arr);
		    
		    for (int i = 0; i < children.length; i++) {
		    	fr.write("gsiftp://"+ hostnameandpath +arr.get(i));
				fr.write("\n");
				fr.write("gsiftp://");
				fr.write(job.rp_host);
				fr.write(remoteJobDir+"/");
				fr.write(arr.get(i)+"");			
				fr.write("\n");
				fr.write("\n");		    	
		    }
			
			fr.close();
		} catch (Exception e) {
			System.out.println (e);
		}		
	}
	
	public void createDAG (String outputDir, int numberOfJobs) {

		File dir = new File(outputDir);
		String jobName = "hazard_";
		String jobName1 = "transfer_";
		
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.endsWith(".sub");
	        }
	    };
	    String[] children = dir.list(filter);
	    
	    ArrayList arr = new ArrayList ();
	    for (int i = 0; i < children.length; i++) {
	    	arr.add(children[i]);
	    }
	    
	    Collections.sort(arr);		
		
		try {
			BufferedOutputStream fos = new BufferedOutputStream (new FileOutputStream(outputDir+"/main.dag"));
			StringBuffer str = new StringBuffer ();
			
			str.append("\n");
			str.append("\n");			
			// main job
			str.append("Job ");
			str.append("jar_transfer ");
			str.append ("tx_jars_2HPC.sub");
			str.append("\n");
			str.append("\n");
			// child jobs
			
			int jobCnt = 1;
			
			for (int i = 0; i < numberOfJobs; i++) {
				str.append("Job ");
				str.append(jobName).append(i+1).append(" ").append(arr.get(i));
				str.append("\n");
			}
			str.append("\n");
			
			for (int i = 0; i < numberOfJobs; i++) {
				str.append("Job ");
				str.append(jobName1).append(i+1).append(" ").append("tx_jars_2HOST_"+(i+1)+".sub");
				str.append("\n");
			}
			str.append("\n");			
			
			// parent child relationship
			str.append("PARENT ");
			str.append("jar_transfer ");
			str.append("CHILD ");
			
			for (int i = 0; i < numberOfJobs; i++) {
				str.append(jobName).append(i+1).append(" ");
			}
			str.append("\n");	
			for (int i = 0; i < numberOfJobs; i++) {
				str.append("PARENT ");
				str.append(jobName).append(i+1).append(" ");
				str.append("CHILD ");
				str.append(jobName1).append(i+1).append(" ");
				str.append("\n");	
			}			
			
			fos.write(str.toString().getBytes());
			fos.close();
		} catch (Exception e) {
			System.out.println (e);
		}		
	}	
	
	public void createJarTransferJobFile () {
		String jobFilePrefix = "test";
		String jobName = "tx_jars_2HPC";
		String exefile = "/usr/local/vds-1.4.7/bin/kickstart";
		
		try {
			FileWriter fr = new FileWriter(outputDir + jobName+".sub");
			
			fr.write ("\n\n");
			fr.write("environment = GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;" + "\n");
			fr.write("arguments = -n transfer -N VDS::transfer:1.0 -i - -R local /usr/local/vds-1.4.7/bin/transfer  -f  base-uri se-mount-point" + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("error = " + jobName + ".err" + "\n");
			fr.write("executable = " + exefile + "\n");

			fr.write("input = " + jobFilePrefix + ".in" + "\n");
			fr.write("log = " + jobFilePrefix + ".log" + "\n");
			fr.write("output = " + jobName + ".out" + "\n");
			
			fr.write ("periodic_release = (NumSystemHolds <= 3)" + "\n");
			fr.write ("periodic_remove = (NumSystemHolds > 3)" + "\n");			
			fr.write("remote_initialdir = " + job.rp_storagePath + "\n");
			
			fr.write("transfer_error = true" + "\n");
			fr.write("transfer_executable = false" + "\n");
			fr.write("transfer_output = true" + "\n");
			fr.write("universe = scheduler" + "\n");
			fr.write("queue" + "\n\n");
			fr.close();	
		} catch (Exception e) {
			System.out.println (e);
		}			
	}
	
	public int getNumberOfJobs () {
		return jobNames.size();
	}
	
	public void createJarTransferToHostInputFile (String outputDir, String remoteJobDir) {
		File dir = new File(outputDir);
		String[] children = dir.list();
		
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.endsWith(".txt");
	        }
	    };
	    children = dir.list(filter);
	    
	    ArrayList arr = new ArrayList ();
	    for (int i = 0; i < children.length; i++) {
	    	arr.add(children[i]);
	    }
	    
	    Collections.sort(arr);
	    
		String line = null;
		BufferedReader fr = null;
		BufferedWriter fw = null;
		try {
			for (int i = 0; i < arr.size(); i++) {
				fw = new BufferedWriter (new FileWriter (outputDir+"/tx_jars_2HOST_"+(i+1)+".in"));
				fr = new BufferedReader (new FileReader (outputDir+"/"+(String)arr.get(i)));
				line = fr.readLine();line = fr.readLine();
				while ((line = fr.readLine()) != null) {
					fw.write("gsiftp://hpc-login1.usc.edu/auto/scec-00/dpmeyers/proj/openSHA/ogale/verificationMap/");
					fw.write(line);
					fw.write("\n");
					fw.write("gsiftp://scecit18.usc.edu/home/dmeyers/proj/ogale/run_3_24_08/"+line);
					fw.write("\n\n");
				}
				fr.close();
				fw.close();
			}
			
			
		} catch (Exception e) {
			System.out.println (e);
		}		
	}
	
	public void createTransferOutputJobFiles (int numberOfJobs) {
		String jobName = "tx_jars_2HOST_";
		String exefile = "/usr/local/vds-1.4.7/bin/kickstart";
		FileWriter fr = null;
		String name = null;
		job.rp_storagePath = "/home/dmeyers/proj/ogale/run_3_24_08";
		try {
			for (int i = 0; i < numberOfJobs; i++) {
				name = jobName+(i+1);
				fr = new FileWriter(outputDir + name+".sub");		
				fr = new FileWriter(outputDir + name+".sub");
				
				fr.write ("\n\n");
				fr.write("environment = GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;" + "\n");
				fr.write("arguments = -n transfer -N VDS::transfer:1.0 -i - -R local /usr/local/vds-1.4.7/bin/transfer  -f  base-uri se-mount-point" + "\n");
				fr.write("copy_to_spool = false" + "\n");
				fr.write("error = " + name + ".err" + "\n");
				fr.write("executable = " + exefile + "\n");
		
				fr.write("input = " + name + ".in" + "\n");
				fr.write("log = " + name + ".log" + "\n");
				fr.write("output = " + name + ".out" + "\n");
				
				fr.write ("periodic_release = (NumSystemHolds <= 3)" + "\n");
				fr.write ("periodic_remove = (NumSystemHolds > 3)" + "\n");			
				fr.write("remote_initialdir = " + job.rp_storagePath + "\n");
				
				fr.write("transfer_error = true" + "\n");
				fr.write("transfer_executable = false" + "\n");
				fr.write("transfer_output = true" + "\n");
				fr.write("universe = scheduler" + "\n");
				fr.write("queue" + "\n\n");
				fr.close();
			}
	} catch (Exception e) {
		System.out.println (e);
	}		
	}
	
	public static void main(String args[]) {
		
		String outputDir = "";
		if (args.length == 0) {
			System.err.println("RUNNING FROM DEBUG MODE!");
			args = new String[1];
			args[0] = "scratchJavaDevelopers/kevin/job_example.xml";
			args[0] = "/home/kevin/OpenSHA/condor/jobs/field_ucerf_nocvm/output.xml";
			outputDir = "/home/kevin/OpenSHA/condor/jobs/";
		}
		
		try {
			String metadata = args[0];
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(metadata));
			Element root = document.getRootElement();
			
			HazardMapJob job = HazardMapJob.fromXMLMetadata(root);
			
			System.out.println("rp_host = " + job.rp_host);
			System.out.println("rp_storagePath = " + job.rp_storagePath);
			System.out.println("rp_javaPath = " + job.rp_javaPath);
			System.out.println("rp_batchScheduler = " + job.rp_batchScheduler);
			System.out.println("repo_host = " + job.repo_host);
			System.out.println("repo_storagePath = " + job.repo_storagePath);
			System.out.println("sitesPerJob = " + job.sitesPerJob);
			System.out.println("useCVM = " + job.useCVM);
			
			outputDir = outputDir + job.jobName + "/";
//			String outputDir = "/home/kevin/OpenSHA/condor/jobs/benchmark_RELM_UCERF_0.1_Purdue/";
			// SDSC
//			String remoteJobDir = "/gpfs/projects/scec/CyberShake2007/opensha/kevin/meetingMap";
			// HPC
//			String remoteJobDir = "/auto/scec-00/kmilner/hazMaps/benchmark_RELM_UCERF_0.1";
			// Dynamic
//			String remoteJobDir = "/nfs/dynamic-1/opensha/kmilner/verification_0.02";
			// ORNL
//			String remoteJobDir = "/tmp/benchmark_RELM_UCERF_0.1";
			
			File outputDirFile = new File(outputDir);
			if (!outputDirFile.exists())
				outputDirFile.mkdir();
			
			// load the region
			Element regionElement = root.element(EvenlyGriddedGeographicRegion.XML_METADATA_NAME);
			EvenlyGriddedGeographicRegion region = EvenlyGriddedGeographicRegion.fromXMLMetadata(regionElement);
			SitesInGriddedRegionAPI sites = new SitesInGriddedRegion(region.getRegionOutline(), region.getGridSpacing());
			
			// see if the ERF needs to be created and saved
			
			if (job.saveERF) {
				Element erfElement = root.element(EqkRupForecast.XML_METADATA_NAME);
				erfElement.detach();
				System.out.println("Creating ERF...");
				EqkRupForecast erf = EqkRupForecast.fromXMLMetadata(erfElement);
				System.out.println("Updating Forecast...");
				erf.updateForecast();
				String erfFileName = job.jobName + "_ERF.obj";
				System.out.println("Saving ERF to " + erfFileName + "...");
				FileUtils.saveObjectInFileThrow(outputDir + erfFileName, erf);
				Element newERFElement = root.addElement(EqkRupForecast.XML_METADATA_NAME);
				newERFElement.addAttribute("fileName", erfFileName);
				System.out.println("Done with ERF");
			}
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileWriter(outputDir + job.metadataFileName), format);
			writer.write(document);
			writer.close();
			
			HazardMapJobCreator creator = new HazardMapJobCreator(outputDir, sites, job);
//			HazardMapJobCreator creator = new HazardMapJobCreator(outputDir, sites, 1700, 2199, job);
			
			try {
				creator.createJobs();
				creator.createSubmitScripts(24);
				
/*				
				creator.createJarTransferToHostInputFile(outputDir, remoteJobDir);
				creator.createDAG (outputDir, creator.getNumberOfJobs());
				creator.createJarTransferJobFile();
				creator.createJarTransferInputFile(outputDir, remoteJobDir);
				creator.createTransferOutputJobFiles (creator.getNumberOfJobs());
				*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (DocumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
