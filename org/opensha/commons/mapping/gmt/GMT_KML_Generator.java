package org.opensha.commons.mapping.gmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.mapping.gmt.raster.RasterExtractor;
import org.opensha.commons.util.FileUtils;

public class GMT_KML_Generator {
	
	String psFile;
	double minLat;
	double minLon;
	double maxLat;
	double maxLon;
	
	public GMT_KML_Generator(String psFile, double minLat, double maxLat,
			double minLon, double maxLon) {
		this.psFile = psFile;
		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.maxLon = maxLon;
	}
	
	private void extract(String pngFile) throws FileNotFoundException, IOException {
		RasterExtractor raster = new RasterExtractor(psFile, pngFile);
		raster.writePNG();
	}
	
	private void writeKML(String kmlFileName, String imgFile) throws IOException {
		FileWriter fw = new FileWriter(kmlFileName);
		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+"\n");
		fw.write("<kml xmlns=\"http://earth.google.com/kml/2.2\">"+"\n");
		fw.write("  <Folder>"+"\n");
		fw.write("    <name>OpenSHA Hazard Maps</name>"+"\n");
		fw.write("    <description>Open Seismic Hazard Analysis</description>"+"\n");
		fw.write("    <GroundOverlay>"+"\n");
		fw.write("      <name>Hazard Map</name>"+"\n");
		fw.write("      <description></description>"+"\n");
		fw.write("      <Icon>"+"\n");
		fw.write("        <href>" + imgFile + "</href>"+"\n");
		fw.write("      </Icon>"+"\n");
		fw.write("      <LatLonBox>"+"\n");
		fw.write("        <north>" + maxLat + "</north>"+"\n");
		fw.write("        <south>" + minLat + "</south>"+"\n");
		fw.write("        <east>" + maxLon + "</east>"+"\n");
		fw.write("        <west>" + minLon + "</west>"+"\n");
		fw.write("        <rotation>0</rotation>"+"\n");
		fw.write("      </LatLonBox>"+"\n");
		fw.write("    </GroundOverlay>"+"\n");
		fw.write("  </Folder>"+"\n");
		fw.write("</kml>"+"\n");
		fw.write(""+"\n");
		
		fw.flush();
		fw.close();
	}
	
	public void makeKMZ(String kmzFileName) throws FileNotFoundException, IOException {
		File kmzFile = new File(kmzFileName);
		
		String parent = kmzFile.getParent();
		if (parent == null)
			throw new IllegalArgumentException("KMZ file doesn't have a parent!");
		if (!parent.endsWith(File.separator))
			parent += File.separator;
		
		ArrayList<String> zipFiles = new ArrayList<String>();
		
		String pngFileName = "map.png";
		String absPNGFileName = parent + pngFileName;
		extract(absPNGFileName);
		zipFiles.add(pngFileName);
		
		String kmlFileName = "map.kml";
		String absKMLFileName = parent + kmlFileName;
		writeKML(absKMLFileName, pngFileName);
		zipFiles.add(kmlFileName);
		
		FileUtils.createZipFile(kmzFileName, parent, zipFiles);
		
		for (String fileName : zipFiles) {
			new File(parent + fileName).delete();
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String psFile = null;
		String kmzFile = null;
		double minLat = 0;
		double maxLat = 0;
		double minLon = 0;
		double maxLon = 0;
		
		if (args.length == 6) {
			psFile = args[0];
			kmzFile = args[1];
			minLat = Double.parseDouble(args[2]);
			maxLat = Double.parseDouble(args[3]);
			minLon = Double.parseDouble(args[4]);
			maxLon = Double.parseDouble(args[5]);
		} else {
			System.err.println("USAGE: GMT_KML_Generator ps_file kmz_file minLat maxLat minLon maxLon");
			System.exit(2);
		}
		
		GMT_KML_Generator gen = new GMT_KML_Generator(psFile, minLat, maxLat, minLon, maxLon);
		
		gen.makeKMZ(kmzFile);
	}

}
