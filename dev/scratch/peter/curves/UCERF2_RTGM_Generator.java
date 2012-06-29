package scratch.peter.curves;

import static org.opensha.nshmp.NEHRP_TestCity.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp.NEHRP_TestCity;
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
import org.opensha.sha.nshmp.Period;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * USed to generate hazard curves for UCERF2 Time Independent List
 * 
 * @author Peter Powers
 * @version $Id:$
 */
class UCERF2_RTGM_Generator {

	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3-UC2map";
	private static AttenRelRef[] imrRefs = { AttenRelRef.NSHMP_2008 };
//	private static Period[] periods = { Period.GM0P20, Period.GM1P00 };
	private static Period[] periods = { Period.GM0P20};
	private static Collection<NEHRP_TestCity> cities;
	
	private List<Future<?>> futures;

	static {
//		cities = NEHRP_TestCity.getCA();
//		cities = EnumSet.of(LOS_ANGELES, SAN_FRANCISCO, SACRAMENTO);
		cities = EnumSet.of(NEHRP_TestCity.LOS_ANGELES);
	}
	
	public static void main(String[] args) {
		new UCERF2_RTGM_Generator();
	}

	private UCERF2_RTGM_Generator() {
		try {
			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(numProc);
			System.out.println("NumProc: " + numProc);
			futures = Lists.newArrayList();
			for (Period period : periods) {
				for (AttenRelRef imrRef : imrRefs) {
					ScalarIMR imr = newIMR(imrRef, period);
					ERF erfFlt = newERFflt();
					ERF erfBg = newERFbg();
					UCERF2_RTGM_Processor proc = new UCERF2_RTGM_Processor(imr,
						erfFlt, erfBg, cities, period, OUT_DIR);
					futures.add(ex.submit(proc));
				}
			}
			ex.shutdown();
			ex.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	static ERF newERFflt() {
		FaultSystemSolutionPoissonERF erf = new FaultSystemSolutionPoissonERF(
			"/Users/pmpowers/projects/OpenSHA/tmp/invSols/ucerf2map/ALLCAL_UCERF2_rakesfixed.zip");
		
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);
		
		erf.updateForecast();
		
		return erf;
	}
	
	static ERF newERFbg() {
		MeanUCERF2 erf = new MeanUCERF2();
		
		Parameter bgInclude = erf.getParameter(UCERF2.BACK_SEIS_NAME);
		bgInclude.setValue(UCERF2.BACK_SEIS_ONLY);
		Parameter bgSrcParam = erf.getParameter(UCERF2.BACK_SEIS_RUP_NAME);
		bgSrcParam.setValue(UCERF2.BACK_SEIS_RUP_POINT);
		Parameter floatParam = erf.getParameter(UCERF2.FLOATER_TYPE_PARAM_NAME);
		floatParam.setValue(UCERF2.FULL_DDW_FLOATER);
		Parameter probParam = erf.getParameter(UCERF2.PROB_MODEL_PARAM_NAME);
		probParam.setValue(UCERF2.PROB_MODEL_POISSON);
		
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);
		
		erf.updateForecast();
		
		return erf;
		
	}

	static ScalarIMR newIMR(AttenRelRef imrRef, Period period) {
		ScalarIMR imr = imrRef.instance(null); 
		imr.setParamDefaults();
		if (period == Period.GM0P00) {
			imr.setIntensityMeasure("PGA");
		} else {
			imr.setIntensityMeasure("SA");
			imr.getParameter(PeriodParam.NAME).setValue(period.getValue());
		}
		return imr;
	}


}
