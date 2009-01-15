package org.opensha.cybershake.gui;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.opensha.cybershake.db.CybershakePeakAmplitudeSiteRecord;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.cybershake.db.SiteInfo2DB;

public class PeakAmpsTableModel extends AbstractTableModel {
	
	public static int NUM_COLUMNS = 7;
	
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
	
	public int deleteAmps(CybershakePeakAmplitudeSiteRecord record) {
		// if it's not a test site, prompt the user
		CybershakeSite site = getSiteForRecord(record);
		if (site.type_id != CybershakeSite.TYPE_TEST_SITE) {
			String title = "Really delete Peak Amplitudes?";
			String message = "Site '" + site.getFormattedName() + "' is not a test site!\n\n" +
					"Are you sure you want to delete the Amplitudes?";
			int response = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
			
			if (response != JOptionPane.YES_OPTION)
				return -1;
		}
		
		int num = this.amps2db.deleteAmpsForSite(record.getSiteID(), record.getErfID(), record.getSgtVarID(), record.getRupVarScenID());
		
		return num;
	}
	
	// columns:
	// 0: Site_ID | 1: Site Short Name | 2: Site Long Name | 3: ERF_ID | 4: Rup_Var_Scen_ID | 5: SGT_Var_ID | 6: Count

	public String getColumnName(int col) {
		if (col == 0) {
			return "Site ID";
		} else if (col == 1) {
			return "Site Name";
		} else if (col == 2) {
			return "Site Long Name";
		} else if (col == 3) {
			return "ERF ID";
		} else if (col == 4) {
			return "Rup Var Scen ID";
		} else if (col == 5) {
			return "SGT Var ID";
		} else if (col == 6) {
			return "Count";
		}
		
		return "";
	}
	
	public CybershakePeakAmplitudeSiteRecord getAmpsAtRow(int row) {
		row = this.getRowCount() - row - 1;
		return amps.get(row);
	}
	
	private CybershakeSite getSiteForRecord(CybershakePeakAmplitudeSiteRecord record) {
		return siteID_NameMap.get(record.getSiteID());
	}

	public Object getValueAt(int row, int col) {
		CybershakePeakAmplitudeSiteRecord amps = getAmpsAtRow(row);
		
		if (col == 0) {
			return amps.getSiteID();
		} else if (col == 1) {
			CybershakeSite site = getSiteForRecord(amps);
			return site.short_name;
		} else if (col == 2) {
			CybershakeSite site = getSiteForRecord(amps);
			return site.name;
		} else if (col == 3) {
			return amps.getErfID();
		} else if (col == 4) {
			return amps.getRupVarScenID();
		} else if (col == 5) {
			return amps.getSgtVarID();
		} else if (col == 6) {
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
