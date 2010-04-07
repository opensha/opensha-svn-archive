package org.opensha.gem.GEM1.scratch.marco.testParsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import org.opensha.gem.GEM1.calc.gemHazardCalculator.GemComputeModel;
import org.opensha.gem.GEM1.calc.gemHazardMaps.GemCalcSetup;
import org.opensha.gem.GEM1.calc.gemModelParsers.turkeyEmme.TurkeyEmmeSourceData;
import org.opensha.gem.GEM1.calc.gemModelParsers.turkeyEmme.TurkeyReadGMLFileFaults;
import org.opensha.gem.GEM1.calc.gemModelParsers.turkeyEmme.TurkeyReadGMLFileSourceZones;
import org.opensha.gem.GEM1.calc.gemModelParsers.turkeyEmme.TurkeyReadSourceData;
import org.opensha.gem.GEM1.util.CpuParams;
import org.opensha.gem.GEM1.util.DistanceParams;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMAreaSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;

public class TurkeyEmmeHazardMap01 extends GemCalcSetup {
	
	public TurkeyEmmeHazardMap01(String inputDir, String outputDir) {
		super(inputDir, outputDir);
	}

	public static void main (String[] args) throws IOException {

		// -----------------------------------------------------------------------------------------
		//           This is the polygon for HELEN: latitude 39.8 - 41.6 and longitude: 27.2 - 31.1.
//		double minLon = 27.0; double maxLon = 31.5; double minLat = 39.5; double maxLat = 42.0;
		double minLon = 25.0; double maxLon = 47.0; double minLat = 35.0; double maxLat = 44.0;
		double grdSpc = 0.1;
	
		// -----------------------------------------------------------------------------------------
		//                                          Relative path from the GemComputeHazardLogicTree
		String inputdir, outputdir;
		inputdir  	= "./../../data/emme/turkey/";
		outputdir 	= "./../../results/emme/turkey/";
		
		// Instantiate Calc Setup
		TurkeyEmmeHazardMap01 clcSetup = new TurkeyEmmeHazardMap01(inputdir,outputdir);

		// -----------------------------------------------------------------------------------------
		//                                                              Read source zones geometries 
//		String fileName = pathi+"areaSources.gml";
//		URL srcURL = Dummydata.class.getResource(fileName);
//		File file = new File(srcURL.getFile());
		BufferedReader file = clcSetup.getInputBufferedReader("areaSources.gml");
		TurkeyReadGMLFileSourceZones zon = new TurkeyReadGMLFileSourceZones(file);
		System.out.println("Number of zones.....: "+zon.getName().size());
		
		// -----------------------------------------------------------------------------------------
		//                                                                    Read faults geometries
//		fileName = pathi+"faultSourcesNOSEG.gml";
//		URL fltURL = Dummydata.class.getResource(fileName);
//		File fileFaults = new File(fltURL.getFile());
		BufferedReader file01 = clcSetup.getInputBufferedReader("faultSourcesNOSEG.gml");
		TurkeyReadGMLFileFaults flt = new TurkeyReadGMLFileFaults(file01);
		System.out.println("Number of faults....: "+flt.getName().size());
		
		// -----------------------------------------------------------------------------------------
		//                                                   Read input file - Seismicity parameters 
//		fileName = pathi+"sourceData.txt";
//		URL dataURL = Dummydata.class.getResource(fileName);
//		File fileData = new File(dataURL.getFile());
		BufferedReader file02 = clcSetup.getInputBufferedReader("sourceData.txt");
		TurkeyReadSourceData dat = new TurkeyReadSourceData(file02);
		
		// -----------------------------------------------------------------------------------------
		//                                                                           Get source data 
		TurkeyEmmeSourceData srcDat = new TurkeyEmmeSourceData(dat,zon,flt);
//		writeXML(srcDat.getList());
//		System.out.println("Execution stopped before running calculation");
//		System.exit(0);
		
		// -----------------------------------------------------------------------------------------
		//                                                                      Calculation settings
		HashMap<String,Object> outMap = clcSetup.getcalcSett().getOut();  
		outMap.put(CpuParams.CPU_NUMBER.toString(),30);
		outMap.put(DistanceParams.MAX_DIST_SOURCE.toString(),300.0);
		clcSetup.getcalcSett().setOut(outMap);

		// -----------------------------------------------------------------------------------------
		//                                                                            Compute hazard			
		double[] prbEx = {0.1};
		GemComputeModel gcm = new GemComputeModel(
				srcDat.getList(), 
				"turkeyEmme", 
				clcSetup.getGmpeLT().getGemLogicTree(), 
				minLat, maxLat, minLon, maxLon, grdSpc, prbEx, 
				clcSetup.getOutputPath(outputdir), 
				true, clcSetup.getcalcSett());
	}	
	
	private static void writeXML(ArrayList<GEMSourceData> lst) throws IOException{
		// Test XML
		System.out.println("write XML");
		String fileName = "/Users/marcop/Desktop/turkeySources.xml";
		BufferedWriter buf = new BufferedWriter(new FileWriter(fileName));
		for (GEMSourceData src: lst){
			if (src instanceof GEMAreaSourceData) {
				GEMAreaSourceData tmpsrc = (GEMAreaSourceData) src;
				tmpsrc.writeXML(buf);
			}
		}
		buf.close();
	}
}
