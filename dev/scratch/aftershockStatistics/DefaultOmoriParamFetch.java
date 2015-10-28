package scratch.aftershockStatistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.opensha.commons.data.siteData.impl.GarciaRegionPerlWrapper;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.ExceptionUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public class DefaultOmoriParamFetch {
	
	private GarciaRegionPerlWrapper garciaFetch;
	
	private Map<String, double[]> dataMap;
	
	public DefaultOmoriParamFetch() {
		garciaFetch = new GarciaRegionPerlWrapper();
		
		// from Morgan via e-mail 10/6/2015
		dataMap = Maps.newHashMap();
		
		// array contents: [ a, p, c ];
		
		double c = 0.042711;
		
		URL paramsURL = DefaultOmoriParamFetch.class.getResource("omori_params.txt");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(paramsURL.openStream()));
			
			String line;
			while ((line = in.readLine()) != null) {
				// example line: ANSR-ABSLDEC: a = -1.99, p = 1.2058
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				Preconditions.checkState(line.contains(":"), "Bad line: ", line);
				String regName = line.substring(0, line.indexOf(":")).trim();
				Preconditions.checkState(line.contains("a ="), "Bad line: ", line);
				line = line.substring(line.indexOf("a =")).replaceAll(" ", "");
				String[] split = line.split(",");
				Preconditions.checkState(split.length == 2, "expected 2");
				double a = Double.parseDouble(split[0].substring(split[0].indexOf("=")+1));
				double p = Double.parseDouble(split[1].substring(split[1].indexOf("=")+1));
				
				dataMap.put(regName, new double[] {a, p, c});
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
	public double[] get(Location loc) {
		String region = getRegion(loc);
		return get(region);
	}
	
	/**
	 * 
	 * @param region
	 * @return array of parameters: {a, p, c};
	 */
	public double[] get(String region) {
		return dataMap.get(region);
	}
	
	public String getRegion(Location loc) {
		try {
			return garciaFetch.getValue(loc);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
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
		
		DefaultOmoriParamFetch fetch = new DefaultOmoriParamFetch();
		Joiner j = Joiner.on(",");
		for (Location loc : locs) {
			System.out.println(loc+": "+j.join(Doubles.asList(fetch.get(loc))));
		}
	}

}
