package scratch.kevin.cybershake;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.jfree.data.Range;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.SiteTranslator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class NorthridgeCSComparison {

	public static void main(String[] args) throws IOException {
		CyberShakeComponent comp = CyberShakeComponent.GEOM_MEAN;
		int sourceID = 223;
		String sourceName = "Northridge";
		int csHistEstNum= 15;
		Range magRange = new Range(6.64, 6.76);
//		int datasetID = 57;
//		String prefix = "study_15_4";
//		File outputDir = new File("/home/kevin/CyberShake/MCER/northridge_comparisons");
//		String prefix = "study_13_4_cvmh";
//		int datasetID = 25;
//		File mainDir = new File("/home/kevin/CyberShake/MCER/northridge_comparisons");
		String prefix = "study_13_4_cvms";
		int datasetID = 19;
		File mainDir = new File("/home/kevin/CyberShake/MCER/northridge_comparisons");
		File outputDir = new File(mainDir, prefix);
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		double maxDist = 2d;
		
		List<Double> periods = Lists.newArrayList(3d, 5d);
		List<String> siteNames = Lists.newArrayList("P1", "P23", "SMCA", "CCP", "P18",
				"LADT", "USC", "COO", "s387", "s478");
		
		CSVFile<String> csv = CSVFile.readFile(new File(mainDir, "Northridge.csv"), true);
		List<Record> records = Lists.newArrayList();
		for (int row=1; row<csv.getNumRows(); row++) {
			int id = Integer.parseInt(csv.get(row, 0));
			String name = csv.get(row, 1);
			double lat = Double.parseDouble(csv.get(row, 12));
			double lon = Double.parseDouble(csv.get(row, 13));
			Location loc = new Location(lat, lon);
			double maxPeriod = Double.parseDouble(csv.get(row, 29));
			Record record = new Record(id, name, loc, maxPeriod);
			for (int col=33; col<csv.getNumCols(); col++) {
				String periodStr = csv.get(0, col).replaceAll("T", "").replace("S", "");
				double period = Double.parseDouble(periodStr);
				double val = Double.parseDouble(csv.get(row, col));
				record.set(period, val);
			}
			records.add(record);
		}
		
		DBAccess db = null;
		try {
			db = Cybershake_OpenSHA_DBApplication.db;
			ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
			List<Integer> rupIDs = Lists.newArrayList();
			ProbEqkSource source = erf.getSource(sourceID);
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				double mag = source.getRupture(rupID).getMag();
				if (magRange.contains(mag))
					rupIDs.add(rupID);
			}
			System.out.println("Ruptures: "+Joiner.on(",").join(rupIDs));
			
			List<ScalarIMR> gmpes = Lists.newArrayList();
			gmpes.add(AttenRelRef.ASK_2014.instance(null));
			gmpes.add(AttenRelRef.BSSA_2014.instance(null));
			gmpes.add(AttenRelRef.CB_2014.instance(null));
			gmpes.add(AttenRelRef.CY_2014.instance(null));
			
			PeakAmplitudesFromDB amps2db = new PeakAmplitudesFromDB(db);
			List<CybershakeIM> ims = amps2db.getIMs(periods, IMType.SA, comp);
			Runs2DB runs2db = new Runs2DB(db);
			
			RunIDFetcher fetch = new RunIDFetcher(db, datasetID, ims.get(0).getID());
			
			for (String csSiteName : siteNames) {
				int runID = fetch.getRunID(csSiteName);
				CybershakeSite csSite = fetch.getSite(csSiteName);
				System.out.println("CS Site: "+csSite+", run "+runID);
				Location loc = csSite.createLocation();
				
				// now find matching record
				Record record = null;
				double recordDist = Double.POSITIVE_INFINITY;
				for (Record r : records) {
					double dist = LocationUtils.horzDistanceFast(loc, r.loc);
					if (dist < recordDist) {
						record = r;
						recordDist = dist;
					}
				}
				if (recordDist > maxDist) {
					System.out.println("*** No matching record within "+maxDist+"km of "+csSiteName
							+" (closest is "+recordDist+" km)");
					continue;
				}
				System.out.println("Matching record "+recordDist+"km away: "+record.id+". "+record.name);
				
				int velModelID = runs2db.getRun(runID).getVelModelID();
				Site site = new Site(loc);
				for (ScalarIMR gmpe : gmpes) {
					gmpe.setParamDefaults();
					for (Parameter<?> param : gmpe.getSiteParams())
						if (!site.containsParameter(param))
							site.addParameter((Parameter<?>)param.clone());
					gmpe.setIntensityMeasure(SA_Param.NAME);
				}
				OrderedSiteDataProviderList provs = HazardCurvePlotter.createProviders(velModelID);
				List<SiteDataValue<?>> datas = provs.getAllAvailableData(site.getLocation());
				SiteTranslator trans = new SiteTranslator();
				for (Parameter<?> param : site)
					trans.setParameterValue(param, datas);
				
				for (int p=0; p<periods.size(); p++) {
					double period = periods.get(p);
					if (period > record.maxPeriod) {
						System.out.println("Can't do "+period+"s, above max period of "+record.maxPeriod+"s");
						continue;
					}
					String periodStr;
					if (period == (int)period)
						periodStr = (int)period+"";
					else
						periodStr = (float)period+"";
					File subDir = new File(outputDir, periodStr+"s");
					Preconditions.checkState(subDir.exists() || subDir.mkdir());
					
					CybershakeIM im = ims.get(p);
					
					List<Double> csAmps = Lists.newArrayList();
					for (int rupID : rupIDs)
						csAmps.addAll(amps2db.getIM_Values(runID, sourceID, rupID, im));
					System.out.println("Loaded "+csAmps.size()+" amps");
					
					ArbDiscrEmpiricalDistFunc csFunc = new ArbDiscrEmpiricalDistFunc();
					for (double val : csAmps)
						csFunc.set(val/HazardCurveComputation.CONVERSION_TO_G,1);
					
					double csHistDelta = (csFunc.getMaxX() - csFunc.getMinX()) / csHistEstNum;
					HistogramFunction csHist = HistogramFunction.getEncompassingHistogram(
							csFunc.getMinX(), csFunc.getMaxX(), csHistDelta);
					for (Point2D pt : csFunc)
						csHist.add(pt.getX(), pt.getY());
//					// convert to density
//					csHist.scale(1d/(csHistDelta*csHist.calcSumOfY_Vals()));
					csHist.normalizeBySumOfY_Vals();
					Range yRange = new Range(0, csHist.getMaxY()*1.2);
					
					XY_DataSet recordingXY = new DefaultXY_DataSet();
					double recording = record.get(period);
					recordingXY.set(recording, 0d);
					recordingXY.set(recording, yRange.getUpperBound());
					
					double csMean = logAverage(csAmps)/HazardCurveComputation.CONVERSION_TO_G;
					XY_DataSet csMeanXY = new DefaultXY_DataSet();
					csMeanXY.set(csMean, 0d);
					csMeanXY.set(csMean, yRange.getUpperBound());
					
					List<Double> gmpeVals = Lists.newArrayList();
					for (ScalarIMR gmpe : gmpes) {
						SA_Param.setPeriodInSA_Param(gmpe.getIntensityMeasure(), period);
						gmpe.setSite(site);
						for (int rupID : rupIDs) {
							ProbEqkRupture rup = source.getRupture(rupID);
							gmpe.setEqkRupture(rup);
							double mean = Math.exp(gmpe.getMean());
							gmpeVals.add(mean);
						}
					}
					double gmpeMean = logAverage(gmpeVals);
					XY_DataSet gmpeMeanXY = new DefaultXY_DataSet();
					gmpeMeanXY.set(gmpeMean, 0d);
					gmpeMeanXY.set(gmpeMean, yRange.getUpperBound());
					
					List<XY_DataSet> funcs = Lists.newArrayList();
					List<PlotCurveCharacterstics> chars = Lists.newArrayList();
					
					funcs.add(csHist);
					chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.DARK_GRAY));
					csHist.setName("CS Distribution");
					
					funcs.add(csMeanXY);
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));
					csMeanXY.setName("CS Log-Mean: "+(float)csMean);
					
					funcs.add(recordingXY);
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.GREEN.darker()));
					recordingXY.setName("Record "+record.id+": "+(float)recording);
					
					funcs.add(gmpeMeanXY);
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLUE));
					gmpeMeanXY.setName("GMPE Log-Mean: "+(float)gmpeMean);
					
					String title = sourceName+", "+csSiteName+"/"+record.name;
					
					PlotSpec spec = new PlotSpec(funcs, chars, title, periodStr+"s SA", "");
					spec.setLegendVisible(true);
					
					HeadlessGraphPanel gp = new HeadlessGraphPanel();
					gp.setTickLabelFontSize(18);
					gp.setAxisLabelFontSize(20);
					gp.setPlotLabelFontSize(21);
					gp.setBackgroundColor(Color.WHITE);
					
					gp.drawGraphPanel(spec, false, false, null, yRange);
					gp.getCartPanel().setSize(1000, 800);
					String fName = sourceName+"_"+csSiteName+"_"+record.id+"_"+(int)period+"s_"+comp.getShortName();
					gp.saveAsPNG(new File(subDir, fName+".png").getAbsolutePath());
					gp.saveAsPDF(new File(subDir, fName+".pdf").getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.destroy();
		}
	}
	
	private static double logAverage(List<Double> linearVals) {
		double[] vals = new double[linearVals.size()];
		for (int i=0; i<linearVals.size(); i++)
			vals[i] = Math.log(linearVals.get(i));
		return Math.exp(StatUtils.mean(vals));
	}
	
	private static class Record {
		final int id;
		final String name;
		final Location loc;
		final double maxPeriod;
		final ArbitrarilyDiscretizedFunc vals;
		
		public Record(int id, String name, Location loc, double maxPeriod) {
			super();
			this.id = id;
			this.name = name;
			this.loc = loc;
			this.maxPeriod = maxPeriod;
			
			vals = new ArbitrarilyDiscretizedFunc();
		}
		
		public void set(double period, double val) {
			vals.set(period, val);
		}
		
		public double get(double period) {
			return vals.getY(period);
		}
	}

}
