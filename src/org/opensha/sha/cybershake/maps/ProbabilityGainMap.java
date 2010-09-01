package org.opensha.sha.cybershake.maps;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.util.GMT_GrdFile;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;

public class ProbabilityGainMap {
	
	private InterpDiffMap referenceMap;
	private InterpDiffMap modifiedMap;
	
	private Double colorScaleMin = null;
	private Double colorScaleMax = null;
	
	private boolean logPlot = false;
	
	public ProbabilityGainMap(InterpDiffMap referenceMap, InterpDiffMap modifiedMap) {
		if (referenceMap.isLogPlot() || modifiedMap.isLogPlot())
			throw new RuntimeException("Probability Gain input maps cannot be in log space!");
		this.referenceMap = referenceMap;
		this.modifiedMap = modifiedMap;
	}
	
	public InterpDiffMap convertModifiedToProbGain(String refGRDFile, String modGRDFile) throws IOException {
		return convertModifiedToProbGain(new GMT_GrdFile(refGRDFile, true), new GMT_GrdFile(modGRDFile, true));
	}
	
	/**
	 * This turns modifiedMap into a probability gain map.
	 * 
	 * @param refGRDFile
	 * @param modGRDFile
	 * @return
	 */
	public InterpDiffMap convertModifiedToProbGain(GMT_GrdFile refGRDFile, GMT_GrdFile modGRDFile) {
		ArbDiscretizedXYZ_DataSet refXYZ = refGRDFile.getXYZDataset(true);
		ArbDiscretizedXYZ_DataSet modXYZ = refGRDFile.getXYZDataset(true);
		
		ArbDiscretizedXYZ_DataSet gainXYZ = getProbabilityGain(refXYZ, modXYZ);
		
		modifiedMap.setGriddedData(gainXYZ);
		modifiedMap.setScatter(null);
		InterpDiffMapType[] mapTypes = { InterpDiffMapType.BASEMAP };
		modifiedMap.setMapTypes(mapTypes);
		
		modifiedMap.setCustomScaleMin(colorScaleMin);
		modifiedMap.setCustomScaleMax(colorScaleMax);
		modifiedMap.setLogPlot(logPlot);
		
		return modifiedMap;
	}
	
	public static ArbDiscretizedXYZ_DataSet getProbabilityGain(ArbDiscretizedXYZ_DataSet refXYZ,
			ArbDiscretizedXYZ_DataSet modXYZ) {
		ArbDiscretizedXYZ_DataSet gainXYZ = new ArbDiscretizedXYZ_DataSet();
		
		ArrayList<Double> refXVals = refXYZ.getX_DataSet();
		ArrayList<Double> refYVals = refXYZ.getY_DataSet();
		ArrayList<Double> refZVals = refXYZ.getZ_DataSet();
		
		ArrayList<Double> modXVals = refXYZ.getX_DataSet();
		ArrayList<Double> modYVals = refXYZ.getY_DataSet();
		ArrayList<Double> modZVals = refXYZ.getZ_DataSet();
		
		for (int refInd=0; refInd<refXVals.size(); refInd++) {
			double refX = refXVals.get(refInd);
			double refY = refYVals.get(refInd);
			double refZ = refZVals.get(refInd);
			for (int modInd=0; modInd<refXVals.size(); modInd++) {
				double modX = modXVals.get(modInd);
				double modY = modYVals.get(modInd);
				double modZ = modZVals.get(modInd);
				
				if (refX != modX || refY != modY)
					continue;
				
				double gain = modZ / refZ;
				
				gainXYZ.addValue(modX, modY, gain);
			}
		}
		
		return gainXYZ;
	}

	public InterpDiffMap getReferenceMap() {
		return referenceMap;
	}

	public void setReferenceMap(InterpDiffMap referenceMap) {
		this.referenceMap = referenceMap;
	}

	public InterpDiffMap getModifiedMap() {
		return modifiedMap;
	}

	public void setModifiedMap(InterpDiffMap modifiedMap) {
		this.modifiedMap = modifiedMap;
	}

	public Double getColorScaleMin() {
		return colorScaleMin;
	}

	public void setColorScaleMin(Double colorScaleMin) {
		this.colorScaleMin = colorScaleMin;
	}

	public Double getColorScaleMax() {
		return colorScaleMax;
	}

	public void setColorScaleMax(Double colorScaleMax) {
		this.colorScaleMax = colorScaleMax;
	}

	public boolean isLogPlot() {
		return logPlot;
	}

	public void setLogPlot(boolean logPlot) {
		this.logPlot = logPlot;
	}

}
