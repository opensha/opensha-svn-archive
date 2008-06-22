package org.opensha.util;

public interface ProgressListener {
	
	public void setProgress(int currentIndex, int total);
	
	public void setProgressIndeterminate(boolean indeterminate);
	
	public void setProgressMessage(String message);
}
