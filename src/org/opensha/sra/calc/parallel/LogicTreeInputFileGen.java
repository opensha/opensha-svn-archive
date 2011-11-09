package org.opensha.sra.calc.parallel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;

import com.google.common.base.Preconditions;

public class LogicTreeInputFileGen {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvocationTargetException 
	 */
	public static void main(String[] args) throws IOException, InvocationTargetException {
		ScalarIMR imr = AttenRelRef.CB_2008.instance(null);
		imr.setParamDefaults();
		
		File dir = new File("/tmp/inputs");
		dir.mkdir();
		
		AbstractEpistemicListERF erfList = new UCERF2_TimeDependentEpistemicList();
		
		for (int i=0; i<erfList.getNumERFs(); i++) {
//			AbstractERF erf = (AbstractERF) erfList.getERF(i);
			
			Document doc = XMLUtils.createDocumentWithRoot();
			Element root = doc.getRootElement();
			
			erfList.toXMLMetadata(root, i);
			imr.toXMLMetadata(root);
			
			String numStr = i+"";
			while (numStr.length() < (erfList.getNumERFs()+"").length())
				numStr = "0"+numStr;
			
			String name = "inputs_"+imr.getShortName()+"_"+numStr+".xml";
			XMLUtils.writeDocumentToFile(new File(dir, name), doc);
			
//			AbstractERF fileERF = AbstractERF.fromXMLMetadata(root.element(AbstractERF.XML_METADATA_NAME));
//			
//			System.out.println("Checking: "+name);
//			for (Parameter<?> param : erf.getAdjustableParameterList()) {
//				Parameter<?> fileParam = fileERF.getAdjustableParameterList().getParameter(param.getName());
//				Preconditions.checkState(param.getValue().equals(fileParam.getValue()));
//				
//				Iterator<Parameter<?>> it = param.getIndependentParametersIterator();
//				while (it.hasNext()) {
//					Parameter<?> indep = it.next();
//					Parameter<?> fileIndep = fileParam.getIndependentParameter(indep.getName());
//					
//					Preconditions.checkState(indep.getValue().equals(fileIndep.getValue()));
//				}
//			}
		}
	}

}
