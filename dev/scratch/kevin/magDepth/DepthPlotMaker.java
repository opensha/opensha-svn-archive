package scratch.kevin.magDepth;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.parsers.UCERF3_CatalogParser;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.collect.Lists;

public class DepthPlotMaker {
	
	private static class Mapping {
		private double depth, mag;
		private Mapping(double depth, double mag) {
			this.depth = depth;
			this.mag = mag;
		}
	}
	
	private static List<Mapping> loadSCFormat(File file) throws IOException {
		ArrayList<Mapping> list = new ArrayList<Mapping>();
		for (String line : FileUtils.loadFile(file.getAbsolutePath())) {
			StringTokenizer tok = new StringTokenizer(line.trim());
			if (tok.countTokens() < 5)
				continue;
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			double lon = Double.parseDouble(tok.nextToken());
			double lat = Double.parseDouble(tok.nextToken());
			double depth = Double.parseDouble(tok.nextToken());
			double mag = Double.parseDouble(tok.nextToken());
			list.add(new Mapping(depth, mag));
		}
		
		return list;
	}
	
	private static List<Mapping> loadUCERF3(int lastYear, File file) throws IOException {
		GregorianCalendar cal = new GregorianCalendar(lastYear, 0, 0);
		ObsEqkRupList cat = UCERF3_CatalogParser.loadCatalog(file);
		
		ArrayList<Mapping> list = new ArrayList<Mapping>();
		for (ObsEqkRupture rup : cat.getRupsAfter(cal.getTimeInMillis())) {
			double depth = rup.getHypocenterLocation().getDepth();
			if (depth != 0)
				list.add(new Mapping(depth, rup.getMag()));
		}
		
		return list;
	}
	
	private static void plot(List<Mapping> list) {
		NoCollissionFunc func = new NoCollissionFunc();
		for (Mapping val : list) {
			func.set(val.mag, -val.depth);
		}
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(func);
		ArrayList<PlotCurveCharacterstics> plotChars = Lists.newArrayList(new PlotCurveCharacterstics(PlotSymbol.X, 2f, Color.BLACK));
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, "Mag Vs. Depth", plotChars);
		gw.setX_AxisLabel("Magnitude");
		gw.setY_AxisLabel("Altitude");
		
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File landersFile = new File("/home/kevin/workspace/scec_vdo_ucerf3/data/Catalogs/landers_etas_new.sc");
		File ucerf3File = new File("/home/kevin/OpenSHA/UCERF3/UCERF3CatalogVersion1.txt");
		List<Mapping> landersList = loadSCFormat(landersFile);
		plot(landersList);
		List<Mapping> ucerf3List = loadUCERF3(1960, ucerf3File);
		plot(ucerf3List);
	}

}
