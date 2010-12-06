package scratch.kevin;

import java.sql.SQLException;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;

public class FaultDBDipDirTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
		
		FaultSectionVer2_DB_DAO secs2db = new FaultSectionVer2_DB_DAO(db);
		for (FaultSectionData fault : secs2db.getAllFaultSections()) {
			float dipDir = fault.getDipDirection();
			if (!Float.isNaN(dipDir)) {
				double calculatedDir = fault.getFaultTrace().getDipDirection();
				if (dipDir != (float)calculatedDir) {
					System.out.println(fault.getSectionId() + ". " + fault.getSectionName()
							+ " differs!\tdb: " + dipDir + "\tcalc: " + calculatedDir
							+ "\tdiff: " + Math.abs(dipDir - calculatedDir));
				}
			}
		}
		try {
			db.destroy();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

}
