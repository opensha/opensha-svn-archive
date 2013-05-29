package scratch.peter.ucerf3.scripts;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Build branch average pbs scripts for individual submission
 */
public class MultiScript {

	public static void main(String[] args) throws IOException {
		// make40();
		make8();
	}

	// make FM-DM branch average scripts
	private static void make8() throws IOException {
		String batchID = "brAvg-FM-DM-5Hz";
		String TMP_LOCAL = "/Users/pmpowers/projects/OpenSHA/tmp/UC33/mapjobs/" +
			batchID + "/";

		String BASEDIR = "/home/scec-00/pmpowers";
		String JAVADIR = BASEDIR + "/lib";
		String SRCDIR = BASEDIR + "/UC33/src/bravg/FM-DM";

		String GRID = "CA_RELM";
		String SPACING = "0.1";
		String PERIOD = "GM0P20";
		int HRS = 2;
		int NODES = 36;
		String QUEUE = null; // "nbns";
		String BG = "INCLUDE";

		List<String> FMs = Lists.newArrayList("FM31", "FM32");
		List<String> DMs = Lists.newArrayList("ABM", "GEOL", "NEOK", "ZENGBB");

		for (String fm : FMs) {
			for (String dm : DMs) {
				String jobgroup = "UC33brAvg_" + fm + "_" + dm;
				String solfile = SRCDIR + "/" + jobgroup + ".zip";
				String outdir = BASEDIR + "/UC33/maps/" + batchID;
				String script = TMP_LOCAL + jobgroup + ".pbs";
				File tmpScript = new File(script);
				Files.createParentDirs(tmpScript);
				List<String> otherArgs = Lists.newArrayList(solfile, GRID,
					SPACING, PERIOD, BG, outdir);
				MapsFromSolution.writeScript(JAVADIR, HRS, NODES, QUEUE,
					script, otherArgs);
			}
		}

	}

	// make FM-DM-MS branch average scripts
	private static void make40() throws IOException {
		String batchID = "brAvg-FM-DM-MS";
		String TMP_LOCAL = "/Users/pmpowers/projects/OpenSHA/tmp/UC33/mapjobs/" +
			batchID + "/";

		String BASEDIR = "/home/scec-00/pmpowers";
		String JAVADIR = BASEDIR + "/lib";
		String SRCDIR = BASEDIR + "/UC33/src/bravg/FM-DM-MS";

		String GRID = "CA_RELM";
		String SPACING = "0.1";
		String PERIOD = "GM0P00";
		int HRS = 1;
		int NODES = 32;
		String QUEUE = null; // "nbns";
		String BG = "INCLUDE";

		List<String> FMs = Lists.newArrayList("FM31", "FM32");
		List<String> DMs = Lists.newArrayList("ABM", "GEOL", "NEOK", "ZENGBB");
		List<String> MSs = Lists.newArrayList("ELLB", "ELLBSL", "HB08",
			"SH09M", "SHCSD");

		for (String fm : FMs) {
			for (String dm : DMs) {
				for (String ms : MSs) {
					String jobgroup = "UC33brAvg_" + fm + "_" + dm + "_" + ms;
					String solfile = SRCDIR + "/" + jobgroup + ".zip";
					String outdir = BASEDIR + "/UC33/maps/" + batchID + "/" +
						jobgroup;
					String script = TMP_LOCAL + jobgroup + ".pbs";
					File tmpScript = new File(script);
					Files.createParentDirs(tmpScript);
					List<String> otherArgs = Lists.newArrayList(solfile, GRID,
						SPACING, PERIOD, BG, outdir);
					MapsFromSolution.writeScript(JAVADIR, HRS, NODES, QUEUE,
						script, otherArgs);
				}
			}
		}
	}

}
