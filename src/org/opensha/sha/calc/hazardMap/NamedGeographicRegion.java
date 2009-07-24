package org.opensha.sha.calc.hazardMap;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.region.BorderType;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.commons.data.region.RELM_TestingRegion;
import org.opensha.commons.util.XMLUtils;

public class NamedGeographicRegion extends GeographicRegion implements NamedObjectAPI {

	private String name;

	public NamedGeographicRegion(LocationList outline, String name) {
		super(outline, BorderType.MERCATOR_LINEAR);

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Element toXMLMetadata(Element root) {
		GeographicRegion region = new GeographicRegion(this.getRegionOutline(), BorderType.MERCATOR_LINEAR);
		
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
	
	public static void main(String args[]) throws IOException {
		NamedGeographicRegion region = new NamedGeographicRegion(new RELM_TestingRegion().getRegionOutline(), "Relm!");
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		region.toXMLMetadata(root);
		XMLUtils.writeDocumentToFile("region.xml", doc);
	}
}
