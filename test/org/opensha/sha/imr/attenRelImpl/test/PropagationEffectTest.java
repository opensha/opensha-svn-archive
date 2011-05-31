package org.opensha.sha.imr.attenRelImpl.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Site;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.DataUtils;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.imr.PropagationEffect;
import org.opensha.sha.imr.param.PropagationEffectParams.AbstractDoublePropEffectParam;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceJBParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceSeisParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceX_Parameter;
import org.opensha.sha.imr.param.PropagationEffectParams.PropagationEffectParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.WarningDoublePropagationEffectParameter;

public class PropagationEffectTest {
	
	private static AbstractERF erf;
	private static Site site;
	
	private static double pdiff_max = 0.05;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		erf = new Frankel96_AdjustableEqkRupForecast();
		erf.updateForecast();
		
		site = new Site(new Location(34, -118));
	}
	
	@Test
	public void testDistJB() {
		doTest(new PropagationEffect(), new DistanceJBParameter());
	}
	
	@Test
	public void testDistRup() {
		doTest(new PropagationEffect(), new DistanceRupParameter());
	}
	
	@Test
	public void testDistSeis() {
		doTest(new PropagationEffect(), new DistanceSeisParameter());
	}
	
	@Test
	public void testDistX() {
		doTest(new PropagationEffect(), new DistanceX_Parameter());
	}
	
	private void doTest(PropagationEffect propEffect, AbstractDoublePropEffectParam param) {
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				
				propEffect.setAll(rup, site);
				param.setValue(rup, site);
				
				double paramVal = param.getValue();
				double propEffectVal = (Double)propEffect.getParamValue(param.getName());
				
				double pDiff = DataUtils.getPercentDiff(propEffectVal, paramVal);
				
				String message = param.getName() + " not equal! param val: " + paramVal
						+ ", prop effect val: " + propEffectVal + ", pDiff: " + pDiff + " %";
				assertTrue(message, pDiff < pdiff_max);
			}
		}
	}

}
