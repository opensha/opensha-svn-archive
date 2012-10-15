package org.opensha.nshmp2.calc;

import static org.opensha.nshmp2.util.Period.GM0P20;
import static org.opensha.nshmp2.util.Period.GM1P00;
import static org.opensha.sra.rtgm.RTGM.Frequency.SA_0P20;
import static org.opensha.sra.rtgm.RTGM.Frequency.SA_1P00;
import static scratch.peter.curves.ProbOfExceed.*;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.util.Period;
import org.opensha.sra.rtgm.RTGM;
import org.opensha.sra.rtgm.RTGM.Frequency;

import scratch.peter.curves.ProbOfExceed;

import com.google.common.base.Charsets;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;

/**
 * Writer of hazard result
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardResultWriterCities implements HazardResultWriter {

	private static final Joiner JOIN = Joiner.on(',');
	private BufferedWriter writer;
	private boolean doRTGM;
	private Period period;

	/**
	 * Creates anew local writer instance.
	 * @param outFile output location
	 * @param period
	 * @throws IOException
	 */
	public HazardResultWriterCities(File outFile, Period period)
			throws IOException {
		this.period = period;
		Files.createParentDirs(outFile);
		writer = Files.newWriter(outFile, Charsets.US_ASCII);
		writeCurveHeader(writer, period);
		doRTGM = period == GM1P00 || period == GM0P20;
	}

	@Override
	public void write(HazardResult result) throws IOException {
		DiscretizedFunc f = result.curve();
		double pe2in50 = ProbOfExceed.get(f, PE2IN50);
		double pe10in50 = ProbOfExceed.get(f, PE10IN50);
		double rtgm = (doRTGM) ? getRTGM(f, period) : 0;
		NEHRP_TestCity city = NEHRP_TestCity.forLocation(result.location());
		Iterable<String> cityData = createResult(city, pe2in50, pe10in50, rtgm, f);
		String cityLine = JOIN.join(cityData);
		writer.write(cityLine);
		writer.newLine();
	}

	@Override
	public void close() {
		Flushables.flushQuietly(writer);
		Closeables.closeQuietly(writer);
	}

	private static double getRTGM(DiscretizedFunc f, Period period) {
		Frequency freq = period.equals(GM0P20) ? SA_0P20 : SA_1P00;
		RTGM rtgm = RTGM.create(f, freq, 0.8).call();
		return rtgm.get();
	}

	private static Iterable<String> createResult(NEHRP_TestCity city, double pe2in50, 
			double pe10in50, double rtgm, DiscretizedFunc curve) {
		
		Iterable<String> intercepts = Lists.newArrayList(
			city.name(),
			Double.toString(pe2in50),
			Double.toString(pe10in50),
			Double.toString(rtgm));

		Iterable<String> values = Collections2.transform(
			curve.yValues(),
			Functions.toStringFunction());
		
		return Iterables.concat(intercepts, values);
	}

	/**
	 * Utility method to write header for a set of curves into a {@code Writer}.
	 * @param writer to add header to
	 * @param period for reference x (ground motion) values
	 * @throws IOException
	 */
	public static void writeCurveHeader(BufferedWriter writer, Period period)
			throws IOException {

		Iterable<String> headers = Lists.newArrayList("city", "2in50",
			"10in50", "rtgm");
		Iterable<String> xValues = Collections2.transform(
			period.getIMLs(),
			Functions.toStringFunction());

		Iterable<String> headerData = Iterables.concat(headers, xValues);
		writer.write(JOIN.join(headerData));
		writer.newLine();
	}

}
