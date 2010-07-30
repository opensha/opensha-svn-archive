package org.opensha.sha.simulators.eqsim_v04;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.opensha.commons.data.NamedObjectComparator;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.PlaneUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.EvenlyGridCenteredSurface;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;

public class General_EQSIM_Tools {

	protected final static boolean D = false;  // for debugging
	
	private ArrayList<FaultSectionPrefData> allFaultSectionPrefData;
	ArrayList<RectangularElement> rectElementsList;
	

	/**
	 * This constructor makes the list of RectangularElements from a UCERF2 deformation model
	 * @param deformationModelID	- D2.1 = 82; D2.2 = 83; D2.3 = 84; D2.4 = 85; D2.5 = 86; D2.6 = 87
	 * @param aseisReducesArea		- whether or not to reduce area (otherwise reduces slip rate?)
	 * @param maxDiscretization		- the maximum element size
	 */
	public General_EQSIM_Tools(int deformationModelID, boolean aseisReducesArea,
			double maxDiscretization) {
		
		mkElementsFromUCERF2_DefMod(deformationModelID, aseisReducesArea, maxDiscretization);
	}
	
	
	/**
	 * This constructor loads the elements from the given lines from a Ward-format file
	 * @param lines		 - ascii lines
	 * @param formatType - format type: 0 for eqsim_v04 Geometry file; 1 for Ward input file lines
	 */
	public General_EQSIM_Tools(ArrayList<String> lines, int formatType) {
		if(formatType == 1)
			rectElementsList = getElementsFromWardFileLines(lines);
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
		
		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelID);

		//Alphabetize:
		Collections.sort(allFaultSectionPrefData, new NamedObjectComparator());

		/*		  
		  // write sections IDs and names
		  for(int i=0; i< this.allFaultSectionPrefData.size();i++)
				System.out.println(allFaultSectionPrefData.get(i).getSectionId()+"\t"+allFaultSectionPrefData.get(i).getName());
		 */

		// remove those with no slip rate
		if (D)System.out.println("Removing the following due to NaN slip rate:");
		for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
			if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
				if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
				allFaultSectionPrefData.remove(i);
			}	 
		
		rectElementsList = new ArrayList<RectangularElement>();
		
		// Loop over sections and create the simulator elements
		int elementID =0;
		int numberAlongStrike = 0;
		int numberDownDip;
		int faultNumber = -1;
		int sectionNumber =0;
		double elementSlipRate=0;
		double elementStrength = Double.NaN;
		double elementStrike=0, elementDip=0, elementRake=0;
		String sectionName;
//		System.out.println("allFaultSectionPrefData.size() = "+allFaultSectionPrefData.size());
		for(int i=0;i<allFaultSectionPrefData.size();i++) {
//		for(int i=0;i<2;i++) {
			sectionNumber +=1;
			FaultSectionPrefData faultSectionPrefData = allFaultSectionPrefData.get(i);
			StirlingGriddedSurface surface = new StirlingGriddedSurface(faultSectionPrefData.getSimpleFaultData(aseisReducesArea), maxDiscretization, maxDiscretization);
			EvenlyGridCenteredSurface gridCenteredSurf = new EvenlyGridCenteredSurface(surface);
			double elementLength = gridCenteredSurf.getGridSpacingAlongStrike();
			double elementDDW = gridCenteredSurf.getGridSpacingDownDip(); // down dip width
			elementRake = faultSectionPrefData.getAveRake();
			elementSlipRate = faultSectionPrefData.getAveLongTermSlipRate()/1000;
			sectionName = faultSectionPrefData.getName();
			for(int col=0; col<gridCenteredSurf.getNumCols();col++) {
				numberAlongStrike += 1;
				for(int row=0; row<gridCenteredSurf.getNumRows();row++) {
					elementID +=1;
					numberDownDip = row+1;
					Location centerLoc = gridCenteredSurf.get(row, col);
					Location top1 = surface.get(row, col);
					Location top2 = surface.get(row, col+1);
					Location bot1 = surface.get(row+1, col);
//					Location bot2 = surface.get(row+1, col+1);
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
					
					Vertex[] vertices = new Vertex[4];
					vertices[0] = new Vertex(newTop1);
					vertices[1] = new Vertex(newBot1);
					vertices[2] = new Vertex(newBot2);
					vertices[3] = new Vertex(newTop2);
					
					FocalMechanism focalMech = new FocalMechanism(elementStrike, elementDip, elementRake);
										
					RectangularElement simSurface =
						new RectangularElement(elementID, vertices, sectionName,
								faultNumber, sectionNumber, numberAlongStrike, numberDownDip,
								elementSlipRate, elementStrength, focalMech);
					
					rectElementsList.add(simSurface);
					
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
		}
//		System.out.println("surfaces.size()="+rectElementsList.size());
	}
	
	
	/**
	 * this returns a list of elements created from the given lines from a Ward-format file
	 * @param lines
	 * @return
	 */
	public static ArrayList<RectangularElement> getElementsFromWardFileLines(ArrayList<String> lines) {
		ArrayList<RectangularElement> elements = new ArrayList<RectangularElement>();
		
		for (String line : lines) {
			if (line == null || line.length() == 0)
				continue;
			elements.add(new RectangularElement(line));
		}
		return elements;
	}
	
	
	public void writeToWardFile(String fileName)
	throws IOException {
		FileWriter efw = new FileWriter(fileName);
		for (RectangularElement surface : rectElementsList) {
			efw.write(surface.toWardFormatLine() + "\n");
		}
		efw.close();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime=System.currentTimeMillis();
		System.out.println("Starting");
		General_EQSIM_Tools test = new General_EQSIM_Tools(82, false, 4.0);
		test.getElementsList();
		int runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("This Run took "+runtime+" seconds");
	}
}
