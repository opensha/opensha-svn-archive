package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelParsers.gshap.south_east_asia.GshapSEAsia2GemSourceData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1_GSHAP_SE_Asia_ERF extends GEM1ERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static String NAME = "GEM1 GSHAP SE Asia ERF";
	
	
	public GEM1_GSHAP_SE_Asia_ERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1_GSHAP_SE_Asia_ERF(CalculationSettings calcSet) throws IOException {
		super(null, calcSet);
	}
	
	private void initSourceData() {
		try {
			if (gemSourceDataList == null)
				gemSourceDataList = new GshapSEAsia2GemSourceData().getList();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateForecast() {
		initSourceData();
		super.updateForecast();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
