package org.opensha.sha.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.opensha.sha.imr.attenRelImpl.NSHMP_2008_CA;

/**
 * NSHMP Utilities. These are primarily used by NSHMP specific attenuation
 * relationships, e.g.:
 * <ul>
 * <li>{@link NSHMP_2008_CA}
 * </ul>
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_Util {

	// internally distance and mag values are scaled up to integers to
	// eliminate decimal precision errors
	private static Map<Integer, Map<Integer, Double>> data;
	private static String rjbDatPath = "/resources/data/nshmp/rjbmean.dat";

	static {
		data = new HashMap<Integer, Map<Integer, Double>>();
		String magID = "#Mag";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
				NSHMP_Util.class.getResourceAsStream(rjbDatPath)));
			String line;
			HashMap<Integer, Double> magMap = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(magID)) {
					double mag = Double.parseDouble(line.substring(
						magID.length() + 1).trim());
					int magKey = new Double(mag * 100).intValue();
					magMap = new HashMap<Integer, Double>();
					data.put(magKey, magMap);
					continue;
				}
				if (line.startsWith("#")) continue;
				String[] dVal = StringUtils.split(line);
				if (dVal.length == 0) continue;
				int distKey = new Double(dVal[0]).intValue();
				magMap.put(distKey, Double.parseDouble(dVal[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a corrected distance value corresponding to the supplied JB
	 * distance and magnitude. Magnitude is expected to be a 0.05 centered value
	 * between 6 and 7.6 (e.g [6.05, 6.15, ... 7.55]). Distance values should be
	 * &le;200km. If <code>D</code> > 200km, method returns D.
	 * 
	 * @param M magnitude
	 * @param D distance
	 * @return the corrected distance or <code>D</code> if <code>D</code> > 200
	 * @throws IllegalArgumentException if <code>M</code> is not one of [6.05,
	 *         6.15, ... 7.55]
	 */
	public static double getMeanRJB(double M, double D) {
		int magKey = new Double(M * 100).intValue();
		checkArgument(data.containsKey(magKey), "Invalid mag value: " + M);
		int distKey = (int) Math.floor(D);
		return (D <= 200) ? data.get(magKey).get(distKey) : D;
	}
	
}
