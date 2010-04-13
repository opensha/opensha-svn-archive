package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelData.nshmp.south_america.NshmpSouthAmericaData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1SouthAmericaERF extends GEM1ERF {
	
	public final static String NAME = "GEM1 South America ERF";
	
	private static double latmin = -55;
	private static double latmax = 15;
	private static double lonmin = -85;
	private static double lonmax = -30;
	
	public GEM1SouthAmericaERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1SouthAmericaERF(CalculationSettings calcSet) throws IOException {
		super(new NshmpSouthAmericaData(latmin,latmax,lonmin,lonmax).getList(), calcSet);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
