package scratch.UCERF3;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

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
public class SimpleFaultSystemRupSet implements FaultSystemRupSet, XMLSaveable {
	
	public static final boolean D = false;
	
	public static final String XML_METADATA_NAME = "SimpleFaultSystemRupSet";
	
	private List<FaultSectionPrefData> faultSectionData;
	private double[] mags;
	private double[] rupAveSlips;
	private List<double[]> rupSectionSlips;
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
				rupSet.getSlipOnSectionsForAllRups(), rupSet.getSlipRateForAllSections(),
				rupSet.getAveRakeForAllRups(), rupSet.getAreaForAllRups(), rupSet.getAreaForAllSections(),
				rupSet.getSectionIndicesForAllRups(), rupSet.getInfoString(), rupSet.getCloseSectionsListList());
		if (rupSet.isClusterBased()) {
			clusterRups = new ArrayList<List<Integer>>();
			clusterSects = new ArrayList<List<Integer>>();
			for (int i=0; i<rupSet.getNumClusters(); i++) {
				clusterRups.add(rupSet.getRupturesForCluster(i));
				clusterSects.add(rupSet.getSectionsForCluster(i));
			}
		}
	}
	
	/**
	 * Creates a non-cluster based rupture set.
	 * 
	 * @param faultSectionData
	 * @param mags
	 * @param rupAveSlips
	 * @param rupSectionSlips
	 * @param sectSlipRates
	 * @param rakes
	 * @param rupAreas
	 * @param sectAreas
	 * @param sectionForRups
	 * @param info
	 * @param closeSections
	 */
	public SimpleFaultSystemRupSet(
			List<FaultSectionPrefData> faultSectionData, 
			double[] mags,
			double[] rupAveSlips,
			List<double[]> rupSectionSlips,
			double[] sectSlipRates,
			double[] rakes,
			double[] rupAreas,
			double[] sectAreas,
			List<List<Integer>> sectionForRups,
			String info,
			List<List<Integer>> closeSections) {
		this(faultSectionData, mags, rupAveSlips, rupSectionSlips, sectSlipRates, rakes,
				rupAreas, sectAreas, sectionForRups, info, closeSections, null, null);
	}
	
	/**
	 * Creates an (optionally) cluster based fault system rupture set.
	 * 
	 * @param faultSectionData
	 * @param mags
	 * @param rupAveSlips
	 * @param rupSectionSlips
	 * @param sectSlipRates
	 * @param rakes
	 * @param rupAreas
	 * @param sectAreas
	 * @param sectionForRups
	 * @param info
	 * @param closeSections
	 * @param clusterRups
	 * @param clusterSects
	 */
	public SimpleFaultSystemRupSet(
			List<FaultSectionPrefData> faultSectionData, 
			double[] mags,
			double[] rupAveSlips,
			List<double[]> rupSectionSlips,
			double[] sectSlipRates,
			double[] rakes,
			double[] rupAreas,
			double[] sectAreas,
			List<List<Integer>> sectionForRups,
			String info,
			List<List<Integer>> closeSections,
			List<List<Integer>> clusterRups,
			List<List<Integer>> clusterSects) {
		int numRups = mags.length;
		int numSects = faultSectionData.size();
		
		this.faultSectionData = faultSectionData;
		
		this.mags = mags;
		
		Preconditions.checkArgument(rupAveSlips.length == numRups, "array sizes inconsistent!");
		this.rupAveSlips = rupAveSlips;
		
		Preconditions.checkArgument(rupSectionSlips.size() == numRups, "array sizes inconsistent!");
		this.rupSectionSlips = rupSectionSlips;
		
		Preconditions.checkArgument(sectSlipRates.length == numSects, "array sizes inconsistent!");
		this.sectSlipRates = sectSlipRates;
		
		Preconditions.checkArgument(rakes.length == numRups, "array sizes inconsistent!");
		this.rakes = rakes;
		
		Preconditions.checkArgument(rupAreas.length == numRups, "array sizes inconsistent!");
		this.rupAreas = rupAreas;
		
		Preconditions.checkArgument(sectAreas.length == numSects, "array sizes inconsistent!");
		this.sectAreas = sectAreas;
		
		Preconditions.checkArgument(sectionForRups.size() == numRups, "array sizes inconsistent!");
		this.sectionForRups = sectionForRups;
		
		this.info = info;
		
		this.closeSections = closeSections;
		this.clusterRups = clusterRups;
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
	public List<double[]> getSlipOnSectionsForAllRups() {
		return rupSectionSlips;
	}

	@Override
	public double[] getSlipOnSectionsForRup(int rthRup) {
		return rupSectionSlips.get(rthRup);
	}
	
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
	
	@Override
	public List<Integer> getCloseSectionsList(int sectIndex) {
		if (closeSections == null)
			return null;
		return closeSections.get(sectIndex);
	}

	@Override
	public List<List<Integer>> getCloseSectionsListList() {
		return closeSections;
	}
	
	public boolean isClusterBased() {
		return getNumClusters() > 0;
	}
	
	@Override
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
	
	private static void fsDataToXML(Element parent, List<FaultSectionPrefData> list, String elName) {
		Element el = parent.addElement(elName);
		
		for (int i=0; i<list.size(); i++) {
			FaultSectionPrefData data = list.get(i);
			data.toXMLMetadata(el, "i"+i);
		}
	}
	
	public static ArrayList<FaultSectionPrefData> fsDataFromXML(Element el, int size) {
		ArrayList<FaultSectionPrefData> list = new ArrayList<FaultSectionPrefData>();
		
		for (int i=0; i<size; i++) {
			Element subEl = el.element("i"+i);
			list.add(FaultSectionPrefData.fromXMLMetadata(subEl));
		}
		
		return list;
	}

	@Override
	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		el.addAttribute("numRups", getNumRuptures()+"");
		el.addAttribute("numSects", getNumSections()+"");
		el.addAttribute("info", getInfoString());
		
		XMLUtils.doubleArrayToXML(el, mags, "mags");
		XMLUtils.doubleArrayToXML(el, rupAveSlips, "rupAveSlips");
		listDoubleArrayToXML(el, rupSectionSlips, "rupSectionSlips");
		XMLUtils.doubleArrayToXML(el, sectSlipRates, "sectSlipRates");
		XMLUtils.doubleArrayToXML(el, rakes, "rakes");
		XMLUtils.doubleArrayToXML(el, rupAreas, "rupAreas");
		XMLUtils.doubleArrayToXML(el, sectAreas, "sectAreas");
		intListArrayToXML(el, sectionForRups, "sectionForRups");
		if (closeSections != null)
			intListArrayToXML(el, closeSections, "closeSections");
		
		fsDataToXML(el, faultSectionData, FaultSectionPrefData.XML_METADATA_NAME+"List");
		
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
		
		String info = rupSetEl.attributeValue("info");
		
		if (D) System.out.println("Loading mags");
		double[] mags = XMLUtils.doubleArrayFromXML(rupSetEl.element("mags"));
		if (D) System.out.println("Loading rupAveSlips");
		double[] rupAveSlips = XMLUtils.doubleArrayFromXML(rupSetEl.element("rupAveSlips"));
		if (D) System.out.println("Loading rupSectionSlips");
		List<double[]> rupSectionSlips = listDoubleArrayFromXML(rupSetEl.element("rupSectionSlips"));
		if (D) System.out.println("Loading sectSlipRates");
		double[] sectSlipRates = XMLUtils.doubleArrayFromXML(rupSetEl.element("sectSlipRates"));
		if (D) System.out.println("Loading rakes");
		double[] rakes = XMLUtils.doubleArrayFromXML(rupSetEl.element("rakes"));
		if (D) System.out.println("Loading rupAreas");
		double[] rupAreas = XMLUtils.doubleArrayFromXML(rupSetEl.element("rupAreas"));
		if (D) System.out.println("Loading sectAreas");
		double[] sectAreas = XMLUtils.doubleArrayFromXML(rupSetEl.element("sectAreas"));
		if (D) System.out.println("Loading sectionForRups");
		List<List<Integer>> sectionForRups = intListArrayFromXML(rupSetEl.element("sectionForRups"));
		
		List<List<Integer>> closeSections = null;
		Element closeSectionsEl = rupSetEl.element("closeSections");
		if (closeSectionsEl != null) {
			if (D) System.out.println("Loading closeSections");
			closeSections = intListArrayFromXML(closeSectionsEl);
		}
		
		if (D) System.out.println("Loading faultSectionData");
		ArrayList<FaultSectionPrefData> faultSectionData =
			fsDataFromXML(rupSetEl.element(FaultSectionPrefData.XML_METADATA_NAME+"List"), numSects);
		
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
				faultSectionData, mags, rupAveSlips, rupSectionSlips, sectSlipRates, rakes,
				rupAreas, sectAreas, sectionForRups, info, closeSections, clusterRups, clusterSects);
		return simple;
	}
	
	public void toFile(File file) throws IOException {
		XMLUtils.writeObjectToXMLAsRoot(this, file);
	}
	
	public static SimpleFaultSystemRupSet fromFile(File file) throws MalformedURLException, DocumentException {
		Document doc = XMLUtils.loadDocument(file);
		Element rupSetEl = doc.getRootElement().element(XML_METADATA_NAME);
		return fromXMLMetadata(rupSetEl);
	}	

}

