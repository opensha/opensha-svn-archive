package scratch.peter.curves;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.nshmp.Period;

import com.google.common.collect.Lists;

/**
 * USed to generate hazard curves for UCERF2 Time Independent List
 * 
 * @author Peter Powers
 * @version $Id:$
 */
class RTGM_BranchGenerator {

	private static final String OUT_DIR = "/Volumes/Scratch/rtgm/UCERF2";
	private static AttenRelRef[] imrRefs = { AttenRelRef.NSHMP_2008 };
	private static Period period = Period.GM0P20;
	private List<Future<?>> futures;

	public static void main(String[] args) {
		new RTGM_BranchGenerator();
	}

	private RTGM_BranchGenerator() {
		try {
			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(numProc);
			futures = Lists.newArrayList();
			for (AttenRelRef imrRef : imrRefs) {
				for (NEHRP_TestCity loc : EnumSet.of(NEHRP_TestCity.LOS_ANGELES)) {
					ScalarIMR imr = newIMR(imrRef);
					EpistemicListERF erfs = newERF();
					RTGM_Processor proc = new RTGM_Processor(imr, erfs, loc, period, OUT_DIR);
					futures.add(ex.submit(proc));
				}
			}
			ex.shutdown();
			ex.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	static EpistemicListERF newERF() {
		AbstractEpistemicListERF erf = new UCERF2_TimeIndependentEpistemicList();
		
		Parameter bgSrcParam = erf.getParameter(UCERF2.BACK_SEIS_RUP_NAME);
		bgSrcParam.setValue(UCERF2.BACK_SEIS_RUP_POINT);
		Parameter floatParam = erf.getParameter(UCERF2.FLOATER_TYPE_PARAM_NAME);
		floatParam.setValue(UCERF2.FULL_DDW_FLOATER);
//		Parameter probParam = erf.getParameter(UCERF2.PROB_MODEL_PARAM_NAME);
//		probParam.setValue(UCERF2.PROB_MODEL_POISSON);
		
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);		
		return erf;
	}

	static ScalarIMR newIMR(AttenRelRef imrRef) {
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