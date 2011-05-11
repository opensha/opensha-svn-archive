package org.opensha.commons.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensha.commons.util.FileUtils;

import com.google.common.base.Preconditions;

public class CSVFile<E> {
	
	private List<String> colNames;
	private Map<String, ? extends List<E>> values;
	private int listSize;
	
	public CSVFile(List<String> colNames) {
		this(colNames, null);
	}
	
	public CSVFile(List<String> colNames, Map<String, ? extends List<E>> values) {
		Preconditions.checkNotNull(colNames, "Column names cannot be null!");
		if (values == null) {
			HashMap<String, ArrayList<E>> newValues = new HashMap<String, ArrayList<E>>();
			for (String colName : colNames) {
				newValues.put(colName, new ArrayList<E>());
			}
			values = newValues;
		}
		Preconditions.checkArgument(colNames.size() == values.keySet().size(),
				"column names must be the same size as values!");
		for (String colName : colNames) {
			Preconditions.checkArgument(values.keySet().contains(colName),
					"Column '"+colName+"' not found in values!");
		}
		listSize = -1;
		for (List<E> list : values.values()) {
			if (listSize < 0)
				listSize = list.size();
			else
				Preconditions.checkArgument(listSize == list.size(),
						"Values lists aren't the same size!");
		}
		this.colNames = colNames;
		this.values = values;
	}
	
	public int getNumLines() {
		return listSize;
	}
	
	public int getNumCols() {
		return colNames.size();
	}
	
	public void addLine(List<E> line) {
		Preconditions.checkNotNull(line, "Cannot add a null line!");
		Preconditions.checkArgument(line.size() == colNames.size(), "New line must contain" +
				" same number of values as columns");
		
		for (int i=0; i<line.size(); i++) {
			String colName = colNames.get(i);
			E value = line.get(i);
			values.get(colName).add(value);
		}
	}
	
	public ArrayList<E> getLine(int i) {
		ArrayList<E> line = new ArrayList<E>();
		for (String colName : colNames) {
			E val = values.get(colName).get(i);
			line.add(val);
		}
		return line;
	}
	
	private ArrayList<E> getLineValues(int i) {
		ArrayList<E> lineVals = new ArrayList<E>();
		for (String colName : colNames) {
			E val = values.get(colName).get(i);
			lineVals.add(val);
		}
		return lineVals;
	}
	
	public String getLineStr(int i) {
		return getLineStr(getLineValues(i));
	}
	
	public static String getLineStr(List<?> lineValues) {
		return getLineStr(lineValues.toArray());
	}
	
	public static String getLineStr(Object[] lineValues) {
		String line = null;
		for (Object val : lineValues) {
			if (line == null)
				line = "";
			else
				line += ",";
			String valStr = val.toString();
			// if it contains a comma, surround it in quotation marks if not already
			if (valStr.contains(",") && !(valStr.startsWith("\"") && valStr.endsWith("\"")))
				valStr = "\""+valStr+"\"";
			line += val.toString();
		}
		return line;
	}
	
	public String getHeader() {
		return getLineStr(colNames);
	}
	
	public void writeToFile(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		fw.write(getHeader() + "\n");
		for (int i=0; i<getNumLines(); i++) {
			fw.write(getLineStr(i) + "\n");
		}
		fw.close();
	}
	
	private static ArrayList<String> loadLine(String line, int num) {
		line = line.trim();
		String[] split = line.split(",");
		ArrayList<String> vals = new ArrayList<String>();
		for (String str : split)
			vals.add(str);
		while (vals.size() < num)
			vals.add("");
		return vals;
	}
	
	public static CSVFile<String> readFile(File file) throws IOException {
		ArrayList<String> colNames = null;
		HashMap<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>();
		for (String line : FileUtils.loadFile(file.toURI().toURL())) {
			if (colNames == null) {
				colNames = loadLine(line, -1);
				for (String colName : colNames)
					values.put(colName, new ArrayList<String>());
				continue;
			}
			ArrayList<String> vals = loadLine(line, colNames.size());
			for (int i=0; i<colNames.size(); i++) {
				values.get(colNames.get(i)).add(vals.get(i));
			}
		}
		
		return new CSVFile<String>(colNames, values);
	}

}
