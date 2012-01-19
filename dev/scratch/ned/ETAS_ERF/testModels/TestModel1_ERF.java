package scratch.ned.ETAS_ERF.testModels;

import java.util.ArrayList;

import org.opensha.commons.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import java.awt.Toolkit;


import scratch.UCERF3.erf.FaultSystemSolutionTimeDepERF;
import scratch.UCERF3.utils.ModUCERF2.NSHMP_GridSourceGeneratorMod2;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class TestModel1_ERF extends FaultSystemSolutionTimeDepERF {
	
	final static boolean D=true;

	GriddedRegion griddedRegion;
	
	ArbIncrementalMagFreqDist  onFaultPointMFD, offFaultPointMFD;
	
	ArrayList<Integer> locIndicesOnFault;
	
	WC1994_MagLengthRelationship magLengthRel;
	
	public TestModel1_ERF() {
		super(new TestModel1_FSS());
		
		griddedRegion = new GriddedRegion(new Location(35,240-360), new Location(37,244-360), 0.05, 0.05, null);
		
		numOtherSources = griddedRegion.getNumLocations();
//		numOtherSources=0;
		System.out.println("numOtherSources="+numOtherSources);
		
		magLengthRel = new WC1994_MagLengthRelationship();
		
		// get the point sources that are on the fault
		LocationList pointLocsOnFault = ((TestModel1_FSS)faultSysSolution).getPointLocsOnFault();
		locIndicesOnFault = new ArrayList<Integer>();
		for(Location loc : pointLocsOnFault){
			int index = griddedRegion.indexForLocation(loc);
			locIndicesOnFault.add(index);
//			Location testLoc = griddedRegion.getLocation(index);
//			System.out.println(loc+"\t"+testLoc);
		}
		

		// the following is the target MFD (down to M 2.5)
		GutenbergRichterMagFreqDist  targetFaultGR = ((TestModel1_FSS)faultSysSolution).getTargetFaultGR();
		// the following is the MFD for the fault (seismogenic and larger)
		ArbIncrementalMagFreqDist faultGR = ((TestModel1_FSS)faultSysSolution).getFaultGR();
		
		double offFaultSeisReductionFactor = 1;
//		double offFaultSeisReductionFactor = 10;
		int numPtsOnFault = pointLocsOnFault.size();
		if(D) System.out.println("numPtsOnFault+"+numPtsOnFault);
		onFaultPointMFD  = new ArbIncrementalMagFreqDist(2.55, 6.05, 36);
		for(int i=0; i<onFaultPointMFD.getNum();i++)
			onFaultPointMFD.set(i,targetFaultGR.getY(i)/numPtsOnFault);
		// make point off fault 1/10th rate of those on the faults
		offFaultPointMFD = new ArbIncrementalMagFreqDist(2.55, 7.55, 51);
		for(int i=0; i<offFaultPointMFD.getNum();i++)
			offFaultPointMFD.set(i,targetFaultGR.getY(i)/(numPtsOnFault*offFaultSeisReductionFactor));
		
		
		if(D) {
			ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
			funcs.add(faultGR);
			faultGR.setName("faultGR");
			faultGR.setInfo(" ");		
			funcs.add(faultGR.getCumRateDistWithOffset());
			funcs.add(targetFaultGR);
			targetFaultGR.setName("targetFaultGR");
			targetFaultGR.setInfo(" ");		
			funcs.add(targetFaultGR.getCumRateDistWithOffset());
			funcs.add(onFaultPointMFD);
			onFaultPointMFD.setName("onFaultPointMFD");
			onFaultPointMFD.setInfo(" ");		
			funcs.add(onFaultPointMFD.getCumRateDistWithOffset());
			funcs.add(offFaultPointMFD);
			offFaultPointMFD.setName("offFaultPointMFD");
			offFaultPointMFD.setInfo(" ");		
			funcs.add(offFaultPointMFD.getCumRateDistWithOffset());
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, ""); 			
		}


	}
	
	public GriddedRegion getGriddedRegion() {
		return griddedRegion;
	}
	
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		int regionIndex = iSource-numFaultSystemSources;
		ArbIncrementalMagFreqDist mfd;
		if(locIndicesOnFault.contains(regionIndex))
			mfd = onFaultPointMFD;
		else
			mfd = offFaultPointMFD;
		double magCutOff =8.0; // all rups below are treated a point sources
		boolean isCrossHair=true;
		return new Point2Vert_FaultPoisSource(griddedRegion.getLocation(regionIndex), mfd,
				magLengthRel,timeSpan.getDuration(), magCutOff ,1.0, 0.0,0.0, isCrossHair);
	}


	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestModel1_ERF erf = new TestModel1_ERF();
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
//		erf.aleatoryMagAreaStdDevParam.setValue(0.0);
//		erf.bpt_AperiodicityParam.setValue(0.2);
		erf.getTimeSpan().setStartTimeInMillis(0);
		erf.getTimeSpan().setDuration(1);
		long runtime = System.currentTimeMillis();
		
//		System.exit(0);

		// update forecast to we can get a main shock
		System.out.println("Running updateForecast()");
		erf.updateForecast();
		
		int nthRup = 892;	// same as source index
		ProbEqkRupture mainshock = erf.getNthRupture(nthRup);		
		ObsEqkRupture obsMainShock = new ObsEqkRupture();
		obsMainShock.setAveRake(mainshock.getAveRake());
		obsMainShock.setMag(mainshock.getMag());
		obsMainShock.setRuptureSurface(mainshock.getRuptureSurface());
		obsMainShock.setOriginTime(0);	// occurs at 1970
		System.out.println("main shock: nthRup="+nthRup+"; mag="+obsMainShock.getMag()+
				"; src name: " +erf.getSource(nthRup).getName());

		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(obsMainShock);
		
		erf.setRuptureOccurrence(nthRup, 0);
		
		erf.testETAS_Simulation(erf.getGriddedRegion(), obsEqkRuptureList);

//		erf.testER_Simulation();
		runtime -= System.currentTimeMillis();
		System.out.println("simulation took "+(double)runtime/(1000.0*60.0)+" minutes");
	}
}
