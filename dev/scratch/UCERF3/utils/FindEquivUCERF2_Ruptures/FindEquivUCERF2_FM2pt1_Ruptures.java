/**
 * 
 */
package scratch.UCERF3.utils.FindEquivUCERF2_Ruptures;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;
import scratch.UCERF3.utils.DeformationModelFetcher;
import sun.tools.tree.ThisExpression;

import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Strings;

/**
 * This class associates UCERF2 ruptures with inversion ruptures.  That is, for a given inversion rupture,
 * this will give a total rate and average magnitude (preserving moment rate) from UCERF2 for that rupture
 * (an average in case more than one UCERF2 associates with a given inversion rupture).  Right now 
 * the only faults exhibiting problems are those that have sub-seismogenic ruptures (see the last lines of
 * the info file: sectEndsForUCERF2_RupsResults_AllButNonCA_B.txt)
 * 
 * Important Notes:
 * 
 * 1) This uses only Fault Model 2.1 (Deformation Models 2.1, 2.2, and 2.3), and this is modified to take
 *    the overlapping sections on the San Jacinto and Elsinore step-overs out (and replace with the 
 *    combined section); see DeformationModelFetcher.createAll_UCERF2_SubSections for details.
 * 2) UCERF2 ruptures that are sub-seimogenic are not associated (since there is no meaningful mapping)
 * 3) Most of the inversion ruptures have no UCERF2 association (I think); these are null.
 * 4) This ignores the non-CA type B faults in UCERF2 (since they are not included in the current inversion).
 * 5) This reads from or writes to some pre-computed data files; these must be deleted if inputs change as
 *    noted in the constructor below.
 * 6) This uses a special version of MeanUCERF2 (that computes floating rupture areas as the ave of HB and EllB)
 * 7) Note that the Mendocino section was not used in UCERF2
 * 
 * 
 * This class also compute various MFDs.
 * 
 * @author field
 */
public class FindEquivUCERF2_FM2pt1_Ruptures {
	
	protected final static boolean D = true;  // for debugging
	
	ArrayList<ArrayList<String>> parentSectionNamesForUCERF2_Sources;
	int[] faultModelForSource;
	
	ArrayList<double[]> magsAndRatesForRuptures;
	
	final static int NUM_UCERF2_RUPTURES=12463;	// this was found after running this once
	final static int NUM_SECTIONS=1593;		// including the SAF Creeping Section
	static ModMeanUCERF2 meanUCERF2;	// note that this is a special version (see notes above)
	final static int NUM_UCERF2_SRC_TO_USE=289; // this is to exclude non-CA B faults
	final static int NUM_UCERF2_SRC = 409;
	
	int NUM_INVERSION_RUPTURES;
	
	String DATA_FILE_NAME = "equivUCERF2_RupDataAllCal";
	String INFO_FILE_PATH_NAME = "dev/scratch/UCERF3/utils/FindEquivUCERF2_Ruptures/sectEndsInfoForUCERF2_Rups_AllButNonCA_B.txt";
	String SECT_FOR_UCERF2_SRC_FILE_PATH_NAME = "dev/scratch/UCERF3/utils/FindEquivUCERF2_Ruptures/SectionsForUCERF2_Sources.txt";
	File dataFile;
	FileWriter info_fw;
	
	List<FaultSectionPrefData> faultSectionData;
	
	// the following hold info about each UCERF2 rupture
	int[] firstSectOfUCERF2_Rup, lastSectOfUCERF2_Rup, srcIndexOfUCERF2_Rup, rupIndexOfUCERF2_Rup, invRupIndexForUCERF2_Rup;
	double[] magOfUCERF2_Rup, lengthOfUCERF2_Rup, rateOfUCERF2_Rup;
	boolean[] subSeismoUCERF2_Rup;
	
	// the following lists the indices of all  UCERF2 ruptures associated with each inversion rupture
	ArrayList<ArrayList<Integer>> rupAssociationList;
	
	SummedMagFreqDist mfdOfAssocRupsAndModMags;		// this is the mfd of the associated ruptures (including ave mag from mult rups)
	SummedMagFreqDist mfdOfAssocRupsWithOrigMags;	// this is the mfd of the associated ruptures (including orig mag from all rups)
	
	IncrementalMagFreqDist nCal_UCERF2_BackgrMFD_WithAftShocks;
	IncrementalMagFreqDist nCalTargetMinusBackground;
	GutenbergRichterMagFreqDist nCalTotalTargetGR_MFD;
	
//	boolean[] ucerf2_rupUsed;
	
		
	/**
	 * See important notes given above for this class.
	 * 
	 * Note that the files saved/read here in precomputedDataDir (DATA_FILE_NAME) ???????? plus others?  should be 
	 * deleted any time the contents of faultSysRupSet change.
	 * 
	 * @param faultSysRupSet
	 * @param precomputedDataDir
	 */
	public FindEquivUCERF2_FM2pt1_Ruptures(SimpleFaultSystemRupSet faultSysRupSet, File precomputedDataDir) {
		
		this.faultSectionData = faultSysRupSet.getFaultSectionDataList();
		
		// Make sure the number of sections in the inversion hasn't changed (a weak test)
		if(faultSectionData.size() != NUM_SECTIONS)
			throw new RuntimeException("Error: Number of sections changed"+
					NUM_SECTIONS+" sections; this run has "+faultSectionData.size()+")");
		
		// Make the following a test
		NUM_INVERSION_RUPTURES = faultSysRupSet.getNumRuptures();
		System.out.println("NUM_INVERSION_RUPTURES = " +NUM_INVERSION_RUPTURES);
		
		// these are what we want to fill in here
		firstSectOfUCERF2_Rup = new int[NUM_UCERF2_RUPTURES];	// contains -1 if no association
		lastSectOfUCERF2_Rup = new int[NUM_UCERF2_RUPTURES];	// contains -1 if no association
		srcIndexOfUCERF2_Rup = new int[NUM_UCERF2_RUPTURES];
		rupIndexOfUCERF2_Rup = new int[NUM_UCERF2_RUPTURES];
		magOfUCERF2_Rup = new double[NUM_UCERF2_RUPTURES];
		lengthOfUCERF2_Rup = new double[NUM_UCERF2_RUPTURES];
		rateOfUCERF2_Rup = new double[NUM_UCERF2_RUPTURES];
		subSeismoUCERF2_Rup = new boolean[NUM_UCERF2_RUPTURES]; 
		invRupIndexForUCERF2_Rup = new int[NUM_UCERF2_RUPTURES];


		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+DATA_FILE_NAME;
		dataFile = new File (fullpathname);
		// read from file if it exists
		if(dataFile.exists()) {
			if(D) System.out.println("Reading existing file: "+ DATA_FILE_NAME);
			try {
				// Wrap the FileInputStream with a DataInputStream
				FileInputStream file_input = new FileInputStream (dataFile);
				DataInputStream data_in    = new DataInputStream (file_input );
				for(int i=0; i<NUM_UCERF2_RUPTURES;i++) {
					firstSectOfUCERF2_Rup[i]=data_in.readInt();
					lastSectOfUCERF2_Rup[i]=data_in.readInt();
					srcIndexOfUCERF2_Rup[i]=data_in.readInt();
					rupIndexOfUCERF2_Rup[i]=data_in.readInt();
					magOfUCERF2_Rup[i]=data_in.readDouble();
					lengthOfUCERF2_Rup[i]=data_in.readDouble();
					rateOfUCERF2_Rup[i]=data_in.readDouble();
					subSeismoUCERF2_Rup[i]=data_in.readBoolean();
					invRupIndexForUCERF2_Rup[i]=data_in.readInt();
				}
				data_in.close ();
				if(D) System.out.println("Done reading file:"+DATA_FILE_NAME);
			} catch  (IOException e) {
				System.out.println ( "IO Exception =: " + e );
			}
			
			// make the rupAssociationList from invRupIndexForUCERF2_Rup
			rupAssociationList = new ArrayList<ArrayList<Integer>>();
			for(int ir=0; ir<NUM_INVERSION_RUPTURES;ir++)
				rupAssociationList.add(new ArrayList<Integer>());  // add a list of UCERF2 ruptures associated with this inversion rupture (ir)
			for(int ur=0;ur<invRupIndexForUCERF2_Rup.length;ur++) // loop over all UCERF2 ruptures
				if(invRupIndexForUCERF2_Rup[ur] != -1)
					rupAssociationList.get(invRupIndexForUCERF2_Rup[ur]).add(ur);
		}
		else {	// compute things from scratch
			
			readSectionsForUCERF2_SourcesFile();
			
			// do the following methods writing to the info file
			try {
				info_fw = new FileWriter(INFO_FILE_PATH_NAME);
				findSectionEndsForUCERF2_Rups();
				findAssociations(faultSysRupSet.getSectionIndicesForAllRups());
				info_fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			writePreComputedDataFile();
		}	

		computeMagsAndRatesForAllRuptures();
	}
	
	
	/**
	 * This generates the UCERF2 instance used here (for a specific set of adjustable params).
	 * @return
	 */
	public static ModMeanUCERF2 getMeanUCERF2_Instance() {
		if(meanUCERF2 == null) {
			if(D) System.out.println("Instantiating UCERF2");
			meanUCERF2 = new ModMeanUCERF2();
			meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
			meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			meanUCERF2.getTimeSpan().setDuration(30.0);
			meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.CENTERED_DOWNDIP_FLOATER);
			meanUCERF2.updateForecast();
			if(D) System.out.println("Done Instantiating UCERF2");
			
			// the following is a weak test to make sure nothering in UCERF2 has changed
			if(meanUCERF2.getNumSources() != 409)
				throw new RuntimeException("Error - wrong number of sources; some UCERF2 adj params not set correctly?");

			// another weak test to make sure nothing has changed
			int numUCERF2_Ruptures = 0;
			for(int s=0; s<NUM_UCERF2_SRC_TO_USE; s++){
				numUCERF2_Ruptures += meanUCERF2.getSource(s).getNumRuptures();
			}
			if(numUCERF2_Ruptures != NUM_UCERF2_RUPTURES)
				throw new RuntimeException("problem with NUM_RUPTURES; something changed?  old="+NUM_UCERF2_RUPTURES+"\tnew="+numUCERF2_Ruptures);
		}

		return meanUCERF2;
	}
	


	
	/**
	 * This method computes the following data: 
	 * 
	 * 	firstSectOfUCERF2_Rup, 
	 * 	lastSectOfUCERF2_Rup, 
	 * 	srcIndexOfUCERF2_Rup, 
	 * 	rupIndexOfUCERF2_Rup, 
	 * 	magOfUCERF2_Rup, 
	 * 	lengthOfUCERF2_Rup,
	 * 	rateOfUCERF2_Rup,
	 *  subSeismoUCERF2_Rup,
	 * 
	 * This also saves an info file (INFO_FILE_NAME) that gives the status of associations for each 
	 * UCERF2 rupture (and the problem sources at the end, all of which are only due to sub-seismogenic
	 * ruptures).
	 */
	private void findSectionEndsForUCERF2_Rups() {
		
		// Note that we're considering all but non-CA B fault sources
		if(D) {
			System.out.println("Considering All but non-CA B fault sources");
			System.out.println("Num UCERF2 Sources to Consider = "+NUM_UCERF2_SRC_TO_USE);
			if(D) System.out.println("Num UCERF2 Ruptues to Consider = "+NUM_UCERF2_RUPTURES);
		}
		
		// initialize the following to bogus indices (the default)
		for(int r=0;r<NUM_UCERF2_RUPTURES;r++) {
			firstSectOfUCERF2_Rup[r]=-1;
			lastSectOfUCERF2_Rup[r]=-1;
		}
		
		ArrayList<String> resultsString = new ArrayList<String>();
		ArrayList<String> problemSourceList = new ArrayList<String>();
		ArrayList<String> subseismoRateString = new ArrayList<String> ();
		
		int rupIndex = -1;
		for(int s=0; s<NUM_UCERF2_SRC_TO_USE; s++){
			boolean problemSource = false;				// this will indicate that source has some problem
			boolean srcHasSubSeismogenicRups = false;	// this will check whether any ruptures are sub-seismogenic
			ProbEqkSource src = meanUCERF2.getSource(s);
			if (D) System.out.println("working on source "+src.getName()+" "+s+" of "+NUM_UCERF2_SRC_TO_USE);
			double srcDDW = src.getSourceSurface().getSurfaceWidth();
			double totMoRate=0, partMoRate=0;
			
			ArrayList<String> parentSectionNames = parentSectionNamesForUCERF2_Sources.get(s);

			for(int r=0; r<src.getNumRuptures(); r++){
				rupIndex += 1;
				ProbEqkRupture rup = src.getRupture(r);
				double ddw = rup.getRuptureSurface().getSurfaceWidth();
				double len = rup.getRuptureSurface().getSurfaceLength();
				double mag = ((int)(rup.getMag()*100.0))/100.0;	// nice value for writing
				totMoRate += MagUtils.magToMoment(rup.getMag())*rup.getMeanAnnualRate(30.0);
				srcIndexOfUCERF2_Rup[rupIndex] = s;
				rupIndexOfUCERF2_Rup[rupIndex] = r;;
				magOfUCERF2_Rup[rupIndex] = rup.getMag();
				lengthOfUCERF2_Rup[rupIndex] = len;
				rateOfUCERF2_Rup[rupIndex] = rup.getMeanAnnualRate(30.0);
				
				//don't fill in anymore if it's part of Fault Model 2.2 (no association)
				if(faultModelForSource[s]==2)
					continue;

				subSeismoUCERF2_Rup[rupIndex] = false;  // the default
				if(ddw < srcDDW) {
					subSeismoUCERF2_Rup[rupIndex] = true;
					srcHasSubSeismogenicRups = true;
					problemSource = true;
					String errorString = rupIndex+":\t"+"Sub-Seismogenic Rupture:  ddw="+(float)ddw+"\tsrcDDW="+(float)srcDDW+
					                     "\tddw/srcDDW="+(float)(ddw/srcDDW)+"\trupLen="+(float)len+"\tmag="+(float)mag+
					                     "\tiRup="+r+"\tiSrc="+s+"\t("+src.getName()+")\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
					partMoRate += MagUtils.magToMoment(rup.getMag())*rup.getMeanAnnualRate(30.0);
					continue;
				}				
				
				FaultTrace rupTrace = rup.getRuptureSurface().getRowAsTrace(0);
				Location rupEndLoc1 = rupTrace.get(0);
				Location rupEndLoc2 = rupTrace.get(rupTrace.size()-1);
				

				// find the appropriate sections for each rup endpoint
				int firstEndIndex = getCloseSection(rupEndLoc1, rupTrace, parentSectionNames);
				int secondEndIndex  = getCloseSection(rupEndLoc2, rupTrace, parentSectionNames);
				
				String sectName1 = "None Found"; // default
				String sectName2 = "None Found"; // default
				
				if(firstEndIndex != -1) {
					firstSectOfUCERF2_Rup[rupIndex]=firstEndIndex;
					sectName1=faultSectionData.get(firstEndIndex).getSectionName();
				}
				else {
					problemSource = true;
					String errorString = "Error - end1 section not found for rup "+r+
										 " of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+")\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
				}

				if(secondEndIndex != -1) {
					lastSectOfUCERF2_Rup[rupIndex]=secondEndIndex;
					sectName2=faultSectionData.get(secondEndIndex).getSectionName();
				}
				else {
					problemSource = true;
					String errorString = "Error - end2 section not found for rup "+r+
										 " of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+")\n";
					if(D) System.out.print(errorString);
					resultsString.add(errorString);
				}

				String result = rupIndex+":\t"+firstSectOfUCERF2_Rup[rupIndex]+"\t"+lastSectOfUCERF2_Rup[rupIndex]+"\t("+sectName1+"   &  "+
								sectName2+")  are the Sections at ends of rup "+ r+" of src "+s+", M="+(float)rup.getMag()+", ("+src.getName()+")\n";
				resultsString.add(result);
				
				if(problemSource && !problemSourceList.contains(src.getName()))
					problemSourceList.add(src.getName());

			}
			String infoString = (float)(partMoRate/totMoRate) +"\tis the fract MoRate below for\t"+src.getName();
			if(srcHasSubSeismogenicRups) {
				if(D) System.out.println(infoString);
				subseismoRateString.add(infoString);
			}
		}
		
		// write info results
		try {
			for(String line:resultsString)
				info_fw.write(line);
			info_fw.write("\nProblem Sources:\n\n");
			for(String line:problemSourceList)
				info_fw.write("\t"+line+"\n");
			info_fw.write("\nSubseimso Sources:\n\n");
			for(String line:subseismoRateString)
				info_fw.write("\t"+line+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This saves the following data to a file with name set by DATA_FILE_NAME: 
	 * 
	 * 	firstSectOfUCERF2_Rup, 
	 * 	lastSectOfUCERF2_Rup, 
	 * 	srcIndexOfUCERF2_Rup, 
	 * 	rupIndexOfUCERF2_Rup, 
	 * 	magOfUCERF2_Rup, 
	 * 	lengthOfUCERF2_Rup,
	 * 	rateOfUCERF2_Rup,
	 *  subSeismoUCERF2_Rup,
	 *  invRupIndexForUCERF2_Rup
	 * 
	 */
	private void writePreComputedDataFile() {
		// write out the data
		try {
			FileOutputStream file_output = new FileOutputStream (dataFile);
			// Wrap the FileOutputStream with a DataOutputStream
			DataOutputStream data_out = new DataOutputStream (file_output);
			for(int i=0; i<NUM_UCERF2_RUPTURES;i++) {
				data_out.writeInt(firstSectOfUCERF2_Rup[i]);
				data_out.writeInt(lastSectOfUCERF2_Rup[i]);
				data_out.writeInt(srcIndexOfUCERF2_Rup[i]);
				data_out.writeInt(rupIndexOfUCERF2_Rup[i]);
				data_out.writeDouble(magOfUCERF2_Rup[i]);
				data_out.writeDouble(lengthOfUCERF2_Rup[i]);
				data_out.writeDouble(rateOfUCERF2_Rup[i]);
				data_out.writeBoolean(subSeismoUCERF2_Rup[i]);
				data_out.writeInt(invRupIndexForUCERF2_Rup[i]);
			}
			file_output.close ();
		}
		catch (IOException e) {
			System.out.println ("IO exception = " + e );
		}
	}
	
	
	/**
	 * This returns the magnitude and rate of the equivalent UCERF2 ruptures.  If more than one UCERF2 
	 * rupture are associated with the inversion rupture (as would come from the type-A segmented models 
	 * where there are diff mags for the same char rup), then the total rate of these is returned with a 
	 * magnitude that preserves the total moment rate.
	 * 
	 * @param invRupIndex - the index of the inversion rupture
	 * @return - double[2] where mag is in the first element and rate is in the second

	 */
	private double[] computeMagAndRateForRupture(int invRupIndex) {
		
		ArrayList<Integer> ucerf2_assocRups = rupAssociationList.get(invRupIndex);
		
		// return null if there are no associations
		if(ucerf2_assocRups.size()==0) {
			return null;
		}
		else if(ucerf2_assocRups.size()==1) {
			int r = ucerf2_assocRups.get(0);
			// this makes sure a UCERF2 rupture is not used more than once
			double[] result = new double[2];
			result[0]=magOfUCERF2_Rup[r];
			result[1]=rateOfUCERF2_Rup[r];
			mfdOfAssocRupsAndModMags.addResampledMagRate(result[0], result[1], true);
			mfdOfAssocRupsWithOrigMags.addResampledMagRate(result[0], result[1], true);
//System.out.println("\t\t"+result[0]+"\t"+result[1]);
			return result;
		}
		else {
			double totRate=0, totMoRate=0;
			for(Integer ur:ucerf2_assocRups) {
				totRate+=rateOfUCERF2_Rup[ur];
				totMoRate+=rateOfUCERF2_Rup[ur]*MagUtils.magToMoment(magOfUCERF2_Rup[ur]);
				mfdOfAssocRupsWithOrigMags.addResampledMagRate(magOfUCERF2_Rup[ur], rateOfUCERF2_Rup[ur], true);
			}
			double aveMoment = totMoRate/totRate;
			double mag = MagUtils.momentToMag(aveMoment);
			double[] result = new double[2];
			result[0]=mag;
			result[1]=totRate;
			mfdOfAssocRupsAndModMags.addResampledMagRate(mag, totRate, false);
			return result;
		}
	}


	
	
	
	
	/**
	 * This fills in invRupIndexForUCERF2_Rup (which inversion rupture each UCERF2 
	 * rupture is associated), with a value of -1 if there is no association.
	 * This also creates rupAssociationList.
	 * @param inversionRups
	 */
	private void findAssociations(List<? extends List<Integer>> inversionRups) {
		
		if(D) System.out.println("Starting associations");

		rupAssociationList = new ArrayList<ArrayList<Integer>>();
		
		// this will give the inversion rup index for each UCERF2 rupture (-1 if no association)
//		invRupIndexForUCERF2_Rup = new int[NUM_UCERF2_RUPTURES];
		for(int r=0;r<NUM_UCERF2_RUPTURES;r++) 
			invRupIndexForUCERF2_Rup[r] = -1;
		
		// loop over inversion ruptures (ir)
		for(int ir=0; ir<inversionRups.size();ir++) {
			List<Integer> invRupSectIDs = inversionRups.get(ir);
			// make a list of parent section names for this inversion rupture
			ArrayList<String> parSectNamesList = new ArrayList<String>();
			for(Integer s : invRupSectIDs) {
				String parName = faultSectionData.get(s).getParentSectionName();	// could do IDs instead
				if(!parSectNamesList.contains(parName))
					parSectNamesList.add(parName);
			}
			// set the first and last section on the inversion rupture
			int invSectID_1 = invRupSectIDs.get(0);
			int invSectID_2 = invRupSectIDs.get(invRupSectIDs.size()-1);

			// now loop over all UCERF2 ruptures
			ArrayList<Integer> ucerfRupsIndexList = new ArrayList<Integer>();  // a list of UCERF2 ruptures associated with this inversion rupture (ir)
			for(int ur=0;ur<NUM_UCERF2_RUPTURES;ur++) {
				int ucerf2_SectID_1 = firstSectOfUCERF2_Rup[ur];
				int ucerf2_SectID_2 = lastSectOfUCERF2_Rup[ur];
				// check that section ends are the same (& check both ways)
				if((invSectID_1==ucerf2_SectID_1 && invSectID_2==ucerf2_SectID_2) || 
						(invSectID_1==ucerf2_SectID_2 && invSectID_2==ucerf2_SectID_1)) {
					boolean match = true;
					// make sure inv rup does not have parent sections that are not in the UCERF2 source (this filters our the multi-path ruptures)
					ArrayList<String> ucerf2_sectNames = parentSectionNamesForUCERF2_Sources.get(srcIndexOfUCERF2_Rup[ur]);
					for(String invParSectName:parSectNamesList) {
						if(!ucerf2_sectNames.contains(invParSectName))
							match = false;
					}
					if(match==true) {
						// check to make sure the UCERF2 rupture was not already associated
						if(invRupIndexForUCERF2_Rup[ur] != -1)
							throw new RuntimeException("UCERF2 rupture "+ur+" was already associated with inv rup "+invRupIndexForUCERF2_Rup[ur]+"\t can assoc with: "+ir);
						ucerfRupsIndexList.add(ur);
						invRupIndexForUCERF2_Rup[ur] = ir;
					}
				}
			}
			rupAssociationList.add(ucerfRupsIndexList);
		}
		
		// test results
		if(D) {
			ArrayList<ArrayList<Integer>> tempRupAssociationList = new ArrayList<ArrayList<Integer>>();
			for(ArrayList<Integer> list: rupAssociationList)
				tempRupAssociationList.add((ArrayList<Integer>)list.clone());
			for(int r=0;r<NUM_UCERF2_RUPTURES;r++) {
				int invRup = invRupIndexForUCERF2_Rup[r];
				if(invRup != -1)
					tempRupAssociationList.get(invRup).remove(new Integer(r));
			}
			for(ArrayList<Integer> list:tempRupAssociationList)
				if(list.size() != 0)
					throw new RuntimeException("List should be zero for!");
		}
		
		// write un-associated UCERF2 ruptures
		try {
			int numUnassociated=0;
			info_fw.write("\nUnassociated UCERF2 ruptures (that aren't from fault model 2.2. and aren't subseismogenic)\n\n");
			info_fw.write("\tu2_rup\tsrcIndex\trupIndex\tfaultMod\tsubSeis\tinvRupIndex\tsrcName\n");
			for(int r=0;r<NUM_UCERF2_RUPTURES;r++) {
				if((faultModelForSource[srcIndexOfUCERF2_Rup[r]] != 2) && !subSeismoUCERF2_Rup[r] && (invRupIndexForUCERF2_Rup[r] == -1)) { // first make sure it's not for fault model 2.2
					info_fw.write("\t"+r+"\t"+srcIndexOfUCERF2_Rup[r]+"\t"+rupIndexOfUCERF2_Rup[r]+
									"\t"+faultModelForSource[srcIndexOfUCERF2_Rup[r]]+
									"\t"+subSeismoUCERF2_Rup[r]+"\t"+invRupIndexForUCERF2_Rup[r]+"\t"+
									meanUCERF2.getSource(srcIndexOfUCERF2_Rup[r]).getName()+"\n");
							numUnassociated+=1;
				}
			}
			info_fw.write("\tTot Num of Above Problems = "+numUnassociated+" (of "+NUM_UCERF2_RUPTURES+")\n\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(D) System.out.println("Done with associations");
	}
	
	
	/**
	 * This returns the UCERF2 mag and rate for the specified inversion rupture
	 * (or null if there was no association, which will usually be the case).
	 * 
	 * @param invRupIndex - the index of the inversion rupture
	 * @return - double[2] with mag in index 0 and rate in index 1.
	 */
	public ArrayList<double[]> getMagsAndRatesForRuptures() {
		return magsAndRatesForRuptures;
	}


	/**
	 * This returns the mag and rate for the specified inversion rupture 
	 * (or null if there is no corresponding UCERF2 rupture, which will usually be the case).
	 * The mag and rate are in the double[] object at index 0 and 1, respectively.
	 * 
	 * @return
	 */
	public double[] getMagsAndRatesForRupture(int invRupIndex) {
		return magsAndRatesForRuptures.get(invRupIndex);
	}

	
	/**
	 * This generates the array list containing the UCERF2 equivalent mag and rate for each rupture 
	 * (or null if there is no corresponding UCERF2 rupture, which will usually be the case).
	 * The mag and rate are in the double[] objects (at index 0 and 1, respectively).
	 * 
	 * If more than one inversion ruptures have the same end sections (multi pathing), then the one with the minimum number 
	 * of sections get's the equivalent UCERF2 ruptures (and an exception is thrown if there are more than one inversion 
	 * rupture that have this same minimum number of sections; which doesn't occur for N Cal ruptures).
	 * 
	 * This also computes the mag-freq-dists: mfdOfAssocRupsAndModMags and mfdOfAssocRupsWithOrigMags, where the latter
	 * preserves the aleatory range of mags for given area (e.g., on the char events) and the former uses the combined
	 * mean mag used for the association (preserving moment rates)
	 * @param inversionRups
	 * @return
	 */
	private void computeMagsAndRatesForAllRuptures() {

		if(D) System.out.println("Starting computeMagsAndRatesForRuptures");
		
		mfdOfAssocRupsAndModMags = new SummedMagFreqDist(5.05,35,0.1);
		mfdOfAssocRupsAndModMags.setName("MFD for UCERF2 associated ruptures");
		mfdOfAssocRupsAndModMags.setInfo("using modified (average) mags; this excludes sub-seimogenic rups");
		mfdOfAssocRupsWithOrigMags = new SummedMagFreqDist(5.05,35,0.1);
		mfdOfAssocRupsWithOrigMags.setName("MFD for UCERF2 associated ruptures");
		mfdOfAssocRupsWithOrigMags.setInfo("using original mags; this excludes sub-seimogenic rups");
		
		// now get the mags and rates of each
		magsAndRatesForRuptures = new ArrayList<double[]>();
		for(int r=0; r<NUM_INVERSION_RUPTURES; r++) 
			magsAndRatesForRuptures.add(computeMagAndRateForRupture(r));

// System.out.println(mfdOfAssocRupsAndModMags);
		if(D) {
			System.out.println("Rate & Moment Rate for mfdOfAssocRupsAndModMags: "+
					(float)mfdOfAssocRupsAndModMags.getTotalIncrRate()+",\t"+
					(float)mfdOfAssocRupsAndModMags.getTotalMomentRate());
			System.out.println("Rate & Moment Rate for mfdOfAssocRupsWithOrigMags: "+
					(float)mfdOfAssocRupsWithOrigMags.getTotalIncrRate()+",\t"+
					(float)mfdOfAssocRupsWithOrigMags.getTotalMomentRate());
		}
		
		if(D) System.out.println("Done with computeMagsAndRatesForRuptures");

	}
	
	
	/**
	 * This reads a file that contains the sections names used by each UCERF2 source put in 
	 * parentSectionNamesForUCERF2_Sources), plus indicates which fault model each source is 
	 * associated with (faultModelForSource[]).
	 */
	private void readSectionsForUCERF2_SourcesFile() {
		
		// make sure UCERF2 is instantiated
		getMeanUCERF2_Instance();
		
		parentSectionNamesForUCERF2_Sources = new ArrayList<ArrayList<String>>();
		faultModelForSource = new int[NUM_UCERF2_SRC_TO_USE];
		if(D) System.out.println("Reading file: "+SECT_FOR_UCERF2_SRC_FILE_PATH_NAME);
	    try {
			BufferedReader reader = new BufferedReader(new FileReader(SECT_FOR_UCERF2_SRC_FILE_PATH_NAME));
			for(int l=0;l<NUM_UCERF2_SRC_TO_USE;l++) {
				String line = reader.readLine();
	        	String[] st = StringUtils.split(line,"\t");
	        	int srcIndex = Integer.valueOf(st[0]);
	        	if(srcIndex != l)
	        		throw new RuntimeException("problem with source index");
	        	String srcName = st[1]; //st.nextToken();
	        	String targetSrcName = meanUCERF2.getSource(l).getName();
	        	if(!srcName.equals(targetSrcName))
	        		throw new RuntimeException("problem with source name:\t"+srcName+"\t"+targetSrcName);
	        	faultModelForSource[l] = Integer.valueOf(st[2]);
	        	ArrayList<String> parentSectNamesList = new ArrayList<String>();
	        	for(int i=3;i<st.length;i++)
	        		parentSectNamesList.add(st[i]);
	        	parentSectionNamesForUCERF2_Sources.add(parentSectNamesList);
	        	
	        	// TEST:
	        	// reconstruct string
	        	String test = l+"\t"+srcName+"\t"+faultModelForSource[l];
	        	for(String name:parentSectNamesList)
	        		test += "\t"+name;
//				System.out.println(test);
	        	if(!test.equals(line))
	        		throw new RuntimeException("problem with recreating file line");

	        }
       } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		if(D) System.out.println("Done reading file");

       // the rest is testing
       if(D) {
    	   // Check to make sure contents of parentSectionNamesForUCERF2_Sources are consistent with parent section names
    	   System.out.println("Num SubSections = "+faultSectionData.size());
    	   ArrayList<String> parentSectionNames = new ArrayList<String>();
    	   for(int i=0; i<faultSectionData.size();i++) {
    		   if(!parentSectionNames.contains(faultSectionData.get(i).getParentSectionName()))
    			   parentSectionNames.add(faultSectionData.get(i).getParentSectionName());
    	   }

    	   ArrayList<String> leftoverSectionNames = (ArrayList<String>)parentSectionNames.clone();
    	   ArrayList<String> faultModel2pt2_SectionNames = new ArrayList<String> ();
    	   System.out.println("\nTesting parentSectionNamesForUCERF2_Sources (index,name,FaultModel):\n");
    	   for(int s=0;s<parentSectionNamesForUCERF2_Sources.size();s++) {
    		   ArrayList<String> parNames = parentSectionNamesForUCERF2_Sources.get(s);
    		   for(String name: parNames) {
    			   if(faultModelForSource[s] != 2) {
    				   if(!parentSectionNames.contains(name)) {
    					   System.out.println("\t"+s+"\t"+name+"\t"+faultModelForSource[s]);
    				   }
    			   }
    			   else {
    				   if(!faultModel2pt2_SectionNames.contains(name))
    					   faultModel2pt2_SectionNames.add(name);
    			   }
    			   leftoverSectionNames.remove(name);
    		   }
    	   }

    	   System.out.println("\nUnassociated elements of parentSectionNames:\n");
    	   System.out.println("\t"+leftoverSectionNames);

    	   System.out.println("\nParent Names for Fault Model 2.2 Sources:\n");
    	   for(String name: faultModel2pt2_SectionNames)
    		   System.out.println("\t"+name);
       }
	}
	
	
	/**
	 * This gets a start on making the "FM2pt1_SectionsForUCERF2_Sources.txt" file 
	 * (the rest was filled in by hand by looking at a number of UCERF2 data files,
	 * and note that I had some problems with hidden characters between tabs, which
	 * was fixed by pasting into an email to myself)
	 */
	private void writePrelimSectionsForUCERF2_Sources() {

		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(DeformationModelFetcher.DefModName.UCERF2_ALL,new File("dev/scratch/UCERF3/preComputedData/"));
		ArrayList<FaultSectionPrefData> faultSectionData = deformationModelFetcher.getSubSectionList();
		ArrayList<String> parentSectionNames = new ArrayList<String>();
		for(int i=0; i<faultSectionData.size();i++) {
			if(!parentSectionNames.contains(faultSectionData.get(i).getParentSectionName()))
				parentSectionNames.add(faultSectionData.get(i).getParentSectionName());
		}

		ModMeanUCERF2 meanUCERF2 = FindEquivUCERF2_FM2pt1_Ruptures.getMeanUCERF2_Instance();

		// Preliminary filename
		String fullpathname = "dev/scratch/UCERF3/utils/FindEquivUCERF2_Ruptures/SectionsForUCERF2_SourcesPrelim.txt";
		File file = new File (fullpathname);
		FileOutputStream file_output;
		try {
			file_output = new FileOutputStream (file);
			DataOutputStream data_out = new DataOutputStream (file_output);

			int s=0;
			for(ProbEqkSource src :meanUCERF2) {
				data_out.writeChars(s+"\t"+src.getName()+"\t0\t");
				if(parentSectionNames.contains(src.getName()))
					data_out.writeChars(src.getName()+"\n");
				else
					data_out.writeChars("NOT_SAME"+"\n");
				s++;
			}

			file_output.close ();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	/**
	 * This finds the end subsection for the given end of a rupture.  This first finds the two closest subsections (to any
	 * point on each subsection).  The closest of the two is assigned if both ends are within half the section length to
	 * any point on the rupture trace.  If not, the second closest is assigned if it passes this test.  If neither pass 
	 * this test (which can happen where there are gaps between faults), we assign whichever section's farthest end point
	 * (from the rupture trace) is closest.
	 * @param rupEndLoc
	 * @param rupTrace
	 * @param parentSectionNames
	 * @return
	 */
	private int getCloseSection(Location rupEndLoc, FaultTrace rupTrace, ArrayList<String> parentSectionNames) {

		int targetSection=-1;
		double dist;

		double closestDist=Double.MAX_VALUE, secondClosestDist=Double.MAX_VALUE;
		int clostestSect=-1, secondClosestSect=-1;
		for(int i=0; i<faultSectionData.size(); i++) {

			FaultSectionPrefData sectionData = faultSectionData.get(i);

			if(!parentSectionNames.contains(sectionData.getParentSectionName()))
				continue;

			//		System.out.println(sectionData.getName());

			FaultTrace sectionTrace = sectionData.getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
			dist = sectionTrace.minDistToLocation(rupEndLoc);
// System.out.println(sectionData.getName()+"\t"+dist);
			if(dist<closestDist) {
				secondClosestDist=closestDist;
				secondClosestSect=clostestSect;
				closestDist=dist;
				clostestSect = i;
			}
			else if(dist<secondClosestDist) {
				secondClosestDist=dist;
				secondClosestSect=i;
			}
		}
		
		
		// return -1 if nothing found (e.g., creeping section)
		if(clostestSect == -1)
			return targetSection;
			
		// now see if both ends of closest section are within half the section length
		FaultTrace sectionTrace = faultSectionData.get(clostestSect).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
		double sectHalfLength = 0.5*sectionTrace.getTraceLength()+0.5; 
		Location sectEnd1 = sectionTrace.get(0);
		Location sectEnd2 = sectionTrace.get(sectionTrace.size()-1);
		double dist1 =rupTrace.minDistToLocation(sectEnd1);
		double dist2 =rupTrace.minDistToLocation(sectEnd2);
		double maxDistClosest = Math.max(dist1, dist2);
// System.out.println("clostestSect\t"+faultSectionData.get(clostestSect).getName()+"\tclosestDist="+closestDist+"\tsectHalfLength="+
//		sectHalfLength+"\tdist1="+dist1+"\tdist2="+dist2);
		if(dist1 <sectHalfLength && dist2 <sectHalfLength)
			targetSection = clostestSect;
		
		// check the second closest if the above failed
		double maxDistSecondClosest=Double.NaN;
		if(targetSection == -1) {	// check the second closest if the above failed
			sectionTrace = faultSectionData.get(secondClosestSect).getStirlingGriddedSurface(true, 1.0).getRowAsTrace(0);
			sectHalfLength = 0.5*sectionTrace.getTraceLength()+0.5; 
			sectEnd1 = sectionTrace.get(0);
			sectEnd2 = sectionTrace.get(sectionTrace.size()-1);
			dist1 =rupTrace.minDistToLocation(sectEnd1);
			dist2 =rupTrace.minDistToLocation(sectEnd2);
			maxDistSecondClosest = Math.max(dist1, dist2);
// System.out.println("secondClosestSect\t"+faultSectionData.get(secondClosestSect).getName()+"\tsecondClosestDist="+secondClosestDist+"\tsectHalfLength="+
//		sectHalfLength+"\tdist1="+dist1+"\tdist2="+dist2);
			if(dist1 <sectHalfLength && dist2 <sectHalfLength)
				targetSection = secondClosestSect;
		}
		
		if(targetSection == -1) {
			if(maxDistClosest<maxDistSecondClosest)
				targetSection = clostestSect;
			else
				targetSection = secondClosestSect;
		}

		// Can't get here?
		if(targetSection == -1) {
			System.out.println("clostestSect\t"+faultSectionData.get(clostestSect).getName()+"\tclosestDist="+closestDist);
			System.out.println("secondClosestSect\t"+faultSectionData.get(secondClosestSect).getName()+"\tsecondClosestDist="+secondClosestDist);
		}

		
		return targetSection;
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File precompDataDir = new File("dev/scratch/UCERF3/preComputedData/");
		
//   		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(DeformationModelFetcher.DefModName.UCERF2_ALL,new File("dev/scratch/UCERF3/preComputedData/"));

   		// read XML rup set file
		System.out.println("Reading XML file");
   		SimpleFaultSystemRupSet faultSysRupSet=null;
   		try {
			faultSysRupSet = SimpleFaultSystemRupSet.fromXMLFile(new File(precompDataDir.getAbsolutePath()+File.separator+"rupSet.xml"));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Done Reading XML file");

		
		FindEquivUCERF2_FM2pt1_Ruptures test = new FindEquivUCERF2_FM2pt1_Ruptures(faultSysRupSet, precompDataDir);
   		
		
/*   		// To Make XML **********************
		double maxJumpDist = 5.0;
		double maxAzimuthChange = 45;
		double maxTotAzimuthChange = 90;
		double maxRakeDiff = 90;
		int minNumSectInRup = 2;
		double moRateReduction = 0.1;
		ArrayList<MagAreaRelationship> magAreaRelList = new ArrayList<MagAreaRelationship>();
		magAreaRelList.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelList.add(new HanksBakun2002_MagAreaRel());
		
		// Instantiate the FaultSystemRupSet
		System.out.println("\nStarting FaultSystemRupSet instantiation");
		long startTime = System.currentTimeMillis();
		InversionFaultSystemRupSet invFaultSystemRupSet = new InversionFaultSystemRupSet(DeformationModelFetcher.DefModName.UCERF2_ALL,
				maxJumpDist,maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, minNumSectInRup, magAreaRelList, 
				moRateReduction,  InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL , precompDataDir);
		long runTime = System.currentTimeMillis()-startTime;
		System.out.println("\nFaultSystemRupSet instantiation took " + (runTime/1000) + " seconds");
		
		File xmlOut = new File(precompDataDir.getAbsolutePath()+File.separator+"rupSet.xml");
		try {
			new SimpleFaultSystemRupSet(invFaultSystemRupSet).toXMLFile(xmlOut);
			if (D) System.out.println("DONE");
		} catch (IOException e) {
			System.out.println("IOException saving Rup Set to XML!");
			e.printStackTrace();
		}
		// End making XML ********************
*/

   		
   		

		
//		FindEquivUCERF2_FM2pt1_Ruptures test = new FindEquivUCERF2_FM2pt1_Ruptures();
//		test.writePrelimFM2pt1_SectionsForUCERF2_Sources();
//		test.readFM2pt1_SectionsForUCERF2_SourcesFile();

	}

}
