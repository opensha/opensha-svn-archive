package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static com.google.common.base.Preconditions.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultCode.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.RateType.*;
import static org.opensha.sha.nshmp.SourceRegion.*;
import static org.opensha.sha.nshmp.SourceType.*;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultCode;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.GridUtils;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.RateType;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.nshmp.Utils;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/*
 * Wrapper for grid source.
 */
class GridParser {

	private static Logger log;
	private static final String GRD_PATH = "../conf";
	private static final String DAT_PATH = "/resources/data/nshmp/sources/";

	// parsed
	File file;
	double minLat, maxLat, dLat;
	double minLon, maxLon, dLon;
	double[] depths;
	Map<FocalMech, Double> mechWtMap;
	// double[] mechWts;
	double dR, rMax;
	GR_Data grSrc;
	FaultCode fltCode;
	boolean bGrid, mMaxGrid, weightGrid;
	double mTaper;
	File aGridFile, bGridFile, mMaxGridFile, weightGridFile;
	double timeSpan;
	RateType rateType;
	double strike = Double.NaN;

	// generated
	double[] aDat, bDat, mMinDat, mMaxDat, wgtDat;
	GriddedRegion region;
	Map<Integer, IncrementalMagFreqDist> mfdMap;
	List<IncrementalMagFreqDist> mfdList;

	GridParser(Logger log) {
		this.log = log;
	}

	public void parse(SourceFile sf) {
		file = sf.getFile();
		List<String> dat = readLines(file, log);
		Iterator<String> it = dat.iterator();

		// grid of sites (1-30) or station list (0)
		int numSta = readInt(it.next(), 0);
		// skip stations or lat-lon bounds
		Iterators.skip(it, (numSta > 0) ? numSta : 2);
		// skip site data (Vs30) and Campbell basin depth
		it.next();
		// read rupture top data (num, [z, wt M<=6.5, wt M>6.5], ...)
		readRuptureTop(it.next());
		// read focal mech weights (SS, REVERSE, NORMAL)
		readMechWeights(it.next());
		// read gm lookup array parameters; delta R and R max
		readLookupArrayDat(it.next());

		// read source region dimensions
		readSourceLatRange(it.next());
		readSourceLonRange(it.next());

		// mag data
		grSrc = new GR_Data(it.next(), log);

		// iflt, ibmat, maxMat, Mtaper
		// iflt = 0 -> no finite faults
		// iflt = 1 -> apply finite fault corrections for M>6 assuming random
		// strike
		// iflt = 2 -> use finite line faults for M>6 and fix strike
		// iflt = 3 -> use finite faults with Johston mblg to Mw converter
		// iflt = 4 -> use finite faults with Boore and Atkinson mblg to Mw
		// converter
		// ibmax = 0 -> use specified b value
		// ibmax = 1 -> use b value matrix (provided in a file)
		// maxMat = 0 -> use specified maximum magnitude
		// maxMat = 1 -> use maximum magnitude matrix (provided in a file)
		// maxMat = -1 -> use as maximum magnitude the minimum between the
		// default and grid value
		String grdDatRaw = it.next();
		int[] grdDat = readInts(grdDatRaw, 3);
		fltCode = FaultCode.typeForID(grdDat[0]);
		bGrid = grdDat[1] > 0 ? true : false;
		mMaxGrid = grdDat[2] > 0 ? true : false;
		mTaper = readDouble(grdDatRaw, 3);
		weightGrid = mTaper > 0 ? true : false;

		if (bGrid) bGridFile = readSourceFile(it.next());
		if (mMaxGrid) mMaxGridFile = readSourceFile(it.next());
		if (weightGrid) weightGridFile = readSourceFile(it.next());
		aGridFile = readSourceFile(it.next());

		// read rate information if rateType is CUMULATIVE
		// it will require conversion to INCREMENTAL
		readRateInfo(it.next());

		// read strike or rjb array
		if (fltCode == FIXED) strike = readDouble(it.next(), 0);

		// done; skip atten rel config
	}

	public GridERF createGridSource() {
		region = new GriddedRegion(new Location(minLat, minLon), new Location(
			maxLat, maxLon), dLat, dLon, GriddedRegion.ANCHOR_0_0);
		mfdList = Lists.newArrayListWithCapacity(region.getNodeCount());
		initDataGrids();
		generateMFDs();

		// KLUDGY: need to post process CEUS grids to handle craton and
		// extended margin weighting grids
		if (file.getName().contains("2007all8")) {
			ceusScaleRates();
		}

		// GridSource gs = new GridSource(file.getName(), region, mfdList);
		GridERF gs = new GridERF(file.getName(), region, mfdList, depths,
			mechWtMap);
		return gs;
	}

	private void generateMFDs() {
		for (int i = 0; i < aDat.length; i++) {
			if (aDat[i] == 0) {
				mfdList.add(null);
				continue;
			}
			// use fixed value if mMax matrix value was 0
			double maxM = mMaxDat[i] <= 0 ? grSrc.mMax : mMaxDat[i];
			// a-value is stored as log10(a)
			GR_Data gr = new GR_Data(aDat[i], bDat[i], mMinDat[i], maxM,
				grSrc.dMag);
			// double minM = mMinDat[i] + gr.dMag / 2;
			// double maxM = mMaxDat[i] <= 0 ? gr.mMax : mMaxDat[i];
			// maxM -= gr.dMag/2;
			// int nMag = GR_Data.getMagCount(minM, maxM, gr.dMag);
			double tmr = totalMoRate(gr.mMin, gr.nMag, gr.dMag, gr.aVal,
				gr.bVal);
			// System.out.println(minM + " " + nMag + " " + gr.dMag);
			// System.out.println(region.locationForIndex(i) + " " + gr.aVal +
			// " " + gr.bVal + " " + gr.mMin + " " + gr.nMag + " " + gr.dMag);

			GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(
				gr.mMin, gr.nMag, gr.dMag);
			// set total moment rate
			mfd.setAllButTotCumRate(gr.mMin, gr.mMin + (gr.nMag - 1) * gr.dMag,
				tmr, gr.bVal);
			// apply weight
			if (weightGrid && mfd.getMaxX() >= mTaper) {
				int j = mfd.getXIndex(mTaper + grSrc.dMag / 2);
				for (; j < mfd.getNum(); j++)
					mfd.set(j, mfd.getY(j) * wgtDat[i]);
			}
			mfdList.add(i, mfd);
		}
	}

	private void initDataGrids() {
		int nRows = (int) Math.rint((maxLat - minLat) / dLat) + 1;
		int nCols = (int) Math.rint((maxLon - minLon) / dLon) + 1;
		// always have an a-grid file
		aDat = readGrid(aGridFile, nRows, nCols);
		// might have a b-grid file, but not likely
		bDat = bGrid ? readGrid(bGridFile, nRows, nCols) : makeGrid(
			aDat.length, grSrc.bVal);

		// KLUDGY numerous b-values are 0 but there is a hook in hazgridXnga5
		// (line 931) to override a grid based b=0 to the b-value set in the
		// config for a grid source.
		for (int i = 0; i < bDat.length; i++) {
			if (bDat[i] == 0.0) bDat[i] = grSrc.bVal;
		}

		// don't have variable mMin, but combined grids could
		mMinDat = makeGrid(aDat.length, grSrc.mMin);
		// variable mMax is common
		mMaxDat = mMaxGrid ? readGrid(mMaxGridFile, nRows, nCols) : makeGrid(
			aDat.length, grSrc.mMax);
		// weights; mostly for CA
		wgtDat = weightGrid ? readGrid(weightGridFile, nRows, nCols) : null;
	}

	private double[] makeGrid(int size, double value) {
		double[] dat = new double[size];
		Arrays.fill(dat, value);
		return dat;
	}

	/*
	 * This line is set up to configure a probability distribution of magnitude
	 * dependent rupture top depths. These are actually not used in favor of
	 * fixed values for M<6.5 and M>=6.5
	 */
	private void readRuptureTop(String line) {
		int numDepths = readInt(line, 0);
		double[] depthDat = readDoubles(line, 1 + 2 * numDepths);
		double loMagDepth, hiMagDepth;
		if (numDepths == 1) {
			loMagDepth = depthDat[1];
			hiMagDepth = depthDat[1];
		} else {
			loMagDepth = depthDat[4];
			hiMagDepth = depthDat[1];
		}
		depths = new double[] { loMagDepth, hiMagDepth };
	}

	private void readMechWeights(String line) {
		double[] weights = readDoubles(line, 3);
		mechWtMap = Maps.newEnumMap(FocalMech.class);
		mechWtMap.put(STRIKE_SLIP, weights[0]);
		mechWtMap.put(REVERSE, weights[1]);
		mechWtMap.put(NORMAL, weights[2]);
		// mechWts = weights;
	}

	private void readLookupArrayDat(String line) {
		double[] rDat = readDoubles(line, 2);
		dR = (int) rDat[0];
		rMax = (int) rDat[1];
	}

	private void readSourceLatRange(String line) {
		double[] latDat = readDoubles(line, 3);
		minLat = latDat[0];
		maxLat = latDat[1];
		dLat = latDat[2];
	}

	private void readSourceLonRange(String line) {
		double[] lonDat = readDoubles(line, 3);
		minLon = lonDat[0];
		maxLon = lonDat[1];
		dLon = lonDat[2];
	}

	private File readSourceFile(String path) {
		checkArgument(path.startsWith(GRD_PATH), "Bad file path: " + path);
		return FileUtils.toFile(GridParser.class.getResource(DAT_PATH +
			path.substring(GRD_PATH.length())));
	}

	private void readRateInfo(String line) {
		timeSpan = readDouble(line, 0);
		int rateTypeVal = readInt(line, 1);
		rateType = (rateTypeVal == 0) ? INCREMENTAL : CUMULATIVE;
	}

	// TODO move to GridERF?
	@Override
	public String toString() {
		// @formatter:off
		return new StringBuilder()
		.append(IOUtils.LINE_SEPARATOR)
		.append("=========== Grid Config ============")
		.append(IOUtils.LINE_SEPARATOR)
		.append("            Name: ").append(file.getName())
		.append(IOUtils.LINE_SEPARATOR)
		.append("       Lat range: ").append(minLat).append(" ").append(maxLat)
		.append(IOUtils.LINE_SEPARATOR)
		.append("       Lon range: ").append(minLon).append(" ").append(maxLon)
		.append(IOUtils.LINE_SEPARATOR)
		.append("     [dLat dLon]: ").append(dLat).append(" ").append(dLon)
		.append(IOUtils.LINE_SEPARATOR)
//		.append("      Node count: ").append(region.getNodeCount())
//		.append(IOUtils.LINE_SEPARATOR)
		.append("   Rup top M\u22646.5: ").append(depths[0])
		.append(IOUtils.LINE_SEPARATOR)
		.append("   Rup top M\u003E6.5: ").append(depths[1])
		.append(IOUtils.LINE_SEPARATOR)
		.append("    Mech weights: ")
		.append("SS=").append(mechWtMap.get(STRIKE_SLIP))
		.append(" R=").append(mechWtMap.get(REVERSE))
		.append(" N=").append(mechWtMap.get(NORMAL))
		.append(IOUtils.LINE_SEPARATOR)
		.append("   opt [dR rMax]: ").append(dR).append(" ").append(rMax)
		.append(IOUtils.LINE_SEPARATOR)
		.append(" GR [b M- M+ dM]: ").append(grSrc.bVal).append(" ").append(grSrc.mMin)
		.append(" ").append(grSrc.mMax).append(" ").append(grSrc.dMag)
		.append(IOUtils.LINE_SEPARATOR)
		.append("          a grid: ").append(aGridFile.getName())
		.append(IOUtils.LINE_SEPARATOR)
		.append("          b grid: ").append(bGrid)
		.append(" ").append((bGridFile != null) ? bGridFile.getName() : "")
		.append(IOUtils.LINE_SEPARATOR)
		.append("       mMax grid: ").append(mMaxGrid)
		.append(" ").append((mMaxGridFile != null) ? mMaxGridFile.getName() : "")
		.append(IOUtils.LINE_SEPARATOR)
		.append("     weight grid: ").append(weightGrid)
		.append(" ").append((weightGridFile != null) ? weightGridFile.getName() : "")
		.append(IOUtils.LINE_SEPARATOR)
		.append("         M taper: ").append(mTaper)
		.append(IOUtils.LINE_SEPARATOR)
		.append("       Time span: ").append(timeSpan)
		.append(IOUtils.LINE_SEPARATOR)
		.append("            Rate: ").append(rateType)
		.append(IOUtils.LINE_SEPARATOR)
		.append("      Fault Code: ").append(fltCode)
		.append(IOUtils.LINE_SEPARATOR)
		.append("          Strike: ").append(strike)
		.append(IOUtils.LINE_SEPARATOR).toString();
		// @formatter:off
	}

	/////////////// CEUS Customizations ///////////////

	// wtmj_cra: full weight up to 6.55; Mmax=6.85 @ 0.2 wt
	// wtmj_ext: full weight up to 6.85; Mmax=7.15 @ 0.2 wt
	// wtmab_cra: full weight up to 6.75; Mmax=7.05 @ 0.2 wt
	// wtmab_ext: full weight up to 7.15; Mmax=7.35 @ 0.2 wt
	private static double[] wtmj_cra =  { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.7, 0.2, 0.2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	private static double[] wtmj_ext =  { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.7, 0.7, 0.2, 0.0, 0.0, 0.0 };
	private static double[] wtmab_cra = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.9, 0.7, 0.2, 0.0, 0.0, 0.0, 0.0 };
	private static double[] wtmab_ext = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.7, 0.2, 0.0 };
	private static boolean[] cratonFlags;
	private static boolean[] marginFlags;
	
	
	private void ceusScaleRates() {
		initMasks();
		
		// set weights by file name
		double[] craWt = wtmj_cra;
		double[] marWt = wtmj_ext;
		if (file.getName().contains(".AB.")) {
			craWt = wtmab_cra;
			marWt = wtmab_ext;
		}
		double[] weights;
		
		// adjust mfds
		for (int i=0; i<cratonFlags.length; i++) {
			IncrementalMagFreqDist mfd = mfdList.get(i);
			if (mfd == null) continue;
			boolean craFlag = cratonFlags[i];
			boolean marFlag = marginFlags[i];
			if ((craFlag | marFlag) == false) continue;
			weights = craFlag ? craWt : marWt;
			applyWeight(mfd, weights);
		}
	}
	
	private void applyWeight(IncrementalMagFreqDist mfd, double[] weights) {
		for (int i=0; i<mfd.getNum(); i++) {
			double weight = weights[i];
			if (weight == 1.0) continue;
			mfd.set(i, mfd.getY(i) * weight);
		}
	}

	private void initMasks() {
		// this is only used for CEUS so we don't have to worry about having
		// the wrong dimensions set for these static fields
		if (cratonFlags == null) {
			File craton = Utils.getResource("/imr/craton");
			File margin = Utils.getResource("/imr/margin");
			int nRows = (int) Math.rint((maxLat - minLat) / dLat) + 1;
			int nCols = (int) Math.rint((maxLon - minLon) / dLon) + 1;
			cratonFlags = NSHMP_Utils.readBoolGrid(craton, nRows, nCols);
			marginFlags = NSHMP_Utils.readBoolGrid(margin, nRows, nCols);
		}
	}

	public static void main(String[] args) {
		Logger log = NSHMP_Utils.logger();
		Level level = Level.FINE;
		log.setLevel(level);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(level);
		}
		//log.info((new Date()) + " " + FaultParser.class.getName());
//		File f = FileUtils.toFile(GridConfig.class.getResource(datPath+"CEUS/gridded/"));
//		String[] fNames = f.list();
//		for (String name : fNames) {
//			System.out.println("paths.add(\"CEUS/gridded/" + name + "\");");
//		}
		List<SourceFile> sources = Lists.newArrayList();
		Map<String, GridERF> cfgs = Maps.newHashMap();
		
//		paths.add("CA/gridded/brawmap.in");
//		paths.add("CA/gridded/CAdeep.in");
//		paths.add("CA/gridded/CAmap.21.ch.in");
//		paths.add("CA/gridded/CAmap.21.gr.in");
//		paths.add("CA/gridded/CAmap.24.ch.in");
//		paths.add("CA/gridded/CAmap.24.gr.in");
//		paths.add("CA/gridded/creepmap.in");
//		paths.add("CA/gridded/impext.ch.in");
//		paths.add("CA/gridded/impext.gr.in");
//		paths.add("CA/gridded/mendo.in");
//		paths.add("CA/gridded/mojave.in");
//		paths.add("CA/gridded/sangorg.in");
//		paths.add("CA/gridded/shear1.in");
//		paths.add("CA/gridded/shear2.in");
//		paths.add("CA/gridded/shear3.in");
//		paths.add("CA/gridded/shear4.in");
//
//		paths.add("WUS/gridded/EXTmap.ch.in");
//		paths.add("WUS/gridded/EXTmap.gr.in");
//		paths.add("WUS/gridded/nopuget.ch.in");
//		paths.add("WUS/gridded/nopuget.gr.in");
//		paths.add("WUS/gridded/pnwdeep.in");
//		paths.add("WUS/gridded/portdeep.in");
//		paths.add("WUS/gridded/puget.ch.in");
//		paths.add("WUS/gridded/puget.gr.in");
//		paths.add("WUS/gridded/WUSmap.ch.in");
//		paths.add("WUS/gridded/WUSmap.gr.in");

//		paths.add("CEUS/gridded/CEUS.2007all8.AB.in");
//		paths.add("CEUS/gridded/CEUS.2007all8.J.in");
//		paths.add("CEUS/gridded/CEUSchar.68.in");
//		paths.add("CEUS/gridded/CEUSchar.71.in");
//		paths.add("CEUS/gridded/CEUSchar.73.in");
//		paths.add("CEUS/gridded/CEUSchar.75.in");

//		sources.addAll(SourceFileMgr.get(WUS, GRIDDED, "EXTmap.gr.in"));
//		sources.addAll(SourceFileMgr.get(WUS, GRIDDED, "EXTmap.ch.in"));
//
//		sources.addAll(SourceFileMgr.get(WUS, GRIDDED, "WUSmap.gr.in"));
//		sources.addAll(SourceFileMgr.get(WUS, GRIDDED, "WUSmap.ch.in"));
		
//		sources.addAll(SourceFileMgr.get(CA, GRIDDED, "CAmap.21.ch.in"));
//		sources.addAll(SourceFileMgr.get(CA, GRIDDED, "CAmap.21.gr.in"));

//		sources.addAll(SourceFileMgr.get(CA, GRIDDED, "CAmap.24.ch.in"));
//		sources.addAll(SourceFileMgr.get(CA, GRIDDED, "CAmap.24.gr.in"));

//		sources.addAll(SourceFileMgr.get(CA, GRIDDED, "impext.ch.in"));
//		sources.addAll(SourceFileMgr.get(CA, GRIDDED, "impext.gr.in"));

//		sources.addAll(SourceFileMgr.get(WUS, GRIDDED, "nopuget.gr.in"));
//		sources.addAll(SourceFileMgr.get(WUS, GRIDDED, "puget.ch.in"));

		sources.addAll(SourceFileMgr.get(CEUS, GRIDDED, "CEUS.2007all8.AB.in"));

		Location loc = new Location(27.9, -96.2);
		
		GridERF gs = null;
		for (SourceFile sf : sources) {
			GridParser parser = new GridParser(log);
			parser.parse(sf);
			log.fine(parser.toString());
			gs = parser.createGridSource();
			cfgs.put(sf.toString(), gs);
//			GridUtils.gridToKML(gs, sf.getName(), Color.ORANGE);
			
			System.out.println(gs.getMFD(loc));
			
		}
		
		
//		int mfdCount = 0;
//		for (IncrementalMagFreqDist mfd:gs.mfds) {
//			if (mfd != null) mfdCount++;
//		}
//		System.out.println(mfdCount);
		
//		System.out.println(gs.mfds.size());
//		System.out.println(gs.region.getNodeCount());
//		
//		int idx = gs.region.indexForLocation(new Location(36.5, -115.5));
//		System.out.println(gs.region.indexForLocation(new Location(36.5, -115.5)));
//		System.out.println(gs.region.indexForLocation(new Location(36.6, -115.5)));
//		System.out.println(gs.region.indexForLocation(new Location(36.7, -115.5)));
//		System.out.println(gs.mfds.get(idx));

//		69959 265 266 264 1 0
//		69960 265 266 265 2 -1
		
//		System.out.println(69959d/264d);
//		System.out.println(69960d/264d);
//
//		System.out.println(69959 % 266);
//		System.out.println(69960 % 266);

//		while(true) {
//			continue;
//		}
		
		
//		GridConfig gc1 = cfgs.get("brawmap.in");
//		GridConfig gc2 = cfgs.get("impext.ch.in");
////		GridConfig gc1 = cfgs.get("EXTmap.ch.in");
////		GridConfig gc2 = cfgs.get("WUSmap.ch.in");
//		
//		System.out.println(gc1);
//		System.out.println(gc2);
//		
//		double[] f1 = readBinaryData(gc1.aGridDat);
//		double[] f2 = readBinaryData(gc2.aGridDat);
//		
//		int count = 0;
//		for (int i=0; i<f1.length; i++) {
//			if (f1[i] != 0 && f2[i] != 0)  count += 1;
//		}
//		System.out.println("Count: " + count);

		
//		int[] tmp = new int[70490];
//		for (int i=0; i<tmp.length; i++) {
//			tmp[calcIndex(i, 265, 266)] = i;
////			if (i == 1000) break;
//		}
//		System.out.println(Arrays.toString(tmp));

	}
}
