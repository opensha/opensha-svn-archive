package org.opensha.sha.nshmp;

import static org.opensha.commons.geo.GeoTools.*;
import static org.apache.commons.math.geometry.RotationOrder.*;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.sha.faultSurface.FaultTrace;

/**
 * Wrapper class to simplify calculating the distance to a fault trace. Class
 * treats calculating the shortest distance as 2D problem. The parallelograms
 * representing each fault segment are rotated into the xy plane of a local
 * cartesian coordinate system. Precalculating and storing the 2D parallelograms
 * and the required rotation matrices drastically reduces the time required to
 * calculate the minimum distance to a fault surface.<br />
 * <br />
 * Internally, this class uses a right-handed cartesian coordinate system where
 * x is latitude, y is latitude, and z is depth (positive down per seismological
 * convention). This convention preserves strike values (degrees clockwise from
 * north) as clockwise rotation about the z-axis per cartesian convention.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class DistanceTransform {

	private FaultTrace trace;
	private List<Rotation> rots;
	private List<Path2D> surfs;

	DistanceTransform(FaultTrace trace, double dip, double width) {
		this.trace = trace;
		rots = new ArrayList<Rotation>();
		surfs = new ArrayList<Path2D>();
		initSegments(dip, width);
	}

	private void initSegments(double dip, double width) {
		for (int i = 0; i < trace.size() - 1; i++) {

			Location p1 = trace.get(i);
			Location p2 = trace.get(i + 1);
			LocationVector vec = LocationUtils.vector(p1, p2);

			double surfStrk = vec.getAzimuthRad();
			double avgDipDir = (trace.getStrikeDirection() * TO_RAD) + PI_BY_2;
			double avgDip = dip * TO_RAD; // avg dip of fault
			double surfDip; // true dip of parallelogram
			double p1p2Dist = vec.getHorzDistance();

			// top trace #1 is at [0,0]
			Vector3D vt1 = Vector3D.ZERO;

			// top trace #2
			Vector3D vt2 = new Vector3D(p1p2Dist, new Vector3D(surfStrk, 0));

			// bottom trace #1
			Vector3D vb1 = new Vector3D(width, new Vector3D(avgDipDir, avgDip));

			// bottom trace #2
			Vector3D vb2 = new Vector3D(1, vt2, 1, vb1);

			// true dip of surface - rotate vb1 the strike angle about
			// the z-axis, and flatten onto xy plane [0,y,z]
			Rotation dRot = new Rotation(Vector3D.PLUS_K, -surfStrk);
			Vector3D dVec = dRot.applyTo(vb1);
			dVec = new Vector3D(0, dVec.getY(), dVec.getZ());
			surfDip = dVec.getDelta();

			// set rotation
			Rotation rot = new Rotation(XYZ, -surfDip, 0, -surfStrk);
			rots.add(rot);

			// rotate parallelogram
			vt2 = rot.applyTo(vt2);
			vb1 = rot.applyTo(vb1);
			vb2 = rot.applyTo(vb2);

			// set up for 2D ops in yz plane
			Path2D surface = new Path2D.Double();
			surface.moveTo(vt1.getX(), vt1.getY());
			surface.lineTo(vt2.getX(), vt2.getY());
			surface.lineTo(vb2.getX(), vb2.getY());
			surface.lineTo(vb1.getX(), vb1.getY());
			surface.lineTo(vt1.getX(), vt1.getY());
			surfs.add(surface);

//			System.out.println(vt1);
//			System.out.println(vt2);
//			System.out.println(vb1);
//			System.out.println(vb2);
		}
	}

	/**
	 * Returns the minimum distance to the surface.
	 * @param loc of interest
	 * @return the minimum distance
	 */
	public double distance(Location loc) {
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < trace.size() - 1; i++) {
			// compute geographic vector to point
			LocationVector vec = LocationUtils.vector(trace.get(i), loc);
			// convert to cartesian
			Vector3D vp = new Vector3D(vec.getHorzDistance(), new Vector3D(
				vec.getAzimuthRad(), 0), vec.getVertDistance(), Vector3D.PLUS_K);
			// rotate
			vp = rots.get(i).applyTo(vp);
			// compute distance
			Path2D surf = surfs.get(i);
			if (surf.contains(vp.getX(), vp.getY())) {
				distance = Math.min(distance, Math.abs(vp.getZ()));
			} else {
				distance = Math.min(distance, distanceToSurface(vp, surf));
			}
		}
		return distance;
	}

	/*
	 * Iterates over surface outline path calculating distance to line segments
	 * and returning the minimum.
	 */
	private static double distanceToSurface(Vector3D p, Path2D border) {
		PathIterator pit = border.getPathIterator(null);
		double[] c = new double[6]; // coordinate array
		double minDistSq = Double.MAX_VALUE;
		while (!pit.isDone()) {
			pit.currentSegment(c);
			double distSq = Line2D.ptSegDistSq(c[0], c[1], c[2], c[3],
				p.getX(), p.getY());
			minDistSq = Math.min(minDistSq, distSq);
			pit.next();
		}
		return Math.sqrt(p.getZ() * p.getZ() + minDistSq);
	}

	public static void main(String[] args) {
		double depth = 0;
		Location l1 = new Location(34.0, -118.0, depth);
		Location l2 = new Location(34.1, -117.9, depth);
		Location l3 = new Location(34.3, -117.8, depth);
		Location l4 = new Location(34.4, -117.7, depth);
		Location l5 = new Location(34.5, -117.5, depth);

		FaultTrace ft = new FaultTrace("Test");
		ft.add(l1);
		ft.add(l2);
		ft.add(l3);
		ft.add(l4);
		ft.add(l5);

		// double stk = 35;
		double dip = 5;
		double wid = 15;
		DistanceTransform dt = new DistanceTransform(ft, dip, wid);

		Location p = new Location(34.0, -117.9);
		System.out.println(dt.distance(p));
		p = new Location(34.2, -117.8);
		System.out.println(dt.distance(p));

		p = new Location(34.3, -117.7);
		System.out.println(dt.distance(p));

		p = new Location(34.4, -117.6);
		System.out.println(dt.distance(p));
	}

}
