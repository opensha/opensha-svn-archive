package org.opensha.commons.util.bugReports;

import java.awt.Component;
import java.lang.Thread.UncaughtExceptionHandler;

import org.opensha.commons.util.ApplicationVersion;

public class DefaultExceptoinHandler implements UncaughtExceptionHandler {
	
	private String appName;
	private ApplicationVersion appVersion;
	private Object app;
	private Component parent;
	
	public DefaultExceptoinHandler(String appName, ApplicationVersion appVersion, Object app, Component parent) {
		this.appName = appName;
		this.appVersion = appVersion;
		this.app = app;
		this.parent = parent;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		BugReport bug = new BugReport(e, null, appName, appVersion, app);
		BugReportDialog dialog = new BugReportDialog(parent, bug, false);
		dialog.setVisible(true);
	}

}
