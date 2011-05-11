package org.opensha.refFaultParamDb.gui.view;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;

public class SectionInfoCSVFileWriter extends AbstractSectionInfoFileWriter {

	private static String[] colNames =
		{ "Fault Section ID", "Fault Name", "Longitude", "Latitude", "Depth" };
	private boolean topBottomOnly;
	
	private static boolean aseisReducesArea = true;
	
	public SectionInfoCSVFileWriter(DB_AccessAPI dbConnection, boolean topBottomOnly) {
		super(dbConnection);
		this.topBottomOnly = topBottomOnly;
	}

	@Override
	public String getFaultAsString(FaultSectionPrefData prefData) {
		String line = "";
		String id = ""+prefData.getSectionId();
		String name = prefData.getSectionName();
		StirlingGriddedSurface surf =
			new StirlingGriddedSurface(prefData.getSimpleFaultData(aseisReducesArea), 1.0);
		for (int row=0; row<surf.getNumRows(); row++) {
			if (topBottomOnly && row != 0 && row != (surf.getNumRows()-1))
				continue;
			for (int col=0; col<surf.getNumCols(); col++) {
				Location loc = surf.getLocation(row, col);
				String lat = ""+loc.getLatitude();
				String lon = ""+loc.getLongitude();
				String depth = ""+loc.getDepth();
				String[] vals = { id, name, lon, lat, depth };
				line += CSVFile.getLineStr(vals) + "\n";
			}
		}
		return line;
	}

	@Override
	public String getFileHeader() {
		return CSVFile.getLineStr(colNames);
	}

}
