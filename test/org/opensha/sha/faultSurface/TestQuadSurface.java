package org.opensha.sha.faultSurface;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TestQuadSurface {
	
	private static FaultTrace straight_trace;
	private static FaultTrace straight_trace_gridded;
	private static FaultTrace jagged_trace;
	private static FaultTrace jagged_trace_gridded;
	
	private static Location start_loc = new Location(34, -119);
	private static Location end_loc = new Location(35, -117);
	
	private static final double grid_disc = 0.2d;
	private static final double test_trace_radius = 200d;
	
	private static Random r = new Random();;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		straight_trace = new FaultTrace("stright");
		straight_trace.add(start_loc);
		straight_trace.add(end_loc);
		
		jagged_trace = new FaultTrace("jagged");
		jagged_trace.add(start_loc);
		jagged_trace.add(new Location(34.3, -118.8));
		jagged_trace.add(new Location(34.4, -118.0));
		jagged_trace.add(new Location(34.8, -117.7));
		jagged_trace.add(new Location(34.6, -117.3));
		jagged_trace.add(end_loc);
		
		straight_trace_gridded = FaultUtils.resampleTrace(straight_trace, (int)Math.round(grid_disc/grid_disc));
		jagged_trace_gridded = FaultUtils.resampleTrace(jagged_trace, (int)Math.round(grid_disc/grid_disc));
	}
	
	/**
	 * this returns a location that is a random distance up to test_trace_radius from a random
	 * point in this gridded fault trace.
	 * @param gridded
	 * @return
	 */
	private static Location getRandomTestLoc(FaultTrace gridded) {
		Location p = gridded.get(r.nextInt(gridded.size()));
		p = new Location(p.getLatitude(), p.getLongitude());
		return LocationUtils.location(p, 2d*Math.PI*r.nextDouble(), test_trace_radius*r.nextDouble());
	}
	
	private static FaultSectionPrefData buildFSD(FaultTrace trace, double upper, double lower, double dip) {
		FaultSectionPrefData fsd = new FaultSectionPrefData();
		fsd.setFaultTrace(trace);
		fsd.setAveUpperDepth(upper);
		fsd.setAveLowerDepth(lower);
		fsd.setAveDip(dip);
		fsd.setDipDirection((float) trace.getDipDirection());
		return fsd;
	}
	
	private enum Dist {
		RUP,
		JB,
		SEIS,
		X;
	}
	
	private static double getDist(RuptureSurface surf, Location loc, Dist dist) {
		switch (dist) {
		case RUP:
			return surf.getDistanceRup(loc);
		case JB:
			return surf.getDistanceJB(loc);
		case SEIS:
			return surf.getDistanceSeis(loc);
		case X:
			return surf.getDistanceX(loc);

		default:
			throw new IllegalStateException();
		}
	}
	
	private static void runTest(FaultSectionPrefData fsd, FaultTrace gridded_trace,
			Dist dist, int num, double tol) {
		// tolerance is in percents
		RuptureSurface gridded = fsd.getStirlingGriddedSurface(grid_disc, false, false);
		RuptureSurface quad = fsd.getQuadSurface(false, grid_disc);
		
		for (int i=0; i<num; i++) {
			Location testLoc = getRandomTestLoc(gridded_trace);
			double dist_gridded = getDist(gridded, testLoc, dist);
			double dist_quad = getDist(quad, testLoc, dist);
			
			double diff = Math.abs(dist_quad-dist_gridded);
			double pDiff = DataUtils.getPercentDiff(dist_quad, dist_gridded);
			
			if (dist_gridded < 3d*grid_disc || diff < 0.5*test_trace_radius)
				// too close to the points for the test to be accurate
				continue;
			
			assertTrue(fsd.getFaultTrace().getName()+" "+dist+" calc outside tolerance:\tgrd="
					+dist_gridded+"\tquad="+dist_quad+"\tdiff="+diff+"\tpDiff="+pDiff+"%", pDiff <= tol);
		}
	}

	@Test
	public void testDistanceRup() {
		// note - only tests with site on surface
		
		Dist dist = Dist.RUP;
		int num = 1000;
		double tol = 0.5d;
		
		// simple vertical fault
		runTest(buildFSD(straight_trace, 0d, 10d, 90), straight_trace_gridded, dist, num, tol);
		
		// simple dipping fault
		runTest(buildFSD(straight_trace, 0d, 10d, 45), straight_trace_gridded, dist, num, tol);
		
		// complex vertical fault
		runTest(buildFSD(jagged_trace, 0d, 10d, 90), jagged_trace_gridded, dist, num, tol);
		
		// complex dipping fault
		runTest(buildFSD(jagged_trace, 0d, 10d, 45), jagged_trace_gridded, dist, num, tol);
	}

	@Test
	public void testDistanceSeis() {
		// note - only tests with site on surface
		
		Dist dist = Dist.SEIS;
		int num = 1000;
		double tol = 0.5d;
		
		// complex vertical fault above
		runTest(buildFSD(jagged_trace, 0d, 10d, 90), jagged_trace_gridded, dist, num, tol);
		
		// complex vertical fault below
		runTest(buildFSD(jagged_trace, 4d, 10d, 90), jagged_trace_gridded, dist, num, tol);
		
		// complex dipping fault above
		runTest(buildFSD(jagged_trace, 0d, 10d, 45), jagged_trace_gridded, dist, num, tol);
		
		// complex dipping fault below
		runTest(buildFSD(jagged_trace, 4d, 10d, 45), jagged_trace_gridded, dist, num, tol);
	}

	@Test
	public void testDistanceJB() {
		// note - only tests with site on surface
		
		Dist dist = Dist.JB;
		int num = 1000;
		double tol = 0.5d;
		
		// complex vertical fault
		runTest(buildFSD(jagged_trace, 0d, 10d, 90), jagged_trace_gridded, dist, num, tol);
		
		// complex dipping fault
		runTest(buildFSD(jagged_trace, 0d, 10d, 45), jagged_trace_gridded, dist, num, tol);
		
		// complex dipping deeper fault
		runTest(buildFSD(jagged_trace, 4d, 10d, 45), jagged_trace_gridded, dist, num, tol);
		
		// now test for zeros on the surface. use gridded surface for test locations
		FaultSectionPrefData dipping = buildFSD(straight_trace, 0d, 10d, 30);
		EvenlyGriddedSurface griddedDipping = dipping.getStirlingGriddedSurface(1d, false, false);
		QuadSurface quadDipping = dipping.getQuadSurface(false);
		for (int row=1; row<griddedDipping.getNumRows()-1; row++) {
			for (int col=1; col<griddedDipping.getNumCols()-1; col++) {
				Location loc = griddedDipping.get(row, col);
				loc = new Location(loc.getLatitude(), loc.getLongitude());
				double qDist = quadDipping.getDistanceJB(loc);
				if (qDist > 1e-10) {
					// reset the cache
					quadDipping.getDistanceJB(new Location(Math.random(), Math.random()));
					QuadSurface.D = true;
					quadDipping.getDistanceJB(loc);
					QuadSurface.D = false;
				}
				Preconditions.checkState((float)griddedDipping.getDistanceJB(loc)==0f);
				assertEquals("Quad distJB isn't zero above surf: "+qDist+"\nloc: "+loc, 0d, qDist, 1e-10);
			}
		}
	}

	@Test
	public void testDistanceX() {
		// note - only tests with site on surface
		Dist dist = Dist.X;
		int num = 1000;
		double tol = 0.5d;
		
		// simple vertical fault
		runTest(buildFSD(straight_trace, 0d, 10d, 90), straight_trace_gridded, dist, num, tol);
		
		// simple dipping fault
		runTest(buildFSD(straight_trace, 0d, 10d, 45), straight_trace_gridded, dist, num, tol);
		
		// complex vertical fault
		runTest(buildFSD(jagged_trace, 0d, 10d, 90), jagged_trace_gridded, dist, num, tol);
		
		// complex dipping fault
		runTest(buildFSD(jagged_trace, 0d, 10d, 45), jagged_trace_gridded, dist, num, tol);
	}
	
	private static void runPlanarZeroZTest(Iterable<Location> locs, QuadSurface surf, double tol) {
		for (Location loc : locs) {
			Vector3D proj = surf.getRupProjectedPoint(0, loc);
			assertEquals(0d, proj.getZ(), tol);
		}
	}
	
	private static void runZeroTest(Iterable<Location> locs, QuadSurface surf, Dist dist, double tol) {
		for (Location loc : locs) {
			double surfDist = getDist(surf, loc, dist);
			assertEquals(0d, surfDist, tol);
		}
	}
	
	// TODO ramp up
	private static final double zero_tol = 1e-8;
	
	@Test
	public void testPerimeter() {
		QuadSurface surf = buildFSD(straight_trace, 0d, 10d, 90).getQuadSurface(false);
		runPlanarZeroZTest(surf.getPerimeter(), surf, zero_tol);
		runZeroTest(surf.getPerimeter(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(straight_trace, 0d, 10d, 45).getQuadSurface(false);
		runPlanarZeroZTest(surf.getPerimeter(), surf, zero_tol);
		runZeroTest(surf.getPerimeter(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 90).getQuadSurface(false);
		runZeroTest(surf.getPerimeter(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 45).getQuadSurface(false);
		runZeroTest(surf.getPerimeter(), surf, Dist.RUP, zero_tol);
	}
	
	@Test
	public void testGriddedPerimeter() {
		QuadSurface surf = buildFSD(straight_trace, 0d, 10d, 90).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedPerimeter(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedPerimeter(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(straight_trace, 0d, 10d, 45).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedPerimeter(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedPerimeter(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 90).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedPerimeter(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 45).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedPerimeter(), surf, Dist.RUP, zero_tol);
	}
	
	@Test
	public void testGriddedSurfLocs() {
		QuadSurface surf = buildFSD(straight_trace, 0d, 10d, 90).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedListOfLocsOnSurface(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedListOfLocsOnSurface(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(straight_trace, 0d, 10d, 45).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedListOfLocsOnSurface(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedListOfLocsOnSurface(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 90).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedListOfLocsOnSurface(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 45).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedListOfLocsOnSurface(), surf, Dist.RUP, zero_tol);
	}
	
	@Test
	public void testGriddedUpperLocs() {
		QuadSurface surf = buildFSD(straight_trace, 0d, 10d, 90).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedUpperEdge(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedUpperEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(straight_trace, 0d, 10d, 45).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedUpperEdge(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedUpperEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 90).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedUpperEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 45).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedUpperEdge(), surf, Dist.RUP, zero_tol);
	}
	
	@Test
	public void testGriddedLowerLocs() {
		QuadSurface surf = buildFSD(straight_trace, 0d, 10d, 90).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedLowerEdge(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedLowerEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(straight_trace, 0d, 10d, 45).getQuadSurface(false);
		runPlanarZeroZTest(surf.getEvenlyDiscritizedLowerEdge(), surf, zero_tol);
		runZeroTest(surf.getEvenlyDiscritizedLowerEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 90).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedLowerEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 45).getQuadSurface(false);
		runZeroTest(surf.getEvenlyDiscritizedLowerEdge(), surf, Dist.RUP, zero_tol);
	}
	
	@Test
	public void testUpperLocs() {
		QuadSurface surf = buildFSD(straight_trace, 0d, 10d, 90).getQuadSurface(false);
		runPlanarZeroZTest(surf.getUpperEdge(), surf, zero_tol);
		runZeroTest(surf.getUpperEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(straight_trace, 0d, 10d, 45).getQuadSurface(false);
		runPlanarZeroZTest(surf.getUpperEdge(), surf, zero_tol);
		runZeroTest(surf.getUpperEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 90).getQuadSurface(false);
		runZeroTest(surf.getUpperEdge(), surf, Dist.RUP, zero_tol);
		
		surf = buildFSD(jagged_trace, 0d, 10d, 45).getQuadSurface(false);
		runZeroTest(surf.getUpperEdge(), surf, Dist.RUP, zero_tol);
		
		List<Location> firstLast = Lists.newArrayList(surf.getFirstLocOnUpperEdge(), surf.getLastLocOnUpperEdge());
		runZeroTest(firstLast, surf, Dist.RUP, zero_tol);
	}

}

