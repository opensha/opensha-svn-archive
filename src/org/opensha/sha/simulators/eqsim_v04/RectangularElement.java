package org.opensha.sha.simulators.eqsim_v04;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.faultSurface.FourPointEvenlyGriddedSurface;

/**
 * This uses "id" rather than "index" to avoid confusion from the fact that our indexing 
 * starts from zero (and that in EQSIM docs starts from 1)
 * @author field
 *
 */
public class RectangularElement {
	
	// these are the official variables
	private int id;	// this is referred to as "index" in the EQSIM documentation
	private Vertex[] vertices;
	private FocalMechanism focalMechanism;
	private double slipRate;
	private double aseisFactor;
	boolean perfect;

	// these are other variable (e.g., used by Ward's simulator)
	private String sectionName;
	private int sectionID;	
	private int faultID;
	private int numAlongStrike;
	private int numDownDip;
	
	/**
	 * This creates the RectangularElement from the supplied information.  Note that this assumes
	 * the vertices correspond to a perfect rectangle.
	 * @param id - an integer for identification (referred to as "index" in the EQSIM documentation)
	 * @param vertices - a list of 4 vertices, where the order is as follows as viewed 
	 *                   from the positive side of the fault: 0th is top left, 1st is lower left,
	 *                   2nd is lower right, and 3rd is upper right (counter clockwise)
	 * @param sectionName - the name of the fault section that this element is on
	 * @param faultID - the ID of the original fault (really needed?)
	 * @param sectionID - the ID of the associated fault section
	 * @param numAlongStrike - index along strike on the fault section
	 * @param numDownDip - index down dip
	 * @param slipRate - slip rate (meters/year; note that this is different from the EQSIM convention (which is m/s))
	 * @param aseisFactor - aseismicity factor
	 * @param focalMechanism - this contains the strike, dip, and rake
	 */
	public RectangularElement(int id, Vertex[] vertices, String sectionName,
			int faultID, int sectionID, int numAlongStrike, int numDownDip,
			double slipRate, double aseisFactor, FocalMechanism focalMechanism, 
			boolean perfectRect) {

		if(vertices.length !=4 )
			throw new RuntimeException("RectangularElement: vertices.length should equal 4");
		
		this.id = id;
		this.vertices = vertices;
		this.sectionName = sectionName;
		this.faultID = faultID;
		this.sectionID = sectionID;
		this.numAlongStrike = numAlongStrike;
		this.numDownDip = numDownDip;
		this.slipRate = slipRate;
		this.aseisFactor = aseisFactor;
		this.focalMechanism = focalMechanism;
		this.perfect = perfectRect;
		
		this.perfect = true;
		
	}
	
	public FourPointEvenlyGriddedSurface getGriddedSurface() {
		return new FourPointEvenlyGriddedSurface(vertices[0],vertices[1],vertices[2],vertices[3]);
	}
	
	/**
	 * This returns the section name for now
	 * @return
	 */
	public String getName() {
		return sectionName;
	}

	public int getID() {
		return id;
	}

	public Vertex[] getVertices() {
		return vertices;
	}
	
	public FocalMechanism getFocalMechanism() {
		return focalMechanism;
	}
	
	public double getSlipRate() {
		return slipRate;
	}

	public double getAseisFactor() {
		return aseisFactor;
	}

	/**
	 * This tells whether it's a perfect rectangle
	 * @return
	 */
	public boolean isPerfect() {
		return perfect;
	}
	
	public int getPerfectInt() {
		if(perfect) return 1;
		else return 0;
	}
	
	public String getSectionName() {
		return sectionName;
	}

	public int getSectionID() {
		return sectionID;
	}
	
	public int getFaultID() {
		return faultID;
	}
	
	public int getNumAlongStrike() {
		return numAlongStrike;
	}

	public int getNumDownDip() {
		return numDownDip;
	}

	
	public String toWardFormatLine() {
		// this is Steve's ordering
		Location newTop1 = vertices[0];
		Location newTop2 = vertices[3];
		Location newBot1 = vertices[1];;
		Location newBot2 = vertices[2];;
		String line = id + "\t"+
			numAlongStrike + "\t"+
			numDownDip + "\t"+
			faultID + "\t"+
			sectionID + "\t"+
			(float)slipRate + "\t"+
			"NA" + "\t"+  // elementStrength not available
			(float)focalMechanism.getStrike() + "\t"+
			(float)focalMechanism.getDip() + "\t"+
			(float)focalMechanism.getRake() + "\t"+
			(float)newTop1.getLatitude() + "\t"+
			(float)newTop1.getLongitude() + "\t"+
			(float)newTop1.getDepth()*-1000 + "\t"+
			(float)newBot1.getLatitude() + "\t"+
			(float)newBot1.getLongitude() + "\t"+
			(float)newBot1.getDepth()*-1000 + "\t"+
			(float)newBot2.getLatitude() + "\t"+
			(float)newBot2.getLongitude() + "\t"+
			(float)newBot2.getDepth()*-1000 + "\t"+
			(float)newTop2.getLatitude() + "\t"+
			(float)newTop2.getLongitude() + "\t"+
			(float)newTop2.getDepth()*-1000 + "\t"+
			sectionName;
		return line;
	}
	

	/**
	 *  This creates the element from a line according to the supplied format
	 * @param line
	 * @param formatType - format type: 0 for Ward input file lines

	 */
	public RectangularElement(String line, int formatType) {
		StringTokenizer tok = new StringTokenizer(line);

		// Ward format
		if(formatType == 0) {
			id = Integer.parseInt(tok.nextToken()); // unique number ID for each element
			numAlongStrike = Integer.parseInt(tok.nextToken()); // Number along strike
			numDownDip = Integer.parseInt(tok.nextToken()); // Number down dip
			faultID = Integer.parseInt(tok.nextToken()); // Fault Number
			sectionID = Integer.parseInt(tok.nextToken()); // Segment Number
			slipRate = Double.parseDouble(tok.nextToken()); // Slip Rate in m/y.
			double strength = Double.parseDouble(tok.nextToken()); // Element Strength in Bars (not used).
			double strike = Double.parseDouble(tok.nextToken()); // stike
			double dip = Double.parseDouble(tok.nextToken()); // dip
			double rake = Double.parseDouble(tok.nextToken()); // rake
			this.focalMechanism = new FocalMechanism(strike, dip, rake);

			vertices = new Vertex[4];
			// 0th vertex
			double lat = Double.parseDouble(tok.nextToken());
			double lon = Double.parseDouble(tok.nextToken());
			double depth = Double.parseDouble(tok.nextToken()) / -1000d;
			vertices[0] = new Vertex(lat, lon, depth);
			// 1st vertex
			lat = Double.parseDouble(tok.nextToken());
			lon = Double.parseDouble(tok.nextToken());
			depth = Double.parseDouble(tok.nextToken()) / -1000d;
			vertices[1] = new Vertex(lat, lon, depth);
			// 2nd vertex
			lat = Double.parseDouble(tok.nextToken());
			lon = Double.parseDouble(tok.nextToken());
			depth = Double.parseDouble(tok.nextToken()) / -1000d;
			vertices[2] = new Vertex(lat, lon, depth);
			// last vertex
			lat = Double.parseDouble(tok.nextToken());
			lon = Double.parseDouble(tok.nextToken());
			depth = Double.parseDouble(tok.nextToken()) / -1000d;
			vertices[3] = new Vertex(lat, lon, depth);

			String name = null;
			while (tok.hasMoreTokens()) {
				if (name == null)
					name = "";
				else
					name += " ";
				name += tok.nextToken();
			}

			sectionName = name;
		}
		else
			throw new RuntimeException("Unknown format specification");

	}

}
