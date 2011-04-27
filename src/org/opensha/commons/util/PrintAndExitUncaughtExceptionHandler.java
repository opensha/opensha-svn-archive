package org.opensha.commons.util;

import java.lang.Thread.UncaughtExceptionHandler;

public class PrintAndExitUncaughtExceptionHandler implements
		UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		System.err.println("PrintAndExitUncaughtExceptionHandler: Exception caught...exiting!");
		System.exit(1);
	}

}
