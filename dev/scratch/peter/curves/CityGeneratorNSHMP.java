package scratch.peter.curves;

import static org.opensha.nshmp2.util.Period.*;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.calc.UC3_CalcDriver;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.SourceIMR;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;

import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;
import scratch.UCERF3.utils.UpdatedUCERF2.GridSources;
import scratch.UCERF3.utils.UpdatedUCERF2.MeanUCERF2update;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Utility class to generate hazard curves for NEHRP test cities using NSHMP
 * List ERFs.
 */
class CityGeneratorNSHMP {

//	private static final String OUT_DIR = "/Volumes/Scratch/rtgm/NSHMP_CA_SHA-epi";
	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/NSHMP_CA_SHA";
	private static Period[] periods = { GM0P00, GM0P20, GM1P00 };
	private static Map<String, Location> locMap;
	private static String sitePath;
	private static boolean epi = false;
	
	static {
		// sitePath = "tmp/curves/sites/NEHRPsites.txt";
		// sitePath = "tmp/curves/sites/PBRsites.txt";
		// sitePath = "tmp/curves/sites/SRPsites.txt";
		sitePath = "tmp/curves/sites/all.txt";
		try {
			locMap = UC3_CalcDriver.readSiteFile(sitePath);
			System.out.println(locMap);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new CityGeneratorNSHMP();
	}

	private CityGeneratorNSHMP() {
		try {
			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(periods.length);
			System.out.println("NumProc: " + numProc);
			NSHMP2008 erf = newERF();
//			erf.updateForecast();
			System.out.println(erf);
			for (Period period : periods) {
//				ScalarIMR imr = newIMR(period);
				CityProcessorNSHMP proc = new CityProcessorNSHMP(erf, locMap,
					period, epi, OUT_DIR);
				ex.submit(proc);
//				proc.run();
			}
			ex.shutdown();
			ex.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	static NSHMP2008 newERF() {
		NSHMP2008 erf = NSHMP2008.createCalifornia();
//		MeanUCERF2 erf = new MeanUCERF2();
//		MeanUCERF2 erf = new MeanUCERF2update(GridSources.ALL);
//		ModMeanUCERF2 erf = new ModMeanUCERF2();
		
//		Parameter bgSrcParam = erf.getParameter(UCERF2.BACK_SEIS_RUP_NAME);
//		bgSrcParam.setValue(UCERF2.BACK_SEIS_RUP_POINT);
//		Parameter floatParam = erf.getParameter(UCERF2.FLOATER_TYPE_PARAM_NAME);
//		floatParam.setValue(UCERF2.FULL_DDW_FLOATER);
//		Parameter probParam = erf.getParameter(UCERF2.PROB_MODEL_PARAM_NAME);
//		probParam.setValue(UCERF2.PROB_MODEL_POISSON);
		
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);
		
		erf.updateForecast();
		
		return erf;
	}

//	static ScalarIMR newIMR(Period period) {
//		ScalarIMR imr = SourceIMR.WUS_FAULT.instance(period); //new NSHMP08_WUS();
//		imr.getParameter(NSHMP08_WUS.IMR_UNCERT_PARAM_NAME).setValue(false);
//		return imr;
//	}

//	static ScalarIMR newIMR(AttenRelRef imrRef, Period period) {
//		ScalarIMR imr = imrRef.instance(null); 
//		imr.setParamDefaults();
//		if (period == Period.GM0P00) {
//			imr.setIntensityMeasure("PGA");
//		} else {
//			imr.setIntensityMeasure("SA");
//			imr.getParameter(PeriodParam.NAME).setValue(period.getValue());
//		}
//		return imr;
//	}


}
