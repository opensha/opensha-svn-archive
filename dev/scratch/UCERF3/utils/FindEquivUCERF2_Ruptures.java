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
import java.util.ListIterator;

import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.SummedMagFreqDist;

/**
 * 
 * 
 * 
 * @author field
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
	
	MeanUCERF2 meanUCERF2;
	int NUM_UCERF2_SRC=289; // this is to exclude non-CA B faults

	SummedMagFreqDist mfdOfAssocRupsAndModMags;	// this is the mfd of the associated ruptures (including ave mag from mult rups)
	SummedMagFreqDist mfdOfAssocRupsWithOrigMags;	// this is the mfd of the associated ruptures (including orig mag from all rups)
	
	boolean[] ucerf2_rupUsed;
		
	/**
	 * This ignores the non-CA type B faults in UCERF2.
	 * This class only
	 * 
	 * @param faultSectionData
	 * @param precomputedDataDir
	 */
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
		else {	// compute section ends for each UCERF2 rupture
			findSectionEndsForUCERF2_Rups();
		}
		
//		tempWriteStuff();
	}
	
	public MeanUCERF2 getMeanUCERF2_Instance() {
		if(meanUCERF2 == null) {
			if(D) System.out.println("Instantiating UCERF2");
			meanUCERF2 = new MeanUCERF2();
			meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
			meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			meanUCERF2.getTimeSpan().setDuration(30.0);
			meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.CENTERED_DOWNDIP_FLOATER);
			meanUCERF2.updateForecast();
		}
		return meanUCERF2;
	}

	/**
	 * This is the same as in ERF_Calculator except the sources are cut off to exclude
	 * non-CA type B faults (using NUM_UCERF2_SRC).
	 * @return
	 */
	public SummedMagFreqDist getMFD_forNcal() {
		Region region = new CaliforniaRegions.RELM_NOCAL();
		MeanUCERF2 erf = getMeanUCERF2_Instance();
		SummedMagFreqDist magFreqDist = new SummedMagFreqDist(5.05,35,0.1);
		double duration = erf.getTimeSpan().getDuration();
		int rupIndex=-1;
		for (int s = 0; s < NUM_UCERF2_SRC; ++s) {
			ProbEqkSource source = erf.getSource(s);
			for (int r = 0; r < source.getNumRuptures(); ++r) {
				rupIndex += 1;
				ProbEqkRupture rupture = source.getRupture(r);
				double mag = rupture.getMag();
				double equivRate = rupture.getMeanAnnualRate(duration);
				EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
				double ptRate = equivRate/rupSurface.size();
				ListIterator<Location> it = rupSurface.getAllByRowsIterator();
				double fractInside = 0;
				while (it.hasNext()) {
					//discard the pt if outside the region 
					if (!region.contains(it.next()))
						continue;
					magFreqDist.addResampledMagRate(mag, ptRate, true);
					fractInside += 1.0;
				}
				fractInside /= rupSurface.size();
				if(fractInside > 0 && ucerf2_rupUsed[rupIndex] == false) {
					boolean subseismogenic = (rupSurface.getSurfaceWidth() < source.getSourceSurface().getSurfaceWidth());
					System.out.println(rupIndex+"\t"+s+"\t"+r+"\t"+(float)fractInside+"\t"+subseismogenic+"\t"+firstSectOfUCERF2_Rup[rupIndex]+
							"\t"+lastSectOfUCERF2_Rup[rupIndex]+"\t"+(float)magOfUCERF2_Rup[rupIndex]+"\t"+source.getName());					
				}
			}
			
//			firstSectOfUCERF2_Rup, lastSectOfUCERF2_Rup, srcIndexOfUCERF2_Rup, rupIndexOfUCERF2_Rup;
//			lengthOfUCERF2_Rup, rateOfUCERF2_Rup
		}
		magFreqDist.setName("N Cal MFD for UCERF2");
		magFreqDist.setInfo("this is for the N Cal RELM region, and excludes non-ca type B faults");
		return magFreqDist;
	}
	
	
	
	public ArrayList<SummedMagFreqDist> getMFDsForUCERF2AssocRups(){
		ArrayList<SummedMagFreqDist> mfds = new ArrayList<SummedMagFreqDist>();
		mfds.add(mfdOfAssocRupsAndModMags);
		mfds.add(mfdOfAssocRupsWithOrigMags);
		return mfds;
	}

	
	/**
	 * 
	 */
	private void findSectionEndsForUCERF2_Rups() {
				
		MeanUCERF2 meanUCERF2 = getMeanUCERF2_Instance();
		
		if(meanUCERF2.getNumSources() != 409)
			throw new RuntimeException("Error - wrong number of sources; some UCERF2 adj params not set correctly?");
		
		// Make the list of UCERF2 sources to consider -- not needed if all non-CA B faults are included!
		ArrayList<Integer> ucerf2_srcIndexList = new ArrayList<Integer>();
		
		// All but non-CA B fault sources
		if(D) System.out.println("Considering All but non-CA B fault sources");
		boolean[] srcNameEqualsParentSectName = new boolean[NUM_UCERF2_SRC];
		for(int i=0; i<NUM_UCERF2_SRC;i++) {
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
			boolean problemSource = false;
			ProbEqkSource src = meanUCERF2.getSource(ucerf2_srcIndexList.get(s));
			if (D) System.out.println("working on source "+src.getName()+" "+s+" of "+ucerf2_srcIndexList.size());
			double srcDDW = src.getSourceSurface().getSurfaceWidth();
			double totMoRate=0, partMoRate=0;
			boolean problemSrc = false;	// this will check whether any ruptures are sub-seismogenic
			for(int r=0; r<src.getNumRuptures(); r++){
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
					if(dist<sectHalfLength)  {
						// now keep only if both ends of section trace is less than 1/2 trace length away
						Location traceEnd1 = sectionTrace.get(0);
						Location traceEnd2 = sectionTrace.get(sectionTrace.size()-1);
						double dist1 =rupTrace.minDistToLocation(traceEnd1);
						double dist2 =rupTrace.minDistToLocation(traceEnd2);
						if(dist1 <sectHalfLength && dist2 <sectHalfLength)
							closeSections2.add(i);
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
	
	/*
	public void tempWriteStuff() {
		
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.getTimeSpan().setDuration(30.0);
		meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.CENTERED_DOWNDIP_FLOATER);
		meanUCERF2.updateForecast();
		
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+"tempInfo.txt";
		FileWriter fw;
		try {
			fw = new FileWriter(fullpathname);
			for(int i=0; i<NUM_RUPTURES;i++) {
				String srcName = meanUCERF2.getSource(srcIndexOfUCERF2_Rup[i]).getName();
				fw.write(srcIndexOfUCERF2_Rup[i]+"\t"+rupIndexOfUCERF2_Rup[i]+"\t"+lengthOfUCERF2_Rup[i]+
						"\t"+(float)magOfUCERF2_Rup[i]+"\t"+srcName+"\t"+(float)rateOfUCERF2_Rup[i]+firstSectOfUCERF2_Rup[i]+"\t"+lastSectOfUCERF2_Rup[i]+"\n");				
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	*/
	

	/**
	 * This returns the magnitude and rate of the equivalent UCERF2 ruptures by finding those that have
	 * the same first and last section index, and that have the minimum difference in rupture lengths
	 * (if more than one options exists).  If more than one UCERF2 rupture has the same minimum rupture length
	 * difference (as would come from the type-A segmented models where there are diff mags for the same char rup),
	 * then the total rate of these is returned with a magnitude that preserves the total moment rate.
	 * 
	 * This assumes that characteristic ruptures on Type A faults all have the exact same rup lengths (which I confirmed)
	 * 
	 * Problems - inversion rups that have same ends (multi pathing) get assigned twice
	 * 
	 * @param sectIndicesForRup
	 * @param rupLength
	 * @return - double[2] where mag is in the first element and rate is in the second
	 */
	private double[] OLDgetMagAndRateForRupture(ArrayList<Integer> sectIndicesForRup, double rupLength) {
		Integer firstSectIndex = sectIndicesForRup.get(0);
		Integer lastSectIndex = sectIndicesForRup.get(sectIndicesForRup.size()-1);
		ArrayList<Integer> viableUCERF2_Rups = new ArrayList<Integer>();
		for(int r=0; r<NUM_RUPTURES;r++) {
			if(firstSectOfUCERF2_Rup[r] == firstSectIndex && lastSectOfUCERF2_Rup[r] == lastSectIndex)
				viableUCERF2_Rups.add(r);
			// check for ends swapped between the two ruptures
			else if(firstSectOfUCERF2_Rup[r] == lastSectIndex && lastSectOfUCERF2_Rup[r] == firstSectIndex)
				viableUCERF2_Rups.add(r);			
		}

		if(viableUCERF2_Rups.size()==0) {
			return null;
		}
		else if(viableUCERF2_Rups.size()==1) {
			double[] result = new double[2];
			result[0]=magOfUCERF2_Rup[viableUCERF2_Rups.get(0)];
			result[1]=rateOfUCERF2_Rup[viableUCERF2_Rups.get(0)];
			return result;
		}
		else {
			// compute diffs in rupture lengths and find the minimum
			double[] lengthDiff = new double[viableUCERF2_Rups.size()];
			double minLengthDiff = Double.MAX_VALUE;
			for(int i=0;i<viableUCERF2_Rups.size();i++) {
				lengthDiff[i] = Math.abs(rupLength-lengthOfUCERF2_Rup[viableUCERF2_Rups.get(i)]);
				if(lengthDiff[i]<minLengthDiff)
					minLengthDiff=lengthDiff[i];
			}			
			// for all UCERF2 rups with lengthDiff[]=minLengthDiff, compute total rate and then get mag by moment-rate balancing
			double totRate=0;
			double totMoRate=0;
			for(int i=0;i<viableUCERF2_Rups.size();i++) {
				if(lengthDiff[i] == minLengthDiff) {
					totRate+=rateOfUCERF2_Rup[i];
					totMoRate+=rateOfUCERF2_Rup[i]*MomentMagCalc.getMoment(magOfUCERF2_Rup[i]);
				}
			}
			double aveMoment = totMoRate/totRate;
			double mag = MomentMagCalc.getMag(aveMoment);
			double[] result = new double[2];
			result[0]=mag;
			result[1]=totRate;
			return result;
		}
	}
	
	
	/**
	 * This returns the magnitude and rate of the equivalent UCERF2 ruptures by finding those that have
	 * the same first and last section index.  If more than one UCERF2 rupture has the same end sections
	 * (as would come from the type-A segmented models where there are diff mags for the same char rup),
	 * then the total rate of these is returned with a magnitude that preserves the total moment rate.
	 * 
	 * @param sectIndicesForRup
	 * @return - double[2] where mag is in the first element and rate is in the second
	 */
	private double[] getMagAndRateForRupture(ArrayList<Integer> sectIndicesForRup) {
		Integer firstSectIndex = sectIndicesForRup.get(0);
		Integer lastSectIndex = sectIndicesForRup.get(sectIndicesForRup.size()-1);
		ArrayList<Integer> equivUCERF2_Rups = new ArrayList<Integer>();
		for(int r=0; r<NUM_RUPTURES;r++) {
			if(firstSectOfUCERF2_Rup[r] == firstSectIndex && lastSectOfUCERF2_Rup[r] == lastSectIndex)
				equivUCERF2_Rups.add(r);
			// check for ends swapped between the two ruptures
			else if(firstSectOfUCERF2_Rup[r] == lastSectIndex && lastSectOfUCERF2_Rup[r] == firstSectIndex)
				equivUCERF2_Rups.add(r);			
		}

		if(equivUCERF2_Rups.size()==0) {
			return null;
		}
		else if(equivUCERF2_Rups.size()==1) {
			int r = equivUCERF2_Rups.get(0);
			if(ucerf2_rupUsed[r]) throw new RuntimeException("Error - UCERF2 rutpure already used");
			ucerf2_rupUsed[r] = true;
			double[] result = new double[2];
			result[0]=magOfUCERF2_Rup[r];
			result[1]=rateOfUCERF2_Rup[r];
			mfdOfAssocRupsAndModMags.addResampledMagRate(result[0], result[1], true);
			mfdOfAssocRupsWithOrigMags.addResampledMagRate(result[0], result[1], true);
			return result;
		}
		else {
			double totRate=0;
			double totMoRate=0;
			for(Integer r:equivUCERF2_Rups) {
				if(ucerf2_rupUsed[r]) throw new RuntimeException("Error - UCERF2 rutpure already used");
				ucerf2_rupUsed[r] = true;
				totRate+=rateOfUCERF2_Rup[r];
				totMoRate+=rateOfUCERF2_Rup[r]*MomentMagCalc.getMoment(magOfUCERF2_Rup[r]);
				mfdOfAssocRupsWithOrigMags.addResampledMagRate(magOfUCERF2_Rup[r], rateOfUCERF2_Rup[r], true);
			}
			double aveMoment = totMoRate/totRate;
			double mag = MomentMagCalc.getMag(aveMoment);
			double[] result = new double[2];
			result[0]=mag;
			result[1]=totRate;
			mfdOfAssocRupsAndModMags.addResampledMagRate(mag, totRate, true);
			return result;
		}
	}

	
	
	/**
	 * This returns an array list containing the UCERF2 equivalent mag and rate for each rupture 
	 * (or null if there is no corresponding UCERF2 rupture, which will usually be the case).
	 * The mag and rate are in the double[] object (at index 0 and 1, respectively)
	 * 
	 * If more than one inversion ruptures have the same end sections (multipathing), then the one with the minimum number 
	 * of sections get's the equivalent UCERF2 ruptures (and an exception is thrown if there are more than one inversion 
	 * rupture that have this same minimum number of sections)
	 * @param inversionRups
	 * @return
	 */
	public ArrayList<double[]> getMagsAndRatesForRuptures(ArrayList<ArrayList<Integer>> inversionRups) {

		ucerf2_rupUsed = new boolean[NUM_RUPTURES];
		for(int r=0;r<NUM_RUPTURES;r++) ucerf2_rupUsed[r] = false;
		
		mfdOfAssocRupsAndModMags = new SummedMagFreqDist(5.05,35,0.1);
		mfdOfAssocRupsAndModMags.setName("MFD for UCERF2 associated ruptures");
		mfdOfAssocRupsAndModMags.setInfo("using modified (average) mags");
		mfdOfAssocRupsWithOrigMags = new SummedMagFreqDist(5.05,35,0.1);
		mfdOfAssocRupsWithOrigMags.setName("MFD for UCERF2 associated ruptures");
		mfdOfAssocRupsWithOrigMags.setInfo("using original mags");


		int numInvRups = inversionRups.size();
		// first establish which ruptures to use for multi-path cases
		boolean[] rupChecked = new boolean[numInvRups];
		boolean[] ignoreRup = new boolean[numInvRups];
		for(int r=0;r<numInvRups;r++) {
			rupChecked[r] = false;
			ignoreRup[r] = false;
		}
		
		// 0 indicates not yet considered; 1 indicate it should be use; 2 indicates to not use because it's a multi-path duplicate
		for(int r=0;r<numInvRups;r++) {
			// if this rupture was already checkd because it was ina a multi-path list of a previous rupture
			if(rupChecked[r])
				continue;
			ArrayList<Integer> rup = inversionRups.get(r);
			int firstSectID = rup.get(0);
			int lastSectID = rup.get(rup.size()-1);
			ArrayList<Integer> multiPathRups = new ArrayList<Integer>();	// this will be the list of ruptures that share the same end sections
			multiPathRups.add(r);
			for(int r2=r+1;r2<inversionRups.size();r2++) {
				ArrayList<Integer> rup2 = inversionRups.get(r2);
				int firstSectID2 = rup2.get(0);
				int lastSectID2 = rup2.get(rup2.size()-1);
				if((firstSectID2 == firstSectID && lastSectID2 == lastSectID) || (firstSectID2 == lastSectID && lastSectID2 == firstSectID)) {
					multiPathRups.add(r2);
				}
			}

			if(multiPathRups.size()>1) {
				
				// find the minNumSections
				int minNumSect = Integer.MAX_VALUE;
				for(Integer i:multiPathRups)
					if(inversionRups.get(i).size()<minNumSect) 
						minNumSect=inversionRups.get(i).size();
				
				// check that only one rupture here has that minimum, and throw exception if not (since I don't know how to choose)
				int numWithMinNumSect =0;
				for(Integer i:multiPathRups)
					if(inversionRups.get(i).size() == minNumSect) numWithMinNumSect += 1;
				if(numWithMinNumSect != 1)
					throw new RuntimeException("Problem: two inversion ruptures (with same section ends) have the same min number of sections; this case is not supported");

				// now set what to do with these ruptures
				for(Integer i:multiPathRups) {
					rupChecked[i] = true;	// indicate that this rupture has already been considered for multi pathing (so it won't check this one again)
					if(inversionRups.get(i).size() != minNumSect) 
						ignoreRup[i] = true;	// take this rupture out of consideration (the one with the min stays with the default ignoreRup=false)
				}
			}
		}
		
		
		ArrayList<double[]> results = new ArrayList<double[]>();
		for(int r=0; r<numInvRups; r++) {
			if(!ignoreRup[r])
				results.add(getMagAndRateForRupture(inversionRups.get(r)));
			else
				results.add(null);
		}
		return results;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		FindEquivUCERF2_Ruptures test = new FindEquivUCERF2_Ruptures();

	}

}
