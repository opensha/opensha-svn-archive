package org.opensha.sha.calc.hazardMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.gui.servlets.siteEffect.BasinDepthClass;
import org.opensha.sha.gui.servlets.siteEffect.WillsSiteClass;
import org.opensha.util.FileNameComparator;
import org.opensha.util.FileUtils;
import org.opensha.util.RunScript;


public class HazardMapJobCreator {

	private String outputDir = "";

	public static final String OUTPUT_FILES_DIR_NAME = "outfiles";
	public static final String SUBMIT_FILES_DIR_NAME = "submitfiles";
	public static final String SCRIPT_FILES_DIR_NAME = "scriptfiles";

	public static final String LOG_SCRIPT_DIR_NAME = "log_sh";

	public static final String LOG_FILE_NAME = "log.txt";

	public static SimpleDateFormat LINUX_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

	// status messages
	// DAG creation begins
	public static final String STATUS_CREATE_BEGIN = "Creating DAG";
	// DAG has been created
	public static final String STATUS_CREATE_END = "DAG Created Successfully";
	// DAG submitted
	public static final String STATUS_SUBMIT_DAG = "Submitting DAG";
	// DAG submit success
	public static final String STATUS_SUBMIT_DAG_SUCCESS = "DAG Submitted Successfully!";
	// DAG submit fail
	public static final String STATUS_SUBMIT_DAG_FAIL = "DAG Submission Error";
	// workflow beginning
	public static final String STATUS_WORKFLOW_BEGIN = "Workflow Execution Has Begun: ";
	// transfer input files
	public static final String STATUS_TRANSFER_INPUTS = "Transferring Files To Compute Resource";
	// test job
	public static final String STATUS_TEST_JOB = "Running Quick Test";
	// test job for stage out transfers
	public static final String STATUS_TEST_JOB_REMOTE = "Running Quick Remote Test";
	// curve calculation
	public static final String STATUS_CURVE_CALCULATION_START = "Calculating Curves: ";
	public static final String STATUS_CURVE_CALCULATION_END = "Curve Calculation Done: ";
	public static final String STATUS_CURVE_RETRIEVED = "Curves Retrieved: ";
	// post process
	public static final String STATUS_POST_PROCESS = "Curve Calculation Complete, Post Processing";
	// done
	public static final String STATUS_DONE = "Done";

	private DecimalFormat decimalFormat=new DecimalFormat("0.00##");

	HazardMapJob job;

	SitesInGriddedRegionAPI sites;

	int startIndex;
	int endIndex;

	boolean cvmFromFile = true;
	boolean skipCVMFiles = false;
	boolean divertFromSCECToMain = false;

	boolean stageOut = false;

	String willsFileName = "/etc/cvmfiles/usgs_cgs_geology_60s_mod.txt";
	String basinFileName = "/etc/cvmfiles/basindepth_OpenSHA.txt";

	boolean gravityLink = true;

	ArrayList<String> regionNames = new ArrayList<String>();
	ArrayList<String> cvmNames = new ArrayList<String>();

	public static int nameLength = 7;

	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>(); 

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

	public static String getCurrentDateString() {
		Date date = new Date(System.currentTimeMillis());
		String dateStr = LINUX_DATE_FORMAT.format(date);
		return dateStr;
	}

	public void addProgressListener(ProgressListener listener) {
		progressListeners.add(listener);
	}

	public boolean removeProgressListener(ProgressListener listener) {
		return progressListeners.remove(listener);
	}

	public void removeAllProgressListeners() {
		progressListeners.clear();
	}

	public boolean addAllProgressListeners(Collection<ProgressListener> listeners) {
		return progressListeners.addAll(listeners);
	}

	private void updateProgress(int current, int total) {
		for (ProgressListener listener : progressListeners) {
			listener.updateProgress(current, total);
		}
	}

	private void updateProgressMessage(String message) {
		for (ProgressListener listener : progressListeners) {
			listener.setMessage(message);
		}
	}

	private void setProgressIndeterminate(boolean indeterminate) {
		for (ProgressListener listener : progressListeners) {
			listener.setIndeterminate(indeterminate);
		}
	}


	public void createJob(int start, int end) throws IOException {
		boolean testJob = false;
		if (end < 0) {// this is the initial test job, just do one curve
			testJob = true;
			end = start;
		}

		String regionName = addLeadingZeros(start, nameLength) + "_" + addLeadingZeros(end, nameLength);
		String jobFilePrefix = "Job_" + regionName;
		if (testJob)
			jobFilePrefix = "testJob";
		String cvmFileName = "";
		if (job.useCVM) {
			cvmFileName = createCVMJobFile(regionName, start, end);
			cvmNames.add(cvmFileName);
		}

		String globusscheduler = job.rp.hostName + "/" + job.rp.batchScheduler;

		String globusrsl = "";
		if (testJob) {
			int oldWall = job.rp.globusRSL.getMaxWallTime();
			int wallTime = 15;
			if (wallTime < oldWall) {
				job.rp.globusRSL.setMaxWallTime(wallTime);
				globusrsl = job.rp.globusRSL.getRSLString();
				job.rp.globusRSL.setMaxWallTime(oldWall);
			} else
				globusrsl = job.rp.globusRSL.getRSLString();
		} else {
			globusrsl = job.rp.globusRSL.getRSLString();
		}
//		if (divertFromSCECToMain) {
//		if (job.rp.rp_host.toLowerCase().contains("hpc.usc.edu")) {
//		if (globusrsl.toLowerCase().contains("(queue=scec)")) {
//		if (jobs % 20 == 0) {
//		globusrsl = globusrsl.replace("(queue=scec)", "");
//		}
//		}
//		}
//		}

		String jobFileName = jobFilePrefix + ".sub";
		if (!testJob)
			regionNames.add(regionName);
		System.out.println("Creating " + jobFileName);
		FileWriter fr = new FileWriter(outputDir + jobFileName);

		fr.write("universe = " + job.rp.universe + "\n");
		fr.write("globusrsl = " + globusrsl + "\n");
		fr.write("globusscheduler = " + globusscheduler + "\n");
		String requirements = job.rp.requirements;
		if (requirements.length() > 0)
			fr.write("requirements = " + requirements + "\n");
		fr.write("should_transfer_files = yes" + "\n");
		fr.write("WhenToTransferOutput = ON_EXIT" + "\n");
		fr.write("executable = " + job.rp.javaPath + "\n");
		String endStr;
		if (testJob)
			endStr = "TEST";
		else
			endStr = end + "";
//		fr.write("arguments = -cp " + job.rp.rp_storagePath + "/" + job.jobName + "/opensha_gridHazMapGenerator.jar org.opensha.sha.calc.GridMetadataHazardMapCalculator " + start + " " + endStr + " " + job.metadataFileName + " " + cvmFileName + " " + job.threadsPerJob + " " + "\n");
		fr.write("arguments = -cp " + job.rp.storagePath + "/" + job.jobName + "/opensha_gridHazMapGenerator.jar org.opensha.sha.calc.GridMetadataHazardMapCalculator " + start + " " + endStr + " " + job.metadataFileName + " " + cvmFileName + "\n");
		fr.write("copy_to_spool = false" + "\n");
		if (testJob) {
			fr.write("output = " + jobFilePrefix + ".out" + "\n");
			fr.write("error = " + jobFilePrefix + ".err" + "\n");
			fr.write("log = " + jobFilePrefix + ".log" + "\n");
		} else {
			fr.write("output = out/" + jobFilePrefix + ".out" + "\n");
			fr.write("error = err/" + jobFilePrefix + ".err" + "\n");
			fr.write("log = log/" + jobFilePrefix + ".log" + "\n");
		}
		fr.write("transfer_executable = false" + "\n");
		fr.write("transfer_error = true" + "\n");
		fr.write("transfer_output = true" + "\n");
		fr.write("notification = never" + "\n");
		fr.write("remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");
		fr.write("queue" + "\n\n");

		fr.close();

//		// create the expected files
//		if (!testJob) {
//		int curveJobEndIndex = end-1;
//		if (curveJobEndIndex > this.endIndex)
//		curveJobEndIndex = this.endIndex;
//		fr = new FileWriter(outputDir + jobFilePrefix + ".txt");
//		fr.write("# globusscheduler = " + globusscheduler + "\n");
//		fr.write("# remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");
//		for (int j=start; j<=curveJobEndIndex; j++) {
//		try {
//		Location loc = sites.getSite(j).getLocation();
//		String lat = decimalFormat.format(loc.getLatitude());
//		String lon = decimalFormat.format(loc.getLongitude());
//		String jobDir = lat + "/";
//		fr.write(jobDir + lat + "_" + lon + ".txt\n");
//		} catch (RegionConstraintException e) {
//		e.printStackTrace();
//		}
//		}
//		fr.close();
//		}

		// create the expected files
		if (!testJob && stageOut) {
			int curveJobEndIndex = end-1;
			if (curveJobEndIndex > this.endIndex)
				curveJobEndIndex = this.endIndex;
			ArrayList<String> outfiles = new ArrayList<String>();
			for (int j=start; j<=curveJobEndIndex; j++) {
				try {
					Location loc = sites.getSite(j).getLocation();
					String lat = decimalFormat.format(loc.getLatitude());
					String lon = decimalFormat.format(loc.getLongitude());
					String jobDir = "curves/" + lat + "/";
					String name = jobDir + lat + "_" + lon + ".txt";
					outfiles.add(name);
				} catch (RegionConstraintException e) {
					e.printStackTrace();
				}
			}
			String tarFileName = this.createStageOutZipJobFile(regionName, outfiles);
			this.createStageOutPostJobFiles(regionName, tarFileName);
		}
	}

	public void createJobs(boolean stageOut) throws IOException {
		System.out.println("Creating jobs for " + sites.getNumGridLocs() + " sites!");

		File outDir = new File(outputDir);
		if (!outDir.exists())
			outDir.mkdir();
		int jobs = 0;
		long start = System.currentTimeMillis();

		// find out how many we're making
		int expectedJobs = 0;
		for (int i=startIndex; i<=endIndex; i+=job.sitesPerJob) {
			expectedJobs++;
		}

		int updateInterval = 1;
		if (expectedJobs > 100) {
			updateInterval = expectedJobs / 100;
		}

		this.setProgressIndeterminate(false);

		for (int i=startIndex; i<=endIndex; i+=job.sitesPerJob) {
			if (jobs % updateInterval == 0)
				this.updateProgress(jobs, expectedJobs);
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

	public void createTestJob() throws IOException {
		this.createJob(this.startIndex, -1);
	}

	public void createRestartJobs(String originalDir, boolean stageOut) throws IOException {

		this.setProgressIndeterminate(true);
		this.updateProgressMessage("Finding jobs to be restarted");

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

		int cur = 0;
		int num = badJobs.size();
		this.setProgressIndeterminate(false);
		this.updateProgressMessage("Creating Restart Jobs");
		for (int[] badJob : badJobs) {
			this.updateProgress(cur++, num);
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
		while (i < regionNames.size()) {
			if (i % jobsPerScript == 0) {
				if (fr != null)
					fr.close();
				String fileName = outputDir + "submit_" + addLeadingZeros(scripts, nameLength) + ".sh";
				System.out.println("Creating " + fileName);
				fr = new FileWriter(fileName);
				scripts++;
			}
			fr.write("condor_submit Job_" + regionNames.get(i) + ".sub" + "\n");
			all.write("condor_submit Job_" + regionNames.get(i) + ".sub" + "\n");
			fr.write("sleep 2\n");
			all.write("sleep 1\n");
			i++;
		}
		if (fr != null)
			fr.close();
		all.close();
	}

	public static String addLeadingZeros(int num, int length) {
		String str = num + "";
		if (str.length() > length)
			return str;

		while (str.length() < length)
			str = "0" + str;

		return str;
	}

	public void createMakeDirJob() throws IOException {
		FileWriter fr = new FileWriter(outputDir + "mkdir.sub");

		String mainDir = job.rp.storagePath + "/" + job.jobName;
		String tarDir = mainDir + "/tgz";
		String tarInDir = mainDir + "/tgz_in";

		fr.write("universe = globus" + "\n");
		fr.write("executable = /bin/mkdir" + "\n");
		fr.write("arguments = -v -p " + mainDir + " " + tarDir + " " + tarInDir + "\n");
		fr.write("notification = NEVER" + "\n");
		fr.write("globusrsl = (jobtype=single)" + "\n");
		fr.write("globusscheduler = " + job.rp.hostName + "/" + job.rp.forkScheduler + "\n");
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
	}

	public void createStorageMakeDirJob() throws IOException {
		FileWriter fr = new FileWriter(outputDir + "mkdirStorage.sub");

		String mainDir = job.storageHost.path + "/" + job.jobName;
		String tgzDir = mainDir + "/tgz";

		fr.write("universe = globus" + "\n");
		fr.write("executable = /bin/mkdir" + "\n");
		fr.write("arguments = -v -p " + mainDir + " " + tgzDir + "\n");
		fr.write("notification = NEVER" + "\n");
		fr.write("globusrsl = (jobtype=single)" + "\n");
		fr.write("globusscheduler = " + job.storageHost.schedulerHostName + "/" + job.storageHost.forkScheduler + "\n");
		fr.write("copy_to_spool = false" + "\n");
		fr.write("error = mkdirStorage.err" + "\n");
		fr.write("log = mkdirStorage.log" + "\n");
		fr.write("output = mkdirStorage.out" + "\n");
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
	}

	public void createCHModJob() throws IOException {
		FileWriter fw = new FileWriter(outputDir + "chmod.sh");
		fw.write("/bin/chmod +r * \n");
		fw.write("/bin/chmod -R +rx curves \n");
		fw.flush();
		fw.close();

		FileWriter fr = new FileWriter(outputDir + "chmod.sub");

		fr.write("universe = globus" + "\n");
		fr.write("executable = /bin/sh" + "\n");
		if (stageOut)
			fr.write("arguments = " + job.storageHost.path + "/" + job.jobName + "/chmod.sh" + "\n");
		else
			fr.write("arguments = " + job.rp.storagePath + "/" + job.jobName + "/chmod.sh" + "\n");
		fr.write("notification = NEVER" + "\n");
		fr.write("globusrsl = (jobtype=single)" + "\n");
		if (stageOut)
			fr.write("globusscheduler = " + job.storageHost.schedulerHostName + "/" + job.storageHost.forkScheduler + "\n");
		else
			fr.write("globusscheduler = " + job.rp.hostName + "/" + job.rp.forkScheduler + "\n");
		fr.write("copy_to_spool = false" + "\n");
		fr.write("error = chmod.err" + "\n");
		fr.write("log = chmod.log" + "\n");
		fr.write("output = chmod.out" + "\n");
		fr.write("transfer_executable = false" + "\n");
		fr.write("transfer_error = true" + "\n");
		fr.write("transfer_output = true" + "\n");
		fr.write("periodic_release = (NumSystemHolds <= 3)" + "\n");
		fr.write("periodic_remove = (NumSystemHolds > 3)" + "\n");
		if (stageOut)
			fr.write("remote_initialdir = " + job.storageHost.path + "/" + job.jobName + "\n");
		else
			fr.write("remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");
		fr.write("queue" + "\n");
		fr.write("" + "\n");

		fr.flush();
		fr.close();
	}

	public void createCopyLinkJob() throws IOException {
		FileWriter fr = new FileWriter(outputDir + "copy_link.sub");

		fr.write("universe = globus" + "\n");
		fr.write("executable = /opt/install/apache-tomcat-5.5.20/webapps/OpenSHA/HazardMapXMLDatasets/copy_link.pl" + "\n");
		fr.write("arguments = " + job.jobName + "\n");
		fr.write("notification = NEVER" + "\n");
		fr.write("globusrsl = (jobtype=single)" + "\n");
		fr.write("globusscheduler = gravity.usc.edu/jobmanager-fork" + "\n");
		fr.write("copy_to_spool = false" + "\n");
		fr.write("error = copy_link.err" + "\n");
		fr.write("log = copy_link.log" + "\n");
		fr.write("output = copy_link.out" + "\n");
		fr.write("transfer_executable = false" + "\n");
		fr.write("transfer_error = true" + "\n");
		fr.write("transfer_output = true" + "\n");
		fr.write("periodic_release = (NumSystemHolds <= 3)" + "\n");
		fr.write("periodic_remove = (NumSystemHolds > 3)" + "\n");
		fr.write("remote_initialdir = " + "/opt/install/apache-tomcat-5.5.20/webapps/OpenSHA/HazardMapXMLDatasets/" + "\n");
		fr.write("queue" + "\n");
		fr.write("" + "\n");

		fr.flush();
		fr.close();
	}

	public void createPrePostJob(boolean pre) throws IOException {
		String jobName = "";
		if (pre)
			jobName = "pre";
		else
			jobName = "post";
		FileWriter fr = new FileWriter(outputDir + jobName + ".sub");

		fr.write("universe = globus" + "\n");

		String prePostStr = "";
		if (pre)
			prePostStr = HazardMapPrePostProcessor.PRE_PROCESS;
		else
			prePostStr = HazardMapPrePostProcessor.POST_PROCESS;

		if (stageOut) {
			fr.write("executable = " + job.storageHost.javaPath + "\n");
			fr.write("arguments = -cp " + job.storageHost.jarPath + " org.opensha.sha.calc.hazardMap.HazardMapPrePostProcessor " + job.metadataFileName + " " + prePostStr + "\n");
		} else {
			fr.write("executable = " + job.rp.javaPath + "\n");
			fr.write("arguments = -cp " + job.rp.storagePath + "/" + job.jobName + "/opensha_gridHazMapGenerator.jar org.opensha.sha.calc.hazardMap.HazardMapPrePostProcessor " + job.metadataFileName + " " + prePostStr + "\n");
		}

		fr.write("notification = NEVER" + "\n");
		fr.write("globusrsl = (jobtype=single)" + "\n");
		if (stageOut)
			fr.write("globusscheduler = " + job.storageHost.schedulerHostName + "/" + job.storageHost.forkScheduler + "\n");
		else
			fr.write("globusscheduler = " + job.rp.hostName + "/" + job.rp.forkScheduler + "\n");
		fr.write("copy_to_spool = false" + "\n");
		fr.write("error = " + jobName + ".err" + "\n");
		fr.write("log = " + jobName + ".log" + "\n");
		fr.write("output = " + jobName + ".out" + "\n");
		fr.write("transfer_executable = false" + "\n");
		fr.write("transfer_error = true" + "\n");
		fr.write("transfer_output = true" + "\n");
		fr.write("periodic_release = (NumSystemHolds <= 3)" + "\n");
		fr.write("periodic_remove = (NumSystemHolds > 3)" + "\n");
		if (stageOut)
			fr.write("remote_initialdir = " + job.storageHost.path + "/" + job.jobName + "\n");
		else
			fr.write("remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");
		fr.write("queue" + "\n");
		fr.write("" + "\n");

		fr.flush();
		fr.close();
	}

	public void createJarTransferInputFile (String outputDir, String remoteJobDir) {

		String gridFTPPath = job.rp.gridFTPHost + job.rp.storagePath + "/" + job.jobName;

		try {
			FileWriter fr = new FileWriter(outputDir + "/test.in");

			fr.write("\n");
			fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.dependencyPath +"/opensha_gridHazMapGenerator.jar\n");
			fr.write("gsiftp://"+gridFTPPath+"/"+"opensha_gridHazMapGenerator.jar");
			fr.write("\n");
			fr.write("\n");

			if (job.saveERF) {
				fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.path +"/" +job.jobName +"/" + job.jobName + "_ERF.obj");
				fr.write("\n");
				fr.write("gsiftp://"+gridFTPPath+"/"+ job.jobName + "_ERF.obj");
				fr.write("\n");
				fr.write("\n");
				fr.write("\n");
			}

			for (String name : cvmNames) {
				fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.path +"/" +job.jobName +"/"+name);
				fr.write("\n");
				fr.write("gsiftp://");
				fr.write(gridFTPPath+"/");
				fr.write(name+"");			
				fr.write("\n");
				fr.write("\n");		    	
			}

			if (stageOut) {
				for (String name : tarInFiles) {
					fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.path +"/" +job.jobName +"/"+name);
					fr.write("\n");
					fr.write("gsiftp://");
					fr.write(gridFTPPath+"/");
					fr.write(name+"");			
					fr.write("\n");
					fr.write("\n");	
				}
				fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.path + "/"+job.jobName +"/" + job.metadataFileName);
				fr.write("\n");
				fr.write("gsiftp://");
				fr.write(job.storageHost.gridFTPHostName + job.storageHost.path + "/" + job.jobName +"/");
				fr.write(job.metadataFileName);
				fr.write("\n");
				fr.write("\n");
			}

			fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.path + "/"+job.jobName +"/" + job.metadataFileName);
			fr.write("\n");
			fr.write("gsiftp://");
			fr.write(gridFTPPath+"/");
			fr.write(job.metadataFileName);
			fr.write("\n");
			fr.write("\n");

			fr.write("gsiftp://"+ job.submitHost.hostName + job.submitHost.path + "/"+job.jobName +"/chmod.sh");
			fr.write("\n");
			fr.write("gsiftp://");
			if (stageOut)
				fr.write(job.storageHost.gridFTPHostName + job.storageHost.path + "/" + job.jobName +"/");
			else
				fr.write(gridFTPPath+"/");
			fr.write("chmod.sh");
			fr.write("\n");
			fr.write("\n");

			fr.close();
		} catch (Exception e) {
			System.out.println (e);
		}		
	}

	public void createDAG (String outputDir, int numberOfJobs) {

		boolean onHPC = job.rp.hostName.toLowerCase().contains("hpc.usc.edu") || 
		(stageOut && (job.storageHost.schedulerHostName.toLowerCase().contains("hpc.usc.edu")
				|| job.storageHost.gridFTPHostName.toLowerCase().contains("hpc.usc.edu")));

		String jobName = "curves_";
		String tarJobName = "tar_";
		String untarJobName = "untar_";

		try {
			BufferedOutputStream fos = new BufferedOutputStream (new FileOutputStream(outputDir+"/main.dag"));
			StringBuffer str = new StringBuffer ();

			// code for create directory job
			str.append("# Hazard Map DAG\n");
			str.append("# This is a DAG to create an OpenSHA Hazard Map\n");
			str.append("# It was generated by scratchJavaDevelopers.kevin.HazardMapJobCreator\n");
			str.append("\n");
			str.append("\n");
			str.append("# This Job creates the direcory on the compute resource where all input files\n");
			str.append("# and curves will be stored\n");
			str.append("Job create_dir mkdir.sub\n");
			str.append("Script PRE create_dir " + createLogShellScript("mkdir", STATUS_WORKFLOW_BEGIN + this.sites.getNumGridLocs()));
			str.append("\n");
			str.append("\n");
			if (stageOut) {
				str.append("# This Job creates the direcory on the compute resource where all input files\n");
				str.append("# and curves will be stored\n");
				str.append("Job create_storage_dir mkdirStorage.sub");
				str.append("\n");
				str.append("\n");
			}
//			str.append("Job ");
//			str.append("create_dir ");
//			str.append ("HPC_cdir.sub");			
//			str.append("\n");
//			str.append("\n");			
			// main job
			str.append("# This job transfers the ERF obj file (if necessary), the jar file,\n");
			str.append("# and all CVM files (if necessary)\n");
			str.append("Job ");
			str.append("transfer_input_files ");
			str.append ("transfer_input_files.sub\n");
			str.append("Script PRE transfer_input_files " + createLogShellScript("transfer_input_files", STATUS_TRANSFER_INPUTS));
			str.append("\n");
			str.append("\n");
			str.append("# This is a simple test job which just computes one curve before all of the regular\n");
			str.append("# compute jobs are submitted\n");
			str.append("Job ");
			str.append("test ");
			str.append ("testJob.sub\n");
			String testMessage;
			if (stageOut)
				testMessage = STATUS_TEST_JOB_REMOTE;
			else
				testMessage = STATUS_TEST_JOB;
			str.append("Script PRE test " + createLogShellScript("test", testMessage));
			str.append("\n");
			str.append("\n");
			str.append("# This job chmod's all of the curves to become globally readable after computation\n");
			str.append("Job ");
			str.append("chmod ");
			str.append ("chmod.sub\n");
			str.append("Script PRE chmod " + createLogShellScript("post", STATUS_POST_PROCESS));
			str.append("\n");
			str.append("\n");
			if (onHPC || stageOut) {
				str.append("# This is the post processing job that sends an e-mail to the submitter at the end\n");
				str.append("# of the computation\n");
				str.append("Job ");
				str.append("postProcess ");
				str.append ("post.sub\n");
				str.append("Script POST postProcess " + createLogShellScript("done", STATUS_DONE));
				str.append("\n");
				str.append("\n");
				str.append("# This is the pre processing job that sends an e-mail to the submitter at the beginning\n");
				str.append("# of the computation\n");
				str.append("Job ");
				str.append("preProcess ");
				str.append ("pre.sub");
				str.append("\n");
				str.append("\n");
			}

			if (onHPC && gravityLink) {
				str.append("# This job is run on gravity.usc.edu and sets up links so that maps can be plotted\n");
				str.append("# directly from HPC (over samba)\n");
				str.append("Job ");
				str.append("copy_link ");
				str.append ("copy_link.sub");
				str.append("\n");
				str.append("\n");
			}
			// child jobs

			str.append("# These are the actual hazard curve calculation jobs\n");
			str.append("# and any stage out jobs if needed\n");
			int num = 1;
			String curvePreScript = createLogShellScript("curve_pre", STATUS_CURVE_CALCULATION_START + this.job.sitesPerJob);
			String curvePostScript = createLogShellScript("curve_post", STATUS_CURVE_CALCULATION_END + this.job.sitesPerJob);
			String curveStageOutPostScript = null;
			if (stageOut)
				curveStageOutPostScript = createLogShellScript("curve_stage", STATUS_CURVE_RETRIEVED + this.job.sitesPerJob);
			for (String region : regionNames) {
				str.append("Job " + jobName + num + " " + "Job_" + region + ".sub" + "\n");
				str.append("Script PRE " + jobName + num + " " + curvePreScript + "\n");
				str.append("Script POST " + jobName + num + " " + curvePostScript + "\n");

				if (stageOut) {
					str.append("Job " + tarJobName + num + " " + job.submitHost.path + "/" + job.jobName + "/" + tgzSubDir + "/Tar_" + region + ".sub" + "\n");
					str.append("Script POST " + tarJobName + num + " " + job.submitHost.path + "/" + job.jobName + "/" + stageOutScriptDir + "/StageOut_" + region + ".sh" + "\n");
					str.append("Job " + untarJobName + num + " " + job.submitHost.path + "/" + job.jobName + "/" + tgzUnzipDir + "/UnTar_" + region + ".sub" + "\n");
					str.append("Script POST " + tarJobName + num + " " + curveStageOutPostScript + "\n");
				}
				num++;
			}
			str.append("\n");

//			for (int i = 0; i < numberOfJobs; i++) {
//			str.append("Job ");
//			str.append(jobName1).append(i+1).append(" ").append("tx_jars_2HOST_"+(i+1)+".sub");
//			str.append("\n");
//			}
//			str.append("\n");			

			// parent child relationship

			str.append("# These are the parent/child relationships that make all hazard curve jobs execute in\n");
			str.append("# parallel after the test job executes to completion without error\n");
			for (int i = numberOfJobs; i > 0 ; i--) {
				str.append("PARENT ");
				str.append("test ");
				str.append("CHILD ");
				str.append(jobName).append(i).append("\n");	

//				str.append("PARENT ");
//				str.append(jobName).append(i).append(" ");
//				str.append("CHILD ");
//				str.append(jobName1).append(i).append(" ");
//				str.append("\n");				
			}
//			str.append("\n");	
//			for (int i = 0; i < numberOfJobs; i++) {
//			str.append("PARENT ");
//			str.append(jobName).append(i+1).append(" ");
//			str.append("CHILD ");
//			str.append(jobName1).append(i+1).append(" ");
//			str.append("\n");	
//			}		

			str.append("\n");
			str.append("# This states that the transfers should only happen after the directory is created\n");
			str.append("PARENT ");
			str.append("create_dir ");
			str.append("CHILD ");
			str.append("transfer_input_files");
			str.append("\n");

			if (stageOut) {
				str.append("\n");
				str.append("# This makes sure that the storage dir is created\n");
				str.append("PARENT ");
				str.append("create_storage_dir ");
				str.append("CHILD ");
				str.append("transfer_input_files");
				str.append("\n");
			}

			if (onHPC || stageOut) {
				str.append("\n");
				str.append("# This sends the pre processing e-mail\n");
				str.append("PARENT ");
				str.append("transfer_input_files ");
				str.append("CHILD ");
				str.append("preProcess");
				str.append("\n");
			}

			str.append("\n");
			str.append("# This states that the test job should happen once everything has been transfered\n");
			str.append("PARENT ");
			str.append("transfer_input_files ");
			str.append("CHILD ");
			str.append("test");
			str.append("\n");

			str.append("\n");
			str.append("# These are the parent/child relationships that make the chmod job run only once all\n");
			str.append("# hazard curve jobs have completed and data stage out (if applicable) has completed\n");

			if (stageOut) {
				for (int i = numberOfJobs; i > 0 ; i--) {
					str.append("PARENT " + jobName + i);
					str.append(" CHILD ");
					str.append(tarJobName + i + "\n");

					str.append("PARENT " + tarJobName + i);
					str.append(" CHILD ");
					str.append(untarJobName + i + "\n");

					str.append("PARENT " + untarJobName + i);
					str.append(" CHILD ");
					str.append("chmod\n");
				}
			} else {
				for (int i = numberOfJobs; i > 0 ; i--) {
					str.append("PARENT " + jobName + i);
					str.append(" CHILD ");
					str.append("chmod\n");
				}
			}

			if (onHPC && gravityLink) { // set up gravity for automatic plotting
				str.append("\n");
				str.append("# chmod job should be run before the files are linked to from gravity\n");
				str.append("PARENT ");
				str.append("chmod ");
				str.append("CHILD ");
				str.append("copy_link");
				str.append("\n");

				str.append("\n");
				str.append("# Once everything is done, run the post process job to e-mail the user\n");
				str.append("PARENT ");
				str.append("copy_link ");
				str.append("CHILD ");
				str.append("postProcess");
				str.append("\n");
			}

			if (onHPC || stageOut) {
				str.append("\n");
				str.append("# Once everything is done, run the post process job to e-mail the user\n");
				str.append("PARENT ");
				str.append("chmod ");
				str.append("CHILD ");
				str.append("postProcess");
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
		String jobName = "transfer_input_files";

		try {
			FileWriter fr = new FileWriter(outputDir + jobName+".sub");
			fr.write ("\n\n");
			fr.write("environment = " + job.submitHost.transferEnvironment + "\n");
			fr.write("arguments = " + job.submitHost.transferArguments + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("error = " + jobName + ".err" + "\n");
			fr.write("executable = " + job.submitHost.transferExecutable + "\n");

			fr.write("input = " + jobFilePrefix + ".in" + "\n");
			fr.write("log = " + jobFilePrefix + ".log" + "\n");
			fr.write("output = " + jobName + ".out" + "\n");

			fr.write ("periodic_release = (NumSystemHolds <= 3)" + "\n");
			fr.write ("periodic_remove = (NumSystemHolds > 3)" + "\n");			
			fr.write("remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");

			fr.write("notification = NEVER" + "\n");
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
		return regionNames.size();
	}

	String tgzInDir = "tgz_in";
	String tgzSubDir = "tgz_sub";
	String stageOutScriptDir = "stage_out_sh";
	String tgzUnzipDir = "tgz_un";

	ArrayList<String> tarInFiles = new ArrayList<String>();

	boolean firstStageOut = true;

	public String createStageOutZipJobFile(String regionName, ArrayList<String> outFiles) {

		String tarFileName = "tgz/" + regionName + ".tgz";

		String tarInputName = tgzInDir + "/Tar_" + regionName + ".in";
		String tarScriptName = tgzSubDir + "/Tar_" + regionName + ".sub";

		File tgz_in = new File(outputDir + "/" + tgzInDir);

		if (!tgz_in.exists()) {
			tgz_in.mkdir();

			File tgz_sub = new File(outputDir + "/" + tgzSubDir);
			tgz_sub.mkdir();

			File tgz_sub_out = new File(outputDir + "/" + tgzSubDir + "/out");
			tgz_sub_out.mkdir();

			File tgz_sub_log = new File(outputDir + "/" + tgzSubDir + "/log");
			tgz_sub_log.mkdir();

			File tgz_sub_err = new File(outputDir + "/" + tgzSubDir + "/err");
			tgz_sub_err.mkdir();
		}

		try {
			FileWriter fw = new FileWriter(outputDir + "/" + tarInputName);
			for (String file : outFiles)
				fw.write(file + "\n");
			fw.close();

			FileWriter fr = new FileWriter(outputDir + "/" + tarScriptName);


			fr.write("universe = globus" + "\n");
			fr.write("executable = /bin/tar" + "\n");
			fr.write("arguments = " + "-czv --files-from " + tarInputName + " --file " + tarFileName + "\n");
			fr.write("notification = NEVER" + "\n");
			fr.write("globusrsl = (jobtype=single)" + "\n");
			fr.write("globusscheduler = " + job.rp.hostName + "/" + job.rp.forkScheduler + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("error = " + outputDir + "/" + tgzSubDir + "/err/" + regionName + ".err" + "\n");
			fr.write("log = " + outputDir + "/" + tgzSubDir + "/log/" + regionName + ".log" + "\n");
			fr.write("output = " + outputDir + "/" + tgzSubDir + "/out/" + regionName + ".out" + "\n");
			fr.write("transfer_executable = false" + "\n");
			fr.write("transfer_error = true" + "\n");
			fr.write("transfer_output = true" + "\n");
			fr.write("periodic_release = (NumSystemHolds <= 3)" + "\n");
			fr.write("periodic_remove = (NumSystemHolds > 3)" + "\n");
			fr.write("remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");
			fr.write("queue" + "\n");
			fr.write("" + "\n");

			fr.flush();
			fr.close();

			tarInFiles.add(tarInputName);

			// now make the submit file
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tarFileName;
	}

	public void createStageOutPostJobFiles(String regionName, String tarFileName) {

		File stageOutDir = new File(outputDir + "/" + stageOutScriptDir);

		if (!stageOutDir.exists()) {
			stageOutDir.mkdir();

			File tgz_sub = new File(outputDir + "/" + tgzUnzipDir);
			tgz_sub.mkdir();

			File tgz_sub_out = new File(outputDir + "/" + tgzUnzipDir + "/out");
			tgz_sub_out.mkdir();

			File tgz_sub_log = new File(outputDir + "/" + tgzUnzipDir + "/log");
			tgz_sub_log.mkdir();

			File tgz_sub_err = new File(outputDir + "/" + tgzUnzipDir + "/err");
			tgz_sub_err.mkdir();
		}

		// make the transfer script file

		String stageOutFile = outputDir + "/" + stageOutScriptDir + "/StageOut_" + regionName + ".sh";

		String source = job.rp.gridFTPHost + job.rp.storagePath + "/" + job.jobName + "/" + tarFileName;
		String destFilePath = job.storageHost.path + "/" + job.jobName + "/" + tarFileName;
		String dest = job.storageHost.gridFTPHostName + destFilePath;

		int streams = 1;

		try {
			FileWriter fw = new FileWriter(stageOutFile);

			fw.write("#!/bin/sh" + "\n");
			fw.write("" + "\n");
			fw.write("globus-url-copy -vb -tcp-bs 2097152 -p " + streams + " gsiftp://" + source + " gsiftp://" + dest + "\n");

			if (firstStageOut) {
				source = job.rp.gridFTPHost + job.rp.storagePath + "/" + job.jobName + "/" + job.metadataFileName;
				dest = job.storageHost.gridFTPHostName + job.storageHost.path + "/" + job.jobName + "/" + job.metadataFileName;
				fw.write("globus-url-copy -vb -tcp-bs 2097152 -p " + streams + " gsiftp://" + source + " gsiftp://" + dest + "\n");
				firstStageOut = false;
			}

			fw.close();

			// now make the untar job

			String unTarScriptName = tgzUnzipDir + "/UnTar_" + regionName + ".sub";

			FileWriter fr = new FileWriter(outputDir + "/" + unTarScriptName);


			fr.write("universe = globus" + "\n");
			fr.write("executable = /bin/tar" + "\n");
			fr.write("arguments = -xzvf " + destFilePath + "\n");
			fr.write("notification = NEVER" + "\n");
			fr.write("globusrsl = (jobtype=single)" + "\n");
			fr.write("globusscheduler = " + job.storageHost.schedulerHostName + "/" + job.storageHost.batchScheduler + "\n");
			fr.write("copy_to_spool = false" + "\n");
			fr.write("error = " + outputDir + "/" + tgzUnzipDir + "/err/" + regionName + ".err" + "\n");
			fr.write("log = " + outputDir + "/" + tgzUnzipDir + "/log/" + regionName + ".log" + "\n");
			fr.write("output = " + outputDir + "/" + tgzUnzipDir + "/out/" + regionName + ".out" + "\n");
			fr.write("transfer_executable = false" + "\n");
			fr.write("transfer_error = true" + "\n");
			fr.write("transfer_output = true" + "\n");
			fr.write("periodic_release = (NumSystemHolds <= 3)" + "\n");
			fr.write("periodic_remove = (NumSystemHolds > 3)" + "\n");
			fr.write("remote_initialdir = " + job.storageHost.path + "/" + job.jobName + "\n");
			fr.write("queue" + "\n");
			fr.write("" + "\n");

			fr.flush();
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setStageOut(boolean stageOut) {
		this.stageOut = stageOut;
	}

	// creates a shell script that will submit the DAG
	public void createSubmitDAGScript(boolean run) {
		try {
			String scriptFileName = job.submitHost.path + "/" + job.jobName + "/submit_DAG.sh";
			FileWriter fw = new FileWriter(scriptFileName);
			fw.write("#!/bin/csh\n");
			fw.write("cd "+outputDir+"\n");
			if (stageOut) {
				fw.write("/bin/chmod -R +x " + outputDir + "/" + stageOutScriptDir + "\n");
			}
			fw.write("/bin/chmod +x " + outputDir + LOG_SCRIPT_DIR_NAME + "/" + "*.sh" + "\n");
			fw.write("\n");
			fw.write("condor_submit_dag main.dag" + "\n");
			fw.close();
			if (run) {
				this.logMessage(STATUS_SUBMIT_DAG);
				int retVal = RunScript.runScript(new String[]{"sh", "-c", "sh "+scriptFileName});
				if (retVal == 0)
					this.logMessage(STATUS_SUBMIT_DAG_SUCCESS);
				else
					this.logMessage(STATUS_SUBMIT_DAG_FAIL + ": " + retVal);
				System.out.println("Command executed with status " + retVal);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void logStart() {
		try {
			this.logMessage(STATUS_CREATE_BEGIN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void logCompletion() {
		try {
			this.logMessage(STATUS_CREATE_END);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void logMessage(String message) throws IOException {
		String logFileName = this.outputDir + LOG_FILE_NAME;
		File logFile = new File(logFileName);
		if (!logFile.exists())
			logFile.createNewFile();
		FileOutputStream stream = new FileOutputStream(logFile, true);
		message = "[" + getCurrentDateString() + "] " + message + "\n";
		stream.write(message.getBytes());
		stream.flush();
		stream.close();
//		File logFile = new File
	}

	private String createLogShellScript(String dagStage, String message) {
		String fileName = this.outputDir + LOG_SCRIPT_DIR_NAME + File.separator + "log_" + dagStage + ".sh";
		try {
			FileWriter fw = new FileWriter(fileName);

			fw.write("#!/bin/sh" + "\n");
			fw.write("" + "\n");
			fw.write("echo [`date`] " + message + " >> " + this.outputDir + LOG_FILE_NAME + "\n");

			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileName;
	}

//	public void createJarTransferToHostInputFile (String outputDir, String remoteJobDir) {
//	File dir = new File(outputDir);
//	String[] children = dir.list();

//	FilenameFilter filter = new FilenameFilter() {
//	public boolean accept(File dir, String name) {
//	return name.endsWith(".txt");
//	}
//	};
//	children = dir.list(filter);

//	ArrayList arr = new ArrayList ();
//	for (int i = 0; i < children.length; i++) {
//	arr.add(children[i]);
//	}

//	Collections.sort(arr);

//	String line = null;
//	BufferedReader fr = null;
//	BufferedWriter fw = null;
//	try {
//	for (int i = 0; i < arr.size(); i++) {
//	fw = new BufferedWriter (new FileWriter (outputDir+"/tx_jars_2HOST_"+(i+1)+".in"));
//	fr = new BufferedReader (new FileReader (outputDir+"/"+(String)arr.get(i)));
//	line = fr.readLine();line = fr.readLine();
//	while ((line = fr.readLine()) != null) {
//	fw.write("gsiftp://"+ job.rp.rp_host +job.rp.rp_storagePath + "/" + job.jobName+"/");
//	fw.write(line);
//	fw.write("\n");
//	fw.write("gsiftp://"+ job.repo_host + job.repo_storagePath + "/" + job.jobName +"/"+line);
//	fw.write("\n\n");
//	}
//	fr.close();
//	fw.close();
//	}


//	} catch (Exception e) {
//	System.out.println (e);
//	}		
//	}

//	public void createTransferOutputJobFiles (int numberOfJobs) {
//	String jobName = "tx_jars_2HOST_";
//	String exefile = "/usr/local/vds-1.4.7/bin/kickstart";
//	FileWriter fr = null;
//	String name = null;

//	try {
//	for (int i = 0; i < numberOfJobs; i++) {
//	name = jobName+(i+1);
//	fr = new FileWriter(outputDir + name+".sub");		

//	fr.write ("\n\n");
//	fr.write("environment = GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;" + "\n");
//	fr.write("arguments = -n transfer -N VDS::transfer:1.0 -i - -R local /usr/local/vds-1.4.7/bin/transfer  -f  base-uri se-mount-point" + "\n");
//	fr.write("copy_to_spool = false" + "\n");
//	fr.write("error = " + name + ".err" + "\n");
//	fr.write("executable = " + exefile + "\n");
//	fr.write("globusrsl ="+ job.rp.rp_globusrsl +"\n");
//	fr.write ("globusscheduler = "+job.rp.rp_host+"/"+job.rp.rp_batchScheduler+"\n");		
//	fr.write("input = " + name + ".in" + "\n");
//	fr.write("log = " + name + ".log" + "\n");
//	fr.write("output = " + name + ".out" + "\n");

//	fr.write ("periodic_release = (NumSystemHolds <= 3)" + "\n");
//	fr.write ("periodic_remove = (NumSystemHolds > 3)" + "\n");			
//	fr.write("remote_initialdir = " + job.repo_storagePath + "/" + job.jobName + "\n");

//	fr.write("transfer_error = true" + "\n");
//	fr.write("transfer_executable = false" + "\n");
//	fr.write("transfer_output = true" + "\n");
//	fr.write("universe = globus" + "\n");
//	fr.write("queue" + "\n\n");
//	fr.close();
//	}
//	} catch (Exception e) {
//	System.out.println (e);
//	}		
//	}

//	public void createUniqueDirOnRemote (String dirName) {
//	String jobName = "HPC_cdir";
//	try {
//	FileWriter fr = new FileWriter(outputDir + jobName+".sub");
//	fr.write ("\n\n");
//	fr.write("environment = GLOBUS_LOCATION=/usr/local/vdt/globus;LD_LIBRARY_PATH=/usr/local/vdt/globus/lib;\n");
//	fr.write ("arguments = -n dirmanager -N Pegasus::dirmanager:1.0 -R hpc /auto/rcf-104/dpmeyers/proj/pegasus-2.0.0RC1/bin/dirmanager");
//	fr.write (" --create --dir "+dirName+"\n");
//	fr.write("copy_to_spool = false\n");
//	fr.write ("error = "+jobName+".err\n");
//	fr.write("executable = /auto/rcf-104/dpmeyers/proj/vds-1.4.7/bin/kickstart\n");
//	fr.write("globusrsl = (jobtype=single)\n");
//	fr.write ("globusscheduler = "+job.rp.hostName+"/" + job.rp.forkScheduler + "\n");
//	fr.write ("log = "+jobName+".log\n");
//	fr.write ("output = "+jobName+".out\n");			
//	fr.write("periodic_release = (NumSystemHolds <= 3)\n");
//	fr.write("periodic_remove = (NumSystemHolds > 3)\n");
//	fr.write("remote_initialdir = " + job.rp.storagePath + "/" + job.jobName + "\n");
//	fr.write("transfer_error = true\n");
//	fr.write("transfer_executable = false\n");
//	fr.write("transfer_output = true\n");
//	fr.write("universe = globus\n");
//	fr.write("queue\n");
//	fr.close();
//	} catch (Exception e) {
//	System.out.println (e);
//	}		
//	}	

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

//		String outputDir = "";
//		if (args.length == 0) {
//		System.err.println("RUNNING FROM DEBUG MODE!");
//		args = new String[5];
//		args[0] = "scratchJavaDevelopers/kevin/job_example.xml";
//		args[0] = "output.xml";
//		args[0] = "/home/kevin/OpenSHA/condor/jobs/abe_output.xml";
//		args[1] = "false"; //don't skip cvm
//		args[2] = "false"; //don't restart
//		args[3] = "1209900";
//		args[4] = "16132000";
//		outputDir = "/home/kevin/OpenSHA/condor/jobs/";
//		}

//		try {
//		String metadata = args[0];
//		SAXReader reader = new SAXReader();
//		Document document = reader.read(new File(metadata));
//		Element root = document.getRootElement();

//		Element jobElem = root.element(HazardMapJob.XML_METADATA_NAME);

//		HazardMapJob job = HazardMapJob.fromXMLMetadata(jobElem);

//		System.out.println("rp_host = " + job.rp.rp_host);
//		System.out.println("rp_storagePath = " + job.rp.rp_storagePath + "/" + job.jobName);
//		System.out.println("rp_javaPath = " + job.rp.rp_javaPath);
//		System.out.println("rp_batchScheduler = " + job.rp.rp_batchScheduler);
//		System.out.println("repo_host = " + job.repo_host);
//		System.out.println("repo_storagePath = " + job.repo_storagePath + "/" + job.jobName);
//		System.out.println("sitesPerJob = " + job.sitesPerJob);
//		System.out.println("useCVM = " + job.useCVM);
//		System.out.println("submitHost = " + job.submit.submitHost);
//		System.out.println("submitHostPath = " + job.submit.submitHostPath+"/"+job.jobName);
//		System.out.println("submitHostPathToDependencies = " + job.submit.submitHostPathToDependencies);

//		boolean restart = false;
//		boolean skipCVMFiles = false;

//		if (args.length >= 2) {
//		skipCVMFiles = Boolean.parseBoolean(args[1]);
//		if (skipCVMFiles)
//		System.out.println("Skipping CVM File Creation!");
//		}

//		if (args.length >= 3) {
//		restart = Boolean.parseBoolean(args[2]);
//		if (restart) {
//		System.out.println("Restarting an old run!");
//		}
//		}

//		if (outputDir.length() == 0) { // we're not debugging
//		outputDir = job.submit.submitHostPath;
//		}

//		if (!outputDir.endsWith("/"))
//		outputDir = outputDir + "/";

//		boolean partial = false;
//		int startIndex = 0;
//		int endIndex = 0;

//		if (args.length >= 5) { // partial DAG
//		startIndex = Integer.parseInt(args[3]);
//		endIndex = Integer.parseInt(args[4]);

//		String suffix = "_" + HazardMapJobCreator.addLeadingZeros(startIndex, HazardMapJobCreator.nameLength)
//		+ "_" + HazardMapJobCreator.addLeadingZeros(endIndex, HazardMapJobCreator.nameLength);

//		job.jobName = job.jobName + suffix;

//		while (job.rp.rp_storagePath.endsWith("/")) {
//		int end = job.rp.rp_storagePath.length() - 2;
//		job.rp.rp_storagePath = job.rp.rp_storagePath.substring(0, end);
//		}

//		job.rp.rp_storagePath = job.rp.rp_storagePath + suffix;

//		job.metadataFileName = job.jobName + ".xml";

//		jobElem.detach();
//		root = job.toXMLMetadata(root);

//		partial = true;
//		}

//		outputDir = outputDir + job.jobName;

//		String originalDir = "";

//		if (restart) {
//		originalDir = outputDir + "/";
//		outputDir = outputDir + "_RESTART/";
//		} else
//		outputDir = outputDir + "/";

////		String outputDir = "/home/kevin/OpenSHA/condor/jobs/benchmark_RELM_UCERF_0.1_Purdue/";
//		// SDSC
////		String remoteJobDir = "/gpfs/projects/scec/CyberShake2007/opensha/kevin/meetingMap";
//		// HPC
////		String remoteJobDir = "/auto/scec-00/kmilner/hazMaps/benchmark_RELM_UCERF_0.1";
//		// Dynamic
////		String remoteJobDir = "/nfs/dynamic-1/opensha/kmilner/verification_0.02";
//		// ORNL
////		String remoteJobDir = "/tmp/benchmark_RELM_UCERF_0.1";

//		File outputDirFile = new File(outputDir);
//		if (!outputDirFile.exists())
//		outputDirFile.mkdir();

//		File outFile = new File(outputDir + "out/");
//		if (!outFile.exists())
//		outFile.mkdir();
//		File errFile = new File(outputDir + "err/");
//		if (!errFile.exists())
//		errFile.mkdir();
//		File logFile = new File(outputDir + "log/");
//		if (!logFile.exists())
//		logFile.mkdir();

//		// load the region
//		Element regionElement = root.element(EvenlyGriddedGeographicRegion.XML_METADATA_NAME);
//		EvenlyGriddedGeographicRegion region = EvenlyGriddedGeographicRegion.fromXMLMetadata(regionElement);
//		SitesInGriddedRegionAPI sites = new SitesInGriddedRegion(region.getRegionOutline(), region.getGridSpacing());

//		// see if the ERF needs to be created and saved

//		if (job.saveERF) {
//		Element erfElement = root.element(EqkRupForecast.XML_METADATA_NAME);
////		erfElement.setName("ERF_OLD");
//		root.add(erfElement.createCopy("ERF_REF"));
//		erfElement.detach();
//		System.out.println("Creating ERF...");
//		EqkRupForecast erf = EqkRupForecast.fromXMLMetadata(erfElement);
//		System.out.println("Updating Forecast...");
//		erf.updateForecast();
//		String erfFileName = job.jobName + "_ERF.obj";
//		System.out.println("Saving ERF to " + erfFileName + "...");
//		FileUtils.saveObjectInFileThrow(outputDir + erfFileName, erf);
//		Element newERFElement = root.addElement(EqkRupForecast.XML_METADATA_NAME);
//		newERFElement.addAttribute("fileName", erfFileName);
//		System.out.println("Done with ERF");
//		}

//		OutputFormat format = OutputFormat.createPrettyPrint();
//		XMLWriter writer = new XMLWriter(new FileWriter(outputDir + job.metadataFileName), format);
//		writer.write(document);
//		writer.close();

//		HazardMapJobCreator creator;
//		if (partial)
//		creator = new HazardMapJobCreator(outputDir, sites, startIndex, endIndex, job);
//		else
//		creator = new HazardMapJobCreator(outputDir, sites, job);


//		creator.skipCVMFiles = skipCVMFiles;

//		try {
//		if (restart)
//		creator.createRestartJobs(originalDir);
//		else
//		creator.createJobs();
//		creator.createSubmitScripts(24);

//		// Mahesh code

//		String remoteJobDir = job.rp.rp_storagePath;
//		/*				
//		DateFormat myformat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");  
//		StringBuffer buf = new StringBuffer ();
//		buf.append(remoteJobDir+"/");
//		buf.append(myformat.format(new Date()));
//		remoteJobDir = new String (buf);
//		System.out.println(remoteJobDir);				
//		*/				
//		creator.createMakeDirJob();
//		creator.createTestJob();
////		creator.createUniqueDirOnRemote (remoteJobDir);
//		creator.createCHModJob();
//		creator.createCopyLinkJob();
//		creator.createPostJob();
////		creator.createJarTransferToHostInputFile(outputDir, remoteJobDir);
//		creator.createDAG (outputDir, creator.getNumberOfJobs());
//		creator.createJarTransferJobFile();
//		creator.createJarTransferInputFile(outputDir, remoteJobDir);
////		creator.createTransferOutputJobFiles (creator.getNumberOfJobs());
//		// Mahesh code

//		} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
//		} catch (MalformedURLException e2) {
//		// TODO Auto-generated catch block
//		e2.printStackTrace();
//		} catch (DocumentException e2) {
//		// TODO Auto-generated catch block
//		e2.printStackTrace();
//		} catch (InvocationTargetException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
	}
}
