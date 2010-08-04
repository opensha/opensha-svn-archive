package org.opensha.commons.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class VersionUtils {
	
	private static URL getVersionFile() throws FileNotFoundException {
		URL url = null;
		try {
			url = new URL("file:ant/include/build.version");
			if (new File(url.toURI()).exists()) {
				return url;
			} else
				url = null;
		} catch (Throwable t) {}
		try {
			url = VersionUtils.class.getResource("/ant/include/build.version");
		} catch (Throwable t) {}
		if (url != null)
			return url;
		try {
			url = VersionUtils.class.getResource("/build.version");
		} catch (Throwable t) {}
		if (url != null)
			return url;
		throw new FileNotFoundException("Couldn't locate build version file!");
	}
	
	public static String loadBuildVersion() throws IOException {
		return loadBuildVersion(getVersionFile());
	}
	
	public static String loadBuildVersion(URL versionFile) throws IOException {
//		System.out.println("Loading version from: " + versionFile);
		int major = 0;
		int minor = 0;
		int build = 0;
		for (String line : FileUtils.loadFile(versionFile)) {
			if (line.startsWith("#"))
				continue;
			if (!line.contains("="))
				continue;
			line = line.trim();
			String[] split = line.split("=");
			if (split.length != 2) {
				System.err.println("Incorrectly formatted line: " + line);
				continue;
			}
			if (split[0].equals("major.version"))
				major = Integer.parseInt(split[1]);
			else if (split[0].equals("minor.version"))
				minor = Integer.parseInt(split[1]);
			else if (split[0].equals("build.number"))
				build = Integer.parseInt(split[1]);
			else {
				System.err.println("Unknown key: " + split[0]);
				continue;
			}
		}
		return major+"."+minor+"."+build;
	}
	
	public static void main(String args[]) throws IOException {
		System.out.println(loadBuildVersion());
	}

}
