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
	
	
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		
//		testPlot();
//		System.exit(0);

//		File geomFileDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/allcal2_1-7-11");
		File geomFileDir = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/simulatorDataFiles");
		File geomFile = new File(geomFileDir, "ALLCAL2_1-7-11_Geometry.dat");

		
//		File simOutputDir = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/ProbModels/ElasticRebound/simulatorDataFiles");
		File simOutputDir = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/simulatorDataFiles");
		
		File eventFile = new File(simOutputDir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
//		File eventFile = new File(simOutputDir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");	// the "long" version Kevin is using
		String dirNameForSavingFiles = "TestRSQsim11";

		General_EQSIM_Tools tools;
		
		try {
			System.out.println("Loading geometry...");
			tools = new General_EQSIM_Tools(geomFile);
			
//			tools.printMinAndMaxElementArea();
//			System.exit(0);

			System.out.println("Loading events...");
			tools.read_EQSIMv04_EventsFile(eventFile);
						
			tools.setDirNameForSavingFiles(dirNameForSavingFiles);
			
//			tools.checkElementSlipRates(null, true);
//			tools.checkEventMagnitudes();
			
//			tools.checkFullDDW_rupturing();
//			System.out.println("Done");

//			System.exit(0);
			
//			tools.computeTotalMagFreqDist(4.05, 8.95, 50, true, true);
//			tools.plotNormRecurIntsForAllSurfaceElements(6.0, true);
//
			
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
	
	
	
	public static void testPlot() {
		
		DefaultXY_DataSet func = new DefaultXY_DataSet();
		
		func.set( 100.90956 , 105.042274 );
		func.set( 101.44242 , 120.59931 );
		func.set( 128.78944 , 102.14821 );
		func.set( 86.27948 , 109.59791 );
		func.set( 116.59927 , 119.44982 );
		func.set( 172.43605 , 211.28998 );
		func.set( 121.29631 , 94.66949 );
		func.set( 107.8798 , 121.30236 );
		func.set( 98.31027 , 102.08074 );
		func.set( 90.90774 , 98.41077 );
		
		// plot obs vs predicted scatter plot
		String plotTitle7 = "test";
		HeadlessGraphPanel plot7 = new HeadlessGraphPanel();
		ArrayList tempList = new ArrayList();
		tempList.add(func);
		ArrayList<PlotCurveCharacterstics> curveCharacteristics = new ArrayList<PlotCurveCharacterstics>();
		curveCharacteristics.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.RED));
//		plot7.setUserMinX(10);
//		plot7.setUserMaxX(10000);
//		plot7.setUserMinY(10);
//		plot7.setUserMaxY(10000);
//		plot7.setUserBounds(10, 10000, 10, 10000);
		plot7.setXLog(true);
		plot7.setYLog(true);
//		System.out.println("getUserMinX()="+plot7.getUserMinX());
		plot7.drawGraphPanel("test", "test", tempList, curveCharacteristics, true, plotTitle7);
		plot7.getCartPanel().setSize(1000, 800);
		String fileName7 = "testFuncPlot.pdf";
		try {
			plot7.saveAsPDF(fileName7);
//			plot7.saveAsPNG(fileName7);
		} catch (IOException e) {
			e.printStackTrace();
		}



	}

}
