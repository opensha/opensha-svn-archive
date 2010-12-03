package org.opensha.commons.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CustomFileFilter extends FileFilter {

	private String extention;
	private String description;
	
	public CustomFileFilter(String extention, String description) {
		this.extention = extention;
		this.description = description;
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String fName = f.getName().toLowerCase();
		if (fName.endsWith("."+extention.toLowerCase()))
			return true;
		return false;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	public String getExtention() {
		return extention;
	}

}
