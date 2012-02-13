package scratch.peter.curves;

import static org.opensha.sha.imr.AttenRelRef.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.geo.Location;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.nshmp.Period;
import org.opensha.sha.util.TectonicRegionType;

import com.google.common.base.Joiner;

public class UcerfBranchGenerator {

	static final String OUT_DIR = "tmp/curve_gen/";
	
	static AttenRelRef[] imrRefs = { CB_2008, BA_2008, CY_2008, AS_2008 };

	static Period period = Period.GM0P00;

	static AbstractEpistemicListERF erfList = new UCERF2_TimeDependentEpistemicList();
	
	
	public static void main(String[] args) {
		for (AttenRelRef imrRef : imrRefs) {

			// init depth params; these should probably have null values
			// from the get go in the relevant imrs
//			imr.setParamDefaults();
//			if (imr.getSiteParams().containsParameter(DepthTo1pt0kmPerSecParam.NAME))
//				imr.getSiteParams().setValue(DepthTo1pt0kmPerSecParam.NAME, null);
//			if (imr.getSiteParams().containsParameter(DepthTo2pt5kmPerSecParam.NAME))
//				imr.getSiteParams().setValue(DepthTo2pt5kmPerSecParam.NAME, null);

		}
	}
	
	
	
	
	static HazardCurveCalculator calc = new HazardCurveCalculator();
	
	protected void handleForecastList(Site site,
			Map<TectonicRegionType, ScalarIMR> imrMap,
			AbstractEpistemicListERF erfList) {

		XY_DataSetList hazardFuncList = new XY_DataSetList();
		for (int i = 0; i < erfList.getNumERFs(); ++i) {
			DiscretizedFunc hazFunction = period.getFunction();
			hazFunction = calc.getHazardCurve(hazFunction, site, imrMap, erfList.getERF(i));
			hazardFuncList.add(hazFunction);
		}
	}

	// shared; be sure that passed in funcs are unique
	private static void writeFile(String city, String imr, String file, String content) {
		String outDirName = OUT_DIR + city + "/" + imr + "/";
		File outDir = new File(outDirName);
		outDir.mkdirs();
		String tmpFile = outDirName + file;
		try {
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(content);
			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private static AbstractEpistemicListERF newERF() {
		AbstractEpistemicListERF erf = new UCERF2_TimeDependentEpistemicList();
		erf.updateForecast();
		return erf;
	}
	
//	private static ScalarIMR newIMR(AttenRelRef imrRef) {
//		
//	}
	
	enum TestLoc {
		HOLLISTER_CITY_HALL(-121.402, 36.851),
		INDIO_RV_SHOWCASE(-116.215,33.747),
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
			Site s = new Site(loc);
			//s.addParameter(param)
			return null;
		}
	}

}
