package org.opensha.util;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.opensha.metadata.XMLSaveable;

public class XMLUtils {
	
	public static String DEFAULT_ROOT_NAME="OpenSHA";
	
	public static OutputFormat format = OutputFormat.createPrettyPrint();
	
	public static void writeDocumentToFile(String fileName, Document document) throws IOException {
		
		XMLWriter writer;
		
		writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(document);
		writer.close();
	}
	
	public static Document createDocumentWithRoot() {
		Document doc = DocumentHelper.createDocument();
		
		doc.addElement(DEFAULT_ROOT_NAME);
		
		return doc;
	}
	
	public static Document loadDocument(String path) throws MalformedURLException, DocumentException {
		SAXReader read = new SAXReader();
		
		return read.read(new File(path));
	}
	
	public static void writeObjectToXMLAsRoot(XMLSaveable obj, String fileName) throws IOException {
		Document document = createDocumentWithRoot();
		
		Element root = document.getRootElement();
		
		root = obj.toXMLMetadata(root);
		
		writeDocumentToFile(fileName, document);
	}
	
	public static void colorToXML(Element parent, Color color) {
		colorToXML(parent, color, "Color");
	}
	
	public static void colorToXML(Element parent, Color color, String elName) {
		Element el = parent.addElement(elName);
		el.addAttribute("r", color.getRed() + "");
		el.addAttribute("g", color.getGreen() + "");
		el.addAttribute("b", color.getBlue() + "");
		el.addAttribute("a", color.getAlpha() + "");
	}
	
	public static Color colorFromXML(Element colorEl) {
		int r = Integer.parseInt(colorEl.attributeValue("r"));
		int g = Integer.parseInt(colorEl.attributeValue("g"));
		int b = Integer.parseInt(colorEl.attributeValue("b"));
		int a = Integer.parseInt(colorEl.attributeValue("a"));
		
		return new Color(r, g, b, a);
	}

}
