package org.opensha.commons.data.siteData;

/**
 * This represents a single site data value, along with metadata describing it's
 * type and source. It is returned by the SiteDataAPI.getAnnotatedValue method. 
 * 
 * @author Kevin
 *
 * @param <Element>
 */
public class SiteDataValue<Element> {
	
	private String dataType;
	private String dataMeasurementType;
	private Element value;
	private String sourceName = null;

	public SiteDataValue(String dataType, String dataMeasurementType, Element value) {
		this(dataType, dataMeasurementType, value, null);
	}
	
	public SiteDataValue(String dataType, String dataMeasurementType, Element value, String sourceName) {
		this.dataType = dataType;
		this.dataMeasurementType = dataMeasurementType;
		this.value = value;
		this.sourceName = sourceName;
	}
	
	public String getDataType() {
		return dataType;
	}

	public String getDataMeasurementType() {
		return dataMeasurementType;
	}

	public Element getValue() {
		return value;
	}
	
	public String getSourceName() {
		return sourceName;
	}

	@Override
	public String toString() {
		String str = "Type: " + dataType + ", Measurement Type: " + dataMeasurementType + ", Value: " + value;
		if (sourceName != null)
			str += ", Source: " + sourceName;
		return str;
	}
}
