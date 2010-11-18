package org.opensha.commons.eq.cat.io;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.eq.cat.Catalog;

/**
 * Class provides basic functionality for catalog file reading. Subclass
 * constructors should initialize arrays as necessary and must implement
 * <code>parseLine()</code>. This reader assumes that all event dates are UTC
 * and should be converted first if they are not.
 *
 * @author Peter Powers
 * @version $Id: AbstractReader.java 31 2010-01-18 18:04:51Z peter $
 */
public abstract class AbstractReader implements CatalogReader {

	private String name;
	private String description;

	/** EventID data store. */
	protected List<Integer> dat_eventIDs;
	/** Event dates data store. */
	protected List<Long> dat_dates;
	/** Event longitudes data store. */
	protected List<Double> dat_longitudes;
	/** Event longitudes data store. */
	protected List<Double> dat_latitudes;
	/** Event depths data store. */
	protected List<Double> dat_depths;
	/** Event magnitudes data store. */
	protected List<Double> dat_magnitudes;
	/** Event magnitudeTypes data store. */
	protected List<Integer> dat_magnitudeTypes;
	/** Event type data store. */
	protected List<Integer> dat_eventTypes;
	/** Event quality data store. */
	protected List<Integer> dat_eventQuality;
	/** Event xy errors data store. */
	protected List<Double> dat_xyErrors;
	/** Event z errors data store. */
	protected List<Double> dat_zErrors;
	/** Event fault plane strikes data store. */
	protected List<Integer> dat_fpStrikes;
	/** Event fault plane dips data store. */
	protected List<Integer> dat_fpDips;
	/** Event fault plane rakes data store. */
	protected List<Integer> dat_fpRakes;

	/**
	 * Utility calendar preset to UTC time for manipulating date/time values.
	 */
	protected final GregorianCalendar cal = new GregorianCalendar(
		TimeZone.getTimeZone("UTC"));

	/** Catalog to receive data. */
	protected Catalog catalog;

	/** Initial size of data arrays. */
	private static final int READER_ARRAY_SIZE = 10000;

	/** Initial size of data import arrays. */
	protected int size;

	/**
	 * Constructs a new catalog file reader that will use the supplied size to
	 * initialize internal data arrays.
	 *
	 * @param size to use when initializing internal data arrays
	 * @throws IllegalArgumentException if <code>size</code> is less than 1
	 */

	public AbstractReader(int size) {
		this("No name", "No description", size);
	}

	/**
	 * Constructs a new catalog file reader that will initialize internal data
	 * arrays to a length of 10000.
	 *
	 * @param name of the reader
	 * @param description of the reader
	 */
	public AbstractReader(String name, String description) {
		this(name, description, READER_ARRAY_SIZE);
	}

	/**
	 * Constructs a new catalog file reader that will use the supplied size to
	 * initialize internal data arrays. 
	 *
	 * @param name of the reader
	 * @param description of the reader
	 * @param size to use when initializing internal data arrays
	 * @throws IllegalArgumentException if <code>size</code> is less than 1
	 */
	public AbstractReader(String name, String description, int size) {
		checkArgument(size > 0, "Supplied array size must be positive");
		this.size = size;
		this.name = name;
		this.description = description;
	}

	/**
	 * Initialize reader by instantiating necessary arrays.
	 */
	public abstract void initReader();

	/**
	 * Parse a line of text into values for data arrays.
	 *
	 * @param line to parse
	 * @throws IllegalArgumentException if a parsing problem occurs
	 * @throws NullPointerException if <code>line</code> is <code>null</code>
	 */
	public abstract void parseLine(String line);

	/**
	 * Populate the internal catalog with data; called once all lines have bneen
	 * parsed/processed. Implementations may throw an
	 * <code>IllegalArgumentException</code> if problems occur while loading
	 * data arrays.
	 */
	public abstract void loadData();

	@Override
	public void process(File file, Catalog catalog) throws IOException {
		this.catalog = catalog;
		BufferedReader br = null;
		try {
			initReader();
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
			loadData();
		} catch (IOException ioe) {
			throw new IOException("Error opening catalog file: "
				+ file.getName(), ioe);
		} finally {
			clearArrays();
			IOUtils.closeQuietly(br);
		}
	}

	/**
	 * Resets all internal arrays to null, thereby releasing references to data.
	 */
	private void clearArrays() {
		dat_eventIDs = null;
		dat_eventTypes = null;
		dat_dates = null;
		dat_longitudes = null;
		dat_latitudes = null;
		dat_depths = null;
		dat_magnitudes = null;
		dat_magnitudeTypes = null;
		dat_eventQuality = null;
		dat_xyErrors = null;
		dat_zErrors = null;
		dat_fpStrikes = null;
		dat_fpDips = null;
		dat_fpRakes = null;
	}

	/**
	 * Overriden to return the name of this reader.
	 */
	@Override
	public String toString() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}
}
