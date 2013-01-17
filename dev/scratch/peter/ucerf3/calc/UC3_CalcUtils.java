package scratch.peter.ucerf3.calc;

import static com.google.common.base.Charsets.US_ASCII;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_CalcUtils {

	private static final Splitter SPLIT = Splitter.on(',');

	/**
	 * Returns an inversion based ERF for the supplied fault system solution.
	 * Assumes the supplied FSS is an inversion solution.
	 * 
	 * @param faultSysSolZipFile
	 * 
	 * @return
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(FaultSystemSolution fss) {
		InversionFaultSystemSolution invFss = new InversionFaultSystemSolution(
			fss);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(
			IncludeBackgroundOption.EXCLUDE);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME)
			.setValue(true);
		erf.getTimeSpan().setDuration(1d);
		return erf;
	}

	/**
	 * Returns an average fault system solution at the specified path.
	 * @param path
	 * @return an AFSS
	 */
	public static AverageFaultSystemSolution getAvgSolution(String path) {
		try {
			File file = new File(path);
			return AverageFaultSystemSolution.fromZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a compound fault system solution at the specified path.
	 * @param path
	 * @return a CFSS
	 */
	public static CompoundFaultSystemSolution getCompoundSolution(String path) {
		try {
			File cfssFile = new File(path);
			return CompoundFaultSystemSolution.fromZipFile(cfssFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reads a text file with lines of the format:
	 * 		SITE_NAME,34.0,-117.0
	 * 
	 * @param path
	 * @return a {@code Map} of site names and their locations
	 * @throws IOException
	 */
	public static Map<String, Location> readSiteFile(String path) throws IOException {
		File f = new File(path);
		List<String> lines = Files.readLines(f, US_ASCII);
		Map<String, Location> siteMap = Maps.newHashMap();
		for (String line : lines) {
			if (line.startsWith("#")) continue;
			Iterator<String> it = SPLIT.split(line).iterator();
			String name = it.next();
			double lat = Double.parseDouble(it.next());
			double lon = Double.parseDouble(it.next());
			siteMap.put(name, new Location(lat, lon));
		}
		return ImmutableMap.copyOf(siteMap);
	}


}
