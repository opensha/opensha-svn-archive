package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelData.gshap.africa.GshapAfricaData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1_GSHAP_Africa_ERF extends GEM1ERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static String NAME = "GEM1 GSHAP Africa ERF";
	
	
	public GEM1_GSHAP_Africa_ERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1_GSHAP_Africa_ERF(CalculationSettings calcSet) throws IOException {
		super(null, calcSet);
	}
	
	private void initSourceData() {
		try {
			if (gemSourceDataList == null)
				gemSourceDataList = new GshapAfricaData().getList();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
