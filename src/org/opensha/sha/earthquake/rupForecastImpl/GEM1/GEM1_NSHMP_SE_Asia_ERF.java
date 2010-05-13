package org.opensha.sha.earthquake.rupForecastImpl.GEM1;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.opensha.gem.GEM1.calc.gemModelData.nshmp.south_east_asia.NshmpSouthEastAsiaData;
import org.opensha.gem.GEM1.commons.CalculationSettings;

public class GEM1_NSHMP_SE_Asia_ERF extends GEM1ERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static String NAME = "GEM1 GSHAP SE Asia ERF";
	
	private static double default_latmin = -55;
	private static double default_latmax = 15;
	private static double default_lonmin = -85;
	private static double default_lonmax = -30;
	
	private double latmin, latmax, lonmin, lonmax;
	
	public GEM1_NSHMP_SE_Asia_ERF() throws IOException {
		this(new CalculationSettings());
	}
	
	public GEM1_NSHMP_SE_Asia_ERF(CalculationSettings calcSet) throws IOException {
		this(default_latmin,default_latmax,default_lonmin,default_lonmax, calcSet);
	}
	
	public GEM1_NSHMP_SE_Asia_ERF(double latmin, double latmax, double lonmin, double lonmax,
			CalculationSettings calcSet) throws IOException {
		super(null, calcSet);
		this.latmin = latmin;
		this.latmax = latmax;
		this.lonmin = lonmin;
		this.lonmax = lonmax;
	}
	
	private void initSourceData() {
		try {
			if (gemSourceDataList == null)
				gemSourceDataList = new NshmpSouthEastAsiaData(latmin, latmax, lonmin, lonmax).getList();
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
