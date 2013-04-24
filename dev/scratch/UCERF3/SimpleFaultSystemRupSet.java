package scratch.UCERF3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;

/**
 * This is a simple (array based) implementation of a {@link FaultSystemRupSet}. All data
 * is set in the constructors, and cannot be changed.
 * 
 * <br><br>The benefit of this implementation is full saving/loading to/from XML files. 
 * 
 * @author Kevin Milner
 *
 */
public class SimpleFaultSystemRupSet extends FaultSystemRupSet implements XMLSaveable {
	
	public static final boolean D = false;
	
	public static final String XML_METADATA_NAME = "SimpleFaultSystemRupSet";
	public static final String DEF_MODEL_XML_NAME = FaultSectionPrefData.XML_METADATA_NAME+"List";
	
	private List<FaultSectionPrefData> faultSectionData;
	private double[] mags;
	private double[] rupAveSlips;
	private SlipAlongRuptureModels slipModelType;
//	private List<double[]> rupSectionSlips;
	private double[] sectSlipRates;
	private double[] sectSlipRateStdDevs;
	private double[] rakes;
	private double[] rupAreas;
	private double[] sectAreas;
	private List<List<Integer>> sectionForRups;
	private String info;
	private List<List<Integer>> closeSections;
	
	// for clusters
	private List<List<Integer>> clusterRups;
	private List<List<Integer>> clusterSects;
	
	private DeformationModels defModName;
	private FaultModels faultModel;
	/**
	 * This converts any FaultSytemRupSet into a SimpleFaultSystemRupSet, which allows it
	 * to be saved to a file. If the given rupSet is already a SimpleFaultSystemRupSet, 
	 * it is simiply casted and returned.
	 * 
	 * @param rupSet
	 * @return
	 */
	public static SimpleFaultSystemRupSet toSimple(FaultSystemRupSet rupSet) {
		// if it's already a SimpleFaultSystemRupSet, just return that
		if (rupSet instanceof SimpleFaultSystemRupSet)
			return (SimpleFaultSystemRupSet)rupSet;
		return new SimpleFaultSystemRupSet(rupSet);
	}
	
	/**
	 * This creates a SimpleFaultSystemRupSet from any arbitrary {@link FaultSystemRupSet} implementation
	 * @param rupSet
	 */
	public SimpleFaultSystemRupSet(FaultSystemRupSet rupSet) {
		this(rupSet.getFaultSectionDataList(), rupSet.getMagForAllRups(), rupSet.getAveSlipForAllRups(),
				null, rupSet.getSlipAlongRuptureModel(),
				rupSet.getSlipRateForAllSections(), rupSet.getSlipRateStdDevForAllSections(),
				rupSet.getAveRakeForAllRups(), rupSet.getAreaForAllRups(), rupSet.getAreaForAllSections(),
				rupSet.getSectionIndicesForAllRups(), rupSet.getInfoString(),
				rupSet.getCloseSectionsListList(), rupSet.getFaultModel(), rupSet.getDeformationModel());
		if (rupSet.isClusterBased()) {
			clusterRups = new ArrayList<List<Integer>>();
			clusterSects = new ArrayList<List<Integer>>();
			for (int i=0; i<rupSet.getNumClusters(); i++) {
				clusterRups.add(rupSet.getRupturesForCluster(i));
				clusterSects.add(rupSet.getSectionsForCluster(i));
			}
		}
		rupSectionSlipsCache = rupSet.rupSectionSlipsCache;
	}
	
	/**
	 * Creates a non-cluster based rupture set.
	 * 
	 * @param faultSectionData cannot be null
	 * @param mags cannot be null
	 * @param rupAveSlips can be null
	 * @param rupSectionSlips can be null
	 * @param sectSlipRates can be null
	 * @param sectSlipRateStdDevs can be null
	 * @param rakes cannot be null
	 * @param rupAreas can be null
	 * @param sectAreas can be null
	 * @param sectionForRups cannot be null
	 * @param info can be null
	 * @param closeSections can be null
	 */
	public SimpleFaultSystemRupSet(
			List<FaultSectionPrefData> faultSectionData, 
			double[] mags,
			double[] rupAveSlips,
			List<double[]> rupSectionSlips,
			SlipAlongRuptureModels slipModel,
			double[] sectSlipRates,
			double[] sectSlipRateStdDevs,
			double[] rakes,
			double[] rupAreas,
			double[] sectAreas,
			List<List<Integer>> sectionForRups,
			String info,
			List<List<Integer>> closeSections,
			FaultModels faultModel,
			DeformationModels defModName) {
		this(faultSectionData, mags, rupAveSlips, rupSectionSlips, slipModel, sectSlipRates, sectSlipRateStdDevs, rakes,
				rupAreas, sectAreas, sectionForRups, info, closeSections, faultModel, defModName, null, null);
	}
	
	/**
	 * Creates an (optionally) cluster based fault system rupture set.
	 * 
	 * @param faultSectionData cannot be null
	 * @param mags cannot be null
	 * @param rupAveSlips can be null
	 * @param rupSectionSlips can be null
	 * @param sectSlipRates can be null
	 * @param sectSlipRateStdDevs can be null
	 * @param rakes cannot be null
	 * @param rupAreas can be null
	 * @param sectAreas can be null
	 * @param sectionForRups cannot be null
	 * @param info can be null
	 * @param closeSections can be null
	 * @param clusterRups can be null
	 * @param clusterSects can be null
	 */
	public SimpleFaultSystemRupSet(
			List<FaultSectionPrefData> faultSectionData, 
			double[] mags,
			double[] rupAveSlips,
			List<double[]> rupSectionSlips,
			SlipAlongRuptureModels slipModelType,
			double[] sectSlipRates,
			double[] sectSlipRateStdDevs,
			double[] rakes,
			double[] rupAreas,
			double[] sectAreas,
			List<List<Integer>> sectionForRups,
			String info,
			List<List<Integer>> closeSections,
			FaultModels faultModel,
			DeformationModels defModName,
			List<List<Integer>> clusterRups,
			List<List<Integer>> clusterSects) {
		int numRups = mags.length;
		int numSects = faultSectionData.size();
		
		// cannot be null
		this.faultSectionData = faultSectionData;
		
		// cannot be null
		this.mags = mags;
		
		// can be null
		Preconditions.checkArgument(rupAveSlips == null
				|| rupAveSlips.length == numRups, "rupAveSlips sizes inconsistent!");
		this.rupAveSlips = rupAveSlips;
		
		// can be null
		Preconditions.checkArgument(rupSectionSlips == null
				|| rupSectionSlips.size() == numRups, "rupSectionSlips sizes inconsistent!");
		if (rupSectionSlips != null) {
			for (int rupIndex=0; rupIndex<numRups; rupIndex++)
				rupSectionSlipsCache.put(rupIndex, rupSectionSlips.get(rupIndex));
		}
		this.slipModelType = slipModelType;
		
		// can be null
		Preconditions.checkArgument(sectSlipRates == null
				|| sectSlipRates.length == numSects, "array sizes inconsistent!");
		this.sectSlipRates = sectSlipRates;
		
		// can be null
		Preconditions.checkArgument(sectSlipRateStdDevs == null
				|| sectSlipRateStdDevs.length == numSects, "array sizes inconsistent!");
		this.sectSlipRateStdDevs = sectSlipRateStdDevs;
		
		// cannot be null
		Preconditions.checkArgument(rakes.length == numRups, "array sizes inconsistent!");
		this.rakes = rakes;
		
		// can be null
		Preconditions.checkArgument(rupAreas == null ||
				rupAreas.length == numRups, "array sizes inconsistent!");
		this.rupAreas = rupAreas;
		
		// can be null
		Preconditions.checkArgument(sectAreas == null ||
				sectAreas.length == numSects, "array sizes inconsistent!");
		this.sectAreas = sectAreas;
		
		// cannot be null
		Preconditions.checkArgument(sectionForRups.size() == numRups, "array sizes inconsistent!");
		this.sectionForRups = sectionForRups;
		
		// can be null
		this.info = info;
		
		// can be null
		Preconditions.checkArgument(closeSections == null || closeSections.size() == numSects,
		"close sub section size doesn't match number of sections!");
		this.closeSections = closeSections;
		
		// can be null
		this.defModName = defModName;
		
		// can be null
		this.faultModel = faultModel;
		
		// can be null
		this.clusterRups = clusterRups;
		// can be null
		this.clusterSects = clusterSects;
	}

	@Override
	public int getNumRuptures() {
		return mags.length;
	}

	@Override
	public int getNumSections() {
		return faultSectionData.size();
	}
	
	@Override
	public List<List<Integer>> getSectionIndicesForAllRups() {
		return sectionForRups;
	}

	@Override
	public List<Integer> getSectionsIndicesForRup(int rupIndex) {
		return sectionForRups.get(rupIndex);
	}
	
	public void setMagForallRups(double[] mags) {
		Preconditions.checkArgument(mags.length == getNumRuptures());
		this.mags = mags;
	}

	@Override
	public double[] getMagForAllRups() {
		return mags;
	}

	@Override
	public double getMagForRup(int rupIndex) {
		return mags[rupIndex];
	}

	@Override
	public double[] getAveSlipForAllRups() {
		return rupAveSlips;
	}

	@Override
	public double getAveSlipForRup(int rupIndex) {
		return rupAveSlips[rupIndex];
	}
	
	@Override
	public double[] getAveRakeForAllRups() {
		return rakes;
	}

	@Override
	public double getAveRakeForRup(int rupIndex) {
		return rakes[rupIndex];
	}
	
	@Override
	public double[] getAreaForAllRups() {
		return rupAreas;
	}


	@Override
	public double getAreaForRup(int rupIndex) {
		return rupAreas[rupIndex];
	}

	@Override
	public double[] getAreaForAllSections() {
		return sectAreas;
	}
	
	@Override
	public double getAreaForSection(int sectIndex) {
		return sectAreas[sectIndex];
	}

	@Override
	public List<FaultSectionPrefData> getFaultSectionDataList() {
		return faultSectionData;
	}

	@Override
	public FaultSectionPrefData getFaultSectionData(int sectIndex) {
		return faultSectionData.get(sectIndex);
	}
	
	@Override
	public List<FaultSectionPrefData> getFaultSectionDataForRupture(int rupIndex) {
		List<Integer> inds = getSectionsIndicesForRup(rupIndex);
		ArrayList<FaultSectionPrefData> datas = new ArrayList<FaultSectionPrefData>();
		for (int ind : inds)
			datas.add(getFaultSectionData(ind));
		return datas;
	}

	@Override
	public double getSlipRateForSection(int sectIndex) {
		return sectSlipRates[sectIndex];
	}

	@Override
	public double[] getSlipRateForAllSections() {
		return sectSlipRates;
	}

	@Override
	public double getSlipRateStdDevForSection(int sectIndex) {
		return sectSlipRateStdDevs[sectIndex];
	}

	@Override
	public double[] getSlipRateStdDevForAllSections() {
		return sectSlipRateStdDevs;
	}

	@Override
	public String getInfoString() {
		return info;
	}
	
	public void setInfoString(String info) {
		this.info = info;
	}
	
	/**
	 * This fetches a list of all of the close sections to this section, as defined by the rupture set.
	 * @param sectIndex index of the section to retrieve
	 * @return close sections, or null if not defined
	 */
	public List<Integer> getCloseSectionsList(int sectIndex) {
		if (closeSections == null)
			return null;
		return closeSections.get(sectIndex);
	}

	/**
	 * This returns a list of lists of close sections for each section.
	 * @return list of all close sections, or null if not defined
	 */
	public List<List<Integer>> getCloseSectionsListList() {
		return closeSections;
	}
	
	/**
	 * 
	 * @return the number of clusters, or 0 if not a cluster based model
	 */
	public int getNumClusters() {
		if (clusterRups == null)
			return 0;
		return clusterRups.size();
	}

	@Override
	public int getNumRupturesForCluster(int index) {
		return clusterRups.get(index).size();
	}

	@Override
	public List<Integer> getRupturesForCluster(int index)
			throws IndexOutOfBoundsException {
		return clusterRups.get(index);
	}
	
	@Override
	public List<Integer> getSectionsForCluster(int index) {
		return clusterSects.get(index);
	}
	
	@Override
	public DeformationModels getDeformationModel() {
		return defModName;
	}
	
	@Override
	public FaultModels getFaultModel() {
		return faultModel;
	}
	
	private static void listDoubleArrayToXML(Element root, List<double[]> list, String elName) {
		Element el = root.addElement(elName);
		
		int sizes[] = new int[list.size()];
		int totSize = 0;
		for (int i=0; i<list.size(); i++) {
			int size = list.get(i).length;
			totSize += size;
			sizes[i] = size;
		}
		
		XMLUtils.intArrayToXML(el, sizes, "sizes");
		
		double data[] = new double[totSize];
		int cnt = 0;
		for (double[] vals : list) {
			for (double val : vals) {
				data[cnt++] = val;
			}
		}
		
		Preconditions.checkState(cnt == totSize, "count not equal to totSize after dat array population!");
		XMLUtils.doubleArrayToXML(el, data, "data");
	}
	
	private static ArrayList<double[]> listDoubleArrayFromXML(Element el) {
		ArrayList<double[]> list = new ArrayList<double[]>();
		
		int[] sizes = XMLUtils.intArrayFromXML(el.element("sizes"));
		
		double[] data = XMLUtils.doubleArrayFromXML(el.element("data"));
		
		int ind = 0;
		for (int size : sizes) {
			int newInd = ind + size;
			list.add(Arrays.copyOfRange(data, ind, newInd));
			ind = newInd;
		}
		
		return list;
	}
	
	protected static void intListArrayToXML(Element root, List<? extends List<Integer>> lists, String elName) {
		Element el = root.addElement(elName);
		
		int sizes[] = new int[lists.size()];
		int totSize = 0;
		for (int i=0; i<lists.size(); i++) {
			int size = lists.get(i).size();
			totSize += size;
			sizes[i] = size;
		}
		
		XMLUtils.intArrayToXML(el, sizes, "sizes");
		
		int data[] = new int[totSize];
		int cnt = 0;
		for (List<Integer> list : lists) {
			for (int val : list) {
				data[cnt++] = val;
			}
		}
		
		Preconditions.checkState(cnt == totSize, "count not equal to totSize after dat array population!");
		XMLUtils.intArrayToXML(el, data, "data");
	}
	
	protected static List<List<Integer>> intListArrayFromXML(Element el) {
		ArrayList<List<Integer>> lists = new ArrayList<List<Integer>>();
		
		int[] sizes = XMLUtils.intArrayFromXML(el.element("sizes"));
		
		int[] data = XMLUtils.intArrayFromXML(el.element("data"));
		
		int ind = 0;
		for (int size : sizes) {
			int newInd = ind + size;
			lists.add(Arrays.asList(ArrayUtils.toObject(Arrays.copyOfRange(data, ind, newInd))));
			ind = newInd;
		}
		
		return lists;
	}
	
	public static void fsDataToXML(Element parent, List<FaultSectionPrefData> list,
			String elName, FaultModels faultModel, DeformationModels defModName) {
		Element el = parent.addElement(elName);
		if (defModName != null)
			el.addAttribute("defModName", defModName.name());
		if (faultModel != null)
			el.addAttribute("faultModName", faultModel.name());
		
		for (int i=0; i<list.size(); i++) {
			FaultSectionPrefData data = list.get(i);
			data.toXMLMetadata(el, "i"+i);
		}
	}
	
	public static ArrayList<FaultSectionPrefData> fsDataFromXML(Element el) {
		ArrayList<FaultSectionPrefData> list = new ArrayList<FaultSectionPrefData>();
		
		for (int i=0; i<el.elements().size(); i++) {
			Element subEl = el.element("i"+i);
			list.add(FaultSectionPrefData.fromXMLMetadata(subEl));
		}
		
		return list;
	}

	@Override
	@Deprecated
	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		el.addAttribute("numRups", getNumRuptures()+"");
		el.addAttribute("numSects", getNumSections()+"");
		if (getInfoString() != null)
			el.addAttribute("info", getInfoString());
		if (getSlipAlongRuptureModel() != null)
			el.addAttribute("slipAlongRuptureModel", getSlipAlongRuptureModel().name());
		
		XMLUtils.doubleArrayToXML(el, mags, "mags");
		if (rupAveSlips != null)
			XMLUtils.doubleArrayToXML(el, rupAveSlips, "rupAveSlips");
		if (sectSlipRates != null)
			XMLUtils.doubleArrayToXML(el, sectSlipRates, "sectSlipRates");
		if (sectSlipRateStdDevs != null)
			XMLUtils.doubleArrayToXML(el, sectSlipRateStdDevs, "sectSlipRateStdDevs");
		XMLUtils.doubleArrayToXML(el, rakes, "rakes");
		if (rupAreas != null)
			XMLUtils.doubleArrayToXML(el, rupAreas, "rupAreas");
		if (sectAreas != null)
			XMLUtils.doubleArrayToXML(el, sectAreas, "sectAreas");
		intListArrayToXML(el, sectionForRups, "sectionForRups");
		if (closeSections != null)
			intListArrayToXML(el, closeSections, "closeSections");
		
		fsDataToXML(el, faultSectionData, DEF_MODEL_XML_NAME, faultModel, defModName);
		
		if (isClusterBased()) {
			el.addAttribute("numClusters", getNumClusters()+"");
			
			intListArrayToXML(el, clusterRups, "clusterRups");
			intListArrayToXML(el, clusterSects, "clusterSects");
		}
		
		return el;
	}
	
	public static SimpleFaultSystemRupSet fromXMLMetadata(Element rupSetEl) {
		int numRups = Integer.parseInt(rupSetEl.attributeValue("numRups"));
		int numSects = Integer.parseInt(rupSetEl.attributeValue("numSects"));
		
		if (D) System.out.println("Loading from XML for numRups="+numRups+" and numSects="+numSects);
		
		SlipAlongRuptureModels slipModelType = null;
		Attribute slipModelAtt = rupSetEl.attribute("slipAlongRuptureModel");
		if (slipModelAtt != null)
			slipModelType = SlipAlongRuptureModels.valueOf(slipModelAtt.getStringValue());
		
		Attribute infoAtt = rupSetEl.attribute("info");
		String info;
		if (infoAtt != null)
			info = infoAtt.getValue();
		else
			info = null;
		
		if (D) System.out.println("Loading mags");
		double[] mags = XMLUtils.doubleArrayFromXML(rupSetEl.element("mags"));
		
		if (D) System.out.println("Loading rupAveSlips");
		double[] rupAveSlips;
		Element rupAveSlipsEl = rupSetEl.element("rupAveSlips");
		if (rupAveSlipsEl != null)
			rupAveSlips = XMLUtils.doubleArrayFromXML(rupAveSlipsEl);
		else
			rupAveSlips = null;
		
		if (D) System.out.println("Loading rupSectionSlips");
		List<double[]> rupSectionSlips;
		Element rupSectionSlipsEl = rupSetEl.element("rupSectionSlips");
		if (rupSectionSlipsEl != null)
			rupSectionSlips = listDoubleArrayFromXML(rupSectionSlipsEl);
		else
			rupSectionSlips = null;
		
		if (D) System.out.println("Loading sectSlipRates");
		double[] sectSlipRates;
		Element sectSlipRatesEl = rupSetEl.element("sectSlipRates");
		if (sectSlipRatesEl != null)
			sectSlipRates = XMLUtils.doubleArrayFromXML(sectSlipRatesEl);
		else
			sectSlipRates = null;
		
		if (D) System.out.println("Loading sectSlipRates");
		double[] sectSlipRateStdDevs;
		Element sectSlipRateStdDevsEl = rupSetEl.element("sectSlipRateStdDevs");
		if (sectSlipRateStdDevsEl != null)
			sectSlipRateStdDevs = XMLUtils.doubleArrayFromXML(sectSlipRateStdDevsEl);
		else
			sectSlipRateStdDevs = null;
		
		if (D) System.out.println("Loading rakes");
		double[] rakes = XMLUtils.doubleArrayFromXML(rupSetEl.element("rakes"));
		
		if (D) System.out.println("Loading rupAreas");
		double[] rupAreas;
		Element rupAreasEl = rupSetEl.element("rupAreas");
		if (rupAreasEl != null)
			rupAreas = XMLUtils.doubleArrayFromXML(rupAreasEl);
		else
			rupAreas = null;
		
		if (D) System.out.println("Loading sectAreas");
		double[] sectAreas;
		Element sectAreasEl = rupSetEl.element("sectAreas");
		if (sectAreasEl != null)
			sectAreas = XMLUtils.doubleArrayFromXML(sectAreasEl);
		else
			sectAreas = null;
		
		if (D) System.out.println("Loading sectionForRups");
		List<List<Integer>> sectionForRups = intListArrayFromXML(rupSetEl.element("sectionForRups"));
		
		List<List<Integer>> closeSections = null;
		Element closeSectionsEl = rupSetEl.element("closeSections");
		if (closeSectionsEl != null) {
			if (D) System.out.println("Loading closeSections");
			closeSections = intListArrayFromXML(closeSectionsEl);
		}
		
		if (D) System.out.println("Loading faultSectionData");
		Element fsEl = rupSetEl.element(FaultSectionPrefData.XML_METADATA_NAME+"List");
		ArrayList<FaultSectionPrefData> faultSectionData =
			fsDataFromXML(fsEl);
		DeformationModels defModName = null;
		Attribute defModAtt = fsEl.attribute("defModName");
		try {
			if (defModAtt != null && !defModAtt.getValue().isEmpty())
				defModName = DeformationModels.valueOf(defModAtt.getValue());
		} catch (Exception e) {
			System.err.println("Warning: unknown DefModeName: "+defModAtt.getValue());
		}
		FaultModels faultModel = null;
		Attribute faultModAtt = fsEl.attribute("faultModName");
		try {
			if (faultModAtt != null && !faultModAtt.getValue().isEmpty())
				faultModel = FaultModels.valueOf(faultModAtt.getValue());
		} catch (Exception e) {
			System.err.println("Warning: unknown FaultModels: "+faultModAtt.getValue());
		}
		
		List<List<Integer>> clusterRups = null;
		List<List<Integer>> clusterSects = null;
		Element clusterRupsEl = rupSetEl.element("clusterRups");
		if (clusterRupsEl != null) {
			// it is cluster based!
			if (D) System.out.println("Loading clusterRups");
			clusterRups = intListArrayFromXML(clusterRupsEl);
			clusterSects = intListArrayFromXML(rupSetEl.element("clusterSects"));
		}
		
		SimpleFaultSystemRupSet simple = new SimpleFaultSystemRupSet(
				faultSectionData, mags, rupAveSlips, rupSectionSlips, slipModelType, sectSlipRates, sectSlipRateStdDevs, rakes,
				rupAreas, sectAreas, sectionForRups, info, closeSections, faultModel, defModName, clusterRups, clusterSects);
		return simple;
	}
	
	@Deprecated
	public void toXMLFile(File file) throws IOException {
		XMLUtils.writeObjectToXMLAsRoot(this, file);
	}
	
	public static SimpleFaultSystemRupSet fromXMLFile(File file) throws MalformedURLException, DocumentException {
		Document doc = XMLUtils.loadDocument(file);
		Element rupSetEl = doc.getRootElement().element(XML_METADATA_NAME);
		return fromXMLMetadata(rupSetEl);
	}
	
	public static void toZipFile(FaultSystemRupSet rupSet, File file) throws IOException {
		toSimple(rupSet).toZipFile(file);
	}
	
	public void toZipFile(File file) throws IOException {
		File tempDir = FileUtils.createTempDir();
		
		HashSet<String> zipFileNames = new HashSet<String>();
		
		toZipFile(file, tempDir, zipFileNames);
	}
	
	private static String getRemappedName(String name, Map<String, String> nameRemappings) {
		if (nameRemappings == null)
			return name;
		return nameRemappings.get(name);
	}
	
	void writeFilesForZip(File tempDir, HashSet<String> zipFileNames, Map<String, String> nameRemappings) throws IOException {
		// first save fault section data as XML
		if (D) System.out.println("Saving fault section xml");
		File fsdFile = new File(tempDir, getRemappedName("fault_sections.xml", nameRemappings));
		if (!zipFileNames.contains(fsdFile.getName())) {
			Document doc = XMLUtils.createDocumentWithRoot();
			Element root = doc.getRootElement();
			fsDataToXML(root, faultSectionData, FaultSectionPrefData.XML_METADATA_NAME+"List", faultModel, defModName);
			XMLUtils.writeDocumentToFile(fsdFile, doc);
			zipFileNames.add(fsdFile.getName());
		}
		
		// write mags
		if (D) System.out.println("Saving mags");
		File magFile = new File(tempDir, getRemappedName("mags.bin", nameRemappings));
		if (!zipFileNames.contains(magFile.getName())) {
			MatrixIO.doubleArrayToFile(mags, magFile);
			zipFileNames.add(magFile.getName());
		}
		
		// write rup slips
		if (rupAveSlips != null) {
			if (D) System.out.println("Saving rup avg slips");
			File rupSlipsFile = new File(tempDir, getRemappedName("rup_avg_slips.bin", nameRemappings));
			if (!zipFileNames.contains(rupSlipsFile.getName())) {
				MatrixIO.doubleArrayToFile(rupAveSlips, rupSlipsFile);
				zipFileNames.add(rupSlipsFile.getName());
			}
		}
		
		// write rup section slips
		if (getSlipAlongRuptureModel() != null) {
			if (D) System.out.println("Saving rup sec slips type");
			File rupSectionSlipsFile = new File(tempDir, getRemappedName("rup_sec_slip_type.txt", nameRemappings));
			if (!zipFileNames.contains(rupSectionSlipsFile.getName())) {
				FileWriter fw = new FileWriter(rupSectionSlipsFile);
				fw.write(getSlipAlongRuptureModel().name());
				fw.close();
				zipFileNames.add(rupSectionSlipsFile.getName());
			}
		}
		
		// write sect slips
		if (sectSlipRates != null) {
			if (D) System.out.println("Saving section slips");
			File sectSlipsFile = new File(tempDir, getRemappedName("sect_slips.bin", nameRemappings));
			if (!zipFileNames.contains(sectSlipsFile.getName())) {
				MatrixIO.doubleArrayToFile(sectSlipRates, sectSlipsFile);
				zipFileNames.add(sectSlipsFile.getName());
			}
		}
		
		if (sectSlipRateStdDevs != null) {
			// write sec slip std devs
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
			MatrixIO.doubleArrayToFile(rakes, rakesFile);
			zipFileNames.add(rakesFile.getName());
		}
		
		// write rup areas
		if (D) System.out.println("Saving rup areas");
		File rupAreasFile = new File(tempDir, getRemappedName("rup_areas.bin", nameRemappings));
		if (!zipFileNames.contains(rupAreasFile.getName())) {
			MatrixIO.doubleArrayToFile(rupAreas, rupAreasFile);
			zipFileNames.add(rupAreasFile.getName());
		}
		
		// write sect areas
		if (D) System.out.println("Saving sect areas");
		File sectAreasFile = new File(tempDir, getRemappedName("sect_areas.bin", nameRemappings));
		if (!zipFileNames.contains(sectAreasFile.getName())) {
			MatrixIO.doubleArrayToFile(sectAreas, sectAreasFile);
			zipFileNames.add(sectAreasFile.getName());
		}
		
		// write sections for rups
		if (D) System.out.println("Saving rup sections");
		File sectionsForRupsFile = new File(tempDir, getRemappedName("rup_sections.bin", nameRemappings));
		if (!zipFileNames.contains(sectionsForRupsFile.getName())) {
			MatrixIO.intListListToFile(sectionForRups, sectionsForRupsFile);
			zipFileNames.add(sectionsForRupsFile.getName());
		}
		
		if (closeSections != null) {
			// write close sections
			if (D) System.out.println("Saving close sections");
			File closeSectionsFile = new File(tempDir, getRemappedName("close_sections.bin", nameRemappings));
			if (!zipFileNames.contains(closeSectionsFile.getName())) {
				MatrixIO.intListListToFile(closeSections, closeSectionsFile);
				zipFileNames.add(closeSectionsFile.getName());
			}
		}
		
		if (clusterRups != null) {
			// write close sections
			if (D) System.out.println("Saving cluster rups");
			File clusterRupsFile = new File(tempDir, getRemappedName("cluster_rups.bin", nameRemappings));
			if (!zipFileNames.contains(clusterRupsFile.getName())) {
				MatrixIO.intListListToFile(clusterRups, clusterRupsFile);
				zipFileNames.add(clusterRupsFile.getName());
			}
		}
		
		if (clusterSects != null) {
			// write close sections
			if (D) System.out.println("Saving cluster sects");
			File clusterSectsFile = new File(tempDir, getRemappedName("cluster_sects.bin", nameRemappings));
			if (!zipFileNames.contains(clusterSectsFile.getName())) {
				MatrixIO.intListListToFile(clusterSects, clusterSectsFile);
				zipFileNames.add(clusterSectsFile.getName());
			}
		}
		
		String info = getInfoString();
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
	}
	
	protected void toZipFile(File file, File tempDir, HashSet<String> zipFileNames) throws IOException {
		final boolean D = true;
		if (D) System.out.println("Saving rup set with "+getNumRuptures()+" rups to: "+file.getAbsolutePath());
		writeFilesForZip(tempDir, zipFileNames, null);
		
		if (D) System.out.println("Making zip file: "+file.getName());
		FileUtils.createZipFile(file.getAbsolutePath(), tempDir.getAbsolutePath(), zipFileNames);
		
		if (D) System.out.println("Deleting temp files");
		FileUtils.deleteRecursive(tempDir);
		
		if (D) System.out.println("Done saving!");
	}
	
	public static SimpleFaultSystemRupSet fromZipFile(File file) throws ZipException, IOException, DocumentException {
		ZipFile zip = new ZipFile(file);
		
		return fromZipFile(zip, null);
	}
	
	public static SimpleFaultSystemRupSet fromZipFile(ZipFile zip, Map<String, String> nameRemappings)
			throws ZipException, IOException, DocumentException {
		
		ZipEntry magEntry = zip.getEntry(getRemappedName("mags.bin", nameRemappings));
		double[] mags = MatrixIO.doubleArrayFromInputStream(
				new BufferedInputStream(zip.getInputStream(magEntry)), magEntry.getSize());
		
		ZipEntry rupSlipsEntry = zip.getEntry(getRemappedName("rup_avg_slips.bin", nameRemappings));
		double[] rupAveSlips;
		if (rupSlipsEntry != null)
			rupAveSlips = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(rupSlipsEntry)),
				rupSlipsEntry.getSize());
		else
			rupAveSlips = null;
		
		// don't use remapping here - this is legacy and new files will never have it
		ZipEntry rupSectionSlipsEntry = zip.getEntry("rup_sec_slips.bin");
		List<double[]> rupSectionSlips;
		if (rupSectionSlipsEntry != null)
			rupSectionSlips = MatrixIO.doubleArraysListFromInputStream(
					new BufferedInputStream(zip.getInputStream(rupSectionSlipsEntry)));
		else
			rupSectionSlips = null;
		
		ZipEntry rupSectionSlipModelEntry = zip.getEntry(getRemappedName("rup_sec_slip_type.txt", nameRemappings));
		SlipAlongRuptureModels slipModelType = null;
		if (rupSectionSlipModelEntry != null) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(zip.getInputStream(rupSectionSlipModelEntry), writer);
			String slipModelName = writer.toString().trim();
			try {
				slipModelType = SlipAlongRuptureModels.valueOf(slipModelName);
			} catch (Exception e) {
				System.err.println("WARNING: Unknown slip model type: "+slipModelName);
			}
		}
		
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
		ArrayList<FaultSectionPrefData> faultSectionData =
			fsDataFromXML(fsEl);
		DeformationModels defModName = null;
		Attribute defModAtt = fsEl.attribute("defModName");
		try {
			if (defModAtt != null && !defModAtt.getValue().isEmpty())
				defModName = DeformationModels.valueOf(defModAtt.getValue());
		} catch (Exception e) {
			System.err.println("Warning: unknown DefModeName: "+defModAtt.getValue());
		}
		FaultModels faultModel = null;
		Attribute faultModAtt = fsEl.attribute("faultModName");
		try {
			if (faultModAtt != null && !faultModAtt.getValue().isEmpty())
				faultModel = FaultModels.valueOf(faultModAtt.getValue());
		} catch (Exception e) {
			System.err.println("Warning: unknown FaultModels: "+faultModAtt.getValue());
		}
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
		
		ZipEntry infoEntry = zip.getEntry(getRemappedName("info.txt", nameRemappings));
		String info;
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
		    info = text.toString();
		} else
			info = null;
		
		
		return new SimpleFaultSystemRupSet(faultSectionData, mags, rupAveSlips, rupSectionSlips, slipModelType,
				sectSlipRates, sectSlipRateStdDevs, rakes, rupAreas, sectAreas, sectionForRups, info,
				closeSections, faultModel, defModName, clusterRups, clusterSects);
	}
	
	public static boolean isXMLFile(File file) {
		return file.getName().toLowerCase().endsWith(".xml");
	}
	
	public static SimpleFaultSystemRupSet fromFile(File file) throws IOException, DocumentException {
		if (isXMLFile(file))
			return fromXMLFile(file);
		else
			return fromZipFile(file);
	}

	@Override
	public SlipAlongRuptureModels getSlipAlongRuptureModel() {
		return slipModelType;
	}

	@Override
	public double getLengthForRup(int rupIndex) {
		throw new RuntimeException("not yet implemented");
	}

}

