package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static org.opensha.sha.nshmp.SourceRegion.*;
import static org.opensha.sha.nshmp.SourceType.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.util.MathUtils;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Utility for plotting mfds for faults or grid sources or both. Utility assumes
 * that magnitude range of interest in 4.55 to 8.05 with a delta of 0.1. Results
 * are undefined for MFDs that exceed this range.
 * 
 * This really only worksd for California
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class MFD_Plotter {

	// self consistent set of min, max, delta, and num
	private static final double M_MIN = 4.8;
	private static final double M_MAX = 8.0;
	private static final double M_DELTA = 0.05;
	private static final int M_NUM = 65;

	public static void main(String[] args) {
		List<SourceFile> grdSrcs = null;
		Location loc = null;
		List<SourceFile> fltSrcs = null;
		List<String> names = null;
		
		// grids
//		grdSrcs = Lists.newArrayList();
//		grdSrcs.addAll(SourceFileMgr.get(CA, GRIDDED, "CAmap.24.ch.in"));
//		grdSrcs.addAll(SourceFileMgr.get(CA, GRIDDED, "CAmap.24.gr.in"));
//		// grid cell spanning Sierra Madre Connected
//		loc = new Location(34.2, -118.1);

		// faults
		fltSrcs = Lists.newArrayList();
		fltSrcs.addAll(SourceFileMgr.get(CA, FAULT, "bFault.ch.in"));
		fltSrcs.addAll(SourceFileMgr.get(CA, FAULT, "bFault.gr.in"));
//		fltSrcs.addAll(SourceFileMgr.get(WUS, FAULT, "orwa_n.3dip.gr.in"));
//		fltSrcs.addAll(SourceFileMgr.get(WUS, FAULT, "orwa_n.3dip.ch.in"));
		names = Lists.newArrayList();
		names.add("Sierra Madre Connected");
//		names.add("856abcdef Steens fault zone");

		plot(grdSrcs, loc, fltSrcs, names);

	}

	public static void plot(List<SourceFile> grids, Location loc,
			List<SourceFile> faults, List<String> names) {
		
		Logger log = NSHMP_Utils.logger();
		Level level = Level.WARNING;
		log.setLevel(level);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(level);
		}

		ArrayList<IncrementalMagFreqDist> mfds = Lists.newArrayList();

		if (grids != null) {
			for (SourceFile sf : grids) {
				GridParser parser = new GridParser(log);
				parser.parse(sf);
				log.fine(parser.toString());
				GridERF gs = parser.createGridSource();
				IncrementalMagFreqDist mfd = gs.getMFD(loc);
				mfd.scale(sf.getWeight());
				mfds.add(expandGR(mfd));
				mfds.add(mfd);
			}
		}

		if (faults != null) {
			for (SourceFile sf : faults) {
				FaultParser parser = new FaultParser(log);
				FaultERF erf = parser.parseFault(sf);
				for (ProbEqkSource source : erf) {
					if (names.contains(source.getName())) {
						List<IncrementalMagFreqDist> list = ((FaultSource) source).mfds;
						mfds.addAll(list);
						for (IncrementalMagFreqDist mfd : list) {
							IncrementalMagFreqDist tmpMfd = null;
							tmpMfd = (mfd instanceof GaussianMagFreqDist)
								? expandCH((GaussianMagFreqDist) mfd)
								: expandGR(mfd);
							tmpMfd.scale(sf.getWeight());
							mfds.add(tmpMfd);
						}
					}
				}
			}
		}

		SummedMagFreqDist smfd = new SummedMagFreqDist(M_MIN, M_NUM, M_DELTA);
		for (IncrementalMagFreqDist mfd : mfds) {
			smfd.addIncrementalMagFreqDist(mfd);
		}
		mfds.add(smfd);

		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfds,
			"Test MFD PLot");
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Incremental Rate");
		graph.setYLog(true);
		graph.setY_AxisRange(1e-6, 1e-2);

	}

	/*
	 * This expands a GR mfd to the set min max for this utility and fills in
	 * medial values, but does not adjust rate as would normally be done when
	 * densifying and mfd. THis is done solely do facilitate addition of GR and
	 * CH curves
	 */
	private static IncrementalMagFreqDist expandGR(IncrementalMagFreqDist mfd) {

		System.out.println(mfd);
		
		Preconditions.checkArgument(mfd.getMinX() >= M_MIN,
			"Supplied MFD min mag too low [" + mfd.getMinX() + "]");
		Preconditions.checkArgument(mfd.getMaxX() <= M_MAX,
			"Supplied MFD max mag too high [" + mfd.getMaxX() + "]");
		Preconditions.checkArgument((int) (mfd.getDelta() * 10) == 1,
			"Supplied MFD delta != 0.1 [" + mfd.getDelta() + "]");
		Preconditions.checkArgument(((int) (mfd.getX(0) * 100)) % 5 == 0,
			"Supplied MFD not centered on 0.05");

		IncrementalMagFreqDist expanded = new IncrementalMagFreqDist(M_MIN,
			M_NUM, M_DELTA);
		expanded.setName(mfd.getName());
		expanded.setInfo(mfd.getInfo());
		Point2D prevPt = null;
		for (Point2D p : mfd) {
			expanded.set(p);
			if (prevPt != null) {
				Point2D newPt = interpolate(prevPt, p);
				expanded.set(newPt);
			}
			prevPt = p;
		}
		return expanded;
	}

	/*
	 * Adjusts (moves) supplied Gaussion CH distribution values to closest 0.05
	 * bin.
	 */
	private static IncrementalMagFreqDist expandCH(GaussianMagFreqDist mfd) {
		IncrementalMagFreqDist expanded = new IncrementalMagFreqDist(M_MIN,
			M_NUM, M_DELTA);
		expanded.setName(mfd.getName());
		expanded.setInfo(mfd.getInfo());
		for (Point2D p : mfd) {
			double newMag = adjustMag(p.getX());
			expanded.set(newMag, p.getY());
		}
		return expanded;
	}

	private static double adjustMag(double mag) {
		int intDelta = (int) (M_DELTA * 100);
		int intMag = (int) (MathUtils.round(mag, 2) * 100);
		int mod = intMag % intDelta;
		int intMagBase = intDelta * (intMag / intDelta);
		intMagBase = (mod > intDelta/2) ? intMagBase + intDelta : intMagBase;
		return intMagBase / 100.0;
	}
	
	/*
	 * Returns the midpoint between two points.
	 */
	private static Point2D interpolate(Point2D p1, Point2D p2) {
		double x = (p1.getX() + p2.getX()) / 2;
		double y = (p1.getY() + p2.getY()) / 2;
		return new Point2D.Double(x, y);
	}
}
