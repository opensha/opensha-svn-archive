package org.opensha.sha.imr.attenRelImpl.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.gui.infoTools.AttenuationRelationshipsInstance;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

import junit.framework.TestCase;

public class XMLSaveLoadTest extends TestCase {

	private AttenuationRelationshipsInstance attenRelInst;
	
	public XMLSaveLoadTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		attenRelInst = new AttenuationRelationshipsInstance();
	}
	
	public void testIMRSaveLoad() throws InvocationTargetException {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = attenRelInst.createIMRClassInstance(null);
		
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			System.out.println("Handling '" + imr.getName() + "'");
			imr.setParamDefaults();
			imr.toXMLMetadata(root);
			Element imrElem = root.element(IntensityMeasureRelationship.XML_METADATA_NAME);
			ScalarIntensityMeasureRelationshipAPI fromXML = (ScalarIntensityMeasureRelationshipAPI)
						IntensityMeasureRelationship.fromXMLMetadata(imrElem, null);
			imrElem.detach();
			
			Iterator<ParameterAPI<?>> it = imr.getOtherParamsIterator();
			while (it.hasNext()) {
				ParameterAPI<?> origParam = it.next();
				Object origVal = origParam.getValue();
				ParameterAPI<?> newParam = fromXML.getParameter(origParam.getName());
				Object newVal = newParam.getValue();
				if (origVal == null) {
					assertNull(newVal);
				} else {
					assertTrue(origParam.getValue().equals(newParam.getValue()));
				}
			}
		}
	}

}
