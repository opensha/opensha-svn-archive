package org.opensha.sha.simulators.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.utm.UTM;
import org.opensha.commons.geo.utm.WGS84;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.simulators.RectangularElement;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.TriangularElement;
import org.opensha.sha.simulators.Vertex;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
		
		boolean triangular = isTriangular(line);
		
		List<SimulatorElement> elements = Lists.newArrayList();
		
		int elemID = 0;
		int vertexID = 0;
		
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
					FocalMechanism focalMechanism = null;
					
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
					FocalMechanism focalMechanism = null;
					
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
		Preconditions.checkState(num >= 9 && num <= 13);
		
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
		File file = new File("/home/kevin/Simulators/UCERF3_35kyrs/UCERF3.1km.tri.flt");
		List<SimulatorElement> elements = readGeometryFile(file, 11, 'S');
		System.out.println("Loaded "+elements.size()+" elements");
		for (Location loc : elements.get(0).getVertices())
			System.out.println(loc);
	}

}
