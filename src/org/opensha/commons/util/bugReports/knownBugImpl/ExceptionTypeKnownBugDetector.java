package org.opensha.commons.util.bugReports.knownBugImpl;

import org.opensha.commons.util.bugReports.BugReport;
import org.opensha.commons.util.bugReports.KnownBugDetector;

public class ExceptionTypeKnownBugDetector implements KnownBugDetector {
	
	private Class<? extends Throwable> exceptionClass;
	private String desc;
	private String message;
	private boolean messageIsRegex = false;
	
	public ExceptionTypeKnownBugDetector(Class<? extends Throwable> exceptionClass, String desc) {
		this(exceptionClass, null, desc);
	}
	
	public ExceptionTypeKnownBugDetector(
			Class<? extends Throwable> exceptionClass,
			String message,
			String desc) {
		this.exceptionClass = exceptionClass;
		this.desc = desc;
		this.message = message;
	}
	
	public ExceptionTypeKnownBugDetector setMessageAsRegex() {
		messageIsRegex = true;
		// return this so it can be called inline with the constructor;
		return this;
	}

	@Override
	public boolean isKnownBug(BugReport bug) {
		Throwable t = bug.getThrowable();
		return isExceptionMatch(t);
	}
	
	private boolean isExceptionMatch(Throwable t) {
		if (t == null)
			return false;
		if (t.getCause() != null && isExceptionMatch(t.getCause()))
			return true;
		if (t.getClass().equals(exceptionClass)) {
			// if the exception type is a match, then return true if our message
			// to match is null, or the message from the exception is not null and
			// starts with our message.
			if (message == null) {
				return true;
			} else if (t.getMessage() != null) {
				if (messageIsRegex)
					return t.getMessage().matches(message);
				else
					return t.getMessage().startsWith(message);
			}
		}
		return false;
	}

	@Override
	public String getKnownBugDescription() {
		return desc;
	}

}
