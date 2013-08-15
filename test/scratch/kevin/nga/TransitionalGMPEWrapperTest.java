package scratch.kevin.nga;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.DevStatus;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

import com.google.common.collect.Lists;

import scratch.peter.newcalc.ScalarGroundMotion;
import scratch.peter.nga.IMT;
import scratch.peter.nga.TransitionalGMPE;

/**
 * This tests that the GMPE wrapper is working correctly in setting paremters
 * @author kevin
 *
 */
@RunWith(Parameterized.class)
public class TransitionalGMPEWrapperTest {
	
	private TransitionalGMPEWrapper wrapper;
	private TransitionalGMPE gmpe;
	
	private static ERF wrapper_erf;
	private static ERF gmpe_erf;
	
	private static GriddedRegion sitesRegion;
	
	private Site wrapper_site;
	private Site gmpe_site;
	
	private static final double tol = 1e-10;
	
	public TransitionalGMPEWrapperTest(AttenRelRef ref) {
		wrapper = (TransitionalGMPEWrapper)ref.instance(null);
		wrapper.setParamDefaults();
		gmpe = wrapper.getGMPE();
		
		wrapper_site = new Site();
		wrapper_site.addParameterList(wrapper.getSiteParams());
		wrapper.setSite(wrapper_site);
		gmpe_site = new Site();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		wrapper_erf = new Frankel96_AdjustableEqkRupForecast();
		wrapper_erf.updateForecast();
		gmpe_erf = new Frankel96_AdjustableEqkRupForecast();
		gmpe_erf.updateForecast();
		
		sitesRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED(1d);
	}
	
	@Parameters
	public static Collection<AttenRelRef[]> data() {
		ArrayList<AttenRelRef[]> ret = new ArrayList<AttenRelRef[]>();
		for (AttenRelRef imr : AttenRelRef.values()) {
			if (!TransitionalGMPEWrapper.class.isAssignableFrom(imr.getAttenRelClass()))
				continue;
			AttenRelRef[] array = { imr };
			ret.add(array);
		}
		System.out.println("GMPE wrappers to test: "+ret.size());
		return ret;
	}

	@Test
	public void test() {
		Random r = new Random();
		
		List<IMT> imts = Lists.newArrayList(gmpe.getSupportedIMTs());
		
		for (int sourceID=0; sourceID<wrapper_erf.getNumSources(); sourceID++) {
			for (int rupID=0; rupID<wrapper_erf.getNumRuptures(sourceID); rupID++) {
				ProbEqkRupture wrapper_rup = wrapper_erf.getRupture(sourceID, rupID);
				ProbEqkRupture gmpe_rup = gmpe_erf.getRupture(sourceID, rupID);
				
				// choose IMT at random
				IMT imt = imts.get(r.nextInt(imts.size()));
				// choose site at random
				Location loc = sitesRegion.getLocation(r.nextInt(sitesRegion.getNodeCount()));
				wrapper_site.setLocation(loc);
				gmpe_site.setLocation(loc);
				
				// site params
				Double vs30 = r.nextDouble()*800d+200d;
				Double z10 = r.nextDouble()*3000d;
				Double z25 = r.nextDouble()*5d;
				if (r.nextDouble() < 0.3)
					z10 = null;
				if (r.nextDouble() < 0.3)
					z25 = null;
				boolean vsInferred = r.nextBoolean();
				
				wrapper_site.setValue(Vs30_Param.NAME, vs30);
				wrapper_site.setValue(DepthTo1pt0kmPerSecParam.NAME, z10);
				wrapper_site.setValue(DepthTo2pt5kmPerSecParam.NAME, z25);
				if (vsInferred)
					wrapper_site.setValue(Vs30_TypeParam.NAME, Vs30_TypeParam.VS30_TYPE_INFERRED);
				else
					wrapper_site.setValue(Vs30_TypeParam.NAME, Vs30_TypeParam.VS30_TYPE_MEASURED);
				
				// set IMT
				if (imt.getPeriod() == null) {
					wrapper.setIntensityMeasure(imt.name());
				} else {
					wrapper.setIntensityMeasure(SA_Param.NAME);
					SA_Param.setPeriodInSA_Param(wrapper.getIntensityMeasure(), imt.getPeriod());
				}
				gmpe.set_IMT(imt);
				
				// set values in wrapper a random way
				int set_type = r.nextInt(3);
				switch (set_type) {
				case 0:
					wrapper.setSite(wrapper_site);
					wrapper.setEqkRupture(wrapper_rup);
					break;
				case 1:
					wrapper.setSiteLocation(loc);
					wrapper.setEqkRupture(wrapper_rup);
					break;
				case 2:
					wrapper.setAll(wrapper_rup, wrapper_site, wrapper.getIntensityMeasure());
					break;

				default:
					throw new IllegalStateException();
				}
				
				// set values in GMPE
				gmpe.set_IMT(imt);
				
				RuptureSurface surf = gmpe_rup.getRuptureSurface();
				
				gmpe.set_Mw(gmpe_rup.getMag());
				
				gmpe.set_rJB(surf.getDistanceJB(loc));
				gmpe.set_rRup(surf.getDistanceRup(loc));
				gmpe.set_rX(surf.getDistanceX(loc));
				
				gmpe.set_dip(surf.getAveDip());
				gmpe.set_width(surf.getAveWidth());
				gmpe.set_zTop(surf.getAveRupTopDepth());
				if (gmpe_rup.getHypocenterLocation() != null)
					gmpe.set_zHyp(gmpe_rup.getHypocenterLocation().getDepth());
				else
					gmpe.set_zHyp(Double.NaN);

				gmpe.set_vs30(vs30);
				gmpe.set_vsInf(vsInferred);
				if (z25 == null)
					gmpe.set_z2p5(Double.NaN);
				else
					gmpe.set_z2p5(z25);
				if (z10 == null)
					gmpe.set_z1p0(Double.NaN);
				else
					gmpe.set_z1p0(z10);
				
				gmpe.set_fault(TransitionalGMPEWrapper.getFaultStyle(gmpe_rup.getAveRake()));
				
				double wrapper_mean = wrapper.getMean();
				double wrapper_std_dev = wrapper.getStdDev();
				
				ScalarGroundMotion gm = gmpe.calc();
				assertEquals("Mean error for rup "+sourceID+","+rupID+", set_type="+set_type,
						gm.mean(), wrapper_mean, tol);
				assertEquals("Std. dev. error for rup "+sourceID+","+rupID+", set_type="+set_type,
						gm.stdDev(), wrapper_std_dev, tol);
			}
		}
	}

}
