package org.opensha.sha.cybershake.maps;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.cpt.CPT;

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
			units += " PCV (cm/sec)";
			prefixAdd = "_psv";
		} else {
			units += " Sa (g)";
			prefixAdd = "_sa";
		}
		
		if (probData != null) {
			GMT_Map probMap = buildScatterMap(probData, "Prob. MCER, "+units, cpt);
			FaultBasedMapGen.plotMap(outputDir, "prob_mcer"+prefixAdd, false, probMap);
		}
		// TODO deterministic
		
		// TODO det lower limit
		
		// TODO combinded MCER
		
		// TODO governing scatter
	}
	
	private static GMT_Map buildScatterMap(GeoDataSet data, String label, CPT cpt) {
		data = data.copy();
		data.log10();
		label = "Log10("+label+")";
		
		GMT_Map map = new GMT_Map(region, data, interpSettings.getInterpSpacing(), cpt);
		map.setInterpSettings(interpSettings);
		map.setLogPlot(false); // already did manually
		map.setMaskIfNotRectangular(true);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		map.setCustomLabel(label);
		
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
