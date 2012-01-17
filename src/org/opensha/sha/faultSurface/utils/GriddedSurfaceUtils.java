package org.opensha.sha.faultSurface.utils;

import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.FrankelGriddedSurface;
import org.opensha.sha.faultSurface.GriddedSubsetSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceSeisParameter;

public class GriddedSurfaceUtils {
	
	/** Class name for debugging. */
	protected final static String C = "GriddedSurfaceUtils";
	/** If true print out debug statements. */
	protected final static boolean D = false;

	/** minimum depth for Campbell model */
	final static double SEIS_DEPTH = DistanceSeisParameter.SEIS_DEPTH;
	
	
	/**
	 * This computes distRup, distJB, & distSeis, which are available in the returned
	 * array in elements 0, 1, and 2 respectively.
	 * @param surface
	 * @param loc
	 * @return
	 */
	public static double[] getPropagationDistances(EvenlyGriddedSurface surface, Location loc) {
		
		Location loc1 = loc;
		Location loc2;
		double distJB = Double.MAX_VALUE;
		double distSeis = Double.MAX_VALUE;
		double distRup = Double.MAX_VALUE;
		
		double horzDist, vertDist, rupDist;

		// flag to project to seisDepth if only one row and depth is below seisDepth
		boolean projectToDepth = false;
		if (surface.getNumRows() == 1 && surface.getLocation(0,0).getDepth() < SEIS_DEPTH)
			projectToDepth = true;

		// get locations to iterate over depending on dip
		ListIterator<Location> it;
		if(surface.getAveDip() > 89) {
			it = surface.getColumnIterator(0);
			if (surface.getLocation(0,0).getDepth() < SEIS_DEPTH)
				projectToDepth = true;
		}
		else
			it = surface.getLocationsIterator();

		while( it.hasNext() ){

			loc2 = (Location) it.next();

			// get the vertical distance
			vertDist = LocationUtils.vertDistance(loc1, loc2);

			// get the horizontal dist depending on desired accuracy
			horzDist = LocationUtils.horzDistanceFast(loc1, loc2);

			if(horzDist < distJB) distJB = horzDist;

			rupDist = horzDist * horzDist + vertDist * vertDist;
			if(rupDist < distRup) distRup = rupDist;

			if (loc2.getDepth() >= SEIS_DEPTH) {
				if (rupDist < distSeis)
					distSeis = rupDist;
			}
			// take care of shallow line or point source case
			else if(projectToDepth) {
				rupDist = horzDist * horzDist + SEIS_DEPTH * SEIS_DEPTH;
				if (rupDist < distSeis)
					distSeis = rupDist;
			}
		}

		distRup = Math.pow(distRup,0.5);
		distSeis = Math.pow(distSeis,0.5);

		if(D) {
			System.out.println(C+": distRup = " + distRup);
			System.out.println(C+": distSeis = " + distSeis);
			System.out.println(C+": distJB = " + distJB);
		}
		
		// Check whether small values of distJB should really be zero
		if(distJB <surface.getAveGridSpacing()) { // check this first since the next steps could take time
			
			// first identify whether it's a frankel type surface
			boolean frankelTypeSurface=false;
			if(surface instanceof FrankelGriddedSurface) {
				frankelTypeSurface = true;
			}
			else if(surface instanceof GriddedSubsetSurface) {
				if(((GriddedSubsetSurface)surface).getParentSurface() instanceof FrankelGriddedSurface) {
					frankelTypeSurface = true;
				}
			}
					
			if (frankelTypeSurface) {
				if (isDistJB_ReallyZero(surface, distJB)) distJB = 0;
			} else {
				Region reg = new Region(surface.getPerimeter(),
					BorderType.MERCATOR_LINEAR);
				if (reg.contains(loc)) distJB = 0;
			}
		}

		double[] results = {distRup, distJB, distSeis};
		
		return results;

	}
	
	/**
	 * This computes distanceX
	 * @param surface
	 * @param siteLoc
	 * @return
	 */
	public static double getDistanceX(FaultTrace trace, Location siteLoc) {

		double distanceX;
		
		// set to zero if it's a point source
		if(trace.size() == 1) {
			distanceX = 0;
		}
		else {
			// We should probably set something here here too if it's vertical strike-slip
			// (to avoid unnecessary calculations)

				// get points projected off the ends
				Location firstTraceLoc = trace.get(0); 						// first trace point
				Location lastTraceLoc = trace.get(trace.size()-1); 	// last trace point

				// get point projected from first trace point in opposite direction of the ave trace
				LocationVector dir = LocationUtils.vector(lastTraceLoc, firstTraceLoc); 		
				dir.setHorzDistance(1000); // project to 1000 km
				dir.setVertDistance(0d);
				Location projectedLoc1 = LocationUtils.location(firstTraceLoc, dir);


				// get point projected from last trace point in ave trace direction
				dir.setAzimuth(dir.getAzimuth()+180);  // flip to ave trace dir
				Location projectedLoc2 = LocationUtils.location(lastTraceLoc, dir);
				// point down dip by adding 90 degrees to the azimuth
				dir.setAzimuth(dir.getAzimuth()+90);  // now point down dip

				// get points projected in the down dip directions at the ends of the new trace
				Location projectedLoc3 = LocationUtils.location(projectedLoc1, dir);

				Location projectedLoc4 = LocationUtils.location(projectedLoc2, dir);

				LocationList locsForExtendedTrace = new LocationList();
				LocationList locsForRegion = new LocationList();

				locsForExtendedTrace.add(projectedLoc1);
				locsForRegion.add(projectedLoc1);
				for(int c=0; c<trace.size(); c++) {
					locsForExtendedTrace.add(trace.get(c));
					locsForRegion.add(trace.get(c));     	
				}
				locsForExtendedTrace.add(projectedLoc2);
				locsForRegion.add(projectedLoc2);

				// finish the region
				locsForRegion.add(projectedLoc4);
				locsForRegion.add(projectedLoc3);

				// write these out if in debug mode
				if(D) {
					System.out.println("Projected Trace:");
					for(int l=0; l<locsForExtendedTrace.size(); l++) {
						Location loc = locsForExtendedTrace.get(l);
						System.out.println(loc.getLatitude()+"\t"+ loc.getLongitude()+"\t"+ loc.getDepth());
					}
					System.out.println("Region:");
					for(int l=0; l<locsForRegion.size(); l++) {
						Location loc = locsForRegion.get(l);
						System.out.println(loc.getLatitude()+"\t"+ loc.getLongitude()+"\t"+ loc.getDepth());
					}
				}

				Region polygon=null;
				try {
					polygon = new Region(locsForRegion, BorderType.MERCATOR_LINEAR);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(locsForRegion);
					System.exit(0);
				}
				boolean isInside = polygon.contains(siteLoc);

				double distToExtendedTrace = locsForExtendedTrace.minDistToLine(siteLoc);

				if(isInside || distToExtendedTrace == 0.0) // zero values are always on the hanging wall
					distanceX = distToExtendedTrace;
				else 
					distanceX = -distToExtendedTrace;
		}
		
		return distanceX;
	}
	
	
	/**
	 * This returns brief info about this surface
	 * @param surf
	 * @return
	 */
	public static String getSurfaceInfo(EvenlyGriddedSurface surf) {
		Location loc1 = surf.getLocation(0, 0);
		Location loc2 = surf.getLocation(0,surf.getNumCols() - 1);
		Location loc3 = surf.getLocation(surf.getNumRows()-1, 0);
		Location loc4 = surf.getLocation(surf.getNumRows()-1,surf.getNumCols()-1);
		return new String("\tRup. Surf. Corner Locations (lat, lon, depth (km):" +
				"\n\n" +
				"\t\t" + (float) loc1.getLatitude() + ", " +
				(float) loc1.getLongitude() + ", " +
				(float) loc1.getDepth() + "\n" +
				"\t\t" + (float) loc2.getLatitude() + ", " +
				(float) loc2.getLongitude() + ", " +
				(float) loc2.getDepth() + "\n" +
				"\t\t" + (float) loc3.getLatitude() + ", " +
				(float) loc3.getLongitude() + ", " +
				(float) loc3.getDepth() + "\n" +
				"\t\t" + (float) loc4.getLatitude() + ", " +
				(float) loc4.getLongitude() + ", " +
				(float) loc4.getDepth() + "\n");
	}
	
	
	/**
	 * This gets the perimeter locations
	 * @param surface
	 * @return
	 */
	public static LocationList getEvenlyDiscritizedPerimeter(EvenlyGriddedSurface surface) {
		LocationList locList = new LocationList();
		int lastRow = surface.getNumRows()-1;
		int lastCol = surface.getNumCols()-1;
		for(int c=0;c<=lastCol;c++) locList.add(surface.get(0, c));
		for(int r=0;r<=lastRow;r++) locList.add(surface.get(r, lastCol));
		for(int c=lastCol;c>=0;c--) locList.add(surface.get(lastRow, c));
		for(int r=lastRow;r>=0;r--) locList.add(surface.get(r, 0));
		return locList;
	}
	
	/**
	 * This is used to check whether a small value of DistJB should really be zero
	 * because of surface discretization.  This is used where a contains call on 
	 * the surface perimeter wont work (e.g., because of loops and gaps at the bottom 
	 * of a FrankelGriddedSurface).  Surfaces that only have one row or column always
	 * return false (which means non-zero distJB along the trace of a straight line source).
	 * Note that this will return true for locations that are slightly off the surface projection
	 * (essentially expanding the edge of the fault by about have the discretization level. 
	 * 
	 */
	public static boolean isDistJB_ReallyZero(EvenlyGriddedSurface surface, double distJB) {
			if(surface.getNumCols() > 1 && surface.getNumRows() > 1) {
				double d1, d2,min_dist;
				d1 = LocationUtils.horzDistanceFast(surface.getLocation(0, 0),surface.getLocation(1, 1));
				d2 = LocationUtils.horzDistanceFast(surface.getLocation(0, 1),surface.getLocation(1, 0));
				min_dist = 1.1*Math.min(d1, d2)/2;	// the 1.1 is to prevent a precisely centered point to  return false
				if(distJB<=min_dist) 
					return true;
				else
					return false;
			}
			else
				return false;
	}
	
	
	/**
	 * This returns the minimum distance as the minimum among all location
	 * pairs between the two surfaces
	 * @param surface1 RuptureSurface 
	 * @param surface2 RuptureSurface 
	 * @return distance in km
	 */
	public static double getMinDistanceBetweenSurfaces(RuptureSurface surface1, RuptureSurface surface2) {
		Iterator<Location> it = surface1.getLocationsIterator();
		double min3dDist = Double.POSITIVE_INFINITY;
		double dist;
		// find distance between all location pairs in the two surfaces
		while(it.hasNext()) { // iterate over all locations in this surface
			Location loc1 = (Location)it.next();
			Iterator<Location> it2 = surface2.getEvenlyDiscritizedListOfLocsOnSurface().iterator();
			while(it2.hasNext()) { // iterate over all locations on the user provided surface
				Location loc2 = (Location)it2.next();
				dist = LocationUtils.linearDistanceFast(loc1, loc2);
				if(dist<min3dDist){
					min3dDist = dist;
				}
			}
		}
		return min3dDist;
	}



}
