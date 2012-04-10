package scratch.ned.ETAS_ERF.testModels;

import java.util.ArrayList;

import org.opensha.commons.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;


import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.erf.FaultSystemSolutionTimeDepERF;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.ModUCERF2.NSHMP_GridSourceGeneratorMod2;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class TestModel1_ERF extends FaultSystemSolutionTimeDepERF {
	
	final static boolean D=true;

	GriddedRegion griddedRegion;
	
	double minGridLat=35.0;
	double maxGridLat=37.0;
	double minGridLon=240-360;
	double maxGridLon=244-360;
	double gridSpacing=0.05;
	
	ArbIncrementalMagFreqDist  onFaultPointMFD, offFaultPointMFD;
	
	ArrayList<Integer> locIndicesOnFault;
	
	WC1994_MagLengthRelationship magLengthRel;
	
	public TestModel1_ERF() {
		super(new TestModel1_FSS());
		
		griddedRegion = new GriddedRegion(new Location(minGridLat,minGridLon), new Location(maxGridLat,maxGridLon), gridSpacing, gridSpacing, null);
		
		numOtherSources = griddedRegion.getNumLocations();
//		numOtherSources=0;
		System.out.println("numOtherSources="+numOtherSources);
//		System.out.println(griddedRegion.getLocation(0));
//		System.out.println(griddedRegion.getLocation(1));
		
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
		
//		double offFaultSeisReductionFactor = 1;
		double offFaultSeisReductionFactor = 10;
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


	
	private void makeNucleationMap() throws IOException {
		
		String scaleLabel="TestMod1 Nucl";
		String metadata=""; 
		String dirName="TestMod1 Nucl"; 
		
		GMT_MapGenerator gmt_MapGenerator = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME, minGridLat);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME, minGridLon);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME, maxGridLat);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME, maxGridLon);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, gridSpacing);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, -6.0);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, 2.0);
		
		// must set this parameter this way because the setValue(CPT) method takes a CPT object, and it must be the
		// exact same object as in the constraint (same instance); the setValue(String) method was added for convenience
		// but it won't succeed for the isAllowed(value) call.
		CPTParameter cptParam = (CPTParameter )gmt_MapGenerator.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());

		
		File GMT_DIR = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "GMT");
		
		GriddedGeoDataSet geoDataSet = ERF_Calculator.getNucleationRatesInRegion(this, griddedRegion, 6.0, 10.0);

		try {
				if (!GMT_DIR.exists())
					GMT_DIR.mkdir();
				String url = gmt_MapGenerator.makeMapUsingServlet(geoDataSet, scaleLabel, metadata, null);
				metadata += GMT_MapGuiBean.getClickHereHTML(gmt_MapGenerator.getGMTFilesWebAddress());
				File downloadDir = new File(GMT_DIR, dirName);
				if (!downloadDir.exists())
					downloadDir.mkdir();
				File zipFile = new File(downloadDir, "allFiles.zip");
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, downloadDir);
				
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		
		// update forecast
		erf.updateForecast();
		
		// print the nucleation rate map
//		try {
//			erf.makeNucleationMap();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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
		
		erf.testETAS_Simulation(erf.getGriddedRegion(), obsEqkRuptureList,false, false, false);

//		erf.testER_Simulation();
		runtime -= System.currentTimeMillis();
		System.out.println("simulation took "+(double)runtime/(1000.0*60.0)+" minutes");
	}
}
