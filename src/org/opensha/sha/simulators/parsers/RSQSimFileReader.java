package org.opensha.sha.simulators.parsers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.utm.UTM;
import org.opensha.commons.geo.utm.WGS84;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.EventRecord;
import org.opensha.sha.simulators.RSQSimEvent;
import org.opensha.sha.simulators.RSQSimEventRecord;
import org.opensha.sha.simulators.RectangularElement;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.TriangularElement;
import org.opensha.sha.simulators.Vertex;
import org.opensha.sha.simulators.iden.LogicalAndRupIden;
import org.opensha.sha.simulators.iden.MagRangeRuptureIdentifier;
import org.opensha.sha.simulators.iden.RegionIden;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.iden.SectionIDIden;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.opensha.sha.simulators.utils.RSQSimUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.LittleEndianDataInputStream;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;

public class RSQSimFileReader {
	
	public static List<SimulatorElement> readGeometryFile(File geomFile,int longZone, char latZone) throws IOException {
		return readGeometryFile(new FileInputStream(geomFile), longZone, latZone);
	}
	
	public static List<SimulatorElement> readGeometryFile(URL url, int longZone, char latZone) throws IOException {
		return readGeometryFile(url.openStream(), longZone, latZone);
	}
	
	public static List<SimulatorElement> readGeometryFile(InputStream is, int longZone, char latZone) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		String line = reader.readLine();
		// strip header lines
		while (line != null && line.startsWith("#"))
			line = reader.readLine();
		
		boolean triangular = isTriangular(line);
		
		List<SimulatorElement> elements = Lists.newArrayList();
		
		int elemID = 1;
		int vertexID = 1;
		
		while (line != null) {
			StringTokenizer tok = new StringTokenizer(line);
			
			try {
				if (triangular) {
					double x1 = Double.parseDouble(tok.nextToken());
					double y1 = Double.parseDouble(tok.nextToken());
					double z1 = Double.parseDouble(tok.nextToken());
					Location loc1 = utmToLoc(longZone, latZone, x1, y1, z1);
					double x2 = Double.parseDouble(tok.nextToken());
					double y2 = Double.parseDouble(tok.nextToken());
					double z2 = Double.parseDouble(tok.nextToken());
					Location loc2 = utmToLoc(longZone, latZone, x2, y2, z2);
					double x3 = Double.parseDouble(tok.nextToken());
					double y3 = Double.parseDouble(tok.nextToken());
					double z3 = Double.parseDouble(tok.nextToken());
					Location loc3 = utmToLoc(longZone, latZone, x3, y3, z3);
					
					double rake = Double.parseDouble(tok.nextToken());
					double slipRate = Double.parseDouble(tok.nextToken());
					
					int sectNum = -1;
					String sectName = null;
					
					if (tok.hasMoreTokens()) {
						sectNum = Integer.parseInt(tok.nextToken());
						if (tok.hasMoreTokens())
							sectName = tok.nextToken();
					}
					
					Vertex[] vertices = new Vertex[3];
					vertices[0] = new Vertex(loc1.getLatitude(), loc1.getLongitude(), loc1.getDepth(), vertexID++);
					vertices[1] = new Vertex(loc2.getLatitude(), loc2.getLongitude(), loc2.getDepth(), vertexID++);
					vertices[2] = new Vertex(loc3.getLatitude(), loc3.getLongitude(), loc3.getDepth(), vertexID++);
					
					int numAlongStrike = -1;
					int numDownDip = -1;
					
					// convert to m/yr form m/s
					slipRate = slipRate*General_EQSIM_Tools.SECONDS_PER_YEAR;
					
					double aseisFactor = 0d;
					FocalMechanism focalMechanism = new FocalMechanism(Double.NaN, Double.NaN, rake); // TODO
					
					SimulatorElement elem = new TriangularElement(elemID++, vertices, sectName, -1, sectNum,
							numAlongStrike, numDownDip, slipRate, aseisFactor, focalMechanism);
					elements.add(elem);
				} else {
					// rectangular
					
					// this is the center
					double x = Double.parseDouble(tok.nextToken());
					double y = Double.parseDouble(tok.nextToken());
					double z = Double.parseDouble(tok.nextToken());
					double l = Double.parseDouble(tok.nextToken());
					double w = Double.parseDouble(tok.nextToken());
					Location center = utmToLoc(longZone, latZone, x, y, z);
					
					double strike = Double.parseDouble(tok.nextToken());
					double dip = Double.parseDouble(tok.nextToken());
					double rake = Double.parseDouble(tok.nextToken());
					double slipRate = Double.parseDouble(tok.nextToken());
					
					int sectNum = -1;
					String sectName = null;
					
					if (tok.hasMoreTokens()) {
						sectNum = Integer.parseInt(tok.nextToken());
						if (tok.hasMoreTokens())
							sectName = tok.nextToken();
					}
					
					double halfWidthKM = w*0.5/1000d;
					double halfLengthKM = l*0.5/1000d;
					
					Vertex[] vertices = new Vertex[3];
					LocationVector v = new LocationVector(strike, halfLengthKM, 0d);
					Location centerLeft = LocationUtils.location(center, v);
					v.reverse();
					Location centerRight = LocationUtils.location(center, v);
					
					// a list of 4 vertices, where the order is as follows as viewed 
					// from the positive side of the fault: 0th is top left, 1st is lower left,
					// 2nd is lower right, and 3rd is upper right (counter clockwise)
					if (dip == 90) {
						// simple case
						vertices[0] = new Vertex(centerRight.getLatitude(), centerRight.getLongitude(),
								center.getDepth()+halfWidthKM, vertexID++);
						vertices[1] = new Vertex(centerRight.getLatitude(), centerRight.getLongitude(),
								center.getDepth()-halfWidthKM, vertexID++);
						vertices[2] = new Vertex(centerLeft.getLatitude(), centerLeft.getLongitude(),
								center.getDepth()-halfWidthKM, vertexID++);
						vertices[3] = new Vertex(centerLeft.getLatitude(), centerLeft.getLongitude(),
								center.getDepth()+halfWidthKM, vertexID++);
					} else {
						// more complicated
						// TODO untested!
						double dipDir = strike + 90;
						double dipDirRad = Math.toRadians(dipDir);
						double widthKM = w/1000d;
						double horizontal = widthKM * Math.cos(dipDirRad);
						double vertical = widthKM * Math.sin(dipDirRad);
						// oriented to go down dip
						v = new LocationVector(dipDir, horizontal, vertical);
						Location botLeft = LocationUtils.location(centerLeft, v);
						Location botRight = LocationUtils.location(centerRight, v);
						v.reverse();
						Location topLeft = LocationUtils.location(centerLeft, v);
						Location topRight = LocationUtils.location(centerRight, v);
						
						vertices[0] = new Vertex(topLeft.getLatitude(), topLeft.getLongitude(), topLeft.getDepth(), vertexID++);
						vertices[1] = new Vertex(botLeft.getLatitude(), botLeft.getLongitude(), botLeft.getDepth(), vertexID++);
						vertices[2] = new Vertex(botRight.getLatitude(), botRight.getLongitude(), botRight.getDepth(), vertexID++);
						vertices[3] = new Vertex(topRight.getLatitude(), topRight.getLongitude(), topRight.getDepth(), vertexID++);
					}
					
					int numAlongStrike = -1;
					int numDownDip = -1;
					
					// convert to m/yr form m/s
					slipRate = slipRate*General_EQSIM_Tools.SECONDS_PER_YEAR;
					
					double aseisFactor = 0d;
					FocalMechanism focalMechanism = new FocalMechanism(Double.NaN, Double.NaN, rake); // TODO
					
					boolean perfectRect = (float)l == (float)w;
					
					SimulatorElement elem = new RectangularElement(elemID++, vertices, sectName, -1, sectNum,
							numAlongStrike, numDownDip, slipRate, aseisFactor, focalMechanism, perfectRect);
					elements.add(elem);
				}
			} catch (RuntimeException e) {
				System.err.println("Offending line: "+line);
				throw e;
			}
			
			line = reader.readLine();
		}
		
		// see if this is a subsection based catalog
		boolean subsections = true;
		Map<String, Integer> faultIDsMap = Maps.newHashMap();
		int curFaultID = 1;
		for (SimulatorElement elem : elements) {
			String sectName = elem.getSectionName();
			if (sectName == null || !sectName.contains("Subsection")) {
				subsections = false;
				break;
			}
			String faultName = sectName.substring(0, sectName.indexOf("Subsection"));
			while (faultName.endsWith(","))
				faultName = faultName.substring(0, faultName.length()-1);
			Integer faultID = faultIDsMap.get(faultName);
			if (faultID == null) {
				faultID = curFaultID++;
				faultIDsMap.put(faultName, faultID);
			}
			// also add mapping from this subsection
			faultIDsMap.put(sectName, faultID);
		}
		if (subsections) {
			for (SimulatorElement elem : elements)
				elem.setFaultID(faultIDsMap.get(elem.getSectionName()));
		}
		
		return elements;
	}
	
	private static boolean isTriangular(String line) {
		// 0	1	2	3	4	5		6	7		8		9			10		11			12
		// triangular line:
		// x1	y1	z1	x2	y2	z2		x3	y3		z3		rake		slip	[sectNum]	[sectName]
		// rectangular line:
		// x	y	z	l	w	strike	dip	rake	slip	[sectNum]	[sectName]
		
		StringTokenizer tok = new StringTokenizer(line);
		int num = tok.countTokens();
		Preconditions.checkState(num >= 9);
		
		if (num > 11)
			// always triangular
			return true;
		if (num < 11)
			// always rectangular
			return false;
		// 11 column file can be rectangular with section info or triangular without
		// if rectangular, 11th column must be a non-numeric string. So if it can be parsed to a double,
		// then it's the slip column in a triangular file
		List<String> tokens = Lists.newArrayList();
		while (tok.hasMoreTokens())
			tokens.add(tok.nextToken());
		String tok11 = tokens.get(10);
		try {
			Double.parseDouble(tok11);
			// can be parsed, must be slip rate
			return true;
		} catch (NumberFormatException e) {
			// can't be parsed, must be section name
			return false;
		}
	}
	
	private static Location utmToLoc(int longZone, char latZone, double x, double y, double z) {
		UTM utm = new UTM(longZone, latZone, x, y);
		WGS84 wgs84 = new WGS84(utm);
		return new Location(wgs84.getLatitude(), wgs84.getLongitude(), -z/1000d);
	}
	
	public static void main(String[] args) throws IOException {
//		File dir = new File("/home/kevin/Simulators/UCERF3_125kyrs");
////		File geomFile = new File(dir, "UCERF3.1km.tri.flt");
//		File geomFile = new File(dir, "UCERF3.D3.1.1km.tri.2.flt");
		
//		File dir = new File("/home/kevin/Simulators/bruce/rundir1420");
//		File geomFile = new File(dir, "zfault_Deepen.in");
		
//		File dir = new File("/home/kevin/Simulators/UCERF3_interns/UCERF3sigmahigh");
		File dir = new File("/home/kevin/Simulators/UCERF3_interns/combine340");
		File geomFile = new File(dir, "UCERF3.D3.1.1km.tri.2.flt");
		
		List<SimulatorElement> elements = readGeometryFile(geomFile, 11, 'S');
		System.out.println("Loaded "+elements.size()+" elements");
//		for (Location loc : elements.get(0).getVertices())
//			System.out.println(loc);
		
//		File eventsDir = new File("/home/kevin/Simulators/UCERF3_qlistbig2");
//		File eventsDir = new File("/home/kevin/Simulators/UCERF3_35kyrs");
		File eventsDir = dir;
		
//		boolean bigEndian = isBigEndian(new File(eventsDir, "UCERF3base_80yrs.pList"), elements);
//		listFileDebug(new File(eventsDir, "UCERF3base_80yrs.eList"), 100, bigEndian, true);
//		boolean bigEndian = isBigEndian(new File(eventsDir, "UCERF3_35kyrs.pList"), elements);
//		listFileDebug(new File(eventsDir, "UCERF3_35kyrs.eList"), 100, bigEndian, true);
		
		List<RSQSimEvent> events = readEventsFile(eventsDir, elements, Lists.newArrayList(new MagRangeRuptureIdentifier(7d, 10d)));
		System.out.println("Loaded "+events.size()+" events");
		System.out.println("Duration: "+General_EQSIM_Tools.getSimulationDurationYears(events)+" years");
		
		SectionIDIden safIden = SectionIDIden.getUCERF3_SAF(FaultModels.FM3_1,
				RSQSimUtils.getUCERF3SubSectsForComparison(FaultModels.FM3_1, DeformationModels.GEOLOGIC), elements);
		RuptureIdentifier ssafIden = new LogicalAndRupIden(safIden, new RegionIden(new CaliforniaRegions.RELM_SOCAL()));
		List<RSQSimEvent> safEvents = ssafIden.getMatches(events);
		
		double delta = 5d;
		for (int i=0; i<safEvents.size()-3; i++) {
			RSQSimEvent e1 = safEvents.get(i);
			RSQSimEvent e2 = safEvents.get(i+1);
			RSQSimEvent e3 = safEvents.get(i+2);
			double t1 = e1.getTimeInYears();
			double t2 = e2.getTimeInYears();
			double t3 = e3.getTimeInYears();
			double myDelta = t3 - t1;
			if (myDelta < delta) {
				System.out.println("****************");
				System.out.println(e1.getID()+" M"+(float)e1.getMagnitude());
				System.out.println(e2.getID()+" M"+(float)e1.getMagnitude()+" "+(float)(t2-t1)+" yrs later");
				System.out.println(e3.getID()+" M"+(float)e3.getMagnitude()+" "+(float)(t3-t2)+" yrs later");
			}
		}
		
//		while (true) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
//		FaultModels fm = FaultModels.FM3_1;
//		SectionIDIden garlockIden = SectionIDIden.getUCERF3_Garlock(
//				fm, RSQSimUtils.getUCERF3SubSectsForComparison(fm, fm.getFilterBasis()), elements);
//		SectionIDIden safIden = SectionIDIden.getUCERF3_SAF(
//				fm, RSQSimUtils.getUCERF3SubSectsForComparison(fm, fm.getFilterBasis()), elements);
//		LogicalAndRupIden safSoCalIden = new LogicalAndRupIden(safIden, new RegionIden(new CaliforniaRegions.RELM_SOCAL()));
//		System.out.println("Num M>=7 events on both Garlock AND S.SAF: "+
//				new LogicalAndRupIden(safSoCalIden, garlockIden).getMatches(events).size());
//		for (double fract : new double[] {0d, 0.25, 0.5, 0.75, 1d}) {
//			garlockIden.setMomentFractForInclusion(fract);
//			System.out.println("Garlock M>=7 events with "+(float)(fract*100d)+" % moment: "+garlockIden.getMatches(events).size());
//			safIden.setMomentFractForInclusion(fract);
//			System.out.println("S.SAF M>=7 events with "+(float)(fract*100d)+" % moment: "+safSoCalIden.getMatches(events).size());
//		}
//		double duration = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
//		System.out.println("Duration: "+duration+" years");
//		System.out.println("\t"+events.get(0).getTimeInYears()+" to "+events.get(events.size()-1).getTimeInYears()+" years");
//		MinMaxAveTracker magTrack = new MinMaxAveTracker();
//		for (EQSIM_Event event : events)
//			magTrack.addValue(event.getMagnitude());
//		System.out.println("Mags: "+magTrack);
//		
//		RegionIden soCalIden = new RegionIden(new CaliforniaRegions.RELM_SOCAL());
//		soCalIden.getMatches(events);
	}
	
	public static List<RSQSimEvent> readEventsFile(File file, List<SimulatorElement> elements) throws IOException {
		return readEventsFile(file, elements, null);
	}
	
	public static List<RSQSimEvent> readEventsFile(File file, List<SimulatorElement> elements,
			Collection<? extends RuptureIdentifier> rupIdens) throws IOException {
		// detect file names
		if (file.isDirectory()) {
			// find the first .*List file and use that as the basis
			for (File sub : file.listFiles()) {
				if (sub.getName().endsWith(".eList")) {
					System.out.println("Found eList file in directory: "+sub.getAbsolutePath());
					return readEventsFile(sub, elements, rupIdens);
				}
			}
			throw new FileNotFoundException("Couldn't find eList file in given directory");
		}
		String name = file.getName();
		Preconditions.checkArgument(name.endsWith("List"),
				"Must supply either directory containing all list files, or one of the files themselves");
		File dir = file.getParentFile();
		String prefix = name.substring(0, name.lastIndexOf("."));
		System.out.println("Detected prefix: "+prefix);
		File eListFile = new File(dir, prefix+".eList");
		Preconditions.checkState(eListFile.exists(),
				"Couldn't find eList file with prefix %s: %s", prefix, eListFile.getAbsolutePath());
		File pListFile = new File(dir, prefix+".pList");
		Preconditions.checkState(pListFile.exists(),
				"Couldn't find eList file with prefix %s: %s", prefix, pListFile.getAbsolutePath());
		File dListFile = new File(dir, prefix+".dList");
		Preconditions.checkState(dListFile.exists(),
				"Couldn't find dList file with prefix %s: %s", prefix, dListFile.getAbsolutePath());
		File tListFile = new File(dir, prefix+".tList");
		Preconditions.checkState(tListFile.exists(),
				"Couldn't find tList file with prefix %s: %s", prefix, tListFile.getAbsolutePath());
		return readEventsFile(new FileInputStream(eListFile), new FileInputStream(pListFile), new FileInputStream(dListFile),
				new FileInputStream(tListFile), elements, rupIdens, isBigEndian(pListFile, elements));
	}
	
	public static List<RSQSimEvent> readEventsFile(File eListFile, File pListFile, File dListFile, File tListFile,
			List<SimulatorElement> elements) throws IOException {
		return readEventsFile(eListFile, pListFile, dListFile, tListFile, elements, null, isBigEndian(pListFile, elements));
	}
	
	public static List<RSQSimEvent> readEventsFile(File eListFile, File pListFile, File dListFile, File tListFile,
			List<SimulatorElement> elements, Collection<? extends RuptureIdentifier> rupIdens) throws IOException {
		return readEventsFile(eListFile, pListFile, dListFile, tListFile, elements, rupIdens, isBigEndian(pListFile, elements));
	}
	
	public static List<RSQSimEvent> readEventsFile(File eListFile, File pListFile, File dListFile, File tListFile,
			List<SimulatorElement> elements, Collection<? extends RuptureIdentifier> rupIdens, boolean bigEndian) throws IOException {
		return readEventsFile(new FileInputStream(eListFile), new FileInputStream(pListFile), new FileInputStream(dListFile),
				new FileInputStream(tListFile), elements, rupIdens, bigEndian);
	}
	
	/**
	 * Detects big endianness by checking patch IDs in the given patch ID file
	 * @param pListFile
	 * @param elements
	 * @return
	 * @throws IOException
	 */
	private static boolean isBigEndian(File pListFile, List<SimulatorElement> elements) throws IOException {
		RandomAccessFile raFile = new RandomAccessFile(pListFile, "r");
		// 4 byte ints
		long len = raFile.length();
		int numVals;
		if (len > Integer.MAX_VALUE)
			numVals = Integer.MAX_VALUE/4;
		else
			numVals = (int)(len/4l);
		
		int numToCheck = 100;
		
		// start out assuming both true, will quickly find out which one is really true
		boolean bigEndian = true;
		boolean littleEndian = true;
		
		byte[] recordBuffer = new byte[4];
		IntBuffer littleRecord = ByteBuffer.wrap(recordBuffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		IntBuffer bigRecord = ByteBuffer.wrap(recordBuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		
		Random r = new Random();
		for (int i=0; i<numToCheck; i++) {
			long pos = r.nextInt(numVals)*4l;
			
			raFile.seek(pos);
			raFile.read(recordBuffer);
			
			// IDs in this file are 1-based, convert to 0-based by subtracting one
			int littleEndianID = littleRecord.get(0) - 1;
			int bigEndianID = bigRecord.get(0) - 1;
			
			bigEndian = bigEndian && isValidPatchID(bigEndianID, elements);
			littleEndian = littleEndian && isValidPatchID(littleEndianID, elements);
		}
		
		raFile.close();
		
		Preconditions.checkState(bigEndian || littleEndian, "Couldn't detect endianness - bad patch IDs?");
		Preconditions.checkState(!bigEndian || !littleEndian, "Passed both big and little endian tests???");
		return bigEndian;
	}
	
	private static boolean isValidPatchID(int patchID, List<SimulatorElement> elements) {
		return patchID > 0 && patchID <= elements.size();
	}
	
	/**
	 * Read RSQSim *List binary files
	 * 
	 * @param eListStream
	 * @param pListStream
	 * @param dListStream
	 * @param tListStream
	 * @param elements
	 * @param bigEndian
	 * @return
	 * @throws IOException
	 */
	private static List<RSQSimEvent> readEventsFile(
			InputStream eListStream, InputStream pListStream, InputStream dListStream, InputStream tListStream,
			List<SimulatorElement> elements, Collection<? extends RuptureIdentifier> rupIdens, boolean bigEndian)
					throws IOException {
		if (!(eListStream instanceof BufferedInputStream))
			eListStream = new BufferedInputStream(eListStream);
		if (!(pListStream instanceof BufferedInputStream))
			pListStream = new BufferedInputStream(pListStream);
		if (!(dListStream instanceof BufferedInputStream))
			dListStream = new BufferedInputStream(dListStream);
		if (!(tListStream instanceof BufferedInputStream))
			tListStream = new BufferedInputStream(tListStream);
		
		DataInput eIn, pIn, dIn, tIn;
		if (bigEndian) {
			eIn = new DataInputStream(eListStream);
			pIn = new DataInputStream(pListStream);
			dIn = new DataInputStream(dListStream);
			tIn = new DataInputStream(tListStream);
		} else {
			eIn = new LittleEndianDataInputStream(eListStream);
			pIn = new LittleEndianDataInputStream(pListStream);
			dIn = new LittleEndianDataInputStream(dListStream);
			tIn = new LittleEndianDataInputStream(tListStream);
		}
		
		// one EventRecord for each section, or one in total if elements don't have section information
		int curEventID = -1;
		Map<Integer, RSQSimEventRecord> curRecordMap = Maps.newHashMap();
		
		HashSet<Integer> eventIDsLoaded = new HashSet<Integer>();
		
		List<RSQSimEvent> events = Lists.newArrayList();
		
		int eventID, patchID;
		double slip, time;
		
		while (true) {
			try {
				eventID = eIn.readInt(); // 1-based, keep as is for now as it shouldn't matter
				patchID = pIn.readInt(); // 1-based, which matches readGeometry
				slip = dIn.readDouble(); // in meters
				time = tIn.readDouble(); // in seconds
				
//				if (eventID % 10000 == 0 && curEventID != eventID)
//					System.out.println("Loading eventID="+eventID+". So far kept "+events.size()+" events");
				
				Preconditions.checkState(isValidPatchID(patchID, elements));
				
				SimulatorElement element = elements.get(patchID-1);
				Preconditions.checkState(element.getID() == patchID, "Elements not sequential");
				double elementMoment = FaultMomentCalc.getMoment(element.getArea(), slip);
				
				if (eventID != curEventID) {
					if (!curRecordMap.isEmpty()) {
						Preconditions.checkState(!eventIDsLoaded.contains(curEventID),
								"Duplicate eventID found, file is out of order or corrupt: %s", curEventID);
						eventIDsLoaded.add(curEventID);
						RSQSimEvent event = buildEvent(curEventID, curRecordMap, rupIdens);
						if (event != null)
							// can be null if filters were supplied
							events.add(event);
					}
					curRecordMap.clear();
					curEventID = eventID;
				}
				
				// EventRecord for this individual fault section in this event
				RSQSimEventRecord event = curRecordMap.get(element.getSectionID());
				if (event == null) {
					event = new RSQSimEventRecord(elements);
					curRecordMap.put(element.getSectionID(), event);
					event.setTime(time);
					event.setMoment(0);
					event.setSectionID(element.getSectionID());
				}
				
				event.addSlip(patchID, slip);
				event.setTime(Math.min(time, event.getTime()));
				event.setMoment(event.getMoment()+elementMoment);
			} catch (EOFException e) {
				break;
			}
		}
		if (!curRecordMap.isEmpty()) {
			Preconditions.checkState(!eventIDsLoaded.contains(curEventID),
					"Duplicate eventID found, file is out of order or corrupt: %s", curEventID);
			eventIDsLoaded.add(curEventID);
			RSQSimEvent event = buildEvent(curEventID, curRecordMap, rupIdens);
			if (event != null)
				// can be null if filters were supplied
				events.add(event);
		}
		((FilterInputStream)eIn).close();
		((FilterInputStream)pIn).close();
		((FilterInputStream)dIn).close();
		((FilterInputStream)tIn).close();
		
		Collections.sort(events);
		
		return events;
	}
	
	private static void listFileDebug(File file, int numToPrint, boolean bigEndian, boolean integer) throws IOException {
		InputStream fin = new BufferedInputStream(new FileInputStream(file));
		
		System.out.println(file.getName()+" big endian? "+bigEndian);
		
		DataInput dataIn;
		
		if (bigEndian) {
			dataIn = new DataInputStream(fin);
		} else {
			dataIn = new LittleEndianDataInputStream(fin);
		}
		
		int count = 0;
		while (true) {
			try {
				if (count == numToPrint)
					break;
				Object val;
				if (integer)
					val = dataIn.readInt();
				else
					val = dataIn.readDouble();
				System.out.println(count+":\t"+val);
				count++;
			} catch (EOFException e) {
				break;
			}
		}
		((FilterInputStream)dataIn).close();
	}
	
	private static EventRecordTimeComparator recordTimeComp = new EventRecordTimeComparator();
	
	private static RSQSimEvent buildEvent(int eventID, Map<Integer, RSQSimEventRecord> records,
			Collection<? extends RuptureIdentifier> rupIdens) {
		List<RSQSimEventRecord> recordsForEvent = Lists.newArrayList(records.values());
		
		// sort records by time, earliest first
		Collections.sort(recordsForEvent, recordTimeComp);
		
		// calculate magnitude
		double totMoment = 0; // in N-m
		for (EventRecord rec : recordsForEvent)
			totMoment += rec.getMoment();
		double mag = MagUtils.momentToMag(totMoment);
		
		// set global properties in each record
		for (RSQSimEventRecord rec : recordsForEvent) {
			rec.setMagnitude(mag);
			rec.setTime(recordsForEvent.get(0).getTime()); // global according to class docs
			rec.setID(eventID);
			double recordArea = 0;
			for (SimulatorElement elem : rec.getElements())
				recordArea += elem.getArea();
			rec.setArea(recordArea);
			// TODO duration?
		}
		
		RSQSimEvent event = new RSQSimEvent(recordsForEvent);
		
		if (rupIdens != null) {
			boolean keep = false;
			for (RuptureIdentifier iden : rupIdens) {
				if (iden.isMatch(event)) {
					keep = true;
					break;
				}
			}
			if (!keep)
				return null;
		}
		return event;
	}
	
	private static class EventRecordTimeComparator implements Comparator<RSQSimEventRecord> {

		@Override
		public int compare(RSQSimEventRecord o1, RSQSimEventRecord o2) {
			return Double.compare(o1.getTime(), o2.getTime());
		}
		
	}

}
