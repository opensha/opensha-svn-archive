package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static org.opensha.sha.nshmp.SourceType.*;
import static org.opensha.sha.nshmp.SourceRegion.*;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.calc.magScalingRelations.MagScalingRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.Site;
import org.opensha.commons.geo.GeoTools;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultType;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.faultSurface.AbstractEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.AbstractEvenlyGriddedSurfaceWithSubsets;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.collect.Lists;

/**
 * This class represents the fault sources that get used 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class ClusterSource extends FaultSource {

	private static final MagScalingRelationship WCL; // W&C mag-length
	private static final MagScalingRelationship CAF; // CA floater

	// fields directly populated by parser
	FaultTrace trace;
	List<IncrementalMagFreqDist> mfds;
	SourceFile file;
	String name;
	FaultType type;
	FocalMech mech;
	int nMag;
	boolean floats;
	double dip;
	double width;
	double top;

	int size = 0;
	// TODO this should not be using abstract impl
	AbstractEvenlyGriddedSurfaceWithSubsets surface;

	List<FloatingPoissonFaultSource> sources;
	List<Integer> rupCount;

	// package private constructor
	ClusterSource() {}

	public void init() {
		// init fault surface
		double lowerSeis = top + width * Math.sin(dip * GeoTools.TO_RAD);
		surface = new StirlingGriddedSurface(trace, dip, top, lowerSeis, 1);
		// create a floating poisson source for each mfd
		if (mfds.size() == 0) return;
		sources = Lists.newArrayList();
		rupCount = Lists.newArrayList();
		MagScalingRelationship msr = ((file.getType() == FAULT) &&
			(file.getRegion() == CA) && floats) ? CAF : WCL;
		FloatingPoissonFaultSource source;
		for (IncrementalMagFreqDist mfd : mfds) {
			source = new FloatingPoissonFaultSource(mfd, // IncrementalMagFreqDist
				surface, // EvenlyGriddedSurface
				msr, // MagScalingRelationship
				0d, // sigma of the mag-scaling relationship
				1d, // floating rupture aspect ratio (length/width)
				5d, // floating rupture offset
				mech.rake(), // average rake of the ruptures
				1d, // duration of forecast
				0d, // minimum mag considered
				0, // type of floater (0 = full DDW, 1 = both, 2= centered)
				floats ? 10d : 0d); // mag above which full rup
			sources.add(source);
			int rups = source.getNumRuptures();
			size += rups;
			rupCount.add(size);
		}
	}

	@Override
	public LocationList getAllSourceLocs() {
		return null; //surface.getLocationList();
	}

	@Override
	public EvenlyGriddedSurface getSourceSurface() {
		return surface;
	}

	@Override
	public double getMinDistance(Site site) {
		if (sources == null || sources.size() == 0) return Double.NaN;
		return sources.get(0).getMinDistance(site);
	}

	@Override
	public int getNumRuptures() {
		return size;
	}

	// for now, ruptures are nested in sources which we iterate over
	@Override
	public ProbEqkRupture getRupture(int idx) {
		if (getNumRuptures() == 0) return null;
		for (int i = 0; i < rupCount.size(); i++) {
			int count = rupCount.get(i);
			if (idx < count) return sources.get(i).getRupture(idx - count);
		}
		return null; // shouldn't get here
	}

	@Override
	public Iterator<ProbEqkRupture> iterator() {
		// @formatter:off
		return new Iterator<ProbEqkRupture>() {
			int caret = 0;
			@Override public boolean hasNext() {
				return (caret < size) ? true : false;
			}
			@Override public ProbEqkRupture next() {
				return getRupture(caret++);
			}
			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		// @formatter:on
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new StringBuilder()
		.append("=============  Source  =============")
		.append(IOUtils.LINE_SEPARATOR)
		.append("   Fault name: ").append(name)
		.append(IOUtils.LINE_SEPARATOR)
		.append("         type: ").append(type)
		.append(IOUtils.LINE_SEPARATOR)
		.append("         mech: ").append(mech)
		.append(IOUtils.LINE_SEPARATOR)
		.append("         mags: ").append(nMag)
		.append(IOUtils.LINE_SEPARATOR)
		.append("         mfds: ").append(mfds.size())
		.append(IOUtils.LINE_SEPARATOR)
		.append("       floats: ").append(floats)
		.append(IOUtils.LINE_SEPARATOR)
		.append("          dip: ").append(dip)
		.append(IOUtils.LINE_SEPARATOR)
		.append("        width: ").append(width)
		.append(IOUtils.LINE_SEPARATOR)
		.append("          top: ").append(top)
		.append(IOUtils.LINE_SEPARATOR).toString();
		// @formatter:on
	}

	static {
		WCL = new WC1994_MagLengthRelationship();
		CAF = new CA_MagAreaRelationship();
	}

}
