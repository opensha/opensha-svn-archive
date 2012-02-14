package scratch.peter.curves;

import static org.opensha.sha.imr.AttenRelRef.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
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
import org.opensha.sha.nshmp.Period;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class UcerfBranchGenerator {

	private static final String OUT_DIR = "tmp/curve_gen/";
	private static AttenRelRef[] imrRefs = { CB_2008, BA_2008, CY_2008, AS_2008 };
	private static Period period = Period.GM0P00;

	public static void main(String[] args) {
		for (AttenRelRef imrRef : imrRefs) {
			for (TestLoc loc : TestLoc.values()) {
				ScalarIMR imr = newIMR(imrRef);
				EpistemicListERF erfs = newERF();
				Site site = loc.getSite();
				Processor proc = new Processor(imr, erfs, site, period);
				proc.doCalc();
			}
		}
	}

	private static EpistemicListERF newERF() {
		AbstractEpistemicListERF erf = new UCERF2_TimeDependentEpistemicList();
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);
		erf.updateForecast();
		return erf;
	}

	private static ScalarIMR newIMR(AttenRelRef imrRef) {
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

	static class Processor {

		private ScalarIMR imr;
		private EpistemicListERF erfs;
		private Site site;
		private Period per;
		
		private int paramCount;
		private HashMap<String, Integer> paramMap;
		private List<List<String>> paramData;
		private List<List<String>> curveData;

		Processor(ScalarIMR imr, EpistemicListERF erfs, Site site, Period per) {
			this.imr = imr;
			this.erfs = erfs;
			this.site = site;
			this.per = per;
		}

		public void doCalc() {
			init();
			HazardCurveCalculator calc = new HazardCurveCalculator();
			XY_DataSetList hazardFuncList = new XY_DataSetList();
			System.out.println("Doing calcs ....");
			for (int i = 0; i < erfs.getNumERFs(); ++i) {
				DiscretizedFunc f = per.getFunction();
				ERF erf = erfs.getERF(i);
				f = calc.getHazardCurve(f, site, imr, erf);
				hazardFuncList.add(f);
				System.out.println("ERF#: " + i);
				addResults(i, erf, f);
			}
		}
		
		private void addResults(int idx, ERF erf, DiscretizedFunc f) {
			// param data
			List<String> paramDat = Lists.newArrayList();
			paramDat.add(Integer.toString(idx));
			for (int i=0; i<paramCount; i++) paramDat.add(null);
			for (Parameter<?> param : erf.getAdjustableParameterList()) {
				System.out.println(paramMap);
				System.out.println(paramMap.get(param.getName()));
				System.out.println(param.getName());
				int index = paramMap.get(param.getName())+1;
				paramDat.set(index, param.getValue().toString());
			}
			// curve data
			List<String> curveDat = Lists.newArrayList();
			curveDat.add(Integer.toString(idx));
			for (Point2D p : f) {
				curveDat.add(Double.toString(p.getY()));
			}
		}
		
		/*
		 * Initialize output lists, one for param values and another for curves
		 */
		private void init() {
			System.out.println("Initializing....");
			paramMap = Maps.newHashMap();
			paramData = Lists.newArrayList();
			List<String> paramHeader = Lists.newArrayList();
			paramHeader.add("ERF#");
			for (int i = 0; i < erfs.getNumERFs(); i++) {
				ERF erf = erfs.getERF(i);
				System.out.println("huh: " + i);
				for (Parameter<?> param : erf.getAdjustableParameterList()) {
					if (!paramMap.containsKey(param.getName())) {
						paramMap.put(param.getName(), paramMap.size());
						paramHeader.add(param.getName());
					}
				}
			}
			paramCount = paramHeader.size() - 1;
			paramData.add(paramHeader);
			curveData = Lists.newArrayList();
			List<String> curveHeader = Lists.newArrayList();
			curveHeader.add("ERF#");
			for (Double d : per.getIMLs()) {
				curveHeader.add(d.toString());
			}
			curveData.add(curveHeader);
		}
		
		private void writeFiles(String city, String imr, List<List<String>> content) {
			String outDirName = OUT_DIR + city + "/";
			File outDir = new File(outDirName);
			outDir.mkdirs();
			String paramFile = outDirName +  imr + "_params.csv";
			String curveFile = outDirName +  imr + "_curves.csv";
			toCSV(paramFile, paramData);
			toCSV(curveFile, curveData);
		}
		
		private static void toCSV(String file, List<List<String>> content) {
			Joiner joiner = Joiner.on(',');
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(file, true));
				for (List<String> lineDat : content) {
					String line = joiner.join(lineDat);
					pw.println(line);
				}
				pw.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
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
