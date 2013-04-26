package scratch.UCERF3.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.griddedSeismicity.GridSourceFileReader;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.logicTree.LogicTreeBranch;

public class FaultSystemIO {
	
	private static final boolean D = true;
	
	/*	******************************************
	 * 		FILE READING
	 *	******************************************/
	
	/**
	 * Loads a FaultSystemRupSet from a zip file. If possible, it will be loaded as an applicable subclass.
	 * @param file
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static FaultSystemRupSet loadRupSet(File file) throws ZipException, IOException, DocumentException {
		return loadRupSetAsApplicable(file);
	}
	
	/**
	 * Load an InversionFaultSystemRupSet from a zip file.
	 * @param file
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static InversionFaultSystemRupSet loadInvRupSet(File file) throws ZipException, IOException, DocumentException {
		FaultSystemRupSet rupSet = loadRupSetAsApplicable(file);
		Preconditions.checkArgument(rupSet instanceof InversionFaultSystemRupSet,
				"Rupture set cannot be loaded as an InversionFaultSystemRupSet");
		return (InversionFaultSystemRupSet)rupSet;
	}
	
	/**
	 * Load an FaultSystemSolution from a zip file. If possible, it will be loaded as an applicable subclass.
	 * @param file
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static FaultSystemSolution loadSol(File file) throws IOException, DocumentException {
		return loadSolAsApplicable(file);
	}
	
	/**
	 * Load an InversionFaultSystemSolution from a zip file. If possible, it will be loaded as an applicable subclass.
	 * @param file
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static InversionFaultSystemSolution loadInvSol(File file) throws IOException, DocumentException {
		FaultSystemSolution sol = loadSolAsApplicable(file);
		Preconditions.checkArgument(sol instanceof InversionFaultSystemSolution,
				"Solution cannot be loaded as an InversionFaultSystemSolution");
		return (InversionFaultSystemSolution)sol;
	}
	
	/**
	 * Load an AverageFaultSystemSolution from a zip file
	 * @param file
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static AverageFaultSystemSolution loadAvgInvSol(File file) throws IOException, DocumentException {
		FaultSystemSolution sol = loadSolAsApplicable(file);
		Preconditions.checkArgument(sol instanceof AverageFaultSystemSolution,
				"Solution cannot be loaded as an AverageFaultSystemSolution");
		return (AverageFaultSystemSolution)sol;
	}
	
	/*	******************************************
	 * 		FILE WRITING
	 *	******************************************/
	
	/**
	 * Writes a FaultSystemRupSet to a zip file
	 * @param rupSet
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static void writeRupSet(FaultSystemRupSet rupSet, File file) throws IOException {
		File tempDir = FileUtils.createTempDir();
		
		HashSet<String> zipFileNames = new HashSet<String>();
		
		toZipFile(rupSet, file, tempDir, zipFileNames);
	}
	
	/**
	 * Write an FaultSystemSolution to a zip file
	 * @param sol
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static void writeSol(FaultSystemSolution sol, File file) throws IOException {
		File tempDir = FileUtils.createTempDir();
		
		HashSet<String> zipFileNames = new HashSet<String>();
		
		toZipFile(sol, file, tempDir, zipFileNames);
	}
	
	/*	******************************************
	 * 		FILE READING UTIL METHODS
	 *	******************************************/
	
	/**
	 * Loads a rup set from the given zip file as the deepest possible subclass
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws DocumentException 
	 */
	private static FaultSystemRupSet loadRupSetAsApplicable(File file) throws ZipException, IOException, DocumentException {
		return loadRupSetAsApplicable(new ZipFile(file), null);
	}
	
	/**
	 * Loads a rup set from the given zip file as the deepest possible subclass
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	private static FaultSystemRupSet loadRupSetAsApplicable(ZipFile zip, Map<String, String> nameRemappings) throws IOException, DocumentException {
		ZipEntry magEntry = zip.getEntry(getRemappedName("mags.bin", nameRemappings));
		double[] mags = MatrixIO.doubleArrayFromInputStream(
				new BufferedInputStream(zip.getInputStream(magEntry)), magEntry.getSize());
		
		ZipEntry sectSlipsEntry = zip.getEntry(getRemappedName("sect_slips.bin", nameRemappings));
		double[] sectSlipRates;
		if (sectSlipsEntry != null)
			sectSlipRates = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(sectSlipsEntry)),
					sectSlipsEntry.getSize());
		else
			sectSlipRates = null;

		ZipEntry sectSlipStdDevsEntry = zip.getEntry(getRemappedName("sect_slips_std_dev.bin", nameRemappings));
		double[] sectSlipRateStdDevs;
		if (sectSlipStdDevsEntry != null)
			sectSlipRateStdDevs = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(sectSlipStdDevsEntry)),
					sectSlipStdDevsEntry.getSize());
		else
			sectSlipRateStdDevs = null;
		
		ZipEntry rakesEntry = zip.getEntry(getRemappedName("rakes.bin", nameRemappings));
		double[] rakes = MatrixIO.doubleArrayFromInputStream(
				new BufferedInputStream(zip.getInputStream(rakesEntry)), rakesEntry.getSize());
		
		ZipEntry rupAreasEntry = zip.getEntry(getRemappedName("rup_areas.bin", nameRemappings));
		double[] rupAreas;
		if (rupAreasEntry != null)
			rupAreas = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(rupAreasEntry)),
					rupAreasEntry.getSize());
		else
			rupAreas = null;
		
		ZipEntry rupLenghtsEntry = zip.getEntry(getRemappedName("rup_lengths.bin", nameRemappings));
		double[] rupLengths;
		if (rupLenghtsEntry != null)
			rupLengths = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(rupLenghtsEntry)),
					rupLenghtsEntry.getSize());
		else
			rupLengths = null;

		ZipEntry sectAreasEntry = zip.getEntry(getRemappedName("sect_areas.bin", nameRemappings));
		double[] sectAreas;
		if (sectAreasEntry != null)
			sectAreas = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(sectAreasEntry)),
					sectAreasEntry.getSize());
		else
			sectAreas = null;

		ZipEntry rupSectionsEntry = zip.getEntry(getRemappedName("rup_sections.bin", nameRemappings));
		List<List<Integer>> sectionForRups;
		if (rupSectionsEntry != null)
			sectionForRups = MatrixIO.intListListFromInputStream(
					new BufferedInputStream(zip.getInputStream(rupSectionsEntry)));
		else
			sectionForRups = null;
		
		String fsdRemappedName = getRemappedName("fault_sections.xml", nameRemappings);
		ZipEntry fsdEntry = zip.getEntry(fsdRemappedName);
		if (fsdEntry == null && fsdRemappedName.startsWith("FM")) {
			// might be a legacy compound solution before the bug fix
			// try removing the DM from the name
			int ind = fsdRemappedName.indexOf("fault_sections");
			// the -1 removes the underscore before fault
			String prefix = fsdRemappedName.substring(0, ind-1);
			prefix = prefix.substring(0, prefix.lastIndexOf("_"));
			fsdRemappedName = prefix+"_fault_sections.xml";
			fsdEntry = zip.getEntry(fsdRemappedName);
			if (fsdEntry != null)
				System.out.println("WARNING: using old non DM-specific fault_sections.xml file, " +
						"may have incorrect non reduced slip rates: "+fsdRemappedName);
		}
		Document doc = XMLUtils.loadDocument(
				new BufferedInputStream(zip.getInputStream(fsdEntry)));
		Element fsEl = doc.getRootElement().element(FaultSectionPrefData.XML_METADATA_NAME+"List");
		ArrayList<FaultSectionPrefData> faultSectionData = fsDataFromXML(fsEl);
		
		ZipEntry infoEntry = zip.getEntry(getRemappedName("info.txt", nameRemappings));
		String info = loadInfoFromEntry(zip, infoEntry);
		
		// IVFSRS specific. Try to load any IVFSRS specific files. Unfortunately a little messy to allow
		// for loading in legacy files
		
		ZipEntry invXMLEntry = zip.getEntry(getRemappedName("inv_rup_set_metadata.xml", nameRemappings));
		LogicTreeBranch branch = null;
		LaughTestFilter filter = null;
		if (invXMLEntry != null) {
			Document invDoc = XMLUtils.loadDocument(zip.getInputStream(invXMLEntry));
			Element invRoot = invDoc.getRootElement().element("InversionFaultSystemRupSet");
			
			Element branchEl = invRoot.element(LogicTreeBranch.XML_METADATA_NAME);
			if (branchEl != null)
				branch = LogicTreeBranch.fromXMLMetadata(branchEl);
			
			Element filterEl = invRoot.element(LaughTestFilter.XML_METADATA_NAME);
			if (branchEl != null)
				filter = LaughTestFilter.fromXMLMetadata(filterEl);
		}
		
		// try to load the logic tree branch via other means for legacy files
		if (branch == null && invXMLEntry == null) {
			ZipEntry rupSectionSlipModelEntry = zip.getEntry(getRemappedName("rup_sec_slip_type.txt", nameRemappings));
			SlipAlongRuptureModels slipModelType = null;
			if (rupSectionSlipModelEntry != null) {
				StringWriter writer = new StringWriter();
				IOUtils.copy(zip.getInputStream(rupSectionSlipModelEntry), writer);
				String slipModelName = writer.toString().trim();
				try {
					slipModelType = SlipAlongRuptureModels.valueOf(slipModelName);
				} catch (Exception e) {}
			}
			
			DeformationModels defModName = null;
			Attribute defModAtt = fsEl.attribute("defModName");
			try {
				if (defModAtt != null && !defModAtt.getValue().isEmpty())
					defModName = DeformationModels.valueOf(defModAtt.getValue());
			} catch (Exception e) {}
			FaultModels faultModel = null;
			Attribute faultModAtt = fsEl.attribute("faultModName");
			try {
				if (faultModAtt != null && !faultModAtt.getValue().isEmpty())
					faultModel = FaultModels.valueOf(faultModAtt.getValue());
			} catch (Exception e) {}
			if (faultModel == null && defModAtt != null) {
				if (defModName == null) {
					// hacks for loading in old files
					String defModText = defModAtt.getValue();
					if (defModText.contains("GEOLOGIC") && !defModText.contains("ABM"))
						defModName = DeformationModels.GEOLOGIC;
					else if (defModText.contains("NCAL"))
						defModName = DeformationModels.UCERF2_NCAL;
					else if (defModText.contains("ALLCAL"))
						defModName = DeformationModels.UCERF2_ALL;
					else if (defModText.contains("BAYAREA"))
						defModName = DeformationModels.UCERF2_BAYAREA;
				}
				
				if (defModName != null)
					faultModel = defModName.getApplicableFaultModels().get(0);
			}
			
			// we need at least a FM and Scaling Relationship to load this as an IVFSRS
			if (faultModel != null && slipModelType != null) {
				branch = LogicTreeBranch.fromValues(false, faultModel, defModName, slipModelType);
			}
		}
		
		FaultSystemRupSet rupSet = new FaultSystemRupSet(faultSectionData, sectSlipRates,
				sectSlipRateStdDevs, sectAreas, sectionForRups, mags, rakes, rupAreas, rupLengths, info);
		
		if (branch != null) {
			// it's an IVFSRS
			
			ZipEntry rupSlipsEntry = zip.getEntry(getRemappedName("rup_avg_slips.bin", nameRemappings));
			double[] rupAveSlips;
			if (rupSlipsEntry != null)
				rupAveSlips = MatrixIO.doubleArrayFromInputStream(
						new BufferedInputStream(zip.getInputStream(rupSlipsEntry)),
					rupSlipsEntry.getSize());
			else
				rupAveSlips = null;

			ZipEntry closeSectionsEntry = zip.getEntry(getRemappedName("close_sections.bin", nameRemappings));
			List<List<Integer>> closeSections;
			if (closeSectionsEntry != null)
				closeSections = MatrixIO.intListListFromInputStream(
						new BufferedInputStream(zip.getInputStream(closeSectionsEntry)));
			else
				closeSections = null;

			ZipEntry clusterRupsEntry = zip.getEntry(getRemappedName("cluster_rups.bin", nameRemappings));
			List<List<Integer>> clusterRups;
			if (clusterRupsEntry != null)
				clusterRups = MatrixIO.intListListFromInputStream(
						new BufferedInputStream(zip.getInputStream(clusterRupsEntry)));
			else
				clusterRups = null;
			
			ZipEntry clusterSectsEntry = zip.getEntry(getRemappedName("cluster_sects.bin", nameRemappings));
			List<List<Integer>> clusterSects;
			if (clusterSectsEntry != null)
				clusterSects = MatrixIO.intListListFromInputStream(
						new BufferedInputStream(zip.getInputStream(clusterSectsEntry)));
			else
				clusterSects = null;
			
			// maybe restore if we ever need it
//			// don't use remapping here - this is legacy and new files will never have it
//			ZipEntry rupSectionSlipsEntry = zip.getEntry("rup_sec_slips.bin");
//			List<double[]> rupSectionSlips;
//			if (rupSectionSlipsEntry != null)
//				rupSectionSlips = MatrixIO.doubleArraysListFromInputStream(
//						new BufferedInputStream(zip.getInputStream(rupSectionSlipsEntry)));
//			else
//				rupSectionSlips = null;
			
			return new InversionFaultSystemRupSet(rupSet, branch, filter, rupAveSlips, closeSections, clusterRups, clusterSects);
		}
		
		return rupSet;
	}
	
	public static ArrayList<FaultSectionPrefData> fsDataFromXML(Element el) {
		ArrayList<FaultSectionPrefData> list = new ArrayList<FaultSectionPrefData>();
		
		for (int i=0; i<el.elements().size(); i++) {
			Element subEl = el.element("i"+i);
			list.add(FaultSectionPrefData.fromXMLMetadata(subEl));
		}
		
		return list;
	}
	
	private static String loadInfoFromEntry(ZipFile zip, ZipEntry infoEntry) throws IOException {
		if (infoEntry != null) {
			StringBuilder text = new StringBuilder();
			String NL = System.getProperty("line.separator");
			Scanner scanner = new Scanner(
					new BufferedInputStream(zip.getInputStream(infoEntry)));
			try {
				while (scanner.hasNextLine()){
					text.append(scanner.nextLine() + NL);
				}
			}
			finally{
				scanner.close();
			}
			return text.toString();
		} else
			return null;
	}
	
	/**
	 * Loads a solution from the given zip file as the deepest possible subclass
	 * 
	 * @param file
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	private static FaultSystemSolution loadSolAsApplicable(File file) throws IOException, DocumentException {
		return loadSolAsApplicable(file, null);
	}
	
	/**
	 * Loads a solution from the given zip file as the deepest possible subclass
	 * 
	 * @param file
	 * @param nameRemappings
	 * @return
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	private static FaultSystemSolution loadSolAsApplicable(File file, Map<String, String> nameRemappings)
			throws IOException, DocumentException {
		ZipFile zip = new ZipFile(file);
		return loadSolAsApplicable(zip, nameRemappings);
	}
	
	/**
	 * Loads a solution from the given zip file as the deepest possible subclass
	 * 
	 * @param zip
	 * @param nameRemappings
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static FaultSystemSolution loadSolAsApplicable(ZipFile zip, Map<String, String> nameRemappings)
			throws IOException, DocumentException {
		// first load the rupture set
		FaultSystemRupSet rupSet = loadRupSetAsApplicable(zip, nameRemappings);
		
		// safe to use rupSet info string as we just loaded it from the same zip file
		String infoString = rupSet.getInfoString();
		
		// now load rates
		ZipEntry ratesEntry = zip.getEntry(getRemappedName("rates.bin", nameRemappings));
		double[] rates = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(ratesEntry)), ratesEntry.getSize());
		
		if (rupSet instanceof InversionFaultSystemRupSet) {
			// it's an IVFSS
			InversionFaultSystemRupSet invRupSet = (InversionFaultSystemRupSet)rupSet;
			
			ZipEntry invXMLEntry = zip.getEntry(getRemappedName("inv_sol_metadata.xml", nameRemappings));
			
			InversionConfiguration conf = null;
			Map<String, Double> energies = null;
			if (invXMLEntry != null) {
				// new file, we can load directly from XML
				Document invDoc = XMLUtils.loadDocument(zip.getInputStream(invXMLEntry));
				Element invRoot = invDoc.getRootElement().element("InversionFaultSystemSolution");
				
				Element confEl = invRoot.element(InversionConfiguration.XML_METADATA_NAME);
				if (confEl != null)
					conf = InversionConfiguration.fromXMLMetadata(confEl);
				
				Element energiesEl = invRoot.element("Energies");
				if (energiesEl != null) {
					energies = Maps.newHashMap();
					for (Element energyEl : XMLUtils.getSubElementsList(energiesEl)) {
						String type = energyEl.attributeValue("type");
						double value = Double.parseDouble(energyEl.attributeValue("value"));
						energies.put(type, value);
					}
				}
			} else {
				// legacy, do string parsing
				InversionFaultSystemSolution legacySol = new InversionFaultSystemSolution(invRupSet, infoString, rates);
				invRupSet.setLogicTreeBranch(legacySol.getLogicTreeBranch());
				conf = legacySol.getInversionConfiguration();
				energies = legacySol.getEnergies();
			}
			
			// now see if it's an average fault system solution
			String ratesPrefix = getRemappedRatesPrefix(nameRemappings);
			
			List<double[]> ratesList = loadIndSolRates(ratesPrefix, zip, nameRemappings);
			if (ratesList == null)
				// try legacy format
				ratesList = loadIndSolRates("sol_rates", zip, nameRemappings);
			if (ratesList != null)
				// it's an AverageFSS
				return new AverageFaultSystemSolution(invRupSet, ratesList, conf, energies);
			else
				// it's a regular IFSS
				return new InversionFaultSystemSolution(invRupSet, rates, conf, energies);
		}
		return new FaultSystemSolution(rupSet, rates);
	}
	
	private static List<double[]> loadIndSolRates(String ratesPrefix, ZipFile zip, Map<String, String> nameRemappings) throws IOException {
		int max_digits = 10;
		
		for (int digits=1; digits<=max_digits; digits++) {
			int c = 0;
			String ratesName = ratesPrefix+"_"+getPaddedNumStr(c++, digits)+".bin";
			ZipEntry entry = zip.getEntry(ratesName);
			if (entry != null) {
				// it's an average sol
				List<double[]> ratesList = Lists.newArrayList();
				
				while (true) {
					double[] rates = MatrixIO.doubleArrayFromInputStream(
							new BufferedInputStream(zip.getInputStream(entry)), entry.getSize());
					ratesList.add(rates);
					
					ratesName = ratesPrefix+"_"+getPaddedNumStr(c++, digits)+".bin";
					entry = zip.getEntry(ratesName);
					if (entry == null)
						break;
				}
				
				if (ratesList.size() > 1)
					return ratesList;
				else
					return null;
			}
		}
		return null;
	}
	
	/*	******************************************
	 * 		FILE WRITING UTIL METHODS
	 *	******************************************/
	
	private static void toZipFile(FaultSystemRupSet rupSet, File file, File tempDir, HashSet<String> zipFileNames) throws IOException {
		final boolean D = true;
		if (D) System.out.println("Saving rup set with "+rupSet.getNumRuptures()+" rups to: "+file.getAbsolutePath());
		writeRupSetFilesForZip(rupSet, tempDir, zipFileNames, null);
		
		if (D) System.out.println("Making zip file: "+file.getName());
		FileUtils.createZipFile(file.getAbsolutePath(), tempDir.getAbsolutePath(), zipFileNames);
		
		if (D) System.out.println("Deleting temp files");
		FileUtils.deleteRecursive(tempDir);
		
		if (D) System.out.println("Done saving!");
	}
	
	public static void writeRupSetFilesForZip(FaultSystemRupSet rupSet, File tempDir,
			HashSet<String> zipFileNames, Map<String, String> nameRemappings) throws IOException {
		// first save fault section data as XML
		if (D) System.out.println("Saving fault section xml");
		File fsdFile = new File(tempDir, getRemappedName("fault_sections.xml", nameRemappings));
		if (!zipFileNames.contains(fsdFile.getName())) {
			Document doc = XMLUtils.createDocumentWithRoot();
			Element root = doc.getRootElement();
			fsDataToXML(root, FaultSectionPrefData.XML_METADATA_NAME+"List", rupSet);
			XMLUtils.writeDocumentToFile(fsdFile, doc);
			zipFileNames.add(fsdFile.getName());
		}
		
		// write mags
		if (D) System.out.println("Saving mags");
		File magFile = new File(tempDir, getRemappedName("mags.bin", nameRemappings));
		if (!zipFileNames.contains(magFile.getName())) {
			MatrixIO.doubleArrayToFile(rupSet.getMagForAllRups(), magFile);
			zipFileNames.add(magFile.getName());
		}
		
		// write sect slips
		double[] sectSlipRates = rupSet.getSlipRateForAllSections();
		if (sectSlipRates != null) {
			if (D) System.out.println("Saving section slips");
			File sectSlipsFile = new File(tempDir, getRemappedName("sect_slips.bin", nameRemappings));
			if (!zipFileNames.contains(sectSlipsFile.getName())) {
				MatrixIO.doubleArrayToFile(sectSlipRates, sectSlipsFile);
				zipFileNames.add(sectSlipsFile.getName());
			}
		}
		
		// write sec slip std devs
		double[] sectSlipRateStdDevs = rupSet.getSlipRateStdDevForAllSections();
		if (sectSlipRateStdDevs != null) {
			if (D) System.out.println("Saving slip std devs");
			File sectSlipStdDevsFile = new File(tempDir, getRemappedName("sect_slips_std_dev.bin", nameRemappings));
			if (!zipFileNames.contains(sectSlipStdDevsFile.getName())) {
				MatrixIO.doubleArrayToFile(sectSlipRateStdDevs, sectSlipStdDevsFile);
				zipFileNames.add(sectSlipStdDevsFile.getName());
			}
		}
		
		// write rakes
		if (D) System.out.println("Saving rakes");
		File rakesFile = new File(tempDir, getRemappedName("rakes.bin", nameRemappings));
		if (!zipFileNames.contains(rakesFile.getName())) {
			MatrixIO.doubleArrayToFile(rupSet.getAveRakeForAllRups(), rakesFile);
			zipFileNames.add(rakesFile.getName());
		}
		
		// write rup areas
		if (D) System.out.println("Saving rup areas");
		File rupAreasFile = new File(tempDir, getRemappedName("rup_areas.bin", nameRemappings));
		if (!zipFileNames.contains(rupAreasFile.getName())) {
			MatrixIO.doubleArrayToFile(rupSet.getAreaForAllRups(), rupAreasFile);
			zipFileNames.add(rupAreasFile.getName());
		}
		
		// write rup areas
		if (rupSet.getLengthForAllRups() != null) {
			if (D) System.out.println("Saving rup lengths");
			File rupLengthsFile = new File(tempDir, getRemappedName("rup_lengths.bin", nameRemappings));
			if (!zipFileNames.contains(rupLengthsFile.getName())) {
				MatrixIO.doubleArrayToFile(rupSet.getLengthForAllRups(), rupLengthsFile);
				zipFileNames.add(rupLengthsFile.getName());
			}
		}
		
		// write sect areas
		double[] sectAreas = rupSet.getAreaForAllSections();
		if (sectAreas != null) {
			if (D) System.out.println("Saving sect areas");
			File sectAreasFile = new File(tempDir, getRemappedName("sect_areas.bin", nameRemappings));
			if (!zipFileNames.contains(sectAreasFile.getName())) {
				MatrixIO.doubleArrayToFile(sectAreas, sectAreasFile);
				zipFileNames.add(sectAreasFile.getName());
			}
		}
		
		// write sections for rups
		if (D) System.out.println("Saving rup sections");
		File sectionsForRupsFile = new File(tempDir, getRemappedName("rup_sections.bin", nameRemappings));
		if (!zipFileNames.contains(sectionsForRupsFile.getName())) {
			MatrixIO.intListListToFile(rupSet.getSectionIndicesForAllRups(), sectionsForRupsFile);
			zipFileNames.add(sectionsForRupsFile.getName());
		}
		
		String info = rupSet.getInfoString();
		if (info != null && !info.isEmpty()) {
			if (D) System.out.println("Saving info");
			File infoFile = new File(tempDir, getRemappedName("info.txt", nameRemappings));
			if (!zipFileNames.contains(infoFile.getName())) {
				FileWriter fw = new FileWriter(infoFile);
				fw.write(info+"\n");
				fw.close();
				zipFileNames.add(infoFile.getName());
			}
		}
		
		// InversionFaultSystemRupSet specific
		
		if (rupSet instanceof InversionFaultSystemRupSet) {
			if (D) System.out.println("Saving InversionFaultSystemRupSet specific data");
			InversionFaultSystemRupSet invRupSet = (InversionFaultSystemRupSet)rupSet;
			
			// save IVFSRS metadata
			if (D) System.out.println("Saving inversion rup set metadata xml");
			File invFile = new File(tempDir, getRemappedName("inv_rup_set_metadata.xml", nameRemappings));
			if (!zipFileNames.contains(invFile.getName())) {
				Document doc = XMLUtils.createDocumentWithRoot();
				Element root = doc.getRootElement();
				invRupSetDataToXML(root, invRupSet);
				XMLUtils.writeDocumentToFile(invFile, doc);
				zipFileNames.add(invFile.getName());
			}
			
			// write rup slips
			double[] rupAveSlips = invRupSet.getAveSlipForAllRups();
			if (rupAveSlips != null) {
				if (D) System.out.println("Saving rup avg slips");
				File rupSlipsFile = new File(tempDir, getRemappedName("rup_avg_slips.bin", nameRemappings));
				if (!zipFileNames.contains(rupSlipsFile.getName())) {
					MatrixIO.doubleArrayToFile(rupAveSlips, rupSlipsFile);
					zipFileNames.add(rupSlipsFile.getName());
				}
			}
			
			List<List<Integer>> closeSections = invRupSet.getCloseSectionsListList();
			if (closeSections != null) {
				// write close sections
				if (D) System.out.println("Saving close sections");
				File closeSectionsFile = new File(tempDir, getRemappedName("close_sections.bin", nameRemappings));
				if (!zipFileNames.contains(closeSectionsFile.getName())) {
					MatrixIO.intListListToFile(closeSections, closeSectionsFile);
					zipFileNames.add(closeSectionsFile.getName());
				}
			}
			
			if (invRupSet.getNumClusters() > 0) {
				List<List<Integer>> clusterRups = Lists.newArrayList();
				List<List<Integer>> clusterSects = Lists.newArrayList();
				
				for (int c=0; c<invRupSet.getNumClusters(); c++) {
					clusterRups.add(invRupSet.getRupturesForCluster(c));
					clusterSects.add(invRupSet.getSectionsForCluster(c));
				}
				
				// write close sections
				if (D) System.out.println("Saving cluster rups");
				File clusterRupsFile = new File(tempDir, getRemappedName("cluster_rups.bin", nameRemappings));
				if (!zipFileNames.contains(clusterRupsFile.getName())) {
					MatrixIO.intListListToFile(clusterRups, clusterRupsFile);
					zipFileNames.add(clusterRupsFile.getName());
				}
				
				
				// write close sections
				if (D) System.out.println("Saving cluster sects");
				File clusterSectsFile = new File(tempDir, getRemappedName("cluster_sects.bin", nameRemappings));
				if (!zipFileNames.contains(clusterSectsFile.getName())) {
					MatrixIO.intListListToFile(clusterSects, clusterSectsFile);
					zipFileNames.add(clusterSectsFile.getName());
				}
			}
		}
	}
	
	public static void fsDataToXML(Element parent, String elName, FaultSystemRupSet rupSet) {
		FaultModels fm = null;
		DeformationModels dm = null;
		if (rupSet instanceof InversionFaultSystemRupSet) {
			InversionFaultSystemRupSet invRupSet = (InversionFaultSystemRupSet)rupSet;
			fm = invRupSet.getFaultModel();
			dm = invRupSet.getDeformationModel();
		}
		fsDataToXML(parent, elName, fm, dm, rupSet.getFaultSectionDataList());
	}
	
	public static void fsDataToXML(Element parent, String elName,
			FaultModels fm, DeformationModels dm, List<FaultSectionPrefData> fsd) {
		Element el = parent.addElement(elName);
		
		if (dm != null)
			el.addAttribute("defModName", dm.name());
		if (fm != null)
			el.addAttribute("faultModName", fm.name());
		
		for (int i=0; i<fsd.size(); i++) {
			FaultSectionPrefData data = fsd.get(i);
			data.toXMLMetadata(el, "i"+i);
		}
	}
	
	private static void invRupSetDataToXML(Element root, InversionFaultSystemRupSet invRupSet) {
		Element el = root.addElement("InversionFaultSystemRupSet");
		
		// add LogicTreeBranch
		LogicTreeBranch branch = invRupSet.getLogicTreeBranch();
		if (branch != null)
			branch.toXMLMetadata(el);
		
		// add LaughTestFilter
		LaughTestFilter filter = invRupSet.getLaughTestFilter();
		if (filter != null)
			filter.toXMLMetadata(el);
	}
	
	private static String getRemappedName(String name, Map<String, String> nameRemappings) {
		if (nameRemappings == null)
			return name;
		return nameRemappings.get(name);
	}
	
	private static void toZipFile(FaultSystemSolution sol, File file, File tempDir, HashSet<String> zipFileNames) throws IOException {
		final boolean D = true;
		if (D) System.out.println("Saving solution with "+sol.getRupSet().getNumRuptures()+" rups to: "+file.getAbsolutePath());
		writeSolFilesForZip(sol, tempDir, zipFileNames, null);
		
		if (D) System.out.println("Making zip file: "+file.getName());
		FileUtils.createZipFile(file.getAbsolutePath(), tempDir.getAbsolutePath(), zipFileNames);
		
		if (D) System.out.println("Deleting temp files");
		FileUtils.deleteRecursive(tempDir);
		
		if (D) System.out.println("Done saving!");
	}
	
	public static void writeSolFilesForZip(FaultSystemSolution sol, File tempDir,
			HashSet<String> zipFileNames, Map<String, String> nameRemappings) throws IOException {
		// first save rup set files
		writeRupSetFilesForZip(sol.getRupSet(), tempDir, zipFileNames, nameRemappings);
		
		// write rates
		File ratesFile = new File(tempDir, getRemappedName("rates.bin", nameRemappings));
		MatrixIO.doubleArrayToFile(sol.getRateForAllRups(), ratesFile);
		zipFileNames.add(ratesFile.getName());
		
		// overwrite info string
		String info = sol.getInfoString();
		if (info != null && !info.isEmpty()) {
			if (D) System.out.println("Saving info");
			File infoFile = new File(tempDir, getRemappedName("info.txt", nameRemappings));
			if (!zipFileNames.contains(infoFile.getName())) {
				FileWriter fw = new FileWriter(infoFile);
				fw.write(info+"\n");
				fw.close();
				zipFileNames.add(infoFile.getName());
			}
		}
		
		// InversionFaultSystemSolution specific
		
		if (sol instanceof InversionFaultSystemSolution) {
			if (D) System.out.println("Saving InversionFaultSystemSolution specific data");
			InversionFaultSystemSolution invSol = (InversionFaultSystemSolution)sol;
			
			// TODO always save grid sources?
			GridSourceProvider gridSources = invSol.getGridSourceProvider();
			if (gridSources != null) {
				if (D) System.out.println("Saving grid sources to xml");
				File gridSourcesFile = new File(tempDir, "grid_sources.xml");
				if (!zipFileNames.contains(gridSourcesFile.getName())) {
					GridSourceFileReader.writeGriddedSeisFile(gridSourcesFile, gridSources);
					zipFileNames.add(gridSourcesFile.getName());
				}
			}
			
			// save IFSS metadata
			if (D) System.out.println("Saving inversion solution metadata xml");
			File invFile = new File(tempDir, getRemappedName("inv_sol_metadata.xml", nameRemappings));
			if (!zipFileNames.contains(invFile.getName())) {
				Document doc = XMLUtils.createDocumentWithRoot();
				Element root = doc.getRootElement();
				invSolDataToXML(root, invSol);
				XMLUtils.writeDocumentToFile(invFile, doc);
				zipFileNames.add(invFile.getName());
			}
			
			if (sol instanceof AverageFaultSystemSolution) {
				AverageFaultSystemSolution avgSol = (AverageFaultSystemSolution)sol;
				int numSols = avgSol.getNumSolutions();
				
				if (D) System.out.println("Saving AverageFaultSystemSolution specific data for "+numSols+" solutions");
				
				String ratesPrefix = getRemappedRatesPrefix(nameRemappings);
				
				int digits = new String(""+(numSols-1)).length();
				for (int s=0; s<numSols; s++) {
					double[] rates = avgSol.getRates(s);
					String rateStr = getPaddedNumStr(s, digits);
					File rateSubFile = new File(tempDir, ratesPrefix+"_"+rateStr+".bin");
					MatrixIO.doubleArrayToFile(rates, rateSubFile);
					zipFileNames.add(rateSubFile.getName());
				}
			}
		}
	}
	
	private static void invSolDataToXML(Element root, InversionFaultSystemSolution invSol) {
		Element el = root.addElement("InversionFaultSystemSolution");
		
		// add InversionConfiguration
		InversionConfiguration conf = invSol.getInversionConfiguration();
		if (conf != null)
			conf.toXMLMetadata(el);
		
		// add LaughTestFilter
		Map<String, Double> energies = invSol.getEnergies();
		if (energies != null && !energies.isEmpty()) {
			Element energiesEl = el.addElement("Energies");
			for (String type : energies.keySet()) {
				double energy = energies.get(type);
				Element energyEl = energiesEl.addElement("Energy");
				energyEl.addAttribute("type", type);
				energyEl.addAttribute("value", energy+"");
			}
		}
	}
	
	private static String getRemappedRatesPrefix(Map<String, String> nameRemappings) {
		String ratesPrefix = getRemappedName("rates.bin", nameRemappings);
		ratesPrefix = ratesPrefix.substring(0, ratesPrefix.indexOf(".bin"));
		return ratesPrefix;
	}
	
	private static String getPaddedNumStr(int num, int digits) {
		String str = num+"";
		while (str.length() < digits)
			str = "0"+str;
		return str;
	}

}
