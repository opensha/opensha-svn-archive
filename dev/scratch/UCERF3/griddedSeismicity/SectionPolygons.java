package scratch.UCERF3.griddedSeismicity;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.util.MathUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.RuptureSurface;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.enumTreeBranches.FaultModels;

/**
 * This utility class will take a FaultSystemSolution and populate all fault
 * subsection zone polygon fields. This is a multistep process that involves:
 * 
 * Polygon problems:
 *    - intersect, union etc... can yield micron-scale residual polys
 *    - some sections do not have polys
 *    - some polys do not span all subsections
 *    
 * @author Peter Powers
 * @version $Id:$
 */
public class SectionPolygons {

	private static boolean log = false;
	
//	private double len;
//	private List<FaultSectionPrefData> faults;
	
	// map of subsections to polygons
	private Map<Integer, Area> polyMap;
//	private Map<Integer, String> nameMap;
	
	private Map<Integer, String> parentNameMap;
	private Map<Integer, Region> parentRegionMap;
	private Map<Integer, List<FaultSectionPrefData>> parentSubSectionMap;
//	private Map<Integer, List<Integer>> parentSubSectIdxMap;
	
	private SectionPolygons() {};
	
	
	/**
	 * FSS based init
	 * @param subs
	 * @return
	 */
	public static SectionPolygons build(List<FaultSectionPrefData> subs) {
		SectionPolygons fp = new SectionPolygons();
		fp.parentRegionMap = Maps.newHashMap();
		fp.parentSubSectionMap = Maps.newHashMap();
		fp.parentNameMap = Maps.newHashMap();
		for (FaultSectionPrefData sect : subs) {
			int pID = sect.getParentSectionId();
			if (!fp.parentRegionMap.containsKey(pID)) {
				fp.parentRegionMap.put(pID, sect.getZonePolygon());
			}
			List<FaultSectionPrefData> sects = fp.parentSubSectionMap.get(pID);
			if (sects == null) {
				sects = Lists.newArrayList();
				fp.parentSubSectionMap.put(pID, sects);
			}
			sects.add(sect);
			if (!fp.parentNameMap.containsKey(pID)) {
				fp.parentNameMap.put(pID, sect.getParentSectionName());
			}
		}
		fp.init();
		return fp;
	}

	/**
	 * Fault Model based init
	 * Builds an instance of this class that can be used to access fault sub-
	 * scetion polygons by sectionID.
	 * @param faults
	 * @param len
	 * @return an instance
	 */
	public static SectionPolygons build(List<FaultSectionPrefData> faults, 
			double len) {
		SectionPolygons fp = new SectionPolygons();
		fp.parentRegionMap = Maps.newHashMap();
		fp.parentSubSectionMap = Maps.newHashMap();
		fp.parentNameMap = Maps.newHashMap();
		for (FaultSectionPrefData fault : faults) {
			int id = fault.getSectionId();
			fp.parentRegionMap.put(id, fault.getZonePolygon());
			fp.parentSubSectionMap.put(id, fault.getSubSectionsList(len));
			fp.parentNameMap.put(id, fault.getName());
		}
		fp.init();
		return fp;
	}
	
	/**
	 * Returns the polygon for the supplied id.
	 * @param id
	 * @return the polygon
	 */
	public Area get(int id) {
		return polyMap.get(id);
	}
	
	public Set<Integer> indices() {
		return polyMap.keySet();
	}
	
	public int size() {
		return polyMap.size();
	}
	
	private void init() {
		polyMap = Maps.newTreeMap();
//		nameMap = Maps.newTreeMap();
		buildPolys();
	}
	

	private void buildPolys() {
//			FaultSectionPrefData f1 = faults.get(248); // San Cayetano 3.2
//			FaultSectionPrefData f1 = faults.get(167); // Mission Ridge 3.1
//			faults.clear();
//			faults.add(f1);
//		System.out.println(f1.getFaultTrace());
			
		int idx = -1;
		for (Integer pID : parentRegionMap.keySet()) {
//		for (FaultSectionPrefData f : faults) {
			idx++;
			
//			initNames(f);
			
			StringBuilder sb = new StringBuilder();
			sb.append(Strings.padEnd(Integer.toString(idx), 5, ' '));
			sb.append(Strings.padEnd(Integer.toString(pID), 5, ' '));
			sb.append(Strings.padEnd(parentNameMap.get(pID), 48, ' '));

			if (parentRegionMap.get(pID) == null) {
				if (log) System.out.println(sb.append("null-poly"));
				initNullPolys(pID);
			} else {
				if (log) System.out.println(sb);
				initPolys(pID);
//				initNullPolys(f);
			}

//			if (f.getZonePolygon() == null) {
//				if (log) System.out.println(sb.append("null-poly"));
//				initNullPolys(f);
//			} else {
//				if (log) System.out.println(sb);
//				initPolys(f);
////				initNullPolys(f);
//			}
		}
		
		cleanPolys();
		mergeDownDip();

//		for (FaultSectionPrefData f : faults) {
//			List<FaultSectionPrefData> subSecs = f.getSubSectionsList(len);
//			for (FaultSectionPrefData sec : subSecs) {
//				int id = sec.getSectionId();
//				Area area = polyMap.get(id);
//				if (area == null) {
//					System.out.println("null");
//				} else {
//					List<LocationList> ppLists = areaToLocLists(area);
//					for (LocationList ppLoc : ppLists) {
//						System.out.println(ppLoc);
//					}
//				}
//			}
//		}

		for (Integer id : polyMap.keySet()) {
			Area poly = polyMap.get(id);
			String mssg = (poly == null) ? "null" : (!poly.isSingular())
				? "non-singular" : "ok";
//			mssg += " " + nameMap.get(id);
//			if (poly == null) {
//				System.out.println(Strings.padEnd(id.toString(), 10, ' ') + mssg);
//			}
			if (poly != null && !poly.isSingular()) {
				System.out.println(Strings.padEnd(id.toString(), 10, ' ') + mssg);
				List<LocationList> locLists = areaToLocLists(poly);
				for (LocationList locs : locLists) {
					System.out.println(locs);
				}
			}
		}
		
		
	}
	
	/* Populates the utilitysubsection name map; allows reverse lookup by id */
//	private void initNames(FaultSectionPrefData fault) {
//		List<FaultSectionPrefData> subSecs = fault.getSubSectionsList(len);
//		for (FaultSectionPrefData sec : subSecs) {
//			int id = sec.getSectionId();
//			nameMap.put(id, fault.getName());
//		}
//	}
	
	/* Populate subsections with null parent poly */
	private void initNullPolys(int pID) {
		List<FaultSectionPrefData> subSecs = parentSubSectionMap.get(pID);
		for (FaultSectionPrefData sec : subSecs) {
			int id = sec.getSectionId();
			polyMap.put(id, null);
		}
	}
//	private void initNullPolys(FaultSectionPrefData fault) {
//		List<FaultSectionPrefData> subSecs = fault.getSubSectionsList(len);
//		for (FaultSectionPrefData sec : subSecs) {
//			int id = sec.getSectionId();
//			polyMap.put(id, null);
//		}
//	}
	
	/*
	 * Builds the subsection:poly Map by creating subsection envelopes used
	 * to slice up the parent fault polygon. This puts all manner of non-
	 * singular and empty Areas into the map that will be filtered out after
	 * this method returns.
	 */
	private void initPolys(int pID) {
		// loop subsections creating polys and modding parent poly
		Area fPoly = parentRegionMap.get(pID).getShape(); // parent poly
		List<FaultSectionPrefData> subSecs = parentSubSectionMap.get(pID);
		for (int i=0; i<subSecs.size(); i++) {
			FaultSectionPrefData ss1 = subSecs.get(i);
			int id = ss1.getSectionId();
			
			// if only 1 segment
			if (subSecs.size() == 1) {
				polyMap.put(id, fPoly);
				break;
			}

			// if on last segment, use remaining fPoly and quit
			if (i == subSecs.size() - 1) {
				if (fPoly.isSingular()) {
					polyMap.put(id, fPoly);
				} else {
					// multi part polys need to have some attributed back
					// to the previous section
					List<LocationList> locLists = areaToLocLists(fPoly);
					for (LocationList locs : locLists) {
						Area polyPart = new Area(locs.toPath());
						FaultTrace trace = ss1.getFaultTrace();
						if (intersects(trace, polyPart)) {
							// this is the poly associated with the fault trace
							polyMap.put(id, polyPart);
						} else {
							Area leftover = polyPart;
							int sectionID = subSecs.get(i-1).getSectionId();
							Area prev = polyMap.get(sectionID);
							prev.add(leftover);
							prev = cleanBorder(prev);
							if (!prev.isSingular()) prev = hardMerge(prev);
							if (prev == null) System.out.println(
								"merge problem last segment");
							polyMap.put(sectionID, prev);
						}
					}
				}
				break;
			}
			
			FaultSectionPrefData ss2 = subSecs.get(i + 1);
			LocationList envelope = createSubSecEnvelope(ss1, ss2);
			
			// intersect with copy of parent
			Area envPoly = new Area(envelope.toPath());
			Area subPoly = (Area) fPoly.clone();
			subPoly.intersect(envPoly);
			
			// keep moving if nothing happened
			if (subPoly.isEmpty()) {
				polyMap.put(id, null);
				continue;
			}
			
			// get rid of dead weight
			subPoly = cleanBorder(subPoly);
			
			// determine if there is a secondary poly not associated with
			// the fault trace that must be added back to parent
			Area leftover = null;
			if (subPoly.isSingular()) {
				polyMap.put(id, subPoly);
			} else {
				List<LocationList> locLists = areaToLocLists(subPoly);
				for (LocationList locs : locLists) {
					Area polyPart = new Area(locs.toPath());
					FaultTrace trace = ss1.getFaultTrace();
					if (intersects(trace, polyPart)) {
						// this is the poly associated with the fault trace
						polyMap.put(id, polyPart);
					} else {
						leftover = polyPart;
					}
				}
			}
			
			// trim parent poly for next slice
			fPoly.subtract(envPoly);
			fPoly = cleanBorder(fPoly);
			
			// try adding back into fault poly
			if (leftover != null) {
				Area fCopy = (Area) fPoly.clone();
				fCopy.add(leftover);
				fCopy = cleanBorder(fCopy);
				if (!fCopy.isSingular()) {
					// try hard merge
					fCopy = hardMerge(fCopy);
					// hard merge failed, go to previous section
					if (fCopy == null) {
						int sectionID = subSecs.get(i-1).getSectionId();
						Area prev = polyMap.get(sectionID);
						prev.add(leftover);
						prev = cleanBorder(prev);
						if (!prev.isSingular()) prev = hardMerge(prev);
						if (prev == null) System.out.println("merge problem");
						polyMap.put(sectionID, prev);
					} else {
						fPoly = fCopy;
					}
				} else {
					fPoly = fCopy;
				}
			}
		}
	}
//	private void initPolys(FaultSectionPrefData fault) {
//		// loop subsections creating polys and modding parent poly
//		Area fPoly = fault.getZonePolygon().getShape(); // parent poly
//		List<FaultSectionPrefData> subSecs = fault.getSubSectionsList(len);
//		for (int i=0; i<subSecs.size(); i++) {
//			FaultSectionPrefData ss1 = subSecs.get(i);
//			int id = ss1.getSectionId();
//			
//			// if only 1 segment
//			if (subSecs.size() == 1) {
//				polyMap.put(id, fPoly);
//				break;
//			}
//
//			// if on last segment, use remaining fPoly and quit
//			if (i == subSecs.size() - 1) {
//				if (fPoly.isSingular()) {
//					polyMap.put(id, fPoly);
//				} else {
//					// multi part polys need to have some attributed back
//					// to the previous section
//					List<LocationList> locLists = areaToLocLists(fPoly);
//					for (LocationList locs : locLists) {
//						Area polyPart = new Area(locs.toPath());
//						FaultTrace trace = ss1.getFaultTrace();
//						if (intersects(trace, polyPart)) {
//							// this is the poly associated with the fault trace
//							polyMap.put(id, polyPart);
//						} else {
//							Area leftover = polyPart;
//							int sectionID = subSecs.get(i-1).getSectionId();
//							Area prev = polyMap.get(sectionID);
//							prev.add(leftover);
//							prev = cleanBorder(prev);
//							if (!prev.isSingular()) prev = hardMerge(prev);
//							if (prev == null) System.out.println(
//								"merge problem last segment");
//							polyMap.put(sectionID, prev);
//						}
//					}
//				}
//				break;
//			}
//			
//			FaultSectionPrefData ss2 = subSecs.get(i + 1);
//			LocationList envelope = createSubSecEnvelope(ss1, ss2);
//			
//			// intersect with copy of parent
//			Area envPoly = new Area(envelope.toPath());
//			Area subPoly = (Area) fPoly.clone();
//			subPoly.intersect(envPoly);
//			
//			// keep moving if nothing happened
//			if (subPoly.isEmpty()) {
//				polyMap.put(id, null);
//				continue;
//			}
//			
//			// get rid of dead weight
//			subPoly = cleanBorder(subPoly);
//			
//			// determine if there is a secondary poly not associated with
//			// the fault trace that must be added back to parent
//			Area leftover = null;
//			if (subPoly.isSingular()) {
//				polyMap.put(id, subPoly);
//			} else {
//				List<LocationList> locLists = areaToLocLists(subPoly);
//				for (LocationList locs : locLists) {
//					Area polyPart = new Area(locs.toPath());
//					FaultTrace trace = ss1.getFaultTrace();
//					if (intersects(trace, polyPart)) {
//						// this is the poly associated with the fault trace
//						polyMap.put(id, polyPart);
//					} else {
//						leftover = polyPart;
//					}
//				}
//			}
//			
//			// trim parent poly for next slice
//			fPoly.subtract(envPoly);
//			fPoly = cleanBorder(fPoly);
//			
//			// try adding back into fault poly
//			if (leftover != null) {
//				Area fCopy = (Area) fPoly.clone();
//				fCopy.add(leftover);
//				fCopy = cleanBorder(fCopy);
//				if (!fCopy.isSingular()) {
//					// try hard merge
//					fCopy = hardMerge(fCopy);
//					// hard merge failed, go to previous section
//					if (fCopy == null) {
//						int sectionID = subSecs.get(i-1).getSectionId();
//						Area prev = polyMap.get(sectionID);
//						prev.add(leftover);
//						prev = cleanBorder(prev);
//						if (!prev.isSingular()) prev = hardMerge(prev);
//						if (prev == null) System.out.println("merge problem");
//						polyMap.put(sectionID, prev);
//					} else {
//						fPoly = fCopy;
//					}
//				} else {
//					fPoly = fCopy;
//				}
//			}
//		}
//	}

	/* Envelope buffer around fault sub sections */
	private static final double BUF = 100;

	/*
	 * Method creates an envelope extending BUF km on either side of and along
	 * the first supplied sub section. One border of the envelope is the
	 * bisector if the two supplied subsections.
	 */
	private static LocationList createSubSecEnvelope(
			FaultSectionPrefData sec1, FaultSectionPrefData sec2) {

		FaultTrace t1 = sec1.getFaultTrace();
		FaultTrace t2 = sec2.getFaultTrace();

		Location p1 = t1.get(t1.size() - 2);
		Location p2 = t1.get(t1.size() - 1);
		// check that last and first points of adjacent subs are coincident
		Preconditions.checkState(p2.equals(t2.get(0)));
		
		LocationVector vBackAz = LocationUtils.vector(p2, p1);
		vBackAz.setHorzDistance(BUF);
		LocationVector vBisect = new LocationVector();
		vBisect.setAzimuth(sec1.getDipDirection());
		vBisect.setHorzDistance(BUF);
		
		// assemble location list that is a U shape starting on one side of
		// fault and passing through bisector and on to other side
		LocationList locs = new LocationList();

		// starting at p2, move to one side of fault
		Location util = LocationUtils.location(p2, vBisect);
		locs.add(util);
		// move back along fault inserting the first point in poly
		// previous point is advanced to second position
		locs.add(0, LocationUtils.location(util, vBackAz));
		// add subsection boundary point
		locs.add(p2);
		// move to other side of fault
		vBisect.reverse();
		util = LocationUtils.location(p2, vBisect);
		locs.add(util);
		// move back along fault
		locs.add(LocationUtils.location(util, vBackAz));
		
		return locs;
	}	

	
	
	
	
	/* Sets empty areas to null and cleans borders of others. */
	private void cleanPolys() {
		for (Integer id : polyMap.keySet()) {
			Area poly = polyMap.get(id);
			if (poly == null) continue;
			if (poly.isEmpty()) {
				polyMap.put(id, null);
			} else {
				polyMap.put(id, cleanBorder(poly));
			}
		}
	}
	
	/** 
	 * Cleans polygon of empty sub-polys and duplicate vertices
	 */
	public static Area cleanBorder(Area area) {
		// break apart poly into component paths; many qualify
		List<LocationList> locLists = areaToLocLists(area);
		// prune 'empty' polygons
		locLists = pruneEmpties(locLists);
		// clean remaining polygons of duplicate vertices
		locLists = removeDupes(locLists);
		// clean double backs
//		locLists = removeDoublebacks(locLists);
		Area areaOut = new Area();
		for (LocationList areaLocs : locLists) {
			areaOut.add(new Area(areaLocs.toPath()));
		}
		return areaOut;
	}
	
	/* Removes mostly empty polygons from a list of LocationLists */
	private static List<LocationList> pruneEmpties(List<LocationList> locLists) {
		List<LocationList> newLocLists = Lists.newArrayList();
		for (LocationList locs : locLists) {
			if (isEmptyPoly(locs)) continue;
			newLocLists.add(locs);
		}
		return newLocLists;
	}
	
	/* Removes adjacent duplicate points from a locationList */
	private static List<LocationList> removeDupes(List<LocationList> locLists) {
		List<LocationList> newLocLists = Lists.newArrayList();
		for (LocationList locs : locLists) {
			LocationList newLocs = new LocationList();
			for (Location loc : locs) {
				validateLoc(newLocs, loc);
			}
			newLocLists.add(newLocs);
		}
		return newLocLists;
	}
	
	/* Removes points that are repeated after only advancing on step */
	private static List<LocationList> removeDoublebacks(List<LocationList> locLists) {
		List<LocationList> newLocLists = Lists.newArrayList();
		for (LocationList locs : locLists) {
			LocationList newLocs = new LocationList();
			for (int i=0; i<locs.size(); i++) {
				Location current = locs.get(i);
				int checkIdx = i+2;
				if (checkIdx < locs.size()) {
					Location check = locs.get(i+2);
					if (areSimilar(current, check)) i+=2;
				} else if (checkIdx == locs.size() - 2) {
					if (areSimilar(current, locs.get(0))) i+=2;
				}
				newLocs.add(current);
			}
			newLocLists.add(newLocs);
		}
		return newLocLists;
	}

	/* Tests whether all points in a LocationList are the same */
	private static boolean isEmptyPoly(LocationList locs) {
		Location start = locs.get(0);
		for (Location loc : locs) {
			if (areSimilar(start, loc)) continue;
			return false;
		}
		return true;
	}

	/*
	 * Intersections, minus', and unions of Location based Areas result in very
	 * very small (sub-micron scale) secondary polygons left over that cause
	 * Area.isSingular() to fail. These appear to always be at the beginning or
	 * end of the Area path and are coincident with some other point in the
	 * path. There are also identical repeated vertices at the junctions of
	 * geometric observations, which are harmless.
	 * 
	 * The following method, when used to help build a path/LocationList from an
	 * Area, eliminates empty areas by scanning the growing list for locations
	 * that are similar. Only if no such Location exists is the supplied
	 * Locaiton added to the list in place.
	 */
	private static void validateLoc(LocationList locs, Location loc) {
		for (Location p : locs) {
			if (areSimilar(p, loc)) return;
		}
		locs.add(loc);
	}
	
	/* Location comparison tolerance and shift for poly merging */
	private static final double TOL = 0.000000001;
	
	/*
	 * Private Location comparer with higher tolerance than that in
	 * LocaitonUtils
	 */
	private static boolean areSimilar(Location p1, Location p2) {
		if (!MathUtils.equals(p1.getLatitude(), p2.getLatitude(), TOL)) {
			return false;
		}
		if (!MathUtils.equals(p1.getLongitude(), p2.getLongitude(), TOL)) {
			return false;
		}
		if (!MathUtils.equals(p1.getDepth(), p2.getDepth(), TOL)) {
			return false;
		}
		return true;
	}

	
	
	
	
	/* Attempts to merge non-singular area */
	private static Area hardMerge(Area area) {
		List<LocationList> locLists = areaToLocLists(area);
		checkArgument(locLists.size() == 2);
		Area a1 = new Area(locLists.get(0).toPath());
		Area a2 = new Area(locLists.get(1).toPath());
		return shiftMerge(a1, a2);
	}
	
	/*
	 * Method does a couple intersection tests by shifting a2 around searching
	 * for a singular result
	 */
	private static Area shiftMerge(Area a1, Area a2) {
		checkArgument(a1.isSingular());
		checkArgument(!a1.isEmpty());
		checkArgument(a2.isSingular());
		checkArgument(!a2.isEmpty());
		LocationList locsToShift = areaToLocLists(a2).get(0);
		LocationList shiftedLocs = null;
		Area merged = (Area) a1.clone();
		// east shift
		shiftedLocs = shiftEW(locsToShift, TOL);
		merged.add(new Area(shiftedLocs.toPath()));
		if (merged.isSingular()) {
			return merged;
		}
		// east south
		shiftedLocs = shiftNS(locsToShift, -TOL);
		merged.add(new Area(shiftedLocs.toPath()));
		if (merged.isSingular()) {
			return merged;
		}
		// east west
		shiftedLocs = shiftEW(locsToShift, -TOL);
		merged.add(new Area(shiftedLocs.toPath()));
		if (merged.isSingular()) {
			return merged;
		}
		// east north
		shiftedLocs = shiftNS(locsToShift, TOL);
		merged.add(new Area(shiftedLocs.toPath()));
		if (merged.isSingular()) {
			return merged;
		}
		return null;
	}
	
	private static LocationList shiftEW(LocationList locs, double shift) {
		LocationList locsOut = new LocationList();
		for (Location loc : locs) {
			Location shiftedLoc = new Location(loc.getLatitude(),
				loc.getLongitude() + shift);
			locsOut.add(shiftedLoc);
		}
		return locsOut;
	}


	private static LocationList shiftNS(LocationList locs, double shift) {
		LocationList locsOut = new LocationList();
		for (Location loc : locs) {
			Location shiftedLoc = new Location(loc.getLatitude() + shift,
				loc.getLongitude());
			locsOut.add(shiftedLoc);
		}
		return locsOut;
	}

	/* Tests whether any part of a fault trace is inside a polygon */
	private static boolean intersects(FaultTrace trace, Area poly) {
		for (Location loc : trace) {
			if (poly.contains(loc.getLongitude(), loc.getLatitude())) {
				return true;
			}
		}
		return false;
	}
		

	
	
	/* 
	 * Merges the downdip subsection representations with the now cleaned
	 * zone polygons
	 */
	private void mergeDownDip() {
		for (Integer pID : parentSubSectionMap.keySet()) {
			List<FaultSectionPrefData> subSecs = parentSubSectionMap.get(pID);
			int numSubSecs = subSecs.size();
			for (int i=0; i<numSubSecs; i++) {
				FaultSectionPrefData subSec = subSecs.get(i);
				int id = subSec.getSectionId();
				Area zone = polyMap.get(id);
				Area dd = createDownDipPoly(subSec);
				Area merged = merge(zone, dd);
				
				// currently bugs in some fault sections (polygons not following
				// fault traces) result in holes; remove using contains
				merged = removeNests(merged);
				polyMap.put(id, merged);
			}
		}
	}
//	private void mergeDownDip() {
//		for (FaultSectionPrefData f : faults) {
//			List<FaultSectionPrefData> subSecs = f.getSubSectionsList(len);
//			int numSubSecs = subSecs.size();
//			for (int i=0; i<numSubSecs; i++) {
//				FaultSectionPrefData subSec = subSecs.get(i);
//				int id = subSec.getSectionId();
//				Area zone = polyMap.get(id);
//				Area dd = createDownDipPoly(subSec);
//				Area merged = merge(zone, dd);
//				
//				// currently bugs in some fault sections (polygons not following
//				// fault traces) result in holes; remove using contains
//				merged = removeNests(merged);
//				polyMap.put(id, merged);
//			}
//		}
//	}
	
	/* 
	 * Returns an area that is the result of merging the two supplied. Returns
	 * null if the merged Area is empty.
	 */
	private static Area merge(Area zone, Area dd) {
		Area area = new Area();
		if (zone != null) area.add(zone);
		if (dd != null) area.add(dd);
		return area.isEmpty() ? null : area;
	}
	
	/* Creates the down dip polygon from the border of a fault surface */
	private static Area createDownDipPoly(FaultSectionPrefData f) {
		RuptureSurface surf = f.getStirlingGriddedSurface(1, false, false);
		LocationList perimeter = surf.getPerimeter();
		return new Area(perimeter.toPath());
	}
	
	/* Removes nested polygons */
	private static Area removeNests(Area area) {
		if (area == null) return null;
		if (area.isSingular()) return area;
		List<LocationList> locLists = areaToLocLists(area);
		checkArgument(locLists.size() > 1);
		Area a = new Area();
		for (LocationList locs : locLists) {
			Area toAdd = new Area(locs.toPath());
			a.add(toAdd);
		}
		a = cleanBorder(a);
		return a;
	}
	
	
	
	/**
	 * Iterates over the path defining an Area and returns a List of
	 * LocationLists. If Area is singular, returned list will only have one
	 * LocationList
	 */
	public static List<LocationList> areaToLocLists(Area area) {
		// break apart poly into component paths; many qualify
		List<LocationList> locLists = Lists.newArrayList();
		LocationList locs = null;
		// placeholder vertex for path iteration
		double[] vertex = new double[6];
		PathIterator pi = area.getPathIterator(null);
		while (!pi.isDone()) {
			int type = pi.currentSegment(vertex);
			double lon = vertex[0];
			double lat = vertex[1];
			if (type == PathIterator.SEG_MOVETO) {
				locs = new LocationList();
				locLists.add(locs);
				locs.add(new Location(lat, lon));
			} else if (type == PathIterator.SEG_LINETO) {
				locs.add(new Location(lat, lon));
			}
			// skip any closing segments as LocationList.toPath() will
			// close polygons
			pi.next();
		}
		return locLists;
	}
	
	
	/**
	 * Returns the {@code Region} spanned by a node centered at the supplied
	 * location with the given width and height.
	 * @param p {@code Location} at center of a grid node
	 * @param w node width
	 * @param h node height
	 * @return the node's {@code Region}
	 */
	public static Area getNodeShape(Location p, double w, double h) {
		double halfW = w / 2;
		double halfH = h / 2;
		double nodeLat = p.getLatitude();
		double nodeLon = p.getLongitude();
		LocationList locs = new LocationList();
		locs.add(new Location(nodeLat + halfH, nodeLon + halfW)); // top right
		locs.add(new Location(nodeLat - halfH, nodeLon + halfW)); // bot right
		locs.add(new Location(nodeLat - halfH, nodeLon - halfW)); // bot left
		locs.add(new Location(nodeLat + halfH, nodeLon - halfW)); // top left
		return new Area(locs.toPath());
	}
	
	/**
	 * Returns a flat-earth estimate of the area of this region in
	 * km<sup>2</sup>. Method uses the center of this {@code Region}'s bounding
	 * polygon as the origin of an orthogonal coordinate system. This method is
	 * not appropriate for use with very large {@code Region}s where the
	 * curvature of the earth is more significant.
	 * 
	 * Assumes aupplied area has already been cleaned of strays etc...
	 * 
	 * @return the area of this region in km<sup>2</sup>
	 */
	public static double getExtent(Area area) {
		List<LocationList> locLists = areaToLocLists(area);
		double total = 0;
		for (LocationList locs : locLists) {
			total += getExtent(locs);
		}
		return total;
	}

	public static double getExtent(LocationList locs) {
		Area area = new Area(locs.toPath());
		Rectangle2D rRect = area.getBounds2D();
		Location origin = new Location(rRect.getCenterY(), rRect.getCenterX());
		// compute orthogonal coordinates in km
		List<Double> xs = Lists.newArrayList();
		List<Double> ys = Lists.newArrayList();
		for (Location loc : locs) {
			LocationVector v = LocationUtils.vector(origin, loc);
			double az = v.getAzimuthRad();
			double d = v.getHorzDistance();
			xs.add(Math.sin(az) * d);
			ys.add(Math.cos(az) * d);
		}
		// repeat first point
		xs.add(xs.get(0));
		ys.add(ys.get(0));
		return computeArea(Doubles.toArray(xs), Doubles.toArray(ys));
		
	}

	/*
	 * Computes the area of a simple polygon; no data validation is performed
	 * except ensuring that all coordinates are positive.
	 */
	private static double computeArea(double[] xs, double[] ys) {
		positivize(xs);
		positivize(ys);
		double area = 0;
		for (int i = 0; i < xs.length - 1; i++) {
			area += xs[i] * ys[i + 1] - xs[i + 1] * ys[i];
		}
		return Math.abs(area) / 2;
	}

	/* Ensures positivity of values by adding Math.abs(min) if min < 0. */
	private static void positivize(double[] v) {
		double min = Doubles.min(v);
		if (min >= 0) return;
		min = Math.abs(min);
		for (int i = 0; i < v.length; i++) {
			v[i] += min;
		}
	}

//	public static void writeFaultModel() {
//		try {
//			File dir = new File("tmp");
//			FaultModels fm = FaultModels.FM3_2;
//			ArrayList<FaultSectionPrefData> datas = fm.fetchFaultSections();
//			Document doc = XMLUtils.createDocumentWithRoot();
//			SimpleFaultSystemRupSet.fsDataToXML(doc.getRootElement(), datas,
//				FaultModels.XML_ELEMENT_NAME, fm, null);
//			XMLUtils.writeDocumentToFile(new File(dir, fm.getShortName() +
//				".xml"), doc);
//			System.exit(0);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//	}
	

	
	
	public static void main(String[] args) {
//		writeFaultModel();
		SectionPolygons.build(FaultModels.FM3_2.fetchFaultSections(), 7);
	}


}
