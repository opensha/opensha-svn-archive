package scratch.peter.curves;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.nshmp.Period;
import org.opensha.sra.rtgm.RTGM;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class RTGM_Processor implements Runnable {

	private ScalarIMR imr;
	private EpistemicListERF erfs;
	private NEHRP_TestCity loc;
	private Period per;
	private Site site;
	
	private String outDir;
	private static final String S = File.separator;
	private static final double TIME = 1;
	
	private HashMap<String, Integer> paramMap;
	
	private List<List<String>> paramData;
	private List<List<String>> curveData;

	RTGM_Processor(ScalarIMR imr, EpistemicListERF erfs, NEHRP_TestCity loc,
		Period per, String outDir) {
		this.outDir = outDir;
		
		checkArgument(per.equals(Period.GM0P20) || per.equals(Period.GM1P00));
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
			DiscretizedFunc f = per.getLogFunction();
			try {
				ERF erf = erfs.getERF(i);
				f = calc.getHazardCurve(f, site, imr, erf);
				f = deLog(f);
				f = calc.getAnnualizedRates(f, TIME);
				System.out.println(f);
				
				RTGM.Frequency freq = per.equals(Period.GM0P20)
					? RTGM.Frequency.SA_0P20 : RTGM.Frequency.SA_1P00;
				RTGM rtgm = RTGM.create(f, freq, 0.8);
				
				double wt = erfs.getERF_RelativeWeight(i);
				
				addResults(i, wt, erf, f, rtgm.get());

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (i % 20 == 0) {
				System.out.println(loc.toString().substring(0, 2) + " " + i);
			}
		}
		writeFiles();
		System.out.println("Finished:");
		System.out.println(toString());
	}
	
	private static DiscretizedFunc deLog(DiscretizedFunc f) {
		DiscretizedFunc fOut = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<f.getNum(); i++) {
			fOut.set(Math.exp(f.getX(i)), f.getY(i));
		}
		return fOut;
	}
	
	private void addResults(int idx, double wt, ERF erf, DiscretizedFunc f, double rtgm) {
		
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
		curveDat.add(Double.toString(wt));
		curveDat.add(Double.toString(rtgm));
		for (Point2D p : f) {
			curveDat.add(Double.toString(p.getY()));
		}
		curveData.add(curveDat);
	}

	private void writeFiles() {
		String outDirName = outDir + S + per + S + loc.name() + S;
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
		curveHeader.add("wt");
		curveHeader.add("rtgm");
		for (Double d : per.getIMLs()) {
			curveHeader.add(d.toString());
		}
		curveData.add(curveHeader);
		
	}

	//////////////////////////////////////
	//////////////////////////////////////
	//////////////////////////////////////

	
//	public static void main(String[] args) {
////		buildParamHeaders();
//		
////		Processor p = new Processor(AttenRelRef.BA_2008.instance(null), null, 
////			TestLoc.ANDERSON_SPRINGS, Period.GM0P00);
////		p.init();
////		p.writeFiles();
//	}
		
	private static void buildParamHeaders() {
		EpistemicListERF erfs = UcerfBranchGenerator2.newERF();
		HashMap<String, Integer> map = Maps.newHashMap();
		List<String> header = Lists.newArrayList();
		for (int i = 0; i < erfs.getNumERFs(); i++) {
			ERF erf = erfs.getERF(i);
			System.out.print(StringUtils.leftPad(Integer.toString(i), 4));
			if (i % 20 == 0) System.out.print(R);
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