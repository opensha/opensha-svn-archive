package org.opensha.commons.util;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.opensha.commons.metadata.XMLSaveable;

/**
 * Static XML utility functions for creating XML documents, parsing XML files,
 * and saving XML to a file.
 * 
 * @author kevin
 *
 */
public class XMLUtils {
	
	/**
	 * Default name for document root element
	 */
	public static String DEFAULT_ROOT_NAME="OpenSHA";
	
	public static OutputFormat format = OutputFormat.createPrettyPrint();
	
	/**
	 * Writes an XML document to a file
	 * 
	 * @param fileName
	 * @param document
	 * @throws IOException
	 */
	public static void writeDocumentToFile(String fileName, Document document) throws IOException {
		
		XMLWriter writer;
		
		writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(document);
		writer.close();
	}
	
	/**
	 * Creates a new XML document with a root element.
	 * 
	 * @return
	 */
	public static Document createDocumentWithRoot() {
		Document doc = DocumentHelper.createDocument();
		
		doc.addElement(DEFAULT_ROOT_NAME);
		
		return doc;
	}
	
	/**
	 * Loads an XML document from a file path
	 * 
	 * @return XML document
	 */
	public static Document loadDocument(String path) throws MalformedURLException, DocumentException {
		SAXReader read = new SAXReader();
		
		return read.read(new File(path));
	}
	
	/**
	 * Loads an XML document from a file path
	 * 
	 * @return XML document
	 */
	public static Document loadDocument(URL url) throws MalformedURLException, DocumentException {
		SAXReader read = new SAXReader();
		
		return read.read(url);
	}
	
	/**
	 * Convenience method to write an XMLSaveable object to a file. It will be the only Element
	 * in the XML document under the default document root. 
	 * 
	 * @param obj
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeObjectToXMLAsRoot(XMLSaveable obj, String fileName) throws IOException {
		Document document = createDocumentWithRoot();
		
		Element root = document.getRootElement();
		
		root = obj.toXMLMetadata(root);
		
		writeDocumentToFile(fileName, document);
	}
	
	/**
	 * Convenience method for writing a java 'Color' object to XML with the default
	 * element name of 'Color'
	 * 
	 * @param parent
	 * @param color
	 */
	public static void colorToXML(Element parent, Color color) {
		colorToXML(parent, color, "Color");
	}
	
	/**
	 * Convenience method for writing a java 'Color' object to XML with the given
	 * element name
	 * 
	 * @param parent
	 * @param color
	 * @param elName
	 */
	public static void colorToXML(Element parent, Color color, String elName) {
		Element el = parent.addElement(elName);
		el.addAttribute("r", color.getRed() + "");
		el.addAttribute("g", color.getGreen() + "");
		el.addAttribute("b", color.getBlue() + "");
		el.addAttribute("a", color.getAlpha() + "");
	}
	
	/**
	 * Convenience method for loading a java 'Color' object from XML
	 * 
	 * @param colorEl
	 * @return
	 */
	public static Color colorFromXML(Element colorEl) {
		int r = Integer.parseInt(colorEl.attributeValue("r"));
		int g = Integer.parseInt(colorEl.attributeValue("g"));
		int b = Integer.parseInt(colorEl.attributeValue("b"));
		int a = Integer.parseInt(colorEl.attributeValue("a"));
		
		return new Color(r, g, b, a);
	}

}
