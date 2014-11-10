package org.opensha.sha.cybershake.calc;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.Range;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ExceptionUtils;
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
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.MultiIMR_Averaged_AttenRel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MCERDataProductsCalc {
	
	private List<CybershakeRun> runs;
	private List<CybershakeSite> sites;
	private CyberShakeComponent comp;
	
	private List<DiscretizedFunc> csDeterms;
	private List<DiscretizedFunc> csProbs;
	
	private List<DiscretizedFunc> gmpeDeterms;
	private List<DiscretizedFunc> gmpeProbs;
	
	private List<DiscretizedFunc> asceDeterms;
	private List<DiscretizedFunc> asceProbs;
	
	private DBAccess db;
	
	public MCERDataProductsCalc(List<Integer> runIDs, File csDeterministicFile,
			ERF erf, List<AttenuationRelationship> gmpes, File asceFile) throws IOException {
		db = Cybershake_OpenSHA_DBApplication.db;
		
		Runs2DB run2db = new Runs2DB(db);
		CybershakeSiteInfo2DB site2db = new CybershakeSiteInfo2DB(db);
		runs = Lists.newArrayList();
		sites = Lists.newArrayList();
		for (int runID : runIDs) {
			CybershakeRun run = run2db.getRun(runID);
			runs.add(run);
			sites.add(site2db.getSiteFromDB(run.getSiteID()));
		}
		
		comp = CyberShakeComponent.RotD100;
		
		// load in deterministic data
		Map<String, Map<CyberShakeComponent, DiscretizedFunc>> csSpectrumMaps = Maps.newHashMap();
		Map<String, Map<CyberShakeComponent, List<DiscretizedFunc>>> gmpeSpectrumMaps = Maps.newHashMap();
		DeterministicResultPlotter.loadData(csDeterministicFile, comp, csSpectrumMaps, gmpeSpectrumMaps);
		csDeterms = Lists.newArrayList();
		gmpeDeterms = Lists.newArrayList();
		for (CybershakeSite site : sites) {
			Preconditions.checkState(csSpectrumMaps.containsKey(site.short_name));
			csDeterms.add(RTGMCalc.saToPsuedoVel(csSpectrumMaps.get(site.short_name).get(comp)));
			// maximum of each deterministic NGA value
			gmpeDeterms.add(RTGMCalc.saToPsuedoVel(maximum(gmpeSpectrumMaps.get(site.short_name).get(comp))));
		}
		
		// calculate probabalistic
		csProbs = Lists.newArrayList();
		gmpeProbs = Lists.newArrayList();
		// need average GMPE
		MultiIMR_Averaged_AttenRel meanGMPE = new MultiIMR_Averaged_AttenRel(gmpes);
		List<AttenuationRelationship> meanGMPEList = Lists.newArrayList();
		meanGMPEList.add(meanGMPE);
		List<CybershakeIM> forceAddIMs = Lists.newArrayList();
		forceAddIMs.add(new CybershakeIM(-1, IMType.SA, 1d, null, comp));
		forceAddIMs.add(new CybershakeIM(-1, IMType.SA, 1.5d, null, comp));
		for (int i=0; i<runs.size(); i++) {
			int runID = runs.get(i).getRunID();
			RTGMCalc rtgmCalc = new RTGMCalc(runID, comp, null, db);
			rtgmCalc.setGMPEs(erf, meanGMPEList);
			rtgmCalc.setForceAddIMs(forceAddIMs);
			Preconditions.checkState(rtgmCalc.calc());
			
			csProbs.add(RTGMCalc.saToPsuedoVel(rtgmCalc.getCSSpectrumMap().get(comp)));
			gmpeProbs.add(RTGMCalc.saToPsuedoVel(rtgmCalc.getGMPESpectrumMap().get(comp).get(0)));
		}
		
		// load in ASCE values
		asceDeterms = Lists.newArrayList();
		asceProbs = Lists.newArrayList();
		HSSFWorkbook wb;
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(asceFile));
			wb = new HSSFWorkbook(fs);
		} catch (Exception e1) {
			System.err.println("Couldn't load input file. Make sure it's an xls file and NOT an xlsx file.");
			throw ExceptionUtils.asRuntimeException(e1);
		}
		HSSFSheet sheet = wb.getSheetAt(0);
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
		DiscretizedFunc xVals = gmpeProbs.get(0);
		for (CybershakeSite site : sites) {
			HSSFRow row = null;
			for (int r=0; r<=sheet.getLastRowNum(); r++) {
				HSSFRow testRow = sheet.getRow(r);
				HSSFCell nameCell = testRow.getCell(0);
				if (nameCell != null && nameCell.getStringCellValue().trim().equals(site.short_name)) {
					row = testRow;
					break;
				}
			}
			Preconditions.checkState(row != null, "Couldn't find site "+site.short_name+" in ASCE spreadsheet");
			double tl = loadASCEValue(row.getCell(4), evaluator);
			double prob = loadASCEValue(row.getCell(5), evaluator);
			double det = loadASCEValue(row.getCell(7), evaluator);
			
			asceDeterms.add(calcASCE(xVals, det, tl));
			asceProbs.add(calcASCE(xVals, prob, tl));
		}
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
	
	public static DiscretizedFunc calcASCE(DiscretizedFunc xValsFunc, double val, double tl) {
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		
		List<Double> xVals = Lists.newArrayList();
		for (Point2D pt : xValsFunc)
			xVals.add(pt.getX());
		xVals.add(tl); // make sure that TL is in there
		
		for (double x : xVals) {
			if (x <= tl)
				ret.set(x, val);
			else
				ret.set(x, val*(tl/x));
		}
		
		return ret;
	}
	
	private static DiscretizedFunc maximum(List<DiscretizedFunc> funcs) {
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		
		DiscretizedFunc xVals = funcs.get(0);
		
		for (int i=0; i<xVals.getNum(); i++) {
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
	
	public void makePlots(File outputDir) throws IOException {
		boolean xLog = true;
		boolean yLog = true;
		Range xRange = new Range(1d, 10d);
		Range yRange = new Range(2e1, 2e3);
		
		for (int i=0; i<runs.size(); i++) {
			String siteName = sites.get(i).short_name;
			int runID = runs.get(i).getRunID();
			
			String prefix = siteName+"_run"+runID;
			
			DiscretizedFunc csDeterm = csDeterms.get(i);
			csDeterm.setName("CyberShake Det");
			DiscretizedFunc gmpeDeterm = gmpeDeterms.get(i);
			gmpeDeterm.setName("GMPE Det");
			DiscretizedFunc asceDeterm = asceDeterms.get(i);
			asceDeterm.setName("ASCE 7-10 Det Lower Limit");
			
			DiscretizedFunc csProb = csProbs.get(i);
			csProb.setName("CyberShake Prob");
			DiscretizedFunc gmpeProb = gmpeProbs.get(i);
			gmpeProb.setName("GMPE Prob");
			DiscretizedFunc asceProb = asceProbs.get(i);
			asceProb.setName("ASCE 7-10 Ch 11.4");
			
			List<DiscretizedFunc> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			funcs.add(asceDeterm);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.DARK_GRAY));
			
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
			
			funcs.add(asceDeterm);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.DARK_GRAY));
			
			funcs.add(asceProb);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLACK));
			
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
	}
	
	public static DiscretizedFunc calcMCER(DiscretizedFunc determ, DiscretizedFunc prob,
			DiscretizedFunc determLowerLimit) {
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		
		// from CB Crouse via e-mail 10/17/2014, subject "RE: New plots and updates"
		// The rules are: take the higher of the deterministic curve and the deterministic lower limit
		// curve at each period; the result is the deterministic MCER. Then take the lower of the
		// probabilistic MCER and the deterministic MCER; this curve is the MCER.
		
		for (Point2D pt : determ) {
			double x = pt.getX();
			
			double dVal = determ.getY(x);
			double pVal = prob.getY(x);
			double dLowVal = determLowerLimit.getY(x);
			
			double val = Math.min(pVal, Math.max(dVal, dLowVal));
			
			ret.set(x, val);
		}
		
		return ret;
	}

	public static void main(String[] args) throws DocumentException, InvocationTargetException, IOException {
		List<Integer> runIDs = Lists.newArrayList(2657, 3037, 2722, 3022, 3030, 3027, 2636, 2638,
				2660, 2703, 3504, 2988, 2965, 3007);
		File baseDir = new File("/home/kevin/CyberShake/MCER");
		File outputDir = new File(baseDir, "combined_plots");
		Preconditions.checkArgument((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir(),
				"Output dir does not exist and could not be created");
		File csDeterministicFile = new File(baseDir, "Deterministic_2014_10_15.xls");
		File asceFile = new File(baseDir, "ASCE7-10_Sms_Sm1_TL_det LL for 14 sites.xls");
		ERF erf = ERFSaver.LOAD_ERF_FROM_FILE(MCERDataProductsCalc.class.getResource(
				"/org/opensha/sha/cybershake/conf/MeanUCERF.xml"));
		erf.updateForecast();
		List<AttenuationRelationship> gmpes = Lists.newArrayList();
		gmpes.add(AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(MCERDataProductsCalc.class.getResource(
				"/org/opensha/sha/cybershake/conf/ask2014.xml")));
		gmpes.add(AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(MCERDataProductsCalc.class.getResource(
				"/org/opensha/sha/cybershake/conf/bssa2014.xml")));
		gmpes.add(AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(MCERDataProductsCalc.class.getResource(
				"/org/opensha/sha/cybershake/conf/cb2014.xml")));
		gmpes.add(AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(MCERDataProductsCalc.class.getResource(
				"/org/opensha/sha/cybershake/conf/cy2014.xml")));
		
		MCERDataProductsCalc calc = new MCERDataProductsCalc(runIDs, csDeterministicFile, erf, gmpes, asceFile);
		calc.makePlots(outputDir);
		
		System.out.println("Success!");
		calc.db.destroy();
		System.exit(0);
	}

}
