package org.opensha.sha.cybershake.maps;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol.Symbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbolSet;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.cybershake.calc.mcer.RTGMCalc;

import scratch.UCERF3.analysis.FaultBasedMapGen;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MCERMapGenerator {
	
	private static Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
	private static final GMT_InterpolationSettings  interpSettings =
			GMT_InterpolationSettings.getDefaultSettings();
	
	public static void generateMaps(GeoDataSet probData, GeoDataSet detData,
			GeoDataSet detLowerLimit, File outputDir, double period, boolean psv)
					throws IOException, GMT_MapException {
		Preconditions.checkArgument(probData != null || detData != null || detLowerLimit != null);
		boolean generateMCER = probData != null && detData != null;
		if (generateMCER && detLowerLimit == null)
			System.err.println("WARNING: Calculating MCER without deterministic lower limit");
		
		CPT cpt = buildCPT(period, psv);
		
		String units = (float)period+"s";
		String prefixAdd;
		if (psv) {
			units += " PSV (cm/sec)";
			prefixAdd = "_psv";
		} else {
			units += " Sa (g)";
			prefixAdd = "_sa";
		}
		
		if (probData != null) {
			GMT_Map probMap = buildScatterMap(probData, psv, period, "Prob. MCER, "+units, cpt);
			FaultBasedMapGen.plotMap(outputDir, "prob_mcer"+prefixAdd+"_marks", false, probMap);
			probMap.setSymbolSet(null);
			FaultBasedMapGen.plotMap(outputDir, "prob_mcer"+prefixAdd, false, probMap);
			probMap.setContourIncrement(0.1);
			FaultBasedMapGen.plotMap(outputDir, "prob_mcer"+prefixAdd+"_contours", false, probMap);
			probMap.setContourOnly(true);
			FaultBasedMapGen.plotMap(outputDir, "prob_mcer"+prefixAdd+"_contours_only", false, probMap);
		}
		// TODO deterministic
		
		// TODO det lower limit
		
		// TODO combinded MCER
		
		// TODO governing scatter
	}
	
	private static GMT_Map buildScatterMap(GeoDataSet data, boolean psv, double period, String label, CPT cpt) {
		data = data.copy();
		if (psv)
			for (int index=0; index<data.size(); index++)
				data.set(index, RTGMCalc.saToPsuedoVel(data.get(index), period));
		data.log10();
		label = "Log10("+label+")";
		
		GMT_Map map = new GMT_Map(region, data, interpSettings.getInterpSpacing(), cpt);
		map.setInterpSettings(interpSettings);
		map.setLogPlot(false); // already did manually
		map.setMaskIfNotRectangular(true);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
//		map.setTopoResolution(null);
		map.setBlackBackground(false);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		map.setCustomLabel(label);
//		map.setDpi(150);
		
		// now add scatter
		PSXYSymbolSet xySet = new PSXYSymbolSet();
		CPT xyCPT = new CPT(0d, 1d, Color.WHITE, Color.WHITE);
		xySet.setCpt(xyCPT);
		for (Location loc : data.getLocationList()) {
			PSXYSymbol sym = new PSXYSymbol(new Point2D.Double(loc.getLongitude(), loc.getLatitude()),
					Symbol.INVERTED_TRIANGLE, 0.08f, 0f, null, Color.WHITE);
			xySet.addSymbol(sym, 0d);
//			symbols.add(sym);
		}
//		map.setSymbols(symbols);
		map.setSymbolSet(xySet);
		
		return map;
	}
	
	private static CPT buildCPT(double period, boolean psv) throws IOException {
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		
		if (psv)
			cpt = cpt.rescale(Math.log10(2e1), Math.log10(2e3));
		else
			cpt = cpt.rescale(-1, 1);
		
		return cpt;
	}

	public static void main(String[] args) throws IOException, GMT_MapException {
		// -1 here means RTGM
		GeoDataSet probData = HardCodedInterpDiffMapCreator.getMainScatter(true, -1, Lists.newArrayList(35), 21);
		
		generateMaps(probData, null, null, new File("/tmp"), 3d, false);
		generateMaps(probData, null, null, new File("/tmp"), 3d, true);
		
		System.exit(0);
	}

}
