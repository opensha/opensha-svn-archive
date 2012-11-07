package org.opensha.nshmp2.tmp;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.MathUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.source.GridERF;
import org.opensha.nshmp2.erf.source.PointSource;
import org.opensha.nshmp2.erf.source.Sources;
import org.opensha.nshmp2.imr.NSHMP08_CEUS;
import org.opensha.nshmp2.imr.NSHMP08_CEUS_Grid;
import org.opensha.nshmp2.imr.NSHMP08_SUB_SlabGrid;
import org.opensha.nshmp2.imr.NSHMP08_WUS_Grid;
import org.opensha.nshmp2.util.FaultCode;
import org.opensha.nshmp2.util.NSHMP_IMR_Util;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.SiteTypeParam;
import org.opensha.nshmp2.util.Utils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08.NSHMP08_CEUS_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.param.EqkRuptureParams.FaultTypeParam;
import org.opensha.sha.imr.param.EqkRuptureParams.RupTopDepthParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;

/**
 * Mostly for GridERF
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class TestCalc {

	private static HazardCurveCalculator hazardCurveCalculator;
	private static GridERF erf;
	private static ScalarIMR imr;
	private static Period per = Period.GM0P00;
	private static double distance = 200;
	private static NEHRP_TestCity city = NEHRP_TestCity.MEMPHIS;
//	private static NEHRP_TestCity city = NEHRP_TestCity.PORTLAND;
//	private static NEHRP_TestCity city = NEHRP_TestCity.LOS_ANGELES;
	
	// NOTE better to use HazardCalc
	public static void main(String[] args) {
		
		System.out.println("init erf");

		erf = Sources.getGrid("CAmap.21.gr.in");
//		erf = Sources.getGrid("portdeep.in");
//		erf = Sources.getGrid("CEUS.2007all8.J.in");
//		erf = GridERF.getTestGrid();
		erf.updateForecast();
		System.out.println("numRup: " + erf.getRuptureCount());
		
//		Location srcLoc = city.location();
//		srcLoc = new Location(srcLoc.getLatitude(), srcLoc.getLongitude() - 0.5);
//
//		System.out.println(erf.getMFD(srcLoc));

		System.out.println("init imr");

		NSHMP08_WUS_Grid tmpIMR = new NSHMP08_WUS_Grid();
		tmpIMR.getParameter(NSHMP08_WUS_Grid.IMR_UNCERT_PARAM_NAME).setValue(false);
		tmpIMR.setIntensityMeasure((per == Period.GM0P00) ? "PGA" : "SA");
		if (per != Period.GM0P00) {
			tmpIMR.getParameter(PeriodParam.NAME).setValue(per.getValue());
		}
		// ERF dependent settings
		tmpIMR.getParameter(FaultTypeParam.NAME).setValue(CB_2008_AttenRel.FLT_TYPE_REVERSE);
		tmpIMR.setGridHybrid(true);
		Function<Double, Double> func = new Function<Double, Double>() {
			@Override public Double apply(Double mag) {
				return (mag < 6.5) ? 5.0 : 1.0;
			}
		}; // this should otherwise be obtained from GridERF
		tmpIMR.setTable(func);

//		NSHMP08_SUB_SlabGrid tmpIMR = new NSHMP08_SUB_SlabGrid();
//		tmpIMR.setIntensityMeasure((per == Period.GM0P00) ? "PGA" : "SA");
//		tmpIMR.getParameter(PeriodParam.NAME).setValue(per.getValue());
//		tmpIMR.getParameter(RupTopDepthParam.NAME).setValue(50.0);
//		tmpIMR.setTable();
		
//		NSHMP08_CEUS_Grid tmpIMR = new NSHMP08_CEUS_Grid();
//		tmpIMR.setIntensityMeasure((per == Period.GM0P00) ? "PGA" : "SA");
//		tmpIMR.getParameter(PeriodParam.NAME).setValue(per.getValue());
//		tmpIMR.setFaultCode(FaultCode.M_CONV_J);
		imr = tmpIMR;
		
//		initIMR();
		System.out.println("do calc");
		doCalc();
		System.exit(0);
//		hazSpeedTest();
	}

	static void doCalc() {
		Vs30_Param vs30param = new Vs30_Param(760);
		vs30param.setValueAsDefault();
		SiteTypeParam siteTypeParam = new SiteTypeParam();

			hazardCurveCalculator = new HazardCurveCalculator();
			hazardCurveCalculator.getAdjustableParams()
				.getParameter(MaxDistanceParam.NAME).setValue(distance);

			// TODO need grid switch to tell whether to shift to grid node for
			// which an actual NSHM calculataion was done. hazpoint.f returns
			// the closest curve via nint
//			System.out.println("Loc: " + adjustToNode(loc));
			Site site = new Site(city.shiftedLocation());
			System.out.println(site.getLocation());
			site.addParameter(vs30param);
			site.addParameter(siteTypeParam);

			// init
			// String imt = (period == 0) ? "PGA" : "SA";
			// String imtString = imt;
			// imtString += (period != 0) ?
			// "_" + periodFormat.format(period) + "sec" : "";

			// File outputDir = new File(outDir);
			// outputDir.mkdirs();

			// Do for First Lat
			// double twoPercentProb, tenPercentProb;
			// int colIndex=1;
			// for(double lon=minLon; lon<=maxLon; lon+=GRID_SPACING,
			// ++colIndex) {
			// System.out.println("Doing Site :" + latLonFormat.format(lat) +
			// "," + latLonFormat.format(lon) + " " + imtString);

			// // ensure that DEPTH_2_5KM_PARAM value iis set from default
			// DEPTH_2_5KM_PARAM.setValueAsDefault();
			// // set DEPTH_1_0KM_PARAM based on vs30; this could conceivably
			// if (vs30 == 760.0) {
			// DEPTH_1_0KM_PARAM.setValue(40.0);
			// } else if (vs30 == 259.0) {
			// DEPTH_1_0KM_PARAM.setValue(330.0);
			// }

//			// TODO need grid switch to tell whether to shift to grid node for
//			// which an actual NSHM calculataion was done. hazpoint.f returns
//			// the closest curve via nint
////			System.out.println("Loc: " + adjustToNode(loc));
//			Site site = new Site(city.shiftedLocation());
//			System.out.println(site.getLocation());
//			site.addParameter(vs30param);
//			site.addParameter(siteTypeParam);
			// site.addParameter(DEPTH_2_5KM_PARAM); // used by CB2008
			// site.addParameter(DEPTH_1_0KM_PARAM); // used by CY2008
			// site.addParameter(VS_30_TYPE_PARAM);

			// do log of X axis values
			DiscretizedFunc hazFunc = per.getFunction();
			// for(int i=0; i<numX_Vals; ++i)
			// hazFunc.set(Math.log(function.getX(i)), 1);

			// Note here that hazardCurveCalculator accepts the Log of X-Values
			hazardCurveCalculator.getHazardCurve(hazFunc, site, imr, erf);
//			System.out.println(hazFunc);
			// convert to annual rate
			for (Point2D p : hazFunc) {
				hazFunc.set(p.getX(), NSHMP_Utils.probToRate(p.getY(), 1));
			}
			System.out.println(hazFunc);
			// Unlog the X-Values before doing interpolation. The Y Values we
			// get from hazardCurveCalculator are unmodified
			// DiscretizedFunc newFunc = new ArbitrarilyDiscretizedFunc();
			// for(int i=0; i<numX_Vals; ++i)
			// newFunc.set(function.getX(i), hazFunc.getY(i));
			//
			// try {
			// twoPercentProb =
			// newFunc.getFirstInterpolatedX_inLogXLogYDomain(0.02);
			// } catch (InvalidRangeException ire) {
			// twoPercentProb = 0.0;
			// }
			// try {
			// tenPercentProb =
			// newFunc.getFirstInterpolatedX_inLogXLogYDomain(0.1);
			// } catch (InvalidRangeException ire) {
			// tenPercentProb = 0.0;
			// }
			// sheet.getRow(0).createCell((short)colIndex).setCellValue(latLonFormat.format(lon));
			// for(int i=0; i<numX_Vals; ++i)
			// sheet.createRow(i+1).createCell((short)colIndex).setCellValue(newFunc.getY(i));
			//
			// sheet.createRow(twoPercentProbRoIndex).createCell((short)colIndex).setCellValue(twoPercentProb);
			// sheet.createRow(tenPercentProbRoIndex).createCell((short)colIndex).setCellValue(tenPercentProb);
			//
			// }
			// FileOutputStream fileOut = new FileOutputStream(outputFileName);
			// wb.write(fileOut);
			// fileOut.close();
		// System.exit(0);

	}

	public static void createCurve(ERF erf, NEHRP_TestCity city, ScalarIMR imr, Period per) {
		
	}
//	// init any IMR params
//	private static void initIMR() {
//		System.out.println("init imr");
//		imr = new NSHMP08_CEUS(null);
//		imr.setParamDefaults();
//
//		// TODO check file name for setting
////		imr.getParameter("Mag Conversion").setValue(FaultCode.M_CONV_);
////		imr.getParameter("Source Type").setValue(CEUS_ERF.SourceCEUS.GRID);
//		// System.out.println("st: " +
//		// imr.getParameter("Source Type").getValue());
//		// System.out.println("st: " +
//		// imr.getParameter("Source Type").getValue());
//		// System.out.println("mc: " + imr.getParameter("Mag Conversion"));
//		// imr.getParameter(name)
//		// if (id == "BA") {
//		// imr = new BA_2008_AttenRel(this);
//		// } else if (id == "CB") {
//		// imr = new CB_2008_AttenRel(this);
//		// } else if (id == "CY"){
//		// imr = new CY_2008_AttenRel(this);
//		// }
//		// imr.setParamDefaults();
//		// imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//		// imr.getParameter(SigmaTruncLevelParam.NAME).setValue(3.0);
//	}

	// init any ERF params
//	private static void initERF() {
//
//		// /erf.
//		// meanUCERF2 = new MeanUCERF2();
//		// meanUCERF2.setParameter(MeanUCERF2.RUP_OFFSET_PARAM_NAME, new
//		// Double(5.0));
//		// meanUCERF2.setParameter(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME,
//		// false);
//		// meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME,
//		// UCERF2.PROB_MODEL_POISSON);
//		// meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME,
//		// UCERF2.BACK_SEIS_INCLUDE);
//		// meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME,
//		// UCERF2.BACK_SEIS_RUP_CROSSHAIR);
//		// meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME,
//		// UCERF2.CENTERED_DOWNDIP_FLOATER);
//		// meanUCERF2.getTimeSpan().setDuration(50.0);
//		// meanUCERF2.updateForecast();
//	}
	
//	private void doCEUS() {
//		erf = new CEUS_ERF();
//		erf.updateForecast();
//
//		imr = new NSHMP08_CEUS(null);
//		imr.setParamDefaults();
//
//	}
	
	private static void hazSpeedTest() {
		int size = 10000000;
		
		DiscretizedFunc pOut = Period.GM0P00.getFunction();
		Utils.oneFunc(pOut);

		DiscretizedFunc rOut = Period.GM0P00.getFunction();
		Utils.zeroFunc(rOut);

		DiscretizedFunc fpe = Period.GM0P00.getFunction();
		for (int i=0; i<fpe.getNum(); i++) fpe.set(i, Math.random());
		
		// set annualized rate array
		double[] rates = new double[size];
		for (int i=0;i<size; i++) rates[i] = 0.00001 * Math.random();
		
		// set Poiss prob array
		double[] probs = new double[size];
		for (int i=0;i<size; i++) probs[i] = 1 - Math.exp(rates[i]);
		
		Stopwatch sw = new Stopwatch();
		
		sw.start();
		// NSHMP 
		//	- annualized rate (R) from source
		//	- for each GM Poiss Prob is: 1 - exp(PE1*R1 + PE2*R2 + ... PEi*Ri)
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < fpe.getNum(); j++) {
				rOut.set(j, rOut.getY(j) + rates[i] * fpe.getY(j));
			}
		}
		// rates to Poiss probs
		for (int i = 0; i < rOut.getNum(); i++) {
			pOut.set(i, 1 - Math.exp(rOut.getY(i)));
		}
		sw.stop();
		System.out.println("NSHMP: " + sw.elapsedTime(TimeUnit.SECONDS));
		
		sw.reset().start();
		// SHA
		//	- PoissProb (P) from source
		//	- for each GM Poiss Prob is: 1 - [ (1-P1)^PE1 * (1-P2)^PE2 * ... (1-Pi)^PEi ]
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < fpe.getNum(); j++) {
				pOut.set(j, pOut.getY(j) * Math.pow(1 - probs[i], fpe.getY(j)));
			}
		}
		// PoissProb final step
		for (int i = 0; i < pOut.getNum(); i++) {
			pOut.set(i, 1 - pOut.getY(i));
		}
		sw.stop();
		System.out.println("  SHA: " + sw.elapsedTime(TimeUnit.SECONDS));
		
		
	}


}
