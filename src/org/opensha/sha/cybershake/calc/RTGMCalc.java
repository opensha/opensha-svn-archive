package org.opensha.sha.cybershake.calc;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.Range;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.HazardDataset2DB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.cybershake.plot.PlotType;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sra.rtgm.RTGM;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RTGMCalc {
	
	private int runID;
	private CyberShakeComponent component;
	private File outputDir;
	
	private DBAccess db;
	private HazardCurve2DB curves2db;
	private Runs2DB runs2db;
	private CybershakeSiteInfo2DB sites2db;
	private HazardDataset2DB dataset2db;
	
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
	
	private ERF erf;
	private List<AttenuationRelationship> attenRels;
	
	private List<CybershakeIM> forceAddIMs;
	
	private static final PlotType PLOT_TYPE_DEFAULT = PlotType.CSV;
	private List<PlotType> plotTypes;

	private static final boolean VEL_PLOT_DEFAULT = false;
	private boolean velPlot;
	
	private static HazardCurveCalculator gmpeCalc = new HazardCurveCalculator();
	
	public RTGMCalc(CommandLine cmd, DBAccess db) {
		Preconditions.checkArgument(cmd.hasOption("run-id"));
		int runID = Integer.parseInt(cmd.getOptionValue("run-id"));
		
		CyberShakeComponent component = null;
		if (cmd.hasOption("component"))
			component = CybershakeIM.fromShortName(cmd.getOptionValue("component"), CyberShakeComponent.class);
		
		Preconditions.checkArgument(cmd.hasOption("output-dir"));
		File outputDir = new File(cmd.getOptionValue("output-dir"));
		
		List<CybershakeIM> forceAddIMs = null;
		if (cmd.hasOption("force-add")) {
			String forceStr = cmd.getOptionValue("force-add").trim();
			Preconditions.checkArgument(forceStr.contains(":"), "Invalid force-add format");
			String compStr = forceStr.substring(0, forceStr.indexOf(":"));
			CyberShakeComponent addComp = CybershakeIM.fromShortName(compStr, CyberShakeComponent.class);
			String periodStr = forceStr.substring(forceStr.indexOf(":")+1);
			List<Double> periods = HazardCurvePlotter.commaDoubleSplit(periodStr);
			
			forceAddIMs = Lists.newArrayList();
			for (Double period : periods)
				forceAddIMs.add(new CybershakeIM(-1, IMType.SA, period, null, addComp));
		}
		
		List<PlotType> types;
		
		if (cmd.hasOption("t")) {
			String typeStr = cmd.getOptionValue("t");
			
			types = PlotType.fromExtensions(HazardCurvePlotter.commaSplit(typeStr));
		} else {
			types = new ArrayList<PlotType>();
			types.add(PLOT_TYPE_DEFAULT);
		}
		
		if (cmd.hasOption("velocities"))
			velPlot = true;
		else
			velPlot = VEL_PLOT_DEFAULT;
		
		init(runID, component, outputDir, db, forceAddIMs, types);
		
		if (cmd.hasOption("ef") && cmd.hasOption("af")) {
			String erfFile = cmd.getOptionValue("ef");
			String attenFiles = cmd.getOptionValue("af");
			
			try {
				erf = ERFSaver.LOAD_ERF_FROM_FILE(erfFile);
				attenRels = Lists.newArrayList();
				
				for (String attenRelFile : HazardCurvePlotter.commaSplit(attenFiles)) {
					AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
					attenRels.add(attenRel);
				}
				
				erf.updateForecast();
			} catch (DocumentException e) {
				e.printStackTrace();
				System.err.println("WARNING: Unable to parse ERF XML, not plotting comparison curves!");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				System.err.println("WARNING: Unable to load comparison ERF, not plotting comparison curves!");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				System.err.println("WARNING: Unable to load comparison ERF, not plotting comparison curves!");
			}
		}
	}
	
	public RTGMCalc(int runID, CyberShakeComponent component, File outputDir, DBAccess db) {
		init(runID, component, outputDir, db, null, Lists.newArrayList(PlotType.CSV));
	}
	
	private void init(int runID, CyberShakeComponent component, File outputDir, DBAccess db,
			List<CybershakeIM> forceAddIMs, List<PlotType> plotTypes) {
		Preconditions.checkArgument(runID >= 0, "Run ID must be >= 0");
		// component CAN be null
		Preconditions.checkArgument(outputDir != null, "Output dir must me supplied");
		Preconditions.checkArgument((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir(),
				"Output dir does not exist and could not be created");
		Preconditions.checkArgument(db != null, "DB connection cannot be null");
		this.runID = runID;
		this.component = component;
		this.outputDir = outputDir;
		this.db = db;
		this.forceAddIMs = forceAddIMs;
		this.plotTypes = plotTypes;
		
		curves2db = new HazardCurve2DB(db);
		runs2db = new Runs2DB(db);
		sites2db = new CybershakeSiteInfo2DB(db);
		dataset2db = new HazardDataset2DB(db);
	}
	
	public boolean calc() throws IOException {
		List<Integer> curveIDs = curves2db.getAllHazardCurveIDs(runID, -1);
		// filter out any oddball probability models
		int origSize = curveIDs.size();
		for (int i=curveIDs.size(); --i>=0;) {
			int datasetID = curves2db.getDatasetIDForCurve(curveIDs.get(i));
			int probModelID = dataset2db.getProbModelID(datasetID);
			if (probModelID != 1)
				// 1 is time independent poisson
				curveIDs.remove(i);
		}
		if (origSize != curveIDs.size())
			System.out.println((origSize-curveIDs.size())+"/"+origSize+" were filtered out due to Prob Model ID != 1");
		List<CybershakeIM> ims = Lists.newArrayList();
		for (int curveID : curveIDs)
			ims.add(curves2db.getIMForCurve(curveID));
		
		if (forceAddIMs != null && !forceAddIMs.isEmpty()) {
			// add IMs as applicable
			for (CybershakeIM im : forceAddIMs) {
				boolean found = false;
				for (CybershakeIM prevIM : ims) {
					if (im.getMeasure() == prevIM.getMeasure() && im.getComponent() == prevIM.getComponent()
							&& (int)(im.getVal()*100d) == (int)(prevIM.getVal()*100d)) {
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("Adding forced IM with no CyberShake data: "+im);
					ims.add(im);
					curveIDs.add(-1);
				}
			}
		}
		
		// remove other components if one has been specified
		if (component != null) {
			origSize = curveIDs.size();
			// remove all curve IDs that are the wrong component
			for (int i=curveIDs.size(); --i>=0;) {
				CybershakeIM im = ims.get(i);
				if (im.getComponent() != component) {
					curveIDs.remove(i);
					ims.remove(i);
				}
			}
			System.out.println(curveIDs.size()+"/"+origSize+" curves match specified component: "
					+component.getShortName());
		}
		if (curveIDs.isEmpty()) {
			System.err.println("No matching curves found in database for runID="
					+runID+", component="+component+" (null means any)");
			System.err.println("Curves must be calculated and inserted prior to calculating RTGM.");
			return false;
		}
		
		// now sort by IM
		List<ComparablePairing<CybershakeIM, Integer>> comparables = ComparablePairing.build(ims, curveIDs);
		Collections.sort(comparables);
		curveIDs = Lists.newArrayList();
		ims = Lists.newArrayList();
		for (ComparablePairing<CybershakeIM, Integer> comparable : comparables) {
			curveIDs.add(comparable.getData());
			ims.add(comparable.getComparable());
		}
		
		CybershakeRun run = runs2db.getRun(runID);
		int siteID = run.getSiteID();
		CybershakeSite site = sites2db.getSiteFromDB(siteID);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList("Site Short Name", "Run ID", "IM Type ID", "IM Type",
				"Component", "Period", "CyberShake RTGM (g)");
		if (attenRels != null) {
			for (AttenuationRelationship attenRel : attenRels) {
				header.add(attenRel.getShortName()+" Metadata");
				header.add(attenRel.getShortName()+" RTGM (g)");
			}
		}
		csv.addLine(header);
		
		List<SiteDataValue<?>> datas = null;
		
		List<DiscretizedFunc> cybershakeCurves = Lists.newArrayList();
		
		Map<CyberShakeComponent, DiscretizedFunc> csSpectrumMap = Maps.newHashMap();
		Map<CyberShakeComponent, List<DiscretizedFunc>> gmpeSpectrumMap;
		if (attenRels != null)
			gmpeSpectrumMap = Maps.newHashMap();
		else
			gmpeSpectrumMap = null;
		
		for (int i=0; i<curveIDs.size(); i++)
			cybershakeCurves.add(curves2db.getHazardCurve(curveIDs.get(i)));
		
		for (int i=0; i<curveIDs.size(); i++) {
			int curveID = curveIDs.get(i);
			CybershakeIM im = ims.get(i);
			DiscretizedFunc curve = cybershakeCurves.get(i);
			
			List<String> line;
			
			double rtgm;
			if (curveID < 0) {
				rtgm = Double.NaN;
				Preconditions.checkState(curve == null);
				// use any other cybershake curve for x values
				for (DiscretizedFunc test : cybershakeCurves) {
					if (test != null) {
						curve = test;
						break;
					}
				}
				line = Lists.newArrayList(site.short_name, "", "", im.getMeasure().getShortName(),
						im.getComponent().getShortName(), (float)im.getVal()+"", "");
			} else {
				line = Lists.newArrayList(site.short_name, runID+"", im.getID()+"",
						im.getMeasure().getShortName(), im.getComponent().getShortName(), (float)im.getVal()+"");
				validateCurveForRTGM(curve);
				
				System.out.println("Calculating RTGM for: "+Joiner.on(",").join(line));
				
				// calculate RTGM
				// first null is frequency which is used for a scaling factor, which we disable
				// second null is Beta value, we want default
				rtgm = calcRTGM(curve);
				line.add(rtgm+"");
				
				DiscretizedFunc csSpectrum = csSpectrumMap.get(im.getComponent());
				if (csSpectrum == null) {
					csSpectrum = new ArbitrarilyDiscretizedFunc("CyberShake");
					csSpectrum.setInfo(site.short_name+" RTGM Spectrum, "
							+im.getComponent().getShortName());
					csSpectrumMap.put(im.getComponent(), csSpectrum);
				}
				csSpectrum.set(im.getVal(), rtgm);
			}
			
			// GMPE comparisons
			if (attenRels != null) {
				if (datas == null) {
					int velModelID = run.getVelModelID();
					OrderedSiteDataProviderList providers = HazardCurvePlotter.createProviders(velModelID);
					datas = providers.getBestAvailableData(site.createLocation());
				}
				List<DiscretizedFunc> gmpeSpectrums = gmpeSpectrumMap.get(im.getComponent());
				if (gmpeSpectrums == null) {
					gmpeSpectrums = Lists.newArrayList();
					for (AttenuationRelationship attenRel : attenRels) {
						ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc(
								attenRel.getShortName());
						func.setInfo(site.short_name+" "+attenRel.getShortName()+" RTGM Spectrum, "
								+im.getComponent().getShortName());
						gmpeSpectrums.add(func);
					}
					gmpeSpectrumMap.put(im.getComponent(), gmpeSpectrums);
				}
				Preconditions.checkState((float)erf.getTimeSpan().getDuration() == 1f,
						"ERF duration should be 1 year");
				for (int j=0; j<attenRels.size(); j++) {
					AttenuationRelationship attenRel = attenRels.get(j);
					System.out.println("Calculating comparison RTGM value for "+attenRel.getShortName());
					DiscretizedFunc hazFunction = HazardCurveSetCalculator.getLogFunction(curve.deepClone());
					Site gmpeSite = HazardCurvePlotter.setAttenRelParams(attenRel, im, run, site, datas);
					gmpeCalc.getHazardCurve(hazFunction, gmpeSite, attenRel, erf);
					hazFunction = HazardCurveSetCalculator.unLogFunction(curve, hazFunction);
					validateCurveForRTGM(hazFunction);
					// now convert component if needed
					// metadata will be place in info string
					hazFunction.setInfo(null);
					hazFunction = HazardCurvePlotter.getScaledCurveForComponent(attenRel, im, hazFunction);
					String metadata = hazFunction.getInfo().trim().replaceAll("\n", "");
					
					rtgm = calcRTGM(hazFunction);
					Preconditions.checkState(rtgm > 0, "RTGM is not positive");
					line.add(metadata);
					line.add(rtgm+"");
					
					DiscretizedFunc gmpeSpectrum = gmpeSpectrums.get(j);
					gmpeSpectrum.set(im.getVal(), rtgm);
				}
			}
			
			csv.addLine(line);
		}
		
		String namePrefix = site.short_name+"_run"+runID+"_RTGM";
		String dateStr = dateFormat.format(new Date());
		Map<CyberShakeComponent, PlotSpec> specs = null;
		Map<CyberShakeComponent, PlotSpec> velSpecs = null;
		
		for (PlotType type : plotTypes) {
			switch (type) {
			case CSV:
				String name = namePrefix;
				if (component != null)
					name += "_"+component.getShortName();
				name += "_"+dateStr;
				File outputFile = new File(outputDir, name+".csv");
				csv.writeToFile(outputFile);
				break;
			case PDF:
				if (specs == null) {
					specs = getSpectrumPlot(site, csSpectrumMap, gmpeSpectrumMap, "RTGM", "(g)");
					if (velPlot)
						velSpecs = getSpectrumPlot(site, saToPsuedoVel(csSpectrumMap),
								sasToPsuedoVel(gmpeSpectrumMap), "RTGM PSV", "(cm/sec)");
				}
				writeSpecs(namePrefix, dateStr, specs, outputDir, type, xLog, yLog, xRangeSA, yRangeSA);
				if (velPlot)
					writeSpecs(namePrefix, "vel_"+dateStr, velSpecs, outputDir, type, xLog, yLog,
							xRangeVel, yRangeVel);
				break;
			case PNG:
				if (specs == null) {
					specs = getSpectrumPlot(site, csSpectrumMap, gmpeSpectrumMap, "RTGM", "(g)");
					if (velPlot)
						velSpecs = getSpectrumPlot(site, saToPsuedoVel(csSpectrumMap),
								sasToPsuedoVel(gmpeSpectrumMap), "RTGM PSV", "(cm/sec)");
				}
				writeSpecs(namePrefix, dateStr, specs, outputDir, type, xLog, yLog, xRangeSA, yRangeSA);
				if (velPlot)
					writeSpecs(namePrefix, "vel_"+dateStr, velSpecs, outputDir, type, xLog, yLog,
							xRangeVel, yRangeVel);
				break;

			default:
				throw new IllegalArgumentException("Unsupported plot type: "+type.getExtension());
			}
		}
		
		return true; // success
	}
	
	private static final boolean xLog = true;
	private static final boolean yLog = true;
	private static final Range xRangeSA = new Range(1d, 10d);
	private static final Range yRangeSA = new Range(2e-2, 2e0);
	private static final Range xRangeVel = new Range(1d, 10d);
	private static final Range yRangeVel = new Range(30, 300);
	
	static Map<CyberShakeComponent, PlotSpec> getSpectrumPlot(CybershakeSite site,
			Map<CyberShakeComponent, DiscretizedFunc> csSpectrumMap,
			Map<CyberShakeComponent, List<DiscretizedFunc>> gmpeSpectrumMap,
			String calcType, String units) {
		
		Map<CyberShakeComponent, PlotSpec> specMap = Maps.newHashMap();
		
		for (CyberShakeComponent comp : csSpectrumMap.keySet()) {
			DiscretizedFunc csSpectrum = csSpectrumMap.get(comp);
			List<DiscretizedFunc> gmpeSpectrum;
			if (gmpeSpectrumMap == null)
				gmpeSpectrum = null;
			else
				gmpeSpectrum = gmpeSpectrumMap.get(comp);
			
			List<DiscretizedFunc> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			funcs.add(csSpectrum);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f,
					PlotSymbol.FILLED_CIRCLE, 4f, Color.BLACK));
			
			if (gmpeSpectrum != null) {
				funcs.addAll(gmpeSpectrum);
				for (int i=0; i<gmpeSpectrum.size(); i++) {
					PlotLineType plt;
					if (gmpeSpectrum.get(i).getName().contains("2014"))
						plt = PlotLineType.DOTTED;
					else
						plt = PlotLineType.DASHED;
					chars.add(new PlotCurveCharacterstics(plt, 2f,
							PlotSymbol.FILLED_CIRCLE, 4f, gmpeColors.get(i % gmpeColors.size())));
				}
			}
			
			PlotSpec spec = new PlotSpec(funcs, chars, site.short_name+" Spectrum", "Period (s)",
					comp.getShortName()+" "+calcType+" "+units);
			spec.setLegendVisible(true);
			specMap.put(comp, spec);
		}
		
		return specMap;
	}
	
	static Map<CyberShakeComponent, DiscretizedFunc> saToPsuedoVel(
			Map<CyberShakeComponent, DiscretizedFunc> map) {
		Map<CyberShakeComponent, DiscretizedFunc> velMap = Maps.newHashMap();
		
		for (CyberShakeComponent comp : map.keySet())
			velMap.put(comp, saToPsuedoVel(map.get(comp)));
		
		return velMap;
	}
	
	static Map<CyberShakeComponent, List<DiscretizedFunc>> sasToPsuedoVel(
			Map<CyberShakeComponent, List<DiscretizedFunc>> map) {
		if (map == null)
			return null;
		Map<CyberShakeComponent, List<DiscretizedFunc>> velMap = Maps.newHashMap();
		
		for (CyberShakeComponent comp : map.keySet()) {
			List<DiscretizedFunc> velFuncs = Lists.newArrayList();
			for (DiscretizedFunc saFunc : map.get(comp))
				velFuncs.add(saToPsuedoVel(saFunc));
			velMap.put(comp, velFuncs);
		}
		
		return velMap;
	}
	
	static DiscretizedFunc saToPsuedoVel(DiscretizedFunc saFunc) {
		ArbitrarilyDiscretizedFunc velFunc = new ArbitrarilyDiscretizedFunc(saFunc.getName());
		
		double twoPi = 2d*Math.PI;
		
		for (int i=0; i<saFunc.getNum(); i++) {
			double period = saFunc.getX(i);
			double sa = saFunc.getY(i)*HazardCurveComputation.CONVERSION_TO_G; // convert to cm/sec^2
			double vel = (period/twoPi)*sa;
			velFunc.set(period, vel);
		}
		
		return velFunc;
	}
    
    private static final List<Color> gmpeColors = Lists.newArrayList(
    		new Color(255, 150, 150),
    		new Color(150, 150, 255),
    		new Color(150, 255, 150),
    		new Color(255, 180, 35),
    		new Color(160, 255, 255));
	
	static void writeSpecs(String namePrefix, String dateStr,
			Map<CyberShakeComponent, PlotSpec> specs, File outputDir, PlotType plotType,
			boolean xLog, boolean yLog, Range xRange, Range yRange) throws IOException {
		for (CyberShakeComponent comp : specs.keySet()) {
			String name = namePrefix+"_"+comp.getShortName()+"_"+dateStr+"."+plotType.getExtension();
			File outputFile = new File(outputDir, name);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setBackgroundColor(null);
			gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			
			gp.drawGraphPanel(specs.get(comp), xLog, yLog, xRange, yRange);
			gp.getCartPanel().setSize(1000, 800);
			gp.setVisible(true);
			
			gp.validate();
			gp.repaint();
			
			switch (plotType) {
			case PDF:
				gp.saveAsPDF(outputFile.getAbsolutePath());
				break;
			case PNG:
				gp.saveAsPNG(outputFile.getAbsolutePath());
				break;

			default:
				throw new IllegalArgumentException("Unsupported plot type: "+plotType.getExtension());
			}
		}
	}
	
	private static void validateCurveForRTGM(DiscretizedFunc curve) {
		// make sure it's not empty
		Preconditions.checkState(curve.getNum() > 2, "curve is empty");
		// make sure it has actual values
		Preconditions.checkState(curve.getMaxY() > 0d, "curve has all zero y values");
		// make sure it is monotonically decreasing
		String xValStr = Iterators.toString(curve.getYValuesIterator());
		for (int j=1; j<curve.getNum(); j++)
			Preconditions.checkState(curve.getY(j) <= curve.getY(j-1),
				"Curve not monotonically decreasing: "+xValStr);
	}
	
	private static double calcRTGM(DiscretizedFunc curve) {
		// convert from annual probability to annual frequency
		curve = gmpeCalc.getAnnualizedRates(curve, 1d);
		RTGM calc = RTGM.create(curve, null, null);
		try {
			calc.call();
		} catch (RuntimeException e) {
			System.err.println("RTGM Calc failed for Hazard Curve:\n"+curve);
			System.err.flush();
			throw e;
		}
		double rtgm = calc.get();
		Preconditions.checkState(rtgm > 0, "RTGM is not positive");
		return rtgm;
	}
	
	private static Options createOptions() {
		Options ops = new Options();
		
		Option run = new Option("R", "run-id", true, "Run ID");
		run.setRequired(true);
		ops.addOption(run);
		
		Option component = new Option("cmp", "component", true, "Intensity measure component. "
				+ "All will be calculated if ommitted. Options: "
				+Joiner.on(",").join(CybershakeIM.getShortNames(CyberShakeComponent.class)));
		component.setRequired(false);
		ops.addOption(component);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		output.setRequired(true);
		ops.addOption(output);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file for comparison");
		erfFile.setRequired(false);
		ops.addOption(erfFile);
		
		Option forceAdd = new Option("f", "force-add", true, "Force the calculator to include the given period(s)"
				+ " in the output file even if no CyberShake results available."
				+ " Format: '<component>:<period1>,<period2>. Example: RotD100:1,1.5");
		forceAdd.setRequired(false);
		ops.addOption(forceAdd);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true,
				"XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		attenRelFiles.setRequired(false);
		ops.addOption(attenRelFiles);
		
		Option vel = new Option("v", "velocities", false,
				"Generate velocity plot as well as SA (if appropriate plot types selected)");
		vel.setRequired(false);
		ops.addOption(vel);
		
		Option help = new Option("?", "help", false, "Display this message");
		help.setRequired(false);
		ops.addOption(help);
		
		Option type = new Option("t", "type", true, "Plot save type. Options are png, pdf, and csv. "
				+"Multiple types can be comma separated (default is "+PLOT_TYPE_DEFAULT.getExtension()+")");
		ops.addOption(type);
		
		return ops;
	}
	
	public static void printHelp(Options options, String appName) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( appName, options, true );
		System.exit(2);
	}
	
	public static void printUsage(Options options, String appName) {
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 80, appName, options);
		pw.flush();
		System.exit(2);
	}
	
	public static void main(String[] args) {
		try {
			Options options = createOptions();
			
			String appName = ClassUtils.getClassNameWithoutPackage(RTGMCalc.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					printHelp(options, appName);
				}
				
				RTGMCalc calc = new RTGMCalc(cmd, Cybershake_OpenSHA_DBApplication.db);
				
				boolean success = calc.calc();
				
				if (!success) {
					System.out.println("FAIL!");
					System.exit(1);
				}
			} catch (MissingOptionException e) {
				Options helpOps = new Options();
				helpOps.addOption(new Option("h", "help", false, "Display this message"));
				try {
					CommandLine cmd = parser.parse( helpOps, args);
					
					if (cmd.hasOption("help")) {
						printHelp(options, appName);
					}
				} catch (ParseException e1) {}
				System.err.println(e.getMessage());
				printUsage(options, appName);
//			e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
				printUsage(options, appName);
			}
			
			System.out.println("Done!");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
