package org.opensha.sha.earthquake.rupForecastImpl.nshmp.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.eq.MagUtils;

import com.google.common.collect.Iterators;
import com.google.common.io.LittleEndianDataInputStream;

/**
 * NSHMP uility methods. Some will eventually be relocated to commons.
 * Others should be in a package private class.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_Utils {

	public static final String WARN_INDENT = "          ";


	/** Path to NSHMP config file directory. */
	public static final String CONF_DIR = "/resources/data/nshmp/sources/";
	
	/**
	 * Reads the specified number of lines into a new line list.
	 * @param it line iterator
	 * @param n number of lines to read
	 * @return a list of lines
	 */
	public static List<String> readLines(Iterator<String> it, int n) {
		List<String> lineSet = new ArrayList<String>();
		for (int i = 0; i < n; i++) {
			lineSet.add(it.next());
		}
		return lineSet;
	}

	/**
	 * Splits line on spaces and reads the value at <code>pos</code> as an
	 * <code>int</code>.
	 * @param line to read from
	 * @param pos position to read
	 * @return the <code>int</code> value at <code>pos</code>
	 */
	public static int readInt(String line, int pos) {
		return readInt(StringUtils.split(line), pos);
	}

	/**
	 * Reads the value at <code>pos</code> in array as an <code>int</code>.
	 * @param vals the <code>String</code> array to read from
	 * @param pos position to read
	 * @return the <code>int</code> value at <code>pos</code>
	 */
	public static int readInt(String[] vals, int pos) {
		return Integer.parseInt(vals[pos]);
	}

	/**
	 * Reads the specified number of values on a line as doubles.
	 * @param line to read from
	 * @param n number of values to read
	 * @return an array of <code>double</code>s
	 */
	public static int[] readInts(String line, int n) {
		String[] dat = StringUtils.split(line);
		int[] vals = new int[n];
		for (int i = 0; i < n; i++) {
			vals[i] = Integer.parseInt(dat[i]);
		}
		return vals;
	}

	/**
	 * Splits line on spaces and reads the value at <code>pos</code> as a
	 * <code>double</code>.
	 * @param line to read from
	 * @param pos position to read
	 * @return the <code>double</code> value at <code>pos</code>
	 */
	public static double readDouble(String line, int pos) {
		return readDouble(StringUtils.split(line), pos);
	}

	/**
	 * Reads the value at <code>pos</code> in array as a <code>double</code>.
	 * @param vals the <code>String</code> array to read from
	 * @param pos position to read
	 * @return the <code>double</code> value at <code>pos</code>
	 */
	public static double readDouble(String[] vals, int pos) {
		return Double.parseDouble(vals[pos]);
	}

	/**
	 * Reads the specified number of values on a line as doubles.
	 * @param line to read from
	 * @param n number of values to read
	 * @return an array of <code>double</code>s
	 */
	public static double[] readDoubles(String line, int n) {
		String[] dat = StringUtils.split(line);
		double[] vals = new double[n];
		for (int i = 0; i < n; i++) {
			vals[i] = Double.parseDouble(dat[i]);
		}
		return vals;
	}

	/**
	 * Computes total moment rate as done by NSHMP code from supplied magnitude
	 * info and the Gutenberg-Richter a- and b-values. <b>Note:</b> the a- and
	 * b-values assume an incremental distribution.
	 * 
	 * @param mMin minimum magnitude (after adding <code>dMag</code>/2)
	 * @param nMag number of magnitudes
	 * @param dMag magnitude bin width
	 * @param a value (incremental and defined wrt <code>dMag</code>
	 * @param b value
	 * @return the total moment rate
	 */
	public static double totalMoRate(double mMin, int nMag, double dMag,
			double a, double b) {
		double moRate = 1e-10; // start with small, non-zero rate
		double M;
		for (int i = 0; i < nMag; i++) {
			M = mMin + i*dMag;
			moRate += MagUtils.gr_rate(a, b, M) * MagUtils.magToMoment(M);
		}
		return moRate;
	}

	/**
	 * Reads lines from a file, skipping any that start with '#'.
	 * @param f file to read from
	 * @param log to log to in the event of an error; may be <code>null</code>
	 * @return a <code>List</code> of <code>String</code>s or <code>null</code>
	 *         if an error occurs
	 */
	public static List<String> readLines(File f, Logger log) {
		try {
			List<String> list = new ArrayList<String>();
			LineIterator it = FileUtils.lineIterator(f);
			String line;
			while (it.hasNext()) {
				if ((line = it.next()).startsWith("#")) continue;
				list.add(line);
			}
			return list;
		} catch (IOException ioe) {
			if (log != null) {
				log.log(Level.SEVERE,
					"Error reading lines from file: " + f.getPath(), ioe);
			}
			return null;
		}
	}

	private static Logger log;
	
	public static Logger logger() {
		if (log != null) return log;
		log = Logger.getLogger("org.opensha.sha.nshmp");
		log.setLevel(Level.WARNING);
		log.setUseParentHandlers(false);

		Formatter cf = new Formatter() {
			@Override
			public String format(LogRecord lr) {
				// @formatter:off
				StringBuilder b = new StringBuilder();
				Level l = lr.getLevel();
				b.append("[").append(l).append("]");
				if (l == Level.SEVERE || l == Level.WARNING) {
					
					b.append(" ").append(lr.getMessage())
					.append(IOUtils.LINE_SEPARATOR)
					.append(WARN_INDENT)
					.append(lr.getSourceClassName())
					.append(": ")
					.append(lr.getSourceMethodName())
					.append("()")
					.append(IOUtils.LINE_SEPARATOR)
					;
					if (lr.getThrown() != null) {
						b.append(lr.getThrown());
					}
				} else {
					b.append(" ").append(lr.getMessage());
				}
				b.append(IOUtils.LINE_SEPARATOR);
				return b.toString();
				// @formatter:on
			}
		};
		Handler ch = new ConsoleHandler();
		ch.setFormatter(cf);
		ch.setLevel(log.getLevel());
		log.addHandler(ch);
		return log;
	}
	
	/**
	 * Method reads a binary file of data into an array. This method is tailored
	 * to the NSHMP grid files that are stored from top left to bottom right,
	 * reading across. The nodes in OpenSHA <code>GriddedRegion</code>s are
	 * stored from bottom left to top right, also reading across. This method
	 * places values at their proper index. <i><b>NOTE</b></i>: NSHMP binary
	 * grid files are all currently little-endian. The grid files in some other
	 * parts of the USGS seismic hazard world are big-endian. Beware.
	 * @param file to read
	 * @param nRows 
	 * @param nCols 
	 * @return a 1D array of appropriately ordered values
	 */
	public static double[] readGrid(File file, int nRows, int nCols) {
		int count = nRows * nCols;
		double[] data = new double[count];
		try {
			LittleEndianDataInputStream in = new LittleEndianDataInputStream(
				FileUtils.openInputStream(file));
			for (int i=0; i<count; i++) {
				double value = new Float(in.readFloat()).doubleValue();
				data[calcIndex(i, nRows, nCols)] = value;
			}
			in.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		return data;
	}
	
	/**
	 * Custom method to read NSHMP CEUS craton and margin files. These are
	 * fortran output logical files and have 4 f-specific bytes on each end of
	 * the file. Each logical fills 4bytes and although the files contain 
	 * 4*128000 bytes, the CEUS mMax files are used in cra.f when generating 
	 * so only 4*127755 are filled. The reamining slots are false and not
	 * considered.
	 * @param file to read
	 * @param nRows 
	 * @param nCols 
	 * @return a 1D array of appropriately ordered values
	 */
	public static boolean[] readBoolGrid(File file, int nRows, int nCols) {
		int count = nRows * nCols;
		boolean[] data = new boolean[count];
		try {
			DataInputStream in = new DataInputStream(
				FileUtils.openInputStream(file));
			// skip first four bytes
			in.skipBytes(4);
			for (int i=0; i<count; i++) {
				int iCor = NSHMP_Utils.calcIndex(i, nRows, nCols);
				// read first byte of each set of four
				data[iCor] = (in.readByte() == 0) ? false : true;
				in.skipBytes(3);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return data;
	}

	/* 
	 * This method converts an NSHMP index to the correct GriddedRegion index
	 */
	private static int calcIndex(int idx, int nRows, int nCols) {
		return (nRows - (idx / nCols) - 1) * nCols + (idx % nCols);
		// compact form of:
		// int col = idx % nCols;
		// int row = idx / nCols;
		// int targetRow = nRows - row - 1;
		// return targetRow * nCols + col;
	}


	
}
