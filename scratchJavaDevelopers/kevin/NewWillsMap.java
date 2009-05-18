package scratchJavaDevelopers.kevin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.commons.data.region.EvenlyGriddedRELM_TestingRegion;
import org.opensha.sha.gui.servlets.siteEffect.WillsSiteClass;

public class NewWillsMap {
	
//	public static final String BIN_FILE = "/home/scec-00/kmilner/wills/out.bin";
	public static final String BIN_FILE = "/home/kevin/OpenSHA/siteClass/out.bin";
	
	public static final int nx = 49867;
	public static final int ny = 44016;
	
	public static final int short_bits = 2;
	public static final long sizeOfX = nx * short_bits;
	
	public static final double spacing = 0.00021967246502752;
	
	public static final double xll_corner = -124.52997177169;
	public static final double yll_corner = 32.441345502265;
	// yul = yll + size * ny
	public static final double yul_corner = yll_corner + spacing * ny;
	public static final double xur_corner = xll_corner + spacing * nx;
	
	EvenlyGriddedGeographicRegion region;
	
	String willsFileName = "/etc/cvmfiles/usgs_cgs_geology_60s_mod.txt";
	
	public NewWillsMap(EvenlyGriddedGeographicRegion region) {
		this.region = region;
		System.out.println("XLL: " + xll_corner);
		System.out.println("YLL: " + yll_corner);
		System.out.println("YUL: " + yul_corner);
		System.out.println("XUR: " + xur_corner);
	}
	
	private void calcOld() {
		WillsSiteClass wills = new WillsSiteClass(region.getGridLocationsList(), willsFileName);
		wills.setLoadFromJar(true);
		long start = System.currentTimeMillis();
		ArrayList<String> vals = wills.getWillsSiteClass();
		System.out.println("Loaded " + vals.size() + " locations!");
		long time = System.currentTimeMillis() - start;
		boolean print = false;
		if (print) {
			for (String val : vals) {
				System.out.println(val);
			}
		}
		printTime(time);
		int setVals = 0;
		int num = region.getGridLocationsList().size();
		for (String val : vals) {
			if (!(val.toLowerCase().contains("nan") || val.toLowerCase().contains("na")))
				setVals++;
		}
		System.out.println("Set " + setVals + "/" + num);
	}
	
	private void printTime(long time) {
		double val = (double)time / 1000d;
		System.out.println(val + " seconds");
	}
	
	private void calcNew() throws IOException {
//		FileWriter fw = new FileWriter("/tmp/newXYZ.txt");
		
		int num = region.getNumGridLocs();
		
		RandomAccessFile file = new RandomAccessFile(new File(BIN_FILE), "r");
		
		long start = System.currentTimeMillis();
		int setVals = 0;
		int modVal = 10000;
		long prevSeek = 0;
		int posSeeks = 0;
		int negSeeks = 0;
		
		byte[] recordBuffer = new byte[2];
		ByteBuffer record = ByteBuffer.wrap(recordBuffer);
		record.order(ByteOrder.LITTLE_ENDIAN);
		ShortBuffer shortBuff = record.asShortBuffer();
		for (int i=0; i<num; i++) {
			Location loc = region.getGridLocation(i);
			
			if (loc.getLatitude() < yll_corner || loc.getLatitude() > yul_corner || loc.getLongitude() < xll_corner
					|| loc.getLongitude() > xur_corner) {
//				if (i % modVal == 0)
//					System.out.println("Skipping " + i + " for: " + loc.toString());
				continue;
			}
			
			long seek = getFilePosition(loc.getLatitude(), loc.getLongitude());
			if (seek - prevSeek < 0)
				negSeeks++;
			else
				posSeeks++;
			prevSeek = seek;
//			if (i % modVal == 0) {
//				System.out.println("Seeking " + i + " to " + seek + " for " + loc.toString() + " pos: " + posSeeks + ", neg: " + negSeeks);
//			}
//			System.out.println("Seeking to " + seek);
			
			file.seek(seek);
			file.read(recordBuffer);
			int val = shortBuff.get(0);
			
//			System.out.println(val);
			if (val > 0) {
				setVals++;
//				fw.write(loc.getLongitude() + " " + loc.getLatitude() + " " + val + "\n");
//				fw.write(loc.getLatitude() + " " + loc.getLongitude() + " " + val + "\n");
			}
//			System.out.println("Read: " + val);
		}
		long time = System.currentTimeMillis() - start;
		System.out.println("Set " + setVals + "/" + num + " pos: " + posSeeks + ", neg: " + negSeeks);
		printTime(time);
//		fw.close();
	}
	
	public static long getFilePosition(double lat, double lon) {
		long x = getX(lon);
		long y = getY(lat);
		
		long pos = (y * sizeOfX) + x * short_bits;
		return pos;
	}
	
	public static long getX(double lon) {
		return (long)((lon - xll_corner) / spacing + 0.5);
	}
	
	public static long getY(double lat) {
		return (long)((yul_corner - lat) / spacing + 0.5);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		EvenlyGriddedGeographicRegion region = new EvenlyGriddedRELM_TestingRegion();
		region.setGridSpacing(0.02);
		
		NewWillsMap wills = new NewWillsMap(region);
		
		wills.calcOld();
		wills.calcNew();
	}

}
