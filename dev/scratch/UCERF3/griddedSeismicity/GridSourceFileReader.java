package scratch.UCERF3.griddedSeismicity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.function.AbstractDiscretizedFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.MatrixIO;

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
			IncrementalMagFreqDist unassociatedMFD = nodeUnassociatedMFDs.get(i);
			
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
	 * This writes gridded seismicity MFDs to the given binary file. XML metadata for the region is stored
	 * in it's own xml file.
	 * @param file
	 * @param region
	 * @param nodeSubSeisMFDs
	 * @param nodeUnassociatedMFDs
	 * @throws IOException
	 */
	public static void writeGriddedSeisBinFile(File binFile, File regXMLFile, GridSourceProvider gridProv) throws IOException {
		DiscretizedFunc[] funcs = new DiscretizedFunc[gridProv.getGriddedRegion().getNodeCount()*2];
		
		for (int i=0; i<funcs.length; i+=2) {
			int id2 = i/2;
			DiscretizedFunc unMFD = gridProv.getNodeUnassociatedMFD(id2);
			DiscretizedFunc subSeisMFD = gridProv.getNodeSubSeisMFD(id2);
			if (unMFD == null)
				unMFD = new ArbitrarilyDiscretizedFunc();
			if (subSeisMFD == null)
				subSeisMFD = new ArbitrarilyDiscretizedFunc();
			funcs[i] = unMFD;
			funcs[i+1] = subSeisMFD;
		}
		
		MatrixIO.discFuncsToFile(funcs, binFile);
		
		if (regXMLFile == null)
			return;
		
		// now write gridded_region
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		
		gridProv.getGriddedRegion().toXMLMetadata(root);
		
		XMLUtils.writeDocumentToFile(regXMLFile, doc);
	}
	
	public static GridSourceFileReader fromBinFile(File binFile, File regXMLFile) throws IOException, DocumentException {
		return fromBinStreams(new BufferedInputStream(new FileInputStream(binFile)),
				new BufferedInputStream(new FileInputStream(regXMLFile)));
	}
	
	public static GridSourceFileReader fromBinStreams(InputStream binFileStream, InputStream regXMLFileStream)
			throws IOException, DocumentException {
		// load region
		Document doc = XMLUtils.loadDocument(regXMLFileStream);
		Element regionEl = doc.getRootElement().element(GriddedRegion.XML_METADATA_NAME);
		
		GriddedRegion region = GriddedRegion.fromXMLMetadata(regionEl);
		
		DiscretizedFunc[] funcs = MatrixIO.discFuncsFromInputStream(binFileStream);
		Preconditions.checkState(funcs.length == region.getNodeCount()*2);
		
		Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = Maps.newHashMap();
		Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = Maps.newHashMap();
		
		for (int i=0; i<funcs.length; i+=2) {
			int id2 = i/2;
			
			DiscretizedFunc unMFD = funcs[i];
			DiscretizedFunc subSeisMFD = funcs[i+1];
			
			if (unMFD.getNum() > 0)
				nodeUnassociatedMFDs.put(id2, asIncr(unMFD));
			if (subSeisMFD.getNum() > 0)
				nodeSubSeisMFDs.put(id2, asIncr(subSeisMFD));
		}
		
		return new GridSourceFileReader(region, nodeSubSeisMFDs, nodeUnassociatedMFDs);
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
			
			nodeSubSeisMFDs.put(index, loadMFD(nodeEl.element(SUB_SIZE_MFD_EL_NAME)));
			nodeUnassociatedMFDs.put(index, loadMFD(nodeEl.element(UNASSOCIATED_MFD_EL_NAME)));
		}
		
		Preconditions.checkState(nodeSubSeisMFDs.size() == numNodes, "Num MFDs inconsistant with number listed in XML file");
		
		return new GridSourceFileReader(region, nodeSubSeisMFDs, nodeUnassociatedMFDs);
	}
	
	private static IncrementalMagFreqDist loadMFD(Element funcEl) {
		if (funcEl == null)
			return null;
		
		EvenlyDiscretizedFunc func =
				(EvenlyDiscretizedFunc)AbstractDiscretizedFunc.fromXMLMetadata(funcEl);
		
		return asIncr(func);
	}
	
	private static IncrementalMagFreqDist asIncr(DiscretizedFunc func) {
		IncrementalMagFreqDist mfd;
		if (func instanceof EvenlyDiscretizedFunc) {
			EvenlyDiscretizedFunc eFunc = (EvenlyDiscretizedFunc)func;
			mfd = new IncrementalMagFreqDist(eFunc.getMinX(), eFunc.getNum(), eFunc.getDelta());
		} else {
			mfd = new IncrementalMagFreqDist(func.getMinX(), func.getNum(), func.getX(1) - func.getX(0));
		}
		mfd.setInfo(func.getInfo());
		mfd.setName(func.getName());
		mfd.setXAxisName(func.getXAxisName());
		mfd.setYAxisName(func.getYAxisName());
		for (int i=0; i<func.getNum(); i++)
			mfd.set(i, func.getY(i));
		
		return mfd;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
//		File fssFile = new File("/tmp/FM3_1_ZENGBB_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_" +
//				"NoFix_SpatSeisU3_VarPaleo0.6_VarSmoothPaleoSect1000_VarSectNuclMFDWt0.01_sol.zip");
		File fssFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
		InversionFaultSystemSolution ivfss = FaultSystemIO.loadInvSol(fssFile);
		GridSourceProvider sourceProv = ivfss.getGridSourceProvider();
		System.out.println("Saving");
//		File gridSourcesFile = new File("/tmp/grid_sources.xml");
//		writeGriddedSeisFile(gridSourcesFile, sourceProv);
//		
//		System.out.println("Loading");
//		GridSourceFileReader reader = fromFile(gridSourcesFile);
		
		File gridSourcesRegionFile = new File("/tmp/grid_sources_reg.xml");
		File gridSourcesBinFile = new File("/tmp/grid_sources.bin");
		writeGriddedSeisBinFile(gridSourcesBinFile, gridSourcesRegionFile, sourceProv);
		
		System.out.println("Loading");
		GridSourceFileReader reader = fromBinFile(gridSourcesBinFile, gridSourcesRegionFile);
		System.out.println("DONE");
		
		for (int i=0; i<sourceProv.getGriddedRegion().getNumLocations(); i++) {
			IncrementalMagFreqDist nodeSubSeisMFD = sourceProv.getNodeSubSeisMFD(i);
			IncrementalMagFreqDist nodeUnassociatedMFD = sourceProv.getNodeUnassociatedMFD(i);
			
			if (nodeSubSeisMFD == null) {
				Preconditions.checkState(reader.getNodeSubSeisMFD(i) == null);
			} else {
				Preconditions.checkNotNull(reader.getNodeSubSeisMFD(i), i+". Was supposed to be size "+nodeSubSeisMFD.getNum()
						+" tot "+(float)nodeSubSeisMFD.getTotalIncrRate()+", was null");
				Preconditions.checkState((float)nodeSubSeisMFD.getTotalIncrRate() ==
						(float)reader.getNodeSubSeisMFD(i).getTotalIncrRate());
			}
			if (nodeUnassociatedMFD == null) {
				Preconditions.checkState(reader.getNodeUnassociatedMFD(i) == null);
			} else {
				Preconditions.checkNotNull(reader.getNodeUnassociatedMFD(i));
				Preconditions.checkState((float)nodeUnassociatedMFD.getTotalIncrRate() ==
						(float)reader.getNodeUnassociatedMFD(i).getTotalIncrRate());
			}
		}
	}

}
