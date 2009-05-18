package org.opensha.sha.cybershake.openshaAPIs;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.util.XMLUtils;

public class CyberShakeUCERFWrapper_ERF extends EqkRupForecast {
	
	public static final String ERF_XML_FILE = "org/opensha/cybershake/conf/MeanUCERF.xml";
	
	public static final int ERF_ID = 35;
	
	private EqkRupForecast erf = null;
	
	public CyberShakeUCERFWrapper_ERF() {
		super();
	}
	
	private EqkRupForecast getERF() {
		if (erf == null) {
			try {
				Document doc = XMLUtils.loadDocument(ERF_XML_FILE);
				Element root = doc.getRootElement();
				Element erfEl = root.element(XML_METADATA_NAME);
				erf = EqkRupForecast.fromXMLMetadata(erfEl);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return erf;
	}

	public int getNumSources() {
		// TODO Auto-generated method stub
		return getERF().getNumSources();
	}

	public ProbEqkSource getSource(int sourceID) {
		ProbEqkSource source = getERF().getSource(sourceID);
		
		CyberShakeProbEqkSource csSource = new CyberShakeProbEqkSource(source, sourceID, ERF_ID);
		
		return csSource;
	}

	public ArrayList getSourceList() {
		// TODO Auto-generated method stub
		return getERF().getSourceList();
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "CyberShake UCERF 2 Wrapper";
	}

	public void updateForecast() {
		getERF().updateForecast();
	}

}
