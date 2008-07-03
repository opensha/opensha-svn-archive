package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.fileGen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.opensha.data.Location;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelSummaryFinal;
import org.opensha.sha.fault.FaultTrace;

public class DeformationModelFileGenerator {
	
	private static final String FILE_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/data/finalReferenceFaultParamDb/fileGen/";
	
	private boolean sort = true;
	
	DeformationModelSummaryFinal summaries = new DeformationModelSummaryFinal();
	DeformationModelPrefDataFinal defModels = new DeformationModelPrefDataFinal();
	
	ArrayList<DeformationModelSummary> deformationModelSummariesList;
	ArrayList<ArrayList<FaultSectionPrefData>> faulSectionIDListList = new ArrayList<ArrayList<FaultSectionPrefData>>();
	
	/**
	 * This class generates files representations of each deformation model. 
	 */
	public DeformationModelFileGenerator() {
		this.loadDefModels();
	}
	
	/** 
	 * This loads the Deformation Models and Data to ArrayLists to be written later.
	 */
	private void loadDefModels() {
		deformationModelSummariesList = summaries.getAllDeformationModels();
		
		for (DeformationModelSummary summary : deformationModelSummariesList) {
			int id = summary.getDeformationModelId();
			
			ArrayList<FaultSectionPrefData> faultSections = defModels.getAllFaultSectionPrefData(id);
			
			if (sort)
				Collections.sort(faultSections, new FaultSectionNameComparator());
			
			faulSectionIDListList.add(faultSections);
		}
	}
	
	/**
	 * Write the deformation models to XML and text files
	 */
	public void saveToFiles() {
		for (int i=0; i<deformationModelSummariesList.size(); i++) {
			DeformationModelSummary summary = deformationModelSummariesList.get(i);
			ArrayList<FaultSectionPrefData> sections = faulSectionIDListList.get(i);
			
			String filePrefix = FILE_PATH + "DeformationModel_" + summary.getDeformationModelName();
			
			// save to XML
			this.saveDefModelToXML(summary, sections, filePrefix);
			this.saveDefModelToText(summary, sections, filePrefix);
		}
	}
	
	/**
	 * Writes a given deformation model to a text file
	 * @param model
	 * @param sections
	 * @param filePrefix
	 */
	private void saveDefModelToText(DeformationModelSummary model, ArrayList<FaultSectionPrefData> sections, String filePrefix) {
		String fileName = filePrefix + ".txt";
		
		System.out.println("Writing Text " + model.getDeformationModelName() + " to " + fileName);
		
		try {
			FileWriter fw = new FileWriter(fileName);
			
//			#Section Name
//			#Ave Upper Seis Depth (km)
//			#Ave Lower Seis Depth (km)
//			#Ave Dip (degrees)
//			#Ave Long Term Slip Rate
//			#Ave Long Term Slip Rate Standard Deviation
//			#Ave Aseismic Slip Factor
//			#Ave Rake
//			#Trace Length (derivative value) (km)
//			#Num Trace Points
//			#lat1 lon1
//			#lat2 lon2 
			
			// write the header
			fw.write("#********************************" + "\n");
			fw.write("# This file represents a WGCEP UCERF 2 Deformation Model" + "\n");
			fw.write("#" + "\n");
			fw.write("# Deformation Model Name: " + model.getDeformationModelName() + "\n");
			fw.write("# Fault Model Name: " + model.getFaultModel().getFaultModelName() + "\n");
			fw.write("#" + "\n");
			fw.write("# Each fault trace is separated by an empty line." + "\n");
			fw.write("# The fields for each fault section are as follows:" + "\n");
			fw.write("# Section Name" + "\n");
			fw.write("# Ave Upper Seis Depth (km)" + "\n");
			fw.write("# Ave Lower Seis Depth (km)" + "\n");
			fw.write("# Ave Dip (degrees)" + "\n");
			fw.write("# Ave Long Term Slip Rate" + "\n");
			fw.write("# Ave Long Term Slip Rate Standard Deviation" + "\n");
			fw.write("# Ave Aseismic Slip Factor" + "\n");
			fw.write("# Ave Rake" + "\n");
//			fw.write("#Trace Length (derivative value) (km)" + "\n");
			fw.write("# Num Trace Points" + "\n");
			fw.write("# lat1 lon1 depth1" + "\n");
			fw.write("# lat2 lon2 depth2" + "\n");
			fw.write("# latN lonN depthN" + "\n");
			fw.write("#********************************" + "\n");
			
			for (FaultSectionPrefData section : sections) {
				fw.write(section.getSectionName() + "\n");
				fw.write(section.getAveUpperDepth() + "\n");
				fw.write(section.getAveLowerDepth() + "\n");
				fw.write(section.getAveDip() + "\n");
				fw.write(section.getAveLongTermSlipRate() + "\n");
				fw.write(section.getSlipRateStdDev() + "\n");
				fw.write(section.getAseismicSlipFactor() + "\n");
				fw.write(section.getAveRake() + "\n");
				
				FaultTrace trace = section.getFaultTrace();
				
				fw.write(trace.getNumLocations() + "\n");
				
				for (int i=0; i<trace.getNumLocations(); i++) {
					Location loc = trace.getLocationAt(i);
					
					fw.write(loc.getLatitude() + " " + loc.getLongitude() + " " + loc.getDepth() + "\n");
				}
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes a given deformation model to XML
	 * @param model
	 * @param sections
	 * @param filePrefix
	 */
	private void saveDefModelToXML(DeformationModelSummary model, ArrayList<FaultSectionPrefData> sections, String filePrefix) {
		String fileName = filePrefix + ".xml";
		
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement( "DeformationModel" );
		
		root = model.toXMLMetadata(root);
		root = model.getFaultModel().toXMLMetadata(root);
		Element sectionsEl = root.addElement("FaultSections");
		for (FaultSectionPrefData section : sections) {
			sectionsEl = section.toXMLMetadata(root);
		}
		
		XMLWriter writer;

		try {
			OutputFormat format = OutputFormat.createPrettyPrint();

			System.out.println("Writing XML " + model.getDeformationModelName() + " to " + fileName);
			writer = new XMLWriter(new FileWriter(fileName), format);
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		DeformationModelFileGenerator gen = new DeformationModelFileGenerator();
		gen.saveToFiles();
	}
	
	/**
	 * Class for sorting fault sections by name
	 * @author kevin
	 *
	 */
	class FaultSectionNameComparator implements Comparator<FaultSectionPrefData> {
		// A Collator does string comparisons
		private Collator c = Collator.getInstance();
		
		/**
		 * This is called when you do Arrays.sort on an array or Collections.sort on a collection (IE ArrayList).
		 * 
		 * It simply compares their names using a Collator. It doesn't know how to compare
		 * a file with a directory, and returns -1 in this case.
		 */
		public int compare(FaultSectionPrefData f1, FaultSectionPrefData f2) {
			if(f1 == f2)
				return 0;

			// let the Collator do the string comparison, and return the result
			return c.compare(f1.getSectionName(), f2.getSectionName());
		}
	}
}
