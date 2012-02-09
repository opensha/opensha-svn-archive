package scratch.UCERF3.utils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.PrefFaultSectionDataFinal;
import org.opensha.sha.faultSurface.FaultTrace;

import com.google.common.base.Preconditions;

public class DeformationModelFileParser {
	
	public static HashMap<Integer, DeformationSection> load(File file) throws IOException {
		CSVFile<String> csv = CSVFile.readFile(file, true);
		
		HashMap<Integer, DeformationSection>  defs = new HashMap<Integer, DeformationModelFileParser.DeformationSection>();
		
		for (List<String> row : csv) {
//			System.out.println("ID: "+row.get(0));
			String[] idStr = row.get(0).split("\\.");
			int id = Integer.parseInt(idStr[0]);
			if (!defs.containsKey(id))
				defs.put(id, new DeformationSection(id));
			double lon1 = Double.parseDouble(row.get(1));
			double lat1 = Double.parseDouble(row.get(2));
			double lon2 = Double.parseDouble(row.get(3));
			double lat2 = Double.parseDouble(row.get(4));
			Location loc1 = new Location(lat1, lon1);
			Location loc2 = new Location(lat2, lon2);
			double slip = Double.parseDouble(row.get(5));
			double rake = Double.parseDouble(row.get(6));
			
			defs.get(id).add(loc1, loc2, slip, rake);
		}
		
		return defs;
	}
	
	public static void compareAgainst(HashMap<Integer, DeformationSection> defs,
			ArrayList<FaultSectionPrefData> datas, ArrayList<Integer> fm) throws IOException {
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
		
		public boolean validateAgainst(FaultSectionPrefData data) {
			String nameID = id+". "+data.getName();
			
			boolean mismatch = false;
			FaultTrace trace = data.getFaultTrace();
			if (trace.size()-1 != locs1.size()) {
				System.out.println(nameID+": trace size mismatch ("+trace.getNumLocations()+" trace pts, "+locs1.size()+" slip vals)");
				mismatch = true;
			}
			
			ArrayList<Location> fileLocs = new ArrayList<Location>();
			fileLocs.addAll(locs1);
			fileLocs.add(locs2.get(locs2.size()-1));
			
			for (int i=0; i<fileLocs.size()&&i<trace.size(); i++) {
				double dist = LocationUtils.horzDistance(fileLocs.get(i), trace.get(i));
				if (dist > 1) {
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
					return false;
				}
			}
			
			return true;
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
		PrefFaultSectionDataDB_DAO pref2db = new PrefFaultSectionDataDB_DAO(db);
		FaultModelDB_DAO fm2db = new FaultModelDB_DAO(db);
		File defFile;
		
		int faultModelId = 101;
		
		File dir = new File("D:\\Documents\\SCEC\\def_models");
		
		try {
			System.out.println("Fetching fault data");
			ArrayList<FaultSectionPrefData> datas = pref2db.getAllFaultSectionPrefData();
			System.out.println("Fetching fault model");
			ArrayList<Integer> fm = fm2db.getFaultSectionIdList(faultModelId);
			defFile = new File(dir, "neokinema_slip_rake_feb06.txt");
			System.out.println("Doing comparison: "+defFile.getName());
			HashMap<Integer, DeformationSection>  defs = load(defFile);
			compareAgainst(defs, datas, fm);
			System.out.println("");
			defFile = new File(dir, "ABM_slip_rake_feb06.txt");
			System.out.println("Doing comparison: "+defFile.getName());
			defs = load(defFile);
			compareAgainst(defs, datas, fm);
			System.out.println("");
			defFile = new File(dir, "geobound_slip_rake_feb06.txt");
			System.out.println("Doing comparison: "+defFile.getName());
			defs = load(defFile);
			compareAgainst(defs, datas, fm);
			System.out.println("");
			defFile = new File(dir, "zeng_slip_rake_feb06.txt");
			System.out.println("Doing comparison: "+defFile.getName());
			defs = load(defFile);
			compareAgainst(defs, datas, fm);
			System.out.println("");
			System.out.println("DONE");
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
