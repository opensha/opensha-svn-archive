package org.opensha.commons.mapping.gmt;

import java.io.Serializable;
import java.util.ArrayList;

import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.commons.data.region.GeographicRegionAPI;
import org.opensha.commons.data.region.RectangularGeographicRegion;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.mapping.gmt.elements.CoastAttributes;
import org.opensha.commons.mapping.gmt.elements.PSXYPolygon;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbolSet;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.cpt.CPT;

public class GMT_Map implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3365240959132801705L;

	private GeographicRegion region;
	
	private String cptFile = null;
	private CPT cpt = null;
	private boolean rescaleCPT = true;
	private double griddedDataInc;
	private XYZ_DataSetAPI griddedData = null;
	
	public enum HighwayFile {
		ALL			("CA All", "ca_hiwys.all.xy"),
		MAIN		("CA Main", "ca_hiwys.main.xy"),
		OTHER		("CA Other", "ca_hiwys.other.xy");
		
		private final String name;
		private final String fileName;
		HighwayFile(String name, String fileName) {
			this.name = name;
			this.fileName = fileName;
		}
		
		public String fileName() { return fileName; }
		public String description() { return name; }
	}
	private HighwayFile highwayFile = null;
	
	public static GeographicRegion ca_topo_region;
	static {
//		try {
			ca_topo_region  = new GeographicRegion(32, 43, -126, -115);
//		} catch (RegionConstraintException e) {
//			e.printStackTrace();
//		}
	}
	private TopographicSlopeFile topoResolution = null;
	
	private CoastAttributes coast = new CoastAttributes();
	
	private double imageWidth = 6.5;
	
	private String customLabel = null;
	
	private Double customScaleMin = null;
	private Double customScaleMax = null;
	
	private int dpi = 72;
	
	private boolean useGMTSmoothing = true;
	
	private boolean logPlot = false;
	
	private String xyzFileName = GMT_MapGenerator.DEFAULT_XYZ_FILE_NAME;
	
	private String gmtScriptFileName = GMT_MapGenerator.DEFAULT_GMT_SCRIPT_NAME;
	
	private ArrayList<PSXYSymbol> xySymbols = new ArrayList<PSXYSymbol>();
	private ArrayList<PSXYPolygon> xyLines = new ArrayList<PSXYPolygon>();
	private PSXYSymbolSet xySymbolSet = null;
	
	public GMT_Map(GeographicRegion region, XYZ_DataSetAPI griddedData,
			double griddedDataInc, String cptFile) {
		this.region = region;
		setGriddedData(griddedData, griddedDataInc, cptFile);
	}
	
	public GMT_Map(GeographicRegion region, XYZ_DataSetAPI griddedData,
			double griddedDataInc, CPT cpt) {
		this.region = region;
		setGriddedData(griddedData, griddedDataInc, cpt);
	}
	
	/**
	 * Set the gridded XYZ dataset for this map
	 * 
	 * @param griddedData - XYZ dataset
	 * @param griddedDataInc - Degree spacing of dataset
	 * @param cptFile - CPT file
	 */
	public void setGriddedData(XYZ_DataSetAPI griddedData, double griddedDataInc, String cptFile) {
		this.griddedData = griddedData;
		this.griddedDataInc = griddedDataInc;
		this.cptFile = cptFile;
		this.cpt = null;
	}
	
	/**
	 * Set the gridded XYZ dataset for this map
	 * 
	 * @param griddedData - XYZ dataset
	 * @param griddedDataInc - Degree spacing of dataset
	 * @param cpt - CPT object
	 */
	public void setGriddedData(XYZ_DataSetAPI griddedData, double griddedDataInc, CPT cpt) {
		this.griddedData = griddedData;
		this.griddedDataInc = griddedDataInc;
		this.cptFile = null;
		this.cpt = cpt;
	}

	public GeographicRegion getRegion() {
		return region;
	}

	public void setRegion(GeographicRegion region) {
		this.region = region;
	}

	public String getCptFile() {
		return cptFile;
	}

	public void setCptFile(String cptFile) {
		this.cptFile = cptFile;
	}

	public CPT getCpt() {
		return cpt;
	}

	public void setCpt(CPT cpt) {
		this.cpt = cpt;
	}
	
	public boolean isRescaleCPT() {
		return rescaleCPT;
	}
	
	public void setRescaleCPT(boolean rescaleCPT) {
		this.rescaleCPT = rescaleCPT;
	}

	public double getGriddedDataInc() {
		return griddedDataInc;
	}

	public void setGriddedDataInc(double griddedDataInc) {
		this.griddedDataInc = griddedDataInc;
	}

	public XYZ_DataSetAPI getGriddedData() {
		return griddedData;
	}

	public void setGriddedData(XYZ_DataSetAPI griddedData) {
		this.griddedData = griddedData;
	}

	public HighwayFile getHighwayFile() {
		return highwayFile;
	}

	public void setHighwayFile(HighwayFile highwayFile) {
		this.highwayFile = highwayFile;
	}

	public TopographicSlopeFile getTopoResolution() {
		return topoResolution;
	}

	public void setTopoResolution(TopographicSlopeFile topoResolution) {
		this.topoResolution = topoResolution;
	}

	public CoastAttributes getCoast() {
		return coast;
	}

	public void setCoast(CoastAttributes coast) {
		this.coast = coast;
	}

	public double getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
	}

	public String getCustomLabel() {
		return customLabel;
	}

	public void setCustomLabel(String customLabel) {
		this.customLabel = customLabel;
	}
	
	public boolean isCustomScale() {
		return customScaleMin != null && customScaleMax != null && customScaleMin < customScaleMax;
	}
	
	public void clearCustomScale() {
		customScaleMin = null;
		customScaleMax = null;
	}

	public Double getCustomScaleMin() {
		return customScaleMin;
	}

	public void setCustomScaleMin(Double customScaleMin) {
		this.customScaleMin = customScaleMin;
	}

	public Double getCustomScaleMax() {
		return customScaleMax;
	}

	public void setCustomScaleMax(Double customScaleMax) {
		this.customScaleMax = customScaleMax;
	}

	public int getDpi() {
		return dpi;
	}

	public void setDpi(int dpi) {
		this.dpi = dpi;
	}

	public boolean isUseGMTSmoothing() {
		return useGMTSmoothing;
	}

	public void setUseGMTSmoothing(boolean useGMTSmoothing) {
		this.useGMTSmoothing = useGMTSmoothing;
	}

	public boolean isLogPlot() {
		return logPlot;
	}

	public void setLogPlot(boolean logPlot) {
		this.logPlot = logPlot;
	}

	public String getXyzFileName() {
		return xyzFileName;
	}

	public void setXyzFileName(String xyzFileName) {
		this.xyzFileName = xyzFileName;
	}

	public String getGmtScriptFileName() {
		return gmtScriptFileName;
	}

	public void setGmtScriptFileName(String gmtScriptFileName) {
		this.gmtScriptFileName = gmtScriptFileName;
	}

	public ArrayList<PSXYSymbol> getSymbols() {
		return xySymbols;
	}

	public void setSymbols(ArrayList<PSXYSymbol> xySymbols) {
		this.xySymbols = xySymbols;
	}
	
	public void addSymbol(PSXYSymbol symbol) {
		this.xySymbols.add(symbol);
	}

	public ArrayList<PSXYPolygon> getPolys() {
		return xyLines;
	}

	public void setPolys(ArrayList<PSXYPolygon> xyLines) {
		this.xyLines = xyLines;
	}
	
	public void addPolys(PSXYPolygon line) {
		this.xyLines.add(line);
	}

	public PSXYSymbolSet getSymbolSet() {
		return xySymbolSet;
	}

	public void setSymbolSet(PSXYSymbolSet xySymbolSet) {
		this.xySymbolSet = xySymbolSet;
	}

}
