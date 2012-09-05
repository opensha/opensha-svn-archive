package org.opensha.nshmp2.tmp;

import static org.opensha.nshmp2.util.Period.*;

import java.util.Map;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.source.PointSource;
import org.opensha.nshmp2.imr.GridIMR;
import org.opensha.nshmp2.imr.NSHMP08_SUB_Slab;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.imr.NSHMP08_WUS_Grid;
import org.opensha.nshmp2.util.FocalMech;
import org.opensha.nshmp2.util.NSHMP_IMR_Util;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.EqkRuptureParams.DipParam;
import org.opensha.sha.imr.param.EqkRuptureParams.FaultTypeParam;
import org.opensha.sha.imr.param.EqkRuptureParams.MagParam;
import org.opensha.sha.imr.param.EqkRuptureParams.RupTopDepthParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.PropagationEffectParams.DistRupMinusJB_OverRupParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceJBParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SingleMagFreqDist;

import com.google.common.collect.Maps;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class TestIMRs {

	
	public static void main(String[] args) {
		
//		System.out.println(NSHMP_Util.getAvgHW_CY(6.65, 10.5, 0.0));
		
		double m = 6.65;
		double r = 10.5; //0.5 10.5 100.5
		Period per = GM0P00;
		String ft;
		boolean hybrid = true;
		
		ft = CB_2008_AttenRel.FLT_TYPE_REVERSE;
		DiscretizedFunc f1 = testIMR(m, r, per,ft, hybrid);
		
		System.out.println(f1);
	}
	
	private static DiscretizedFunc testIMR(double m, double r, Period p, String ft, boolean gridHybrid) {
		
		// WUS/CA test
		double dH = r;
		double dV = (m < 6.5) ? 5.0 : 1.0;
		NSHMP08_WUS_Grid imr = new NSHMP08_WUS_Grid();
		imr.setIntensityMeasure((p == GM0P00) ? PGA_Param.NAME : SA_Param.NAME);
		if (p != GM0P00) {
			imr.getParameter(PeriodParam.NAME).setValue(p.getValue());
		}
		imr.getParameter(MagParam.NAME).setValue(m);
		imr.getParameter(FaultTypeParam.NAME).setValue(ft);
		imr.setGridHybrid(gridHybrid);
		imr.getParameter(RupTopDepthParam.NAME).setValue(dV);
		imr.setSite(NEHRP_TestCity.LOS_ANGELES.getSite());
		
		// because eqrup is never set directly, setPropEffect is never called
		// in NHSMP08_WUS so we must set d1p0kmps directly in CY. (The null value
		// in the Site object is not transferred). We got lucky
		// with the default 1.0km value for CB; setting it explicitely to 
		// ensure consistency with NSHMP grid src CB
//		imr.getParameter(DepthTo1pt0kmPerSecParam.NAME).setValue(null);
//		imr.getParameter(DepthTo2pt5kmPerSecParam.NAME).setValue(null);
		
		// distance params
		imr.getParameter(DistanceJBParameter.NAME).setValue(dH);
		double rRup = Math.sqrt(dH * dH + dV * dV);
		imr.getParameter(DistanceRupParameter.NAME).setValue(rRup);
		double rOver = (rRup == 0.0) ? 0.0 : (rRup - dH) / rRup;
		imr.getParameter(DistRupMinusJB_OverRupParameter.NAME).setValue(rOver);

//		// SUB test
//		double dH = r;
//		double dV = 50;
//		NSHMP08_SUB_Slab imr = new NSHMP08_SUB_Slab();
//		imr.setIntensityMeasure((p == GM0P00) ? PGA_Param.NAME : SA_Param.NAME);
//		imr.getParameter(PeriodParam.NAME).setValue(p.getValue());
//		imr.getParameter(MagParam.NAME).setValue(m);
//		imr.getParameter(RupTopDepthParam.NAME).setValue(dV);
//		imr.setSite(NEHRP_TestCity.SEATTLE.getSite());
//				
//		// distance params
//		double rRup = Math.sqrt(dH * dH + dV * dV);
//		imr.getParameter(DistanceRupParameter.NAME).setValue(rRup);
////		imr.getParameter(DistanceJBParameter.NAME).setValue(dH);
		
		DiscretizedFunc f = ((GridIMR) imr).getExceedProbFromParent(p.getFunction());
		return f;
	}
	

}
