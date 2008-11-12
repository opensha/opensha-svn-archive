package scratchJavaDevelopers.ned.rupsInFaultSystem;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.calc.RelativeLocation;

public class CreateRupturesFromSections {
	
	ArrayList<FaultSectionPrefData> allFaultSectionPrefData;
	double sectionDistances[][], sectionAngleDiffs[][];
	double maxTotStrikeChange = 60;
	String endPointNames[];
	int num_sections, numSectEndPts, counter;
	ArrayList<ArrayList> sectionConnectionsList, endToEndSectLinksList;

	
	CreateRupturesFromSections() {
		getAllSections();
	}
	
	
	
	private void getAllSections() {
		/** Set the deformation model
		 * D2.1 = 82
		 * D2.2 = 83
		 * D2.3 = 84
		 * D2.4 = 85
		 * D2.5 = 86
		 * D2.6 = 87
		 */
		int deformationModelId = 82;
		
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId);
/*		
		// remove those with no slip rate
		System.out.println("Removing the following due to NaN slip rate:");
		for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
			if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
				System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
				allFaultSectionPrefData.remove(i);
			}
*/	
//		System.out.println("size = "+allFaultSectionPrefData.size());
		
		num_sections = allFaultSectionPrefData.size();
		
//		for(int s=0;s<num_sections;s++) System.out.println(allFaultSectionPrefData.get(s).getFaultTrace().getStrikeDirection());

		
		numSectEndPts = 2*num_sections;
		sectionDistances = new double[numSectEndPts][numSectEndPts];
		sectionAngleDiffs = new double[numSectEndPts][numSectEndPts];
//		sectionAngleDiffs = new double[num_sections][num_sections];
		endPointNames = new String[numSectEndPts];
		
		// loop over first fault section (A)
		for(int a=0;a<allFaultSectionPrefData.size();a++) {
			FaultSectionPrefData dataA = allFaultSectionPrefData.get(a);
//if (dataA.getSectionName().equals("Burnt Mtn")) System.out.println("Burnt Mtn index ="+a);
			int indexA_firstPoint = 2*a;
			int indexA_lastPoint = indexA_firstPoint+1;
			endPointNames[indexA_firstPoint] = dataA.getSectionName() +" -- first";
			endPointNames[indexA_lastPoint] = dataA.getSectionName() +" -- last";
//			System.out.println(endPointNames[indexA_firstPoint]+"\t"+endPointNames[indexA_lastPoint]);
			// loop over second fault section (B)
			for(int b=0;b<allFaultSectionPrefData.size();b++) {
				FaultSectionPrefData dataB = allFaultSectionPrefData.get(b);
				int indexB_firstPoint = 2*b;
				int indexB_lastPoint = indexB_firstPoint+1;
				Location locA_1st = dataA.getFaultTrace().getLocationAt(0);
				Location locA_2nd = dataA.getFaultTrace().getLocationAt(dataA.getFaultTrace().size()-1);
				Location locB_1st = dataB.getFaultTrace().getLocationAt(0);
				Location locB_2nd = dataB.getFaultTrace().getLocationAt(dataB.getFaultTrace().size()-1);
				sectionDistances[indexA_firstPoint][indexB_firstPoint] = RelativeLocation.getApproxHorzDistance(locA_1st, locB_1st);
				sectionDistances[indexA_firstPoint][indexB_lastPoint] = RelativeLocation.getApproxHorzDistance(locA_1st, locB_2nd);
				sectionDistances[indexA_lastPoint][indexB_firstPoint] = RelativeLocation.getApproxHorzDistance(locA_2nd, locB_1st);
				sectionDistances[indexA_lastPoint][indexB_lastPoint] = RelativeLocation.getApproxHorzDistance(locA_2nd, locB_2nd);
				
				double dirA = dataA.getFaultTrace().getStrikeDirection();  // values are between -180 and 180
				double dirB = dataB.getFaultTrace().getStrikeDirection();
				
				sectionAngleDiffs[indexA_firstPoint][indexB_firstPoint] = getStrikeDirectionDifference(reverseAzimuth(dirA), dirB);
				sectionAngleDiffs[indexA_firstPoint][indexB_lastPoint] = getStrikeDirectionDifference(reverseAzimuth(dirA), reverseAzimuth(dirB));
				sectionAngleDiffs[indexA_lastPoint][indexB_firstPoint] = getStrikeDirectionDifference(dirA, dirB);
				sectionAngleDiffs[indexA_lastPoint][indexB_lastPoint] = getStrikeDirectionDifference(dirA, reverseAzimuth(dirB));

				//sectionAngleDiffs[a][b] = dataA.getFaultTrace().getStrikeDirectionDifference(dataB.getFaultTrace());
			
			}
		}
	}
	
	
	/**
	 * For each section endpoint, this creates a list of endpoints on other sections that are both within 
	 * the given distance and where the angle differences between sections is not larger than given.  This
	 * generates and ArrayList of ArrayLists.
	 * @param maxDist
	 * @param maxAngle
	 */
	public void computeConnectedSectionEndpointPairs(double maxDist, double maxAngle) {
		sectionConnectionsList = new ArrayList<ArrayList>();
		for(int i=0;i<numSectEndPts;i++) {
			ArrayList<Integer> sectionConnections = new ArrayList<Integer>();
			for(int j=0;j<numSectEndPts;j++)  // i+1 to avoid duplicates
				if(sectionDistances[i][j] <= maxDist && getSectionIndexForEndPoint(i) != getSectionIndexForEndPoint(j)) 
					if(sectionAngleDiffs[i][j] <= maxAngle && sectionAngleDiffs[i][j] >= -maxAngle) {
						sectionConnections.add(j);
					}
			sectionConnectionsList.add(sectionConnections);
/*
			System.out.print("\n"+i+"\t"+endPointNames[i]+"\thas "+sectionConnections.size()+"\t");
			for(int k=0; k<sectionConnections.size(); k++) System.out.print(sectionConnections.get(k)+",");
			System.out.print("\n");
*/
		}
	}
	
	
	   
	/**
	 * For each section, this adds neighboring sections as links unit until the ends are reached, at which 
	 * time this end-to-end link is saved if it doesn't already exist.  All branches are followed. For example,
	 * three sections in a "Y" shape would lead to two end-to-end links, and four sections in an "X" shape would 
	 * lead to four end-to-end links.  The result is put into endTEndSectLinksList, an ArrayList of ArrayList<Integer>
	 * objects (where the latter lists all the section end points in the list).
	 */
    private void computeEndToEndSectLinksList() {
    	endToEndSectLinksList  = new ArrayList<ArrayList>();
    	
    	System.out.println("sum_sections = "+num_sections);
    	
    	int oldNum=0;
        for(int sect=0; sect<num_sections; sect++) {
//          for(int sect=76; sect<77; sect++) {

  //      	System.out.println("sect = "+sect+"  "+allFaultSectionPrefData.get(sect).getSectionName());

//        	System.out.println("\n\nfirst path");
    		ArrayList<ArrayList> firstConnectionsList = new ArrayList<ArrayList>();
    		int startPtIndex1 = sect*2;
    		ArrayList<Integer> currentList1 = new ArrayList<Integer>();
    		currentList1.add(startPtIndex1);
    		addConnections(startPtIndex1, currentList1, firstConnectionsList);
//    		System.out.println("firstConnectionsList="+firstConnectionsList);
    		
//        	System.out.println("second path");
    		ArrayList<ArrayList> secondConnectionsList = new ArrayList<ArrayList>();
    		int startPtIndex2 = sect*2+1;
    		ArrayList<Integer> currentList2 = new ArrayList<Integer>();
    		currentList2.add(startPtIndex2);
    		addConnections(startPtIndex2, currentList2, secondConnectionsList);
//    		System.out.println("secondConnectionsList="+secondConnectionsList);
    		
    		// now stitch together all combinations
    		int num=0, numRemoved=0;
			for(int j=0; j<secondConnectionsList.size(); j++) {
				ArrayList<Integer> secondList = secondConnectionsList.get(j);
				for(int i=0; i<firstConnectionsList.size();i++) {
					num += 1;
					ArrayList<Integer> firstList = firstConnectionsList.get(i);
					ArrayList<Integer> stitchedList = new ArrayList<Integer>();
        			for(int k= firstList.size()-1; k >=0; k--) stitchedList.add(firstList.get(k));
    				stitchedList.addAll(secondList);
/* this works!
    				if(linkedSectionsList.contains(stitchedList))
    					System.out.println(num+" is a Duplicate Here !!!!!!!!!!!!!!!!!!!!");
*/
    				if(!endToEndSectLinksList.contains(stitchedList))
    					endToEndSectLinksList.add(stitchedList);
    				else
    					numRemoved += 1;
    			}
    		}
    		
    		

 		
			int numNew = endToEndSectLinksList.size()-oldNum;
			/*    
			System.out.println("\n"+numNew+" in list for "+
    				allFaultSectionPrefData.get(sect).getSectionName()+" (section "+sect+"), "+numRemoved+" removed as duplicates  ******************");
			for(int k=oldNum; k<endToEndSectLinksList.size(); k++) {
				System.out.println(k);
				ArrayList<Integer> list = endToEndSectLinksList.get(k);
				for(int l=0; l<list.size();l++)
					System.out.println(endPointNames[list.get(l)]);
			}
			*/
			oldNum = endToEndSectLinksList.size();

    	}
//        System.out.println("linkedSectionsList.size()="+linkedSectionsList.size());
    	
    }
    
    
    private void addConnections(Integer endPtID, ArrayList currentList, ArrayList<ArrayList> finalList) {
    	ArrayList<Integer> sectionConnections = sectionConnectionsList.get(endPtID);
    	
//    	System.out.println("Connections for "+endPtID+" ("+endPointNames[endPtID]+") are: ");
//    	for(int temp=0;temp<sectionConnections.size();temp++) System.out.println("\t"+sectionConnections.get(temp)+" ("+endPointNames[sectionConnections.get(temp).intValue()]+")");
    	
    	if(sectionConnections.size() == 0) {  // no more connections, so add it to the final list
    		finalList.add((ArrayList)currentList.clone());
//    		System.out.println("\tDONE");
    	}
    	else {
//    		System.out.println(sectionConnections.size()+" connections for "+this.endPointNames[endPtID]);
    		for(int i=0; i< sectionConnections.size(); i++) {
    			int connectedPtIndex = sectionConnections.get(i).intValue();
    			int otherPtIndex;
    			if(connectedPtIndex  % 2 != 0) // if the index is odd (last point)
    				otherPtIndex = connectedPtIndex-1;
    			else
    				otherPtIndex = connectedPtIndex+1;
    			
    			// check total direction change between 1st and this new section & skip if
    			int firstPt = ((Integer)currentList.get(0)).intValue();
    			double totalStrikeChange = Math.abs(sectionAngleDiffs[firstPt][connectedPtIndex]);
    			if(totalStrikeChange > maxTotStrikeChange) {
    				finalList.add((ArrayList)currentList.clone());
    				System.out.println("NOTE - total strike became too large between "+endPointNames[firstPt]+" and "+endPointNames[connectedPtIndex]);
    				continue;
    			}
    			
    			ArrayList newCurrentList = (ArrayList) currentList.clone();
    			newCurrentList.add(connectedPtIndex);
    			newCurrentList.add(otherPtIndex);
//    			System.out.println("\tadded "+endPointNames[otherPtIndex]+" and "+endPointNames[connectedPtIndex]+" to "+
//    					endPointNames[endPtID]+"  TOTAL STRIKE CHANGE = "+totalStrikeChange);
//    			counter+=1;
//    			if(counter<100)
    			addConnections(otherPtIndex, newCurrentList, finalList);
    		}
    	}
    }

	
	public int getSectionIndexForEndPoint(int endPointIndex) {
		if(endPointIndex % 2 != 0) return (endPointIndex-1)/2;  // test to see if it's odd
		else return endPointIndex/2;
	}
	
	
	// Note that this produces erroneous zig-zag plot for traces that have multiple lats for a given lon 
	// (functions force x values to monotonically increase)
	public void plotAllTraces(double maxDist, double minAngle, double maxAngle) {
		for(int i=0;i<numSectEndPts;i++)
			for(int j=0;j<numSectEndPts;j++)
				if(sectionDistances[i][j] <= maxDist && i != j) 
					if(sectionAngleDiffs[i][j] >= minAngle && sectionAngleDiffs[i][j] <= maxAngle) {
						FaultTrace ftA = this.allFaultSectionPrefData.get(getSectionIndexForEndPoint(i)).getFaultTrace();
						FaultTrace ftB = this.allFaultSectionPrefData.get(getSectionIndexForEndPoint(j)).getFaultTrace();
//						if (allFaultSectionPrefData.get(indexB).getSectionName().equals("Burnt Mtn")) System.out.println("Burnt Mtn index ="+allFaultSectionPrefData.get(indexB).getFaultTrace().toString());
//						boolean flag=false; if (allFaultSectionPrefData.get(indexB).getSectionName().equals("Burnt Mtn")) flag = true;
						plotTraces(ftA, ftB);
					}
	}
	
	
	// Note that this produces erroneous zig-zag plot for traces that have multiple lats for a given lon 
	// (functions force x values to monotonically increase)
	public void plotAllEndToEndLinks() {
		for(int i=0;i<this.endToEndSectLinksList.size();i++) {
			ArrayList<Integer> link = endToEndSectLinksList.get(i);
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			double minLat=1000, maxLat=-1000, minLon=1000, maxLon=-1000;
			String name = new String();
			for(int j=0; j<link.size(); j+=2) {
				int sectIndex = getSectionIndexForEndPoint(link.get(j).intValue());
				FaultTrace ft = allFaultSectionPrefData.get(sectIndex).getFaultTrace();
				name += allFaultSectionPrefData.get(sectIndex).getSectionName() +"+";
				for(int l=0; l<ft.size();l++) {
					Location loc = ft.getLocationAt(l);
					func.set(loc.getLongitude(), loc.getLatitude());
					if(loc.getLongitude()<minLon) minLon = loc.getLongitude();
					if(loc.getLongitude()>maxLon) maxLon = loc.getLongitude();
					if(loc.getLatitude()<minLat) minLat = loc.getLatitude();
					if(loc.getLatitude()>maxLat) maxLat = loc.getLatitude();
				}
			}
			func.setName(name);
			ArrayList funcs = new ArrayList();
			funcs.add(func);
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "");  
			// make the axis range equal
			if((maxLat-minLat) < (maxLon-minLon)) maxLat = minLat  + (maxLon-minLon);
			else maxLon = minLon + (maxLat-minLat);
			graph.setAxisRange(minLon, maxLon, minLat, maxLat);
			
		}
	}

	
	
	
	public void writeSectionDistances() {
		
		try{
			FileWriter fw = new FileWriter("/Users/field/workspace/OpenSHA/scratchJavaDevelopers/ned/rupsInFaultSystem/sectionDistances.txt");
			
			int num = numSectEndPts;
			String outputString = new String();
			outputString += "\t";
			for(int i=0;i<num;i++) outputString += endPointNames[i]+"\t";
			outputString += "\n";
			fw.write(outputString);
			/* */
			for(int i=0;i<num;i++) {
//				System.out.print(i);
				outputString = new String();
				outputString += endPointNames[i]+"\t";
				for(int j=0;j<num;j++) {
					String distString = new Double(Math.round(sectionDistances[i][j])).toString()+"\t";
					outputString += distString;
					if(sectionDistances[i][j] <= 5 && i != j) System.out.println(endPointNames[i]+"\t"+endPointNames[j]+"\t"+
							Math.round(sectionDistances[i][j])+"\t"+Math.round(sectionAngleDiffs[i][j]));
				}
				outputString += "\n";
				fw.write(outputString);
			}

			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
//		System.out.print(output);

	}
	
	
	public void plotTraces(FaultTrace ft1, FaultTrace ft2) {
		ArbitrarilyDiscretizedFunc ft1_func = new ArbitrarilyDiscretizedFunc();
		ft1_func.setName(ft1.getName());
		ArbitrarilyDiscretizedFunc ft2_func = new ArbitrarilyDiscretizedFunc();
		ft2_func.setName(ft2.getName());
		double minLat=1000, maxLat=-1000, minLon=1000, maxLon=-1000;
		for(int l=0; l<ft1.size();l++) {
			Location loc = ft1.getLocationAt(l);
			ft1_func.set(loc.getLongitude(), loc.getLatitude());
			if(loc.getLongitude()<minLon) minLon = loc.getLongitude();
			if(loc.getLongitude()>maxLon) maxLon = loc.getLongitude();
			if(loc.getLatitude()<minLat) minLat = loc.getLatitude();
			if(loc.getLatitude()>maxLat) maxLat = loc.getLatitude();
		}
		for(int l=0; l<ft2.size();l++) {
			Location loc = ft2.getLocationAt(l);
			ft2_func.set(loc.getLongitude(), loc.getLatitude());
			if(loc.getLongitude()<minLon) minLon = loc.getLongitude();
			if(loc.getLongitude()>maxLon) maxLon = loc.getLongitude();
			if(loc.getLatitude()<minLat) minLat = loc.getLatitude();
			if(loc.getLatitude()>maxLat) maxLat = loc.getLatitude();
		}
		ArrayList ft_funcs = new ArrayList();
		ft_funcs.add(ft1_func);
		ft_funcs.add(ft2_func);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(ft_funcs, "");  
		// make the axis range equal
		if((maxLat-minLat) < (maxLon-minLon)) maxLat = minLat  + (maxLon-minLon);
		else maxLon = minLon + (maxLat-minLat);
		sr_graph.setAxisRange(minLon, maxLon, minLat, maxLat);
	}
	
	
    /**
     * This returns the change in strike direction in going from this azimuth1 to azimuth2,
     * where these azimuths are assumed to be defined between -180 and 180 degrees.
     * The output is between -180 and 180 degrees.
     * @return
     */
    public double getStrikeDirectionDifference(double azimuth1, double azimuth2) {
    	double diff = azimuth2 - azimuth1;
    	if(diff>180)
    		return diff-360;
    	else if (diff<-180)
    		return diff+360;
    	else
    		return diff;
     }

    /**
     * This reverses the given azimuth (assumed to be between -180 and 180 degrees).
     * The output is between -180 and 180 degrees.
     * @return
     */

    public double reverseAzimuth(double azimuth) {
    	if(azimuth<0) return azimuth+180;
    	else return azimuth-180;
     }
    
    
 	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CreateRupturesFromSections createRups = new CreateRupturesFromSections();
		createRups.computeConnectedSectionEndpointPairs(5, 45);
		createRups.computeEndToEndSectLinksList();
//		createRups.plotAllEndToEndLinks();
//		createRups.writeBurntMtTrace();
//		createRups.plotAllTraces(5, -45, 45);
//		createRups.writeSectionDistances();
		
		

	}
	

}
