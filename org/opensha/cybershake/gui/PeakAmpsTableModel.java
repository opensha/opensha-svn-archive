package org.opensha.cybershake.gui;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import org.opensha.cybershake.db.CybershakePeakAmplitudeSiteRecord;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.cybershake.db.SiteInfo2DB;

public class PeakAmpsTableModel extends AbstractTableModel {
	
	public static int NUM_COLUMNS = 6;
	
	PeakAmplitudesFromDB amps2db;
	SiteInfo2DB site2db;
	
	ArrayList<CybershakePeakAmplitudeSiteRecord> amps = new ArrayList<CybershakePeakAmplitudeSiteRecord>();
	
	HashMap<Integer, CybershakeSite> siteID_NameMap = new HashMap<Integer, CybershakeSite>();
	HashMap<String, Integer> countMap = new HashMap<String, Integer>();
	
	public PeakAmpsTableModel(DBAccess db) {
		this.amps2db = new PeakAmplitudesFromDB(db);
		this.site2db = new SiteInfo2DB(db);
		
		this.reloadAmps();
	}
	
	public void reloadAmps() {
		amps = amps2db.getPeakAmpSiteRecords();
		
		for (CybershakePeakAmplitudeSiteRecord amp : amps) {
			int siteID = amp.getSiteID();
			
			CybershakeSite site = siteID_NameMap.get(siteID);
			
			if (site == null) {
				site = site2db.getSiteFromDB(siteID);
				siteID_NameMap.put(siteID, site);
			}
		}
		
		this.fireTableDataChanged();
	}

	public int getColumnCount() {
		return NUM_COLUMNS;
	}

	public int getRowCount() {
		return amps.size();
	}
	
	public void loadCount(int row) {
		CybershakePeakAmplitudeSiteRecord record = this.getAmpsAtRow(row);
		
		int count = this.amps2db.countAmps(record.getSiteID(), record.getErfID(), record.getSgtVarID(), record.getRupVarScenID(), null);
		
		countMap.put(record.toString(), new Integer(count));
	}
	
	// columns:
	// 0: Site_ID | 1: Site_Short_Name | 2: ERF_ID | 3: Rup_Var_Scen_ID | 4: SGT_Var_ID | 5: Count

	public String getColumnName(int col) {
		if (col == 0) {
			return "Site ID";
		} else if (col == 1) {
			return "Site Name";
		} else if (col == 2) {
			return "ERF ID";
		} else if (col == 3) {
			return "Rup Var Scen ID";
		} else if (col == 4) {
			return "SGT Var ID";
		} else if (col == 5) {
			return "Count";
		}
		
		return "";
	}
	
	public CybershakePeakAmplitudeSiteRecord getAmpsAtRow(int row) {
		row = this.getRowCount() - row - 1;
		return amps.get(row);
	}

	public Object getValueAt(int row, int col) {
		CybershakePeakAmplitudeSiteRecord amps = getAmpsAtRow(row);
		
		if (col == 0) {
			return amps.getSiteID();
		} else if (col == 1) {
			CybershakeSite site = siteID_NameMap.get(amps.getSiteID());
			return site.short_name;
		} else if (col == 2) {
			return amps.getErfID();
		} else if (col == 3) {
			return amps.getRupVarScenID();
		} else if (col == 4) {
			return amps.getSgtVarID();
		} else if (col == 5) {
			Integer count = countMap.get(amps.toString());
			if (count == null) {
				return "(not counted)";
			} else {
				return count;
			}
		}
		
		return null;
	}

}
