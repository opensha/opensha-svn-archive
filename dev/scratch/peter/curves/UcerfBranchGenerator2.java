package scratch.peter.curves;

import static org.opensha.sha.imr.AttenRelRef.*;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;
import org.opensha.sha.nshmp.Period;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * USed to generate hazard curves for UCERF2 Time Independent List
 * 
 * @author Peter Powers
 * @version $Id:$
 */
class UcerfBranchGenerator2 implements PropertyChangeListener {

	static final String OUT_DIR = "/Volumes/Scratch/rtgm/UCERF2_1p00_5-2-2012/";
	 private static AttenRelRef[] imrRefs = { NSHMP_2008 };
	private static Period period = Period.GM1P00;
	private List<Future<?>> futures;

	public static void main(String[] args) {
		UcerfBranchGenerator2 gen = new UcerfBranchGenerator2();
	}

	private UcerfBranchGenerator2() {
		try {
			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(numProc);
			futures = Lists.newArrayList();
			for (AttenRelRef imrRef : imrRefs) {
				for (NEHRP_TestCity loc : NEHRP_TestCity.getCA()) {
					ScalarIMR imr = newIMR(imrRef);
					EpistemicListERF erfs = newERF();
					Processor2 proc = new Processor2(imr, erfs, loc, period);
					futures.add(ex.submit(proc));
					
					// proc.addPropertyChangeListener(this);
					// proc.execute();
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
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);
		
		Parameter bgSrcParam = erf.getParameter(UCERF2.BACK_SEIS_RUP_NAME);
		bgSrcParam.setValue(UCERF2.BACK_SEIS_RUP_POINT);
		Parameter floatParam = erf.getParameter(UCERF2.FLOATER_TYPE_PARAM_NAME);
		floatParam.setValue(UCERF2.FULL_DDW_FLOATER);
		
		// updateForecast called by EpistemicListERF
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

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		System.out.println(pce.getPropertyName() + ": " + pce.getNewValue());
		Object obj = pce.getNewValue();
		// if ("state".equals(pce.getPropertyName())) {
		// Processor p = (Processor) pce.getSource();
		// System.out.println(pce.getNewValue() + ": " + p.imr.getShortName() +
		// " " + p.loc);
		// }
	}

}
