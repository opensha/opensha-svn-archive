package org.opensha.sha.faultSurface;

import static org.opensha.commons.geo.GeoTools.*;
import static org.apache.commons.math3.geometry.euclidean.threed.RotationOrder.*;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceSeisParameter;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

/**
 * Wrapper class to simplify calculating the distance to a fault. Class
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
 * @author Peter Powers, Kevin Milner
 * @version $Id:$
 */
public class QuadSurface implements RuptureSurface {
	
	static boolean D = false;
	
	private double dip;
	private double width;
	private double avgUpperDepth;
	private double avgDipDirRad;
	
	/* true if the entire trace is below 3km */
	private boolean traceBelowSeis;

	/* actual 3d values */
	private FaultTrace trace;
	private List<Rotation> rots;
	private List<Path2D> surfs;

	/* surface projection (for dist jb) */
	private FaultTrace proj_trace;
	private List<Path2D> proj_surfs;

	/* portion of fault below seismogenic depth of 3km (for dist seis) */
	private FaultTrace seis_trace;
	private List<Rotation> seis_rots;
	private List<Path2D> seis_surfs;
	
	/*
	 * discretization to use for evenly discretized methods
	 */
	private double discr_km = 1d;
	
	// caching for distance measures
	private Location siteLocForDistRupCalc = new Location(Double.NaN,Double.NaN);
	private Location siteLocForDistJBCalc = new Location(Double.NaN,Double.NaN);
	private Location siteLocForDistSeisCalc = new Location(Double.NaN,Double.NaN);
	private Location siteLocForDistXCalc= new Location(Double.NaN,Double.NaN);
	private double distanceJB, distanceSeis, distanceRup, distanceX;
	
	private static double calcWidth(FaultSectionPrefData prefData, boolean aseisReducesArea) {
		if (aseisReducesArea)
			return prefData.getAveLowerDepth() - prefData.getReducedAveUpperDepth();
		return prefData.getAveLowerDepth() - prefData.getOrigAveUpperDepth();
	}
	
	/**
	 * This moves the trace down to the top of the seismogenic zone
	 * @param prefData
	 * @param aseisReducesArea
	 * @return
	 */
	private static FaultTrace getTraceBelowSeismogenic(FaultSectionPrefData prefData, boolean aseisReducesArea) {
		FaultTrace belowTrace = new FaultTrace("");
		double upperSeismogenicDepth;
		if (aseisReducesArea)
			upperSeismogenicDepth = prefData.getReducedAveUpperDepth();
		else
			upperSeismogenicDepth = prefData.getOrigAveUpperDepth();
		double aveDipRadians = Math.toRadians(prefData.getAveDip());
		double aveDipDirection = prefData.getDipDirection();
		for (Location loc : prefData.getFaultTrace())
			belowTrace.add(StirlingGriddedSurface.getTopLocation(
					loc, upperSeismogenicDepth, aveDipRadians, aveDipDirection));
		return belowTrace;
	}
	
	public QuadSurface(FaultSectionPrefData prefData, boolean aseisReducesArea) {
//		this(getTraceBelowSeismogenic(prefData, aseisReducesArea),
//				prefData.getAveDip(), calcWidth(prefData, aseisReducesArea));
		this(prefData.getFaultTrace(),
				prefData.getAveDip(), calcWidth(prefData, aseisReducesArea));
	}

	public QuadSurface(FaultTrace trace, double dip, double width) {
		this.trace = trace;
		this.dip = dip;
		this.width = width;
		rots = new ArrayList<Rotation>();
		surfs = new ArrayList<Path2D>();
		
		// TODO USE DIP DIR FROM FSD
		avgDipDirRad = (trace.getStrikeDirection() * TO_RAD) + PI_BY_2;
		
		initSegments(dip, avgDipDirRad, width, trace, rots, surfs);
		
		traceBelowSeis = true;
		avgUpperDepth = 0d;
		// TODO weight average
		for (Location loc : trace) {
			if (loc.getDepth() <= DistanceSeisParameter.SEIS_DEPTH)
				traceBelowSeis = false;
			avgUpperDepth += loc.getDepth();
		}
		avgUpperDepth /= (double)trace.size();
	}

	private static void initSegments(double dip, double avgDipDirRad, double width,
			FaultTrace trace, List<Rotation> rots, List<Path2D> surfs) {
		Preconditions.checkState(!Double.isNaN(dip));
		Preconditions.checkState(!Double.isNaN(avgDipDirRad));
		Preconditions.checkState(!Double.isNaN(width));
		double avgDip = dip * TO_RAD; // avg dip of fault
		for (int i = 0; i < trace.size() - 1; i++) {

			Location p1 = trace.get(i);
			Location p2 = trace.get(i + 1);
			LocationVector vec = LocationUtils.vector(p1, p2);

			double surfStrk = vec.getAzimuthRad();
			double surfDip; // true dip of parallelogram
			double p1p2Dist = vec.getHorzDistance();

			// top trace #1 is at [0,0]
			Vector3D vt1 = Vector3D.ZERO;

			// top trace #2
			Vector3D vt2 = new Vector3D(p1p2Dist, new Vector3D(surfStrk, 0));

			// bottom trace #1
			Vector3D vb1 = new Vector3D(width, new Vector3D(avgDipDirRad, avgDip));

			// bottom trace #2
			Vector3D vb2 = new Vector3D(1, vt2, 1, vb1);

			// set rotation // true dip of surface - rotate vb1 the strike angle about
			// the z-axis, and flatten onto xy plane [0,y,z]
			Rotation dRot = new Rotation(Vector3D.PLUS_K, -surfStrk);
			Vector3D dVec = dRot.applyTo(vb1);
			dVec = new Vector3D(0, dVec.getY(), dVec.getZ());
			surfDip = dVec.getDelta();

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

//			System.out.println("vt1: "+vt1);
//			System.out.println("vt2: "+vt2);
//			System.out.println("vb1: "+vb1);
//			System.out.println("vb2: "+vb2);
		}
	}

	private static void initSegmentsJB(double dip, double avgDipDirRad, double width,
			FaultTrace trace, List<Path2D> surfs) {
		// this is for distance JB
		Preconditions.checkState(!Double.isNaN(dip));
		Preconditions.checkState(!Double.isNaN(avgDipDirRad));
		Preconditions.checkState(!Double.isNaN(width));
		double avgDip = dip * TO_RAD; // avg dip of fault
		// now project width to the surface;
		width = width*Math.cos(avgDip);
		avgDip = 0;
		for (int i = 0; i < trace.size() - 1; i++) {

			Location p1 = trace.get(i);
			Location p2 = trace.get(i + 1);
			LocationVector vec = LocationUtils.vector(p1, p2);

			double surfStrk = vec.getAzimuthRad();
			double surfDip; // true dip of parallelogram
			double p1p2Dist = vec.getHorzDistance();

			// top trace #1 is at [0,0]
			Vector3D vt1 = Vector3D.ZERO;

			// top trace #2
			Vector3D vt2 = new Vector3D(p1p2Dist, new Vector3D(surfStrk, 0));

			// bottom trace #1
			Vector3D vb1 = new Vector3D(width, new Vector3D(avgDipDirRad, avgDip));

			// bottom trace #2
			Vector3D vb2 = new Vector3D(1, vt2, 1, vb1);
			
			// now make sure everything is indeed still at the surface
			Preconditions.checkState(vt2.getZ() == 0 && vb1.getZ() == 0 && vb2.getZ() == 0);

			// set up for 2D ops in yz plane
			Path2D surface = new Path2D.Double();
			surface.moveTo(vt1.getX(), vt1.getY());
			surface.lineTo(vt2.getX(), vt2.getY());
			if (dip < 90) {
				// only need line at the top for vertical
				surface.lineTo(vb2.getX(), vb2.getY());
				surface.lineTo(vb1.getX(), vb1.getY());
				surface.lineTo(vt1.getX(), vt1.getY());
			}
			surfs.add(surface);

//			System.out.println("vt1: "+vt1);
//			System.out.println("vt2: "+vt2);
//			System.out.println("vb1: "+vb1);
//			System.out.println("vb2: "+vb2);
		}
	}

	/**
	 * Returns the minimum distance to the surface.
	 * @param loc of interest
	 * @return the minimum distance
	 */
	public double getDistanceRup(Location loc) {
		synchronized (trace) {
			if (loc.equals(siteLocForDistRupCalc))
				return distanceRup;
			distanceRup = distance3D(trace, rots, surfs, loc);
			siteLocForDistRupCalc = loc;
			return distanceRup;
		}
	}

	public double getDistanceJB(Location loc) {
		if (proj_trace == null) {
			synchronized(this) {
				if (proj_trace == null) {
					// surface projection for calculating distance JB
					proj_trace = new FaultTrace("surface projection");
					for (Location traceLoc : trace)
						proj_trace.add(new Location(traceLoc.getLatitude(), traceLoc.getLongitude()));
					proj_surfs = new ArrayList<Path2D>();
					initSegmentsJB(dip, avgDipDirRad, width, trace, proj_surfs);
				}
			}
		}
		synchronized (proj_trace) {
			if (loc.equals(siteLocForDistJBCalc))
				return distanceJB;
			distanceJB = distance3D(proj_trace, null, proj_surfs, new Location(loc.getLatitude(), loc.getLongitude()));
			siteLocForDistJBCalc = loc;
			return distanceJB;
		}
	}

	public double getDistanceSeis(Location loc) {
		if (seis_trace == null) {
			synchronized(this) {
				if (seis_trace == null) {
					if (traceBelowSeis) {
						// it's already below the seismogenic depth, use normal trace/rots/surfs
						seis_trace = trace;
						seis_rots = rots;
						seis_surfs = surfs;
					} else {
						seis_trace = new FaultTrace("seis depth trace");
						for (Location traceLoc : trace) {
							double depth = traceLoc.getDepth();
							if (depth < DistanceSeisParameter.SEIS_DEPTH)
								depth = DistanceSeisParameter.SEIS_DEPTH;
							seis_trace.add(new Location(traceLoc.getLatitude(), traceLoc.getLongitude(), depth));
						}
						seis_rots = new ArrayList<Rotation>();
						seis_surfs = new ArrayList<Path2D>();
						
						// new width below seis
						double widthBelowSeis;
						if (avgUpperDepth < DistanceSeisParameter.SEIS_DEPTH)
							widthBelowSeis = width - (DistanceSeisParameter.SEIS_DEPTH - avgUpperDepth);
						else
							widthBelowSeis = width;
						initSegments(dip, avgDipDirRad, widthBelowSeis, seis_trace, seis_rots, seis_surfs);
					}
				}
			}
		}
		if (traceBelowSeis)
			return getDistanceRup(loc);
		synchronized (seis_trace) {
			if (loc.equals(siteLocForDistSeisCalc))
				return distanceSeis;
			distanceSeis = distance3D(seis_trace, seis_rots, seis_surfs, new Location(loc.getLatitude(), loc.getLongitude()));
			siteLocForDistSeisCalc = loc;
			return distanceSeis;
		}
	}
	
	private static double distance3D(FaultTrace trace, List<Rotation> rots, List<Path2D> surfs, Location loc) {
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < trace.size() - 1; i++) {
			if (D) System.out.println("Calc dist for trace pt "+i);
			// compute geographic vector to point
			LocationVector vec = LocationUtils.vector(trace.get(i), loc);
			// convert to cartesian
			Vector3D vp = new Vector3D(vec.getHorzDistance(), new Vector3D(
				vec.getAzimuthRad(), 0), vec.getVertDistance(), Vector3D.PLUS_K);
			if (rots != null)
				// rotate
				vp = rots.get(i).applyTo(vp);
			// compute distance
			Path2D surf = surfs.get(i);
			if (surf.contains(vp.getX(), vp.getY())) {
				if (D) System.out.println("Contained! Z dist: "+vp.getZ());
				distance = Math.min(distance, Math.abs(vp.getZ()));
			} else {
				if (D) System.out.println("Outside! dist: "+distanceToSurface(vp, surf));
				distance = Math.min(distance, distanceToSurface(vp, surf));
			}
			if (D) {
				System.out.flush();
				showDebugGraphIgnoreError(surf, vp, true);
			}
			if (distance == 0)
				return 0;
		}
		if (Double.isNaN(distance)) {
			for (int i = 0; i < trace.size() - 1; i++) {
				if (rots != null)
					System.out.println(rots.get(i).getAngle());
				System.out.println(surfs.get(i));
			}
		}
		Preconditions.checkState(!Double.isNaN(distance));
		return distance;
	}
	
	/*
	 * this will prevent headless exceptions if debug is enabled in a headless env
	 */
	private static void showDebugGraphIgnoreError(Path2D surf, Vector3D vp, boolean waitForClose) {
		try {
			showDebugGraph(surf, vp, waitForClose);
		} catch (Exception e) {}
	}
	
	private static void showDebugGraph(Path2D surf, Vector3D vp, boolean waitForClose) {
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		PathIterator pit = surf.getPathIterator(null);
		double[] c = new double[6]; // coordinate array
		double[] prev_pt = new double[2]; // previous coordinate array
		while (!pit.isDone()) {
			int type = pit.currentSegment(c);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				// do nothing, this is just resetting the current location. not a line
				break;
			case PathIterator.SEG_LINETO:
				// this defines a line, check the distance
				DefaultXY_DataSet xy = new DefaultXY_DataSet();
				xy.set(prev_pt[0], prev_pt[1]);
				xy.set(c[0], c[1]);
				funcs.add(xy);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
				break;

			default:
				throw new IllegalStateException("unkown path operation: "+type);
			}
			// this will set the previous location
			prev_pt[0] = c[0];
			prev_pt[1] = c[1];
			pit.next();
		}
		if (vp != null) {
			DefaultXY_DataSet xy = new DefaultXY_DataSet();
			xy.set(vp.getX(), vp.getY());
			funcs.add(xy);
			Color col;
			if (surf.contains(vp.getX(), vp.getY()))
				col = Color.GREEN;
			else
				col = Color.RED;
			chars.add(new PlotCurveCharacterstics(PlotSymbol.X, 6f, col));
		}
		GraphWindow gw = new GraphWindow(funcs, "Surface Debug", chars);
		// now wait until closed
		while (waitForClose && gw.isVisible()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}

	/*
	 * Iterates over surface outline path calculating distance to line segments
	 * and returning the minimum.
	 */
	private static double distanceToSurface(Vector3D p, Path2D border) {
		PathIterator pit = border.getPathIterator(null);
		double[] c = new double[6]; // coordinate array
		double[] prev_pt = new double[2];
		double minDistSq = Double.MAX_VALUE;
		while (!pit.isDone()) {
			// this puts the current location in the first two elements of c
			int type = pit.currentSegment(c);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				// do nothing, this is just resetting the current location. not a line
				break;
			case PathIterator.SEG_LINETO:
				// this defines a line, check the distance
				double distSq = Line2D.ptSegDistSq(prev_pt[0], prev_pt[1], c[0], c[1],
						p.getX(), p.getY());
				minDistSq = Math.min(minDistSq, distSq);
				break;

			default:
				throw new IllegalStateException("unkown path operation: "+type);
			}
			// this will set the previous location
			prev_pt[0] = c[0];
			prev_pt[1] = c[1];
			pit.next();
		}
		return Math.sqrt(p.getZ() * p.getZ() + minDistSq);
	}
	
	public synchronized double getDistanceX(Location siteLoc) {
		if (siteLoc.equals(siteLocForDistXCalc))
			return distanceX;
		siteLocForDistXCalc = siteLoc;
		// TODO do it right
		distanceX =  GriddedSurfaceUtils.getDistanceX(getEvenlyDiscritizedUpperEdge(), siteLoc);
		return distanceX;
//		double distance = Double.MAX_VALUE;
//		for (int i = 0; i < trace.size() - 1; i++) {
//			// compute geographic vector to point
//			LocationVector vec = LocationUtils.vector(trace.get(i), loc);
//			// convert to cartesian
//			Vector3D vp = new Vector3D(vec.getHorzDistance(), new Vector3D(
//				vec.getAzimuthRad(), 0), vec.getVertDistance(), Vector3D.PLUS_K);
//			// rotate
//			vp = rots.get(i).applyTo(vp);
//			// compute distance
//			Path2D surf = surfs.get(i);
//			Line2D.pt
//			if (surf.contains(vp.getX(), vp.getY())) {
//				distance = Math.min(distance, Math.abs(vp.getZ()));
//			} else {
//				distance = Math.min(distance, distanceToSurface(vp, surf));
//			}
//		}
//		return distance;
	}
	
//	private EvenlyGriddedSurface getGridded() {
//		if (gridSurf == null) {
//			double lower = avgUpperDepth + width;
//			gridSurf = new StirlingGriddedSurface(trace, dip, avgUpperDepth, lower, discr_km);
//		}
//		return gridSurf;
//	}

	@Override
	public double getAveDip() {
		return dip;
	}

	@Override
	public double getAveStrike() {
		return trace.getAveStrike();
	}

	@Override
	public double getAveLength() {
		return trace.getTraceLength();
	}

	@Override
	public double getAveWidth() {
		return width;
	}

	@Override
	public double getArea() {
		return getAveLength()*getAveWidth();
	}

	@Override
	public LocationList getEvenlyDiscritizedListOfLocsOnSurface() {
		LocationList locs = new LocationList();
		locs.addAll(getEvenlyDiscritizedUpperEdge());
		int numSpans = (int)Math.ceil(trace.getTraceLength()/discr_km);
		int numDDW = (int)Math.ceil(width/discr_km);
		double ddw_increment = width/(double)numDDW;
		for (int i=0; i<numDDW; i++) {
			FaultTrace subTrace = new FaultTrace("subTrace");
			subTrace.addAll(getHorizontalPoints(ddw_increment*(double)(i+1)));
			locs.addAll(FaultUtils.resampleTrace(subTrace, numSpans));
		}
		return locs;
	}

	@Override
	public ListIterator<Location> getLocationsIterator() {
		return getEvenlyDiscritizedListOfLocsOnSurface().listIterator();
	}

	@Override
	public LocationList getEvenlyDiscritizedPerimeter() {
		// build permineter
		LocationList perim = new LocationList();
		LocationList upper = getEvenlyDiscritizedUpperEdge();
		LocationList lower = getEvenlyDiscritizedLowerEdge();
		// top, forwards
		perim.addAll(upper);
		// "right"
		perim.addAll(GriddedSurfaceUtils.getEvenlyDiscretizedLine(upper.last(), lower.last(), discr_km));
		// bottom, backwards
		perim.addAll(getReversed(lower));
		// "left"
		perim.addAll(GriddedSurfaceUtils.getEvenlyDiscretizedLine(lower.first(), upper.first(), discr_km));
		return perim;
	}

	@Override
	public FaultTrace getEvenlyDiscritizedUpperEdge() {
		// TODO cache these?
		int numSpans = (int)Math.ceil(trace.getTraceLength()/discr_km);
		return FaultUtils.resampleTrace(trace, numSpans);
	}

	@Override
	public LocationList getEvenlyDiscritizedLowerEdge() {
		FaultTrace lower = new FaultTrace("lower");
		lower.addAll(getHorizontalPoints(width));
		int numSpans = (int)Math.ceil(lower.getTraceLength()/discr_km);
		return FaultUtils.resampleTrace(lower, numSpans);
	}

	@Override
	public double getAveGridSpacing() {
		return discr_km;
	}
	
	/**
	 * Sets grid spacing used for all evenly discretized methods
	 * @param gridSpacing
	 */
	public void setAveGridSpacing(double gridSpacing) {
		this.discr_km = gridSpacing;
	}

	@Override
	public double getAveRupTopDepth() {
		return avgUpperDepth;
	}

	@Override
	public double getAveDipDirection() {
		return Math.toDegrees(avgDipDirRad);
	}

	@Override
	public FaultTrace getUpperEdge() {
		return trace;
	}

	@Override
	public LocationList getPerimeter() {
		// build permineter
		LocationList perim = new LocationList();
		// top, forwards
		for (Location loc : trace)
			perim.add(loc);
		// bottom, backwards
		perim.addAll(getReversed(getHorizontalPoints(width)));
		return perim;
	}
	
	private static LocationList getReversed(LocationList locs) {
		LocationList reversed = new LocationList();
		for (int i=locs.size(); --i>=0;)
			reversed.add(locs.get(i));
		return reversed;
	}
	
	/**
	 * This returns basically a fault trace, but at the given depth down dip
	 * of the fault. If width is passed in, the bottom trace is given.
	 * 
	 * Points given in same order as top fault trace.
	 * @param depthDownDip
	 * @return
	 */
	private LocationList getHorizontalPoints(double depthDownDip) {
		LocationList locs = new LocationList();
		double dipRad = Math.toRadians(dip);
		double hDistance = depthDownDip * Math.cos( dipRad );
		double vDistance = depthDownDip * Math.sin(dipRad);
		LocationVector dir;
		for (Location traceLoc : trace) {
			dir = new LocationVector(avgDipDirRad, hDistance, vDistance);
			locs.add(LocationUtils.location(traceLoc, dir));
		}
		return locs;
	}

	@Override
	public Location getFirstLocOnUpperEdge() {
		return trace.get(0);
	}

	@Override
	public Location getLastLocOnUpperEdge() {
		return trace.last();
	}

	@Override
	public double getFractionOfSurfaceInRegion(Region region) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPointSurface() {
		return false;
	}

	@Override
	public double getMinDistance(RuptureSurface surface) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not yet implemented");
	}
	
	private static Location getTestLoc(boolean randomize) {
		if (randomize)
			return new Location(34d + Math.random(), -120d + Math.random());
		return new Location(34d, -120d);
	}

	public static void main(String[] args) throws IOException, DocumentException {
//		double depth = 0;
//		Location l1 = new Location(34.0, -118.0, depth);
//		Location l2 = new Location(34.1, -117.9, depth);
//		Location l3 = new Location(34.3, -117.8, depth);
//		Location l4 = new Location(34.4, -117.7, depth);
//		Location l5 = new Location(34.5, -117.5, depth);
//
//		FaultTrace ft = new FaultTrace("Test");
//		ft.add(l1);
//		ft.add(l2);
//		ft.add(l3);
//		ft.add(l4);
//		ft.add(l5);
//
//		// double stk = 35;
//		double dip = 5;
//		double wid = 15;
//		QuadSurface dt = new QuadSurface(ft, dip, wid);
//
//		Location p = new Location(34.0, -117.9);
//		System.out.println(dt.getDistanceRup(p));
//		p = new Location(34.2, -117.8);
//		System.out.println(dt.getDistanceRup(p));
//
//		p = new Location(34.3, -117.7);
//		System.out.println(dt.getDistanceRup(p));
//
//		p = new Location(34.4, -117.6);
//		System.out.println(dt.getDistanceRup(p));
		
		double topDepth = 0d;
		double width = 10d;
		double dip = 10;
//		Location l1 = new Location(34.0, -118.0, topDepth);
//		Location l2 = new Location(34.1, -117.9, topDepth);
		Location l1 = new Location(34.0, -118.0, topDepth);
		Location l2 = new Location(34.1, -118.0, topDepth);
//		Location l1 = new Location(0.00, 0.00, topDepth);
//		Location l2 = new Location(0.01, 0.01, topDepth);

		FaultTrace ft = new FaultTrace("Test");
		ft.add(l1);
		ft.add(l2);
		
		QuadSurface q = new QuadSurface(ft, dip, width);
		
//		PathIterator pit = q.surfs.get(0).getPathIterator(null);
//		double[] c = new double[6]; // coordinate array
//		double minDistSq = Double.MAX_VALUE;
//		double[] prev_c = null;
//		while (!pit.isDone()) {
//			int ret = pit.currentSegment(c);
//			System.out.println("PIT iter. ret="+ret);
//			System.out.println("C: ["+Joiner.on(",").join(Doubles.asList(c))+"]");
//			Preconditions.checkState(c[2] == 0 && c[3] == 0, "this should fail but isn't yet for unknown reasons");
//			pit.next();
//		}
		
//		showDebugGraph(q.surfs.get(0), null, false);
		D = true;
		double d12 = LocationUtils.horzDistanceFast(l1, l2);
		LocationVector v12 = LocationUtils.vector(l1, l2);
		Location middle12 = LocationUtils.location(l1, v12.getAzimuthRad(), 0.5*d12);
		double dipDirRad = ft.getDipDirection()*TO_RAD;
		Location onDipOffMiddle = LocationUtils.location(middle12, dipDirRad, 0.1*d12);
		System.out.println("Dip dir: "+ft.getDipDirection());
//		q.getDistanceRup(l2);
		q.getDistanceJB(onDipOffMiddle);
		
		
//		testPlanar();
	}
	
	private static void testPlanar() throws IOException {
		Location startLoc = new Location (34, -120);
		
		final int num_calcs = 100000;
		
		double[] test_lengths = { 10d, 50d, 100d, 200d, 500d, 1000d };
		long[] point_counts = new long[test_lengths.length];
		
		ArbitrarilyDiscretizedFunc[] quadFuncs = new ArbitrarilyDiscretizedFunc[5];
		ArbitrarilyDiscretizedFunc[] griddedFuncs = new ArbitrarilyDiscretizedFunc[5];
		
		for (int i=0; i<quadFuncs.length; i++) {
			quadFuncs[i] = new ArbitrarilyDiscretizedFunc();
			griddedFuncs[i] = new ArbitrarilyDiscretizedFunc();
		}
		
		String[] dist_labels = { "Rup", "JB", "Seis", "X", "Combined" };
		
		for (int l=0; l<test_lengths.length; l++) {
			double length = test_lengths[l];
			Location endLoc = LocationUtils.location(startLoc, Math.PI*0.5, length);
			FaultTrace trace = new FaultTrace("trace");
			trace.add(startLoc);
			trace.add(endLoc);
			FaultSectionPrefData prefData = new FaultSectionPrefData();
			prefData.setAveDip(80);
			prefData.setAveLowerDepth(10d);
			prefData.setAveUpperDepth(0d);
			prefData.setAseismicSlipFactor(0d);
			prefData.setFaultTrace(trace);
			RuptureSurface gridded = prefData.getStirlingGriddedSurface(1d, false, false);
			point_counts[l] = ((EvenlyGriddedSurface)gridded).size();
			RuptureSurface quad = prefData.getQuadSurface(false);
			
			// initialize without the timer
			runTest(1, quad);
			runTest(1, gridded);
			System.out.println("Calculating for length "+length +" ("+point_counts[l]+" pts)");
			long[] quad_times = runTest(num_calcs, quad);
			long[] gridded_times = runTest(num_calcs, gridded);
			
			for (int i=0; i<quad_times.length; i++)
				quadFuncs[i].set(length, (double)quad_times[i]/1000d);
			for (int i=0; i<gridded_times.length; i++)
				griddedFuncs[i].set(length, (double)gridded_times[i]/1000d);
		}
		
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		List<PlotSpec> specs = Lists.newArrayList();
		for (int i=0; i<dist_labels.length; i++) {
			List<ArbitrarilyDiscretizedFunc> funcs = Lists.newArrayList();
			funcs.add(quadFuncs[i]);
			funcs.add(griddedFuncs[i]);
			PlotSpec spec = new PlotSpec(funcs, chars, "Distance Calculation Speed", "Fault Length (km)",
					"Time for "+num_calcs+" Distance "+dist_labels[i]+" calcs");
			specs.add(spec);
		}
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.drawGraphPanel(specs, false, false, null, null);
		gp.getCartPanel().setSize(1000, 1500);
		gp.setBackground(Color.WHITE);
		gp.saveAsPNG("/tmp/dist_benchmarks_by_length.png");
		
		specs = Lists.newArrayList();
		for (int i=0; i<dist_labels.length; i++) {
			ArbitrarilyDiscretizedFunc quadFunc = new ArbitrarilyDiscretizedFunc();
			ArbitrarilyDiscretizedFunc griddedFunc = new ArbitrarilyDiscretizedFunc();
			for (int j=0; j<point_counts.length; j++) {
				quadFunc.set((double)point_counts[j], quadFuncs[i].getY(j));
				griddedFunc.set((double)point_counts[j], griddedFuncs[i].getY(j));
			}
			List<ArbitrarilyDiscretizedFunc> funcs = Lists.newArrayList();
			funcs.add(quadFunc);
			funcs.add(griddedFunc);
			PlotSpec spec = new PlotSpec(funcs, chars, "Distance Calculation Speed", "# Gridded Points",
					"Time for "+num_calcs+" Distance "+dist_labels[i]+" calcs");
			specs.add(spec);
		}
		gp = new HeadlessGraphPanel();
		gp.drawGraphPanel(specs, false, false, null, null);
		gp.getCartPanel().setSize(1000, 1500);
		gp.setBackground(Color.WHITE);
		gp.saveAsPNG("/tmp/dist_benchmarks_by_pts.png");
	}
	
	private static long[] runTest(int num_calcs, RuptureSurface surf) {
		long[] ret = new long[5];
		
		// distance rup
		Stopwatch watch = new Stopwatch();
		watch.start();
		for (int i=0; i<num_calcs; i++) {
			Location loc = getTestLoc(true);
			surf.getDistanceRup(loc);
		}
		watch.stop();
		ret[0] = watch.elapsed(TimeUnit.MILLISECONDS);
		
		// distance JB
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<num_calcs; i++) {
			Location loc = getTestLoc(true);
			surf.getDistanceJB(loc);
		}
		watch.stop();
		ret[1] = watch.elapsed(TimeUnit.MILLISECONDS);
		
		// distance Seis
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<num_calcs; i++) {
			Location loc = getTestLoc(true);
			surf.getDistanceSeis(loc);
		}
		watch.stop();
		ret[2] = watch.elapsed(TimeUnit.MILLISECONDS);
		
		// distance X
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<num_calcs; i++) {
			Location loc = getTestLoc(true);
			surf.getDistanceX(loc);
		}
		watch.stop();
		ret[3] = watch.elapsed(TimeUnit.MILLISECONDS);
		
		// combined
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<num_calcs; i++) {
			Location loc = getTestLoc(true);
			surf.getDistanceRup(loc);
			surf.getDistanceJB(loc);
			surf.getDistanceSeis(loc);
			surf.getDistanceX(loc);
		}
		watch.stop();
		ret[4] = watch.elapsed(TimeUnit.MILLISECONDS);
		
		return ret;
	}
	
	private static void testUCERF3calcs() throws IOException, DocumentException {
		// ok now the real test, FSS
		FaultSystemSolution sol = FaultSystemIO.loadSol(new File(
				new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		
		boolean useQuad = false;
		boolean randomizeLoc = true;
		System.gc();
		System.out.println("Loaded, waiting");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {}
		System.out.println("Building surfaces");
		Stopwatch watch = new Stopwatch();
		watch.start();
		List<RuptureSurface> surfs = Lists.newArrayList();
		for (int r=0; r<sol.getRupSet().getNumRuptures(); r++) {
			surfs.add(sol.getRupSet().getSurfaceForRupupture(r, 1d, useQuad));
		}
		watch.stop();
		System.out.println("Done building surfaces: "+(float)(watch.elapsed(TimeUnit.MILLISECONDS)/1000d)+" s");
		System.gc();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<surfs.size(); i++) {
			surfs.get(i).getDistanceRup(getTestLoc(randomizeLoc));
		}
		watch.stop();
		System.out.println("Distance Rup: "+(float)(watch.elapsed(TimeUnit.MILLISECONDS)/1000d)+" s");
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<surfs.size(); i++) {
			surfs.get(i).getDistanceJB(getTestLoc(randomizeLoc));
		}
		watch.stop();
		System.out.println("Distance JB: "+(float)(watch.elapsed(TimeUnit.MILLISECONDS)/1000d)+" s");
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<surfs.size(); i++) {
			surfs.get(i).getDistanceSeis(getTestLoc(randomizeLoc));
		}
		watch.stop();
		System.out.println("Distance Seis: "+(float)(watch.elapsed(TimeUnit.MILLISECONDS)/1000d)+" s");
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<surfs.size(); i++) {
			surfs.get(i).getDistanceX(getTestLoc(randomizeLoc));
		}
		watch.stop();
		System.out.println("Distance X: "+(float)(watch.elapsed(TimeUnit.MILLISECONDS)/1000d)+" s");
		
		// now do it with the same location, calculating each
		watch = new Stopwatch();
		watch.start();
		for (int i=0; i<surfs.size(); i++) {
			Location testLoc = getTestLoc(randomizeLoc);
			surfs.get(i).getDistanceRup(testLoc);
			surfs.get(i).getDistanceJB(testLoc);
			surfs.get(i).getDistanceSeis(testLoc);
			surfs.get(i).getDistanceX(testLoc);
		}
		watch.stop();
		System.out.println("Distance combined for each rup: "+(float)(watch.elapsed(TimeUnit.MILLISECONDS)/1000d)+" s");
		
		
//		System.out.println("done, waiting on profiling");
//		System.gc();
//		try {
//			Thread.sleep(50000);
//		} catch (InterruptedException e) {}
	}

}
