package scratch.ned.ETAS_Tests.MeanUCERF2;

/**
 * This simply overrides nshmp_gridSrcGen of parent with one that goes down to M 2.5 for background seismicity
 * 
 * @author field
 *
 */
public class MeanUCERF2_ETAS extends org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2 {
	
	public MeanUCERF2_ETAS() {
		nshmp_gridSrcGen = new NSHMP_GridSourceGeneratorMod();
	}
}
