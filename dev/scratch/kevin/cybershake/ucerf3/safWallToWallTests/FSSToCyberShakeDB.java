package scratch.kevin.cybershake.ucerf3.safWallToWallTests;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.ERF2DB;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;

public class FSSToCyberShakeDB {

	public static void main(String[] args) throws IOException, DocumentException {
		FaultSystemSolution fss = FaultSystemIO.loadSol(
				new File("/home/kevin/CyberShake/ucerf3/saf_wall_to_wall_tests/saf_subset_sol.zip"));
		
		String erfName = "UCERF3 SAF Test ERF";
		String erfDescription = "Test UCERF3 ERF which contains only ruptures that are  entirely on the SAF and at least"
				+ " partially within Southern California";
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.getAuthenticatedDBAccess(true, true);
		if (db.isReadOnly())
			db.setIgnoreInserts(true);
		
		ERF2DB erf2db = new FSS_ERF2DB(fss, db);
		System.out.println("ERF has "+erf2db.getERF_Instance().getNumSources()+" sources");
		erf2db.insertForecaseInDB(erfName, erfDescription);
		
		
		
		
		db.destroy();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

}
