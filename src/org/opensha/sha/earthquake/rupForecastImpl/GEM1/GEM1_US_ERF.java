package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelData.nshmp.us.NshmpUsData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1_US_ERF extends GEM1ERF {
	
	public final static String NAME = "GEM1 US ERF";
	
	private static double latmin = 24.6;
	private static double latmax = 50.0;
	private static double lonmin = -125.0;
	private static double lonmax = -65.0;
	
	public GEM1_US_ERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1_US_ERF(CalculationSettings calcSet) throws IOException {
		this(latmin,latmax,lonmin,lonmax, calcSet);
	}
	
	public GEM1_US_ERF(double latmin, double latmax, double lonmin, double lonmax,
			CalculationSettings calcSet) throws IOException {
		super(new NshmpUsData(latmin,latmax,lonmin,lonmax).getList(), calcSet);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
