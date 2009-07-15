package org.opensha.sha.calc.hazardMap.servlet;

import java.io.Serializable;
import java.text.Collator;

public class DatasetID implements Comparable<DatasetID>, Serializable {
	public static Collator c = Collator.getInstance();
	
	private String id;
	private String name;
	private boolean logFile;
	private boolean mapDir;
	
	public DatasetID(String id, String name, boolean logFile, boolean mapDir) {
		this.id = id;
		this.name = name;
		this.logFile = logFile;
		this.mapDir = mapDir;
	}

	public int compareTo(DatasetID d2) {
		return -1*c.compare(id, d2.getID());
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isLogFile() {
		return logFile;
	}

	public boolean isMapDir() {
		return mapDir;
	}

	public void setIsLogFile(boolean logFile) {
		this.logFile = logFile;
	}

	public void setIsMapDir(boolean mapDir) {
		this.mapDir = mapDir;
	}
}