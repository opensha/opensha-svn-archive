package scratch.peter.curves;

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
import org.opensha.sha.nshmp.Period;
import org.opensha.sha.util.TectonicRegionType;

public class UcerfBranchGenerator {
	
	static ScalarIMR[] imrs = {
		AttenRelRef.CB_2008.instance(null),
		AttenRelRef.BA_2008.instance(null),
		AttenRelRef.CY_2008.instance(null),
		AttenRelRef.AS_2008.instance(null)};
	
	static Period period = Period.GM0P00;

	static AbstractEpistemicListERF erfList = new UCERF2_TimeDependentEpistemicList();
	
	
	public static void main(String[] args) {
		for (ScalarIMR imr : imrs) {

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

//	String outDirName = "sha_kml/";
//	File outDir = new File(outDirName);
//	outDir.mkdirs();
//	String tmpFile = outDirName + kmlFileName;
//	
//	try {
//		//XMLUtils.writeDocumentToFile(tmpFile, doc);
//		XMLWriter writer;
//		OutputFormat format = new OutputFormat("\t", true);
//		writer = new XMLWriter(new FileWriter(tmpFile), format);
//		writer.write(doc);
//		writer.close();
//	} catch (IOException ioe) {
//		ioe.printStackTrace();
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
	}

}
