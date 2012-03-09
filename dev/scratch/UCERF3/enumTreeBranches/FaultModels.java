package scratch.UCERF3.enumTreeBranches;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.ShortNamed;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.SimpleFaultSystemRupSet;

public enum FaultModels implements ShortNamed {

	FM2_1("Fault Model 2.1", 41),
	FM3_1("Fault Model 3.1", 101),
	FM3_2("Fault Model 3.2", 102);
	
	public static final String XML_ELEMENT_NAME = "FaultModel";
	public static final String FAULT_MODEL_STORE_PROPERTY_NAME = "FaultModelStore";
	
	private String modelName;
	private int id;
	
	private FaultModels(String modelName, int id) {
		this.modelName = modelName;
		this.id = id;
	}
	
	public String getName() {
		return modelName;
	}
	
	public String getShortName() {
		return name();
	}
	
	public int getID() {
		return id;
	}
	
	/**
	 * This returns the Deformation Model that should be used for construction of rupture sets, or null
	 * if any can be used.
	 * 
	 * @return
	 */
	public DeformationModels getFilterBasis() {
		// this has to be hard coded here because DeformationModels can't be instantiated before
		// fault models because they depend on fault models. Complicated enum order of operations
		// junk - just trust me.
		switch (this) {
		case FM3_1:
			return DeformationModels.GEOLOGIC;
		case FM3_2:
			return DeformationModels.GEOLOGIC;

		default:
			return null;
		}
	}
	
	private DB_AccessAPI getDBAccess() {
		switch (this) {
		case FM2_1:
			return DB_ConnectionPool.getDB2ReadOnlyConn();
		case FM3_1:
			return DB_ConnectionPool.getDB3ReadOnlyConn();
		case FM3_2:
			return DB_ConnectionPool.getDB3ReadOnlyConn();

		default:
			throw new IllegalStateException("DB access cannot be created for Fault Model: "+this);
		}
	}
	
	private static ArrayList<FaultSectionPrefData> loadStoredFaultSections(File fmStoreFile)
			throws MalformedURLException, DocumentException {
		System.out.println("Loading fault model from: "+fmStoreFile.getAbsolutePath());
		Document doc = XMLUtils.loadDocument(fmStoreFile);
		Element root = doc.getRootElement();
		return SimpleFaultSystemRupSet.fsDataFromXML(root.element("FaultModel"));
	}
	
	public ArrayList<FaultSectionPrefData> fetchFaultSections() {
		// this lets us load the FM from XML if we're on the cluster
		String fmStoreProp = System.getProperty("FaultModelStore");
		if (fmStoreProp != null) {
			File fmStoreFile = new File(fmStoreProp, getShortName()+".xml");
			if (fmStoreFile.exists()) {
				try {
					return loadStoredFaultSections(fmStoreFile);
				} catch (Exception e) {
					// ok to fail here, will try it the other way
					e.printStackTrace();
				}
			}
		}
		DB_AccessAPI db = getDBAccess();
		PrefFaultSectionDataDB_DAO pref2db = new PrefFaultSectionDataDB_DAO(db);
		ArrayList<FaultSectionPrefData> datas = pref2db.getAllFaultSectionPrefData();
		FaultModelDB_DAO fm2db = new FaultModelDB_DAO(db);
		ArrayList<Integer> faultSectionIds = fm2db.getFaultSectionIdList(id);

		ArrayList<FaultSectionPrefData> faultModel = new ArrayList<FaultSectionPrefData>();
		for (FaultSectionPrefData data : datas) {
			if (!faultSectionIds.contains(data.getSectionId()))
				continue;
			faultModel.add(data);
		}

		return faultModel;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
