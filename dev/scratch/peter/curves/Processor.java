package scratch.peter.curves;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.nshmp.Period;

import scratch.peter.curves.UcerfBranchGenerator.TestLoc;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class Processor extends SwingWorker<Void, Void> {

	private ScalarIMR imr;
	private EpistemicListERF erfs;
	private TestLoc loc;
	private Period per;
	private Site site;
	
	private static int paramCount;
	private static HashMap<String, Integer> paramMap;
	private static List<String> paramHeader;
	
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
	public Void doInBackground() throws Exception {
		try {
		System.out.println("HELLO");
		init();
		HazardCurveCalculator calc = new HazardCurveCalculator();
		for (int i = 0; i < erfs.getNumERFs(); ++i) {
			DiscretizedFunc f = per.getFunction();
			ERF erf = erfs.getERF(i);
			f = calc.getHazardCurve(f, site, imr, erf);
			System.out.println("f: " + f);
			addResults(i, erf, f);
		}
		writeFiles();
		return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void addResults(int idx, ERF erf, DiscretizedFunc f) {
		// param data
		List<String> paramDat = Lists.newArrayList();
		paramDat.add(Integer.toString(idx));
		for (int i=0; i<paramCount; i++) paramDat.add(null);
		for (Parameter<?> param : erf.getAdjustableParameterList()) {
			int index = paramMap.get(param.getName())+1;
			paramDat.set(index, param.getValue().toString());
		}
		paramData.add(paramDat);
		// curve data
		List<String> curveDat = Lists.newArrayList();
		curveDat.add(Integer.toString(idx));
		for (Point2D p : f) {
			curveDat.add(Double.toString(p.getY()));
		}
		curveData.add(curveDat);
	}
	
	/*
	 * Initialize output lists, one for param values and another for curves
	 */
	private void init() {
		paramData = Lists.newArrayList();
		paramData.add(paramHeader);
		curveData = Lists.newArrayList();
		List<String> curveHeader = Lists.newArrayList();
		curveHeader.add("ERF#");
		for (Double d : per.getIMLs()) {
			curveHeader.add(d.toString());
		}
		curveData.add(curveHeader);
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
	
	
	
	
	
	//////////////////////////////////////
	//////////////////////////////////////
	//////////////////////////////////////

	
	public static void main(String[] args) {
		buildParamHeaders();
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
		System.out.println("MAP: " + IOUtils.LINE_SEPARATOR + map);
		System.out.println("HEADER: " + IOUtils.LINE_SEPARATOR + header);
	}
	

}
