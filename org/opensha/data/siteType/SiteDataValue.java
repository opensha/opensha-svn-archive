package org.opensha.data.siteType;

/**
 * This represents a single site data value, along with metadata describing it's
 * type and source. It is returned by the SiteDataAPI.getAnnotatedValue method. 
 * 
 * @author Kevin
 *
 * @param <Element>
 */
public class SiteDataValue<Element> {
	
	private String type;
	private String flag;
	private Element value;
	private String sourceName = null;

	public SiteDataValue(String type, String flag, Element value) {
		this(type, flag, value, null);
	}
	
	public SiteDataValue(String type, String flag, Element value, String sourceName) {
		this.type = type;
		this.flag = flag;
		this.value = value;
		this.sourceName = sourceName;
	}
	
	public String getDataType() {
		return type;
	}

	public String getDataMeasurementType() {
		return flag;
	}

	public Element getValue() {
		return value;
	}
	
	public String getSourceName() {
		return sourceName;
	}

	@Override
	public String toString() {
		String str = "Type: " + type + ", Flag: " + flag + ", Value: " + value;
		if (sourceName != null)
			str += ", Source: " + sourceName;
		return str;
	}
}
