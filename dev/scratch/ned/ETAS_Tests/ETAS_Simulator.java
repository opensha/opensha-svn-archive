package scratch.ned.ETAS_Tests;

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
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.ned.ETAS_Tests.MeanUCERF2.MeanUCERF2_ETAS;

public class ETAS_Simulator {
	
	String dirToSaveData;
	String infoForOutputFile;
	
	ArrayList<EqksInGeoBlock> blockList;
	GriddedRegion griddedRegion;
	EqkRupForecast erf;
	ETAS_Utils etasUtils;
//	double distDecay=1.4;
	double distDecay;
	double minDist;
//	double minDist=2.0;
	double tMin=0;
	double tMax=360;
	boolean useAdaptiveBlocks=true;
	boolean includeBlockRates=true;
	int sourceID_ToIgnore = -1;
	
	ProbEqkRupture mainShock;
	
	ArrayList<PrimaryAftershock> primaryAftershockList;
	ETAS_PrimaryEventSampler etas_FirstGenSampler, etas_sampler;
	EqkRupture parentRup;
	double expectedNumPrimaryAftershocks, expectedNum;
	SummedMagFreqDist totalExpectedNumMagDist;
	ArrayList<PrimaryAftershock> allAftershocks;

	
	
	public ETAS_Simulator(EqkRupForecast erf, GriddedRegion griddedRegion, int sourceID_ToIgnore) {
		this.erf = erf;
		this.griddedRegion = griddedRegion;
		this.sourceID_ToIgnore = sourceID_ToIgnore;
		etasUtils = new ETAS_Utils();
		makeAllEqksInGeoBlocks();
				
	}
	
	/**
	 * This returns a list of randomly sampled primary aftershocks
	 * @param parentRup
	 * @return list of PrimaryAftershock objects
	 */
	public ArrayList<PrimaryAftershock> getPrimaryAftershocksList(PrimaryAftershock parentRup) {
				
		double originTime = parentRup.getOriginTime();
		
		int parentID = parentRup.getID();
		
				
		// compute the number of primary aftershocks:
		expectedNum = etasUtils.getDefaultExpectedNumEvents(parentRup.getMag(), originTime, tMax);
		int numAftershocks = etasUtils.getPoissonRandomNumber(expectedNum);
//		System.out.println("\tMag="+(float)parentRup.getMag()+"\tOriginTime="+(float)originTime+"\tExpNum="+
//				(float)expectedNum+"\tSampledNum = "+numAftershocks);
		
		if(numAftershocks == 0)
			return new ArrayList<PrimaryAftershock>();
		
		// Make the ETAS sampler for the given main shock:
		etas_sampler = new ETAS_PrimaryEventSampler(parentRup,blockList, erf, distDecay,minDist, useAdaptiveBlocks, includeBlockRates);
		
		// Write spatial probability dist data to file:
//		ETAS_sampler.writeRelBlockProbToFile();

		// Now make the list of aftershocks:
		ArrayList<PrimaryAftershock> primaryAftershocks = new ArrayList<PrimaryAftershock>();
		for (int i = 0; i < numAftershocks; i++) {
			PrimaryAftershock aftershock = etas_sampler.samplePrimaryAftershock(etasUtils.getDefaultRandomTimeOfEvent(originTime, tMax));
			aftershock.setParentID(parentID);
			double dist = LocationUtils.distanceToSurfFast(aftershock.getHypocenterLocation(), parentRup.getRuptureSurface());
			aftershock.setDistanceToParent(dist);
			primaryAftershocks.add(aftershock);
		}
		// save info if this is a mainshock
		if(parentRup.getGeneration() == 0) {
			etas_FirstGenSampler = etas_sampler;
			expectedNumPrimaryAftershocks = expectedNum;
			primaryAftershockList = primaryAftershocks;
		}
		
		return primaryAftershocks;
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
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 5));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 5));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 5));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.BLACK, 3));
		sr_graph3.setPlottingFeatures(plotChars);
		sr_graph3.setY_AxisRange(1e-6, sr_graph3.getY_AxisMax());
		
	}

		
	
	/**
	 * This generates two mag-freq dist plots for the primary aftershock sequence.
	 * @param srcIndex - include if you want to show that for specific source (leave null otherwise)
	 */
	public String plotMagFreqDists(Integer srcIndex, String info, boolean savePDF_Files) {
		
		double days = tMax-tMin;
		ArrayList magProbDists = new ArrayList();
		ArrayList expNumDists = new ArrayList();

		// FIRST PLOT MAG-PROB DISTS
		
		// get the expected mag-prob dist for the primary aftershock sequence:
		ArbIncrementalMagFreqDist expMagProbDist = etas_FirstGenSampler.getMagProbDist();
		expMagProbDist.setName("Expected Mag-Prob Dist for first-generation events");
		expMagProbDist.setInfo(" ");
		// get expected cum MFD for sequence
		EvenlyDiscretizedFunc expCumDist = expMagProbDist.getCumRateDist();
		expCumDist.multiplyY_ValsBy(expectedNumPrimaryAftershocks);
		expCumDist.setName("Expected num 1st-generation >=M in "+(float)days+" days");
		expCumDist.setInfo(" ");
		magProbDists.add(expMagProbDist);
		expNumDists.add(expCumDist);


		// get the observed mag-prob dist for the sampled set of events 
		ArbIncrementalMagFreqDist obsMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		for (PrimaryAftershock event : primaryAftershockList)
			obsMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
		EvenlyDiscretizedFunc obsCumDist = obsMagProbDist.getCumRateDist();
		obsMagProbDist.scaleToCumRate(2.05, 1);	// normalize to 1.0
		obsMagProbDist.setName("Sampled Mag-Prob Dist for first-generation events");
		obsMagProbDist.setInfo(" ");
		obsCumDist.setName("Sampled num 1st-generation >=M in "+(float)days+" days");
		obsCumDist.setInfo(" ");
		magProbDists.add(obsMagProbDist);
		expNumDists.add(obsCumDist);
		
		// MFDs for the specified source
		EvenlyDiscretizedFunc expCumDistForSource = new EvenlyDiscretizedFunc(0.0,10.0,10); // bogus function just so one exists
		if(srcIndex != null) {
			ArbIncrementalMagFreqDist expMagProbDistForSource = etas_FirstGenSampler.getMagProbDistForSource(srcIndex);
			expCumDistForSource = expMagProbDistForSource.getCumRateDist();
			expCumDistForSource.multiplyY_ValsBy(expectedNumPrimaryAftershocks);
			expCumDistForSource.setName("Expected num 1st-generation >=M in "+(float)days+" days for source: "+erf.getSource(srcIndex).getName());
			expCumDistForSource.setInfo(" ");
			expMagProbDistForSource.setName("Expected Mag-Prob Dist for first-generation events for source: "+erf.getSource(srcIndex).getName());
			expMagProbDistForSource.setInfo(" ");
			magProbDists.add(expMagProbDistForSource);
			expNumDists.add(expCumDistForSource);
		}
		
		// now make the cumulative distributions for the entire sequence
		EvenlyDiscretizedFunc totalExpCumDist = totalExpectedNumMagDist.getCumRateDist();
		totalExpCumDist.setName("Expected total num aftershocks >=M in "+(float)days+" days (all generations)");
		totalExpCumDist.setInfo(" ");
		totalExpectedNumMagDist.scaleToCumRate(2.05, 1.0);	// a permanent change!!!!
		totalExpectedNumMagDist.setName("Expected Mag-Prob Dist for all events (all generations)");
		totalExpectedNumMagDist.setInfo(" ");
		magProbDists.add(totalExpectedNumMagDist);
		expNumDists.add(totalExpCumDist);


		// get the observed mag-prob dist for all sampled events 
		ArbIncrementalMagFreqDist totObsMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		for (PrimaryAftershock event : allAftershocks)
			totObsMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
		EvenlyDiscretizedFunc totObsCumDist = totObsMagProbDist.getCumRateDist();
		totObsMagProbDist.scaleToCumRate(2.05, 1);	// normalize to 1.0
		totObsMagProbDist.setName("Sampled Mag-Prob Dist for all events (all generations)");
		totObsMagProbDist.setInfo(" ");
		totObsCumDist.setName("Sampled num for all event >=M in "+(float)days+" days (all generations)");
		totObsCumDist.setInfo(" ");
		magProbDists.add(totObsMagProbDist);
		expNumDists.add(totObsCumDist);

		
		// GR dist for comparison
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(1.0, 1.0,2.55,8.95, 65);
		double scaleBy = expectedNumPrimaryAftershocks/gr.getY(2.55);
		gr.multiplyY_ValsBy(scaleBy);
		String name = gr.getName();
		gr.setName(name+" (for comparison)");
		expNumDists.add(gr);
		
		
		// Plot these MFDs
		GraphiWindowAPI_Impl magProbDistsGraph = new GraphiWindowAPI_Impl(magProbDists, "Mag-Prob Distributions for "+info+" Aftershocks"); 
		magProbDistsGraph.setX_AxisLabel("Mag");
		magProbDistsGraph.setY_AxisLabel("Probability");
		magProbDistsGraph.setY_AxisRange(1e-8, magProbDistsGraph.getY_AxisMax());
		magProbDistsGraph.setYLog(true);
		
	
		GraphiWindowAPI_Impl expNumDistGraph = new GraphiWindowAPI_Impl(expNumDists, "Mag-Num Distributions for Aftershocks for "+(float)days+" days following "+info); 
		expNumDistGraph.setX_AxisLabel("Mag");
		expNumDistGraph.setY_AxisLabel("Expected Num");
		expNumDistGraph.setY_AxisRange(1e-6, expNumDistGraph.getY_AxisMax());
		expNumDistGraph.setYLog(true);
		
		if(savePDF_Files) {
			try {
				magProbDistsGraph.saveAsPDF(dirToSaveData+"magProbDistsGraph.pdf");
				expNumDistGraph.saveAsPDF(dirToSaveData+"magNumDistsGraph.pdf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// make the infoText
		String srcName=erf.getSource(srcIndex).getName();
		String infoText = "Expected num primary aftershocks above mags:\n\n\tMag\tTotal\t"+srcName+"\t(% from "+srcName+")\n";
		double cumRateSrc, cumRate, ratio;
		expCumDist.setTolerance(0.0001);
		expCumDistForSource.setTolerance(0.0001);
		for(int i=0; i<3; i++) {
			double mag = i*0.5+6.5;
			cumRate = expCumDist.getY(mag+0.05);
			if(srcIndex != null) {
				cumRateSrc = expCumDistForSource.getY(mag+0.05);
				ratio = 100*cumRateSrc/cumRate;				
			}
			else {
				cumRateSrc = Double.NaN;
				ratio = Double.NaN;				
			}
			infoText += "\t"+mag+"\t"+(float)cumRate+"\t"+(float)cumRateSrc+"\t"+(float)ratio+"\n";
		}
		// add row for the main shock mag
		double mag = mainShock.getMag();
		cumRate = expCumDist.getY(mag);
		if(srcIndex != null) {
			cumRateSrc = expCumDistForSource.getY(mag);
			ratio = 100*cumRateSrc/cumRate;				
		}
		else {
			cumRateSrc = Double.NaN;
			ratio = Double.NaN;				
		}
		infoText += "\t"+mag+"\t"+(float)cumRate+"\t"+(float)cumRateSrc+"\t"+(float)ratio+"\n";


		return infoText;
	}
	
	
	/**
	 * 
	 * @param info
	 */
	public void plotNumVsTime(String info, boolean savePDF_File, ProbEqkRupture mainShock) {
		
		double delta = 1.0; // days

		// make the target function & change it to a PDF
		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithTimeFunc(mainShock.getMag(), tMin, tMax, delta);
		targetFunc.setName("Expected Number for First-generation Aftershocks");
		
		int numPts = (int) Math.round(tMax-tMin);
		EvenlyDiscretizedFunc allEvents = new EvenlyDiscretizedFunc(tMin+0.5,numPts,delta);
		EvenlyDiscretizedFunc firstGenEvents= new EvenlyDiscretizedFunc(tMin+0.5,numPts,delta);
		allEvents.setTolerance(2.0);
		firstGenEvents.setTolerance(2.0);
		for (PrimaryAftershock event : allAftershocks) {
			double time = event.getOriginTime();
			allEvents.add(time, 1.0);
			if(event.getGeneration() == 1)
				firstGenEvents.add(time, 1.0);
		}
		allEvents.setName("All aftershocks");
		firstGenEvents.setName("First-generation aftershocks");
		allEvents.setInfo(" ");
		firstGenEvents.setInfo(" ");
		
		ArrayList funcs = new ArrayList();
		funcs.add(allEvents);
		funcs.add(firstGenEvents);
		funcs.add(targetFunc);
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Num aftershocks per day for "+info); 
		graph.setX_AxisLabel("Days (since main shock)");
		graph.setY_AxisLabel("Num Events");
		graph.setX_AxisRange(0.4, 360);
		graph.setY_AxisRange(0.1, graph.getY_AxisMax());
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,Color.BLUE, 3));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,Color.RED, 3));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,Color.BLACK, 2));
		graph.setPlottingFeatures(plotChars);
		graph.setYLog(true);
		graph.setXLog(true);
		if(savePDF_File)
		try {
			graph.saveAsPDF(dirToSaveData+"numAshocksVsTime.pdf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private DefaultXY_DataSet getEpicenterLocsXY_DataSet(double magLow, double magHigh, int generation) {
		DefaultXY_DataSet epicenterLocs = new DefaultXY_DataSet();
		for (PrimaryAftershock event : allAftershocks) {
			if(event.getMag()>=magLow && event.getMag()<magHigh && event.getGeneration()==generation)
				epicenterLocs.set(event.getHypocenterLocation().getLongitude(), event.getHypocenterLocation().getLatitude());
		}
		epicenterLocs.setName("Generation "+generation+" Aftershock Epicenters for "+magLow+"<=Mag<"+magHigh);
		return epicenterLocs;
	}
	
	
	
	public void plotEpicenterMap(String info, boolean savePDF_File, ProbEqkRupture mainShock) {
		
		ArrayList<AbstractXY_DataSet> funcs = new ArrayList<AbstractXY_DataSet>();
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();

		// M<5
		DefaultXY_DataSet epLocsGen1_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 1);
		if(epLocsGen1_Mlt5.getNum()>0) {
			funcs.add(epLocsGen1_Mlt5);
			epLocsGen1_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CIRCLES,Color.BLACK, 1));
		}
		DefaultXY_DataSet epLocsGen2_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 2);
		if(epLocsGen2_Mlt5.getNum()>0) {
			funcs.add(epLocsGen2_Mlt5);
			epLocsGen2_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CIRCLES,Color.BLUE, 1));
		}
		DefaultXY_DataSet epLocsGen3_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 3);
		if(epLocsGen3_Mlt5.getNum()>0) {
			funcs.add(epLocsGen3_Mlt5);
			epLocsGen3_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CIRCLES,Color.GREEN, 1));
		}
		DefaultXY_DataSet epLocsGen4_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 4);
		if(epLocsGen4_Mlt5.getNum()>0) {
			funcs.add(epLocsGen4_Mlt5);
			epLocsGen4_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CIRCLES,Color.RED, 1));
		}
		DefaultXY_DataSet epLocsGen5_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 5);
		if(epLocsGen5_Mlt5.getNum()>0) {
			funcs.add(epLocsGen5_Mlt5);
			epLocsGen5_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CIRCLES,Color.ORANGE, 1));
		}
		DefaultXY_DataSet epLocsGen6_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 6);
		if(epLocsGen6_Mlt5.getNum()>0) {
			funcs.add(epLocsGen6_Mlt5);
			epLocsGen6_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CIRCLES,Color.YELLOW, 1));
		}


		// 5.0<=M<6.5
		DefaultXY_DataSet epLocsGen1_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 1);
		if(epLocsGen1_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen1_Mgt5lt65);
			epLocsGen1_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES,Color.BLACK, 4));
		}
		DefaultXY_DataSet epLocsGen2_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 2);
		if(epLocsGen2_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen2_Mgt5lt65);
			epLocsGen2_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES,Color.BLUE, 4));
		}
		DefaultXY_DataSet epLocsGen3_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 3);
		if(epLocsGen3_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen3_Mgt5lt65);
			epLocsGen3_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES,Color.GREEN, 3));
		}
		DefaultXY_DataSet epLocsGen4_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 4);
		if(epLocsGen4_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen4_Mgt5lt65);
			epLocsGen4_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES,Color.RED, 4));
		}
		DefaultXY_DataSet epLocsGen5_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 5);
		if(epLocsGen5_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen5_Mgt5lt65);
			epLocsGen5_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES,Color.ORANGE, 4));
		}
		DefaultXY_DataSet epLocsGen6_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 6);
		if(epLocsGen6_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen6_Mgt5lt65);
			epLocsGen6_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES,Color.YELLOW, 4));
		}


		// 6.5<=M<9.0
		DefaultXY_DataSet epLocsGen1_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 1);
		if(epLocsGen1_Mgt65.getNum()>0) {
			funcs.add(epLocsGen1_Mgt65);
			epLocsGen1_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SQUARES,Color.LIGHT_GRAY, 8));
		}
		DefaultXY_DataSet epLocsGen2_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 2);
		if(epLocsGen2_Mgt65.getNum()>0) {
			funcs.add(epLocsGen2_Mgt65);
			epLocsGen2_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SQUARES,Color.BLUE, 8));
		}
		DefaultXY_DataSet epLocsGen3_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 3);
		if(epLocsGen3_Mgt65.getNum()>0) {
			funcs.add(epLocsGen3_Mgt65);
			epLocsGen3_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SQUARES,Color.GREEN, 8));
		}
		DefaultXY_DataSet epLocsGen4_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 4);
		if(epLocsGen4_Mgt65.getNum()>0) {
			funcs.add(epLocsGen4_Mgt65);
			epLocsGen4_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SQUARES,Color.RED, 8));
		}
		DefaultXY_DataSet epLocsGen5_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 5);
		if(epLocsGen5_Mgt65.getNum()>0) {
			funcs.add(epLocsGen5_Mgt65);
			epLocsGen5_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SQUARES,Color.ORANGE, 8));
		}
		DefaultXY_DataSet epLocsGen6_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 6);
		if(epLocsGen6_Mgt65.getNum()>0) {
			funcs.add(epLocsGen6_Mgt65);
			epLocsGen6_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SQUARES,Color.YELLOW, 8));
		}
		
		double minLat=90, maxLat=-90,minLon=360,maxLon=-360;
		for(AbstractXY_DataSet func:funcs) {
			System.out.println(func.getMinX()+"\t"+func.getMaxX()+"\t"+func.getMinY()+"\t"+func.getMaxY());
			if(func.getMaxX()>maxLon) maxLon = func.getMaxX();
			if(func.getMinX()<minLon) minLon = func.getMinX();
			if(func.getMaxY()>maxLat) maxLat = func.getMaxY();
			if(func.getMinY()<minLat) minLat = func.getMinY();
		}
		
		System.out.println("latDada\t"+minLat+"\t"+maxLat+"\t"+minLon+"\t"+maxLon+"\t");
		
		FaultTrace trace = mainShock.getRuptureSurface().getRowAsTrace(0);
		ArbitrarilyDiscretizedFunc traceFunc = new ArbitrarilyDiscretizedFunc();
		traceFunc.setName("Main Shock Trace");
		for(Location loc:trace)
			traceFunc.set(loc.getLongitude(), loc.getLatitude());
		funcs.add(traceFunc);
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,Color.MAGENTA, 1));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Aftershock Epicenters for "+info); 
		graph.setX_AxisLabel("Longitude");
		graph.setY_AxisLabel("Latitude");
		double deltaLat = maxLat-minLat;
		double deltaLon = maxLon-minLon;
		double aveLat = (minLat+maxLat)/2;
		double scaleFactor = 1.57/Math.cos(aveLat*Math.PI/180);	// this is what deltaLon/deltaLat should equal
		if(deltaLat > deltaLon/scaleFactor) {	// expand lon range
			double newLonMax = minLon + deltaLat*scaleFactor;
			graph.setX_AxisRange(minLon, newLonMax);
			graph.setY_AxisRange(minLat, maxLat);
		}
		else { // expand lat range
			double newMaxLat = minLat + deltaLon/scaleFactor;
			graph.setX_AxisRange(minLon, maxLon);
			graph.setY_AxisRange(minLat, newMaxLat);
		}
		graph.setPlottingFeatures(plotChars);
		if(savePDF_File)
		try {
			graph.saveAsPDF(dirToSaveData+"epicenterMap.pdf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * This writes aftershock data to a file
	 */
	public void writeAftershockDataToFile(ArrayList<PrimaryAftershock> events, String filePathAndName) {
		try{
			FileWriter fw1 = new FileWriter(filePathAndName);
			fw1.write("id\ttime\tlat\tlon\tdepth\tmag\tParentID\tGeneration\tdistToMainShock\tdistToParent\tERF_srcID\tERF_rupID\tERF_SrcName\n");
			for(PrimaryAftershock event: events) {
				Location hLoc = event.getHypocenterLocation();
				double dist = LocationUtils.distanceToSurfFast(hLoc, mainShock.getRuptureSurface());
				fw1.write(event.getID()+"\t"+(float)event.getOriginTime()+"\t"
						+(float)hLoc.getLatitude()+"\t"
						+(float)hLoc.getLongitude()+"\t"
						+(float)hLoc.getDepth()+"\t"
						+(float)event.getMag()+"\t"
						+event.getParentID()+"\t"
						+event.getGeneration()+"\t"
						+(float)dist+"\t"
						+(float)event.getDistanceToParent()+"\t"
						+event.getERF_SourceIndex()+"\t"
						+event.getERF_RupIndex()+"\t"
						+erf.getSource(event.getERF_SourceIndex()).getName()+"\n");
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
			ProbEqkSource src = erf.getSource(s);
			
			// skip the source if it's to be ignored (blunt elastic rebound application)
			if(s==sourceID_ToIgnore) {
				System.out.println("Ignoring source "+s+" ("+src.getName()+") in the sampling of events");
				continue;
			}

			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				ProbEqkRupture rup = src.getRupture(r);
				ArbDiscrEmpiricalDistFunc numInEachNode = new ArbDiscrEmpiricalDistFunc(); // node on x-axis and num on y-axis
				EvenlyGriddedSurface surface = rup.getRuptureSurface();
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
		
		double cumRate = Math.round(10*ERF_Calculator.getTotalMFD_ForERF(meanUCERF2, 2.05,8.95, 70, true).getCumRate(5.05))/10.0;
		System.out.println("\nCumRate >= M5 for MeanUCERF2_ETAS: "+cumRate+"  (should be 6.8)\n");
				
		
		
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
		

		// Create the ETAS simulator
		int srcID_ToIgnore = -1; // none
//		int srcID_ToIgnore = 195; // landers
//		int srcID_ToIgnore = 223; // Northridge
		// SSAF is 42 to 98
		// Landers is 195
		// Northridge is 223
		ETAS_Simulator etasSimulator = new ETAS_Simulator(meanUCERF2,griddedRegion,srcID_ToIgnore);
		
//		etasSimulator.plotERF_MagFreqDists();
		
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


		// 195	Landers Rupture	 #rups=46 (rup 41 for M 7.25)
		mainShock = meanUCERF2.getSource(195).getRupture(41);
		double distDecay = 1.7;
		double minDist = 0.3;
		String info = "Landers Rupture (M="+mainShock.getMag()+"); distDecay="+distDecay;
		etasSimulator.runTests(mainShock,info, 195, rootDir+"Landers_decay1pt7_withSrc/",distDecay,minDist);
	*/
				
		// 223	Northridge	 #rups=13	(rup 8 for M 6.75)
		mainShock = meanUCERF2.getSource(223).getRupture(8);
		double distDecay = 1.7;
		double minDist = 0.3;
		String info = "Northridge Rupture (M="+mainShock.getMag()+"); distDecay="+distDecay;
		etasSimulator.runTests(mainShock,info, 223, rootDir+"Northridge_decay1pt7_withSrc/",distDecay,minDist);
		/*
		

		// 236	Pitas Point (Lower, West)	 #rups=19	13.0 (shallowest dipping rupture I could find)
		mainShock = meanUCERF2.getSource(236).getRupture(10);
		String info = "Pitas Point (shallowest dipping source); M="+mainShock.getMag()+"; AveDip="+mainShock.getRuptureSurface().getAveDip();
		etasSimulator.runTests(mainShock,info,null);
*/

		runtime = (System.currentTimeMillis()-startRunTime)/1000;
		System.out.println("Test Run took "+runtime+" seconds");
		
	}
	
	
	public ArrayList<PrimaryAftershock> getAllAftershocks(PrimaryAftershock mainShock) {
		int generation = 0;
		totalExpectedNumMagDist = new SummedMagFreqDist(2.05, 8.95, 70);
		ArrayList<PrimaryAftershock> mainShocksToProcess = new ArrayList<PrimaryAftershock>();
		ArrayList<PrimaryAftershock> allEvents = new ArrayList<PrimaryAftershock>();
		mainShocksToProcess.add(mainShock);
//		allEvents.add(mainShock);
		int numToProcess = mainShocksToProcess.size();
		
		while(numToProcess > 0) {
			generation +=1;
			System.out.println("WORKING ON GENERATION: "+generation+ "  (numToProcess="+numToProcess+")");
			ArrayList<PrimaryAftershock> aftershockList = new ArrayList<PrimaryAftershock>();
			CalcProgressBar progressBar = new CalcProgressBar("ETAS_Simulator", "Progress on Generation "+generation);
			progressBar.displayProgressBar();
			for(int i=0;i<numToProcess; i++) {
				progressBar.updateProgress(i,numToProcess);
//				System.out.print(numToProcess-i);
				aftershockList.addAll(getPrimaryAftershocksList(mainShocksToProcess.get(i)));
				// add to the total expected MFD
				ArbIncrementalMagFreqDist expMFD = etas_sampler.getMagProbDist();
				expMFD.multiplyY_ValsBy(expectedNum);
				totalExpectedNumMagDist.addIncrementalMagFreqDist(expMFD);
			}	
			
			// set the IDs & generation
			int firstID = allEvents.size();
			for(int i=0;i<aftershockList.size(); i++) {
				PrimaryAftershock aShock = aftershockList.get(i);
				aShock.setID(i+firstID);
				aShock.setGeneration(generation);
			}
			allEvents.addAll(aftershockList);
			mainShocksToProcess = aftershockList;
//			numToProcess = 0;
			numToProcess = mainShocksToProcess.size();
			progressBar.dispose();

		}
		System.out.println("Total Num Aftershocks="+allEvents.size());

		return allEvents;

	}

	
	/**
	 * This runs some tests for the given main shock
	 * @param mainShock
	 * @param info - plotting label
	 * @param srcIndex - the index of a source to compare results with
	 */
	public void runTests(ProbEqkRupture mainShock, String info, Integer srcIndex, String dirName, double distDecay, double minDist) {
		
		this.distDecay = distDecay;
		this.minDist = minDist;
		
		dirToSaveData = dirName;
		
		this.mainShock = mainShock;
				
		// make the directory
		File file1 = new File(dirName);
		file1.mkdirs();

		// convert mainShock to PrimaryAftershock so it has the origina time
		PrimaryAftershock mainShockConverted = new PrimaryAftershock(mainShock);
		mainShockConverted.setOriginTime(tMin);
		mainShockConverted.setID(-1);
		mainShockConverted.setGeneration(0);
		
		infoForOutputFile = new String();
		infoForOutputFile += "Run for "+info+"\n\n";
		infoForOutputFile += "sourceID_ToIgnore = "+sourceID_ToIgnore;
		if(sourceID_ToIgnore != -1)
			infoForOutputFile += " ("+erf.getSource(sourceID_ToIgnore).getName()+")\n\n";
		else
			infoForOutputFile += " (null)\n\n";

		infoForOutputFile += "ERF Parameter Settings for "+erf.getName()+": "+erf.getAdjustableParameterList().toString()+"\n\n";
		
		infoForOutputFile += "ETAS Param Settings:\n"+
			"\n\tdistDecay="+distDecay+
			"\n\tminDist="+minDist+
			"\n\ttMin="+tMin+
			"\n\ttMax="+tMax;
		for(String str: etasUtils.getDefaultParametersAsStrings()) {
			infoForOutputFile += "\n\t"+str;
		}
		infoForOutputFile += "\n\n";

		// get the aftershocks
		long startRunTime=System.currentTimeMillis();
		allAftershocks = getAllAftershocks(mainShockConverted);
		double runtime = (System.currentTimeMillis()-startRunTime)/1000;
		infoForOutputFile += "Generating all aftershocks took "+runtime+" seconds ("+(float)(runtime/60)+" minutes)\n\n";

		
		System.out.println("Num aftershocks = "+allAftershocks.size());
		infoForOutputFile += "Num aftershocks = "+allAftershocks.size()+"\n\n";

		// get the number of aftershocks in each generation
		int[] numEventsInEachGeneration = new int[10];
		for(PrimaryAftershock aShock:allAftershocks) {
			int gen = aShock.getGeneration();
			numEventsInEachGeneration[gen] += 1;
		}
		String numInGenString = "Num events in each generation:\n"+
				"\n\t1st\t"+numEventsInEachGeneration[1]+
				"\n\t2nd\t"+numEventsInEachGeneration[2]+
				"\n\t3rd\t"+numEventsInEachGeneration[3]+
				"\n\t4th\t"+numEventsInEachGeneration[4]+
				"\n\t5th\t"+numEventsInEachGeneration[5]+
				"\n\t6th\t"+numEventsInEachGeneration[6]+
				"\n\t7th\t"+numEventsInEachGeneration[7]+
				"\n\t8th\t"+numEventsInEachGeneration[8]+
				"\n\t9th\t"+numEventsInEachGeneration[9];
		System.out.println(numInGenString);
		infoForOutputFile += numInGenString+"\n\n";
		
		
		// write out any occurrences of srcIndex aftershocks:
		infoForOutputFile += erf.getSource(srcIndex).getName()+" (srcId="+srcIndex+") aftershocks:\n\n";
		ArrayList<PrimaryAftershock> srcID_aftershocks = new ArrayList<PrimaryAftershock>();
		for(PrimaryAftershock aShock:allAftershocks)
			if(aShock.getERF_SourceIndex() == srcIndex)
				srcID_aftershocks.add(aShock);
		if(srcID_aftershocks.size()==0)
			infoForOutputFile += "\tNone\n";
		else {
			infoForOutputFile += "\tid\tmag\tgen\tparID\trupID\tsrcID\toriginTime\tdistToParent\n";
			for(PrimaryAftershock aShock:srcID_aftershocks) {
				infoForOutputFile += "\t"+aShock.getID()+
				"\t"+aShock.getMag()+
				"\t"+aShock.getGeneration()+
				"\t"+aShock.getParentID()+
				"\t"+aShock.getERF_RupIndex()+
				"\t"+aShock.getERF_SourceIndex()+
				"\t"+(float)aShock.getOriginTime()+
				"\t"+(float)aShock.getDistanceToParent()+"\n";
			}
		}
		infoForOutputFile += "\n";
		
		// write out event larger that M 6.5
		infoForOutputFile += "Aftershocks larger than M 6.5:\n\n";
		ArrayList<PrimaryAftershock> largeAftershocks = new ArrayList<PrimaryAftershock>();
		for(PrimaryAftershock aShock:allAftershocks)
			if(aShock.getMag()>6.5)
				largeAftershocks.add(aShock);
		if(largeAftershocks.size()==0)
			infoForOutputFile += "\tNone\n";
		else {
			infoForOutputFile += "\tid\tmag\tgen\tparID\trupID\tsrcID\toriginTime\tdistToParent\tsrcName\n";
			for(PrimaryAftershock aShock:largeAftershocks) {
				infoForOutputFile += "\t"+aShock.getID()+
				"\t"+(float)aShock.getMag()+
				"\t"+aShock.getGeneration()+
				"\t"+aShock.getParentID()+
				"\t"+aShock.getERF_RupIndex()+
				"\t"+aShock.getERF_SourceIndex()+
				"\t"+(float)aShock.getOriginTime()+
				"\t"+(float)aShock.getDistanceToParent()+
				"\t"+erf.getSource(aShock.getERF_SourceIndex()).getName()+"\n";
			}
		}
		infoForOutputFile += "\n";

		
		String infoText = plotMagFreqDists(srcIndex, info, true);

		infoForOutputFile += infoText;	// this writes the total num expected above M 6.5, plus that from the srcIndex
		
		writeAftershockDataToFile(allAftershocks, dirToSaveData+"eventList.txt");
				
		plotDistDecayForAshocks(info, true, mainShock);
		
		plotEpicenterMap(info, true, mainShock);
		
		String mapMetadata = etas_FirstGenSampler.plotBlockProbMap(info);
		infoForOutputFile += "\n"+mapMetadata+".  The allFiles.zip should have been"+
				" moved to this dir, uncompressed, and the name changed from allFiles to samplingProbMaps.\n\n";
		
		plotNumVsTime(info, true, mainShock);
		
		
		//write info file
		try{
			FileWriter fw = new FileWriter(dirToSaveData+"INFO.txt");
			fw.write(infoForOutputFile);
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

			
	}
	
	
	
	

	
	
	
	
	public void plotDistDecayForAshocks(String info, boolean savePDF_File, ProbEqkRupture mainShock) {
		
		double delta = 10;
		ArrayList<EvenlyDiscretizedFunc> distDecayFuncs = etas_FirstGenSampler.getDistDecayTestFuncs(delta);
		distDecayFuncs.get(0).setName("Approx Expected Distance Decay for Primary Aftershocks of "+info);
		distDecayFuncs.get(0).setInfo("Diff from theoretical mostly due to no events to sample outside RELM region, but also spatially variable a-values");
		distDecayFuncs.get(1).setName("Theoretical Distance Decay");
		distDecayFuncs.get(1).setInfo("(dist+minDist)^-distDecay, where minDist="+minDist+" and distDecay="+distDecay+", and where finite discretization accounted for");
		EvenlyDiscretizedFunc tempFunc = distDecayFuncs.get(0);
		EvenlyDiscretizedFunc obsPrimaryDistHist = new EvenlyDiscretizedFunc(delta/2, tempFunc.getNum(), tempFunc.getDelta());
		obsPrimaryDistHist.setTolerance(tempFunc.getTolerance());
		EvenlyDiscretizedFunc obsAllDistHist = new EvenlyDiscretizedFunc(delta/2, tempFunc.getNum(), tempFunc.getDelta());
		obsAllDistHist.setTolerance(tempFunc.getTolerance());
		double totAllNum = 0, totPrimaryNum = 0;
		for (PrimaryAftershock event : allAftershocks) {
			if(event.getGeneration()==1) {
				obsPrimaryDistHist.add(event.getDistanceToParent(), 1.0);
				totPrimaryNum += 1;	
				obsAllDistHist.add(event.getDistanceToParent(), 1.0);
				totAllNum += 1;
			}
			// get distance to mainshock for the rest of the ruptures
			else {
				double dist = LocationUtils.distanceToSurfFast(event.getHypocenterLocation(), mainShock.getRuptureSurface());
				obsAllDistHist.add(dist, 1.0);
				totAllNum += 1;
			}
		}
		for(int i=0; i<obsPrimaryDistHist.getNum();i++) {
			obsPrimaryDistHist.set(i, obsPrimaryDistHist.getY(i)/totPrimaryNum);		// convert to PDF
			obsAllDistHist.set(i, obsAllDistHist.getY(i)/totAllNum);					// convert to PDF
		}
		obsPrimaryDistHist.setName("Sampled Distance-Decay Histogram for Primary Aftershocks of "+info);
		obsPrimaryDistHist.setInfo("(filled circles)");
		distDecayFuncs.add(obsPrimaryDistHist);
		obsAllDistHist.setName("Sampled Distance-Decay Histogram for All Aftershocks of "+info);
		obsAllDistHist.setInfo("(Crosses, and these are distances to the main shock, not to the parent)");
		distDecayFuncs.add(obsAllDistHist);

		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(distDecayFuncs, "Distance Decay for Aftershocks of "+info); 
		graph.setX_AxisLabel("Distance (km)");
		graph.setY_AxisLabel("Fraction of Aftershocks");
		graph.setX_AxisRange(0.4, 1200);
		graph.setY_AxisRange(1e-6, 1);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,Color.BLACK, 2));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,Color.BLUE, 2));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,Color.RED, 3));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,Color.GREEN, 3));
		graph.setPlottingFeatures(plotChars);
		graph.setYLog(true);
		graph.setXLog(true);
		if(savePDF_File)
		try {
			graph.saveAsPDF(dirToSaveData+"primaryAshocksDistDecay.pdf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
