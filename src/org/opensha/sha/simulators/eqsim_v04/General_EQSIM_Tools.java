package org.opensha.sha.simulators.eqsim_v04;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2007_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.PlaneUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;
import org.opensha.sha.earthquake.calc.recurInterval.LognormalDistCalc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.EvenlyGridCenteredSurface;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.simulators.UCERF2_DataForComparisonFetcher;

/**
 * This class reads and writes various files, as well as doing some analysis of simulator results.
 * 
 * Note that this class could extend some class representing the "Container" defined in EQSIM, but it's not clear that generality is necessary.
 * 
 * Things to keep in mind:
 * 
 * Indexing in the EQSIM files starts from 1, not zero.  Therefore, here we refer to their "indices" as IDs to avoid confusion
 * (so IDs here start from 1, but indices here start from zero).  Thus, the ID for the ith RectangularElement in rectElementsList 
 * equals i+1 (rectElementsList.get(i).getID() = i+1) because the input file has everything in an order of increasing IDs. The
 * same goes for other lists.
 * 
 * All units in EQSIM files are SI
 * 
 * Note that slip rates in EQSIM files are in units of m/s, whereas we convert these to m/yr internally here.
 * 
 * We assume the first vertex in each element here is the first on the upper edge 
 * (traceFlag=2 if the element is at the top); this is not checked for explicitly.
 * 
 * @author field
 *
 */
public class General_EQSIM_Tools {

	protected final static boolean D = false;  // for debugging
	
	private ArrayList<FaultSectionPrefData> allFaultSectionPrefData;
	ArrayList<RectangularElement> rectElementsList;
	ArrayList<Vertex> vertexList;
	ArrayList<ArrayList<RectangularElement>> rectElementsListForSections;
	ArrayList<ArrayList<Vertex>> vertexListForSections;
	ArrayList<String> namesOfSections;
	ArrayList<Integer> faultIDs_ForSections;
	ArrayList<Double> depthLoForSections;
	ArrayList<Double> depthHiForSections;
	ArrayList<EQSIM_Event> eventList;
	
	final static String GEOM_FILE_SIG = "EQSim_Input_Geometry_2";	// signature of the geometry file
	final static int GEOM_FILE_SPEC_LEVEL = 2;
	final static String EVENT_FILE_SIG = "EQSim_Output_Event_2";
	final static int EVENT_FILE_SPEC_LEVEL = 2;
	public final static double SECONDS_PER_YEAR = 365*24*60*60;
	
	ArrayList<String> infoStrings;
	String dirNameForSavingFiles;
	
	UCERF2_DataForComparisonFetcher ucerf2_dataFetcher = new UCERF2_DataForComparisonFetcher();


	/**
	 * This constructor makes the list of RectangularElements from a UCERF2 deformation model
	 * @param deformationModelID	- D2.1 = 82; D2.2 = 83; D2.3 = 84; D2.4 = 85; D2.5 = 86; D2.6 = 87
	 * @param aseisReducesArea		- whether or not to reduce area (otherwise reduces slip rate?)
	 * @param maxDiscretization		- the maximum element size
	 */
	public General_EQSIM_Tools(int deformationModelID, boolean aseisReducesArea, double maxDiscretization) {
		mkElementsFromUCERF2_DefMod(deformationModelID, aseisReducesArea, maxDiscretization);
	}
	
	
	/**
	 * This constructor loads the data from an EQSIM_v04 Geometry file
	 * @param filePathName		 - full path and file name
	 */
	public General_EQSIM_Tools(String filePathName) {
		ArrayList<String> lines=null;
		try {
			lines = FileUtils.loadJarFile(filePathName);
			System.out.println("Number of file lines: "+lines.size()+" (in "+filePathName+")");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadFromEQSIMv04_GeometryLines(lines);
	}
	
	
	/**
	 * This constructor loads the data from either an EQSIM_v04 Geometry file (formatType=0)
	 * or from Steve Ward's format (formatType=1).
	 * @param filePathName		 - full path and file name
	 * @param formatType		 - set as 0 for EQSIM_v04 Geometry file or 1 for Steve Ward's format
	 */
	public General_EQSIM_Tools(String filePathName, int formatType) {
		System.out.println(filePathName);
		ArrayList<String> lines=null;
		try {
			lines = FileUtils.loadJarFile(filePathName);
			System.out.println("Number of file lines: "+lines.size()+" (in "+filePathName+")");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(formatType==0)
			loadFromEQSIMv04_GeometryLines(lines);
		else if (formatType==1)
			loadFromSteveWardLines(lines);
		else
			throw new RuntimeException("format type not supported");
	}

	
	/**
	 * This constructor loads the data from an EQSIM_v04 Geometry file
	 * @param url		 - full URL path name
	 * @throws IOException 
	 */
	public General_EQSIM_Tools(File file) throws IOException {
		ArrayList<String> lines = FileUtils.loadFile(file.getAbsolutePath());
		System.out.println("Number of file lines: "+lines.size()+" (in "+file.getAbsolutePath()+")");
		loadFromEQSIMv04_GeometryLines(lines);
	}

	
	/**
	 * This constructor loads the data from an EQSIM_v04 Geometry file
	 * @param url		 - full URL path name
	 */
	public General_EQSIM_Tools(URL url) {
		ArrayList<String> lines=null;
		
		try {
			lines = FileUtils.loadFile(url);
			System.out.println("Number of file lines: "+lines.size()+" (in "+url+")");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		loadFromEQSIMv04_GeometryLines(lines);
	}
	
	
	public ArrayList<String> getSectionsNameList() {
		return namesOfSections;
	}
	

	public void read_EQSIMv04_EventsFile(URL url) {
		ArrayList<String> lines=null;
		try {
			lines = FileUtils.loadFile(url);
			System.out.println("Number of file lines: "+lines.size()+" (in "+url+")");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		read_EQSIMv04_EventsFile(lines);
	}
	
	public void read_EQSIMv04_EventsFile(File file) throws IOException {
		ArrayList<String> lines = FileUtils.loadFile(file.getAbsolutePath());
		read_EQSIMv04_EventsFile(lines);
	}
	
	
	public void read_EQSIMv04_EventsFile(String filePathName) {

		ArrayList<String> lines=null;
		try {
			lines = FileUtils.loadJarFile(filePathName);
			System.out.println("Number of file lines: "+lines.size()+" (in "+filePathName+")");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		read_EQSIMv04_EventsFile(lines);
	}
	
	
	private void read_EQSIMv04_EventsFile(ArrayList<String> lines) {
		
		ListIterator<String> linesIterator = lines.listIterator();
		
		// get & check first line (must be the signature line)
		String line = linesIterator.next();
		StringTokenizer tok = new StringTokenizer(line);
		int kindOfLine = Integer.parseInt(tok.nextToken());
		String fileSignature = tok.nextToken();
		int fileSpecLevel = Integer.parseInt(tok.nextToken());
//		if(kindOfLine != 101 || !fileSignature.equals(EVENT_FILE_SIG) || fileSpecLevel < EVENT_FILE_SPEC_LEVEL)
		if(kindOfLine != 101 || !fileSignature.equals(EVENT_FILE_SIG))
			throw new RuntimeException("wrong type of event input file; your first file line is:\n\n\t"+line+"\n");

		eventList = new ArrayList<EQSIM_Event>();
		EQSIM_Event currEvent = null;
		EventRecord evRec = new EventRecord(); // this one never used, but created to compile
		int numEventRecs=0;
		while (linesIterator.hasNext()) {
			line = linesIterator.next();
			tok = new StringTokenizer(line);
			kindOfLine = Integer.parseInt(tok.nextToken());
			if(kindOfLine ==200) {	// event record
				try {
					evRec = new EventRecord(line);
				} catch (Exception e) {
					System.err.println("Unable to parse line: "+line.trim()+" (error: "+e.getMessage()+")");
					continue;
				}
				numEventRecs+=1;
				
				// check whether this is the first event in the list
				if(eventList.size() == 0) {
					EQSIM_Event event = new EQSIM_Event(evRec);
					eventList.add(event);
					currEvent = event;
				}
				else { // check whether this is part of currEvent (same ID)
					if(currEvent.isSameEvent(evRec)) {
						currEvent.add(evRec);
					}
					else { // it's a new event
						EQSIM_Event event = new EQSIM_Event(evRec);
						eventList.add(event);
						currEvent = event;
					}
				}
			}
			else if(kindOfLine ==201) {	// Slip map record
				evRec.addSlipAndElementData(line); // add to the last event record created
			}
			else if(kindOfLine ==202)
				evRec.addType202_Line(line);
		}
		
		System.out.println("Num Events = "+this.eventList.size()+"\tNum Event Records = "+numEventRecs);
	}
	
	
	
	
	/**
	 * This creates the data from lines from an EQSIM Geometry file
	 * @param lines
	 * @return
	 */
	private void loadFromEQSIMv04_GeometryLines(ArrayList<String> lines) {
		
		// note that the following lists have indices that start from 0
		rectElementsList = new ArrayList<RectangularElement>();
		vertexList = new ArrayList<Vertex>();
		rectElementsListForSections = new ArrayList<ArrayList<RectangularElement>> ();
		vertexListForSections = new ArrayList<ArrayList<Vertex>>();
		namesOfSections = new ArrayList<String>();
		faultIDs_ForSections = new ArrayList<Integer>();
		depthLoForSections = new ArrayList<Double>();
		depthHiForSections = new ArrayList<Double>();

		
		ListIterator<String> linesIterator = lines.listIterator();
		
		// get & check first line (must be the signature line)
		String line = linesIterator.next();
		StringTokenizer tok = new StringTokenizer(line);
		int kindOfLine = Integer.parseInt(tok.nextToken());
		String fileSignature = tok.nextToken();
		int fileSpecLevel = Integer.parseInt(tok.nextToken());
		if(kindOfLine != 101 || !fileSignature.equals(GEOM_FILE_SIG) || fileSpecLevel < GEOM_FILE_SPEC_LEVEL)
			throw new RuntimeException("wrong type of input file");
		
		int n_section=-1, n_vertex=-1,n_triangle=-1, n_rectangle=-1;

		while (linesIterator.hasNext()) {
			
			line = linesIterator.next();
			tok = new StringTokenizer(line);
			kindOfLine = Integer.parseInt(tok.nextToken());
			
			// read "Fault System Summary Record" (values kept are use as a check later)
			if(kindOfLine == 200) {
				n_section=Integer.parseInt(tok.nextToken());
				n_vertex=Integer.parseInt(tok.nextToken());
				n_triangle=Integer.parseInt(tok.nextToken());
				n_rectangle=Integer.parseInt(tok.nextToken());
				// the rest of the line contains:
				// lat_lo lat_hi lon_lo lon_hi depth_lo depth_hi comment_text
			}
			
			// read "Fault Section Information Record"
			if(kindOfLine == 201) {
				int sid = Integer.parseInt(tok.nextToken());  // section ID
				String name = tok.nextToken();
				int n_sect_vertex=Integer.parseInt(tok.nextToken());
				int n_sect_triangle=Integer.parseInt(tok.nextToken());
				int n_sect_rectangle=Integer.parseInt(tok.nextToken());
				tok.nextToken(); // lat_lo
				tok.nextToken(); // lat_hi
				tok.nextToken(); // lon_lo
				tok.nextToken(); // lon_hi
				double depth_lo = Double.parseDouble(tok.nextToken()); // depth_lo
				double depth_hi = Double.parseDouble(tok.nextToken()); // depth_hi
				tok.nextToken(); // das_lo
				tok.nextToken(); // das_hi
				int fault_id = Integer.parseInt(tok.nextToken());
				// the rest of the line contains: comment_text
				
				// check for triangular elements
				if(n_sect_triangle>0) throw new RuntimeException("Don't yet support trinagles");
				
				namesOfSections.add(name);
				faultIDs_ForSections.add(fault_id);
				depthLoForSections.add(depth_lo);
				depthHiForSections.add(depth_hi);

				// read the vertices for this section
				ArrayList<Vertex> verticesForThisSect = new ArrayList<Vertex>();
				for(int v=0; v<n_sect_vertex; v++) {
					line = linesIterator.next();
					tok = new StringTokenizer(line);
					kindOfLine = Integer.parseInt(tok.nextToken());
					if(kindOfLine != 202) throw new RuntimeException("Problem with file (line should start with 202)");
					int id = Integer.parseInt(tok.nextToken());
					double lat = Double.parseDouble(tok.nextToken());
					double lon = Double.parseDouble(tok.nextToken());
					double depth = -Double.parseDouble(tok.nextToken())/1000; 	// convert to km & change sign
					double das = Double.parseDouble(tok.nextToken())/1000;		// convert to km
					int trace_flag = Integer.parseInt(tok.nextToken());
					// the rest of the line contains:
					// comment_text
					
					Vertex vertex = new Vertex(lat,lon,depth, id, das, trace_flag); 
					verticesForThisSect.add(vertex);
					vertexList.add(vertex);
				}
				vertexListForSections.add(verticesForThisSect);
				
				// now read the elements
				ArrayList<RectangularElement> rectElemForThisSect = new ArrayList<RectangularElement>();
				for(int r=0; r<n_sect_rectangle; r++) {
					line = linesIterator.next();
					tok = new StringTokenizer(line);
					kindOfLine = Integer.parseInt(tok.nextToken());
					if(kindOfLine != 204) throw new RuntimeException("Problem with file (line should start with 204)");
					int id = Integer.parseInt(tok.nextToken());
					int vertex_1_ID = Integer.parseInt(tok.nextToken());
					int vertex_2_ID = Integer.parseInt(tok.nextToken());
					int vertex_3_ID = Integer.parseInt(tok.nextToken());
					int vertex_4_ID = Integer.parseInt(tok.nextToken());
				    double rake = Double.parseDouble(tok.nextToken());
				    double slip_rate = Double.parseDouble(tok.nextToken())*SECONDS_PER_YEAR; // convert to meters per year
				    double aseis_factor = Double.parseDouble(tok.nextToken());
				    double strike = Double.parseDouble(tok.nextToken());
				    double dip = Double.parseDouble(tok.nextToken());
				    int perfect_flag = Integer.parseInt(tok.nextToken());
					// the rest of the line contains: comment_text
				    boolean perfectBoolean = false;
				    if(perfect_flag == 1) perfectBoolean = true;
				    Vertex[] vertices = new Vertex[4];
				    
				    vertices[0] = vertexList.get(vertex_1_ID-1);  // vertex index is one minus vertex ID
				    vertices[1] = vertexList.get(vertex_2_ID-1);
				    vertices[2] = vertexList.get(vertex_3_ID-1);
				    vertices[3] = vertexList.get(vertex_4_ID-1);
				    int numAlongStrike = -1;// unknown
				    int numDownDip = -1;	// unknown
				    FocalMechanism focalMechanism = new FocalMechanism(strike,dip,rake);
				    RectangularElement rectElem = new RectangularElement(id, vertices, name, fault_id, sid, numAlongStrike, 
				    													numDownDip, slip_rate, aseis_factor, focalMechanism, perfectBoolean);
				    rectElemForThisSect.add(rectElem);
				    rectElementsList.add(rectElem);
				    
				}
				rectElementsListForSections.add(rectElemForThisSect);
			}
		}
		
		// check the numbers of things:  n_sction, n_vertex, n_triangle, n_rectangle
		if(n_section != namesOfSections.size())
			throw new RuntimeException("something wrong with number of sections");
		if(n_vertex != vertexList.size())
			throw new RuntimeException("something wrong with number of vertices");
		if(n_rectangle != rectElementsList.size())
			throw new RuntimeException("something wrong with number of eleents");
		
		System.out.println("namesOfSections.size()="+namesOfSections.size()+"\tvertexList.size()="+vertexList.size()+"\trectElementsList.size()="+rectElementsList.size());
		
		// check that indices are in order, and that index is one minus the ID:
		for(int i=0;i<vertexList.size();i++) {
			if(i != vertexList.get(i).getID()-1) throw new RuntimeException("vertexList index problem at "+i);
		}
		for(int i=0;i<rectElementsList.size();i++) {
			if(i != rectElementsList.get(i).getID()-1) throw new RuntimeException("rectElementsList index problem at "+i);
		}
		
	}
	
	/**
	 * This tells whether any part of the earthquake ruptured completely down dip
	 * @param event
	 * @return
	 */
	private boolean doesEventRuptureFullDepth(EQSIM_Event event) {
		boolean result = false;
		for(EventRecord evRec:event) {
			int sectIndex = evRec.getSectionID()-1;
			double rupThickness = evRec.getDepthHi()-evRec.getDepthLo();
			double sectThickness = depthHiForSections.get(sectIndex)-depthLoForSections.get(sectIndex);
			if(rupThickness > 0.95*sectThickness) result = true;
		}
		return result;
	}

	
	
	/**
	 * This returns the list of RectangularElement objects
	 * @return
	 */
	public ArrayList<RectangularElement> getElementsList() { return rectElementsList; }

	
	/**
	 * This makes the elements from a UCERF2 deformation model
	 * @param deformationModelID	- D2.1 = 82; D2.2 = 83; D2.3 = 84; D2.4 = 85; D2.5 = 86; D2.6 = 87
	 * @param aseisReducesArea		- whether or not to reduce area (otherwise reduces slip rate?)
	 * @param maxDiscretization		- the maximum element size
	 */
	public void mkElementsFromUCERF2_DefMod(int deformationModelID, boolean aseisReducesArea, 
			double maxDiscretization) {
		
		rectElementsList = new ArrayList<RectangularElement>();
		vertexList = new ArrayList<Vertex>();
		rectElementsListForSections = new ArrayList<ArrayList<RectangularElement>> ();
		vertexListForSections = new ArrayList<ArrayList<Vertex>>();
		namesOfSections = new ArrayList<String>();
		faultIDs_ForSections = null;	// no info for this

		
		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelID);

		//Alphabetize:
		Collections.sort(allFaultSectionPrefData, new NamedComparator());

		/*		  
		  // write sections IDs and names
		  for(int i=0; i< this.allFaultSectionPrefData.size();i++)
				System.out.println(allFaultSectionPrefData.get(i).getSectionId()+"\t"+allFaultSectionPrefData.get(i).getName());
		 */

		// remove those with no slip rate
		if (D)System.out.println("Removing the following due to NaN slip rate:");
		for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
			if(Double.isNaN(allFaultSectionPrefData.get(i).getOrigAveSlipRate())) {
				if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
				allFaultSectionPrefData.remove(i);
			}	 
				
		// Loop over sections and create the simulator elements
		int elementID =0;
		int numberAlongStrike = 0;
		int numberDownDip;
		int faultNumber = -1; // unknown for now
		int sectionNumber =0;
		double elementSlipRate=0;
		double elementAseis;
		double elementStrike=0, elementDip=0, elementRake=0;
		String sectionName;
//		System.out.println("allFaultSectionPrefData.size() = "+allFaultSectionPrefData.size());
		for(int i=0;i<allFaultSectionPrefData.size();i++) {
			ArrayList<RectangularElement> sectionElementsList = new ArrayList<RectangularElement>();
			ArrayList<Vertex> sectionVertexList = new ArrayList<Vertex>();
			sectionNumber +=1; // starts from 1, not zero
			FaultSectionPrefData faultSectionPrefData = allFaultSectionPrefData.get(i);
			StirlingGriddedSurface surface = new StirlingGriddedSurface(faultSectionPrefData.getSimpleFaultData(aseisReducesArea), maxDiscretization, maxDiscretization);
			EvenlyGridCenteredSurface gridCenteredSurf = new EvenlyGridCenteredSurface(surface);
			double elementLength = gridCenteredSurf.getGridSpacingAlongStrike();
			double elementDDW = gridCenteredSurf.getGridSpacingDownDip(); // down dip width
			elementRake = faultSectionPrefData.getAveRake();
			elementSlipRate = faultSectionPrefData.getOrigAveSlipRate()/1000;
			elementAseis = faultSectionPrefData.getAseismicSlipFactor();
			sectionName = faultSectionPrefData.getName();
			for(int col=0; col<gridCenteredSurf.getNumCols();col++) {
				numberAlongStrike += 1;
				for(int row=0; row<gridCenteredSurf.getNumRows();row++) {
					elementID +=1; // starts from 1, not zero
					numberDownDip = row+1;
					Location centerLoc = gridCenteredSurf.get(row, col);
					Location top1 = surface.get(row, col);
					Location top2 = surface.get(row, col+1);
					Location bot1 = surface.get(row+1, col);
					double[] strikeAndDip = PlaneUtils.getStrikeAndDip(top1, top2, bot1);
					elementStrike = strikeAndDip[0];
					elementDip = strikeAndDip[1];	
					
					double hDistAlong = elementLength/2;
					double dipRad = Math.PI*elementDip/180;
					double vDist = (elementDDW/2)*Math.sin(dipRad);
					double hDist = (elementDDW/2)*Math.cos(dipRad);
					
//					System.out.println(elementID+"\telementDDW="+elementDDW+"\telementDip="+elementDip+"\tdipRad="+dipRad+"\tvDist="+vDist+"\thDist="+hDist);
					
					LocationVector vect = new LocationVector(elementStrike+180, hDistAlong, 0);
					Location newMid1 = LocationUtils.location(centerLoc, vect);  // half way down the first edge
					vect.set(elementStrike-90, hDist, -vDist); // up-dip direction
					Location newTop1 = LocationUtils.location(newMid1, vect);
					vect.set(elementStrike+90, hDist, vDist); // down-dip direction
					Location newBot1 = LocationUtils.location(newMid1, vect);
					 
					vect.set(elementStrike, hDistAlong, 0);
					Location newMid2 = LocationUtils.location(centerLoc, vect); // half way down the other edge
					vect.set(elementStrike-90, hDist, -vDist); // up-dip direction
					Location newTop2 = LocationUtils.location(newMid2, vect);
					vect.set(elementStrike+90, hDist, vDist); // down-dip direction
					Location newBot2 = LocationUtils.location(newMid2, vect);
					
					 // @param traceFlag - tells whether is on the fault trace  (0 means no; 1 means yes, but not
					 // 		              the first or last point; 2 means yes & it's the first; and 3 means yes 
					 //                    & it's the last point)
					
					
					// set DAS
					double das1 = col*elementLength;	// this is in km
					double das2 = das1+elementLength;	// this is in km
					// set traceFlag - tells whether is on the fault trace  (0 means no; 1 means yes, but not the 
					// first or last point; 2 means yes & it's the first; and 3 means yes & it's the last point)
					int traceFlagBot = 0;
					int traceFlagTop1 = 0;
					int traceFlagTop2 = 0;
					if(row ==0) {
						traceFlagTop1 = 1;
						traceFlagTop2 = 1;
					}
					if(row==0 && col==0) traceFlagTop1 = 2;
					if(row==0 && col==gridCenteredSurf.getNumCols()-1) traceFlagTop2 = 3;

					Vertex[] elementVertices = new Vertex[4];
					elementVertices[0] = new Vertex(newTop1,vertexList.size()+1, das1, traceFlagTop1);  
					elementVertices[1] = new Vertex(newBot1,vertexList.size()+2, das1, traceFlagBot);
					elementVertices[2] = new Vertex(newBot2,vertexList.size()+3, das2, traceFlagBot);
					elementVertices[3] = new Vertex(newTop2,vertexList.size()+4, das2, traceFlagTop2);
					
					FocalMechanism focalMech = new FocalMechanism(elementStrike, elementDip, elementRake);
										
					RectangularElement simSurface =
						new RectangularElement(elementID, elementVertices, sectionName,
								faultNumber, sectionNumber, numberAlongStrike, numberDownDip,
								elementSlipRate, elementAseis, focalMech, true);
					
					rectElementsList.add(simSurface);
					vertexList.add(elementVertices[0]);
					vertexList.add(elementVertices[1]);
					vertexList.add(elementVertices[2]);
					vertexList.add(elementVertices[3]);
					
					sectionElementsList.add(simSurface);
					sectionVertexList.add(elementVertices[0]);
					sectionVertexList.add(elementVertices[1]);
					sectionVertexList.add(elementVertices[2]);
					sectionVertexList.add(elementVertices[3]);

					
//					String line = elementID + "\t"+
//						numberAlongStrike + "\t"+
//						numberDownDip + "\t"+
//						faultNumber + "\t"+
//						sectionNumber + "\t"+
//						(float)elementSlipRate + "\t"+
//						(float)elementStrength + "\t"+
//						(float)elementStrike + "\t"+
//						(float)elementDip + "\t"+
//						(float)elementRake + "\t"+
//						(float)newTop1.getLatitude() + "\t"+
//						(float)newTop1.getLongitude() + "\t"+
//						(float)newTop1.getDepth()*-1000 + "\t"+
//						(float)newBot1.getLatitude() + "\t"+
//						(float)newBot1.getLongitude() + "\t"+
//						(float)newBot1.getDepth()*-1000 + "\t"+
//						(float)newBot2.getLatitude() + "\t"+
//						(float)newBot2.getLongitude() + "\t"+
//						(float)newBot2.getDepth()*-1000 + "\t"+
//						(float)newTop2.getLatitude() + "\t"+
//						(float)newTop2.getLongitude() + "\t"+
//						(float)newTop2.getDepth()*-1000 + "\t"+
//						sectionName;
//
//					System.out.println(line);
				}
			}
			rectElementsListForSections.add(sectionElementsList);
			vertexListForSections.add(sectionVertexList);
			namesOfSections.add(faultSectionPrefData.getName());
		}
		System.out.println("rectElementsList.size()="+rectElementsList.size());
		System.out.println("vertexList.size()="+vertexList.size());
		
		/*
		for(int i=0;i<allFaultSectionPrefData.size();i++) {
			ArrayList<RectangularElement> elList = rectElementsListForSections.get(i);
			ArrayList<Vertex> verList = vertexListForSections.get(i);;
			System.out.println(allFaultSectionPrefData.get(i).getName());
			System.out.println("\tEl Indices:  "+elList.get(0).getID()+"\t"+elList.get(elList.size()-1).getID());
//			System.out.println("\tVer Indices:  "+verList.get(0).getID()+"\t"+verList.get(verList.size()-1).getID());
		}
		*/
	}
	
	
	public void writeToWardFile(String fileName) throws IOException {
		FileWriter efw = new FileWriter(fileName);
		for (RectangularElement rectElem : rectElementsList) {
			efw.write(rectElem.toWardFormatLine() + "\n");
		}
		efw.close();
	}


	/**
	 * This loads from Steve Wards file format (at least for the format he sent on Sept 2, 2010).  This
	 * implementation does not put DAS for traceFlag in the vertices, and there are assumptions about the
	 * ordering of things in his file.  Note also that his NAS does not start over for each section, but
	 * rather starts over for each fault.
	 * @param lines
	 */
	private void loadFromSteveWardLines(ArrayList<String> lines) {


		// now need to fill these lists
		rectElementsList = new ArrayList<RectangularElement>();
		vertexList = new ArrayList<Vertex>();
		rectElementsListForSections = new ArrayList<ArrayList<RectangularElement>> ();
		vertexListForSections = new ArrayList<ArrayList<Vertex>>();
		namesOfSections = new ArrayList<String>();
		faultIDs_ForSections = new ArrayList<Integer>();

		int lastSectionID = -1;
		ArrayList<RectangularElement> currentRectElForSection = null;
		ArrayList<Vertex> currVertexListForSection = null;

		int numVertices= 0; // to set vertexIDs


		for (String line : lines) {
			if (line == null || line.length() == 0)
				continue;

			StringTokenizer tok = new StringTokenizer(line);

			int id = Integer.parseInt(tok.nextToken()); // unique number ID for each element
			int numAlongStrike = Integer.parseInt(tok.nextToken()); // Number along strike
			int numDownDip = Integer.parseInt(tok.nextToken()); // Number down dip
			int faultID = Integer.parseInt(tok.nextToken()); // Fault Number
			int sectionID = Integer.parseInt(tok.nextToken()); // Segment Number
			double slipRate = Double.parseDouble(tok.nextToken()); // Slip Rate in m/y.
			double strength = Double.parseDouble(tok.nextToken()); // Element Strength in Bars (not used).
			double strike = Double.parseDouble(tok.nextToken()); // stike
			double dip = Double.parseDouble(tok.nextToken()); // dip
			double rake = Double.parseDouble(tok.nextToken()); // rake
			FocalMechanism focalMechanism = new FocalMechanism(strike, dip, rake);

			Vertex[] vertices = new Vertex[4];
			// 0th vertex
			double lat = Double.parseDouble(tok.nextToken());
			double lon = Double.parseDouble(tok.nextToken());
			double depth = Double.parseDouble(tok.nextToken()) / -1000d;
			numVertices+=1;
			vertices[0] = new Vertex(lat, lon, depth, numVertices);
			// 1st vertex
			lat = Double.parseDouble(tok.nextToken());
			lon = Double.parseDouble(tok.nextToken());
			depth = Double.parseDouble(tok.nextToken()) / -1000d;
			numVertices+=1;
			vertices[1] = new Vertex(lat, lon, depth, numVertices);
			// 2nd vertex
			lat = Double.parseDouble(tok.nextToken());
			lon = Double.parseDouble(tok.nextToken());
			depth = Double.parseDouble(tok.nextToken()) / -1000d;
			numVertices+=1;
			vertices[2] = new Vertex(lat, lon, depth, numVertices);
			// last vertex
			lat = Double.parseDouble(tok.nextToken());
			lon = Double.parseDouble(tok.nextToken());
			depth = Double.parseDouble(tok.nextToken()) / -1000d;
			numVertices+=1;
			vertices[3] = new Vertex(lat, lon, depth, numVertices);

			String name = null;
			while (tok.hasMoreTokens()) {
				if (name == null)
					name = "";
				else
					name += " ";
				name += tok.nextToken();
			}
			String sectionName = name;

			RectangularElement rectElem = new RectangularElement(id, vertices, sectionName,faultID, sectionID, 
					numAlongStrike, numDownDip, slipRate, Double.NaN, focalMechanism, true);

			rectElementsList.add(rectElem);

			// check if this is a new section
			if(sectionID != lastSectionID) {
				// encountered a new section
				currentRectElForSection = new ArrayList<RectangularElement>();
				currVertexListForSection = new ArrayList<Vertex>();
				rectElementsListForSections.add(currentRectElForSection);
				vertexListForSections.add(currVertexListForSection);
				namesOfSections.add(sectionName);
				faultIDs_ForSections.add(faultID);
			}
			currentRectElForSection.add(rectElem);
			for(int i=0; i<4;i++) {
				vertexList.add(vertices[i]);
				currVertexListForSection.add(vertices[i]);
			}
		}

		// check that indices are in order, and that index is one minus the ID:
		for(int i=0;i<vertexList.size();i++) {
			int idMinus1 = vertexList.get(i).getID()-1;
			if(i != idMinus1) throw new RuntimeException("vertexList index problem at index "+i+" (ID-1="+idMinus1+")");
		}
		for(int i=0;i<rectElementsList.size();i++) {
			if(i != rectElementsList.get(i).getID()-1) throw new RuntimeException("rectElementsList index problem at "+i);
		}

		System.out.println("namesOfSections.size()="+namesOfSections.size()+"\tvertexList.size()="+vertexList.size()+"\trectElementsList.size()="+rectElementsList.size());


	}

	
	
	/**
	 * The creates a EQSIM V04 Geometry file for the given instance.
	 * @param fileName
	 * @param infoLines - each line here should NOT end with a new line char "\n" (this will be added)
	 * @param titleLine
	 * @param author
	 * @param date
	 * @throws IOException
	 */
	public void writeTo_EQSIM_V04_GeometryFile(String fileName, ArrayList<String> infoLines, String titleLine, 
			String author, String date) throws IOException {
			FileWriter efw = new FileWriter(fileName);
			
			// write the standard file signature info
			efw.write("101 "+GEOM_FILE_SIG +" "+GEOM_FILE_SPEC_LEVEL+ "\n");
			
			// add the file-specific meta data records/lines
			if(titleLine!=null)
				efw.write("111 "+titleLine+ "\n");
			if(author!=null)
				efw.write("112 "+author+ "\n");
			if(date!=null)
				efw.write("113 "+date+ "\n");
			if(infoLines!=null)
				for(int i=0; i<infoLines.size();i++)
					efw.write("110 "+infoLines.get(i)+ "\n");
			
			// add the standard descriptor records/lines for the Geometry file (read from another file)
			String fullPath = "org/opensha/sha/simulators/eqsim_v04/ALLCAL_Model_v04/ALLCAL_Ward_Geometry.dat";
			ArrayList<String> lines=null;
			try {
				lines = FileUtils.loadJarFile(fullPath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for(int l=0;l<lines.size();l++) {
				String line = lines.get(l);
				StringTokenizer tok = new StringTokenizer(line);
				int kindOfLine = Integer.parseInt(tok.nextToken());
				if(kindOfLine==120 || kindOfLine==121 || kindOfLine==103)
					efw.write(line+"\n");
			}
			
			// now add the data records/lines 
			
			// Fault System Summary Record:
			// 200 n_section n_vertex n_triangle n_rectangle lat_lo lat_hi lon_lo lon_hi depth_lo depth_hi comment_text
			efw.write("200 "+namesOfSections.size()+" "+vertexList.size()+" 0 "+rectElementsList.size()+" "+
							getMinMaxFileString(vertexList, false)+"\n");

			// loop over sections
			for(int i=0;i<namesOfSections.size();i++) {
				ArrayList<Vertex> vertListForSect = vertexListForSections.get(i);
				ArrayList<RectangularElement> rectElemForSect = rectElementsListForSections.get(i);
				String fault_id;
				if(faultIDs_ForSections == null)
					fault_id = "NA";
				else
					fault_id = faultIDs_ForSections.get(i).toString();
				// Fault Section Information Record:
				// 201 sid name n_vertex n_triangle n_rectangle lat_lo lat_hi lon_lo lon_hi depth_lo depth_hi das_lo das_hi fault_id comment_text
				efw.write("201 "+(i+1)+" "+namesOfSections.get(i)+" "+vertListForSect.size()+" 0 "+
						rectElemForSect.size()+" "+getMinMaxFileString(vertListForSect, true)+" "+fault_id+"\n");
				for(int v=0; v<vertListForSect.size(); v++) {
					Vertex vert = vertListForSect.get(v);
					// Vertex Record: 202 ID lat lon depth das trace_flag comment_text
					efw.write("202 "+vert.getID()+" "+(float)vert.getLatitude()+" "+(float)vert.getLongitude()+" "+
							(float)(vert.getDepth()*-1000)+" "+(float)vert.getDAS()*1000+" "+vert.getTraceFlag()+"\n");
				}
				for(int e=0; e<rectElemForSect.size(); e++) {
					RectangularElement elem = rectElemForSect.get(e);
					Vertex[] vert = elem.getVertices();
					FocalMechanism focalMech = elem.getFocalMechanism();
					// Rectangle Record:  204 ID vertex_1 vertex_2 vertex_3 vertex_4 rake slip_rate aseis_factor strike dip perfect_flag comment_text
					efw.write("204 "+elem.getID()+" "+vert[0].getID()+" "+vert[1].getID()+" "+vert[2].getID()+" "+
							vert[3].getID()+" "+(float)focalMech.getRake()+" "+(float)(elem.getSlipRate()/SECONDS_PER_YEAR)+" "+
							(float)elem.getAseisFactor()+" "+(float)focalMech.getStrike()+" "+(float)focalMech.getDip()
							+" "+elem.getPerfectInt()+"\n");
				}
			}
			
			// add the last line
			efw.write("999 End\n");

			efw.close();
	}
	
	
	/**
	 * This produces the string of min and max lat, lon, depth, and (optionally) DAS from the
	 * given list of vertices.  There are no spaces before or after the first and last values,
	 * respectively.  Depth and DAS values are converted to meters (from km).
	 * @param vertexList
	 * @param includeDAS
	 * @return
	 */
	private String getMinMaxFileString(ArrayList<Vertex> vertexList, boolean includeDAS) {
		double minLat=Double.MAX_VALUE, maxLat=-Double.MAX_VALUE;
		double minLon=Double.MAX_VALUE, maxLon=-Double.MAX_VALUE;
		double minDep=Double.MAX_VALUE, maxDep=-Double.MAX_VALUE;
		double minDAS=Double.MAX_VALUE, maxDAS=-Double.MAX_VALUE;
		for(Vertex vertex: vertexList) {
			if(vertex.getLatitude()<minLat) minLat = vertex.getLatitude();
			if(vertex.getLongitude()<minLon) minLon = vertex.getLongitude();
			if(vertex.getDepth()<minDep) minDep = vertex.getDepth();
			if(vertex.getDAS()<minDAS) minDAS = vertex.getDAS();
			if(vertex.getLatitude()>maxLat) maxLat = vertex.getLatitude();
			if(vertex.getLongitude()>maxLon) maxLon = vertex.getLongitude();
//			if(!includeDAS) System.out.println(maxLon);
			if(vertex.getDepth()>maxDep) maxDep = vertex.getDepth();
			if(vertex.getDAS()>maxDAS) maxDAS = vertex.getDAS();
		}
		String string = (float)minLat+" "+(float)maxLat+" "+(float)minLon+" "+(float)maxLon+" "+(float)maxDep*-1000+" "+(float)minDep*-1000;
		if(includeDAS) string += " "+(float)minDAS*1000+" "+(float)maxDAS*1000;
		return string;
	}
	
	
	/**
	 * This computes the total magnitude frequency distribution (with an option to plot the results)
	 * @return
	 */
	public ArbIncrementalMagFreqDist computeTotalMagFreqDist(double minMag, double maxMag, int numMag, 
			boolean makePlot, boolean savePlot) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		
		double simDurr = getSimulationDurationInYears();
		for(EQSIM_Event event : eventList) {
			mfd.addResampledMagRate(event.getMagnitude(), 1.0/simDurr, true);
		}
		mfd.setName("Total Simulator Incremental Mag Freq Dist");
		mfd.setInfo("  ");
				
		if(makePlot){
			ArrayList<DiscretizedFunc> mfdList = new ArrayList<DiscretizedFunc>();
			mfdList.add(mfd);
			mfdList.add(mfd.getCumRateDistWithOffset());
			mfdList.get(1).setName("Total Simulator Cumulative Mag Freq Dist");
			mfdList.get(1).setInfo(" ");
			
			// get observed MFDs from UCERF2	
			mfdList.add(UCERF2_DataForComparisonFetcher.getHalf_UCERF2_ObsIncrMFDs(true));
			mfdList.addAll(UCERF2_DataForComparisonFetcher.getHalf_UCERF2_ObsCumMFDs(true));
			ArrayList<PlotCurveCharacterstics> curveChar = new ArrayList<PlotCurveCharacterstics>();
			Color pink = new Color(255, 127, 127);
			curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));
			curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.RED));
			curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GRAY));
			curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, pink));
			curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, pink));
			curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, pink));

			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfdList, "Total Mag Freq Dist"); 
			graph.setX_AxisLabel("Magnitude");
			graph.setY_AxisLabel("Rate (per yr)");
			graph.setX_AxisRange(4.5, 8.5);
			double yMin = Math.pow(10,Math.floor(Math.log10(1/getSimulationDurationInYears())));
			double yMax = graph.getY_AxisMax();
			if(yMin<yMax) {
				graph.setY_AxisRange(yMin,yMax);
				graph.setYLog(true);
			}
			graph.setPlottingFeatures(curveChar);

			if(savePlot)
				try {
					graph.saveAsPDF(dirNameForSavingFiles+"/TotalMagFreqDist.pdf");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


		}

		return mfd;
	}
	
		
// 

	/**
	 * This returns a list of incremental MFDs reflecting the rates of nucleation (as a function of mag) 
	 * on each fault section.  It also optionally makes plots.
	 */
	/**
	 * @param minMag
	 * @param maxMag
	 * @param numMag
	 * @param makeOnePlotWithAll - plot all incremental dists in one graph
	 * @param makeSeparatePlots - make separate plots of incremental and cumulative distributions
	 * @return
	 */
	public ArrayList<ArbIncrementalMagFreqDist> computeMagFreqDistByFaultSection(double minMag, double maxMag, int numMag, 
			boolean makeOnePlotWithAll, boolean makeSeparatePlots, boolean savePlots) {
		
		ArrayList<ArbIncrementalMagFreqDist> mfdList = new ArrayList<ArbIncrementalMagFreqDist>();
		for(int s=0; s<namesOfSections.size(); s++) {
			ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
			mfd.setName(namesOfSections.get(s)+" Incremental MFD");
			mfd.setInfo(" ");
			mfdList.add(mfd);
		}
		
		double simDurr = getSimulationDurationInYears();
		for(EQSIM_Event event : eventList) {
			int sectionIndex = event.get(0).getSectionID()-1;	// nucleates on first (0th) event record, and index is one minus ID 
			mfdList.get(sectionIndex).addResampledMagRate(event.getMagnitude(), 1.0/simDurr, true);
		}
		
		double yMin = Math.pow(10,Math.floor(Math.log10(1/getSimulationDurationInYears())));
		if(makeOnePlotWithAll){
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfdList, "Mag Freq Dists (Incremental)");   
			graph.setX_AxisLabel("Magnitude");
			graph.setY_AxisLabel("Rate (per yr)");
			graph.setX_AxisRange(4.5, 8.5);
			double yMax = graph.getY_AxisMax();
			if(yMin<yMax) {
				graph.setY_AxisRange(yMin,yMax);
				graph.setYLog(true);
			}
			if(savePlots)
				try {
					graph.saveAsPDF(dirNameForSavingFiles+"/MagFreqDistForAllSections.pdf");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		
		if(makeSeparatePlots) {
			int sectNum=-1;
			for(ArbIncrementalMagFreqDist mfd :mfdList) {
				sectNum +=1;
				ArrayList<EvenlyDiscretizedFunc> mfdList2 = new ArrayList<EvenlyDiscretizedFunc>();
				mfdList2.add(mfd);
				mfdList2.add(mfd.getCumRateDistWithOffset());
				mfdList2.get(1).setName(namesOfSections.get(sectNum)+" Cumulative MFD");
				mfdList2.get(1).setInfo(" ");
				GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfdList2, namesOfSections.get(sectNum)+" MFD"); 
				graph.setX_AxisLabel("Magnitude");
				graph.setY_AxisLabel("Rate (per yr)");
				graph.setX_AxisRange(4.5, 8.5);
				double yMax = graph.getY_AxisMax();
				if(yMin<yMax) {
					graph.setY_AxisRange(yMin,yMax);
					graph.setYLog(true);
				}
				if(savePlots)
					try {
						graph.saveAsPDF(dirNameForSavingFiles+"/MagFreqDistFor"+namesOfSections.get(sectNum)+".pdf");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
		}
		
		return mfdList;
	}

	
	/**
	 * This tells whether all events have data on the slip on each element
	 * @return
	 */
	public int getNumEventsWithElementSlipData() {
		int numTrue =0;
		for (EQSIM_Event event : eventList) {
			if(event.hasElementSlipsAndIDs()) numTrue +=1;
		}
		return numTrue;
	}
	
	
	public ArrayList<EQSIM_Event> getEventsList() {
		return eventList;
	}
	
	
	/**
	 * This replaces all the event times with a random value sampled between
	 * the first and last original event time using a uniform distribution.
	 */
	public void randomizeEventTimes() {
		System.out.println("Event Times have been randomized");
		double firstEventTime=eventList.get(0).getTime();
		double simDurInSec = eventList.get(eventList.size()-1).getTime() - firstEventTime;
		for(EQSIM_Event event:eventList)
			event.setTime(firstEventTime+Math.random()*simDurInSec);
		Collections.sort(eventList);
		
	}
	
	
	/**
	 * This plots yearly event rates
	 */
	public void plotYearlyEventRates() {
		
		double startTime=eventList.get(0).getTime();
		int numYears = (int)getSimulationDurationInYears();
		EvenlyDiscretizedFunc evPerYear = new EvenlyDiscretizedFunc(0.0, numYears+1, 1.0);
		for(EQSIM_Event event :eventList) {
			int year = (int)((event.getTime()-startTime)/SECONDS_PER_YEAR);
			evPerYear.add(year, 1.0);
		}
		ArrayList<EvenlyDiscretizedFunc> funcList = new ArrayList<EvenlyDiscretizedFunc>();
		funcList.add(evPerYear);
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcList, "Num Events Per Year"); 
		graph.setX_AxisLabel("Year");
		graph.setY_AxisLabel("Number");
	}
	
	
	/**
	 * This is a utility method for plotting histograms of normalized recurrence intervals,
	 * and it also finds a best-fit BPT and Lognormal distribuiton.
	 * @param normRI_List
	 * @param plotTitle
	 */
	public static GraphiWindowAPI_Impl plotNormRI_Distribution(ArrayList<Double> normRI_List, String plotTitle) {
		// find max value
		double max=0;
		for(Double val:normRI_List)
			if(val>max) max = val;
		double delta=0.1;
		int num = (int)Math.ceil(max/delta)+2;
		EvenlyDiscretizedFunc dist = new EvenlyDiscretizedFunc(delta/2, num,delta);
		dist.setTolerance(2*delta);
		int numData=normRI_List.size();
		for(Double val:normRI_List)
			dist.add(val, 1.0/(numData*delta));  // this makes it a true PDF
		
		// now make the function list for the plot
		ArrayList<EvenlyDiscretizedFunc> funcList = new ArrayList<EvenlyDiscretizedFunc>();
		
		// add best-fit BPT function
		BPT_DistCalc bpt_calc = new BPT_DistCalc();
		bpt_calc.fitToThisFunction(dist, 0.5, 1.5, 11, 0.1, 1.5, 151);
		EvenlyDiscretizedFunc fitBPT_func = bpt_calc.getPDF();
		fitBPT_func.setName("Best Fit BPT Dist");
		fitBPT_func.setInfo("(mean="+(float)bpt_calc.getMean()+", aper="+(float)bpt_calc.getAperiodicity()+")");
		funcList.add(fitBPT_func);
		
		// add best-fit Lognormal dist function
		LognormalDistCalc logNorm_calc = new LognormalDistCalc();
		logNorm_calc.fitToThisFunction(dist, 0.5, 1.5, 11, 0.1, 1.5, 141);
		EvenlyDiscretizedFunc fitLogNorm_func = logNorm_calc.getPDF();
		fitLogNorm_func.setName("Best Fit Lognormal Dist");
		fitLogNorm_func.setInfo("(mean="+(float)bpt_calc.getMean()+", aper="+(float)bpt_calc.getAperiodicity()+")");
		funcList.add(fitLogNorm_func);
		
		// add the histogram created here
		dist.setName("Recur. Int. Dist");
		dist.setInfo("(Number of points = "+ numData+")");
		funcList.add(dist);
		
		// make plot
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcList, plotTitle); 
		graph.setX_AxisLabel("RI (yrs)");
		graph.setY_AxisLabel("Density");
		ArrayList<PlotCurveCharacterstics> curveCharacteristics = new ArrayList<PlotCurveCharacterstics>();
		curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.RED));
		graph.setPlottingFeatures(curveCharacteristics);
		graph.setX_AxisRange(0, 5);
		
		return graph;
		
	}
	
	
	/**
	 * This method evaluates Ned's average time- and slip-predictability in various ways.
	 * @param magThresh
	 * @param fileName
	 */
	public void testTimePredictability(double magThresh, boolean saveStuff) {
		
		String linesFor_fw_timePred = new String();
		String tempInfoString = new String();


			double[] lastTimeForElement = new double[rectElementsList.size()];
			double[] lastSlipForElement = new double[rectElementsList.size()];
			for(int i=0; i<lastTimeForElement.length;i++) lastTimeForElement[i]=-1;  // initialize to bogus value so we can check
			
			int numBad=0;
			double minElementArea = Double.MAX_VALUE;
			double maxElementArea = 0;
			int counter=-1;
			
			ArrayList<Double> obsIntervalList = new ArrayList<Double>();
			ArrayList<Double> tpInterval1List = new ArrayList<Double>();
			ArrayList<Double> tpInterval2List = new ArrayList<Double>();
			ArrayList<Double> spInterval1List = new ArrayList<Double>();
			ArrayList<Double> spInterval2List = new ArrayList<Double>();
			ArrayList<Double> norm_tpInterval2List = new ArrayList<Double>();
			ArrayList<Double> norm_spInterval2List = new ArrayList<Double>();
			ArrayList<Integer> firstSectionList = new ArrayList<Integer>();
			
			// these are for an element-specific analysis (e.g., to see if tp and sp are correlated at an element)
			Integer testElementID = new Integer(661);
			boolean eventUtilizesTestElement;
			ArrayList<Double> tpInterval2ListForTestElement = new ArrayList<Double>();
			ArrayList<Double> spInterval2ListForTestElement = new ArrayList<Double>();
			
			tempInfoString+="Minimum Magnitude Considered for time and slip predicatbility = "+magThresh+"\n";
			
			// write file header
			linesFor_fw_timePred+="counter\tobsInterval\ttpInterval1\tnorm_tpInterval1\ttpInterval2\tnorm_tpInterval2\t"+
						"spInterval1\tnorm_spInterval1\tspInterval2\tnorm_spInterval2\t"+
						"aveLastSlip\taveSlip\teventMag\teventID\tfirstSectionID\tnumSectionsInEvent\tsectionsInEventString\n";
			
			
			// loop over all events
			for(EQSIM_Event event:eventList) {
				double eventTime = event.getTime();
				
//				if(event.hasElementSlipsAndIDs() && doesEventRuptureFullDepth(event)) {  // this didn't work better for eqs.NCA_RSQSim.barall.txt (but had weird events)
				if(event.hasElementSlipsAndIDs() && event.getMagnitude() >= magThresh) {
					boolean goodSample = true;
					double eventMag = event.getMagnitude();
					String sectionsInEventString = new String();
					ArrayList<Double> slipList = new ArrayList<Double>();
					ArrayList<Integer> elementID_List = new ArrayList<Integer>();
					// collect slip and ID data from all event records
					for(EventRecord evRec: event) {
						if(eventTime != evRec.getTime()) throw new RuntimeException("problem with event times");  // just a check
						slipList.addAll(evRec.getElementSlipList());
						elementID_List.addAll(evRec.getElementID_List());
/*if((evRec.getSectionID()-1) == 61) {
	System.out.println("eventID="+event.getID()+"\teventMag="+event.getMagnitude()+"\tevRec="+evRec.getID()+"\tevRecSectID="+evRec.getSectionID());
}*/
						sectionsInEventString += namesOfSections.get(evRec.getSectionID()-1) + " + ";
					}
					// get average date of last event and average predicted date of next
					double aveLastEvTime=0;
					double ave_tpNextEvTime=0;
					double ave_spNextEvTime=0;
					double aveSlipRate =0;
					double aveLastSlip =0;
					double aveSlip=0;
					int numElements = slipList.size();
					for(int e=0;e<numElements;e++) {
						int index = elementID_List.get(e).intValue() -1;  // index = ID-1
						double lastTime = lastTimeForElement[index];
						double lastSlip = lastSlipForElement[index];
						double slipRate = rectElementsList.get(index).getSlipRate();
						double area = rectElementsList.get(index).getGriddedSurface().getArea();
						if(area<minElementArea) minElementArea = area;
						if(area>maxElementArea) maxElementArea = area;
						aveLastEvTime += lastTime;
						if(slipRate != 0) {  // there are a few of these, and I don't know what else to do
							ave_tpNextEvTime += lastTime + lastSlip/(slipRate/SECONDS_PER_YEAR);
							ave_spNextEvTime += lastTime + slipList.get(e)/(slipRate/SECONDS_PER_YEAR);
						}
						aveSlipRate += slipRate/SECONDS_PER_YEAR;
						aveLastSlip += lastSlip;
						aveSlip += slipList.get(e);
						// mark as bad sample if  lastTime is -1
						if(lastTime==-1){
							goodSample=false;
//							System.out.println("time=0 for element"+e+" of event"+eventNum);
						}
					}
					aveLastEvTime /= numElements;
					ave_tpNextEvTime /= numElements;
					ave_spNextEvTime /= numElements;
					aveSlipRate /= numElements;
					aveLastSlip /= numElements;
					aveSlip /= numElements;
					double obsInterval = (eventTime-aveLastEvTime)/SECONDS_PER_YEAR;
					double tpInterval1 = (ave_tpNextEvTime-aveLastEvTime)/SECONDS_PER_YEAR;
					double tpInterval2 = (aveLastSlip/aveSlipRate)/SECONDS_PER_YEAR;
					double spInterval1 = (ave_spNextEvTime-aveLastEvTime)/SECONDS_PER_YEAR;
					double spInterval2 = (aveSlip/aveSlipRate)/SECONDS_PER_YEAR;
					double norm_tpInterval1 = obsInterval/tpInterval1;
					double norm_tpInterval2 = obsInterval/tpInterval2;
					double norm_spInterval1 = obsInterval/spInterval1;
					double norm_spInterval2 = obsInterval/spInterval2;
					
					// skip those that have zero aveSlipRate (causes Inf for tpInterval2 &spInterval2)
					if(aveSlipRate == 0) goodSample = false;
					
					// set boolean for whether event utilizes test element
					if(elementID_List.contains(testElementID))
						eventUtilizesTestElement=true;
					else
						eventUtilizesTestElement=false;

					if(goodSample) {
						counter += 1;
						linesFor_fw_timePred+=counter+"\t"+obsInterval+"\t"+
								tpInterval1+"\t"+(float)norm_tpInterval1+"\t"+
								tpInterval2+"\t"+(float)norm_tpInterval2+"\t"+
								spInterval1+"\t"+(float)norm_spInterval1+"\t"+
								spInterval2+"\t"+(float)norm_spInterval2+"\t"+
								(float)aveLastSlip+"\t"+(float)aveSlip+"\t"+
								(float)eventMag+"\t"+event.getID()+"\t"+
								event.get(0).getSectionID()+"\t"+
								event.size()+"\t"+sectionsInEventString+"\n";
						// save for calculating correlations
						obsIntervalList.add(obsInterval);
						tpInterval1List.add(tpInterval1);
						tpInterval2List.add(tpInterval2);
						spInterval1List.add(spInterval1);
						spInterval2List.add(spInterval2);
						firstSectionList.add(event.get(0).getSectionID());
						norm_tpInterval2List.add(norm_tpInterval2);
						norm_spInterval2List.add(norm_spInterval2);
						
						// add to test element list if it's the right element
						if(eventUtilizesTestElement) {
							tpInterval2ListForTestElement.add(tpInterval2);
							spInterval2ListForTestElement.add(spInterval2);
						}
						
					}
					else {
//						System.out.println("event "+ eventNum+" is bad");
						numBad += 1;
					}

					// now fill in the last event data for next time
					for(int e=0;e<numElements;e++) {
						int index = elementID_List.get(e).intValue() -1;
						lastTimeForElement[index] = eventTime;
						lastSlipForElement[index] = slipList.get(e);
					}
				}
			}
			
			// plot the normalized distributions and best fits
			GraphiWindowAPI_Impl plot1 = plotNormRI_Distribution(norm_tpInterval2List, "Normalized Ave Time-Pred RI (norm_tpInterval2List)");
			GraphiWindowAPI_Impl plot2 = plotNormRI_Distribution(norm_spInterval2List, "Normalized Ave Slip-Pred RI (norm_spInterval2List)");			
			if(saveStuff) {
				try {
					plot1.saveAsPDF(dirNameForSavingFiles+"/norm_tpInterval2_Dist.pdf");
					plot2.saveAsPDF(dirNameForSavingFiles+"/norm_spInterval2_Dist.pdf");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Print correlations for "observed" and predicted intervals
			double[] result;
			tempInfoString +="\nCorrelations (and chance it's random) between all Observed and Predicted Intervals:\n";
			result = this.getCorrelationAndP_Value(tpInterval1List, obsIntervalList);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for tpInterval1 (num pts ="+tpInterval1List.size()+")\n";
			result = this.getCorrelationAndP_Value(tpInterval2List, obsIntervalList);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for tpInterval2 (num pts ="+tpInterval2List.size()+")\n";
			result = this.getCorrelationAndP_Value(spInterval1List, obsIntervalList);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for spInterval1 (num pts ="+spInterval1List.size()+")\n";
			result = this.getCorrelationAndP_Value(spInterval2List, obsIntervalList);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for spInterval2 (num pts ="+spInterval2List.size()+")\n";

			// Print correlations between time-pred. and slip-pred. intervals
			tempInfoString +="\nCorrelations (and chance it's random) between all Predicted Intervals:\n";
			result = this.getCorrelationAndP_Value(tpInterval1List, spInterval1List);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for tpInterval1 vs spInterval1List (num pts ="+tpInterval1List.size()+")\n";
			result = this.getCorrelationAndP_Value(tpInterval2List, spInterval2List);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for tpInterval2 vs spInterval2List (num pts ="+tpInterval2List.size()+")\n";

			
			// now do correlations for each section
			tempInfoString +="\nCorrelations (and chance it's random) between Observed-tpInterval2 & tpInterval2-spInterval2 by Section:\n";
			ArrayList<DefaultXY_DataSet> obs_tp2_funcs = new ArrayList<DefaultXY_DataSet>();
			for(int s=0;s<namesOfSections.size();s++) {
				ArrayList<Double> vals1 = new ArrayList<Double>();
				ArrayList<Double> vals2 = new ArrayList<Double>();
				ArrayList<Double> vals3 = new ArrayList<Double>();
				for(int i=0;i<obsIntervalList.size();i++) {
					if(firstSectionList.get(i).intValue() == s+1) {
						vals1.add(obsIntervalList.get(i));
						vals2.add(tpInterval2List.get(i));
						vals3.add(spInterval2List.get(i));
					}
				}
				if(vals1.size()>2) {
					result = this.getCorrelationAndP_Value(vals1, vals2);
					double[] result2 = this.getCorrelationAndP_Value(vals2, vals3);
					tempInfoString +="\t"+(s+1)+"\t"+(float)result[0]+"\t("+(float)result[1]+
							")\t"+(float)result2[0]+"\t("+(float)result2[1]+")\tfor section "+namesOfSections.get(s)+" (num points = "+vals1.size()+")\n";
					// make XY data for plot
					DefaultXY_DataSet xy_data = new DefaultXY_DataSet(vals2,vals1);
					xy_data.setName(namesOfSections.get(s));
					obs_tp2_funcs.add(xy_data);

				}
				else
					tempInfoString +="\t"+(s+1)+"\tNaN\t\t\t\t\t\t\t\t"+
							namesOfSections.get(s)+" (num points = "+vals1.size()+")\n";
			}
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(obs_tp2_funcs, "Obs vs Time-Pred RIs");   
			graph.setX_AxisLabel("Time Pred RI (tpInterval2List) (years)");
			graph.setY_AxisLabel("Observed RI (years)");
			graph.setAllLineTypes(null, PlotSymbol.CROSS);
			graph.setYLog(true);
			graph.setXLog(true);
			
			// print and plot the test element correlations
			tempInfoString +="\nCorrelations (and chance it's random) between Predicted Intervals That Involve Element ID="+testElementID+":\n";
			result = getCorrelationAndP_Value(tpInterval2ListForTestElement, spInterval2ListForTestElement);
			tempInfoString +="\t"+(float)result[0]+"\t("+result[1]+") for tpInterval2 vs spInterval2List (num pts ="+tpInterval2ListForTestElement.size()+")\n";
			ArrayList<DefaultXY_DataSet> obs_tp1_funcsForTestElement = new ArrayList<DefaultXY_DataSet>();
			DefaultXY_DataSet xy_data = new DefaultXY_DataSet(tpInterval2ListForTestElement,spInterval2ListForTestElement);
			obs_tp1_funcsForTestElement.add(xy_data);
			GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(obs_tp1_funcsForTestElement, "Slip-Pred vs Time-Pred RIs at Element ID="+testElementID);   
			graph2.setX_AxisLabel("Time-Pred RI (years)");
			graph2.setY_AxisLabel("Slip-Pred RI (years)");
			graph2.setAllLineTypes(null, PlotSymbol.CROSS);
			graph2.setYLog(true);
			graph2.setXLog(true);
			
			if(saveStuff) {
				try {
					graph.saveAsPDF(dirNameForSavingFiles+"/obsVersusTimePred2_RIs.pdf");
					graph2.saveAsPDF(dirNameForSavingFiles+"/slipVersusTimePred2_RI_AtElemID"+testElementID+".pdf");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Make the norm RI plot for each section
			for(int s=0;s<namesOfSections.size();s++) {
//			for(int s=0;s<1;s++) {  // only do first one for now
				ArrayList<Double> sectNorm_tpInterval2List = new ArrayList<Double>();
				for(int i=0; i<norm_tpInterval2List.size();i++) {
					if(firstSectionList.get(i) == (s+1)) { // does section ID correspond to section index
						sectNorm_tpInterval2List.add(norm_tpInterval2List.get(i));
					}
				}
				if(sectNorm_tpInterval2List.size()>25){
					String plotTitle = "Normalized Ave Time-Pred RI (norm_tpInterval2List) for "+namesOfSections.get(s);
					GraphiWindowAPI_Impl plot = plotNormRI_Distribution(sectNorm_tpInterval2List, plotTitle);
					if(saveStuff) {
						String fileName = "/norm_tpInterval2_Dist_forSect"+s+".pdf";
						try {
							plot.saveAsPDF(dirNameForSavingFiles+fileName);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}			
				}
			}


			tempInfoString +="\n"+numBad+" events were bad\n";
			
			tempInfoString +="minElementArea="+(float)minElementArea+"\tmaxElementArea"+(float)maxElementArea+"\n";
			
		try {
			if(saveStuff) {
				FileWriter fw_timePred = new FileWriter(dirNameForSavingFiles+"/TimePredTestData.txt");
				fw_timePred.write(linesFor_fw_timePred);
				fw_timePred.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		infoStrings.add(tempInfoString);
		System.out.println(tempInfoString);

	}
	
	
	/**
	 * This computes the correlation coefficient and the p-value between the two lists.  
	 * The p-value represents the two-tailed significance of the result (and it depends on the 
	 * number of points).  This represents the probability that a truly random process
	 * would produce a correlation greater than the value or less than the negative value.  
	 * In other words, if you reject the null hypothesis that there is no correlation, then
	 * there is the p-value chance that you are wrong.  The one sided values are exactly half 
	 * the two-sided values.  I verified the p-values against an on-line calculator.
	 * @param list1
	 * @param list2
	 * @return double[2], where the first element is the correlation and the second is the p-value
	 */
	private double[] getCorrelationAndP_Value(ArrayList<Double> list1, ArrayList<Double> list2) {
		double[][] vals = new double[list1.size()][2];
		for(int i=0;i<list1.size();i++) {
			vals[i][0] = list1.get(i);
			vals[i][1] = list2.get(i);
		}
		PearsonsCorrelation corrCalc = new PearsonsCorrelation(vals);
		double[] result = new double[2];
		RealMatrix matrix;
		try {
			matrix = corrCalc.getCorrelationMatrix();
			result[0] = matrix.getEntry(0, 1);
			matrix = corrCalc.getCorrelationPValues();
			result[1] = matrix.getEntry(0, 1);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	
	
	/**
	 * This compares all the computed magnitudes to those given on the input files 
	 * and writes out the maximum absolute difference.
	 */
	public void checkEventMagnitudes() {

		double maxMagDiff = 0;

		// loop over all events
		for(EQSIM_Event event:eventList) {
			if(event.hasElementSlipsAndIDs()) {

				double eventMag = event.getMagnitude();
				double moment =0;
				ArrayList<Double> slipList = new ArrayList<Double>();
				ArrayList<Integer> elementID_List = new ArrayList<Integer>();
				// collect slip and ID data from all event records
				for(EventRecord evRec: event) {
					slipList.addAll(evRec.getElementSlipList());
					elementID_List.addAll(evRec.getElementID_List());
				}
				// get average date of last event and average predicted date of next
				int numElements = slipList.size();
				for(int e=0;e<numElements;e++) {
					int index = elementID_List.get(e).intValue() -1;
					double area = rectElementsList.get(index).getGriddedSurface().getArea();
					double slip = slipList.get(e); // this is in meters
					moment += FaultMomentCalc.getMoment(area*1e6, slip);	// convert area to meters squared
				}
				double computedMag = MagUtils.momentToMag(moment);
				double diff = Math.abs(eventMag-computedMag);
				if(diff> maxMagDiff) maxMagDiff = diff;
			}
		}
		System.out.println("maximum abs(eventMag-computedMag) ="+maxMagDiff);
	}
	
	
	public void writeEventsThatInvolveMultSections() {
		System.out.println("Events that involve more than one section:");
		System.out.println("\t\tEvID\t# Sect\tMag\tSections involved...");
		int num =0;
		for(EQSIM_Event event:eventList) {
			if(event.size()>1) {
				num += 1;
				double mag = Math.round(event.getMagnitude()*100.0)/100.0;
				System.out.print("\t"+num+"\t"+event.getID()+"\t"+event.size()+"\t"+mag);
				for(EventRecord rec:event)
					System.out.print("\t"+this.namesOfSections.get(rec.getSectionID()-1));
				System.out.print("\n");
			}
		}
	}
	
	
	/**
	 * This computes the simulation duration from the times of the first and last event
	 * @return
	 */
	public double getSimulationDurationInYears() {
		double startTime=eventList.get(0).getTime();
		double endTime=eventList.get(eventList.size()-1).getTime();
		return (endTime-startTime)/SECONDS_PER_YEAR;
	}

	
	/**
	 * This compares observed slip rate (from events) with those imposed.  This writes out
	 * the correlation coefficient to System, and optionally: makes a plot and a file.
	 * @param fileName - set as null to not write the data out.
	 */
	public void checkElementSlipRates(String fileName, boolean makePlot) {

		FileWriter fw_slipRates;
		double[] obsAveSlipRate = new double[rectElementsList.size()];
		double[] imposedSlipRate = new double[rectElementsList.size()];
		int[] numEvents = new int[rectElementsList.size()];
		int eventNum=0;
		// loop over all events
		for(EQSIM_Event event:eventList) {
			eventNum++;
			if(event.hasElementSlipsAndIDs()) {
				ArrayList<Double> slipList = new ArrayList<Double>();
				ArrayList<Integer> elementID_List = new ArrayList<Integer>();
				// collect slip and ID data from all event records
				for(EventRecord evRec: event) {
					slipList.addAll(evRec.getElementSlipList());
					elementID_List.addAll(evRec.getElementID_List());
				}
				int numElements = slipList.size();
				for(int e=0;e<numElements;e++) {
					int index = elementID_List.get(e).intValue() -1;
					obsAveSlipRate[index] += slipList.get(e);
					numEvents[index] += 1;
					//						if(eventNum ==3) System.out.println("Test: el_ID="+elementID_List.get(e).intValue()+"\tindex="+index+"\tslip="+slipList.get(e));
				}
			}
		}

		// finish obs and get imposed slip rates:
		double simDurr = getSimulationDurationInYears();
		for(int i=0; i<obsAveSlipRate.length; i++) {
			obsAveSlipRate[i] /= simDurr;
			imposedSlipRate[i] = rectElementsList.get(i).getSlipRate();
		}

		PearsonsCorrelation corrCalc = new PearsonsCorrelation();
		double slipRateCorr = corrCalc.correlation(obsAveSlipRate, imposedSlipRate);
		System.out.println("Correlation between obs and imposed slip rate = "+(float)slipRateCorr);
		
		// make plot if desired
		if(makePlot) {
			DefaultXY_DataSet xy_data = new DefaultXY_DataSet(imposedSlipRate,obsAveSlipRate);
			xy_data.setName("Obs versus Imposed Slip Rate");
			xy_data.setInfo(" ");
			ArrayList<DefaultXY_DataSet> funcs = new ArrayList<DefaultXY_DataSet>();
			funcs.add(xy_data);
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Slip Rate Comparison");   
			graph.setX_AxisLabel("Imposed Slip Rate (m/s)");
			graph.setY_AxisLabel("Observed Slip Rate (m/s)");
			ArrayList<PlotCurveCharacterstics> curveCharacteristics = new ArrayList<PlotCurveCharacterstics>();
			curveCharacteristics.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 4f, Color.BLUE));
			graph.setPlottingFeatures(curveCharacteristics);
		}

		// write file if name is non null
		if(fileName != null) {
			try {
				fw_slipRates = new FileWriter(fileName);
				fw_slipRates.write("obsSlipRate\timposedSlipRate\tdiff\tnumEvents\n");
				//				System.out.println(endTime+"\t"+startTime);
				for(int i=0; i<obsAveSlipRate.length; i++) {
					double diff = obsAveSlipRate[i]-imposedSlipRate[i];
					fw_slipRates.write(obsAveSlipRate[i]+"\t"+imposedSlipRate[i]+"\t"+diff+"\t"+numEvents[i]+"\n");
				}
				fw_slipRates.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	/**
	 * This plots slip versus length, mag versus area, and mag versus length.
	 */
	public void plotScalingRelationships(boolean savePlotsToFile) {
		double[] slip = new double[eventList.size()];
		double[] mag = new double[eventList.size()];
		double[] area = new double[eventList.size()];
		double[] length = new double[eventList.size()];
		
		int index = -1;
		for(EQSIM_Event event:eventList) {
			index +=1;
			slip[index]=event.getMeanSlip();
			mag[index]=event.getMagnitude();
			area[index]=event.getArea()/1e6; 		// convert to km-sq
			length[index]=event.getLength()/1000; 	// convert to km
		}
		/**/
		
		// SLIP VS LENGTH PLOT
		DefaultXY_DataSet s_vs_l_data = new DefaultXY_DataSet(slip,length);
		s_vs_l_data.setName("Mean Slip vs Length");
		s_vs_l_data.setInfo(" ");
		ArrayList s_vs_l_funcs = new ArrayList();
		s_vs_l_funcs.add(s_vs_l_data);
		GraphiWindowAPI_Impl s_vs_l_graph = new GraphiWindowAPI_Impl(s_vs_l_funcs, "Mean Slip vs Length");   
		s_vs_l_graph.setX_AxisLabel("Mean Slip (m)");
		s_vs_l_graph.setY_AxisLabel("Length (km)");
		ArrayList<PlotCurveCharacterstics> s_vs_l_curveChar = new ArrayList<PlotCurveCharacterstics>();
		s_vs_l_curveChar.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 3f, Color.BLUE));
		s_vs_l_graph.setPlottingFeatures(s_vs_l_curveChar);
		
		// MAG VS AREA PLOT
		DefaultXY_DataSet m_vs_a_data = new DefaultXY_DataSet(area,mag);
		m_vs_a_data.setName("Mag-Area data from simulation");
		m_vs_a_data.setInfo(" ");
		ArrayList m_vs_a_funcs = new ArrayList();
		Ellsworth_B_WG02_MagAreaRel elB = new Ellsworth_B_WG02_MagAreaRel();
		HanksBakun2002_MagAreaRel hb = new HanksBakun2002_MagAreaRel();
		WC1994_MagAreaRelationship wc = new WC1994_MagAreaRelationship();
		wc.setRake(0);
		Shaw_2007_MagAreaRel sh = new Shaw_2007_MagAreaRel();
		m_vs_a_funcs.add(elB.getMagAreaFunction(1, 10000, 101));
		m_vs_a_funcs.add(hb.getMagAreaFunction(1, 10000, 101));
		m_vs_a_funcs.add(wc.getMagAreaFunction(1, 10000, 101));
		m_vs_a_funcs.add(sh.getMagAreaFunction(1, 10000, 101));
		m_vs_a_funcs.add(m_vs_a_data);	// do this after the above so it plots underneath
		GraphiWindowAPI_Impl m_vs_a_graph = new GraphiWindowAPI_Impl(m_vs_a_funcs, "Mag vs Area");   
		m_vs_a_graph.setY_AxisLabel("Magnitude (Mw)");
		m_vs_a_graph.setX_AxisLabel("Area (km-sq)");
		ArrayList<PlotCurveCharacterstics> m_vs_a_curveChar = new ArrayList<PlotCurveCharacterstics>();
		m_vs_a_curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));
		m_vs_a_curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLUE));
		m_vs_a_curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.GREEN));
		m_vs_a_curveChar.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.MAGENTA));
		m_vs_a_curveChar.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 3f, Color.RED));
		m_vs_a_graph.setPlottingFeatures(m_vs_a_curveChar);
		m_vs_a_graph.setXLog(true);
		m_vs_a_graph.setY_AxisRange(4.5, 8.5);
	/**/
		// MAG VS LENGTH PLOT
		DefaultXY_DataSet m_vs_l_data = new DefaultXY_DataSet(length,mag);
		m_vs_l_data.setName("Mag vs Length");
		m_vs_l_data.setInfo(" ");
		ArrayList m_vs_l_funcs = new ArrayList();
		m_vs_l_funcs.add(m_vs_l_data);
		GraphiWindowAPI_Impl m_vs_l_graph = new GraphiWindowAPI_Impl(m_vs_l_funcs, "Mag vs Length");   
		m_vs_l_graph.setY_AxisLabel("Magnitude (Mw)");
		m_vs_l_graph.setX_AxisLabel("Length (km)");
		ArrayList<PlotCurveCharacterstics> m_vs_l_curveChar = new ArrayList<PlotCurveCharacterstics>();
		m_vs_l_curveChar.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 3f, Color.GREEN));
		m_vs_l_graph.setPlottingFeatures(m_vs_l_curveChar);
		m_vs_l_graph.setXLog(true);
		m_vs_l_graph.setY_AxisRange(4.5, 8.5);
		
		if(savePlotsToFile) {
			try {
				s_vs_l_graph.saveAsPDF(dirNameForSavingFiles+"/s_vs_l_graph.pdf");
				m_vs_a_graph.saveAsPDF(dirNameForSavingFiles+"/m_vs_a_graph.pdf");
				m_vs_l_graph.saveAsPDF(dirNameForSavingFiles+"/m_vs_l_graph.pdf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	
	/**
	 * For the specified element ID, this computes the intervals between events that have 
	 * magnitude greater than the given threshold.
	 * @param elemID
	 * @param magThresh
	 * @return
	 */
	public double[] getRecurIntervalsForElement(int elemID, double magThresh) {
		ArrayList<Double> eventTimes = new ArrayList<Double>();
		for(EQSIM_Event event:eventList)
			if(event.hasElementSlipsAndIDs())
				if(event.getAllElementIDs().contains(elemID) && event.getMagnitude() >= magThresh)
					eventTimes.add(event.getTimeInYears());
		if (eventTimes.size()>0) {
			double[] intervals = new double[eventTimes.size()-1];
			for(int i=1;i<eventTimes.size();i++)
				intervals[i-1] = (eventTimes.get(i)-eventTimes.get(i-1));
			return intervals;
		}
		else return null;
	}
	
	
	/**
	 * This plots a histogram of normalized recurrence intervals for all surface elements
	 * (normalized by the average interval at each  element).
	 * @param magThresh
	 */
	public void plotNormRecurIntsForAllSurfaceElements(double magThresh, boolean savePlot) {

		ArrayList<Double> vals = new ArrayList<Double>();
		// Loop over elements
		for(RectangularElement elem:rectElementsList) {
			// check whether it's a surface element
			if(elem.getVertices()[0].getTraceFlag() != 0) {
//				System.out.println("trace vertex found");
				double[] recurInts = getRecurIntervalsForElement(elem.getID(), magThresh);
				if(recurInts != null) {
					double mean=0;
					for(int i=0;i<recurInts.length; i++) 
						mean += recurInts[i]/recurInts.length;
					for(int i=0;i<recurInts.length; i++) {
						vals.add(recurInts[i]/mean);
					}					
				}
			}
		}
		GraphiWindowAPI_Impl graph = plotNormRI_Distribution(vals, "Normalized RI for All Surface Elements");
		if(savePlot)
			try {
				graph.saveAsPDF(dirNameForSavingFiles+"/NormRecurIntsForAllSurfaceElements.pdf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}


	
	/**
	 * This one only includes events that utilize the nearest rectangular element (presumably the 
	 * one at the surface), which means non-surface rupturing events will not be included.  This quits
	 * and returns null if the loc is not within 5 km of an element.
	 * @param lat
	 * @param lon
	 * @param magThresh
	 * @param makePlot
	 * @return
	 */
	public double[] getRecurIntervalsForNearestLoc(Location loc, double magThresh, boolean makePlot, 
			boolean savePlot, String locName) {
		double minDist= Double.MAX_VALUE;
		int elementIndex=-1;
		//Find nearest Element
		for(int i=0; i<rectElementsList.size(); i++) {
			double dist = DistanceRupParameter.getDistance(loc, rectElementsList.get(i).getGriddedSurface());
			if(dist<minDist){
				minDist=dist;
				elementIndex= i;
			}
		}
		
		// quit and return null if not near element
		if(minDist>5.0) {
			System.out.println("No element found near the site "+locName);
			return null;
		}
		
		Integer elemID = rectElementsList.get(elementIndex).getID();
		System.out.println("Closest Element to location "+locName+" is rect elem ID "+elemID+
				" on "+rectElementsList.get(elementIndex).getSectionName()+" ("+minDist+" km away)");
		infoStrings.add("Closest Element to loc"+locName+" is rect elem ID "+elemID+
				" on "+rectElementsList.get(elementIndex).getSectionName()+" ("+minDist+" km away)");
		
		double[] intervals = getRecurIntervalsForElement(elemID, magThresh);
		double maxInterval=0;
		for(int i=1;i<intervals.length;i++) {
			if(intervals[i]>maxInterval) maxInterval = intervals[i];
		}
		
		System.out.println("number of RIs for loc is "+intervals.length+" for Mag>="+magThresh);
		infoStrings.add("\tnumber of RIs for loc is "+intervals.length+" for Mag>="+magThresh);
		
		// calc num bins at 10-year intervals
		int numBins = (int)Math.ceil(maxInterval/10.0);
		EvenlyDiscretizedFunc riHist = new EvenlyDiscretizedFunc(5.0, numBins, 10.0);
		riHist.setTolerance(20.0);  // anything more than 10 should do it
		
		double mean=0;
		for(int i=0; i<intervals.length;i++) {
			riHist.add(intervals[i], 1.0/intervals.length);
			mean += intervals[i]/intervals.length;
			System.out.println(intervals[i]);
		}

		
		// now compute stdDevOfMean in log10 space
		double stdDevOfMean=0;
		for(int i=0; i<intervals.length;i++) {
			stdDevOfMean += (intervals[i]-mean)*(intervals[i]-mean);
		}
		stdDevOfMean = Math.sqrt(stdDevOfMean/(intervals.length-1)); // this is the standard deviation
		stdDevOfMean /= Math.sqrt(intervals.length); // this is the standard deviation of mean
		
		double firstBin = 10*Math.round(0.1*(mean-1.96*stdDevOfMean));
		double lastBin = 10*Math.round(0.1*(mean+1.96*stdDevOfMean));
		double meanBin = 10*Math.round(0.1*mean);
		int numBin = (int)Math.round((lastBin-firstBin)/10) +1;
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(firstBin, lastBin, numBin);
		func.set(firstBin,0.05);
		func.set(lastBin,0.05);
		func.set(meanBin,0.2);
		func.setName("Mean and 95% confidence bounds on the mean simulator RI for "+locName);
		func.setInfo("  ");

		riHist.setName("RI histogram for "+locName+" (& Mag>="+magThresh+")");
		riHist.setInfo("Lat="+loc.getLatitude()+"; lon="+loc.getLongitude()+ " for "+locName);
		
		if(makePlot){
			// funcs added first plot on top
			ArrayList<DiscretizedFunc> funcList = new ArrayList<DiscretizedFunc>();
			funcList.add(func);
			EvenlyDiscretizedFunc parsFunc = ucerf2_dataFetcher.getParsons95PercentPoisFunction(loc);
			if(parsFunc != null)
				funcList.add(parsFunc);  
			funcList.add(riHist);
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcList, "Recurence Intervals for "+locName+" (& Mag>="+magThresh+")"); 
			graph.setX_AxisLabel("RI (yrs)");
			graph.setY_AxisLabel("Number of Observations");
			ArrayList<PlotCurveCharacterstics> curveCharacteristics = new ArrayList<PlotCurveCharacterstics>();
			curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLACK));
			if(parsFunc != null) curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.RED));
			curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.LIGHT_GRAY));
			graph.setPlottingFeatures(curveCharacteristics);
			if(savePlot)
				try {
					graph.saveAsPDF(dirNameForSavingFiles+"/RI_HistogramFor_"+locName+".pdf");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}



		}

		return intervals;
	}
	
	
	/**
	 * This version includes events that pass anywhere below the site (by using DAS values the way Keith Richards-Dinger does it)
	 * @param lat
	 * @param lon
	 * @param magThresh
	 * @param makePlot
	 * @return
	 */
	public double[] getRecurIntervalsForNearestLoc2(double lat, double lon, double magThresh, boolean makePlot) {
		Location loc = new Location(lat,lon);
		double minDist= Double.MAX_VALUE;
		int vertexIndex=-1;
		//Find nearest Element
		for(int i=0; i<vertexList.size(); i++) {
			double dist = LocationUtils.linearDistance(loc, vertexList.get(i));
			if(dist<minDist){
				minDist=dist;
				vertexIndex= i;
			}
		}
		Vertex closestVertex = vertexList.get(vertexIndex);
		// Find 2nd closest vertex
		double secondMinDist= Double.MAX_VALUE;
		int secondClosestVertexIndex=-1;
		//Find nearest Element
		for(int i=0; i<vertexList.size(); i++) {
			double dist = LocationUtils.linearDistance(loc, vertexList.get(i));
			if(dist<secondMinDist && i != vertexIndex){
				secondMinDist=dist;
				secondClosestVertexIndex= i;
			}
		}
		Vertex secondClosestVertex = vertexList.get(secondClosestVertexIndex);

		double das = 1000*(closestVertex.getDAS()*minDist+secondClosestVertex.getDAS()*secondMinDist)/(minDist+secondMinDist); // convert to meters for comparisons below
		int sectIndex = -1;
		// find the section index for the closest vertex
		for(int i=0; i<vertexListForSections.size(); i++)
			if(vertexListForSections.get(i).contains(closestVertex))
				sectIndex = i;
		int sectID = sectIndex+1;
				
		System.out.println("RI PDF at site ("+lat+","+lon+"):\n\tClosest vertex ID is "+closestVertex.getID()+" & second closest vertex ID "+secondClosestVertex.getID()+
				", on section "+namesOfSections.get(sectIndex)+" at average DAS of "+(float)das+"; site is "+(float)minDist+" km away from closest vertex.");

		ArrayList<Double> eventTimes = new ArrayList<Double>();
		for(EQSIM_Event event:eventList) {
			if(event.getMagnitude() >= magThresh && event.doesEventIncludeSectionAndDAS(sectID,das)) {
						eventTimes.add(event.getTimeInYears());
			}
		}
		double[] intervals = new double[eventTimes.size()-1];
		double maxInterval=0;
		for(int i=1;i<eventTimes.size();i++) {
			intervals[i-1] = (eventTimes.get(i)-eventTimes.get(i-1));
			if(intervals[i-1]>maxInterval) maxInterval = intervals[i-1];
		}
		
		System.out.println("\tnumber of RIs for loc is "+intervals.length);
		
		// calc num bins at 10-year intervals
		int numBins = (int)Math.ceil(maxInterval/10.0);
		EvenlyDiscretizedFunc riHist = new EvenlyDiscretizedFunc(5.0, numBins, 10.0);
		riHist.setTolerance(20.0);  // anything more than 10 should do it
		
		for(int i=0; i<intervals.length;i++)
			riHist.add(intervals[i], 1.0);
		
		if(makePlot){
			ArrayList<EvenlyDiscretizedFunc> funcList = new ArrayList<EvenlyDiscretizedFunc>();
			funcList.add(riHist);
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcList, "Recurence Intervals"); 
			graph.setX_AxisLabel("RI (yrs)");
			graph.setY_AxisLabel("Number Observed");
			ArrayList<PlotCurveCharacterstics> curveCharacteristics = new ArrayList<PlotCurveCharacterstics>();
			curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLUE));
			graph.setPlottingFeatures(curveCharacteristics);
		}

		return intervals;
	}
	
	
	public void doAllAnalysis(String dirNameForSavingFiles, double magThresh) {
		infoStrings = new ArrayList<String>();
		this.dirNameForSavingFiles = dirNameForSavingFiles;
		File file1 = new File(dirNameForSavingFiles);
		file1.mkdirs();

		infoStrings.add("Simulation Duration is "+(float)this.getSimulationDurationInYears()+" years");
		
		// plot & save scaling relationships
//		plotScalingRelationships(true);
		
//		plotNormRecurIntsForAllSurfaceElements(magThresh, true);
		
		/*
		// need to loop over all interesting sites, and to add observed dists
		ArrayList<Location> locList = ucerf2_dataFetcher.getParsonsSiteLocs();
		ArrayList<String> namesList = ucerf2_dataFetcher.getParsonsSiteNames();
		for(int i=0;i<locList.size();i++)
			getRecurIntervalsForNearestLoc(locList.get(i), 6.5, true, true,namesList.get(i));
*/
		
		// this is a location that has a very non-BPT looking PDF of recurrence times.
//		Location loc = rectElementsList.get(497-1).getGriddedSurface().get(0, 1);
//		getRecurIntervalsForNearestLoc(loc, 6.5, true, false,loc.toString());
		
//		computeTotalMagFreqDist(4.05,9.05,51,true,true);
		
//		computeMagFreqDistByFaultSection(4.05,9.05,51,true,true,true);
		
		randomizeEventTimes();
		testTimePredictability(magThresh, true);

		
//		System.out.println(infoStrings);

		try {
			FileWriter infoFileWriter = new FileWriter(this.dirNameForSavingFiles+"/INFO.txt");
			for(String string: infoStrings) 
				infoFileWriter.write(string+"\n");
			infoFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/**
	 * This looks at what rupture do and do not rupture all the way down dip
	 */
	public void checkFullDDW_rupturing() {
		double minThatDoes=10;
		double maxThatDoesnt=0;
		int eventID_forMin=-1;
		int eventID_forMax=-1;
		ArbIncrementalMagFreqDist mfd_does = new ArbIncrementalMagFreqDist(5, 8.5, 36);
		mfd_does.setName("Rups Completely Down Dip");
		mfd_does.setTolerance(1.0);
		ArbIncrementalMagFreqDist mfd_doesNot = new ArbIncrementalMagFreqDist(5, 8.5, 36);
		mfd_doesNot.setName("Does Not Rup Completely Down Dip");
		mfd_doesNot.setTolerance(1.0);
		for(EQSIM_Event event:eventList) {
			double mag = event.getMagnitude();
			if(doesEventRuptureFullDepth(event)) {
				if(mag<minThatDoes) {
					minThatDoes=mag;
					eventID_forMin=event.getID();
				}
				mfd_does.add(mag, 1.0);
			}
			else {
				if(mag>maxThatDoesnt) {
					maxThatDoesnt=mag;
					eventID_forMax=event.getID();
				}
				mfd_doesNot.add(mag, 1.0);
			}
		}
		System.out.println("minThatDoes="+minThatDoes+"\teventID="+eventID_forMin);
		System.out.println("maxThatDoesnt="+maxThatDoesnt+"\teventID="+eventID_forMax);
		System.out.println(mfd_does);
		System.out.println(mfd_doesNot);
		
	}
	
	public void mkFigsForUCERF3_ProjPlanRepot(String dirNameForSavingFiles, double magThresh) {
		infoStrings = new ArrayList<String>();
		this.dirNameForSavingFiles = dirNameForSavingFiles;
		File file1 = new File(dirNameForSavingFiles);
		file1.mkdirs();

		infoStrings.add("Simulation Duration is "+(float)this.getSimulationDurationInYears()+" years");
		
		// this is a location that has a very non-BPT looking PDF of recurrence times for "eqs.NCA_RSQSim.barall.txt" file.
		Location loc = rectElementsList.get(497-1).getGriddedSurface().get(0, 1);
		getRecurIntervalsForNearestLoc(loc, 6.5, true, true,"RI_distAt_NSAF_ElementID497");
		
		testTimePredictability(magThresh, true);
		
//		System.out.println(infoStrings);

		try {
			FileWriter infoFileWriter = new FileWriter(this.dirNameForSavingFiles+"/INFO.txt");
			for(String string: infoStrings) 
				infoFileWriter.write(string+"\n");
			infoFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		long startTime=System.currentTimeMillis();
		System.out.println("Starting");
/*		*/
		// RSQSim Runs:
		String fullPath = "org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCA_Ward_Geometry.dat.txt";
		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath);
		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/eqs.NCA_RSQSim.barall.txt");
		test.mkFigsForUCERF3_ProjPlanRepot("RSQSim_Run",  6.5);
		
		test.randomizeEventTimes();
		test.mkFigsForUCERF3_ProjPlanRepot("RSQSim_Run_Randomized",  6.5);

/*		
		// VC Runs:
		String fullPath = "org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCA_Ward_Geometry.dat.txt";
		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath);
		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/VC_NCAL_Ward_Event.d");
		test.mkFigsForUCERF3_ProjPlanRepot("VC_Run",  6.5);
*/
		/*
		// Ward Runs:
//		String fullPath = "org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCAL4_Ward_Geometry.dat.txt";
//		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath);
//		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCAL4-WARD-30k.dat");
		String fullPath = "org/opensha/sha/simulators/eqsim_v04/WardsInputFile/test.txt";  // I had to rename the file "NCAL(9/1/10)-elements.dat.txt" to test.txt to get this to work
		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath, 1);
		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCAL_Ward.out.txt");

		test.mkFigsForUCERF3_ProjPlanRepot("Ward_Run",  6.5);
	*/
		
		int runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("This Run took "+runtime+" seconds");


		
		//OLD JUNK BELOW
		
		
		
		
		/*
		// this is for analysis of the Ward Results:
		String fullPath = "org/opensha/sha/simulators/eqsim_v04/WardsInputFile/test.txt";
		// I had to rename the file "NCAL(9/1/10)-elements.dat.txt" to test.txt to get this to work
		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath, 1);
		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCAL_Ward.out.txt");
		test.checkEventMagnitudes();
		test.checkElementSlipRates("testSlipRateFileForWard");
		test.testTimePredictability(6.5, "testTimePredFileForWard_M6pt5");
		 */
		
		// this is for analysis of the RQSim Results:
//		String fullPath = "org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCA_Ward_Geometry.dat.txt";
//		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath);
//		ArrayList<String> sectNames = test.getSectionsNameList();
//		System.out.println("Section Names (IDs)");
//		for(int s=0; s<sectNames.size();s++)	System.out.println("\t"+sectNames.get(s)+"("+(s+1)+")");
//		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/eqs.NCA_RSQSim.barall.txt");
//		test.checkEventMagnitudes();
//		test.checkElementSlipRates("testSlipRateFileForEQSim", true);
//		System.out.println("Simulation Duration is "+(float)test.getSimulationDurationInYears()+" years");
//		test.randomizeEventTimes();
//		test.plotYearlyEventRates();
//		test.test();
//		test.doAllAnalysis("NEDS_TEST", 6.5);
//		test.writeEventsThatInvolveMultSections();
		

		
		/*  This writes an EQSIM file from a UCERF2 deformation model
		General_EQSIM_Tools test = new General_EQSIM_Tools(82, false, 4.0);
//		test.getElementsList();
		String writePath = "testEQSIM_Output.txt";
		try {
			test.writeTo_EQSIM_V04_GeometryFile(writePath, null, "test UCERF2 output", "Ned Field", "Aug 3, 2010");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		
		/*
		// THE FOLLOWING TEST LOOKS GOOD FROM A VISUAL INSPECTION
		String fullPath = "org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCA_Ward_Geometry.dat.txt";
//		String fullPath = "org/opensha/sha/simulators/eqsim_v04/ALLCAL_Model_v04/ALLCAL_Ward_Geometry.dat";
		General_EQSIM_Tools test = new General_EQSIM_Tools(fullPath);
		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/NCAL_Ward.out.txt");
//		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/VC_norcal.d.txt");
//		test.read_EQSIMv04_EventsFile("org/opensha/sha/simulators/eqsim_v04/ExamplesFromKeith/eqs.NCA_RSQSim.barall.txt");


		System.out.println(test.getNumEventsWithElementSlipData()+" out of "+test.getEventsList().size()+" have slip on elements data");

		// find the mag cutoff for inclusion of element slip data
		double maxWithOut = 0;
		double minWith =10;
		for(EQSIM_Event event:test.getEventsList()) {
			if(event.hasElementSlipsAndIDs()) {
				if (event.getMagnitude()<minWith) minWith = event.getMagnitude();
			}
			else
				if(event.getMagnitude()>maxWithOut) maxWithOut = event.getMagnitude();
		}
		System.out.println("minWith="+minWith+";\tmaxWithOut="+maxWithOut);
		
		*/
						
	}
}
