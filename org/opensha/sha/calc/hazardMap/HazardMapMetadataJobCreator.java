package org.opensha.sha.calc.hazardMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.util.FileUtils;
import org.opensha.util.RunScript;


public class HazardMapMetadataJobCreator {

	private static final String DEBUG_FOLDER_NAME = "/home/kevin/OpenSHA/condor/jobs/";

	private String restartOriginalDir = "";

	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>(); 

	private Document metadata;
	private boolean skipCVMFiles;
	private boolean restart;
	private boolean debug;
	private int startDAG;
	private int endDAG;

	public HazardMapMetadataJobCreator(Document metadata, boolean skipCVMFiles, boolean restart, boolean debug, int startDAG, int endDAG) {
		this.metadata = metadata;
		this.skipCVMFiles = skipCVMFiles;
		this.restart = restart;
		this.debug = debug;
		this.startDAG = startDAG;
		this.endDAG = endDAG;
	}

	public HazardMapMetadataJobCreator(Document metadata, boolean skipCVMFiles, boolean restart) throws InvocationTargetException, IOException {
		this(metadata, skipCVMFiles, restart, false, -1, -1);
	}

	public void createDAG(boolean submit) throws InvocationTargetException, IOException {
		this.setProgressIndeterminate(true);

		// get the root element
		Element root = metadata.getRootElement();
		// load the job params from metadata
		HazardMapJob job = this.loadJob(root, startDAG, endDAG);
		// get and create the output directory (and subdirs)
		String outputDir = this.createDirs(job, restart, debug);
		// load the sites from metadata
		SitesInGriddedRegionAPI sites = this.loadSites(root);
		System.out.println("Loaded " + sites.getNumGridLocs() + " sites!");
		// save the ERF to a file if needed
		if (job.saveERF) {
			this.saveERF(root, job, outputDir);
		}
		// write out new metadata file
		String metadataFileName = outputDir + job.metadataFileName;
		this.writeCalcMetadataFile(metadata, metadataFileName);

		// initialize the job creator
		HazardMapJobCreator creator;

		if (startDAG >= 0 && endDAG > startDAG)
			creator = new HazardMapJobCreator(outputDir, sites, startDAG, endDAG, job);
		else
			creator = new HazardMapJobCreator(outputDir, sites, job);
		creator.addAllProgressListeners(progressListeners);
		
		boolean stageOut = true;
		// if it's already being computed on the storage host, don't stage out
		if (job.rp.hostName.toLowerCase().contains(job.storageHost.schedulerHostName.toLowerCase())
				|| job.rp.hostName.toLowerCase().contains(job.storageHost.gridFTPHostName.toLowerCase()))
			stageOut = false;
		creator.setStageOut(stageOut);
		
		// create the actual jobs
		if (restart)
			creator.createRestartJobs(this.restartOriginalDir, stageOut);
		else
			creator.createJobs(stageOut);

		// create all of the DAG elements
		int totalDAG = 7;
		this.updateProgressMessage("Creating DAG");
		this.updateProgress(0, totalDAG);
		if (stageOut)
			creator.createStorageMakeDirJob();
		creator.createMakeDirJob();
		this.updateProgress(1, totalDAG);
		creator.createTestJob();
		this.updateProgress(2, totalDAG);
		creator.createCHModJob();
		this.updateProgress(3, totalDAG);
		creator.createCopyLinkJob();
		this.updateProgress(4, totalDAG);
		creator.createPostJob();
		this.updateProgress(5, totalDAG);
		creator.createDAG (outputDir, creator.getNumberOfJobs());
		this.updateProgress(6, totalDAG);
		creator.createJarTransferJobFile();
		this.updateProgress(7, totalDAG);
		creator.createJarTransferInputFile(outputDir, job.rp.storagePath);

		creator.createSubmitDAGScript(submit);
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

	/**
	 * Loads the Job Params from metadata
	 * @param jobElem
	 * @return
	 */
	private HazardMapJob loadJob(Element root, int start, int end) {

		this.updateProgressMessage("Loading Job");

		// extract element for job
		Element jobElem = root.element(HazardMapJob.XML_METADATA_NAME);

		// load job from metadata
		HazardMapJob job = HazardMapJob.fromXMLMetadata(jobElem);

		if (start >= 0 && end > start) { // this is a partial DAG
			// create suffix for job name with indices
			String suffix = "_" + HazardMapJobCreator.addLeadingZeros(start, HazardMapJobCreator.nameLength)
			+ "_" + HazardMapJobCreator.addLeadingZeros(end, HazardMapJobCreator.nameLength);

			// add the suffix to the job name
			job.jobName = job.jobName + suffix;

			// remove trailing slashes
			while (job.rp.storagePath.endsWith("/")) {
				int endLine = job.rp.storagePath.length() - 2;
				job.rp.storagePath = job.rp.storagePath.substring(0, endLine);
			}

			// change the remote directory for the partial DAG
			job.rp.storagePath = job.rp.storagePath + suffix;

			// rename the metadata file name
			job.metadataFileName = job.jobName + ".xml";

			// delete and reattach the job to the xml document
			jobElem.detach();
			root = job.toXMLMetadata(root);
		}

		System.out.println("rp_host = " + job.rp.hostName);
		System.out.println("rp_storagePath = " + job.rp.storagePath);
		System.out.println("rp_javaPath = " + job.rp.javaPath);
		System.out.println("rp_batchScheduler = " + job.rp.batchScheduler);
		System.out.println("sitesPerJob = " + job.sitesPerJob);
		System.out.println("useCVM = " + job.useCVM);
		System.out.println("submitHost = " + job.submitHost.hostName);
		System.out.println("submitHostPath = " + job.submitHost.path+"/"+job.jobName);
		System.out.println("submitHostPathToDependencies = " + job.submitHost.dependencyPath);

		return job;
	}

	private String createDirs(HazardMapJob job, boolean restart, boolean debug) {
		this.updateProgressMessage("Creating Directories");

		String outputDir = "";

		// if we're just debugging
		if (debug)
			outputDir = DEBUG_FOLDER_NAME;
		else
			outputDir = job.submitHost.path;

		if (!outputDir.endsWith("/"))
			outputDir = outputDir + "/";

		outputDir = outputDir + job.jobName;

		if (restart) {
			restartOriginalDir = outputDir + "/";
			outputDir = outputDir + "_RESTART/";
		} else
			outputDir = outputDir + "/";

		// create job dir
		File outputDirFile = new File(outputDir);
		if (!outputDirFile.exists())
			outputDirFile.mkdir();

		// create out, err, and log dirs
		File outFile = new File(outputDir + "out/");
		if (!outFile.exists())
			outFile.mkdir();
		File errFile = new File(outputDir + "err/");
		if (!errFile.exists())
			errFile.mkdir();
		File logFile = new File(outputDir + "log/");
		if (!logFile.exists())
			logFile.mkdir();

		return outputDir;
	}

	private SitesInGriddedRegionAPI loadSites(Element root) {
		this.updateProgressMessage("Loading Sites");
		Element regionElement = root.element(EvenlyGriddedGeographicRegion.XML_METADATA_NAME);
		EvenlyGriddedGeographicRegion region = EvenlyGriddedGeographicRegion.fromXMLMetadata(regionElement);
		SitesInGriddedRegionAPI sites = null;
		if (region.isRectangular()) {
			try {
				sites = new SitesInGriddedRectangularRegion(region, region.getGridSpacing());
			} catch (RegionConstraintException e) {
				sites = new SitesInGriddedRegion(region.getRegionOutline(), region.getGridSpacing());
			}
		} else {
			sites = new SitesInGriddedRegion(region.getRegionOutline(), region.getGridSpacing());
		}

		return sites;
	}

	private void saveERF(Element root, HazardMapJob job, String outputDir) throws InvocationTargetException, IOException {
		this.updateProgressMessage("Loading ERF");
		// load the erf element from metadata
		Element erfElement = root.element(EqkRupForecast.XML_METADATA_NAME);

		// rename the old erf to ERF_REF so that the params are preserved, but it is not used for calculation
		root.add(erfElement.createCopy("ERF_REF"));
		erfElement.detach();

		// load the erf from metadata
		System.out.println("Creating ERF...");
		EqkRupForecast erf = EqkRupForecast.fromXMLMetadata(erfElement);

		// update it's forecast
		this.updateProgressMessage( "Updating ERF Forecast");
		System.out.println("Updating Forecast...");
		erf.updateForecast();

		// save ERF to file
		String erfFileName = job.jobName + "_ERF.obj";
		System.out.println("Saving ERF to " + erfFileName + "...");
		FileUtils.saveObjectInFileThrow(outputDir + erfFileName, erf);

		// create new ERF element and add to root
		Element newERFElement = root.addElement(EqkRupForecast.XML_METADATA_NAME);
		newERFElement.addAttribute("fileName", erfFileName);

		System.out.println("Done with ERF");
	}

	public void writeCalcMetadataFile(Document document, String fileName) throws IOException {
		this.updateProgressMessage("Writing Metadata File");
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(document);
		writer.close();
	}

	public static void main(String args[]) {
		boolean skipCVMFiles = false;
		boolean restart = false;
		boolean debug = false;
		if (args.length == 0) {
			System.err.println("RUNNING FROM DEBUG MODE!");
			args = new String[1];
			args[0] = "output.xml";
			debug = true;
		}

		try {
			String metadata = args[0];
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(metadata));

			HazardMapMetadataJobCreator creator = new HazardMapMetadataJobCreator(document, skipCVMFiles, restart, debug, -1, -1);
			creator.createDAG(false);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
