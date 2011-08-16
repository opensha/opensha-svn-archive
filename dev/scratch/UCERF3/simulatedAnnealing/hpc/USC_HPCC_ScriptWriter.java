package scratch.UCERF3.simulatedAnnealing.hpc;

import java.util.ArrayList;
import java.util.List;

public class USC_HPCC_ScriptWriter extends BatchScriptWriter {

	@Override
	public List<String> getBatchHeader(int mins, int nodes,
			int ppn, String queue) {
		ArrayList<String> pbs = new ArrayList<String>();
		
		if (queue != null && !queue.isEmpty())
			pbs.add("#PBS -q "+queue);
		pbs.add("#PBS -l walltime=00:"+mins+":00,nodes="+nodes+":ppn="+ppn);
		pbs.add("#PBS -V");
		pbs.add("");
		
		return pbs;
	}

}
