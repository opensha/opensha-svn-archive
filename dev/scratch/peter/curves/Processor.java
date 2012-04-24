package scratch.peter.curves;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jpedal.utils.sleep;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.nshmp.Period;

import scratch.peter.curves.UcerfBranchGenerator.TestLoc;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class Processor implements Runnable {

	private ScalarIMR imr;
	private EpistemicListERF erfs;
	private TestLoc loc;
	private Period per;
	private Site site;
	
	private HashMap<String, Integer> paramMap;
	
	private List<List<String>> paramData;
	private List<List<String>> curveData;

	Processor(ScalarIMR imr, EpistemicListERF erfs, TestLoc loc, Period per) {
		this.imr = imr;
		this.erfs = erfs;
		this.loc = loc;
		this.per = per;
		site = loc.getSite();
	}

	@Override
	public void run() {
		System.out.println("Starting:");
		System.out.println(toString());
		init();
		HazardCurveCalculator calc = new HazardCurveCalculator();
		for (int i = 0; i < erfs.getNumERFs(); ++i) {
			DiscretizedFunc f = per.getFunction();
			try {
				ERF erf = erfs.getERF(i);
				f = calc.getHazardCurve(f, site, imr, erf);
				addResults(i, erf, f);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (i % 20 == 0) {
				System.out.println(imr.getShortName().substring(0, 2) + " "
					+ loc.toString().substring(0, 2) + " " + i);
			}
		}
		writeFiles();
		System.out.println("Finished:");
		System.out.println(toString());
	}
	
	private void addResults(int idx, ERF erf, DiscretizedFunc f) {
		// param data
		List<String> paramDat = Lists.newArrayList();
		paramDat.add(Integer.toString(idx));
		for (int i=0; i<paramMap.size(); i++) paramDat.add(null);
		for (Parameter<?> param : erf.getAdjustableParameterList()) {
			int index = paramMap.get(param.getName());
			String paramVal = StringUtils.replace(param.getValue().toString(), ",", ";");
			paramDat.set(index, paramVal);
		}
		paramData.add(paramDat);
		// curve data
		List<String> curveDat = Lists.newArrayList();
		curveDat.add(Integer.toString(idx));
		for (Point2D p : f) {
			curveDat.add(Double.toString(p.getY()));
		}
		curveData.add(curveDat);
		
//		System.out.println(paramData);
//		System.out.println(idx);
	}
		
	private void writeFiles() {
		String outDirName = UcerfBranchGenerator.OUT_DIR + loc.name() + "/";
		File outDir = new File(outDirName);
		outDir.mkdirs();
		String paramFile = outDirName +  imr.getShortName() + "_params.csv";
		String curveFile = outDirName +  imr.getShortName() + "_curves.csv";
		toCSV(paramFile, paramData);
		toCSV(curveFile, curveData);
	}
	
	private static void toCSV(String file, List<List<String>> content) {
		Joiner joiner = Joiner.on(',').useForNull(" ");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file, true));
			for (List<String> lineDat : content) {
				String line = joiner.join(lineDat);
				pw.println(line);
			}
			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "  " + imr.getShortName() + R + "  " + loc.name();
	}
	
	
	private static final String MAP = "Min Fraction for Unlikely Ruptures=11, Mag-Area Relationship=12, Truncation Level=14, % Char vs GR=16, Probability Model=25, Fract MoRate to Background=1, Seg Dependent Aperiodicity=26, Coupling Coefficient=2, A-Fault Slip Model=6, Fraction Smaller Events & Aftershocks=3, B-Faults b-value=18, A-Faults b-value=28, Min Fraction for Unknown Ruptures=10, Deformation Model=0, Mag Sigma=13, Mean Mag Correction=15, C-Zone Weight=23, Segmented A-Fault Solution Types=5, Aperiodicity=27, Connect More B Faults?=20, Wt On A-Priori Rates=7, Background Seismicity=21, MFD for Background=24, Floater Type=17, Relative Wt On Segment Rates=8, A-Fault Solution Type=4, B-Faults Min Mag=19, Treat Background Seismicity As=22, Weighted Inversion?=9";
	private static final String HEADER = "Deformation Model, Fract MoRate to Background, Coupling Coefficient, Fraction Smaller Events & Aftershocks, A-Fault Solution Type, Segmented A-Fault Solution Types, A-Fault Slip Model, Wt On A-Priori Rates, Relative Wt On Segment Rates, Weighted Inversion?, Min Fraction for Unknown Ruptures, Min Fraction for Unlikely Ruptures, Mag-Area Relationship, Mag Sigma, Truncation Level, Mean Mag Correction, % Char vs GR, Floater Type, B-Faults b-value, B-Faults Min Mag, Connect More B Faults?, Background Seismicity, Treat Background Seismicity As, C-Zone Weight, MFD for Background, Probability Model, Seg Dependent Aperiodicity, Aperiodicity, A-Faults b-value";
	private static final String SEP = ", ";
	private static final String R = IOUtils.LINE_SEPARATOR;
	/*
	 * Initialize output lists, one for param values and another for curves
	 */
	private void init() {
		
		paramMap = Maps.newHashMap();
		String[] entries = StringUtils.splitByWholeSeparator(MAP, SEP);
		for (String entry : entries) {
			String[] nameVal = StringUtils.split(entry, "=");
			paramMap.put(nameVal[0], Integer.valueOf(nameVal[1])+1);
		}
		
		paramData = Lists.newArrayList();
		String[] paramNames = StringUtils.splitByWholeSeparator(HEADER, SEP);
		List<String> paramNameList = Arrays.asList(paramNames);
		List<String> paramDataHeader = Lists.newArrayList();
		paramDataHeader.add("ERF#");
		paramDataHeader.addAll(paramNameList);
		paramData.add(paramDataHeader);
		
		curveData = Lists.newArrayList();
		List<String> curveHeader = Lists.newArrayList();
		curveHeader.add("ERF#");
		for (Double d : per.getIMLs()) {
			curveHeader.add(d.toString());
		}
		curveData.add(curveHeader);
		
//		System.out.println(paramMap);
//		System.out.println(paramData.get(0));
//		System.out.println(curveData.get(0));
	}
	

	//////////////////////////////////////
	//////////////////////////////////////
	//////////////////////////////////////

	
	public static void main(String[] args) {
//		buildParamHeaders();
		
//		Processor p = new Processor(AttenRelRef.BA_2008.instance(null), null, 
//			TestLoc.ANDERSON_SPRINGS, Period.GM0P00);
//		p.init();
//		p.writeFiles();
	}
		
	private static void buildParamHeaders() {
		EpistemicListERF erfs = UcerfBranchGenerator.newERF();
		HashMap<String, Integer> map = Maps.newHashMap();
		List<String> header = Lists.newArrayList();
		for (int i = 0; i < erfs.getNumERFs(); i++) {
			ERF erf = erfs.getERF(i);
			System.out.print(StringUtils.leftPad(Integer.toString(i), 4));
			if (i % 20 == 0) System.out.print(IOUtils.LINE_SEPARATOR);
			for (Parameter<?> param : erf.getAdjustableParameterList()) {
				if (!map.containsKey(param.getName())) {
					map.put(param.getName(), map.size());
					header.add(param.getName());
				}
			}
		}
		System.out.println("MAP: " + R + map);
		System.out.println("HEADER: " + R + header);
	}
	

}
