package scratch.UCERF3.elasticRebound.simulatorAnalysis;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

public class simulatorAnalysisUtils {
	
	
	public static void test() {
		
		// Set the simulator Geometry file
		File geomFileDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/allcal2_1-7-11");
//		File geomFileDir = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/simulatorDataFiles");
		File geomFile = new File(geomFileDir, "ALLCAL2_1-7-11_Geometry.dat");
		
		// Set the dir for simulator event files 
		File simEventFileDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/simulatorDataFiles");
//		File simEventFileDir = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/simulatorDataFiles");
		
//		File eventFile = new File(simEventFileDir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
//		File eventFile = new File(simEventFileDir, "ALLCAL2_1-7-11_no-creep_dyn-05_st-20_108764-277803_Events_slip-map-5.5.dat");
//		File eventFile = new File(simEventFileDir, "ALLCAL2-30k-output[3-24-11].converted");
		File eventFile = new File(simEventFileDir, "Fred-allcal2-7june11.txt");

				String dirNameForSavingFiles = "tempSimTest";

				try {
					System.out.println("Loading geometry...");
					General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
					System.out.println("Loading events...");
					tools.read_EQSIMv04_EventsFile(eventFile);
					tools.setDirNameForSavingFiles(dirNameForSavingFiles);
					
//					tools.testElementAreas();
//					tools.printMinAndMaxElementArea();
//					tools.checkElementSlipRates(null, true);
//					tools.checkEventMagnitudes();
//					tools.checkFullDDW_rupturing();
					tools.computeTotalMagFreqDist(4.05, 8.95, 50, true, false);
//					tools.plotNormRecurIntsForAllSurfaceElements(6.0, true);
					
//					ArrayList<String> infoStrings = new ArrayList<String>();
//					infoStrings.add("UCERF3.elasticRebound.simulatorAnalysis.simulatorAnalysisUtils.runAll()\n");
//					infoStrings.add(dirNameForSavingFiles+"\tusing file "+fileName+"\n");
//					infoStrings.add("Simulation Duration is "+(float)tools.getSimulationDurationYears()+" years\n");
//					
//					String info = tools.testTimePredictability(magThresh, true, null, true);
//					infoStrings.add(info);
//
//					try {
//						FileWriter infoFileWriter = new FileWriter(dirNameForSavingFiles+"/INFO.txt");
//						for(String string: infoStrings) 
//							infoFileWriter.write(string+"\n");
//						infoFileWriter.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					System.out.println("Done");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
	}

	
	
	public static void runAll() {
		
		// Set the simulator Geometry file
//		File geomFileDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/allcal2_1-7-11");
		File geomFileDir = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/simulatorDataFiles");
		File geomFile = new File(geomFileDir, "ALLCAL2_1-7-11_Geometry.dat");
		
		// Set the dir for simulator event files 
//		File simEventFileDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/simulatorDataFiles");
		File simEventFileDir = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/simulatorDataFiles");
		
		// set the list of event files to loop over (and corresponding short dir names for each)
		String[] eventFileArray = {
//				"eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall",	// Kevin has long version:  eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall
				"ALLCAL2_1-7-11_no-creep_dyn-05_st-20_108764-277803_Events_slip-map-5.5.dat",
				"ALLCAL2-30k-output[3-24-11].converted",
				"Fred-allcal2-7june11.txt"
				};
		String[] dirNamesPrefixArray = {"RSQSim","VirtCal","ALLCAL","ViscoSim"};
		
		// set the list of supra-seismogenic mag thresholds (NaN means it will be defined by ave fault DDW)
		double[] seismoMagThreshArray = {6.5,Double.NaN};
				
		// loop over desired runs
		for(double magThresh:seismoMagThreshArray) {
			int dirIndex = -1;
			for(String fileName:eventFileArray) {
				
				File eventFile = new File(simEventFileDir, fileName);
				dirIndex+=1;
				String dirNameForSavingFiles = dirNamesPrefixArray[dirIndex]+"_"+ (new Double(magThresh)).toString().replaceAll("\\.", "pt");

				try {
					System.out.println("Loading geometry...");
					General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
					System.out.println("Loading events...");
					tools.read_EQSIMv04_EventsFile(eventFile);
					tools.setDirNameForSavingFiles(dirNameForSavingFiles);
					
//					tools.printMinAndMaxElementArea();
//					tools.checkElementSlipRates(null, true);
//					tools.checkEventMagnitudes();
//					tools.checkFullDDW_rupturing();
//					tools.computeTotalMagFreqDist(4.05, 8.95, 50, true, true);
//					tools.plotNormRecurIntsForAllSurfaceElements(6.0, true);
					
					ArrayList<String> infoStrings = new ArrayList<String>();
					infoStrings.add("UCERF3.elasticRebound.simulatorAnalysis.simulatorAnalysisUtils.runAll()\n");
					infoStrings.add(dirNameForSavingFiles+"\tusing file "+fileName+"\n");
					infoStrings.add("Simulation Duration is "+(float)tools.getSimulationDurationYears()+" years\n");
					
					String info = tools.testTimePredictability(magThresh, true, null, true);
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
					
					System.out.println("Done");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		
		test();
//		runAll();
		
	}

}
