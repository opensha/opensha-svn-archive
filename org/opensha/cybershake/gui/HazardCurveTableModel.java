package org.opensha.cybershake.gui;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import org.opensha.cybershake.db.CybershakeHazardCurveRecord;
import org.opensha.cybershake.db.CybershakeIM;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurve2DB;
import org.opensha.cybershake.db.SiteInfo2DB;

public class HazardCurveTableModel extends AbstractTableModel {
	
	public static int NUM_COLUMNS = 10;
	
	HazardCurve2DB curve2db;
	SiteInfo2DB site2db;
	
	ArrayList<CybershakeHazardCurveRecord> curves = new ArrayList<CybershakeHazardCurveRecord>();
	
	HashMap<Integer, CybershakeSite> siteID_NameMap = new HashMap<Integer, CybershakeSite>();
	HashMap<Integer, CybershakeIM> imTypeMap = new HashMap<Integer, CybershakeIM>();
	
	public HazardCurveTableModel(DBAccess db) {
		this.curve2db = new HazardCurve2DB(db);
		this.site2db = new SiteInfo2DB(db);
		
		this.reloadCurves();
	}
	
	public void reloadCurves() {
		curves = curve2db.getAllHazardCurveRecords();
		for (CybershakeHazardCurveRecord curve : curves) {
			CybershakeSite site = siteID_NameMap.get(curve.getSiteID());
			if (site == null) {
				site = site2db.getSiteFromDB(curve.getSiteID());
				siteID_NameMap.put(site.id, site);
			}
			CybershakeIM im = imTypeMap.get(curve.getImTypeID());
			if (im == null) {
				im = curve2db.getIMFromID(curve.getImTypeID());
				imTypeMap.put(im.getID(), im);
			}
		}
		this.fireTableDataChanged();
	}

	public int getColumnCount() {
		return NUM_COLUMNS;
	}

	public int getRowCount() {
		return curves.size();
	}
	
	// columns:
	// 0: CurveID | 1: Site_ID | 2: Site Short Name | 3: Site Long Name | 4: Date | 5: ERF_ID | 6: IM_Type_ID |
	//		 7: SA Period | 8: Rup_Var_Scen_ID | 9: SGT_Var_ID 

	public String getColumnName(int col) {
		if (col == 0) {
			return "Curve ID";
		} else if (col == 1) {
			return "Site ID";
		} else if (col == 2) {
			return "Site Name";
		} else if (col == 3) {
			return "Site Long Name";
		} else if (col == 4) {
			return "Date";
		} else if (col == 5) {
			return "ERF ID";
		} else if (col == 6) {
			return "IM Type ID";
		} else if (col == 7) {
			return "SA Period";
		} else if (col == 8) {
			return "Rup Var Scen ID";
		} else if (col == 9) {
			return "SGT Var ID";
		}
		
		return "";
	}
	
	public CybershakeHazardCurveRecord getCurveAtRow(int row) {
		row = this.getRowCount() - row - 1;
		return curves.get(row);
	}

	public Object getValueAt(int row, int col) {
		CybershakeHazardCurveRecord curve = getCurveAtRow(row);
		
		if (col == 0) {
			return curve.getCurveID();
		} else if (col == 1) {
			return curve.getSiteID();
		} else if (col == 2) {
			CybershakeSite site = siteID_NameMap.get(curve.getSiteID());
			return site.short_name;
		} else if (col == 3) {
			CybershakeSite site = siteID_NameMap.get(curve.getSiteID());
			return site.name;
		} else if (col == 4) {
			return curve.getDate();
		} else if (col == 5) {
			return curve.getErfID();
		} else if (col == 6) {
			return curve.getImTypeID();
		} else if (col == 7) {
			CybershakeIM im = imTypeMap.get(curve.getImTypeID());
			return im.getVal();
		} else if (col == 8) {
			return curve.getRupVarScenID();
		} else if (col == 9) {
			return curve.getSgtVarID();
		}
		
		return null;
	}

}
