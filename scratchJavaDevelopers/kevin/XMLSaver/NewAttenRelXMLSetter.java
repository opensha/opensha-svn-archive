package scratchJavaDevelopers.kevin.XMLSaver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import javax.swing.JDialog;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

public class NewAttenRelXMLSetter {
	
	public NewAttenRelXMLSetter() {
		
	}
	
	public static void main(String args[]) throws DocumentException, InvocationTargetException, IOException {
		AttenRelSaver saver = new AttenRelSaver();
		saver.setHideOnSave(true);
		saver.setFileName("before.xml");
		
		JDialog daig = saver.getAsDialog();
		
		daig.setModal(true);
		daig.setVisible(true);
		
		ScalarIntensityMeasureRelationshipAPI orig = saver.getSelectedAttenRel();
		
		System.out.println("**********ORIGINAL PERIOD: " + orig.getParameter(PeriodParam.NAME).getValue());
		
		Document doc = XMLUtils.loadDocument("before.xml");
		Element root = doc.getRootElement();
		
		Element imrEl = root.element(IntensityMeasureRelationship.XML_METADATA_NAME);
		
		IntensityMeasureRelationship imr = IntensityMeasureRelationship.fromXMLMetadata(imrEl, null);
		System.out.println("**********NEW PERIOD: " + imr.getParameter(PeriodParam.NAME).getValue());
		
		Document doc2 = XMLUtils.createDocumentWithRoot();
		Element root2 = doc2.getRootElement();
		
		imr.toXMLMetadata(root2);
		
		XMLUtils.writeDocumentToFile("after.xml", doc2);
		System.exit(0);
	}

}
