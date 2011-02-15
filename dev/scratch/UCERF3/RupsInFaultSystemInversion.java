package scratch.UCERF3;

import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;


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
 * @author field & Page
 *
 */
public class RupsInFaultSystemInversion {

	protected final static boolean D = true;  // for debugging


	ArrayList<FaultSectionPrefData> faultSectionData;
	double sectionDistances[][],sectionAzimuths[][];;
	double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange;
	int minNumSectInRup;
	MagAreaRelationship magAreaRel;

	String endPointNames[];
	Location endPointLocs[];
	int numSections;
	ArrayList<ArrayList<Integer>> sectionConnectionsListList, endToEndSectLinksList;
	
	ArrayList<SegRateConstraint> segRateConstraints;

	ArrayList<SectionCluster> sectionClusterList;
	
	// slip model:
	private String slipModelType;
	public final static String CHAR_SLIP_MODEL = "Characteristic (Dsr=Ds)";
	public final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar (Dsr=Dr)";
	public final static String WG02_SLIP_MODEL = "WGCEP-2002 model (Dsr prop to Vs)";
	public final static String TAPERED_SLIP_MODEL = "Tapered Ends ([Sin(x)]^0.5)";
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;



	/**
	 * 
	 * @param faultSectionData - this assumes subsections (if any) are in proper order (have adjacent indices)
	 * @param sectionDistances
	 * @param subSectionAzimuths
	 * @param maxJumpDist
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 * @param minNumSectInRup
	 */
	public RupsInFaultSystemInversion(ArrayList<FaultSectionPrefData> faultSectionData,
			double[][] sectionDistances, double[][] subSectionAzimuths, double maxJumpDist, 
			double maxAzimuthChange, double maxTotAzimuthChange, int minNumSectInRup,
			MagAreaRelationship magAreaRel) {

		if(D) System.out.println("Instantiating RupsInFaultSystemInversion");
		this.faultSectionData = faultSectionData;
		this.sectionDistances = sectionDistances;
		this.sectionAzimuths = subSectionAzimuths;
		this.maxJumpDist=maxJumpDist;
		this.maxAzimuthChange=maxAzimuthChange; 
		this.maxTotAzimuthChange=maxTotAzimuthChange;
		this.minNumSectInRup=minNumSectInRup;
		this.magAreaRel=magAreaRel;

		// write out settings if in debug mode
		if(D) System.out.println("faultSectionData.size() = "+faultSectionData.size() +
				"; sectionDistances.length = "+sectionDistances.length +
				"; subSectionAzimuths.length = "+subSectionAzimuths.length +
				"; maxJumpDist = "+maxJumpDist +
				"; maxAzimuthChange = "+maxAzimuthChange + 
				"; maxTotAzimuthChange = "+maxTotAzimuthChange +
				"; minNumSectInRup = "+minNumSectInRup);

		// check that indices are same as IDs
		for(int i=0; i<faultSectionData.size();i++)
			if(faultSectionData.get(i).getSectionId() != i)
				throw new RuntimeException("RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();

		// make the list of nearby sections for each section (branches)
		if(D) System.out.println("Making sectionConnectionsListList");
		computeCloseSubSectionsListList();
		if(D) System.out.println("Done making sectionConnectionsListList");
		
		getPaleoSegRateConstraints();


		// make the list of SectionCluster objects 
		// (each represents a set of nearby sections and computes the possible
		//  "ruptures", each defined as a list of sections in that rupture)
		/* 
		makeClusterList();

		for(int i=0;i<this.sectionClusterList.size(); i++)
			System.out.println("Cluster "+i+" has "+getCluster(i).size()+" sections & "+getCluster(i).getNumRuptures()+" ruptures");
//			System.out.println("Cluster "+i+" has "+getCluster(i).getNumRuptures()+" ruptures");
*/		
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


	public int getNumRupRuptures() {
		int num = 0;
		for(int i=0; i<sectionClusterList.size();i++) {
			num += sectionClusterList.get(i).getNumRuptures();
		}
		return num;
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
					sectionAzimuths, maxAzimuthChange, maxTotAzimuthChange);
			newCluster.add(firstSubSection);
			if (D) System.out.println("\tfirst is "+faultSectionData.get(firstSubSection).getName());
			addLinks(firstSubSection, newCluster);
			// remove the used subsections from the available list
			for(int i=0; i<newCluster.size();i++) availableSections.remove(newCluster.get(i));
			// add this cluster to the list
			sectionClusterList.add(newCluster);
			if (D) System.out.println(newCluster.size()+"\tsubsections in cluster #"+sectionClusterList.size()+"\t"+
					availableSections.size()+"\t subsections left to allocate");
		}
	}


	private void addLinks(int subSectIndex, SectionCluster list) {
		ArrayList<Integer> branches = sectionConnectionsListList.get(subSectIndex);
		for(int i=0; i<branches.size(); i++) {
			Integer subSect = branches.get(i);
			if(!list.contains(subSect)) {
				list.add(subSect);
				addLinks(subSect, list);
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
	private void getPaleoSegRateConstraints() {
		String SEG_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/data/Appendix_C_Table7_091807.xls";
		segRateConstraints   = new ArrayList<SegRateConstraint>();
		try {				
			if(D) System.out.println("Reading Paleo Seg Rate Data from "+SEG_RATE_FILE_NAME);
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(SEG_RATE_FILE_NAME));
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
			e.printStackTrace();
		}
	}

	/**
	 * This gets the slip on each section based on the value of slipModelType.
	 * The slips are in meters.
	 *
	 */
	private double[] getSlipOnSectionsForRup(ArrayList<Integer> sectIndicesForRup) {
		
		double[] slipsForRup = new double[sectIndicesForRup.size()];
		
		// compute rupture area
		double rupAreaInKM=0, totMoRate=0;
		double[] sectArea = new double[sectIndicesForRup.size()];
		double[] sectMoRate = new double[sectIndicesForRup.size()];
		int index=0;
		for(Integer sectIndex: sectIndicesForRup) {
			FaultSectionPrefData sectData = faultSectionData.get(sectIndex);
			sectArea[index] = sectData.getDownDipWidth()*sectData.getLength()*(1.0-sectData.getAseismicSlipFactor());
			sectMoRate[index] = FaultMomentCalc.getMoment(sectArea[index]*1e6, sectData.getAveLongTermSlipRate());    // 1e6 converts to meters-sq
			rupAreaInKM += sectArea[index];
			totMoRate += sectMoRate[index];
			index += 1;
		}
			 
		double rupMeanMoment = MomentMagCalc.getMoment(magAreaRel.getMedianMag(rupAreaInKM));
		// the above is meanMoment in case we add aleatory uncertainty later (aveMoment needed below); 
		// the above will have to be corrected accordingly as in SoSAF_SubSectionInversion
		
		double aveSlip = rupMeanMoment/(rupAreaInKM*1e6*FaultMomentCalc.SHEAR_MODULUS);  // 1e6 converts to meters
		
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
				slipsForRup[s] = aveSlip*sectMoRate[s]*rupAreaInKM/(totMoRate*sectArea[s]);
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
				normEnd = normBegin + sectArea[s]/rupAreaInKM;
				// fix normEnd values that are just past 1.0
				if(normEnd > 1 && normEnd < 1.00001) normEnd = 1.0;
				scaleFactor = taperedSlipCDF.getInterpolatedY(normEnd)-taperedSlipCDF.getInterpolatedY(normBegin);
				scaleFactor /= (normEnd-normBegin);
				slipsForRup[s] = aveSlip*scaleFactor;
				normBegin = normEnd;
			}
			/*
				if(rup == num_rup-1) { // check results
					double d_aveTest=0;
					for(int seg=0; seg<num_seg; seg++)
						d_aveTest += segSlipInRup[seg][rup]*segArea[seg]/rupArea[rup];
					System.out.println("AveSlipCheck: " + (float) (d_aveTest/aveSlip));
				}
			 */
		}
		else throw new RuntimeException("slip model not supported");
		
		if (D) for(int s=0; s<slipsForRup.length; s++) System.out.println(s+"\t"+slipsForRup[s]);

		return slipsForRup;		
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
