package scratch.UCERF3.griddedSeismicity;

import java.awt.geom.Point2D;
import java.util.Map;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.geo.Location;
import org.opensha.nshmp2.erf.source.PointSource13b;
import org.opensha.nshmp2.util.FocalMech;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.collect.Maps;

import scratch.UCERF3.utils.GardnerKnopoffAftershockFilter;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public abstract class AbstractGridSourceProvider implements GridSourceProvider {

	private final WC1994_MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();
	private double[] fracStrikeSlip,fracNormal,fracReverse;
	private double ptSrcCutoff = 6.0;

	protected AbstractGridSourceProvider() {
		initFocalMechGrids();
	}
	
	@Override
	public int size() {
		return getGriddedRegion().getNodeCount();
	}

	private static final double[] DEPTHS = new double[] {5.0, 1.0};
	
	@Override
	public ProbEqkSource getSource(int idx, double duration,
			boolean filterAftershocks, BackgroundRupType bgRupType) {
		Location loc = getGriddedRegion().locationForIndex(idx);
		IncrementalMagFreqDist mfd = getNodeMFD(idx, 5.05);
		if (filterAftershocks) scaleMFD(mfd);

		switch (bgRupType) {
		case CROSSHAIR:
			return new Point2Vert_FaultPoisSource(loc, mfd, magLenRel, duration,
					ptSrcCutoff, fracStrikeSlip[idx], fracNormal[idx],
					fracReverse[idx], true);
		case FINITE:
			return new Point2Vert_FaultPoisSource(loc, mfd, magLenRel, duration,
					ptSrcCutoff, fracStrikeSlip[idx], fracNormal[idx],
					fracReverse[idx], false);
		case POINT:
			Map<FocalMech, Double> mechMap = Maps.newHashMap();
			mechMap.put(FocalMech.STRIKE_SLIP, fracStrikeSlip[idx]);
			mechMap.put(FocalMech.REVERSE, fracReverse[idx]);
			mechMap.put(FocalMech.NORMAL, fracNormal[idx]);
			return new PointSource13b(loc, mfd, duration, DEPTHS, mechMap);

		default:
			throw new IllegalStateException("Unknown Background Rup Type: "+bgRupType);
		}
		
	}
	
//	@Override
//	public void setAsPointSources(boolean usePoints) {
//		ptSrcCutoff = (usePoints) ? 10.0 : 6.0;
//	}


	/**
	 * Returns the MFD associated with a grid node trimmed to the supplied 
	 * minimum magnitude and the maximum non-zero magnitude.
	 * 
	 * @param idx node index
	 * @param minMag minimum magniitude to trim MFD to
	 * @return the trimmed MFD
	 */
	public IncrementalMagFreqDist getNodeMFD(int idx, double minMag) {
		return trimMFD(getNodeMFD(idx), minMag);
		
		// NOTE trimMFD clones the MFD returned by getNodeMFD so its safe for
		// subsequent modification; if this changes, then we need to review if
		// MFD is safe from alteration.
	}
	
	/**
	 * Returns the MFD associated with a grid node. This is the sum of any
	 * unassociated and sub-seismogenic MFDs for the node.
	 * @param idx node index
	 * @return the MFD
	 * @see UCERF3_GridSourceGenerator#getNodeUnassociatedMFD(int)
	 * @see UCERF3_GridSourceGenerator#getNodeSubSeisMFD(int)
	 */
	public IncrementalMagFreqDist getNodeMFD(int idx) {
		
		IncrementalMagFreqDist nodeIndMFD = getNodeUnassociatedMFD(idx);
		IncrementalMagFreqDist nodeSubMFD = getNodeSubSeisMFD(idx);
		if (nodeIndMFD == null) return nodeSubMFD;
		if (nodeSubMFD == null) return nodeIndMFD;
		
		SummedMagFreqDist sumMFD = initSummedMFD(nodeIndMFD);
		sumMFD.addIncrementalMagFreqDist(nodeSubMFD);
		sumMFD.addIncrementalMagFreqDist(nodeIndMFD);
		return sumMFD;
	}
	
	private static SummedMagFreqDist initSummedMFD(IncrementalMagFreqDist model) {
		return new SummedMagFreqDist(model.getMinX(), model.getMaxX(),
			model.getNum());
	}

	private void initFocalMechGrids() {
		GridReader gRead;
		gRead = new GridReader("StrikeSlipWts.txt");
		fracStrikeSlip = gRead.getValues();
		gRead = new GridReader("ReverseWts.txt");
		fracReverse = gRead.getValues();
		gRead = new GridReader("NormalWts.txt");
		fracNormal = gRead.getValues();
	}


	/*
	 * Applies gardner Knopoff aftershock filter scaling to MFD in place.
	 */
	private static void scaleMFD(IncrementalMagFreqDist mfd) {
		double scale;
		for (Point2D p : mfd) {
			scale = GardnerKnopoffAftershockFilter.scaleForMagnitude(p.getX());
			p.setLocation(p.getX(), p.getY() * scale);
			mfd.set(p);
		}
	}

	/*
	 * Utility to trim the supplied MFD to the supplied min mag and the maximum
	 * non-zero mag. This method makes the assumtions that the min mag of the 
	 * supplied mfd is lower then the mMin, and that mag bins are centered on
	 * 0.05.
	 */
	private static IncrementalMagFreqDist trimMFD(IncrementalMagFreqDist mfdIn, double mMin) {
		// in GR nofix branches there are mfds with all zero rates
		try {
			double mMax = mfdIn.getMaxMagWithNonZeroRate();
			int num = (int) ((mMax - mMin) / 0.1) + 1;
			IncrementalMagFreqDist mfdOut = new IncrementalMagFreqDist(mMin, mMax, num);
			for (int i=0; i<mfdOut.getNum(); i++) {
				double mag = mfdOut.getX(i);
				double rate = mfdIn.getY(mag);
				mfdOut.set(mag, rate);
			}
			return mfdOut;
		} catch (Exception e) {
			System.out.println("empty MFD");
			IncrementalMagFreqDist mfdOut = new IncrementalMagFreqDist(mMin,mMin,1);
			mfdOut.scaleToCumRate(mMin, 0.0);
			return mfdOut;
		}
	}



}
