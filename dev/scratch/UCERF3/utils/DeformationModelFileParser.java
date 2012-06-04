package scratch.UCERF3.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.PrefFaultSectionDataFinal;
import org.opensha.sha.faultSurface.FaultTrace;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DeformationModelFileParser {
	
	public static Map<Integer, DeformationSection> load(File file) throws IOException {
		return load(file.toURI().toURL());
	}
	
	public static Map<Integer, DeformationSection> load(URL url) throws IOException {
		CSVFile<String> csv = CSVFile.readURL(url, true);
		
		HashMap<Integer, DeformationSection>  defs = new HashMap<Integer, DeformationModelFileParser.DeformationSection>();
		
		for (List<String> row : csv) {
//			System.out.println("ID: "+row.get(0));
			int id[] = parseMinisectionNumber(row.get(0));
			DeformationSection def = defs.get(id[0]);
			if (def == null) {
				def = new DeformationSection(id[0]);
				defs.put(id[0], def);
			}
			double lon1 = Double.parseDouble(row.get(1));
			double lat1 = Double.parseDouble(row.get(2));
			double lon2 = Double.parseDouble(row.get(3));
			double lat2 = Double.parseDouble(row.get(4));
			Location loc1 = new Location(lat1, lon1);
			Location loc2 = new Location(lat2, lon2);
			
			double slip;
			try {
				slip = Double.parseDouble(row.get(5));
			} catch (NumberFormatException e) {
				slip = Double.NaN;
			}
			
			double rake;
			try {
				rake = Double.parseDouble(row.get(6));
			} catch (NumberFormatException e) {
				rake = Double.NaN;
			}
			
			// make sure that the mini section number is correct (starts at 1)
			Preconditions.checkState(def.slips.size() == id[1]-1, "bad row with minisectin: "
						+row.get(0)+" (parsed as: "+id[0]+", "+id[1]+")");
			
			def.add(loc1, loc2, slip, rake);
		}
		
		return defs;
	}
	
	private static int[] parseMinisectionNumber(String miniSection) {
		String[] split = miniSection.trim().split("\\.");
		int id = Integer.parseInt(split[0]);
		Preconditions.checkState(split.length > 1 && !split[1].isEmpty(),
				"Mini section was left blank for "+id+": "+miniSection);
		String str = split[1];
		// must be at least two digits (some files give it at xx.1 meaning xx.10)
		if (str.length() == 1)
			str = str+"0";
		int section = Integer.parseInt(str);
		
		int[] ret = {id, section};
		return ret;
	}
	
	public static String getMinisectionString(int[] miniSection) {
		String str = miniSection[0]+".";
		if (miniSection[1] < 10)
			str += "0";
		str += miniSection[1];
		return str;
	}
	
	public static void compareAgainst(Map<Integer, DeformationSection> defs,
			List<FaultSectionPrefData> datas, List<Integer> fm) throws IOException {
		HashSet<DeformationSection> dones = new HashSet<DeformationModelFileParser.DeformationSection>();
		int noneCnt = 0;
		for (int id : fm) {
			DeformationSection def = defs.get(id);
			FaultSectionPrefData data = null;
			for (FaultSectionPrefData myData : datas) {
				if (myData.getSectionId() == id) {
					data = myData;
					break;
				}
			}
			Preconditions.checkNotNull(data);
			if (def == null) {
				System.out.println("No def model data for: "+data.getSectionId()+". "+data.getSectionName());
				noneCnt++;
				continue;
			}
			def.validateAgainst(data);
			dones.add(def);
		}
		
		System.out.println("No def model data for: "+noneCnt+" sections");
		
		for (DeformationSection def : defs.values()) {
			if (!dones.contains(def))
				System.out.println("No fault exists for def model section of id: "+def.id);
		}
	}
	
	public static void write(Map<Integer, DeformationSection> model, File file) throws IOException {
		write(model.values(), file);
	}
	
	public static void write(Collection<DeformationSection> model, File file) throws IOException {
		write(model, file, null);
	}
	
	public static void write(Collection<DeformationSection> model, File file, Map<Integer, String> namesMap) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		for (DeformationSection def : model) {
			List<Location> locs1 = def.getLocs1();
			List<Location> locs2 = def.getLocs2();
			List<Double> slips = def.getSlips();
			List<Double> rakes = def.getRakes();
			for (int i=0; i<slips.size(); i++) {
				int[] mini = { def.getId(), (i+1) };
				String id = getMinisectionString(mini);
				ArrayList<String> line = new ArrayList<String>();
				line.add(id);
				line.add(""+(float)locs1.get(i).getLongitude());
				line.add(""+(float)locs1.get(i).getLatitude());
				line.add(""+(float)locs2.get(i).getLongitude());
				line.add(""+(float)locs2.get(i).getLatitude());
				line.add(""+slips.get(i).floatValue());
				line.add(""+rakes.get(i).floatValue());
				if (namesMap != null)
					line.add(namesMap.get(def.id));
				csv.addLine(line);
			}
		}
		csv.writeToFile(file);
	}
	
	public static class DeformationSection {
		public int getId() {
			return id;
		}
		
		protected void setID(int id) {
			this.id = id;
		}

		public List<Location> getLocs1() {
			return locs1;
		}

		public List<Location> getLocs2() {
			return locs2;
		}

		public List<Double> getSlips() {
			return slips;
		}

		public List<Double> getRakes() {
			return rakes;
		}

		private int id;
		private List<Location> locs1;
		private List<Location> locs2;
		private List<Double> slips;
		private List<Double> rakes;
		private List<Double> momentReductions; // defaults to null
		
		public DeformationSection(int id) {
			this.id = id;
			this.locs1 = new ArrayList<Location>();
			this.locs2 = new ArrayList<Location>();
			this.slips = new ArrayList<Double>();
			this.rakes = new ArrayList<Double>();
		}
		
		public void add(Location loc1, Location loc2, double slip, double rake) {
			locs1.add(loc1);
			locs2.add(loc2);
			slips.add(slip);
			rakes.add(rake);
		}
		
		public void add(int index, Location loc1, Location loc2, double slip, double rake) {
			locs1.add(index, loc1);
			locs2.add(index, loc2);
			slips.add(index, slip);
			rakes.add(index, rake);
		}
		
		public void remove(int index) {
			locs1.remove(index);
			locs2.remove(index);
			slips.remove(index);
			rakes.remove(index);
		}
		
		public void setMomentReductions(List<Double> momentReductions) {
			Preconditions.checkState(momentReductions.size() == slips.size(),
					"Size of moment reductions must match that of the slips");
		}
		
		public List<Double> getMomentReductions() {
			return momentReductions;
		}
		
		public LocationList getLocsAsTrace() {
			LocationList trace = new LocationList();
			trace.addAll(locs1);
			trace.add(locs2.get(locs2.size()-1));
			return trace;
		}
		
		public boolean validateAgainst(FaultSectionPrefData data) {
			String nameID = id+". "+data.getName();
			
			boolean mismatch = false;
			FaultTrace trace = data.getFaultTrace();
			if (trace.size()-1 != locs1.size()) {
				System.out.println(nameID+": trace size mismatch ("+trace.getNumLocations()+" trace pts, "+locs1.size()+" slip vals)");
				mismatch = true;
			}
			
			ArrayList<Location> fileLocs = getLocsAsTrace();
			
			for (int i=0; i<fileLocs.size()&&i<trace.size(); i++) {
				double dist = LocationUtils.horzDistance(fileLocs.get(i), trace.get(i));
				if (dist > 0.5) {
					System.out.println(nameID+": trace location mismatch at index "+i+"/"+(fileLocs.size()-1));
					mismatch = true;
				}
			}
			
			if (mismatch) {
				for (int i=0; i<fileLocs.size(); i++) {
					Location loc = fileLocs.get(i);
					System.out.print("["+(float)loc.getLatitude()+"\t"+(float)loc.getLongitude()+"\t0]");
					
					if (trace.size() > i) {
						Location traceLoc = trace.get(i);
						System.out.print("\t["+(float)traceLoc.getLatitude()+"\t"+(float)traceLoc.getLongitude()+"\t0]");
						System.out.print("\tdist: "+(float)LocationUtils.linearDistanceFast(loc, traceLoc));
					}
					System.out.println();
				}
			}
			
			if (mismatch)
				return false;
			
			double dist1 = LocationUtils.horzDistance(locs1.get(0), trace.get(0));
			if (dist1 > 1) {
				System.out.println(nameID+": start trace location mismatch ("+dist1+" km)");
				return false;
			}
			
			double dist2 = LocationUtils.horzDistance(locs2.get(locs2.size()-1), trace.get(trace.size()-1));
			if (dist2 > 1) {
				System.out.println(nameID+": end trace location mismatch ("+dist2+" km)");
				return false;
			}
			
			for (int i=1; i<locs2.size(); i++) {
				if (!LocationUtils.areSimilar(locs1.get(i), locs2.get(i-1))) {
					System.out.println(nameID+": trace locations inconsistant in def model!");
					for (int j=0; j<locs1.size(); j++) {
						System.out.print("["+(float)locs1.get(j).getLatitude()+"\t"+(float)locs1.get(j).getLongitude()+"\t0]");
						System.out.println("\t["+(float)locs2.get(j).getLatitude()+"\t"+(float)locs2.get(j).getLongitude()+"\t0]");
					}
					return false;
				}
			}
			
			return true;
		}
	}
	
	private static <E> E getFromEnd(List<E> list, int numFromEnd) {
		return list.get(list.size()-1-numFromEnd);
	}
	
	private static void fixForRevisedFM(Map<Integer, DeformationSection> sects, FaultModels fm) {
		ArrayList<FaultSectionPrefData> datas = fm.fetchFaultSections(true);
		HashMap<Integer, FaultSectionPrefData> fmMap = Maps.newHashMap();
		for (FaultSectionPrefData data : datas)
			fmMap.put(data.getSectionId(), data);
		
		// fix 667. Almanor 2011 CFM:
		FaultSectionPrefData data = fmMap.get(667);
		FaultTrace trace = data.getFaultTrace();
		DeformationSection sect = sects.get(667);
		sect.add(getFromEnd(trace, 1), getFromEnd(trace, 0), getFromEnd(sect.getSlips(), 0), getFromEnd(sect.getRakes(), 0));
		
		// fix 74. Los Osos 2011
		sect = sects.get(74);
		// remove the last 3 points, but save them for oceanic
		ArrayList<Double> slipsForOceanic = Lists.newArrayList();
		ArrayList<Double> rakesForOceanic = Lists.newArrayList();
		for (int i=sect.getSlips().size()-4; i<sect.getSlips().size(); i++) {
			slipsForOceanic.add(sect.getSlips().get(i));
			rakesForOceanic.add(sect.getRakes().get(i));
		}
		sect.remove(sect.getSlips().size()-1);
		sect.remove(sect.getSlips().size()-1);
		sect.remove(sect.getSlips().size()-1);
		
		// fix 82. North Frontal  (West)
		// remove the first point, then average the next two for the first span in the new fault model
		data = fmMap.get(82);
		sect = sects.get(82);
		trace = data.getFaultTrace();
		sect.remove(0);
		double slip = FaultUtils.getLengthBasedAngleAverage(sect.getLocsAsTrace().subList(0, 3), sect.getSlips().subList(0, 2));
		double rake = FaultUtils.getLengthBasedAngleAverage(sect.getLocsAsTrace().subList(0, 3), sect.getRakes().subList(0, 2));
		sect.remove(0);
		sect.remove(0);
		sect.remove(0);
		sect.add(0, trace.get(0), trace.get(1), slip, rake);
		sect.add(1, trace.get(1), trace.get(2), slip, rake);
		sect.add(2, trace.get(2), trace.get(3), slip, rake);
		sect.add(3, trace.get(3), trace.get(4), slip, rake);
		
		// 687. North Tahoe 2011 CFM
		data = fmMap.get(687);
		sect = sects.get(687);
		trace = data.getFaultTrace();
		slip = getFromEnd(sect.getSlips(), 0);
		rake = getFromEnd(sect.getRakes(), 0);
		sect.remove(sect.getSlips().size()-1);
		sect.add(getFromEnd(trace, 2), getFromEnd(trace, 1), slip, rake);
		sect.add(getFromEnd(trace, 1), getFromEnd(trace, 0), slip, rake);
		sect.getLocs1().set(0, trace.get(0));
		sect.getLocs2().set(0, trace.get(1));
		
		// fix 709. Oceanic - West Huasna
		data = fmMap.get(709);
		sect = sects.get(709);
		trace = data.getFaultTrace();
		sect.add(0, trace.get(0), trace.get(1), getFromEnd(slipsForOceanic, 0), getFromEnd(rakesForOceanic, 0));
		sect.add(1, trace.get(1), trace.get(2), getFromEnd(slipsForOceanic, 1), getFromEnd(rakesForOceanic, 1));
		sect.add(2, trace.get(2), trace.get(3), getFromEnd(slipsForOceanic, 2), getFromEnd(rakesForOceanic, 2));
		
		// fix 123. Rose Canyon
		data = fmMap.get(123);
		sect = sects.get(123);
		trace = data.getFaultTrace();
		for (int i=1; i<trace.size(); i++) {
			sect.getLocs1().set(i-1, trace.get(i-1));
			sect.getLocs2().set(i-1, trace.get(i));
		}
		
		// fix 113. Sierra Madre (San Fernando)
		data = fmMap.get(113);
		sect = sects.get(113);
		trace = data.getFaultTrace();
		for (int i=1; i<trace.size(); i++) {
			sect.getLocs1().set(i-1, trace.get(i-1));
			sect.getLocs2().set(i-1, trace.get(i));
		}
		
		// remove 182. Shelf (projection)
		sects.remove(182);
	}
	
	private static Map<String, double[]> momemtReductionsData = null;
	
	private synchronized static void loadMomentReductionsData() throws IOException {
		if (momemtReductionsData == null) {
			InputStream is = UCERF3_DataUtils.locateResourceAsStream("DeformationModels",
					"Moment_Reductions_2012_03_28_v2.xls");
			POIFSFileSystem fs = new POIFSFileSystem(is);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			int numModels = 6;
			momemtReductionsData = new HashMap<String, double[]>();
			for(int r=1; r<=lastRowIndex; ++r) {
//				System.out.println("Coulomb row: "+r);
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell miniCell = row.getCell(1);
				if (miniCell == null)
					continue;
				String miniSection;
				if (miniCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
					miniSection = ""+(float)miniCell.getNumericCellValue();
				else
					miniSection = miniCell.getStringCellValue();
				double[] reductions = new double[numModels];
				for (int i=0; i<numModels; i++) {
					HSSFCell dataCell = row.getCell(2+i);
					Preconditions.checkState(dataCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC,
							"non numeric data cell!");
					reductions[i] = dataCell.getNumericCellValue();
				}
				momemtReductionsData.put(miniSection, reductions);
			}
		}
	}
	
//	private synchronized static void fixMomentReductionsData() throws IOException {
//		InputStream is = UCERF3_DataUtils.locateResourceAsStream("DeformationModels", "Moment_Reductions_2012_03_01.xls");
//		POIFSFileSystem fs = new POIFSFileSystem(is);
//		HSSFWorkbook wb = new HSSFWorkbook(fs);
//		HSSFSheet sheet = wb.getSheetAt(0);
//		int lastRowIndex = sheet.getLastRowNum();
//		int numModels = 6;
//		for(int r=1; r<=lastRowIndex; ++r) {
//			//				System.out.println("Coulomb row: "+r);
//			HSSFRow row = sheet.getRow(r);
//			if(row==null) continue;
//			HSSFCell miniCell = row.getCell(1);
//			if (miniCell == null)
//				continue;
//			String miniSection;
//			if (miniCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
//				miniSection = ""+(float)miniCell.getNumericCellValue();
//			else
//				miniSection = miniCell.getStringCellValue();
//			int[] mini = parseMinisectionNumber(miniSection);
//			mini[1] = mini[1] + 1;
//			miniCell.setCellValue(getMinisectionString(mini));
//			double[] reductions = new double[numModels];
//			for (int i=0; i<numModels; i++) {
//				HSSFCell dataCell = row.getCell(2+i);
//				Preconditions.checkState(dataCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC,
//						"non numeric data cell!");
//				reductions[i] = dataCell.getNumericCellValue();
//			}
//		}
//		wb.write(new FileOutputStream(new File("/tmp/fixed.xls")));
//	}
	
	public static void applyMomentReductions(Map<Integer, DeformationSection> model, DeformationModels dm,
			double maxMomentReduction) {
		// first load the data if needed
		try {
			loadMomentReductionsData();
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		int index; // this is the index in the array, and thus the file for this def model
		switch (dm) {
		case GEOBOUND:
			index = 0;
			break;
		case ZENG:
			index = 1;
			break;
		case NEOKINEMA:
			index = 2;
			break;
		case ABM:
			index = 3;
			break;
		case GEOLOGIC:
			index = 4;
			break;
		case GEOLOGIC_PLUS_ABM:
			index = 5;
			break;
		case GEOLOGIC_LOWER:
			index = 4;
			break;
		case GEOLOGIC_UPPER:
			index = 4;
			break;

		default:
			throw new IllegalStateException("No data file index is known for DM: "+dm);
		}
		
		for (String miniSectionStr : momemtReductionsData.keySet()) {
			double[] reductions = momemtReductionsData.get(miniSectionStr);
			int[] miniSection = parseMinisectionNumber(miniSectionStr);
			int id = miniSection[0];
			int sect = miniSection[1];
			
			DeformationSection def = model.get(id);
			Preconditions.checkNotNull(def, "The given deformation model doesn't have a mapping for section "+id);
			int numMinisForSection = def.getSlips().size();
			if (def.momentReductions == null) {
				def.momentReductions = new ArrayList<Double>(numMinisForSection);
				for (int i=0; i<numMinisForSection; i++)
					def.momentReductions.add(0d);
			}
			Preconditions.checkState(sect <= numMinisForSection, "Mini sections inconsistant for section: "+id);
			double reduction = reductions[index];
			if (reduction <= maxMomentReduction)
				def.momentReductions.set(sect-1, reductions[index]);
			else
				def.momentReductions.set(sect-1, maxMomentReduction);
		}
	}
	
	static void writeFromDatabase(FaultModels fm, File file, boolean names) throws IOException {
		int fmID = fm.getID();
		DB_AccessAPI db = fm.getDBAccess();
		
		PrefFaultSectionDataDB_DAO pref2db = new PrefFaultSectionDataDB_DAO(db);
		ArrayList<FaultSectionPrefData> datas = pref2db.getAllFaultSectionPrefData();
		FaultModelDB_DAO fm2db = new FaultModelDB_DAO(db);
		ArrayList<Integer> faultSectionIds = fm2db.getFaultSectionIdList(fmID);

		ArrayList<FaultSectionPrefData> faultModel = new ArrayList<FaultSectionPrefData>();
		for (FaultSectionPrefData data : datas) {
			if (!faultSectionIds.contains(data.getSectionId()))
				continue;
			faultModel.add(data);
		}
		
		if (names) {
			Collections.sort(faultModel, new Comparator<FaultSectionPrefData>() {
				
				private Collator c = Collator.getInstance();

				@Override
				public int compare(FaultSectionPrefData o1, FaultSectionPrefData o2) {
					return c.compare(o1.getSectionName(), o2.getSectionName());
				}
			});
		} else {
			Collections.sort(faultModel, new Comparator<FaultSectionPrefData>() {

				@Override
				public int compare(FaultSectionPrefData o1, FaultSectionPrefData o2) {
					return Double.compare(o1.getSectionId(), o2.getSectionId());
				}
			});
		}
		
		ArrayList<DeformationSection> sects = new ArrayList<DeformationModelFileParser.DeformationSection>();
		Map<Integer, String> namesMap;
		if (names)
			namesMap = Maps.newHashMap();
		else
			namesMap = null;
		
		for (FaultSectionPrefData data : faultModel) {
			DeformationSection sect = new DeformationSection(data.getSectionId());
			
			double slip = data.getOrigAveSlipRate();
			double rake = data.getAveRake();
			
			FaultTrace trace = data.getFaultTrace();
			
			for (int i=1; i<trace.size(); i++) {
				sect.add(trace.get(i-1), trace.get(i), slip, rake);
			}
			
			if (namesMap != null)
				namesMap.put(data.getSectionId(), data.getSectionName());
			sects.add(sect);
		}
		
		write(sects, file, namesMap);
		
		File asciiFile = new File(file.getParentFile(), file.getName().replaceAll(".csv", ".txt"));
		FaultSectionDataWriter.writeSectionsToFile(faultModel, null, asciiFile.getAbsolutePath());
		
		try {
			db.destroy();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		writeFromDatabase(FaultModels.FM3_2, new File("/tmp/fm_3_2_revised_minisections_with_names.csv"), true);
//		System.exit(0);
		
		
		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
		PrefFaultSectionDataDB_DAO pref2db = new PrefFaultSectionDataDB_DAO(db);
		FaultModelDB_DAO fm2db = new FaultModelDB_DAO(db);
		File defFile;
		
//		File dir = new File("D:\\Documents\\SCEC\\def_models");
//		File dir = new File("/home/kevin/OpenSHA/UCERF3/def_models/2012_02_07-initial");
		
		File precomputedDataDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
		
		try {
//			load(DeformationModels.GEOLOGIC.getDataFileURL(FaultModels.FM3_2));
//			fixMomentReductionsData();
			
			// this will fix the mini sction numbering problem
//			String subDirName = "DeformationModels";
//			File dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR.getParent(), subDirName);
//			FaultModels[] fms = { FaultModels.FM3_1, FaultModels.FM3_2 };
//			for (FaultModels fm : fms) {
//				for (DeformationModels dm : DeformationModels.forFaultModel(fm)) {
//					String fileName = dm.getDataFileName(fm);
//					File origFile = new File(dir, fileName);
//					File origBakFile = new File(dir, fileName+".bak");
//					System.out.println("Fixing: "+origFile.getName());
//					CSVFile<String> csv = CSVFile.readFile(origFile, true);
//					for (int i=0; i<csv.getNumRows(); i++) {
//						String miniStr = csv.get(i, 0);
//						int[] mini = parseMinisectionNumber(miniStr);
//						mini[1] = mini[1] + 1;
//						miniStr = getMinisectionString(mini);
//						csv.set(i, 0, miniStr);
//					}
//					FileUtils.moveFile(origFile, origBakFile);
//					csv.writeToFile(origFile);
//				}
//			}
			
			
//			DeformationModels.GEOLOGIC;
//			CSVFile<String> csv = CSVFile.readURL(url, true);
			
//			System.out.println("Fetching fault data");
			ArrayList<FaultSectionPrefData> datas = pref2db.getAllFaultSectionPrefData();
//			System.out.println("Fetching fault model");
			FaultModels fm = FaultModels.FM3_1;
			ArrayList<Integer> fmSects = fm2db.getFaultSectionIdList(fm.getID());
			File dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR.getParentFile(),
					"DeformationModels");
			defFile = new File(dir, "geologic_slip_rake_2012_03_02.csv");
//			System.out.println("Doing comparison: "+defFile.getName());
			Map<Integer, DeformationSection>  defs = load(defFile);
			compareAgainst(defs, datas, fmSects);
			System.out.println("ok lets fix it...");
			fixForRevisedFM(defs, fm);
			compareAgainst(defs, datas, fmSects);
//			System.out.println("");
//			defFile = new File(dir, "ABM_slip_rake_feb06.txt");
//			System.out.println("Doing comparison: "+defFile.getName());
//			defs = load(defFile);
//			compareAgainst(defs, datas, fm);
//			System.out.println("");
//			defFile = new File(dir, "geobound_slip_rake_feb06.txt");
//			System.out.println("Doing comparison: "+defFile.getName());
//			defs = load(defFile);
//			compareAgainst(defs, datas, fm);
//			System.out.println("");
//			defFile = new File(dir, "zeng_slip_rake_feb06.txt");
//			System.out.println("Doing comparison: "+defFile.getName());
//			defs = load(defFile);
//			compareAgainst(defs, datas, fm);
//			System.out.println("");
//			System.out.println("DONE");
			
			
//			ArrayList<FaultSectionPrefData> datas = DeformationModelFetcher.loadUCERF3FaultModel(faultModelId);
//			
//			for (DeformationModels dm : DeformationModels.values()) {
//				if (dm.getDataFileURL() == null)
//					continue;
//				HashMap<Integer, DeformationSection> model = load(dm.getDataFileURL());
//				HashMap<Integer, DeformationSection> fixed = DeformationModelFetcher.getFixedModel(datas, model, dm);
//				File outFile = new File(dm.getDataFileURL().toURI());
//				outFile = new File(outFile.getParentFile(), outFile.getName()+".fixed");
//				System.out.println("Writing: "+outFile.getAbsolutePath());
//				write(fixed, outFile);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				db.destroy();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}

}
