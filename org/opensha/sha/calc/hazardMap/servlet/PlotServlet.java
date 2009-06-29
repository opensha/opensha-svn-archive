package org.opensha.sha.calc.hazardMap.servlet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.gridComputing.StorageHost;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.servlet.GMT_MapGeneratorServlet;
import org.opensha.sha.calc.hazardMap.MakeXYZFromHazardMapDir;

public class PlotServlet extends ConfLoadingServlet {
	
//	public static final String XYZ_FILES_DIR = "/scratch/opensha/xyzDatasets/";
	public static final String XYZ_FILES_DIR = "/home/scec-01/opensha/xyzDatasets/";
	
	public static final String OP_PLOT = "Plot";
	public static final String OP_RETRIEVE = "Retrieve Data";
	
	private GMT_MapGenerator gmt;
	
	/**
	 * Servlet for plotting and retrieving Hazard Map data
	 * 
	 * Object flow:
	 * Client ==> Server:
	 * * Dataset ID (String)
	 * * operation (String)
	 * * isProbAt_IML (Boolean)
	 * * level (Double)
	 * * overwrite (Boolean)
	 * * If Map:
	 * ** map specification (GMT_Map)
	 * Server ==> Client:
	 * * If Map:
	 * ** Map url
	 * * Else:
	 * ** XYZ dataset (XYZ_DataSetAPI)
	 */
	public PlotServlet() {
		super("PlotServlet");
		gmt = new GMT_MapGenerator();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		debug("Handling GET");
		
		// get an input stream from the applet
		ObjectInputStream in = new ObjectInputStream(request.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
		
		try {
			String id = (String)in.readObject();
			String op = (String)in.readObject();
			
			boolean isProbAt_IML = (Boolean)in.readObject();
			double level = (Double)in.readObject();
			
			boolean overwrite = (Boolean)in.readObject();
			
			GMT_Map map = null;
			
			if (op.equals(OP_PLOT)) {
				map = (GMT_Map)in.readObject();
			}
			
			String xyzDir = XYZ_FILES_DIR + id;
			File xyzDirFile = new File(xyzDir);
			
			try {
				if (!xyzDirFile.exists())
					if (!xyzDirFile.mkdir())
						fail(out, "Couldn't make directory for xyz files!");
				
				StorageHost storage = this.confLoader.getPresets().getStorageHosts().get(0);
				
				String curveDirName = storage.getPath() + File.separator + id + File.separator + "curves";
				File curveDirFile = new File(curveDirName);
				if (!curveDirFile.exists())
					fail(out, "Couldn't find curves for dataset '" + id + "'");
				
				String curveXYZFile = xyzDir + File.separator + "xyzCurves";
				if (isProbAt_IML)
					curveXYZFile += "_PROB";
				else
					curveXYZFile += "_IML";
				curveXYZFile += "_" + level + ".txt";
				
				File curveXYZFileFile = new File(curveXYZFile);
				XYZ_DataSetAPI xyz;
				if (!curveXYZFileFile.exists() || overwrite) {
					MakeXYZFromHazardMapDir maker = new MakeXYZFromHazardMapDir(curveDirName, false, true);
					xyz = maker.getXYZDataset(isProbAt_IML, level, curveXYZFile);
				} else {
					xyz = ArbDiscretizedXYZ_DataSet.loadXYZFile(curveXYZFile);
				}
				
				if (map == null) {
					// they just want the data, no plot
					out.writeObject(xyz);
					out.close();
					return;
				}
				// if we made it this far, then we're making a map!
				map.setGriddedData(xyz);
				
				String url = GMT_MapGeneratorServlet.createMap(gmt, map, null, "", "metadata.txt");
				
				out.writeObject(url);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
				fail(out, e);
			}
		} catch (ClassNotFoundException e) {
			fail(out, "ClassNotFoundException: " + e.getMessage());
			return;
		}
	}

}
