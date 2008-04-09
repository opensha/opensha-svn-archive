package scratchJavaDevelopers.kevin;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

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
	boolean skipCVMFiles = false;
	boolean divertFromSCECToMain = false;
	String willsFileName = "/etc/cvmfiles/usgs_cgs_geology_60s_mod.txt";
	String basinFileName = "/etc/cvmfiles/basindepth_OpenSHA.txt";
	
	ArrayList<String> jobNames = new ArrayList<String>();
	ArrayList<String> cvmNames = new ArrayList<String>();
	
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
	
	public void createJob(int start, int end) throws IOException {
		
		String regionName = addLeadingZeros(start) + "_" + addLeadingZeros(end);
		String jobFilePrefix = "Job_" + regionName;
		String cvmFileName = "";
		if (job.useCVM) {
			cvmFileName = createCVMJobFile(regionName, start, end);
			cvmNames.add(cvmFileName);
		}
		
		String globusscheduler = job.rp_host + "/" + job.rp_batchScheduler;
		
		String globusrsl = job.rp_globusrsl;
//		if (divertFromSCECToMain) {
//			if (job.rp_host.toLowerCase().contains("hpc.usc.edu")) {
//				if (globusrsl.toLowerCase().contains("(queue=scec)")) {
//					if (jobs % 20 == 0) {
//						globusrsl = globusrsl.replace("(queue=scec)", "");
//					}
//				}
//			}
//		}
		
		String jobFileName = jobFilePrefix + ".sub";
		jobNames.add(jobFileName);
		System.out.println("Creating " + jobFileName);
		FileWriter fr = new FileWriter(outputDir + jobFileName);
		fr.write("universe = globus" + "\n");
		fr.write("globusrsl = " + globusrsl + "\n");
		fr.write("globusscheduler = " + globusscheduler + "\n");
		fr.write("should_transfer_files = yes" + "\n");
		fr.write("WhenToTransferOutput = ON_EXIT" + "\n");
		fr.write("executable = " + job.rp_javaPath + "\n");
		fr.write("arguments = -cp " + job.rp_storagePath + "/opensha_gridHazMapGenerator.jar org.opensha.sha.calc.GridMetadataHazardMapCalculator " + start + " " + end + " " + job.metadataFileName + " " + cvmFileName + "\n");
		fr.write("copy_to_spool = false" + "\n");
		fr.write("output = out/" + jobFilePrefix + ".out" + "\n");
		fr.write("error = err/" + jobFilePrefix + ".err" + "\n");
		fr.write("log = log/" + jobFilePrefix + ".log" + "\n");
		fr.write("transfer_executable = false" + "\n");
		fr.write("transfer_error = true" + "\n");
		fr.write("transfer_output = true" + "\n");
		fr.write("notification = never" + "\n");
		fr.write("remote_initialdir = " + job.rp_storagePath + "\n");
		fr.write("queue" + "\n\n");
		
//		fr.write("universe = java" + "\n");
//		fr.write("executable = /opt/jdk1.6.0/jre/bin/java\n");
//		fr.write("jar_files = /usr/rmt_share/scratch96/k/kevinm/benchmark_RELM_UCERF_0.1_3/opensha_gridHazMapGenerator.jar\n");
//		fr.write("should_transfer_files = yes" + "\n");
//		fr.write("WhenToTransferOutput = ON_EXIT" + "\n");
//		fr.write("arguments = org.opensha.sha.calc.GridHardcodedHazardMapCalculator " + i + " " + jobEndIndex + " true " + cvmFileName + "\n");
//		fr.write("requirements = (HasCTSS=?=True) && (CanReachInternet==True) && (JavaVersion==\"1.6.0\")\n");
//		fr.write("copy_to_spool = false" + "\n");
//		fr.write("output = " + jobFilePrefix + ".out" + "\n");
//		fr.write("error = " + jobFilePrefix + ".err" + "\n");
//		fr.write("log = " + jobFilePrefix + ".log" + "\n");
//		fr.write("transfer_executable = false" + "\n");
//		fr.write("transfer_error = true" + "\n");
//		fr.write("transfer_output = true" + "\n");
//		fr.write("periodic_release = (NumSystemHolds <= 3)\n");
//		fr.write("periodic_remove = (NumSystemHolds > 3)\n");
//		fr.write("notification = never" + "\n");
//		fr.write("remote_initialdir = /usr/rmt_share/scratch96/k/kevinm/benchmark_RELM_UCERF_0.1_3/\n");
//		fr.write("queue" + "\n\n");
		
		
		fr.close();
		
		// create the expected files
		int curveJobEndIndex = end-1;
		if (curveJobEndIndex > this.endIndex)
			curveJobEndIndex = this.endIndex;
		fr = new FileWriter(outputDir + jobFilePrefix + ".txt");
		fr.write("# globusscheduler = " + globusscheduler + "\n");
		fr.write("# remote_initialdir = " + job.rp_storagePath + "\n");
		for (int j=start; j<=curveJobEndIndex; j++) {
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
	
	public void createJobs() throws IOException {
		System.out.println("Creating jobs for " + sites.getNumGridLocs() + " sites!");
		
		File outDir = new File(outputDir);
		if (!outDir.exists())
			outDir.mkdir();
		int jobs = 0;
		long start = System.currentTimeMillis();
		for (int i=startIndex; i<=endIndex; i+=job.sitesPerJob) {
			createJob(i, i + job.sitesPerJob);
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
		
		double estimatedMins = (mins / (double)jobs) * (double)sites.getNumGridLocs() / (double)job.sitesPerJob;
		System.out.println("Estimated time (based on current, " + sites.getNumGridLocs() + " curves): " + new DecimalFormat(	"###.##").format(estimatedMins) + " mins");
		estimatedMins = (mins / (double)jobs) * 200000d / (double)job.sitesPerJob;
		System.out.println("Estimated time (based on 200,000 curves): " + new DecimalFormat(	"###.##").format(estimatedMins) + " mins");
	}
	
	public void createRestartJobs(String originalDir) throws IOException {
		
		File outDir = new File(originalDir);
		File dirList[] = outDir.listFiles();
		
		Arrays.sort(dirList, new FileNameComparator());
		
		ArrayList<int[]> badJobs = new ArrayList<int[]>(); 
		
		for (File file : dirList) {
			if (file.getName().endsWith(".sub") && file.getName().startsWith("Job_")) {
				String outFileDir = file.getParentFile().getAbsolutePath() + "/out/";
				String outFileName = file.getName().replace(".sub", ".out");
				String outFilePath = outFileDir + outFileName;
				File outFile = new File(outFilePath);
				boolean good = false;
				System.out.println("Checking " + file.getName());
				if (outFile.exists()) {
					try {
						ArrayList<String> lines = FileUtils.loadFile(outFilePath);
						for (String line : lines) {
							if (line.contains("DONE")) {
								good = true;
								break;
							}
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (!good) {
					StringTokenizer tok = new StringTokenizer(file.getName(), "_.");
					
					// "Job"
					tok.nextToken();
					int start = Integer.parseInt(tok.nextToken());
					int end = Integer.parseInt(tok.nextToken());
					
					System.out.println("Start: " + start + " End: " + end);
					
					int indices[] = {start, end};
					badJobs.add(indices);
				}
			}
		}
		
		for (int[] badJob : badJobs) {
			createJob(badJob[0], badJob[1]);
		}
		
	}
	
	private String createCVMJobFile(String jobName, int startIndex, int endIndex) {
		boolean forCPT = false;
		
		if (job.sitesPerJob < 50000) // in case i forget to change forCPT to false when doing a regular run
			forCPT = false;
		
		String fileName = jobName + ".cvm";
		
		if (skipCVMFiles) // we're skipping creation of the CVM files
			return fileName;
		
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
				wills.setLoadFromJar(true);
				willsSiteClassList=  wills.getWillsSiteClass();
				BasinDepthClass basin = new BasinDepthClass(locs, basinFileName);
				basin.setLoadFromJar(true);
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
				double lat = locs.getLocationAt(i).getLatitude();
				double lon = locs.getLocationAt(i).getLongitude();
				if (forCPT) {
					lat = lat * 1000d;
					lat = (double)Math.rint(lat);
					lat = lat / 1000d;
					lon = lon * 1000d;
					lon = (double)Math.rint(lon);
					lon = lon / 1000d;
				}
				fr.write(lat + "\t");
				fr.write(lon + "\t");
				if (forCPT) {
					int num = 0;
					if (((String)willsSiteClassList.get(i)).equals("E"))
						num = 7;
					else if (((String)willsSiteClassList.get(i)).equals("DE"))
						num = 6;
					else if (((String)willsSiteClassList.get(i)).equals("D"))
						num = 5;
					else if (((String)willsSiteClassList.get(i)).equals("CD"))
						num = 4;
					else if (((String)willsSiteClassList.get(i)).equals("C"))
						num = 3;
					else if (((String)willsSiteClassList.get(i)).equals("BC"))
						num = 2;
					else if (((String)willsSiteClassList.get(i)).equals("B"))
						num = 1;
					fr.write(num + "\t" + basinDepth.get(i) + "\n");
				} else
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
	
	public void createMakeDirJob() {
		try {
			FileWriter fr = new FileWriter(outputDir + "mkdir.sub");
			
			fr.write("universe = globus" + "\n");
			fr.write("executable = /bin/mkdir" + "\n");
			fr.write("arguments = -p " + job.rp_storagePath + "\n");
			fr.write("notification = NEVER" + "\n");
			fr.write("globusrsl = (jobtype=single)" + "\n");
			fr.write("globusscheduler = " + job.rp_host + "/jobmanager-fork" + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("error = mkdir.err" + "\n");
			fr.write("log = mkdir.log" + "\n");
			fr.write("output = mkdir.out" + "\n");
			fr.write("transfer_executable = false" + "\n");
			fr.write("transfer_error = true" + "\n");
			fr.write("transfer_output = true" + "\n");
			fr.write("periodic_release = (NumSystemHolds <= 3)" + "\n");
			fr.write("periodic_remove = (NumSystemHolds > 3)" + "\n");
			fr.write("remote_initialdir = /tmp" + "\n");
			fr.write("queue" + "\n");
			fr.write("" + "\n");
			
			fr.flush();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void createJarTransferInputFile (String outputDir, String remoteJobDir) {
		try {
			FileWriter fr = new FileWriter(outputDir + "/test.in");
			
			fr.write("\n");
			fr.write("gsiftp://"+ job.submitHost + job.submitHostPathToDependencies +"/opensha_gridHazMapGenerator.jar\n");
			fr.write("gsiftp://"+job.rp_host+remoteJobDir+"/"+"opensha_gridHazMapGenerator.jar");
			fr.write("\n");
			fr.write("\n");
			
			fr.write("gsiftp://"+ job.submitHost + job.submitHostPath +"/" +job.jobName +"/" + job.jobName + "_ERF.obj");
			fr.write("\n");
			fr.write("gsiftp://"+job.rp_host + remoteJobDir+"/"+ job.jobName + "_ERF.obj");
			fr.write("\n");
			fr.write("\n");
			fr.write("\n");
		    
		    for (String name : cvmNames) {
		    	fr.write("gsiftp://"+ job.submitHost + job.submitHostPath +"/" +job.jobName +"/"+name);
				fr.write("\n");
				fr.write("gsiftp://");
				fr.write(job.rp_host);
				fr.write(remoteJobDir+"/");
				fr.write(name+"");			
				fr.write("\n");
				fr.write("\n");		    	
		    }
		    
		    fr.write("gsiftp://"+ job.submitHost + job.submitHostPath + "/"+job.jobName +"/" + job.jobName + ".xml");
		    fr.write("\n");
		    fr.write("gsiftp://");
		    fr.write(job.rp_host);
		    fr.write(remoteJobDir+"/");
		    fr.write(job.jobName + ".xml");
		    fr.write("\n");
		    fr.write("\n");		    
			
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
	            return name.startsWith ("Job") && name.endsWith(".sub");
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

			// code for create directory job
            str.append("\n");
            str.append("\n");
            str.append("Job create_dir mkdir.sub");
            str.append("\n");
            str.append("\n");
//            str.append("Job ");
//            str.append("create_dir ");
//            str.append ("HPC_cdir.sub");			
//			str.append("\n");
//			str.append("\n");			
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
			
//			for (int i = 0; i < numberOfJobs; i++) {
//				str.append("Job ");
//				str.append(jobName1).append(i+1).append(" ").append("tx_jars_2HOST_"+(i+1)+".sub");
//				str.append("\n");
//			}
//			str.append("\n");			
			
			// parent child relationship
			
			
			for (int i = numberOfJobs; i > 0 ; i--) {
				str.append("PARENT ");
				str.append("jar_transfer ");
				str.append("CHILD ");
				str.append(jobName).append(i).append("\n");	
				
//				str.append("PARENT ");
//				str.append(jobName).append(i).append(" ");
//				str.append("CHILD ");
//				str.append(jobName1).append(i).append(" ");
//				str.append("\n");				
			}
			str.append("\n");	
//			for (int i = 0; i < numberOfJobs; i++) {
//				str.append("PARENT ");
//				str.append(jobName).append(i+1).append(" ");
//				str.append("CHILD ");
//				str.append(jobName1).append(i+1).append(" ");
//				str.append("\n");	
//			}		
			
			str.append("\n");
            str.append("PARENT ");
            str.append("create_dir ");
            str.append("CHILD ");
            str.append("jar_transfer");
            str.append("\n");			
			
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
					fw.write("gsiftp://"+ job.rp_host +job.rp_storagePath+"/");
					fw.write(line);
					fw.write("\n");
					fw.write("gsiftp://"+ job.repo_host + job.repo_storagePath +"/"+line);
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

		try {
			for (int i = 0; i < numberOfJobs; i++) {
				name = jobName+(i+1);
				fr = new FileWriter(outputDir + name+".sub");		
				
				fr.write ("\n\n");
				fr.write("environment = GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;" + "\n");
				fr.write("arguments = -n transfer -N VDS::transfer:1.0 -i - -R local /usr/local/vds-1.4.7/bin/transfer  -f  base-uri se-mount-point" + "\n");
				fr.write("copy_to_spool = false" + "\n");
				fr.write("error = " + name + ".err" + "\n");
				fr.write("executable = " + exefile + "\n");
				fr.write("globusrsl ="+ job.rp_globusrsl +"\n");
				fr.write ("globusscheduler = "+job.rp_host+"/"+job.rp_batchScheduler+"\n");		
				fr.write("input = " + name + ".in" + "\n");
				fr.write("log = " + name + ".log" + "\n");
				fr.write("output = " + name + ".out" + "\n");
				
				fr.write ("periodic_release = (NumSystemHolds <= 3)" + "\n");
				fr.write ("periodic_remove = (NumSystemHolds > 3)" + "\n");			
				fr.write("remote_initialdir = " + job.repo_storagePath + "\n");
				
				fr.write("transfer_error = true" + "\n");
				fr.write("transfer_executable = false" + "\n");
				fr.write("transfer_output = true" + "\n");
				fr.write("universe = globus" + "\n");
				fr.write("queue" + "\n\n");
				fr.close();
			}
	} catch (Exception e) {
		System.out.println (e);
	}		
	}
	
	public void createUniqueDirOnRemote (String dirName) {
		String jobName = "HPC_cdir";
		try {
			FileWriter fr = new FileWriter(outputDir + jobName+".sub");
			fr.write ("\n\n");
			fr.write("environment = GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;\n");
			fr.write ("arguments = -n dirmanager -N Pegasus::dirmanager:1.0 -R hpc /auto/rcf-104/dpmeyers/proj/pegasus-2.0.0RC1/bin/dirmanager");
			fr.write (" --create --dir "+dirName+"\n");
			fr.write("copy_to_spool = false\n");
			fr.write ("error = "+jobName+".err\n");
			fr.write("executable = /auto/rcf-104/dpmeyers/proj/vds-1.4.7/bin/kickstart\n");
			fr.write("globusrsl = (jobtype=single)\n");
			fr.write ("globusscheduler = "+job.rp_host+"/jobmanager-fork\n");
			fr.write ("log = "+jobName+".log\n");
			fr.write ("output = "+jobName+".out\n");			
			fr.write("periodic_release = (NumSystemHolds <= 3)\n");
			fr.write("periodic_remove = (NumSystemHolds > 3)\n");
			fr.write("remote_initialdir = " + job.rp_storagePath + "\n");
			fr.write("transfer_error = true\n");
			fr.write("transfer_executable = false\n");
			fr.write("transfer_output = true\n");
			fr.write("universe = globus\n");
			fr.write("queue\n");
			fr.close();
		} catch (Exception e) {
			System.out.println (e);
		}		
	}	
	
	/**
	 * Creates condor submit jobs and DAG from the given xml file (first argument).
	 * 
	 * The 2nd optional argument is a boolean that, if true, will not actually create
	 * CVM files but assume that they are already created in the job directory (or will
	 * be moved there after the job creation is completed)
	 * 
	 * The 3rd optional argument is a boolean that, if true, treats this as a
	 * restart of a failed run.It will check to see if any of the jobs failed, and create
	 * submission files in jobName_restart
	 * 
	 * @param args - XML_File.xml [skipCVMfiles] [restart]
	 */
	public static void main(String args[]) {
		
		String outputDir = "";
		if (args.length == 0) {
			System.err.println("RUNNING FROM DEBUG MODE!");
			args = new String[2];
			args[0] = "scratchJavaDevelopers/kevin/job_example.xml";
			args[0] = "output.xml";
			args[1] = "true"; //skip cvm
			outputDir = "/home/kevin/OpenSHA/condor/jobs/";
		}
		
		try {
			String metadata = args[0];
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(metadata));
			Element root = document.getRootElement();
			
			HazardMapJob job = HazardMapJob.fromXMLMetadata(root.element(HazardMapJob.XML_METADATA_NAME));
			
			System.out.println("rp_host = " + job.rp_host);
			System.out.println("rp_storagePath = " + job.rp_storagePath);
			System.out.println("rp_javaPath = " + job.rp_javaPath);
			System.out.println("rp_batchScheduler = " + job.rp_batchScheduler);
			System.out.println("repo_host = " + job.repo_host);
			System.out.println("repo_storagePath = " + job.repo_storagePath);
			System.out.println("sitesPerJob = " + job.sitesPerJob);
			System.out.println("useCVM = " + job.useCVM);
			System.out.println("submitHost = " + job.submitHost);
			System.out.println("submitHostPath = " + job.submitHostPath+"/"+job.jobName);
			System.out.println("submitHostPathToDependencies = " + job.submitHostPathToDependencies);
			
			boolean restart = false;
			boolean skipCVMFiles = false;
			
			if (args.length >= 2) {
				skipCVMFiles = Boolean.parseBoolean(args[1]);
				if (skipCVMFiles)
					System.out.println("Skipping CVM File Creation!");
			}
			
			if (args.length >= 3) {
				restart = Boolean.parseBoolean(args[2]);
				if (restart) {
					System.out.println("Restarting an old run!");
				}
			}
			
			if (outputDir.length() == 0) { // we're not debugging
				outputDir = job.submitHostPath;
			}
			
			if (!outputDir.endsWith("/"))
				outputDir = outputDir + "/";
			
			outputDir = outputDir + job.jobName;
			
			String originalDir = "";
			
			if (restart) {
				originalDir = outputDir + "/";
				outputDir = outputDir + "_RESTART/";
			} else
				outputDir = outputDir + "/";
			
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
			
			creator.skipCVMFiles = skipCVMFiles;
			
			try {
				if (restart)
					creator.createRestartJobs(originalDir);
				else
					creator.createJobs();
				creator.createSubmitScripts(24);
				
				// Mahesh code
				
				String remoteJobDir = job.rp_storagePath;
/*				
				DateFormat myformat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");  
				StringBuffer buf = new StringBuffer ();
				buf.append(remoteJobDir+"/");
				buf.append(myformat.format(new Date()));
				remoteJobDir = new String (buf);
				System.out.println(remoteJobDir);				
*/				
				creator.createMakeDirJob();
				creator.createUniqueDirOnRemote (remoteJobDir);
//				creator.createJarTransferToHostInputFile(outputDir, remoteJobDir);
				creator.createDAG (outputDir, creator.getNumberOfJobs());
				creator.createJarTransferJobFile();
				creator.createJarTransferInputFile(outputDir, remoteJobDir);
//				creator.createTransferOutputJobFiles (creator.getNumberOfJobs());
				// Mahesh code
				
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
