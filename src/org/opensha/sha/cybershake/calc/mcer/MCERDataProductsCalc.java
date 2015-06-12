package org.opensha.sha.cybershake.calc.mcer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.Range;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.cybershake.plot.PlotType;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.MultiIMR_Averaged_AttenRel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class MCERDataProductsCalc {
	
	private static final String ASCE_REL_PATH = "data/ASCE7-10_Sms_Sm1_TL_det LL for 14 sites.xls";
	public static File cacheDir;
	static {
		File dir = new File("/home/kevin/CyberShake/MCER/.amps_cache");
		if (dir.exists())
			cacheDir = dir;
	}
	
	private CyberShakeComponent comp;
	private List<Double> periods;
	
	private ERF erf;
	private List<AttenuationRelationship> gmpes;
	
	private File outputDir;
	
	private List<CybershakeIM> ims;
	
	private DBAccess db;
	private Runs2DB runs2db;
	private CybershakeSiteInfo2DB sites2db;
	private CachedPeakAmplitudesFromDB amps2db;
	
//	private HSSFSheet asceSheet;
//	private FormulaEvaluator evaluator;
	
	private CyberShakeDeterministicCalc csDetCalc;
	
	private static final double det_percentile = 84;
	private static final double cs_det_mag_range = 0.11;
	
	private static final String default_periods = "1,1.5,2,3,4,5,7.5,10";
	
	public MCERDataProductsCalc(ERF erf, List<AttenuationRelationship> gmpes,
			CyberShakeComponent comp, List<Double> periods, File outputDir) throws IOException {
		init(db = Cybershake_OpenSHA_DBApplication.db, erf, gmpes, comp, periods, outputDir);
	}
	
	public MCERDataProductsCalc(CommandLine cmd) throws IOException, DocumentException, InvocationTargetException {
		ERF erf = ERFSaver.LOAD_ERF_FROM_FILE(cmd.getOptionValue("erf-file"));
		List<AttenuationRelationship> attenRels = Lists.newArrayList();
		
		for (String attenRelFile : HazardCurvePlotter.commaSplit(cmd.getOptionValue("atten-rel-file"))) {
			AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
			attenRels.add(attenRel);
		}
		
		erf.updateForecast();
		
		CyberShakeComponent comp = CybershakeIM.fromShortName(cmd.getOptionValue("component"), CyberShakeComponent.class);
		
		String periodStrs;
		if (cmd.hasOption("period"))
			periodStrs = cmd.getOptionValue("period");
		else
			periodStrs = default_periods;
		List<Double> periods = HazardCurvePlotter.commaDoubleSplit(periodStrs);
		
		File outputDir = new File(cmd.getOptionValue("output-dir"));
		Preconditions.checkArgument((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir(),
				"Output dir does not exist and could not be created");
		
		init(Cybershake_OpenSHA_DBApplication.db, erf, attenRels, comp, periods, outputDir);
	}
	
	private void init(DBAccess db, ERF erf, List<AttenuationRelationship> gmpes,
			CyberShakeComponent comp, List<Double> periods, File outputDir) throws IOException {
		this.db = db;
		this.comp = comp;
		this.periods = periods;
		this.erf = erf;
		this.gmpes = gmpes;
		this.outputDir = outputDir;
		
		runs2db = new Runs2DB(db);
		sites2db = new CybershakeSiteInfo2DB(db);
		amps2db = new CachedPeakAmplitudesFromDB(db, cacheDir, erf);
		
		csDetCalc = new CyberShakeDeterministicCalc(amps2db, erf, det_percentile, cs_det_mag_range);
		
		// load IMs
		ims = amps2db.getIMs(periods, IMType.SA, comp);
		
//		// load ASCE table
//		HSSFWorkbook wb;
//		try {
//			POIFSFileSystem fs = new POIFSFileSystem(
//					MCERDataProductsCalc.class.getResourceAsStream(ASCE_REL_PATH));
//			wb = new HSSFWorkbook(fs);
//		} catch (Exception e1) {
//			System.err.println("Couldn't load input file. Make sure it's an xls file and NOT an xlsx file.");
//			throw ExceptionUtils.asRuntimeException(e1);
//		}
//		asceSheet = wb.getSheetAt(0);
//		evaluator = wb.getCreationHelper().createFormulaEvaluator();
	}
	
	public void calc(int runID) throws IOException {
		calc(Lists.newArrayList(runID));
	}
	
	public void calc(List<Integer> runIDs) throws IOException {
		for (int runID : runIDs) {
			CybershakeRun run = runs2db.getRun(runID);
			CybershakeSite site = sites2db.getSiteFromDB(run.getSiteID());
			
			doCalc(run, site);
		}
	}
	
	private void doCalc(CybershakeRun run, CybershakeSite site) throws IOException {
		System.out.println("Calculating for "+site.short_name+", runID="+run.getRunID());
		File runOutputDir = new File(outputDir, site.short_name+"_run"+run.getRunID());
		Preconditions.checkState(runOutputDir.exists() && runOutputDir.isDirectory() || runOutputDir.mkdir());
		
		// calc CyberShake deterministic
		System.out.println("Calculating CyberShake Deterministic");
		List<DeterministicResult> csDeterms = Lists.newArrayList();
		List<CybershakeIM> forceAddIMs = Lists.newArrayList();
		DiscretizedFunc csDetSpectrum = new ArbitrarilyDiscretizedFunc();
		csDetSpectrum.setName("CyberShake Deterministic");
		
		List<Double> csRoundedPeriods = Lists.newArrayList();
		for (int i=0; i<ims.size(); i++) {
			CybershakeIM im = ims.get(i);
			if (im == null)
				im = new CybershakeIM(-1, IMType.SA, periods.get(i), null, comp);
			csRoundedPeriods.add(im.getVal());
			if (im.getID() < 0 || amps2db.countAmps(run.getRunID(), im) <= 0) {
				// IM not applicable for CyberShake
				csDeterms.add(null);
				forceAddIMs.add(im);
				continue;
			}
			try {
				DeterministicResult csDet = csDetCalc.calculate(run.getRunID(), im);
				csDeterms.add(csDet);
				csDetSpectrum.set(im.getVal(), csDet.getVal());
			} catch (SQLException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
		// calc GMPE deterministic
		// this will also write deterministic table
		System.out.println("Calculating GMPE Deterministic");
		GMPEDeterministicComparisonCalc gmpeDetermCalc = new GMPEDeterministicComparisonCalc(
				run, site, comp, periods, det_percentile, erf, gmpes, runOutputDir);
		gmpeDetermCalc.setCyberShakeData(csDeterms);
		gmpeDetermCalc.calc();
		Table<Double, AttenuationRelationship, DeterministicResult> gmpeDeterms =
				gmpeDetermCalc.getResults();
		Preconditions.checkNotNull(gmpeDeterms);
		// get GMPE combined deterministic
		DiscretizedFunc gmpeDetSpectrum = new ArbitrarilyDiscretizedFunc("GMPE Deterministic");
		List<DiscretizedFunc> gmpeDetSpectrums = Lists.newArrayList();
		for (AttenuationRelationship gmpe : gmpes)
			gmpeDetSpectrums.add(new ArbitrarilyDiscretizedFunc(gmpe.getShortName()+" Deterministic"));
		for (Double period : gmpeDeterms.rowKeySet()) {
			double maxVal = 0d;
			for (int i=0; i<gmpes.size(); i++) {
				AttenuationRelationship gmpe = gmpes.get(i);
				DeterministicResult res = gmpeDeterms.get(period, gmpe);
				gmpeDetSpectrums.get(i).set(period, res.getVal());
				maxVal = Math.max(maxVal, res.getVal());
			}
			gmpeDetSpectrum.set(period, maxVal);
		}
		// plot deterministic
		for (boolean velPlot : new boolean[] {true, false})
			DeterministicResultPlotter.plot(buildDetermMapForSite(site.short_name, comp, csDetSpectrum),
					buildDetermMapForSite(site.short_name, comp, gmpeDetSpectrums),
					Lists.newArrayList(PlotType.PNG, PlotType.PDF), velPlot, runOutputDir);
		
		
		// calc probabalistic
		System.out.println("Calculating Probabilistic");
		RTGMCalc probCalc = new RTGMCalc(run.getRunID(), comp, runOutputDir, db);
		probCalc.setForceAddIMs(forceAddIMs);
		probCalc.setGMPEs(erf, gmpes);
		probCalc.setVelPlot(true);
		probCalc.setPlotTypes(Lists.newArrayList(PlotType.CSV, PlotType.PNG, PlotType.PDF));
		Preconditions.checkState(probCalc.calc());
		// now do again for regular plot
		probCalc.setVelPlot(false);
		probCalc.setPlotTypes(Lists.newArrayList(PlotType.PNG, PlotType.PDF));
		Preconditions.checkState(probCalc.calc());
		DiscretizedFunc csProb = probCalc.getCSSpectrumMap().get(comp);
		
		// get mean GMPE RTGM
		Map<Double, List<DiscretizedFunc>> gmpeHazCurvesMap = probCalc.getGMPEHazardCurves().rowMap().get(comp);
		ArbitrarilyDiscretizedFunc gmpeProb = new ArbitrarilyDiscretizedFunc("GMPE Probabilistic");
		for (int i=0; i<periods.size(); i++) {
			Double period = periods.get(i);
			List<DiscretizedFunc> gmpeHazCurves = gmpeHazCurvesMap.get(csRoundedPeriods.get(i));
			Preconditions.checkNotNull(gmpeHazCurves, "No GMPE haz curves for period="+period
					+". Avail: "+Joiner.on(",").join(gmpeHazCurvesMap.keySet()));
			DiscretizedFunc meanCurve = average(gmpeHazCurves);
			double rtgm = RTGMCalc.calcRTGM(meanCurve);
			gmpeProb.set(period, rtgm);
		}
		
//		// load in ASCE values if available
//		HSSFRow row = null;
//		for (int r=0; r<=asceSheet.getLastRowNum(); r++) {
//			HSSFRow testRow = asceSheet.getRow(r);
//			HSSFCell nameCell = testRow.getCell(0);
//			if (nameCell != null && nameCell.getStringCellValue().trim().equals(site.short_name)) {
//				row = testRow;
//				break;
//			}
//		}
//		DiscretizedFunc asceDeterm, asceProb;
//		if (row == null) {
//			System.out.println("WARNING: Couldn't find site "+site.short_name+" in ASCE spreadsheet");
//			asceDeterm = null;
//			asceProb = null;
//		} else {
//			DiscretizedFunc xVals = gmpeProb;
//			double tl = loadASCEValue(row.getCell(4), evaluator);
//			double prob = loadASCEValue(row.getCell(5), evaluator);
//			double det = loadASCEValue(row.getCell(7), evaluator);
//			asceDeterm = calcASCE(xVals, det, tl);
//			asceProb = calcASCE(xVals, prob, tl);
//			asceProb = null;
//		}
		// get vs30 from GMPE calc
		double vs30 = Double.NaN;
		for (SiteDataValue<?> val : gmpeDetermCalc.getSiteData()) {
			if (val.getDataType().equals(SiteData.TYPE_VS30))
				vs30 = (Double)val.getValue();
		}
		Preconditions.checkState(!Double.isNaN(vs30), "Vs30 not loaded in GMPE calc");
		// gmpeProb just used for x values here
		DiscretizedFunc asceDeterm = calcASCE_DetLowerLimit(gmpeProb, vs30, site.createLocation());
		DiscretizedFunc asceProb = null; // not used
		
		// now generate combined plots
		System.out.println("Generating plots");
		makePlots(runOutputDir, site, run, RTGMCalc.saToPsuedoVel(csDetSpectrum),
				RTGMCalc.saToPsuedoVel(gmpeDetSpectrum), RTGMCalc.saToPsuedoVel(asceDeterm), RTGMCalc.saToPsuedoVel(csProb),
				RTGMCalc.saToPsuedoVel(gmpeProb), asceProb);
	}
	
	private static DiscretizedFunc average(List<DiscretizedFunc> funcs) {
		ArbitrarilyDiscretizedFunc mean = new ArbitrarilyDiscretizedFunc();
		double scalar = 1d/funcs.size(); // evenly weight
		for (DiscretizedFunc func : funcs) {
			if (mean.size() == 0) {
				// initialize with zeros
				for (Point2D pt : func)
					mean.set(pt.getX(), 0d);
			}
			Preconditions.checkState(mean.size() == func.size());
			for (int index=0; index<mean.size(); index++) {
				double y = mean.getY(index);
				y += scalar * func.getY(index);
				mean.set(index, y);
			}
		}
		return mean;
	}
	
	private static Map<String, Map<CyberShakeComponent, DiscretizedFunc>> buildDetermMapForSite(
			String siteName, CyberShakeComponent comp, DiscretizedFunc spectrum) {
		Map<String, Map<CyberShakeComponent, DiscretizedFunc>> maps = Maps.newHashMap();
		Map<CyberShakeComponent, DiscretizedFunc> map = Maps.newHashMap();
		maps.put(siteName, map);
		map.put(comp, spectrum);
		return maps;
	}
	
	private static Map<String, Map<CyberShakeComponent, List<DiscretizedFunc>>> buildDetermMapForSite(
			String siteName, CyberShakeComponent comp, List<DiscretizedFunc> spectrums) {
		Map<String, Map<CyberShakeComponent, List<DiscretizedFunc>>> maps = Maps.newHashMap();
		Map<CyberShakeComponent, List<DiscretizedFunc>> map = Maps.newHashMap();
		maps.put(siteName, map);
		map.put(comp, spectrums);
		return maps;
	}
	
	public static double loadASCEValue(HSSFCell cell, FormulaEvaluator evaluator) {
		if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			return cell.getNumericCellValue();
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
			return evaluator.evaluate(cell).getNumberValue();
		} else {
			try {
				return Double.parseDouble(cell.getStringCellValue());
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
	}
	
//	public static DiscretizedFunc calcASCE(DiscretizedFunc xValsFunc, double val, double tl) {
//		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
//		
//		List<Double> xVals = Lists.newArrayList();
//		for (Point2D pt : xValsFunc)
//			xVals.add(pt.getX());
//		xVals.add(tl); // make sure that TL is in there
//		
//		for (double x : xVals) {
//			if (x <= tl)
//				ret.set(x, val);
//			else
//				ret.set(x, val*(tl/x));
//		}
//		
//		return ret;
//	}
	
	private static TLDataLoader tlData;
	
	public static DiscretizedFunc calcASCE_DetLowerLimit(DiscretizedFunc xValsFunc, double vs30, Location loc) {
		// convert vs30 from m/s to ft/s
		vs30 *= 3.2808399;
		double fa, fv;
		if (vs30 > 5000) {
			// site class A
			fa = 0.8;
			fv = 0.8;
		} else if (vs30 > 2500) {
			// site class B
			fa = 1.0;
			fv = 1.0;
		} else if (vs30 > 1200) {
			// site class C
			fa = 1.0;
			fv = 1.3;
		} else if (vs30 > 600) {
			// site class D
			fa = 1.0;
			fv = 1.5;
		} else {
			// site class E
			fa = 0.9;
			fv = 2.4;
		}
		
		synchronized (MCERDataProductsCalc.class) {
			if (tlData == null) {
				try {
					tlData = new TLDataLoader(
							CSVFile.readStream(TLDataLoader.class.getResourceAsStream(
									"/resources/data/site/USGS_TL/tl-nodes.csv"), true),
							CSVFile.readStream(TLDataLoader.class.getResourceAsStream(
									"/resources/data/site/USGS_TL/tl-attributes.csv"), true));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
		}
		
		double tl = tlData.getValue(loc);
		Preconditions.checkState(!Double.isNaN(tl), "No TL data found for site at "+loc);
		
		return calcASCE_DetLowerLimit(xValsFunc, fv, fa, tl);
	}
	
	public static DiscretizedFunc calcASCE_DetLowerLimit(DiscretizedFunc xValsFunc, double fv, double fa, double tl) {
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		
		double firstRatioXVal = 0.08*fv/fa;
		double secondRatioXVal = 0.4*fv/fa;
		
		List<Double> xVals = Lists.newArrayList();
		for (Point2D pt : xValsFunc)
			xVals.add(pt.getX());
		// make sure that the discontinuities in the function are included for plotting purposes
		if (isWithinDomain(xValsFunc, tl) && !xValsFunc.hasX(tl))
			xVals.add(tl);
		if (isWithinDomain(xValsFunc, firstRatioXVal) && !xValsFunc.hasX(firstRatioXVal))
			xVals.add(firstRatioXVal);
		if (isWithinDomain(xValsFunc, secondRatioXVal) && !xValsFunc.hasX(secondRatioXVal))
			xVals.add(secondRatioXVal);
		
		for (double t : xVals) {
			double sa;
			if (t >= tl)
				sa = 0.6*fv*tl/(t*t);
			else if (t >= secondRatioXVal)
				sa = 0.6*fv/t;
			else if (t >= firstRatioXVal)
				sa = 1.5*fa;
			else
				// linear interpolation from (0, 0.6*fa) to (0.08*fv/fa, 1.5*fa)
				sa = (1.5*fa - 0.6*fa)*t/firstRatioXVal + 0.6*fa;
			ret.set(t, sa);
		}
		
		return ret;
	}
	
	private static boolean isWithinDomain(DiscretizedFunc func, double x) {
		return x >= func.getMinX() && x <= func.getMaxX();
	}
	
	private static DiscretizedFunc maximum(List<DiscretizedFunc> funcs) {
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		
		DiscretizedFunc xVals = funcs.get(0);
		
		for (int i=0; i<xVals.size(); i++) {
			double x = xVals.getX(i);
			
			double y = 0;
			for (DiscretizedFunc func : funcs) {
				Preconditions.checkState((float)x == (float)func.getX(i));;
				y = Math.max(y, func.getY(i));
			}
			
			ret.set(x, y);
		}
		
		return ret;
	}
	
	public void makePlots(File outputDir, CybershakeSite site, CybershakeRun run,
			DiscretizedFunc csDeterm, DiscretizedFunc gmpeDeterm, DiscretizedFunc asceDeterm,
			DiscretizedFunc csProb, DiscretizedFunc gmpeProb, DiscretizedFunc asceProb) throws IOException {
		boolean xLog = true;
		boolean yLog = true;
		Range xRange = new Range(1d, 10d);
		Range yRange = new Range(2e1, 2e3);
		
		String siteName = site.short_name;
		int runID = run.getRunID();

		String prefix = siteName+"_run"+runID;

		csDeterm.setName("CyberShake Det");
		gmpeDeterm.setName("GMPE Det");
		if (asceDeterm != null)
			asceDeterm.setName("ASCE 7-10 Det Lower Limit");

		csProb.setName("CyberShake Prob");
		gmpeProb.setName("GMPE Prob");
		if (asceProb != null)
			asceProb.setName("ASCE 7-10 Ch 11.4");

		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();

		if (asceDeterm != null) {
			funcs.add(asceDeterm);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.DARK_GRAY));
		}

		funcs.add(gmpeProb);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.RED));

		funcs.add(gmpeDeterm);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, PlotSymbol.CIRCLE, 4f, Color.RED));

		funcs.add(csProb);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLUE));

		funcs.add(csDeterm);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, PlotSymbol.CIRCLE, 4f, Color.BLUE));

		PlotSpec spec = new PlotSpec(funcs, chars, siteName, "Period (s)", "PSV (cm/s)");
		spec.setLegendVisible(true);

		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		//			gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);

		gp.drawGraphPanel(spec, xLog, yLog, xRange, yRange);
		gp.getCartPanel().setSize(1000, 800);
		gp.setVisible(true);

		gp.validate();
		gp.repaint();

		File file = new File(outputDir, prefix+"_all_curves");
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");

		DiscretizedFunc csMCER = calcMCER(csDeterm, csProb, asceDeterm);
		csMCER.setName("CyberShake MCER");
		DiscretizedFunc gmpeMCER = calcMCER(gmpeDeterm, gmpeProb, asceDeterm);
		gmpeMCER.setName("GMPE MCER");

		funcs = Lists.newArrayList();
		chars = Lists.newArrayList();

		if (asceDeterm != null) {
			funcs.add(asceDeterm);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.DARK_GRAY));
		}

		if (asceProb != null) {
			funcs.add(asceProb);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLACK));
		}

		funcs.add(gmpeMCER);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.RED));

		funcs.add(csMCER);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLUE));

		spec = new PlotSpec(funcs, chars, siteName+" MCER", "Period (s)", "PSV (cm/s)");
		spec.setLegendVisible(true);

		gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		//			gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);

		gp.drawGraphPanel(spec, xLog, yLog, xRange, yRange);
		gp.getCartPanel().setSize(1000, 800);
		gp.setVisible(true);

		gp.validate();
		gp.repaint();

		file = new File(outputDir, prefix+"_MCER");
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
	}
	
	public static DiscretizedFunc calcMCER(DiscretizedFunc determ, DiscretizedFunc prob,
			DiscretizedFunc determLowerLimit) {
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		
		// from CB Crouse via e-mail 10/17/2014, subject "RE: New plots and updates"
		// The rules are: take the higher of the deterministic curve and the deterministic lower limit
		// curve at each period; the result is the deterministic MCER. Then take the lower of the
		// probabilistic MCER and the deterministic MCER; this curve is the MCER.
		
		for (Point2D pt : prob) {
			double x = pt.getX();
			
			double dVal;
			if (determ == null)
				dVal = Double.POSITIVE_INFINITY;
			else
				dVal = determ.getY(x);
			Preconditions.checkState(dVal >= 0, "Deterministic value must be >= 0");
			double pVal = prob.getY(x);
			Preconditions.checkState(pVal >= 0, "Probabilistic value must be >= 0");
			double dLowVal;
			if (determLowerLimit != null)
				dLowVal = determLowerLimit.getY(x);
			else
				dLowVal = 0d;
			
			double val = calcMCER(dVal, pVal, dLowVal);
			
			ret.set(x, val);
		}
		
		return ret;
	}
	
	public static double calcMCER(double dVal, double pVal, double dLowVal) {
		double val = Math.min(pVal, Math.max(dVal, dLowVal));
		Preconditions.checkState(val > 0d, "It's zero???? pVal="+pVal+", dVal="+dVal+", dLowVal="+dLowVal);
		return val;
	}
	
	private static Options createOptions() {
		Options ops = new Options();
		
		Option run = new Option("R", "run-id", true, "Run ID. Multiple can be comma separated");
		run.setRequired(true);
		ops.addOption(run);
		
		Option component = new Option("cmp", "component", true, "Intensity measure component. "
				+ "Options: "+Joiner.on(",").join(CybershakeIM.getShortNames(CyberShakeComponent.class)));
		component.setRequired(true);
		ops.addOption(component);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		output.setRequired(true);
		ops.addOption(output);
		
		Option period = new Option("p", "period", true, "SA periods. Default: "+default_periods);
		period.setRequired(false);
		ops.addOption(period);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file for comparison");
		erfFile.setRequired(true);
		ops.addOption(erfFile);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true,
				"XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		attenRelFiles.setRequired(true);
		ops.addOption(attenRelFiles);
		
		Option help = new Option("?", "help", false, "Display this message");
		help.setRequired(false);
		ops.addOption(help);
		
		return ops;
	}
	
	public static void printHelp(Options options, String appName) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( appName, options, true );
		System.exit(2);
	}
	
	public static void printUsage(Options options, String appName) {
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 80, appName, options);
		pw.flush();
		System.exit(2);
	}

	public static void main(String[] args) throws DocumentException, InvocationTargetException, IOException {
		if (args.length == 1 && args[0].equals("--hardcoded")) {
//			String argStr = "--run-id 2657,3037,2722,3022,3030,3027,2636,2638,2660,2703,3504,2988,2965,3007";
//			String argStr = "--run-id 2657";
			String argStr = "--run-id 3030"; // STNI orig
//			String argStr = "--run-id 3873"; // STNI 1 hz
			argStr += " --component RotD100";
//			argStr += " --output-dir /home/kevin/CyberShake/MCER/mcer_data_products";
			argStr += " --output-dir /tmp/mcer_data_products";
			argStr += " --erf-file src/org/opensha/sha/cybershake/conf/MeanUCERF.xml";
			argStr += " --atten-rel-file src/org/opensha/sha/cybershake/conf/ask2014.xml,"
					+ "src/org/opensha/sha/cybershake/conf/bssa2014.xml,"
					+ "src/org/opensha/sha/cybershake/conf/cb2014.xml,"
					+ "src/org/opensha/sha/cybershake/conf/cy2014.xml";
			args = Splitter.on(" ").splitToList(argStr).toArray(new String[0]);
		}
		
		DBAccess db = null;
		
		try {
			Options options = createOptions();
			
			String appName = ClassUtils.getClassNameWithoutPackage(RTGMCalc.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					printHelp(options, appName);
				}
				
				MCERDataProductsCalc calc = new MCERDataProductsCalc(cmd);
				db = calc.db;
				
				List<Integer> runIDs = Lists.newArrayList();
				for (String runID : HazardCurvePlotter.commaSplit(cmd.getOptionValue("run-id")))
					runIDs.add(Integer.parseInt(runID));
				
				calc.calc(runIDs);
			} catch (MissingOptionException e) {
				Options helpOps = new Options();
				helpOps.addOption(new Option("h", "help", false, "Display this message"));
				try {
					CommandLine cmd = parser.parse( helpOps, args);
					
					if (cmd.hasOption("help")) {
						printHelp(options, appName);
					}
				} catch (ParseException e1) {}
				System.err.println(e.getMessage());
				printUsage(options, appName);
//			e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
				printUsage(options, appName);
			}
			
			System.out.println("Done!");
			if (db != null)
				db.destroy();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			if (db != null)
				db.destroy();
			System.exit(1);
		}
	}

}
