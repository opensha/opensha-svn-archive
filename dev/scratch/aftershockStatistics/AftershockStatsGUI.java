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

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DatasetBinner;
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
import org.opensha.commons.param.impl.LocationParameter;
import org.opensha.commons.param.impl.ParameterListParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupListCalc;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class AftershockStatsGUI extends JFrame implements ParameterChangeListener {
	
	private StringParameter eventIDParam;
	private DoubleParameter startTimeParam;
	private DoubleParameter endTimeParam;
	
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
	
	private ButtonParameter fetchButton;
	
	private DoubleParameter mcParam;
	private DoubleParameter magPrecisionParam;
	private ButtonParameter computeBButton;
	private DoubleParameter bParam;
	
	private ParameterList regionList;
	private ParameterListParameter regionEditParam;
	
	private JTabbedPane tabbedPane;
	private JScrollPane consoleScroll;
	
	private static final int hypo_tab_index = 1;
	private static final int mag_num_tab_index = 2;
	private static final int mag_time_tab_index = 3;
	private GraphWidget hypocenterGraph;
	private GraphWidget magNumGraph;
	private GraphWidget magTimeGraph;
	
	private ComcatAccessor accessor;
	private WC1994_MagLengthRelationship wcMagLen;
	
	private Region region;
	private ObsEqkRupture mainshock;
	private ObsEqkRupList aftershocks;
	
	private IncrementalMagFreqDist aftershockMND;
	
	public AftershockStatsGUI() {
		ParameterList params = new ParameterList();
		
		eventIDParam = new StringParameter("USGS Event ID");
		eventIDParam.setValue("us20002926");
		eventIDParam.addParameterChangeListener(this);
		params.addParameter(eventIDParam);
		
		startTimeParam = new DoubleParameter("Start Time", 0d, 3650, new Double(0d));
		startTimeParam.setUnits("Days");
		startTimeParam.addParameterChangeListener(this);
		params.addParameter(startTimeParam);
		
		endTimeParam = new DoubleParameter("End Time", 0d, 3650, new Double(7d));
		endTimeParam.setUnits("Days");
		endTimeParam.addParameterChangeListener(this);
		params.addParameter(endTimeParam);
		
		regionTypeParam = new EnumParameter<AftershockStatsGUI.RegionType>(
				"Region Type", EnumSet.allOf(RegionType.class), RegionType.CIRCULAR_WC94, null);
		regionTypeParam.addParameterChangeListener(this);
		params.addParameter(regionTypeParam);
		
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
				"Region Center", EnumSet.allOf(RegionCenterType.class), RegionCenterType.EPICENTER, null);
		regionCenterLocParam = new LocationParameter("Region Center Location");
		regionCenterTypeParam.addParameterChangeListener(this);
		
		regionList = new ParameterList();
		regionEditParam = new ParameterListParameter("Edit Region", regionList);
		regionEditParam.addParameterChangeListener(this);
		updateRegionParamList(regionTypeParam.getValue(), regionCenterTypeParam.getValue());
		params.addParameter(regionEditParam);
		
		fetchButton = new ButtonParameter("USGS Event Webservice", "Fetch Data");
		fetchButton.addParameterChangeListener(this);
		params.addParameter(fetchButton);
		
		mcParam = new DoubleParameter("Mc", 0d, 9d);
		mcParam.getConstraint().setNullAllowed(true);
		mcParam.addParameterChangeListener(this);
		params.addParameter(mcParam);
		
		magPrecisionParam = new DoubleParameter("Mag Precision", 0d, 1d, new Double(0.1));
		magPrecisionParam.addParameterChangeListener(this);
		params.addParameter(magPrecisionParam);
		
		computeBButton = new ButtonParameter("GR b-value", "Compute B");
		computeBButton.addParameterChangeListener(this);
		params.addParameter(computeBButton);
		
		bParam = new DoubleParameter("b-value", 1d);
		bParam.setValue(null);
		bParam.addParameterChangeListener(this);
		params.addParameter(bParam);
		
		disableParamsPostFetch();
		
		ParameterListEditor editor = new ParameterListEditor(params);
		editor.setTitle("Parameters");
		ConsoleWindow console = new ConsoleWindow(true);
		consoleScroll = console.getScrollPane();
		consoleScroll.setSize(600, 600);
		JTextArea text = console.getTextArea();
		text.setCaretPosition(0);
		text.setCaretPosition(text.getText().length());
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Console", null, consoleScroll, "View Console");
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(editor, BorderLayout.WEST);
		editor.setPreferredSize(new Dimension(200, 800));
//		JPanel scrollPanel = new JPanel();
//		scrollPanel.add(consoleScroll);
//		panel.add(scrollPanel, BorderLayout.EAST);
		panel.add(tabbedPane, BorderLayout.CENTER);
		
		setContentPane(panel);
		setSize(1000, 800);
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
		
		double minDays = startTimeParam.getValue();
		double maxDays = endTimeParam.getValue();
		
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
	
	private void plotMFDs(IncrementalMagFreqDist mfd) {
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
		
		// add mainshock mag
		DefaultXY_DataSet xy = new DefaultXY_DataSet();
		xy.set(mainshock.getMag(), 0d);
		xy.set(mainshock.getMag(), 1e-16);
		xy.set(mainshock.getMag(), plotMinY);
		xy.set(mainshock.getMag(), 1d);
		xy.set(mainshock.getMag(), plotMaxY);
		xy.set(mainshock.getMag(), 1e3);
		xy.setName("Mainshock Mag ("+(float)mainshock.getMag()+")");
		funcs.add(xy);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
		if (bParam.getValue() != null) {
			// add best fitting G-R
			double b = bParam.getValue();
			
			// TODO
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
		magTimeGraph.setX_AxisRange(-0.75, endTimeParam.getValue()+0.75);
		magTimeGraph.setY_AxisRange(Math.max(0, magTrack.getMin()-1d), magTrack.getMax()+1d);
		
		if (tabbedPane.getTabCount() == mag_time_tab_index)
			tabbedPane.addTab("Mag/Time Plot", null, magTimeGraph,
					"Aftershock Magnitude vs Time Plot");
		else
			Preconditions.checkState(tabbedPane.getTabCount() > mag_time_tab_index, "Plots added out of order");
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == eventIDParam || param == startTimeParam || param == endTimeParam
				|| param == regionEditParam) {
			disableParamsPostFetch();
		} else if (param == regionTypeParam || param == regionCenterTypeParam) {
			updateRegionParamList(regionTypeParam.getValue(), regionCenterTypeParam.getValue());
			disableParamsPostFetch();
		} else if (param == fetchButton) {
			String title = "Error Fetching Events";
			disableParamsPostFetch();
			try {
				fetchEvents();
				title = "Error Calculating Mag Num Distrubution";
				aftershockMND = ObsEqkRupListCalc.getMagNumDist(aftershocks, 1.05, 81, 0.1);
				// plots
				title = "Error Plotting Events";
				plotAftershockHypocs();
				plotMFDs(aftershockMND);
				plotMagVsTime();
				
				title = "Error Calculating Mmaxc";
				double mmaxc = AftershockStatsCalc.getMmaxC(aftershockMND);
				mcParam.setValue(mmaxc+0.5);

				magPrecisionParam.getEditor().setEnabled(true);
				mcParam.getEditor().setEnabled(true);
				mcParam.getEditor().refreshParamEditor();
				computeBButton.getEditor().setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
				String message = e.getMessage();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		} else if (param == mcParam || param == magPrecisionParam) {
			disableParamsPostComputeB();
		} else if (param == computeBButton) {
			String title = "Error Computing b";
			disableParamsPostComputeB();
			try {
				Double mc = mcParam.getValue();
				Preconditions.checkState(mc != null, "Must supply Mc");
				
				double magPrecision = magPrecisionParam.getValue();
				
				double b = AftershockStatsCalc.getMaxLikelihood_b_value(aftershocks, mc, magPrecision);
				System.out.println("Computed b-value: "+b);
				bParam.setValue(b);
				bParam.getEditor().refreshParamEditor();
				
				bParam.getEditor().setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
				String message = e.getMessage();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		} else  if (param == bParam) {
			// TODO disable downstream params when added
		}
	}
	
	/**
	 * disables all parameters that are dependent on the fetch step and beyond
	 */
	private void disableParamsPostFetch() {
		mcParam.getEditor().setEnabled(false);
		magPrecisionParam.getEditor().setEnabled(false);
		computeBButton.getEditor().setEnabled(false);
		disableParamsPostComputeB();
	}
	
	/**
	 * disables all parameters that are dependent on the compute b step and beyond
	 */
	private void disableParamsPostComputeB() {
		bParam.getEditor().setEnabled(false);
	}
	
	public static void main(String[] args) {
		new AftershockStatsGUI();
	}

}
