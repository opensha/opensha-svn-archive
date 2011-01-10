package scratch.ned.ETAS_Tests;

import java.io.FileWriter;
import java.util.ArrayList;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;

import scratch.ned.ETAS_Tests.MeanUCERF2.MeanUCERF2_ETAS;

public class ETAS_Simulator {
	
	ArrayList<EqksInGeoBlock> blockList;
	GriddedRegion griddedRegion;
	EqkRupForecast erf;
	ETAS_Utils etasUtils;
	double distDecay=1.4;
	double minDist=2.0;
	double tMin=0;
	double tMax=360;
	boolean useAdaptiveBlocks=true;
	boolean includeBlockRates=true;

	
	
	public ETAS_Simulator(EqkRupForecast erf,GriddedRegion griddedRegion) {
		this.erf = erf;
		this.griddedRegion = griddedRegion;
		etasUtils = new ETAS_Utils();
		makeAllEqksInGeoBlocks();
				
	}
	
	/**
	 * This returns a list of randomly sampled primary aftershocks
	 * @param parentRup
	 * @param blockList
	 * @param erf
	 * @param distDecay
	 * @param minDist
	 * @param useAdaptiveBlocks
	 * @param includeBlockRates
	 * @param tMin
	 * @param tMax
	 * @return
	 */
	public ArrayList<PrimaryAftershock> getPrimaryAftershocksList(EqkRupture parentRup) {
		
		// This makes the original MFD from the ERF
		ArbIncrementalMagFreqDist origMagDist = new ArbIncrementalMagFreqDist(2.05, 8.95, 70);
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			for(int r=0;r<src.getNumRuptures();r++) {
				ProbEqkRupture rup = src.getRupture(r);
				origMagDist.addResampledMagRate(rup.getMag(), rup.getMeanAnnualRate(duration), true);
			}			
		}
		origMagDist.scaleToCumRate(2.05, 1);
		

		ArrayList<PrimaryAftershock> aftershockList = null;
		ETAS_PrimaryEventSampler ETAS_sampler = new ETAS_PrimaryEventSampler(parentRup,blockList, erf, distDecay,minDist, useAdaptiveBlocks, includeBlockRates);
		ArbIncrementalMagFreqDist magProbDist = ETAS_sampler.getMagProbDist();
//		System.out.println(magProbDist);
//		ETAS_sampler.writeRelBlockProbToFile();
		System.out.println(parentRup.getMag()+"\t"+tMin+"\t"+tMax);
		double expectedNum = etasUtils.getDefaultExpectedNumEvents(parentRup.getMag(), tMin, tMax);
		int numAftershocks = etasUtils.getPoissonRandomNumber(expectedNum);
		System.out.println("Expected num = "+expectedNum+"\tSampled num = "+numAftershocks);
		
		//
		ArbIncrementalMagFreqDist magDist = new ArbIncrementalMagFreqDist(2.05, 8.95, 70);
		System.out.print("\n");
//		for(int j=0; j<50; j++) {
//			System.out.print(j+", ");
			aftershockList = new ArrayList<PrimaryAftershock>();
			for(int i=0; i<numAftershocks;i++) {
				aftershockList.add(ETAS_sampler.samplePrimaryAftershock(etasUtils.getDefaultRandomTimeOfEvent(tMin, tMax)));
			}
			for(PrimaryAftershock event : aftershockList)
				magDist.addResampledMagRate(event.getMag(), 1.0, true);			
//		}
		System.out.print("\n");
		magDist.scaleToCumRate(2.05, 1);
//		System.out.println(magDist);

		/**/
		try{
			FileWriter fw1 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/aftershockList.txt");
			fw1.write("mag\ttime\tdist\tblockID\n");
			for(PrimaryAftershock event: aftershockList) {
				double dist = LocationUtils.distanceToSurfFast(event.getHypocenterLocation(), parentRup.getRuptureSurface());
				fw1.write((float)event.getMag()+"\t"+(float)event.getOriginTime()+"\t"+(float)dist+"\t"+event.getParentID()+
						"\t"+event.getSourceIndex()+"\t"+event.getRupIndex()+"\n");
			}
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		ArrayList funcs = new ArrayList();
		origMagDist.setName("from ERF");
		magProbDist.setName("from ETAS_PrimaryEventSampler");
		magDist.setName("from random samples");
		funcs.add(origMagDist);
		funcs.add(magProbDist);
		funcs.add(magDist);
//		funcs.add(ETAS_sampler.getRevisedBlockList().get(5880).getMagProbDist());
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(funcs, ""); 
		
		
		// plot expected number greater than mag
		EvenlyDiscretizedFunc expCumDist = magProbDist.getCumRateDist();
		expCumDist.multiplyY_ValsBy(expectedNum);
		ArbIncrementalMagFreqDist magProbDistForSrc = ETAS_sampler.getMagProbDistForSource(195);
		EvenlyDiscretizedFunc expCumDistForSource = magProbDistForSrc.getCumRateDist();
		expCumDistForSource.multiplyY_ValsBy(expectedNum);

		double days = tMax-tMin;
		expCumDist.setInfo("Expected num greater then M in "+(float)days+" days");
		expCumDistForSource.setInfo("Expected num greater then M in "+(float)days+" days for source");
		ArrayList funcs2 = new ArrayList();
		funcs2.add(expCumDist);
		funcs2.add(expCumDistForSource);
		GraphiWindowAPI_Impl sr_graph2 = new GraphiWindowAPI_Impl(funcs2, "Expected Number of Events"); 

/*		
		CodeTests tests = new CodeTests();
		boolean testResult = tests.testIntegerPDF_FunctionSampler(ETAS_sampler.getRevisedBlockList().get(5880).getRandomSampler());
		System.out.println("test passed = "+testResult);
		System.out.println("Results for block 5880");
		ETAS_sampler.getRevisedBlockList().get(5880).writeResults();
*/
		
		return aftershockList;
	}
	
	
	
	public void testDistanceDecays(boolean adaptiveBlocks, boolean includeBlockRates) {
		
		ETAS_PrimaryEventSampler sampler;
		
		//  Point source rupture:
		ProbEqkRupture rup = new ProbEqkRupture();
		rup.setMag(5);
		/**/
		// point source at edge of sub-blocks
		rup.setPointSurface(new Location(34,-118,8));
		sampler = new ETAS_PrimaryEventSampler(rup,blockList, erf, distDecay, minDist, adaptiveBlocks, includeBlockRates);
		sampler.testRandomDistanceDecay("M5 Point Src at 34,-118,8");
		sampler.plotBlockProbMap();

	/*	
		// point source in midpoint of sub block
		rup.setPointSurface(new Location(34.00625,-118.00625,7));
		sampler = new ETAS_PrimaryEventSampler(rup,blockList, erf, distDecay, minDist, adaptiveBlocks, includeBlockRates);
		sampler.testRandomDistanceDecay("M5 Point Src at 34.00625,-118.00625,7");

		// point source in center of sub block
		rup.setPointSurface(new Location(34.0125,	-118.0125,	6.0));
		sampler = new ETAS_PrimaryEventSampler(rup,blockList, erf, distDecay, minDist, adaptiveBlocks, includeBlockRates);
		sampler.testRandomDistanceDecay("M5 Point Src at 34.0125,-118.0125,6.0");
		

		// 68	S. San Andreas;CH+CC+BB+NM+SM+NSB+SSB+BG+CO	 #rups=8
		rup = erf.getSource(68).getRupture(0);
		sampler = new ETAS_PrimaryEventSampler(rup,blockList, erf, distDecay, minDist, adaptiveBlocks, includeBlockRates);
		sampler.testRandomDistanceDecay("SSAF Wall-to-wall Rupture; M="+rup.getMag());
		sampler.plotBlockProbMap();

		// 236	Pitas Point (Lower, West)	 #rups=19	13.0 (shallowest dipping rupture I could find)
		rup = erf.getSource(236).getRupture(0);
		sampler = new ETAS_PrimaryEventSampler(rup,blockList, erf, distDecay, minDist, adaptiveBlocks, includeBlockRates);
		sampler.testRandomDistanceDecay("Pitas Point (shallowest dipping source); M="+rup.getMag()+"; AveDip="+rup.getRuptureSurface().getAveDip());
*/
//		sampler.writeRelBlockProbToFile();
	}

	
	/**
	 * This creates an EqksInGeoBlock for the given ERF at each point in the GriddedRegion region
	 */
	public void makeAllEqksInGeoBlocks() {

		blockList = new ArrayList<EqksInGeoBlock>();
		for(Location loc: griddedRegion) {
			EqksInGeoBlock block = new EqksInGeoBlock(loc,griddedRegion.getSpacing(),0,16);
			blockList.add(block);
		}
		System.out.println("Number of Blocks: "+blockList.size()+" should be("+griddedRegion.getNodeCount()+")");


		double calcStartTime=System.currentTimeMillis();
		System.out.println("Starting to make blocks");

		double forecastDuration = erf.getTimeSpan().getDuration();
		double rateUnAssigned = 0;
		int numSrc = erf.getNumSources();
		for(int s=0;s<numSrc;s++) {
// if(s>42 && s<98) continue;
// if(s==128)  continue;
			ProbEqkSource src = erf.getSource(s);
			//			System.out.println(s+"\t"+src.getName()+"\t"+numSrc);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				ProbEqkRupture rup = src.getRupture(r);
				ArbDiscrEmpiricalDistFunc numInEachNode = new ArbDiscrEmpiricalDistFunc(); // node on x-axis and num on y-axis
				EvenlyGriddedSurfaceAPI surface = rup.getRuptureSurface();
				double rate = rup.getMeanAnnualRate(forecastDuration);
				int numUnAssigned=0;
				for(Location loc: surface) {
					int nodeIndex = griddedRegion.indexForLocation(loc);
					if(nodeIndex != -1)
						numInEachNode.set((double)nodeIndex,1.0);
					else
						numUnAssigned +=1;
				}
				int numNodes = numInEachNode.getNum();
				if(numNodes>0) {
					for(int i=0;i<numNodes;i++) {
						int nodeIndex = (int)Math.round(numInEachNode.getX(i));
						double fracInside = numInEachNode.getY(i)/surface.size();
						double nodeRate = rate*fracInside;	// fraction of rate in node
						blockList.get(nodeIndex).processRate(nodeRate, fracInside, s, r, rup.getMag());
					}
				}
				float fracUnassigned = (float)numUnAssigned/(float)surface.size();
				if(numUnAssigned>0) System.out.println(fracUnassigned+" of rup "+r+" were unassigned for source "+s+" ("+erf.getSource(s).getName()+")");
				rateUnAssigned += rate*fracUnassigned;
			}
		}

		System.out.println("rateUnAssigned = "+rateUnAssigned);

		double runtime = (System.currentTimeMillis()-calcStartTime)/1000;
		System.out.println("Making blocks took "+runtime+" seconds");

		// This checks to make sure total rate in all blocks (plus rate unassigned) is equal the the total ERF rate
		System.out.println("TESTING RESULT");
		double testRate1=0;
		for(EqksInGeoBlock block: blockList) {
			testRate1+=block.getTotalRateInside();
		}
		testRate1+=rateUnAssigned;
		double testRate2=0;
		for(int s=0;s<numSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				testRate2 += src.getRupture(r).getMeanAnnualRate(forecastDuration);
			}
		}
		System.out.println("\tRate1="+testRate1);
		System.out.println("\tRate2="+testRate2);

/*
		// Make SubBlocks
		startTime=System.currentTimeMillis();
		System.out.println("Starting to make subblocks");
		ArrayList<EqksInGeoBlock> subBlockList = new ArrayList<EqksInGeoBlock>();
		int counter =0, counterThresh=100, counterIncr=100;
		for(EqksInGeoBlock block:blockList) {
			subBlockList.addAll(block.getSubBlocks(2, 2,erf));
			counter+=1;
			if(counter==counterThresh) {
				System.out.println("\t"+counter+"\tof\t"+blockList.size());
				counterThresh+=counterIncr;
			}
		}
		runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("Making sub-blocks took "+runtime+" seconds");
		*/
		
//		blockList.get(0).testRandomSampler();
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		ETAS_Utils utils = new ETAS_Utils();
		System.out.println("Num primary aftershocks for mag:");
		for(int i=2;i<9;i++) {
			System.out.println("\t"+i+"\t"+utils.getDefaultExpectedNumEvents((double)i, 0, 365));
		}
				
		
		/*	*/
		// Create UCERF2 instance
		System.out.println("Starting ERF instantiation");
		long startTime=System.currentTimeMillis();
		int duration = 1;
		MeanUCERF2_ETAS meanUCERF2 = new MeanUCERF2_ETAS();
		meanUCERF2.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
//		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2.getTimeSpan().setDuration(duration);
		meanUCERF2.updateForecast();
		double runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("ERF instantiation took "+runtime+" seconds");
		
		// print out first sources
//		for(int s=meanUCERF2.getNumSources()-1;s>meanUCERF2.getNumSources()-100;s--) System.out.println(meanUCERF2.getSource(s).getName()+"\t"+meanUCERF2.getSource(s).getRupture(0).getMag());
//		for(int s=0;s<100;s++) System.out.println(meanUCERF2.getSource(s).getName()+"\t"+meanUCERF2.getSource(s).getRupture(0).getMag());
//		for(int s=0;s<meanUCERF2.getNumSources();s++) System.out.println(s+"\t"+meanUCERF2.getSource(s).getName()+"\t #rups="+
//				meanUCERF2.getSource(s).getNumRuptures()+"\t"+meanUCERF2.getSource(s).getRupture(0).getRuptureSurface().getAveDip());
/**/
		startTime=System.currentTimeMillis();
		ETAS_Simulator etasSimulator = new ETAS_Simulator(meanUCERF2,griddedRegion);
		
		// Full SSAF rupture
//		ProbEqkRupture rup = meanUCERF2.getSource(68).getRupture(0);
		
		// Landers Rupture
//		ProbEqkRupture rup = meanUCERF2.getSource(195).getRupture(0);

//		ProbEqkRupture rup = new ProbEqkRupture();
//		rup.setMag(6);
//		rup.setPointSurface(new Location(33.35028,-115.7167));

//		ArrayList<PrimaryAftershock> aftershockList = etasSimulator.getPrimaryAftershocksList(rup);
//		System.out.println("aftershockList size = "+aftershockList.size());
	
		etasSimulator.testDistanceDecays(true,true);
		runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("Tests Run took "+runtime+" seconds");
		
	}

}
