package scratch.UCERF3.inversion;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;

public class SimpleClusterBasedFaultSystemRupSet extends
		SimpleFaultSystemRupSet implements ClusterBasedFaultSystemRupSet {
	
	List<List<Integer>> clusterRups;
	List<List<Integer>> clusterSects;

	public SimpleClusterBasedFaultSystemRupSet(
			List<FaultSectionPrefData> faultSectionData, double[] mags,
			double[] rupAveSlips, List<double[]> rupSectionSlips,
			double[] sectSlipRates, double[] rakes, double[] rupAreas,
			double[] sectAreas, List<List<Integer>> sectionForRups, String info,
			List<List<Integer>> clusterRups, List<List<Integer>> clusterSects) {
		super(faultSectionData, mags, rupAveSlips, rupSectionSlips, sectSlipRates,
				rakes, rupAreas, sectAreas, sectionForRups, info);
		this.clusterRups = clusterRups;
		this.clusterSects = clusterSects;
	}
	
	public SimpleClusterBasedFaultSystemRupSet(
			FaultSystemRupSet rupSet,
			List<List<Integer>> clusterRups,
			List<List<Integer>> clusterSects) {
		super(rupSet);
		this.clusterRups = clusterRups;
		this.clusterSects = clusterSects;
	}
	
	public static List<List<Integer>> getClusters(ClusterBasedFaultSystemRupSet clusterRupSet) {
		ArrayList<List<Integer>> clusters = new ArrayList<List<Integer>>();
		for (int i=0; i<clusterRupSet.getNumClusters(); i++)
			clusters.add(clusterRupSet.getRupturesForCluster(i));
		return clusters;
	}
	
	public SimpleClusterBasedFaultSystemRupSet(ClusterBasedFaultSystemRupSet clusterRupSet) {
		super(clusterRupSet);
		clusterRups = new ArrayList<List<Integer>>();
		clusterSects = new ArrayList<List<Integer>>();
		for (int i=0; i<clusterRupSet.getNumClusters(); i++) {
			clusterRups.add(clusterRupSet.getRupturesForCluster(i));
			clusterSects.add(clusterRupSet.getSectionsForCluster(i));
		}
	}

	@Override
	public int getNumClusters() {
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
	public Element toXMLMetadata(Element root) {
		super.toXMLMetadata(root);
		
		Element el = root.element(XML_METADATA_NAME);
		
		el.addAttribute("numClusters", getNumClusters()+"");
		
		intListArrayToXML(el, clusterRups, "clusterRups");
		intListArrayToXML(el, clusterSects, "clusterSects");
		
		return root;
	}
	
	public static SimpleClusterBasedFaultSystemRupSet fromXMLMetadata(SimpleFaultSystemRupSet simple, Element rupSetEl) {
		
		int numClusters = Integer.parseInt(rupSetEl.attributeValue("numClusters"));
		if (D) System.out.println("Loading "+numClusters+" clusters");
		Element clusterRupsEl = rupSetEl.element("clusterRups");
		Element clusterSectsEl = rupSetEl.element("clusterSects");
		List<List<Integer>> clusterRups = intListArrayFromXML(clusterRupsEl);
		List<List<Integer>> clusterSects = intListArrayFromXML(clusterSectsEl);
		
		return new SimpleClusterBasedFaultSystemRupSet(simple, clusterRups, clusterSects);
	}
	
	public static SimpleClusterBasedFaultSystemRupSet fromFile(File file) throws MalformedURLException, DocumentException {
		return (SimpleClusterBasedFaultSystemRupSet)SimpleFaultSystemRupSet.fromFile(file);
	}

}
