package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.util.MathUtils;
import org.opensha.sha.nshmp.SourceRegion;
import org.opensha.sha.nshmp.SourceType;

/**
 * Wrapper for NSHMP source files
 */
class SourceFile {

	private SourceRegion region;
	private SourceType type;
	private File file;
	private double weight;

	SourceFile(SourceRegion region, SourceType type, File file, double weight) {
		this.region = region;
		this.type = type;
		this.file = file;
		this.weight = weight;
	}

	@Override
	public String toString() {
		return new StringBuffer(StringUtils.rightPad(region.toString(), 24))
			.append(StringUtils.rightPad(type.toString(), 12))
			.append(
				StringUtils.rightPad(
					new Double(MathUtils.round(weight, 7)).toString(), 11))
			.append(file.getName()).toString();
	}

	SourceRegion getRegion() {
		return region;
	}

	SourceType getType() {
		return type;
	}

	File getFile() {
		return file;
	}

	String getName() {
		return file.getName();
	}

	String getPath() {
		return file.getPath();
	}

	double getWeight() {
		return weight;
	}

}
