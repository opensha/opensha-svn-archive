package scratch.ned.ETAS_ERF;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.AbstractEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.ned.ETAS_Tests.ETAS_Utils;

public class ETAS_Simulator {
	
	String dirToSaveData;
	String infoForOutputFile;
	
	ArrayList<EqksInGeoBlock> blockList;
	GriddedRegion griddedRegion;
	FaultSystemSolutionPoissonERF erf;
	ETAS_Utils etasUtils;
//	double distDecay=1.4;
	double distDecay;
	double minDist;
//	double minDist=2.0;
	double tMin=0;		//days
	double tMax=360;	//days
	boolean useAdaptiveBlocks=true;
	boolean includeBlockRates=true;
	
	ProbEqkRupture mainShock;
	
	ArrayList<PrimaryAftershock> primaryAftershockList;
//	ETAS_PrimaryEventSampler etas_FirstGenSampler, etas_sampler;
	EqkRupture parentRup;
	double expectedNumPrimaryAftershocks, expectedNum;
	SummedMagFreqDist totalExpectedNumMagDist;
	ArrayList<PrimaryAftershock> allAftershocks;

	
	
	public ETAS_Simulator(FaultSystemSolutionPoissonERF erf, GriddedRegion griddedRegion) {
		this.erf = erf;
		this.griddedRegion = griddedRegion;
		etasUtils = new ETAS_Utils();
		makeAllEqksInGeoBlocks();
				
	}
	
	
	public void plotERF_MagFreqDists() {
		
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
		erfMagDist.setName("Total Mag-Prob Dist for "+erf.getName());
		erfMagDist.setInfo(" ");
		EvenlyDiscretizedFunc erfCumDist = erfMagDist.getCumRateDistWithOffset();
		erfCumDist.setName("Total Cum Mag-Freq Dist for "+erf.getName());
		erfMagDist.scaleToCumRate(2.05, 1); // normalize to mag-prob dist

		// Plot cum MFDs for ERF (plus Karen's UCERF2 obs range)
		ArrayList funcs3 = new ArrayList();
		funcs3.addAll(UCERF2.getObsCumMFD(true));
		funcs3.add(erfCumDist);
		GraphiWindowAPI_Impl sr_graph3 = new GraphiWindowAPI_Impl(funcs3, "Cum MFD for ERF and Karen's Obs for CA"); 
		sr_graph3.setX_AxisLabel("Mag");
		sr_graph3.setY_AxisLabel("Cumulative Rate");
		sr_graph3.setYLog(true);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 5f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 5f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 5f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));
		sr_graph3.setPlottingFeatures(plotChars);
		sr_graph3.setY_AxisRange(1e-6, sr_graph3.getY_AxisMax());
		
	}

		
	public void testSubBlockList() {
		ArrayList<EqksInGeoBlock> subBlockList = new ArrayList<EqksInGeoBlock>();
		int bl =0;
		for(EqksInGeoBlock block: blockList) {
			System.out.println(bl++);
			subBlockList.addAll(block.getSubBlocks(3, 3, erf));
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
			ProbEqkSource src = erf.getSource(s);
			
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				ProbEqkRupture rup = src.getRupture(r);
				ArbDiscrEmpiricalDistFunc numInEachNode = new ArbDiscrEmpiricalDistFunc(); // node on x-axis and num on y-axis
				LocationList locsOnRupSurf = rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				double rate = rup.getMeanAnnualRate(forecastDuration);
				int numUnAssigned=0;
				for(Location loc: locsOnRupSurf) {
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
						double fracInside = numInEachNode.getY(i)/locsOnRupSurf.size();
						double nodeRate = rate*fracInside;	// fraction of rate in node
						int nthRup = erf.getIndexN_ForSrcAndRupIndices(s, r);
						blockList.get(nodeIndex).processRate(nodeRate, fracInside, nthRup, rup.getMag());
					}
				}
				float fracUnassigned = (float)numUnAssigned/(float)locsOnRupSurf.size();
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
		System.out.println("\tRate1="+(float)testRate1+" should equal Rate2="+(float)testRate2+";\tratio="+(float)(testRate1/testRate2));
	//	System.out.println("\tRate2="+testRate2);
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String rootDir = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/computedData/";
		
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
		FaultSystemSolutionPoissonERF erf = new FaultSystemSolutionPoissonERF("/Users/field/ALLCAL_UCERF2.zip");
		erf.getAdjustableParameterList().getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.12);
		erf.updateForecast();
		double runtime = (System.currentTimeMillis()-startRunTime)/1000;
		System.out.println("ERF instantiation took "+runtime+" seconds");
		
		double cumRate = Math.round(10*ERF_Calculator.getTotalMFD_ForERF(erf, 2.05,8.95, 70, true).getCumRate(5.05))/10.0;
		// get a slightly different result using ERF_Calculator.getMagFreqDistInRegion(meanUCERF2, CaliforniaRegions.RELM_GRIDDED(),5.05,35,0.1, true), but with rounding it's the same
		System.out.println("\nCumRate >= M5 for erf: "+cumRate+"\n");
		

		
		
		ETAS_Simulator etasSimulator = new ETAS_Simulator(erf,griddedRegion);
		
//		long startBlockRunTime = System.currentTimeMillis();
//		System.out.println("Starting subblocks");
//		etasSimulator.testSubBlockList();
//		System.out.println("Making Sublocks took "+(System.currentTimeMillis()-startBlockRunTime)+" seconds");
		
		etasSimulator.plotERF_MagFreqDists();
		
		
		
		/*
		// print info about sources
		for(int s=0;s<meanUCERF2.getNumSources();s++) {
			System.out.println(s+"\t"+meanUCERF2.getSource(s).getName()+"\t #rups="+
				meanUCERF2.getSource(s).getNumRuptures()+"\t"+meanUCERF2.getSource(s).getRupture(0).getRuptureSurface().getAveDip());
			if(s==223) {
				ProbEqkSource src = meanUCERF2.getSource(s);
				for(int r=0; r<src.getNumRuptures(); r++ )
					System.out.println("\tLanders rup "+r+"; M="+src.getRupture(r).getMag());
			}
		}
		*/
		
		
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

		
		FaultTrace trace = new FaultTrace("Test");
		trace.add(new Location (34,-117));
		trace.add(new Location (35,-117));
		StirlingGriddedSurface rupSurf = new StirlingGriddedSurface(trace, 90.0, 0.0,16.0, 1.0);
		mainShock = new ProbEqkRupture(7.0,0,1,rupSurf,null);
		double distDecay = 1.7;
		double minDist = 0.3;
		String info = "Test Event (M="+mainShock.getMag()+"); distDecay="+distDecay;
		etasSimulator.runTempTest(mainShock,info, 195, rootDir+"TestM7/",distDecay,minDist);
*/		


		

		runtime = (System.currentTimeMillis()-startRunTime)/1000;
		System.out.println("Test Run took "+runtime+" seconds");
		
	}
	
	

}
