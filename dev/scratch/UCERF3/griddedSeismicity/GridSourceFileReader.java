package scratch.UCERF3.griddedSeismicity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.function.AbstractDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class GridSourceFileReader extends AbstractGridSourceProvider implements XMLSaveable {
	
	private static final String NODE_MFD_LIST_EL_NAME = "MFDNodeList";
	private static final String NODE_MFD_ITEM_EL_NAME = "MFDNode";
	private static final String SUB_SIZE_MFD_EL_NAME = "SubSeisMFD";
	private static final String UNASSOCIATED_MFD_EL_NAME = "UnassociatedFD";
	
	private GriddedRegion region;
	private Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs;
	private Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs;
	
	public GridSourceFileReader(GriddedRegion region,
			Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs,
			Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs) {
		this.region = region;
		this.nodeSubSeisMFDs = nodeSubSeisMFDs;
		this.nodeUnassociatedMFDs = nodeUnassociatedMFDs;
	}
	
	@Override
	public IncrementalMagFreqDist getNodeUnassociatedMFD(int idx) {
		return nodeUnassociatedMFDs.get(idx);
	}

	@Override
	public IncrementalMagFreqDist getNodeSubSeisMFD(int idx) {
		return nodeSubSeisMFDs.get(idx);
	}

	@Override
	public GriddedRegion getGriddedRegion() {
		return region;
	}

	@Override
	public Element toXMLMetadata(Element root) {
		region.toXMLMetadata(root);
		
		Element nodeListEl = root.addElement(NODE_MFD_LIST_EL_NAME);
		nodeListEl.addAttribute("num", region.getNumLocations()+"");
		for (int i=0; i<region.getNumLocations(); i++) {
			Element nodeEl = nodeListEl.addElement(NODE_MFD_ITEM_EL_NAME);
			nodeEl.addAttribute("index", i+"");
			
			IncrementalMagFreqDist subSeisMFD = nodeSubSeisMFDs.get(i);
			IncrementalMagFreqDist unassociatedMFD = nodeSubSeisMFDs.get(i);
			
			if (subSeisMFD != null)
				subSeisMFD.toXMLMetadata(nodeEl, SUB_SIZE_MFD_EL_NAME);
			if (unassociatedMFD != null)
				unassociatedMFD.toXMLMetadata(nodeEl, UNASSOCIATED_MFD_EL_NAME);
		}
		
		return root;
	}
	
	/**
	 * This writes gridded seismicity MFDs to the given XML file
	 * @param file
	 * @throws IOException
	 */
	public void writeGriddedSeisFile(File file) throws IOException {
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		
		toXMLMetadata(root);
		
		XMLUtils.writeDocumentToFile(file, doc);
	}
	
	/**
	 * This writes gridded seismicity MFDs to the given XML file
	 * @param file
	 * @param region
	 * @param nodeSubSeisMFDs
	 * @param nodeUnassociatedMFDs
	 * @throws IOException
	 */
	public static void writeGriddedSeisFile(File file, GridSourceProvider gridProv) throws IOException {
		GridSourceFileReader fileBased;
		
		if (gridProv instanceof GridSourceFileReader) {
			fileBased = (GridSourceFileReader)gridProv;
		} else {
			GriddedRegion region = gridProv.getGriddedRegion();
			Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = Maps.newHashMap();
			Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = Maps.newHashMap();
			
			for (int i=0; i<region.getNumLocations(); i++) {
				nodeSubSeisMFDs.put(i, gridProv.getNodeSubSeisMFD(i));
				nodeUnassociatedMFDs.put(i, gridProv.getNodeUnassociatedMFD(i));
			}
			
			fileBased = new GridSourceFileReader(region, nodeSubSeisMFDs, nodeUnassociatedMFDs);
		}
		
		fileBased.writeGriddedSeisFile(file);
	}
	
	/**
	 * Loads grid sources from the given file
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static GridSourceFileReader fromFile(File file) throws IOException, DocumentException {
		Document doc = XMLUtils.loadDocument(file);
		return fromXMLMetadata(doc.getRootElement());
	}
	
	/**
	 * Loads grid sources from the given input stream
	 * @param is
	 * @return
	 * @throws DocumentException
	 */
	public static GridSourceFileReader fromInputStream(InputStream is) throws DocumentException {
		Document doc = XMLUtils.loadDocument(is);
		return fromXMLMetadata(doc.getRootElement());
	}
	
	/**
	 * Loads grid sources from the given XML root element
	 * @param root
	 * @return
	 */
	public static GridSourceFileReader fromXMLMetadata(Element root) {
		Element regionEl = root.element(GriddedRegion.XML_METADATA_NAME);
		
		GriddedRegion region = GriddedRegion.fromXMLMetadata(regionEl);
		
		Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = Maps.newHashMap();
		Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = Maps.newHashMap();
		
		Element nodeListEl = root.element(NODE_MFD_LIST_EL_NAME);
		int numNodes = Integer.parseInt(nodeListEl.attributeValue("num"));
		
		Iterator<Element> nodeElIt = nodeListEl.elementIterator(NODE_MFD_ITEM_EL_NAME);
		
		while (nodeElIt.hasNext()) {
			Element nodeEl = nodeElIt.next();
			
			int index = Integer.parseInt(nodeEl.attributeValue("index"));
			
			nodeSubSeisMFDs.put(index, loadMFD(root.element(SUB_SIZE_MFD_EL_NAME)));
			nodeUnassociatedMFDs.put(index, loadMFD(root.element(UNASSOCIATED_MFD_EL_NAME)));
		}
		
		Preconditions.checkState(nodeSubSeisMFDs.size() == numNodes, "Num MFDs inconsistant with number listed in XML file");
		
		return new GridSourceFileReader(region, nodeSubSeisMFDs, nodeUnassociatedMFDs);
	}
	
	private static IncrementalMagFreqDist loadMFD(Element funcEl) {
		if (funcEl == null)
			return null;
		
		EvenlyDiscretizedFunc func =
				(EvenlyDiscretizedFunc)AbstractDiscretizedFunc.fromXMLMetadata(funcEl);
		
		IncrementalMagFreqDist mfd = new IncrementalMagFreqDist(func.getMinX(), func.getNum(), func.getDelta());
		mfd.setInfo(func.getInfo());
		mfd.setName(func.getName());
		mfd.setXAxisName(func.getXAxisName());
		mfd.setYAxisName(func.getYAxisName());
		for (int i=0; i<func.getNum(); i++)
			mfd.set(i, func.getY(i));
		
		return mfd;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		File fssFile = new File("/tmp/FM3_1_ZENGBB_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_" +
				"NoFix_SpatSeisU3_VarPaleo0.6_VarSmoothPaleoSect1000_VarSectNuclMFDWt0.01_sol.zip");
		InversionFaultSystemSolution ivfss = new InversionFaultSystemSolution(SimpleFaultSystemSolution.fromFile(fssFile));
		UCERF3_GridSourceGenerator srcGen = new UCERF3_GridSourceGenerator(ivfss);
		
		GriddedRegion region = srcGen.getGriddedRegion();
		
		Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = Maps.newHashMap();
		Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = Maps.newHashMap();
		for (int i=0; i<region.getNumLocations(); i++) {
			nodeSubSeisMFDs.put(i, srcGen.getNodeSubSeisMFD(i));
			nodeUnassociatedMFDs.put(i, srcGen.getNodeUnassociatedMFD(i));
		}
		File gridSourcesFile = new File("/tmp/grid_sources.xml");
		System.out.println("Saving");
		writeGriddedSeisFile(gridSourcesFile, srcGen);
		
		System.out.println("Loading");
		fromFile(gridSourcesFile);
		System.out.println("DONE");
	}

}
