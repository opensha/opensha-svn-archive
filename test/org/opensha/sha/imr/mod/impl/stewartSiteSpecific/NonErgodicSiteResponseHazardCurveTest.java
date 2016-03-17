package org.opensha.sha.imr.mod.impl.stewartSiteSpecific;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.DataUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.imr.mod.impl.stewartSiteSpecific.NonErgodicSiteResponseMod.Params;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public class NonErgodicSiteResponseHazardCurveTest {
	
	private static final double precision = 0.02;
	private static final double precision_percent = 0.1;
	
	private static ERF erf;
	private static NonErgodicSiteResponseGMPE gmpe;
	private static NonErgodicSiteResponseMod mod;
	
	private static Table<Site, Map<Params, Double>, ArbitrarilyDiscretizedFunc> pgaData;
	private static Table<Site, Map<Params, Double>, ArbitrarilyDiscretizedFunc> sa1Data;
	
	private static HazardCurveCalculator calc;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		erf = new Frankel96_AdjustableEqkRupForecast();
		erf.updateForecast();
		
		gmpe = new NonErgodicSiteResponseGMPE();
		gmpe.setParamDefaults();
		mod = gmpe.getMod();
		mod.setTsite(Double.NaN);
		
		pgaData = loadData("hazard_pga.xls");
		sa1Data = loadData("hazard_sa_1s.xls");
		
		calc = new HazardCurveCalculator();
	}
	
	private static Table<Site, Map<Params, Double>, ArbitrarilyDiscretizedFunc> loadData(String fileName) throws IOException {
		System.out.println("Loading from "+fileName);
		Table<Site, Map<Params, Double>, ArbitrarilyDiscretizedFunc> data = HashBasedTable.create();
		
		POIFSFileSystem fs = new POIFSFileSystem(NonErgodicSiteResponseHazardCurveTest.class.getResourceAsStream(fileName));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		for (int s=0; s<wb.getNumberOfSheets(); s++) {
//			System.out.println("Sheet "+s);
			HSSFSheet sheet = wb.getSheetAt(s);
			HSSFRow siteRow = sheet.getRow(1);
			int col = 0;
			double lon = siteRow.getCell(col++).getNumericCellValue();
			double lat = siteRow.getCell(col++).getNumericCellValue();
			double vs30 = siteRow.getCell(col++).getNumericCellValue();
			double z1p0 = siteRow.getCell(col++).getNumericCellValue();
			double f1 = siteRow.getCell(col++).getNumericCellValue();
			double f2 = siteRow.getCell(col++).getNumericCellValue();
			double f3 = siteRow.getCell(col++).getNumericCellValue();
			double F = siteRow.getCell(col++).getNumericCellValue();
			double phiLnY = siteRow.getCell(col++).getNumericCellValue();
			double phiS2S = siteRow.getCell(col++).getNumericCellValue();
			HSSFCell yMaxCell = siteRow.getCell(col++);
			double yMax;
			if (yMaxCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
				yMax = yMaxCell.getNumericCellValue();
			else
				yMax = Double.NaN;
			
			Site site = new Site(new Location(lat, lon));
			for (Parameter<?> param : gmpe.getSiteParams()) {
				site.addParameter((Parameter<?>) param.clone());
			}
			site.getParameter(Double.class, Vs30_Param.NAME).setValue(vs30);
			site.getParameter(Double.class, DepthTo1pt0kmPerSecParam.NAME).setValue(z1p0);
			
			Map<Params, Double> params = Maps.newHashMap();
			params.put(Params.F1, f1);
			params.put(Params.F2, f2);
			params.put(Params.F3, f3);
			params.put(Params.F, F);
			params.put(Params.PHI_lnY, phiLnY);
			params.put(Params.PHI_S2S, phiS2S);
			params.put(Params.Ymax, yMax);
			
			int imCol = 2;
			int probCol = 3;
			
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			for (int r=5; r<=sheet.getLastRowNum(); r++) {
				HSSFRow row = sheet.getRow(r);
				func.set(row.getCell(imCol).getNumericCellValue(), row.getCell(probCol).getNumericCellValue());
			}
			System.out.println("Loaded "+func.size()+" curve points from "+func.getMinX()+" to "+func.getMaxX());
			
			data.put(site, params, func);
		}
		
		return data;
	}

	@Test
	public void testPGA() {
		doTest(0, pgaData);
	}

	@Test
	public void testSA1() {
		doTest(1, sa1Data);
	}
	
	private void doTest(double period, Table<Site, Map<Params, Double>, ArbitrarilyDiscretizedFunc> data) {
		if (period == 0) {
			gmpe.setIntensityMeasure(PGA_Param.NAME);
			System.out.println("Testing PGA");
		} else {
			gmpe.setIntensityMeasure(SA_Param.NAME);
			SA_Param.setPeriodInSA_Param(gmpe.getIntensityMeasure(), period);
			System.out.println("Testing 1s SA");
		}
		double maxDiff = 0d;
		double pDiffAtMax = 0d;
		for (Cell<Site, Map<Params, Double>, ArbitrarilyDiscretizedFunc> cell : data.cellSet()) {
			Site site = cell.getRowKey();
			mod.setSiteAmpParams(period, cell.getColumnKey());
			ArbitrarilyDiscretizedFunc dataFunc = cell.getValue();
			ArbitrarilyDiscretizedFunc calcFunc = HazardCurveSetCalculator.getLogFunction(dataFunc);
			calc.getHazardCurve(calcFunc, site, gmpe, erf);
			calcFunc = HazardCurveSetCalculator.unLogFunction(dataFunc, calcFunc);
			
			System.out.println("Testing for site at "+site.getLocation()+", vs30="+site.getParameter(Vs30_Param.NAME).getValue()
					+", z1.0="+site.getParameter(DepthTo1pt0kmPerSecParam.NAME).getValue());
			mod.printCurParams();
			
			System.out.println("IM\tData\tCalc\tDiff\t% Diff");
			for (int i=0; i<calcFunc.size(); i++) {
				double im = dataFunc.getX(i);
				double dataProb = dataFunc.getY(i);
				double calcProb = calcFunc.getY(i);
				
				double diff = Math.abs(dataProb - calcProb);
				double pDiff = DataUtils.getPercentDiff(calcProb, dataProb);
				
				System.out.println((float)im+"\t"+(float)dataProb+"\t"+(float)calcProb+"\t"+(float)diff+"\t"+(float)pDiff+" %");
			}
			
			for (int i=0; i<calcFunc.size(); i++) {
				double dataProb = dataFunc.getY(i);
				double calcProb = calcFunc.getY(i);
				
				double diff = Math.abs(dataProb - calcProb);
				double pDiff = DataUtils.getPercentDiff(calcProb, dataProb);
				if (diff > maxDiff) {
					maxDiff = diff;
					pDiffAtMax = pDiff;
				}
				if (pDiff > precision_percent)
					assertEquals("Mismatch at im="+dataFunc.getX(i)+", diff="+diff+", pDiff="+pDiff+" %",
							dataProb, calcProb, precision);
			}
		}
		
		System.out.println("Max diff: "+maxDiff+" (has pDiff of "+pDiffAtMax+" %)");
	}

}
