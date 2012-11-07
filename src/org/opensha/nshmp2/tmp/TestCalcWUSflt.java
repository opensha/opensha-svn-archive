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
import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.source.FaultERF;
import org.opensha.nshmp2.erf.source.GridERF;
import org.opensha.nshmp2.erf.source.PointSource;
import org.opensha.nshmp2.erf.source.Sources;
import org.opensha.nshmp2.erf.source.SubductionERF;
import org.opensha.nshmp2.imr.NSHMP08_CEUS;
import org.opensha.nshmp2.imr.NSHMP08_CEUS_Grid;
import org.opensha.nshmp2.imr.NSHMP08_SUB_Interface;
import org.opensha.nshmp2.imr.NSHMP08_SUB_SlabGrid;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.imr.NSHMP08_WUS_Grid;
import org.opensha.nshmp2.util.FaultCode;
import org.opensha.nshmp2.util.NSHMP_IMR_Util;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;
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
public class TestCalcWUSflt {

	private static HazardCurveCalculator hazardCurveCalculator;
	private static FaultERF erf;
//	private static ERF erf;
	private static ScalarIMR imr;
	private static Period per = Period.GM1P00;
	private static double distance = 200;
	private static NEHRP_TestCity city = NEHRP_TestCity.SALT_LAKE_CITY;
	
	public static void main(String[] args) {
		
		System.out.println("init erf");

//		erf = Sources.getFault("bFault.ch.in");
		erf = Sources.getFault("wasatch.3dip.ch.in");
		erf.updateForecast();
		
//		erf = new MeanUCERF2();
//		erf.updateForecast();

		//		for (int i=0; i<erf.getNumSources(); i++) {
//			System.out.println(erf.getSource(i).toString());
//		}
//		System.out.println("numRup: " + erf.getRuptureCount());
		System.out.println("init imr");

		NSHMP08_WUS tmpIMR = new NSHMP08_WUS();
		tmpIMR.setIntensityMeasure((per == Period.GM0P00) ? "PGA" : "SA");
		// rem this must be wrapped in if statement for NGA (all NGAs do not use
		// 0.0 as id for PGA; here use try block b/c Youns and AB03 both do use
		// 0.0 for pGA, but Zhao does not
		try {
			tmpIMR.getParameter(PeriodParam.NAME).setValue(per.getValue());
		}  catch (ConstraintException ce) { /* do nothing */ }

		imr = tmpIMR;
		
		System.out.println("do calc");
		doCalc();
		System.exit(0);
	}

	static void doCalc() {
		Vs30_Param vs30param = new Vs30_Param(760);
		vs30param.setValueAsDefault();

			hazardCurveCalculator = new HazardCurveCalculator();
			hazardCurveCalculator.getAdjustableParams()
				.getParameter(MaxDistanceParam.NAME).setValue(distance);

			// do log of X axis values
			DiscretizedFunc hazFunc = per.getFunction();

			// Note here that hazardCurveCalculator accepts the Log of X-Values
			hazardCurveCalculator.getHazardCurve(hazFunc, city.getSite(), imr, erf);

			// convert to annual rate
			for (Point2D p : hazFunc) {
				hazFunc.set(p.getX(), NSHMP_Utils.probToRate(p.getY(), 1));
			}
			System.out.println(hazFunc);

		// System.exit(0);

	}



}
