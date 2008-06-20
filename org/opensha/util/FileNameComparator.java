package org.opensha.util;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

public class FileNameComparator implements Comparator {
	// A Collator does string comparisons
	private Collator c = Collator.getInstance();
	
	/**
	 * This is called when you do Arrays.sort on an array or Collections.sort on a collection (IE ArrayList).
	 * 
	 * It assumes that the Objects given are Files, and compares their names. It doesn't know how to compare
	 * a file with a directory, and returns -1 in this case.
	 */
	public int compare(Object o1, Object o2) {
		if(o1 == o2)
			return 0;

		// cast the objects to Files.
		File f1 = (File) o1;
		File f2 = (File) o2;

		// check if it is comparing a file with a directory, and if so return -1
		if(f1.isDirectory() && f2.isFile())
			return -1;
		if(f1.isFile() && f2.isDirectory())
			return 1;


		// let the Collator do the string comparison, and return the result
		return c.compare(f1.getName(), f2.getName());
	}
}