package scratchJavaDevelopers.kevin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.gui.servlets.siteEffect.BasinDepthClass;
import org.opensha.sha.gui.servlets.siteEffect.WillsSiteClass;
import org.opensha.sha.util.SiteTranslator;

public class CVMToXYZ {
	
	String willsFileName = "/etc/cvmfiles/usgs_cgs_geology_60s_mod.txt";
	String basinFileName = "/etc/cvmfiles/basindepth_OpenSHA.txt";
	
	LocationList locs;
	
	WillsSiteClass wills;
	BasinDepthClass basin;
	
	boolean useWeb = false;
	
	public CVMToXYZ(EvenlyGriddedGeographicRegionAPI region, boolean useWeb) {
		this.useWeb = useWeb;
		locs = region.getGridLocationsList();
		if (!useWeb) {
			wills = new WillsSiteClass(locs, willsFileName);
			wills.setLoadFromJar(true);
			basin = new BasinDepthClass(locs, basinFileName);
			basin.setLoadFromJar(true);
		}
	}
	
	public ArrayList<String> getWillsVals() throws Exception {
		if (useWeb)
			return ConnectToCVM.getWillsSiteTypeFromCVM(locs);
		else
			return wills.getWillsSiteClass();
	}
	
	public ArrayList<Double> getBasinDepthVals() throws Exception {
		if (useWeb)
			return ConnectToCVM.getBasinDepthFromCVM(locs);
		else
			return basin.getBasinDepth();
	}
	
	public void writeVs30XYZFile(String fileName) throws Exception {
		FileWriter fw = new FileWriter(fileName);
		
		ArrayList<String> classes = this.getWillsVals();
		
		if (classes.size() != locs.size()) {
			throw new RuntimeException("Wills classes not filled in at every site!!!");
		}
		
		int numLocs = classes.size();
		System.out.println("Writing " + numLocs + " vs30 vals");
		for (int i=0; i<numLocs; i++) {
			Location loc = locs.getLocationAt(i);
			double lat = loc.getLatitude();
			double lon = loc.getLongitude();
			
			double val = SiteTranslator.getVS30FromWillsClass(classes.get(i));
			
			fw.write(lat + " " + lon + " " + val + "\n");
		}
		System.out.println("done");
		
		fw.close();
	}
	
	public void writeBasinXYZFile(String fileName) throws Exception {
		FileWriter fw = new FileWriter(fileName);
		
		ArrayList<Double> classes = this.getBasinDepthVals();
		
		if (classes.size() != locs.size()) {
			throw new RuntimeException("Wills classes not filled in at every site!!!");
		}
		
		int numLocs = classes.size();
		System.out.println("Writing " + numLocs + " basin vals");
		for (int i=0; i<numLocs; i++) {
			Location loc = locs.getLocationAt(i);
			double lat = loc.getLatitude();
			double lon = loc.getLongitude();
			
			double val = classes.get(i);
			
			fw.write(lat + " " + lon + " " + val + "\n");
		}
		System.out.println("done");
		
		fw.close();
	}
	
	public static void main(String args[]) {
		String vs30File = "/home/kevin/CyberShake/scatterMap/gmt/vs30.txt";
		String basinFile = "/home/kevin/CyberShake/scatterMap/gmt/basin.txt";
		
		boolean useWeb = false;
		
		try {
			EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(33.5, 34.75, -119, -117, 0.01667);
			CVMToXYZ cvm = new CVMToXYZ(region, useWeb);
			
			cvm.writeVs30XYZFile(vs30File);
			
			region = new EvenlyGriddedRectangularGeographicRegion(33.5, 34.75, -119, -117, 0.01);
			cvm = new CVMToXYZ(region, useWeb);
			
			cvm.writeBasinXYZFile(basinFile);
		} catch (RegionConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
