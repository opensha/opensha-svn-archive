package org.opensha.cybershake.plot;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.opensha.cybershake.HazardCurveFetcher;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurve2DB;
import org.opensha.data.Location;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.sha.calc.hazardMap.MakeXYZFromHazardMapDir;
import org.opensha.util.FileUtils;
import org.opensha.util.XYZClosestPointFinder;
import org.opensha.util.cpt.CPT;

public class HazardMapScatterCreator {
	
	public static final Color BLANK_COLOR = Color.WHITE;
	
	ArrayList<CybershakeSite> sites;
	ArrayList<DiscretizedFuncAPI> funcs;
	ArrayList<Double> vals;
	
	ArrayList<CybershakeSite> allSites = null;
	
	ArrayList<XYZClosestPointFinder> comps = new ArrayList<XYZClosestPointFinder>();
	ArrayList<String> compNames = new ArrayList<String>();
	
	CPT cpt;
	
	HazardCurveFetcher fetcher;
	
	public HazardMapScatterCreator(DBAccess db, int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID, CPT cpt, boolean isProbAt_IML, double val) {
		this.cpt = cpt;
		
		fetcher = new HazardCurveFetcher(db, erfID, rupVarScenarioID, sgtVarID, imTypeID);
		sites = fetcher.getCurveSites();
		funcs = fetcher.getFuncs();
		
		vals = fetcher.getSiteValues(isProbAt_IML, val);
	}
	
	public void addComparison(String name, String fileName) {
		try {
			comps.add(new XYZClosestPointFinder(fileName));
			compNames.add(name);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printScatterCommands(String symbol) {
		for (double val : vals) {
			System.out.println("");
		}
	}
	
	private void printVals() {
		ArrayList<CurveSite> curveSites = new ArrayList<CurveSite>();
		for (int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			CybershakeSite site = sites.get(i);
			curveSites.add(new CurveSite(site, val));
		}
		Collections.sort(curveSites);
		for (CurveSite cs : curveSites) {
			System.out.println(cs.getSite());
			System.out.println("CyberShake: " + cs.getVal());
			for (int i=0; i<comps.size(); i++) {
				XYZClosestPointFinder comp = comps.get(i);
				System.out.println(compNames.get(i) + ": " + comp.getClosestVal(cs.getSite().lat, cs.getSite().lon));
			}
			System.out.println();
		}
	}
	
	private void printCurves() {
		for (int i=0; i<funcs.size(); i++) {
			DiscretizedFuncAPI func = funcs.get(i);
			CybershakeSite site = sites.get(i);
			
			System.out.println("SITE: " + site);
			System.out.println(func);
			System.out.println();
		}
	}
	
	class CurveSite implements Comparable<CurveSite> {
		CybershakeSite site;
		double val;
		
		public CurveSite(CybershakeSite site, double val) {
			this.site = site;
			this.val = val;
		}

		public int compareTo(CurveSite comp) {
			if (comp.getVal() > this.val)
				return 1;
			else if (comp.getVal() < this.val)
				return -1;
			return 0;
		}

		public CybershakeSite getSite() {
			return site;
		}

		public double getVal() {
			return val;
		}
	}
	
	public static class Symbol {
		
		String sym;
		
		int start;
		int end;
		
		public Symbol(String sym, int start, int end) {
			this.sym = sym;
			this.start = start;
			this.end = end;
		}
		
		public boolean use(int id) {
			return id >= start && id <= end;
		}
		
		public String getSymbol() {
			return sym;
		}
	}
	
	private String getSymbol(CybershakeSite site, ArrayList<Symbol> symbols, String defaultSym) {
		String sym = defaultSym;
		for (Symbol symbol : symbols) {
			if (symbol.use(site.id)) {
				sym = symbol.getSymbol();
				break;
			}
		}
		return sym;
	}
	
	private double scaleSize(double size, String symbol) {
		if (symbol.equals("c"))
			size = 0.75 * size;
		else if (symbol.equals("d"))
			size = 0.85 * size;
		return size;
	}
	
	private String getGMTColorString(Color color) {
		return "-G" + color.getRed() + "/" + color.getGreen() + "/" + color.getBlue();
	}
	
	private String getGMTSymbolLine(ArrayList<Symbol> symbols, String defaultSym, CybershakeSite site, double val, double size) {
		
		String sym = this.getSymbol(site, symbols, defaultSym);
		double scaledSize = this.scaleSize(size, sym);
		
		Color color;
		if (val < 0) {
			color = BLANK_COLOR;
			scaledSize = scaledSize * 0.5;
		} else {
			color = cpt.getColor((float)val);
		}
		
		String colorStr = this.getGMTColorString(color);
		String outline = "-W" + (float)(size * 0.09) + "i,255/255/255";
//		String outline = "-W2,255/255/255";
		
		String line = "echo " + site.lon + " " + site.lat + " | ";
		// arg 1: plot region
		// arg 2: plot projection
		// arg 3: ps file
		line += "psxy $1 $2 -S" + sym + scaledSize + "i " + colorStr + " " + outline + " -O -K >> $3";
		
		return line;
	}
	
	public String getGMTLabelLine(CybershakeSite site, double fontSize) {
		double x = site.lon + (0.025);
		double y = site.lat;
		double angle = 0;
		String fontno = "1";
		String justify = "LM";
		String text = site.short_name;
		
		String colorStr = this.getGMTColorString(Color.white);
		
		String line = "echo " + x + " " + y + " " + fontSize + " " + angle + " " + fontno + " " + justify + " " + text +  " | ";
		
		// arg 1: plot region
		// arg 2: plot projection
		// arg 3: ps file
		line += "pstext $1 $2 " + colorStr + " -O -K >> $3";
		
		return line;
	}
	
	private ArrayList<CybershakeSite> getAllSites() {
		if (allSites == null) {
			allSites = fetcher.getAllSites();
		}
		return allSites;
	}
	
	public void writeScatterColoredScript(ArrayList<Symbol> symbols, String defaultSym, String script, boolean writeEmptySites, boolean labels) throws IOException {
		FileWriter write = new FileWriter(script);
		
		double size = 0.18;
		double fontSize = 10;
		
		for (CybershakeSite site : this.getAllSites()) {
			double val = -1d;
			for (int i=0; i<vals.size(); i++) {
				CybershakeSite valSite = sites.get(i);
				if (site.id == valSite.id) {
					val = vals.get(i);
					break;
				}
			}
			if (writeEmptySites || val >=0) {
				String line = this.getGMTSymbolLine(symbols, defaultSym, site, val, size);
				
				System.out.println(line);
				write.write(line + "\n");
				
				if (labels) {
					line = this.getGMTLabelLine(site, fontSize);
					System.out.println(line);
					write.write(line + "\n");
				}
			}
		}
		
		write.close();
	}
	
	public void writeScatterMarkerScript(ArrayList<Symbol> symbols, String defaultSym, String script, boolean labels) throws IOException {
		FileWriter write = new FileWriter(script);
		
		double size = 0.18;
		double fontSize = 10;
		
		for (CybershakeSite site : this.getAllSites()) {
			double val = -1d;
			String line = this.getGMTSymbolLine(symbols, defaultSym, site, val, size);
			
			System.out.println(line);
			write.write(line + "\n");
			
			if (labels) {
				line = this.getGMTLabelLine(site, fontSize);
				System.out.println(line);
				write.write(line + "\n");
			}
		}
		
		write.close();
	}
	
	public static void main(String args[]) {
		String cptFile = "/home/kevin/CyberShake/scatterMap/gmt/cpt.cpt";
		CPT cpt = null;
		try {
			cpt = CPT.loadFromFile(new File(cptFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		int erfID = 34;
		int rupVarScenID = 3;
		int sgtVarID = 5;
		int imTypeID = 21;
		
		boolean isProbAt_IML = false;
		double val = 0.0004;
		
		HazardMapScatterCreator map = new HazardMapScatterCreator(db, erfID, rupVarScenID, sgtVarID, imTypeID, cpt, isProbAt_IML, val);
		
//		map.addComparison("CB 2008", "/home/kevin/CyberShake/scatterMap/base_cb.txt");
//		map.addComparison("BA 2008", "/home/kevin/CyberShake/scatterMap/base_ba.txt");
//		map.printCurves();
//		map.printVals();
		
		ArrayList<Symbol> symbols = new ArrayList<Symbol>();
		symbols.add(new Symbol("s", 18, 19));
		symbols.add(new Symbol("d", 28, 36));
		symbols.add(new Symbol("s", 37, 57));
		
		boolean writeEmptySites = true;
		boolean labels = true;
		
		try {
			map.writeScatterColoredScript(symbols, "c", "/home/kevin/CyberShake/scatterMap/gmt/scatter.sh", writeEmptySites, labels);
			map.writeScatterMarkerScript(symbols, "c", "/home/kevin/CyberShake/scatterMap/gmt/scatter_mark.sh", labels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.destroy();
		
	}
}
