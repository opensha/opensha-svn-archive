package org.opensha.commons.gridComputing.condor;


import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.opensha.commons.gridComputing.condor.SubmitScript.Universe;
import org.opensha.commons.util.FileUtils;

public class TestDAGCreation extends TestCase {

	@Before
	public void setUp() throws Exception {
		
	}
	
	public void testDAGCreation() throws IOException {
		File tempDir = FileUtils.createTempDir();
		String dir = tempDir.getAbsolutePath() + File.separator;
		
		System.out.println("DIR: " + dir);
		
		SubmitScriptForDAG id = new SubmitScriptForDAG("id", "/usr/bin/id", null,
				tempDir.getAbsolutePath(), Universe.SCHEDULER, false);
		SubmitScriptForDAG pwd = new SubmitScriptForDAG("pwd", "/bin/pwd", null,
				tempDir.getAbsolutePath(), Universe.SCHEDULER, false);
		SubmitScriptForDAG ls = new SubmitScriptForDAG("ls", "/bin/ls", "-lah",
				tempDir.getAbsolutePath(), Universe.SCHEDULER, false);
		SubmitScriptForDAG ps = new SubmitScriptForDAG("ps", "/bin/ps", "aux",
				tempDir.getAbsolutePath(), Universe.SCHEDULER, false);
		
		id.writeScriptInDir(dir);
		pwd.writeScriptInDir(dir);
		ls.writeScriptInDir(dir);
		ps.writeScriptInDir(dir);
		
		DAG dag = new DAG();
		
		dag.addJob(id);
		dag.addJob(pwd);
		dag.addJob(ls);
		dag.addJob(ps);
		
		dag.addParentChildRelationship(id, pwd);
		dag.addParentChildRelationship(id, ls);
		dag.addParentChildRelationship(pwd, ps);
		dag.addParentChildRelationship(ls, ps);
		
		dag.writeDag(dir + "main.dag");
	}

}
