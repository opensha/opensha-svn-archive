package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import org.dom4j.DocumentException;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol.Symbol;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;


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
			plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		}
		
		// now plot non point source aftershocks
		int lai=0;
		for(ETAS_EqkRupture rup : allAftershocks) {
			if(!rup.getRuptureSurface().isPointSurface()) {
				FaultTrace trace = rup.getRuptureSurface().getEvenlyDiscritizedUpperEdge();
				DefaultXY_DataSet traceFunc = new DefaultXY_DataSet();
				int gen=rup.getGeneration();
				traceFunc.setName("Large aftershock ID="+rup.getID()+";  generation="+gen+"; mag="+rup.getMag());
				for(Location loc:trace)
					traceFunc.set(loc.getLongitude(), loc.getLatitude());
				funcs.add(traceFunc);
				Color color;
				if(gen==0)
					color = Color.BLACK;
				else if(gen==1)
					color = Color.RED;
				else if(gen==2)
					color = Color.BLUE;
				else if(gen==3)
					color = Color.GREEN;
				else if(gen==4)
					color = Color.LIGHT_GRAY;
				else if(gen==5)
					color = Color.ORANGE;
				else// gen 6 and above
					color = Color.YELLOW;
				plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color));
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
			Collection<ETAS_EqkRupture> eventsList) {
		DefaultXY_DataSet epicenterLocs = new DefaultXY_DataSet();
		for (ETAS_EqkRupture event : eventsList) {
			if(event.getMag()>=magLow && event.getMag()<magHigh && event.getGeneration()==generation)
				epicenterLocs.set(event.getHypocenterLocation().getLongitude(), event.getHypocenterLocation().getLatitude());
		}
		epicenterLocs.setName("Generation "+generation+" Aftershock Epicenters for "+magLow+"<=Mag<"+magHigh);
		return epicenterLocs;
	}
	
	
	
	
	public static void plotMagFreqDists(String info, File resultsDir, Collection<ETAS_EqkRupture> eventsList) {
		
		// get the observed mag-prob dist for the sampled set of events 
		ArbIncrementalMagFreqDist allEventsMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		ArbIncrementalMagFreqDist aftershockMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		for (ETAS_EqkRupture event : eventsList) {
			allEventsMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
			if(event.getGeneration() != 0)
				aftershockMagProbDist.addResampledMagRate(event.getMag(), 1.0, true);
		}
		allEventsMagProbDist.setName("All Events MFD for simulation "+info);
		allEventsMagProbDist.setInfo("Total Num = "+allEventsMagProbDist.calcSumOfY_Vals());
		aftershockMagProbDist.setName("Aftershock MFD for simulation "+info);
		aftershockMagProbDist.setInfo("Total Num = "+aftershockMagProbDist.calcSumOfY_Vals());
		
		ArrayList<EvenlyDiscretizedFunc> magProbDists = new ArrayList<EvenlyDiscretizedFunc>();
		magProbDists.add(allEventsMagProbDist);
		magProbDists.add(aftershockMagProbDist);
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
				
		// Plot these MFDs
		GraphWindow magProbDistsGraph = new GraphWindow(magProbDists, "MFD for All Events",plotChars); 
		magProbDistsGraph.setX_AxisLabel("Mag");
		magProbDistsGraph.setY_AxisLabel("Number");
		magProbDistsGraph.setY_AxisRange(0.1, 1e3);
		magProbDistsGraph.setX_AxisRange(2d, 9d);
		magProbDistsGraph.setYLog(true);
		magProbDistsGraph.setPlotLabelFontSize(18);
		magProbDistsGraph.setAxisLabelFontSize(16);
		magProbDistsGraph.setTickLabelFontSize(14);
		
		
		ArrayList<EvenlyDiscretizedFunc> cumMagProbDists = new ArrayList<EvenlyDiscretizedFunc>();
		cumMagProbDists.add(allEventsMagProbDist.getCumRateDistWithOffset());
		cumMagProbDists.add(aftershockMagProbDist.getCumRateDistWithOffset());
		
		ArrayList<PlotCurveCharacterstics> plotCharsCum = new ArrayList<PlotCurveCharacterstics>();
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
				
		// Plot these MFDs
		GraphWindow cuMagProbDistsGraph = new GraphWindow(cumMagProbDists, "Cumulative MFD for All Events",plotCharsCum); 
		cuMagProbDistsGraph.setX_AxisLabel("Mag");
		cuMagProbDistsGraph.setY_AxisLabel("Number");
		cuMagProbDistsGraph.setY_AxisRange(0.1, 1e4);
		cuMagProbDistsGraph.setX_AxisRange(2d, 9d);
		cuMagProbDistsGraph.setYLog(true);
		cuMagProbDistsGraph.setPlotLabelFontSize(18);
		cuMagProbDistsGraph.setAxisLabelFontSize(16);
		cuMagProbDistsGraph.setTickLabelFontSize(14);
		
		if(resultsDir != null) {
			
			String pathName = new File(resultsDir,"simEventsMFD.pdf").getAbsolutePath();
			String pathNameCum = new File(resultsDir,"simEventsCumMFD.pdf").getAbsolutePath();
			try {
				magProbDistsGraph.saveAsPDF(pathName);
				cuMagProbDistsGraph.saveAsPDF(pathNameCum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * This computes two aftershock MFDs for the given rupture from the list of events: all aftershocks 
	 * and primary aftershocks (in that order).  These are the total count of events, not yearly rates.
	 * @param eventsList
	 * @param rupID
	 * @param info
	 */
	public static ArrayList<ArbIncrementalMagFreqDist> getAftershockMFDsForRup(Collection<ETAS_EqkRupture> eventsList, int rupID, String info) {
		// get the observed mag-prob dist for the sampled set of events 
		ArbIncrementalMagFreqDist allAftershocksMFD = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		ArbIncrementalMagFreqDist primaryAftershocksMFD = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		for (ETAS_EqkRupture event : eventsList) {
			ETAS_EqkRupture oldestAncestor = event.getOldestAncestor();
			if(oldestAncestor != null) {
				if(oldestAncestor.getID() == rupID) {
					allAftershocksMFD.addResampledMagRate(event.getMag(), 1.0, true);
					if(event.getGeneration() == 1)
						primaryAftershocksMFD.addResampledMagRate(event.getMag(), 1.0, true);
				}
			}
		}
		allAftershocksMFD.setName("MFD Histogram for all aftershocks of rupture (ID="+rupID+") for simulation "+info);
		allAftershocksMFD.setInfo("Total Num = "+allAftershocksMFD.calcSumOfY_Vals());
		primaryAftershocksMFD.setName("MFD Histogram of primary aftershocks of input rupture (ID="+rupID+") for simulation "+info);
		primaryAftershocksMFD.setInfo("Total Num = "+primaryAftershocksMFD.calcSumOfY_Vals());
		
		ArrayList<ArbIncrementalMagFreqDist> magProbDists = new ArrayList<ArbIncrementalMagFreqDist>();
		magProbDists.add(allAftershocksMFD);
		magProbDists.add(primaryAftershocksMFD);

		return magProbDists;
	}

	
	/**
	 * This plots the results returned by getAftershockMFDsForRup(Collection<ETAS_EqkRupture> eventsList, int rupID, String info)
	 * @param info
	 * @param resultsDir
	 * @param eventsList
	 * @param rupID
	 * @param mfds
	 */
	public static void plotMagFreqDistsForRup(String info, File resultsDir, int rupID, ArrayList<ArbIncrementalMagFreqDist> mfdList) {
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
				
		// Plot these MFDs
		GraphWindow magProbDistsGraph = new GraphWindow(mfdList, "MFD Histogram for Input Rupture",plotChars); 
		magProbDistsGraph.setX_AxisLabel("Mag");
		magProbDistsGraph.setY_AxisLabel("Number");
		magProbDistsGraph.setY_AxisRange(0.1, 1e3);
		magProbDistsGraph.setX_AxisRange(2d, 9d);
		magProbDistsGraph.setYLog(true);
		magProbDistsGraph.setPlotLabelFontSize(18);
		magProbDistsGraph.setAxisLabelFontSize(16);
		magProbDistsGraph.setTickLabelFontSize(14);
		
		
		ArrayList<EvenlyDiscretizedFunc> cumMagProbDists = new ArrayList<EvenlyDiscretizedFunc>();
		cumMagProbDists.add(mfdList.get(0).getCumRateDistWithOffset());
		cumMagProbDists.add(mfdList.get(1).getCumRateDistWithOffset());
		
		ArrayList<PlotCurveCharacterstics> plotCharsCum = new ArrayList<PlotCurveCharacterstics>();
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
				
		// Plot these MFDs
		GraphWindow cuMagProbDistsGraph = new GraphWindow(cumMagProbDists, "Cumulative MFD Hist for RupID="+rupID,plotCharsCum); 
		cuMagProbDistsGraph.setX_AxisLabel("Mag");
		cuMagProbDistsGraph.setY_AxisLabel("Number");
		cuMagProbDistsGraph.setY_AxisRange(0.1, 1e4);
		cuMagProbDistsGraph.setX_AxisRange(2d, 9d);
		cuMagProbDistsGraph.setYLog(true);
		cuMagProbDistsGraph.setPlotLabelFontSize(18);
		cuMagProbDistsGraph.setAxisLabelFontSize(16);
		cuMagProbDistsGraph.setTickLabelFontSize(14);
		
		if(resultsDir != null) {
			
			String pathName = new File(resultsDir,"AshocksOfInputRupMFD.pdf").getAbsolutePath();
			String pathNameCum = new File(resultsDir,"AshocksOfInputRupCumMFD.pdf").getAbsolutePath();
			try {
				magProbDistsGraph.saveAsPDF(pathName);
				cuMagProbDistsGraph.saveAsPDF(pathNameCum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * This plots a PDF of the number of aftershocks versus log10-distance from the parent.  Also plotted 
	 * is the expected distance decay.
	 * @param info - string describing data
	 * @param pdf_FileName - full path name of PDF files to save to (leave null if not wanted)
	 * @param simulatedRupsQueue - list of sampled events
	 * @param distDecay - ETAS distance decay for expected curve
	 * @param minDist - ETAS min distance for expected curve
	 */
	public static void plotDistDecayHistForAshocks(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, 
			double distDecay, double minDist) {
		
		double histLogMin=-2.0;;
		double histLogMax = 4.0;
		int histNum = 31;
		EvenlyDiscretizedFunc expectedLogDistDecay = ETAS_Utils.getTargetDistDecayFunc(histLogMin, histLogMax, histNum, distDecay, minDist);
		expectedLogDistDecay.setName("Expected Dist Decay");
		expectedLogDistDecay.setInfo("(distDecay="+distDecay+" and minDist="+minDist+")");

		EvenlyDiscretizedFunc obsLogDistDecayHist = new EvenlyDiscretizedFunc(histLogMin, histLogMax, histNum);
		obsLogDistDecayHist.setTolerance(obsLogDistDecayHist.getDelta());
		obsLogDistDecayHist.setName("Observed Dist Decay (relative to parent)");
		
		double numFromPrimary = 0;
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
			}
		}
				
		
		// normalize to PDF
		obsLogDistDecayHist.scale(1.0/(double)numFromPrimary);
		
		// Set num in info fields
		obsLogDistDecayHist.setInfo("(based on "+numFromPrimary+" aftershocks)");

		ArrayList<EvenlyDiscretizedFunc> distDecayFuncs = new ArrayList<EvenlyDiscretizedFunc>();
		distDecayFuncs.add(expectedLogDistDecay);
		distDecayFuncs.add(obsLogDistDecayHist);

		GraphWindow graph = new GraphWindow(distDecayFuncs, "Distance Decay for all Aftershocks "+info); 
		graph.setX_AxisLabel("Log10-Distance (km)");
		graph.setY_AxisLabel("Fraction of Aftershocks");
		graph.setX_AxisRange(histLogMin, histLogMax);
		graph.setY_AxisRange(1e-6, 1);

		graph.setX_AxisRange(-1.5, 3);
		graph.setY_AxisRange(1e-4, 0.3);
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 3f, Color.RED));
		graph.setPlotChars(plotChars);
		graph.setYLog(true);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(16);
		graph.setTickLabelFontSize(14);
		if(pdf_FileName != null)
			try {
				graph.saveAsPDF(pdf_FileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	
	/**
	 * This plots a PDF of the number of aftershocks versus log10-distance for descendants of the specified rupture.  Also plotted 
	 * is the expected distance decay.
	 * @param info - string describing data
	 * @param pdf_FileName - full path name of PDF files to save to (leave null if not wanted)
	 * @param simulatedRupsQueue - list of sampled events
	 * @param distDecay - ETAS distance decay for expected curve
	 * @param minDist - ETAS min distance for expected curve
	 */
	public static void plotDistDecayHistOfAshocksForRup(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, 
			double distDecay, double minDist, int rupID) {
		
		double histLogMin=-2.0;;
		double histLogMax = 4.0;
		int histNum = 31;
		EvenlyDiscretizedFunc expectedLogDistDecay = ETAS_Utils.getTargetDistDecayFunc(histLogMin, histLogMax, histNum, distDecay, minDist);
		expectedLogDistDecay.setName("Expected Log-Dist Decay");
		expectedLogDistDecay.setInfo("(distDecay="+distDecay+" and minDist="+minDist+")");

		EvenlyDiscretizedFunc obsLogDistDecayHist = new EvenlyDiscretizedFunc(histLogMin, histLogMax, histNum);
		obsLogDistDecayHist.setTolerance(obsLogDistDecayHist.getDelta());
		obsLogDistDecayHist.setName("Observed Dist Decay Hist (relative to parent) for input rupture of simulation "+info);
		
		// this is for distances from the specified main shock
		EvenlyDiscretizedFunc obsLogDistDecayFromOldestAncestor = new EvenlyDiscretizedFunc(histLogMin, histLogMax, histNum);
		obsLogDistDecayFromOldestAncestor.setName("Observed Dist Decay (from orig rup surface) for input rupture of simulation "+info);
		obsLogDistDecayFromOldestAncestor.setTolerance(obsLogDistDecayHist.getDelta());

		double numFromOrigSurface = 0;
		double numFromParent = 0;
		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			ETAS_EqkRupture oldestAncestor = event.getOldestAncestor();
			if(oldestAncestor != null && oldestAncestor.getID() == rupID) {
				double logDist = Math.log10(event.getDistanceToParent());
				if(logDist<histLogMin) {
					obsLogDistDecayHist.add(0, 1.0);
				}
				else if(logDist<histLogMax) {
					obsLogDistDecayHist.add(logDist, 1.0);
				}
				numFromParent += 1;	
				
				// fill in distance from most ancient ancestor
				logDist = Math.log10(LocationUtils.distanceToSurfFast(event.getHypocenterLocation(), oldestAncestor.getRuptureSurface()));
				if(logDist<histLogMin) {
					obsLogDistDecayFromOldestAncestor.add(0, 1.0);
				}
				else if(logDist<histLogMax) {
					obsLogDistDecayFromOldestAncestor.add(logDist, 1.0);
				}
				numFromOrigSurface += 1;
			}
		}
				
		
		// normalize to PDF
		obsLogDistDecayHist.scale(1.0/(double)numFromParent);
		obsLogDistDecayFromOldestAncestor.scale(1.0/(double)numFromOrigSurface);
		
		// Set num in info fields
		obsLogDistDecayHist.setInfo("(based on "+numFromParent+" aftershocks)");
		obsLogDistDecayFromOldestAncestor.setInfo("(based on "+numFromOrigSurface+" aftershocks)");

		ArrayList<EvenlyDiscretizedFunc> distDecayFuncs = new ArrayList<EvenlyDiscretizedFunc>();
		distDecayFuncs.add(expectedLogDistDecay);
		distDecayFuncs.add(obsLogDistDecayHist);
		distDecayFuncs.add(obsLogDistDecayFromOldestAncestor);			

		GraphWindow graph = new GraphWindow(distDecayFuncs, "Aftershock Dist Decay for Input Rupture"); 
		graph.setX_AxisLabel("Log10-Distance (km)");
		graph.setY_AxisLabel("Fraction of Aftershocks");
		graph.setX_AxisRange(histLogMin, histLogMax);
		graph.setY_AxisRange(1e-6, 1);

		graph.setX_AxisRange(-1.5, 3);
		graph.setY_AxisRange(1e-4, 0.3);
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 3f, Color.RED));
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
				e.printStackTrace();
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
	public static void oldPlotDistDecayHistForAshocks(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, 
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
	
	
	/**
	 * This plots the expected MFD (PDF) of primary events, the expected number of secondary events 
	 * (as a function of magnitude), and the the expected cumulative MFD for primary events (if the
	 * given expNum is not NaN) from the given rupture.
	 * 
	 * This returns the expected cumulative MFD (or null is expNum=NaN)
	 * 
	 * @param rupInfo - Info String
	 * @param pdf_FileName - plots are saved if this is non null
	 * @param etas_PrimEventSampler
	 * @param rupture
	 * @param expNum - expected number of primary aftershocks
	 * @return
	 */
	public static EvenlyDiscretizedFunc plotExpectedPrimaryMFD_ForRup(String rupInfo, String pdf_FileName,  ETAS_PrimaryEventSampler etas_PrimEventSampler, 
			EqkRupture rupture, double expNum) {
		SummedMagFreqDist mfd = etas_PrimEventSampler.getExpectedMFD(rupture);
		// convert MFD to probability density function
		mfd.scale(1.0/(mfd.getTotalIncrRate()*mfd.getDelta()));
		mfd.setName("Expected MFD for primary aftershocks of "+rupInfo);
		mfd.setInfo("Data:\n"+mfd.getMetadataString());
		GraphWindow magProbDistsGraph = new GraphWindow(mfd, "Expected Primary Aftershock MFD"); 
		magProbDistsGraph.setX_AxisLabel("Mag");
		magProbDistsGraph.setY_AxisLabel("Probability Density");
		magProbDistsGraph.setY_AxisRange(10e-9, 10e-1);
		magProbDistsGraph.setX_AxisRange(2., 9.);
		magProbDistsGraph.setYLog(true);
		magProbDistsGraph.setPlotLabelFontSize(22);
		magProbDistsGraph.setAxisLabelFontSize(20);
		magProbDistsGraph.setTickLabelFontSize(18);			
		
		EvenlyDiscretizedFunc cumMFD=null;
		
		// cumulative distribution of expected num primary
		GraphWindow cumDistsGraph = null;
		if(!Double.isNaN(expNum)) {
			cumMFD = mfd.getCumRateDistWithOffset();
			cumMFD.scale(mfd.getDelta()*expNum);
			cumMFD.setName("Cum MFD for primary aftershocks of "+rupInfo);
			String info = "expNum="+(float)expNum+" over forecast duration\n";
			double expNumAtMainshockMag = cumMFD.getInterpolatedY(rupture.getMag());
			info+="expNumAtMainshockMag="+(float)expNumAtMainshockMag+" (at mag "+(float)rupture.getMag()+")\n";
			info += "Data:\n"+cumMFD.getMetadataString();
			cumMFD.setInfo(info);
			cumDistsGraph = new GraphWindow(cumMFD, "Expected Cumulative Primary Aftershock MFD"); 
			cumDistsGraph.setX_AxisLabel("Mag");
			cumDistsGraph.setY_AxisLabel("Rate (per year)");
			cumDistsGraph.setY_AxisRange(10e-8, 10e4);
			cumDistsGraph.setX_AxisRange(2.,9.);
			cumDistsGraph.setYLog(true);
			cumDistsGraph.setPlotLabelFontSize(22);
			cumDistsGraph.setAxisLabelFontSize(20);
			cumDistsGraph.setTickLabelFontSize(18);			
		}
		
		// expected relative num secondary aftershocks at each magnitude
		IncrementalMagFreqDist expSecondaryNum = new IncrementalMagFreqDist(mfd.getMinX(), mfd.getMaxX(), mfd.getNum());
		for(int i= 0;i<expSecondaryNum.getNum();i++)
			expSecondaryNum.set(i,mfd.getY(i)*Math.pow(10,mfd.getX(i)));
		
		double aveNum = 0;
		int count=0;
		int lowIndex=expSecondaryNum.getClosestXIndex(expSecondaryNum.getMinMagWithNonZeroRate());
		int hiIndex=expSecondaryNum.getClosestXIndex(expSecondaryNum.getMaxMagWithNonZeroRate());
		for(int i=lowIndex; i<=hiIndex; i++) {
			aveNum += expSecondaryNum.getY(i);
			count += 1;
		}
		aveNum /=count;
		expSecondaryNum.setName("RelNumExpSecAftershocks");
		String info = "This is the MFD of primary aftershocks multiplied by 10^mag\n";
		info += "aveNum="+(float)aveNum+" (vs "+(float)expSecondaryNum.getY(lowIndex)+" at low mag; ratio="+(float)(aveNum/expSecondaryNum.getY(lowIndex))+")\n";
		info += "Data:\n"+expSecondaryNum.getMetadataString();
		expSecondaryNum.setInfo(info);
		GraphWindow expSecGraph = new GraphWindow(expSecondaryNum, "Relative Expected Num Secondary Aftershocks"); 
		expSecGraph.setX_AxisLabel("Mag");
		expSecGraph.setY_AxisLabel("Rel Num Secondary");
		expSecGraph.setX_AxisRange(2.,9.);
		expSecGraph.setPlotLabelFontSize(22);
		expSecGraph.setAxisLabelFontSize(20);
		expSecGraph.setTickLabelFontSize(18);			

		
		if(pdf_FileName != null) {
			try {
				magProbDistsGraph.saveAsPDF(pdf_FileName+"_Incr.pdf");
				expSecGraph.saveAsPDF(pdf_FileName+"_ExpRelSecAft.pdf");
				if(!Double.isNaN(expNum))
					cumDistsGraph.saveAsPDF(pdf_FileName+"_Cum.pdf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(cumMFD != null)
			return cumMFD;
		else
			return null;

	}


	/**
	 * This plots a histogram time since parent for aftershocks.
	 * @param info
	 * @param pdf_FileName
	 * @param simulatedRupsQueue
	 */
	public static void plotNumVsTimeSinceParent(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue,
			double etasProductivity_k, double etasTemporalDecay_p, double etasMinTime_c) {
		
		double delta = 1.0; // days
		double tMin=0;		//days
		double tMax=500;	//days
		
		ETAS_Utils etasUtils = new ETAS_Utils();

		// make the target function & change it to a PDF
//		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithTimeFunc(7, tMin, tMax, delta);
		EvenlyDiscretizedFunc targetFunc = etasUtils.getNumWithTimeFunc(etasProductivity_k, etasTemporalDecay_p, 7d, ETAS_Utils.magMin_DEFAULT, etasMinTime_c, tMin, tMax, delta);
		targetFunc.setName("Expected Temporal Decay");
		targetFunc.scale(1.0/targetFunc.calcSumOfY_Vals());
		
		int numPts = (int) Math.round(tMax-tMin);
		HistogramFunction firstGenEventTimes= new HistogramFunction(tMin+delta/2d,numPts,delta);
		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			if(event.getParentRup() != null) {
				double timeDays = (event.getOriginTime()-event.getParentRup().getOriginTime())/ProbabilityModelsCalc.MILLISEC_PER_DAY;
				if(timeDays<tMax+delta/2.0)
					firstGenEventTimes.add(timeDays, 1.0);
			}
		}
		firstGenEventTimes.setName("Observed Temporal Decay (relative to parent) for all events in "+info);
		firstGenEventTimes.setInfo(" ");
		firstGenEventTimes.normalizeBySumOfY_Vals();
		
		ArrayList funcs = new ArrayList();
		funcs.add(firstGenEventTimes);
		funcs.add(targetFunc);
		
		GraphWindow graph = new GraphWindow(funcs, "Temporal Decay (relative to parent)"); 
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


	/**
	 * This computes the number of ruptures in each generation (the latter being the index of the returned array,
	 * with zero being spontaneous rupture).
	 * @param simulatedRupsQueue
	 * @param maxGeneration
	 * @return
	 */
	public static int[] getNumAftershocksForEachGeneration(PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue, int maxGeneration) {
		int[] numForGen = new int[maxGeneration+1];	// add 1 to include 0
		for(ETAS_EqkRupture rup:simulatedRupsQueue) {
			if(rup.getGeneration() < numForGen.length)
				numForGen[rup.getGeneration()] +=1;
		}
		return numForGen;
	}
	

	
	/**
	 * This plots a histogram of log10 time since parent for aftershocks.
	 * @param info
	 * @param pdf_FileName
	 * @param simulatedRupsQueue
	 */
	public static void plotNumVsLogTimeSinceParent(String info, String pdf_FileName, PriorityQueue<ETAS_EqkRupture> simulatedRupsQueue,
			double etasProductivity_k, double etasTemporalDecay_p, double etasMinTime_c) {
		
		double firstLogDay = -4;
		double lastLogDay = 5;
		double deltaLogDay =0.1;
		int numPts = (int)Math.round((lastLogDay-firstLogDay)/deltaLogDay);
		
		ETAS_Utils etasUtils = new ETAS_Utils();

		// make the target function & change it to a PDF
//		EvenlyDiscretizedFunc targetFunc = etasUtils.getDefaultNumWithLogTimeFunc(7, firstLogDay, lastLocDay, deltaLogDay);	// any mangitude will do
		EvenlyDiscretizedFunc targetFunc = etasUtils.getNumWithLogTimeFunc(etasProductivity_k, etasTemporalDecay_p, 7d, ETAS_Utils.magMin_DEFAULT, etasMinTime_c, firstLogDay, lastLogDay, deltaLogDay);
		
		targetFunc.scale(1.0/targetFunc.calcSumOfY_Vals());
		targetFunc.setName("Expected Temporal Decay");
		
		HistogramFunction firstGenEventTimes= new HistogramFunction(firstLogDay+deltaLogDay/2d,numPts,deltaLogDay);

		for (ETAS_EqkRupture event : simulatedRupsQueue) {
			if(event.getParentRup() != null) {
				double timeMillis = event.getOriginTime()-event.getParentRup().getOriginTime();
				double logTimeDays = Math.log10(timeMillis/ProbabilityModelsCalc.MILLISEC_PER_DAY);
				if(logTimeDays<=firstLogDay)
					firstGenEventTimes.add(0, 1.0);
				else if(logTimeDays<lastLogDay+deltaLogDay/2.0)
					firstGenEventTimes.add(logTimeDays, 1.0);
			}
		}
		firstGenEventTimes.setName("Observed Temporal Decay (relative to parent) for all events in "+info);
		firstGenEventTimes.setInfo(" ");
		firstGenEventTimes.normalizeBySumOfY_Vals();
		
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(firstGenEventTimes);
		funcs.add(targetFunc);
		
		GraphWindow graph = new GraphWindow(funcs, "Temporal Decay (relative to parent)"); 
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

	
	public static void writeEventDataToFile(String fileName, Collection<ETAS_EqkRupture> simulatedRupsQueue)
			throws IOException {
		FileWriter fw1 = new FileWriter(fileName);
		writeEventHeaderToFile(fw1);
		for(ETAS_EqkRupture rup:simulatedRupsQueue) {
			writeEventToFile(fw1, rup);
		}
		fw1.close();
	}
	
	public static void writeEventHeaderToFile(FileWriter fileWriter) throws IOException {
		fileWriter.write("# nthERFIndex\tID\tparID\tGen\tOrigTime\tdistToParent\tMag\tLat\tLon\tDep\tFSS_ID\tGridNodeIndex\n");
	}
	
	public static void writeEventToFile(FileWriter fileWriter, ETAS_EqkRupture rup) throws IOException {
		Location hypoLoc = rup.getHypocenterLocation();
		fileWriter.write(rup.getNthERF_Index()+"\t"+rup.getID()+"\t"+rup.getParentID()+"\t"+rup.getGeneration()+"\t"+
					rup.getOriginTime()+"\t"+rup.getDistanceToParent()
					+"\t"+rup.getMag()+"\t"+hypoLoc.getLatitude()+"\t"+hypoLoc.getLongitude()+"\t"+hypoLoc.getDepth()
					+"\t"+rup.getFSSIndex()+"\t"+rup.getGridNodeIndex()+"\n");
	}
	
	public static ETAS_EqkRupture loadRuptureFromFileLine(String line) {
		line = line.trim();
		
		String[] split = line.split("\t");
		Preconditions.checkState(split.length == 10 || split.length == 12, "Line has unexpected number of items."
				+ "Expected 10/12, got "+split.length+". Line: "+line);
		
		int nthERFIndex = Integer.parseInt(split[0]);
		int id = Integer.parseInt(split[1]);
		int parentID = Integer.parseInt(split[2]);
		int gen = Integer.parseInt(split[3]);
		long origTime = Long.parseLong(split[4]);
		double distToParent = Double.parseDouble(split[5]);
		double mag = Double.parseDouble(split[6]);
		double lat = Double.parseDouble(split[7]);
		double lon = Double.parseDouble(split[8]);
		double depth = Double.parseDouble(split[9]);
		
		Location loc = new Location(lat, lon, depth);
		
		ETAS_EqkRupture rup = new ETAS_EqkRupture();
		rup.setNthERF_Index(nthERFIndex);
		rup.setID(id);
		rup.setParentID(parentID);
		rup.setGeneration(gen);
		rup.setOriginTime(origTime);
		rup.setDistanceToParent(distToParent);
		rup.setMag(mag);
		rup.setHypocenterLocation(loc);
		
		if (split.length == 12) {
			// has FSS and grid node indexes
			rup.setFSSIndex(Integer.parseInt(split[10]));
			rup.setGridNodeIndex(Integer.parseInt(split[11]));
		}
		
		return rup;
	}
	
	public static List<ETAS_EqkRupture> loadCatalog(File catalogFile) throws IOException {
		return loadCatalog(catalogFile, -10d);
	}
	
	public static List<ETAS_EqkRupture> loadCatalog(File catalogFile, double minMag) throws IOException {
		List<ETAS_EqkRupture> catalog = Lists.newArrayList();
		for (String line : Files.readLines(catalogFile, Charset.defaultCharset())) {
			line = line.trim();
			if (line.startsWith("#") || line.isEmpty())
				continue;
			ETAS_EqkRupture rup = ETAS_SimAnalysisTools.loadRuptureFromFileLine(line);
			if (rup.getMag() >= minMag)
				catalog.add(rup);
		}
		return catalog;
	}
	
	public static List<ETAS_EqkRupture> loadCatalog(InputStream catalogStream) throws IOException {
		return loadCatalog(catalogStream, -10d);
	}
	
	public static List<ETAS_EqkRupture> loadCatalog(InputStream catalogStream, double minMag) throws IOException {
		List<ETAS_EqkRupture> catalog = Lists.newArrayList();
		BufferedReader reader = new BufferedReader(new InputStreamReader(catalogStream));
		
		for (String line : CharStreams.readLines(reader)) {
			line = line.trim();
			if (line.startsWith("#") || line.isEmpty())
				continue;
			ETAS_EqkRupture rup = ETAS_SimAnalysisTools.loadRuptureFromFileLine(line);
			if (rup.getMag() >= minMag)
				catalog.add(rup);
		}
		return catalog;
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
	
	public static void plotCatalogGMT(List<? extends ObsEqkRupture> catalog, File outputDir, String outputPrefix, boolean display)
			throws IOException, GMT_MapException {
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(2.5d, 8.5d);
		
		List<LocationList> faults = Lists.newArrayList();
		List<Double> valuesList = Lists.newArrayList();
		
		ArrayList<PSXYSymbol> symbols = Lists.newArrayList();
		
		for (ObsEqkRupture rup : catalog) {
			double mag = rup.getMag();
			if (rup.getRuptureSurface() == null) {
				// gridded
				Location hypo = rup.getHypocenterLocation();
				double width = mag - 2.5;
				if (width < 0.1)
					width = 0.1;
				width /= 50;
				symbols.add(new PSXYSymbol(new Point2D.Double(hypo.getLongitude(), hypo.getLatitude()),
						Symbol.CIRCLE, width, 0d, null, cpt.getColor((float)mag)));
			} else {
				// fault based
				faults.add(rup.getRuptureSurface().getEvenlyDiscritizedPerimeter());
				valuesList.add(mag);
			}
		}
		
		Region region = new CaliforniaRegions.RELM_TESTING();
		boolean skipNans = false;
		String label = "Magnitude";
		
		System.out.println("Making map with "+faults.size()+" fault based ruptures");
		
		double[] values = Doubles.toArray(valuesList);
		GMT_Map map = FaultBasedMapGen.buildMap(cpt, faults, values, null, 1d, region, skipNans, label);
		map.setSymbols(symbols);
		
		FaultBasedMapGen.plotMap(outputDir, outputPrefix, display, map);
	}
	
	public static int getFSSIndex(ETAS_EqkRupture rup, FaultSystemSolutionERF erf) {
		int nthIndex = rup.getNthERF_Index();
		Preconditions.checkState(nthIndex >= 0, "No Nth rupture index!");
		int sourceIndex;
		try {
			sourceIndex = erf.getSrcIndexForNthRup(nthIndex);
		} catch (Exception e) {
			// it's a grid source that's above our max because we are using a different min mag cutoff
			return -1;
		}
		if (sourceIndex < erf.getNumFaultSystemSources())
			return erf.getFltSysRupIndexForSource(sourceIndex);
		return -1;
	}
	
	public static void loadFSSIndexesFromNth(List<ETAS_EqkRupture> catalog, FaultSystemSolutionERF erf) {
		for (ETAS_EqkRupture rup : catalog) {
			int fssIndex = getFSSIndex(rup, erf);
			if (fssIndex >= 0)
				rup.setFSSIndex(fssIndex);
		}
	}
	
	/**
	 * This will set the rupture surface in each ETAS_EqkRupture with an Nth rupture index that corresponds
	 * to a fault systen rupture in the given erf
	 * @param catalog
	 * @param erf
	 */
	public static void loadFSSRupSurfaces(List<ETAS_EqkRupture> catalog, FaultSystemSolutionERF erf) {
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		for (ETAS_EqkRupture rup : catalog) {
			int fssIndex = getFSSIndex(rup, erf);
			if (fssIndex >= 0) {
				Preconditions.checkState((float)rup.getMag() == (float)rupSet.getMagForRup(fssIndex),
						"Magnitude discrepancy: "+(float)rup.getMag()+" != "+(float)rupSet.getMagForRup(fssIndex));
				rup.setRuptureSurface(rupSet.getSurfaceForRupupture(fssIndex, 1d, false));
			}
		}
	}
	
	/**
	 * This will return a catalog that contains only ruptures that are, or are children/grandchildren/etc of the given
	 * parent event ID.
	 * @param catalog
	 * @param parentID
	 * @return
	 */
	public static List<ETAS_EqkRupture> getChildrenFromCatalog(List<ETAS_EqkRupture> catalog, int parentID) {
		List<ETAS_EqkRupture> ret = Lists.newArrayList();
		HashSet<Integer> parents = new HashSet<Integer>();
		parents.add(parentID);
		
		for (ETAS_EqkRupture rup : catalog) {
			if (parents.contains(rup.getParentID()) || rup.getID() == parentID) {
				// it's in the chain
				ret.add(rup);
				parents.add(rup.getID());
			}
		}
		
		return ret;
	}
	
	public static void plotMaxMagVsNumAftershocks(List<List<ETAS_EqkRupture>> catalogs, int parentID) {
		try {
			plotMaxMagVsNumAftershocks(catalogs, parentID, 0d, null, null);
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}
	
	public static void plotMaxMagVsNumAftershocks(List<List<ETAS_EqkRupture>> catalogs, int parentID,
			double mainShockMag, File outputFile, String title) throws IOException {
		XY_DataSet scatter = new DefaultXY_DataSet();
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			if (parentID >= 0)
				catalog = getChildrenFromCatalog(catalog, parentID);
			double maxMag = 0d;
			for (ETAS_EqkRupture rup : catalog) {
				double mag = rup.getMag();
				if (mag > maxMag)
					maxMag = mag;
			}
			scatter.set(maxMag, (double)catalog.size());
		}
		
		List<PlotElement> elems = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		int bufferSize = 20;
		if (scatter.getMaxY() < 300)
			bufferSize = 5;
		
		double minX = scatter.getMinX() - 0.1;
		double maxX = scatter.getMaxX() + 0.1;
		double minY = scatter.getMinY() - bufferSize;
		double maxY = scatter.getMaxY() + bufferSize;
		
		if (mainShockMag > 0) {
			DefaultXY_DataSet magLine = new DefaultXY_DataSet();
			magLine.setName("Mainshock Magnitude");
			magLine.set(mainShockMag, 0);
			magLine.set(mainShockMag, minY);
			magLine.set(mainShockMag, 0.5*(minY+maxY));
			magLine.set(mainShockMag, maxY);
			magLine.set(mainShockMag, maxX*5);
			elems.add(magLine);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		}
		
		elems.add(scatter);
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLACK));
		if (title == null)
			title = "Max Aftershock Mag vs Num Afershocks";
		PlotSpec spec = new PlotSpec(elems, chars, title,
				"Max Aftershock Mag", "Number of Aftershocks");
		
		if (mainShockMag > 0) {
			// now annotation
			int numAbove = 0;
			int tot = scatter.getNum();
			for (Point2D pt : scatter) {
				if (pt.getX() > mainShockMag)
					numAbove++;
			}
			
			String text = numAbove+"/"+tot+" ("+(float)(100d*(double)numAbove/(double)tot)+" %) above M"+(float)mainShockMag;
			
			if (title != null)
				System.out.println(title+": "+text);
			
			List<XYTextAnnotation> annotations = Lists.newArrayList();
			XYTextAnnotation ann = new XYTextAnnotation(text, minX+(maxX-minX)*0.2, minY+(maxY-minY)*0.95);
			ann.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
			annotations.add(ann);
			spec.setPlotAnnotations(annotations);
		}
		
		if (outputFile == null) {
			GraphWindow gw = new GraphWindow(spec);
			gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
			gw.setAxisRange(minX, maxX, minY, maxY);
			gw.setYLog(true);
		} else {
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			gp.setBackgroundColor(Color.WHITE);
			
			gp.setUserBounds(minX, maxX, minY, maxY);
			gp.setYLog(true);
			gp.drawGraphPanel(spec);
			gp.getCartPanel().setSize(1000, 800);
			
			if (outputFile.getName().toLowerCase().endsWith(".png"))
				gp.saveAsPNG(outputFile.getAbsolutePath());
			else
				gp.saveAsPDF(outputFile.getAbsolutePath());
		}
		
	}
	
	public static void main(String[] args) throws IOException, GMT_MapException, DocumentException {
//		File catalogFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-mojave_7/"
//				+ "results/sim_003/simulatedEvents.txt");
//		
//		// needed to laod finite fault surfaces
//		FaultSystemSolution baSol = FaultSystemIO.loadSol(
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
//				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_"
//				+ "COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
//		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(baSol);
//		erf.updateForecast();
//		
//		List<ETAS_EqkRupture> catalog = loadCatalog(catalogFile, 5d);
//		loadFSSRupSurfaces(catalog, erf);
//		
//		plotCatalogGMT(catalog, new File("/tmp"), "etas_catalog", true);
		
//		File catalogsDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_07_30-bombay_beach-extra/results");
		File catalogsDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_interns/2014_07_09-64481/results");
		List<List<ETAS_EqkRupture>> catalogs = Lists.newArrayList();
		for (File subdir : catalogsDir.listFiles()) {
			if (!subdir.getName().startsWith("sim_") || !subdir.isDirectory())
				continue;
			if (!MPJ_ETAS_Simulator.isAlreadyDone(subdir))
				continue;
			File catalogFile = new File(subdir, "simulatedEvents.txt");
			catalogs.add(loadCatalog(catalogFile));
		}
		plotMaxMagVsNumAftershocks(catalogs, 0);
	}

}
