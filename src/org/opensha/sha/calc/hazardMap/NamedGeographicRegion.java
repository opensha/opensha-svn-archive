package org.opensha.sha.calc.hazardMap;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.region.BorderType;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.region.Region;
import org.opensha.commons.util.XMLUtils;

// TODO this is in an odd location and shouldn't necessarily exits.
// Region or GriddedRegion should implement Named interface
public class NamedGeographicRegion extends Region implements NamedObjectAPI {

	private String name;

	public NamedGeographicRegion(LocationList outline, String name) {
		super(outline, BorderType.MERCATOR_LINEAR);

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Element toXMLMetadata(Element root) {
		Region region = new Region(this.getBorder(), BorderType.MERCATOR_LINEAR);
		
		root = region.toXMLMetadata(root);
		
		Element regionEl = root.element(XML_METADATA_NAME);
		
		regionEl.addAttribute("name", name);

		return root;
	}

	public static NamedGeographicRegion fromXMLMetadata(Element geographicElement) {
		Region region = Region.fromXMLMetadata(geographicElement);
		
		String name = geographicElement.attributeValue("name");
		
		return new NamedGeographicRegion(region.getBorder(), name);
	}
	
	public static void main(String args[]) {
		NamedGeographicRegion region = new NamedGeographicRegion(
				new CaliforniaRegions.RELM_TESTING().getBorder(), "Relm!");
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		region.toXMLMetadata(root);
		try {
		XMLUtils.writeDocumentToFile("region.xml", doc);
		} catch( IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
