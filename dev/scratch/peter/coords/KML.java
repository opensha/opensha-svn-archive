package scratch.peter.coords;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.commons.data.region.EvenlyGriddedWG02_Region;
import org.opensha.commons.data.region.EvenlyGriddedWG07_LA_Box_Region;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.commons.util.XMLUtils;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 * 
 */
public class KML {
	
	//private static final String FILE_NAME = "test.kml";
	
	// style references
	private static final String BORDER_STYLE = "grid_border";
	private static final String NODE_STYLE = "grid_node";
	
	// TODO centralize commonly used constants
	private static final String NL = SystemUtils.LINE_SEPARATOR;
	
	// read document
	
	
	// write region
	public static void regionToKML(GeographicRegion region, String filename) {
		String kmlFileName = filename + ".kml";
		Document doc = DocumentHelper.createDocument();
		Element root = new DefaultElement(
				"kml", 
				new Namespace("", "http://www.opengis.net/kml/2.2"));
		doc.add(root);
		
		Element e_doc = root.addElement("Document");
		Element e_doc_name = e_doc.addElement("name");
		e_doc_name.addText(kmlFileName);
		
		addBorderStyle(e_doc);
		addNodeStyle(e_doc);
		
		Element e_folder = e_doc.addElement("Folder");
		Element e_folder_name = e_folder.addElement("name");
		e_folder_name.addText("region");
		Element e_open = e_folder.addElement("open");
		e_open.addText("1");
		
		addBorder(e_folder, region);
		
		if (region instanceof EvenlyGriddedGeographicRegion) {
			addNodes(e_folder, (EvenlyGriddedGeographicRegion) region);
		}

		String tmpFile = "/Users/pliny/Desktop/sha_kml/" + kmlFileName;
		
		try {
			//XMLUtils.writeDocumentToFile(tmpFile, doc);
			XMLWriter writer;
			OutputFormat format = new OutputFormat("\t", true);
			writer = new XMLWriter(new FileWriter(tmpFile), format);
			writer.write(doc);
			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		//Element e = new Elem
	}
	
	// border polygon
	private static Element addBorder(Element e, GeographicRegion region) {
		Element e_placemark = e.addElement("Placemark");
		Element e_name = e_placemark.addElement("name");
		e_name.addText("Border");
		Element e_style = e_placemark.addElement("styleUrl");
		e_style.addText("#" + BORDER_STYLE);
		Element e_poly = e_placemark.addElement("Polygon");
		Element e_tessellate = e_poly.addElement("tessellate");
		e_tessellate.addText("1");
		Element e_oBI = e_poly.addElement("outerBoundaryIs");
		Element e_LR = e_oBI.addElement("LinearRing");
		Element e_coord = e_LR.addElement("coordinates");
		e_coord.addText(parseBorderCoords(region));
		return e;
	}
	
	// create lat-lon data string
	private static String parseBorderCoords(GeographicRegion region) {
		LocationList ll = region.getRegionOutline();
		StringBuffer sb = new StringBuffer(NL);
		for (Location loc: ll) {
			sb.append(loc.toKML() + NL);
			System.out.println(loc.toKML()); // TODO clean
		}
		// region borders do not repeat the first
		// vertex, but kml closed polygons do
		sb.append(ll.getLocationAt(0).toKML() + NL);
		System.out.println("---"); // TODO clean
		return sb.toString();
	}
	
	// node placemarks
	private static Element addNodes(Element e, 
			EvenlyGriddedGeographicRegion region) {
		Element e_folder = e.addElement("Folder");
		Element e_folder_name = e_folder.addElement("name");
		e_folder_name.addText("nodes");
		Element e_open = e_folder.addElement("open");
		e_open.addText("0");
		// loop nodes
		LocationList ll = region.getGridLocationsList();
		for (Location loc: ll) {
			Element e_placemark = e_folder.addElement("Placemark");
			Element e_style = e_placemark.addElement("styleUrl");
			e_style.addText("#" + NODE_STYLE);
			Element e_poly = e_placemark.addElement("Point");
			Element e_coord = e_poly.addElement("coordinates");
			System.out.println(loc.toKML()); // TODO clean
			e_coord.addText(loc.toKML());
		}
		return e;
	}
	
	//<Folder>
	//	<name>region</name>
	//	<open>1</open>
	//	<Placemark>
	//		<name>test region</name>
	//		<styleUrl>#msn_ylw-pushpin</styleUrl>
	//		<Polygon>
	//			<tessellate>1</tessellate>
	//			<outerBoundaryIs>
	//				<LinearRing>
	//					<coordinates>
	//-118.494013126698,34.12890715714403,0 -118.2726369206852,34.02666906748863,0 -117.9627114364491,34.07186823617815,0 -117.9620310910423,34.2668764027905,0 -118.3264939969918,34.39919060861001,0 -118.5320559633752,34.23801999324961,0 -118.494013126698,34.12890715714403,0 </coordinates>
	//				</LinearRing>
	//			</outerBoundaryIs>
	//		</Polygon>
	//	</Placemark>
	//	<Placemark>
	//		<LookAt>
	//			<longitude>-118.247043582626</longitude>
	//			<latitude>34.21293007086929</latitude>
	//			<altitude>0</altitude>
	//			<range>60381.34272309824</range>
	//			<tilt>0</tilt>
	//			<heading>-3.115946006858405e-08</heading>
	//			<altitudeMode>relativeToGround</altitudeMode>
	//		</LookAt>
	//		<styleUrl>#msn_placemark_circle</styleUrl>
	//		<Point>
	//			<coordinates>-118.3897877691312,34.24834787236836,0</coordinates>
	//		</Point>
	//	</Placemark>
//</Folder>

	// border style elements
	private static Element addBorderStyle(Element e) {
		Element e_style = e.addElement("Style");
		e_style.addAttribute("id", BORDER_STYLE);
		
		// icon style
		Element e_iconStyle = e_style.addElement("IconStyle");
		Element e_scale = e_iconStyle.addElement("scale");
		e_scale.addText("1.1");
		Element e_icon = e_iconStyle.addElement("Icon");
		Element e_href = e_icon.addElement("href");
		e_href.addText(
			"http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
		Element e_hotSpot = e_iconStyle.addElement("hotSpot"); 
		e_hotSpot.addAttribute("x", "20");
		e_hotSpot.addAttribute("y", "2");
		e_hotSpot.addAttribute("xunits", "pixels");
		e_hotSpot.addAttribute("yunits", "pixels");
		
		// line style
		Element e_lineStyle = e_style.addElement("LineStyle");
		Element e_color = e_lineStyle.addElement("color");
		e_color.addText("ff16b4ff");
		Element e_width = e_lineStyle.addElement("width");
		e_width.addText("3");
		
		// poly style
		Element e_polyStyle = e_style.addElement("PolyStyle");
		e_polyStyle.add((Element) e_color.clone());
		Element e_fill = e_polyStyle.addElement("fill");
		e_fill.addText("0");
		
		return e;
	}
	
	// node style elements
	private static Element addNodeStyle(Element e) {
		Element e_style = e.addElement("Style");
		e_style.addAttribute("id", NODE_STYLE);
		
		// icon style
		Element e_iconStyle = e_style.addElement("IconStyle");
		Element e_color = e_iconStyle.addElement("color");
		e_color.addText("ff16b4ff");
		Element e_scale = e_iconStyle.addElement("scale");
		e_scale.addText("0.8");
		Element e_icon = e_iconStyle.addElement("Icon");
		Element e_href = e_icon.addElement("href");
		e_href.addText(
			"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
		return e;
	}
	
	
	
//	private String convertLocations(LocationList ll) {
//		
//	}

	public static void main(String[] args) {
//		KML.regionToKML(
//				DefinedRegion.RELM.getRegion(), 
//				DefinedRegion.RELM.name());
//		KML.regionToKML(
//				DefinedRegion.RELM_TESTING.getRegion(), 
//				DefinedRegion.RELM_TESTING.name());
//		KML.regionToKML(
//				DefinedRegion.RELM_COLLECTION.getRegion(), 
//				DefinedRegion.RELM_COLLECTION.name());
//		KML.regionToKML(
//				DefinedRegion.RELM_NOCAL.getRegion(), 
//				DefinedRegion.RELM_NOCAL.name());
//		KML.regionToKML(
//				DefinedRegion.WG02.getRegion(), 
//				DefinedRegion.WG02.name());
		GeographicRegion gr = new CaliforniaRegions.RELM_TESTING();
		EvenlyGriddedGeographicRegion eggr = new EvenlyGriddedGeographicRegion(gr, 0.4);
		KML.regionToKML(
				eggr, 
				"RELM");
		
//		KML.regionToKML(new EvenlyGriddedWG07_LA_Box_Region());
//		KML.regionToKML(new EvenlyGriddedWG02_Region());
	}
}
