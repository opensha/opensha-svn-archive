package scratch.aftershockStatistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.jfree.data.Range;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DatasetBinner;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.ConsoleWindow;
import org.opensha.commons.gui.plot.GraphWidget;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.param.impl.LocationParameter;
import org.opensha.commons.param.impl.ParameterListParameter;
import org.opensha.commons.param.impl.RangeParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupListCalc;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class AftershockStatsGUI extends JFrame implements ParameterChangeListener {
	
	/*
	 * Data parameters
	 */
	
	private StringParameter eventIDParam;
	private DoubleParameter dataStartTimeParam;
	private DoubleParameter dataEndTimeParam;
	
	private enum RegionType {
		CIRCULAR("Circular"),
		CIRCULAR_WC94("WC 1994 Circular"),
		RECTANGULAR("Rectangular");
		
		private String name;
		
		private RegionType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public boolean isCircular() {
			return this == CIRCULAR_WC94 || this == CIRCULAR;
		}
	}
	
	private enum RegionCenterType {
		EPICENTER("Epicenter"),
		SPECIFIED("Custom Location"),
		CENTROID("Two Step Average Loc");
		
		private String name;
		
		private RegionCenterType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private EnumParameter<RegionType> regionTypeParam;
	private DoubleParameter radiusParam;
	private DoubleParameter minLatParam;
	private DoubleParameter maxLatParam;
	private DoubleParameter minLonParam;
	private DoubleParameter maxLonParam;
	private DoubleParameter minDepthParam;
	private DoubleParameter maxDepthParam;
	private EnumParameter<RegionCenterType> regionCenterTypeParam;
	private LocationParameter regionCenterLocParam;
	
	private ParameterList regionList;
	private ParameterListParameter regionEditParam;
	
	private ButtonParameter fetchButton;
	
	/*
	 * B-value fit parameters
	 */
	
	private DoubleParameter mcParam;
	private DoubleParameter magPrecisionParam;
	private ButtonParameter computeBButton;
	private DoubleParameter bParam;
	
	/*
	 * Aftershock model parameters
	 */
	
	private RangeParameter aValRangeParam;
	private IntegerParameter aValNumParam;
	private RangeParameter pValRangeParam;
	private IntegerParameter pValNumParam;
	private RangeParameter cValRangeParam;
	private IntegerParameter cValNumParam;
	
	private ButtonParameter computeAftershockParamsButton;
	
	private DoubleParameter aValParam;
	private DoubleParameter pValParam;
	private DoubleParameter cValParam;
	
	private DoubleParameter forecastStartTimeParam;
	private DoubleParameter forecastEndTimeParam;
	
	private ButtonParameter computeAftershockForecastButton;
	
	private JTabbedPane tabbedPane;
	private JScrollPane consoleScroll;
	
	private static final int hypo_tab_index = 1;
	private static final int mag_num_tab_index = 2;
	private static final int mag_time_tab_index = 3;
	private static final int pdf_tab_index = 4;
	private GraphWidget hypocenterGraph;
	private GraphWidget magNumGraph;
	private GraphWidget magTimeGraph;
	private JTabbedPane pdfGraphsPane;
	
	private ComcatAccessor accessor;
	private WC1994_MagLengthRelationship wcMagLen;
	
	private Region region;
	private ObsEqkRupture mainshock;
	private ObsEqkRupList aftershocks;
	
	private IncrementalMagFreqDist aftershockMND;
	private double mmaxc;
	
	private ReasenbergJonesAftershockModel model;
	
	public AftershockStatsGUI() {
		/*
		 * Data parameters
		 */
		ParameterList dataParams = new ParameterList();
		
		eventIDParam = new StringParameter("USGS Event ID");
		eventIDParam.setValue("us20002926");
		eventIDParam.addParameterChangeListener(this);
		dataParams.addParameter(eventIDParam);
		
		dataStartTimeParam = new DoubleParameter("Data Start Time", 0d, 3650, new Double(0d));
		dataStartTimeParam.setUnits("Days");
		dataStartTimeParam.addParameterChangeListener(this);
		dataParams.addParameter(dataStartTimeParam);
		
		dataEndTimeParam = new DoubleParameter("Data End Time", 0d, 3650, new Double(7d));
		dataEndTimeParam.setUnits("Days");
		dataEndTimeParam.addParameterChangeListener(this);
		dataParams.addParameter(dataEndTimeParam);
		
		regionTypeParam = new EnumParameter<AftershockStatsGUI.RegionType>(
				"Region Type", EnumSet.allOf(RegionType.class), RegionType.CIRCULAR_WC94, null);
		regionTypeParam.addParameterChangeListener(this);
		dataParams.addParameter(regionTypeParam);
		
		// these are inside region editor
		radiusParam = new DoubleParameter("Radius", 0d, 1000, new Double(20));
		radiusParam.setUnits("km");
		minLatParam = new DoubleParameter("Min Lat", -90d, 90d, new Double(32d));
		maxLatParam = new DoubleParameter("Max Lat", -90d, 90d, new Double(36d));
		minLonParam = new DoubleParameter("Min Lon", -180d, 180d, new Double(32d));
		maxLonParam = new DoubleParameter("Max Lon", -180d, 180d, new Double(36d));
		minDepthParam = new DoubleParameter("Min Depth", 0d, 1000d, new Double(0));
		minDepthParam.setUnits("km");
		maxDepthParam = new DoubleParameter("Max Depth", 0d, 1000d, new Double(20));
		maxDepthParam.setUnits("km");
		regionCenterTypeParam = new EnumParameter<AftershockStatsGUI.RegionCenterType>(
				"Region Center", EnumSet.allOf(RegionCenterType.class), RegionCenterType.CENTROID, null);
		regionCenterLocParam = new LocationParameter("Region Center Location");
		regionCenterTypeParam.addParameterChangeListener(this);
		
		regionList = new ParameterList();
		regionEditParam = new ParameterListParameter("Edit Region", regionList);
		regionEditParam.addParameterChangeListener(this);
		updateRegionParamList(regionTypeParam.getValue(), regionCenterTypeParam.getValue());
		dataParams.addParameter(regionEditParam);
		
		fetchButton = new ButtonParameter("USGS Event Webservice", "Fetch Data");
		fetchButton.addParameterChangeListener(this);
		dataParams.addParameter(fetchButton);
		
		mcParam = new DoubleParameter("Mc", 0d, 9d);
		mcParam.getConstraint().setNullAllowed(true);
		mcParam.addParameterChangeListener(this);
		dataParams.addParameter(mcParam);
		
		magPrecisionParam = new DoubleParameter("Mag Precision", 0d, 1d, new Double(0.1));
		magPrecisionParam.addParameterChangeListener(this);
		dataParams.addParameter(magPrecisionParam);
		
		computeBButton = new ButtonParameter("G-R b-value", "Compute B");
		computeBButton.addParameterChangeListener(this);
		dataParams.addParameter(computeBButton);
		
		bParam = new DoubleParameter("b-value", 1d);
		bParam.setValue(null);
		bParam.addParameterChangeListener(this);
		dataParams.addParameter(bParam);
		
		/*
		 * Fit params
		 */
		
		ParameterList fitParams = new ParameterList();
		
		aValRangeParam = new RangeParameter("a-value range", new Range(-0.7695510786217261, -0.46852108295774486));
		aValRangeParam.addParameterChangeListener(this);
		fitParams.addParameter(aValRangeParam);
		
		aValNumParam = new IntegerParameter("a-value num", 1, 10000, new Integer(69));
		aValNumParam.addParameterChangeListener(this);
		fitParams.addParameter(aValNumParam);
		
		pValRangeParam = new RangeParameter("p-value range", new Range(0.9, 1.15));
		pValRangeParam.addParameterChangeListener(this);
		fitParams.addParameter(pValRangeParam);
		
		pValNumParam = new IntegerParameter("p-value num", 1, 10000, new Integer(21));
		pValNumParam.addParameterChangeListener(this);
		fitParams.addParameter(pValNumParam);
		
		cValRangeParam = new RangeParameter("c-value range", new Range(0.05, 0.05));
		cValRangeParam.addParameterChangeListener(this);
		fitParams.addParameter(cValRangeParam);
		
		cValNumParam = new IntegerParameter("c-value num", 1, 10000, new Integer(1));
		cValNumParam.addParameterChangeListener(this);
		fitParams.addParameter(cValNumParam);
		
		computeAftershockParamsButton = new ButtonParameter("Aftershock Params", "Compute");
		computeAftershockParamsButton.addParameterChangeListener(this);
		fitParams.addParameter(computeAftershockParamsButton);
		
		aValParam = new DoubleParameter("a-value", new Double(0d));
		aValParam.setValue(null);
		aValParam.addParameterChangeListener(this);
		fitParams.addParameter(aValParam);
		
		pValParam = new DoubleParameter("p-value", new Double(0d));
		pValParam.setValue(null);
		pValParam.addParameterChangeListener(this);
		fitParams.addParameter(pValParam);
		
		cValParam = new DoubleParameter("c-value", new Double(0d));
		cValParam.setValue(null);
		cValParam.addParameterChangeListener(this);
		fitParams.addParameter(cValParam);
		
		forecastStartTimeParam = new DoubleParameter("Forecast Start Time", 0d, 3650, new Double(0d));
		forecastStartTimeParam.setUnits("Days");
		forecastStartTimeParam.addParameterChangeListener(this);
		fitParams.addParameter(forecastStartTimeParam);
		
		forecastEndTimeParam = new DoubleParameter("Forecast End Time", 0d, 3650, new Double(7d));
		forecastEndTimeParam.setUnits("Days");
		forecastEndTimeParam.addParameterChangeListener(this);
		fitParams.addParameter(forecastEndTimeParam);
		
		computeAftershockForecastButton = new ButtonParameter("Aftershock Forecast", "Compute");
		computeAftershockForecastButton.addParameterChangeListener(this);
		fitParams.addParameter(computeAftershockForecastButton);
		
		setEnableParamsPostFetch(false);
		
		ParameterListEditor dataEditor = new ParameterListEditor(dataParams);
		dataEditor.setTitle("Data Parameters");
		
		ParameterListEditor fitEditor = new ParameterListEditor(fitParams);
		fitEditor.setTitle("Aftershock Parameters");
		
		ConsoleWindow console = new ConsoleWindow(true);
		consoleScroll = console.getScrollPane();
		consoleScroll.setSize(600, 600);
		JTextArea text = console.getTextArea();
		text.setCaretPosition(0);
		text.setCaretPosition(text.getText().length());
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Console", null, consoleScroll, "View Console");
		
		int paramWidth = 250;
		int chartWidth = 800;
		int height = 900;
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel paramsPanel = new JPanel(new BorderLayout());
		dataEditor.setPreferredSize(new Dimension(paramWidth, height));
		fitEditor.setPreferredSize(new Dimension(paramWidth, height));
		paramsPanel.add(dataEditor, BorderLayout.WEST);
		paramsPanel.add(fitEditor, BorderLayout.EAST);
		mainPanel.add(paramsPanel, BorderLayout.WEST);
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		
		setContentPane(mainPanel);
		setSize(250*2+chartWidth, height);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Aftershock Statistics GUI");
		setLocationRelativeTo(null);
		setVisible(true);
		
		accessor = new ComcatAccessor();
	}
	
	private void updateRegionParamList(RegionType type, RegionCenterType centerType) {
		regionList.clear();
		
		switch (type) {
		case CIRCULAR:
			regionList.addParameter(radiusParam);
			break;
		case CIRCULAR_WC94:
			// do nothing
			break;
		case RECTANGULAR:
			regionList.addParameter(minLatParam);
			regionList.addParameter(maxLatParam);
			regionList.addParameter(minLonParam);
			regionList.addParameter(maxLonParam);
			break;

		default:
			throw new IllegalStateException("Unknown region type: "+type);
		}
		
		if (type == RegionType.CIRCULAR || type == RegionType.CIRCULAR_WC94) {
			regionList.addParameter(regionCenterTypeParam);
			if (centerType == RegionCenterType.SPECIFIED)
				regionList.addParameter(regionCenterLocParam);
		}
		
		regionList.addParameter(minDepthParam);
		regionList.addParameter(maxDepthParam);
		
		regionEditParam.getEditor().refreshParamEditor();
	}
	
	private Region buildRegion(ObsEqkRupture event, Location centroid) {
		RegionType type = regionTypeParam.getValue();
		
		if (type == RegionType.CIRCULAR || type == RegionType.CIRCULAR_WC94) {
			double radius;
			if (type == RegionType.CIRCULAR) {
				radius = radiusParam.getValue();
			} else {
				if (wcMagLen == null)
					wcMagLen = new WC1994_MagLengthRelationship();
				radius = wcMagLen.getMedianLength(event.getMag());
				System.out.println("Using Wells & Coppersmith 94 Radius: "+(float)radius+" km");
			}
			
			RegionCenterType centerType = regionCenterTypeParam.getValue();
			Location loc;
			if (centerType == RegionCenterType.EPICENTER)
				loc = event.getHypocenterLocation();
			else if (centerType == RegionCenterType.CENTROID)
				loc = centroid;
			else if (centerType == RegionCenterType.SPECIFIED)
				loc = regionCenterLocParam.getValue();
			else
				throw new IllegalStateException("Unknown Region Center Type: "+centerType);
			
			return new Region(loc, radius);
		} else  if (type == RegionType.RECTANGULAR) {
			Location lower = new Location(minLatParam.getValue(), minLonParam.getValue());
			Location upper = new Location(maxLatParam.getValue(), maxLonParam.getValue());
			return new Region(lower, upper);
		} else {
			throw new IllegalStateException("Unknown region type: "+type);
		}
	}
	
	private void fetchEvents() {
		System.out.println("Fetching Events");
		
		String eventID = eventIDParam.getValue();
		Preconditions.checkState(eventID != null && !eventID.isEmpty(), "Must supply event ID!");
		
		mainshock = accessor.fetchEvent(eventID);
		Preconditions.checkState(mainshock != null, "Event not found: %s", eventID);
		
		double minDepth = minDepthParam.getValue();
		double maxDepth = maxDepthParam.getValue();
		
		double minDays = dataStartTimeParam.getValue();
		double maxDays = dataEndTimeParam.getValue();
		
		if (regionTypeParam.getValue().isCircular()
				&& regionCenterTypeParam.getValue() == RegionCenterType.CENTROID) {
			// first with hypocenter
			region = buildRegion(mainshock, mainshock.getHypocenterLocation());
			
			aftershocks = accessor.fetchAftershocks(mainshock, minDays, maxDays, minDepth, maxDepth, region);
			
			// now find centroid
			if (aftershocks.isEmpty()) {
				System.out.println("No aftershocks found, skipping centroid");
			} else {
				region = buildRegion(mainshock, getCentroid());
				
				aftershocks = accessor.fetchAftershocks(mainshock, minDays, maxDays, minDepth, maxDepth, region);
			}
		} else {
			region = buildRegion(mainshock, null);
			
			aftershocks = accessor.fetchAftershocks(mainshock, minDays, maxDays, minDepth, maxDepth, region);
		}
	}
	
	private Location getCentroid() {
		List<Location> locs = Lists.newArrayList(mainshock.getHypocenterLocation());
		for (ObsEqkRupture aftershock : aftershocks)
			locs.add(aftershock.getHypocenterLocation());
		double lat = 0;
		double lon = 0;
		for (Location loc : locs) {
			lat += loc.getLatitude();
			lon += loc.getLongitude();
		}
		lat /= (double)locs.size();
		lon /= (double)locs.size();
		Location centroid = new Location(lat, lon);
		double dist = LocationUtils.horzDistanceFast(mainshock.getHypocenterLocation(), centroid);
		System.out.println("Centroid: "+(float)lat+", "+(float)lon+" ("+(float)dist+" km from epicenter)");
		return centroid;
	}
	
	private EvenlyDiscretizedFunc magSizeFunc;
	
	private EvenlyDiscretizedFunc getMagSizeFunc() {
		if (magSizeFunc != null)
			return magSizeFunc;
		
		// size function
		double minMag = 1.25;
		double magDelta = 0.5;
		int numMag = 2*8;
		magSizeFunc = new EvenlyDiscretizedFunc(minMag, numMag, magDelta);
		double maxMag = magSizeFunc.getMaxX();
		double minSize = 1d;
//		double maxSize = 20d;
		double sizeMult = 1.4;
		double size = minSize;
		for (int i=0; i<magSizeFunc.size(); i++) {
			double mag = magSizeFunc.getX(i);
//			double fract = (mag - minMag)/(maxMag - minMag);
//			double size = minSize + fract*(maxSize - minSize);
			
			magSizeFunc.set(i, size);
			
			size *= sizeMult;
		}
		
		return magSizeFunc;
	}
	
	private CPT magCPT;
	
	private CPT getMagCPT() {
		if (magCPT != null)
			return magCPT;
		EvenlyDiscretizedFunc magSizeFunc = getMagSizeFunc();
		try {
			magCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(
					magSizeFunc.getMinX(), magSizeFunc.getMaxX());
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		return magCPT;
	}
	
	private void plotAftershockHypocs() {
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		EvenlyDiscretizedFunc magSizeFunc = getMagSizeFunc();
		
		RuptureSurface mainSurf = mainshock.getRuptureSurface();
		if (mainSurf != null && !mainSurf.isPointSurface()) {
			FaultTrace trace = mainshock.getRuptureSurface().getEvenlyDiscritizedUpperEdge();
			DefaultXY_DataSet traceFunc = new DefaultXY_DataSet();
			traceFunc.setName("Main Shock Trace");
			for(Location loc:trace)
				traceFunc.set(loc.getLongitude(), loc.getLatitude());
			funcs.add(traceFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		} else {
			Location hypo = mainshock.getHypocenterLocation();
			DefaultXY_DataSet xy = new DefaultXY_DataSet(new double[] {hypo.getLongitude()},
					new double[] {hypo.getLatitude()});
			xy.setName("Main Shock Location");
			funcs.add(xy);
			float size = (float)magSizeFunc.getY(magSizeFunc.getClosestXIndex(mainshock.getMag()));
			chars.add(new PlotCurveCharacterstics(PlotSymbol.TRIANGLE, size, Color.BLACK));
		}
		
		// now aftershocks
		List<Point2D> points = Lists.newArrayList();
		List<Double> mags = Lists.newArrayList();
		for (ObsEqkRupture rup : aftershocks) {
			Location loc = rup.getHypocenterLocation();
			points.add(new Point2D.Double(loc.getLongitude(), loc.getLatitude()));
			mags.add(rup.getMag());
		}
		XY_DataSet[] aftershockDatasets = XY_DatasetBinner.bin(points, mags, magSizeFunc);
		
		buildFuncsCharsForBinned(aftershockDatasets, funcs, chars);
		
		// now add outline
		DefaultXY_DataSet outline = new DefaultXY_DataSet();
		for (Location loc : region.getBorder())
			outline.set(loc.getLongitude(), loc.getLatitude());
		outline.set(outline.get(0));
		outline.setName("Region Outline");
		
		funcs.add(outline);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		Collections.reverse(funcs);
		Collections.reverse(chars);
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Aftershock Hypocenters", "Longitude", "Latitude");
		if (hypocenterGraph == null)
			hypocenterGraph = new GraphWidget(spec);
		else
			hypocenterGraph.setPlotSpec(spec);
		
		double regBuff = 0.05;
		hypocenterGraph.setAxisRange(region.getMinLon()-regBuff, region.getMaxLon()+regBuff,
				region.getMinLat()-regBuff, region.getMaxLat()+regBuff);
		
		if (tabbedPane.getTabCount() == hypo_tab_index)
			tabbedPane.addTab("Hypocenters", null, hypocenterGraph, "Hypocenter Map");
		else
			Preconditions.checkState(tabbedPane.getTabCount() > hypo_tab_index, "Plots added out of order");
	}
	
	private void buildFuncsCharsForBinned(XY_DataSet[] binnedFuncs,
			List<PlotElement> funcs, List<PlotCurveCharacterstics> chars) {
		EvenlyDiscretizedFunc magSizeFunc = getMagSizeFunc();
		double magDelta = magSizeFunc.getDelta();
		CPT magColorCPT = getMagCPT();
		for (int i=0; i<binnedFuncs.length; i++) {
			double mag = magSizeFunc.getX(i);
			XY_DataSet xy = binnedFuncs[i];
			if (xy.size() == 0)
				continue;
			xy.setName((float)(mag-0.5*magDelta)+" < M < "+(float)(mag+0.5*magDelta)
					+": "+xy.size()+" EQ");
			float size = (float)magSizeFunc.getY(i);
			Color c = magColorCPT.getColor((float)magSizeFunc.getX(i));
			funcs.add(xy);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, size, c));
		}
	}
	
	private void plotMFDs(IncrementalMagFreqDist mfd, double mmaxc) {
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		mfd.setName("Incremental");
		funcs.add(mfd);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
		
		EvenlyDiscretizedFunc cmlMFD =  mfd.getCumRateDistWithOffset();
		cmlMFD.setName("Cumulative");
		funcs.add(cmlMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		double plotMinY = 0.5;
		double plotMaxY = cmlMFD.getMaxY()+2d;
		
		List<Double> yValsForVerticalLines = Lists.newArrayList(0d, 1e-16, plotMinY, 1d, plotMaxY, 1e3);
		
		// add mainshock mag
		DefaultXY_DataSet xy = new DefaultXY_DataSet();
		for (double y : yValsForVerticalLines)
			xy.set(mainshock.getMag(), y);
		xy.setName("Mainshock Mag ("+(float)mainshock.getMag()+")");
		funcs.add(xy);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
		// add Mmaxc mag
		xy = new DefaultXY_DataSet();
		for (double y : yValsForVerticalLines)
			xy.set(mmaxc, y);
		xy.setName("Mmaxc ("+(float)mmaxc+")");
		funcs.add(xy);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GREEN));
		
		if (bParam.getValue() != null) {
			// add Mc used for b-value calculation
			double mc = mcParam.getValue();
			xy = new DefaultXY_DataSet();
			for (double y : yValsForVerticalLines)
				xy.set(mc, y);
			xy.setName("Mc ("+(float)mc+")");
			funcs.add(xy);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.CYAN));
			
			// add best fitting G-R
			double b = bParam.getValue();
			GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(b, 1d, mfd.getMinX(), mfd.getMaxX(), mfd.size());
			// scale to rate at Mc
			int index = mfd.getClosestXIndex(mc);
			gr.scaleToCumRate(index, cmlMFD.getY(index));
			
			gr.setName("G-R b="+(float)b);
			funcs.add(gr);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.ORANGE));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Aftershock Mag Num Dist", "Magnitude", "Count");
		spec.setLegendVisible(true);
		
		if (magNumGraph == null)
			magNumGraph = new GraphWidget(spec);
		else
			magNumGraph.setPlotSpec(spec);
		magNumGraph.setY_Log(true);
		magNumGraph.setY_AxisRange(plotMinY, plotMaxY);
		
		if (tabbedPane.getTabCount() == mag_num_tab_index)
			tabbedPane.addTab("Mag/Num Dist", null, magNumGraph,
					"Aftershock Magnitude vs Number Distribution");
		else
			Preconditions.checkState(tabbedPane.getTabCount() > mag_num_tab_index, "Plots added out of order");
	}
	
	private double getTimeSinceMainshock(ObsEqkRupture rup) {
		long ms = mainshock.getOriginTime();
		long as = rup.getOriginTime();
		long delta = as - ms;
		return (double)delta/(1000*60*60*24);
	}
	
	private void plotMagVsTime() {
		List<Point2D> points = Lists.newArrayList();
		List<Double> mags = Lists.newArrayList();
		
		points.add(new Point2D.Double(getTimeSinceMainshock(mainshock), mainshock.getMag()));
		mags.add(mainshock.getMag());
		
		MinMaxAveTracker magTrack = new MinMaxAveTracker();
		magTrack.addValue(mainshock.getMag());
		for (int i=0; i<aftershocks.size(); i++) {
			ObsEqkRupture aftershock = aftershocks.get(i);
			points.add(new Point2D.Double(getTimeSinceMainshock(aftershock), aftershock.getMag()));
			mags.add(aftershock.getMag());
			magTrack.addValue(aftershock.getMag());
		}
		
		EvenlyDiscretizedFunc magSizeFunc = getMagSizeFunc();
		XY_DataSet[] binnedFuncs = XY_DatasetBinner.bin(points, mags, magSizeFunc);
		
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		buildFuncsCharsForBinned(binnedFuncs, funcs, chars);
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Magnitude Vs Time", "Days Since Mainshock", "Magnitude");
		
		if (magTimeGraph == null)
			magTimeGraph = new GraphWidget(spec);
		else
			magTimeGraph.setPlotSpec(spec);
		magTimeGraph.setX_AxisRange(-0.75, dataEndTimeParam.getValue()+0.75);
		magTimeGraph.setY_AxisRange(Math.max(0, magTrack.getMin()-1d), magTrack.getMax()+1d);
		
		if (tabbedPane.getTabCount() == mag_time_tab_index)
			tabbedPane.addTab("Mag/Time Plot", null, magTimeGraph,
					"Aftershock Magnitude vs Time Plot");
		else
			Preconditions.checkState(tabbedPane.getTabCount() > mag_time_tab_index, "Plots added out of order");
	}
	
	private void plotPDFs() {
		if (pdfGraphsPane == null)
			pdfGraphsPane = new JTabbedPane();
		else
			while (pdfGraphsPane.getTabCount() > 0)
				pdfGraphsPane.removeTabAt(0);
		
		add1D_PDF(model.getPDF_a(), "a-value");
		add1D_PDF(model.getPDF_p(), "p-value");
		add1D_PDF(model.getPDF_c(), "c-value");
		add2D_PDF(model.get2D_PDF_for_a_and_c(), "a-value", "c-value");
		add2D_PDF(model.get2D_PDF_for_a_and_p(), "a-value", "p-value");
		add2D_PDF(model.get2D_PDF_for_c_and_p(), "c-value", "p-value");
		
		if (tabbedPane.getTabCount() == pdf_tab_index)
			tabbedPane.addTab("Model PDFs", null, pdfGraphsPane,
					"Aftershock Model Prob Dist Funcs");
		else
			Preconditions.checkState(tabbedPane.getTabCount() > pdf_tab_index, "Plots added out of order");
	}
	
	private void add1D_PDF(HistogramFunction pdf, String name) {
		if (pdf == null)
			return;
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(pdf);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, pdf.getName(), name, "log-liklihood");
		
		GraphWidget widget = new GraphWidget(spec);
		pdfGraphsPane.addTab(name, null, widget);
	}
	
	private void add2D_PDF(EvenlyDiscrXYZ_DataSet pdf, String name1, String name2) {
		if (pdf == null)
			return;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == eventIDParam || param == dataStartTimeParam || param == dataEndTimeParam
				|| param == regionEditParam) {
			setEnableParamsPostFetch(false);
		} else if (param == regionTypeParam || param == regionCenterTypeParam) {
			updateRegionParamList(regionTypeParam.getValue(), regionCenterTypeParam.getValue());
			setEnableParamsPostFetch(false);
		} else if (param == fetchButton) {
			String title = "Error Fetching Events";
			setEnableParamsPostFetch(false);
			try {
				fetchEvents();
				title = "Error Calculating Mag Num Distrubution";
				aftershockMND = ObsEqkRupListCalc.getMagNumDist(aftershocks, 1.05, 81, 0.1);
				mmaxc = AftershockStatsCalc.getMmaxC(aftershockMND);
				mcParam.setValue(mmaxc+0.5);
				mcParam.getEditor().refreshParamEditor();
				// plots
				title = "Error Plotting Events";
				plotAftershockHypocs();
				plotMFDs(aftershockMND, mmaxc);
				plotMagVsTime();

				setEnableParamsPostFetch(true);
			} catch (Exception e) {
				e.printStackTrace();
				String message = e.getMessage();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		} else if (param == mcParam || param == magPrecisionParam) {
			setEnableParamsPostComputeB(false);
			bParam.setValue(null);
		} else if (param == computeBButton) {
			String title = "Error Computing b";
			setEnableParamsPostComputeB(false);
			try {
				Double mc = mcParam.getValue();
				Preconditions.checkState(mc != null, "Must supply Mc");
				
				double magPrecision = magPrecisionParam.getValue();
				
				double b = AftershockStatsCalc.getMaxLikelihood_b_value(aftershocks, mc, magPrecision);
				System.out.println("Computed b-value: "+b);
				bParam.setValue(b);
				bParam.getEditor().refreshParamEditor();
				
				setEnableParamsPostComputeB(true);
				tabbedPane.setSelectedIndex(mag_num_tab_index);
			} catch (Exception e) {
				e.printStackTrace();
				String message = e.getMessage();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		} else  if (param == bParam) {
			setEnabledParamsPostAfershockParams(false);
			if (tabbedPane.getTabCount() > mag_time_tab_index)
				plotMFDs(aftershockMND, mmaxc);
		} else if (param == aValRangeParam || param == aValNumParam) {
			updateRangeParams(aValRangeParam, aValNumParam, 69);
			setEnabledParamsPostAfershockParams(false);
		} else if (param == pValRangeParam || param == pValNumParam) {
			updateRangeParams(pValRangeParam, pValNumParam, 21);
			setEnabledParamsPostAfershockParams(false);
		} else if (param == cValRangeParam || param == cValNumParam) {
			updateRangeParams(cValRangeParam, cValNumParam, 21);
			setEnabledParamsPostAfershockParams(false);
		} else if (param == computeAftershockParamsButton) {
			String title = "Error Computing Aftershock Params";
			setEnabledParamsPostAfershockParams(false);
			try {
				Range aRange = aValRangeParam.getValue();
				int aNum = aValNumParam.getValue();
				validateRange(aRange, aNum, "a-value");
				Range pRange = pValRangeParam.getValue();
				int pNum = pValNumParam.getValue();
				validateRange(pRange, pNum, "p-value");
				Range cRange = cValRangeParam.getValue();
				int cNum = cValNumParam.getValue();
				validateRange(cRange, cNum, "c-value");
				
				Double mc = mcParam.getValue();
				Preconditions.checkState(mc != null, "Must supply Mc");
				
				Double b = bParam.getValue();
				Preconditions.checkState(b != null, "Must supply b-value");
				
				model = new ReasenbergJonesAftershockModel(mainshock, aftershocks, mc, b,
						aRange.getLowerBound(), aRange.getUpperBound(), aNum,
						pRange.getLowerBound(), pRange.getUpperBound(), pNum,
						cRange.getLowerBound(), cRange.getUpperBound(), cNum);
				
				aValParam.setValue(model.getMaxLikelihood_a());
				aValParam.getEditor().refreshParamEditor();
				pValParam.setValue(model.getMaxLikelihood_p());
				pValParam.getEditor().refreshParamEditor();
				cValParam.setValue(model.getMaxLikelihood_c());
				cValParam.getEditor().refreshParamEditor();
				
				setEnabledParamsPostAfershockParams(true);
				plotPDFs();
				tabbedPane.setSelectedIndex(pdf_tab_index);
			} catch (Exception e) {
				e.printStackTrace();
				String message = e.getMessage();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void updateRangeParams(RangeParameter rangeParam, IntegerParameter numParam, int defaultNum) {
		Preconditions.checkState(defaultNum > 1);
		Range range = rangeParam.getValue();
		if (range == null)
			return;
		boolean same = range.getLowerBound() == range.getUpperBound();
		if (same && numParam.getValue() > 1)
			numParam.setValue(1);
		else if (!same && numParam.getValue() == 1)
			numParam.setValue(defaultNum);
		numParam.getEditor().refreshParamEditor();
	}
	
	private void validateRange(Range range, int num, String name) {
		Preconditions.checkState(range != null, "Must supply "+name+" range");
		boolean same = range.getLowerBound() == range.getUpperBound();
		if (same)
			Preconditions.checkState(num == 1, "Num must equal 1 for fixed "+name);
		else
			Preconditions.checkState(num > 1, "Num must be >1 for variable "+name);
	}
	
	/**
	 * disables all parameters that are dependent on the fetch step and beyond
	 */
	private void setEnableParamsPostFetch(boolean enabled) {
		mcParam.getEditor().setEnabled(enabled);
		magPrecisionParam.getEditor().setEnabled(enabled);
		computeBButton.getEditor().setEnabled(enabled);
		if (!enabled)
			setEnableParamsPostComputeB(enabled);
	}
	
	/**
	 * disables all parameters that are dependent on the compute b step and beyond
	 */
	private void setEnableParamsPostComputeB(boolean enabled) {
		bParam.getEditor().setEnabled(enabled);
		aValRangeParam.getEditor().setEnabled(enabled);
		aValNumParam.getEditor().setEnabled(enabled);
		pValRangeParam.getEditor().setEnabled(enabled);
		pValNumParam.getEditor().setEnabled(enabled);
		cValRangeParam.getEditor().setEnabled(enabled);
		cValNumParam.getEditor().setEnabled(enabled);
		computeAftershockParamsButton.getEditor().setEnabled(enabled);
		if (!enabled)
			setEnabledParamsPostAfershockParams(enabled);
	}
	
	private void setEnabledParamsPostAfershockParams(boolean enabled) {
		aValParam.getEditor().setEnabled(enabled);
		pValParam.getEditor().setEnabled(enabled);
		cValParam.getEditor().setEnabled(enabled);
		forecastStartTimeParam.getEditor().setEnabled(enabled);
		forecastEndTimeParam.getEditor().setEnabled(enabled);
		computeAftershockForecastButton.getEditor().setEnabled(enabled);
	}
	
	public static void main(String[] args) {
		new AftershockStatsGUI();
	}

}
