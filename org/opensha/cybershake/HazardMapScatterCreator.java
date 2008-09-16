package org.opensha.cybershake;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurve2DB;
import org.opensha.data.Location;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.sha.calc.hazardMap.MakeXYZFromHazardMapDir;
import org.opensha.util.FileUtils;
import org.opensha.util.cpt.CPT;

public class HazardMapScatterCreator {
	
	HazardCurve2DB curve2db;
	CybershakeSiteInfo2DB site2db;
	
	ArrayList<Integer> ids;
	ArrayList<CybershakeSite> sites;
	ArrayList<DiscretizedFuncAPI> funcs;
	ArrayList<Double> vals;
	
	ArrayList<XYZComparison> comps = new ArrayList<XYZComparison>();
	
	CPT cpt;
	
	public HazardMapScatterCreator(DBAccess db, int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID, CPT cpt, boolean isProbAt_IML, double val) {
		this.cpt = cpt;
		this.initDBConnections(db);
		ids = curve2db.getAllHazardCurveIDs(erfID, rupVarScenarioID, sgtVarID, imTypeID);
		sites = new ArrayList<CybershakeSite>();
		funcs = new ArrayList<DiscretizedFuncAPI>();
		for (int id : ids) {
			sites.add(site2db.getSiteFromDB(curve2db.getSiteIDFromCurveID(id)));
			DiscretizedFuncAPI curve = curve2db.getHazardCurve(id);
			funcs.add(curve);
		}
		vals = this.getSiteValues(isProbAt_IML, val);
	}
	
	public void addComparison(String name, String fileName) {
		try {
			comps.add(new XYZComparison(name, fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ArrayList<Double> getSiteValues(boolean isProbAt_IML, double val) {
		ArrayList<Double> vals = new ArrayList<Double>();
		for (DiscretizedFuncAPI func : funcs) {
			vals.add(MakeXYZFromHazardMapDir.getCurveVal(func, isProbAt_IML, val));
		}
		return vals;
	}
	
	private void initDBConnections(DBAccess db) {
		curve2db = new HazardCurve2DB(db);
		site2db = new CybershakeSiteInfo2DB(db);
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
			for (XYZComparison comp : this.comps) {
				System.out.println(comp.getName() + ": " + comp.getClosestVal(cs));
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
	
	class XYZComparison {
		ArrayList<double[]> vals;
		String name;
		
		public XYZComparison(String name, String fileName) throws FileNotFoundException, IOException {
			this.name = name;
			
			ArrayList<String> lines = null;
			lines = FileUtils.loadFile(fileName);
			
			vals = new ArrayList<double[]>();
			
			for (String line : lines) {
				line = line.trim();
				if (line.length() < 2)
					continue;
				StringTokenizer tok = new StringTokenizer(line);
				double lat = Double.parseDouble(tok.nextToken());
				double lon = Double.parseDouble(tok.nextToken());
				double val = Double.parseDouble(tok.nextToken());
				double doub[] = new double[3];
				doub[0] = lat;
				doub[1] = lon;
				doub[2] = val;
				vals.add(doub);
			}
		}
		
		public double getClosestVal(CurveSite site) {
			double closest = 9999999;
			double closeVal = 0;
			
			for (double val[] : vals) {
				double dist = Math.pow(val[0] - site.getSite().lat, 2) + Math.pow(val[1] - site.getSite().lon, 2);
				if (dist < closest) {
					closest = dist;
					closeVal = val[2];
				}
			}
			
			return closeVal;
		}
		
		public String getName() {
			return name;
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
	
	public void writeScript(ArrayList<Symbol> symbols, String defaultSym, String script) throws IOException {
		FileWriter write = new FileWriter(script);
		
		double size = 0.15;
		
		for (int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			CybershakeSite site = sites.get(i);
			
			Color color = cpt.getColor((float)val);
			
			double theSize = size;
			
			String sym = defaultSym;
			for (Symbol symbol : symbols) {
				if (symbol.use(site.id)) {
					sym = symbol.getSymbol();
					break;
				}
			}
			
			if (sym.equals("c"))
				theSize = 0.75 * theSize;
			
			String colorStr = "-G" + color.getRed() + "/" + color.getGreen() + "/" + color.getBlue();
			String outline = "-W" + (float)(theSize * 0.4) + ",255/255/255";
			
			String line = "echo " + site.lon + " " + site.lat + " | ";
			// arg 1: plot region
			// arg 2: plot projection
			// arg 3: ps file
			line += "psxy $1 $2 -S" + sym + theSize + "i " + colorStr + " " + outline + " -O -K >> $3";
			System.out.println(line);
			write.write(line + "\n");
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
		
		HazardMapScatterCreator map = new HazardMapScatterCreator(db, erfID, rupVarScenID, sgtVarID, imTypeID, cpt, false, val);
		
//		map.addComparison("CB 2008", "/home/kevin/CyberShake/scatterMap/base_cb.txt");
//		map.addComparison("BA 2008", "/home/kevin/CyberShake/scatterMap/base_ba.txt");
//		map.printCurves();
		map.printVals();
		
		ArrayList<Symbol> symbols = new ArrayList<Symbol>();
		symbols.add(new Symbol("s", 18, 19));
		symbols.add(new Symbol("d", 28, 36));
		symbols.add(new Symbol("s", 37, 57));
		
		try {
			map.writeScript(symbols, "c", "/home/kevin/CyberShake/scatterMap/gmt/scatter.sh");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.destroy();
		
	}
}
