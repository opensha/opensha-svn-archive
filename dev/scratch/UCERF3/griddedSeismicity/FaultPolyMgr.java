package scratch.UCERF3.griddedSeismicity;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

/**
 * Maintains collections of the relationships between grid nodes and fault
 * sections.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class FaultPolyMgr {

	private boolean log = true;
	
	private SectionPolygons polys;
	
	
	// Table<NodeIndex, SubSectionID, Value>
	private Table<Integer, Integer, Double> sectInNodePartic;
	// Table<SubSectionID, NodeIndex, Value>
	private Table<Integer, Integer, Double> nodeInSectPartic;
	
	// utility collections
	private Multimap<Integer, Integer> sectToProbNodes;
	private Multimap<Integer, Integer> sectToNodes;
	private Multimap<Integer, Integer> nodeToSects;
	private Map<Integer, Area> nodeAreas;
	private Map<Integer, Double> nodeExtents;
	private Map<Integer, Double> sectExtents;

	private GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED();

	/**
	 * Returns the fraction of the node at idx that participates in fault
	 * section related seismicity.
	 * @param idx
	 */
	double getNodeFraction(int idx) {
		Double fraction = nodeExtents.get(idx);
		return (fraction == null) ? 0.0 : fraction;
	}
	
	/**
	 * Returns a map of the percent area of a node occupied by each of the
	 * sections that intersect it.
	 * @param idx
	 * @return
	 */
	Map<Integer, Double> getSectFractions(int idx) {
		return sectInNodePartic.column(idx);
	}
	
	/**
	 * Returns a map of the percent area of a section that is present in each
	 * node it intersects.
	 * @param idx
	 * @return
	 */
	Map<Integer, Double> getNodeFractions(int idx) {
		return sectInNodePartic.column(idx);
	}

	FaultPolyMgr(FaultSystemSolution fss) {
		if (log) System.out.println("Building...");
		if (log) System.out.println("  getting faults from solution");
		List<FaultSectionPrefData> faults = fss.getFaultSectionDataList();
		if (log) System.out.println("  subsection polygons");
		polys = SectionPolygons.build(faults);
		init();
	}
	
	FaultPolyMgr(FaultModels fm, double len) {		
		if (log) System.out.println("Building...");
		if (log) System.out.println("  getting faults from model");
		List<FaultSectionPrefData> faults = fm.fetchFaultSections();
		if (log) System.out.println("  subsection polygons");
		polys = SectionPolygons.build(faults, len);
		init();
	}
	
	private void init() {
		if (log) System.out.println("  section:node map");
		initSectionToProbableNodes();
		if (log) System.out.println("  node area cache");
		initNodeAreas();
		if (log) System.out.println("  section extents");
		initSectionExtents();
		if (log) System.out.println("  section participation");
		initSectInNodeParticipTable();
		if (log) System.out.println("  update node cache");
		updateNodeAreas();
		if (log) System.out.println("  node participation");
		initNodeInSectParticipTable();
		if (log) System.out.println("  node participation");
		initNodeParticipation();
		if (log) System.out.println("  update section participation");
		updateParticipationTable();
		if (log) System.out.println("Done.");
	}
	
	/*
	 * Initializes a multimap of all nodes each section MAY intersect. This is
	 * done to prevent looping all nodes and all subsections.
	 */
	private void initSectionToProbableNodes() {
		sectToProbNodes = ArrayListMultimap.create();
		for (Integer id : polys.indices()) {
			Area poly = polys.get(id);
			List<Integer> indices = null;
			if (poly != null) {
				indices = region.indicesForBounds(poly.getBounds2D());
				sectToProbNodes.putAll(id, indices);
			}
		}
	}
	
	/*
	 * Initializes a lookup table for all PROBABLE areas intersected by fault
	 * subsections. Some will not have any intersections.
	 */
	private void initNodeAreas() {
		nodeAreas = Maps.newHashMap();
		nodeExtents = Maps.newHashMap();
		Set<Integer> nodeIdxs = Sets.newHashSet(sectToProbNodes.values());
		for (Integer nodeIdx : nodeIdxs) {
			Area nodeArea = region.areaForIndex(nodeIdx);
			double nodeExtent = SectionPolygons.getExtent(nodeArea);
			nodeAreas.put(nodeIdx, nodeArea);
			nodeExtents.put(nodeIdx, nodeExtent);
		}
	}
	
	/*
	 * Initializes a map of the total area covered by each fault section .
	 */
	private void initSectionExtents() {
		sectExtents = Maps.newHashMap();
		for (Integer id : polys.indices()) {
			Area area = polys.get(id);
			double extent = (area == null) ? 0.0 : SectionPolygons
				.getExtent(area);
			sectExtents.put(id, extent);
		}
	}
	
 	/*
	 * Initializes table of fault section participation in each node, i.e.
	 * the percent of a node's area covered by a fault section's polygon. In
	 * the process, the sect:node and node:sect maps are created. This is later
	 * revised to to multiple overlapping section sin many nodes.
	 */
	private void initSectInNodeParticipTable() {
		sectToNodes = ArrayListMultimap.create();
		sectInNodePartic = HashBasedTable.create();
		for (Integer ssIdx : sectToProbNodes.keySet()) {
			Collection<Integer> nodeIdxs = sectToProbNodes.get(ssIdx);
			List<Integer> revisedIdxs = processFault(ssIdx, nodeIdxs);
			sectToNodes.putAll(ssIdx, revisedIdxs);
		}
		// create node:sect via inversion
		nodeToSects =  ArrayListMultimap.create();
		Multimaps.invertFrom(sectToNodes, nodeToSects);
	}
	
	private List<Integer> processFault(Integer ssIdx, Iterable<Integer> nodeIdxs) {
		List<Integer> newNodeIdxs = Lists.newArrayList();
		Area ssArea = polys.get(ssIdx); // should be singular
		for (Integer nodeIdx : nodeIdxs) {
			Area nodeArea = region.areaForIndex(nodeIdx); // should be singular
			nodeArea.intersect(ssArea);
			if (nodeArea.isEmpty()) continue; // no overlap; eliminate
			nodeArea = SectionPolygons.cleanBorder(nodeArea);
			double faultExtent = SectionPolygons.getExtent(nodeArea);
			double ratio = faultExtent / nodeExtents.get(nodeIdx);
			newNodeIdxs.add(nodeIdx);
			sectInNodePartic.put(nodeIdx, ssIdx, ratio);
		}
		return newNodeIdxs;
	}
	
	/*
	 * Initializes table of node participation in each section, i.e. the percent
	 * of a section's area present in each node it intersects. The participation
	 * values for a section sum to 1.
	 */
	private void initNodeInSectParticipTable() {
		nodeInSectPartic = HashBasedTable.create();
		for (Table.Cell<Integer, Integer, Double> cell : sectInNodePartic.cellSet()) {
			int sectIdx = cell.getColumnKey();
			int nodeIdx = cell.getRowKey();
			double sectExtent = sectExtents.get(sectIdx);
			double nodeExtent = nodeExtents.get(nodeIdx);
			double sectPartic = cell.getValue();
			// sectExtentInNode = nodeExtent * partic
			// nodePartic = sectExtentInNode / sectExtent
			double nodePartic = nodeExtent * sectPartic / sectExtent;
			nodeInSectPartic.put(sectIdx, nodeIdx, nodePartic);
		}
	}
	
	/*
	 * Once the true section:node mapping is established, revise node area
	 * caches.
	 */
	private void updateNodeAreas() {
		Set<Integer> allIdxs = Sets.newHashSet(sectToProbNodes.values());
		Set<Integer> goodIdxs = Sets.newHashSet(sectToNodes.values());
		Set<Integer> badIdxs = Sets.difference(allIdxs, goodIdxs);
		nodeAreas.keySet().removeAll(badIdxs);
		nodeExtents.keySet().removeAll(badIdxs);
	}
	
	/*
	 * Initializes map of node participation, i.e. the percent of a node's
	 * extent covered by one or more fault sections. This modifies areas in the
	 * node:area map in place and updates extents in node:extent to their
	 * participating percent; former value was actual extent
	 */
	private void initNodeParticipation() {
		for (Integer nodeIdx : nodeToSects.keySet()) {
			Area nodeArea = nodeAreas.get(nodeIdx);
			double totalExtent = nodeExtents.get(nodeIdx);
			for (Integer sectIdx : nodeToSects.get(nodeIdx)) {
				nodeArea.subtract(polys.get(sectIdx));
			}
			nodeArea = SectionPolygons.cleanBorder(nodeArea);
			nodeAreas.put(nodeIdx, nodeArea);
			double nodeExtent = SectionPolygons.getExtent(nodeArea);
			nodeExtent = 1 - (nodeExtent / totalExtent);
			nodeExtents.put(nodeIdx, nodeExtent);			
		}
	}
	
	/*
	 * Updates table of section participation. Because numerous nodes intersect
	 * multiple overlapping faults, we scale the section participation in each
	 * node to the relative fraction of the total participating area in the
	 * node. So if S1=0.6, S2=0.4, and S3=0.2 and in agreggate these three
	 * sections cover 60% of the node, then they are scaled to S1=0.3, S2=0.2, 
	 * and S3=0.1
	 */
	private void updateParticipationTable() {
		// sum of section participations in node
		for (Integer nodeIdx : sectInNodePartic.rowKeySet()) {
			Map<Integer, Double> sects = sectInNodePartic.row(nodeIdx);
			double totalPartic = sum(sects.values());
			double nodePartic = nodeExtents.get(nodeIdx);
			for (Integer sectIdx : sects.keySet()) {
				// scaled value
				double val = (sects.get(sectIdx) / totalPartic) * nodePartic;
				sects.put(sectIdx, val);
			}
		}
	}
	
	private static double sum(Iterable<Double> values) {
		double sum = 0;
		for (Double v : values) {
			sum += v;
		}
		return sum;
	}
	
	
	public static void main(String[] args) {
		FaultPolyMgr mgr = new FaultPolyMgr(FaultModels.FM3_2, 7);
//		System.out.println(mgr.nodeInSectPartic);
//		
//		for (Integer sectIdx : mgr.nodeInSectPartic.rowKeySet()) {
//			Map<Integer, Double> nodeVals = mgr.nodeInSectPartic.row(sectIdx);
//			System.out.println(sum(nodeVals.values()));
//		}
	}
	
	
}
