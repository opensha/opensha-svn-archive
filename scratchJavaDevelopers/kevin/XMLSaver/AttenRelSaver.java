package scratchJavaDevelopers.kevin.XMLSaver;

import java.lang.reflect.InvocationTargetException;
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
import org.opensha.util.EmptyParameterChangeWarningListener;

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
	
	public static AttenuationRelationship LOAD_ATTEN_REL_FROM_FILE(String fileName) throws DocumentException, InvocationTargetException {
		SAXReader reader = new SAXReader();
		
		Document doc = reader.read(fileName);
		
		Element el = doc.getRootElement().element(IntensityMeasureRelationship.XML_METADATA_NAME);
		
		return (AttenuationRelationship)AttenuationRelationship.fromXMLMetadata(el, new EmptyParameterChangeWarningListener());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AttenRelSaver saver = new AttenRelSaver();
	}

	public void updateIM() {
		
	}

	public void updateSiteParams() {
		
	}

}
