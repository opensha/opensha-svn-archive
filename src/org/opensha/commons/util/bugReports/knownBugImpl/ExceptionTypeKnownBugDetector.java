package org.opensha.commons.util.bugReports.knownBugImpl;

import org.opensha.commons.util.bugReports.BugReport;
import org.opensha.commons.util.bugReports.KnownBugDetector;

public class ExceptionTypeKnownBugDetector implements KnownBugDetector {
	
	private Class<? extends Throwable> exceptionClass;
	private String desc;
	
	public ExceptionTypeKnownBugDetector(Class<? extends Throwable> exceptionClass, String desc) {
		this.exceptionClass = exceptionClass;
		this.desc = desc;
	}

	@Override
	public boolean isKnownBug(BugReport bug) {
		Throwable t = bug.getThrowable();
		if (t == null)
			return false;
		return t.getClass().equals(exceptionClass);
	}

	@Override
	public String getKnownBugDescription() {
		return desc;
	}

}
