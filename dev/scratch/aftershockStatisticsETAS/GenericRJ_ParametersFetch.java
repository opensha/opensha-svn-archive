package scratch.aftershockStatisticsETAS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.impl.GarciaRegionPerlWrapper;
import org.opensha.commons.data.siteData.impl.STREC_DataWrapper;
import org.opensha.commons.data.siteData.impl.TectonicRegime;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.ExceptionUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public class GenericRJ_ParametersFetch {
	
	private OrderedSiteDataProviderList regimeProviders;
	
	private Map<TectonicRegime, GenericRJ_Parameters> dataMap;
	
	public GenericRJ_ParametersFetch() {
		ArrayList<SiteData<?>> providers = Lists.newArrayList();
		providers.add(new STREC_DataWrapper());
		regimeProviders = new OrderedSiteDataProviderList(providers);
		
		// from Morgan via e-mail 10/6/2015
		dataMap = Maps.newHashMap();
		
		// array contents: [ a, p, c ];
		
		double c = 0.042711;
		
		URL paramsURL = GenericRJ_ParametersFetch.class.getResource("PageEtAlGenericParams_032116.csv");
		try {
			CSVFile<String> csv = CSVFile.readURL(paramsURL, true);
			Preconditions.checkState(csv.getNumCols() == 9, "unexpected number of columns: %s", csv.getNumCols());
			
			for (int row=1; row<csv.getNumRows(); row++) {
				String regimeName = csv.get(row, 0).trim();
				TectonicRegime regime = TectonicRegime.forName(regimeName);
				Preconditions.checkNotNull(regime, "Unknown regime: %s", regimeName);
				
				// columns: regionName,aValue_MaxLike,pValue_MaxLike,aValue_Mean,aValue_Sigma,
				//						aValue_Sigma1,aValue_Sigma0,bValue,cValue
				
				double pValue = Double.parseDouble(csv.get(row, 2));
				double aValue_mean = Double.parseDouble(csv.get(row, 3));
				double aValue_sigma = Double.parseDouble(csv.get(row, 4));
				double aValue_sigma1 = Double.parseDouble(csv.get(row, 5));
				double aValue_sigma0 = Double.parseDouble(csv.get(row, 6));
				double bValue = Double.parseDouble(csv.get(row, 7));
				double cValue = Double.parseDouble(csv.get(row, 8));
				
				dataMap.put(regime, new GenericRJ_Parameters(
						aValue_mean, aValue_sigma, aValue_sigma0, aValue_sigma1, bValue, pValue, cValue));
			}
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}
	
	/**
	 * Fetches a Garcia region for the given location and returns default omori parameters
	 * @param region
	 * @return array of parameters: {a, p, c};
	 */
	public GenericRJ_Parameters get(Location loc) {
		TectonicRegime region = getRegion(loc);
		return get(region);
	}
	
	/**
	 * 
	 * @param region
	 * @return array of parameters: {a, p, c};
	 */
	public GenericRJ_Parameters get(TectonicRegime region) {
		return dataMap.get(region);
	}
	
	public TectonicRegime getRegion(Location loc) {
		ArrayList<SiteDataValue<?>> datas = regimeProviders.getBestAvailableData(loc);
		if (datas.isEmpty())
			return null;
		return (TectonicRegime)datas.get(0).getValue();
	}
	
	public static void main(String[] args) {
		LocationList locs = new LocationList();
		
		locs.add(new Location(28.2305, 84.7314, 8.22));
		locs.add(new Location(35, -118, 7d));
		locs.add(new Location(35, -50, 7d));
		for (int i=0; i<5; i++) {
			double lat = 180d*Math.random()-90d;
			double lon = 360d*Math.random()-180d;
			double depth = 20d*Math.random();
			locs.add(new Location(lat, lon, depth));
		}
		
		GenericRJ_ParametersFetch fetch = new GenericRJ_ParametersFetch();
		for (Location loc : locs) {
			TectonicRegime regime = fetch.getRegion(loc);
			System.out.println(loc+", "+regime+": "+fetch.get(regime));
		}
	}

}
