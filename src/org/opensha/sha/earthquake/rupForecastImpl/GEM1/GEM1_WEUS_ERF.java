package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelData.nshmp.us.NshmpUsData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1_WEUS_ERF extends GEM1_US_ERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String NAME = "GEM1 WEUS ERF";
	
	private static double default_latmin = 24.6;
	private static double default_latmax = 50.0;
	private static double default_lonmin = -126.0;
	private static double default_lonmax = -100.0;
	
	public GEM1_WEUS_ERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1_WEUS_ERF(CalculationSettings calcSet) throws IOException {
		super(default_latmin,default_latmax,default_lonmin,default_lonmax, calcSet);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
