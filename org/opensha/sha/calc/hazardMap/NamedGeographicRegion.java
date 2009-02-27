package org.opensha.sha.calc.hazardMap;

import org.dom4j.Element;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;

public class NamedGeographicRegion extends GeographicRegion {

	private String name;

	public NamedGeographicRegion(LocationList outline, String name) {
		super(outline);

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Element toXMLMetadata(Element root) {
		GeographicRegion region = new GeographicRegion(this.getRegionOutline());
		
		root = region.toXMLMetadata(root);
		
		Element regionEl = root.element(XML_METADATA_NAME);
		
		regionEl.addAttribute("name", name);

		return root;
	}

	public static NamedGeographicRegion fromXMLMetadata(Element geographicElement) {
		GeographicRegion region = GeographicRegion.fromXMLMetadata(geographicElement);
		
		String name = geographicElement.attributeValue("name");
		
		return new NamedGeographicRegion(region.getRegionOutline(), name);
	}
}
