package scratch.UCERF3.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.PrefFaultSectionDataFinal;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;

import scratch.UCERF3.inversion.InversionFaultSystemRupSet;

import com.google.common.base.Preconditions;


/**
 * This is a general utility class for obtaining a deformation model (defined as an 
 * ArrayList<FaultSectionPrefData>), plus other derivative information.  This class
 * stores files in order to recreate info more quickly, so these files may need to
 * be deleted if things change.
 * 
 * @author Field
 *
 */
public class DeformationModelFetcher {

	protected final static boolean D = true;  // for debugging

	//	 Stepover fix for Elsinor
	private final static int GLEN_IVY_STEPOVER_FAULT_SECTION_ID = 297;
	private final static int TEMECULA_STEPOVER_FAULT_SECTION_ID = 298;
	private final static int ELSINORE_COMBINED_STEPOVER_FAULT_SECTION_ID = 402;
	//	 Stepover fix for San Jacinto
	private final static int SJ_VALLEY_STEPOVER_FAULT_SECTION_ID = 290;
	private final static int SJ_ANZA_STEPOVER_FAULT_SECTION_ID = 291;
	private final static int SJ_COMBINED_STEPOVER_FAULT_SECTION_ID = 401;

	DefModName chosenDefModName;


	public enum DefModName {
		UCERF2_NCAL,
		UCERF2_BAYAREA,
		UCERF2_ALL,
		UCERF3_FM_3_1_KLUDGE;
	}

	String fileNamePrefix;
	File precomputedDataDir;

	ArrayList<FaultSectionPrefData> faultSubSectPrefDataList;
	HashMap<Integer, FaultSectionPrefData> faultSubSectPrefDataIDMap;

	/** Set the UCERF2 deformation model ID
	 * D2.1 = 82
	 * D2.2 = 83
	 * D2.3 = 84
	 * D2.4 = 85
	 * D2.5 = 86
	 * D2.6 = 87
	 */
	static int ucerf2_DefModelId = 82;
	static boolean alphabetize = true;


	/**
	 * Constructor
	 * 
	 * @param name - then name of the desire deformation model (from the DefModName enum here).
	 * @param precomputedDataDir - the dir where pre-computed data can be found (for faster instantiation)
	 */
	public DeformationModelFetcher(DefModName name, File precomputedDataDir) {
		this.precomputedDataDir = precomputedDataDir;
		chosenDefModName = name;
		if(name == DefModName.UCERF2_NCAL) {
			faultSubSectPrefDataList = createNorthCal_UCERF2_SubSections(false, 0.5);
			fileNamePrefix = "nCal_0_82_"+faultSubSectPrefDataList.size();	// now hard coded as no NaN slip rates (the 0), defModID=82, & number of sections
		}
		else if(name == DefModName.UCERF2_ALL) {
			faultSubSectPrefDataList = createAll_UCERF2_SubSections(false, 0.5);
			fileNamePrefix = "all_0_82_"+faultSubSectPrefDataList.size();			
		}
		else if (name == DefModName.UCERF2_BAYAREA) {
			faultSubSectPrefDataList = this.createBayAreaSubSections(0.5);
			fileNamePrefix = "bayArea_0_82_"+faultSubSectPrefDataList.size();						
		} else if (name == DefModName.UCERF3_FM_3_1_KLUDGE) {
			faultSubSectPrefDataList = this.createUCERF3_KludgeSections(0.5);
			fileNamePrefix = "ucerf3_kludge_3_1_"+faultSubSectPrefDataList.size();		
		}

		faultSubSectPrefDataIDMap = new HashMap<Integer, FaultSectionPrefData>();
		for (FaultSectionPrefData data : faultSubSectPrefDataList) {
			int id = data.getSectionId();
			Preconditions.checkState(!faultSubSectPrefDataIDMap.containsKey(id),
					"multiple sub sections exist with ID: "+id);
			faultSubSectPrefDataIDMap.put(id, data);
		}
	}

	public DefModName getDefModName() {
		return chosenDefModName;
	}

	public ArrayList<FaultSectionPrefData> getSubSectionList() {
		return faultSubSectPrefDataList;
	}


	/**
	 * This gets creates UCERF2 subsections for the entire region.
	 * @param includeSectionsWithNaN_slipRates
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 * 
	 */
	private ArrayList<FaultSectionPrefData> createAll_UCERF2_SubSections(boolean includeSectionsWithNaN_slipRates, double maxSubSectionLength) {

		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = getAll_UCERF2Sections(includeSectionsWithNaN_slipRates);

		// make subsection data
		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectionIndex=0;
		for(int i=0; i<allFaultSectionPrefData.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)allFaultSectionPrefData.get(i);
			double maxSectLength = faultSectionPrefData.getOrigDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectionIndex);
			subSectionIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}		

		return subSectionPrefDataList;
	}



	/**
	 * This gets all UCERF2 subsection data in the N. Cal RELM region.
	 * Note that this has to use a modified version of CaliforniaRegions.RELM_NOCAL() in 
	 * order to not include the Parkfield section (one that uses BorderType.GREAT_CIRCLE 
	 * rather than the default BorderType.MERCATOR_LINEAR).
	 * 
	 * @param includeSectionsWithNaN_slipRates
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 */
	private ArrayList<FaultSectionPrefData>  createNorthCal_UCERF2_SubSections(boolean includeSectionsWithNaN_slipRates, double maxSubSectionLength) {

		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = getAll_UCERF2Sections(includeSectionsWithNaN_slipRates);

		// remove those that don't have at least one trace end-point in in the N Cal RELM region
		Region relm_nocal_reg = new CaliforniaRegions.RELM_NOCAL();
		Region mod_relm_nocal_reg = new Region(relm_nocal_reg.getBorder(), BorderType.GREAT_CIRCLE); // needed to exclude Parkfield
		ArrayList<FaultSectionPrefData> nCalFaultSectionPrefData = new ArrayList<FaultSectionPrefData>();
		for(FaultSectionPrefData sectData:allFaultSectionPrefData) {
			FaultTrace trace = sectData.getFaultTrace();
			Location endLoc1 = trace.get(0);
			Location endLoc2 = trace.get(trace.size()-1);
			if(mod_relm_nocal_reg.contains(endLoc1) || mod_relm_nocal_reg.contains(endLoc2))
				nCalFaultSectionPrefData.add(sectData);
		}

		// write sections IDs and names
		if (D) {
			System.out.println("Fault Sections in the N Cal RELM region");
			for(int i=0; i< nCalFaultSectionPrefData.size();i++)
				System.out.println("\t"+nCalFaultSectionPrefData.get(i).getSectionId()+"\t"+nCalFaultSectionPrefData.get(i).getName());			
		}

		// make subsection data
		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectionIndex=0;
		for(int i=0; i<nCalFaultSectionPrefData.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)nCalFaultSectionPrefData.get(i);
			double maxSectLength = faultSectionPrefData.getOrigDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectionIndex);
			subSectionIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}		

		return subSectionPrefDataList;
	}


	/**
	 * This gets all section data from the UCERF2 deformation model, where the list is alphabetized,
	 * and section with NaN slip rates are removed.  
	 * Note that overlapping sections on the Elsinore and San Jacinto are replaced with the combined sections
	 * (the ones used in the UCERF2 un-segmented models rather than the segmented models).  This means
	 * the sections here are not exactly the same as in the official UCERF2 deformation models.
	 */
	private ArrayList<FaultSectionPrefData> getAll_UCERF2Sections(boolean includeSectionsWithNaN_slipRates) {

		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> prelimFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(ucerf2_DefModelId);

		//		ArrayList<FaultSectionPrefData> allFaultSectionPrefData=prelimFaultSectionPrefData;

		// ****  Make a revised list, replacing step over sections on Elsinore and San Jacinto with the combined sections
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData= new ArrayList<FaultSectionPrefData>();

		FaultSectionPrefData glenIvyStepoverfaultSectionPrefData=null,temeculaStepoverfaultSectionPrefData=null,anzaStepoverfaultSectionPrefData=null,valleyStepoverfaultSectionPrefData=null;

		for(FaultSectionPrefData data : prelimFaultSectionPrefData) {
			int id = data.getSectionId();
			if(id==GLEN_IVY_STEPOVER_FAULT_SECTION_ID) {
				glenIvyStepoverfaultSectionPrefData = data;
				continue;
			}
			else if(id==TEMECULA_STEPOVER_FAULT_SECTION_ID) {
				temeculaStepoverfaultSectionPrefData = data;
				continue;
			}
			else if(id==SJ_ANZA_STEPOVER_FAULT_SECTION_ID) {
				anzaStepoverfaultSectionPrefData = data;
				continue;
			}
			else if(id==SJ_VALLEY_STEPOVER_FAULT_SECTION_ID) {
				valleyStepoverfaultSectionPrefData = data;
				continue;
			}
			else {
				allFaultSectionPrefData.add(data);
			}
		}
		PrefFaultSectionDataFinal faultSectionDataFinal = new PrefFaultSectionDataFinal();

		FaultSectionPrefData newElsinoreSectionData = faultSectionDataFinal.getFaultSectionPrefData(ELSINORE_COMBINED_STEPOVER_FAULT_SECTION_ID);
		newElsinoreSectionData.setAveSlipRate(glenIvyStepoverfaultSectionPrefData.getOrigAveSlipRate()+temeculaStepoverfaultSectionPrefData.getOrigAveSlipRate());
		newElsinoreSectionData.setSlipRateStdDev(glenIvyStepoverfaultSectionPrefData.getOrigSlipRateStdDev()+temeculaStepoverfaultSectionPrefData.getOrigSlipRateStdDev());
		allFaultSectionPrefData.add(newElsinoreSectionData);

		FaultSectionPrefData newSanJacinntoSectionData = faultSectionDataFinal.getFaultSectionPrefData(SJ_COMBINED_STEPOVER_FAULT_SECTION_ID);
		newSanJacinntoSectionData.setAveSlipRate(anzaStepoverfaultSectionPrefData.getOrigAveSlipRate()+valleyStepoverfaultSectionPrefData.getOrigAveSlipRate());
		newSanJacinntoSectionData.setSlipRateStdDev(anzaStepoverfaultSectionPrefData.getOrigSlipRateStdDev()+valleyStepoverfaultSectionPrefData.getOrigSlipRateStdDev());
		allFaultSectionPrefData.add(newSanJacinntoSectionData);


		//Alphabetize:
		if(alphabetize)
			Collections.sort(allFaultSectionPrefData, new NamedComparator());

		// remove those with no slip rate if appropriate
		if(!includeSectionsWithNaN_slipRates) {
			if (D)System.out.println("Removing the following due to NaN slip rate:");
			for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
				if(Double.isNaN(allFaultSectionPrefData.get(i).getOrigAveSlipRate())) {
					if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
					allFaultSectionPrefData.remove(i);
				}	 
		}

		/*
		// REMOVE CREEPING SECTION for now (aseismicity not incorporated correctly)
		if (D)System.out.println("Removing SAF Creeping Section.");
		for(int i=0; i< allFaultSectionPrefData.size();i++) {
			if (allFaultSectionPrefData.get(i).getSectionId() == 57)
				allFaultSectionPrefData.remove(i);
		}
		 */
		/*		
		if(D) {
			System.out.println("FINAL SECTIONS");
			for(FaultSectionPrefData data : allFaultSectionPrefData) {
				System.out.println(data.getName());
			}
		}
		 */		
		return allFaultSectionPrefData;
	}


	/**
	 * This gets all section data & creates sub-sections for the SF Bay Area
	 * 
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 */
	private ArrayList<FaultSectionPrefData> createBayAreaSubSections(double maxSubSectionLength) {

		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();	

		ArrayList<Integer> faultSectionIds = new ArrayList<Integer>();
		// Bay Area Faults
		faultSectionIds.add(26); //  San Andreas (Offshore)
		faultSectionIds.add(27); //  San Andreas (North Coast)
		faultSectionIds.add(67); //  San Andreas (Peninsula)
		faultSectionIds.add(56); //  San Andreas (Santa Cruz Mtn) 
		faultSectionIds.add(25); //  Rodgers Creek
		faultSectionIds.add(68); //  Hayward (No)
		faultSectionIds.add(69); //  Hayward (So)
		faultSectionIds.add(4);  //  Calaveras (No)
		faultSectionIds.add(5);  //  Calaveras (Central)
		faultSectionIds.add(55); //  Calaveras (So)
		faultSectionIds.add(71); //  Green Valley (No)
		faultSectionIds.add(1);  //  Green Valley (So)
		faultSectionIds.add(3);  //  Concord
		faultSectionIds.add(12); //  San Gregorio (No)
		faultSectionIds.add(29); //  San Gregorio (So)
		faultSectionIds.add(6);  //  Greenville (No)
		faultSectionIds.add(7);  //  Greenville (So)
		faultSectionIds.add(2);  //  Mount Diablo Thrust


		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		for (int i = 0; i < faultSectionIds.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = deformationModelPrefDB.getFaultSectionPrefData(ucerf2_DefModelId, faultSectionIds.get(i));
			double maxSectLength = faultSectionPrefData.getOrigDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectIndex);
			subSectIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}

		return subSectionPrefDataList;
	}


	/**
	 * KLUDGE! FM 3.1, fake slips
	 * 
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 */
	private ArrayList<FaultSectionPrefData> createUCERF3_KludgeSections(double maxSubSectionLength) {
		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
		PrefFaultSectionDataDB_DAO pref2db = new PrefFaultSectionDataDB_DAO(db);
		ArrayList<FaultSectionPrefData> datas = pref2db.getAllFaultSectionPrefData();
		FaultModelDB_DAO fm2db = new FaultModelDB_DAO(db);
		ArrayList<Integer> faultSectionIds = fm2db.getFaultSectionIdList(101);

		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		//		for (int i = 0; i < faultSectionIds.size(); ++i) {
		for (FaultSectionPrefData data : datas) {
			if (!faultSectionIds.contains(data.getSectionId()))
				continue;
			double maxSectLength = data.getOrigDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = data.getSubSectionsList(maxSectLength, subSectIndex);
			subSectIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}

		return subSectionPrefDataList;
	}

	private StirlingGriddedSurface getSurfaceForSubSect(FaultSectionPrefData data) {
		return data.getStirlingGriddedSurface(1.0, false, false);
	}

	private Map<IDPairing, Double> readMapFile(File file) throws IOException {
		HashMap<IDPairing, Double> map = new HashMap<IDPairing, Double>();

		// first value is integer specifying length
		// Wrap the FileInputStream with a DataInputStream
		FileInputStream file_input = new FileInputStream (file);
		DataInputStream data_in    = new DataInputStream (file_input );
		int size = data_in.readInt();
		// format <id1><id2><distance> (<int><int><double>)
		for (int i=0; i<size; i++) {
			int id1 = data_in.readInt();
			int id2 = data_in.readInt();
			double dist = data_in.readDouble();
			IDPairing ind = new IDPairing(id1, id2);
			map.put(ind, dist);
		}
		data_in.close ();
		return map;
	}

	private void writeMapFile(Map<IDPairing, Double> map, File file) throws IOException {
		// Create an output stream to the file.
		FileOutputStream file_output = new FileOutputStream (file);
		// Wrap the FileOutputStream with a DataOutputStream
		DataOutputStream data_out = new DataOutputStream (file_output);
		//				for(int i=0; i<numSubSections;i++)
		//					for(int j=i; j<numSubSections;j++)
		//						data_out.writeDouble(subSectionDistances[i][j]);
		Set<IDPairing> keys = map.keySet();
		data_out.writeInt(keys.size());
		// format <id1><id2><distance> (<int><int><double>)
		for (IDPairing ind : keys) {
			data_out.writeInt(ind.getID1());
			data_out.writeInt(ind.getID2());
			data_out.writeDouble(map.get(ind));
		}
		// Close file
		file_output.close ();
	}

	/**
	 * This computes the distances between subsection if it hasn't already been done.
	 * (otherwise the values are read from a file).<br>
	 * <br>
	 * File format:<br>
	 * [length]<br>
	 * [id1][id2][distance]<br>
	 * [id1][id2][distance]<br>
	 * ...<br>
	 * where length and IDs are integers, and distance is a double
	 */
	public Map<IDPairing, Double> getSubSectionDistanceMap(double maxDistance) {

		int numSubSections = faultSubSectPrefDataList.size();
		// map from [id1, id2] to the distance. index1 is always less than index2
		Map<IDPairing, Double> distances;

		// construct filename
		String name = fileNamePrefix+"_Distances";
		name += "_"+(float)maxDistance+"km";
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+name;
		File file = new File (fullpathname);

		//		 Read data if already computed and saved
		if(file.exists()) {
			if(D) System.out.println("Reading existing file: "+ name);
			try {
				distances = readMapFile(file);
			} catch  (IOException e) {
				System.out.println ( "IO Exception =: " + e );
				throw ExceptionUtils.asRuntimeException(e);
			}

		}
		else {// Calculate new distance matrix & save to a file
			System.out.println("Calculating data and will save to file: "+name);

			distances = new HashMap<IDPairing, Double>();

			int progress = 0, progressInterval=10;  // for progress report
			System.out.print("Dist Calc % Done:");
			for(int a=0;a<numSubSections;a++) {
				if (100*a/numSubSections > progress) {
					System.out.print("\t"+progress);
					progress += progressInterval;
				}
				//				StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 2.0);
				FaultSectionPrefData data1 = faultSubSectPrefDataList.get(a);
				StirlingGriddedSurface surf1 = getSurfaceForSubSect(data1);
				//				StirlingGriddedSurface surf1 = new StirlingGriddedSurface(data1.getSimpleFaultData(false), 1.0, 1.0);

				for(int b=a+1;b<numSubSections;b++) { // a+1 because array is initialized to zero
					//					StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 2.0);
					FaultSectionPrefData data2 = faultSubSectPrefDataList.get(b);
					//					StirlingGriddedSurface surf2 = new StirlingGriddedSurface(data2.getSimpleFaultData(false), 1.0, 1.0);
					StirlingGriddedSurface surf2 = getSurfaceForSubSect(data2);
					//					double minDist = surf1.getMinDistance(surf2);
					//					subSectionDistances[a][b] = minDist;
					//					subSectionDistances[b][a] = minDist;
					double minDist = QuickSurfaceDistanceCalculator.calcMinDistance(surf1, surf2, maxDistance*3);
					if (minDist < maxDistance) {
						IDPairing ind = new IDPairing(data1.getSectionId(), data2.getSectionId());
						Preconditions.checkState(!distances.containsKey(ind), "distances already computed for given sections!" +
								" duplicate sub section ids?");
						distances.put(ind, minDist);
					}
				}
			}
			// Now save to a binary file
			try {
				writeMapFile(distances, file);
			}
			catch (IOException e) {
				System.out.println ("IO exception = " + e );
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		System.out.print("\tDONE.\n");

		return distances;
	}


	/**
	 * This creates (or reads) a matrix giving the azimuth between the midpoint of each subSection.
	 * @return
	 */
	public Map<IDPairing, Double> getSubSectionAzimuthMap(Set<IDPairing> indices) {

		Map<IDPairing, Double> azimuths;

		// construct filename
		//		String name = fileNamePrefix+"_Azimuths_"+indices.size()+"inds";
		//		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+name;
		//		File file = new File (fullpathname);

		//		// Read data if already computed and saved
		//		if(file.exists()) {
		//			if(D) System.out.println("Reading existing file: "+ name);
		//			try {
		//				azimuths = readMapFile(file);
		//			} catch  (IOException e) {
		//				System.out.println ( "IO Exception =: " + e );
		//				throw ExceptionUtils.asRuntimeException(e);
		//			}
		//
		//		}
		//		else {// Calculate new distance matrix & save to a file
		//		System.out.println("Calculating azimuth data and will save to file: "+name);

		azimuths = new HashMap<IDPairing, Double>();

		int progress = 0, progressInterval=10;  // for progress report
		System.out.print("Azimuth Calc % Done:");

		int cnt = 0;
		for (IDPairing ind : indices) {
			if (100*(double)cnt/indices.size() > progress) {
				System.out.print("\t"+progress);
				progress += progressInterval;
			}
			cnt++;
			FaultSectionPrefData data1 = faultSubSectPrefDataIDMap.get(ind.getID1());
			StirlingGriddedSurface surf1 =  getSurfaceForSubSect(data1);
			Location loc1 = surf1.getLocation(surf1.getNumRows()/2, surf1.getNumCols()/2);
			FaultSectionPrefData data2 = faultSubSectPrefDataIDMap.get(ind.getID2());
			StirlingGriddedSurface surf2 =  getSurfaceForSubSect(data2);
			Location loc2 = surf2.getLocation((int)(surf2.getNumRows()/2), (int)(surf2.getNumCols()/2));
			azimuths.put(ind, LocationUtils.azimuth(loc1, loc2));
			IDPairing ind_bak = ind.getReversed();
			azimuths.put(ind_bak, LocationUtils.azimuth(loc2, loc1));
			//			}
			//			// Now save to a binary file
			//			try {
			//				writeMapFile(azimuths, file);
			//			}
			//			catch (IOException e) {
			//				System.out.println ("IO exception = " + e );
			//				throw ExceptionUtils.asRuntimeException(e);
			//			}
		}
		System.out.print("\tDONE.\n");

		return azimuths;
	}
}
