/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.opensha.data.Location;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.fault.FaultTrace;
/**
 * <p>Title: PrefFaultSectionDataFinal.java </p>
 * <p>Description: This class reads the Preferred Fault Section Data from an XML file.
 * @author Ned Field
 * @version 1.0
 *
 */
public class PrefFaultSectionDataFinal {
	private static ArrayList<FaultSectionPrefData> faultSectionsList;
	private static HashMap indexForID_Map;
	
	private static final String XML_DATA_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/data/finalReferenceFaultParamDb/PrefFaultSectionData.xml";
	
	public PrefFaultSectionDataFinal() {
//		writeFaultSectionDataFromDatabaseTo_XML();
		readFaultSectionDataFromXML();
		
	}


	private void writeFaultSectionDataFromDatabaseTo_XML() {
		PrefFaultSectionDataDB_DAO faultSectionDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
		ArrayList faultSectionDataListFromDatabase = faultSectionDAO.getAllFaultSectionPrefData();
		
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement( "PrefFaultSectionData" );
		
		// make the index to ID hashmap
		indexForID_Map = new HashMap();
		FaultSectionPrefData fspd;
		for(int i=0; i<faultSectionDataListFromDatabase.size(); i++) {
			fspd = (FaultSectionPrefData) faultSectionDataListFromDatabase.get(i);
			
			root = fspd.toXMLMetadata(root);
			
			indexForID_Map.put(fspd.getSectionId(), new Integer(i));
//			System.out.println(fspd.getSectionId()+"\t"+fspd.getSectionName());
		}
		
		// save each fault section to an XML file (save all elements that have an associated set method in FaultSectionPrefData) 
		
		XMLWriter writer;


		try {
			OutputFormat format = OutputFormat.createPrettyPrint();

			System.out.println("Writing Pref Fault Section Data to " + XML_DATA_FILENAME);
			writer = new XMLWriter(new FileWriter(XML_DATA_FILENAME), format);
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// need the following until the read* method is implemented
		faultSectionsList = faultSectionDataListFromDatabase;
	}
	
	/**
	 * This reads the XML file and populates faultSectionsList 
	 */
	private void readFaultSectionDataFromXML() {
		
		SAXReader reader = new SAXReader();
		faultSectionsList = new ArrayList<FaultSectionPrefData>();
		indexForID_Map = new HashMap();
        try {
			Document document = reader.read(new File(XML_DATA_FILENAME));
			Element root = document.getRootElement();
			
			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element el = it.next();

				FaultSectionPrefData data;
				try {
					data = FaultSectionPrefData.fromXMLMetadata(el);
					faultSectionsList.add(data);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
			for (int i=0; i<faultSectionsList.size(); i++) {
				FaultSectionPrefData fspd = faultSectionsList.get(i);
				indexForID_Map.put(fspd.getSectionId(), new Integer(i));
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a list of all Fault Section Pref Data from the database
	 * @return
	 */
	public ArrayList getAllFaultSectionPrefData() {
		return faultSectionsList;
	}
	
	/**
	 * Get Preferred fault section data for a Fault Section Id
	 * @param faultSectionId
	 * @return
	 */
	public FaultSectionPrefData getFaultSectionPrefData(int faultSectionId) {
		int index = ((Integer)indexForID_Map.get(faultSectionId)).intValue();
		return faultSectionsList.get(index);
	}
	
	public static void main(String[] args) {
		PrefFaultSectionDataFinal test = new PrefFaultSectionDataFinal();
		ArrayList junk = test.getAllFaultSectionPrefData();
		FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData) junk.get(5);
		int id = faultSectionPrefData.getSectionId();
		System.out.println(id);
		FaultSectionPrefData faultSectionPrefData2 = test.getFaultSectionPrefData(id);
		System.out.println(faultSectionPrefData2.getSectionId());
		
	}

}
