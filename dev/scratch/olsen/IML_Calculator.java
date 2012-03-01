package scratch.olsen;

import static org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2.*;
import static org.opensha.sha.imr.param.OtherParams.StdDevTypeParam.*;
import static org.opensha.sha.imr.AttenRelRef.*;
import static org.opensha.sha.nshmp.Period.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.Site;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;
import org.opensha.sha.nshmp.Period;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Utility class to calculate IMLs at specific Sites for specific scenario
 * ruptures. This can be made multithreaded if performance becomes an issue.
 */
public class IML_Calculator {
	
	private static final String OUT_DIR = "tmp/iml_gen/";
	private static final Joiner J = Joiner.on(", ").useForNull("");
	
	// this can also be BA_2008, AS_2008, or CY_2008
	private static final AttenRelRef IMR_REF = CY_2008;
	private static final Period PERIOD = GM1P00;
	private static final int NUM_EVENT_SETS = 2;
	
	private static List<Location> locs;
	private static List<Long> ids;
	
	static {
		Map<Long, Location> locsMap = readLocations();
		locs = Lists.newArrayList(locsMap.values());
		ids = Lists.newArrayList(locsMap.keySet());
	}

	public IML_Calculator() {
		System.out.println("Initializing ERF...");
		ERF erf = newERF();
		ScalarIMR imr = newIMR(IMR_REF, PERIOD);
		
		// =====  !! Currently shortening site list !! =======
//		SiteSupplier sites = new SiteSupplier(locs);
		SiteSupplier sites = new SiteSupplier(locs.subList(0, 100));
				
		System.out.println("Processing...");
		for (int i=0; i<NUM_EVENT_SETS; i++) {
			System.out.println("  EventSet " + i);
			List<EqkRupture> rups = erf.drawRandomEventSet();
			String outDir = OUT_DIR + "ES_" + (i+1) + "/";
			processEventSet(rups, imr, sites, outDir);
		}
		
	}
	
	/*
	 * Processes an individual event set.
	 */
	private void processEventSet(List<EqkRupture> rups, ScalarIMR imr,
			Iterable<Site> sites, String dir) {
		List<String> results = Lists.newArrayList();
		int i = 1; // rupture counter
		for (EqkRupture rup : rups) {
			results.add("#" + i + " M=" + rup.getMag());
			imr.setEqkRupture(rup);
			int j = 0; // site counter
			for (Site site : sites) {
				imr.setSite(site);
				double[] vals = getValues(imr);
				Location loc = site.getLocation();
				double rjb = rup.getRuptureSurface().getDistanceJB(loc);
				results.add(buildResult(ids.get(j), loc, vals, rjb));
				j++;
			}
			writeResult(dir, "Event_" + i + ".dat", results);
			results.clear();
			i++;
		}
	}
	
	private String buildResult(long id, Location loc, double[] data, double dist) {
		return J.join(id, loc.getLongitude(), loc.getLatitude(), data[0],
			data[1], data[2], dist);
	}	
	
	/*
	 * Compute the mean ground motion and stadard deviations.
	 */
	private double[] getValues(ScalarIMR imr) {
		double[] vals = new double[3];
		// mean
		vals[0] = imr.getMean();
		// inter event
		imr.getParameter(StdDevTypeParam.NAME).setValue(STD_DEV_TYPE_INTER);
		vals[1] = imr.getStdDev();
		// intra event
		imr.getParameter(StdDevTypeParam.NAME).setValue(STD_DEV_TYPE_INTRA);
		vals[2] = imr.getStdDev();
		return vals;
	}
	
	
	/*
	 * Write result set to file
	 */
	private void writeResult(String dir, String file, List<String> content) {
		new File(dir).mkdirs();
		File f = new File(dir + file);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(f, false));
			for (String line : content) {
				pw.println(line);
			}
			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}	
	
	/*
	 * Instantiate and configure a new UCERF2 forecast. A few default options
	 * are indicated that could be overridden if desired. For example, a 30-year
	 * forecast nets anywhere from 8-20 ruptures on average. Increasing the
	 * forecast duration to 500 years will increase the event set to 200+.
	 */
	private ERF newERF() {
		ERF erf = new MeanUCERF2();
		erf.setParameter(PROB_MODEL_PARAM_NAME, PROB_MODEL_POISSON);
		erf.setParameter(BACK_SEIS_NAME, BACK_SEIS_EXCLUDE);
//		default is 1.0 km
//		erf.setParameter(RUP_OFFSET_PARAM_NAME, 5.0);
//		default is 30 years
//		erf.getTimeSpan().setDuration(500);
		erf.updateForecast();
		
		// alternative erf 
//		UCERF2_TimeDependentEpistemicList erf = new UCERF2_TimeDependentEpistemicList();
//		erf.getERF(0);
//		erf.updateForecast();
		
		return erf;
	}
	
	
	/*
	 * Instantiate and configure a new GMPE/IMR
	 */
	private ScalarIMR newIMR(AttenRelRef imrRef, Period per) {
		ScalarIMR imr = imrRef.instance(null);
		imr.setParamDefaults();
		if (per == Period.GM0P00) {
			imr.setIntensityMeasure("PGA");
		} else {
			imr.setIntensityMeasure("SA");
			imr.getParameter(PeriodParam.NAME).setValue(per.getValue());
		}
		return imr;
	}

	
	/*
	 * Reads locations and object IDs; called on startup
	 */
	private static Map<Long, Location> readLocations() {
		Map<Long, Location> locMap = Maps.newTreeMap();
		try {
			URL url = IML_Calculator.class.getResource("hzTract.csv");
			File f = new File(url.toURI());
			List<String> lines = Files.readLines(f, Charsets.US_ASCII);
			lines.remove(0);
			for (String line : lines) {
				String[] ss =  StringUtils.splitPreserveAllTokens(line, ',');
				
				locMap.put(Long.valueOf(ss[2]),
					new Location(Double.valueOf(ss[8]), Double.valueOf(ss[9])));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return locMap;
	}
	
	
	/*
	 * Nested class that is iterable over Sites corresponding to the locations
	 * of interest. 
	 */
	private static class SiteSupplier implements Iterable<Site> {
		
		private List<Location> locs;
		private Site site;
		
		SiteSupplier(List<Location> locs) {
			this.locs = locs;
			initSite();
		}
		
		@Override
		public Iterator<Site> iterator() {
			return new Iterator<Site>() {
				int caret = 0;
				@Override 
				public boolean hasNext() {
					return caret < locs.size();
				}
				@Override 
				public Site next() {
					site.setLocation(locs.get(caret++));
					return site;
				}
				@Override 
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		private void initSite() {
			site = new Site();
			// CY AS
			DepthTo1pt0kmPerSecParam d10p = new DepthTo1pt0kmPerSecParam(null,
				0, 1000, true);
			d10p.setValueAsDefault();
			site.addParameter(d10p);
			// CB
			DepthTo2pt5kmPerSecParam d25p = new DepthTo2pt5kmPerSecParam(null,
				0, 1000, true);
			d25p.setValueAsDefault();
			site.addParameter(d25p);
			// all
			Vs30_Param vs30param = new Vs30_Param(760);
			vs30param.setValueAsDefault();
			site.addParameter(vs30param);
			// AS CY
			Vs30_TypeParam vs30tp = new Vs30_TypeParam();
			site.addParameter(vs30tp);
		}
	}
	
	
	/*
	 * Run me!
	 */
	public static void main(String[] args) {		
		IML_Calculator imc = new IML_Calculator();
		
	}
	

}
