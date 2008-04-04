package scratchJavaDevelopers.kevin;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

class FileNameComparator implements Comparator {
	private Collator c = Collator.getInstance();

	public int compare(Object o1, Object o2) {
		if(o1 == o2)
			return 0;

		File f1 = (File) o1;
		File f2 = (File) o2;

		if(f1.isDirectory() && f2.isFile())
			return -1;
		if(f1.isFile() && f2.isDirectory())
			return 1;



		return c.compare(invertFileName(f1.getName()), invertFileName(f2.getName()));
	}

	public String invertFileName(String fileName) {
		int index = fileName.indexOf("_");
		int firstIndex = fileName.indexOf(".");
		int lastIndex = fileName.lastIndexOf(".");
		// Hazard data files have 3 "." in their names
		//And leaving the rest of the files which contains only 1"." in their names
		if(firstIndex != lastIndex){

			//getting the lat and Lon values from file names
			String lat = fileName.substring(0,index).trim();
			String lon = fileName.substring(index+1,lastIndex).trim();

			return lon + "_" + lat;
		}
		return fileName;
	}
}