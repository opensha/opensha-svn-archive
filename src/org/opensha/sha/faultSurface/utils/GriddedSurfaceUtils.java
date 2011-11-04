package org.opensha.sha.faultSurface.utils;

import java.util.ListIterator;

import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
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
		
		// fix small values of distanceJB (since they can be non-zero over the rupture)
		// WAY1
		if(surface.getNumCols() > 1 && surface.getNumRows() > 1) {
			double d1, d2,min_dist;
			d1 = LocationUtils.horzDistanceFast(surface.getLocation(0, 0),surface.getLocation(1, 1));
			d2 = LocationUtils.horzDistanceFast(surface.getLocation(0, 1),surface.getLocation(1, 0));
			min_dist = 1.1*Math.min(d1, d1)/2;
			if(distJB<=min_dist) distJB = 0;
		}
		
		// WAY2 - BUT WHAT WILL HAPPEN WITH FRANKEL SURFACE
		/*
		if(distJB <surface.getAveGridSpacing()) {
			Region region = new Region(surface.getPerimeter(), BorderType.MERCATOR_LINEAR);
			if(region.contains(loc))
				distJB = 0;;
		}
		*/

		
		double[] results = {distRup, distJB, distSeis};
		
		return results;

	}
	
	/**
	 * This computes distanceX
	 * @param surface
	 * @param siteLoc
	 * @return
	 */
	public static double getDistanceX(EvenlyGriddedSurface surface, Location siteLoc) {

		double distanceX;
		
		// set to zero if it's a point source
		if(surface.getNumCols() == 1) {
			distanceX = 0;
		}
		else {
			// We should probably set something here here too if it's vertical strike-slip
			// (to avoid unnecessary calculations)

				// get points projected off the ends
				Location firstTraceLoc = surface.getLocation(0, 0); 						// first trace point
				Location lastTraceLoc = surface.getLocation(0, surface.getNumCols()-1); 	// last trace point

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
				for(int c=0; c<surface.getNumCols(); c++) {
					locsForExtendedTrace.add(surface.getLocation(0, c));
					locsForRegion.add(surface.getLocation(0, c));     	
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

				Region polygon = new Region(locsForRegion, BorderType.MERCATOR_LINEAR);
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
	

}
