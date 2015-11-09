package org.opensha.commons.data.siteData.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.siteData.AbstractSiteData;
import org.opensha.commons.data.siteData.servlet.SiteDataServletAccessor;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.RunScript;
import org.opensha.commons.util.ServerPrefUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class STREC_DataWrapper extends AbstractSiteData<String> {
	
	private static final boolean D = false;
	
	private File pythonScript;
	private SiteDataServletAccessor<String> accessor;
	
	private Region region = Region.getGlobalRegion(); 
	
	public static final String SERVLET_URL = ServerPrefUtils.SERVER_PREFS.getServletBaseURL()
			+"SiteData/STREC_DataWrapper";
	
	public STREC_DataWrapper(File pythonScript) {
		Preconditions.checkState(pythonScript.exists(),
				"Python script doesn't exist: %s", pythonScript.getAbsolutePath());
		this.pythonScript = pythonScript;
	}
	
	public STREC_DataWrapper() {
		accessor = new SiteDataServletAccessor<String>(this, SERVLET_URL);
	}

	@Override
	public Region getApplicableRegion() {
		return region;
	}

	@Override
	public double getResolution() {
		return 0;
	}

	@Override
	public String getName() {
		return "STREC Tectonic Regime Wrapper";
	}

	@Override
	public String getShortName() {
		return "STREC_DataWrapper";
	}

	@Override
	public String getDataType() {
		return TYPE_TECTONIC_REGIME;
	}

	@Override
	public String getDataMeasurementType() {
		return TYPE_FLAG_INFERRED;
	}

	@Override
	public Location getClosestDataLocation(Location loc) throws IOException {
		return loc;
	}

	@Override
	public ArrayList<String> getValues(LocationList locs) throws IOException {
		if (accessor != null)
			return accessor.getValues(locs);
		if (locs.isEmpty())
			return new ArrayList<String>();
		File tempDir = null;
		ArrayList<String> ret = null;
		try {
			tempDir = Files.createTempDir();
			
			if (D) System.out.println("Temp dir: "+tempDir.getAbsolutePath());
			
			String args = "--csv-out";
			
			if (locs.size() > 1) {
				// batch mode
				File inputFile = new File(tempDir, "input.txt");
				FileWriter fw = new FileWriter(inputFile);
				for (Location loc : locs)
					// use placeholder mag, not used for regionalization
					fw.write(loc.getLatitude()+" "+loc.getLongitude()+" "+loc.getDepth()+" 7.0\n");
				fw.close();
				
				args += " --batch-input "+inputFile.getAbsolutePath();
			} else {
				Location loc = locs.get(0);
				args += " "+loc.getLatitude()+" "+loc.getLongitude()+" "+loc.getDepth()+" 7.0";
			}
			
			File outputFile = new File(tempDir, "output.txt");
			
			File runScript = new File(tempDir, "runSTREC.sh");
			
			FileWriter fw = new FileWriter(runScript);
			
			fw.write("#!/bin/bash\n");
			fw.write("\n");
			fw.write(pythonScript.getAbsolutePath()+" "+args+" > "+outputFile.getAbsolutePath()+"\n");
			
			fw.close();
			
			String[] command ={"sh","-c","sh "+runScript.getAbsolutePath()};
			
			if (D) System.out.println("Running script: "+runScript.getAbsolutePath());
			RunScript.runScript(command);
			
			Preconditions.checkState(outputFile.exists());
			
			ret = Lists.newArrayList();
			
			CSVFile<String> csv = CSVFile.readFile(outputFile, false);
			for (int row=0; row<csv.getNumRows(); row++) {
				String regime = csv.get(row, 10);
				Preconditions.checkNotNull(regime);
				ret.add(regime);
			}
			
			Preconditions.checkState(ret.size() == locs.size(), "Bad output size");
		} finally {
			if (tempDir != null) {
				if (D) System.out.println("Deleting "+tempDir.getAbsolutePath());
				FileUtils.deleteDirectory(tempDir);
			}
		}
		return ret;
	}

	@Override
	public String getValue(Location loc) throws IOException {
		LocationList locs = new LocationList();
		
		locs.add(loc);
		
		return getValues(locs).get(0);
	}

	@Override
	public boolean isValueValid(String el) {
		return el != null && el.length() > 0;
	}

	@Override
	public String getMetadata() {
		return "Java wrapper to F-E region Perl Script, updated for Garcia subregions";
	}
	
	public static void main(String[] args) {
		STREC_DataWrapper garcia = new STREC_DataWrapper(
				new File("/scratch/opensha/strec/anaconda2/bin/getstrec_bulk.py"));
//		STREC_DataWrapper garcia = new STREC_DataWrapper();
		
		LocationList locs = new LocationList();
		
		locs.add(new Location(28.2305, 84.7314, 8.22));
		locs.add(new Location(35, -118, 7d));
		locs.add(new Location(35, -50, 7d));
		for (int i=0; i<10; i++) {
			double lat = 180d*Math.random()-90d;
			double lon = 360d*Math.random()-180d;
			double depth = 20d*Math.random();
			locs.add(new Location(lat, lon, depth));
		}
		
		try {
			List<String> vals = garcia.getValues(locs);
			for (int i=0; i<locs.size(); i++)
				System.out.println(locs.get(i)+": "+vals.get(i));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
