package scratch.kevin.cybershake.etasCalcs;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.DataUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DurationLogPlotter {
	
	private static final long day_millis = 24l*60l*60l*1000l;

	public static void main(String[] args) throws IOException {
		SimpleDateFormat df = MPJTaskCalculator.df;
		
//		File logFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2015_05_13-mojave_7/"
//				+ "2015_05_13-mojave_7.pbs.o5290046");
		File logFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2015_06_10-mojave_7/"
				+ "2015_06_10-mojave_7.pbs.o5388265");
		
		Map<Integer, Date> startDates = Maps.newHashMap();
		Map<Integer, Date> endDates = Maps.newHashMap();
		
		BufferedReader tis =
				new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
		String str = tis.readLine();
		while(str != null) {
			if (str.startsWith("[") && (str.contains("calculating") || str.contains("completed"))) {
				if (str.contains("batch")) {
					str = tis.readLine();
					continue;
				}
				// isolate date
				String dateStr = str.substring(1);
				dateStr = dateStr.substring(0, dateStr.indexOf(" "));
				Date date;
				try {
					date = df.parse(dateStr);
				} catch (ParseException e) {
					str = tis.readLine();
					continue;
				}
				
				// now get index
				String[] split = str.split(" ");
				try {
					Integer index = Integer.parseInt(split[split.length-1]);
					if (str.contains("calculating")) {
//						Preconditions.checkState(!startDates.containsKey(index), "Duplicate for "+index);
						startDates.put(index, date);
					} else {
						Preconditions.checkState(!endDates.containsKey(index));
						endDates.put(index, date);
					}
				} catch (NumberFormatException e) {
					str = tis.readLine();
					continue;
				}
			}
			str = tis.readLine();
		}
		tis.close();
		
		System.out.println("Successfully parsed "+startDates.size()+" start dates and "+endDates.size()+" end dates");
		
		Map<Integer, Double> deltaSecsMap = Maps.newHashMap();
		
		for (Integer index : endDates.keySet()) {
			if (!startDates.containsKey(index))
				continue;
			Date start = startDates.get(index);
			Date end = endDates.get(index);
			
			long diffMillis = end.getTime() - start.getTime();
			while (diffMillis < 0)
				diffMillis += day_millis;
			double diffSecs = diffMillis / 1000d;
			
			deltaSecsMap.put(index, diffSecs);
		}
		
		HistogramFunction hist = new HistogramFunction(0.5, 100, 1d);
		
		double[] vals = new double[deltaSecsMap.size()];
		int cnt = 0;
		
		for (Integer index : deltaSecsMap.keySet()) {
			double delta = deltaSecsMap.get(index)/60d; // convert to minutes
			int histInd = hist.getClosestXIndex(delta);
			hist.add(histInd, 1d);
			
			vals[cnt++] = delta;
		}
		
		System.out.println("Mean: "+StatUtils.mean(vals)+" m, Median: "+DataUtils.median(vals)+" m");
		
		List<XY_DataSet> elems = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		elems.add(hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		PlotSpec histSpec = new PlotSpec(elems, chars, "Simulation Time Hist", "Duration (minutes)", "Number");
		
		GraphWindow gw = new GraphWindow(histSpec);
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
	}

}
