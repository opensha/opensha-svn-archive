package org.opensha.sha.nshmp;

import org.apache.commons.lang3.StringUtils;

/**
 * Identifier for different earthquake source types.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum SourceType {

	FAULT, 
	GRIDDED,
	AREA,
	SUBDUCTION,
	CLUSTER;
	
	public String paramLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append("Enable ");
		sb.append(StringUtils.capitalize(toString().toLowerCase()));
		sb.append(" Sources");
		return sb.toString();
	}
	
}
