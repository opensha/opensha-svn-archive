package scratch.UCERF3.erf.ETAS;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import org.opensha.commons.geo.Location;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

public class ETAS_CatalogIO {
	
	public static void writeCatalogBinary(File file, List<ETAS_EqkRupture> catalog) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkNotNull(catalog, "Catalog cannot be null!");

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

		writeCatalogBinary(out, catalog);

		out.close();
	}
	
	public static void writeCatalogsBinary(File file, List<List<ETAS_EqkRupture>> catalogs) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkNotNull(catalogs, "Catalog cannot be null!");
		Preconditions.checkArgument(!catalogs.isEmpty(), "Must supply at least one catalog");

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

		// write number of catalogs as int
		out.writeInt(catalogs.size());
		
		for (List<ETAS_EqkRupture> catalog : catalogs)
			writeCatalogBinary(out, catalog);

		out.close();
	}
	
	private static void writeCatalogBinary(DataOutputStream out, List<ETAS_EqkRupture> catalog) throws IOException {
		// write file version as short
		out.writeShort(1);
		
		// write catalog size as int
		out.writeInt(catalog.size());
		
		// text fields: Year\tMonth\tDay\tHour\tMinute\tSec\tLat\tLon\tDepth\tMagnitude\t"
		// "ID\tparID\tGen\tOrigTime\tdistToParent\tnthERFIndex\tFSS_ID\tGridNodeIndex
		
		// binary format:
		// id - int
		// parent id - int
		// generation - short
		// origin time - long
		// latitude - double
		// longitude - double
		// depth - double
		// magnitude - double
		// distance to parent - double
		// nth ERF index - int
		// FSS index - int
		// grid node index - int
		
		for (ETAS_EqkRupture rup : catalog) {
			out.writeInt(rup.getID());
			out.writeInt(rup.getParentID());
			out.writeShort(rup.getGeneration());
			out.writeLong(rup.getOriginTime());
			Location hypo = rup.getHypocenterLocation();
			out.writeDouble(hypo.getLatitude());
			out.writeDouble(hypo.getLongitude());
			out.writeDouble(hypo.getDepth());
			out.writeDouble(rup.getMag());
			out.writeDouble(rup.getDistanceToParent());
			out.writeInt(rup.getNthERF_Index());
			out.writeInt(rup.getFSSIndex());
			out.writeInt(rup.getGridNodeIndex());
		}
	}
	
	public static List<ETAS_EqkRupture> loadCatalogBinary(File file) throws IOException {
		return loadCatalogBinary(file, -10d);
	}
	
	public static List<ETAS_EqkRupture> loadCatalogBinary(File file, double minMag) throws IOException {
		return loadCatalogBinary(getIS(file), minMag);
	}
	
	public static List<ETAS_EqkRupture> loadCatalogBinary(InputStream is, double minMag) throws IOException {
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		if (!(is instanceof BufferedInputStream))
			is = new BufferedInputStream(is);
		DataInputStream in = new DataInputStream(is);
		
		List<ETAS_EqkRupture> catalog = loadCatalogBinary(in, minMag);
		
		in.close();
		
		return catalog;
	}
	
	public static List<List<ETAS_EqkRupture>> loadCatalogsBinary(File file) throws IOException {
		return loadCatalogsBinary(file, -10d);
	}
	
	public static List<List<ETAS_EqkRupture>> loadCatalogsBinary(File file, double minMag) throws IOException {
		return loadCatalogsBinary(getIS(file), minMag);
	}
	
	private static final int buffer_len = 65536;
	
	private static InputStream getIS(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkArgument(file.exists(), "File doesn't exist!");
		
		FileInputStream fis = new FileInputStream(file);
		
		if (file.getName().toLowerCase().endsWith(".gz"))
			return new GZIPInputStream(fis, buffer_len);
		return new BufferedInputStream(fis, buffer_len);
	}
	
	public static List<List<ETAS_EqkRupture>> loadCatalogsBinary(InputStream is, double minMag) throws IOException {
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		if (!(is instanceof BufferedInputStream) && !(is instanceof GZIPInputStream))
			is = new BufferedInputStream(is);
		DataInputStream in = new DataInputStream(is);
		
		List<List<ETAS_EqkRupture>> catalogs = Lists.newArrayList();
		
		int numCatalogs = in.readInt();
		
		Preconditions.checkState(numCatalogs > 0, "Bad num catalogs: %s", numCatalogs);
		
		for (int i=0; i<numCatalogs; i++)
			catalogs.add(loadCatalogBinary(in, minMag));
		
		in.close();
		
		return catalogs;
	}
	
	private static List<ETAS_EqkRupture> loadCatalogBinary(DataInputStream in, double minMag) throws IOException {
		short version = in.readShort();
		
		Preconditions.checkState(version == 1, "Unknown binary file version: "+version);
		
		int numRups = in.readInt();
		
		Preconditions.checkState(numRups >= 0, "Bad num rups: "+numRups);
		
		List<ETAS_EqkRupture> catalog = Lists.newArrayList();
		
		for (int i=0; i<numRups; i++) {
			int id = in.readInt();
			int parentID = in.readInt();
			int gen = in.readShort();
			long origTime = in.readLong();
			double lat = in.readDouble();
			double lon = in.readDouble();
			double depth = in.readDouble();
			double mag = in.readDouble();
			double distToParent = in.readDouble();
			int nthERFIndex = in.readInt();
			int fssIndex = in.readInt();
			int gridNodeIndex = in.readInt();
			
			if (mag < minMag)
				continue;
			
			Location loc = new Location(lat, lon, depth);
			
			ETAS_EqkRupture rup = new ETAS_EqkRupture();
			
			rup.setNthERF_Index(nthERFIndex);
			rup.setID(id);
			rup.setParentID(parentID);
			rup.setGeneration(gen);
			rup.setOriginTime(origTime);
			rup.setDistanceToParent(distToParent);
			rup.setMag(mag);
			rup.setHypocenterLocation(loc);
			rup.setFSSIndex(fssIndex);
			rup.setGridNodeIndex(gridNodeIndex);
			
			catalog.add(rup);
		}
		
		return catalog;
	}
	
	private static void assertEquals(ETAS_EqkRupture expected, ETAS_EqkRupture actual) {
		Preconditions.checkState(expected.getID() == actual.getID());
		Preconditions.checkState(expected.getParentID() == actual.getParentID());
		Preconditions.checkState(expected.getGeneration() == actual.getGeneration());
		Preconditions.checkState(expected.getOriginTime() == actual.getOriginTime());
		if (Double.isNaN(expected.getDistanceToParent()))
			Preconditions.checkState(Double.isNaN(actual.getDistanceToParent()));
		else
			Preconditions.checkState(expected.getDistanceToParent() == actual.getDistanceToParent(),
				"%s != %s", expected.getDistanceToParent(), actual.getDistanceToParent());
		Preconditions.checkState(expected.getMag() == actual.getMag());
		Preconditions.checkState(expected.getHypocenterLocation().equals(actual.getHypocenterLocation()));
		Preconditions.checkState(expected.getFSSIndex() == actual.getFSSIndex());
		Preconditions.checkState(expected.getGridNodeIndex() == actual.getGridNodeIndex());
	}

	public static void main(String[] args) throws ZipException, IOException {
//		File resultsZipFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
//				+ "2015_08_07-mojave_m7-poisson-grCorr/results_m4.zip");
//		File resultsZipFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
//				+ "2015_08_07-mojave_m7-full_td/results.zip");
//		
//		boolean validate = false;
//		
//		Stopwatch timer = Stopwatch.createStarted();
//		List<List<ETAS_EqkRupture>> origCatalogs = ETAS_MultiSimAnalysisTools.loadCatalogsZip(resultsZipFile);
//		timer.stop();
//		long time = timer.elapsed(TimeUnit.SECONDS);
//		System.out.println("ASCII loading took "+time+" seconds");
//		
//		File binaryFile = new File("/tmp/catalog.bin");
//		timer.reset();
//		timer.start();
//		writeCatalogsBinary(binaryFile, origCatalogs);
//		timer.stop();
//		time = timer.elapsed(TimeUnit.SECONDS);
//		System.out.println("Binary writing took "+time+" seconds");
//		
//		if (!validate)
//			origCatalogs = null;
//		System.gc();
//		
//		timer.reset();
//		timer.start();
//		List<List<ETAS_EqkRupture>> newCatalogs = loadCatalogsBinary(binaryFile);
//		timer.stop();
//		time = timer.elapsed(TimeUnit.SECONDS);
//		System.out.println("Binary loading took "+time+" seconds ("+newCatalogs.size()+" catalogs)");
//		
//		// now validate
//		if (validate) {
//			Random r = new Random();
//			for (int i=0; i<origCatalogs.size(); i++) {
//				List<ETAS_EqkRupture> catalog1 = origCatalogs.get(i);
//				List<ETAS_EqkRupture> catalog2 = newCatalogs.get(i);
//				
//				Preconditions.checkState(catalog1.size() == catalog2.size());
//				
//				for (int j=0; j<100; j++) {
//					int index = r.nextInt(catalog1.size());
//					assertEquals(catalog1.get(index), catalog2.get(index));
//				}
//			}
//		}

//		Stopwatch timer = Stopwatch.createStarted();
//		List<List<ETAS_EqkRupture>> newCatalogs = loadCatalogsBinary(
//				new GZIPInputStream(new FileInputStream(new File("/tmp/catalog.bin.gz"))), -10d);
//		timer.stop();
//		long time = timer.elapsed(TimeUnit.SECONDS);
//		System.out.println("Binary loading took "+time+" seconds ("+newCatalogs.size()+" catalogs)");
	}

}
