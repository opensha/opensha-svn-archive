package scratch.kevin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.UCERF2_FaultSectionPrefData;

public class DM_Rake_Writer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int deformationModelID = 82;
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<UCERF2_FaultSectionPrefData> allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelID);
		
		FileWriter fw = new FileWriter("/tmp/dm_2.1_vals.txt");
		for (UCERF2_FaultSectionPrefData data : allFaultSectionPrefData) {
			String name = data.getName();
			double rake = data.getAveRake();
			double slip = data.getAveLongTermSlipRate();
			double strike = data.getFaultTrace().getStrikeDirection();
			double dip = data.getAveDip();
			
			fw.write("name: '"+name+"'\tslip: "+slip+"\tstrike: "+strike+"'\tdip: "+dip+"\trake: "+rake+"\n");
		}
		fw.close();
	}

}
