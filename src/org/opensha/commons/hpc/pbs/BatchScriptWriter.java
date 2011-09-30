package org.opensha.commons.hpc.pbs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public abstract class BatchScriptWriter {
	
	public abstract List<String> getBatchHeader(int mins, int nodes, int ppn, String queue);
	
	public List<String> buildScript(List<String> script, int mins, int nodes, int ppn, String queue) {
		List<String> pbs = getBatchHeader(mins, nodes, ppn, queue);
		
		if (!pbs.get(pbs.size()-1).isEmpty())
			pbs.add("");
		
		script.addAll(2, pbs);
		
		return script;
	}
	
	public void writeScript(File file, List<String> script, int mins, int nodes, int ppn, String queue)
	throws IOException {
		List<String> pbs = buildScript(script, mins, nodes, ppn, queue);
		writeScript(file, pbs);
	}
	
	public void writeScript(File file, List<String> script) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		for (String line: script)
			fw.write(line + "\n");
		
		fw.close();
	}

}
