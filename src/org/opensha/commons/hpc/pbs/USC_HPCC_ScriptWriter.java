package org.opensha.commons.hpc.pbs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class USC_HPCC_ScriptWriter extends BatchScriptWriter {
	
	public static final File MPJ_HOME = new File("/home/rcf-12/kmilner/mpj-v0_38");
	public static final File JAVA_BIN = new File("/usr/usc/jdk/default/jre/bin/java");

	@Override
	public List<String> getBatchHeader(int mins, int nodes,
			int ppn, String queue) {
		ArrayList<String> pbs = new ArrayList<String>();
		
		if (queue != null && !queue.isEmpty())
			pbs.add("#PBS -q "+queue);
		String dashL = "#PBS -l walltime=00:"+mins+":00,nodes="+nodes;
		if (ppn > 0)
			dashL += ":ppn="+ppn;
		pbs.add(dashL);
		pbs.add("#PBS -V");
		pbs.add("");
		pbs.add("NEW_NODEFILE=\"/tmp/${USER}-hostfile-${PBS_JOBID}\"");
		pbs.add("echo \"creating PBS_NODEFILE: $NEW_NODEFILE\"");
		pbs.add("cat $PBS_NODEFILE | sort | uniq > $NEW_NODEFILE");
		pbs.add("export PBS_NODEFILE=$NEW_NODEFILE");
		pbs.add("");
		
		return pbs;
	}

}
