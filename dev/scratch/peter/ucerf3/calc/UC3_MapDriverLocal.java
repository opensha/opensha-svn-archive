package scratch.peter.ucerf3.calc;

import static org.opensha.sha.earthquake.param.IncludeBackgroundOption.*;
import static org.opensha.nshmp2.tmp.TestGrid.*;
import static org.opensha.nshmp2.util.Period.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.geo.LocationList;
import org.opensha.nshmp2.calc.HazardResultWriter;
import org.opensha.nshmp2.calc.HazardResultWriterLocal;
import org.opensha.nshmp2.calc.HazardResultWriterMPJ;
import org.opensha.nshmp2.calc.ThreadedHazardCalc;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_MapDriverLocal {

	private static String S = File.separator;

	UC3_MapDriverLocal(String solPath, String outDir, TestGrid grid,
		double spacing, Period p, IncludeBackgroundOption bg) {

		try {
			outDir += grid + S + p + S;
			File out = new File(outDir, "curves");
			Files.createParentDirs(out);
			HazardResultWriter writer = new HazardResultWriterLocal(out, p);
			LocationList locs = grid.grid(spacing).getNodeList();
			ThreadedHazardCalc calc = new ThreadedHazardCalc(solPath, locs, p,
			false, bg, writer);
			calc.calculate(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String solPathFM31 = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2013_01_14-UC32-MEAN_BRANCH_AVG_SOL_FM31.zip";
		String solPathFM32 = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2013_01_14-UC32-MEAN_BRANCH_AVG_SOL_FM32.zip";

		String outDir = "/Users/pmpowers/projects/OpenSHA/tmp/remapLocal/FM31";
		TestGrid tg = TestGrid.SAN_FRANCISCO;
		Period p = Period.GM0P00;
		IncludeBackgroundOption bg = INCLUDE;
		
		outDir += (bg == INCLUDE) ? "all/" : (bg == EXCLUDE) ? "flt/" : "bg/";
		
		Stopwatch sw = new Stopwatch();
		sw.start();
		new UC3_MapDriverLocal(solPathFM31, outDir, tg, 0.02, p, bg);
		sw.stop();

		System.out.println(sw.elapsed(TimeUnit.MINUTES));
	}

}
