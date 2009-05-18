package org.opensha.commons.gridComputing;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.util.XMLUtils;

public abstract class GridResource implements XMLSaveable, Serializable {
	
	abstract public String getName();
	
	public void writeToFile(String fileName) throws IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(XMLUtils.DEFAULT_ROOT_NAME);
		
		root = this.toXMLMetadata(root);
		
		XMLUtils.writeDocumentToFile(fileName, document);
	}
	
	public static void main(String args[]) {
		String mainDir = "";
		
		if (args.length > 0) {
			mainDir = args[0];
			if (!mainDir.endsWith(File.separator))
				mainDir += File.separator;
		} else
			mainDir = "org/opensha/gridComputing/defaults/";
		String outDir;
		
		// write the defaults
		
		try {
			// RR
			outDir = mainDir + XMLPresetLoader.DEFAULT_RP_SUBDIR + "/";
			ResourceProvider rp = ResourceProvider.HPC();
			rp.writeToFile(outDir + "1hpc.xml");
			
			rp = ResourceProvider.DYNAMIC();
			rp.writeToFile(outDir + "3dynamic.xml");
			
			rp = ResourceProvider.ABE_NO_GLIDE_INS();
			rp.setName("Abe (NCSA/TeraGrid)");
			rp.writeToFile(outDir + "2abe.xml");
			
			rp = ResourceProvider.STEELE_GLIDE_INS();
			rp.setName("Steel (Purdue/TeraGrid)");
			rp.writeToFile(outDir + "5steele.xml");
			
			rp = ResourceProvider.STEELE_NO_GLIDE_INS();
			rp.setName("Steel WITH GLIDE INS (Purdue/TeraGrid)");
			rp.writeToFile(outDir + "6steele_glide_in.xml");
			
			// SUBMIT
			outDir = mainDir + XMLPresetLoader.DEFAULT_SUBMIT_SUBDIR + "/";
			SubmitHost submit = SubmitHost.AFTERSHOCK;
			submit.writeToFile(outDir + "aftershock.xml");
			
			// STORAGE
			outDir = mainDir + XMLPresetLoader.DEFAULT_STORAGE_SUBDIR + "/";
			StorageHost storage = StorageHost.HPC;
			storage.writeToFile(outDir + "hpc.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
