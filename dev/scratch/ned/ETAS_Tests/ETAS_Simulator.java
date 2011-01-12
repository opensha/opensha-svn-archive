package scratch.ned.ETAS_Tests;

import java.awt.Color;
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
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

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
	
	
	ArrayList<PrimaryAftershock> primaryAftershockList;
	ETAS_PrimaryEventSampler etas_sampler;
	EqkRupture parentRup;
	double expectedNumAftershocks;

	
	
	public ETAS_Simulator(EqkRupForecast erf,GriddedRegion griddedRegion) {
		this.erf = erf;
		this.griddedRegion = griddedRegion;
		etasUtils = new ETAS_Utils();
		makeAllEqksInGeoBlocks();
				
	}
	
	/**
	 * This returns a list of randomly sampled primary aftershocks
	 * @param parentRup
	 * @return list of PrimaryAftershock objects
	 */
	public ArrayList<PrimaryAftershock> getPrimaryAftershocksList(EqkRupture parentRup) {
		
		this.parentRup=parentRup;
		
		System.out.println("Main Shock Mag = "+parentRup.getMag()+"\tMin & Max time (days): "+tMin+"; "+tMax);
				
		// compute the number of primary aftershocks:
		expectedNumAftershocks = etasUtils.getDefaultExpectedNumEvents(parentRup.getMag(), tMin, tMax);
		int numAftershocks = etasUtils.getPoissonRandomNumber(expectedNumAftershocks);
		System.out.println("Expected num primary aftershocks = "+expectedNumAftershocks+"\tSampled num = "+numAftershocks);
		
		// Make the ETAS sampler for the given main shock:
		etas_sampler = new ETAS_PrimaryEventSampler(parentRup,blockList, erf, distDecay,minDist, useAdaptiveBlocks, includeBlockRates);
		
		// Write spatial probability dist data to file:
//		ETAS_sampler.writeRelBlockProbToFile();

		// Now make the list of aftershocks:
		primaryAftershockList = new ArrayList<PrimaryAftershock>();
		for (int i = 0; i < numAftershocks; i++) {
			PrimaryAftershock aftershock = etas_sampler.samplePrimaryAftershock(etasUtils.getDefaultRandomTimeOfEvent(tMin, tMax));
			primaryAftershockList.add(aftershock);
		}
		
		return primaryAftershockList;
		
	}
		
	
	/**
	 * This generates two mag-freq dist plots for the primary aftershock sequence.
	 * @param srcIndex - include if you want to show that for specific source (leave null otherwise)
	 */
	public void plotMagFreqDists(Integer srcIndex, String info) {
		
		// FIRST PLOT MAG-PROB DISTS
		
		// get the expected mag-prob dist for the aftershock sequence:
		ArbIncrementalMagFreqDist expMagProbDist = etas_sampler.getMagProbDist();
		expMagProbDist.setName("Expected Mag-Prob Dist for Sequence");
		expMagProbDist.setInfo(" ");

		// get the observed mag-prob dist for the sampled set of events 
		ArbIncrementalMagFreqDist obsMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		for (PrimaryAftershock event : primaryAftershockList)
			obsMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
		obsMagProbDist.scaleToCumRate(2.05, 1);	// normalize to 1.0
		obsMagProbDist.setName("Observed Mag-Prob Dist for Sequence");
		obsMagProbDist.setInfo(" ");

		// This makes an MFD of the original, total ERF (for entire region)
		ArbIncrementalMagFreqDist erfMagDist = new ArbIncrementalMagFreqDist(2.05, 8.95, 70);
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			for(int r=0;r<src.getNumRuptures();r++) {
				ProbEqkRupture rup = src.getRupture(r);
				erfMagDist.addResampledMagRate(rup.getMag(), rup.getMeanAnnualRate(duration), true);
			}			
		}
		erfMagDist.setName("Total Mag-Freq Dist for ERF");
		erfMagDist.setInfo(" ");
		EvenlyDiscretizedFunc erfCumDist = erfMagDist.getCumRateDistWithOffset();
		erfCumDist.setName("Total Cum Mag-Freq Dist for ERF");
		erfMagDist.scaleToCumRate(2.05, 1); // normalize to mag-prob dist
		
		UCERF2 ucerf2 = new UCERF2();

		// Plot these MFDs
		ArrayList funcs = new ArrayList();
		funcs.add(erfMagDist);
		funcs.add(expMagProbDist);
		funcs.add(obsMagProbDist);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(funcs, "Mag-Prob Dists for "+info); 
		sr_graph.setX_AxisLabel("Mag");
		sr_graph.setY_AxisLabel("Probability");
		sr_graph.setYLog(true);
		
		
		// Plot cum MFDs
		ArrayList funcs3 = new ArrayList();
		funcs3.addAll(ucerf2.getObsCumMFD(true));
		funcs3.add(erfCumDist);
		GraphiWindowAPI_Impl sr_graph3 = new GraphiWindowAPI_Impl(funcs3, "Cum MFD for ERF and Karen's Obs for CA"); 
		sr_graph3.setX_AxisLabel("Mag");
		sr_graph3.setY_AxisLabel("Cumulative Rate");
		sr_graph3.setYLog(true);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 5));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 5));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 5));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.BLACK, 3));
		sr_graph3.setPlottingFeatures(plotChars);

		
		// NOW PLOT EXPECTED CUM MAG-FREQ DISTS FOR TIME SPAN
		
		ArrayList funcs2 = new ArrayList();
		double days = tMax-tMin;
		
		// get expected cum MFD for sequence
		EvenlyDiscretizedFunc expCumDist = expMagProbDist.getCumRateDist();
		expCumDist.multiplyY_ValsBy(expectedNumAftershocks);
		expCumDist.setName("Expected num greater then M in "+(float)days+" days");
		expCumDist.setInfo(" ");
		funcs2.add(expCumDist);

		if(srcIndex != null) {
			EvenlyDiscretizedFunc expCumDistForSource = etas_sampler.getMagProbDistForSource(srcIndex).getCumRateDist();
			expCumDistForSource.multiplyY_ValsBy(expectedNumAftershocks);
			expCumDistForSource.setName("Expected num greater then M in "+(float)days+" days for source: "+erf.getSource(srcIndex).getName());
			expCumDistForSource.setInfo(" ");
			funcs2.add(expCumDistForSource);
		}
		
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(1.0, 1.0,2.55,8.95, 65);
		double scaleBy = expectedNumAftershocks/gr.getY(2.55);
		gr.multiplyY_ValsBy(scaleBy);
		funcs2.add(gr);

		GraphiWindowAPI_Impl sr_graph2 = new GraphiWindowAPI_Impl(funcs2, "Expected Number of Primary Events for "+info); 
		sr_graph2.setX_AxisLabel("Mag");
		sr_graph2.setY_AxisLabel("Expected Num");
		sr_graph2.setYLog(true);

	}
	
	
	/**
	 * This writes aftershock data to a file
	 */
	public void writeAftershockDataToFile() {
		try{
			FileWriter fw1 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/computedData/aftershockList.txt");
			fw1.write("mag\ttime\tdist\tblockID\n");
			for(PrimaryAftershock event: primaryAftershockList) {
				double dist = LocationUtils.distanceToSurfFast(event.getHypocenterLocation(), parentRup.getRuptureSurface());
				fw1.write((float)event.getMag()+"\t"+(float)event.getOriginTime()+"\t"+(float)dist+"\t"+event.getParentID()+
						"\t"+event.getSourceIndex()+"\t"+event.getRupIndex()+"\n");
			}
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * This creates an EqksInGeoBlock for the given ERF at each point in the GriddedRegion region
	 */
	public void makeAllEqksInGeoBlocks() {

		double calcStartTime=System.currentTimeMillis();
		System.out.println("Starting to make blocks");

		blockList = new ArrayList<EqksInGeoBlock>();
		for(Location loc: griddedRegion) {
			EqksInGeoBlock block = new EqksInGeoBlock(loc,griddedRegion.getSpacing(),0,16);
			blockList.add(block);
		}
		System.out.println("Number of Blocks: "+blockList.size()+" should be("+griddedRegion.getNodeCount()+")");


		double forecastDuration = erf.getTimeSpan().getDuration();
		double rateUnAssigned = 0;
		int numSrc = erf.getNumSources();
		for(int s=0;s<numSrc;s++) {
// if(s>42 && s<98) continue; // Exclude SSAF
// if(s==195)  continue;	// Exclude Landers
			ProbEqkSource src = erf.getSource(s);
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
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		// make the gridded region
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		// write the expected number of primary aftershocks for different mags
		ETAS_Utils utils = new ETAS_Utils();
		System.out.println("Num primary aftershocks for mag:");
		for(int i=2;i<9;i++) {
			System.out.println("\t"+i+"\t"+utils.getDefaultExpectedNumEvents((double)i, 0, 365));
		}
		
		// for keeping track of runtime:
		long startRunTime=System.currentTimeMillis();

		// Create the UCERF2 instance
		System.out.println("Starting ERF instantiation");
		double forecastDuration = 1.0;	// years
		MeanUCERF2_ETAS meanUCERF2 = new MeanUCERF2_ETAS();
		meanUCERF2.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
//		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2.getTimeSpan().setDuration(forecastDuration);
		meanUCERF2.updateForecast();
		double runtime = (System.currentTimeMillis()-startRunTime)/1000;
		System.out.println("ERF instantiation took "+runtime+" seconds");
		
		/*
		// print info about sources
		for(int s=0;s<meanUCERF2.getNumSources();s++) {
			System.out.println(s+"\t"+meanUCERF2.getSource(s).getName()+"\t #rups="+
				meanUCERF2.getSource(s).getNumRuptures()+"\t"+meanUCERF2.getSource(s).getRupture(0).getRuptureSurface().getAveDip());
			if(s==195) {
				ProbEqkSource landersSrc = meanUCERF2.getSource(s);
				for(int r=0; r<landersSrc.getNumRuptures(); r++ )
					System.out.println("\tLanders rup "+r+"; M="+landersSrc.getRupture(r).getMag());
			}
		}
		*/
		
		// for keeping track of runtimes:
		startRunTime=System.currentTimeMillis();
		
		// Create the ETAS simulator
		ETAS_Simulator etasSimulator = new ETAS_Simulator(meanUCERF2,griddedRegion);
		
		ProbEqkRupture mainShock;
		
		// NOW RUN TESTS FOR VARIOUS MAIN SHOCKS
/*	
		//  Point source at edge of sub-blocks
		mainShock = new ProbEqkRupture();
		mainShock.setMag(5);
		mainShock.setPointSurface(new Location(34,-118,8));
		etasSimulator.runTests(mainShock,"M5 Pt Src at 34,-118,8 (sub-block edge)",null);
		
		// Point source in midpoint of sub block
		mainShock.setPointSurface(new Location(34.00625,-118.00625,7));
		etasSimulator.runTests(mainShock,"M5 Pt Src at 34.00625,-118.00625,7 (sub-block mid)",null);

		// point source in center of sub block
		mainShock.setPointSurface(new Location(34.0125,	-118.0125,	6.0));
		etasSimulator.runTests(mainShock,"M5 Pt Src at 34.0125,-118.0125,6.0 (sub-block center)",null);

		// 68	S. San Andreas;CH+CC+BB+NM+SM+NSB+SSB+BG+CO	 #rups=8
		mainShock = meanUCERF2.getSource(68).getRupture(4);
		etasSimulator.runTests(mainShock,"SSAF Wall-to-wall Rupture; M="+mainShock.getMag(), null);
*/
/**/	
		// 195	Landers Rupture	 #rups=46 (rup 41 for M 7.25)
		mainShock = meanUCERF2.getSource(195).getRupture(41);
		etasSimulator.runTests(mainShock,"Landers Rupture; M="+mainShock.getMag(), 195);

/*
		// 236	Pitas Point (Lower, West)	 #rups=19	13.0 (shallowest dipping rupture I could find)
		mainShock = meanUCERF2.getSource(236).getRupture(10);
		String info = "Pitas Point (shallowest dipping source); M="+mainShock.getMag()+"; AveDip="+mainShock.getRuptureSurface().getAveDip();
		etasSimulator.runTests(mainShock,info,null);

*/
//		System.out.println("Tests Run took "+runtime+" seconds");
		
	}
	
	
	/**
	 * This runs some tests for the given main shock
	 * @param mainShock
	 * @param info - plotting label
	 * @param srcIndex - the index of a source to compare results with
	 */
	public void runTests(ProbEqkRupture mainShock, String info, Integer srcIndex) {
		ArrayList<PrimaryAftershock> aftershockList = getPrimaryAftershocksList(mainShock);
		System.out.println("aftershockList size = "+aftershockList.size());
		plotMagFreqDists(srcIndex, info);
		etas_sampler.testRandomDistanceDecay(info);
		etas_sampler.plotBlockProbMap(info);
	}

}
