package org.opensha.sha.calc.hazardMap;

public interface ProgressListener {
	
	public void updateProgress(int currentIndex, int total);
	
	public void setIndeterminate(boolean indeterminate);
	
	public void setMessage(String message);
}
