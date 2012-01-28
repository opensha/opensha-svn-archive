package scratch.UCERF3.erf;

import java.util.ArrayList;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;

import java.awt.Toolkit;


import scratch.UCERF3.utils.ModUCERF2.NSHMP_GridSourceGeneratorMod2;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class UCERF2_FaultSysSol_ERF extends FaultSystemSolutionTimeDepERF {

	NSHMP_GridSourceGenerator nshmp_gridSrcGen;
	
	
	public UCERF2_FaultSysSol_ERF() {
		super("/Users/field/ALLCAL_UCERF2.zip");
		nshmp_gridSrcGen = new NSHMP_GridSourceGeneratorMod2();
		numOtherSources = nshmp_gridSrcGen.getNumSources();
//		numOtherSources=0;
		// treat as point sources
		nshmp_gridSrcGen.setAsPointSources(true);
		System.out.println("numOtherSources="+numOtherSources);

	}
	
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		return nshmp_gridSrcGen.getCrosshairGriddedSource(iSource - numFaultSystemSources, timeSpan.getDuration());
	}


	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
		erf.aleatoryMagAreaStdDevParam.setValue(0.0);
		erf.bpt_AperiodicityParam.setValue(0.2);
		erf.getTimeSpan().setStartTimeInMillis(0);
		erf.getTimeSpan().setDuration(1);
		long runtime = System.currentTimeMillis();
		// make the gridded region
//		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
//		System.out.println("Location(37.00000, -119.30000, 0.00000):\t"+griddedRegion.indexForLocation(new Location(37.00000, -119.30000, 0.00000)));
//		System.out.println("Location(37.10000, -119.30000, 0.00000):\t"+griddedRegion.indexForLocation(new Location(37.10000, -119.30000, 0.00000)));
//		System.out.println("Location(37.00000, -119.40000, 0.00000):\t"+griddedRegion.indexForLocation(new Location(37.00000, -119.4000, 0.00000)));
//		System.out.println("Location(37.10000, -119.40000, 0.00000):\t"+griddedRegion.indexForLocation(new Location(37.10000, -119.4000, 0.00000)));
//		System.out.println("Location(37.05000, -119.35000, 0.00000):\t"+griddedRegion.indexForLocation(new Location(37.10000, -119.4000, 0.00000)));
//		System.exit(0);
		
		// update forecast to we can get a main shock
		erf.updateForecast();
		
		// get the rupture index of a Landers rupture
		int nthRup = erf.getIndexN_ForSrcAndRupIndices(4755, 0);
		ProbEqkRupture landers = erf.getSource(4755).getRupture(0);
		ObsEqkRupture landersObs = new ObsEqkRupture();
		landersObs.setAveRake(landers.getAveRake());
		landersObs.setMag(landers.getMag());
		Location surfLoc = landers.getRuptureSurface().getFirstLocOnUpperEdge().clone();
		Location ptSurf = new Location(surfLoc.getLatitude(),surfLoc.getLongitude(),0.0);
		landersObs.setPointSurface(ptSurf);
//		landersObs.setRuptureSurface(landers.getRuptureSurface());
		landersObs.setOriginTime(0);	// occurs at 1970
//		landersObs.setMag(7);
		System.out.println("main shock: s=4755, r=0, nthRup="+nthRup+"mag="+landersObs.getMag()+
				"; src name: " +erf.getSource(4755).getName());
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(landersObs);
		
		erf.setRuptureOccurrence(nthRup, 0);
		
		erf.testETAS_Simulation(griddedRegion, obsEqkRuptureList);
//		erf.testETAS_SimulationOld(griddedRegion, obsEqkRuptureList);

//		erf.testER_Simulation();
		runtime -= System.currentTimeMillis();
		System.out.println("simulation took "+(double)runtime/(1000.0*60.0)+" minutes");


	}

}
