/**
 * 
 */
package scratch.UCERF3.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.FaultTrace;

/**
 * @author field
 *
 *
 *
 */
public class FindEquivUCERF2_Ruptures {
	
	protected final static boolean D = true;  // for debugging
	
	String DATA_FILE_NAME = "equivUCERF2_RupData";
	String INFO_FILE_NAME = "sectEndsForUCERF2_RupsResults_AllButNonCA_B";
	private File precomputedDataDir;
	File dataFile;
	
	final static int NUM_RUPTURES=11490;	// this was found after running this once
	
	ArrayList<FaultSectionPrefData> faultSectionData;
	
	// the following hold info about each UCERF2 rupture
	int[] firstSectOfUCERF2_Rup, lastSectOfUCERF2_Rup, srcIndexOfUCERF2_Rup, rupIndexOfUCERF2_Rup;
	double[] magOfUCERF2_Rup, lengthOfUCERF2_Rup, rateOfUCERF2_Rup;
	
	
		
	
	public FindEquivUCERF2_Ruptures(ArrayList<FaultSectionPrefData> faultSectionData) {
		this(faultSectionData, new File("dev/scratch/UCERF3/preComputedData/"));
	}
	
	
	public FindEquivUCERF2_Ruptures(ArrayList<FaultSectionPrefData> faultSectionData, File precomputedDataDir) {
		
		this.faultSectionData = faultSectionData;
		this.precomputedDataDir=precomputedDataDir;
		
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+DATA_FILE_NAME;
		dataFile = new File (fullpathname);

//		HanksBakun2002_MagAreaRel hb_magArea = new HanksBakun2002_MagAreaRel();
//		double m6pt5_width = Math.sqrt(hb_magArea.getMedianArea(6.55));
//		if(D) System.out.println("Square dimention of M 6.55 event: "+(float)m6pt5_width);
		
		// these are what we want
		firstSectOfUCERF2_Rup = new int[NUM_RUPTURES];
		lastSectOfUCERF2_Rup = new int[NUM_RUPTURES];
		srcIndexOfUCERF2_Rup = new int[NUM_RUPTURES];
		rupIndexOfUCERF2_Rup = new int[NUM_RUPTURES];;
		magOfUCERF2_Rup = new double[NUM_RUPTURES];;
		lengthOfUCERF2_Rup = new double[NUM_RUPTURES];;
		rateOfUCERF2_Rup = new double[NUM_RUPTURES];;

		// read from file if it exists
		if(dataFile.exists()) {
			if(D) System.out.println("Reading existing file: "+ fullpathname);
			try {
				// Wrap the FileInputStream with a DataInputStream
				FileInputStream file_input = new FileInputStream (dataFile);
				DataInputStream data_in    = new DataInputStream (file_input );
				for(int i=0; i<NUM_RUPTURES;i++) {
					firstSectOfUCERF2_Rup[i]=data_in.readInt();
					lastSectOfUCERF2_Rup[i]=data_in.readInt();
					srcIndexOfUCERF2_Rup[i]=data_in.readInt();
					rupIndexOfUCERF2_Rup[i]=data_in.readInt();
					magOfUCERF2_Rup[i]=data_in.readDouble();
					lengthOfUCERF2_Rup[i]=data_in.readDouble();
					rateOfUCERF2_Rup[i]=data_in.readDouble();
				}
				data_in.close ();
			} catch  (IOException e) {
				System.out.println ( "IO Exception =: " + e );
			}

		}
		else {
			
			findSectionEndsForUCERF2_Rups();
			
		}

	}


	
	/**
	 * 
	 */
	private void findSectionEndsForUCERF2_Rups() {
				
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.getTimeSpan().setDuration(30.0);
		meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.CENTERED_DOWNDIP_FLOATER);
		meanUCERF2.updateForecast();
		
		if(meanUCERF2.getNumSources() != 409)
			throw new RuntimeException("Error - wrong number of sources; some adj params not set correctly?");
		
		// Make the list of UCERF2 sources to consider -- not needed if all non-CA B faults are included!
		ArrayList<Integer> ucerf2_srcIndexList = new ArrayList<Integer>();
		
		// All but non-CA B fault sources
		if(D) System.out.println("Considering All but non-CA B fault sources");
		int numSrc=289; // this excludes non-CA B faults
		boolean[] srcNameEqualsParentSectName = new boolean[numSrc];
		for(int i=0; i<numSrc;i++) {
			ucerf2_srcIndexList.add(i);
			srcNameEqualsParentSectName[i]=true;
//			System.out.println(i+"\t"+meanUCERF2.getSource(i).getName());
		}
		
		// now set the cases where srcNameEqualsParentSectName[i]=false (hard coded; found "by hand")
		for(int i=0; i<131; i++) // these are the segmented and A-fault connected sources
			srcNameEqualsParentSectName[i]=false;
		srcNameEqualsParentSectName[154]=false;	// the following all have "Connected" in the names
		srcNameEqualsParentSectName[178]=false;
		srcNameEqualsParentSectName[179]=false;
		srcNameEqualsParentSectName[188]=false;
		srcNameEqualsParentSectName[202]=false;
		srcNameEqualsParentSectName[218]=false;
		srcNameEqualsParentSectName[219]=false;
		srcNameEqualsParentSectName[227]=false;
		srcNameEqualsParentSectName[232]=false;
		srcNameEqualsParentSectName[239]=false;
		srcNameEqualsParentSectName[256]=false;
		srcNameEqualsParentSectName[264]=false;
		srcNameEqualsParentSectName[265]=false;
		srcNameEqualsParentSectName[270]=false;
		srcNameEqualsParentSectName[273]=false;
		
//		for(int i=0; i<numSrc;i++)
//			System.out.println(i+"\t"+srcNameEqualsParentSectName[i]+"\t"+meanUCERF2.getSource(i).getName());

		
/*		
		// Bay area sources only
		if(D) System.out.println("Considering Bay-Area Sources Only");
		ucerf2_srcIndexList.add(0);		// 0  Calaveras;CC
		ucerf2_srcIndexList.add(1);		// 1  Calaveras;CC+CS
		ucerf2_srcIndexList.add(2);		// 2  Calaveras;CN
		ucerf2_srcIndexList.add(3);		// 3  Calaveras;CN+CC
		ucerf2_srcIndexList.add(4);		// 4  Calaveras;CN+CC+CS
		ucerf2_srcIndexList.add(5);		// 5  Calaveras;CS
		ucerf2_srcIndexList.add(27);	// 6  Hayward-Rodgers Creek;HN
		ucerf2_srcIndexList.add(28);	// 7  Hayward-Rodgers Creek;HN+HS
		ucerf2_srcIndexList.add(29);	// 8  Hayward-Rodgers Creek;HS
		ucerf2_srcIndexList.add(30);	// 9  Hayward-Rodgers Creek;RC
		ucerf2_srcIndexList.add(31);	// 10 Hayward-Rodgers Creek;RC+HN
		ucerf2_srcIndexList.add(32);	// 11 Hayward-Rodgers Creek;RC+HN+HS
		ucerf2_srcIndexList.add(33);	// 12 N. San Andreas;SAN
		ucerf2_srcIndexList.add(34);	// 13 N. San Andreas;SAN+SAP
		ucerf2_srcIndexList.add(35);	// 14 N. San Andreas;SAN+SAP+SAS
		ucerf2_srcIndexList.add(36);	// 15 N. San Andreas;SAO
		ucerf2_srcIndexList.add(37);	// 16 N. San Andreas;SAO+SAN
		ucerf2_srcIndexList.add(38);	// 17 N. San Andreas;SAO+SAN+SAP
		ucerf2_srcIndexList.add(39);	// 18 N. San Andreas;SAO+SAN+SAP+SAS
		ucerf2_srcIndexList.add(40);	// 19 N. San Andreas;SAP
		ucerf2_srcIndexList.add(41);	// 20 N. San Andreas;SAP+SAS
		ucerf2_srcIndexList.add(42);	// 21 N. San Andreas;SAS
		ucerf2_srcIndexList.add(123);	// 22 Calaveras
		ucerf2_srcIndexList.add(126);	// 23 Hayward-Rodgers Creek
		ucerf2_srcIndexList.add(127);	// 24 N. San Andreas
		ucerf2_srcIndexList.add(178);	// 25 Green Valley Connected
		ucerf2_srcIndexList.add(179);	// 26 Greenville Connected
		ucerf2_srcIndexList.add(214);	// 27 Mount Diablo Thrust
		ucerf2_srcIndexList.add(256);	// 28 San Gregorio Connected
*/
			
		int numUCERF2_Ruptures = 0;
		for(int s=0; s<ucerf2_srcIndexList.size(); s++){
			numUCERF2_Ruptures += meanUCERF2.getSource(ucerf2_srcIndexList.get(s)).getNumRuptures();
		}
		if(numUCERF2_Ruptures != NUM_RUPTURES)
			throw new RuntimeException("problem with NUM_RUPTURES; something changed?");
		
		
		if(D) System.out.println("Num UCERF2 Sources to Consider = "+ucerf2_srcIndexList.size());
		if(D) System.out.println("Num UCERF2 Ruptues to Consider = "+NUM_RUPTURES);
		
		// initialize to bogus indices
		for(int r=0;r<NUM_RUPTURES;r++) {
			firstSectOfUCERF2_Rup[r]=-1;
			lastSectOfUCERF2_Rup[r]=-1;
		}

		
		
		
		ArrayList<String> resultsString = new ArrayList<String>();
		ArrayList<String> problemSourceList = new ArrayList<String>();
		
		int rupIndex = -1;
		for(int s=0; s<ucerf2_srcIndexList.size(); s++){
//		for(int s=133; s<134; s++){
			boolean problemSource = false;
			ProbEqkSource src = meanUCERF2.getSource(ucerf2_srcIndexList.get(s));
//			ProbEqkSource src = meanUCERF2.getSource(s);
			if (D) System.out.println("working on source "+src.getName()+" "+s+" of "+ucerf2_srcIndexList.size());
			double srcDDW = src.getSourceSurface().getSurfaceWidth();
			double totMoRate=0, partMoRate=0;
			boolean problemSrc = false;	// this will check whether any ruptures are sub-seismogenic
			for(int r=0; r<src.getNumRuptures(); r++){
//			for(int r=3; r<4; r++){
				rupIndex += 1;
				ProbEqkRupture rup = src.getRupture(r);
				double ddw = rup.getRuptureSurface().getSurfaceWidth();
				double len = rup.getRuptureSurface().getSurfaceLength();
				double mag = ((int)(rup.getMag()*100.0))/100.0;	// nice value for writing
				totMoRate += MomentMagCalc.getMoment(rup.getMag())*rup.getMeanAnnualRate(30.0);
				srcIndexOfUCERF2_Rup[rupIndex] = s;
				rupIndexOfUCERF2_Rup[rupIndex] = r;;
				magOfUCERF2_Rup[rupIndex] = rup.getMag();
				lengthOfUCERF2_Rup[rupIndex] = len;
				rateOfUCERF2_Rup[rupIndex] = rup.getMeanAnnualRate(30.0);

				if(ddw < srcDDW) {
					problemSource = true;
					String errorString = rupIndex+":\t"+"Sub-Seismogenic Rupture:  ddw="+ddw+"\t"+srcDDW+"\t"+len+"\t"+(float)mag+"\tiRup="+r+"\tiSrc="+s+"\t("+src.getName()+")\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
					partMoRate += MomentMagCalc.getMoment(rup.getMag())*rup.getMeanAnnualRate(30.0);
					problemSrc = true;
					continue;
				}
				FaultTrace rupTrace = rup.getRuptureSurface().getRowAsTrace(0);

				Location rupEndLoc1 = rupTrace.get(0);
				Location rupEndLoc2 = rupTrace.get(rupTrace.size()-1);
				ArrayList<Integer> closeSections1 = new ArrayList<Integer>();
				ArrayList<Integer> closeSections2 = new ArrayList<Integer>();
				double dist;
				for(int i=0; i<faultSectionData.size(); i++) {
					
					// skip if section's parent name should equal source name
					if(srcNameEqualsParentSectName[s])
						if(!faultSectionData.get(i).getParentSectionName().equals(src.getName()))
							continue;
					
					FaultTrace sectionTrace = faultSectionData.get(i).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
					double sectHalfLength = 0.5*sectionTrace.getTraceLength()+0.5; 
					// Process first end of rupture
					dist = sectionTrace.minDistToLocation(rupEndLoc1);
					if(dist<sectHalfLength)  {
						// now keep only if both ends of section trace is less than 1/2 trace length away
						Location traceEnd1 = sectionTrace.get(0);
						Location traceEnd2 = sectionTrace.get(sectionTrace.size()-1);
						double dist1 =rupTrace.minDistToLocation(traceEnd1);
						double dist2 =rupTrace.minDistToLocation(traceEnd2);
						if(dist1 <sectHalfLength && dist2 <sectHalfLength)
							closeSections1.add(i);
					}
					// Process second end of rupture
					dist = sectionTrace.minDistToLocation(rupEndLoc2);
//if(i==12)
	//System.out.println("HERE:i=12\t"+dist);

					if(dist<sectHalfLength)  {
						// now keep only if both ends of section trace is less than 1/2 trace length away
						Location traceEnd1 = sectionTrace.get(0);
						Location traceEnd2 = sectionTrace.get(sectionTrace.size()-1);
						double dist1 =rupTrace.minDistToLocation(traceEnd1);
						double dist2 =rupTrace.minDistToLocation(traceEnd2);
						if(dist1 <sectHalfLength && dist2 <sectHalfLength)
							closeSections2.add(i);
//System.out.println("HERE:\t"+dist+"\t"+dist1+"\t"+dist2+"\t"+sectHalfLength+"\t"+i);
					}

				}
				
				// For cases that have 2 close sections, choose whichever is closest: 1) farthest point on closest section;or 2) nearest point
				// of farthest section (where distance of section is the ave of the distances of the ends to the end of rupture)
				// this breaks down for closely space sections (like Fickle Hill & Mad River)
				if(closeSections1.size() == 2) {
					FaultTrace sect1_trace = faultSectionData.get(closeSections1.get(0)).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
					FaultTrace sect2_trace = faultSectionData.get(closeSections1.get(1)).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
					double dist_tr1_end1=LocationUtils.horzDistanceFast(rupEndLoc1, sect1_trace.get(0));
					double dist_tr1_end2=LocationUtils.horzDistanceFast(rupEndLoc1, sect1_trace.get(sect1_trace.size()-1));
					double aveDistTr1 =  (dist_tr1_end1+dist_tr1_end2)/2;
					double dist_tr2_end1=LocationUtils.horzDistanceFast(rupEndLoc1, sect2_trace.get(0));
					double dist_tr2_end2=LocationUtils.horzDistanceFast(rupEndLoc1, sect2_trace.get(sect2_trace.size()-1));
					double aveDistTr2 =  (dist_tr2_end1+dist_tr2_end2)/2;
//					int closeSectID, farSectID;
					double farEndOfCloseTr, closeEndOfFarTr;
					if(aveDistTr1<aveDistTr2) {  // trace 1 is closer
						if(dist_tr1_end1>dist_tr1_end2)
							farEndOfCloseTr=dist_tr1_end1;
						else
							farEndOfCloseTr=dist_tr1_end2;
						if(dist_tr2_end1<dist_tr2_end2)
							closeEndOfFarTr=dist_tr2_end1;
						else
							closeEndOfFarTr=dist_tr2_end2;
						if(farEndOfCloseTr<closeEndOfFarTr) // remove far trace from list, which is trace 2 (index 1)
							closeSections1.remove(1);
						else
							closeSections1.remove(0);
					}
					else {						// trace 2 is closer
						if(dist_tr2_end1>dist_tr2_end2)
							farEndOfCloseTr=dist_tr2_end1;
						else
							farEndOfCloseTr=dist_tr2_end2;
						if(dist_tr1_end1<dist_tr1_end2)
							closeEndOfFarTr=dist_tr1_end1;
						else
							closeEndOfFarTr=dist_tr1_end2;
						if(farEndOfCloseTr<closeEndOfFarTr) // remove far trace from list, which is trace 1 (index 0)
							closeSections1.remove(0);
						else
							closeSections1.remove(1);
					}
						
				}
				if(closeSections2.size() == 2) {
					FaultTrace sect1_trace = faultSectionData.get(closeSections2.get(0)).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
					FaultTrace sect2_trace = faultSectionData.get(closeSections2.get(1)).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
					double dist_tr1_end1=LocationUtils.horzDistanceFast(rupEndLoc1, sect1_trace.get(0));
					double dist_tr1_end2=LocationUtils.horzDistanceFast(rupEndLoc1, sect1_trace.get(sect1_trace.size()-1));
					double aveDistTr1 =  (dist_tr1_end1+dist_tr1_end2)/2;
					double dist_tr2_end1=LocationUtils.horzDistanceFast(rupEndLoc1, sect2_trace.get(0));
					double dist_tr2_end2=LocationUtils.horzDistanceFast(rupEndLoc1, sect2_trace.get(sect2_trace.size()-1));
					double aveDistTr2 =  (dist_tr2_end1+dist_tr2_end2)/2;
//					int closeSectID, farSectID;
					double farEndOfCloseTr, closeEndOfFarTr;
					if(aveDistTr1<aveDistTr2) {  // trace 1 is closer
						if(dist_tr1_end1>dist_tr1_end2)
							farEndOfCloseTr=dist_tr1_end1;
						else
							farEndOfCloseTr=dist_tr1_end2;
						if(dist_tr2_end1<dist_tr2_end2)
							closeEndOfFarTr=dist_tr2_end1;
						else
							closeEndOfFarTr=dist_tr2_end2;
						if(farEndOfCloseTr<closeEndOfFarTr) // remove far trace from list, which is trace 2 (index 1)
							closeSections2.remove(1);
						else
							closeSections2.remove(0);
					}
					else {						// trace 2 is closer
						if(dist_tr2_end1>dist_tr2_end2)
							farEndOfCloseTr=dist_tr2_end1;
						else
							farEndOfCloseTr=dist_tr2_end2;
						if(dist_tr1_end1<dist_tr1_end2)
							closeEndOfFarTr=dist_tr1_end1;
						else
							closeEndOfFarTr=dist_tr1_end2;
						if(farEndOfCloseTr<closeEndOfFarTr) // remove far trace from list, which is trace 1 (index 0)
							closeSections2.remove(0);
						else
							closeSections2.remove(1);
					}
						
				}

				String sectName1 = "None Found";
				String sectName2 = "None Found";
				
				if(closeSections1.size() == 1) {
					firstSectOfUCERF2_Rup[rupIndex]=closeSections1.get(0);
					sectName1=faultSectionData.get(firstSectOfUCERF2_Rup[rupIndex]).getSectionName();
				}
				else if(closeSections1.size() > 1){
					problemSource = true;
					String errorString = "Error - end1 num found not 1, but "+closeSections1.size() + " -- for rup "+r+
										 " of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+"); Sections:";
					for(int sect:closeSections1)
						errorString += "\t"+faultSectionData.get(sect).getName();
					errorString += "\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
				}
				else { // if zero found
					problemSource = true;
					String end="S";
					if(rupEndLoc1.getLatitude()>rupEndLoc2.getLatitude())
						end = "N";
					String errorString = "Error - "+end+"-end num found not 1, but "+closeSections1.size() +
					" -- for rup "+r+" of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+")\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
				}

				
				if(closeSections2.size() == 1) {
					lastSectOfUCERF2_Rup[rupIndex]=closeSections2.get(0);
					sectName2=faultSectionData.get(lastSectOfUCERF2_Rup[rupIndex]).getSectionName();
				}
				else if(closeSections2.size() > 1){
					problemSource = true;
					String errorString = "Error - end2 num found not 1, but "+closeSections2.size() + " -- for rup "+r+
										 " of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+"); Sections:";
					for(int sect:closeSections2)
						errorString += "\t"+faultSectionData.get(sect).getName();
					errorString += "\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
				}
				else { // if zero found
					problemSource = true;
					String end="S";
					if(rupEndLoc2.getLatitude()>rupEndLoc1.getLatitude())
						end = "N";
					String errorString = "Error - "+end+"-end num found not 1, but "+closeSections2.size() +
					" -- for rup "+r+" of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+")\n";
					if(D) System.out.print(errorString);	
					resultsString.add(errorString);
				}
				

				String result = rupIndex+":\t"+firstSectOfUCERF2_Rup[rupIndex]+"\t"+lastSectOfUCERF2_Rup[rupIndex]+"\t("+sectName1+"   &  "+
								sectName2+")  are the Sections at ends of rup "+ r+" of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+")\n";
//				if(D) System.out.print(result);
				resultsString.add(result);
				
				if(problemSource && !problemSourceList.contains(src.getName()))
					problemSourceList.add(src.getName());


//				if(D) System.out.println("Sections at ends of rup "+r+" of src "+s+", M="+(float)rup.getMag()+", ("+
//						src.getName()+") are: "+firstSectOfUCERF2_Rup[rupIndex]+" ("+sectName1+")  & "+lastSectOfUCERF2_Rup[rupIndex]+" ("+sectName2+")");
			}
			String infoString = (float)(partMoRate/totMoRate) +" is the fract MoRate below for "+src.getName();
			if(problemSrc) {
				System.out.println(infoString);
				resultsString.add(infoString+"\n");
			}
		}
		
		// write info results
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+INFO_FILE_NAME;
		FileWriter fw;
		try {
			fw = new FileWriter(fullpathname);
			for(String line:resultsString)
				fw.write(line);
			fw.write("Problem Sources:\n");
			for(String line:problemSourceList)
				fw.write(line+"\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		// write out the data
		try {
			FileOutputStream file_output = new FileOutputStream (dataFile);
			// Wrap the FileOutputStream with a DataOutputStream
			DataOutputStream data_out = new DataOutputStream (file_output);
			for(int i=0; i<NUM_RUPTURES;i++) {
				data_out.writeInt(firstSectOfUCERF2_Rup[i]);
				data_out.writeInt(lastSectOfUCERF2_Rup[i]);
				data_out.writeInt(srcIndexOfUCERF2_Rup[i]);
				data_out.writeInt(rupIndexOfUCERF2_Rup[i]);
				data_out.writeDouble(magOfUCERF2_Rup[i]);
				data_out.writeDouble(lengthOfUCERF2_Rup[i]);
				data_out.writeDouble(rateOfUCERF2_Rup[i]);
			}
			file_output.close ();
		}
		catch (IOException e) {
			System.out.println ("IO exception = " + e );
		}


		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		FindEquivUCERF2_Ruptures test = new FindEquivUCERF2_Ruptures();

	}

}
