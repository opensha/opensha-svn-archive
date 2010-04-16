package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelParsers.forecastML.ForecastML2GemSourceData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1_GlobalSS_ERF extends GEM1ERF {
	
	public static final String NAME = "GEM1 Global Smoothed Seismicity ERF";

	public static final String inputFile = "/org/opensha/gem/GEM1/data/" +
			"global_smooth_seismicity/zechar.triple_s.global.rate_forecast.xml";
	
	public GEM1_GlobalSS_ERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1_GlobalSS_ERF(CalculationSettings calcSet) throws IOException {
		super(new ForecastML2GemSourceData(inputFile).getList(), calcSet);
	}
}
