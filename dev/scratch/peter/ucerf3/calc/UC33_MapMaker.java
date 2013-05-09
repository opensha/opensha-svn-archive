package scratch.peter.ucerf3.calc;

import static org.opensha.nshmp2.tmp.TestGrid.CA_RELM;
import static org.opensha.nshmp2.util.Period.GM0P00;
import static scratch.peter.curves.ProbOfExceed.PE2IN50;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

import scratch.peter.curves.ProbOfExceed;
import scratch.peter.nshmp.CurveContainer;
import scratch.peter.nshmp.NSHMP_DataUtils;
import scratch.peter.nshmp.NSHMP_PlotUtils;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC33_MapMaker {

	private static final String S = File.separator;
	private static final String ROOT = "tmp/UC33/maps/";
	private static final String SRC = ROOT + "src/";

	public static void main(String[] args) throws IOException {
		makeBrAvgRatioMapUC33();
//		makePrelimBrAvgHazardMaps();
	}

	// UCERF3.3 prelim branchAvg over comparable UC32 branch avg.
	private static void makeBrAvgRatioMapUC33() throws IOException {

		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		double spacing = 0.1;

		String over = SRC + "UC33brAvg5x_fm32";
		String under = SRC + "UC32brAvg5x_fm32";
		String out = ROOT + "UC-33-32-brAvg-fm32";

		GeoDataSet xyzOver = loadSingle(over, pe, grid, p, spacing);
		GeoDataSet xyzUnder = loadSingle(under, pe, grid, p, spacing);
		GeoDataSet xyz = GeoDataSetMath.divide(xyzOver, xyzUnder);

		UC3_MapMaker.makeRatioPlot(xyz, 0.1, grid.bounds(), out, "GM ratio",
			true, true, true);
	}
	
	// UCERF3.3 prelim ground motion maps
	private static void makePrelimBrAvgHazardMaps() {
		
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		double spacing = 0.1;
		
		String src33 = SRC + "UC33brAvg_prelim";
		String src32 = SRC + "UC32brAvg_cf33";

		GeoDataSet xyz33 = loadSingle(src33, pe, grid, p, spacing);
		GeoDataSet xyz32 = loadSingle(src32, pe, grid, p, spacing);

		String out33 = ROOT + "UC33brAvgMap";
		String out32 = ROOT + "UC32brAvgMap";

		// map
		double[] minmax = NSHMP_PlotUtils.getRange(p);
		GMT_CPT_Files cpt = NSHMP_PlotUtils.getCPT(p);
		String label = pe + " " + p.getLabel() + " (g)";
		
		UC3_MapMaker.makeMapPlot(xyz33, spacing, grid.bounds(), out33, label,
			minmax[0], minmax[1], cpt, true, true);
		UC3_MapMaker.makeMapPlot(xyz32, spacing, grid.bounds(), out32, label,
			minmax[0], minmax[1], cpt, true, true);
	}


	private static GeoDataSet loadSingle(String dir, ProbOfExceed pe,
			TestGrid grid, Period p, double spacing) {
		File curves = new File(dir + S + grid + S + p + S + "curves.csv");
		CurveContainer cc = CurveContainer.create(curves, grid, spacing);
		GriddedRegion gr = grid.grid(spacing);
		return NSHMP_DataUtils.extractPE(cc, gr, pe);
	}

}
