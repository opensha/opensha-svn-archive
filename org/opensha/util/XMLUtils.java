package org.opensha.util;

import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

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

}
