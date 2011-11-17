package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static org.opensha.sha.nshmp.SourceType.*;

import java.util.Collection;
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
	// private static Map<SourceRegion, List<ClusterSource>> clustERFs;

	static {
		log = NSHMP_Utils.logger();
		Level level = Level.WARNING;
		log.setLevel(level);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(level);
		}
		gridERFs = ArrayListMultimap.create();
		faultERFs = ArrayListMultimap.create();
		subERFs = ArrayListMultimap.create();
	}

	public static GridERF getGrid(SourceRegion region, SourceType type, String name) {
		List<SourceFile> files = SourceFileMgr.get(region, type, name);
		if (files == null) return null;
		GridParser parser = new GridParser(log);
		parser.parse(files.get(0));
		log.fine(parser.toString());
		return parser.createGridSource();
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
				parser.parse(sf);
				log.fine(parser.toString());
				gridERFs.put(region, parser.createGridSource());
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
		if (faultERFs.get(region) == null) {
			List<SourceFile> files = SourceFileMgr.get(region, FAULT);
			if (files == null) return null;
			for (SourceFile sf : files) {
				FaultParser parser = new FaultParser(log);
				FaultERF ferf = parser.parseFault(sf);
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
		if (subERFs.get(region) == null) {
			List<SourceFile> files = SourceFileMgr.get(region, SUBDUCTION);
			if (files == null) return null;
			for (SourceFile sf : files) {
				SubductionParser parser = new SubductionParser(log);
				SubductionERF serf = parser.parseSubduction(sf);
				subERFs.put(region, serf);
			}
		}
		return subERFs.get(region);
	}

	public static void main(String[] args) {
		// Map<String, GridSource> gMap = getGridSources(CEUS);
		// System.out.println(gMap);0
		// List<GridERF> erfs = getGridList(CA);
		List<GridERF> erfs = getGridList(SourceRegion.CEUS);
		 for (GridERF erf : erfs) {
		 System.out.println(erf.getName());
		 }
	}

}
