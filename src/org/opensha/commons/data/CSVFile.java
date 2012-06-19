package org.opensha.commons.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.util.FileUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class CSVFile<E> implements Iterable<List<E>> {
	
	private List<List<E>> values;
//	private List<String> colNames;
//	private Map<String, ? extends List<E>> values;
	private int cols;
	private boolean strictRowSizes;
	
	public CSVFile(boolean strictRowSizes) {
		this(null, strictRowSizes);
	}
	
	public CSVFile(List<List<E>> values, boolean strictRowSizes) {
		if (values == null)
			values = new ArrayList<List<E>>();
		this.strictRowSizes = strictRowSizes;
		cols = -1;
		if (strictRowSizes) {
			for (List<E> row : values) {
				if (cols < 0)
					cols = row.size();
				else
					Preconditions.checkArgument(cols == row.size(),
							"Values lists aren't the same size!");
			}
		}
		this.values = values;
	}
	
	public int getNumRows() {
		return values.size();
	}
	
	/**
	 * Return the number or rows, or -1 if empty or non strict row sizes
	 * @return
	 */
	public int getNumCols() {
		return cols;
	}
	
	/**
	 * @return true if all rows must have the same number of columns, false otherwise
	 */
	public boolean isStrictRowSizes() {
		return strictRowSizes;
	}
	
	public void set(int row, int col, E value) {
		getLine(row).set(col, value);
	}
	
	public void addLine(List<E> line) {
		checkValidLine(line);
		values.add(line);
	}
	
	public void addLine(E... line) {
		addLine(Lists.newArrayList(line));
	}
	
	public void addLine(int index, List<E> line) {
		checkValidLine(line);
		values.add(index, line);
	}
	
	public void setLine(int index, List<E> line) {
		checkValidLine(line);
		values.set(index, line);
	}
	
	public void addAll(Collection<List<E>> lines) {
		Preconditions.checkNotNull(lines, "lines cannot be null!");
		// first make sure they're ALL going to pass before adding anything
		for (List<E> line : lines) {
			checkValidLine(line);
		}
		// add them.
		for (List<E> line : lines) {
			values.add(line);
		}
	}
	
	public List<E> removeLine(int index) {
		List<E> ret = values.remove(index);
		
		// if list is now empty, reset column size
		if (values.isEmpty())
			cols = -1;
		
		return ret;
	}
	
	private void checkValidLine(List<E> line) {
		Preconditions.checkNotNull(line, "Cannot add a null line!");
		if (strictRowSizes) {
			if (cols < 0) {
				// this means it's empty
				cols = line.size();
			} else {
				Preconditions.checkArgument(line.size() == cols, "New line must contain" +
					" same number of values as columns (expected "+cols+", got "+line.size()+")");
			}
		}
	}
	
	public E get(int row, int col) {
		return getLine(row).get(col);
	}
	
	public List<E> getLine(int index) {
		return values.get(index);
	}
	
	public String getLineStr(int i) {
		return getLineStr(getLine(i));
	}
	
	public static String getLineStr(List<?> line) {
		return getLineStr(line.toArray());
	}
	
	public static String getLineStr(Object[] line) {
		String lineStr = null;
		for (Object val : line) {
			if (lineStr == null)
				lineStr = "";
			else
				lineStr += ",";
			String valStr;
			if (val == null)
				valStr = ""+null;
			else
				valStr = val.toString();
			// if it contains a comma, surround it in quotation marks if not already
			if (valStr.contains(",") && !(valStr.startsWith("\"") && valStr.endsWith("\"")))
				valStr = "\""+valStr+"\"";
			lineStr += valStr;
		}
		return lineStr;
	}
	
	public String getHeader() {
		return getLineStr(getLine(0));
	}
	
	public void writeToFile(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		for (int i=0; i<getNumRows(); i++) {
			fw.write(getLineStr(i) + "\n");
		}
		fw.close();
	}
	
	public void writeToTabSeparatedFile(File file, int headerLines) throws IOException {
		FileWriter fw = new FileWriter(file);

		for (int i=0; i<getNumRows(); i++) {
			List<E> line = getLine(i);

			String lineStr = null;
			for (E val : line) {
				if (lineStr == null)
					lineStr = "";
				else
					lineStr += "\t";
				lineStr += val.toString();
			}

			if (i < headerLines)
				// header
				lineStr = "# "+lineStr;

			fw.write(lineStr + "\n");
		}

		fw.close();
	}
	
	public void removeColumn(int i) {
		Preconditions.checkArgument(i >= 0, "column must be >= 0");
		Preconditions.checkArgument(cols < 0 || i < cols, "invalid column: "+i);
		
		for (List<E> list : values) {
			if (list.size() > i)
				list.remove(i);
		}
	}
	
	private static ArrayList<String> loadLine(String line, int num) {
		line = line.trim();
		ArrayList<String> vals = new ArrayList<String>();
		boolean inside = false;
		String cur = "";
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (!inside && c == ',') {
				// we're done with a value
				vals.add(cur);
				cur = "";
				continue;
			}
			if (c == '"') {
				inside = !inside;
				continue;
			}
			cur += c;
		}
		if (!cur.isEmpty())
			vals.add(cur);
		while (vals.size() < num)
			vals.add("");
		return vals;
	}
	
	public static CSVFile<String> readFile(File file, boolean strictRowSizes) throws IOException {
		return readFile(file, strictRowSizes, -1);
	}
	
	public static CSVFile<String> readFile(File file, boolean strictRowSizes, int cols) throws IOException {
		return readURL(file.toURI().toURL(), strictRowSizes, cols);
	}
	
	public static CSVFile<String> readURL(URL url, boolean strictRowSizes) throws IOException {
		return readURL(url, strictRowSizes, -1);
	}
	
	public static CSVFile<String> readURL(URL url, boolean strictRowSizes, int cols) throws IOException {
		return readStream((InputStream)url.getContent(), strictRowSizes, cols);
	}
	
	public static CSVFile<String> readStream(InputStream is, boolean strictRowSizes) throws IOException {
		return readStream(is, strictRowSizes, -1);
	}
	
	public static CSVFile<String> readStream(InputStream is, boolean strictRowSizes, int cols) throws IOException {
		List<List<String>> values = new ArrayList<List<String>>();
		for (String line : FileUtils.loadStream(is)) {
			if (strictRowSizes && cols < 0) {
				cols = loadLine(line, -1).size();
			}
			ArrayList<String> vals = loadLine(line, cols);
			if (strictRowSizes && vals.size() > cols)
				throw new IllegalStateException("Line lenghts inconsistant and strictRowSizes=true");
			values.add(vals);
		}
		
		return new CSVFile<String>(values, strictRowSizes);
	}

	@Override
	public Iterator<List<E>> iterator() {
		return values.iterator();
	}
	
	public void sort(int col, int headerRows, Comparator<E> comparator) {
		ArrayList<List<E>> header = Lists.newArrayList();
		for (int row=0; row<headerRows; row++)
			header.add(removeLine(0));
		
		// sort
		ColumnComparator comp = new ColumnComparator(col, comparator);
		Collections.sort(values, comp);
		
		Collections.reverse(header);
		for (List<E> line : header)
			addLine(0, line);
	}
	
	private class ColumnComparator implements Comparator<List<E>> {
		private Comparator<E> comp;
		private int col;
		
		public ColumnComparator(int col, Comparator<E> comp) {
			this.col = col;
			this.comp = comp;
		}

		@Override
		public int compare(List<E> o1, List<E> o2) {
			return comp.compare(o1.get(col), o2.get(col));
		}
		
	}

}
