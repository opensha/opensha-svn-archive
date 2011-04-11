package scratch.UCERF3;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures;
import scratch.UCERF3.utils.SimulatedAnnealing;


/**
 * This does the "Grand Inversion" for UCERF3 (or other ERFs)
 * 
 * Important Notes:
 * 
 * 1) If the sections are actually subsections of larger sections, then the method 
 * computeCloseSubSectionsListList() only allows one connection between parent sections
 * (to avoid ruptures jumping back and forth for closely spaced and parallel sections).
 * Is this potentially problematic?
 * 
 * Aseismicity reduces area here
 * 
 * @author field & Page
 *
 */
public class RupsInFaultSystemInversion {

	protected final static boolean D = true;  // for debugging
	
	final static String PALEO_DATA_FILE_NAME = "Appendix_C_Table7_091807.xls";

	ArrayList<FaultSectionPrefData> faultSectionData;
	double sectionDistances[][],sectionAzimuths[][];;
	double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff;
	int minNumSectInRup;
	
	ArrayList<MagAreaRelationship> magAreaRelList;

	String endPointNames[];
	Location endPointLocs[];
	int numSections;
	ArrayList<ArrayList<Integer>> sectionConnectionsListList, endToEndSectLinksList;
	
	File precomputedDataDir; // this is where pre-computed data are stored (we read these to make things faster)
	
	ArrayList<SegRateConstraint> segRateConstraints;

	ArrayList<SectionCluster> sectionClusterList;
	
	// rupture attributes (all in SI units)
	double[] rupMeanMag, rupMeanMoment, rupTotMoRateAvail, rupArea, rupLength;
	int[] clusterIndexForRup, rupIndexInClusterForRup;
	int numRuptures=0;
	
	double moRateReduction;  // reduction of section slip rates to account for smaller events
	
	// section attributes (all in SI units)
	double[] sectSlipRateReduced;	// this gets reduced by moRateReduction (if non zero)
	
	// slip model:
	public final static String CHAR_SLIP_MODEL = "Characteristic (Dsr=Ds)";
	public final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar (Dsr=Dr)";
	public final static String WG02_SLIP_MODEL = "WGCEP-2002 model (Dsr prop to Vs)";
	public final static String TAPERED_SLIP_MODEL = "Tapered Ends ([Sin(x)]^0.5)";
	private String slipModelType = TAPERED_SLIP_MODEL;

	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;

	//rates and magnitudes for closest equivalents to UCERF2 ruptures
	ArrayList<double[]> ucerf2_magsAndRates;

	/**
	 * 
	 * @param faultSectionData - this assumes subsections (if any) are in proper order (have adjacent indices)
	 * @param sectionDistances
	 * @param subSectionAzimuths
	 * @param maxJumpDist
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 * @param maxRakeDiff
	 * @param minNumSectInRup
	 */
	public RupsInFaultSystemInversion(ArrayList<FaultSectionPrefData> faultSectionData,
			double[][] sectionDistances, double[][] subSectionAzimuths, double maxJumpDist, 
			double maxAzimuthChange, double maxTotAzimuthChange, double maxRakeDiff, int minNumSectInRup,
			ArrayList<MagAreaRelationship> magAreaRelList, File precomputedDataDir, double moRateReduction) {

		if(D) System.out.println("Instantiating RupsInFaultSystemInversion");
		this.faultSectionData = faultSectionData;
		this.sectionDistances = sectionDistances;
		this.sectionAzimuths = subSectionAzimuths;
		this.maxJumpDist=maxJumpDist;
		this.maxAzimuthChange=maxAzimuthChange; 
		this.maxTotAzimuthChange=maxTotAzimuthChange; 
		this.maxRakeDiff=maxRakeDiff;
		this.minNumSectInRup=minNumSectInRup;
		this.magAreaRelList=magAreaRelList;
		this.precomputedDataDir = precomputedDataDir;
		this.moRateReduction=moRateReduction;

		// write out settings if in debug mode
		if(D) System.out.println("faultSectionData.size() = "+faultSectionData.size() +
				"; sectionDistances.length = "+sectionDistances.length +
				"; subSectionAzimuths.length = "+subSectionAzimuths.length +
				"; maxJumpDist = "+maxJumpDist +
				"; maxAzimuthChange = "+maxAzimuthChange + 
				"; maxTotAzimuthChange = "+maxTotAzimuthChange +
				"; maxRakeDiff = "+maxRakeDiff +
				"; minNumSectInRup = "+minNumSectInRup);

		// check that indices are same as IDs
		for(int i=0; i<faultSectionData.size();i++)
			if(faultSectionData.get(i).getSectionId() != i)
				throw new RuntimeException("RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();
		
		// add standard deviations here as well
		sectSlipRateReduced = new double[numSections];
		for(int s=0; s<numSections; s++)
			sectSlipRateReduced[s] = faultSectionData.get(s).getAveLongTermSlipRate()*1e-3*(1-moRateReduction); // mm/yr --> m/yr; includes moRateReduction

		// make the list of nearby sections for each section (branches)
		if(D) System.out.println("Making sectionConnectionsListList");
		computeCloseSubSectionsListList();
		if(D) System.out.println("Done making sectionConnectionsListList");

		// get paleoseismic constraints
		getPaleoSegRateConstraints();

		// make the list of SectionCluster objects 
		// (each represents a set of nearby sections and computes the possible
		//  "ruptures", each defined as a list of sections in that rupture)
		makeClusterList();
		
		// calculate rupture magnitude and other attributes
		calcRuptureAttributes();
		
		
		// plot magnitude histogram for the inversion ruptures (how many rups at each mag)
		// comment this out if you don't want it popping up (if you're using SCEC VDO)
/*		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<getNumRupRuptures();r++)
			magHist.add(rupMeanMag[r], 1.0);
		ArrayList funcs = new ArrayList();
		funcs.add(magHist);
		magHist.setName("Histogram of Inversion ruptures");
		magHist.setInfo("(number in each mag bin)");
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Magnitude Histogram"); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Num");
*/	
		
		ArrayList<ArrayList<Integer>> rupList = getRupList();
		

		// Now get the UCERF2 equivalent mag and rate for each inversion rupture (where there's a meaningful
		// association).  ucerf2_magsAndRates is an ArrayList of length getRupList().size(), where each element 
		// is a double[2] with mag in the first element and rate in the second. The element is null if there is
		// no association.  For example, to get the UCERF2 mag and rate for the 10th Inversion rupture, do the
		// following:
		// 
		//		double[] magAndRate = ucerf2_magsAndRates.get(10);
		//		if(magAndRate != null) {
		//			double mag = magAndRate[0];
		//			double rate = magAndRate[1];
		//		}
		//
		// Notes: 
		//
		// 1) files saved/read in precomputedDataDir by the following class should be 
		// deleted any time the contents of faultSectionData or rupList change; these filenames
		// are specified by FindEquivUCERF2_Ruptures.DATA_FILE_NAME and FindEquivUCERF2_Ruptures.INFO_FILE_NAME.
		//
		// 2) This currently only works for N. Cal Inversions
		//		
		FindEquivUCERF2_Ruptures findUCERF2_Rups = new FindEquivUCERF2_Ruptures(faultSectionData, precomputedDataDir);
		ucerf2_magsAndRates = findUCERF2_Rups.getMagsAndRatesForRuptures(rupList);
		// the following plot verifies that associations are made properly from the perspective of mag-freq-dists
		// this is valid only if createNorthCalSubSections() has been used in TestInversion!
//		findUCERF2_Rups.plotMFD_TestForNcal();

		
		doInversion(findUCERF2_Rups, rupList);
		
		
	}


	/**
	 * This returns the list of FaultSectionPrefData used in the inversion
	 * @return
	 */
	public ArrayList<FaultSectionPrefData> getFaultSectionData() {
		return faultSectionData;
	}


	public int getNumClusters() {
		return sectionClusterList.size();
	}


	public SectionCluster getCluster(int clusterIndex) {
		return sectionClusterList.get(clusterIndex);
	}


	public ArrayList<ArrayList<Integer>> getRupList() {
		ArrayList<ArrayList<Integer>> rupList = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<sectionClusterList.size();i++) {
			if(D) System.out.println("Working on rupture list for cluster "+i);
			rupList.addAll(sectionClusterList.get(i).getSectionIndicesForRuptures());
		}
		return rupList;
	}


	/**
	 * This returns the total number of ruptures
	 * @return
	 */
	public int getNumRupRuptures() {
		if(numRuptures ==0) {
			for(int c=0; c<sectionClusterList.size();c++)
				numRuptures += sectionClusterList.get(c).getNumRuptures();
		}
		return numRuptures;
	}



	/**
	 * For each section, create a list of sections that are within maxJumpDist.  
	 * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	 * Reciprocal duplicates are not filtered out.
	 * If sections are actually subsections (meaning getParentSectionId() != -1), then each parent section can only
	 * have one connection to another parent section (whichever subsections are closest).  This prevents parallel 
	 * and closely space faults from having connections back and forth all the way down the section.
	 */
	private void computeCloseSubSectionsListList() {

		sectionConnectionsListList = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<numSections;i++)
			sectionConnectionsListList.add(new ArrayList<Integer>());

		// in case the sections here are subsections of larger sections, create a subSectionDataListList where each
		// ArrayList<FaultSectionPrefData> is a list of subsections from the parent section
		ArrayList<ArrayList<FaultSectionPrefData>> subSectionDataListList = new ArrayList<ArrayList<FaultSectionPrefData>>();
		int lastID=-1;
		ArrayList<FaultSectionPrefData> newList = new ArrayList<FaultSectionPrefData>();
		for(int i=0; i<faultSectionData.size();i++) {
			FaultSectionPrefData subSect = faultSectionData.get(i);
			int parentID = subSect.getParentSectionId();
			if(parentID != lastID || parentID == -1) { // -1 means there is no parent
				newList = new ArrayList<FaultSectionPrefData>();
				subSectionDataListList.add(newList);
				lastID = subSect.getParentSectionId();
			}
			newList.add(subSect);
		}


		// First, if larger sections have been sub-sectioned, fill in neighboring subsection connections
		// (using the other algorithm below might lead to subsections being skipped if their width is < maxJumpDist) 
		for(int i=0; i<subSectionDataListList.size(); ++i) {
			ArrayList<FaultSectionPrefData> subSectList = subSectionDataListList.get(i);
			int numSubSect = subSectList.size();
			for(int j=0;j<numSubSect;j++) {
				// get index of section
				int sectIndex = subSectList.get(j).getSectionId();
				ArrayList<Integer> sectionConnections = sectionConnectionsListList.get(sectIndex);
				if(j != 0) // skip the first one since it has no previous subsection
					sectionConnections.add(subSectList.get(j-1).getSectionId());
				if(j != numSubSect-1) // the last one has no subsequent subsection
					sectionConnections.add(subSectList.get(j+1).getSectionId());
			}
		}

		// now add subsections on other sections, keeping only one connection between each section (the closest)
		for(int i=0; i<subSectionDataListList.size(); ++i) {
			ArrayList<FaultSectionPrefData> sect1_List = subSectionDataListList.get(i);
			for(int j=i+1; j<subSectionDataListList.size(); ++j) {
				ArrayList<FaultSectionPrefData> sect2_List = subSectionDataListList.get(j);
				double minDist=Double.MAX_VALUE;
				int subSectIndex1 = -1;
				int subSectIndex2 = -1;
				// find the closest pair
				for(int k=0;k<sect1_List.size();k++) {
					for(int l=0;l<sect2_List.size();l++) {
						int index1 = sect1_List.get(k).getSectionId();
						int index2 = sect2_List.get(l).getSectionId();;
						double dist = sectionDistances[index1][index2];
						if(dist < minDist) {
							minDist = dist;
							subSectIndex1 = index1;
							subSectIndex2 = index2;
						}					  
					}
				}
				// add to lists for each subsection
				if (minDist<maxJumpDist) {
					sectionConnectionsListList.get(subSectIndex1).add(subSectIndex2);
					sectionConnectionsListList.get(subSectIndex2).add(subSectIndex1);  // reciprocal of the above
				}
			}
		}
	}

	
	public ArrayList<ArrayList<Integer>> getCloseSubSectionsListList() {
		return sectionConnectionsListList;
	}


	private void makeClusterList() {

		// make an arrayList of section indexes
		ArrayList<Integer> availableSections = new ArrayList<Integer>();
		for(int i=0; i<numSections; i++) availableSections.add(i);

		sectionClusterList = new ArrayList<SectionCluster>();
		while(availableSections.size()>0) {
			if (D) System.out.println("WORKING ON CLUSTER #"+(sectionClusterList.size()+1));
			int firstSubSection = availableSections.get(0);
			SectionCluster newCluster = new SectionCluster(faultSectionData, minNumSectInRup,sectionConnectionsListList,
					sectionAzimuths, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff);
			newCluster.add(firstSubSection);
			if (D) System.out.println("\tfirst is "+faultSectionData.get(firstSubSection).getName());
			addClusterLinks(firstSubSection, newCluster);
			// remove the used subsections from the available list
			for(int i=0; i<newCluster.size();i++) availableSections.remove(newCluster.get(i));
			// add this cluster to the list
			sectionClusterList.add(newCluster);
			if (D) System.out.println(newCluster.size()+"\tsubsections in cluster #"+sectionClusterList.size()+"\t"+
					availableSections.size()+"\t subsections left to allocate");
		}
	}


	private void addClusterLinks(int subSectIndex, SectionCluster list) {
		ArrayList<Integer> branches = sectionConnectionsListList.get(subSectIndex);
		for(int i=0; i<branches.size(); i++) {
			Integer subSect = branches.get(i);
			if(!list.contains(subSect)) {
				list.add(subSect);
				addClusterLinks(subSect, list);
			}
		}
	}


	/**
	 * This writes out the close subsections to each subsection (and the distance)
	 */
	public void writeCloseSubSections(String filePathAndName) {
		if (D) System.out.print("writing file closeSubSections.txt");
		try{
//			FileWriter fw = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/closeSubSections.txt");
			FileWriter fw = new FileWriter(filePathAndName);
			//			FileWriter fw = new FileWriter("/Users/pagem/eclipse/workspace/OpenSHA/dev/scratch/pagem/rupsInFaultSystem/closeSubSections.txt");
			String outputString = new String();

			for(int sIndex1=0; sIndex1<sectionConnectionsListList.size();sIndex1++) {
				ArrayList<Integer> sectList = sectionConnectionsListList.get(sIndex1);
				String sectName = faultSectionData.get(sIndex1).getName();
				outputString += "\n"+ sectName + "  connections:\n\n";
				for(int i=0;i<sectList.size();i++) {
					int sIndex2 = sectList.get(i);
					String sectName2 = faultSectionData.get(sIndex2).getName();
					float dist = (float) sectionDistances[sIndex1][sIndex2];
					outputString += "\t"+ sectName2 + "\t"+dist+"\n";
				}
			}

			fw.write(outputString);
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		if (D) System.out.println(" - done");
	}
	
	
	/**
	 * This gets the seg-rate constraints by associating locations from UCERF2 Appendix 
	 * C to those sections created here.  This is a temporary solution until we have the new data.
	 * Also, these data could be added to an extended version of FaultSectionPrefData (e.g., as
	 * a list of SegRateConstraint objects, where the list would be to accomodate more than one
	 * on a given section)
	 */
	public ArrayList<SegRateConstraint> getPaleoSegRateConstraints() {
		
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+PALEO_DATA_FILE_NAME;
		segRateConstraints   = new ArrayList<SegRateConstraint>();
		try {				
			if(D) System.out.println("Reading Paleo Seg Rate Data from "+fullpathname);
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fullpathname));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			double lat, lon, rate, sigma, lower95Conf, upper95Conf;
			String siteName;
			for(int r=1; r<=lastRowIndex; ++r) {	
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell cell = row.getCell(1);
				if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
				lat = cell.getNumericCellValue();
				siteName = row.getCell(0).getStringCellValue().trim();
				lon = row.getCell(2).getNumericCellValue();
				rate = row.getCell(3).getNumericCellValue();
				sigma =  row.getCell(4).getNumericCellValue();
				lower95Conf = row.getCell(7).getNumericCellValue();
				upper95Conf =  row.getCell(8).getNumericCellValue();
				
				// get Closest section
				double minDist = Double.MAX_VALUE, dist;
				int closestFaultSectionIndex=-1;
				Location loc = new Location(lat,lon);
				for(int sectionIndex=0; sectionIndex<faultSectionData.size(); ++sectionIndex) {
					dist  = faultSectionData.get(sectionIndex).getFaultTrace().minDistToLine(loc);
					if(dist<minDist) {
						minDist = dist;
						closestFaultSectionIndex = sectionIndex;
					}
				}
				if(minDist>2) continue; // closest fault section is at a distance of more than 2 km
				
				// add to Seg Rate Constraint list
				String name = faultSectionData.get(closestFaultSectionIndex).getSectionName();
				SegRateConstraint segRateConstraint = new SegRateConstraint(name);
				segRateConstraint.setSegRate(closestFaultSectionIndex, rate, sigma, lower95Conf, upper95Conf);
				if(D) System.out.println("\t"+siteName+" (lat="+lat+", lon="+lon+") associated with "+name+
						" (section index = "+closestFaultSectionIndex+")\tdist="+(float)minDist+"\trate="+(float)rate+
						"\tsigma="+(float)sigma+"\tlower95="+(float)lower95Conf+"\tupper95="+(float)upper95Conf);
				segRateConstraints.add(segRateConstraint);
			}
		}catch(Exception e) {
			System.out.println("UNABLE TO READ PALEO DATA");
		}
		return segRateConstraints;
	}
	
	
	/**
	 * This returns the section indices for the rth rupture
	 * @param rthRup
	 * @return
	 */
	public ArrayList<Integer> getSectionIndicesForRupture(int rthRup) {
		return sectionClusterList.get(clusterIndexForRup[rthRup]).getSectionIndicesForRupture(rupIndexInClusterForRup[rthRup]);

	}


	
	/**
	 * This gets the slip on each section based on the value of slipModelType.
	 * The slips are in meters.  Note that taper slipped model wts slips by area
	 * to maintain moment balance (so it doesn't plot perfectly); do something about this?
	 * 
	 * Note that for two parallel faults that have some overlap, the slip won't be reduced
	 * along the overlap the way things are implemented here.
	 * 
	 * This has been spot checked, but needs a formal test.
	 *
	 */
	public double[] getSlipOnSectionsForRup(int rthRup) {
		
		ArrayList<Integer> sectionIndices = getSectionIndicesForRupture(rthRup);
		int numSects = sectionIndices.size();

		double[] slipsForRup = new double[numSects];
		
		// compute rupture area
		double[] sectArea = new double[numSects];
		double[] sectMoRate = new double[numSects];
		int index=0;
		for(Integer sectID: sectionIndices) {	
			FaultSectionPrefData sectData = faultSectionData.get(sectID);
			sectArea[index] = sectData.getLength()*sectData.getDownDipWidth()*1e6*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; 1e6 for sq-km --> sq-m
			sectMoRate[index] = FaultMomentCalc.getMoment(sectArea[index], sectSlipRateReduced[sectID]);
			index += 1;
		}
			 		
		double aveSlip = rupMeanMoment[rthRup]/(rupArea[rthRup]*FaultMomentCalc.SHEAR_MODULUS);  // in meters
		
		// for case segment slip is independent of rupture (constant), and equal to slip-rate * MRI
		if(slipModelType.equals(CHAR_SLIP_MODEL)) {
			throw new RuntimeException(CHAR_SLIP_MODEL+ " not yet supported");
		}
		// for case where ave slip computed from mag & area, and is same on all segments 
		else if (slipModelType.equals(UNIFORM_SLIP_MODEL)) {
			for(int s=0; s<slipsForRup.length; s++)
				slipsForRup[s] = aveSlip;
		}
		// this is the model where seg slip is proportional to segment slip rate 
		// (bumped up or down based on ratio of seg slip rate over wt-ave slip rate (where wts are seg areas)
		else if (slipModelType.equals(WG02_SLIP_MODEL)) {
			for(int s=0; s<slipsForRup.length; s++) {
				slipsForRup[s] = aveSlip*sectMoRate[s]*rupArea[rthRup]/(rupTotMoRateAvail[rthRup]*sectArea[s]);
			}
		}
		else if (slipModelType.equals(TAPERED_SLIP_MODEL)) {
			// note that the ave slip is partitioned by area, not length; this is so the final model is moment balanced.

			if(taperedSlipCDF == null) {
				taperedSlipCDF = new EvenlyDiscretizedFunc(0, 5001, 0.0002);
				taperedSlipPDF = new EvenlyDiscretizedFunc(0, 5001, 0.0002);
				double x,y, sum=0;
				int num = taperedSlipPDF.getNum();
				for(int i=0; i<num;i++) {
					x = taperedSlipPDF.getX(i);
					y = Math.pow(Math.sin(x*Math.PI), 0.5);
					taperedSlipPDF.set(i,y);
					sum += y;
				}
				// now make final PDF & CDF
				y=0;
				for(int i=0; i<num;i++) {
						y += taperedSlipPDF.getY(i);
						taperedSlipCDF.set(i,y/sum);
						taperedSlipPDF.set(i,taperedSlipPDF.getY(i)/sum);
//						System.out.println(taperedSlipCDF.getX(i)+"\t"+taperedSlipPDF.getY(i)+"\t"+taperedSlipCDF.getY(i));
				}
			}
			double normBegin=0, normEnd, scaleFactor;
			for(int s=0; s<slipsForRup.length; s++) {
				normEnd = normBegin + sectArea[s]/rupArea[rthRup];
				// fix normEnd values that are just past 1.0
				if(normEnd > 1 && normEnd < 1.00001) normEnd = 1.0;
				scaleFactor = taperedSlipCDF.getInterpolatedY(normEnd)-taperedSlipCDF.getInterpolatedY(normBegin);
				scaleFactor /= (normEnd-normBegin);
				slipsForRup[s] = aveSlip*scaleFactor;
				normBegin = normEnd;
			}
		}
		else throw new RuntimeException("slip model not supported");
/*		*/
		// check the average
//		if(D) {
//			double aveCalcSlip =0;
//			for(int s=0; s<slipsForRup.length; s++)
//				aveCalcSlip += slipsForRup[s]*sectArea[s];
//			aveCalcSlip /= rupArea[rthRup];
//			System.out.println("AveSlip & CalcAveSlip:\t"+(float)aveSlip+"\t"+(float)aveCalcSlip);
//		}

//		if (D) {
//			System.out.println("\tsectionSlip\tsectSlipRate\tsectArea");
//			for(int s=0; s<slipsForRup.length; s++) {
//				FaultSectionPrefData sectData = faultSectionData.get(sectionIndices.get(s));
//				System.out.println(s+"\t"+(float)slipsForRup[s]+"\t"+(float)sectData.getAveLongTermSlipRate()+"\t"+sectArea[s]);
//			}
//					
//		}
		return slipsForRup;		
	}
	
	
	/**
	 * This computes mag and various other attributes of the ruptures
	 */
	private void calcRuptureAttributes() {
	
		if(numRuptures == 0) // make sure this has been computed
			getNumRupRuptures();
		rupMeanMag = new double[numRuptures];
		rupMeanMoment = new double[numRuptures];
		rupTotMoRateAvail = new double[numRuptures];
		rupArea = new double[numRuptures];
		rupLength = new double[numRuptures];
		clusterIndexForRup = new int[numRuptures];
		rupIndexInClusterForRup = new int[numRuptures];
				
		int rupIndex=-1;
		for(int c=0;c<sectionClusterList.size();c++) {
			SectionCluster cluster = sectionClusterList.get(c);
			ArrayList<ArrayList<Integer>> clusterRups = cluster.getSectionIndicesForRuptures();
			for(int r=0;r<clusterRups.size();r++) {
				rupIndex+=1;
				clusterIndexForRup[rupIndex] = c;
				rupIndexInClusterForRup[rupIndex] = r;
				double totArea=0;
				double totLength=0;
				double totMoRate=0;
				ArrayList<Integer> sectsInRup = clusterRups.get(r);
				for(Integer sectID:sectsInRup) {
					FaultSectionPrefData sectData = faultSectionData.get(sectID);
					double length = sectData.getLength()*1e3;	// km --> m
					totLength += length;
					double area = length*sectData.getDownDipWidth()*1e3*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; km --> m on DDW
					totArea += area;
					totMoRate = FaultMomentCalc.getMoment(area, sectSlipRateReduced[sectID]);
				}
				rupArea[rupIndex] = totArea;
				rupLength[rupIndex] = totLength;
				double mag=0;
				for(MagAreaRelationship magArea: magAreaRelList) {
					mag += magArea.getMedianMag(totArea*1e-6)/magAreaRelList.size();
				}
				rupMeanMag[rupIndex] = mag;
//				rupMeanMag[rupIndex] = magAreaRel.getMedianMag(totArea*1e-6);
				rupMeanMoment[rupIndex] = MomentMagCalc.getMoment(rupMeanMag[rupIndex]);
				rupTotMoRateAvail[rupIndex]=totMoRate;
				// the above is meanMoment in case we add aleatory uncertainty later (aveMoment needed elsewhere); 
				// the above will have to be corrected accordingly as in SoSAF_SubSectionInversion
				// (mean moment != moment of mean mag if aleatory uncertainty included)
				// rupMeanMoment[rupIndex] = MomentMagCalc.getMoment(rupMeanMag[rupIndex])* gaussMFD_slipCorr; // increased if magSigma >0
			}
		}

	}


	public void doInversion(FindEquivUCERF2_Ruptures findUCERF2_Rups, ArrayList<ArrayList<Integer>> rupList) {
		
		double relativeSegRateWt = 0.01;  // weight of paleo-rate constraint relative to slip-rate constraint (recommended: 0.01)
		double relativeMagDistWt = 0.0;  // weight of UCERF2 magnitude-distribution constraint relative to slip-rate constraint - WORKS ONLY FOR NORTHERN CALIFORNIA INVERSION (recommended: 10.0)
		int numiter=0;  // number of simulated annealing iterations (increase this to decrease misfit)
		
		// Find number of rows in A matrix (equals the total number of constraints)
		int numRows=numSections + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size();  // number of rows used for slip-rate and paleo-rate constraints
		if (relativeMagDistWt > 0.0) {
			findUCERF2_Rups.getN_Cal_UCERF2_BackgrMFD_WithAfterShocks();
			findUCERF2_Rups.getN_CalTotalTargetGR_MFD();
			IncrementalMagFreqDist targetMagFreqDist = findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(); 
			numRows=numRows+targetMagFreqDist.getNum(); // add number of rows used for magnitude distribution constraint
		}
		
		
		// Components of matrix equation to invert (Ax=d)
		OpenMapRealMatrix A = new OpenMapRealMatrix(numRows,numRuptures); // A matrix
		double[] d = new double[numRows];	// data vector d
		double[] rupRateSolution = new double[numRuptures]; // final solution (x of Ax=d)
		double[] initial_state = new double[numRuptures];  // initial guess at solution x
		
		if(D) System.out.println("\nNumber of sections: " + numSections + ". Number of ruptures: " + numRuptures + ".\n");
		if(D) System.out.println("Total number of constraints (rows): " + numRows + ".\n");
		
		
		
		// PUT TOGETHER "A" MATRIX AND DATA VECTOR
		
		// Make sparse matrix of slip in each rupture & data vector of section slip rates
		int numElements = 0;  
		if(D) System.out.println("\nAdding slip per rup to A matrix ...");
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			ArrayList<Integer> sects = rupList.get(rup);
			for (int i=0; i < slips.length; i++) {
				A.addToEntry(sects.get(i),rup,slips[i]);
				if(D) numElements++;
			}
		}
		for (int sect=0; sect<numSections; sect++) d[sect] = sectSlipRateReduced[sect];	
		if(D) System.out.println("Number of nonzero elements in A matrix = "+numElements);

		
		
		// Make sparse matrix of paleo event probs for each rupture & data vector of mean event rates
		if (relativeSegRateWt > 0.0) {
			numElements = 0;
			if(D) System.out.println("\nAdding event rates to A matrix ...");
			for (int i=numSections; i<numSections+segRateConstraints.size(); i++) {
				SegRateConstraint constraint = segRateConstraints.get(i-numSections);
				d[i]=relativeSegRateWt * constraint.getMean() / constraint.getStdDevOfMean();
				double[] row = A.getRow(constraint.getSegIndex());
				for (int rup=0; rup<numRuptures; rup++) {
					if (row[rup]>0) {
						A.setEntry(i,rup,relativeSegRateWt * getProbVisible(rupMeanMag[rup]) / constraint.getStdDevOfMean());  
						if(D) numElements++;			
					}
				}
			}
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numElements);
		}

		
		
		// Constrain Solution MFD to equal the Target UCERF2 MFD (minus background eqs)
		// WORKS ONLY FOR NORTHERN CALIFORNIA INVERSION
		if (relativeMagDistWt > 0.0) {	
			numElements = 0;
			if(D) System.out.println("\nAdding magnitude constraint to A matrix (match Target UCERF2 minus background) ...");
			IncrementalMagFreqDist targetMagFreqDist = findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(); 
			targetMagFreqDist.setTolerance(0.1); // apply constraint to 0.1 magnitude-unit bins (this is 0.1 in the input MFDs also)		
			int rowIndex = numSections + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size(); // number of rows used for slip-rate and paleo-rate constraints
			for(int rup=0; rup<numRuptures;rup++) {
				double mag = rupMeanMag[rup];
				A.setEntry(rowIndex+targetMagFreqDist.getXIndex(mag),rup,relativeMagDistWt);
				numElements++;
			}		
			for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
				d[rowIndex]=targetMagFreqDist.getY(m)*relativeMagDistWt;
				rowIndex++; 
			}
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numElements);
		}
			
		
		
		// SOLVE THE INVERSE PROBLEM
		
		if(D) System.out.println("\nSolving inverse problem with simulated annealing ... ");
		// First pick starting solution: leave one of 3 below uncommented
//		initial_state = getGRStartingSolution(rupList,rupMeanMag);  // G-R initial solution
		initial_state = getUCERF2StartingSolution(); 				// UCERF2 rups as initial solution
//		for (int r=0; r<numRuptures; r++) initial_state[r]=0;		// initial solution of zero rupture rates
		
		rupRateSolution = SimulatedAnnealing.getSolution(A,d,initial_state, numiter);    
		
		
		// Plots of misfit, magnitude distribution, rupture rates
		PlotStuff(rupList, A, d, rupRateSolution, relativeMagDistWt, findUCERF2_Rups);
			

	}
	

	
	
	private void PlotStuff(ArrayList<ArrayList<Integer>> rupList, OpenMapRealMatrix A, double[] d, double[] rupRateSolution, double relativeMagDistWt, FindEquivUCERF2_Ruptures findUCERF2_Rups) {
		
		
		// Plot the rupture rates
		if(D) System.out.println("Making plot of rupture rates . . . ");
		ArrayList funcs = new ArrayList();		
		EvenlyDiscretizedFunc ruprates = new EvenlyDiscretizedFunc(0,(double)rupRateSolution.length-1,rupRateSolution.length);
		for(int i=0; i<rupRateSolution.length; i++)
			ruprates.set(i,rupRateSolution[i]);
		funcs.add(ruprates); 	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Inverted Rupture Rates"); 
		graph.setX_AxisLabel("Rupture Index");
		graph.setY_AxisLabel("Rate");
		
		
		// Plot the slip rate data vs. synthetics
		if(D) System.out.println("Making plot of slip rate misfit . . . ");
		ArrayList funcs2 = new ArrayList();		
		EvenlyDiscretizedFunc syn = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc data = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		for (int i = 0; i < numSections; i++) {
			for (int j = 0; j < A.getColumnDimension(); j++) {	
				syn.add(i,A.getEntry(i,j) * rupRateSolution[j]); // compute predicted data
			}
			data.add(i,d[i]);
		}	
		funcs2.add(syn);
		funcs2.add(data);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "Slip Rate Synthetics (blue) & Data (black)"); 
		graph2.setX_AxisLabel("Fault Section Index");
		graph2.setY_AxisLabel("Slip Rate");
		
		
		// Plot the paleo segment rate data vs. synthetics
		if(D) System.out.println("Making plot of event rate misfit . . . ");
		ArrayList funcs3 = new ArrayList();		
		EvenlyDiscretizedFunc finalEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc finalPaleoVisibleEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);	
		for (int r=0; r<numRuptures; r++) {
			ArrayList<Integer> rup=rupList.get(r);
			for (int i=0; i<rup.size(); i++) {			
				finalEventRateFunc.add(rup.get(i),rupRateSolution[r]);  
				finalPaleoVisibleEventRateFunc.add(rup.get(i),getProbVisible(rupMeanMag[r])*rupRateSolution[r]);  			
			}
		}	
		finalEventRateFunc.setName("Total Event Rates oer Section");
		finalPaleoVisibleEventRateFunc.setName("Paleo Visible Event Rates oer Section");
		funcs3.add(finalEventRateFunc);
		funcs3.add(finalPaleoVisibleEventRateFunc);	
		int num = segRateConstraints.size();
		ArbitrarilyDiscretizedFunc func;
		ArrayList obs_er_funcs = new ArrayList();
		SegRateConstraint constraint;
		for (int c = 0; c < num; c++) {
			func = new ArbitrarilyDiscretizedFunc();
			constraint = segRateConstraints.get(c);
			int seg = constraint.getSegIndex();
			func.set((double) seg - 0.0001, constraint.getLower95Conf());
			func.set((double) seg, constraint.getMean());
			func.set((double) seg + 0.0001, constraint.getUpper95Conf());
			func.setName(constraint.getFaultName());
			funcs3.add(func);
		}			
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(funcs3, "Synthetic Event Rates (total - black & paleo visible - blue) and Paleo Data (red)");
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(
				PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.BLACK,
				2));
		plotChars.add(new PlotCurveCharacterstics(
				PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
				Color.BLUE, 2));
		for (int c = 0; c < num; c++)
			plotChars.add(new PlotCurveCharacterstics(
					PlotColorAndLineTypeSelectorControlPanel.LINE_AND_CIRCLES,
					Color.RED, 1));
		graph3.setPlottingFeatures(plotChars);
		graph3.setX_AxisLabel("Fault Section Index");
		graph3.setY_AxisLabel("Event Rate (per year)");
		
		
		
		// plot magnitude histogram for final rupture rates
		if(D) System.out.println("Making plot of final magnitude distribution . . . ");
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<getNumRupRuptures();r++)
			magHist.add(rupMeanMag[r], rupRateSolution[r]);
		ArrayList funcs4 = new ArrayList();
		magHist.setName("Magnitude Distribution of SA Solution");
		magHist.setInfo("(number in each mag bin)");
		funcs4.add(magHist);
		// If the magnitude constraint is used, add a plot of the target MFD
		if (relativeMagDistWt > 0.0) {		
			IncrementalMagFreqDist targetMagFreqDist = findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(); 
			targetMagFreqDist.setTolerance(0.1); 
			targetMagFreqDist.setName("Target Magnitude Distribution");
			targetMagFreqDist.setInfo("UCERF2 Solution minus background (with aftershocks added back in)");
			funcs4.add(targetMagFreqDist);
		}
		GraphiWindowAPI_Impl graph4 = new GraphiWindowAPI_Impl(funcs4, "Magnitude Histogram for Final Rates"); 
		graph4.setX_AxisLabel("Magnitude");
		graph4.setY_AxisLabel("Frequency (per bin)");
		
		
		
	}


	/**
	 * This returns the probability that the given magnitude event will be
	 * observed at the ground surface. This is based on equation 4 of Youngs et
	 * al. [2003, A Methodology for Probabilistic Fault Displacement Hazard
	 * Analysis (PFDHA), Earthquake Spectra 19, 191-219] using the coefficients
	 * they list in their appendix for "Data from Wells and Coppersmith (1993)
	 * 276 worldwide earthquakes". Their function has the following
	 * probabilities:
	 * 
	 * mag prob 5 0.10 6 0.45 7 0.87 8 0.98 9 1.00
	 * 
	 * @return
	 */
	private double getProbVisible(double mag) {
		return Math.exp(-12.51 + mag * 2.053)
				/ (1.0 + Math.exp(-12.51 + mag * 2.053));
		/*
		 * Ray & Glenn's equation if(mag <= 5) return 0.0; else if (mag <= 7.6)
		 * return -0.0608*mag*mag + 1.1366*mag + -4.1314; else return 1.0;
		 */
	}

	private double[] getGRStartingSolution(ArrayList<ArrayList<Integer>> rupList, double[] rupMeanMag) {
		
		int numRup = rupMeanMag.length;
		double[] meanSlipRate = new double[numRup]; // mean slip rate per section for each rupture
		double[] initial_state = new double[numRup]; // starting model
		
		// Find mean slip rate per section for each rupture
		for (int rup=0; rup<numRup; rup++) {			
			ArrayList<Integer> sects = rupList.get(rup);
			meanSlipRate[rup]=0;
			for (int i=0; i<sects.size(); i++) {
				meanSlipRate[rup] += sectSlipRateReduced[sects.get(i)];	
			}
			meanSlipRate[rup] = meanSlipRate[rup]/sects.size();
		}
		
		// Find magnitude distribution of ruptures (as discretized)
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist.setTolerance(0.1);	// this makes it a histogram
		for(int rup=0; rup<numRup;rup++) {
			// magHist.add(rupMeanMag[rup], 1.0);
			// Each bin in the magnitude histogram should be weighted by the mean slip rates of those ruptures 
			// (since later we weight the ruptures by the mean slip rate, which would otherwise result in a non-G-R 
			// starting solution if the mean slip rates per rupture differed between magnitude bins)
			magHist.add(rupMeanMag[rup], meanSlipRate[rup]);  // each bin
		}
		
		
		// Set up initial (non-normalized) G-R rates for each rupture, normalized by meanSlipRate
		for (int rup=0; rup<numRup; rup++) {
			double magToUse = rupMeanMag[rup];
			if (magToUse<6.5)
				magToUse = 6.5; // Use G-R rate for M6.5 for mags less than 6.5.  This prevents crazy high rates for far and few between M<6.5 rups	
			initial_state[rup] = Math.pow(10,-magToUse) * meanSlipRate[rup] / magHist.getY(magToUse);
		}
		
		// Find normalization for all ruptures (so that sum_over_rups(slip*rate)=total slip per year)
		double totalSlipInRups=0;
		for (int rup=0; rup<numRup; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			for (int i=0; i<slips.length; i++) 
				totalSlipInRups += slips[i]*initial_state[rup];	
		}
		double targetTotalSlip=0;
		for (int sect=0; sect<sectSlipRateReduced.length; sect++) 
			targetTotalSlip += sectSlipRateReduced[sect];	
		double normalization = targetTotalSlip/totalSlipInRups;
		
		// Adjust G-R rates to match target moment
		for (int rup=0; rup<numRup; rup++) 
			initial_state[rup]=initial_state[rup]*normalization;
		
		
		// plot magnitude histogram for the inversion starting model
		IncrementalMagFreqDist magHist2 = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist2.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<getNumRupRuptures();r++)
			magHist2.add(rupMeanMag[r], initial_state[r]);
		ArrayList funcs = new ArrayList();
		funcs.add(magHist2);
		magHist2.setName("Magnitude Distribution of Starting Model (before Annealing)");
		magHist2.setInfo("(number in each mag bin)");
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Magnitude Histogram"); 
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Frequency (per bin)");
		
		
		
		
		return initial_state;
		
	}

	private double[] getUCERF2StartingSolution() {
	
		double[] initial_state = new double[numRuptures]; // starting model
		
		for (int r=0; r<numRuptures; r++) {
			double[] magAndRate = ucerf2_magsAndRates.get(r);
			if(magAndRate != null) 
				initial_state[r] = magAndRate[1];
			else
				initial_state[r] = 0;
		
		}

		return initial_state;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
