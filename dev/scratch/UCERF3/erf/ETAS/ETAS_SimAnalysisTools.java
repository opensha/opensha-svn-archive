package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.erf.FaultSystemSolutionTimeDepERF;
import scratch.UCERF3.erf.ETAS.ETAS_PrimaryEventSampler;
import scratch.ned.ETAS_ERF.testModels.TestModel1_ERF;
import scratch.ned.ETAS_ERF.testModels.TestModel1_FSS;
import scratch.ned.ETAS_Tests.PrimaryAftershock;


public class ETAS_SimAnalysisTools {

	
	/**
	 * 
	 * @param info
	 * @param pdf_FileNameFullPath - set null is not PDF plot desired
	 * @param mainShock - leave null if not available or desired
	 * @param allAftershocks
	 */
	public static void plotEpicenterMap(String info, String pdf_FileNameFullPath, ObsEqkRupture mainShock, 
			Collection<ETAS_EqkRupture> allAftershocks, LocationList regionBorder) {
		
		ArrayList<AbstractXY_DataSet> funcs = new ArrayList<AbstractXY_DataSet>();
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();

		// M<5
		DefaultXY_DataSet epLocsGen0_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 0, allAftershocks);
		if(epLocsGen0_Mlt5.getNum()>0) {
			funcs.add(epLocsGen0_Mlt5);
			epLocsGen0_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.BLACK));
		}
		DefaultXY_DataSet epLocsGen1_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 1, allAftershocks);
		if(epLocsGen1_Mlt5.getNum()>0) {
			funcs.add(epLocsGen1_Mlt5);
			epLocsGen1_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.RED));
		}
		DefaultXY_DataSet epLocsGen2_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 2, allAftershocks);
		if(epLocsGen2_Mlt5.getNum()>0) {
			funcs.add(epLocsGen2_Mlt5);
			epLocsGen2_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.BLUE));
		}
		DefaultXY_DataSet epLocsGen3_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 3, allAftershocks);
		if(epLocsGen3_Mlt5.getNum()>0) {
			funcs.add(epLocsGen3_Mlt5);
			epLocsGen3_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.GREEN));
		}
		DefaultXY_DataSet epLocsGen4_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 4, allAftershocks);
		if(epLocsGen4_Mlt5.getNum()>0) {
			funcs.add(epLocsGen4_Mlt5);
			epLocsGen4_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.LIGHT_GRAY));
		}
		DefaultXY_DataSet epLocsGen5_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 5, allAftershocks);
		if(epLocsGen5_Mlt5.getNum()>0) {
			funcs.add(epLocsGen5_Mlt5);
			epLocsGen5_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.ORANGE));
		}
		DefaultXY_DataSet epLocsGen6_Mlt5 = getEpicenterLocsXY_DataSet(2.0, 5.0, 6, allAftershocks);
		if(epLocsGen6_Mlt5.getNum()>0) {
			funcs.add(epLocsGen6_Mlt5);
			epLocsGen6_Mlt5.setInfo("(circles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 1f, Color.YELLOW));
		}


		// 5.0<=M<6.5
		DefaultXY_DataSet epLocsGen0_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 0, allAftershocks);
		if(epLocsGen0_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen0_Mgt5lt65);
			epLocsGen0_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.BLACK));
		}
		DefaultXY_DataSet epLocsGen1_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 1, allAftershocks);
		if(epLocsGen1_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen1_Mgt5lt65);
			epLocsGen1_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.RED));
		}
		DefaultXY_DataSet epLocsGen2_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 2, allAftershocks);
		if(epLocsGen2_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen2_Mgt5lt65);
			epLocsGen2_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.BLUE));
		}
		DefaultXY_DataSet epLocsGen3_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 3, allAftershocks);
		if(epLocsGen3_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen3_Mgt5lt65);
			epLocsGen3_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.GREEN));
		}
		DefaultXY_DataSet epLocsGen4_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 4, allAftershocks);
		if(epLocsGen4_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen4_Mgt5lt65);
			epLocsGen4_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.LIGHT_GRAY));
		}
		DefaultXY_DataSet epLocsGen5_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 5, allAftershocks);
		if(epLocsGen5_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen5_Mgt5lt65);
			epLocsGen5_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.ORANGE));
		}
		DefaultXY_DataSet epLocsGen6_Mgt5lt65 = getEpicenterLocsXY_DataSet(5.0, 6.5, 6, allAftershocks);
		if(epLocsGen6_Mgt5lt65.getNum()>0) {
			funcs.add(epLocsGen6_Mgt5lt65);
			epLocsGen6_Mgt5lt65.setInfo("(triangles)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, 4f, Color.YELLOW));
		}


		// 6.5<=M<9.0
		DefaultXY_DataSet epLocsGen0_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 0, allAftershocks);
		if(epLocsGen0_Mgt65.getNum()>0) {
			funcs.add(epLocsGen0_Mgt65);
			epLocsGen0_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.BLACK));
		}
		DefaultXY_DataSet epLocsGen1_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 1, allAftershocks);
		if(epLocsGen1_Mgt65.getNum()>0) {
			funcs.add(epLocsGen1_Mgt65);
			epLocsGen1_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.RED));
		}
		DefaultXY_DataSet epLocsGen2_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 2, allAftershocks);
		if(epLocsGen2_Mgt65.getNum()>0) {
			funcs.add(epLocsGen2_Mgt65);
			epLocsGen2_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.BLUE));
		}
		DefaultXY_DataSet epLocsGen3_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 3, allAftershocks);
		if(epLocsGen3_Mgt65.getNum()>0) {
			funcs.add(epLocsGen3_Mgt65);
			epLocsGen3_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.GREEN));
		}
		DefaultXY_DataSet epLocsGen4_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 4, allAftershocks);
		if(epLocsGen4_Mgt65.getNum()>0) {
			funcs.add(epLocsGen4_Mgt65);
			epLocsGen4_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.LIGHT_GRAY));
		}
		DefaultXY_DataSet epLocsGen5_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 5, allAftershocks);
		if(epLocsGen5_Mgt65.getNum()>0) {
			funcs.add(epLocsGen5_Mgt65);
			epLocsGen5_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.ORANGE));
		}
		DefaultXY_DataSet epLocsGen6_Mgt65 = getEpicenterLocsXY_DataSet(6.5, 9.0, 6, allAftershocks);
		if(epLocsGen6_Mgt65.getNum()>0) {
			funcs.add(epLocsGen6_Mgt65);
			epLocsGen6_Mgt65.setInfo("(squares)");
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.SQUARE, 8f, Color.YELLOW));
		}
		
		double minLat=90, maxLat=-90,minLon=360,maxLon=-360;
		for(AbstractXY_DataSet func:funcs) {
//			System.out.println(func.getMinX()+"\t"+func.getMaxX()+"\t"+func.getMinY()+"\t"+func.getMaxY());
			if(func.getMaxX()>maxLon) maxLon = func.getMaxX();
			if(func.getMinX()<minLon) minLon = func.getMinX();
			if(func.getMaxY()>maxLat) maxLat = func.getMaxY();
			if(func.getMinY()<minLat) minLat = func.getMinY();
		}
		
//		System.out.println("latDada\t"+minLat+"\t"+maxLat+"\t"+minLon+"\t"+maxLon+"\t");
		
		if(mainShock != null) {
			FaultTrace trace = mainShock.getRuptureSurface().getEvenlyDiscritizedUpperEdge();
			DefaultXY_DataSet traceFunc = new DefaultXY_DataSet();
			traceFunc.setName("Main Shock Trace");
			for(Location loc:trace)
				traceFunc.set(loc.getLongitude(), loc.getLatitude());
			funcs.add(traceFunc);
			plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.MAGENTA));
		}
		
		// now plot non point source aftershocks
		int lai=0;
		for(ETAS_EqkRupture rup : allAftershocks) {
			if(!rup.getRuptureSurface().isPointSurface()) {
				FaultTrace trace = rup.getRuptureSurface().getEvenlyDiscritizedUpperEdge();
				DefaultXY_DataSet traceFunc = new DefaultXY_DataSet();
				traceFunc.setName("Large aftershock "+lai);
				for(Location loc:trace)
					traceFunc.set(loc.getLongitude(), loc.getLatitude());
				funcs.add(traceFunc);
				plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
				lai+=1;
			}
		}
		
		// now plot the region border if not null
		if(regionBorder != null) {
			DefaultXY_DataSet regBorderFunc = new DefaultXY_DataSet();
			regBorderFunc.setName("Region Border");
			for(Location loc: regionBorder) {
				regBorderFunc.set(loc.getLongitude(), loc.getLatitude());
			}
			// close the polygon:
			regBorderFunc.set(regBorderFunc.get(0).getX(), regBorderFunc.get(0).getY());
			funcs.add(regBorderFunc);
			plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		}

		GraphWindow graph = new GraphWindow(funcs, "Aftershock Epicenters for "+info); 
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
		
//		// ****** HACK FOR SSA TALK ************ (delete next two lines when done)
//		graph.setX_AxisRange(-120, -116);
//		graph.setY_AxisRange(35, 37);

		
		graph.setPlotChars(plotChars);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(16);
		graph.setTickLabelFontSize(14);

		if(pdf_FileNameFullPath != null)
		try {
			graph.saveAsPDF(pdf_FileNameFullPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	private static DefaultXY_DataSet getEpicenterLocsXY_DataSet(double magLow, double magHigh, int generation,
			Collection<ETAS_EqkRupture> allAftershocks) {
		DefaultXY_DataSet epicenterLocs = new DefaultXY_DataSet();
		for (ETAS_EqkRupture event : allAftershocks) {
			if(event.getMag()>=magLow && event.getMag()<magHigh && event.getGeneration()==generation)
				epicenterLocs.set(event.getHypocenterLocation().getLongitude(), event.getHypocenterLocation().getLatitude());
		}
		epicenterLocs.setName("Generation "+generation+" Aftershock Epicenters for "+magLow+"<=Mag<"+magHigh);
		return epicenterLocs;
	}
	
	
	public static void plotMagFreqDists(String info, String pdf_FileNameFullPath, 
			Collection<ETAS_EqkRupture> allAftershocks) {
		
		// get the observed mag-prob dist for the sampled set of events 
		ArbIncrementalMagFreqDist allAftShMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		ArbIncrementalMagFreqDist primaryAftShMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
//		ArbIncrementalMagFreqDist obsMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		for (ETAS_EqkRupture event : allAftershocks) {
			allAftShMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
			if(event.getGeneration()==1)
				primaryAftShMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
		}
		allAftShMagProbDist.setName("All Aftershock MFD");
		allAftShMagProbDist.setInfo(" ");
		primaryAftShMagProbDist.setName("Primary Aftershock MFD");
		primaryAftShMagProbDist.setInfo(" ");
		
		ArrayList<EvenlyDiscretizedFunc> magProbDists = new ArrayList<EvenlyDiscretizedFunc>();
		magProbDists.add(allAftShMagProbDist);
		magProbDists.add(primaryAftShMagProbDist);
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));

		
//		// HACK FOR SSA TALK ******************
//		TestModel1_FSS temp = new TestModel1_FSS();
//		GutenbergRichterMagFreqDist targetMFD = temp.getTargetFaultGR(); 
//		targetMFD.normalizeByTotalRate();
//		targetMFD.scale(ETAS_Utils.getDefaultExpectedNumEvents(6.93, 0, 360.25));
//		System.out.println("targetMFD.getTotalIncrRate()"+targetMFD.getTotalIncrRate());
//		targetMFD.setName("Target Primary MFD");
//		targetMFD.setInfo(" ");
//		magProbDists.add(targetMFD);
//		magProbDists.add(targetMFD.getCumRateDistWithOffset());
//		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
//		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
//
//		//****************************

				
		// Plot these MFDs
		GraphWindow magProbDistsGraph = new GraphWindow(magProbDists, "Mag-Freq Distributions for "+info+" Aftershocks",plotChars); 
		magProbDistsGraph.setX_AxisLabel("Mag");
		magProbDistsGraph.setY_AxisLabel("Number");
		magProbDistsGraph.setY_AxisRange(0.001, 1e3);
		magProbDistsGraph.setX_AxisRange(2d, 8d);
		magProbDistsGraph.setYLog(true);
		magProbDistsGraph.setPlotLabelFontSize(18);
		magProbDistsGraph.setAxisLabelFontSize(16);
		magProbDistsGraph.setTickLabelFontSize(14);
		
		if(pdf_FileNameFullPath != null) {
			try {
				magProbDistsGraph.saveAsPDF(pdf_FileNameFullPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	
	
	
	/**
	 * This plots the number of aftershocks versus log10-distance from the parent, 
	 * and if a "mainShock" is provided, also from this event.  Also plotted is the expected distance decay.
	 * @param info - string describing the given mainShock (below); set null if mainShock is also null
	 * @param pdf_FileName - full path name of PDF files to save to (leave null if not wanted)
	 * @param simulatedRupsQueue - list of sampled events
	 * @param mainShock - distances of all aftershocks  will be computed to this event if it's not null
	 */
	public static void plotDistDecayHistForAshocks(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, 
			EqkRupture mainShock, double distDecay, double minDist) {
		
		double histLogMin=-2.0;;
		double histLogMax = 4.0;
		int histNum = 31;
		EvenlyDiscretizedFunc expectedLogDistDecay = ETAS_Utils.getTargetDistDecayFunc(histLogMin, histLogMax, histNum, distDecay, minDist);
		expectedLogDistDecay.setName("Expected Log-Dist Decay");
		expectedLogDistDecay.setInfo("(distDecay="+distDecay+" and minDist="+minDist+")");

		EvenlyDiscretizedFunc obsLogDistDecayHist = new EvenlyDiscretizedFunc(histLogMin, histLogMax, histNum);
		obsLogDistDecayHist.setTolerance(obsLogDistDecayHist.getDelta());
		obsLogDistDecayHist.setName("Observed Log_Dist Decay Histogram");
		
		// this is for distances from the specified main shock
		EvenlyDiscretizedFunc obsLogDistDecayFromMainShockHist = new EvenlyDiscretizedFunc(histLogMin, histLogMax, histNum);
		obsLogDistDecayFromMainShockHist.setName("Observed Log_Dist Decay From Specified Minshock Histogram");
		obsLogDistDecayFromMainShockHist.setTolerance(obsLogDistDecayHist.getDelta());


		double numFromMainShock = 0, numFromPrimary = 0;
		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			if(event.getGeneration()>0) {	// skip spontaneous events
				double logDist = Math.log10(event.getDistanceToParent());
				if(logDist<histLogMin) {
					obsLogDistDecayHist.add(0, 1.0);
				}
				else if(logDist<histLogMax) {
					obsLogDistDecayHist.add(logDist, 1.0);
				}
				numFromPrimary += 1;	
//}
				if(mainShock != null) {	// might want to try leaving spontaneous events in this?
					logDist = Math.log10(LocationUtils.distanceToSurfFast(event.getHypocenterLocation(), mainShock.getRuptureSurface()));
					if(logDist<histLogMin) {
						obsLogDistDecayFromMainShockHist.add(0, 1.0);
					}
					else if(logDist<histLogMax) {
						obsLogDistDecayFromMainShockHist.add(logDist, 1.0);
					}
					numFromMainShock += 1;
				}
			}
		}
				
		
		// normalize to PDF
		obsLogDistDecayHist.scale(1.0/(double)numFromPrimary);
		if(mainShock != null) {
			obsLogDistDecayFromMainShockHist.scale(1.0/(double)numFromMainShock);
			if(mainShock.getRuptureSurface().isPointSurface())
				System.out.println("mainShock Loc: "+mainShock.getRuptureSurface().getFirstLocOnUpperEdge());
		}
		
		// convert to PDF
		
		// Set num in info fields
		obsLogDistDecayHist.setInfo("(based on "+numFromPrimary+" aftershocks)");


		ArrayList distDecayFuncs = new ArrayList();
		distDecayFuncs.add(expectedLogDistDecay);
		distDecayFuncs.add(obsLogDistDecayHist);
		if(mainShock != null) {
			obsLogDistDecayFromMainShockHist.setInfo("(based on "+numFromMainShock+" aftershocks)");

			// TEMP HACK FOR SSA TALK
//			distDecayFuncs.add(obsLogDistDecayFromMainShockHist);			
		}

		GraphWindow graph = new GraphWindow(distDecayFuncs, "Distance Decay for Aftershocks"+info); 
		graph.setX_AxisLabel("Log10-Distance (km)");
		graph.setY_AxisLabel("Fraction of Aftershocks");
		graph.setX_AxisRange(histLogMin, histLogMax);
		graph.setY_AxisRange(1e-6, 1);

// TEMP HACK FOR SSA TALK
		graph.setX_AxisRange(-1.5, 3);
		graph.setY_AxisRange(1e-4, 0.3);
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 3f, Color.RED));
		if(mainShock != null)
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.GREEN));
		graph.setPlotChars(plotChars);
		graph.setYLog(true);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(16);
		graph.setTickLabelFontSize(14);
		if(pdf_FileName != null)
			try {
				graph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	public static void plotExpectedPrimaryMFD_ForRup(String rupInfo, String pdf_FileName,  ETAS_PrimaryEventSamplerTest1 etas_PrimEventSampler, EqkRupture rupture) {
		SummedMagFreqDist mfd = etas_PrimEventSampler.getExpectedMFD(rupture);
		// convert MFD to probability density function
		mfd.scale(1.0/(mfd.getTotalIncrRate()*mfd.getDelta()));
		mfd.setName("Expected MFD for primary aftershocks of "+rupInfo);
		GraphWindow magProbDistsGraph = new GraphWindow(mfd, "Expected Primary Aftershock MFD"); 
		magProbDistsGraph.setX_AxisLabel("Mag");
		magProbDistsGraph.setY_AxisLabel("Probability Density");
		magProbDistsGraph.setY_AxisRange(10e-9, 10e-1);
		magProbDistsGraph.setX_AxisRange(2., 9.);
		magProbDistsGraph.setYLog(true);
		magProbDistsGraph.setPlotLabelFontSize(22);
		magProbDistsGraph.setAxisLabelFontSize(20);
		magProbDistsGraph.setTickLabelFontSize(18);			
		if(pdf_FileName != null)
			try {
				magProbDistsGraph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}


	
	public static void plotNumVsTimePrimaryAftershocks(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue) {
		
		double delta = 1.0; // days
		double tMin=0;		//days
		double tMax=366;	//days
		
		ETAS_Utils etasUtils = new ETAS_Utils();

		// make the target function & change it to a PDF
		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithTimeFunc(7, tMin, tMax, delta);
		targetFunc.setName("Expected Temporal Decay for  First-generation Aftershocks");
		targetFunc.scale(1.0/targetFunc.calcSumOfY_Vals());
		
		int numPts = (int) Math.round(tMax-tMin);
		HistogramFunction firstGenEventTimes= new HistogramFunction(tMin+delta/2d,numPts,delta);
		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			if(event.getParentRup() != null) {
				double timeDays = (event.getOriginTime()-event.getParentRup().getOriginTime())/FaultSystemSolutionTimeDepERF.MILLISEC_PER_DAY;
				firstGenEventTimes.add(timeDays, 1.0);
			}
		}
		firstGenEventTimes.setName("Observed Temporal Decay for First-generation Aftershocks");
		firstGenEventTimes.setInfo(" ");
		firstGenEventTimes.normalizeBySumOfY_Vals();
		
		ArrayList funcs = new ArrayList();
		funcs.add(firstGenEventTimes);
		funcs.add(targetFunc);
		
		GraphWindow graph = new GraphWindow(funcs, "Temporal Decay for  First-generation Aftershocks for "+info); 
		graph.setX_AxisLabel("Days (since main shock)");
		graph.setY_AxisLabel("Num Events");
		graph.setX_AxisRange(0.4, 360);
		graph.setY_AxisRange(1e-5, graph.getY_AxisRange().getUpperBound());
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		graph.setPlotChars(plotChars);
		graph.setYLog(true);
		graph.setXLog(true);
		if(pdf_FileName != null)
			try {
				graph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}


	public static void plotNumVsTime(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, 
			ObsEqkRupture mainShock) {
		
		long startTimeMillis = mainShock.getOriginTime();
		double delta = 1.0; // days
		double tMin=0;		//days
		double tMax=366;	//days
		
		ETAS_Utils etasUtils = new ETAS_Utils();

		// make the target function & change it to a PDF
		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithTimeFunc(mainShock.getMag(), tMin, tMax, delta);
		targetFunc.setName("Expected Number for First-generation Aftershocks");
		
		int numPts = (int) Math.round(tMax-tMin);
		EvenlyDiscretizedFunc allEvents = new EvenlyDiscretizedFunc(tMin+0.5,numPts,delta);
		EvenlyDiscretizedFunc firstGenEvents= new EvenlyDiscretizedFunc(tMin+0.5,numPts,delta);
		allEvents.setTolerance(2.0);
		firstGenEvents.setTolerance(2.0);
		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			double time = (double)(event.getOriginTime()-startTimeMillis)/FaultSystemSolutionTimeDepERF.MILLISEC_PER_DAY;
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
		
		GraphWindow graph = new GraphWindow(funcs, "Num aftershocks per day for "+info); 
		graph.setX_AxisLabel("Days (since main shock)");
		graph.setY_AxisLabel("Num Events");
		graph.setX_AxisRange(0.4, 360);
		graph.setY_AxisRange(0.1, graph.getY_AxisRange().getUpperBound());
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		graph.setPlotChars(plotChars);
		graph.setYLog(true);
		graph.setXLog(true);
		if(pdf_FileName != null)
			try {
				graph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	/**
	 * This plots the number of events versus time from the given main shock.  Don't use this if 
	 * the simulation includes spontaneous events, or more than one input observed event (so
	 * that all aftershocks are ultimately spawned from the main shock here)
	 * @param info
	 * @param pdf_FileName
	 * @param simulatedRupsQueue
	 * @param mainShock
	 */
	public static void plotNumVsLogTime(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, 
			ObsEqkRupture mainShock) {
		
		long startTimeMillis = mainShock.getOriginTime();
		double firstLogDay = -4;
		double lastLocDay = 3;
		double deltaLogDay =0.1;
		int numPts = (int)Math.round((lastLocDay-firstLogDay)/deltaLogDay);
		
		ETAS_Utils etasUtils = new ETAS_Utils();

		// make the target function & change it to a PDF
		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithLogTimeFunc(mainShock.getMag(), firstLogDay, lastLocDay, deltaLogDay);
		targetFunc.setName("Expected Number for First-generation Aftershocks");
		
		EvenlyDiscretizedFunc allEvents = new EvenlyDiscretizedFunc(firstLogDay+deltaLogDay/2d,numPts,deltaLogDay);
		EvenlyDiscretizedFunc firstGenEvents= new EvenlyDiscretizedFunc(firstLogDay+deltaLogDay/2d,numPts,deltaLogDay);

		allEvents.setTolerance(deltaLogDay);
		firstGenEvents.setTolerance(deltaLogDay);
		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			long timeMillis = event.getOriginTime()-startTimeMillis;
			double logTimeYrs = Math.log10((double)timeMillis/FaultSystemSolutionTimeDepERF.MILLISEC_PER_DAY);
			if(logTimeYrs<=firstLogDay) {
				allEvents.add(0, 1.0);
				if(event.getGeneration() == 1)
					firstGenEvents.add(0, 1.0);
			}
			else {
				allEvents.add(logTimeYrs, 1.0);
				if(event.getGeneration() == 1)
					firstGenEvents.add(logTimeYrs, 1.0);
			}
		}
		allEvents.setName("All aftershocks");
		firstGenEvents.setName("First-generation aftershocks");
		allEvents.setInfo(" ");
		firstGenEvents.setInfo(" ");
		
		ArrayList funcs = new ArrayList();
		// TEMP HACK FOR SSA TALK
		funcs.add(allEvents);
		funcs.add(firstGenEvents);
		funcs.add(targetFunc);
		
		GraphWindow graph = new GraphWindow(funcs, "Temporal Aftershock Decay"); 
		graph.setX_AxisLabel("Log-day");
		graph.setY_AxisLabel("Num Events");
		graph.setX_AxisRange(firstLogDay, lastLocDay);
		graph.setY_AxisRange(0.1, graph.getY_AxisRange().getUpperBound());
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		// TEMP HACK FOR SSA TALK (delete first two when done, un-comment 3rd)
		graph.setX_AxisRange(-2.5, 3);
		graph.setY_AxisRange(1, 100);
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		graph.setPlotChars(plotChars);
		graph.setYLog(true);
//		graph.setXLog(true);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(16);
		graph.setTickLabelFontSize(14);
		if(pdf_FileName != null)
			try {
				graph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	
	/**
	 * This plots the distribution of event time for primary aftershocks, relative to the time of the parent.
	 * This assumes that event IDs for ruptures in the obsEqkRuptureList have been assigned as the index in that
	 * list.
	 * @param info
	 * @param pdf_FileName
	 * @param simulatedRupsQueue
	 */
	public static void plotNumVsLogTimePrimaryAftershocks(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue) {
		
		double firstLogDay = -4;
		double lastLocDay = 3;
		double deltaLogDay =0.1;
		int numPts = (int)Math.round((lastLocDay-firstLogDay)/deltaLogDay);
		
		ETAS_Utils etasUtils = new ETAS_Utils();

		// make the target function & change it to a PDF
		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithLogTimeFunc(7, firstLogDay, lastLocDay, deltaLogDay);	// any mangitude will do
		targetFunc.scale(1.0/targetFunc.calcSumOfY_Vals());
		targetFunc.setName("Expected Temporal Decay for First-generation Aftershocks");
		
		HistogramFunction firstGenEventTimes= new HistogramFunction(firstLogDay+deltaLogDay/2d,numPts,deltaLogDay);

		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			if(event.getParentRup() != null) {
				double timeMillis = event.getOriginTime()-event.getParentRup().getOriginTime();
				double logTimeDays = Math.log10(timeMillis/FaultSystemSolutionTimeDepERF.MILLISEC_PER_DAY);
				if(logTimeDays<=firstLogDay)
					firstGenEventTimes.add(0, 1.0);
				else
					firstGenEventTimes.add(logTimeDays, 1.0);
			}
		}
		firstGenEventTimes.setName("Observed Temporal Decay for First-generation Aftershocks");
		firstGenEventTimes.setInfo(" ");
		firstGenEventTimes.normalizeBySumOfY_Vals();
		
		ArrayList funcs = new ArrayList();
		funcs.add(firstGenEventTimes);
		funcs.add(targetFunc);
		
		GraphWindow graph = new GraphWindow(funcs, "Temporal Decay for First-generation Aftershocks"); 
		graph.setX_AxisLabel("Log-day");
		graph.setY_AxisLabel("Num Events");
//		graph.setX_AxisRange(firstLogDay, lastLocDay);
		graph.setY_AxisRange(1e-5, graph.getY_AxisRange().getUpperBound());
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		graph.setPlotChars(plotChars);
		graph.setYLog(true);
//		graph.setXLog(true);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(16);
		graph.setTickLabelFontSize(14);
		if(pdf_FileName != null)
			try {
				graph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	
	public static void writeEventDataToFile(String fileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue) {
		try{
			FileWriter fw1 = new FileWriter(fileName);
//			fw1.write("ID\tparID\tGen\tOrigTime\tdistToPar\n");
			fw1.write("ID\tparID\tGen\tOrigTime\tLat\tLon\tDep\n");
			for(ETAS_EqkRupture rup:simulatedRupsQueue) {
				Location hypoLoc = rup.getHypocenterLocation();
				fw1.write(rup.getID()+"\t"+rup.getParentID()+"\t"+rup.getGeneration()+"\t"+
						rup.getOriginTime()//+"\t"+rup.getDistanceToParent()
						+"\t"+hypoLoc.getLatitude()+"\t"+hypoLoc.getLongitude()+"\t"+hypoLoc.getDepth()+"\n");
			}
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();

		}
	}
	
	public static void writeEventToFile(FileWriter fileWriter, ETAS_EqkRupture rup) {
		try{
			Location hypoLoc = rup.getHypocenterLocation();
			fileWriter.write(rup.getNthERF_Index()+"\t"+rup.getID()+"\t"+rup.getParentID()+"\t"+rup.getGeneration()+"\t"+
					rup.getOriginTime()+"\t"+rup.getDistanceToParent()
					+"\t"+rup.getMag()+"\t"+hypoLoc.getLatitude()+"\t"+hypoLoc.getLongitude()+"\t"+hypoLoc.getDepth()+"\n");
		}catch(Exception e) {
			e.printStackTrace();

		}
	}

	
	public static void writeMemoryUse(String info) {
		Runtime runtime = Runtime.getRuntime();

	    NumberFormat format = NumberFormat.getInstance();

	    StringBuilder sb = new StringBuilder();
	    long maxMemory = runtime.maxMemory();
	    long allocatedMemory = runtime.totalMemory();
	    long freeMemory = runtime.freeMemory();

	    System.out.println(info);
	    System.out.println("\tin use memory: " + format.format((allocatedMemory-freeMemory) / 1024));
	    System.out.println("\tfree memory: " + format.format(freeMemory / 1024));
	    System.out.println("\tallocated memory: " + format.format(allocatedMemory / 1024));
	    System.out.println("\tmax memory: " + format.format(maxMemory / 1024));
	    System.out.println("\ttotal free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
	}
	


}
