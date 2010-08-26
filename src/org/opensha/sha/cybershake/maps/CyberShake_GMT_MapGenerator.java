package org.opensha.sha.cybershake.maps;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GeoTools;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.SecureMapGenerator;
import org.opensha.commons.mapping.gmt.elements.CoastAttributes;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.plot.ScatterSymbol;

public class CyberShake_GMT_MapGenerator implements SecureMapGenerator {
	
	public static int[] dpis = {72, 150, 300};
	
	private static ArbDiscretizedXYZ_DataSet getLogXYZ(XYZ_DataSetAPI orig) {
		ArbDiscretizedXYZ_DataSet log = new ArbDiscretizedXYZ_DataSet();
		
		ArrayList<Double> x = orig.getX_DataSet();
		ArrayList<Double> y = orig.getY_DataSet();
		ArrayList<Double> z = orig.getZ_DataSet();
		
		for (int i=0; i<orig.getX_DataSet().size(); i++) {
			double zVal = z.get(i);
			if (zVal < 0)
				throw new RuntimeException("log cannot be taken with dataset values < 0");
			if (zVal == 0)
				zVal = Double.NaN;
			else
				zVal = Math.log(z.get(i));
			log.addValue(x.get(i), y.get(i), zVal);
		}
		
		return log;
	}
	
	public ArrayList<String> getGMT_ScriptLines(GMT_Map map, String dir) throws GMT_MapException {
		if (!(map instanceof InterpDiffMap))
			throw new IllegalArgumentException("map must be of type InterpDiffMap!");
		return getGMT_ScriptLines((InterpDiffMap)map, dir);
	}
	
	public ArrayList<String> getGMT_ScriptLines(InterpDiffMap map, String dir) throws GMT_MapException {
		System.out.println("Generating map for dir: " + dir);
		
		if (!dir.endsWith(File.separator))
			dir += File.separator;

		ArrayList<String> rmFiles = new ArrayList<String>();
		
		String commandLine;

		ArrayList<String> gmtCommandLines = new ArrayList<String>();
		
		InterpDiffMapType[] mapTypes = map.getMapTypes();
		if (mapTypes == null)
			mapTypes = InterpDiffMapType.values();
		
		boolean shouldInterp = false;
		boolean shouldMakeTopo = false;
		for (InterpDiffMapType type : mapTypes) {
			if (type != InterpDiffMapType.BASEMAP) {
				// if it's anything but a basemap, we need to do the interpolation
				shouldInterp = true;
			}
			if (type != InterpDiffMapType.DIFF) {
				// if it's anything but a diff map, we need to prepare the topo
				shouldMakeTopo = map.getTopoResolution() != null;
			}
		}
		
		double minLon = map.getRegion().getMinLon();
		double maxLon = map.getRegion().getMaxLon();
		double minLat = map.getRegion().getMinLat();
		double maxLat = map.getRegion().getMaxLat();
		
		gmtCommandLines.add("#!/bin/bash");
		gmtCommandLines.add("");
		gmtCommandLines.add("cd " + dir);
		gmtCommandLines.add("");
		gmtCommandLines.addAll(GMT_MapGenerator.getGMTPathEnvLines());
		String regionVals = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;
		gmtCommandLines.add("REGION=\""+regionVals+"\"");
		String region = " $REGION ";
		double plotWdth = 6.5;
		String proj = " $PROJ ";
		gmtCommandLines.add("PROJ=\"-JM"+plotWdth+"i\"");
		gmtCommandLines.add("## Plot Script ##");
		gmtCommandLines.add("");
		
		double mapGridSpacing = map.getGriddedDataInc();
		GMT_InterpolationSettings interpSettings = map.getInterpSettings();
		double interpGridSpacing = interpSettings.getInterpSpacing();
		
		XYZ_DataSetAPI griddedData = map.getGriddedData();
		XYZ_DataSetAPI scatterData = map.getScatter();
		if (map.isLogPlot()) {
			griddedData = getLogXYZ(griddedData);
			scatterData = getLogXYZ(scatterData);
		}
		
		// write the basemap
		String basemapXYZName = map.getXyzFileName();
		try {
			ArbDiscretizedXYZ_DataSet.writeXYZFile(griddedData, dir + basemapXYZName);
		} catch (IOException e) {
			throw new GMT_MapException("Could not write XYZ data to a file", e);
		}
		String baseGRD = "base_map.grd";
		rmFiles.add(baseGRD);
		gmtCommandLines.add("# convert xyz file to grd file");
		commandLine = "${GMT_PATH}xyz2grd "+ basemapXYZName +" -G"+ baseGRD+ " -I"+mapGridSpacing+
						region +" -D/degree/degree/amp/=/=/=  -: -H0";
		gmtCommandLines.add(commandLine+"\n");
		
		String interpUnsampledGRD = "interpolated.grd";
		String interpSampledGRD = "interp_resampled.grd";
		if (shouldInterp) {
			// do the interpolation
			String interpXYZName = "scatter_points.xyz";
			try {
				ArbDiscretizedXYZ_DataSet.writeXYZFile(scatterData, dir + interpXYZName);
			} catch (IOException e) {
				throw new GMT_MapException("Could not write XYZ data to a file", e);
			}
			rmFiles.add(interpUnsampledGRD);
			gmtCommandLines.add("# do GMT interpolation on the scatter data");
			commandLine = "${GMT_PATH}surface "+ interpXYZName +" -G"+ interpUnsampledGRD+ " -I"+interpGridSpacing
							+region+interpSettings.getConvergenceArg()+" "+interpSettings.getSearchArg()
							+" "+interpSettings.getTensionArg()+" -: -H0";
			gmtCommandLines.add(commandLine);
			// resample the interpolation
			
			rmFiles.add(interpSampledGRD);
			if (interpGridSpacing == mapGridSpacing) {
				gmtCommandLines.add("# the grid spacings are equal, we can just copy");
				gmtCommandLines.add("cp " + interpUnsampledGRD + " " + interpSampledGRD);
			} else {
				gmtCommandLines.add("# resample the interpolated file");
				boolean bicubic = true;
				commandLine = "${GMT_PATH}grdsample "+interpUnsampledGRD+" -G"+interpSampledGRD
								+" -I"+mapGridSpacing+region;
				if (!bicubic)
					commandLine += "-Q";
				gmtCommandLines.add(commandLine+"\n");
			}
		}
		
		// write the CPT
		String cptFile = "cptFile.cpt";
		CPT cpt = map.getCpt();
		try {
			cpt.writeCPTFile(dir + cptFile);
		} catch (IOException e) {
			throw new GMT_MapException("Could not write custom CPT file", e);
		}
		
		gmtCommandLines.add("# Set GMT paper/font defaults");
		commandLine = "${GMT_PATH}gmtset ANOT_FONT_SIZE 14p LABEL_FONT_SIZE 18p PAGE_COLOR" +
				" 0/0/0 PAGE_ORIENTATION portrait PAPER_MEDIA csmap BASEMAP_FRAME_RGB 255/255/255" +
				" PLOT_DEGREE_FORMAT -D FRAME_WIDTH 0.1i COLOR_FOREGROUND 255/255/255";
		gmtCommandLines.add(commandLine+"\n");
		
		String maskGRD = null;
		if (!map.getRegion().isRectangular()) {
			String maskName = "mask.xy";
			maskGRD = "mask.grd";
			rmFiles.add(maskGRD);
			try {
				writeMaskFile(map.getRegion(), dir+maskName);
			} catch (IOException e) {
				throw new GMT_MapException("Couldn't write mask file!", e);
			}
			gmtCommandLines.add("# creat mask");
			commandLine = "${GMT_PATH}grdmask "+maskName+region+" -I"+mapGridSpacing+" -NNaN/1/1 -G"+maskGRD;
			gmtCommandLines.add(commandLine+"\n");
		}
		
		String interpDiffGRD = "interp_diff.grd";
		if (shouldInterp) {
			// combine the basemap and interpolated map
			rmFiles.add(interpDiffGRD);
			gmtCommandLines.add("# add the interpolated vals to the basemap");
			gmtCommandLines.add("${GMT_PATH}grdmath "+baseGRD+" "+interpSampledGRD+" ADD = "+interpDiffGRD+"\n");
		}
		
		String intenGRD = null;
		TopographicSlopeFile topoFile = map.getTopoResolution();
		if (shouldMakeTopo) {
			// redefine the region so that maxLat, minLat, and delta fall exactly on the topoIntenFile
			// i don't think we need this, we'll add it back in if needed
//			double topoGridSpacing = GeoTools.secondsToDeg(map.getTopoResolution().resolution());
//			double tempNum = Math.ceil((minLat-topoFile.region().getMinLat())/topoGridSpacing);
//			minLat = tempNum*topoGridSpacing+topoFile.region().getMinLat();
//			tempNum = Math.ceil((minLon-(topoFile.region().getMinLon()))/topoGridSpacing);
//			minLon = tempNum*topoGridSpacing+(topoFile.region().getMinLon());
//			maxLat = Math.floor(((maxLat-minLat)/topoGridSpacing))*topoGridSpacing +minLat;
//			maxLon = Math.floor(((maxLon-minLon)/topoGridSpacing))*topoGridSpacing +minLon;
//			String tregion = " -R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat + " ";
			String topoIntenFile = GMT_MapGenerator.SCEC_GMT_DATA_PATH + topoFile.fileName();

			intenGRD = "topo_inten.grd";
			gmtCommandLines.add("# Cut the topo file to match the data region");
			commandLine="${GMT_PATH}grdcut " + topoIntenFile + " -G"+intenGRD+ " " +region;
			rmFiles.add(intenGRD);
			gmtCommandLines.add(commandLine);
		}
		
		String xOff = " -X1.5i";
		String yOff = " -Y1.5i";
		
		for (InterpDiffMapType mapType : mapTypes) {
			gmtCommandLines.add("# PLOTTING: "+mapType+"\n");
			String grdFile;
			String psFile = mapType.getPrefix()+".ps";
			Color markerColor = mapType.getMarkerColor();
			boolean markers = markerColor != null;
			if (mapType == InterpDiffMapType.BASEMAP) {
				grdFile = baseGRD;
			} else if (mapType == InterpDiffMapType.INTERP_MARKS) {
				grdFile = interpDiffGRD;
			} else if (mapType == InterpDiffMapType.INTERP_NOMARKS) {
				grdFile = interpDiffGRD;
			} else {
				// TODO implement DIFF
				continue;
			}
			
			if (maskGRD != null) {
				String unmaskedGRD = "unmasked_"+grdFile;
				rmFiles.add(unmaskedGRD);
				gmtCommandLines.add("mv "+grdFile+" "+unmaskedGRD);
				gmtCommandLines.add("${GMT_PATH}grdmath "+unmaskedGRD+" "+maskGRD+" MUL = "+grdFile+"\n");
			}
			
			int dpi = map.getDpi();
			String gmtSmoothOption="";
			if (!map.isUseGMTSmoothing()) gmtSmoothOption=" -T ";
			// generate the image depending on whether topo relief is desired
			String topoOption = "";
			if (shouldMakeTopo) {
				String topoResGRD = "topores_"+grdFile;
				rmFiles.add(topoResGRD);
				gmtCommandLines.add("# Resample the map to the topo resolution");
				commandLine="${GMT_PATH}grdsample "+grdFile+" -G"+topoResGRD+" -I" +
				topoFile.resolution() + "c -Q "+region;
				gmtCommandLines.add(commandLine);
				grdFile = topoResGRD;
				topoOption = " -I"+intenGRD;
			}
			gmtCommandLines.add("# Plot the gridded data");
			commandLine="${GMT_PATH}grdimage "+ grdFile + xOff + yOff + proj + topoOption + " -C"+cptFile+
							" "+gmtSmoothOption+" -K -E"+dpi+ region + " > " + psFile;
			gmtCommandLines.add(commandLine+"\n");
			
			if (markers) {
				gmtCommandLines.add("# scatter markers");
				ArrayList<Double> xVals = scatterData.getX_DataSet();
				ArrayList<Double> yVals = scatterData.getY_DataSet();
				String colorStr = GMT_MapGenerator.getGMTColorString(markerColor);
				for (int i=0; i<xVals.size(); i++) {
					double x = xVals.get(i);
					double y = yVals.get(i);
					
					commandLine = "echo " + x + " " + y + " | ";
					commandLine += "${GMT_PATH}psxy"+region+proj+"-S"+ScatterSymbol.SYMBOL_INVERTED_TRIANGLE
									+"0.03i -G"+colorStr + " -W0.0162i,"+colorStr + " -: -K -O >> "+psFile;
					gmtCommandLines.add(commandLine);
				}
			}
			
			GMT_MapGenerator.addSpecialElements(gmtCommandLines, map, region, proj, psFile);
			
			GMT_MapGenerator.addColorbarCommand(gmtCommandLines, map,
					(double)cpt.getMinValue(), (double)cpt.getMaxValue(), cptFile, psFile);
			
			gmtCommandLines.add("# basemap");
			commandLine = "${GMT_PATH}psbasemap -B0.5/0.5eWNs"+region+proj+"-O >> "+psFile;
			gmtCommandLines.add(commandLine+"\n");
			
			
			gmtCommandLines.add("# conversions");
			for (int odpi : dpis) {
				String convertArgs = "-density " + odpi;
				String fName = mapType.getPrefix() + "." + odpi + ".png";

				// add a command line to convert the ps file to a jpg file - using convert
				gmtCommandLines.add("${CONVERT_PATH} " + convertArgs + " " + psFile + " " + fName);
			}
		}
		
		return gmtCommandLines;
	}
	
	private static void writeMaskFile(Region region, String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		
		Location first = null;
		for (Location loc : region.getBorder()) {
			if (first == null)
				first = loc;
			fw.write(loc.getLongitude() + " " + loc.getLatitude() + "\n");
		}
		fw.write(first.getLongitude() + " " + first.getLatitude() + "\n");
		
		fw.close();
	}

}
