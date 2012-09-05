package org.opensha.nshmp2.erf.source;

import static org.opensha.sha.nshmp.SourceType.*;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils;
import org.opensha.sha.nshmp.SourceRegion;
import org.opensha.sha.nshmp.SourceType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Utility class to fetch NSHMP sources. Sources are generally wrapped in
 * individual ERFs.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class Sources {

	private static Logger log;
	private static ListMultimap<SourceRegion, GridERF> gridERFs;
	private static ListMultimap<SourceRegion, FaultERF> faultERFs;
	private static ListMultimap<SourceRegion, SubductionERF> subERFs;
	private static ListMultimap<SourceRegion, ClusterERF> clustERFs;

	static {
		// NSHMP_Utils logger is set to WARNING; probably want to use prefs
		log = NSHMP_Utils.logger();
		Level level = Level.SEVERE;
		log.setLevel(level);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(level);
		}
		gridERFs = ArrayListMultimap.create();
		faultERFs = ArrayListMultimap.create();
		subERFs = ArrayListMultimap.create();
		clustERFs = ArrayListMultimap.create();
	}

	/**
	 * Returns the ERF with the specified name.
	 * @param name
	 * @return a {@code NSHMP_ERF}
	 */
	public static NSHMP_ERF get(String name) {
		SourceRegion region = regionForSource(name);
		SourceType type = typeForSource(name);
		if (region == null && type == null) return null;
		switch (type) {
			case GRIDDED:
				return getGrid(name);
			case FAULT:
				return getFault(name);
			case CLUSTER:
				return getCluster(name);
			default:
				return null;
		}
	}

	/**
	 * Returns the {@code ClusterERF} with the specified name.
	 * @param name
	 * @return a {@code ClusterERF}
	 */
	public static ClusterERF getCluster(String name) {
		List<SourceFile> files = SourceFileMgr.get(null, CLUSTER, name);
		if (files == null) return null;
		ClusterParser parser = new ClusterParser(log);
		return parser.parse(files.get(0));
	}

	/**
	 * Returns the {@code GridERF} with the specified name.
	 * @param name
	 * @return a {@code GridERF}
	 */
	public static GridERF getGrid(String name) {
		List<SourceFile> files = SourceFileMgr.get(null, GRIDDED, name);
		if (files == null) return null;
		GridParser parser = new GridParser(log);
		return parser.parse(files.get(0));
	}

	/**
	 * Returns the {@code FaultERF} with the specified name.
	 * @param name
	 * @return a {@code FaultERF}
	 */
	public static FaultERF getFault(String name) {
		List<SourceFile> files = SourceFileMgr.get(null, FAULT, name);
		if (files == null) return null;
		FaultParser parser = new FaultParser(log);
		return parser.parse(files.get(0));
	}

	/**
	 * Returns the {@code SubductionERF} with the specified name.
	 * @param name
	 * @return a {@code SubductionERF}
	 */
	public static SubductionERF getSub(String name) {
		List<SourceFile> files = SourceFileMgr.get(null, SUBDUCTION, name);
		if (files == null) return null;
		SubductionParser parser = new SubductionParser(log);
		return parser.parse(files.get(0));
	}

	/**
	 * Returns the <code>GridERF</code>s for the requested region.
	 * @param region of interest
	 * @return the <code>List</code> of ERFs
	 */
	public static List<ClusterERF> getClusterList(SourceRegion region) {
		if (clustERFs.get(region).isEmpty()) {
			List<SourceFile> files = SourceFileMgr.get(region, CLUSTER);
			if (files == null) return null;
			for (SourceFile sf : files) {
				ClusterParser parser = new ClusterParser(log);
				ClusterERF erf = parser.parse(sf);
				log.fine(erf.toString());
				clustERFs.put(region, erf);
			}
		}
		return clustERFs.get(region);
	}

	/**
	 * Returns the <code>GridERF</code>s for the requested region.
	 * @param region of interest
	 * @return the <code>List</code> of ERFs
	 */
	public static List<GridERF> getGridList(SourceRegion region) {
		if (gridERFs.get(region).isEmpty()) {
			List<SourceFile> files = SourceFileMgr.get(region, GRIDDED);
			if (files == null) return null;
			for (SourceFile sf : files) {
				GridParser parser = new GridParser(log);
				GridERF erf = parser.parse(sf);
				log.fine(erf.toString());
				gridERFs.put(region, erf);
			}
		}
		return gridERFs.get(region);
	}

	/**
	 * Returns the <code>FaultERF</code>s for the requested region.
	 * @param region of interest
	 * @return the <code>List</code> of ERFs
	 */
	public static List<FaultERF> getFaultList(SourceRegion region) {
		if (faultERFs.get(region).isEmpty()) {
			List<SourceFile> files = SourceFileMgr.get(region, FAULT);
			if (files == null) return null;
			for (SourceFile sf : files) {
				FaultParser parser = new FaultParser(log);
				FaultERF ferf = parser.parse(sf);
				faultERFs.put(region, ferf);
			}
		}
		return faultERFs.get(region);
	}

	/**
	 * Returns the <code>SubductionERF</code>s for the requested region.
	 * @param region of interest
	 * @return the <code>List</code> of ERFs
	 */
	public static List<SubductionERF> getSubductionList(SourceRegion region) {
		if (subERFs.get(region).isEmpty()) {
			List<SourceFile> files = SourceFileMgr.get(region, SUBDUCTION);
			if (files == null) return null;
			for (SourceFile sf : files) {
				SubductionParser parser = new SubductionParser(log);
				SubductionERF serf = parser.parse(sf);
				subERFs.put(region, serf);
			}
		}
		return subERFs.get(region);
	}

	/**
	 * Returns the region for the source with the supplied name, or
	 * <code>null</code> if name does nat match any source file
	 * @param name of the source to lookup
	 * @return the associated region
	 */
	public static SourceRegion regionForSource(String name) {
		SourceFile sf = SourceFileMgr.get(null, null, name).get(0);
		return (sf != null) ? sf.getRegion() : null;
	}

	/**
	 * Returns the type for the source with the supplied name, or
	 * <code>null</code> if name does nat match any source file
	 * @param name of the source to lookup
	 * @return the associated type
	 */
	public static SourceType typeForSource(String name) {
		SourceFile sf = SourceFileMgr.get(null, null, name).get(0);
		return (sf != null) ? sf.getType() : null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(getFaultList(SourceRegion.WUS));
//		System.out.println(typeForSource("CEUSchar.73.in"));
		// Map<String, GridSource> gMap = getGridSources(CEUS);
		// System.out.println(gMap);0
		// List<GridERF> erfs = getGridList(CA);
		// List<GridERF> erfs = getGridList(SourceRegion.CEUS);
		// for (GridERF erf : erfs) {
		// System.out.println(erf.getName());
		// }
	}

}
