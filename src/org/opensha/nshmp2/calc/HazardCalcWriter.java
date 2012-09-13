package org.opensha.nshmp2.calc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.opensha.commons.geo.Location;
import org.opensha.nshmp2.util.Period;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;

/**
 * Manages the writing of {@code HazardCalcResult}s from a {@code Queue} to a
 * {@code File}. This class is not a {@code java.io.Writer}, although it does
 * use one internally.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcWriter implements Callable<Void> {

	private static final Joiner J = Joiner.on(',').useForNull(" ");
	private BlockingQueue<HazardCalcResult> queue;
	private BufferedWriter writer;
	
	/**
	 * Creates a new writer of {@code HazardCalcResult}s. 
	 * 
	 * @param queue to take results from 
	 * @param out file
	 * @param period for hazard data; used to retrieve IMLs
	 * @throws IOException if {@code out} file initialization fails
	 */
	public HazardCalcWriter(BlockingQueue<HazardCalcResult> queue, File out, Period period) 
			throws IOException {
		checkNotNull(queue);
		checkNotNull(out);
		this.queue = queue;
		Files.createParentDirs(out);
		writer = Files.newWriter(out, Charsets.US_ASCII);
		writeHeader(period);
	}
	
	/**
	 * Closes the streams used by this class.
	 */
	public void close() {
		Flushables.flushQuietly(writer);
		Closeables.closeQuietly(writer);
	}
	
	@Override
	public Void call() throws Exception {
		while (true) write(queue.take());
	}

	/*
	 * Write a result.
	 */
	private void write(HazardCalcResult result) throws IOException {
		String resultStr = formatResult(result);
		writer.write(resultStr);
		writer.newLine();
	}
	
	/*
	 * Format a result.
	 */
	private static String formatResult(HazardCalcResult result) {
		List<String> dat = Lists.newArrayList();
		Location loc = result.location();
		dat.add(Double.toString(loc.getLatitude()));
		dat.add(Double.toString(loc.getLongitude()));
		for (Point2D p : result.curve()) {
			dat.add(Double.toString(p.getY()));
		}
		return J.join(dat);
	}
	
	private void writeHeader(Period p) throws IOException {
		List<String> headerVals = Lists.newArrayList();
		headerVals.add("lat");
		headerVals.add("lon");
		for (Double d : p.getIMLs()) {
			headerVals.add(d.toString());
		}
		writer.write(J.join(headerVals));
		writer.newLine();
	}

}
