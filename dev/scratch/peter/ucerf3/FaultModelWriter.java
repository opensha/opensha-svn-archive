package scratch.peter.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.collect.Lists;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.FaultModels;

/*
 * Used to write out fault models for local caching
 */
class FaultModelWriter {

	public static void writeFaultModel() {
		try {
			File dir = new File("tmp");
			FaultModels fm = FaultModels.FM3_2;
			ArrayList<FaultSectionPrefData> datas = fm.fetchFaultSections();
			Document doc = XMLUtils.createDocumentWithRoot();
			SimpleFaultSystemRupSet.fsDataToXML(doc.getRootElement(), datas,
				FaultModels.XML_ELEMENT_NAME, fm, null);
			XMLUtils.writeDocumentToFile(new File(dir, fm.getShortName() +
				".xml"), doc);
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		writeFaultModel();
		
//		double[] dd = { 1,2,3,4,5,6,7,8};
//		List<Double> dList = Lists.newArrayList();
//		for (double d : dd) {
//			dList.add(d);
//		}
//		Iterator<Double> it = dList.iterator();
//		while (it.hasNext()) {
//			Double D = it.next();
//			System.out.println(D + " " +D.equals(3.0));
//			if (D.equals(3.0)) it.remove();
//		}
//		System.out.println(dList);
	}

}
