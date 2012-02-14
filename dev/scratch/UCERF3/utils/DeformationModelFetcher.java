package scratch.UCERF3.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FaultUtils;
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
import scratch.UCERF3.utils.DeformationModelFileParser.DeformationSection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


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
		UCERF3_ABM("ABM_slip_rake_feb06.txt"),
		UCERF3_GEOBOUND("geobound_slip_rake_feb06.txt"),
		UCERF3_NEOKINEMA("neokinema_slip_rake_feb06.txt"),
		UCERF3_ZENG("zeng_slip_rake_feb06.txt"),
		UCERF3_GEOLOGIC("geologic_2012_02_09.csv");
		
		private DefModName() {
			this(null);
		}
		
		private String fileName;
		
		private DefModName(String fileName) {
			this.fileName = fileName;
		}
		
		public URL getDataFileURL() {
			if (fileName == null)
				return null;
			return this.getClass().getResource("/scratch/UCERF3/preComputedData/DeformationModels/"+fileName);
		}
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
		double maxSubSectionLength = 0.5; // in units of DDW
		this.precomputedDataDir = precomputedDataDir;
		chosenDefModName = name;
		if(name == DefModName.UCERF2_NCAL) {
			faultSubSectPrefDataList = createNorthCal_UCERF2_SubSections(false, maxSubSectionLength);
			fileNamePrefix = "nCal_0_82_"+faultSubSectPrefDataList.size();	// now hard coded as no NaN slip rates (the 0), defModID=82, & number of sections
		}
		else if(name == DefModName.UCERF2_ALL) {
			faultSubSectPrefDataList = createAll_UCERF2_SubSections(false, maxSubSectionLength);
			fileNamePrefix = "all_0_82_"+faultSubSectPrefDataList.size();			
		}
		else if (name == DefModName.UCERF2_BAYAREA) {
			faultSubSectPrefDataList = this.createBayAreaSubSections(maxSubSectionLength);
			fileNamePrefix = "bayArea_0_82_"+faultSubSectPrefDataList.size();						
		} else if (name.getDataFileURL() != null) {
			int fmID = 101;
			URL url = name.getDataFileURL();
			try {
				System.out.println("Loading def model from: "+url);
				HashMap<Integer,DeformationSection> model = DeformationModelFileParser.load(url);
				System.out.println("Loading fault model wtih ID: "+fmID);
				ArrayList<FaultSectionPrefData> sections = loadUCERF3FaultModel(fmID);
				System.out.println("Combining model with sections...");
				faultSubSectPrefDataList = loadUCERF3DefModel(sections, model, maxSubSectionLength);
				fileNamePrefix = name.name()+"_"+faultSubSectPrefDataList.size();
				System.out.println("DONE.");
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
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
		int faultModelID = 101;
		
		ArrayList<FaultSectionPrefData> fm = loadUCERF3FaultModel(faultModelID);
		
		ArrayList<FaultSectionPrefData> subSections = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		for (FaultSectionPrefData section : fm) {
			ArrayList<FaultSectionPrefData> subSectData = buildSubSections(
					section, maxSubSectionLength, subSectIndex);
			subSections.addAll(subSectData);
			subSectIndex += subSectData.size();
		}
		
		return subSections;
	}
	
	private static ArrayList<FaultSectionPrefData> buildSubSections(
			FaultSectionPrefData section, double maxSubSectionLength, int subSectIndex) {
		double maxSectLength = section.getOrigDownDipWidth()*maxSubSectionLength;
		return section.getSubSectionsList(maxSectLength, subSectIndex);
	}
	
	private ArrayList<FaultSectionPrefData> loadUCERF3FaultModel(int faultModelID) {
		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
		PrefFaultSectionDataDB_DAO pref2db = new PrefFaultSectionDataDB_DAO(db);
		ArrayList<FaultSectionPrefData> datas = pref2db.getAllFaultSectionPrefData();
		FaultModelDB_DAO fm2db = new FaultModelDB_DAO(db);
		ArrayList<Integer> faultSectionIds = fm2db.getFaultSectionIdList(faultModelID);

		ArrayList<FaultSectionPrefData> faultModel = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		//		for (int i = 0; i < faultSectionIds.size(); ++i) {
		for (FaultSectionPrefData data : datas) {
			if (!faultSectionIds.contains(data.getSectionId()))
				continue;
			faultModel.add(data);
		}

		return faultModel;
	}
	
	private ArrayList<FaultSectionPrefData> loadUCERF3DefModel(
			ArrayList<FaultSectionPrefData> sections, HashMap<Integer,DeformationSection> model, double maxSubSectionLength)
			throws IOException {
		final boolean DD = D && false;
		// TODO remove special cases
		if (model.containsKey(297) && model.containsKey(298) && model.containsKey(402)) {
			/*
			 * We need to fix the Elsinore problem. Here, the connector, and both overlapping alternates
			 * were incorrectly included in the deformation model. we need to combine them...we simply average
			 * each section from the models and add them together (some models distributed the slip across all 3,
			 * others only gave it to the middle.)
			 */
			DeformationSection def297 = model.get(297);
			DeformationSection def298 = model.get(298);
			DeformationSection def402 = model.get(402);
			System.out.println("Fixing Elsinore Special Case!");
			double slips297 = getLengthBasedAverage(def297.getLocsAsTrace(), def297.getSlips());
			double slips298 = getLengthBasedAverage(def298.getLocsAsTrace(), def298.getSlips());
			double slips402 = getLengthBasedAverage(def402.getLocsAsTrace(), def402.getSlips());
			
			double combinedSlip = 0;
			if (!Double.isNaN(slips297))
				combinedSlip += slips297;
			if (!Double.isNaN(slips298))
				combinedSlip += slips298;
			if (!Double.isNaN(slips402))
				combinedSlip += slips402;
			
			model.remove(297);
			model.remove(298);
			List<Double> slipsList = def402.getSlips();
			for (int i=0; i<slipsList.size(); i++)
				slipsList.set(i, combinedSlip);
		}
		
		ArrayList<FaultSectionPrefData> subSections = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		for (FaultSectionPrefData section : sections) {
			
			if (DD) System.out.println("Working on section "+section.getSectionId()+". "+section.getSectionName());
			if (DD) System.out.println("Building sub sections.");
			ArrayList<FaultSectionPrefData> subSectData = buildSubSections(
					section, maxSubSectionLength, subSectIndex);
			
			// replace the slip rates with the def model rates
			DeformationSection def = model.get(section.getSectionId());
			
			FaultTrace trace = section.getFaultTrace();
			
			if (DD) {
				System.out.println("Trace:");
				for (int i=0; i<trace.size(); i++)
					System.out.println("\t"+i+":\t"+trace.get(i).getLatitude()+"\t"+trace.get(i).getLongitude());
			}
			
			if (DD) System.out.println("Applying any special cases...");
			
			if (def == null || !def.validateAgainst(section)) {
				/* TODO remove special cases when files are updated
				 * these are special cases where the files were generated for an older fault model
				 * unfortunate be necessary, temporarily */
				if (section.getSectionId() == 82) {
					/* 
					 * special case for North Frontal  (West)
					 * 
					 * The problem here is that the 5th trace point (index 4) was accidentally
					 * present in the fault section. it has since been deleted, but the deformation
					 * models still have this incorrect location. To fix this, we simply remove the
					 * offending point from the deformation model's fault trace and average the two
					 * original slip rates (scaled by their lengths). 
					 */
					System.out.println("Fixing North Frontal Special Case!");
					
					List<Location> locs1 = def.getLocs1();
					List<Location> locs2 = def.getLocs2();
					List<Double> slips = def.getSlips();
					List<Double> rakes = def.getRakes();
					
					// we need to average slips 3 & 4, with respects to locs 3, 4, & 5
					// this will contains pts 3, 4, 5 (sublist is exclusive)
					List<Location> locs = locs1.subList(3, 6);
					// this will contain slips 3 & 4
					List<Double> slipsPart = slips.subList(3, 5);
					List<Double> rakesPart = rakes.subList(3, 5);
					
					double avgSlip = getLengthBasedAverage(locs, slipsPart);
					double avgRake = getLengthBasedRakeAverage(locs, rakesPart);
					
					// now fix the lists
					slips.remove(3);
					slips.remove(4);
					slips.add(3, avgSlip);
					rakes.remove(3);
					rakes.remove(4);
					rakes.add(3, avgRake);
					
					locs1.remove(4);
					locs2.remove(3);
				} else if (section.getSectionId() == 664) {
					/* 
					 * this is Silver Creek 2011 CFM. an error was corrected in this fault trace the
					 * point with index 5. We can fix this by setting the value of pt 5 to 
					 * that from the trace
					 */
					System.out.println("Fixing Silver Creek Special Case!");
					
					List<Location> locs2 = def.getLocs2();
					
					Location correctPt = trace.get(5);
					locs2.set(4, correctPt);
					// don't need to set anything on locs1 because this is the last point
				} else if (section.getSectionId() == 666) {
					/* 
					 * this is Point Reyes 2011 CFM. an error was corrected in this fault trace the
					 * point with index 2. We can fix this by setting the longitude of pt 2 to 
					 * that from the trace
					 */
					System.out.println("Fixing Point Reyes Special Case!");
					
					List<Location> locs1 = def.getLocs1();
					List<Location> locs2 = def.getLocs2();
					
					Location correctPt = trace.get(2);
					locs1.set(2, correctPt);
					locs2.set(1, correctPt);
				} else if (section.getSectionId() == 401) {
					/* 
					 * this is on the San Jicento where the connector should have been included, but wasn't. Instead
					 * two overlapping planes were included (290 & 291). We combine them by averaging each one, and
					 * adding the slips.
					 */
					DeformationSection def290 = model.get(290);
					DeformationSection def291 = model.get(291);
					System.out.println("Fixing San Jacinto Special Case!");
					double slips290 = getLengthBasedAverage(def290.getLocsAsTrace(), def290.getSlips());
					double slips291 = getLengthBasedAverage(def291.getLocsAsTrace(), def291.getSlips());
					double slips401 = 0;
					if (def != null) {
						slips401 = getLengthBasedAverage(def.getLocsAsTrace(), def.getSlips());
					}
					
					double combinedSlip = 0;
					if (!Double.isNaN(slips290))
						combinedSlip += slips290;
					if (!Double.isNaN(slips291))
						combinedSlip += slips291;
					if (!Double.isNaN(slips401))
						combinedSlip += slips401;
					
					model.remove(290);
					model.remove(291);
					
					if (def == null) {
						DeformationSection def401 = new DeformationSection(401);
						for (int i=0; i<trace.size()-1; i++) {
							def401.add(trace.get(i), trace.get(i+1), combinedSlip, section.getAveRake());
						}
						model.put(401, def401);
						def = def401;
					} else {
						for (int i=0; i<def.getSlips().size(); i++) {
							def.getSlips().set(i, combinedSlip);
						}
					}
				}
				
				// make sure we fixed it
				Preconditions.checkState(def.validateAgainst(section), "fix didn't work for section: "
						+section.getSectionId());
			}
			
			if (DD) System.out.println("Done with special cases");
			
			List<Double> slips = def.getSlips();
			List<Double> rakes = def.getRakes();
			Preconditions.checkState(slips.size() == rakes.size());
			
			// now set the subsection rates from the def model
			for (int s=0; s<subSectData.size(); s++) {
				FaultSectionPrefData subSect = subSectData.get(s);
				FaultTrace subTrace = subSect.getFaultTrace();
				Preconditions.checkState(subTrace.size()>1, "sub section trace only has one point!!!!");
				Location subStart = subTrace.get(0);
				Location subEnd = subTrace.get(subTrace.size()-1);
				
				if (DD) {
					System.out.println("Sbusection "+s+"/"+subSectData.size());
					System.out.println("Start: "+subStart.getLatitude()+"\t"+subStart.getLongitude());
					System.out.println("End: "+subEnd.getLatitude()+"\t"+subEnd.getLongitude());
				}
				
				// this is the index of the trace point that is either before or equal to the start of the
				// sub section
				int traceIndexBefore = -1;
				// this is the index of the trace point that is either after or exactly at the end of the
				// sub section
				int traceIndexAfter = -1;
				
				// now see if there are any trace points in between the start and end of the sub section
				for (int i=0; i<trace.size(); i++) {
					// loop over section trace. we leave out the first and last point because those are
					// by definition end points and are already equal to sub section start/end points
					Location tracePt = trace.get(i);
					
					if (isBefore(subStart, subEnd, tracePt)) {
						if (DD) System.out.println("Trace "+i+" is BEFORE");
						traceIndexBefore = i;
					} else if (isAfter(subStart, subEnd, tracePt)) {
						// we want just the first index after, so we break
						if (DD) System.out.println("Trace "+i+" is AFTER");
						traceIndexAfter = i;
						break;
					} else {
						if (DD) System.out.println("Trace "+i+" must be BETWEEN");
					}
				}
				Preconditions.checkState(traceIndexBefore >=0, "trace index before not found!");
				Preconditions.checkState(traceIndexAfter > traceIndexBefore, "trace index after not found!");
				
				// this is the list of locations on the sub section, including any trace points inbetween
				ArrayList<Location> subLocs = new ArrayList<Location>();
				// this is the slip of all spans of the locations above
				ArrayList<Double> subSlips = new ArrayList<Double>();
				// this is the rake of all spans of the locations above
				ArrayList<Double> subRakes = new ArrayList<Double>();
				
				subLocs.add(subStart);
				
				for (int i=traceIndexBefore; i<traceIndexAfter; i++) {
					subSlips.add(slips.get(i));
					subRakes.add(rakes.get(i));
					if (i > traceIndexBefore && i < traceIndexAfter)
						subLocs.add(trace.get(i));
				}
				subLocs.add(subEnd);
				
				double avgSlip = getLengthBasedAverage(subLocs, subSlips);
				double avgRake = getLengthBasedRakeAverage(subLocs, subRakes);
				
				subSect.setAveSlipRate(avgSlip);
				subSect.setAveRake(avgRake);
			}
			
			if (DD) System.out.println("Done with subsection assignment.");
			if (DD) System.out.flush();
			
			subSections.addAll(subSectData);
			subSectIndex += subSectData.size();
		}
		
		if (DD) System.out.println("Done with sections!");
		if (DD) System.out.flush();
		
		return subSections;
	}
	
	private static double getLengthBasedRakeAverage(List<Location> locs, List<Double> rakes) {
		double avg = FaultUtils.getLengthBasedAngleAverage(locs, rakes);
		if (avg > 180)
			avg -= 360;
		return avg;
	}
	
	/**
	 * This averages multiple scalars based on the length of each corresponding span.
	 * 
	 * @param locs a list of locations
	 * @param scalars a list of scalar values to be averaged based on the distance between
	 * each location in the location list
	 */
	private static double getLengthBasedAverage(List<Location> locs, List<Double> scalars) {
		Preconditions.checkArgument(locs.size() == scalars.size()+1,
				"there must be exactly one less slip than location!");
		Preconditions.checkArgument(!scalars.isEmpty(), "there must be at least 2 locations and 1 slip rate");
		
		if (scalars.size() == 1)
			return scalars.get(0);
		if (Double.isNaN(scalars.get(0)))
			return Double.NaN;
		boolean equal = true;
		for (int i=1; i<scalars.size(); i++) {
			if (Double.isNaN(scalars.get(i)))
				return Double.NaN;
			if (scalars.get(i) != scalars.get(0)) {
				equal = false;
				break;
			}
		}
		if (equal)
			return scalars.get(0);
		
		double totDist = 0;
		ArrayList<Double> dists = new ArrayList<Double>();
		
		for (int i=1; i<locs.size(); i++) {
			double dist = LocationUtils.linearDistanceFast(locs.get(i-1), locs.get(i));
			dists.add(dist);
			totDist += dist;
		}
		
		double scaledAvg = 0;
		for (int i=0; i<dists.size(); i++) {
			double relative = dists.get(i) / totDist;
			scaledAvg += relative * scalars.get(i);
		}
		
		return scaledAvg;
	}
	
	/**
	 * Determines if the given point, pt, is before or equal to the start point. This is
	 * done by determining that pt is closer to start than end, and is further from end
	 * than start is.
	 * 
	 * @param start
	 * @param end
	 * @param pt
	 * @return
	 */
	private static boolean isBefore(Location start, Location end, Location pt) {
		if (start.equals(pt) || LocationUtils.areSimilar(start, pt))
			return true;
		double pt_start_dist = LocationUtils.linearDistanceFast(pt, start);
		if (pt_start_dist == 0)
			return true;
		double pt_end_dist = LocationUtils.linearDistanceFast(pt, end);
		double start_end_dist = LocationUtils.linearDistanceFast(start, end);
		
		return pt_start_dist < pt_end_dist && pt_end_dist > start_end_dist;
	}
	
	/**
	 * Determines if the given point, pt, is after or equal to the end point. This is
	 * done by determining that pt is closer to end than start, and is further from start
	 * than end is.
	 * 
	 * @param start
	 * @param end
	 * @param pt
	 * @return
	 */
	private static boolean isAfter(Location start, Location end, Location pt) {
		if (end.equals(pt) || LocationUtils.areSimilar(end, pt))
			return true;
		double pt_end_dist = LocationUtils.linearDistanceFast(pt, end);
		if (pt_end_dist == 0)
			return true;
		double pt_start_dist = LocationUtils.linearDistanceFast(pt, start);
		double start_end_dist = LocationUtils.linearDistanceFast(start, end);
		
		return pt_end_dist < pt_start_dist && pt_start_dist > start_end_dist;
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
		
		HashMap<IDPairing, Double> reversed = new HashMap<IDPairing, Double>();
		
		// now add the reverse distance
		for (IDPairing pair : distances.keySet()) {
			IDPairing reverse = pair.getReversed();
			reversed.put(reverse, distances.get(pair));
		}
		distances.putAll(reversed);

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
	
	public static void main(String[] args) {
		try {
			File precomputedDataDir = new File("dev/scratch/UCERF3/preComputedData");
			new DeformationModelFetcher(DefModName.UCERF3_GEOLOGIC, precomputedDataDir);
//			new DeformationModelFetcher(DefModName.UCERF3_ZENG, precomputedDataDir);
//			new DeformationModelFetcher(DefModName.UCERF3_NEOKINEMA, precomputedDataDir);
//			new DeformationModelFetcher(DefModName.UCERF3_GEOBOUND, precomputedDataDir);
//			new DeformationModelFetcher(DefModName.UCERF3_ABM, precomputedDataDir);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
