package org.opensha.commons.data.region;

import java.io.File;
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
import org.opensha.commons.util.XMLUtils;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 * 
 */
public class RegionUtils {
	
	//private static final String FILE_NAME = "test.kml";
	
	// TODO centralize commonly used constants
	private static final String NL = SystemUtils.LINE_SEPARATOR;
	
	public enum Style {
		BORDER,
		BORDER_VERTEX,
		GRID_NODE;
	}
	
	public enum Color {
		ORANGE("FF16B4FF"),
		BLUE("FFFF8153"),
		RED("FF150CFF");
		private String hex;
		private Color(String hex) {this.hex = hex;}
		public String getHex() {return hex;} 
	}
	
	// write region
	public static void regionToKML(
			Region region, String filename, Color c) {
		String kmlFileName = filename + ".kml";
		Document doc = DocumentHelper.createDocument();
		Element root = new DefaultElement(
				"kml", 
				new Namespace("", "http://www.opengis.net/kml/2.2"));
		doc.add(root);
		
		Element e_doc = root.addElement("Document");
		Element e_doc_name = e_doc.addElement("name");
		e_doc_name.addText(kmlFileName);
		
		addBorderStyle(e_doc, c);
		addBorderVertexStyle(e_doc);
		addGridNodeStyle(e_doc, c);
		
		Element e_folder = e_doc.addElement("Folder");
		Element e_folder_name = e_folder.addElement("name");
		e_folder_name.addText("region");
		Element e_open = e_folder.addElement("open");
		e_open.addText("1");
		
		addBorder(e_folder, region);
		addPoints(e_folder, "Border Nodes", region.getRegionOutline(), 
				Style.BORDER_VERTEX);
		
		if (region instanceof EvenlyGriddedGeographicRegion) {
			addPoints(e_folder, "Grid Nodes", ((EvenlyGriddedGeographicRegion) 
					region).getGridLocationsList(), Style.GRID_NODE);
		}

		// TODO absolutely need to create seom platform specific output directory
		// that is not in project space (e.g. desktop, Decs and Settings);
		
		
		String outDirName = "sha_kml/";
		File outDir = new File(outDirName);
		outDir.mkdirs();
		String tmpFile = outDirName + kmlFileName;
		
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
	private static Element addBorder(Element e, Region region) {
		Element e_placemark = e.addElement("Placemark");
		Element e_name = e_placemark.addElement("name");
		e_name.addText("Border");
		Element e_style = e_placemark.addElement("styleUrl");
		e_style.addText("#" + Style.BORDER.toString());
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
	private static String parseBorderCoords(Region region) {
		LocationList ll = region.getRegionOutline();
		StringBuffer sb = new StringBuffer(NL);
		//System.out.println("parseBorderCoords: "); // TODO clean
		for (Location loc: ll) {
			sb.append(loc.toKML() + NL);
			//System.out.println(loc.toKML()); // TODO clean
		}
		// region borders do not repeat the first
		// vertex, but kml closed polygons do
		sb.append(ll.getLocationAt(0).toKML() + NL);
		//System.out.println("---"); // TODO clean
		return sb.toString();
	}
	
	// node placemarks
	private static Element addPoints(
			Element e, String folderName,
			LocationList locations, Style style) {
		Element e_folder = e.addElement("Folder");
		Element e_folder_name = e_folder.addElement("name");
		e_folder_name.addText(folderName);
		Element e_open = e_folder.addElement("open");
		e_open.addText("0");
		// loop nodes
		for (Location loc: locations) {
			Element e_placemark = e_folder.addElement("Placemark");
			Element e_style = e_placemark.addElement("styleUrl");
			e_style.addText("#" + style.toString());
			Element e_poly = e_placemark.addElement("Point");
			Element e_coord = e_poly.addElement("coordinates");
			//System.out.println(loc.toKML()); // TODO clean
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
	private static Element addBorderStyle(Element e, Color c) {
		Element e_style = e.addElement("Style");
		e_style.addAttribute("id", Style.BORDER.toString());
				
		// line style
		Element e_lineStyle = e_style.addElement("LineStyle");
		Element e_color = e_lineStyle.addElement("color");
		e_color.addText(c.getHex());
		Element e_width = e_lineStyle.addElement("width");
		e_width.addText("3");
		
		// poly style
		Element e_polyStyle = e_style.addElement("PolyStyle");
		e_polyStyle.add((Element) e_color.clone());
		Element e_fill = e_polyStyle.addElement("fill");
		e_fill.addText("0");
		
		return e;
	}
	
	// border vertex style elements
	private static Element addBorderVertexStyle(Element e) {
		Element e_style = e.addElement("Style");
		e_style.addAttribute("id", Style.BORDER_VERTEX.toString());
		
		// icon style
		Element e_iconStyle = e_style.addElement("IconStyle");
		Element e_color = e_iconStyle.addElement("color");
		e_color.addText(Color.RED.getHex());
		Element e_scale = e_iconStyle.addElement("scale");
		e_scale.addText("0.6");
		Element e_icon = e_iconStyle.addElement("Icon");
		Element e_href = e_icon.addElement("href");
		e_href.addText(
			"http://maps.google.com/mapfiles/kml/shapes/open-diamond.png");
		return e;
	}

	// node style elements
	private static Element addGridNodeStyle(Element e, Color c) {
		Element e_style = e.addElement("Style");
		e_style.addAttribute("id", Style.GRID_NODE.toString());
		
		// icon style
		Element e_iconStyle = e_style.addElement("IconStyle");
		Element e_color = e_iconStyle.addElement("color");
		e_color.addText(c.getHex());
		Element e_scale = e_iconStyle.addElement("scale");
		e_scale.addText("0.6");
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

		
		// visual verification tests for GeographiRegionTest
		EvenlyGriddedGeographicRegion eggr;
		
		// nocal
		eggr = new CaliforniaRegions.RELM_NOCAL_GRIDDED();
		regionToKML(eggr, "ver_NoCal_new", Color.ORANGE);
		// relm
		eggr = new CaliforniaRegions.RELM_GRIDDED();
		regionToKML(eggr, "ver_RELM_new", Color.ORANGE);
		// relm_testing
		eggr = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		regionToKML(eggr, "ver_RELM_testing_new", Color.ORANGE);
		// socal
		eggr = new CaliforniaRegions.RELM_SOCAL_GRIDDED();
		regionToKML(eggr, "ver_SoCal_new", Color.ORANGE);
		// wg02
		eggr = new CaliforniaRegions.WG02_GRIDDED();
		regionToKML(eggr, "ver_WG02_new", Color.ORANGE);
		// wg07
		eggr = new CaliforniaRegions.WG07_GRIDDED();
		regionToKML(eggr, "ver_WG07_new", Color.ORANGE);
		// relm_collect
		eggr = new CaliforniaRegions.RELM_COLLECTION_GRIDDED();
		regionToKML(eggr, "ver_RELM_collect_new", Color.ORANGE);

//		// visual verification tests for GeographiRegionTest
//		Region gr;
//		
//		Location L1 = new Location(32,112);
//		Location L3 = new Location(34,118);
//		gr = new Region(L1,L3);
//		RegionUtils.regionToKML(gr, "RegionLocLoc", Color.ORANGE);
//		
		
//		EvenlyGriddedGeographicRegion rect_gr = 
//			new EvenlyGriddedGeographicRegion(
//					new Location(40.0,-113),
//					new Location(42.0,-117),
//					0.2,null);
//		KML.regionToKML(
//				rect_gr,
//				"RECT_REGION2",
//				Color.ORANGE);
		
//		EvenlyGriddedGeographicRegion relm_gr = new CaliforniaRegions.RELM_TESTING_GRIDDED();
//		KML.regionToKML(
//				relm_gr,
//				"RELM_TESTanchor",
//				Color.ORANGE);

//		System.out.println(relm_gr.getMinLat());
//		System.out.println(relm_gr.getMinGridLat());
//		System.out.println(relm_gr.getMinLon());
//		System.out.println(relm_gr.getMinGridLon());
//		System.out.println(relm_gr.getMaxLat());
//		System.out.println(relm_gr.getMaxGridLat());
//		System.out.println(relm_gr.getMaxLon());
//		System.out.println(relm_gr.getMaxGridLon());

//		EvenlyGriddedGeographicRegion eggr1 = new CaliforniaRegions.WG02_GRIDDED();
//		KML.regionToKML(
//				eggr1, 
//				"WG02anchor",
//				Color.ORANGE);

//		EvenlyGriddedGeographicRegion eggr2 = new CaliforniaRegions.WG07_GRIDDED();
//		KML.regionToKML(
//				eggr2, 
//				"WG07anchor",
//				Color.ORANGE);
		
		// TODO test that borders for diff constructors end up the same.
		
		
		// test mercator/great-circle region
//		EvenlyGriddedGeographicRegion eggr3 = new EvenlyGriddedGeographicRegion(
//				new Location(35,-125),
//				new Location(45,-90),
//				0.5);
//		KML.regionToKML(
//				eggr3, 
//				"TEST1_box",
//				Color.ORANGE);
		
// SAUSAGE
//		LocationList ll = new LocationList();
//		ll.addLocation(new Location(35,-125));
//		ll.addLocation(new Location(38,-117));
//		ll.addLocation(new Location(37,-109));
//		ll.addLocation(new Location(41,-95));
//		
//		EvenlyGriddedGeographicRegion sausage = 
//			new EvenlyGriddedGeographicRegion(ll,100,0.5,null);
//		KML.regionToKML(
//				sausage,
//				"Sausage",
//				Color.ORANGE);
//
//		EvenlyGriddedGeographicRegion sausageAnchor = 
//			new EvenlyGriddedGeographicRegion(ll,100,0.5,new Location(0,0));
//		KML.regionToKML(
//				sausageAnchor,
//				"SausageAnchor",
//				Color.BLUE);

		
// CIRCLE
//		Location loc = new Location(35, -125);
//		EvenlyGriddedGeographicRegion circle =
//				new EvenlyGriddedGeographicRegion(loc, 400, 0.2, null);
//		KML.regionToKML(circle, "Circle", Color.ORANGE);
//
//		EvenlyGriddedGeographicRegion circleAnchor =
//				new EvenlyGriddedGeographicRegion(loc, 400, 0.2, new Location(0,0));
//		KML.regionToKML(circleAnchor, "CircleAnchor", Color.BLUE);

//		
//		EvenlyGriddedGeographicRegion eggr4 = new EvenlyGriddedGeographicRegion(
//				ll,BorderType.MERCATOR_LINEAR,0.5);
//		KML.regionToKML(
//				eggr4, 
//				"TEST1_loclist_lin",
//				Color.ORANGE);
//		
//		
//		EvenlyGriddedGeographicRegion eggr5 = new EvenlyGriddedGeographicRegion(
//				ll,BorderType.GREAT_CIRCLE,0.5);
//		KML.regionToKML(
//				eggr5, 
//				"TEST1_loclist_gc",
//				Color.ORANGE);
		

	}
}
