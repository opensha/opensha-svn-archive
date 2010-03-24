package org.opensha.gem.GEM1.calc.gemHazardMaps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.gem.GEM1.calc.gemLogicTree.GemLogicTree;
import org.opensha.gem.GEM1.calc.gemLogicTree.gemLogicTreeImpl.gmpe.GemGmpe;
import org.opensha.gem.GEM1.calc.gemModelData.nshmp.south_america.NshmpSouthAmericaData;
import org.opensha.gem.GEM1.scratch.GEM1ERF;
import org.opensha.gem.condor.calc.components.AsciiFileCurveArchiver;
import org.opensha.gem.condor.calc.components.CalculationSettings;
import org.opensha.gem.condor.dagGen.HazardDataSetDAGCreator;
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.util.TectonicRegionType;

public class CalcInputsGenerator {
	
	public static void main(String args[]) throws IOException {
		/*			GMPEs				 */
		GemLogicTree<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> gmpeTree
		= new GemGmpe().getGemLogicTree();
		ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> maps =
			new ArrayList<HashMap<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI>>();
		maps.add(gmpeTree.getEBMap().get("1"));
		maps.add(gmpeTree.getEBMap().get("2"));
		
		/*			Sites				*/
		double latmin = -55;
		double latmax = 15;
		double lonmin = -85;
		double lonmax = -30;
		Location topLeft = new Location(latmax, lonmin);
		Location bottomRight = new Location(latmin, lonmax);
		double spacing = 0.1;
		GriddedRegion region = new GriddedRegion(topLeft, bottomRight, spacing, topLeft);
		ArrayList<Site> sites = new ArrayList<Site>();
		for (Location loc : region.getNodeList()) {
			Site site = new Site(loc);
			// USE DEFAULT SITE VALUES!
			for (HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> map : maps) {
				for (TectonicRegionType tect : map.keySet()) {
					ScalarIntensityMeasureRelationshipAPI imr = map.get(tect);
					ListIterator<ParameterAPI> it = imr.getSiteParamsIterator();
					while (it.hasNext()) {
						ParameterAPI param = it.next();
						try {
							site.getParameter(param.getName());
						} catch (ParameterException e) {
							// if we want to assign custom vals here, we need to clone.
							site.addParameter(param);
						}
					}
				}
			}
			sites.add(site);
		}
		
		/*			Calc Settings		*/
		CalculationSettings settings = new CalculationSettings(
				CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function(), 200d);
		
		/*			ERF					*/
		NshmpSouthAmericaData model = new NshmpSouthAmericaData(latmin,latmax,lonmin,lonmax);
		GEM1ERF modelERF = new GEM1ERF(model.getList(),new org.opensha.gem.GEM1.commons.CalculationSettings());
		
		/*			Archiver			*/
		boolean binByLat = true;
		boolean binByLon = false;
		String outputDir = "";		//TODO - fill this in
		String curveOutputDir = outputDir + "/curves";
		AsciiFileCurveArchiver archiver = new AsciiFileCurveArchiver(curveOutputDir, binByLat, binByLon);
		
		String javaExec = "";		//TODO - fill this in
		String jarFile = "";		//TODO - fill this in
		HazardDataSetDAGCreator dag = new HazardDataSetDAGCreator(modelERF, maps, sites, settings,
				archiver, javaExec, jarFile);
		
		int sitesPerJob = 400;
		dag.writeDAG(new File(outputDir), sitesPerJob, false);
	}

}
