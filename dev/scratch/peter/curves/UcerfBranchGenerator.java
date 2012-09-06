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
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This was used to generate hazard curves for the sites in the TestLoc enum for
 * Frank Cherbaum and Nicolas Kuehn <Nicolas.Kuehn@geo.uni-potsdam.de>
 *
 * @author Peter Powers
 * @version $Id:$
 */
class UcerfBranchGenerator implements PropertyChangeListener {

	static final String OUT_DIR = "/Volumes/Scratch/scherbaum/PGA_03-09-2012/";
	 private static AttenRelRef[] imrRefs = {CY_2008, AS_2008, CB_2008, BA_2008 };
//	private static AttenRelRef[] imrRefs = { BA_2008 };
	private static Period period = Period.GM0P00;
	private List<Future<?>> futures;

	public static void main(String[] args) {
		UcerfBranchGenerator gen = new UcerfBranchGenerator();
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		//
		// }
		// });
	}

	private UcerfBranchGenerator() {
		try {
			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(numProc);
			futures = Lists.newArrayList();
			for (AttenRelRef imrRef : imrRefs) {
				for (TestLoc loc : TestLoc.values()) {
					ScalarIMR imr = newIMR(imrRef);
					EpistemicListERF erfs = newERF();
//					Processor proc = new Processor(imr, erfs, loc, period);
//					futures.add(ex.submit(proc));
					
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
		AbstractEpistemicListERF erf = new UCERF2_TimeDependentEpistemicList();
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);
		// erf.updateForecast();
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

	enum TestLoc {
		 HOLLISTER_CITY_HALL(-121.402, 36.851),
		 INDIO_RV_SHOWCASE(-116.215, 33.747),
		 CALEXICO_FIRE_STATION(-115.493, 32.6695),
		 SAN_LUIS_OBISPO_REC(-120.661, 35.285),
		 ANDERSON_SPRINGS(-122.706, 38.7742),
		 COBB(-122.753, 38.8387);

		private Location loc;

		private TestLoc(double lon, double lat) {
			loc = new Location(lat, lon);
		}

		public Location getLocation() {
			return loc;
		}

		public Site getSite() {
			Site s = new Site(loc, this.name());
			// CY AS
			DepthTo1pt0kmPerSecParam d10p = new DepthTo1pt0kmPerSecParam(null,
				0, 1000, true);
			d10p.setValueAsDefault();
			s.addParameter(d10p);
			// CB
			DepthTo2pt5kmPerSecParam d25p = new DepthTo2pt5kmPerSecParam(null,
				0, 1000, true);
			d25p.setValueAsDefault();
			s.addParameter(d25p);
			// all
			Vs30_Param vs30p = new Vs30_Param(760);
			vs30p.setValueAsDefault();
			s.addParameter(vs30p);
			// AS CY
			Vs30_TypeParam vs30tp = new Vs30_TypeParam();
			s.addParameter(vs30tp);
			return s;
		}
	}

}
