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
		try {
			e.printStackTrace();
			BugReport bug = new BugReport(e, null, appName, appVersion, app);
			BugReportDialog dialog = new BugReportDialog(parent, bug, false);
			if (!dialog.canIgnoreKnownBug())
				// there are some stray swing exceptions that we wish to ignore. only show if not one of those
				dialog.setVisible(true);
			else
				System.err.println("Ignoring bug as detected in uncaught handler and flagged as not critical");
		} catch (Throwable e1) {
			System.err.println("Error in exception handler!");
			e1.printStackTrace();
		}
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public ApplicationVersion getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(ApplicationVersion appVersion) {
		this.appVersion = appVersion;
	}

	public Object getApp() {
		return app;
	}

	public void setApp(Object app) {
		this.app = app;
	}

	public Component getParent() {
		return parent;
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

}
