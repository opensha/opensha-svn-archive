package org.opensha.sha.earthquake.observedEarthquake.ngaWest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.GriddedSurfaceImpl;

import com.google.common.base.Preconditions;

public class NGAWestParser {
	
	private static final FilenameFilter polFilter = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".POL");
		}
	};
	
	public static HashMap<Integer, ArrayList<EvenlyGriddedSurface>> loadPolTlls(File dir) throws IOException {
		HashMap<Integer, ArrayList<EvenlyGriddedSurface>> map = new HashMap<Integer, ArrayList<EvenlyGriddedSurface>>();
		
		for (File polFile : dir.listFiles(polFilter)) {
			File tllFile = new File(dir, polFile.getName().replace(".POL", ".TLL"));
			
			if (!tllFile.exists()) {
				System.out.println("No TLL file for "+polFile.getName());
				continue;
			}
			
			/* -------- load the TLL -------- */
			
			ArrayList<String> tllLines = FileUtils.loadFile(tllFile.getAbsolutePath());
			
			String nameLine = tllLines.get(0).trim();
			String originLine = tllLines.get(1);
			String hypoLine = tllLines.get(2);
			
			// load the name
			int firstSpace = nameLine.indexOf(' ');
			int eqID = Integer.parseInt(nameLine.substring(0, firstSpace));
			nameLine = nameLine.substring(firstSpace).trim();
			String name = "";
			for (int i=0; i<nameLine.length(); i++) {
				char c = nameLine.charAt(i);
				if (c == ' ' && name.endsWith(" "))
					break;
				name += c;
			}
			name = name.trim();
			
			StringTokenizer origTok = new StringTokenizer(originLine);
			double origLat = Double.parseDouble(origTok.nextToken());
			double origLon = Double.parseDouble(origTok.nextToken());
			Location origin = new Location(origLat, origLon);
			
			/* -------- load the POL -------- */
			
			ArrayList<String> polLines = FileUtils.loadFile(polFile.getAbsolutePath());
			polLines.remove(0); // header
//			int numPts = Integer.parseInt(polLines.remove(0).trim());
//			Preconditions.checkState(numPts == 5, "numPts should always be 5!");
			polLines.remove(0).trim();
			
			Location[][] locs = null;
			
			int size = -1;
			
			for (int i=0; i<4; i++) {
				String line = polLines.get(i);
				
				StringTokenizer tok = new StringTokenizer(line.trim());
				
				if (size < 0) {
					size = tok.countTokens()/3;
					Preconditions.checkState(size>0, "Size is 0 for line: "+line);
					locs = new Location[size][4];
				} else {
					int mySize = tok.countTokens()/3;
					Preconditions.checkState(size == mySize, "inconsistent sizes for "+polFile.getName()
							+" (expected="+size+", actual="+mySize+")\nline: "+line);
				}
				
				for (int j=0; j<size; j++) {
					double kmEast = Double.parseDouble(tok.nextToken());
					double kmNorth = Double.parseDouble(tok.nextToken());
					double dep = -Double.parseDouble(tok.nextToken());
					
					// move north
					Location loc = LocationUtils.location(origin, 0, kmNorth);
					// move east
					loc = LocationUtils.location(loc, Math.PI/2d, kmEast);
					
					locs[j][i] = new Location(loc.getLatitude(), loc.getLongitude(), dep);
				}
			}
			
			// if all locs are the same, we can just skip this as there is actually no finite rupture
			boolean equal = true;
			Location prev = null;
			for (Location[] locArray : locs) {
				for (Location loc : locArray) {
					if (prev == null) {
						prev = loc;
						continue;
					}
					equal = equal && loc.getLatitude() == prev.getLatitude() && loc.getLongitude() == prev.getLongitude();
					prev = loc;
					if (!equal)
						break;
				}
				if (!equal)
					break;
			}
			if (equal)
				continue;
					
			
			GriddedSurfaceImpl surface = new GriddedSurfaceImpl(2, size+1);
			
			Preconditions.checkState(surface.size()>=4, "surface's size is <4: "+surface.size()+" (dims: 2x"+(size+1)+")");
			
			for (int i=0; i<size; i++) {
				if (i == 0) {
					surface.set(0, i, locs[i][0]);
					surface.set(1, i, locs[i][3]);
				}
				surface.set(0, i+1, locs[i][1]);
				surface.set(1, i+1, locs[i][2]);
			}
			
			if (!map.containsKey(eqID))
				map.put(eqID, new ArrayList<EvenlyGriddedSurface>());
			map.get(eqID).add(surface);
		}
		
		return map;
	}
	
	public static ArrayList<NGAWestEqkRupture> loadNGAWestFiles(File excelFile, File polTllDir) throws IOException {
		Preconditions.checkNotNull(excelFile, "Excel file is null!");
		Preconditions.checkArgument(excelFile.exists(), "Excel file doesn't exist!");
		Preconditions.checkArgument(excelFile.isFile(), "Excel file isn't a regular file!");
		
		Preconditions.checkNotNull(polTllDir, "Pol/Tll directory is null!");
		Preconditions.checkArgument(polTllDir.exists(), "Excel directory doesn't exist!");
		Preconditions.checkArgument(polTllDir.isDirectory(), "Excel directory isn't a directory!");
		
		// first load the pol/tll files
		HashMap<Integer, ArrayList<EvenlyGriddedSurface>> surfaces = loadPolTlls(polTllDir);
		
		ArrayList<NGAWestEqkRupture> rups = new ArrayList<NGAWestEqkRupture>();
		
		POIFSFileSystem fs = new POIFSFileSystem(new BufferedInputStream(new FileInputStream(excelFile)));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		for (int i=1; i<=sheet.getLastRowNum(); i++) {
			HSSFRow row = sheet.getRow(i);
			// make sure this is a valid row first by checking that it has a year entry
			try {
				double test = row.getCell(2).getNumericCellValue();
				if (test <= 0)
					continue;
			} catch (Exception e) {
				continue;
			}
			
			NGAWestEqkRupture rup = new NGAWestEqkRupture(row);
			int id = rup.getId();
			if (surfaces.containsKey(id)) {
				rup.setFiniteRuptureSurfaces(surfaces.get(id));
			}
			Preconditions.checkState(rup.isFiniteRuptureModel() == (rup.getFiniteRuptureSurfaces() != null),
					"Excel sheet & existance of POL/TLL files doesn't match up! EQ: "+id);
			rups.add(rup);
		}
		
		return rups;
	}
	
	public static void main(String[] args) throws IOException {
		Location zoo = new Location(38.92875, -77.04927);
		Location quake = new Location(37.936, -77.933);
		double dist = LocationUtils.linearDistance(zoo, quake);
		System.out.println("Distance from epicenter to zoo (KM): "+dist);
		double pTimeTravel = dist / 8d;
		double sTimeTravel = dist / 3.5d;
		System.out.println("pTimeTravel (assuming 8km/s p wave speed): "+pTimeTravel);
		System.out.println("sTimeTravel (assuming 3.5km/s s wave speed): "+sTimeTravel);
		System.out.println("delta: "+(sTimeTravel-pTimeTravel));
		System.exit(0);
		File polTllDir = new File("src"+File.separator+"resources"+File.separator+"data"+File.separator+"ngaWest");
		File excelFile = new File(polTllDir, "EQ.V8.xls");
		
		for (NGAWestEqkRupture rup : loadNGAWestFiles(excelFile, polTllDir))
			if (rup.isFiniteRuptureModel())
				System.out.println(rup);
	}

}
