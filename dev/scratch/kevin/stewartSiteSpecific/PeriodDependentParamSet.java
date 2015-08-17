package scratch.kevin.stewartSiteSpecific;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.util.Interpolate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PeriodDependentParamSet<E extends Enum<E>> {
	
	private E[] params;
	private Map<E, Integer> paramToIndexMap;
	
	// sorted, increasing
	private List<Double> periods;
	private List<double[]> values;
	
	public PeriodDependentParamSet(E[] params) {
		Preconditions.checkArgument(params.length > 0);
		
		this.params = params;
		
		paramToIndexMap = Maps.newHashMap();
		for (int i=0; i<params.length; i++)
			paramToIndexMap.put(params[i], i);
		
		periods = Lists.newArrayList();
		values = Lists.newArrayList();
	}
	
	public synchronized void set(double period, double[] vals) {
		Preconditions.checkState(vals.length == params.length);
		
		int index = Collections.binarySearch(periods, period);
		if (index >= 0) {
			// we're replacing
			values.set(index, vals);
		} else {
			// we're adding
			index = -(index + 1);
			periods.add(index, period);
			values.add(index, vals);
		}
	}
	
	public synchronized void set(double period, E param, double value) {
		int periodIndex = periodToIndex(period);
		Preconditions.checkState(periodIndex >= 0, "period not found: %s", period);
		
		set(periodIndex, param, value);
	}
	
	public synchronized void set(int periodIndex, E param, double value) {
		int paramIndex = paramToIndexMap.get(param);
		Preconditions.checkState(paramIndex >= 0, "param not found: %s", param);
		
		values.get(periodIndex)[paramIndex] = value;
	}
	
	public synchronized void remove(double period) {
		int index = periodToIndex(period);
		Preconditions.checkState(index >= 0, "period not found: %s", period);
		
		remove(index);
	}
	
	public synchronized void remove(int index) {
		Preconditions.checkState(index >= 0 && index < periods.size(), "bad index: %s", index);
		
		periods.remove(index);
		values.remove(index);
	}
	
	public synchronized void clear() {
		periods.clear();
		values.clear();
	}
	
	/**
	 * @return the number of periods
	 */
	public int size() {
		return periods.size();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * Gets a copy of the array of parameter enums.
	 * @return
	 */
	public E[] getParams() {
		return Arrays.copyOf(params, params.length);
	}
	
	/**
	 * Returns a sorted list of all current periods. This is an immutable view of the period list.
	 * @return
	 */
	public List<Double> getPeriods() {
		return Collections.unmodifiableList(periods);
	}
	
	public double getPeriod(int index) {
		return periods.get(index);
	}
	
	public int periodToIndex(double period) {
		return periods.indexOf(period);
	}
	
	/**
	 * Returns view of the values at the given period
	 * @param period
	 * @return
	 */
	public double[] getValues(double period) {
		int index = periodToIndex(period);
		Preconditions.checkState(index >= 0, "period not found: %s", period);
		return getValues(index);
	}
	
	/**
	 * Returns view of the values at the given index.
	 * 
	 * @param index
	 * @return
	 */
	public double[] getValues(int index) {
		int size = size();
		Preconditions.checkArgument(index >= 0 && index < size, "bad index: %s, size: %s", index, size);
		return Arrays.copyOf(values.get(index), params.length);
	}
	
	public int getParamIndex(E param) {
		return paramToIndexMap.get(param);
	}
	
	public double get(E param, double period) {
		int periodIndex = periodToIndex(period);
		Preconditions.checkState(periodIndex >= 0, "period not found: %s", period);
		return get(param, periodIndex);
	}
	
	public double get(E param, int periodIndex) {
		int paramIndex = paramToIndexMap.get(param);
		Preconditions.checkState(paramIndex >= 0, "param not found: %s", param);
		return values.get(periodIndex)[paramIndex];
	}
	
	public double[] get(E[] params, int periodIndex) {
		double[] ret = new double[params.length];
		for (int i=0; i<params.length; i++) {
			E param = params[i];
			int paramIndex = paramToIndexMap.get(param);
			Preconditions.checkState(paramIndex >= 0, "param not found: %s", param);
			ret[i] = values.get(periodIndex)[paramIndex];
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public double getInterpolated(E param, double period) {
		return doGetInterpolated(period, param)[0];
	}
	
	public double[] getInterpolated(E[] param, double period) {
		return doGetInterpolated(period, params);
	}
	
	private double[] doGetInterpolated(double period, E... params) {
		
		int periodIndex = Collections.binarySearch(periods, period);
		if (periodIndex >= 0)
			// exact match
			return get(params, periodIndex);
		// check for outside, in which case use first/last
		if (period < periods.get(0))
			return get(params, 0);
		if (period > periods.get(periods.size()-1))
			return get(params, periods.size()-1);
		
		// this means that it's not an exact match and is within min/max period
		int insertionIndex = -(periodIndex + 1);
		Preconditions.checkState(insertionIndex > 0 && insertionIndex < periods.size());
		
		double x1 = periods.get(insertionIndex-1);
		double x2 = periods.get(insertionIndex);
		
		double[] ret = new double[params.length];
		
		for (int i=0; i<params.length; i++) {
			double y1 = get(params[i], insertionIndex-1);
			double y2 = get(params[i], insertionIndex);
			
			ret[i] = Interpolate.findY(x1, y1, x2, y2, period);
		}
		
		return ret;
	}
	
	public static <E extends Enum<E>> PeriodDependentParamSet<E> loadCSV(E[] params, File csvFile) throws IOException {
		PeriodDependentParamSet<E> data = new PeriodDependentParamSet<E>(params);
		
		return loadCSV(data, csvFile);
	}
	
	public static <E extends Enum<E>> PeriodDependentParamSet<E> loadCSV(
			PeriodDependentParamSet<E> data, File csvFile) throws IOException {
		data.clear();
		
		E[] params = data.params;
		
		CSVFile<String> csv = CSVFile.readFile(csvFile, true);
		Preconditions.checkState(csv.getNumCols() == params.length+1, "Param count mismatch");
		
		for (int i=0; i<params.length; i++) {
			String paramName = params[i].name().trim();
			String inputName = csv.get(0, i+1).trim();
			Preconditions.checkState(paramName.equals(inputName),
					"Parameter mismatch at column %s. Expected: %s, Actual: %s", i, paramName, inputName);
		}
		
		for (int row=1; row<csv.getNumRows(); row++) {
			List<String> line = csv.getLine(row);
			double period = Double.parseDouble(line.get(0));
			double[] values = new double[params.length];
			for (int i=0; i<params.length; i++)
				values[i] = Double.parseDouble(line.get(i+1));
			data.set(period, values);
		}
		
		return data;
	}
	
	public void writeCSV(File csvFile) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList("Period");
		for (E param : params)
			header.add(param.name());
		csv.addLine(header);
		
		for (int i=0; i<size(); i++) {
			List<String> line = Lists.newArrayList(getPeriod(i)+"");
			for (double val : getValues(i))
				line.add(val+"");
			csv.addLine(line);
		}
		
		csv.writeToFile(csvFile);
	}
}
