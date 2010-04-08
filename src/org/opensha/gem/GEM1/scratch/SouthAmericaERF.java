package org.opensha.gem.GEM1.scratch;

import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelData.nshmp.south_america.NshmpSouthAmericaData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class SouthAmericaERF extends GEM1ERF {
	
	private static CalculationSettings calcSet = new CalculationSettings();
	
	private static double latmin = -55;
	private static double latmax = 15;
	private static double lonmin = -85;
	private static double lonmax = -30;
	
	public SouthAmericaERF() throws IOException {
		super(new NshmpSouthAmericaData(latmin,latmax,lonmin,lonmax).getList(), calcSet);
	}

}
