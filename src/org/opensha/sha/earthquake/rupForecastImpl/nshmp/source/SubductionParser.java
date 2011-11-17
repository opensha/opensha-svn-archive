package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static com.google.common.base.Preconditions.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils.*;
import static org.opensha.sha.nshmp.SourceRegion.*;
import static org.opensha.sha.nshmp.SourceType.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.GeoTools;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultType;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.nshmp.CEUSdev;
import org.opensha.sha.nshmp.SourceType;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
class SubductionParser extends FaultParser {

	SubductionParser(Logger log) {
		super(log);
	}

	private List<SubductionSource> srcList;

	public SubductionERF parseSubduction(SourceFile sf) {
		srcList = Lists.newArrayList();

		File f = sf.getFile();
		List<String> dat = readLines(f, log);

		Iterator<String> srcLines = dat.iterator();
		skipHeader(srcLines);

		// set dummy magnitude uncertainty data; all flags false
		MagData md = new MagData();
		md.toLog(log);

		while (srcLines.hasNext()) {
			// NSHMP subduction sources are processed by two differnt versions
			// of hazSUBXnga; one assumes a single mfd, the other reads the
			// number of mfd models
			String[] fltDat = StringUtils.split(srcLines.next());
			SubductionSource ss = new SubductionSource();
			ss.file = sf;
			ss.mfds = Lists.newArrayList();
			ss.type = FaultType.typeForID(readInt(fltDat, 0));
			ss.mech = FocalMech.typeForID(readInt(fltDat, 1));
			try {
				ss.nMag = readInt(fltDat, 2);
				ss.name = StringUtils.join(fltDat, ' ', 3, fltDat.length);
			} catch (NumberFormatException nfe) {
				// if can't read 3rd int, set name and nMag to 1
				ss.nMag = 1;
				ss.name = StringUtils.join(fltDat, ' ', 2, fltDat.length);
			}
			List<String> mfdSrcDat = readLines(srcLines, ss.nMag);
			generateMFDs(ss, mfdSrcDat, md.clone());
			generateTraces(srcLines, ss);
			toLog(log, ss);

			if (ss.mfds.size() == 0) {
				StringBuilder sb = new StringBuilder()
					.append("Source with no mfds");
				appendFaultDat(sb, ss);
				log.warning(sb.toString());
			}
			srcList.add(ss);
		}

		SubductionERF erf = new SubductionERF(f.getName(), srcList);
		return erf;
	}

	private void generateTraces(Iterator<String> it, SubductionSource ss) {
		int upperTraceLen = readInt(it.next(), 0);
		ss.trace = generateTrace(it, upperTraceLen, ss.name + " Upper Trace");
		int lowerTraceLen = readInt(it.next(), 0);
		ss.lowerTrace = generateTrace(it, lowerTraceLen, ss.name + " Lower Trace");
		if (log.isLoggable(Level.FINE)) {
			log.fine(IOUtils.LINE_SEPARATOR + ss.trace.toString());
			log.fine(IOUtils.LINE_SEPARATOR + ss.lowerTrace.toString());
		}
	}
	
	private static FaultTrace generateTrace(Iterator<String> it, int traceCount, String name) {
		FaultTrace trace = new FaultTrace(name);
		List<String> traceDat = readLines(it, traceCount);
		for (String ptDat : traceDat) {
			String[] locDat = StringUtils.split(ptDat);
			trace.add(new Location(readDouble(locDat, 0),
				readDouble(locDat, 1), readDouble(locDat, 2)));
		}
		return trace;
	}

	public static void main(String[] args) {
		Logger log = NSHMP_Utils.logger();
		Level level = Level.FINE;
		log.setLevel(level);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(level);
		}

		// log.setLevel(Level.FINE);
		// log date and class as these are suppressed by custom formatter
		log.info((new Date()) + " " + FaultParser.class.getName());

		SubductionParser dev = new SubductionParser(log);
		// String srcPath = datPath + "WUS/faults/brange.3dip.gr.in";
		// String srcPath = datPath + "WUS/faults/brange.3dip.ch.in";
		// String srcPath = datPath + "WUS/faults/brange.3dip.65.in";
		//
//		 String srcPath = datPath + "WUS/faults/nv.3dip.gr.in";
//		 String srcPath = datPath + "WUS/faults/nv.3dip.ch.in";
		// String srcPath = datPath + "WUS/faults/nvut.3dip.65.in";
		// String srcPath = datPath + "WUS/faults/ut.3dip.gr.in";
		// String srcPath = datPath + "WUS/faults/ut.3dip.ch.in";
		//
		// String srcPath = datPath + "WUS/faults/orwa_n.3dip.gr.in";
		// String srcPath = datPath + "WUS/faults/orwa_n.3dip.ch.in";
		// String srcPath = datPath + "WUS/faults/orwa_c.in";
		//
		// String srcPath = datPath + "WUS/faults/wasatch.3dip.gr.in";
		// String srcPath = datPath + "WUS/faults/wasatch.3dip.ch.in";
		// String srcPath = datPath + "WUS/faults/wasatch.3dip.74.in";
		//
//		 String srcPath = datPath + "CA/faults/bFault.gr.in";
		// String srcPath = datPath + "CA/faults/bFault.ch.in";
//		 String srcPath = datPath + "CA/faults/aFault_aPriori_D2.1.in";
		// String srcPath = datPath + "CA/faults/aFault_MoBal.in";
		// String srcPath = datPath + "CA/faults/aFault_MoBal.in";
		// String srcPath = datPath + "CA/faults/aFault_unsegEll.in";
		// String srcPath = datPath + "CA/faults/aFault_unseg_HB.in";
		// String srcPath = datPath + "CA/faults/creepflt.in";

		// String srcPath = datPath + "CEUS/faults/CEUScm.in";
			// String srcPath = datPath + "CEUS/faults/NMSZnocl.1000yr.5branch.in";
			// String srcPath = datPath + "CEUS/faults/NMSZnocl.500yr.5branch.in";

//		SourceFile sf = SourceFileMgr.get(CA, FAULT, "bFault.gr.in").get(0);
		SourceFile sf = SourceFileMgr.get(CASC, SUBDUCTION, "cascadia.bot.8082.in").get(0);
//		SourceFile sf = SourceFileMgr.get(CA, FAULT, "aFault_unseg.in").get(0);

//		File f = FileUtils.toFile(CEUSdev.class.getResource(srcPath));

		log.info("Source: " + sf.getFile().getPath());
		SubductionERF erf = dev.parseSubduction(sf);

//		System.out.println("NumSrcs: " + erf.getNumSources());
		for (ProbEqkSource source : erf) {
			((FaultSource) source).init();
			System.out.println("Source: " + source.getName());
			System.out.println("  size: " + source.getNumRuptures());
		}
	}

	private static void skipHeader(Iterator<String> it) {
		int nP, nAR;
		int numSta = readInt(it.next(), 0); // grid of sites or station list
		if (numSta > 0) {
			for (int k=0; k<numSta; k++) {
				it.next();
			}
		} else {
			it.next(); // lat bounds and discretization
			it.next(); // lon bounds and discretization
		}
		it.next(); // site data (Vs30)
		nP = readInt(it.next(), 0); // num periods
		for (int i = 0; i < nP; i++) {
			it.next(); // period
			it.next(); // out file
			nAR = readInt(it.next(), 0); // num atten. rel.
			for (int j = 0; j < nAR; j++) {
				it.next(); // atten rel
			}
			it.next(); // num ground motion values
			it.next(); // ground motion values
		}
		it.next(); // discretization
		it.next(); // distance sampling to fault and max distance
	}

}
