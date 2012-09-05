package org.opensha.nshmp2.tmp;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.source.Sources;
import org.opensha.nshmp2.erf.source.SubductionERF;
import org.opensha.nshmp2.imr.NSHMP08_SUB_Interface;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

/**
 * Mostly for GridERF
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class TestCalcSub {

	private static HazardCurveCalculator hazardCurveCalculator;
	private static SubductionERF erf;
	private static ScalarIMR imr;
	private static Period per = Period.GM0P00;
	private static double distance = 1000;
	private static NEHRP_TestCity city = NEHRP_TestCity.SEATTLE;
	
	public static void main(String[] args) {
		
		System.out.println("init erf");

//		erf = Sources.getSub("cascadia.bot.88.in");
		erf = Sources.getSub("cascadia.bot.8082.in");
//		erf = Sources.getSub("cascadia.bot.9pm.in");
		erf.updateForecast();
		
//		for (int i=0; i<erf.getNumSources(); i++) {
//			System.out.println(erf.getSource(i).toString());
//		}
		System.out.println("numRup: " + erf.getRuptureCount());
		System.out.println("init imr");

		NSHMP08_SUB_Interface tmpIMR = new NSHMP08_SUB_Interface();
		tmpIMR.setIntensityMeasure((per == Period.GM0P00) ? "PGA" : "SA");
		// rem this must be wrapped in if statement for NGA (all NGAs do not use
		// 0.0 as id for PGA; here use try block b/c Youns and AB03 both do use
		// 0.0 for pGA, but Zhao does not
		try { // this actually isn't needed now that zhao has been rewritten ann appears stable
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

			Site site = new Site(city.location());
			System.out.println(site.getLocation());
			site.addParameter(vs30param);


			// do log of X axis values
			DiscretizedFunc hazFunc = per.getFunction();

			// Note here that hazardCurveCalculator accepts the Log of X-Values
			hazardCurveCalculator.getHazardCurve(hazFunc, site, imr, erf);

			// convert to annual rate
			for (Point2D p : hazFunc) {
				hazFunc.set(p.getX(), NSHMP_Utils.probToRate(p.getY(), 1));
			}
			System.out.println(hazFunc);
		// System.exit(0);

	}



}
