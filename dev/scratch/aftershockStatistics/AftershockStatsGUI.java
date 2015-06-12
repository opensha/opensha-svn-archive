package scratch.aftershockStatistics;

import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.ParameterListParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
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
		CIRCULAR_WC94("Wells and Coppersmith Circular"),
		RECTANGULAR("Rectangular");
		
		private String name;
		
		private RegionType(String name) {
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
	
	private ButtonParameter fetchButton;
	
	private ParameterList regionList;
	private ParameterListParameter regionEditParam;
	
	private ComcatAccessor accessor;
	private WC1994_MagLengthRelationship wcMagLen;
	
	private Region region;
	private ObsEqkRupture mainshock;
	private ObsEqkRupList aftershocks;
	
	public AftershockStatsGUI() {
		ParameterList params = new ParameterList();
		
		eventIDParam = new StringParameter("USGS Event ID");
		eventIDParam.setValue("ci37166079");
		params.addParameter(eventIDParam);
		
		startTimeParam = new DoubleParameter("Start Time", 0d, 3650, new Double(0d));
		startTimeParam.setUnits("Days");
		params.addParameter(startTimeParam);
		
		endTimeParam = new DoubleParameter("End Time", 0d, 3650, new Double(7d));
		endTimeParam.setUnits("Days");
		params.addParameter(endTimeParam);
		
		regionTypeParam = new EnumParameter<AftershockStatsGUI.RegionType>(
				"Region Type", EnumSet.allOf(RegionType.class), RegionType.CIRCULAR, null);
		params.addParameter(regionTypeParam);
		regionTypeParam.addParameterChangeListener(this);
		
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
		
		regionList = new ParameterList();
		regionEditParam = new ParameterListParameter("Edit Region", regionList);
		updateRegionParamList(regionTypeParam.getValue());
		params.addParameter(regionEditParam);
		
		fetchButton = new ButtonParameter("USGS Event Webservice", "Fetch Data");
		fetchButton.addParameterChangeListener(this);
		params.addParameter(fetchButton);
		
		ParameterListEditor editor = new ParameterListEditor(params);
		setContentPane(editor);
		setSize(400, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Aftershock Statistics GUI");
		setVisible(true);
		
		accessor = new ComcatAccessor();
	}
	
	private void updateRegionParamList(RegionType type) {
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
		
		regionList.addParameter(minDepthParam);
		regionList.addParameter(maxDepthParam);
		
		regionEditParam.getEditor().refreshParamEditor();
	}
	
	private Region buildRegion(ObsEqkRupture event) {
		RegionType type = regionTypeParam.getValue();
		
		if (type == RegionType.CIRCULAR || type == RegionType.CIRCULAR_WC94) {
			double radius;
			if (type == RegionType.CIRCULAR) {
				radius = radiusParam.getValue();
			} else {
				if (wcMagLen == null)
					wcMagLen = new WC1994_MagLengthRelationship();
				radius = wcMagLen.getMedianLength(event.getMag());
			}
			Location loc = event.getHypocenterLocation();
			
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
		
		region = buildRegion(mainshock);
		
		double minDepth = minDepthParam.getValue();
		double maxDepth = maxDepthParam.getValue();
		
		double minDays = startTimeParam.getValue();
		double maxDays = endTimeParam.getValue();
		
		aftershocks = accessor.fetchAftershocks(mainshock, minDays, maxDays, minDepth, maxDepth, region);
	}
	
	private void plotAftershockHypocs() {
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		// size function
		double minMag = 1.25;
		double magDelta = 0.5;
		int numMag = 2*8;
		EvenlyDiscretizedFunc magSizeFunc = new EvenlyDiscretizedFunc(minMag, numMag, magDelta);
		double maxMag = magSizeFunc.getMaxX();
		double minSize = 2d;
		double maxSize = 10d;
		for (int i=0; i<magSizeFunc.size(); i++) {
			double mag = magSizeFunc.getX(i);
			double fract = (mag - minMag)/(maxMag - minMag);
			double size = minSize + fract*(maxSize - minSize);
			magSizeFunc.set(i, size);
		}
		
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
		CPT magColorCPT;
		try {
			magColorCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(minMag, maxMag);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		DefaultXY_DataSet[] aftershockDatasets = new DefaultXY_DataSet[numMag];
		for (int i=0; i<numMag; i++)
			aftershockDatasets[i] = new DefaultXY_DataSet();
		
		for (ObsEqkRupture rup : aftershocks) {
			double mag = rup.getMag();
			int index = magSizeFunc.getClosestXIndex(mag);
			Location hypo = rup.getHypocenterLocation();
			aftershockDatasets[index].set(hypo.getLongitude(), hypo.getLatitude());
		}
		
		for (int i=0; i<numMag; i++) {
			DefaultXY_DataSet xy = aftershockDatasets[i];
			if (xy.size() == 0)
				continue;
			double mag = magSizeFunc.getX(i);
			xy.setName((float)(mag-0.5*magDelta)+" < M < "+(float)(mag+0.5*magDelta)
					+": "+xy.size()+" aftershocks");
			float size = (float)magSizeFunc.getY(i);
			Color c = magColorCPT.getColor((float)magSizeFunc.getX(i));
			funcs.add(xy);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, size, c));
		}
		
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
		GraphWindow gw = new GraphWindow(spec, false);
		
		double regBuff = 0.05;
		gw.setAxisRange(region.getMinLon()-regBuff, region.getMaxLon()+regBuff,
				region.getMinLat()-regBuff, region.getMaxLat()+regBuff);
		gw.setVisible(true);
	}
	
	private IncrementalMagFreqDist getAftershockMFD() {
		IncrementalMagFreqDist mfd = new IncrementalMagFreqDist(1.05, 81, 0.1);
		double min = mfd.getMinX()-0.5*mfd.getDelta();
		double max = mfd.getMaxX()+0.5*mfd.getDelta();
		mfd.setName("Incremental MFD");
		
		int numBelow = 0;
		int numAbove = 0;
		
		for (ObsEqkRupture rup : aftershocks) {
			double mag = rup.getMag();
			if (mag < min)
				numBelow++;
			else if (mag > mag)
				numAbove++;
			else
				mfd.add(mfd.getClosestXIndex(mag), 1d);
		}
		
		if (numBelow > 0)
			System.out.println("Skipped "+numBelow+" events in MFD below M="+(float)min);
		Preconditions.checkState(numAbove == 0, "MFD max mag too small!");
		
		return mfd;
	}
	
	private void plotMFDs() {
		IncrementalMagFreqDist mfd = getAftershockMFD();
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(mfd);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
		
		EvenlyDiscretizedFunc cmlMFD = mfd.getCumRateDistWithOffset();
		cmlMFD.setName("Cumulative MFD");
		funcs.add(cmlMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		double plotMinY = 0.5;
		double plotMaxY = cmlMFD.getMaxY()+2d;
		
		// add mainshock mag
		DefaultXY_DataSet xy = new DefaultXY_DataSet();
		xy.set(mainshock.getMag(), 0d);
		xy.set(mainshock.getMag(), plotMinY);
		xy.set(mainshock.getMag(), 1d);
		xy.set(mainshock.getMag(), plotMaxY);
		xy.setName("Mainshock Mag ("+(float)mainshock.getMag()+")");
		funcs.add(xy);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Aftershock MFD", "Magnitude", "Count");
		spec.setLegendVisible(true);
		
		GraphWindow gw = new GraphWindow(spec, false);
		gw.setYLog(true);
		gw.setY_AxisRange(plotMinY, plotMaxY);
		gw.setVisible(true);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == regionTypeParam) {
			updateRegionParamList(regionTypeParam.getValue());
		} else if (event.getParameter() == fetchButton) {
			String title = "Error Fetching Events";
			try {
				fetchEvents();
				title = "Error Plotting Events";
				plotAftershockHypocs();
				plotMFDs();
			} catch (Exception e) {
				e.printStackTrace();
				String message = e.getMessage();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void main(String[] args) {
		new AftershockStatsGUI();
	}

}
