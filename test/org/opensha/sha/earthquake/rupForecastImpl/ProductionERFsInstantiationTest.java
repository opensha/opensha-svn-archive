package org.opensha.sha.earthquake.rupForecastImpl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.DevStatus;
import org.opensha.sha.earthquake.BaseERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ERF_Ref;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast;
import org.opensha.sha.faultSurface.AbstractEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.util.TectonicRegionType;

/**
 * Tests the following criteria for each {@link DevStatus}.PRODUCTION ERF.
 * 
 * <ul>
 * <li> ERF instantiation is not null
 * <li> updateForecast() returns successfully
 * </ul>
 * 
 * If the erf is a regular {@link ERF} the following criteria is checked:
 * <ul>
 * <li> has at least 1 source
 * <li> each source is not null
 * <li> each source has a non null source surface with at least 1 point
 * (exceptions are allowed on getSourceSurface if it is a point source)
 * <li> each source has a non null name
 * <li> each source has a non null {@link TectonicRegionType}
 * <li> each source has at least 1 rupture
 * <li> each rupture is not null
 * <li> each rupture's surface is not null and has at least 1 point
 * <li> each rupture's magnitude and probability are not NaN
 * <li> each rupture's magnitude is within 0<mag<=12
 * <li> each rupture's probability is within 0<=prob<=1
 * </ul>
 * 
 * If the erf is an {@link EpistemicListERF} the following criteria is checked:
 * <ul>
 * <li> has at least one ERF
 * <li> each ERF conforms to the critera above for regular ERFs.
 * </ul>
 * 
 * @author Kevin
 *
 */
@RunWith(Parameterized.class)
public class ProductionERFsInstantiationTest {
	
	@Parameters
	public static Collection<ERF_Ref[]> data() {
		Set<ERF_Ref> set = ERF_Ref.get(false, true, DevStatus.PRODUCTION);
		ArrayList<ERF_Ref[]> ret = new ArrayList<ERF_Ref[]>();
		for (ERF_Ref erf : set) {
			ERF_Ref[] array = { erf };
			ret.add(array);
		}
		return ret;
	}
	
	private ERF_Ref erfRef;
	
	public ProductionERFsInstantiationTest(ERF_Ref erfRef) {
		this.erfRef = erfRef;
	}
	
	private void validateRupture(int sourceID, int rupID, ProbEqkRupture rupture) {
		String rupStr = erfRef+": source "+sourceID+", rup "+rupID;
		assertNotNull(rupStr+" is null!", rupture);
		RuptureSurface surface = rupture.getRuptureSurface();
		assertNotNull(rupStr+" surface is null!", surface);
		assertTrue(rupStr+" surface has zero points!", surface.getEvenlyDiscritizedListOfLocsOnSurface().size()>0l);
		double prob = rupture.getProbability();
		assertFalse(rupStr+" probability is NaN", Double.isNaN(prob));
		assertTrue(rupStr+" probability is <0", prob>=0d);
		assertTrue(rupStr+" probability is >1", prob<=1d);
		double mag = rupture.getMag();
		assertFalse(rupStr+" magnitude is NaN", Double.isNaN(mag));
		assertTrue(rupStr+" magnitude is <=0", mag>0d);
		assertTrue(rupStr+" magnitude is >12", mag<=12d);
	}
	
	private void validateSourceSurface(String srcStr, ProbEqkSource source) {
		try {
			RuptureSurface sourceSurface = source.getSourceSurface();
			assertNotNull(srcStr+" surface is null!", sourceSurface);
			assertTrue(srcStr+" surface has zero points!", sourceSurface.getEvenlyDiscritizedListOfLocsOnSurface().size()>0l);
		} catch (RuntimeException e) {
			// if there was an error, only throw it if not a point source
			// dirty, I know
			if (ClassUtils.getClassNameWithoutPackage(source.getClass()).toLowerCase().contains("point"))
				return;
			ProbEqkRupture rup1 = source.getRupture(0);
			if (rup1.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size() != 1l)
				throw e;
		}
	}
	
	private void validateSource(int sourceID, ProbEqkSource source) {
		String srcStr = erfRef+": source "+sourceID;
		assertNotNull(srcStr+" is null!", source);
		assertNotNull(srcStr+" name is null!", source.getName());
		srcStr += " ("+source.getName()+")";
		assertTrue(srcStr+" has no ruptures!", source.getNumRuptures() > 0);
		validateSourceSurface(srcStr, source);
		assertNotNull(srcStr+" tectonic region type is null!", source.getTectonicRegionType());
		for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
			ProbEqkRupture rupture = source.getRupture(rupID);
			validateRupture(sourceID, rupID, rupture);
		}
	}
	
	private void validateERF(ERF erf) {
		assertTrue("ERF "+erf.getName()+" has no sources!", erf.getNumSources() > 0);
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			validateSource(sourceID, source);
		}
	}
	
	@Test
	public void testInstantiation() {
		BaseERF baseERF = erfRef.instance();
		assertNotNull("ERF "+baseERF.getName()+" is null!", baseERF);
		
		// several PEER and simple ERFs require user input without which
		// update forecast will throw an exception.
		if (ERFsToSkip.contains(erfRef.toString())) return;
		
		baseERF.updateForecast();
		if (baseERF instanceof ERF) {
			ERF erf = (ERF)baseERF;
			validateERF(erf);
		} else if (baseERF instanceof EpistemicListERF) {
			EpistemicListERF listERF = (EpistemicListERF)baseERF;
			assertTrue(erfRef+" is epistemic, but contains zero ERFs!", listERF.getNumERFs()>0);
			int numERFs = listERF.getNumERFs();
			if (numERFs < 10) {
				for (int erfID=0; erfID<numERFs; erfID++) {
					ERF erf = listERF.getERF(erfID);
					validateERF(erf);
				}
			} else {
				// if there are more than 5 ERFs it will take too long to do the test, so just use 5 random ones
				Random r = new Random(System.currentTimeMillis());
				for (int i=0; i<5; i++) {
					int erfID = r.nextInt(numERFs);
					validateERF(listERF.getERF(erfID));
				}
			}
		}
	}
	
	private static List<String> ERFsToSkip;
	static {
		ERFsToSkip = new ArrayList<String>();
		ERFsToSkip.add(PEER_AreaForecast.NAME);
		ERFsToSkip.add(PEER_MultiSourceForecast.NAME);
		ERFsToSkip.add(FloatingPoissonFaultERF.NAME);
		ERFsToSkip.add(PoissonFaultERF.NAME);
	}

}
