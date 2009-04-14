package scratchJavaDevelopers.kevin.XMLSaver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBeanAPI;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.util.FakeParameterListener;

public class AttenRelSaver extends XMLSaver implements IMR_GuiBeanAPI {
	
	private IMR_GuiBean bean;
	
	public AttenRelSaver() {
		super();
		bean = createIMR_GUI_Bean();
		super.init();
	}
	
	private IMR_GuiBean createIMR_GUI_Bean() {
		return new IMR_GuiBean(this);
	}

	@Override
	public JPanel getPanel() {
		return bean;
	}

	@Override
	public Element getXML(Element root) {
		AttenuationRelationship attenRel = (AttenuationRelationship)bean.getSelectedIMR_Instance();
		attenRel.setIntensityMeasure(AttenuationRelationship.SA_NAME);
		
		return attenRel.toXMLMetadata(root);
	}
	
	public static AttenuationRelationship LOAD_ATTEN_REL_FROM_FILE(String fileName) throws DocumentException, InvocationTargetException, MalformedURLException {
		return LOAD_ATTEN_REL_FROM_FILE(new File(fileName).toURI().toURL());
	}
	
	public static AttenuationRelationship LOAD_ATTEN_REL_FROM_FILE(URL file) throws DocumentException, InvocationTargetException, MalformedURLException {
		SAXReader reader = new SAXReader();
		
		Document doc = reader.read(file);
		
		Element el = doc.getRootElement().element(IntensityMeasureRelationship.XML_METADATA_NAME);
		
		return (AttenuationRelationship)AttenuationRelationship.fromXMLMetadata(el, new FakeParameterListener());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AttenRelSaver();
		
//		CY_2008_AttenRel cy08 = new CY_2008_AttenRel(new FakeParameterListener());
//		
//		cy08.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
//		cy08.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
//		cy08.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(3.0));
//		
//		
	}

	public void updateIM() {
		
	}

	public void updateSiteParams() {
		
	}

}
