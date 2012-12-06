package scratch.UCERF3.elasticRebound.simulatorAnalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.geo.Location;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

public class simulatorAnalysisUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		

		File geomFileDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/allcal2_1-7-11");
		File geomFile = new File(geomFileDir, "ALLCAL2_1-7-11_Geometry.dat");

		
		File simOutputDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/simulatorDataFiles");
		
		File eventFile = new File(simOutputDir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
//		File eventFile = new File(simOutputDir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");	// the "long" version Kevin is using
		String dirNameForSavingFiles = "TestRSQsim";

		General_EQSIM_Tools tools;
		
		try {
			System.out.println("Loading geometry...");
			tools = new General_EQSIM_Tools(geomFile);
			System.out.println("Loading events...");
			tools.read_EQSIMv04_EventsFile(eventFile);
			System.out.println("Done");
			
//			tools.checkElementSlipRates(null, true);
//			tools.checkEventMagnitudes();
			
			tools.setDirNameForSavingFiles(dirNameForSavingFiles);
			
			ArrayList<String> infoStrings = new ArrayList<String>();
			infoStrings.add("Simulation Duration is "+(float)tools.getSimulationDurationYears()+" years");
			
			String info = tools.testTimePredictability(6.5, true, null, true);
			infoStrings.add(info);

			try {
				FileWriter infoFileWriter = new FileWriter(dirNameForSavingFiles+"/INFO.txt");
				for(String string: infoStrings) 
					infoFileWriter.write(string+"\n");
				infoFileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
